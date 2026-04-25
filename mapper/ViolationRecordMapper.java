package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ViolationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 违规记录Mapper接口
 */
@Mapper
public interface ViolationRecordMapper {

    /**
     * 添加违规记录
     */
    @Insert("INSERT INTO violation_record(exam_code, student_id, violation_type, description, violation_time, client_ip, user_agent) " +
            "VALUES(#{examCode}, #{studentId}, #{violationType}, #{description}, #{violationTime}, #{clientIp}, #{userAgent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int add(ViolationRecord record);

    /**
     * 根据学生ID和考试编号查询违规记录
     */
    @Select("SELECT id, exam_code, student_id, violation_type, description, violation_time, client_ip, user_agent " +
            "FROM violation_record WHERE student_id = #{studentId} AND exam_code = #{examCode} ORDER BY violation_time DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "exam_code", property = "examCode"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "violation_type", property = "violationType"),
            @Result(column = "description", property = "description"),
            @Result(column = "violation_time", property = "violationTime"),
            @Result(column = "client_ip", property = "clientIp"),
            @Result(column = "user_agent", property = "userAgent")
    })
    List<ViolationRecord> findByStudentAndExam(@Param("studentId") Integer studentId, @Param("examCode") Integer examCode);

    /**
     * 根据考试编号查询所有违规记录
     */
    @Select("SELECT id, exam_code, student_id, violation_type, description, violation_time, client_ip, user_agent " +
            "FROM violation_record WHERE exam_code = #{examCode} ORDER BY violation_time DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "exam_code", property = "examCode"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "violation_type", property = "violationType"),
            @Result(column = "description", property = "description"),
            @Result(column = "violation_time", property = "violationTime"),
            @Result(column = "client_ip", property = "clientIp"),
            @Result(column = "user_agent", property = "userAgent")
    })
    List<ViolationRecord> findByExamCode(@Param("examCode") Integer examCode);

    /**
     * 分页查询考试的违规记录
     */
    @Select("SELECT id, exam_code, student_id, violation_type, description, violation_time, client_ip, user_agent " +
            "FROM violation_record WHERE exam_code = #{examCode} ORDER BY violation_time DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "exam_code", property = "examCode"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "violation_type", property = "violationType"),
            @Result(column = "description", property = "description"),
            @Result(column = "violation_time", property = "violationTime"),
            @Result(column = "client_ip", property = "clientIp"),
            @Result(column = "user_agent", property = "userAgent")
    })
    IPage<ViolationRecord> findByExamCodePage(Page<ViolationRecord> page, @Param("examCode") Integer examCode);

    /**
     * 统计学生在某次考试中的违规次数
     */
    @Select("SELECT COUNT(*) FROM violation_record WHERE student_id = #{studentId} AND exam_code = #{examCode}")
    int countByStudentAndExam(@Param("studentId") Integer studentId, @Param("examCode") Integer examCode);

    /**
     * 统计学生在某次考试中某种类型的违规次数
     */
    @Select("SELECT COUNT(*) FROM violation_record WHERE student_id = #{studentId} AND exam_code = #{examCode} AND violation_type = #{violationType}")
    int countByStudentExamAndType(@Param("studentId") Integer studentId, @Param("examCode") Integer examCode, @Param("violationType") String violationType);

    /**
     * 删除违规记录
     */
    @Delete("DELETE FROM violation_record WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    /**
     * 根据考试编号删除所有违规记录
     */
    @Delete("DELETE FROM violation_record WHERE exam_code = #{examCode}")
    int deleteByExamCode(@Param("examCode") Integer examCode);
}
