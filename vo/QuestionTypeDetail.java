package com.rabbiter.oes.vo;

import lombok.Data;
import java.util.List;

/**
 * 题型详情 VO
 */
@Data
public class QuestionTypeDetail {
    private String typeName;           // 题型名称（选择题/填空题等）
    private Integer totalQuestions;    // 题目总数
    private Integer correctCount;      // 正确数量
    private Integer scoreGot;          // 该题型得分
    private List<QuestionItem> questions; // 各题详情
}
