package com.rabbiter.oes.entity;

import java.util.Date;

/**
 * 学习记录/做题记录实体类
 * 用于统计学习曲线
 */
public class StudyRecord {
    
    private Integer id;
    
    // 学生ID
    private Integer studentId;
    
    // 练习日期
    private Date practiceDate;
    
    // 练习类型: 1-随机练习 2-错题练习 3-正式考试
    private Integer practiceType;
    
    // 做题总数
    private Integer totalQuestions;
    
    // 正确数
    private Integer correctCount;
    
    // 错误数
    private Integer wrongCount;
    
    // 得分
    private Integer score;
    
    // 总分
    private Integer totalScore;
    
    // 正确率（百分比）
    private Double accuracy;
    
    // 用时（秒）
    private Integer duration;
    
    // 练习来源/科目
    private String subject;
    
    // 试卷ID（如果是正式考试）
    private Integer examCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Date getPracticeDate() {
        return practiceDate;
    }

    public void setPracticeDate(Date practiceDate) {
        this.practiceDate = practiceDate;
    }

    public Integer getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(Integer practiceType) {
        this.practiceType = practiceType;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getExamCode() {
        return examCode;
    }

    public void setExamCode(Integer examCode) {
        this.examCode = examCode;
    }
}
