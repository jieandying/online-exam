package com.rabbiter.oes.vo;

import lombok.Data;

/**
 * 答卷详情 VO - 用于考后查看错题解析
 */
@Data
public class ExamDetailVO {
    private Integer examCode;
    private String subject;
    private Integer totalScore;      // 总分
    private Integer studentScore;    // 学生得分
    private String answerDate;       // 考试日期
    
    // 各题型答题情况
    private QuestionTypeDetail choiceQuestions;   // 选择题
    private QuestionTypeDetail fillQuestions;     // 填空题
    private QuestionTypeDetail judgeQuestions;    // 判断题
    private QuestionTypeDetail subjectiveQuestions; // 主观题
}
