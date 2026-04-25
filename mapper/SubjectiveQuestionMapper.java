package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 主观题Mapper
 */
@Mapper
public interface SubjectiveQuestionMapper {

        @Select("SELECT * FROM subjective_question WHERE questionId = #{questionId}")
        SubjectiveQuestion findById(Integer questionId);

        @Select("SELECT * FROM subjective_question")
        IPage<SubjectiveQuestion> findAll(Page<?> page);

        /**
         * 查询最后一条记录的questionId
         * 
         * @return SubjectiveQuestion
         */
        @Select("select questionId from subjective_question order by questionId desc limit 1")
        SubjectiveQuestion findOnlyQuestionId();

        @Select("SELECT * FROM subjective_question WHERE subject = #{subject}")
        List<SubjectiveQuestion> findBySubject(String subject);

        @Select("SELECT questionId FROM subjective_question WHERE subject = #{subject} ORDER BY RAND() LIMIT #{count}")
        List<Integer> findRandomBySubject(@Param("subject") String subject, @Param("count") Integer count);

        @Select("SELECT * FROM subjective_question WHERE questionId IN " +
                        "(SELECT questionId FROM paper_manage WHERE questionType = 4 AND paperId = #{paperId})")
        List<SubjectiveQuestion> findByIdAndType(Integer paperId);

        @Options(useGeneratedKeys = true, keyProperty = "questionId")
        @Insert("INSERT INTO subjective_question(subject, question, referenceAnswer, score, section, level, analysis) "
                        +
                        "VALUES(#{subject}, #{question}, #{referenceAnswer}, #{score}, #{section}, #{level}, #{analysis})")
        int add(SubjectiveQuestion question);

        @Update("UPDATE subjective_question SET subject=#{subject}, question=#{question}, " +
                        "referenceAnswer=#{referenceAnswer}, score=#{score}, section=#{section}, " +
                        "level=#{level}, analysis=#{analysis} WHERE questionId=#{questionId}")
        int update(SubjectiveQuestion question);

        @Delete("DELETE FROM subjective_question WHERE questionId = #{questionId}")
        int delete(Integer questionId);

        /**
         * 按科目和难度查询（自助组卷用）
         */
        @Select("<script>" +
                        "SELECT questionId FROM subjective_question WHERE subject = #{subject} " +
                        "<if test='level != null and level != \"\"'> AND level = #{level} </if>" +
                        "ORDER BY RAND() LIMIT #{count}" +
                        "</script>")
        List<Integer> findBySubjectAndLevel(@Param("subject") String subject, @Param("level") String level,
                        @Param("count") Integer count);

        /** 统计总题数 */
        @Select("SELECT COUNT(*) FROM subjective_question")
        int countAll();
}
