package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.FillQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

//填空题
@Mapper
public interface FillQuestionMapper {

    @Select("select * from fill_question where questionId in (select questionId from paper_manage where questionType = 2 and paperId = #{paperId})")
    List<FillQuestion> findByIdAndType(Integer paperId);

    @Select("select * from fill_question")
    IPage<FillQuestion> findAll(Page page);

    /**
     * 查询最后一条questionId
     * @return FillQuestion
     */
    @Select("select questionId from fill_question order by questionId desc limit 1")
    FillQuestion findOnlyQuestionId();

    @Options(useGeneratedKeys = true,keyProperty ="questionId" )
    @Insert("insert into fill_question(subject,question,answer,analysis,level,section) values " +
            "(#{subject},#{question},#{answer},#{analysis},#{level},#{section})")
    int add(FillQuestion fillQuestion);

    @Select("select questionId from fill_question where subject = #{subject} order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update fill_question set section = #{section}, question = #{question}, answer = #{answer}, level = #{level}, analysis = #{analysis} where questionId = #{questionId}")
    int edit(FillQuestion fillQuestion);

    /**
     * 随机获取指定数量的填空题
     */
    @Select("SELECT * FROM fill_question ORDER BY RAND() LIMIT #{count}")
    List<FillQuestion> findRandom(@Param("count") Integer count);

    /**
     * 根据ID查询单个题目
     */
    @Select("SELECT * FROM fill_question WHERE questionId = #{questionId}")
    FillQuestion findById(@Param("questionId") Integer questionId);

    /**
     * 按科目和难度查询（自助组卷用）
     */
    @Select("<script>" +
            "SELECT questionId FROM fill_question WHERE subject = #{subject} " +
            "<if test='level != null and level != \"\"'> AND level = #{level} </if>" +
            "ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Integer> findBySubjectAndLevel(@Param("subject") String subject, @Param("level") String level, @Param("count") Integer count);

    /** 统计总题数 */
    @Select("SELECT COUNT(*) FROM fill_question")
    int countAll();
}
