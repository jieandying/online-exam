package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.AIQuestionRequest;
import com.rabbiter.oes.entity.AIQuestionResponse;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.service.AIQuestionService;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI出题控制器
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AIQuestionController {
    
    @Autowired
    private AIQuestionService aiQuestionService;
    
    /**
     * 基于提示词生成题目
     */
    @PostMapping("/generateByPrompt")
    public ApiResult<AIQuestionResponse> generateQuestionsByPrompt(@RequestBody AIQuestionRequest request) {
        try {
            // 参数验证
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "科目不能为空", null);
            }
            
            if (request.getQuestionType() == null || request.getQuestionType().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "题目类型不能为空", null);
            }
            
            if (request.getQuestionCount() == null || request.getQuestionCount() <= 0) {
                return ApiResultHandler.buildApiResult(400, "题目数量必须大于0", null);
            }
            
            if (request.getQuestionCount() > 10) {
                return ApiResultHandler.buildApiResult(400, "单次生成题目数量不能超过10道", null);
            }
            
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "提示词不能为空", null);
            }
            
            // 设置默认值
            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                request.setDifficulty("2"); // 默认中等难度
            }
            
            AIQuestionResponse response = aiQuestionService.generateQuestionsByPrompt(request);
            
            if (response.isSuccess()) {
                return ApiResultHandler.buildApiResult(200, "生成题目成功", response);
            } else {
                return ApiResultHandler.buildApiResult(500, response.getMessage(), null);
            }
            
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "生成题目失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 基于文档生成题目
     */
    @PostMapping("/generateByDocument")
    public ApiResult<AIQuestionResponse> generateQuestionsByDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("questionType") String questionType,
            @RequestParam("questionCount") Integer questionCount,
            @RequestParam(value = "difficulty", defaultValue = "2") String difficulty,
            @RequestParam(value = "chapter", required = false) String chapter,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "requirements", required = false) String requirements) {
        
        try {
            // 文件验证
            if (file == null || file.isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "请选择要上传的文件", null);
            }
            
            // 文件大小限制 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ApiResultHandler.buildApiResult(400, "文件大小不能超过10MB", null);
            }
            
            // 文件格式验证  
            String fileName = file.getOriginalFilename();
            if (fileName == null || !isValidFileFormat(fileName)) {
                return ApiResultHandler.buildApiResult(400, "只支持PDF、Word、Excel、TXT格式的文件", null);
            }
            
            // 构建请求对象
            AIQuestionRequest request = new AIQuestionRequest();
            request.setSubject(subject);
            request.setQuestionType(questionType);
            request.setQuestionCount(questionCount);
            request.setDifficulty(difficulty);
            request.setChapter(chapter);
            request.setPrompt(prompt);
            request.setRequirements(requirements);
            
            // 参数验证
            if (subject == null || subject.trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "科目不能为空", null);
            }
            
            if (questionType == null || questionType.trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "题目类型不能为空", null);
            }
            
            if (questionCount == null || questionCount <= 0) {
                return ApiResultHandler.buildApiResult(400, "题目数量必须大于0", null);
            }
            
            if (questionCount > 10) {
                return ApiResultHandler.buildApiResult(400, "单次生成题目数量不能超过10道", null);
            }
            
            AIQuestionResponse response = aiQuestionService.generateQuestionsByDocument(file, request);
            
            if (response.isSuccess()) {
                return ApiResultHandler.buildApiResult(200, "基于文档生成题目成功", response);
            } else {
                return ApiResultHandler.buildApiResult(500, response.getMessage(), null);
            }
            
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "基于文档生成题目失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 保存AI生成的题目到数据库
     */
    @PostMapping("/saveQuestions")
    public ApiResult<String> saveQuestions(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> questions = (List<Object>) requestData.get("questions");
            String questionType = (String) requestData.get("questionType");
            
            if (questions == null || questions.isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "题目列表不能为空", null);
            }
            
            if (questionType == null || questionType.trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "题目类型不能为空", null);
            }
            
            boolean success = aiQuestionService.saveQuestionsToDatabase(questions, questionType);
            
            if (success) {
                return ApiResultHandler.buildApiResult(200, "题目保存成功", null);
            } else {
                return ApiResultHandler.buildApiResult(500, "题目保存失败", null);
            }
            
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "保存题目失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取支持的科目列表
     */
    @GetMapping("/subjects")
    public ApiResult<List<String>> getSupportedSubjects() {
        try {
            List<String> subjects = aiQuestionService.getSupportedSubjects();
            return ApiResultHandler.buildApiResult(200, "获取科目列表成功", subjects);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取科目列表失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取支持的题目类型
     */
    @GetMapping("/questionTypes")
    public ApiResult<Map<String, String>> getSupportedQuestionTypes() {
        try {
            // 转换为前端需要的格式
            Map<String, String> typeMap = new HashMap<>();
            typeMap.put("multiple", "选择题");
            typeMap.put("fill", "填空题");
            typeMap.put("judge", "判断题");
            typeMap.put("subjective", "主观题");
            
            return ApiResultHandler.buildApiResult(200, "获取题目类型成功", typeMap);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取题目类型失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取难度等级选项
     */
    @GetMapping("/difficulties")
    public ApiResult<Map<String, String>> getDifficulties() {
        try {
            Map<String, String> difficulties = new HashMap<>();
            difficulties.put("1", "简单");
            difficulties.put("2", "中等");
            difficulties.put("3", "困难");
            difficulties.put("4", "极难");
            difficulties.put("5", "超纲");
            
            return ApiResultHandler.buildApiResult(200, "获取难度等级成功", difficulties);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取难度等级失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 验证文件格式
     */
    private boolean isValidFileFormat(String fileName) {
        String extension = fileName.toLowerCase().substring(fileName.lastIndexOf("."));
        return extension.equals(".pdf") || extension.equals(".docx") || 
               extension.equals(".xlsx") || extension.equals(".txt");
    }
}
