package com.rabbiter.oes.entity;

import java.util.Date;

/**
 * 错题记录实体类
 */
public class WrongQuestion {

    private Integer id;

    // 学生ID
    private Integer studentId;

    // 题目类型: 1-选择题 2-填空题 3-判断题 4-主观题
    private Integer questionType;

    // 题目ID
    private Integer questionId;

    // 错误次数
    private Integer wrongCount;

    // 最近一次错误时间
    private Date lastWrongTime;

    // 是否已掌握 0-未掌握 1-已掌握
    private Integer mastered;

    // 题目内容（冗余存储便于展示）
    private String questionContent;

    // 正确答案
    private String correctAnswer;

    // 学生最近一次错误答案
    private String wrongAnswer;

    // 题目分值
    private Integer score;

    // 所属科目/来源
    private String subject;

    // 题目解析/知识点说明
    private String analysis;

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

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Date getLastWrongTime() {
        return lastWrongTime;
    }

    public void setLastWrongTime(Date lastWrongTime) {
        this.lastWrongTime = lastWrongTime;
    }

    public Integer getMastered() {
        return mastered;
    }

    public void setMastered(Integer mastered) {
        this.mastered = mastered;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getWrongAnswer() {
        return wrongAnswer;
    }

    public void setWrongAnswer(String wrongAnswer) {
        this.wrongAnswer = wrongAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 获取题目解析
     * 
     * @return 解析内容
     */
    public String getAnalysis() {
        return analysis;
    }

    /**
     * 设置题目解析
     * 
     * @param analysis 解析内容
     */
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
