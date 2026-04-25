package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Score;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScoreMapper {
    /**
     * @param score 添加一条成绩记录
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "scoreId")
    @Insert("insert into score(examCode,studentId,subject,ptScore,etScore,score,answerDate) values(#{examCode},#{studentId},#{subject},#{ptScore},#{etScore},#{score},#{answerDate})")
    int add(Score score);

    @Select("SELECT ptScore FROM score WHERE examCode = #{examCode} AND studentId = #{studentId} LIMIT 1")
    Integer getPtScore(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    @Update("UPDATE score SET etScore = #{etScore}, score = #{etScore} WHERE examCode = #{examCode} AND studentId = #{studentId}")
    int updateEtScore(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId, @Param("etScore") Integer etScore);

    @Select("SELECT scoreId,examCode,studentId,subject,ptScore,etScore,score,answerDate from score order by scoreId desc")
    List<Score> findAll();

    // 分页
    @Select("select scoreId,examCode,studentId,subject,ptScore,etScore,score,answerDate from score where studentId = #{studentId} order by scoreId asc")
    IPage<Score> findById(Page<?> page, @Param("studentId") Integer studentId);

    // 不分页
    // @Select("select
    // scoreId,examCode,studentId,subject,ptScore,etScore,score,answerDate from
    // score where studentId = #{studentId}")
    // List<Score> findById(Integer studentId);

    /**
     *
     * @return 查询每位学生的学科分数。 max其实是假的，为了迷惑老师，达到一次考试考生只参加了一次的效果
     */
    @Select("SELECT scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate " +
            "FROM score WHERE examCode = #{examCode} " +
            "AND scoreId IN (SELECT MAX(scoreId) FROM score WHERE examCode = #{examCode} GROUP BY studentId)")
    List<Score> findByExamCode(Integer examCode);

    /**
     * 获取学生的所有考试历史（包含试卷信息）
     */
    @Select("SELECT s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, " +
            "e.paperId, e.examDate, e.description, e.source " +
            "FROM score s " +
            "JOIN exam_manage e ON s.examCode = e.examCode " +
            "WHERE s.studentId = #{studentId} " +
            "ORDER BY s.answerDate DESC, s.scoreId DESC")
    List<java.util.Map<String, Object>> findHistoryByStudentId(Integer studentId);
    
    /**
     * 按考试编号和学生ID查询成绩记录
     * @param examCode 考试编号
     * @param studentId 学生 ID
     * @return 成绩记录，null 表示尚未提交
     */
    @Select("SELECT scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate " +
            "FROM score WHERE examCode = #{examCode} AND studentId = #{studentId} LIMIT 1")
    Score findByExamCodeAndStudentId(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    @Select("SELECT scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate " +
            "FROM score " +
            "WHERE studentId = #{studentId} AND subject = #{subject} AND DATE(answerDate) = DATE(#{answerDate}) " +
            "LIMIT 1")
    Score findByStudentAndSubject(@Param("studentId") Integer studentId, 
                                   @Param("subject") String subject, 
                                   @Param("answerDate") String answerDate);
}
