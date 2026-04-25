package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.ViolationRecord;
import com.rabbiter.oes.serviceimpl.ViolationServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 违规检测控制器
 * 用于处理考试期间的违规行为检测和记录
 */
@RestController
@RequestMapping("/violation")
public class ViolationController {

    @Autowired
    private ViolationServiceImpl violationService;

    /**
     * 记录违规行为
     * 前端检测到违规行为时调用此接口
     */
    @PostMapping("/record")
    public ApiResult recordViolation(@RequestBody ViolationRecord record, HttpServletRequest request) {
        // 获取客户端IP
        String clientIp = getClientIp(request);
        record.setClientIp(clientIp);
        
        // 获取User-Agent
        String userAgent = request.getHeader("User-Agent");
        record.setUserAgent(userAgent);
        
        int result = violationService.recordViolation(record);
        if (result > 0) {
            // 检查是否超过违规阈值（默认5次）
            boolean exceeded = violationService.isViolationExceeded(
                    record.getStudentId(), record.getExamCode(), 5);
            Map<String, Object> data = new HashMap<>();
            data.put("recorded", true);
            data.put("exceeded", exceeded);
            if (exceeded) {
                return ApiResultHandler.buildApiResult(200, "违规记录成功，警告：违规次数已超过阈值！", data);
            }
            return ApiResultHandler.buildApiResult(200, "违规记录成功", data);
        }
        return ApiResultHandler.buildApiResult(400, "违规记录失败", null);
    }

    /**
     * 批量记录违规行为
     */
    @PostMapping("/records")
    public ApiResult recordViolations(@RequestBody List<ViolationRecord> records, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        int successCount = 0;
        for (ViolationRecord record : records) {
            record.setClientIp(clientIp);
            record.setUserAgent(userAgent);
            if (violationService.recordViolation(record) > 0) {
                successCount++;
            }
        }
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("total", records.size());
        batchData.put("success", successCount);
        return ApiResultHandler.buildApiResult(200, "批量记录完成", batchData);
    }

    /**
     * 查询学生在某次考试中的违规记录
     */
    @GetMapping("/student/{studentId}/exam/{examCode}")
    public ApiResult getViolationsByStudentAndExam(
            @PathVariable("studentId") Integer studentId,
            @PathVariable("examCode") Integer examCode) {
        List<ViolationRecord> records = violationService.getViolationsByStudentAndExam(studentId, examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", records);
    }

    /**
     * 查询某次考试的所有违规记录
     */
    @GetMapping("/exam/{examCode}")
    public ApiResult getViolationsByExam(@PathVariable("examCode") Integer examCode) {
        List<ViolationRecord> records = violationService.getViolationsByExam(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", records);
    }

    /**
     * 分页查询某次考试的违规记录
     */
    @GetMapping("/exam/{examCode}/{page}/{size}")
    public ApiResult getViolationsByExamPage(
            @PathVariable("examCode") Integer examCode,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {
        Page<ViolationRecord> violationPage = new Page<>(page, size);
        IPage<ViolationRecord> result = violationService.getViolationsByExamPage(violationPage, examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", result);
    }

    /**
     * 获取学生在某次考试中的违规统计
     */
    @GetMapping("/stats/{studentId}/{examCode}")
    public ApiResult getViolationStats(
            @PathVariable("studentId") Integer studentId,
            @PathVariable("examCode") Integer examCode) {
        Map<String, Object> stats = violationService.getViolationStats(studentId, examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", stats);
    }

    /**
     * 检查学生是否超过违规阈值
     */
    @GetMapping("/check/{studentId}/{examCode}")
    public ApiResult checkViolationExceeded(
            @PathVariable("studentId") Integer studentId,
            @PathVariable("examCode") Integer examCode,
            @RequestParam(value = "threshold", defaultValue = "5") Integer threshold) {
        boolean exceeded = violationService.isViolationExceeded(studentId, examCode, threshold);
        int count = violationService.getViolationsByStudentAndExam(studentId, examCode).size();
        Map<String, Object> checkData = new HashMap<>();
        checkData.put("exceeded", exceeded);
        checkData.put("count", count);
        checkData.put("threshold", threshold);
        return ApiResultHandler.buildApiResult(200, "检查完成", checkData);
    }

    /**
     * 删除违规记录
     */
    @DeleteMapping("/{id}")
    public ApiResult deleteViolation(@PathVariable("id") Integer id) {
        int result = violationService.deleteViolation(id);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "删除成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }

    /**
     * 删除考试的所有违规记录
     */
    @DeleteMapping("/exam/{examCode}")
    public ApiResult deleteViolationsByExam(@PathVariable("examCode") Integer examCode) {
        int result = violationService.deleteViolationsByExam(examCode);
        return ApiResultHandler.buildApiResult(200, "删除成功", result);
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
