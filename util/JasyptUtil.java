package com.rabbiter.oes.util;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.springframework.stereotype.Component;

/**
 * Jasypt 加密工具类
 * 用于加密配置文件中的敏感信息
 * 
 * @author OES
 * @date 2026-03-29
 */
@Slf4j
@Component
public class JasyptUtil {

    // 默认密钥（开发环境）
    private static final String DEFAULT_PASSWORD = "oes_jasypt_secret_key_2026";
    
    /**
     * 加密字符串
     * 
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, DEFAULT_PASSWORD);
    }
    
    /**
     * 加密字符串
     * 
     * @param plainText 明文
     * @param secretKey 密钥
     * @return 密文
     */
    public static String encrypt(String plainText, String secretKey) {
        try {
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setAlgorithm("PBEWithMD5AndDES");
            encryptor.setPassword(secretKey);
            encryptor.setIvGenerator(new NoIvGenerator());
            
            String encrypted = encryptor.encrypt(plainText);
            log.info("加密成功：{} -> ENC({})", plainText, encrypted);
            return encrypted;
        } catch (Exception e) {
            log.error("加密失败：{}", e.getMessage(), e);
            throw new RuntimeException("加密失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解密字符串
     * 
     * @param encryptedText 密文（不包含 ENC() 前缀和后缀）
     * @return 明文
     */
    public static String decrypt(String encryptedText) {
        return decrypt(encryptedText, DEFAULT_PASSWORD);
    }
    
    /**
     * 解密字符串
     * 
     * @param encryptedText 密文（不包含 ENC() 前缀和后缀）
     * @param secretKey 密钥
     * @return 明文
     */
    public static String decrypt(String encryptedText, String secretKey) {
        try {
            StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
            decryptor.setAlgorithm("PBEWithMD5AndDES");
            decryptor.setPassword(secretKey);
            decryptor.setIvGenerator(new NoIvGenerator());
            
            String decrypted = decryptor.decrypt(encryptedText);
            log.info("解密成功：ENC({}) -> {}", encryptedText, decrypted);
            return decrypted;
        } catch (Exception e) {
            log.error("解密失败：{}", e.getMessage(), e);
            throw new RuntimeException("解密失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 测试加密功能
     */
    public static void main(String[] args) {
        // 示例：加密数据库密码
        String dbPassword = "123456";
        String encrypted = encrypt(dbPassword);
        System.out.println("原始密码：" + dbPassword);
        System.out.println("加密后：ENC(" + encrypted + ")");
        System.out.println("解密后：" + decrypt(encrypted));
        
        System.out.println("\n========== 生成生产环境配置 ==========");
        System.out.println("# 将以下配置添加到 application.properties");
        System.out.println("spring.datasource.password=ENC(" + encrypted + ")");
        System.out.println("\n# 注意：生产环境请使用环境变量设置密钥");
        System.out.println("# jasypt.encryptor.password=${DB_ENCRYPT_KEY}");
    }
}
