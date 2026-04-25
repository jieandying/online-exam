package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.AIQuestionRequest;
import com.rabbiter.oes.entity.AIQuestionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI出题服务接口
 */
public interface AIQuestionService {
    
    /**
     * 基于提示词生成题目
     */
    AIQuestionResponse generateQuestionsByPrompt(AIQuestionRequest request);
    
    /**
     * 基于文档内容生成题目
     */
    AIQuestionResponse generateQuestionsByDocument(MultipartFile file, AIQuestionRequest request);
    
    /**
     * 保存AI生成的题目到数据库
     * @param questions 题目列表
     * @param questionType 题目类型：multiple, fill, judge
     * @return 保存结果
     */
    boolean saveQuestionsToDatabase(List<Object> questions, String questionType);
    
    /**
     * 获取支持的科目列表
     */
    List<String> getSupportedSubjects();
    
    /**
     * 获取支持的题目类型
     */
    List<String> getSupportedQuestionTypes();
    
    /**
     * 验证AI生成的题目格式
     */
    boolean validateQuestionFormat(Object question, String questionType);
}
