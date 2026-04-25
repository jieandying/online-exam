package com.rabbiter.oes.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 讯飞星火API工具类（参考官方文档实现）
 */
@Component
public class SparkApiUtils {
    
    /**
     * 创建WebSocket URI（手动解析，避免unknown protocol错误）
     */
    public static URI createWebSocketURI(String hostUrl, String apiKey, String apiSecret) throws Exception {
        // 使用GMT时间，符合HTTP 1.1标准
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(Calendar.getInstance().getTime());
        
        // 手动解析WebSocket URL（避免Java URL类的protocol限制）
        Pattern pattern = Pattern.compile("(wss?)://([^:/]+)(:\\d+)?(/.*)?");
        Matcher matcher = pattern.matcher(hostUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("无效的WebSocket URL格式: " + hostUrl);
        }
        
        String protocol = matcher.group(1); // ws 或 wss
        String host = matcher.group(2); // 主机名
        String port = matcher.group(3); // 端口（可能为空）
        String path = matcher.group(4); // 路径
        
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        
        System.out.println("解析WebSocket URL:");
        System.out.println("  协议: " + protocol);
        System.out.println("  主机: " + host);
        System.out.println("  端口: " + (port != null ? port : "默认"));
        System.out.println("  路径: " + path);
        
        // 生成签名
        Mac mac = Mac.getInstance("hmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacSHA256"));
        String signContent = "host: " + host + "\n" + 
                "date: " + date + "\n" + 
                "GET " + path + " HTTP/1.1";
        
        System.out.println("签名内容: " + signContent.replace("\n", "\\n"));
        
        byte[] bytes = mac.doFinal(signContent.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.encodeBase64String(bytes);
        
        // 构造鉴权字符串
        String authBase = "api_key=\"" + apiKey + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
        String authorizationBase64 = Base64.encodeBase64String(authBase.getBytes(StandardCharsets.UTF_8));
        
        // 构造鉴权URL
        String url = hostUrl + "?" +
                "authorization=" + URLEncoder.encode(authorizationBase64, "UTF-8") +
                "&date=" + URLEncoder.encode(date, "UTF-8") +
                "&host=" + URLEncoder.encode(host, "UTF-8");
        
        System.out.println("生成鉴权URL: " + url.substring(0, Math.min(100, url.length())) + "...");
        
        return new URI(url);
    }
}
