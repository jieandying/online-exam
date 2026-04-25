package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.FaceRecognitionRequest;
import com.rabbiter.oes.entity.FaceRecognitionResult;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.service.FaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * 人脸识别服务实现类
 * 使用感知哈希算法（pHash）进行人脸相似度比对
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    // 人脸验证通过的相似度阈值（0-100）
    private static final double SIMILARITY_THRESHOLD = 75.0;
    
    // 哈希图像大小
    private static final int HASH_SIZE = 32;
    
    @Override
    public FaceRecognitionResult registerFace(Integer studentId, String faceImage) {
        FaceRecognitionResult result = new FaceRecognitionResult();
        
        try {
            // 验证学生是否存在
            Student student = studentMapper.findById(studentId);
            if (student == null) {
                result.setSuccess(false);
                result.setMessage("学生不存在");
                return result;
            }
            
            // 检查是否已经注册过人脸（只能注册一次）
            if (hasFaceRegistered(studentId)) {
                result.setSuccess(false);
                result.setMessage("您已注册过人脸，每位学生只能注册一次");
                result.setStudentId(studentId);
                return result;
            }
            
            // 验证图片数据是否有效
            if (faceImage == null || faceImage.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("人脸图片数据为空");
                return result;
            }
            
            // 验证图片格式
            if (!isValidBase64Image(faceImage)) {
                result.setSuccess(false);
                result.setMessage("人脸图片格式无效");
                return result;
            }
            
            // 保存人脸数据
            int rows = studentMapper.updateFaceData(studentId, faceImage);
            if (rows > 0) {
                result.setSuccess(true);
                result.setMessage("人脸注册成功");
                result.setStudentId(studentId);
                result.setStudentName(student.getStudentName());
            } else {
                result.setSuccess(false);
                result.setMessage("人脸注册失败");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("人脸注册异常: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public FaceRecognitionResult verifyFaceForExam(FaceRecognitionRequest request) {
        FaceRecognitionResult result = new FaceRecognitionResult();
        
        try {
            Integer studentId = request.getStudentId();
            String capturedFaceImage = request.getFaceImage();
            
            // 验证参数
            if (studentId == null) {
                result.setSuccess(false);
                result.setMessage("学生ID不能为空");
                return result;
            }
            
            if (capturedFaceImage == null || capturedFaceImage.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("人脸图片不能为空");
                return result;
            }
            
            // 获取学生信息
            Student student = studentMapper.findById(studentId);
            if (student == null) {
                result.setSuccess(false);
                result.setMessage("学生不存在");
                return result;
            }
            
            // 获取已注册的人脸数据
            String registeredFaceData = studentMapper.getFaceData(studentId);
            if (registeredFaceData == null || registeredFaceData.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("该学生未注册人脸，请先进行人脸注册");
                return result;
            }
            
            // 比对人脸相似度
            double similarity = compareFaces(registeredFaceData, capturedFaceImage);
            result.setSimilarity(similarity);
            result.setStudentId(studentId);
            result.setStudentName(student.getStudentName());
            
            if (similarity >= SIMILARITY_THRESHOLD) {
                result.setSuccess(true);
                result.setMessage("人脸验证通过，可以进入考试");
            } else {
                result.setSuccess(false);
                result.setMessage("人脸验证失败，相似度不足。请确保光线充足并正对摄像头");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("人脸验证异常: " + e.getMessage());
            result.setSimilarity(0);
        }
        
        return result;
    }
    
    @Override
    public double compareFaces(String faceImage1, String faceImage2) {
        try {
            // 解码Base64图片
            BufferedImage image1 = decodeBase64Image(faceImage1);
            BufferedImage image2 = decodeBase64Image(faceImage2);
            
            if (image1 == null || image2 == null) {
                return 0;
            }
            
            // 使用感知哈希算法计算相似度
            String hash1 = calculatePHash(image1);
            String hash2 = calculatePHash(image2);
            
            // 计算汉明距离
            int hammingDistance = calculateHammingDistance(hash1, hash2);
            
            // 将汉明距离转换为相似度（0-100）
            double similarity = (1.0 - (double) hammingDistance / (HASH_SIZE * HASH_SIZE)) * 100;
            
            return Math.max(0, Math.min(100, similarity));
        } catch (Exception e) {
            System.err.println("人脸比对异常: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public boolean hasFaceRegistered(Integer studentId) {
        String faceData = studentMapper.getFaceData(studentId);
        return faceData != null && !faceData.isEmpty();
    }
    
    /**
     * 解码Base64图片
     */
    private BufferedImage decodeBase64Image(String base64Image) {
        try {
            // 移除Base64前缀（如果有）
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.substring(base64Image.indexOf(",") + 1);
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);
        } catch (Exception e) {
            System.err.println("解码Base64图片失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Base64图片是否有效
     */
    private boolean isValidBase64Image(String base64Image) {
        try {
            BufferedImage image = decodeBase64Image(base64Image);
            return image != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算感知哈希（pHash）
     * 用于图像相似度比较
     */
    private String calculatePHash(BufferedImage image) {
        // 1. 缩小图片到指定大小
        BufferedImage resized = resizeImage(image, HASH_SIZE, HASH_SIZE);
        
        // 2. 转换为灰度图
        BufferedImage grayscale = toGrayscale(resized);
        
        // 3. 计算像素平均值
        int totalPixels = HASH_SIZE * HASH_SIZE;
        long sum = 0;
        int[][] pixels = new int[HASH_SIZE][HASH_SIZE];
        
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                int gray = grayscale.getRGB(x, y) & 0xFF;
                pixels[y][x] = gray;
                sum += gray;
            }
        }
        
        int average = (int) (sum / totalPixels);
        
        // 4. 生成哈希值
        StringBuilder hash = new StringBuilder();
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                hash.append(pixels[y][x] >= average ? "1" : "0");
            }
        }
        
        return hash.toString();
    }
    
    /**
     * 计算汉明距离
     */
    private int calculateHammingDistance(String hash1, String hash2) {
        if (hash1.length() != hash2.length()) {
            return Integer.MAX_VALUE;
        }
        
        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }
        
        return distance;
    }
    
    /**
     * 缩放图片
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
    
    /**
     * 转换为灰度图
     */
    private BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayscale.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscale;
    }
}
