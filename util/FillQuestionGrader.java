package com.rabbiter.oes.util;

/**
 * 填空题评分工具类
 * 提供精确的填空题答案匹配和评分逻辑
 * 
 * @author OES
 * @date 2026-03-29
 */
public class FillQuestionGrader {

    /**
     * 判断填空题答案是否正确（精确匹配）
     * 
     * @param userAnswer 用户答案
     * @param correctAnswer 正确答案
     * @return true-正确，false-错误
     */
    public static boolean isCorrect(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }
        
        // 去除首尾空格，忽略大小写
        String normalizedUser = normalizeAnswer(userAnswer);
        String normalizedCorrect = normalizeAnswer(correctAnswer);
        
        return normalizedUser.equals(normalizedCorrect);
    }

    /**
     * 判断填空题答案（支持多答案情况）
     * 例如：正确答案是 "ABC,ACB,BAC"，用户答对其中一个即可
     * 
     * @param userAnswer 用户答案
     * @param correctAnswers 多个正确答案（逗号分隔）
     * @return true-正确，false-错误
     */
    public static boolean isCorrectWithMultipleAnswers(String userAnswer, String correctAnswers) {
        if (userAnswer == null || correctAnswers == null) {
            return false;
        }
        
        // 标准化用户答案
        String normalizedUser = normalizeAnswer(userAnswer);
        
        // 分割多个正确答案
        String[] answers = correctAnswers.split(",");
        for (String answer : answers) {
            if (normalizedUser.equals(normalizeAnswer(answer.trim()))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 计算部分得分
     * 适用于一空多分的情况
     * 
     * @param userAnswer 用户答案
     * @param correctAnswer 正确答案
     * @param totalScore 该空总分
     * @return 实际得分
     */
    public static double calculatePartialScore(String userAnswer, String correctAnswer, double totalScore) {
        if (isCorrect(userAnswer, correctAnswer)) {
            return totalScore;
        }
        
        // 如果答案包含部分正确内容（如正确答案的一部分）
        String normalizedUser = normalizeAnswer(userAnswer);
        String normalizedCorrect = normalizeAnswer(correctAnswer);
        
        // 检查是否包含关键信息
        if (normalizedCorrect.length() > 2 && normalizedUser.length() >= normalizedCorrect.length() / 2) {
            if (normalizedCorrect.contains(normalizedUser) || normalizedUser.contains(normalizedCorrect.substring(0, normalizedCorrect.length() / 2))) {
                return totalScore * 0.5; // 给一半分数
            }
        }
        
        return 0;
    }

    /**
     * 标准化答案（去除多余空格、统一大小写等）
     * 
     * @param answer 原始答案
     * @return 标准化后的答案
     */
    private static String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        
        // 去除首尾空格
        answer = answer.trim();
        
        // 转换为小写（不区分大小写）
        answer = answer.toLowerCase();
        
        // 去除多余的空格（多个空格变一个）
        answer = answer.replaceAll("\\s+", " ");
        
        // 去除常见的标点符号（可选）
        // answer = answer.replaceAll("[,.!?;:]$", "");
        
        return answer;
    }

    /**
     * 检查答案相似度（用于智能评分）
     * 
     * @param userAnswer 用户答案
     * @param correctAnswer 正确答案
     * @return 相似度 0.0-1.0
     */
    public static double checkSimilarity(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return 0.0;
        }
        
        String normalizedUser = normalizeAnswer(userAnswer);
        String normalizedCorrect = normalizeAnswer(correctAnswer);
        
        if (normalizedUser.isEmpty() || normalizedCorrect.isEmpty()) {
            return 0.0;
        }
        
        // 完全匹配
        if (normalizedUser.equals(normalizedCorrect)) {
            return 1.0;
        }
        
        // 计算编辑距离（Levenshtein Distance）
        int distance = levenshteinDistance(normalizedUser, normalizedCorrect);
        int maxLen = Math.max(normalizedUser.length(), normalizedCorrect.length());
        
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 计算编辑距离（Levenshtein Distance）
     * 两个字符串之间，由一个转换成另一个所需的最少编辑操作次数
     */
    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        // 创建二维数组
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // 初始化边界
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // 动态规划计算
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,      // 删除
                        dp[i][j - 1] + 1),     // 插入
                        dp[i - 1][j - 1] + cost); // 替换
            }
        }
        
        return dp[len1][len2];
    }
}
