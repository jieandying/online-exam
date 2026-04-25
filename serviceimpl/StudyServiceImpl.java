package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.StudyRecord;
import com.rabbiter.oes.entity.WrongQuestion;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.StudyRecordMapper;
import com.rabbiter.oes.mapper.WrongQuestionMapper;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习记录服务实现类
 */
@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    @Autowired
    private StudyRecordMapper studyRecordMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;

    // ========== 错题本相关 ==========

    @Override
    public void addOrUpdateWrongQuestion(WrongQuestion wrongQuestion) {
        // 查找是否已存在该错题记录
        WrongQuestion existing = wrongQuestionMapper.findByStudentAndQuestion(
                wrongQuestion.getStudentId(),
                wrongQuestion.getQuestionType(),
                wrongQuestion.getQuestionId());

        if (existing != null) {
            // 已存在，更新错误次数
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongTime(new Date());
            existing.setWrongAnswer(wrongQuestion.getWrongAnswer());
            existing.setMastered(0); // 又做错了，标记为未掌握

            // 更新其他可能变动的信息
            existing.setAnalysis(wrongQuestion.getAnalysis());
            existing.setCorrectAnswer(wrongQuestion.getCorrectAnswer());
            existing.setQuestionContent(wrongQuestion.getQuestionContent());
            existing.setSubject(wrongQuestion.getSubject());

            wrongQuestionMapper.update(existing);
        } else {
            // 不存在，新增记录
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongTime(new Date());
            wrongQuestion.setMastered(0);
            wrongQuestionMapper.add(wrongQuestion);
        }
    }

    @Override
    public List<WrongQuestion> getWrongQuestions(Integer studentId) {
        return wrongQuestionMapper.findByStudentId(studentId);
    }

    @Override
    public List<WrongQuestion> getWrongQuestionsByType(Integer studentId, Integer questionType) {
        return wrongQuestionMapper.findByType(studentId, questionType);
    }

    @Override
    public void markAsMastered(Integer wrongQuestionId) {
        wrongQuestionMapper.markAsMastered(wrongQuestionId);
    }

    /**
     * 按排序方式获取错题列表
     * 
     * @param studentId 学生ID
     * @param sortType  排序方式：recommend-推荐排序, wrongCount-错误次数, latest-最近出错
     * @return 排序后的错题列表
     */
    @Override
    public List<WrongQuestion> getWrongQuestionsWithSort(Integer studentId, String sortType) {
        if ("recommend".equals(sortType)) {
            return wrongQuestionMapper.findByStudentIdOrderByRecommend(studentId);
        } else if ("wrongCount".equals(sortType)) {
            return wrongQuestionMapper.findByStudentIdOrderByWrongCount(studentId);
        } else {
            // 默认按最近出错排序（latest）
            return wrongQuestionMapper.findByStudentId(studentId);
        }
    }

    /**
     * 重做错题并校验答案
     * 选择题和判断题做精确匹配（忽略大小写），填空题做去空格模糊匹配，主观题仅记录不自动判分
     * 
     * @param wrongQuestionId 错题记录ID
     * @param userAnswer      用户提交的答案
     * @return 校验结果 Map，包含 correct(是否正确)、correctAnswer(正确答案)
     */
    @Override
    public Map<String, Object> checkRedoAnswer(Integer wrongQuestionId, String userAnswer) {
        Map<String, Object> result = new HashMap<>();
        WrongQuestion wq = wrongQuestionMapper.findById(wrongQuestionId);

        if (wq == null) {
            result.put("correct", false);
            result.put("message", "错题记录不存在");
            return result;
        }

        String correctAnswer = wq.getCorrectAnswer();
        result.put("correctAnswer", correctAnswer);
        result.put("questionType", wq.getQuestionType());

        // 主观题(4)不自动判分，仅记录
        if (wq.getQuestionType() != null && wq.getQuestionType() == 4) {
            result.put("correct", null);
            result.put("message", "主观题需人工批阅，答案已记录");
            return result;
        }

        // 比对答案
        boolean isCorrect = false;
        if (correctAnswer != null && userAnswer != null) {
            String normalizedCorrect = correctAnswer.trim().toLowerCase();
            String normalizedUser = userAnswer.trim().toLowerCase();

            if (wq.getQuestionType() != null && wq.getQuestionType() == 2) {
                // 填空题：去除所有空格后比较
                normalizedCorrect = normalizedCorrect.replaceAll("\\s+", "");
                normalizedUser = normalizedUser.replaceAll("\\s+", "");
            }
            isCorrect = normalizedCorrect.equals(normalizedUser);
        }

        result.put("correct", isCorrect);

        if (isCorrect) {
            // 回答正确，自动标记为已掌握
            wrongQuestionMapper.markAsMastered(wrongQuestionId);
            result.put("message", "回答正确！已自动标记为掌握");
        } else {
            // 回答错误，错误次数+1
            wq.setWrongCount(wq.getWrongCount() + 1);
            wq.setLastWrongTime(new Date());
            wq.setWrongAnswer(userAnswer);
            wrongQuestionMapper.update(wq);
            result.put("message", "回答错误，继续加油！");
        }

        return result;
    }

    @Override
    public Map<String, Object> getWrongQuestionStats(Integer studentId) {
        Map<String, Object> stats = new HashMap<>();

        // 总错题数
        int totalWrong = wrongQuestionMapper.countByStudentId(studentId);
        stats.put("totalWrong", totalWrong);

        // 按科目统计
        List<Map<String, Object>> bySubject = wrongQuestionMapper.countBySubject(studentId);
        stats.put("bySubject", bySubject);

        // 各类型错题数
        stats.put("multiWrong", wrongQuestionMapper.findByType(studentId, 1).size());
        stats.put("fillWrong", wrongQuestionMapper.findByType(studentId, 2).size());
        stats.put("judgeWrong", wrongQuestionMapper.findByType(studentId, 3).size());

        return stats;
    }

    // ========== 学习记录相关 ==========

    @Override
    public void addStudyRecord(StudyRecord record) {
        if (record.getPracticeDate() == null) {
            record.setPracticeDate(new Date());
        }
        // 计算正确率
        if (record.getTotalQuestions() != null && record.getTotalQuestions() > 0) {
            double accuracy = (double) record.getCorrectCount() / record.getTotalQuestions() * 100;
            record.setAccuracy(Math.round(accuracy * 100.0) / 100.0); // 保留两位小数
        }
        studyRecordMapper.add(record);
    }

    @Override
    public List<StudyRecord> getStudyRecords(Integer studentId) {
        return studyRecordMapper.findByStudentId(studentId);
    }

    @Override
    public List<Map<String, Object>> getDailyStats(Integer studentId, Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        return studyRecordMapper.getDailyStats(studentId, days);
    }

    @Override
    public Map<String, Object> getStudentOverview(Integer studentId) {
        return studyRecordMapper.getStudentOverview(studentId);
    }

    // ========== 老师端相关 ==========

    @Override
    public Map<String, Object> getStudentReport(Integer studentId) {
        Map<String, Object> report = new HashMap<>();

        // 学生基本信息
        Student student = studentMapper.findById(studentId);
        report.put("student", student);

        // 学习概览
        Map<String, Object> overview = getStudentOverview(studentId);
        report.put("overview", overview);

        // 最近30天学习曲线数据
        List<Map<String, Object>> dailyStats = getDailyStats(studentId, 30);
        report.put("dailyStats", dailyStats);

        // 错题统计
        Map<String, Object> wrongStats = getWrongQuestionStats(studentId);
        report.put("wrongStats", wrongStats);

        // 错题列表
        List<WrongQuestion> wrongQuestions = getWrongQuestions(studentId);
        report.put("wrongQuestions", wrongQuestions);

        return report;
    }

    @Override
    public List<Map<String, Object>> getClassOverview(String clazz) {
        return studyRecordMapper.getClassOverview(clazz);
    }

    // ========== AI智能推荐相关 ==========

    @Override
    public Map<String, Object> analyzeWeakPoints(Integer studentId) {
        Map<String, Object> analysis = new HashMap<>();

        // 1. 获取薄弱科目排名（按错误次数排序）
        List<Map<String, Object>> weakSubjects = wrongQuestionMapper.getTopWeakSubjects(studentId, 5);
        analysis.put("weakSubjects", weakSubjects);

        // 2. 按科目和题型统计
        List<Map<String, Object>> bySubjectAndType = wrongQuestionMapper.countBySubjectAndType(studentId);
        analysis.put("bySubjectAndType", bySubjectAndType);

        // 3. 计算每个科目的薄弱程度分析
        List<Map<String, Object>> subjectAnalysis = new java.util.ArrayList<>();
        for (Map<String, Object> subject : weakSubjects) {
            Map<String, Object> subjectInfo = new HashMap<>();
            String subjectName = (String) subject.get("subject");
            subjectInfo.put("subject", subjectName);
            subjectInfo.put("wrongCount", subject.get("count"));
            subjectInfo.put("totalWrongTimes", subject.get("totalWrongCount"));

            // 计算薄弱程度分数 (0-100)
            Object totalWrongCount = subject.get("totalWrongCount");
            int totalWrong = 0;
            if (totalWrongCount instanceof Long) {
                totalWrong = ((Long) totalWrongCount).intValue();
            } else if (totalWrongCount instanceof Integer) {
                totalWrong = (Integer) totalWrongCount;
            } else if (totalWrongCount instanceof java.math.BigDecimal) {
                totalWrong = ((java.math.BigDecimal) totalWrongCount).intValue();
            }

            // 薄弱度算法：错误次数越多，薄弱度越高
            int weaknessScore = Math.min(100, totalWrong * 10);
            subjectInfo.put("weaknessScore", weaknessScore);

            // 薄弱程度等级
            String level;
            String suggestion;
            if (weaknessScore >= 80) {
                level = "严重薄弱";
                suggestion = "建议立即进行专项练习，重点攻克该科目";
            } else if (weaknessScore >= 60) {
                level = "比较薄弱";
                suggestion = "建议增加练习频率，注意错题复习";
            } else if (weaknessScore >= 30) {
                level = "轻度薄弱";
                suggestion = "保持现有学习节奏，适当巩固";
            } else {
                level = "掌握较好";
                suggestion = "继续保持，可以挑战更高难度";
            }
            subjectInfo.put("level", level);
            subjectInfo.put("suggestion", suggestion);

            // 获取该科目的典型错题（最多3道）
            List<WrongQuestion> typicalWrongs = wrongQuestionMapper.findBySubject(studentId, subjectName);
            if (typicalWrongs.size() > 3) {
                typicalWrongs = typicalWrongs.subList(0, 3);
            }
            subjectInfo.put("typicalWrongs", typicalWrongs);

            subjectAnalysis.add(subjectInfo);
        }
        analysis.put("subjectAnalysis", subjectAnalysis);

        // 4. 总体评估
        int totalWrongQuestions = wrongQuestionMapper.countByStudentId(studentId);
        analysis.put("totalWrongQuestions", totalWrongQuestions);

        // 综合评伍
        String overallAssessment;
        if (totalWrongQuestions == 0) {
            overallAssessment = "太棒了！你目前没有错题记录，继续保持！";
        } else if (totalWrongQuestions <= 5) {
            overallAssessment = "表现不错！错题较少，注意复习巩固即可。";
        } else if (totalWrongQuestions <= 15) {
            overallAssessment = "还需加油！建议按照薄弱点分析进行针对性练习。";
        } else {
            overallAssessment = "需要努力！建议系统性复习，重点攻克薄弱科目。";
        }
        analysis.put("overallAssessment", overallAssessment);

        // 5. 全局题型统计 (用于图表)
        analysis.put("multiWrong", wrongQuestionMapper.findByType(studentId, 1).size());
        analysis.put("fillWrong", wrongQuestionMapper.findByType(studentId, 2).size());
        analysis.put("judgeWrong", wrongQuestionMapper.findByType(studentId, 3).size());

        return analysis;
    }

    @Override
    public List<Map<String, Object>> getRecommendedQuestions(Integer studentId, String subject, Integer count) {
        List<Map<String, Object>> recommended = new java.util.ArrayList<>();

        if (count == null || count <= 0) {
            count = 10;
        }

        // 如果没有指定科目，根据薄弱点推荐
        List<String> targetSubjects = new java.util.ArrayList<>();
        if (subject == null || subject.isEmpty()) {
            // 获取最薄弱的科目
            List<Map<String, Object>> weakSubjects = wrongQuestionMapper.getTopWeakSubjects(studentId, 3);
            for (Map<String, Object> ws : weakSubjects) {
                String subjectName = (String) ws.get("subject");
                if (subjectName != null && !subjectName.isEmpty()) {
                    targetSubjects.add(subjectName);
                }
            }
            // 如果没有错题记录，用默认科目
            if (targetSubjects.isEmpty()) {
                targetSubjects.add("计算机网络");
                targetSubjects.add("Java程序设计");
                targetSubjects.add("数据结构");
            }
        } else {
            targetSubjects.add(subject);
        }

        // 为每个科目推荐题目
        int questionsPerSubject = Math.max(1, count / targetSubjects.size());

        for (String targetSubject : targetSubjects) {
            // 获取该科目的错题
            List<WrongQuestion> wrongQuestions = wrongQuestionMapper.findBySubject(studentId, targetSubject);

            // 根据错题的题目类型推荐相似题目
            java.util.Set<Integer> wrongQuestionIds = new java.util.HashSet<>();
            for (WrongQuestion wq : wrongQuestions) {
                wrongQuestionIds.add(wq.getQuestionId());
            }

            // 从题库中随机获取该科目的题目（排除已做错的）
            int addedCount = 0;

            // 选择题
            try {
                List<Integer> multiIds = multiQuestionMapper.findBySubjectAndLevel(targetSubject, null,
                        questionsPerSubject);
                for (Integer qId : multiIds) {
                    if (addedCount >= questionsPerSubject)
                        break;
                    Map<String, Object> question = new HashMap<>();
                    question.put("questionId", qId);
                    question.put("type", 1);
                    question.put("typeName", "选择题");
                    question.put("subject", targetSubject);
                    question.put("reason", "该科目是你的薄弱点，建议加强练习");
                    recommended.add(question);
                    addedCount++;
                }
            } catch (Exception e) {
                // 忽略异常
            }

            // 填空题
            try {
                List<Integer> fillIds = fillQuestionMapper.findBySubjectAndLevel(targetSubject, null,
                        questionsPerSubject);
                for (Integer qId : fillIds) {
                    if (addedCount >= questionsPerSubject)
                        break;
                    Map<String, Object> question = new HashMap<>();
                    question.put("questionId", qId);
                    question.put("type", 2);
                    question.put("typeName", "填空题");
                    question.put("subject", targetSubject);
                    question.put("reason", "该科目是你的薄弱点，建议加强练习");
                    recommended.add(question);
                    addedCount++;
                }
            } catch (Exception e) {
                // 忽略异常
            }

            // 判断题
            try {
                List<Integer> judgeIds = judgeQuestionMapper.findBySubjectAndLevel(targetSubject, null,
                        questionsPerSubject);
                for (Integer qId : judgeIds) {
                    if (addedCount >= questionsPerSubject)
                        break;
                    Map<String, Object> question = new HashMap<>();
                    question.put("questionId", qId);
                    question.put("type", 3);
                    question.put("typeName", "判断题");
                    question.put("subject", targetSubject);
                    question.put("reason", "该科目是你的薄弱点，建议加强练习");
                    recommended.add(question);
                    addedCount++;
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        // 限制返回数量
        if (recommended.size() > count) {
            recommended = recommended.subList(0, count);
        }

        return recommended;
    }

    /**
     * 生成模拟考试试卷
     * 从题库随机抽取各类型题目，组成一份模拟试卷
     *
     * @param subject    科目（可选）
     * @param multiCount 选择题数量
     * @param fillCount  填空题数量
     * @param judgeCount 判断题数量
     * @return 模拟试卷数据
     */
    @Override
    public Map<String, Object> generateMockExam(String subject, int multiCount, int fillCount, int judgeCount) {
        Map<String, Object> paper = new HashMap<>();
        java.util.List<Map<String, Object>> questions = new java.util.ArrayList<>();
        int totalScore = 0;
        int questionIndex = 1;

        // 抽取选择题
        List<com.rabbiter.oes.entity.MultiQuestion> multiList = multiQuestionMapper.findRandom(multiCount);
        for (com.rabbiter.oes.entity.MultiQuestion q : multiList) {
            Map<String, Object> item = new HashMap<>();
            item.put("index", questionIndex++);
            item.put("type", 1);
            item.put("typeName", "选择题");
            item.put("questionId", q.getQuestionId());
            item.put("question", q.getQuestion());
            item.put("answerA", q.getAnswerA());
            item.put("answerB", q.getAnswerB());
            item.put("answerC", q.getAnswerC());
            item.put("answerD", q.getAnswerD());
            item.put("score", q.getScore() != null ? q.getScore() : 2);
            item.put("subject", q.getSubject());
            // 不暴露正确答案，交卷后再返回
            totalScore += q.getScore() != null ? q.getScore() : 2;
            questions.add(item);
        }

        // 抽取填空题
        List<com.rabbiter.oes.entity.FillQuestion> fillList = fillQuestionMapper.findRandom(fillCount);
        for (com.rabbiter.oes.entity.FillQuestion q : fillList) {
            Map<String, Object> item = new HashMap<>();
            item.put("index", questionIndex++);
            item.put("type", 2);
            item.put("typeName", "填空题");
            item.put("questionId", q.getQuestionId());
            item.put("question", q.getQuestion());
            item.put("score", q.getScore() != null ? q.getScore() : 5);
            item.put("subject", q.getSubject());
            totalScore += q.getScore() != null ? q.getScore() : 5;
            questions.add(item);
        }

        // 抽取判断题
        List<com.rabbiter.oes.entity.JudgeQuestion> judgeList = judgeQuestionMapper.findRandom(judgeCount);
        for (com.rabbiter.oes.entity.JudgeQuestion q : judgeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("index", questionIndex++);
            item.put("type", 3);
            item.put("typeName", "判断题");
            item.put("questionId", q.getQuestionId());
            item.put("question", q.getQuestion());
            item.put("score", q.getScore() != null ? q.getScore() : 2);
            item.put("subject", q.getSubject());
            totalScore += q.getScore() != null ? q.getScore() : 2;
            questions.add(item);
        }

        paper.put("questions", questions);
        paper.put("totalScore", totalScore);
        paper.put("totalQuestions", questions.size());
        paper.put("timeLimit", Math.max(30, questions.size() * 2)); // 每题约2分钟
        paper.put("multiCount", multiList.size());
        paper.put("fillCount", fillList.size());
        paper.put("judgeCount", judgeList.size());

        return paper;
    }

    @Autowired
    private com.rabbiter.oes.mapper.ScoreMapper scoreMapper;

    @Autowired
    private com.rabbiter.oes.service.PaperService paperService;

    @Override
    public List<Map<String, Object>> getExamHistory(Integer studentId) {
        return scoreMapper.findHistoryByStudentId(studentId);
    }

    @Override
    public Map<String, Object> getPaperDetails(Integer paperId) {
        List<com.rabbiter.oes.entity.PaperManage> paperList = paperService.findById(paperId);
        if (paperList == null || paperList.isEmpty()) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        java.util.List<Map<String, Object>> questions = new java.util.ArrayList<>();
        int totalScore = 0;
        int questionIndex = 1;

        for (com.rabbiter.oes.entity.PaperManage pm : paperList) {
            Integer type = pm.getQuestionType();
            Integer qId = pm.getQuestionId();
            Map<String, Object> item = new HashMap<>();

            if (type == 1) { // Multi
                com.rabbiter.oes.entity.MultiQuestion q = multiQuestionMapper.findById(qId);
                if (q != null) {
                    item.put("type", 1);
                    item.put("typeName", "选择题");
                    item.put("questionId", q.getQuestionId());
                    item.put("question", q.getQuestion());
                    item.put("answerA", q.getAnswerA());
                    item.put("answerB", q.getAnswerB());
                    item.put("answerC", q.getAnswerC());
                    item.put("answerD", q.getAnswerD());
                    item.put("score", q.getScore());
                    item.put("subject", q.getSubject());
                    totalScore += q.getScore() != null ? q.getScore() : 2;
                }
            } else if (type == 2) { // Fill
                com.rabbiter.oes.entity.FillQuestion q = fillQuestionMapper.findById(qId);
                if (q != null) {
                    item.put("type", 2);
                    item.put("typeName", "填空题");
                    item.put("questionId", q.getQuestionId());
                    item.put("question", q.getQuestion());
                    item.put("score", q.getScore());
                    item.put("subject", q.getSubject());
                    totalScore += q.getScore() != null ? q.getScore() : 5;
                }
            } else if (type == 3) { // Judge
                com.rabbiter.oes.entity.JudgeQuestion q = judgeQuestionMapper.findById(qId);
                if (q != null) {
                    item.put("type", 3);
                    item.put("typeName", "判断题");
                    item.put("questionId", q.getQuestionId());
                    item.put("question", q.getQuestion());
                    item.put("score", q.getScore());
                    item.put("subject", q.getSubject());
                    totalScore += q.getScore() != null ? q.getScore() : 2;
                }
            }

            if (!item.isEmpty()) {
                item.put("index", questionIndex++);
                questions.add(item);
            }
        }

        result.put("questions", questions);
        result.put("totalScore", totalScore);
        result.put("totalQuestions", questions.size());
        result.put("timeLimit", 120);

        return result;
    }
}
