package com.rabbiter.oes.util;

import java.util.*;

/**
 * 主观题自动评分算法
 *
 * 评分维度（四维加权）：
 *   1. 关键词覆盖度 (35%) —— 参考答案中的关键词在学生答案中出现的比例
 *   2. 字符级 Bigram 相似度 (35%) —— Dice 系数，适合中文短文本
 *   3. 答案长度合理性 (15%) —— 过短严重扣分，过长轻微扣分
 *   4. 核心短语匹配 (15%) —— 3~5 字核心短语的命中率
 *
 * 最终分值经过 x^0.85 曲线压缩，使满分更难获得、区分度更高。
 */
public class SubjectiveGradingUtil {

    /* ──────────────── 权重配置 ──────────────── */
    private static final double W_KEYWORD = 0.35;
    private static final double W_BIGRAM  = 0.35;
    private static final double W_LENGTH  = 0.15;
    private static final double W_PHRASE  = 0.15;

    /** 评分曲线指数：< 1.0 则高分区间被压缩，使满分更难得 */
    private static final double CURVE_EXP = 0.85;

    /* ──────────────── 停用词表 ──────────────── */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都",
        "一", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
        "但是", "因为", "所以", "如果", "对于", "通过", "进行", "可以",
        "使用", "需要", "应该", "以及", "并且", "或者", "这个", "那个",
        "这", "那", "们", "一个", "没有", "看", "好", "自己"
    ));

    /* ═══════════════════════════════════════
       公共入口
       ═══════════════════════════════════════ */

    /**
     * 自动评分
     *
     * @param studentAnswer   学生答案
     * @param referenceAnswer 参考答案
     * @param maxScore        题目满分
     * @return 系统推荐分数 [0, maxScore]
     */
    public static int autoGrade(String studentAnswer, String referenceAnswer, int maxScore) {
        // ── 空答案 ──
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) return 0;

        // ── 无参考答案：默认给 50%，建议人工复核 ──
        if (referenceAnswer == null || referenceAnswer.trim().isEmpty()) {
            return maxScore / 2;
        }

        String stu = normalize(studentAnswer);
        String ref = normalize(referenceAnswer);

        if (stu.isEmpty()) return 0;

        // ── 完全匹配直接满分 ──
        if (stu.equals(ref)) return maxScore;

        // ── 四维评分 ──
        double kwScore     = calcKeywordScore(stu, ref);
        double bgScore     = calcBigramSimilarity(stu, ref);
        double lenScore    = calcLengthScore(stu, ref);
        double phraseScore = calcPhraseScore(stu, ref);

        double raw = W_KEYWORD * kwScore + W_BIGRAM * bgScore
                   + W_LENGTH  * lenScore + W_PHRASE * phraseScore;

        // ── 曲线压缩 ──
        double curved = applyCurve(raw);

        int result = (int) Math.round(curved * maxScore);
        return Math.max(0, Math.min(maxScore, result));
    }

    /**
     * 生成系统评语（可选调用）
     */
    public static String generateComment(String studentAnswer, String referenceAnswer,
                                         int maxScore, int actualScore) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return "【系统阅卷】学生未作答，得 0 分。";
        }
        double ratio = (double) actualScore / maxScore;
        if (ratio >= 0.90) {
            return "【系统阅卷】答案完整，核心知识点覆盖充分，表述清晰。（" + actualScore + "/" + maxScore + "分）";
        } else if (ratio >= 0.70) {
            return "【系统阅卷】答案基本正确，涵盖主要要点，部分细节有所欠缺。（" + actualScore + "/" + maxScore + "分）";
        } else if (ratio >= 0.50) {
            return "【系统阅卷】答案涉及部分要点，与参考答案仍有差距，建议复习相关内容。（" + actualScore + "/" + maxScore + "分）";
        } else if (ratio >= 0.20) {
            return "【系统阅卷】答案仅包含少量关键信息，得分较低。（" + actualScore + "/" + maxScore + "分）";
        } else {
            return "【系统阅卷】答案与参考答案相关性极低，请认真复习。（" + actualScore + "/" + maxScore + "分）";
        }
    }

    /**
     * 返回各维度原始得分（供前端展示打分明细）
     * key: keyword / bigram / length / phrase / total
     */
    public static Map<String, Double> scoreDetail(String studentAnswer,
                                                   String referenceAnswer, int maxScore) {
        Map<String, Double> detail = new LinkedHashMap<>();
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            detail.put("keyword", 0.0);
            detail.put("bigram",  0.0);
            detail.put("length",  0.0);
            detail.put("phrase",  0.0);
            detail.put("total",   0.0);
            return detail;
        }
        if (referenceAnswer == null || referenceAnswer.trim().isEmpty()) {
            detail.put("keyword", 0.5);
            detail.put("bigram",  0.5);
            detail.put("length",  0.5);
            detail.put("phrase",  0.5);
            detail.put("total",   0.5);
            return detail;
        }
        String stu = normalize(studentAnswer);
        String ref = normalize(referenceAnswer);

        double kw  = calcKeywordScore(stu, ref);
        double bg  = calcBigramSimilarity(stu, ref);
        double len = calcLengthScore(stu, ref);
        double ph  = calcPhraseScore(stu, ref);
        double raw = W_KEYWORD * kw + W_BIGRAM * bg + W_LENGTH * len + W_PHRASE * ph;
        double curved = applyCurve(raw);

        detail.put("keyword", round2(kw));
        detail.put("bigram",  round2(bg));
        detail.put("length",  round2(len));
        detail.put("phrase",  round2(ph));
        detail.put("total",   round2(curved));
        return detail;
    }

    /* ═══════════════════════════════════════
       私有算法实现
       ═══════════════════════════════════════ */

    /** 文本归一化 */
    private static String normalize(String text) {
        if (text == null) return "";
        return text.trim()
            .replaceAll("[\\s\u3000]+", " ")
            .replaceAll("[\u3001\u3002\uff01\uff1f\uff1b\uff1a\u300a\u300b\u3010\u3011\uff08\uff09\u2026\u2014\u201c\u201d\u2018\u2019]", " ")
            .replaceAll("[,\\.!?;:\"'<>\\[\\]()\\-_]", " ")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase();
    }

    /** 提取关键词：过滤停用词，保留 2 字以上词汇，并补充中文 2-4 字子串 */
    private static List<String> extractKeywords(String reference) {
        List<String> kws = new ArrayList<>();
        for (String token : reference.split("\\s+")) {
            if (token.length() >= 2 && !STOP_WORDS.contains(token)) {
                kws.add(token);
            }
        }
        // 补充提取连续中文字符的 2~4 字子串
        String chinese = reference.replaceAll("[^\\u4e00-\\u9fa5]", "");
        for (int len = 2; len <= 4; len++) {
            for (int i = 0; i <= chinese.length() - len; i++) {
                String sub = chinese.substring(i, i + len);
                if (!STOP_WORDS.contains(sub) && !kws.contains(sub)) {
                    kws.add(sub);
                }
            }
        }
        return kws;
    }

    /** 关键词覆盖度 */
    private static double calcKeywordScore(String stu, String ref) {
        List<String> kws = extractKeywords(ref);
        if (kws.isEmpty()) return 0.5;
        long matched = kws.stream().filter(stu::contains).count();
        return (double) matched / kws.size();
    }

    /** 字符级 Bigram 相似度（Dice 系数） */
    private static double calcBigramSimilarity(String s1, String s2) {
        Set<String> b1 = getBigrams(s1);
        Set<String> b2 = getBigrams(s2);
        if (b1.isEmpty() && b2.isEmpty()) return 1.0;
        if (b1.isEmpty() || b2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(b1);
        intersection.retainAll(b2);
        return 2.0 * intersection.size() / (b1.size() + b2.size());
    }

    private static Set<String> getBigrams(String text) {
        String t = text.replaceAll("\\s+", "");
        Set<String> bigrams = new LinkedHashSet<>();
        for (int i = 0; i < t.length() - 1; i++) {
            bigrams.add(t.substring(i, i + 2));
        }
        return bigrams;
    }

    /**
     * 答案长度合理性
     *  ratio in [0.4, 2.5] → 1.0（合理范围）
     *  ratio < 0.4         → ratio / 0.4（过短严重扣分）
     *  ratio > 2.5         → max(0.5, 2.5/ratio)（过长轻微扣分）
     */
    private static double calcLengthScore(String stu, String ref) {
        int sLen = stu.replaceAll("\\s+", "").length();
        int rLen = ref.replaceAll("\\s+", "").length();
        if (rLen == 0) return 0.5;
        double ratio = (double) sLen / rLen;
        if (ratio >= 0.4 && ratio <= 2.5) return 1.0;
        if (ratio < 0.4) return ratio / 0.4;
        return Math.max(0.5, 2.5 / ratio);
    }

    /** 核心短语（3~5 字）匹配率 */
    private static double calcPhraseScore(String stu, String ref) {
        String refCompact = ref.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
        List<String> phrases = new ArrayList<>();
        for (int len = 3; len <= 5; len++) {
            for (int i = 0; i <= refCompact.length() - len; i++) {
                String phrase = refCompact.substring(i, i + len);
                if (!STOP_WORDS.contains(phrase) && !phrases.contains(phrase)) {
                    phrases.add(phrase);
                }
            }
        }
        if (phrases.isEmpty()) return calcKeywordScore(stu, ref);
        String stuCompact = stu.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
        long matched = phrases.stream().filter(stuCompact::contains).count();
        return (double) matched / phrases.size();
    }

    /** 评分曲线：x^CURVE_EXP，压缩高分区间 */
    private static double applyCurve(double raw) {
        return Math.pow(Math.max(0, Math.min(1, raw)), CURVE_EXP);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
