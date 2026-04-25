package com.rabbiter.oes.entity;

import java.util.List;
import java.util.Map;

/**
 * 试卷质量评价结果实体类
 */
public class PaperQualityResult {
    
    // 基本信息
    private Integer examCode;
    private String examName;
    private Integer totalStudents;      // 参考人数
    private Integer totalQuestions;     // 题目总数
    private Double totalScore;          // 试卷总分
    
    // 整体指标
    private Double averageScore;        // 平均分
    private Double standardDeviation;   // 标准差
    private Double passRate;            // 及格率
    private Double excellentRate;       // 优秀率
    private Double averageDifficulty;   // 平均难度
    private Double averageDiscrimination; // 平均区分度
    private Double reliability;         // 信度（克伦巴赫α）
    private Double validity;            // 效度
    
    // 信度评价
    private String reliabilityLevel;    // 信度等级：非常好/良好/一般/较差
    private String reliabilityDesc;     // 信度描述
    
    // 效度评价
    private String validityLevel;       // 效度等级
    private String validityDesc;        // 效度描述
    
    // 难度分布
    private Integer easyCount;          // 简单题数量（难度<0.3）
    private Integer mediumCount;        // 中等题数量（0.3-0.7）
    private Integer hardCount;          // 困难题数量（>0.7）
    
    // 区分度分布
    private Integer goodDiscrimination;     // 良好区分度题数（>0.4）
    private Integer fairDiscrimination;     // 一般区分度题数（0.2-0.4）
    private Integer poorDiscrimination;     // 较差区分度题数（<0.2）
    
    // 题目详细分析
    private List<QuestionAnalysis> questionAnalysisList;
    
    // 分数分布
    private Map<String, Integer> scoreDistribution;
    
    // 高级统计量
    private Double skewness;            // 偏度
    private Double kurtosis;            // 峰度
    private String normalityAssessment; // 正态性评价
    private Double fergusonDelta;       // 弗格森系数（整体区分力）
    private Double medianScore;         // 中位数
    private Double maxScoreValue;       // 最高分
    private Double minScoreValue;       // 最低分
    private Double coefficientOfVariation; // 变异系数（CV）
    private Double splitHalfReliability;   // 折半信度（Spearman-Brown校正后）
    
    // 质量等级
    private String qualityGrade;        // 综合质量等级 A/B/C/D/E
    private Double qualityScore;        // 综合质量分（0-100）
    
    // 高级可视化数据
    private Map<String, Double> radarData;  // 雷达图数据（多维评价）
    private List<Map<String, Object>> scatterData; // 难度-区分度散点图数据
    private List<Map<String, Object>> normalCurveData; // 正态拟合曲线数据
    private List<Map<String, Object>> scoreFrequency;  // 细粒度分数频率分布
    
    // 综合评价
    private String overallAssessment;   // 综合评价
    private List<String> suggestions;   // 改进建议
    
    // 内部类：题目分析
    public static class QuestionAnalysis {
        private Integer questionId;
        private Integer questionType;   // 1选择 2填空 3判断 4主观
        private String questionTypeName;
        private String questionContent;
        private Double difficulty;      // 难度系数（答错率）
        private Double discrimination;  // 区分度
        private Double pointBiserial;   // 点双列相关系数（题目-总分相关）
        private Integer correctCount;   // 答对人数
        private Integer totalCount;     // 作答人数
        private String difficultyLevel; // 难度等级
        private String discriminationLevel; // 区分度等级
        private String itemQuality;     // 题目质量综合评价：优秀/良好/一般/差/极差
        
        // Getters and Setters
        public Integer getQuestionId() { return questionId; }
        public void setQuestionId(Integer questionId) { this.questionId = questionId; }
        
        public Integer getQuestionType() { return questionType; }
        public void setQuestionType(Integer questionType) { this.questionType = questionType; }
        
        public String getQuestionTypeName() { return questionTypeName; }
        public void setQuestionTypeName(String questionTypeName) { this.questionTypeName = questionTypeName; }
        
        public String getQuestionContent() { return questionContent; }
        public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
        
        public Double getDifficulty() { return difficulty; }
        public void setDifficulty(Double difficulty) { this.difficulty = difficulty; }
        
        public Double getDiscrimination() { return discrimination; }
        public void setDiscrimination(Double discrimination) { this.discrimination = discrimination; }
        
        public Integer getCorrectCount() { return correctCount; }
        public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
        
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        
        public String getDiscriminationLevel() { return discriminationLevel; }
        public void setDiscriminationLevel(String discriminationLevel) { this.discriminationLevel = discriminationLevel; }
        
        public Double getPointBiserial() { return pointBiserial; }
        public void setPointBiserial(Double pointBiserial) { this.pointBiserial = pointBiserial; }
        
        public String getItemQuality() { return itemQuality; }
        public void setItemQuality(String itemQuality) { this.itemQuality = itemQuality; }
    }
    
    // Getters and Setters
    public Integer getExamCode() { return examCode; }
    public void setExamCode(Integer examCode) { this.examCode = examCode; }
    
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    
    public Double getStandardDeviation() { return standardDeviation; }
    public void setStandardDeviation(Double standardDeviation) { this.standardDeviation = standardDeviation; }
    
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    
    public Double getExcellentRate() { return excellentRate; }
    public void setExcellentRate(Double excellentRate) { this.excellentRate = excellentRate; }
    
    public Double getAverageDifficulty() { return averageDifficulty; }
    public void setAverageDifficulty(Double averageDifficulty) { this.averageDifficulty = averageDifficulty; }
    
    public Double getAverageDiscrimination() { return averageDiscrimination; }
    public void setAverageDiscrimination(Double averageDiscrimination) { this.averageDiscrimination = averageDiscrimination; }
    
    public Double getReliability() { return reliability; }
    public void setReliability(Double reliability) { this.reliability = reliability; }
    
    public Double getValidity() { return validity; }
    public void setValidity(Double validity) { this.validity = validity; }
    
    public String getReliabilityLevel() { return reliabilityLevel; }
    public void setReliabilityLevel(String reliabilityLevel) { this.reliabilityLevel = reliabilityLevel; }
    
    public String getReliabilityDesc() { return reliabilityDesc; }
    public void setReliabilityDesc(String reliabilityDesc) { this.reliabilityDesc = reliabilityDesc; }
    
    public String getValidityLevel() { return validityLevel; }
    public void setValidityLevel(String validityLevel) { this.validityLevel = validityLevel; }
    
    public String getValidityDesc() { return validityDesc; }
    public void setValidityDesc(String validityDesc) { this.validityDesc = validityDesc; }
    
    public Integer getEasyCount() { return easyCount; }
    public void setEasyCount(Integer easyCount) { this.easyCount = easyCount; }
    
    public Integer getMediumCount() { return mediumCount; }
    public void setMediumCount(Integer mediumCount) { this.mediumCount = mediumCount; }
    
    public Integer getHardCount() { return hardCount; }
    public void setHardCount(Integer hardCount) { this.hardCount = hardCount; }
    
    public Integer getGoodDiscrimination() { return goodDiscrimination; }
    public void setGoodDiscrimination(Integer goodDiscrimination) { this.goodDiscrimination = goodDiscrimination; }
    
    public Integer getFairDiscrimination() { return fairDiscrimination; }
    public void setFairDiscrimination(Integer fairDiscrimination) { this.fairDiscrimination = fairDiscrimination; }
    
    public Integer getPoorDiscrimination() { return poorDiscrimination; }
    public void setPoorDiscrimination(Integer poorDiscrimination) { this.poorDiscrimination = poorDiscrimination; }
    
    public List<QuestionAnalysis> getQuestionAnalysisList() { return questionAnalysisList; }
    public void setQuestionAnalysisList(List<QuestionAnalysis> questionAnalysisList) { this.questionAnalysisList = questionAnalysisList; }
    
    public Map<String, Integer> getScoreDistribution() { return scoreDistribution; }
    public void setScoreDistribution(Map<String, Integer> scoreDistribution) { this.scoreDistribution = scoreDistribution; }
    
    public String getOverallAssessment() { return overallAssessment; }
    public void setOverallAssessment(String overallAssessment) { this.overallAssessment = overallAssessment; }
    
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    
    public Double getSkewness() { return skewness; }
    public void setSkewness(Double skewness) { this.skewness = skewness; }
    
    public Double getKurtosis() { return kurtosis; }
    public void setKurtosis(Double kurtosis) { this.kurtosis = kurtosis; }
    
    public String getNormalityAssessment() { return normalityAssessment; }
    public void setNormalityAssessment(String normalityAssessment) { this.normalityAssessment = normalityAssessment; }
    
    public Double getFergusonDelta() { return fergusonDelta; }
    public void setFergusonDelta(Double fergusonDelta) { this.fergusonDelta = fergusonDelta; }
    
    public Double getMedianScore() { return medianScore; }
    public void setMedianScore(Double medianScore) { this.medianScore = medianScore; }
    
    public Double getMaxScoreValue() { return maxScoreValue; }
    public void setMaxScoreValue(Double maxScoreValue) { this.maxScoreValue = maxScoreValue; }
    
    public Double getMinScoreValue() { return minScoreValue; }
    public void setMinScoreValue(Double minScoreValue) { this.minScoreValue = minScoreValue; }
    
    public Double getCoefficientOfVariation() { return coefficientOfVariation; }
    public void setCoefficientOfVariation(Double coefficientOfVariation) { this.coefficientOfVariation = coefficientOfVariation; }
    
    public Double getSplitHalfReliability() { return splitHalfReliability; }
    public void setSplitHalfReliability(Double splitHalfReliability) { this.splitHalfReliability = splitHalfReliability; }
    
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    
    public Map<String, Double> getRadarData() { return radarData; }
    public void setRadarData(Map<String, Double> radarData) { this.radarData = radarData; }
    
    public List<Map<String, Object>> getScatterData() { return scatterData; }
    public void setScatterData(List<Map<String, Object>> scatterData) { this.scatterData = scatterData; }
    
    public List<Map<String, Object>> getNormalCurveData() { return normalCurveData; }
    public void setNormalCurveData(List<Map<String, Object>> normalCurveData) { this.normalCurveData = normalCurveData; }
    
    public List<Map<String, Object>> getScoreFrequency() { return scoreFrequency; }
    public void setScoreFrequency(List<Map<String, Object>> scoreFrequency) { this.scoreFrequency = scoreFrequency; }
}
