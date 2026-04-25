package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.FaceChangeRequest;
import com.rabbiter.oes.mapper.FaceChangeRequestMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.service.FaceChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人脸修改申请服务实现类
 * 处理学生提交申请和管理员审批的具体业务逻辑
 */
@Service
public class FaceChangeRequestServiceImpl implements FaceChangeRequestService {

    @Autowired
    private FaceChangeRequestMapper faceChangeRequestMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 学生提交人脸修改申请
     * 校验：学生必须已注册人脸，且没有待审批的申请
     * 
     * @param studentId 学生ID
     * @param reason    申请原因
     * @return 操作结果消息
     */
    @Override
    public String submitRequest(Integer studentId, String reason) {
        // 检查学生是否已注册人脸
        String faceData = studentMapper.getFaceData(studentId);
        if (faceData == null || faceData.isEmpty()) {
            return "您尚未注册人脸，无需申请修改";
        }

        // 检查是否已有待审批的申请
        FaceChangeRequest pending = faceChangeRequestMapper.findPendingByStudentId(studentId);
        if (pending != null) {
            return "您已有一条待审批的申请，请等待管理员处理";
        }

        // 创建申请
        FaceChangeRequest request = new FaceChangeRequest();
        request.setStudentId(studentId);
        request.setReason(reason);
        faceChangeRequestMapper.insert(request);

        return "申请提交成功，请等待管理员审批";
    }

    /**
     * 查询学生的所有申请记录
     * 
     * @param studentId 学生ID
     * @return 申请记录列表（按时间倒序）
     */
    @Override
    public List<FaceChangeRequest> getMyRequests(Integer studentId) {
        return faceChangeRequestMapper.findByStudentId(studentId);
    }

    /**
     * 分页查询所有申请记录（管理员/教师使用）
     * 
     * @param page     页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可为空）
     * @return 分页结果
     */
    @Override
    public IPage<FaceChangeRequest> getAllRequests(Integer page, Integer pageSize, String status) {
        Page<FaceChangeRequest> pageParam = new Page<>(page, pageSize);
        return faceChangeRequestMapper.findAllPaged(pageParam, status);
    }

    /**
     * 审批申请
     * 审批通过时会清除学生的旧人脸数据，允许其重新注册
     * 
     * @param id            申请ID
     * @param status        审批结果（approved/rejected）
     * @param reviewerId    审批人ID
     * @param reviewerName  审批人姓名
     * @param reviewComment 审批意见
     * @return 操作结果消息
     */
    @Override
    public String reviewRequest(Integer id, String status, Integer reviewerId,
            String reviewerName, String reviewComment) {
        // 校验状态值
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            return "审批状态无效，只能为 approved 或 rejected";
        }

        // 查询申请记录
        FaceChangeRequest request = faceChangeRequestMapper.findById(id);
        if (request == null) {
            return "申请记录不存在";
        }

        if (!"pending".equals(request.getStatus())) {
            return "该申请已被处理，无法重复审批";
        }

        // 更新审批状态
        int rows = faceChangeRequestMapper.updateStatus(id, status, reviewerId, reviewerName, reviewComment);
        if (rows <= 0) {
            return "审批操作失败";
        }

        // 审批通过：清除学生的人脸数据，允许重新注册
        if ("approved".equals(status)) {
            studentMapper.updateFaceData(request.getStudentId(), null);
            return "审批通过，已清除学生人脸数据，学生可重新注册";
        }

        return "已拒绝该申请";
    }
}
