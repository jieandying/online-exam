package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 主观题答案Mapper
 */
@Mapper
public interface SubjectiveAnswerMapper {

    @Select("SELECT * FROM subjective_answer WHERE answerId = #{answerId}")
    SubjectiveAnswer findById(Integer answerId);

    @Select("SELECT sa.*, sq.question, sq.referenceAnswer, sq.score as maxScore, sq.subject, s.studentName " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "LEFT JOIN student s ON sa.studentId = s.studentId " +
            "WHERE sa.examCode = #{examCode} AND sa.status = 0")
    IPage<SubjectiveAnswer> findPendingByExam(Page<?> page, @Param("examCode") Integer examCode);

    @Select("SELECT sa.*, sq.question, sq.referenceAnswer, sq.score as maxScore, sq.subject, s.studentName " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "LEFT JOIN student s ON sa.studentId = s.studentId " +
            "WHERE sa.status = 0")
    IPage<SubjectiveAnswer> findAllPending(Page<?> page);

    @Select("SELECT sa.*, sq.question, sq.referenceAnswer, sq.score as maxScore, sq.subject " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "WHERE sa.examCode = #{examCode} AND sa.studentId = #{studentId}")
    List<SubjectiveAnswer> findByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    @Select("SELECT COUNT(*) FROM subjective_answer WHERE examCode = #{examCode} AND status = 0")
    int countPendingByExam(Integer examCode);

    @Select("SELECT COUNT(*) FROM subjective_answer WHERE examCode = #{examCode} AND studentId = #{studentId} AND status = 0")
    int countPendingByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    @Insert("INSERT INTO subjective_answer(questionId, examCode, studentId, studentAnswer, status, submitTime) " +
            "VALUES(#{questionId}, #{examCode}, #{studentId}, #{studentAnswer}, 0, NOW())")
    int add(SubjectiveAnswer answer);

    @Update("UPDATE subjective_answer SET teacherScore = #{teacherScore}, teacherComment = #{teacherComment}, " +
            "scoredBy = #{scoredBy}, scoredTime = NOW(), status = 1 WHERE answerId = #{answerId}")
    int score(SubjectiveAnswer answer);

    @Delete("DELETE FROM subjective_answer WHERE answerId = #{answerId}")
    int delete(Integer answerId);

    @Select("SELECT COALESCE(SUM(teacherScore), 0) FROM subjective_answer " +
            "WHERE examCode = #{examCode} AND studentId = #{studentId} AND status = 1")
    int getTotalScore(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    /**
     * 分页查询所有已批阅答案（status=1）
     */
    @Select("SELECT sa.*, sq.question, sq.referenceAnswer, sq.score as maxScore, sq.subject, s.studentName " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "LEFT JOIN student s ON sa.studentId = s.studentId " +
            "WHERE sa.status = 1 " +
            "ORDER BY sa.scoredTime DESC")
    IPage<SubjectiveAnswer> findAllGraded(Page<?> page);

    /**
     * 按考试分页查询已批阅答案（status=1）
     */
    @Select("SELECT sa.*, sq.question, sq.referenceAnswer, sq.score as maxScore, sq.subject, s.studentName " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "LEFT JOIN student s ON sa.studentId = s.studentId " +
            "WHERE sa.examCode = #{examCode} AND sa.status = 1 " +
            "ORDER BY sa.scoredTime DESC")
    IPage<SubjectiveAnswer> findGradedByExam(Page<?> page, @Param("examCode") Integer examCode);

    /**
     * 获取某场考试所有已批阅的主观题得分详情（用于试卷质量分析）
     */
    @Select("SELECT sa.questionId, sa.studentId, sa.teacherScore, sq.score as maxScore " +
            "FROM subjective_answer sa " +
            "LEFT JOIN subjective_question sq ON sa.questionId = sq.questionId " +
            "WHERE sa.examCode = #{examCode} AND sa.status = 1")
    List<java.util.Map<String, Object>> findScoreDetailsByExam(@Param("examCode") Integer examCode);
}
