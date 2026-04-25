package com.rabbiter.oes.entity;

/**
 * 人脸识别请求实体类
 */
public class FaceRecognitionRequest {
    
    // 学生ID
    private Integer studentId;
    
    // 考试编号
    private Integer examCode;
    
    // 人脸图片数据（Base64编码）
    private String faceImage;
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getExamCode() {
        return examCode;
    }
    
    public void setExamCode(Integer examCode) {
        this.examCode = examCode;
    }
    
    public String getFaceImage() {
        return faceImage;
    }
    
    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }
}
