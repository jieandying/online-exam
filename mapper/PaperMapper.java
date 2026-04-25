package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.PaperManage;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaperMapper {
    @Select("select paperId, questionType,questionId from paper_manage")
    List<PaperManage> findAll();

    @Select("select paperId, questionType,questionId from paper_manage where paperId = #{paperId}")
    List<PaperManage> findById(Integer paperId);

    @Insert("insert into paper_manage(paperId,questionType,questionId) values " +
            "(#{paperId},#{questionType},#{questionId})")
    int add(PaperManage paperManage);

    @Delete("delete from paper_manage where paperId = #{paperId} and questionType = #{type} and questionId = #{questionId}")
    void delete(@Param("paperId") String paperId, @Param("type") String type, @Param("questionId") String questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    @Delete("DELETE FROM paper_manage WHERE paperId = #{paperId}")
    void deleteByPaperId(@Param("paperId") Integer paperId);

    /**
     * 一次 SQL 查询：找出含有指定科目题目的所有正式试卷条目（paperId + questionType + questionId）
     * 算法：UNION 四张题目表确定 paperId 集合，再关联 paper_manage 取全部题目行
     * 时间复杂度：O(1) SQL 调用，Java 侧 O(m) 分组，m 为命中行数
     */
    @Select("<script>" +
            "SELECT pm.paperId, pm.questionType, pm.questionId FROM paper_manage pm " +
            "WHERE pm.paperId IN (" +
            "  SELECT DISTINCT p1.paperId FROM paper_manage p1 " +
            "  INNER JOIN multi_question mq ON p1.questionType = 1 AND p1.questionId = mq.questionId " +
            "  WHERE mq.subject IN <foreach collection='subjects' item='s' open='(' close=')' separator=','>#{s}</foreach> " +
            "  UNION " +
            "  SELECT DISTINCT p2.paperId FROM paper_manage p2 " +
            "  INNER JOIN fill_question fq ON p2.questionType = 2 AND p2.questionId = fq.questionId " +
            "  WHERE fq.subject IN <foreach collection='subjects' item='s' open='(' close=')' separator=','>#{s}</foreach> " +
            "  UNION " +
            "  SELECT DISTINCT p3.paperId FROM paper_manage p3 " +
            "  INNER JOIN judge_question jq ON p3.questionType = 3 AND p3.questionId = jq.questionId " +
            "  WHERE jq.subject IN <foreach collection='subjects' item='s' open='(' close=')' separator=','>#{s}</foreach> " +
            "  UNION " +
            "  SELECT DISTINCT p4.paperId FROM paper_manage p4 " +
            "  INNER JOIN subjective_question sq ON p4.questionType = 4 AND p4.questionId = sq.questionId " +
            "  WHERE sq.subject IN <foreach collection='subjects' item='s' open='(' close=')' separator=','>#{s}</foreach> " +
            ") ORDER BY pm.paperId" +
            "</script>")
    List<PaperManage> findPaperItemsBySubjects(@Param("subjects") List<String> subjects);
}
