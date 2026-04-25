package com.rabbiter.oes.entity;

/**
 * 学生画像实体类
 * 用于K-Means聚类分析的数据结构
 */
public class StudentProfile {
    // 学生基本信息
    private Integer studentId;
    private String studentName;
    private String clazz;
    private String major;
    
    // 三个聚类维度
    private Double avgScore;        // 平均分 (0-100)
    private Double answerSpeed;     // 答题速度 (题/分钟)
    private Double scoreVariance;   // 得分方差 (衡量成绩稳定性)
    private Double errorRate;       // 错题率 (兼容旧字段)
    
    // 标准化后的数据 (用于聚类计算)
    private Double normalizedScore;
    private Double normalizedSpeed;
    private Double normalizedVariance;  // 方差标准化字段
    private Double normalizedError;     // 兼容旧字段
    
    // 聚类结果
    private Integer cluster;        // 聚类编号 (0,1,2)
    private String clusterName;     // 聚类名称
    private String clusterDesc;     // 聚类描述
    
    // 统计信息
    private Integer examCount;      // 参加考试次数
    private Integer correctCount;   // 正确题数
    private Integer wrongCount;     // 错误题数
    private Integer totalTime;      // 总答题时间(秒)
    
    public StudentProfile() {}
    
    public StudentProfile(Integer studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }
    
    // Getters and Setters
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getClazz() { return clazz; }
    public void setClazz(String clazz) { this.clazz = clazz; }
    
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    
    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }
    
    public Double getAnswerSpeed() { return answerSpeed; }
    public void setAnswerSpeed(Double answerSpeed) { this.answerSpeed = answerSpeed; }
    
    public Double getScoreVariance() { return scoreVariance; }
    public void setScoreVariance(Double scoreVariance) { this.scoreVariance = scoreVariance; }

    public Double getErrorRate() { return errorRate; }
    public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }
    
    public Double getNormalizedScore() { return normalizedScore; }
    public void setNormalizedScore(Double normalizedScore) { this.normalizedScore = normalizedScore; }
    
    public Double getNormalizedSpeed() { return normalizedSpeed; }
    public void setNormalizedSpeed(Double normalizedSpeed) { this.normalizedSpeed = normalizedSpeed; }
    
    public Double getNormalizedVariance() { return normalizedVariance; }
    public void setNormalizedVariance(Double normalizedVariance) { this.normalizedVariance = normalizedVariance; }

    public Double getNormalizedError() { return normalizedError; }
    public void setNormalizedError(Double normalizedError) { this.normalizedError = normalizedError; }
    
    public Integer getCluster() { return cluster; }
    public void setCluster(Integer cluster) { this.cluster = cluster; }
    
    public String getClusterName() { return clusterName; }
    public void setClusterName(String clusterName) { this.clusterName = clusterName; }
    
    public String getClusterDesc() { return clusterDesc; }
    public void setClusterDesc(String clusterDesc) { this.clusterDesc = clusterDesc; }
    
    public Integer getExamCount() { return examCount; }
    public void setExamCount(Integer examCount) { this.examCount = examCount; }
    
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    
    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }
    
    public Integer getTotalTime() { return totalTime; }
    public void setTotalTime(Integer totalTime) { this.totalTime = totalTime; }
}
