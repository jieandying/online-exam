package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.StudyRecord;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 学习记录Mapper
 */
@Mapper
public interface StudyRecordMapper {

    /**
     * 添加学习记录
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO study_record(studentId, practiceDate, practiceType, totalQuestions, correctCount, " +
            "wrongCount, score, totalScore, accuracy, duration, subject, examCode) " +
            "VALUES(#{studentId}, #{practiceDate}, #{practiceType}, #{totalQuestions}, #{correctCount}, " +
            "#{wrongCount}, #{score}, #{totalScore}, #{accuracy}, #{duration}, #{subject}, #{examCode})")
    int add(StudyRecord record);

    /**
     * 查询学生的学习记录（按日期倒序）
     */
    @Select("SELECT * FROM study_record WHERE studentId = #{studentId} ORDER BY practiceDate DESC")
    List<StudyRecord> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 查询学生指定日期范围的学习记录
     */
    @Select("SELECT * FROM study_record WHERE studentId = #{studentId} " +
            "AND practiceDate BETWEEN #{startDate} AND #{endDate} ORDER BY practiceDate ASC")
    List<StudyRecord> findByDateRange(@Param("studentId") Integer studentId,
                                      @Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);

    /**
     * 查询学生最近N天的学习记录（用于曲线图）
     */
    @Select("SELECT * FROM study_record WHERE studentId = #{studentId} " +
            "AND practiceDate >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) ORDER BY practiceDate ASC")
    List<StudyRecord> findRecentDays(@Param("studentId") Integer studentId, @Param("days") Integer days);

    /**
     * 按日期统计每天的练习情况（用于曲线图）
     */
    @Select("SELECT DATE(practiceDate) as date, " +
            "SUM(totalQuestions) as totalQuestions, " +
            "SUM(correctCount) as correctCount, " +
            "SUM(wrongCount) as wrongCount, " +
            "AVG(accuracy) as avgAccuracy, " +
            "COUNT(*) as practiceCount " +
            "FROM study_record WHERE studentId = #{studentId} " +
            "AND practiceDate >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(practiceDate) ORDER BY date ASC")
    List<Map<String, Object>> getDailyStats(@Param("studentId") Integer studentId, @Param("days") Integer days);

    /**
     * 统计学生总练习次数
     */
    @Select("SELECT COUNT(*) FROM study_record WHERE studentId = #{studentId}")
    int countByStudentId(@Param("studentId") Integer studentId);

    /**
     * 统计学生总做题数
     */
    @Select("SELECT IFNULL(SUM(totalQuestions), 0) FROM study_record WHERE studentId = #{studentId}")
    int sumTotalQuestions(@Param("studentId") Integer studentId);

    /**
     * 统计学生平均正确率
     */
    @Select("SELECT IFNULL(AVG(accuracy), 0) FROM study_record WHERE studentId = #{studentId}")
    Double avgAccuracy(@Param("studentId") Integer studentId);

    /**
     * 获取学生学习概览统计
     */
    @Select("SELECT " +
            "COUNT(*) as practiceCount, " +
            "IFNULL(SUM(totalQuestions), 0) as totalQuestions, " +
            "IFNULL(SUM(correctCount), 0) as totalCorrect, " +
            "IFNULL(SUM(wrongCount), 0) as totalWrong, " +
            "IFNULL(AVG(accuracy), 0) as avgAccuracy, " +
            "IFNULL(SUM(duration), 0) as totalDuration " +
            "FROM study_record WHERE studentId = #{studentId}")
    Map<String, Object> getStudentOverview(@Param("studentId") Integer studentId);

    /**
     * 查询班级所有学生的学习概览（老师端使用）
     */
    @Select("SELECT s.studentId, s.studentName, s.clazz, " +
            "IFNULL(COUNT(sr.id), 0) as practiceCount, " +
            "IFNULL(SUM(sr.totalQuestions), 0) as totalQuestions, " +
            "IFNULL(AVG(sr.accuracy), 0) as avgAccuracy " +
            "FROM student s LEFT JOIN study_record sr ON s.studentId = sr.studentId " +
            "WHERE s.clazz = #{clazz} " +
            "GROUP BY s.studentId ORDER BY avgAccuracy DESC")
    List<Map<String, Object>> getClassOverview(@Param("clazz") String clazz);

    /**
     * 删除学习记录
     */
    @Delete("DELETE FROM study_record WHERE id = #{id}")
    int delete(@Param("id") Integer id);
}
