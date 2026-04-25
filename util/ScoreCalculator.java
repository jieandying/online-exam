package com.rabbiter.oes.util;

import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.SubjectiveQuestion;

import java.util.List;

/**
 * 统一的分数计算工具类
 * 用于计算试卷总分、题目得分等
 * 
 * @author OES
 * @date 2026-03-29
 */
public class ScoreCalculator {

    // 默认分值配置
    private static final int DEFAULT_MULTI_SCORE = 2;      // 选择题默认 2 分
    private static final int DEFAULT_FILL_SCORE = 2;       // 填空题默认 2 分
    private static final int DEFAULT_JUDGE_SCORE = 2;      // 判断题默认 2 分
    private static final int DEFAULT_SUBJECTIVE_SCORE = 10; // 主观题默认 10 分

    /**
     * 计算试卷总分
     * 
     * @param multiQuestions 选择题列表
     * @param fillQuestions 填空题列表
     * @param judgeQuestions 判断题列表
     * @param subjectiveQuestions 主观题列表
     * @return 试卷总分
     */
    public static int calculateTotalScore(
            List<MultiQuestion> multiQuestions,
            List<FillQuestion> fillQuestions,
            List<JudgeQuestion> judgeQuestions,
            List<SubjectiveQuestion> subjectiveQuestions) {
        
        int totalScore = 0;
        
        if (multiQuestions != null) {
            for (MultiQuestion q : multiQuestions) {
                totalScore += getQuestionScore(q.getScore(), DEFAULT_MULTI_SCORE);
            }
        }
        
        if (fillQuestions != null) {
            for (FillQuestion q : fillQuestions) {
                totalScore += getQuestionScore(q.getScore(), DEFAULT_FILL_SCORE);
            }
        }
        
        if (judgeQuestions != null) {
            for (JudgeQuestion q : judgeQuestions) {
                totalScore += getQuestionScore(q.getScore(), DEFAULT_JUDGE_SCORE);
            }
        }
        
        if (subjectiveQuestions != null) {
            for (SubjectiveQuestion q : subjectiveQuestions) {
                totalScore += getQuestionScore(q.getScore(), DEFAULT_SUBJECTIVE_SCORE);
            }
        }
        
        return totalScore;
    }

    /**
     * 获取题目分数（如果为空则返回默认值）
     * 
     * @param score 题目分数
     * @param defaultScore 默认分数
     * @return 题目分数
     */
    private static int getQuestionScore(Integer score, int defaultScore) {
        return score != null ? score : defaultScore;
    }

    /**
     * 计算选择题总分
     */
    public static int calculateMultiScore(List<MultiQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        return questions.stream()
                .mapToInt(q -> getQuestionScore(q.getScore(), DEFAULT_MULTI_SCORE))
                .sum();
    }

    /**
     * 计算填空题总分
     */
    public static int calculateFillScore(List<FillQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        return questions.stream()
                .mapToInt(q -> getQuestionScore(q.getScore(), DEFAULT_FILL_SCORE))
                .sum();
    }

    /**
     * 计算判断题总分
     */
    public static int calculateJudgeScore(List<JudgeQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        return questions.stream()
                .mapToInt(q -> getQuestionScore(q.getScore(), DEFAULT_JUDGE_SCORE))
                .sum();
    }

    /**
     * 计算主观题总分
     */
    public static int calculateSubjectiveScore(List<SubjectiveQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        return questions.stream()
                .mapToInt(q -> getQuestionScore(q.getScore(), DEFAULT_SUBJECTIVE_SCORE))
                .sum();
    }

    /**
     * 获取选择题默认分值
     */
    public static int getDefaultMultiScore() {
        return DEFAULT_MULTI_SCORE;
    }

    /**
     * 获取填空题默认分值
     */
    public static int getDefaultFillScore() {
        return DEFAULT_FILL_SCORE;
    }

    /**
     * 获取判断题默认分值
     */
    public static int getDefaultJudgeScore() {
        return DEFAULT_JUDGE_SCORE;
    }

    /**
     * 获取主观题默认分值
     */
    public static int getDefaultSubjectiveScore() {
        return DEFAULT_SUBJECTIVE_SCORE;
    }
}
