package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.FaceChangeRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 人脸修改申请Mapper接口
 * 提供对 face_change_request 表的CRUD操作
 */
@Mapper
public interface FaceChangeRequestMapper {

        /**
         * 插入一条人脸修改申请记录
         * 
         * @param request 申请实体（包含studentId、reason）
         * @return 受影响的记录条数
         */
        @Options(useGeneratedKeys = true, keyProperty = "id")
        @Insert("INSERT INTO face_change_request(studentId, reason, status, createTime) " +
                        "VALUES(#{studentId}, #{reason}, 'pending', NOW())")
        int insert(FaceChangeRequest request);

        /**
         * 根据学生ID查询其所有申请记录（按时间倒序）
         * 
         * @param studentId 学生ID
         * @return 申请记录列表
         */
        @Select("SELECT r.*, s.studentName FROM face_change_request r " +
                        "LEFT JOIN student s ON r.studentId = s.studentId " +
                        "WHERE r.studentId = #{studentId} ORDER BY r.createTime DESC")
        List<FaceChangeRequest> findByStudentId(@Param("studentId") Integer studentId);

        /**
         * 查询学生是否存在待审批的申请
         * 
         * @param studentId 学生ID
         * @return 待审批的申请记录（如果存在）
         */
        @Select("SELECT * FROM face_change_request WHERE studentId = #{studentId} AND status = 'pending' LIMIT 1")
        FaceChangeRequest findPendingByStudentId(@Param("studentId") Integer studentId);

        /**
         * 分页查询所有申请记录（支持按状态筛选）
         * 
         * @param page   分页参数
         * @param status 状态筛选（空字符串表示不筛选）
         * @return 分页结果
         */
        @Select("<script>" +
                        "SELECT r.*, s.studentName FROM face_change_request r " +
                        "LEFT JOIN student s ON r.studentId = s.studentId " +
                        "<where>" +
                        "  <if test=\"status != null and status != ''\">" +
                        "    AND r.status = #{status}" +
                        "  </if>" +
                        "</where>" +
                        " ORDER BY FIELD(r.status, 'pending', 'approved', 'rejected'), r.createTime DESC" +
                        "</script>")
        IPage<FaceChangeRequest> findAllPaged(Page page, @Param("status") String status);

        /**
         * 更新申请状态（审批操作）
         * 
         * @param id            申请ID
         * @param status        新状态（approved/rejected）
         * @param reviewerId    审批人ID
         * @param reviewerName  审批人姓名
         * @param reviewComment 审批意见
         * @return 受影响的记录条数
         */
        @Update("UPDATE face_change_request SET status = #{status}, reviewerId = #{reviewerId}, " +
                        "reviewerName = #{reviewerName}, reviewComment = #{reviewComment}, updateTime = NOW() " +
                        "WHERE id = #{id} AND status = 'pending'")
        int updateStatus(@Param("id") Integer id, @Param("status") String status,
                        @Param("reviewerId") Integer reviewerId, @Param("reviewerName") String reviewerName,
                        @Param("reviewComment") String reviewComment);

        /**
         * 根据ID查询申请记录
         * 
         * @param id 申请ID
         * @return 申请记录
         */
        @Select("SELECT r.*, s.studentName FROM face_change_request r " +
                        "LEFT JOIN student s ON r.studentId = s.studentId " +
                        "WHERE r.id = #{id}")
        FaceChangeRequest findById(@Param("id") Integer id);

        /**
         * 将学生最新的 approved 申请标记为 completed（重新注册成功后调用）
         * 
         * @param studentId 学生ID
         * @return 受影响的记录条数
         */
        @Update("UPDATE face_change_request SET status = 'completed', updateTime = NOW() " +
                        "WHERE studentId = #{studentId} AND status = 'approved' " +
                        "ORDER BY createTime DESC LIMIT 1")
        int markCompleted(@Param("studentId") Integer studentId);
}
