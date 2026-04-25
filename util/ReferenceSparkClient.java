package com.rabbiter.oes.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.SparkConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 参考文档的讯飞星火WebSocket客户端实现
 */
@Component
public class ReferenceSparkClient {
    
    private static final int TIMEOUT_SECONDS = 60;
    
    /**
     * 生成内容（带重试机制）
     */
    public String generateContent(SparkConfig config, String systemPrompt, String userPrompt) {
        int retryCount = 0;
        final int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try {
                System.out.println("🚀 第" + (retryCount + 1) + "次尝试生成内容...");
                SparkClient client = new SparkClient(config, systemPrompt, userPrompt);
                String result = client.sendMessage();
                System.out.println("✅ 第" + (retryCount + 1) + "次尝试成功");
                return result;
            } catch (Exception e) {
                System.err.println("❌ 第" + (retryCount + 1) + "次尝试失败: " + e.getMessage());
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("调用讯飞星火API失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }
                
                try {
                    Thread.sleep(2000); // 重试间隔2秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("调用讯飞星火API失败，已重试" + maxRetries + "次");
    }
    
    /**
     * 讯飞星火WebSocket客户端（参考文档实现）
     */
    private static class SparkClient extends WebSocketClient {
        private final SparkConfig config;
        private final String systemPrompt;
        private final String userPrompt;
        private final StringBuilder responseBuilder = new StringBuilder();
        private final CompletableFuture<String> responseFuture = new CompletableFuture<>();
        
        public SparkClient(SparkConfig config, String systemPrompt, String userPrompt) throws Exception {
            super(SparkApiUtils.createWebSocketURI(config.getHostUrl(), config.getApiKey(), config.getApiSecret()));
            this.config = config;
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
            this.setConnectionLostTimeout(TIMEOUT_SECONDS);
            this.responseBuilder.setLength(0);
            
            System.out.println("🔧 SparkClient已初始化");
        }
        
        public String sendMessage() throws Exception {
            try {
                System.out.println("🚀 尝试WebSocket连接...");
                boolean connected = this.connectBlocking(30, TimeUnit.SECONDS);
                if (!connected || !this.isOpen()) {
                    throw new Exception("WebSocket连接失败，请检查网络和API配置");
                }
                
                System.out.println("✅ WebSocket连接成功，发送请求...");
                String params = generateParams();
                this.send(params);
                
                String response = responseFuture.get(TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);
                if (response == null || response.isEmpty()) {
                    throw new Exception("AI服务未返回有效内容");
                }
                
                System.out.println("✅ 收到AI响应，长度: " + response.length());
                return response;
            } finally {
                try {
                    if (this.isOpen()) {
                        System.out.println("🔒 关闭WebSocket连接");
                        this.close();
                    }
                } catch (Exception e) {
                    System.err.println("关闭WebSocket连接时出错: " + e.getMessage());
                }
            }
        }
        
        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            System.out.println("🎉 WebSocket连接已打开，状态: " + serverHandshake.getHttpStatus());
        }
        
        @Override
        public void onMessage(String text) {
            try {
                System.out.println("📥 收到消息: " + text.substring(0, Math.min(200, text.length())) + "...");
                
                JSONObject responseJson = JSONObject.parseObject(text);
                
                JSONObject header = responseJson.getJSONObject("header");
                if (header == null) {
                    return;
                }
                
                Integer code = header.getInteger("code");
                if (code != null && code != 0) {
                    String errorMsg = header.getString("message");
                    System.err.println("❌ API返回错误: " + errorMsg);
                    responseFuture.completeExceptionally(new Exception("API返回错误: " + errorMsg));
                    return;
                }
                
                Integer status = header.getInteger("status");
                
                JSONObject payload = responseJson.getJSONObject("payload");
                if (payload == null) {
                    return;
                }
                
                JSONObject choices = payload.getJSONObject("choices");
                if (choices == null) {
                    return;
                }
                
                JSONArray textArray = choices.getJSONArray("text");
                if (textArray != null && !textArray.isEmpty()) {
                    JSONObject textObj = textArray.getJSONObject(0);
                    if (textObj != null) {
                        String content = textObj.getString("content");
                        if (content != null) {
                            responseBuilder.append(content);
                            System.out.println("📝 收到内容片段: " + content);
                        }
                    }
                }
                
                // 检查是否结束
                if (status != null && status == 2 || 
                    (choices.getInteger("status") != null && choices.getInteger("status") == 2)) {
                    System.out.println("✅ 响应完成");
                    responseFuture.complete(responseBuilder.toString());
                }
            } catch (Exception e) {
                System.err.println("❌ 解析消息失败: " + e.getMessage());
                responseFuture.completeExceptionally(e);
            }
        }
        
        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("🔒 WebSocket连接关闭: " + code + " - " + reason + " (远程: " + remote + ")");
            if (!responseFuture.isDone()) {
                responseFuture.complete(responseBuilder.toString());
            }
        }
        
        @Override
        public void onError(Exception ex) {
            System.err.println("❌ WebSocket错误: " + ex.getMessage());
            responseFuture.completeExceptionally(ex);
        }
        
        /**
         * 生成请求参数（参考文档实现）
         */
        private String generateParams() {
            JSONObject request = new JSONObject();
            
            // 构造header
            request.put("header", new JSONObject()
                    .fluentPut("app_id", config.getAppId())
                    .fluentPut("uid", UUID.randomUUID().toString().replace("-", ""))
            );
            
            // 构造parameter
            JSONObject parameter = new JSONObject();
            parameter.put("chat", new JSONObject()
                    .fluentPut("domain", config.getDomain())  // 使用配置的domain
                    .fluentPut("temperature", 0.1)  // 降低温度，提高输出格式稳定性
                    .fluentPut("max_tokens", 4096)
            );
            request.put("parameter", parameter);
            
            // 构造payload
            JSONObject payload = new JSONObject();
            JSONObject messageObj = new JSONObject();
            JSONArray messages = new JSONArray();
            
            // 添加系统提示词
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", this.systemPrompt);
            messages.add(systemMsg);
            
            // 添加用户提示词
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", this.userPrompt);
            messages.add(userMsg);
            
            messageObj.put("text", messages);
            payload.put("message", messageObj);
            request.put("payload", payload);
            
            String jsonString = request.toJSONString();
            System.out.println("📤 发送请求: " + jsonString.substring(0, Math.min(300, jsonString.length())) + "...");
            
            return jsonString;
        }
    }
}
