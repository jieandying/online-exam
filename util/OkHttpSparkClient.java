package com.rabbiter.oes.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.SparkConfig;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 使用OkHttp的讯飞星火WebSocket客户端（更稳定的WSS支持）
 */
@Component
public class OkHttpSparkClient {
    
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * 使用OkHttp调用讯飞星火API
     */
    public String generateContent(SparkConfig config, String systemPrompt, String userPrompt) {
        try {
            String authUrl = getAuthUrl(config);
            StringBuilder responseContent = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            
            // 创建信任所有证书的SSL配置
            X509TrustManager trustAllCerts = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
            };
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllCerts}, new java.security.SecureRandom());
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory(), trustAllCerts)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            
            Request request = new Request.Builder()
                    .url(authUrl)
                    .build();
            
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    System.out.println("OkHttp WebSocket连接已建立");
                    // 发送请求
                    JSONObject requestJson = buildRequest(config, systemPrompt, userPrompt);
                    webSocket.send(requestJson.toString());
                }
                
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    try {
                        JSONObject response = JSON.parseObject(text);
                        JSONObject header = response.getJSONObject("header");
                        
                        if (header != null && header.getInteger("code") != 0) {
                            System.err.println("API错误: " + header.getString("message"));
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
                                }
                            }
                        }
                        
                        // 检查是否结束
                        if (header != null && header.getInteger("status") == 2) {
                            webSocket.close(1000, "Completed");
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        System.err.println("解析响应失败: " + e.getMessage());
                        latch.countDown();
                    }
                }
                
                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    System.out.println("WebSocket正在关闭: " + reason);
                    latch.countDown();
                }
                
                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    System.out.println("WebSocket已关闭: " + reason);
                    latch.countDown();
                }
                
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    System.err.println("WebSocket错误: " + t.getMessage());
                    latch.countDown();
                }
            };
            
            WebSocket webSocket = client.newWebSocket(request, listener);
            
            // 等待响应完成
            if (latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return responseContent.toString();
            } else {
                webSocket.close(1000, "Timeout");
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
     * 生成鉴权URL
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
