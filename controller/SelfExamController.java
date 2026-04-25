package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.SubjectiveQuestionMapper;
import com.rabbiter.oes.util.ApiResultHandler;
import com.rabbiter.oes.util.ScoreCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生自助组卷Controller
 * 支持按科目和难度组卷
 */
@RestController
@RequestMapping("/selfExam")
public class SelfExamController {

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;

    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;

    /**
     * 获取所有可用科目
     */
    @GetMapping("/subjects")
    public ApiResult getSubjects() {
        Set<String> subjects = new LinkedHashSet<>();
        // 预定义的科目列表
        subjects.add("计算机网络");
        subjects.add("数据结构");
        subjects.add("Java程序设计");
        subjects.add("高等数学");
        subjects.add("数据库理论");
        subjects.add("操作系统");
        subjects.add("软件工程");
        subjects.add("离散数学");
        subjects.add("C语言");
        subjects.add("Python程序设计");
        return ApiResultHandler.buildApiResult(200, "查询成功", subjects);
    }

    /**
     * 获取难度级别说明
     */
    @GetMapping("/levels")
    public ApiResult getLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        
        Map<String, Object> level1 = new HashMap<>();
        level1.put("level", "1");
        level1.put("name", "入门");
        level1.put("description", "基础概念题，适合刚开始学习");
        level1.put("color", "#67C23A");
        levels.add(level1);
        
        Map<String, Object> level2 = new HashMap<>();
        level2.put("level", "2");
        level2.put("name", "简单");
        level2.put("description", "基础应用题，掌握基本知识点");
        level2.put("color", "#409EFF");
        levels.add(level2);
        
        Map<String, Object> level3 = new HashMap<>();
        level3.put("level", "3");
        level3.put("name", "中等");
        level3.put("description", "综合应用题，需要一定分析能力");
        level3.put("color", "#E6A23C");
        levels.add(level3);
        
        Map<String, Object> level4 = new HashMap<>();
        level4.put("level", "4");
        level4.put("name", "困难");
        level4.put("description", "复杂综合题，需要深入理解");
        level4.put("color", "#F56C6C");
        levels.add(level4);
        
        Map<String, Object> level5 = new HashMap<>();
        level5.put("level", "5");
        level5.put("name", "挑战");
        level5.put("description", "高难度题目，适合学霸挑战");
        level5.put("color", "#909399");
        levels.add(level5);
        
        return ApiResultHandler.buildApiResult(200, "查询成功", levels);
    }

    /**
     * 自助组卷 - 按科目和难度生成试卷
     */
    @PostMapping("/generate")
    public ApiResult generateExam(@RequestBody SelfExamRequest request) {
        if (request.getSubject() == null || request.getSubject().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请选择科目", null);
        }
        
        Map<String, Object> result = new HashMap<>();
        List<MultiQuestion> multiQuestions = new ArrayList<>();
        List<FillQuestion> fillQuestions = new ArrayList<>();
        List<JudgeQuestion> judgeQuestions = new ArrayList<>();
        List<SubjectiveQuestion> subjectiveQuestions = new ArrayList<>();
        
        String subject = request.getSubject();
        String level = request.getLevel(); // 可以为null，表示不限制难度
        int multiCount = request.getMultiCount() != null ? request.getMultiCount() : 5;
        int fillCount = request.getFillCount() != null ? request.getFillCount() : 3;
        int judgeCount = request.getJudgeCount() != null ? request.getJudgeCount() : 5;
        int subjectiveCount = request.getSubjectiveCount() != null ? request.getSubjectiveCount() : 0;
        
        // 获取选择题
        if (multiCount > 0) {
            List<Integer> multiIds = multiQuestionMapper.findBySubjectAndLevel(subject, level, multiCount);
            for (Integer id : multiIds) {
                MultiQuestion q = multiQuestionMapper.findById(id);
                if (q != null) multiQuestions.add(q);
            }
        }
        
        // 获取填空题
        if (fillCount > 0) {
            List<Integer> fillIds = fillQuestionMapper.findBySubjectAndLevel(subject, level, fillCount);
            for (Integer id : fillIds) {
                FillQuestion q = fillQuestionMapper.findById(id);
                if (q != null) fillQuestions.add(q);
            }
        }
        
        // 获取判断题
        if (judgeCount > 0) {
            List<Integer> judgeIds = judgeQuestionMapper.findBySubjectAndLevel(subject, level, judgeCount);
            for (Integer id : judgeIds) {
                JudgeQuestion q = judgeQuestionMapper.findById(id);
                if (q != null) judgeQuestions.add(q);
            }
        }
        
        // 获取主观题
        if (subjectiveCount > 0) {
            List<Integer> subjectiveIds = subjectiveQuestionMapper.findBySubjectAndLevel(subject, level, subjectiveCount);
            for (Integer id : subjectiveIds) {
                SubjectiveQuestion q = subjectiveQuestionMapper.findById(id);
                if (q != null) subjectiveQuestions.add(q);
            }
        }
        
        result.put("multiQuestions", multiQuestions);
        result.put("fillQuestions", fillQuestions);
        result.put("judgeQuestions", judgeQuestions);
        result.put("subjectiveQuestions", subjectiveQuestions);
        result.put("totalCount", multiQuestions.size() + fillQuestions.size() + judgeQuestions.size() + subjectiveQuestions.size());
        
        // 计算总分 - 使用统一的分数计算器
        int totalScore = ScoreCalculator.calculateTotalScore(
            multiQuestions, fillQuestions, judgeQuestions, subjectiveQuestions);
        result.put("totalScore", totalScore);
        
        return ApiResultHandler.buildApiResult(200, "组卷成功", result);
    }

    /**
     * 获取题库统计（按科目和难度）
     */
    @GetMapping("/stats")
    public ApiResult getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 从数据库实时统计各题型数量
        stats.put("totalMulti", multiQuestionMapper.countAll());
        stats.put("totalFill", fillQuestionMapper.countAll());
        stats.put("totalJudge", judgeQuestionMapper.countAll());
        stats.put("totalSubjective", subjectiveQuestionMapper.countAll());

        return ApiResultHandler.buildApiResult(200, "查询成功", stats);
    }

    /**
     * 自助组卷请求实体
     */
    public static class SelfExamRequest {
        private String subject;
        private String level;
        private Integer multiCount;
        private Integer fillCount;
        private Integer judgeCount;
        private Integer subjectiveCount;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public Integer getMultiCount() { return multiCount; }
        public void setMultiCount(Integer multiCount) { this.multiCount = multiCount; }
        public Integer getFillCount() { return fillCount; }
        public void setFillCount(Integer fillCount) { this.fillCount = fillCount; }
        public Integer getJudgeCount() { return judgeCount; }
        public void setJudgeCount(Integer judgeCount) { this.judgeCount = judgeCount; }
        public Integer getSubjectiveCount() { return subjectiveCount; }
        public void setSubjectiveCount(Integer subjectiveCount) { this.subjectiveCount = subjectiveCount; }
    }
}
