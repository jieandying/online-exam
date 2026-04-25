package com.rabbiter.oes.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 图形验证码工具类
 */
public class CaptchaUtil {
    
    // 验证码字符集（去除易混淆字符）
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Random random = new Random();
    
    /**
     * 生成验证码结果对象
     */
    public static class CaptchaResult {
        private String code;      // 验证码文本
        private String imageBase64; // Base64编码的图片
        
        public CaptchaResult(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getImageBase64() {
            return imageBase64;
        }
    }
    
    /**
     * 生成图形验证码
     */
    public static CaptchaResult generateCaptcha() {
        // 生成随机验证码文本
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        
        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(getRandomColor(160, 200));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(getRandomColor(130, 180));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
        
        // 绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < CODE_LENGTH; i++) {
            g.setColor(getRandomColor(50, 120));
            // 随机旋转角度
            double theta = Math.toRadians(random.nextInt(30) - 15);
            g.rotate(theta, 25 + i * 25, 25);
            g.drawString(String.valueOf(code.charAt(i)), 15 + i * 25, 30);
            g.rotate(-theta, 25 + i * 25, 25);
        }
        
        g.dispose();
        
        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new CaptchaResult(code.toString(), "data:image/png;base64," + base64);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }
    
    /**
     * 生成随机颜色
     */
    private static Color getRandomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
    
    /**
     * 校验验证码（忽略大小写）
     */
    public static boolean verify(String input, String stored) {
        if (input == null || stored == null) {
            return false;
        }
        return input.equalsIgnoreCase(stored);
    }
}
