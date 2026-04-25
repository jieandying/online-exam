package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.SubjectiveAnswer;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.entity.WrongQuestion;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.service.ScoreService;
import com.rabbiter.oes.service.StudyService;
import com.rabbiter.oes.service.SubjectiveAnswerService;
import com.rabbiter.oes.util.ApiResultHandler;
import com.rabbiter.oes.util.SubjectiveGradingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师评分Controller
 */
@RestController
@RequestMapping("/scoring")
public class ScoringController {

    @Autowired
    private SubjectiveAnswerService subjectiveAnswerService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private ExamManageMapper examManageMapper;

    /**
     * 获取所有考试待批阅的总数
     */
    @GetMapping("/pending/total")
    public ApiResult getPendingTotal() {
        Page<SubjectiveAnswer> p = new Page<>(1, 1);
        long total = subjectiveAnswerService.findAllPending(p).getTotal();
        return ApiResultHandler.buildApiResult(200, "查询成功", total);
    }

    /**
     * 获取所有待评分的主观题答案（分页）
     */
    @GetMapping("/pending")
    public ApiResult findAllPending(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        Page<SubjectiveAnswer> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveAnswerService.findAllPending(p));
    }

    /**
     * 根据考试获取待评分的答案
     */
    @GetMapping("/pending/exam/{examCode}")
    public ApiResult findPendingByExam(@PathVariable Integer examCode,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        Page<SubjectiveAnswer> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveAnswerService.findPendingByExam(p, examCode));
    }

    /**
     * 获取某考试待评分数量
     */
    @GetMapping("/pending/count/{examCode}")
    public ApiResult countPending(@PathVariable Integer examCode) {
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveAnswerService.countPendingByExam(examCode));
    }

    /**
     * 获取某学生某次考试的所有主观题答案
     */
    @GetMapping("/answers/{examCode}/{studentId}")
    public ApiResult findByExamAndStudent(@PathVariable Integer examCode,
                                          @PathVariable Integer studentId) {
        List<SubjectiveAnswer> answers = subjectiveAnswerService.findByExamAndStudent(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", answers);
    }

    /**
     * 教师评分
     */
    @PostMapping("/score")
    public ApiResult score(@RequestBody SubjectiveAnswer answer) {
        if (answer.getAnswerId() == null) {
            return ApiResultHandler.buildApiResult(400, "答案 ID 不能为空", null);
        }
        if (answer.getTeacherScore() == null) {
            return ApiResultHandler.buildApiResult(400, "评分不能为空", null);
        }
            
        // 获取原答案，检查状态和分值
        SubjectiveAnswer original = subjectiveAnswerService.findById(answer.getAnswerId());
        if (original == null) {
            return ApiResultHandler.buildApiResult(404, "答案不存在", null);
        }
            
        // 检查是否已评分
        if (original.getStatus() != null && original.getStatus() == 1) {
            return ApiResultHandler.buildApiResult(400, "该答案已评分，请勿重复评分", null);
        }
            
        // 获取题目信息，校验评分范围
        SubjectiveQuestion question = subjectiveAnswerService.getQuestionById(original.getQuestionId());
        if (question == null || question.getScore() == null) {
            return ApiResultHandler.buildApiResult(404, "题目信息不存在", null);
        }
            
        // 校验评分是否在合理范围内
        if (answer.getTeacherScore() < 0 || answer.getTeacherScore() > question.getScore()) {
            return ApiResultHandler.buildApiResult(400, 
                "评分必须在 0-" + question.getScore() + " 分之间", null);
        }
            
        int result = subjectiveAnswerService.score(answer);
        if (result > 0) {
            // 主观题评分低于5分，自动记入错题本
            recordSubjectiveWrongIfNeeded(answer.getTeacherScore(), original, question);

            // 检查该学生该考试是否还有未评分的主观题
            int pendingLeft = subjectiveAnswerService.countPendingByExamAndStudent(
                    original.getExamCode(), original.getStudentId());
            if (pendingLeft == 0) {
                // 全部批阅完成，自动计算并更新最终成绩
                int subjectiveTotal = subjectiveAnswerService.getTotalScore(
                        original.getExamCode(), original.getStudentId());
                Integer ptScore = scoreService.getPtScore(original.getExamCode(), original.getStudentId());
                int finalScore = (ptScore != null ? ptScore : 0) + subjectiveTotal;
                scoreService.updateEtScore(original.getExamCode(), original.getStudentId(), finalScore);
                return ApiResultHandler.buildApiResult(200, "评分成功！该学生所有主观题已全部批阅，最终成绩已更新为 " + finalScore + " 分", null);
            }
            return ApiResultHandler.buildApiResult(200, "评分成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "评分失败", null);
    }

    /**
     * 批量评分
     */
    @PostMapping("/score/batch")
    public ApiResult scoreBatch(@RequestBody List<SubjectiveAnswer> answers) {
        int successCount = 0;
        // 记录本次批量评分涉及的 (examCode, studentId) 组合
        Map<String, int[]> studentExamMap = new HashMap<>();

        for (SubjectiveAnswer answer : answers) {
            if (answer.getAnswerId() != null && answer.getTeacherScore() != null) {
                // 获取原始答案以拿到 examCode 和 studentId
                SubjectiveAnswer original = subjectiveAnswerService.findById(answer.getAnswerId());
                int result = subjectiveAnswerService.score(answer);
                if (result > 0) {
                    successCount++;
                    // 主观题评分低于5分，自动记入错题本
                    if (original != null && answer.getTeacherScore() < 5) {
                        SubjectiveQuestion question = subjectiveAnswerService.getQuestionById(original.getQuestionId());
                        if (question != null) {
                            recordSubjectiveWrongIfNeeded(answer.getTeacherScore(), original, question);
                        }
                    }
                    if (original != null && original.getExamCode() != null && original.getStudentId() != null) {
                        String key = original.getExamCode() + "_" + original.getStudentId();
                        studentExamMap.put(key, new int[]{original.getExamCode(), original.getStudentId()});
                    }
                }
            }
        }

        // 批量评分完成后，检查每个学生是否已全部批阅，并更新最终成绩
        int updatedCount = 0;
        for (int[] pair : studentExamMap.values()) {
            int examCode = pair[0];
            int studentId = pair[1];
            int pendingLeft = subjectiveAnswerService.countPendingByExamAndStudent(examCode, studentId);
            if (pendingLeft == 0) {
                int subTotal = subjectiveAnswerService.getTotalScore(examCode, studentId);
                Integer ptScore = scoreService.getPtScore(examCode, studentId);
                int finalScore = (ptScore != null ? ptScore : 0) + subTotal;
                scoreService.updateEtScore(examCode, studentId, finalScore);
                updatedCount++;
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("total", answers.size());
        resultMap.put("success", successCount);
        resultMap.put("updated", updatedCount);
        return ApiResultHandler.buildApiResult(200, "批量评分完成，已更新 " + updatedCount + " 名学生成绩", resultMap);
    }

    /**
     * 获取学生主观题总分
     */
    @GetMapping("/total/{examCode}/{studentId}")
    public ApiResult getTotalScore(@PathVariable Integer examCode,
                                   @PathVariable Integer studentId) {
        int totalScore = subjectiveAnswerService.getTotalScore(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", totalScore);
    }

    /**
     * 提交主观题答案（学生端）
     */
    @PostMapping("/answer")
    public ApiResult submitAnswer(@RequestBody SubjectiveAnswer answer) {
        if (answer.getQuestionId() == null || answer.getExamCode() == null || 
            answer.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "参数不完整", null);
        }
        
        int result = subjectiveAnswerService.add(answer);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "提交成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "提交失败", null);
    }

    /**
     * 批量提交主观题答案（交卷时）
     */
    @PostMapping("/answers/batch")
    public ApiResult submitAnswersBatch(@RequestBody List<SubjectiveAnswer> answers) {
        int successCount = 0;
        for (SubjectiveAnswer answer : answers) {
            if (answer.getQuestionId() != null && answer.getExamCode() != null &&
                answer.getStudentId() != null) {
                int result = subjectiveAnswerService.add(answer);
                if (result > 0) {
                    successCount++;
                }
            }
        }
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("total", answers.size());
        resultMap.put("success", successCount);
        return ApiResultHandler.buildApiResult(200, "提交完成", resultMap);
    }

    /* ═══════════════════════════════════════
       系统自动阅卷接口
       ═══════════════════════════════════════ */

    /**
     * 纯计算接口（练习模式 / 预览）
     * 不写入数据库，仅返回评分结果与明细
     */
    @PostMapping("/auto/compute")
    public ApiResult autoCompute(@RequestBody Map<String, Object> body) {
        String studentAnswer   = (String) body.getOrDefault("studentAnswer", "");
        String referenceAnswer = (String) body.getOrDefault("referenceAnswer", "");
        int maxScore = body.containsKey("maxScore")
                       ? Integer.parseInt(body.get("maxScore").toString()) : 10;

        int score = SubjectiveGradingUtil.autoGrade(studentAnswer, referenceAnswer, maxScore);
        Map<String, Double> detail = SubjectiveGradingUtil.scoreDetail(studentAnswer, referenceAnswer, maxScore);
        String comment = SubjectiveGradingUtil.generateComment(studentAnswer, referenceAnswer, maxScore, score);

        Map<String, Object> result = new HashMap<>();
        result.put("score",   score);
        result.put("detail",  detail);
        result.put("comment", comment);
        return ApiResultHandler.buildApiResult(200, "计算完成", result);
    }

    /**
     * 系统自动阅卷（单题）
     * 将计算结果写入数据库，相当于老师手动评分一题
     */
    @PostMapping("/auto/{answerId}")
    public ApiResult autoGradeSingle(@PathVariable Integer answerId) {
        SubjectiveAnswer original = subjectiveAnswerService.findById(answerId);
        if (original == null) {
            return ApiResultHandler.buildApiResult(404, "答案不存在", null);
        }
        SubjectiveQuestion question = subjectiveAnswerService.getQuestionById(original.getQuestionId());
        if (question == null) {
            return ApiResultHandler.buildApiResult(404, "题目信息不存在", null);
        }

        int autoScore = SubjectiveGradingUtil.autoGrade(
                original.getStudentAnswer(),
                question.getReferenceAnswer(),
                question.getScore());
        String comment = SubjectiveGradingUtil.generateComment(
                original.getStudentAnswer(), question.getReferenceAnswer(),
                question.getScore(), autoScore);

        // 将系统评分写入（复用现有 score 接口逻辑）
        SubjectiveAnswer graded = new SubjectiveAnswer();
        graded.setAnswerId(answerId);
        graded.setTeacherScore(autoScore);
        graded.setTeacherComment(comment);
        int result = subjectiveAnswerService.score(graded);

        if (result > 0) {
            // 主观题评分低于5分，自动记入错题本
            recordSubjectiveWrongIfNeeded(autoScore, original, question);

            // 检查是否全部批阅完成
            int pendingLeft = subjectiveAnswerService.countPendingByExamAndStudent(
                    original.getExamCode(), original.getStudentId());
            Map<String, Object> resp = new HashMap<>();
            resp.put("score",   autoScore);
            resp.put("comment", comment);
            if (pendingLeft == 0) {
                int subTotal = subjectiveAnswerService.getTotalScore(
                        original.getExamCode(), original.getStudentId());
                Integer ptScore = scoreService.getPtScore(original.getExamCode(), original.getStudentId());
                int finalScore = (ptScore != null ? ptScore : 0) + subTotal;
                scoreService.updateEtScore(original.getExamCode(), original.getStudentId(), finalScore);
                resp.put("finalScore",    finalScore);
                resp.put("allDone",       true);
                return ApiResultHandler.buildApiResult(200,
                    "系统阅卷完成，该学生所有主观题已全部批阅，最终成绩已更新为 " + finalScore + " 分", resp);
            }
            return ApiResultHandler.buildApiResult(200, "系统阅卷完成", resp);
        }
        return ApiResultHandler.buildApiResult(400, "系统阅卷失败", null);
    }

    /**
     * 批量系统阅卷（某考试所有待批阅答案）
     * 老师点击"一键系统阅卷"后调用
     */
    @PostMapping("/auto/exam/{examCode}")
    public ApiResult autoGradeExam(@PathVariable Integer examCode) {
        // 获取该考试所有待批阅答案（最多一次处理 1000 题）
        Page<SubjectiveAnswer> page = new Page<>(1, 1000);
        List<SubjectiveAnswer> pendingList = subjectiveAnswerService
                .findPendingByExam(page, examCode).getRecords();

        int successCount = 0;
        List<Integer> completedStudents = new ArrayList<>();

        for (SubjectiveAnswer pending : pendingList) {
            SubjectiveQuestion question = subjectiveAnswerService.getQuestionById(pending.getQuestionId());
            if (question == null) continue;

            int autoScore = SubjectiveGradingUtil.autoGrade(
                    pending.getStudentAnswer(),
                    question.getReferenceAnswer(),
                    question.getScore());
            String comment = SubjectiveGradingUtil.generateComment(
                    pending.getStudentAnswer(), question.getReferenceAnswer(),
                    question.getScore(), autoScore);

            SubjectiveAnswer graded = new SubjectiveAnswer();
            graded.setAnswerId(pending.getAnswerId());
            graded.setTeacherScore(autoScore);
            graded.setTeacherComment(comment);
            int res = subjectiveAnswerService.score(graded);
            if (res > 0) {
                successCount++;
                // 主观题评分低于5分，自动记入错题本
                recordSubjectiveWrongIfNeeded(autoScore, pending, question);
            }
        }

        // 批量更新完成后有应更新最终成绩的学生
        // 重新查询，找出已全部完成的学生
        for (SubjectiveAnswer pending : pendingList) {
            Integer studentId = pending.getStudentId();
            if (completedStudents.contains(studentId)) continue;
            int left = subjectiveAnswerService.countPendingByExamAndStudent(examCode, studentId);
            if (left == 0) {
                int subTotal = subjectiveAnswerService.getTotalScore(examCode, studentId);
                Integer ptScore = scoreService.getPtScore(examCode, studentId);
                int finalScore = (ptScore != null ? ptScore : 0) + subTotal;
                scoreService.updateEtScore(examCode, studentId, finalScore);
                completedStudents.add(studentId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total",     pendingList.size());
        result.put("success",   successCount);
        result.put("updated",   completedStudents.size());
        return ApiResultHandler.buildApiResult(200,
                "批量系统阅卷完成，共处理 " + successCount + " 题，已更新" + completedStudents.size() + "个学生成绩", result);
    }

    /**
     * 主观题评分低于5分时自动记入错题本
     */
    private void recordSubjectiveWrongIfNeeded(int teacherScore, SubjectiveAnswer original, SubjectiveQuestion question) {
        if (teacherScore >= 5) return;
        try {
            // 获取考试的科目信息
            String subject = "";
            if (original.getExamCode() != null) {
                ExamManage exam = examManageMapper.findById(original.getExamCode());
                if (exam != null) {
                    subject = exam.getSource() != null ? exam.getSource() : (exam.getDescription() != null ? exam.getDescription() : "");
                }
            }
            WrongQuestion wq = new WrongQuestion();
            wq.setStudentId(original.getStudentId());
            wq.setQuestionType(4); // 主观题
            wq.setQuestionId(original.getQuestionId());
            wq.setQuestionContent(question.getQuestion());
            wq.setCorrectAnswer(question.getReferenceAnswer() != null ? question.getReferenceAnswer() : "");
            wq.setWrongAnswer(original.getStudentAnswer() != null ? original.getStudentAnswer() : "");
            wq.setScore(question.getScore());
            wq.setSubject(subject);
            wq.setAnalysis(question.getAnalysis() != null ? question.getAnalysis() : "");
            wq.setWrongCount(1);
            wq.setMastered(0);
            wq.setLastWrongTime(new java.util.Date());
            studyService.addOrUpdateWrongQuestion(wq);
            System.out.println("主观题评分<5分，已记入错题本 - 学生:" + original.getStudentId() + " 题目:" + original.getQuestionId() + " 得分:" + teacherScore);
        } catch (Exception e) {
            System.err.println("记录主观题错题失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有已批阅记录（分页）
     * GET /scoring/graded?page=1&size=20
     */
    @GetMapping("/graded")
    public ApiResult findAllGraded(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        Page<SubjectiveAnswer> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveAnswerService.findAllGraded(p));
    }

    /**
     * 按考试查询已批阅记录（分页）
     * GET /scoring/graded/exam/{examCode}?page=1&size=20
     */
    @GetMapping("/graded/exam/{examCode}")
    public ApiResult findGradedByExam(@PathVariable Integer examCode,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size) {
        Page<SubjectiveAnswer> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", subjectiveAnswerService.findGradedByExam(p, examCode));
    }
}
