package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.JudgeQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

//判断题

@Mapper
public interface JudgeQuestionMapper {

    @Select("select * from judge_question where questionId in (select questionId from paper_manage where questionType = 3 and paperId = #{paperId})")
    List<JudgeQuestion> findByIdAndType(Integer paperId);

    @Select("select * from judge_question")
    IPage<JudgeQuestion> findAll(Page page);

    /**
     * 查询最后一条记录的questionId
     * @return JudgeQuestion
     */
    @Select("select questionId from judge_question order by questionId desc limit 1")
    JudgeQuestion findOnlyQuestionId();

    @Insert("insert into judge_question(subject,question,answer,analysis,level,section) values " +
            "(#{subject},#{question},#{answer},#{analysis},#{level},#{section})")
    int add(JudgeQuestion judgeQuestion);

    @Select("select questionId from judge_question  where subject=#{subject}  order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update judge_question set subject = #{subject}, question = #{question}, answer = #{answer}, section = #{section}, analysis = #{analysis}, level = #{level} where questionId = #{questionId}")
    int edit(JudgeQuestion judgeQuestion);

    /**
     * 随机获取指定数量的判断题
     */
    @Select("SELECT * FROM judge_question ORDER BY RAND() LIMIT #{count}")
    List<JudgeQuestion> findRandom(@Param("count") Integer count);

    /**
     * 根据ID查询单个题目
     */
    @Select("SELECT * FROM judge_question WHERE questionId = #{questionId}")
    JudgeQuestion findById(@Param("questionId") Integer questionId);

    /**
     * 按科目和难度查询（自助组卷用）
     */
    @Select("<script>" +
            "SELECT questionId FROM judge_question WHERE subject = #{subject} " +
            "<if test='level != null and level != \"\"'> AND level = #{level} </if>" +
            "ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Integer> findBySubjectAndLevel(@Param("subject") String subject, @Param("level") String level, @Param("count") Integer count);

    /** 统计总题数 */
    @Select("SELECT COUNT(*) FROM judge_question")
    int countAll();
}
