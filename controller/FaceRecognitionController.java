package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.FaceRecognitionRequest;
import com.rabbiter.oes.entity.FaceRecognitionResult;
import com.rabbiter.oes.mapper.FaceChangeRequestMapper;
import com.rabbiter.oes.service.FaceRecognitionService;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 人脸识别控制器
 * 提供人脸注册和验证进入考试的接口
 */
@RestController
@RequestMapping("/face")
public class FaceRecognitionController {
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private FaceChangeRequestMapper faceChangeRequestMapper;
    
    /**
     * 注册人脸
     * POST /face/register
     * 请求体: { "studentId": 1, "faceImage": "base64..." }
     */
    @PostMapping("/register")
    public ApiResult registerFace(@RequestBody FaceRecognitionRequest request) {
        System.out.println("人脸注册请求 - 学生ID: " + request.getStudentId());
        
        if (request.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        
        if (request.getFaceImage() == null || request.getFaceImage().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "人脸图片不能为空", null);
        }
        
        FaceRecognitionResult result = faceRecognitionService.registerFace(
                request.getStudentId(), 
                request.getFaceImage()
        );
        
        if (result.isSuccess()) {
            // 注册成功：同步将该学生最新的 approved 申请标记为 completed
            try {
                faceChangeRequestMapper.markCompleted(request.getStudentId());
            } catch (Exception e) {
                // 即使状态更新失败，注册本身已成功，不影响主流程
                System.err.println("标记申请为completed时异常（不影响注册）: " + e.getMessage());
            }
            return ApiResultHandler.buildApiResult(200, result.getMessage(), result);
        } else {
            return ApiResultHandler.buildApiResult(400, result.getMessage(), result);
        }
    }
    
    /**
     * 验证人脸进入考试
     * POST /face/verify
     * 请求体: { "studentId": 1, "examCode": 1001, "faceImage": "base64..." }
     */
    @PostMapping("/verify")
    public ApiResult verifyFaceForExam(@RequestBody FaceRecognitionRequest request) {
        System.out.println("人脸验证请求 - 学生ID: " + request.getStudentId() + ", 考试编号: " + request.getExamCode());
        
        if (request.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        
        if (request.getExamCode() == null) {
            return ApiResultHandler.buildApiResult(400, "考试编号不能为空", null);
        }
        
        if (request.getFaceImage() == null || request.getFaceImage().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "人脸图片不能为空", null);
        }
        
        FaceRecognitionResult result = faceRecognitionService.verifyFaceForExam(request);
        
        if (result.isSuccess()) {
            return ApiResultHandler.buildApiResult(200, result.getMessage(), result);
        } else {
            return ApiResultHandler.buildApiResult(400, result.getMessage(), result);
        }
    }
    
    /**
     * 检查学生是否已注册人脸
     * GET /face/check/{studentId}
     */
    @GetMapping("/check/{studentId}")
    public ApiResult checkFaceRegistered(@PathVariable("studentId") Integer studentId) {
        System.out.println("检查人脸注册状态 - 学生ID: " + studentId);
        
        if (studentId == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        
        boolean hasRegistered = faceRecognitionService.hasFaceRegistered(studentId);
        
        FaceRecognitionResult result = new FaceRecognitionResult();
        result.setStudentId(studentId);
        result.setSuccess(hasRegistered);
        result.setMessage(hasRegistered ? "已注册人脸" : "未注册人脸");
        
        return ApiResultHandler.buildApiResult(200, "查询成功", result);
    }
    
    /**
     * 更新人脸数据（重新注册）
     * PUT /face/update
     * 请求体: { "studentId": 1, "faceImage": "base64..." }
     */
    @PutMapping("/update")
    public ApiResult updateFace(@RequestBody FaceRecognitionRequest request) {
        System.out.println("更新人脸请求 - 学生ID: " + request.getStudentId());
        
        if (request.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        
        if (request.getFaceImage() == null || request.getFaceImage().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "人脸图片不能为空", null);
        }
        
        FaceRecognitionResult result = faceRecognitionService.registerFace(
                request.getStudentId(), 
                request.getFaceImage()
        );
        
        if (result.isSuccess()) {
            result.setMessage("人脸更新成功");
            return ApiResultHandler.buildApiResult(200, result.getMessage(), result);
        } else {
            return ApiResultHandler.buildApiResult(400, result.getMessage(), result);
        }
    }
}
