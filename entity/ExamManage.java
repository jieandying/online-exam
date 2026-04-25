package com.rabbiter.oes.entity;

public class ExamManage {
    private Integer examCode;

    private String description;

    private String source;

    private Integer paperId;

    private String examDate;

    private Integer totalTime;

    private String grade;

    private String term;

    private String major;

    private String institute;

    private Integer totalScore;

    private String type;

    private String tips;

    /** 考试开始时间，格式 HH:mm，默认 00:00 即全天均可 */
    private String examStartTime;

    /** 考试截止时间，格式 HH:mm，默认 23:59 */
    private String examEndTime;

    /** 选择题每题分值 */
    private Integer multiScore;

    /** 填空题每题分值 */
    private Integer fillScore;

    /** 判断题每题分值 */
    private Integer judgeScore;

    /** 主观题每题分值 */
    private Integer subjectiveScore;

    public Integer getExamCode() {
        return examCode;
    }

    public void setExamCode(Integer examCode) {
        this.examCode = examCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getExamStartTime() { return examStartTime; }
    public void setExamStartTime(String examStartTime) { this.examStartTime = examStartTime; }

    public String getExamEndTime() { return examEndTime; }
    public void setExamEndTime(String examEndTime) { this.examEndTime = examEndTime; }

    public Integer getMultiScore() { return multiScore; }
    public void setMultiScore(Integer multiScore) { this.multiScore = multiScore; }

    public Integer getFillScore() { return fillScore; }
    public void setFillScore(Integer fillScore) { this.fillScore = fillScore; }

    public Integer getJudgeScore() { return judgeScore; }
    public void setJudgeScore(Integer judgeScore) { this.judgeScore = judgeScore; }

    public Integer getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(Integer subjectiveScore) { this.subjectiveScore = subjectiveScore; }

    @Override
    public String toString() {
        return "ExamManage{" +
                "examCode=" + examCode +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", paperId=" + paperId +
                ", examDate='" + examDate + '\'' +
                ", totalTime=" + totalTime +
                ", grade='" + grade + '\'' +
                ", term='" + term + '\'' +
                ", major='" + major + '\'' +
                ", institute='" + institute + '\'' +
                ", totalScore=" + totalScore +
                ", type='" + type + '\'' +
                ", tips='" + tips + '\'' +
                '}';
    }
}