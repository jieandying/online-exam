package com.rabbiter.oes.entity;

import java.util.Date;

/**
 * 人脸修改申请实体类
 * 对应数据库表 face_change_request，用于存储学生申请修改人脸的记录
 */
public class FaceChangeRequest {

    /** 主键ID */
    private Integer id;

    /** 学生ID */
    private Integer studentId;

    /** 学生姓名（非数据库字段，用于前端展示） */
    private String studentName;

    /** 申请原因 */
    private String reason;

    /** 状态：pending-待审批, approved-已通过, rejected-已拒绝 */
    private String status;

    /** 审批人ID */
    private Integer reviewerId;

    /** 审批人姓名 */
    private String reviewerName;

    /** 审批意见 */
    private String reviewComment;

    /** 申请时间 */
    private Date createTime;

    /** 审批时间 */
    private Date updateTime;

    /**
     * 获取主键ID
     * 
     * @return 主键ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置主键ID
     * 
     * @param id 主键ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取学生ID
     * 
     * @return 学生ID
     */
    public Integer getStudentId() {
        return studentId;
    }

    /**
     * 设置学生ID
     * 
     * @param studentId 学生ID
     */
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    /**
     * 获取学生姓名（关联查询字段）
     * 
     * @return 学生姓名
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * 设置学生姓名
     * 
     * @param studentName 学生姓名
     */
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    /**
     * 获取申请原因
     * 
     * @return 申请原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置申请原因
     * 
     * @param reason 申请原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取审批状态
     * 
     * @return 状态：pending/approved/rejected
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置审批状态
     * 
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取审批人ID
     * 
     * @return 审批人ID
     */
    public Integer getReviewerId() {
        return reviewerId;
    }

    /**
     * 设置审批人ID
     * 
     * @param reviewerId 审批人ID
     */
    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    /**
     * 获取审批人姓名
     * 
     * @return 审批人姓名
     */
    public String getReviewerName() {
        return reviewerName;
    }

    /**
     * 设置审批人姓名
     * 
     * @param reviewerName 审批人姓名
     */
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    /**
     * 获取审批意见
     * 
     * @return 审批意见
     */
    public String getReviewComment() {
        return reviewComment;
    }

    /**
     * 设置审批意见
     * 
     * @param reviewComment 审批意见
     */
    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    /**
     * 获取申请时间
     * 
     * @return 申请时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置申请时间
     * 
     * @param createTime 申请时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取审批时间
     * 
     * @return 审批时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置审批时间
     * 
     * @param updateTime 审批时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
