package com.rabbiter.oes.entity;

//AI出题请求实体类
public class AIQuestionRequest {
    private String subject; // 科目
    private String questionType; // 题目类型: multiple(选择题), fill(填空题), judge(判断题)
    private Integer questionCount; // 生成题目数量
    private String difficulty; // 难度等级: 1-简单, 2-中等, 3-困难, 4-极难, 5-超纲
    private String chapter; // 章节
    private String prompt; // 提示词
    private String documentContent; // 文档内容(当基于文档出题时)
    private String requirements; // 额外要求

    public AIQuestionRequest() {}

    public AIQuestionRequest(String subject, String questionType, Integer questionCount, 
                           String difficulty, String chapter, String prompt) {
        this.subject = subject;
        this.questionType = questionType;
        this.questionCount = questionCount;
        this.difficulty = difficulty;
        this.chapter = chapter;
        this.prompt = prompt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    @Override
    public String toString() {
        return "AIQuestionRequest{" +
                "subject='" + subject + '\'' +
                ", questionType='" + questionType + '\'' +
                ", questionCount=" + questionCount +
                ", difficulty='" + difficulty + '\'' +
                ", chapter='" + chapter + '\'' +
                ", prompt='" + prompt + '\'' +
                ", documentContent='" + documentContent + '\'' +
                ", requirements='" + requirements + '\'' +
                '}';
    }
}
