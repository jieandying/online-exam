package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamManageMapper {
//    @Select("select * from exam_manage")
//    List<ExamManage> findAll();

    @Select("select * from exam_manage ORDER BY examCode DESC")
    IPage<ExamManage> findAll(Page page);

    @Select("select * from exam_manage where examCode = #{examCode}")
    ExamManage findById(Integer examCode);

    @Delete("delete from exam_manage where examCode = #{examCode}")
    int delete(Integer examCode);

    @Update("update exam_manage set description = #{description},source = #{source},paperId = #{paperId}," +
            "examDate = #{examDate},totalTime = #{totalTime},grade = #{grade},term = #{term}," +
            "major = #{major},institute = #{institute},totalScore = #{totalScore}," +
            "type = #{type},tips = #{tips}," +
            "examStartTime = #{examStartTime},examEndTime = #{examEndTime}," +
            "multiScore = #{multiScore},fillScore = #{fillScore}," +
            "judgeScore = #{judgeScore},subjectiveScore = #{subjectiveScore}" +
            " where examCode = #{examCode}")
    int update(ExamManage exammanage);

    @Options(useGeneratedKeys = true,keyProperty = "examCode")
    @Insert("insert into exam_manage(description,source,paperId,examDate,totalTime,grade,term,major,institute,totalScore,type,tips,examStartTime,examEndTime,multiScore,fillScore,judgeScore,subjectiveScore)" +
            " values(#{description},#{source},#{paperId},#{examDate},#{totalTime},#{grade},#{term},#{major},#{institute},#{totalScore},#{type},#{tips},#{examStartTime},#{examEndTime},#{multiScore},#{fillScore},#{judgeScore},#{subjectiveScore})")
    int add(ExamManage exammanage);

    /**
     * 查询最后一条记录的paperId,返回给前端达到自增效果
     * @return paperId
     */
    @Select("select paperId from exam_manage order by paperId desc limit 1")
    ExamManage findOnlyPaperId();

    /**
     * 按 paperId 列表批量查询考试信息（用于获取考试名称）
     */
    @Select("<script>" +
            "SELECT examCode, source, paperId FROM exam_manage " +
            "WHERE paperId IN <foreach collection='paperIds' item='id' open='(' close=')' separator=','>#{id}</foreach>" +
            "</script>")
    List<ExamManage> findByPaperIds(@Param("paperIds") List<Integer> paperIds);
}
