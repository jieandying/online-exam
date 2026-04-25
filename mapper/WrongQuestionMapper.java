package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.WrongQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 错题记录Mapper
 */
@Mapper
public interface WrongQuestionMapper {

        /**
         * 添加错题记录
         */
        @Insert("INSERT INTO wrong_question(studentId, questionType, questionId, wrongCount, lastWrongTime, mastered, "
                        +
                        "questionContent, correctAnswer, wrongAnswer, score, subject, analysis) " +
                        "VALUES(#{studentId}, #{questionType}, #{questionId}, #{wrongCount}, #{lastWrongTime}, #{mastered}, "
                        +
                        "#{questionContent}, #{correctAnswer}, #{wrongAnswer}, #{score}, #{subject}, #{analysis})")
        int add(WrongQuestion wrongQuestion);

        /**
         * 更新错题记录
         */
        @Update("UPDATE wrong_question SET wrongCount = #{wrongCount}, lastWrongTime = #{lastWrongTime}, " +
                        "wrongAnswer = #{wrongAnswer}, mastered = #{mastered}, analysis = #{analysis}, " +
                        "correctAnswer = #{correctAnswer}, questionContent = #{questionContent}, subject = #{subject} WHERE id = #{id}")
        int update(WrongQuestion wrongQuestion);

        /**
         * 根据学生ID和题目信息查找错题
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} AND questionType = #{questionType} " +
                        "AND questionId = #{questionId}")
        WrongQuestion findByStudentAndQuestion(@Param("studentId") Integer studentId,
                        @Param("questionType") Integer questionType,
                        @Param("questionId") Integer questionId);

        /**
         * 查询学生的所有错题（未掌握的）
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0 " +
                        "ORDER BY lastWrongTime DESC")
        List<WrongQuestion> findByStudentId(@Param("studentId") Integer studentId);

        /**
         * 查询学生的所有错题（包括已掌握）
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} ORDER BY lastWrongTime DESC")
        List<WrongQuestion> findAllByStudentId(@Param("studentId") Integer studentId);

        /**
         * 按题目类型查询错题
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} AND questionType = #{questionType} " +
                        "AND mastered = 0 ORDER BY wrongCount DESC")
        List<WrongQuestion> findByType(@Param("studentId") Integer studentId,
                        @Param("questionType") Integer questionType);

        /**
         * 标记错题为已掌握
         */
        @Update("UPDATE wrong_question SET mastered = 1 WHERE id = #{id}")
        int markAsMastered(@Param("id") Integer id);

        /**
         * 删除错题记录
         */
        @Delete("DELETE FROM wrong_question WHERE id = #{id}")
        int delete(@Param("id") Integer id);

        /**
         * 统计学生错题数量
         */
        @Select("SELECT COUNT(*) FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0")
        int countByStudentId(@Param("studentId") Integer studentId);

        /**
         * 按科目统计错题
         */
        @Select("SELECT subject, COUNT(*) as count, SUM(wrongCount) as totalWrongCount FROM wrong_question WHERE studentId = #{studentId} "
                        +
                        "AND mastered = 0 GROUP BY subject ORDER BY totalWrongCount DESC")
        List<java.util.Map<String, Object>> countBySubject(@Param("studentId") Integer studentId);

        /**
         * 按科目和题型统计错题
         */
        @Select("SELECT subject, questionType, COUNT(*) as count, SUM(wrongCount) as totalWrongCount " +
                        "FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0 " +
                        "GROUP BY subject, questionType ORDER BY totalWrongCount DESC")
        List<java.util.Map<String, Object>> countBySubjectAndType(@Param("studentId") Integer studentId);

        /**
         * 获取错误次数最多的科目
         */
        @Select("SELECT subject, COUNT(*) as count, SUM(wrongCount) as totalWrongCount " +
                        "FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0 " +
                        "GROUP BY subject ORDER BY totalWrongCount DESC LIMIT #{limit}")
        List<java.util.Map<String, Object>> getTopWeakSubjects(@Param("studentId") Integer studentId,
                        @Param("limit") Integer limit);

        /**
         * 按科目获取错题列表
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} AND subject = #{subject} " +
                        "AND mastered = 0 ORDER BY wrongCount DESC")
        List<WrongQuestion> findBySubject(@Param("studentId") Integer studentId, @Param("subject") String subject);

        /**
         * 按错误次数排序查询错题（降序）
         * 
         * @param studentId 学生ID
         * @return 错题列表，按 wrongCount 降序
         */
        @Select("SELECT * FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0 " +
                        "ORDER BY wrongCount DESC, lastWrongTime DESC")
        List<WrongQuestion> findByStudentIdOrderByWrongCount(@Param("studentId") Integer studentId);

        /**
         * 按推荐分排序查询错题（综合 错误次数×权重 + 时间衰减因子）
         * 推荐分 = wrongCount * 10 - DATEDIFF(CURDATE(), lastWrongTime) * 0.5
         * 错误次数越多、距今越近的题目排得越靠前
         * 
         * @param studentId 学生ID
         * @return 错题列表，按推荐分降序
         */
        @Select("SELECT *, (wrongCount * 10 - DATEDIFF(CURDATE(), lastWrongTime) * 0.5) AS recommendScore " +
                        "FROM wrong_question WHERE studentId = #{studentId} AND mastered = 0 " +
                        "ORDER BY recommendScore DESC")
        List<WrongQuestion> findByStudentIdOrderByRecommend(@Param("studentId") Integer studentId);

        /**
         * 根据ID查询错题记录
         * 
         * @param id 错题记录ID
         * @return 错题记录
         */
        @Select("SELECT * FROM wrong_question WHERE id = #{id}")
        WrongQuestion findById(@Param("id") Integer id);

        /**
         * 获取某场考试所有参考学生的错题记录（用于试卷质量分析）
         */
        @Select("SELECT wq.questionId, wq.questionType, wq.studentId " +
                "FROM wrong_question wq " +
                "WHERE wq.studentId IN (SELECT DISTINCT studentId FROM score WHERE examCode = #{examCode})")
        List<java.util.Map<String, Object>> findWrongByExamStudents(@Param("examCode") Integer examCode);
}
