package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.StudentProfile;
import java.util.List;
import java.util.Map;

/**
 * K-Means聚类服务接口
 * 用于学生群体画像分析
 */
public interface KMeansClusterService {
    
    /**
     * 获取所有学生画像数据（含聚类结果）
     * @return 学生画像列表
     */
    List<StudentProfile> getStudentProfiles();
    
    /**
     * 执行K-Means聚类分析
     * @param k 聚类数量
     * @return 聚类结果
     */
    Map<String, Object> performClustering(int k);
    
    /**
     * 获取聚类统计信息
     * @return 各聚类的统计数据
     */
    Map<String, Object> getClusterStatistics();
    
    /**
     * 根据班级获取学生画像
     * @param clazz 班级名称
     * @return 该班级学生画像列表
     */
    List<StudentProfile> getStudentProfilesByClass(String clazz);
}
