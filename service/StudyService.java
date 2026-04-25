package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.StudyRecord;
import com.rabbiter.oes.entity.WrongQuestion;

import java.util.List;
import java.util.Map;

/**
 * 学习记录服务接口
 */
public interface StudyService {

    // ========== 错题本相关 ==========

    /**
     * 添加或更新错题记录
     */
    void addOrUpdateWrongQuestion(WrongQuestion wrongQuestion);

    /**
     * 获取学生错题列表
     */
    List<WrongQuestion> getWrongQuestions(Integer studentId);

    /**
     * 按题型获取错题
     */
    List<WrongQuestion> getWrongQuestionsByType(Integer studentId, Integer questionType);

    /**
     * 标记错题为已掌握
     */
    void markAsMastered(Integer wrongQuestionId);

    /**
     * 按排序方式获取错题列表
     * 
     * @param studentId 学生ID
     * @param sortType  排序方式：recommend-推荐排序, wrongCount-错误次数, latest-最近出错
     * @return 排序后的错题列表
     */
    List<WrongQuestion> getWrongQuestionsWithSort(Integer studentId, String sortType);

    /**
     * 重做错题并校验答案
     * 
     * @param wrongQuestionId 错题记录ID
     * @param userAnswer      用户提交的答案
     * @return 校验结果 Map，包含 correct(是否正确)、correctAnswer(正确答案)
     */
    Map<String, Object> checkRedoAnswer(Integer wrongQuestionId, String userAnswer);

    /**
     * 获取错题统计
     */
    Map<String, Object> getWrongQuestionStats(Integer studentId);

    // ========== 学习记录相关 ==========

    /**
     * 添加学习记录
     */
    void addStudyRecord(StudyRecord record);

    /**
     * 获取学生学习记录
     */
    List<StudyRecord> getStudyRecords(Integer studentId);

    /**
     * 获取最近N天的每日统计（用于曲线图）
     */
    List<Map<String, Object>> getDailyStats(Integer studentId, Integer days);

    /**
     * 获取学生学习概览
     */
    Map<String, Object> getStudentOverview(Integer studentId);

    // ========== 老师端相关 ==========

    /**
     * 获取学生详细学习报告
     */
    Map<String, Object> getStudentReport(Integer studentId);

    /**
     * 获取班级学习概览
     */
    List<Map<String, Object>> getClassOverview(String clazz);

    // ========== AI智能推荐相关 ==========

    /**
     * 分析薄弱知识点
     * 
     * @param studentId 学生ID
     * @return 薄弱知识点分析结果
     */
    Map<String, Object> analyzeWeakPoints(Integer studentId);

    /**
     * 获取薄弱知识点推荐练习题
     * 
     * @param studentId 学生ID
     * @param subject   科目（可选）
     * @param count     题目数量
     * @return 推荐的练习题列表
     */
    List<Map<String, Object>> getRecommendedQuestions(Integer studentId, String subject, Integer count);

    /**
     * 生成模拟考试试卷
     * 从题库随机抽取指定数量的各类型题目
     *
     * @param subject    科目（可选，null表示全部科目）
     * @param multiCount 选择题数量
     * @param fillCount  填空题数量
     * @param judgeCount 判断题数量
     * @return 模拟试卷数据（含各类型题目列表、总分、时间限制等）
     */
    Map<String, Object> generateMockExam(String subject, int multiCount, int fillCount, int judgeCount);

    /**
     * 获取用户的所有考试记录（包含试卷信息）
     */
    List<Map<String, Object>> getExamHistory(Integer studentId);

    /**
     * 根据试卷ID获取试卷详情（用于重做）
     */
    Map<String, Object> getPaperDetails(Integer paperId);
}
