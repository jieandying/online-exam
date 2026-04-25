package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.GeneticPaperRequest;
import com.rabbiter.oes.entity.StudyRecord;
import com.rabbiter.oes.entity.WrongQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.entity.PaperManage;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.mapper.SubjectiveQuestionMapper;
import com.rabbiter.oes.mapper.ScoreMapper;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.mapper.WrongQuestionMapper;
import com.rabbiter.oes.service.GeneticPaperService;
import com.rabbiter.oes.service.StudyService;
import com.rabbiter.oes.service.PaperService;
import com.rabbiter.oes.util.ApiResultHandler;
import com.rabbiter.oes.util.SubjectiveGradingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习中心控制器
 * 包含错题本、学习曲线等功能
 */
@RestController
@RequestMapping("/study")
public class StudyController {

    @Autowired
    private StudyService studyService;

    @Autowired
    private GeneticPaperService geneticPaperService;

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private ExamManageMapper examManageMapper;

    @Autowired
    private PaperService paperService;

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;

    // ========== 错题本相关接口 ==========

    /**
     * 记录错题
     * POST /study/wrong
     */
    @PostMapping("/wrong")
    public ApiResult addWrongQuestion(@RequestBody WrongQuestion wrongQuestion) {
        try {
            System.out.println("========== 记录错题 ==========");
            System.out.println("学生ID: " + wrongQuestion.getStudentId());
            System.out.println("题目类型: " + wrongQuestion.getQuestionType());
            System.out.println("题目ID: " + wrongQuestion.getQuestionId());
            System.out.println("题目内容: " + wrongQuestion.getQuestionContent());
            System.out.println("正确答案: " + wrongQuestion.getCorrectAnswer());
            System.out.println("错误答案: " + wrongQuestion.getWrongAnswer());
            System.out.println("科目: " + wrongQuestion.getSubject());

            studyService.addOrUpdateWrongQuestion(wrongQuestion);
            System.out.println("错题记录成功!");
            return ApiResultHandler.buildApiResult(200, "记录成功", null);
        } catch (Exception e) {
            System.out.println("错题记录失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "记录失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取学生错题列表（支持排序）
     * GET /study/wrong/{studentId}?sort=recommend|wrongCount|latest
     * 
     * @param studentId 学生ID
     * @param sort      排序方式：recommend-推荐排序, wrongCount-错误次数, latest-最近出错(默认)
     * @return 错题列表
     */
    @GetMapping("/wrong/{studentId}")
    public ApiResult getWrongQuestions(
            @PathVariable Integer studentId,
            @RequestParam(required = false, defaultValue = "latest") String sort) {
        System.out.println("获取错题列表 - 学生ID:" + studentId + " 排序:" + sort);

        List<WrongQuestion> wrongQuestions = studyService.getWrongQuestionsWithSort(studentId, sort);
        return ApiResultHandler.buildApiResult(200, "查询成功", wrongQuestions);
    }

    /**
     * 按题型获取错题
     * GET /study/wrong/{studentId}/type/{type}
     */
    @GetMapping("/wrong/{studentId}/type/{type}")
    public ApiResult getWrongQuestionsByType(
            @PathVariable Integer studentId,
            @PathVariable Integer type) {

        List<WrongQuestion> wrongQuestions = studyService.getWrongQuestionsByType(studentId, type);
        return ApiResultHandler.buildApiResult(200, "查询成功", wrongQuestions);
    }

    /**
     * 标记错题为已掌握
     * PUT /study/wrong/master/{id}
     */
    @PutMapping("/wrong/master/{id}")
    public ApiResult markAsMastered(@PathVariable Integer id) {
        System.out.println("标记错题已掌握 - ID:" + id);

        studyService.markAsMastered(id);
        return ApiResultHandler.buildApiResult(200, "标记成功", null);
    }

    /**
     * 重做错题并校验答案
     * POST /study/wrong/redo
     * 
     * @param params 参数 Map，包含 wrongQuestionId(错题ID) 和 userAnswer(用户答案)
     * @return 校验结果，包含 correct(是否正确)、correctAnswer(正确答案)、message(提示信息)
     */
    @PostMapping("/wrong/redo")
    public ApiResult redoWrongQuestion(@RequestBody Map<String, Object> params) {
        try {
            Integer wrongQuestionId = (Integer) params.get("wrongQuestionId");
            String userAnswer = (String) params.get("userAnswer");

            System.out.println("重做错题 - ID:" + wrongQuestionId + " 用户答案:" + userAnswer);

            if (wrongQuestionId == null || userAnswer == null) {
                return ApiResultHandler.buildApiResult(400, "参数不完整", null);
            }

            Map<String, Object> result = studyService.checkRedoAnswer(wrongQuestionId, userAnswer);
            return ApiResultHandler.buildApiResult(200, "校验完成", result);
        } catch (Exception e) {
            System.out.println("重做错题失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取错题统计
     * GET /study/wrong/stats/{studentId}
     */
    @GetMapping("/wrong/stats/{studentId}")
    public ApiResult getWrongQuestionStats(@PathVariable Integer studentId) {
        Map<String, Object> stats = studyService.getWrongQuestionStats(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", stats);
    }

    /**
     * 批量记录错题（考试交卷时调用）
     * POST /study/wrong/batch
     */
    @PostMapping("/wrong/batch")
    public ApiResult addWrongQuestionBatch(@RequestBody List<WrongQuestion> wrongQuestions) {
        try {
            int count = 0;
            for (WrongQuestion wq : wrongQuestions) {
                if (wq.getStudentId() == null || wq.getQuestionId() == null || wq.getQuestionType() == null) {
                    continue;
                }
                wq.setWrongCount(1);
                wq.setMastered(0);
                wq.setLastWrongTime(new java.util.Date());
                studyService.addOrUpdateWrongQuestion(wq);
                count++;
            }
            System.out.println("批量记录错题: " + count + "/" + wrongQuestions.size());
            return ApiResultHandler.buildApiResult(200, "记录成功", count);
        } catch (Exception e) {
            System.out.println("批量记录错题失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "记录失败: " + e.getMessage(), null);
        }
    }

    /**
     * 删除错题（取消标记）
     * DELETE /study/wrong/{studentId}/{questionType}/{questionId}
     */
    @DeleteMapping("/wrong/{studentId}/{questionType}/{questionId}")
    public ApiResult deleteWrongQuestion(
            @PathVariable Integer studentId,
            @PathVariable Integer questionType,
            @PathVariable Integer questionId) {
        try {
            WrongQuestion existing = wrongQuestionMapper.findByStudentAndQuestion(studentId, questionType, questionId);
            if (existing != null) {
                wrongQuestionMapper.delete(existing.getId());
            }
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "删除失败: " + e.getMessage(), null);
        }
    }

    // ========== 模拟考试相关接口 ==========

    /**
     * 获取可用科目列表
     * GET /study/mock/subjects
     */
    @GetMapping("/mock/subjects")
    public ApiResult getAvailableSubjects() {
        List<String> subjects = geneticPaperService.getAvailableSubjects();
        return ApiResultHandler.buildApiResult(200, "获取科目成功", subjects);
    }

    /**
     * 获取模拟考试智能推荐配置
     * GET /study/mock/suggestion
     * 
     * @param studentId 学生ID
     * @return 推荐的考试配置
     */
    @GetMapping("/mock/suggestion")
    public ApiResult getMockExamSuggestion(@RequestParam Integer studentId) {
        try {
            Map<String, Object> suggestion = new HashMap<>();

            // 调用学情分析服务
            Map<String, Object> weakPoints = studyService.analyzeWeakPoints(studentId);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> weakSubjects = (List<Map<String, Object>>) weakPoints.get("weakSubjects");

            if (weakSubjects != null && !weakSubjects.isEmpty()) {
                // 推荐最薄弱的科目
                String recommendedSubject = (String) weakSubjects.get(0).get("subject");
                suggestion.put("subject", recommendedSubject);
                suggestion.put("reason", "该科目是您当前最薄弱的知识点，建议重点练习");
            } else {
                // 如果没有错题记录，随机推荐一个科目
                List<String> allSubjects = geneticPaperService.getAvailableSubjects();
                if (!allSubjects.isEmpty()) {
                    suggestion.put("subject", allSubjects.get(0));
                    suggestion.put("reason", "开始您的第一次模拟考试吧！");
                }
            }

            // 推荐题目数量（可根据学生水平调整）
            suggestion.put("multiCount", 10);
            suggestion.put("fillCount", 5);
            suggestion.put("judgeCount", 5);

            return ApiResultHandler.buildApiResult(200, "获取推荐成功", suggestion);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "获取推荐失败: " + e.getMessage(), null);
        }
    }

    /**
     * 生成模拟试卷 (智能组卷)
     * POST /study/mock/generate
     */
    @PostMapping("/mock/generate")
    public ApiResult generateMockExam(@RequestBody Map<String, Object> params) {
        String subject = (String) params.get("subject");
        Integer multiCount = (Integer) params.get("multiCount");
        Integer fillCount = (Integer) params.get("fillCount");
        Integer judgeCount = (Integer) params.get("judgeCount");

        // 构建遗传算法组卷请求
        GeneticPaperRequest request = new GeneticPaperRequest();
        if (subject != null && !subject.isEmpty()) {
            List<String> subjects = new ArrayList<>();
            subjects.add(subject);
            request.setSubjects(subjects);
        }
        if (multiCount != null)
            request.setMultiCount(multiCount);
        if (fillCount != null)
            request.setFillCount(fillCount);
        if (judgeCount != null)
            request.setJudgeCount(judgeCount);

        // 设置默认分值
        request.setMultiScore(5);
        request.setFillScore(5);
        request.setJudgeScore(2); // 判断题通常分值较低
        request.setSubjectiveCount(0); // 暂不支持主观题

        // 设置难度
        double targetDifficulty = 3.0; // 默认中等难度

        // 自适应难度调整
        Integer studentId = params.get("studentId") != null ? Integer.parseInt(String.valueOf(params.get("studentId")))
                : null;
        if (studentId != null) {
            List<StudyRecord> records = studyService.getStudyRecords(studentId);
            if (records != null && !records.isEmpty()) {
                // 取最近5次练习/考试
                int count = 0;
                double totalAcc = 0;
                for (int i = 0; i < records.size() && count < 5; i++) {
                    StudyRecord r = records.get(i);
                    totalAcc += r.getAccuracy() != null ? r.getAccuracy() : 0;
                    count++;
                }
                double avgAcc = count > 0 ? totalAcc / count : 0;

                // 如果正确率>80%，提高难度
                if (avgAcc > 80)
                    targetDifficulty = 4.0;
                // 如果正确率<60%，降低难度
                else if (avgAcc < 60)
                    targetDifficulty = 2.0;

                System.out.println("自适应难度调整 - 学生ID:" + studentId + " 近期正确率:" + avgAcc + "% 目标难度:" + targetDifficulty);
            }
        }

        request.setTargetDifficulty(targetDifficulty);
        request.setDifficultyTolerance(0.5);

        // 调用遗传算法服务
        Map<String, Object> paper = geneticPaperService.generatePaper(request);

        if ((boolean) paper.get("success")) {
            // 构造前端需要的格式
            Map<String, Object> result = new HashMap<>();

            List<Map<String, Object>> questions = new ArrayList<>();

            // 处理选择题
            List<MultiQuestion> multiList = (List<MultiQuestion>) paper.get("multiQuestions");
            if (multiList != null) {
                for (MultiQuestion q : multiList) {
                    Map<String, Object> qMap = new HashMap<>();
                    qMap.put("questionId", q.getQuestionId());
                    qMap.put("type", 1);
                    qMap.put("typeName", "选择题");
                    qMap.put("question", q.getQuestion());
                    qMap.put("answerA", q.getAnswerA());
                    qMap.put("answerB", q.getAnswerB());
                    qMap.put("answerC", q.getAnswerC());
                    qMap.put("answerD", q.getAnswerD());
                    qMap.put("score", q.getScore());
                    questions.add(qMap);
                }
            }

            // 处理填空题
            List<FillQuestion> fillList = (List<FillQuestion>) paper.get("fillQuestions");
            if (fillList != null) {
                for (FillQuestion q : fillList) {
                    Map<String, Object> qMap = new HashMap<>();
                    qMap.put("questionId", q.getQuestionId());
                    qMap.put("type", 2);
                    qMap.put("typeName", "填空题");
                    qMap.put("question", q.getQuestion());
                    qMap.put("score", q.getScore());
                    questions.add(qMap);
                }
            }

            // 处理判断题
            List<JudgeQuestion> judgeList = (List<JudgeQuestion>) paper.get("judgeQuestions");
            if (judgeList != null) {
                for (JudgeQuestion q : judgeList) {
                    Map<String, Object> qMap = new HashMap<>();
                    qMap.put("questionId", q.getQuestionId());
                    qMap.put("type", 3);
                    qMap.put("typeName", "判断题");
                    qMap.put("question", q.getQuestion());
                    qMap.put("score", q.getScore());
                    questions.add(qMap);
                }
            }

            result.put("questions", questions);
            Map<String, Object> stats = (Map<String, Object>) paper.get("stats");
            result.put("totalScore", stats.get("totalScore"));
                        
            // 动态计算考试时间：根据题目数量，每题约 2-3 分钟
            int questionCount = questions.size();
            int timeLimit = Math.min(questionCount * 3, 180); // 最多 3 小时
            result.put("timeLimit", timeLimit); // 单位：分钟
                        
            // 添加考试说明
            result.put("examInstructions", "本考试共有 " + questionCount + " 道题，总分 " + stats.get("totalScore") + " 分，考试时间 " + timeLimit + " 分钟。");

            return ApiResultHandler.buildApiResult(200, "智能组卷成功", result);
        } else {
            return ApiResultHandler.buildApiResult(500, (String) paper.get("message"), null);
        }
    }

    /**
     * 提交模拟考试答卷并评分
     * POST /study/mock/grade
     *
     * @param params 参数 Map，包含 answers(答案列表)
     * @return 评分结果
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/mock/grade")
    public ApiResult gradeMockExam(@RequestBody Map<String, Object> params) {
        try {
            java.util.List<Map<String, Object>> answers = (java.util.List<Map<String, Object>>) params.get("answers");
            Integer studentId = params.get("studentId") != null
                    ? Integer.parseInt(String.valueOf(params.get("studentId")))
                    : null;

            if (studentId == null) {
                System.out.println("警告: 模拟考试提交没有 studentId，无法保存记录！");
            }

            if (answers == null || answers.isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "答案不能为空", null);
            }

            int totalScore = 0;
            int earnedScore = 0;
            int objEarned = 0; // 客观题（选择/填空/判断）实际得分，与正式考试的 ptScore 语义保持一致
            int correctCount = 0;
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();

            for (Map<String, Object> ans : answers) {
                // 安全转换类型，防止 ClassCastException
                int type = Integer.parseInt(String.valueOf(ans.get("type")));
                int questionId = Integer.parseInt(String.valueOf(ans.get("questionId")));
                String userAnswer = ans.get("answer") != null ? ans.get("answer").toString().trim() : "";
                int score = ans.get("score") != null ? Integer.parseInt(String.valueOf(ans.get("score"))) : 2;

                Map<String, Object> result = new java.util.HashMap<>();
                result.put("questionId", questionId);
                result.put("type", type);
                result.put("userAnswer", userAnswer);
                result.put("score", score);
                totalScore += score;

                String correctAnswer = "";
                String analysis = "";
                boolean isCorrect = false;
                int questionEarned = 0; // 本题实际得分

                if (type == 1) {
                    // 选择题
                    com.rabbiter.oes.entity.MultiQuestion q = multiQuestionMapper.findById(questionId);
                    if (q != null) {
                        correctAnswer = q.getRightAnswer();
                        analysis = q.getAnalysis();
                        result.put("question", q.getQuestion());
                        isCorrect = correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer);
                    }
                    if (isCorrect) questionEarned = score;
                } else if (type == 2) {
                    // 填空题（支持逗号分隔的多个可接受答案，与前端 answer.vue 逻辑一致）
                    com.rabbiter.oes.entity.FillQuestion q = fillQuestionMapper.findById(questionId);
                    if (q != null) {
                        correctAnswer = q.getAnswer();
                        analysis = q.getAnalysis();
                        result.put("question", q.getQuestion());
                        if (correctAnswer != null && !userAnswer.isEmpty()) {
                            String[] alternatives = correctAnswer.split(",");
                            for (String alt : alternatives) {
                                if (alt.trim().equalsIgnoreCase(userAnswer)) {
                                    isCorrect = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isCorrect) questionEarned = score;
                } else if (type == 3) {
                    // 判断题
                    com.rabbiter.oes.entity.JudgeQuestion q = judgeQuestionMapper.findById(questionId);
                    if (q != null) {
                        correctAnswer = q.getAnswer();
                        analysis = q.getAnalysis();
                        result.put("question", q.getQuestion());
                        isCorrect = correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer);
                    }
                    if (isCorrect) questionEarned = score;
                } else if (type == 4) {
                    // 主观题 - 使用 SubjectiveGradingUtil 算法自动评分（与试卷练习、正式考试的算法保持一致）
                    com.rabbiter.oes.entity.SubjectiveQuestion q = subjectiveQuestionMapper.findById(questionId);
                    if (q != null) {
                        correctAnswer = q.getReferenceAnswer() != null ? q.getReferenceAnswer() : "";
                        analysis = q.getAnalysis();
                        result.put("question", q.getQuestion());
                        if (!userAnswer.isEmpty()) {
                            questionEarned = SubjectiveGradingUtil.autoGrade(userAnswer, correctAnswer, score);
                        }
                        // 得分达到满分 60% 以上计为正确
                        isCorrect = questionEarned >= (int) Math.ceil(score * 0.6);
                        result.put("subjectiveScore", questionEarned);
                    }
                }

                earnedScore += questionEarned;
                if (type != 4) objEarned += questionEarned; // 累计客观题得分
                if (isCorrect) correctCount++;
                result.put("correct", isCorrect);
                result.put("correctAnswer", correctAnswer);
                result.put("analysis", analysis != null ? analysis : "");
                results.add(result);
            }

            Map<String, Object> gradeResult = new java.util.HashMap<>();
            gradeResult.put("results", results);
            gradeResult.put("totalScore", totalScore);
            gradeResult.put("earnedScore", earnedScore);
            gradeResult.put("totalQuestions", answers.size());
            gradeResult.put("correctCount", correctCount);
            gradeResult.put("accuracy", answers.size() > 0 ? Math.round(correctCount * 100.0 / answers.size()) : 0);

            System.out.println("模拟考试评分 - 得分:" + earnedScore + "/" + totalScore +
                    " 正确:" + correctCount + "/" + answers.size());

            // ========== 持久化模拟考试数据 ==========
            if (studentId != null) {
                try {
                    String subject = params.get("subject") != null ? params.get("subject").toString() : "综合";
                    String examDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String answerDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    // 检查是否重复提交（同一天同一科目）
                    Score existingScore = scoreMapper.findByStudentAndSubject(studentId, subject, examDateStr);
                    if (existingScore != null) {
                        throw new IllegalArgumentException("您今天已经提交过" + subject + "的模拟考试，请勿重复提交！");
                    }

                    // 1. 创建并保存 ExamManage 记录
                    ExamManage examManage = new ExamManage();
                    examManage.setDescription("模拟考试 - " + subject);
                    examManage.setSource("智能组卷");
                    examManage.setExamDate(examDateStr);
                    examManage.setTotalTime(60); // 默认60分钟
                    examManage.setTotalScore(totalScore);
                    examManage.setType("模拟考试");
                    examManage.setGrade("");
                    examManage.setTerm("");
                    examManage.setMajor("");
                    examManage.setInstitute("");
                    examManage.setTips("本次模拟考试由系统智能生成");

                    // 2. 获取新的 paperId (从现有最大paperId+1)
                    ExamManage lastPaper = examManageMapper.findOnlyPaperId();
                    int newPaperId = (lastPaper != null && lastPaper.getPaperId() != null) ? lastPaper.getPaperId() + 1
                            : 1;
                    examManage.setPaperId(newPaperId);

                    // 3. 插入 ExamManage，获取自动生成的 examCode
                    examManageMapper.add(examManage);
                    Integer examCode = examManage.getExamCode();

                    // 4. 保存 PaperManage 记录（题目与试卷的关联）
                    for (Map<String, Object> ans : answers) {
                        PaperManage pm = new PaperManage();
                        pm.setPaperId(newPaperId);
                        pm.setQuestionId(Integer.parseInt(String.valueOf(ans.get("questionId"))));
                        pm.setQuestionType(Integer.parseInt(String.valueOf(ans.get("type"))));
                        paperService.add(pm);
                    }

                    // 5. 保存 Score 记录
                    Score scoreRecord = new Score();
                    scoreRecord.setExamCode(examCode);
                    scoreRecord.setStudentId(studentId);
                    scoreRecord.setSubject(subject);
                    scoreRecord.setEtScore(earnedScore);
                    scoreRecord.setScore(earnedScore);
                    scoreRecord.setPtScore(objEarned); // 客观题得分，与正式考试 ptScore 语义一致（非试卷满分）
                    scoreRecord.setAnswerDate(examDateStr);
                    scoreMapper.add(scoreRecord);

                    // 6. 保存错题到 WrongQuestion 表
                    for (Map<String, Object> result : results) {
                        boolean isCorrect = (boolean) result.get("correct");
                        if (!isCorrect) {
                            WrongQuestion wq = new WrongQuestion();
                            wq.setStudentId(studentId);
                            wq.setQuestionId((Integer) result.get("questionId"));
                            wq.setQuestionType((Integer) result.get("type"));
                            wq.setSubject(subject);
                            wq.setWrongAnswer(
                                    result.get("userAnswer") != null ? result.get("userAnswer").toString() : "");
                            wq.setCorrectAnswer(
                                    result.get("correctAnswer") != null ? result.get("correctAnswer").toString() : "");
                            wq.setAnalysis(result.get("analysis") != null ? result.get("analysis").toString() : "");
                            wq.setQuestionContent(
                                    result.get("question") != null ? result.get("question").toString() : "");
                            wq.setWrongCount(1);
                            wq.setMastered(0); // 0-未掌握
                            wq.setLastWrongTime(new Date());

                            // 使用 StudyService 的方法来处理重复错题（自动累加错误次数）
                            studyService.addOrUpdateWrongQuestion(wq);
                        }
                    }

                    // 7. 保存 StudyRecord 记录（用于学习曲线统计）
                    StudyRecord studyRecord = new StudyRecord();
                    studyRecord.setStudentId(studentId);
                    studyRecord.setPracticeDate(new Date());
                    studyRecord.setPracticeType(3); // 3-正式考试/模拟考试
                    studyRecord.setTotalQuestions(answers.size());
                    studyRecord.setCorrectCount(correctCount);
                    studyRecord.setWrongCount(answers.size() - correctCount);
                    studyRecord.setScore(earnedScore);
                    studyRecord.setTotalScore(totalScore);
                    studyRecord.setSubject(subject);
                    studyRecord.setExamCode(examCode);
                    // 计算正确率
                    if (answers.size() > 0) {
                        double acc = (double) correctCount / answers.size() * 100;
                        studyRecord.setAccuracy(Math.round(acc * 100.0) / 100.0);
                    } else {
                        studyRecord.setAccuracy(0.0);
                    }
                    studyService.addStudyRecord(studyRecord);

                    // 提示保存成功
                    System.out.println("模拟考试记录已保存 - ExamCode: " + examCode);
                    return ApiResultHandler.buildApiResult(200, "评分完成，记录已保存", gradeResult);
                } catch (Exception saveEx) {
                    System.err.println("保存模拟考试记录失败: " + saveEx.getMessage());
                    saveEx.printStackTrace();
                    return ApiResultHandler.buildApiResult(500, "保存记录失败: " + saveEx.getMessage(), gradeResult);
                }
            }

            return ApiResultHandler.buildApiResult(200, "评分完成，未保存记录(无学生ID)", gradeResult);
        } catch (Exception e) {
            System.out.println("评分失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "评分失败: " + e.getMessage(), null);
        }
    }

    // ========== 学习记录相关接口 ==========

    /**
     * 提交练习结果（记录学习记录+错题）
     * POST /study/record
     */
    @PostMapping("/record")
    public ApiResult addStudyRecord(@RequestBody StudyRecord record) {
        try {
            System.out.println("========== 记录学习记录 ==========");
            System.out.println("学生ID: " + record.getStudentId());
            System.out.println("总题数: " + record.getTotalQuestions());
            System.out.println("正确数: " + record.getCorrectCount());
            System.out.println("错误数: " + record.getWrongCount());
            System.out.println("正确率: " + record.getAccuracy());
            System.out.println("科目: " + record.getSubject());

            studyService.addStudyRecord(record);
            System.out.println("学习记录成功!");
            return ApiResultHandler.buildApiResult(200, "记录成功", null);
        } catch (Exception e) {
            System.out.println("学习记录失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "记录失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取学习记录列表
     * GET /study/record/{studentId}
     */
    @GetMapping("/record/{studentId}")
    public ApiResult getStudyRecords(@PathVariable Integer studentId) {
        List<StudyRecord> records = studyService.getStudyRecords(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", records);
    }

    /**
     * 获取学习曲线数据（每日统计）
     * GET /study/curve/{studentId}?days=30
     */
    @GetMapping("/curve/{studentId}")
    public ApiResult getDailyStats(
            @PathVariable Integer studentId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        System.out.println("获取学习曲线 - 学生ID:" + studentId + " 天数:" + days);

        List<Map<String, Object>> dailyStats = studyService.getDailyStats(studentId, days);
        return ApiResultHandler.buildApiResult(200, "查询成功", dailyStats);
    }

    /**
     * 获取学生学习概览
     * GET /study/overview/{studentId}
     */
    @GetMapping("/overview/{studentId}")
    public ApiResult getStudentOverview(@PathVariable Integer studentId) {
        Map<String, Object> overview = studyService.getStudentOverview(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", overview);
    }

    // ========== 老师端相关接口 ==========

    /**
     * 获取学生详细学习报告
     * GET /study/report/{studentId}
     */
    @GetMapping("/report/{studentId}")
    public ApiResult getStudentReport(@PathVariable Integer studentId) {
        System.out.println("获取学生报告 - 学生ID:" + studentId);

        Map<String, Object> report = studyService.getStudentReport(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", report);
    }

    /**
     * 获取班级学习概览
     * GET /study/class/overview?clazz=xxx
     */
    @GetMapping("/class/overview")
    public ApiResult getClassOverview(@RequestParam String clazz) {
        System.out.println("获取班级概览 - 班级:" + clazz);

        List<Map<String, Object>> overview = studyService.getClassOverview(clazz);
        return ApiResultHandler.buildApiResult(200, "查询成功", overview);
    }

    // ========== AI智能推荐相关接口 ==========

    /**
     * AI分析薄弱知识点
     * GET /study/ai/weak-points/{studentId}
     */
    @GetMapping("/ai/weak-points/{studentId}")
    public ApiResult analyzeWeakPoints(@PathVariable Integer studentId) {
        System.out.println("AI分析薄弱知识点 - 学生ID:" + studentId);

        Map<String, Object> analysis = studyService.analyzeWeakPoints(studentId);
        return ApiResultHandler.buildApiResult(200, "分析成功", analysis);
    }

    /**
     * 获取AI推荐练习题
     * GET /study/ai/recommend/{studentId}?subject=xxx&count=10
     */
    @GetMapping("/ai/recommend/{studentId}")
    public ApiResult getRecommendedQuestions(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false, defaultValue = "10") Integer count) {

        System.out.println("AI推荐练习题 - 学生ID:" + studentId + " 科目:" + subject + " 数量:" + count);

        List<Map<String, Object>> questions = studyService.getRecommendedQuestions(studentId, subject, count);
        return ApiResultHandler.buildApiResult(200, "获取成功", questions);
    }

    /**
     * 根据学生薄弱点智能组卷
     * POST /study/ai/smart-practice/{studentId}
     * 根据学生的学习情况自动生成针对性练习试卷
     */
    @PostMapping("/ai/smart-practice/{studentId}")
    public ApiResult generateSmartPractice(@PathVariable Integer studentId) {
        System.out.println("============ 智能专项练习组卷 ============");
        System.out.println("学生ID: " + studentId);

        try {
            // 1. 获取学生薄弱知识点分析
            Map<String, Object> analysis = studyService.analyzeWeakPoints(studentId);
            List<Map<String, Object>> weakSubjects = (List<Map<String, Object>>) analysis.get("weakSubjects");

            // 2. 构建组卷参数
            GeneticPaperRequest request = new GeneticPaperRequest();

            // 设置薄弱科目
            List<String> subjects = new ArrayList<>();
            if (weakSubjects != null && !weakSubjects.isEmpty()) {
                for (int i = 0; i < Math.min(3, weakSubjects.size()); i++) {
                    String subject = (String) weakSubjects.get(i).get("subject");
                    if (subject != null && !subject.isEmpty()) {
                        subjects.add(subject);
                    }
                }
            }
            request.setSubjects(subjects.isEmpty() ? null : subjects);

            // 设置难度（根据薄弱程度调整，薄弱越严重越简单）
            Integer totalWrongQuestions = (Integer) analysis.get("totalWrongQuestions");
            double difficulty;
            if (totalWrongQuestions == null || totalWrongQuestions < 10) {
                difficulty = 3.0; // 错题较少，中等难度
            } else if (totalWrongQuestions < 30) {
                difficulty = 2.5; // 错题较多，稍低难度
            } else {
                difficulty = 2.0; // 错题很多，较低难度帮助巩固
            }
            request.setTargetDifficulty(difficulty);
            request.setDifficultyTolerance(1.0);

            // 设置题型配置（专项练习量适中）
            request.setMultiCount(8);
            request.setFillCount(4);
            request.setJudgeCount(4);
            request.setSubjectiveCount(1);
            request.setMultiScore(5);
            request.setFillScore(5);
            request.setJudgeScore(5);
            request.setSubjectiveScore(15);

            // 简化算法参数以加快组卷速度
            request.setPopulationSize(30);
            request.setMaxGenerations(50);

            System.out.println("薄弱科目: " + subjects);
            System.out.println("目标难度: " + difficulty);

            // 3. 调用智能组卷
            Map<String, Object> result = geneticPaperService.generatePaper(request);

            if ((Boolean) result.get("success")) {
                result.put("weakAnalysis", analysis);
                System.out.println("智能专项练习组卷成功!");
                return ApiResultHandler.buildApiResult(200, "智能组卷成功", result);
            } else {
                System.out.println("组卷失败: " + result.get("message"));
                return ApiResultHandler.buildApiResult(400, (String) result.get("message"), null);
            }
        } catch (Exception e) {
            System.out.println("智能组卷异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "组卷失败: " + e.getMessage(), null);
        }
    }

    // ========== 考试档案/历史相关接口 ==========

    /**
     * 获取考试历史
     * GET /study/history/{studentId}
     */
    @GetMapping("/history/{studentId}")
    public ApiResult getExamHistory(@PathVariable Integer studentId) {
        System.out.println("获取考试历史 - 学生ID:" + studentId);
        List<Map<String, Object>> history = studyService.getExamHistory(studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", history);
    }

    /**
     * 重做试卷（获取试卷详情）
     * GET /study/redo/{paperId}
     */
    @GetMapping("/redo/{paperId}")
    public ApiResult redoExam(@PathVariable Integer paperId) {
        System.out.println("重做试卷 - PaperID:" + paperId);
        Map<String, Object> paper = studyService.getPaperDetails(paperId);
        if (paper != null) {
            return ApiResultHandler.buildApiResult(200, "获取试卷成功", paper);
        } else {
            return ApiResultHandler.buildApiResult(404, "试卷不存在", null);
        }
    }
}
