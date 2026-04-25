package com.rabbiter.oes.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.SparkConfig;
import org.apache.commons.codec.binary.Base64;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 改进的讯飞星火WebSocket客户端 - 专注解决WSS协议问题
 */
@Component
public class ImprovedSparkClient {
    
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * 使用改进的WebSocket配置调用讯飞星火API
     */
    public String generateContent(SparkConfig config, String systemPrompt, String userPrompt) {
        try {
            String authUrl = getAuthUrl(config);
            StringBuilder responseContent = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            
            System.out.println("🔧 使用改进的WebSocket客户端连接: " + authUrl);
            
            // 创建URI时强制使用wss协议
            URI uri = new URI(authUrl.replace("ws://", "wss://"));
            System.out.println("📡 连接URI: " + uri);
            
            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("✅ 改进的WebSocket连接已建立");
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
                        System.out.println("📥 收到响应: " + message.substring(0, Math.min(100, message.length())) + "...");
                        
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
                                    System.out.println("📝 收到内容片段: " + content);
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
                        e.printStackTrace();
                        latch.countDown();
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔒 WebSocket连接关闭: " + reason);
                    latch.countDown();
                }
                
                @Override
                public void onError(Exception ex) {
                    System.err.println("❌ WebSocket错误: " + ex.getMessage());
                    ex.printStackTrace();
                    latch.countDown();
                }
            };
            
            // 设置SSL上下文 - 最宽松的配置
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }}, new java.security.SecureRandom());
                
                client.setSocketFactory(sslContext.getSocketFactory());
                System.out.println("🔐 SSL上下文配置完成");
                
                // 设置SSL参数
                SSLParameters sslParams = new SSLParameters();
                sslParams.setEndpointIdentificationAlgorithm(null); // 禁用主机名验证
                System.out.println("🛡️ SSL参数配置完成");
                
            } catch (Exception e) {
                System.err.println("⚠️ SSL配置失败，但继续尝试连接: " + e.getMessage());
            }
            
            // 设置连接超时
            client.setConnectionLostTimeout(TIMEOUT_SECONDS);
            
            // 连接
            System.out.println("🚀 开始建立连接...");
            boolean connected = client.connectBlocking(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!connected) {
                throw new RuntimeException("WebSocket连接超时");
            }
            
            // 等待响应完成
            if (latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                String result = responseContent.toString();
                if (result.isEmpty()) {
                    throw new RuntimeException("API返回空结果");
                }
                return result;
            } else {
                client.close();
                throw new RuntimeException("API调用超时");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("调用讯飞星火API失败: " + e.getMessage(), e);
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
        chat.put("max_tokens", 4096);
        parameter.put("chat", chat);
        request.put("parameter", parameter);
        
        // payload
        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();
        JSONArray text = new JSONArray();
        
        // 添加system消息
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            text.add(systemMsg);
        }
        
        // 添加user消息
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        text.add(userMsg);
        
        message.put("text", text);
        payload.put("message", message);
        request.put("payload", payload);
        
        return request;
    }
    
    /**
     * 生成鉴权URL（严格按照官方文档）
     */
    private String getAuthUrl(SparkConfig config) throws Exception {
        URL url = new URL(config.getHostUrl());
        
        // 时间戳
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = dateFormat.format(new Date());
        
        // 构建签名字符串
        String signatureOrigin = "host: " + url.getHost() + "\n" +
                                "date: " + date + "\n" +
                                "GET " + url.getPath() + " HTTP/1.1";
        
        // HMAC-SHA256签名
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(config.getApiSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signatureBase64 = Base64.encodeBase64String(signature);
        
        // 构建authorization
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                config.getApiKey(), "hmac-sha256", "host date request-line", signatureBase64);
        String authorizationBase64 = Base64.encodeBase64String(authorization.getBytes(StandardCharsets.UTF_8));
        
        // 构建鉴权URL
        return config.getHostUrl() + "?authorization=" + authorizationBase64 + "&date=" + 
               java.net.URLEncoder.encode(date, "UTF-8") + "&host=" + url.getHost();
    }
}
