package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.FaceRecognitionRequest;
import com.rabbiter.oes.entity.FaceRecognitionResult;

/**
 * 人脸识别服务接口
 */
public interface FaceRecognitionService {
    
    /**
     * 注册人脸数据
     * @param studentId 学生ID
     * @param faceImage 人脸图片（Base64编码）
     * @return 注册结果
     */
    FaceRecognitionResult registerFace(Integer studentId, String faceImage);
    
    /**
     * 验证人脸进入考试
     * @param request 人脸识别请求
     * @return 验证结果
     */
    FaceRecognitionResult verifyFaceForExam(FaceRecognitionRequest request);
    
    /**
     * 比对两张人脸图片的相似度
     * @param faceImage1 人脸图片1（Base64编码）
     * @param faceImage2 人脸图片2（Base64编码）
     * @return 相似度（0-100）
     */
    double compareFaces(String faceImage1, String faceImage2);
    
    /**
     * 检查学生是否已注册人脸
     * @param studentId 学生ID
     * @return 是否已注册
     */
    boolean hasFaceRegistered(Integer studentId);
}
