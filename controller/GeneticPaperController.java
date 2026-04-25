package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.GeneticPaperRequest;
import com.rabbiter.oes.service.GeneticPaperService;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 遗传算法智能组卷控制器
 */
@RestController
@RequestMapping("/genetic")
public class GeneticPaperController {

    @Autowired
    private GeneticPaperService geneticPaperService;

    /**
     * 基于遗传算法智能组卷
     * @param request 组卷约束条件
     * @return 组卷结果
     */
    @PostMapping("/generate")
    public ApiResult generatePaper(@RequestBody GeneticPaperRequest request) {
        try {
            Map<String, Object> result = geneticPaperService.generatePaper(request);
            
            if ((Boolean) result.get("success")) {
                return ApiResultHandler.buildApiResult(200, (String) result.get("message"), result);
            } else {
                return ApiResultHandler.buildApiResult(400, (String) result.get("message"), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "组卷失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取可用科目列表
     * @return 科目列表
     */
    @GetMapping("/subjects")
    public ApiResult getSubjects() {
        try {
            List<String> subjects = geneticPaperService.getAvailableSubjects();
            return ApiResultHandler.buildApiResult(200, "获取成功", subjects);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取科目失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取指定科目的章节列表
     * @param subject 科目名称
     * @return 章节列表
     */
    @GetMapping("/sections")
    public ApiResult getSections(@RequestParam(required = false) String subject) {
        try {
            List<String> sections = geneticPaperService.getSections(subject);
            return ApiResultHandler.buildApiResult(200, "获取成功", sections);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取章节失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取题库统计信息
     * @return 题库统计
     */
    @GetMapping("/stats")
    public ApiResult getQuestionBankStats() {
        try {
            Map<String, Object> stats = geneticPaperService.getQuestionBankStats();
            return ApiResultHandler.buildApiResult(200, "获取成功", stats);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取统计失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 将组卷结果创建为考试
     * @param request 包含考试信息和组卷结果
     * @return 创建结果
     */
    @PostMapping("/createExam")
    public ApiResult createExam(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> examInfo = (Map<String, Object>) request.get("examInfo");
            Map<String, Object> paperResult = (Map<String, Object>) request.get("paperResult");
            
            if (examInfo == null || paperResult == null) {
                return ApiResultHandler.buildApiResult(400, "参数不完整", null);
            }
            
            Map<String, Object> result = geneticPaperService.createExamFromPaper(examInfo, paperResult);
            
            if ((Boolean) result.get("success")) {
                return ApiResultHandler.buildApiResult(200, (String) result.get("message"), result);
            } else {
                return ApiResultHandler.buildApiResult(400, (String) result.get("message"), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "创建考试失败: " + e.getMessage(), null);
        }
    }

    /**
     * 计算新组卷与历史同科目试卷的相似度
     * 相似度 > 50% 将返回 warning=true
     * @param request 包含 subjects(科目列表)和 paperResult(组卷结果)
     * @return 相似度分析结果
     */
    @PostMapping("/compare")
    public ApiResult compareSimilarity(@RequestBody Map<String, Object> request) {
        try {
            List<String> subjects = (List<String>) request.get("subjects");
            Map<String, Object> paperResult = (Map<String, Object>) request.get("paperResult");

            if (paperResult == null) {
                return ApiResultHandler.buildApiResult(400, "缺少 paperResult 参数", null);
            }

            Map<String, Object> result = geneticPaperService.compareSimilarity(subjects, paperResult);
            return ApiResultHandler.buildApiResult(200, "相似度对比完成", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "相似度对比失败: " + e.getMessage(), null);
        }
    }

    /**
     * 计算新组卷与正式考试试卷（paper_manage 体系）的相似度
     * 核心算法：一次 SQL (UNION 四表) + Jaccard 相似系数 + HashMap 分组
     * @param request 包含 subjects(科目列表)和 paperResult(组卷结果)
     * @return 相似度分析结果，包含考试名称、相似度等
     */
    @PostMapping("/compareExam")
    public ApiResult compareWithFormalExams(@RequestBody Map<String, Object> request) {
        try {
            List<String> subjects = (List<String>) request.get("subjects");
            Map<String, Object> paperResult = (Map<String, Object>) request.get("paperResult");

            if (paperResult == null) {
                return ApiResultHandler.buildApiResult(400, "缺少 paperResult 参数", null);
            }

            Map<String, Object> result = geneticPaperService.compareWithFormalExams(subjects, paperResult);
            return ApiResultHandler.buildApiResult(200, "正式考试相似度对比完成", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "正式考试相似度对比失败: " + e.getMessage(), null);
        }
    }
}
