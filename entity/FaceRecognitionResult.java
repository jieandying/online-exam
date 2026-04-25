package com.rabbiter.oes.entity;

/**
 * 人脸识别结果实体类
 */
public class FaceRecognitionResult {
    
    // 是否验证通过
    private boolean success;
    
    // 相似度（0-100）
    private double similarity;
    
    // 消息
    private String message;
    
    // 学生ID
    private Integer studentId;
    
    // 学生姓名
    private String studentName;
    
    public FaceRecognitionResult() {
    }
    
    public FaceRecognitionResult(boolean success, double similarity, String message) {
        this.success = success;
        this.similarity = similarity;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public double getSimilarity() {
        return similarity;
    }
    
    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
