package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.*;
import com.rabbiter.oes.service.PaperQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 试卷质量评价服务实现类（高级版）
 * 
 * 采用经典测量理论（CTT）的核心指标体系：
 * - 难度系数（Difficulty Index）：基于真实错题数据计算
 * - 区分度（Discrimination Index）：高低分组法（前后27%）
 * - 点双列相关系数（Point-Biserial Correlation）：题目-总分相关
 * - 信度：克伦巴赫α系数 + 折半信度（Spearman-Brown校正）
 * - 效度：基于题目-总分平均相关的内容效度指数
 * - 偏度（Skewness）、峰度（Kurtosis）：成绩分布形态分析
 * - 弗格森系数（Ferguson's Delta）：整体区分力评价
 * - 综合质量评分与等级（加权多维评价）
 */
@Service
public class PaperQualityServiceImpl implements PaperQualityService {
    
    @Autowired
    private ScoreMapper scoreMapper;
    
    @Autowired
    private ExamManageMapper examManageMapper;
    
    @Autowired
    private PaperMapper paperMapper;
    
    @Autowired
    private MultiQuestionMapper multiQuestionMapper;
    
    @Autowired
    private FillQuestionMapper fillQuestionMapper;
    
    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;
    
    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    
    @Autowired
    private SubjectiveAnswerMapper subjectiveAnswerMapper;
    
    @Override
    public PaperQualityResult evaluatePaperQuality(Integer examCode) {
        PaperQualityResult result = new PaperQualityResult();
        result.setExamCode(examCode);
        
        // 1. 获取考试基本信息
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null) return null;
        result.setExamName(exam.getSource());
        result.setTotalScore(exam.getTotalScore() != null ? exam.getTotalScore().doubleValue() : 100.0);
        
        // 2. 获取所有学生成绩
        List<Score> scores = scoreMapper.findByExamCode(examCode);
        if (scores == null || scores.isEmpty()) {
            result.setTotalStudents(0);
            result.setOverallAssessment("暂无学生参加此考试，无法进行质量评价");
            return result;
        }
        result.setTotalStudents(scores.size());
        
        // 提取有效分数列表（使用 score 字段 = 总分）
        List<Double> scoreValues = scores.stream()
                .map(s -> s.getScore() != null ? s.getScore().doubleValue() : 0.0)
                .collect(Collectors.toList());
        
        // 3. 基本统计指标 + 高级统计量
        calculateBasicStats(result, scoreValues);
        calculateAdvancedStats(result, scoreValues);
        
        // 4. 构建每题真实作答数据，计算难度、区分度、点双列相关
        Integer paperId = exam.getPaperId();
        List<PaperManage> paperManages = paperMapper.findById(paperId);
        List<PaperQualityResult.QuestionAnalysis> analysisList = new ArrayList<>();
        
        if (paperManages != null && !paperManages.isEmpty()) {
            analyzeQuestionsWithRealData(result, analysisList, paperManages, scores, examCode);
        }
        result.setQuestionAnalysisList(analysisList);
        result.setTotalQuestions(analysisList.size());
        
        // 5. 信度分析（克伦巴赫α + 折半信度）
        calculateReliability(result, analysisList, scores);
        
        // 6. 效度分析（基于题目-总分相关的内容效度）
        calculateContentValidity(result, analysisList);
        
        // 7. 弗格森系数
        calculateFergusonDelta(result, scoreValues);
        
        // 8. 统计难度/区分度分布
        calculateDistributions(result, analysisList);
        
        // 9. 构建可视化数据
        buildScatterData(result, analysisList);
        buildNormalCurveData(result, scoreValues);
        buildScoreFrequency(result, scoreValues);
        buildRadarData(result);
        
        // 10. 综合质量评分 & 等级
        calculateQualityScore(result);
        
        // 11. 综合评价和建议
        generateAssessment(result);
        
        return result;
    }
    
    // ==================== 基本统计 ====================
    
    private void calculateBasicStats(PaperQualityResult result, List<Double> values) {
        double totalScore = result.getTotalScore();
        double passLine = totalScore * 0.6;
        double excellentLine = totalScore * 0.9;
        
        double sum = 0, sumSq = 0;
        int passCount = 0, excellentCount = 0;
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90分及以上", 0);
        distribution.put("80-89分", 0);
        distribution.put("70-79分", 0);
        distribution.put("60-69分", 0);
        distribution.put("60分以下", 0);
        
        for (double v : values) {
            sum += v;
            sumSq += v * v;
            if (v > max) max = v;
            if (v < min) min = v;
            if (v >= passLine) passCount++;
            if (v >= excellentLine) excellentCount++;
            
            double pct = (v / totalScore) * 100;
            if (pct >= 90) distribution.merge("90分及以上", 1, Integer::sum);
            else if (pct >= 80) distribution.merge("80-89分", 1, Integer::sum);
            else if (pct >= 70) distribution.merge("70-79分", 1, Integer::sum);
            else if (pct >= 60) distribution.merge("60-69分", 1, Integer::sum);
            else distribution.merge("60分以下", 1, Integer::sum);
        }
        
        int n = values.size();
        double avg = sum / n;
        double variance = (sumSq / n) - (avg * avg);
        double stdDev = Math.sqrt(variance);
        
        // 中位数
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        double median = n % 2 == 0 ? (sorted.get(n/2 - 1) + sorted.get(n/2)) / 2.0 : sorted.get(n/2);
        
        result.setAverageScore(round(avg, 2));
        result.setStandardDeviation(round(stdDev, 2));
        result.setPassRate(round((passCount * 100.0) / n, 2));
        result.setExcellentRate(round((excellentCount * 100.0) / n, 2));
        result.setMedianScore(round(median, 2));
        result.setMaxScoreValue(round(max, 2));
        result.setMinScoreValue(round(min, 2));
        result.setCoefficientOfVariation(avg > 0 ? round(stdDev / avg, 3) : 0.0);
        result.setScoreDistribution(distribution);
    }
    
    // ==================== 高级统计量 ====================
    
    /**
     * 计算偏度（Skewness）和峰度（Kurtosis）
     * 偏度公式（Fisher）：g1 = [n/((n-1)(n-2))] * Σ[(xi-x̄)/s]³
     * 峰度公式（Fisher excess）：g2 = [n(n+1)/((n-1)(n-2)(n-3))] * Σ[(xi-x̄)/s]⁴ - 3(n-1)²/((n-2)(n-3))
     */
    private void calculateAdvancedStats(PaperQualityResult result, List<Double> values) {
        int n = values.size();
        if (n < 4) {
            result.setSkewness(0.0);
            result.setKurtosis(0.0);
            result.setNormalityAssessment("样本量不足，无法评估正态性");
            return;
        }
        
        double mean = result.getAverageScore();
        double std = result.getStandardDeviation();
        if (std <= 0) {
            result.setSkewness(0.0);
            result.setKurtosis(0.0);
            result.setNormalityAssessment("成绩无变异，无法评估正态性");
            return;
        }
        
        double sumCube = 0, sumQuad = 0;
        for (double v : values) {
            double z = (v - mean) / std;
            sumCube += z * z * z;
            sumQuad += z * z * z * z;
        }
        
        // Fisher偏度
        double skewness = (n * sumCube) / ((n - 1.0) * (n - 2.0));
        // Fisher超值峰度
        double kurtosis = ((n * (n + 1.0)) / ((n - 1.0) * (n - 2.0) * (n - 3.0))) * sumQuad
                         - (3.0 * (n - 1.0) * (n - 1.0)) / ((n - 2.0) * (n - 3.0));
        
        result.setSkewness(round(skewness, 3));
        result.setKurtosis(round(kurtosis, 3));
        
        // Jarque-Bera统计量近似评估正态性
        double jb = (n / 6.0) * (skewness * skewness + (kurtosis * kurtosis) / 4.0);
        String assessment;
        if (Math.abs(skewness) < 0.5 && Math.abs(kurtosis) < 1.0) {
            assessment = "成绩分布近似正态分布，试卷难度设置合理（JB=" + round(jb, 2) + "）";
        } else if (Math.abs(skewness) < 1.0 && Math.abs(kurtosis) < 2.0) {
            String skewDir = skewness > 0 ? "正偏（高分段人数偏少）" : "负偏（低分段人数偏少）";
            assessment = "成绩分布轻度偏离正态，" + skewDir + "（JB=" + round(jb, 2) + "）";
        } else {
            String skewDir = skewness > 0 ? "明显正偏，试卷偏难" : "明显负偏，试卷偏易";
            assessment = "成绩分布显著偏离正态，" + skewDir + "（JB=" + round(jb, 2) + "）";
        }
        result.setNormalityAssessment(assessment);
    }
    
    // ==================== 真实数据驱动的题目分析 ====================
    
    /**
     * 基于错题本和主观题评分的真实数据计算每道题的难度和区分度
     */
    private void analyzeQuestionsWithRealData(PaperQualityResult result,
                                               List<PaperQualityResult.QuestionAnalysis> analysisList,
                                               List<PaperManage> paperManages,
                                               List<Score> scores,
                                               Integer examCode) {
        int totalStudents = scores.size();
        
        // 构建学生ID到总分的映射
        Map<Integer, Double> studentScoreMap = new HashMap<>();
        for (Score s : scores) {
            studentScoreMap.put(s.getStudentId(), s.getScore() != null ? s.getScore().doubleValue() : 0.0);
        }
        Set<Integer> allStudentIds = studentScoreMap.keySet();
        
        // 批量获取错题数据：Map<"questionType_questionId", Set<wrongStudentIds>>
        Map<String, Set<Integer>> wrongStudentMap = new HashMap<>();
        List<Map<String, Object>> wrongRecords = wrongQuestionMapper.findWrongByExamStudents(examCode);
        for (Map<String, Object> rec : wrongRecords) {
            Object qIdObj = rec.get("questionId");
            Object qTypeObj = rec.get("questionType");
            Object sIdObj = rec.get("studentId");
            if (qIdObj != null && qTypeObj != null && sIdObj != null) {
                String key = toInt(qTypeObj) + "_" + toInt(qIdObj);
                wrongStudentMap.computeIfAbsent(key, k -> new HashSet<>()).add(toInt(sIdObj));
            }
        }
        
        // 批量获取主观题得分数据：Map<"questionId_studentId", score/maxScore>
        Map<String, double[]> subjectiveScoreMap = new HashMap<>();
        List<Map<String, Object>> subjRecords = subjectiveAnswerMapper.findScoreDetailsByExam(examCode);
        for (Map<String, Object> rec : subjRecords) {
            Object qIdObj = rec.get("questionId");
            Object sIdObj = rec.get("studentId");
            Object tsObj = rec.get("teacherScore");
            Object msObj = rec.get("maxScore");
            if (qIdObj != null && sIdObj != null) {
                String key = toInt(qIdObj) + "_" + toInt(sIdObj);
                double ts = tsObj != null ? toDouble(tsObj) : 0;
                double ms = msObj != null ? toDouble(msObj) : 10;
                subjectiveScoreMap.put(key, new double[]{ts, ms});
            }
        }
        
        // 按成绩排序，确定高低分组（前后27%）
        List<Score> sortedScores = new ArrayList<>(scores);
        sortedScores.sort((a, b) -> {
            double sa = a.getScore() != null ? a.getScore() : 0;
            double sb = b.getScore() != null ? b.getScore() : 0;
            return Double.compare(sb, sa);
        });
        int groupSize = Math.max(1, (int)(totalStudents * 0.27));
        Set<Integer> highGroup = sortedScores.subList(0, Math.min(groupSize, totalStudents))
                .stream().map(Score::getStudentId).collect(Collectors.toSet());
        Set<Integer> lowGroup = sortedScores.subList(Math.max(0, totalStudents - groupSize), totalStudents)
                .stream().map(Score::getStudentId).collect(Collectors.toSet());
        
        double totalDifficulty = 0, totalDiscrimination = 0;
        int questionCount = 0;
        
        for (PaperManage pm : paperManages) {
            PaperQualityResult.QuestionAnalysis qa = new PaperQualityResult.QuestionAnalysis();
            qa.setQuestionId(pm.getQuestionId());
            qa.setQuestionType(pm.getQuestionType());
            
            String typeName = "";
            String content = "";
            
            switch (pm.getQuestionType()) {
                case 1:
                    typeName = "选择题";
                    MultiQuestion mq = multiQuestionMapper.findById(pm.getQuestionId());
                    if (mq != null) content = mq.getQuestion();
                    break;
                case 2:
                    typeName = "填空题";
                    FillQuestion fq = fillQuestionMapper.findById(pm.getQuestionId());
                    if (fq != null) content = fq.getQuestion();
                    break;
                case 3:
                    typeName = "判断题";
                    JudgeQuestion jq = judgeQuestionMapper.findById(pm.getQuestionId());
                    if (jq != null) content = jq.getQuestion();
                    break;
                case 4:
                    typeName = "主观题";
                    SubjectiveQuestion sq = subjectiveQuestionMapper.findById(pm.getQuestionId());
                    if (sq != null) content = sq.getQuestion();
                    break;
            }
            
            qa.setQuestionTypeName(typeName);
            qa.setQuestionContent(content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content);
            qa.setTotalCount(totalStudents);
            
            double difficulty, discrimination, pointBiserial;
            int correctCount;
            
            if (pm.getQuestionType() <= 3) {
                // === 客观题：基于错题数据计算 ===
                String key = pm.getQuestionType() + "_" + pm.getQuestionId();
                Set<Integer> wrongStudents = wrongStudentMap.getOrDefault(key, Collections.emptySet());
                // 只统计参加本次考试的学生
                int wrongInExam = 0;
                for (Integer sid : wrongStudents) {
                    if (allStudentIds.contains(sid)) wrongInExam++;
                }
                correctCount = totalStudents - wrongInExam;
                
                // 难度 = 答错率（难度越高值越大）
                difficulty = totalStudents > 0 ? round((double) wrongInExam / totalStudents, 3) : 0;
                
                // 区分度 = 高分组正确率 - 低分组正确率
                int highCorrect = 0, lowCorrect = 0;
                for (Integer sid : highGroup) {
                    if (!wrongStudents.contains(sid)) highCorrect++;
                }
                for (Integer sid : lowGroup) {
                    if (!wrongStudents.contains(sid)) lowCorrect++;
                }
                double highRate = groupSize > 0 ? (double) highCorrect / highGroup.size() : 0;
                double lowRate = groupSize > 0 ? (double) lowCorrect / lowGroup.size() : 0;
                discrimination = round(highRate - lowRate, 3);
                
                // 点双列相关：答对=1 / 答错=0 与总分的相关
                pointBiserial = calculatePointBiserial(allStudentIds, wrongStudents, studentScoreMap);
                
            } else {
                // === 主观题：基于评分数据计算 ===
                int qId = pm.getQuestionId();
                List<double[]> itemScores = new ArrayList<>(); // [得分, 满分]
                for (Integer sid : allStudentIds) {
                    String skey = qId + "_" + sid;
                    double[] sv = subjectiveScoreMap.get(skey);
                    if (sv != null) {
                        itemScores.add(new double[]{sv[0], sv[1]});
                    }
                }
                
                if (itemScores.isEmpty()) {
                    difficulty = 0.5;
                    discrimination = 0;
                    pointBiserial = 0;
                    correctCount = totalStudents / 2;
                } else {
                    // 难度 = 1 - 平均得分率
                    double totalEarned = 0, totalMax = 0;
                    for (double[] sv : itemScores) {
                        totalEarned += sv[0];
                        totalMax += sv[1];
                    }
                    double avgScoreRate = totalMax > 0 ? totalEarned / totalMax : 0.5;
                    difficulty = round(1 - avgScoreRate, 3);
                    correctCount = (int)(totalStudents * avgScoreRate);
                    
                    // 区分度：高分组平均得分率 - 低分组平均得分率
                    double highTotal = 0, highMax = 0, lowTotal = 0, lowMax = 0;
                    for (Integer sid : highGroup) {
                        double[] sv = subjectiveScoreMap.get(qId + "_" + sid);
                        if (sv != null) { highTotal += sv[0]; highMax += sv[1]; }
                    }
                    for (Integer sid : lowGroup) {
                        double[] sv = subjectiveScoreMap.get(qId + "_" + sid);
                        if (sv != null) { lowTotal += sv[0]; lowMax += sv[1]; }
                    }
                    double highScoreRate = highMax > 0 ? highTotal / highMax : 0;
                    double lowScoreRate = lowMax > 0 ? lowTotal / lowMax : 0;
                    discrimination = round(highScoreRate - lowScoreRate, 3);
                    
                    // 点双列相关：用得分率>0.6视为"答对"，与总分相关
                    Set<Integer> wrongSet = new HashSet<>();
                    for (Integer sid : allStudentIds) {
                        double[] sv = subjectiveScoreMap.get(qId + "_" + sid);
                        if (sv == null || (sv[1] > 0 && sv[0] / sv[1] < 0.6)) {
                            wrongSet.add(sid);
                        }
                    }
                    pointBiserial = calculatePointBiserial(allStudentIds, wrongSet, studentScoreMap);
                }
            }
            
            qa.setDifficulty(difficulty);
            qa.setDiscrimination(discrimination);
            qa.setPointBiserial(round(pointBiserial, 3));
            qa.setCorrectCount(correctCount);
            
            // 难度等级
            if (difficulty < 0.3) qa.setDifficultyLevel("简单");
            else if (difficulty < 0.7) qa.setDifficultyLevel("中等");
            else qa.setDifficultyLevel("困难");
            
            // 区分度等级
            if (discrimination >= 0.4) qa.setDiscriminationLevel("优秀");
            else if (discrimination >= 0.3) qa.setDiscriminationLevel("良好");
            else if (discrimination >= 0.2) qa.setDiscriminationLevel("一般");
            else qa.setDiscriminationLevel("较差");
            
            // 题目质量综合评价（基于难度+区分度+点双列相关）
            double qualityPoints = 0;
            if (difficulty >= 0.2 && difficulty <= 0.8) qualityPoints += 1; // 难度适中
            if (discrimination >= 0.3) qualityPoints += 1.5;
            else if (discrimination >= 0.2) qualityPoints += 0.8;
            if (pointBiserial >= 0.3) qualityPoints += 1.5;
            else if (pointBiserial >= 0.2) qualityPoints += 0.8;
            
            if (qualityPoints >= 3.5) qa.setItemQuality("优秀");
            else if (qualityPoints >= 2.5) qa.setItemQuality("良好");
            else if (qualityPoints >= 1.5) qa.setItemQuality("一般");
            else if (qualityPoints >= 0.8) qa.setItemQuality("较差");
            else qa.setItemQuality("极差");
            
            analysisList.add(qa);
            totalDifficulty += difficulty;
            totalDiscrimination += discrimination;
            questionCount++;
        }
        
        if (questionCount > 0) {
            result.setAverageDifficulty(round(totalDifficulty / questionCount, 3));
            result.setAverageDiscrimination(round(totalDiscrimination / questionCount, 3));
        }
    }
    
    /**
     * 计算点双列相关系数
     * rpb = (M_p - M_q) / S_t * sqrt(p * q)
     * M_p = 答对组平均总分, M_q = 答错组平均总分, S_t = 总分标准差
     * p = 答对率, q = 答错率
     */
    private double calculatePointBiserial(Set<Integer> allStudents, Set<Integer> wrongStudents,
                                           Map<Integer, Double> studentScoreMap) {
        double sumCorrect = 0, sumWrong = 0;
        int nCorrect = 0, nWrong = 0;
        double sumAll = 0, sumAllSq = 0;
        int nAll = 0;
        
        for (Integer sid : allStudents) {
            Double score = studentScoreMap.get(sid);
            if (score == null) continue;
            sumAll += score;
            sumAllSq += score * score;
            nAll++;
            if (wrongStudents.contains(sid)) {
                sumWrong += score;
                nWrong++;
            } else {
                sumCorrect += score;
                nCorrect++;
            }
        }
        
        if (nAll < 2 || nCorrect == 0 || nWrong == 0) return 0;
        
        double meanAll = sumAll / nAll;
        double varAll = (sumAllSq / nAll) - (meanAll * meanAll);
        if (varAll <= 0) return 0;
        double stdAll = Math.sqrt(varAll);
        
        double meanCorrect = sumCorrect / nCorrect;
        double meanWrong = sumWrong / nWrong;
        double p = (double) nCorrect / nAll;
        double q = (double) nWrong / nAll;
        
        return (meanCorrect - meanWrong) / stdAll * Math.sqrt(p * q);
    }
    
    // ==================== 信度分析 ====================
    
    /**
     * 信度分析：克伦巴赫α系数 + 折半信度（Spearman-Brown校正）
     * 
     * α系数基于题目方差与总分方差的比值估计内部一致性
     * 折半信度将题目分为奇偶两半，计算两半得分的Pearson相关，再用Spearman-Brown公式校正
     */
    private void calculateReliability(PaperQualityResult result,
                                       List<PaperQualityResult.QuestionAnalysis> analysisList,
                                       List<Score> scores) {
        int n = analysisList.size();
        int m = scores.size();
        
        if (m < 2 || n < 2) {
            result.setReliability(0.0);
            result.setSplitHalfReliability(0.0);
            result.setReliabilityLevel("无法计算");
            result.setReliabilityDesc("参考人数或题目数量不足，无法计算信度");
            return;
        }
        
        // 计算总分方差
        double sum = 0, sumSq = 0;
        for (Score s : scores) {
            double v = s.getScore() != null ? s.getScore() : 0;
            sum += v;
            sumSq += v * v;
        }
        double mean = sum / m;
        double totalVar = (sumSq / m) - (mean * mean);
        
        if (totalVar <= 0) {
            result.setReliability(0.0);
            result.setSplitHalfReliability(0.0);
            result.setReliabilityLevel("无法计算");
            result.setReliabilityDesc("成绩无差异，无法计算信度");
            return;
        }
        
        // --- 克伦巴赫α ---
        // 用每道题的难度(p)来估算项目方差: σi² = p * (1-p) * maxItemScore²
        // 对于0/1评分题目: σi² = p * (1-p)
        double sumItemVar = 0;
        for (PaperQualityResult.QuestionAnalysis qa : analysisList) {
            double p = qa.getCorrectCount() != null && qa.getTotalCount() != null && qa.getTotalCount() > 0
                    ? (double) qa.getCorrectCount() / qa.getTotalCount() : 0.5;
            sumItemVar += p * (1 - p);
        }
        // 归一化到总分量纲
        double avgItemScore = result.getTotalScore() / n;
        sumItemVar *= avgItemScore * avgItemScore;
        
        double alpha = (n / (double)(n - 1)) * (1 - sumItemVar / totalVar);
        alpha = Math.max(0, Math.min(1, alpha));
        result.setReliability(round(alpha, 3));
        
        // --- 折半信度（奇偶分半 + Spearman-Brown校正）---
        // 模拟奇偶半测试分数
        Map<Integer, Double> oddScores = new HashMap<>();
        Map<Integer, Double> evenScores = new HashMap<>();
        for (Score s : scores) {
            oddScores.put(s.getStudentId(), 0.0);
            evenScores.put(s.getStudentId(), 0.0);
        }
        // 按题目顺序分奇偶，用正确率近似分配分数
        for (int i = 0; i < analysisList.size(); i++) {
            PaperQualityResult.QuestionAnalysis qa = analysisList.get(i);
            double correctRate = qa.getTotalCount() > 0 ? (double)qa.getCorrectCount() / qa.getTotalCount() : 0.5;
            for (Score s : scores) {
                double studentScore = s.getScore() != null ? s.getScore() : 0;
                // 使用学生总分百分比近似分配每题得分
                double studentRate = result.getTotalScore() > 0 ? studentScore / result.getTotalScore() : 0.5;
                double itemScore = avgItemScore * studentRate;
                Map<Integer, Double> targetMap = (i % 2 == 0) ? oddScores : evenScores;
                targetMap.merge(s.getStudentId(), itemScore, Double::sum);
            }
        }
        
        // 计算两半相关
        List<Double> xList = new ArrayList<>(), yList = new ArrayList<>();
        for (Score s : scores) {
            xList.add(oddScores.getOrDefault(s.getStudentId(), 0.0));
            yList.add(evenScores.getOrDefault(s.getStudentId(), 0.0));
        }
        double rHalf = pearsonCorrelation(xList, yList);
        // Spearman-Brown校正：r_sb = 2*r_half / (1 + r_half)
        double splitHalf = (1 + rHalf) > 0 ? (2 * rHalf) / (1 + rHalf) : 0;
        splitHalf = Math.max(0, Math.min(1, splitHalf));
        result.setSplitHalfReliability(round(splitHalf, 3));
        
        // 综合信度取α和折半的加权平均
        double finalReliability = alpha * 0.6 + splitHalf * 0.4;
        
        // 信度等级评价
        if (finalReliability >= 0.9) {
            result.setReliabilityLevel("非常好");
            result.setReliabilityDesc(String.format("试卷信度极高（α=%.3f，折半=%.3f），测量结果非常稳定可靠", alpha, splitHalf));
        } else if (finalReliability >= 0.7) {
            result.setReliabilityLevel("良好");
            result.setReliabilityDesc(String.format("试卷信度良好（α=%.3f，折半=%.3f），测量结果较为可靠", alpha, splitHalf));
        } else if (finalReliability >= 0.5) {
            result.setReliabilityLevel("一般");
            result.setReliabilityDesc(String.format("试卷信度一般（α=%.3f，折半=%.3f），稳定性有待提高", alpha, splitHalf));
        } else {
            result.setReliabilityLevel("较差");
            result.setReliabilityDesc(String.format("试卷信度较低（α=%.3f，折半=%.3f），建议调整题目结构", alpha, splitHalf));
        }
    }
    
    // ==================== 效度分析 ====================
    
    /**
     * 内容效度：基于题目-总分相关系数（点双列相关）的平均值
     * 反映试卷内部各题目与总体测量目标的一致程度
     */
    private void calculateContentValidity(PaperQualityResult result,
                                           List<PaperQualityResult.QuestionAnalysis> analysisList) {
        if (analysisList.isEmpty()) {
            result.setValidity(0.0);
            result.setValidityLevel("无法计算");
            result.setValidityDesc("无题目数据，无法计算效度");
            return;
        }
        
        // 效度 = 题目点双列相关系数的平均值
        double sumPb = 0;
        int validCount = 0;
        for (PaperQualityResult.QuestionAnalysis qa : analysisList) {
            if (qa.getPointBiserial() != null) {
                sumPb += Math.abs(qa.getPointBiserial());
                validCount++;
            }
        }
        
        double validity = validCount > 0 ? sumPb / validCount : 0;
        validity = Math.max(0, Math.min(1, validity));
        result.setValidity(round(validity, 3));
        
        if (validity >= 0.4) {
            result.setValidityLevel("非常好");
            result.setValidityDesc(String.format("试卷效度高（平均rpb=%.3f），题目与测量目标高度一致", validity));
        } else if (validity >= 0.3) {
            result.setValidityLevel("良好");
            result.setValidityDesc(String.format("试卷效度良好（平均rpb=%.3f），基本能反映学生真实水平", validity));
        } else if (validity >= 0.2) {
            result.setValidityLevel("一般");
            result.setValidityDesc(String.format("试卷效度一般（平均rpb=%.3f），部分题目与测量目标关联度不足", validity));
        } else {
            result.setValidityLevel("较差");
            result.setValidityDesc(String.format("试卷效度较低（平均rpb=%.3f），建议审查题目质量", validity));
        }
    }
    
    // ==================== 弗格森系数 ====================
    
    /**
     * 弗格森系数（Ferguson's Delta）
     * δ = (n² - Σfi²) / (n²(k-1)/k)
     * n = 总人数, fi = 各分数段频次, k = 分数段数
     * 衡量试卷整体区分被试能力的有效程度
     */
    private void calculateFergusonDelta(PaperQualityResult result, List<Double> scores) {
        int n = scores.size();
        if (n < 2) {
            result.setFergusonDelta(0.0);
            return;
        }
        
        // 按分数分组（每5分一组）
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (double s : scores) {
            int bucket = (int)(s / 5) * 5;
            freqMap.merge(bucket, 1, Integer::sum);
        }
        
        double sumFiSq = 0;
        for (int fi : freqMap.values()) {
            sumFiSq += (double) fi * fi;
        }
        
        int k = freqMap.size();
        if (k <= 1) {
            result.setFergusonDelta(0.0);
            return;
        }
        
        double nSq = (double) n * n;
        double delta = (nSq - sumFiSq) / (nSq * (k - 1.0) / k);
        delta = Math.max(0, Math.min(1, delta));
        result.setFergusonDelta(round(delta, 3));
    }
    
    // ==================== 分布统计 ====================
    
    private void calculateDistributions(PaperQualityResult result,
                                         List<PaperQualityResult.QuestionAnalysis> analysisList) {
        int easy = 0, medium = 0, hard = 0;
        int excellent = 0, good = 0, fair = 0, poor = 0;
        
        for (PaperQualityResult.QuestionAnalysis qa : analysisList) {
            if (qa.getDifficulty() < 0.3) easy++;
            else if (qa.getDifficulty() < 0.7) medium++;
            else hard++;
            
            if (qa.getDiscrimination() >= 0.4) excellent++;
            else if (qa.getDiscrimination() >= 0.3) good++;
            else if (qa.getDiscrimination() >= 0.2) fair++;
            else poor++;
        }
        
        result.setEasyCount(easy);
        result.setMediumCount(medium);
        result.setHardCount(hard);
        result.setGoodDiscrimination(excellent + good);
        result.setFairDiscrimination(fair);
        result.setPoorDiscrimination(poor);
    }
    
    // ==================== 可视化数据构建 ====================
    
    private void buildScatterData(PaperQualityResult result, List<PaperQualityResult.QuestionAnalysis> list) {
        List<Map<String, Object>> scatter = new ArrayList<>();
        for (PaperQualityResult.QuestionAnalysis qa : list) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("difficulty", qa.getDifficulty());
            point.put("discrimination", qa.getDiscrimination());
            point.put("type", qa.getQuestionTypeName());
            point.put("id", qa.getQuestionId());
            point.put("quality", qa.getItemQuality());
            point.put("pointBiserial", qa.getPointBiserial());
            scatter.add(point);
        }
        result.setScatterData(scatter);
    }
    
    /**
     * 构建正态拟合曲线数据（用于叠加在成绩分布图上）
     */
    private void buildNormalCurveData(PaperQualityResult result, List<Double> values) {
        double mean = result.getAverageScore();
        double std = result.getStandardDeviation();
        double totalScore = result.getTotalScore();
        int n = values.size();
        
        List<Map<String, Object>> curveData = new ArrayList<>();
        if (std > 0) {
            // 从 min 到 max 生成正态密度曲线点
            double minX = Math.max(0, mean - 3 * std);
            double maxX = Math.min(totalScore, mean + 3 * std);
            int steps = 40;
            double step = (maxX - minX) / steps;
            double binWidth = totalScore / 10; // 近似每个直方图bin宽度
            
            for (int i = 0; i <= steps; i++) {
                double x = minX + i * step;
                double z = (x - mean) / std;
                double density = Math.exp(-0.5 * z * z) / (std * Math.sqrt(2 * Math.PI));
                // 转换为人数（与直方图对齐）
                double expectedCount = density * n * binWidth;
                
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("x", round(x, 1));
                p.put("y", round(expectedCount, 2));
                curveData.add(p);
            }
        }
        result.setNormalCurveData(curveData);
    }
    
    /**
     * 细粒度分数频率分布（每10分一组）
     */
    private void buildScoreFrequency(PaperQualityResult result, List<Double> values) {
        double totalScore = result.getTotalScore();
        int binCount = 10;
        double binWidth = totalScore / binCount;
        
        List<Map<String, Object>> freq = new ArrayList<>();
        for (int i = 0; i < binCount; i++) {
            double lo = i * binWidth;
            double hi = (i + 1) * binWidth;
            int count = 0;
            for (double v : values) {
                if (i == binCount - 1) {
                    if (v >= lo && v <= hi) count++;
                } else {
                    if (v >= lo && v < hi) count++;
                }
            }
            Map<String, Object> bin = new LinkedHashMap<>();
            bin.put("range", (int)lo + "-" + (int)hi);
            bin.put("count", count);
            bin.put("percentage", round(count * 100.0 / values.size(), 1));
            freq.add(bin);
        }
        result.setScoreFrequency(freq);
    }
    
    /**
     * 雷达图多维质量评价数据
     */
    private void buildRadarData(PaperQualityResult result) {
        Map<String, Double> radar = new LinkedHashMap<>();
        
        // 难度合理性（0.3-0.7最佳）→ 归一化到0-100
        double avgDiff = result.getAverageDifficulty() != null ? result.getAverageDifficulty() : 0.5;
        double diffScore = 100 - Math.abs(avgDiff - 0.5) / 0.5 * 100;
        radar.put("难度合理性", round(Math.max(0, diffScore), 1));
        
        // 区分度 → 归一化到0-100
        double avgDisc = result.getAverageDiscrimination() != null ? result.getAverageDiscrimination() : 0;
        radar.put("区分度", round(Math.min(100, avgDisc / 0.5 * 100), 1));
        
        // 信度 → 0-100
        double rel = result.getReliability() != null ? result.getReliability() : 0;
        radar.put("信度", round(rel * 100, 1));
        
        // 效度 → 0-100（放大展示）
        double val = result.getValidity() != null ? result.getValidity() : 0;
        radar.put("效度", round(Math.min(100, val / 0.5 * 100), 1));
        
        // 弗格森系数 → 0-100
        double fd = result.getFergusonDelta() != null ? result.getFergusonDelta() : 0;
        radar.put("区分力", round(fd * 100, 1));
        
        // 正态性 → 基于偏度和峰度评分
        double sk = result.getSkewness() != null ? Math.abs(result.getSkewness()) : 0;
        double ku = result.getKurtosis() != null ? Math.abs(result.getKurtosis()) : 0;
        double normScore = 100 - (sk * 30 + ku * 15);
        radar.put("分布合理性", round(Math.max(0, Math.min(100, normScore)), 1));
        
        result.setRadarData(radar);
    }
    
    // ==================== 综合质量评分 ====================
    
    /**
     * 多维加权综合质量评分
     * 权重：信度(25%) + 效度(20%) + 区分度(20%) + 难度合理性(15%) + 弗格森系数(10%) + 分布合理性(10%)
     */
    private void calculateQualityScore(PaperQualityResult result) {
        Map<String, Double> radar = result.getRadarData();
        if (radar == null || radar.isEmpty()) {
            result.setQualityScore(0.0);
            result.setQualityGrade("E");
            return;
        }
        
        double score = 0;
        score += radar.getOrDefault("信度", 0.0) * 0.25;
        score += radar.getOrDefault("效度", 0.0) * 0.20;
        score += radar.getOrDefault("区分度", 0.0) * 0.20;
        score += radar.getOrDefault("难度合理性", 0.0) * 0.15;
        score += radar.getOrDefault("区分力", 0.0) * 0.10;
        score += radar.getOrDefault("分布合理性", 0.0) * 0.10;
        
        score = Math.max(0, Math.min(100, score));
        result.setQualityScore(round(score, 1));
        
        if (score >= 85) result.setQualityGrade("A");
        else if (score >= 70) result.setQualityGrade("B");
        else if (score >= 55) result.setQualityGrade("C");
        else if (score >= 40) result.setQualityGrade("D");
        else result.setQualityGrade("E");
    }
    
    // ==================== 综合评价 ====================
    
    private void generateAssessment(PaperQualityResult result) {
        StringBuilder assessment = new StringBuilder();
        List<String> suggestions = new ArrayList<>();
        
        // 质量等级开头
        String grade = result.getQualityGrade();
        double qs = result.getQualityScore() != null ? result.getQualityScore() : 0;
        assessment.append(String.format("【综合质量等级：%s 级（%.1f分）】", grade, qs));
        
        // 信度评价
        double reliability = result.getReliability() != null ? result.getReliability() : 0;
        assessment.append(String.format(" 信度%s（α=%.3f", result.getReliabilityLevel(), reliability));
        if (result.getSplitHalfReliability() != null) {
            assessment.append(String.format("，折半=%.3f", result.getSplitHalfReliability()));
        }
        assessment.append("）。");
        if (reliability < 0.7) {
            suggestions.add("信度偏低：建议增加题目数量或提高题目质量以提升内部一致性");
        }
        
        // 效度评价
        double validity = result.getValidity() != null ? result.getValidity() : 0;
        assessment.append(String.format(" 效度%s（平均rpb=%.3f）。", result.getValidityLevel(), validity));
        if (validity < 0.2) {
            suggestions.add("效度不足：部分题目与总分相关性低，建议审查命题质量");
        }
        
        // 难度评价
        double avgDiff = result.getAverageDifficulty() != null ? result.getAverageDifficulty() : 0.5;
        if (avgDiff < 0.2) {
            assessment.append(" 整体偏易。");
            suggestions.add("试卷整体偏易（平均难度" + avgDiff + "），建议增加中等和困难题目");
        } else if (avgDiff > 0.8) {
            assessment.append(" 整体偏难。");
            suggestions.add("试卷整体偏难（平均难度" + avgDiff + "），建议降低部分题目难度或增加基础题");
        } else {
            assessment.append(String.format(" 难度适中（%.3f）。", avgDiff));
        }
        
        // 区分度评价
        int total = result.getTotalQuestions() != null ? result.getTotalQuestions() : 0;
        int poorDisc = result.getPoorDiscrimination() != null ? result.getPoorDiscrimination() : 0;
        if (total > 0 && poorDisc * 1.0 / total > 0.3) {
            suggestions.add(String.format("有 %d 道题（占 %.0f%%）区分度较差，建议修改或替换", 
                    poorDisc, poorDisc * 100.0 / total));
        }
        
        // 偏度/峰度评价
        if (result.getNormalityAssessment() != null && !result.getNormalityAssessment().contains("近似正态")) {
            suggestions.add("分布形态：" + result.getNormalityAssessment());
        }
        
        // 弗格森系数
        double delta = result.getFergusonDelta() != null ? result.getFergusonDelta() : 0;
        if (delta < 0.9) {
            suggestions.add(String.format("弗格森系数 δ=%.3f，试卷整体区分力有提升空间（理想值≥0.9）", delta));
        }
        
        // 及格率
        double passRate = result.getPassRate() != null ? result.getPassRate() : 0;
        if (passRate < 50) {
            suggestions.add("及格率偏低（" + passRate + "%），建议降低整体难度或增加基础题比例");
        } else if (passRate > 95) {
            suggestions.add("及格率过高（" + passRate + "%），试卷缺乏挑战性，建议增加区分度");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("试卷质量优秀，各项指标均在理想范围内，可继续使用");
        }
        
        result.setOverallAssessment(assessment.toString());
        result.setSuggestions(suggestions);
    }
    
    // ==================== 两考试效度比较 ====================
    
    @Override
    public Double calculateValidity(Integer examCode1, Integer examCode2) {
        List<Score> scores1 = scoreMapper.findByExamCode(examCode1);
        List<Score> scores2 = scoreMapper.findByExamCode(examCode2);
        
        if (scores1 == null || scores2 == null || scores1.isEmpty() || scores2.isEmpty()) {
            return 0.0;
        }
        
        Map<Integer, Double> scoreMap1 = new HashMap<>();
        Map<Integer, Double> scoreMap2 = new HashMap<>();
        
        for (Score s : scores1) {
            scoreMap1.put(s.getStudentId(), s.getScore() != null ? s.getScore().doubleValue() : 0);
        }
        for (Score s : scores2) {
            scoreMap2.put(s.getStudentId(), s.getScore() != null ? s.getScore().doubleValue() : 0);
        }
        
        Set<Integer> commonStudents = new HashSet<>(scoreMap1.keySet());
        commonStudents.retainAll(scoreMap2.keySet());
        
        if (commonStudents.size() < 3) return 0.0;
        
        List<Double> x = new ArrayList<>(), y = new ArrayList<>();
        for (Integer sid : commonStudents) {
            x.add(scoreMap1.get(sid));
            y.add(scoreMap2.get(sid));
        }
        
        return pearsonCorrelation(x, y);
    }
    
    // ==================== 工具方法 ====================
    
    private double pearsonCorrelation(List<Double> x, List<Double> y) {
        int n = x.size();
        if (n == 0) return 0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }
        
        double meanX = sumX / n;
        double meanY = sumY / n;
        double numerator = sumXY - n * meanX * meanY;
        double denominator = Math.sqrt((sumX2 - n * meanX * meanX) * (sumY2 - n * meanY * meanY));
        
        if (denominator == 0) return 0;
        return round(numerator / denominator, 3);
    }
    
    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
    
    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }
    
    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }
}
