package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.GeneticPaperRequest;

import java.util.List;
import java.util.Map;

/**
 * 遗传算法智能组卷服务接口
 */
public interface GeneticPaperService {
    
    /**
     * 基于遗传算法智能组卷
     * @param request 组卷约束条件
     * @return 组卷结果
     */
    Map<String, Object> generatePaper(GeneticPaperRequest request);
    
    /**
     * 获取可用科目列表
     * @return 科目列表
     */
    List<String> getAvailableSubjects();
    
    /**
     * 获取指定科目的章节列表
     * @param subject 科目名称
     * @return 章节列表
     */
    List<String> getSections(String subject);
    
    /**
     * 获取题库统计信息
     * @return 题库统计
     */
    Map<String, Object> getQuestionBankStats();
    
    /**
     * 将组卷结果创建为考试
     * @param examInfo 考试信息
     * @param paperResult 组卷结果
     * @return 创建结果
     */
    Map<String, Object> createExamFromPaper(Map<String, Object> examInfo, Map<String, Object> paperResult);

    /**
     * 计算新组卷与历史同科目试卷的相似度
     * 相似度使用 Jaccard 相似系数：|A∩B| / |A∪B|
     * @param subjects 科目列表
     * @param paperResult 当前组卷结果
     * @return 相似度分析结果，包含 maxSimilarity、details 等字段
     */
    Map<String, Object> compareSimilarity(List<String> subjects, Map<String, Object> paperResult);

    /**
     * 计算新组卷与正式考试试卷（paper_manage 体系）的相似度
     * 核心算法：一次 SQL（UNION 四表）取同科目试卷指纹 → Jaccard 分批计算
     * @param subjects 科目列表
     * @param paperResult 当前组卷结果
     * @return 相似度分析结果，包含 maxSimilarity、details（含考试名称）等字段
     */
    Map<String, Object> compareWithFormalExams(List<String> subjects, Map<String, Object> paperResult);
}
