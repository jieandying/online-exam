package com.rabbiter.oes.entity;

import java.util.Date;

/**
 * 遗传算法组卷记录实体
 */
public class GeneticPaperRecord {
    private Integer id;
    private String subjects;
    private Double targetDifficulty;
    private Integer multiCount;
    private Integer fillCount;
    private Integer judgeCount;
    private Integer totalScore;
    private Integer totalQuestions;
    private Double avgDifficulty;
    private Double fitness;
    private Integer generationTime;
    private Integer isCreatedExam;
    private Integer examCode;
    private Date createTime;
    private String paperJson;
    private Integer studentId;  // 学生组卷用于统计

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public Double getTargetDifficulty() { return targetDifficulty; }
    public void setTargetDifficulty(Double targetDifficulty) { this.targetDifficulty = targetDifficulty; }

    public Integer getMultiCount() { return multiCount; }
    public void setMultiCount(Integer multiCount) { this.multiCount = multiCount; }

    public Integer getFillCount() { return fillCount; }
    public void setFillCount(Integer fillCount) { this.fillCount = fillCount; }

    public Integer getJudgeCount() { return judgeCount; }
    public void setJudgeCount(Integer judgeCount) { this.judgeCount = judgeCount; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Double getAvgDifficulty() { return avgDifficulty; }
    public void setAvgDifficulty(Double avgDifficulty) { this.avgDifficulty = avgDifficulty; }

    public Double getFitness() { return fitness; }
    public void setFitness(Double fitness) { this.fitness = fitness; }

    public Integer getGenerationTime() { return generationTime; }
    public void setGenerationTime(Integer generationTime) { this.generationTime = generationTime; }

    public Integer getIsCreatedExam() { return isCreatedExam; }
    public void setIsCreatedExam(Integer isCreatedExam) { this.isCreatedExam = isCreatedExam; }

    public Integer getExamCode() { return examCode; }
    public void setExamCode(Integer examCode) { this.examCode = examCode; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getPaperJson() { return paperJson; }
    public void setPaperJson(String paperJson) { this.paperJson = paperJson; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
}
