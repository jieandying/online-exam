package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rabbiter.oes.entity.FaceChangeRequest;

import java.util.List;

/**
 * 人脸修改申请服务接口
 * 提供学生提交申请和管理员审批的业务逻辑
 */
public interface FaceChangeRequestService {

    /**
     * 学生提交人脸修改申请
     * 
     * @param studentId 学生ID
     * @param reason    申请原因
     * @return 操作结果消息
     */
    String submitRequest(Integer studentId, String reason);

    /**
     * 查询学生的所有申请记录
     * 
     * @param studentId 学生ID
     * @return 申请记录列表
     */
    List<FaceChangeRequest> getMyRequests(Integer studentId);

    /**
     * 分页查询所有申请记录（管理员/教师用）
     * 
     * @param page     页码
     * @param pageSize 每页条数
     * @param status   状态筛选
     * @return 分页结果
     */
    IPage<FaceChangeRequest> getAllRequests(Integer page, Integer pageSize, String status);

    /**
     * 审批申请
     * 
     * @param id            申请ID
     * @param status        审批结果（approved/rejected）
     * @param reviewerId    审批人ID
     * @param reviewerName  审批人姓名
     * @param reviewComment 审批意见
     * @return 操作结果消息
     */
    String reviewRequest(Integer id, String status, Integer reviewerId,
            String reviewerName, String reviewComment);
}
