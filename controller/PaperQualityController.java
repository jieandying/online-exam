package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.PaperQualityResult;
import com.rabbiter.oes.service.PaperQualityService;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 试卷质量评价控制器
 */
@RestController
@RequestMapping("/paper-quality")
public class PaperQualityController {
    
    @Autowired
    private PaperQualityService paperQualityService;
    
    /**
     * 评价试卷质量
     * GET /paper-quality/evaluate/{examCode}
     */
    @GetMapping("/evaluate/{examCode}")
    public ApiResult evaluatePaperQuality(@PathVariable Integer examCode) {
        try {
            PaperQualityResult result = paperQualityService.evaluatePaperQuality(examCode);
            if (result == null) {
                return ApiResultHandler.buildApiResult(404, "未找到该考试信息", null);
            }
            return ApiResultHandler.buildApiResult(200, "评价成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "评价失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 计算两次考试的效度
     * GET /paper-quality/validity?examCode1=xxx&examCode2=xxx
     */
    @GetMapping("/validity")
    public ApiResult calculateValidity(@RequestParam Integer examCode1, @RequestParam Integer examCode2) {
        try {
            Double validity = paperQualityService.calculateValidity(examCode1, examCode2);
            return ApiResultHandler.buildApiResult(200, "计算成功", validity);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "计算失败: " + e.getMessage(), null);
        }
    }
}
