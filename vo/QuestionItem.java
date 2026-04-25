package com.rabbiter.oes.vo;

import lombok.Data;

/**
 * 单题详情 VO
 */
@Data
public class QuestionItem {
    private Integer questionId;      // 题目 ID
    private String questionText;     // 题干
    private Integer score;           // 题目分值
    private String studentAnswer;    // 学生答案（A/B/C/D 或具体文字）
    private String correctAnswer;    // 正确答案
    private boolean isCorrect;       // 是否正确
    private String analysis;         // 题目解析
}
