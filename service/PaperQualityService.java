package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.PaperQualityResult;

/**
 * 试卷质量评价服务接口
 */
public interface PaperQualityService {
    
    /**
     * 评价试卷质量
     * @param examCode 考试编码
     * @return 试卷质量评价结果
     */
    PaperQualityResult evaluatePaperQuality(Integer examCode);
    
    /**
     * 计算两次考试的效度（相关系数）
     * @param examCode1 第一次考试编码
     * @param examCode2 第二次考试编码
     * @return 效度值
     */
    Double calculateValidity(Integer examCode1, Integer examCode2);
}
