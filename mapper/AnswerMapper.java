package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.vo.AnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnswerMapper {

        /**
         * 查询所有题型（UNION），支持按科目/章节/题干模糊搜索 + 难度 LIKE 过滤
         *
         * @param page     分页对象
         * @param subject  科目（空串匹配全部）
         * @param section  章节（空串匹配全部）
         * @param question 题干关键词（空串匹配全部）
         * @param level    难度等级（空串匹配全部，1-5 精确匹配）
         * @return 分页结果
         */
        @Select("select questionId, question, subject, score, section, level, '1' as type from multi_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')"
                        +
                        " union select questionId, question, subject, score, section, level, '3' as type from judge_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')"
                        +
                        " union select questionId, question, subject, score, section, level, '2' as type from fill_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')"
                        +
                        " union select questionId, question, subject, score, section, level, '4' as type from subjective_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')")
        IPage<AnswerVO> findAll(Page<AnswerVO> page,
                        @Param("subject") String subject,
                        @Param("section") String section,
                        @Param("question") String question,
                        @Param("level") String level);

        /**
         * 按题型1（选择题）查询 multi_question 表
         *
         * @param page     分页对象
         * @param subject  科目
         * @param section  章节
         * @param question 题干关键词
         * @param level    难度等级
         * @return 分页结果
         */
        @Select("select questionId, question, subject, score, section, level, '1' as type from multi_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')")
        IPage<AnswerVO> findByType1(Page<AnswerVO> page,
                        @Param("subject") String subject,
                        @Param("section") String section,
                        @Param("question") String question,
                        @Param("level") String level);

        /**
         * 按题型2（填空题）查询 fill_question 表
         *
         * @param page     分页对象
         * @param subject  科目
         * @param section  章节
         * @param question 题干关键词
         * @param level    难度等级
         * @return 分页结果
         */
        @Select("select questionId, question, subject, score, section, level, '2' as type from fill_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')")
        IPage<AnswerVO> findByType2(Page<AnswerVO> page,
                        @Param("subject") String subject,
                        @Param("section") String section,
                        @Param("question") String question,
                        @Param("level") String level);

        /**
         * 按题型3（判断题）查询 judge_question 表
         *
         * @param page     分页对象
         * @param subject  科目
         * @param section  章节
         * @param question 题干关键词
         * @param level    难度等级
         * @return 分页结果
         */
        @Select("select questionId, question, subject, score, section, level, '3' as type from judge_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')")
        IPage<AnswerVO> findByType3(Page<AnswerVO> page,
                        @Param("subject") String subject,
                        @Param("section") String section,
                        @Param("question") String question,
                        @Param("level") String level);

        /**
         * 按题型4（主观题）查询 subjective_question 表
         *
         * @param page     分页对象
         * @param subject  科目
         * @param section  章节
         * @param question 题干关键词
         * @param level    难度等级
         * @return 分页结果
         */
        @Select("select questionId, question, subject, score, section, level, '4' as type from subjective_question where question like concat('%',#{question},'%') and subject like concat('%',#{subject},'%') and section like concat('%',#{section},'%') and level like concat('%',#{level},'%')")
        IPage<AnswerVO> findByType4(Page<AnswerVO> page,
                        @Param("subject") String subject,
                        @Param("section") String section,
                        @Param("question") String question,
                        @Param("level") String level);

        // ==================== 以下为按ID查询单题的方法 ====================

        /**
         * 查选择题
         *
         * @param questionId 选择题id
         * @return 选择题
         */
        @Select("select questionId, subject, question, answerA, answerB, answerC, answerD, rightAnswer, section, level, analysis from multi_question where questionId = #{questionId}")
        MultiQuestion findMultiQuestionById(Long questionId);

        /**
         * 查填空题
         *
         * @param questionId 题目id
         * @return 填空题
         */
        @Select("select questionId, subject, question, answer, analysis, level, section from fill_question where questionId = #{questionId}")
        FillQuestion findFillQuestionById(Long questionId);

        /**
         * 查判断题
         *
         * @param questionId 题目id
         * @return 判断题
         */
        @Select("select questionId, subject, question, answer, analysis, level, section from judge_question where questionId = #{questionId}")
        JudgeQuestion findJudgeQuestionById(Long questionId);

        /**
         * 查主观题
         *
         * @param questionId 题目id
         * @return 主观题
         */
        @Select("select questionId, subject, question, referenceAnswer, score, section, level, analysis from subjective_question where questionId = #{questionId}")
        SubjectiveQuestion findSubjectiveQuestionById(Long questionId);
}
