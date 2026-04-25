package com.rabbiter.oes.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.SparkConfig;
import com.rabbiter.oes.util.SparkApiUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 诊断用WebSocket客户端 - 测试不同协议的连接情况
 */
@Component
public class DiagnosticWebSocketClient {
    
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * 诊断WebSocket连接问题
     */
    public String generateContent(SparkConfig config, String systemPrompt, String userPrompt) {
        
        // 首先诊断Java环境
        diagnoseEnvironment();
        
        // 测试不同的连接方式
        String[] protocols = {"ws://", "wss://"};
        
        for (String protocol : protocols) {
            try {
                System.out.println("\n🧪 测试协议: " + protocol);
                String testUrl = config.getHostUrl().replace("ws://", protocol).replace("wss://", protocol);
                System.out.println("📡 测试URL: " + testUrl);
                
                SparkConfig testConfig = new SparkConfig(config.getAppId(), config.getApiSecret(), config.getApiKey(), testUrl, config.getDomain());
                
                String result = attemptConnection(testConfig, systemPrompt, userPrompt);
                if (result != null) {
                    System.out.println("✅ 协议 " + protocol + " 连接成功！");
                    return result;
                }
                
            } catch (Exception e) {
                System.err.println("❌ 协议 " + protocol + " 失败: " + e.getMessage());
            }
        }
        
        throw new RuntimeException("所有协议测试失败，请检查网络环境和Java配置");
    }
    
    /**
     * 诊断Java环境
     */
    private void diagnoseEnvironment() {
        System.out.println("\n🔍 Java环境诊断:");
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("Java供应商: " + System.getProperty("java.vendor"));
        System.out.println("操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        
        // 检查SSL支持
        try {
            String[] supportedProtocols = javax.net.ssl.SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
            System.out.println("支持的SSL协议: " + Arrays.toString(supportedProtocols));
        } catch (Exception e) {
            System.err.println("SSL检查失败: " + e.getMessage());
        }
        
        // 检查网络属性
        System.out.println("网络代理设置:");
        System.out.println("  http.proxyHost: " + System.getProperty("http.proxyHost", "未设置"));
        System.out.println("  https.proxyHost: " + System.getProperty("https.proxyHost", "未设置"));
        System.out.println("  java.net.useSystemProxies: " + System.getProperty("java.net.useSystemProxies", "未设置"));
    }
    
    /**
     * 尝试建立连接
     */
    private String attemptConnection(SparkConfig config, String systemPrompt, String userPrompt) {
        try {
            // 使用SparkApiUtils创建WebSocket URI（避免unknown protocol错误）
            URI uri = SparkApiUtils.createWebSocketURI(config.getHostUrl(), config.getApiKey(), config.getApiSecret());
            StringBuilder responseContent = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            
            System.out.println("🔗 使用SparkApiUtils创建WebSocket URI成功");
            System.out.println("📍 URI详情: " + uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + uri.getPath());
            
            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("✅ WebSocket连接已建立");
                    System.out.println("服务器握手状态: " + handshake.getHttpStatus());
                    try {
                        JSONObject request = buildRequest(config, systemPrompt, userPrompt);
                        send(request.toString());
                        System.out.println("📤 请求已发送");
                    } catch (Exception e) {
                        System.err.println("❌ 发送请求失败: " + e.getMessage());
                        latch.countDown();
                    }
                }
                
                @Override
                public void onMessage(String message) {
                    try {
                        System.out.println("📥 收到响应: " + message.substring(0, Math.min(200, message.length())) + "...");
                        
                        JSONObject response = JSON.parseObject(message);
                        JSONObject header = response.getJSONObject("header");
                        
                        if (header != null && header.getInteger("code") != 0) {
                            System.err.println("❌ API错误: " + header.getString("message"));
                            latch.countDown();
                            return;
                        }
                        
                        JSONObject payload = response.getJSONObject("payload");
                        if (payload != null) {
                            JSONObject choices = payload.getJSONObject("choices");
                            if (choices != null) {
                                JSONArray textArray = choices.getJSONArray("text");
                                if (textArray != null && textArray.size() > 0) {
                                    String content = textArray.getJSONObject(0).getString("content");
                                    responseContent.append(content);
                                    System.out.println("📝 收到内容: " + content);
                                }
                            }
                        }
                        
                        // 检查是否结束
                        if (header != null && header.getInteger("status") == 2) {
                            System.out.println("✅ 响应完成");
                            close();
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        System.err.println("❌ 解析响应失败: " + e.getMessage());
                        latch.countDown();
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔒 连接关闭: " + code + " - " + reason + " (远程关闭: " + remote + ")");
                    latch.countDown();
                }
                
                @Override
                public void onError(Exception ex) {
                    System.err.println("❌ 连接错误: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                    ex.printStackTrace();
                    latch.countDown();
                }
            };
            
            // 设置连接超时
            client.setConnectionLostTimeout(TIMEOUT_SECONDS);
            
            // 尝试连接
            System.out.println("🚀 开始建立连接...");
            boolean connected = client.connectBlocking(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!connected) {
                throw new RuntimeException("连接超时");
            }
            
            // 等待响应
            if (latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                String result = responseContent.toString();
                if (result.isEmpty()) {
                    throw new RuntimeException("收到空响应");
                }
                return result;
            } else {
                client.close();
                throw new RuntimeException("响应超时");
            }
            
        } catch (Exception e) {
            System.err.println("连接失败详情: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e.getMessage().contains("unknown protocol")) {
                System.err.println("💡 提示: WebSocket协议解析问题已通过SparkApiUtils解决");
            }
            throw new RuntimeException("连接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建请求JSON
     */
    private JSONObject buildRequest(SparkConfig config, String systemPrompt, String userPrompt) {
        JSONObject request = new JSONObject();
        
        // header
        JSONObject header = new JSONObject();
        header.put("app_id", config.getAppId());
        header.put("uid", UUID.randomUUID().toString());
        request.put("header", header);
        
        // parameter
        JSONObject parameter = new JSONObject();
        JSONObject chat = new JSONObject();
        chat.put("domain", config.getDomain());
        chat.put("temperature", 0.7);
        chat.put("max_tokens", 1024);
        parameter.put("chat", chat);
        request.put("parameter", parameter);
        
        // payload
        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();
        JSONArray text = new JSONArray();
        
        // 简化测试：只发送用户消息
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "你好");  // 简化测试消息
        text.add(userMsg);
        
        message.put("text", text);
        payload.put("message", message);
        request.put("payload", payload);
        
        return request;
    }
    
}
