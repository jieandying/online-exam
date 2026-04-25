package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.StudentProfile;
import com.rabbiter.oes.service.KMeansClusterService;
import com.rabbiter.oes.entity.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * K-Means聚类分析控制器
 * 提供学生群体画像相关接口
 */
@RestController
@RequestMapping("/kmeans")
public class KMeansClusterController {
    
    @Autowired
    private KMeansClusterService kMeansClusterService;
    
    /**
     * 获取学生画像数据（含聚类结果）
     */
    @GetMapping("/profiles")
    public ApiResult<List<StudentProfile>> getStudentProfiles() {
        try {
            List<StudentProfile> profiles = kMeansClusterService.getStudentProfiles();
            return new ApiResult<>(200, "success", profiles);
        } catch (Exception e) {
            return new ApiResult<>(500, "获取学生画像失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 执行聚类分析
     * @param k 聚类数量，默认为3
     */
    @GetMapping("/cluster")
    public ApiResult<Map<String, Object>> performClustering(
            @RequestParam(defaultValue = "3") int k) {
        try {
            Map<String, Object> result = kMeansClusterService.performClustering(k);
            return new ApiResult<>(200, "success", result);
        } catch (Exception e) {
            return new ApiResult<>(500, "聚类分析失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取聚类统计信息
     */
    @GetMapping("/statistics")
    public ApiResult<Map<String, Object>> getClusterStatistics() {
        try {
            Map<String, Object> stats = kMeansClusterService.getClusterStatistics();
            return new ApiResult<>(200, "success", stats);
        } catch (Exception e) {
            return new ApiResult<>(500, "获取统计信息失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 根据班级获取学生画像
     */
    @GetMapping("/profiles/class")
    public ApiResult<List<StudentProfile>> getStudentProfilesByClass(
            @RequestParam(required = false) String clazz) {
        try {
            List<StudentProfile> profiles = kMeansClusterService.getStudentProfilesByClass(clazz);
            return new ApiResult<>(200, "success", profiles);
        } catch (Exception e) {
            return new ApiResult<>(500, "获取班级画像失败: " + e.getMessage(), null);
        }
    }
}
