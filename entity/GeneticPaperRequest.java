package com.rabbiter.oes.entity;

import java.util.List;

/**
 * 遗传算法智能组卷请求参数
 */
public class GeneticPaperRequest {
    
    // 目标总分
    private Integer targetScore;
    
    // 目标难度系数 (1-5，可以是小数如 2.5)
    private Double targetDifficulty;
    
    // 难度误差容忍范围
    private Double difficultyTolerance;
    
    // 科目列表（可选多个科目组合出题）
    private List<String> subjects;
    
    // 选择题数量
    private Integer multiCount;
    
    // 填空题数量
    private Integer fillCount;
    
    // 判断题数量
    private Integer judgeCount;
    
    // 主观题数量
    private Integer subjectiveCount;
    
    // 各题型分值
    private Integer multiScore;
    private Integer fillScore;
    private Integer judgeScore;
    private Integer subjectiveScore;
    
    // 章节覆盖要求（可选）
    private List<String> sections;
    
    // 遗传算法参数
    private Integer populationSize;   // 种群大小，默认50
    private Integer maxGenerations;   // 最大迭代次数，默认100
    private Double crossoverRate;     // 交叉概率，默认0.8
    private Double mutationRate;      // 变异概率，默认0.1
    
    // 学生ID（学生端组卷时用于统计）
    private Integer studentId;
    
    public GeneticPaperRequest() {
        // 默认值
        this.targetScore = 100;
        this.targetDifficulty = 3.0;
        this.difficultyTolerance = 0.5;
        this.multiCount = 10;
        this.fillCount = 5;
        this.judgeCount = 0;
        this.subjectiveCount = 2;
        this.multiScore = 5;
        this.fillScore = 5;
        this.judgeScore = 5;
        this.subjectiveScore = 20;
        this.populationSize = 50;
        this.maxGenerations = 100;
        this.crossoverRate = 0.8;
        this.mutationRate = 0.1;
    }

    public Integer getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(Integer targetScore) {
        this.targetScore = targetScore;
    }

    public Double getTargetDifficulty() {
        return targetDifficulty;
    }

    public void setTargetDifficulty(Double targetDifficulty) {
        this.targetDifficulty = targetDifficulty;
    }

    public Double getDifficultyTolerance() {
        return difficultyTolerance;
    }

    public void setDifficultyTolerance(Double difficultyTolerance) {
        this.difficultyTolerance = difficultyTolerance;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public Integer getMultiCount() {
        return multiCount;
    }

    public void setMultiCount(Integer multiCount) {
        this.multiCount = multiCount;
    }

    public Integer getFillCount() {
        return fillCount;
    }

    public void setFillCount(Integer fillCount) {
        this.fillCount = fillCount;
    }

    public Integer getJudgeCount() {
        return judgeCount;
    }

    public void setJudgeCount(Integer judgeCount) {
        this.judgeCount = judgeCount;
    }

    public Integer getMultiScore() {
        return multiScore;
    }

    public void setMultiScore(Integer multiScore) {
        this.multiScore = multiScore;
    }

    public Integer getFillScore() {
        return fillScore;
    }

    public void setFillScore(Integer fillScore) {
        this.fillScore = fillScore;
    }

    public Integer getJudgeScore() {
        return judgeScore;
    }

    public void setJudgeScore(Integer judgeScore) {
        this.judgeScore = judgeScore;
    }
    
    public Integer getSubjectiveCount() {
        return subjectiveCount;
    }
    
    public void setSubjectiveCount(Integer subjectiveCount) {
        this.subjectiveCount = subjectiveCount;
    }
    
    public Integer getSubjectiveScore() {
        return subjectiveScore;
    }
    
    public void setSubjectiveScore(Integer subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public Integer getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(Integer populationSize) {
        this.populationSize = populationSize;
    }

    public Integer getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(Integer maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public Double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(Double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public Double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(Double mutationRate) {
        this.mutationRate = mutationRate;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    /**
     * 计算期望总分
     */
    public int getExpectedTotalScore() {
        return (multiCount != null ? multiCount : 0) * (multiScore != null ? multiScore : 2)
             + (fillCount != null ? fillCount : 0) * (fillScore != null ? fillScore : 2)
             + (judgeCount != null ? judgeCount : 0) * (judgeScore != null ? judgeScore : 2)
             + (subjectiveCount != null ? subjectiveCount : 0) * (subjectiveScore != null ? subjectiveScore : 10);
    }
}
