package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.service.SubjectiveQuestionService;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主观题管理Controller
 */
@RestController
public class SubjectiveQuestionController {

    @Autowired
    private SubjectiveQuestionService subjectiveQuestionService;

    /**
     * 分页查询所有主观题
     */
    @GetMapping("/subjective/list/{page}/{size}")
    public ApiResult findAll(@PathVariable Integer page, @PathVariable Integer size) {
        Page<SubjectiveQuestion> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveQuestionService.findAll(p));
    }

    /**
     * 根据ID查询主观题
     */
    @GetMapping("/subjective/{questionId}")
    public ApiResult findById(@PathVariable Integer questionId) {
        SubjectiveQuestion question = subjectiveQuestionService.findById(questionId);
        if (question != null) {
            return ApiResultHandler.buildApiResult(200, "查询成功", question);
        }
        return ApiResultHandler.buildApiResult(404, "题目不存在", null);
    }

    /**
     * 根据科目查询主观题
     */
    @GetMapping("/subjective/subject/{subject}")
    public ApiResult findBySubject(@PathVariable String subject) {
        List<SubjectiveQuestion> list = subjectiveQuestionService.findBySubject(subject);
        return ApiResultHandler.buildApiResult(200, "查询成功", list);
    }

    /**
     * 随机获取主观题
     */
    @GetMapping("/subjective/random/{subject}/{count}")
    public ApiResult findRandom(@PathVariable String subject, @PathVariable Integer count) {
        List<Integer> ids = subjectiveQuestionService.findRandomBySubject(subject, count);
        return ApiResultHandler.buildApiResult(200, "查询成功", ids);
    }

    /**
     * 添加主观题
     */
    @PostMapping("/subjectiveQuestion")
    public ApiResult add(@RequestBody SubjectiveQuestion question) {
        if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "题目内容不能为空", null);
        }
        if (question.getSubject() == null || question.getSubject().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "科目不能为空", null);
        }

        int result = subjectiveQuestionService.add(question);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "添加成功", question);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }

    /**
     * 修改主观题
     */
    @PostMapping("/editSubjectiveQuestion")
    public ApiResult update(@RequestBody SubjectiveQuestion question) {
        if (question.getQuestionId() == null) {
            return ApiResultHandler.buildApiResult(400, "题目ID不能为空", null);
        }

        int result = subjectiveQuestionService.update(question);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "修改失败", null);
    }

    @GetMapping("/subjectiveQuestionId")
    public ApiResult findOnlyQuestion() {
        SubjectiveQuestion res = subjectiveQuestionService.findOnlyQuestionId();
        return ApiResultHandler.buildApiResult(200, "查询成功", res);
    }

    /**
     * 删除主观题
     */
    @DeleteMapping("/subjective/delete/{questionId}")
    public ApiResult delete(@PathVariable Integer questionId) {
        int result = subjectiveQuestionService.delete(questionId);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }

    /**
     * 批量添加主观题
     */
    @PostMapping("/batch")
    public ApiResult batchAdd(@RequestBody List<SubjectiveQuestion> questions) {
        int successCount = 0;
        for (SubjectiveQuestion q : questions) {
            if (q.getQuestion() != null && !q.getQuestion().trim().isEmpty()) {
                int result = subjectiveQuestionService.add(q);
                if (result > 0) {
                    successCount++;
                }
            }
        }
        return ApiResultHandler.buildApiResult(200, "批量添加完成，成功: " + successCount, successCount);
    }
}
