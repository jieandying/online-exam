package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.FaceChangeRequest;
import com.rabbiter.oes.service.FaceChangeRequestService;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 人脸修改申请控制器
 * 提供学生提交申请和管理员审批的REST接口
 */
@RestController
@RequestMapping("/faceChange")
public class FaceChangeRequestController {

    @Autowired
    private FaceChangeRequestService faceChangeRequestService;

    /**
     * 学生提交人脸修改申请
     * POST /faceChange/apply
     * 
     * @param params 请求体，包含 studentId（学生ID）和 reason（申请原因）
     * @return ApiResult 操作结果
     */
    @PostMapping("/apply")
    public ApiResult apply(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        String reason = (String) params.get("reason");

        if (studentId == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        if (reason == null || reason.trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请填写申请原因", null);
        }

        String result = faceChangeRequestService.submitRequest(studentId, reason.trim());

        if (result.contains("成功")) {
            return ApiResultHandler.buildApiResult(200, result, null);
        } else {
            return ApiResultHandler.buildApiResult(400, result, null);
        }
    }

    /**
     * 查询学生的所有申请记录
     * GET /faceChange/myRequests/{studentId}
     * 
     * @param studentId 学生ID
     * @return ApiResult 包含申请记录列表
     */
    @GetMapping("/myRequests/{studentId}")
    public ApiResult getMyRequests(@PathVariable("studentId") Integer studentId) {
        if (studentId == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }

        List<FaceChangeRequest> requests = faceChangeRequestService.getMyRequests(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", requests);
    }

    /**
     * 分页查询所有申请记录（管理员/教师用）
     * GET /faceChange/list?page=1&pageSize=10&status=pending
     * 
     * @param page     页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param status   状态筛选（可选）
     * @return ApiResult 包含分页数据
     */
    @GetMapping("/list")
    public ApiResult getAllRequests(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String status) {

        IPage<FaceChangeRequest> result = faceChangeRequestService.getAllRequests(page, pageSize, status);
        return ApiResultHandler.buildApiResult(200, "查询成功", result);
    }

    /**
     * 审批申请（管理员/教师操作）
     * PUT /faceChange/review
     * 
     * @param params 请求体，包含 id（申请ID）、status（审批结果）、
     *               reviewerId（审批人ID）、reviewerName（审批人姓名）、reviewComment（审批意见）
     * @return ApiResult 操作结果
     */
    @PutMapping("/review")
    public ApiResult review(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        Integer reviewerId = params.get("reviewerId") != null ? (Integer) params.get("reviewerId") : null;
        String reviewerName = (String) params.get("reviewerName");
        String reviewComment = (String) params.get("reviewComment");

        if (id == null) {
            return ApiResultHandler.buildApiResult(400, "申请ID不能为空", null);
        }
        if (status == null || status.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "审批状态不能为空", null);
        }

        String result = faceChangeRequestService.reviewRequest(id, status, reviewerId, reviewerName, reviewComment);

        if (result.contains("失败") || result.contains("无效") || result.contains("不存在")) {
            return ApiResultHandler.buildApiResult(400, result, null);
        }
        return ApiResultHandler.buildApiResult(200, result, null);
    }
}
