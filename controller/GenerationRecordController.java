package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.AiQuestionRecord;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.GeneticPaperRecord;
import com.rabbiter.oes.mapper.AiQuestionRecordMapper;
import com.rabbiter.oes.mapper.GeneticPaperRecordMapper;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 生成记录管理控制器
 */
@RestController
@RequestMapping("/generation-records")
public class GenerationRecordController {

    @Autowired
    private AiQuestionRecordMapper aiQuestionRecordMapper;

    @Autowired
    private GeneticPaperRecordMapper geneticPaperRecordMapper;

    // ==================== AI出题记录 ====================

    @GetMapping("/ai/{page}/{size}")
    public ApiResult getAiRecords(@PathVariable Integer page, @PathVariable Integer size) {
        try {
            Page<AiQuestionRecord> pageParam = new Page<>(page, size);
            IPage<AiQuestionRecord> result = aiQuestionRecordMapper.findAll(pageParam);
            return ApiResultHandler.buildApiResult(200, "查询成功", result);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "查询失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/ai/detail/{id}")
    public ApiResult getAiRecordDetail(@PathVariable Integer id) {
        try {
            AiQuestionRecord record = aiQuestionRecordMapper.findById(id);
            if (record == null) {
                return ApiResultHandler.buildApiResult(404, "记录不存在", null);
            }
            return ApiResultHandler.buildApiResult(200, "查询成功", record);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "查询失败: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/ai/{id}")
    public ApiResult deleteAiRecord(@PathVariable Integer id) {
        try {
            int result = aiQuestionRecordMapper.delete(id);
            if (result > 0) {
                return ApiResultHandler.buildApiResult(200, "删除成功", result);
            }
            return ApiResultHandler.buildApiResult(404, "记录不存在", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "删除失败: " + e.getMessage(), null);
        }
    }

    // ==================== 组卷记录 ====================

    @GetMapping("/genetic/{page}/{size}")
    public ApiResult getGeneticRecords(@PathVariable Integer page, @PathVariable Integer size) {
        try {
            Page<GeneticPaperRecord> pageParam = new Page<>(page, size);
            IPage<GeneticPaperRecord> result = geneticPaperRecordMapper.findAll(pageParam);
            return ApiResultHandler.buildApiResult(200, "查询成功", result);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "查询失败: " + e.getMessage(), null);
        }
    }

    @GetMapping("/genetic/detail/{id}")
    public ApiResult getGeneticRecordDetail(@PathVariable Integer id) {
        try {
            GeneticPaperRecord record = geneticPaperRecordMapper.findById(id);
            if (record == null) {
                return ApiResultHandler.buildApiResult(404, "记录不存在", null);
            }
            return ApiResultHandler.buildApiResult(200, "查询成功", record);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "查询失败: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/genetic/{id}")
    public ApiResult deleteGeneticRecord(@PathVariable Integer id) {
        try {
            int result = geneticPaperRecordMapper.delete(id);
            if (result > 0) {
                return ApiResultHandler.buildApiResult(200, "删除成功", result);
            }
            return ApiResultHandler.buildApiResult(404, "记录不存在", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "删除失败: " + e.getMessage(), null);
        }
    }
}
