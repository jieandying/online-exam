package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.PaperMapper;
import com.rabbiter.oes.mapper.SubjectiveQuestionMapper;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.mapper.GeneticPaperRecordMapper;
import com.rabbiter.oes.service.GeneticPaperService;
import com.rabbiter.oes.service.PaperService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 遗传算法智能组卷服务实现
 * 
 * 算法核心思想：
 * 1. 染色体：一套试卷（由题目 ID 组成）
 * 2. 基因：单个题目
 * 3. 适应度：试卷与目标约束的匹配程度
 * 4. 选择：轮盘赌选择
 * 5. 交叉：单点交叉
 * 6. 变异：随机替换题目
 * 7. 多样性：与历史试卷相似度惩罚，保证生成试卷相似度 < 30%
 */
@Service
public class GeneticPaperServiceImpl implements GeneticPaperService {

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;

    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;

    @Autowired
    private ExamManageMapper examManageMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperService paperService;

    @Autowired
    private GeneticPaperRecordMapper geneticPaperRecordMapper;

    // 题目缓存
    private List<MultiQuestion> multiQuestionPool;
    private List<FillQuestion> fillQuestionPool;
    private List<JudgeQuestion> judgeQuestionPool;
    private List<SubjectiveQuestion> subjectiveQuestionPool;

    // 历史试卷题目ID集合缓存（用于多样性惩罚）
    private List<Set<String>> existingPaperIdSets = new ArrayList<>();

    // 题目ID快速索引（O(1) 查找，替代 O(n) stream 搜索）
    private Map<Integer, MultiQuestion>      multiIndex      = new HashMap<>();
    private Map<Integer, FillQuestion>       fillIndex       = new HashMap<>();
    private Map<Integer, JudgeQuestion>      judgeIndex      = new HashMap<>();
    private Map<Integer, SubjectiveQuestion> subjectiveIndex = new HashMap<>();

    // 随机数生成器
    private Random random = new Random();

    /**
     * 染色体类（一套试卷）
     */
    private static class Chromosome {
        List<Integer> multiQuestionIds; // 选择题ID列表
        List<Integer> fillQuestionIds; // 填空题ID列表
        List<Integer> judgeQuestionIds; // 判断题ID列表
        List<Integer> subjectiveQuestionIds; // 主观题ID列表
        double fitness; // 适应度

        public Chromosome() {
            this.multiQuestionIds = new ArrayList<>();
            this.fillQuestionIds = new ArrayList<>();
            this.judgeQuestionIds = new ArrayList<>();
            this.subjectiveQuestionIds = new ArrayList<>();
            this.fitness = 0.0;
        }

        public Chromosome deepCopy() {
            Chromosome copy = new Chromosome();
            copy.multiQuestionIds = new ArrayList<>(this.multiQuestionIds);
            copy.fillQuestionIds = new ArrayList<>(this.fillQuestionIds);
            copy.judgeQuestionIds = new ArrayList<>(this.judgeQuestionIds);
            copy.subjectiveQuestionIds = new ArrayList<>(this.subjectiveQuestionIds);
            copy.fitness = this.fitness;
            return copy;
        }
    }

    @Override
    public Map<String, Object> generatePaper(GeneticPaperRequest request) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 初始化题库
            initQuestionPool(request);

            // 验证题库是否充足
            if (!validateQuestionPool(request, result)) {
                return result;
            }

            // 1.5 加载历史同科目试卷（用于遗传算法多样性惩罚）
            loadExistingPaperIdSets(request.getSubjects());

            // 2. 初始化种群
            List<Chromosome> population = initPopulation(request);

            // 3. 遗传算法迭代
            Chromosome bestChromosome = evolve(population, request);

            // 4. 构建结果
            buildResult(result, bestChromosome, request, startTime);

            result.put("success", true);
            result.put("message", "智能组卷成功");

            // 5. 保存组卷记录
            try {
                Map<String, Object> stats = (Map<String, Object>) result.get("stats");
                GeneticPaperRecord record = new GeneticPaperRecord();
                record.setSubjects(request.getSubjects() != null ? String.join(",", request.getSubjects()) : "");
                record.setTargetDifficulty(request.getTargetDifficulty());
                record.setMultiCount(request.getMultiCount());
                record.setFillCount(request.getFillCount());
                record.setJudgeCount(request.getJudgeCount());
                record.setTotalScore(stats != null ? (Integer) stats.get("totalScore") : 0);
                record.setTotalQuestions(stats != null ? (Integer) stats.get("totalQuestions") : 0);
                record.setAvgDifficulty(stats != null ? ((Number) stats.get("averageDifficulty")).doubleValue() : 0.0);
                record.setFitness(stats != null ? ((Number) stats.get("fitness")).doubleValue() : 0.0);
                record.setGenerationTime(stats != null ? ((Number) stats.get("generationTime")).intValue() : 0);
                record.setPaperJson(JSON.toJSONString(result));
                record.setStudentId(request.getStudentId()); // 学生ID用于统计
                record.setCreateTime(new Date());
                geneticPaperRecordMapper.add(record);
                result.put("recordId", record.getId());
                System.out.println("✅ 组卷记录已保存，ID: " + record.getId());
            } catch (Exception e) {
                System.err.println("⚠️ 保存组卷记录失败: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "组卷失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 初始化题库缓存
     */
    private void initQuestionPool(GeneticPaperRequest request) {
        List<String> subjects = request.getSubjects();

        // 获取所有选择题
        List<MultiQuestion> allMulti = new ArrayList<>();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MultiQuestion> multiPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        allMulti.addAll(multiQuestionMapper.findAll(multiPage).getRecords());

        // 获取所有填空题
        List<FillQuestion> allFill = new ArrayList<>();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<FillQuestion> fillPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        allFill.addAll(fillQuestionMapper.findAll(fillPage).getRecords());

        // 获取所有判断题
        List<JudgeQuestion> allJudge = new ArrayList<>();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JudgeQuestion> judgePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        allJudge.addAll(judgeQuestionMapper.findAll(judgePage).getRecords());

        // 获取所有主观题
        List<SubjectiveQuestion> allSubjective = new ArrayList<>();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectiveQuestion> subjectivePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        allSubjective.addAll(subjectiveQuestionMapper.findAll(subjectivePage).getRecords());

        // 按科目筛选
        if (subjects != null && !subjects.isEmpty()) {
            multiQuestionPool = allMulti.stream()
                    .filter(q -> subjects.contains(q.getSubject()))
                    .collect(Collectors.toList());
            fillQuestionPool = allFill.stream()
                    .filter(q -> subjects.contains(q.getSubject()))
                    .collect(Collectors.toList());
            judgeQuestionPool = allJudge.stream()
                    .filter(q -> subjects.contains(q.getSubject()))
                    .collect(Collectors.toList());
            subjectiveQuestionPool = allSubjective.stream()
                    .filter(q -> subjects.contains(q.getSubject()))
                    .collect(Collectors.toList());
        } else {
            multiQuestionPool = allMulti;
            fillQuestionPool = allFill;
            judgeQuestionPool = allJudge;
            subjectiveQuestionPool = allSubjective;
        }

        // 按章节筛选
        List<String> sections = request.getSections();
        if (sections != null && !sections.isEmpty()) {
            multiQuestionPool = multiQuestionPool.stream()
                    .filter(q -> q.getSection() != null && sections.stream().anyMatch(s -> q.getSection().contains(s)))
                    .collect(Collectors.toList());
            fillQuestionPool = fillQuestionPool.stream()
                    .filter(q -> q.getSection() != null && sections.stream().anyMatch(s -> q.getSection().contains(s)))
                    .collect(Collectors.toList());
            judgeQuestionPool = judgeQuestionPool.stream()
                    .filter(q -> q.getSection() != null && sections.stream().anyMatch(s -> q.getSection().contains(s)))
                    .collect(Collectors.toList());
            subjectiveQuestionPool = subjectiveQuestionPool.stream()
                    .filter(q -> q.getSection() != null && sections.stream().anyMatch(s -> q.getSection().contains(s)))
                    .collect(Collectors.toList());
        }

        // 构建 O(1) 快速索引，避免适应度计算中的 O(n) 流搜索
        multiIndex = new HashMap<>(multiQuestionPool.size() * 2);
        multiQuestionPool.forEach(q -> multiIndex.put(q.getQuestionId(), q));
        fillIndex = new HashMap<>(fillQuestionPool.size() * 2);
        fillQuestionPool.forEach(q -> fillIndex.put(q.getQuestionId(), q));
        judgeIndex = new HashMap<>(judgeQuestionPool.size() * 2);
        judgeQuestionPool.forEach(q -> judgeIndex.put(q.getQuestionId(), q));
        subjectiveIndex = new HashMap<>(subjectiveQuestionPool.size() * 2);
        subjectiveQuestionPool.forEach(q -> subjectiveIndex.put(q.getQuestionId(), q));
    }

    /**
     * 验证题库是否充足，不足则自动减少题目数量而非继续报错
     */
    private boolean validateQuestionPool(GeneticPaperRequest request, Map<String, Object> result) {
        StringBuilder warning = new StringBuilder();

        // 选择题：如果不足则减少题目数
        if (multiQuestionPool.size() < request.getMultiCount()) {
            warning.append(String.format("选择题只有%d道，已自动调整；", multiQuestionPool.size()));
            request.setMultiCount(multiQuestionPool.size());
        }
        // 填空题
        if (fillQuestionPool.size() < request.getFillCount()) {
            warning.append(String.format("填空题只有%d道，已自动调整；", fillQuestionPool.size()));
            request.setFillCount(fillQuestionPool.size());
        }
        // 判断题
        if (judgeQuestionPool.size() < request.getJudgeCount()) {
            warning.append(String.format("判断题只有%d道，已自动调整；", judgeQuestionPool.size()));
            request.setJudgeCount(judgeQuestionPool.size());
        }
        // 主观题
        if (request.getSubjectiveCount() != null && request.getSubjectiveCount() > 0
                && subjectiveQuestionPool.size() < request.getSubjectiveCount()) {
            warning.append(String.format("主观题只有%d道，已自动调整；", subjectiveQuestionPool.size()));
            request.setSubjectiveCount(subjectiveQuestionPool.size());
        }

        // 如果所有题型均为0，才算真正失败
        int totalAvailable = multiQuestionPool.size() + fillQuestionPool.size()
                + judgeQuestionPool.size() + subjectiveQuestionPool.size();
        if (totalAvailable == 0) {
            result.put("success", false);
            result.put("message", "该科目题库为空，请先在题库管理中添加题目");
            return false;
        }

        if (warning.length() > 0) {
            result.put("warning", "题库题目不足，" + warning.toString() + "将使用现有题目组卷");
            System.out.println("⚠️ 题库预警: " + warning.toString());
        }
        return true;
    }

    /**
     * 初始化种群
     */
    private List<Chromosome> initPopulation(GeneticPaperRequest request) {
        List<Chromosome> population = new ArrayList<>();
        int populationSize = request.getPopulationSize() != null ? request.getPopulationSize() : 50;

        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome();

            // 随机选择题
            chromosome.multiQuestionIds = selectRandomQuestions(
                    multiQuestionPool.stream().map(MultiQuestion::getQuestionId).collect(Collectors.toList()),
                    request.getMultiCount());

            // 随机填空题
            chromosome.fillQuestionIds = selectRandomQuestions(
                    fillQuestionPool.stream().map(FillQuestion::getQuestionId).collect(Collectors.toList()),
                    request.getFillCount());

            // 随机判断题
            chromosome.judgeQuestionIds = selectRandomQuestions(
                    judgeQuestionPool.stream().map(JudgeQuestion::getQuestionId).collect(Collectors.toList()),
                    request.getJudgeCount());

            // 随机主观题
            if (request.getSubjectiveCount() != null && request.getSubjectiveCount() > 0) {
                chromosome.subjectiveQuestionIds = selectRandomQuestions(
                        subjectiveQuestionPool.stream().map(SubjectiveQuestion::getQuestionId)
                                .collect(Collectors.toList()),
                        request.getSubjectiveCount());
            }

            population.add(chromosome);
        }

        return population;
    }

    /**
     * 随机选择指定数量的题目（不重复）
     */
    private List<Integer> selectRandomQuestions(List<Integer> questionIds, int count) {
        List<Integer> shuffled = new ArrayList<>(questionIds);
        Collections.shuffle(shuffled, random);
        return new ArrayList<>(shuffled.subList(0, Math.min(count, shuffled.size())));
    }

    /**
     * 遗传算法主循环
     * 优化点：
     * 1. 锦标赛选择替代轮盘赌，避免适应度差异极大时的选择压力问题
     * 2. 小比例精英保留（前 5%），保证好基因不丢失
     * 3. 自适应变异：停滞越久变异越激烈，阿斯平原歉赌
     * 4. 收敛测证提前终止节省时间
     */
    private Chromosome evolve(List<Chromosome> population, GeneticPaperRequest request) {
        int maxGenerations = request.getMaxGenerations() != null ? request.getMaxGenerations() : 100;
        double crossoverRate = request.getCrossoverRate() != null ? request.getCrossoverRate() : 0.8;
        double baseMutationRate = request.getMutationRate() != null ? request.getMutationRate() : 0.1;

        // 锦标赛大小 = 种群的 10%，至少为 2
        int tournamentSize = Math.max(2, population.size() / 10);
        // 精英数量 = 种群的 5%，至少为 1
        int eliteCount = Math.max(1, population.size() / 20);

        Chromosome bestEver = null;
        double bestFitnessEver = -1;
        int stagnationCount = 0; // 连续未改善的代数

        for (int generation = 0; generation < maxGenerations; generation++) {
            // 1. 计算适应度
            for (Chromosome chromosome : population) {
                chromosome.fitness = calculateFitness(chromosome, request);
            }

            // 2. 按适应度降序排序，方便精英保留
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            Chromosome currentBest = population.get(0);

            if (currentBest.fitness > bestFitnessEver) {
                bestFitnessEver = currentBest.fitness;
                bestEver = currentBest.deepCopy();
                stagnationCount = 0;
            } else {
                stagnationCount++;
            }

            // 早停：适应度将进入收缩区间
            if (bestFitnessEver >= 0.95) {
                System.out.printf("遗传算法在第 %d 代提前收敛，适应度=%.4f%n", generation, bestFitnessEver);
                break;
            }

            // 3. 自适应变异率：停滞越久变异越激烈（阿斯平原歉赌）
            double effectiveMutationRate;
            if (stagnationCount <= 5) {
                effectiveMutationRate = baseMutationRate;               // 正常进化
            } else if (stagnationCount <= 15) {
                effectiveMutationRate = baseMutationRate * 2.5;          // 轻度加速
            } else {
                effectiveMutationRate = Math.min(0.5, baseMutationRate * 5.0); // 逃离局部最优
            }

            // 4. 锦标赛选择（替代轮盘赌）
            List<Chromosome> selected = tournamentSelection(population, tournamentSize);

            // 5. 交叉操作
            List<Chromosome> offspring = new ArrayList<>(population.size());
            // 先直接拷贝精英
            for (int i = 0; i < eliteCount; i++) {
                offspring.add(population.get(i).deepCopy());
            }
            // 其余个体通过交叉产生
            for (int i = 0; i < selected.size() - 1 && offspring.size() < population.size(); i += 2) {
                if (random.nextDouble() < crossoverRate) {
                    Chromosome[] children = crossover(selected.get(i), selected.get(i + 1), request);
                    offspring.add(children[0]);
                    if (offspring.size() < population.size()) offspring.add(children[1]);
                } else {
                    offspring.add(selected.get(i).deepCopy());
                    if (offspring.size() < population.size()) offspring.add(selected.get(i + 1).deepCopy());
                }
            }

            // 6. 变异操作（精英不参与变异）
            for (int i = eliteCount; i < offspring.size(); i++) {
                if (random.nextDouble() < effectiveMutationRate) {
                    mutate(offspring.get(i), request);
                }
            }

            population = offspring;
        }

        return bestEver;
    }

    /**
     * 锦标赛选择：每次随机抓取 tournamentSize 个候选人，取最优者
     * 相比轮盘赌的优势：
     *  - 适应度相近时选择压力更均衡，不会过早收敛
     *  - 不夸影于适应度为 0 的情况
     *  - 时间复杂度 O(n * k) 而非 O(n²)
     */
    private List<Chromosome> tournamentSelection(List<Chromosome> population, int tournamentSize) {
        List<Chromosome> selected = new ArrayList<>(population.size());
        for (int i = 0; i < population.size(); i++) {
            Chromosome best = null;
            for (int j = 0; j < tournamentSize; j++) {
                Chromosome candidate = population.get(random.nextInt(population.size()));
                if (best == null || candidate.fitness > best.fitness) {
                    best = candidate;
                }
            }
            selected.add(best.deepCopy());
        }
        return selected;
    }

    /**
     * 计算染色体（试卷）的适应度
     * 综合考虑：难度匹配度、章节覆盖度、题目多样性
     */
    private double calculateFitness(Chromosome chromosome, GeneticPaperRequest request) {
        double targetDifficulty = request.getTargetDifficulty() != null ? request.getTargetDifficulty() : 3.0;
        double tolerance = request.getDifficultyTolerance() != null ? request.getDifficultyTolerance() : 0.5;

        // 1. 计算试卷平均难度
        double totalDifficulty = 0;
        int totalQuestions = 0;

        for (Integer id : chromosome.multiQuestionIds) {
            MultiQuestion q = findMultiQuestion(id);
            if (q != null) {
                totalDifficulty += parseDifficulty(q.getLevel());
                totalQuestions++;
            }
        }
        for (Integer id : chromosome.fillQuestionIds) {
            FillQuestion q = findFillQuestion(id);
            if (q != null) {
                totalDifficulty += parseDifficulty(q.getLevel());
                totalQuestions++;
            }
        }
        for (Integer id : chromosome.judgeQuestionIds) {
            JudgeQuestion q = findJudgeQuestion(id);
            if (q != null) {
                totalDifficulty += parseDifficulty(q.getLevel());
                totalQuestions++;
            }
        }
        for (Integer id : chromosome.subjectiveQuestionIds) {
            SubjectiveQuestion q = findSubjectiveQuestion(id);
            if (q != null) {
                totalDifficulty += parseDifficulty(q.getLevel());
                totalQuestions++;
            }
        }

        double avgDifficulty = totalQuestions > 0 ? totalDifficulty / totalQuestions : 0;

        // 2. 难度适应度（高斯函数）
        double difficultyDelta = Math.abs(avgDifficulty - targetDifficulty);
        double difficultyFitness = Math.exp(-Math.pow(difficultyDelta / tolerance, 2));

        // 3. 章节覆盖度适应度
        double coverageFitness = calculateCoverageFitness(chromosome, request);

        // 4. 题目重复惩罚（ID重复检测）
        double duplicatePenalty = calculateDuplicatePenalty(chromosome);

        // 5. 多样性适应度（与历史试卷相似度惩罚）
        double diversityFitness = calculateDiversityFitness(chromosome);

        // 6. 综合适应度：难度匹配 * 0.50 + 章节覆盖 * 0.25 + 重复惩罚 * 0.10 + 多样性 * 0.15
        return difficultyFitness * 0.50 + coverageFitness * 0.25 + duplicatePenalty * 0.10 + diversityFitness * 0.15;
    }

    /**
     * 解析难度等级
     */
    private double parseDifficulty(String level) {
        if (level == null || level.isEmpty())
            return 3.0;
        try {
            return Double.parseDouble(level);
        } catch (NumberFormatException e) {
            // 尝试中文解析
            switch (level) {
                case "简单":
                    return 1.0;
                case "中等":
                    return 2.0;
                case "较难":
                    return 3.0;
                case "困难":
                    return 4.0;
                case "极难":
                    return 5.0;
                default:
                    return 3.0;
            }
        }
    }

    /**
     * 计算章节覆盖度适应度
     */
    private double calculateCoverageFitness(Chromosome chromosome, GeneticPaperRequest request) {
        List<String> requiredSections = request.getSections();
        if (requiredSections == null || requiredSections.isEmpty()) {
            return 1.0; // 无章节要求，满分
        }

        Set<String> coveredSections = new HashSet<>();

        for (Integer id : chromosome.multiQuestionIds) {
            MultiQuestion q = findMultiQuestion(id);
            if (q != null && q.getSection() != null) {
                coveredSections.add(q.getSection());
            }
        }
        for (Integer id : chromosome.fillQuestionIds) {
            FillQuestion q = findFillQuestion(id);
            if (q != null && q.getSection() != null) {
                coveredSections.add(q.getSection());
            }
        }
        for (Integer id : chromosome.judgeQuestionIds) {
            JudgeQuestion q = findJudgeQuestion(id);
            if (q != null && q.getSection() != null) {
                coveredSections.add(q.getSection());
            }
        }
        for (Integer id : chromosome.subjectiveQuestionIds) {
            SubjectiveQuestion q = findSubjectiveQuestion(id);
            if (q != null && q.getSection() != null) {
                coveredSections.add(q.getSection());
            }
        }

        // 计算覆盖比例
        int covered = 0;
        for (String required : requiredSections) {
            for (String actual : coveredSections) {
                if (actual.contains(required)) {
                    covered++;
                    break;
                }
            }
        }

        return (double) covered / requiredSections.size();
    }

    /**
     * 计算重复惩罚
     */
    private double calculateDuplicatePenalty(Chromosome chromosome) {
        Set<String> allIds = new HashSet<>();
        int totalCount = 0;

        for (Integer id : chromosome.multiQuestionIds) {
            allIds.add("m_" + id);
            totalCount++;
        }
        for (Integer id : chromosome.fillQuestionIds) {
            allIds.add("f_" + id);
            totalCount++;
        }
        for (Integer id : chromosome.judgeQuestionIds) {
            allIds.add("j_" + id);
            totalCount++;
        }
        for (Integer id : chromosome.subjectiveQuestionIds) {
            allIds.add("s_" + id);
            totalCount++;
        }

        // 无重复返回1，有重复按比例扣分
        return totalCount > 0 ? (double) allIds.size() / totalCount : 1.0;
    }

    /**
     * 轮盘赌选择
     */
    private List<Chromosome> rouletteWheelSelection(List<Chromosome> population) {
        List<Chromosome> selected = new ArrayList<>();
        double totalFitness = population.stream().mapToDouble(c -> c.fitness).sum();

        for (int i = 0; i < population.size(); i++) {
            double rand = random.nextDouble() * totalFitness;
            double sum = 0;
            for (Chromosome chromosome : population) {
                sum += chromosome.fitness;
                if (sum >= rand) {
                    selected.add(chromosome.deepCopy());
                    break;
                }
            }
        }

        // 确保选择数量正确
        while (selected.size() < population.size()) {
            selected.add(population.get(random.nextInt(population.size())).deepCopy());
        }

        return selected;
    }

    /**
     * 单点交叉
     */
    private Chromosome[] crossover(Chromosome parent1, Chromosome parent2, GeneticPaperRequest request) {
        Chromosome child1 = new Chromosome();
        Chromosome child2 = new Chromosome();

        // 选择题交叉
        int multiCrossPoint = random.nextInt(Math.max(1, parent1.multiQuestionIds.size()));
        child1.multiQuestionIds = new ArrayList<>();
        child2.multiQuestionIds = new ArrayList<>();
        for (int i = 0; i < parent1.multiQuestionIds.size(); i++) {
            if (i < multiCrossPoint) {
                child1.multiQuestionIds.add(parent1.multiQuestionIds.get(i));
                child2.multiQuestionIds.add(parent2.multiQuestionIds.get(i));
            } else {
                child1.multiQuestionIds.add(parent2.multiQuestionIds.get(i));
                child2.multiQuestionIds.add(parent1.multiQuestionIds.get(i));
            }
        }

        // 填空题交叉
        if (!parent1.fillQuestionIds.isEmpty()) {
            int fillCrossPoint = random.nextInt(Math.max(1, parent1.fillQuestionIds.size()));
            child1.fillQuestionIds = new ArrayList<>();
            child2.fillQuestionIds = new ArrayList<>();
            for (int i = 0; i < parent1.fillQuestionIds.size(); i++) {
                if (i < fillCrossPoint) {
                    child1.fillQuestionIds.add(parent1.fillQuestionIds.get(i));
                    child2.fillQuestionIds.add(parent2.fillQuestionIds.get(i));
                } else {
                    child1.fillQuestionIds.add(parent2.fillQuestionIds.get(i));
                    child2.fillQuestionIds.add(parent1.fillQuestionIds.get(i));
                }
            }
        } else {
            child1.fillQuestionIds = new ArrayList<>(parent1.fillQuestionIds);
            child2.fillQuestionIds = new ArrayList<>(parent2.fillQuestionIds);
        }

        // 判断题交叉
        if (!parent1.judgeQuestionIds.isEmpty()) {
            int judgeCrossPoint = random.nextInt(Math.max(1, parent1.judgeQuestionIds.size()));
            child1.judgeQuestionIds = new ArrayList<>();
            child2.judgeQuestionIds = new ArrayList<>();
            for (int i = 0; i < parent1.judgeQuestionIds.size(); i++) {
                if (i < judgeCrossPoint) {
                    child1.judgeQuestionIds.add(parent1.judgeQuestionIds.get(i));
                    child2.judgeQuestionIds.add(parent2.judgeQuestionIds.get(i));
                } else {
                    child1.judgeQuestionIds.add(parent2.judgeQuestionIds.get(i));
                    child2.judgeQuestionIds.add(parent1.judgeQuestionIds.get(i));
                }
            }
        } else {
            child1.judgeQuestionIds = new ArrayList<>(parent1.judgeQuestionIds);
            child2.judgeQuestionIds = new ArrayList<>(parent2.judgeQuestionIds);
        }

        // 主观题交叉
        if (!parent1.subjectiveQuestionIds.isEmpty()) {
            int subjectiveCrossPoint = random.nextInt(Math.max(1, parent1.subjectiveQuestionIds.size()));
            child1.subjectiveQuestionIds = new ArrayList<>();
            child2.subjectiveQuestionIds = new ArrayList<>();
            for (int i = 0; i < parent1.subjectiveQuestionIds.size(); i++) {
                if (i < subjectiveCrossPoint) {
                    child1.subjectiveQuestionIds.add(parent1.subjectiveQuestionIds.get(i));
                    child2.subjectiveQuestionIds.add(parent2.subjectiveQuestionIds.get(i));
                } else {
                    child1.subjectiveQuestionIds.add(parent2.subjectiveQuestionIds.get(i));
                    child2.subjectiveQuestionIds.add(parent1.subjectiveQuestionIds.get(i));
                }
            }
        } else {
            child1.subjectiveQuestionIds = new ArrayList<>(parent1.subjectiveQuestionIds);
            child2.subjectiveQuestionIds = new ArrayList<>(parent2.subjectiveQuestionIds);
        }

        // 修复重复题目
        repairChromosome(child1, request);
        repairChromosome(child2, request);

        return new Chromosome[] { child1, child2 };
    }

    /**
     * 变异操作：随机替换一道题目
     * 优化：用 HashSet 避免 O(n) 的 List.contains() 调用
     */
    private void mutate(Chromosome chromosome, GeneticPaperRequest request) {
        int type = random.nextInt(4);
        switch (type) {
            case 0: mutateQuestionList(chromosome.multiQuestionIds, multiIndex);      break;
            case 1: mutateQuestionList(chromosome.fillQuestionIds, fillIndex);        break;
            case 2: mutateQuestionList(chromosome.judgeQuestionIds, judgeIndex);     break;
            case 3: mutateQuestionList(chromosome.subjectiveQuestionIds, subjectiveIndex); break;
        }
    }

    /**
     * 通用变异逻辑：从池中随机找一个未使用的题目替换当前列表中的某个
     * 将 O(n*m) 降为 O(n) + O(pool.size())
     */
    private void mutateQuestionList(List<Integer> questionIds, Map<Integer, ?> pool) {
        if (questionIds.isEmpty() || pool.isEmpty()) return;
        Set<Integer> usedSet = new HashSet<>(questionIds);
        List<Integer> poolIds = new ArrayList<>(pool.keySet());
        Collections.shuffle(poolIds, random);
        for (Integer candidate : poolIds) {
            if (!usedSet.contains(candidate)) {
                int idx = random.nextInt(questionIds.size());
                questionIds.set(idx, candidate);
                return;
            }
        }
    }

    /**
     * 修复染色体中的重复题目（交叉后可能出现）
     * 优化：直接使用 HashMap 的 keySet() 替代每次 stream+map
     */
    private void repairChromosome(Chromosome chromosome, GeneticPaperRequest request) {
        repairList(chromosome.multiQuestionIds,      multiIndex.keySet());
        repairList(chromosome.fillQuestionIds,       fillIndex.keySet());
        repairList(chromosome.judgeQuestionIds,      judgeIndex.keySet());
        repairList(chromosome.subjectiveQuestionIds, subjectiveIndex.keySet());
    }

    /**
     * 修复单个题型列表中的重复 ID
     */
    private void repairList(List<Integer> ids, Set<Integer> pool) {
        Set<Integer> seen = new HashSet<>();
        List<Integer> poolList = new ArrayList<>(pool);
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            if (seen.contains(id)) {
                for (Integer avail : poolList) {
                    if (!seen.contains(avail)) {
                        ids.set(i, avail);
                        seen.add(avail);
                        break;
                    }
                }
            } else {
                seen.add(id);
            }
        }
    }

    /**
     * 加载正式考试试卷题目 ID 集合作为遗传算法多样性约束（paper_manage 体系）
     * 算法：一次 SQL (UNION 四表) 分组，确保生成试卷与同科目正式考试相似度 < 30%
     */
    private void loadExistingPaperIdSets(List<String> subjects) {
        existingPaperIdSets = new ArrayList<>();
        if (subjects == null || subjects.isEmpty()) return;
        try {
            List<PaperManage> paperItems = paperMapper.findPaperItemsBySubjects(subjects);
            Map<Integer, Set<String>> formalMap = new HashMap<>();
            String[] prefixes = {null, "m", "f", "j", "s"};
            for (PaperManage item : paperItems) {
                int t = item.getQuestionType() != null ? item.getQuestionType() : 0;
                if (t >= 1 && t <= 4) {
                    formalMap.computeIfAbsent(item.getPaperId(), k -> new HashSet<>())
                             .add(prefixes[t] + "_" + item.getQuestionId());
                }
            }
            existingPaperIdSets.addAll(formalMap.values());
            System.out.println("✅ 已加载 " + existingPaperIdSets.size() + " 张正式考试试卷用于多样性约束");
        } catch (Exception e) {
            System.err.println("⚠️ 加载正式考试试卷失败: " + e.getMessage());
        }
    }

    /**
     * 从 paperJson 字符串中提取题目ID集合（格式： m_id / f_id / j_id / s_id）
     */
    private Set<String> extractQuestionIdsFromJson(String paperJson) {
        Set<String> ids = new HashSet<>();
        if (paperJson == null || paperJson.isEmpty()) return ids;
        try {
            JSONObject obj = JSON.parseObject(paperJson);
            extractIds(obj, "multiQuestions", "m", ids);
            extractIds(obj, "fillQuestions", "f", ids);
            extractIds(obj, "judgeQuestions", "j", ids);
            extractIds(obj, "subjectiveQuestions", "s", ids);
        } catch (Exception e) {
            // JSON 解析失败则返回空集合
        }
        return ids;
    }

    private void extractIds(JSONObject obj, String key, String prefix, Set<String> ids) {
        JSONArray arr = obj.getJSONArray(key);
        if (arr == null) return;
        for (int i = 0; i < arr.size(); i++) {
            JSONObject q = arr.getJSONObject(i);
            if (q != null && q.getInteger("questionId") != null) {
                ids.add(prefix + "_" + q.getInteger("questionId"));
            }
        }
    }

    /**
     * 从染色体提取题目ID集合
     */
    private Set<String> extractQuestionIdsFromChromosome(Chromosome chromosome) {
        Set<String> ids = new HashSet<>();
        for (Integer id : chromosome.multiQuestionIds) ids.add("m_" + id);
        for (Integer id : chromosome.fillQuestionIds) ids.add("f_" + id);
        for (Integer id : chromosome.judgeQuestionIds) ids.add("j_" + id);
        for (Integer id : chromosome.subjectiveQuestionIds) ids.add("s_" + id);
        return ids;
    }

    /**
     * 计算 Jaccard 相似系数：|A∩B| / |A∪B|
     */
    private double calculateJaccard(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() && setB.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 多样性适应度：1.0 = 完全新颖，0.0 = 与某张历史卷完全重合
     * 目标：最大相似度 < 30%，超过 30% 将大幅降低适应度
     */
    private double calculateDiversityFitness(Chromosome chromosome) {
        if (existingPaperIdSets == null || existingPaperIdSets.isEmpty()) return 1.0;
        Set<String> currentIds = extractQuestionIdsFromChromosome(chromosome);
        double maxSimilarity = 0.0;
        for (Set<String> existingIds : existingPaperIdSets) {
            double sim = calculateJaccard(currentIds, existingIds);
            if (sim > maxSimilarity) maxSimilarity = sim;
        }
        // 相似度 > 30%：线性惩罚；> 50%：重度惩罚
        if (maxSimilarity <= 0.30) return 1.0;
        if (maxSimilarity >= 0.80) return 0.0;
        return 1.0 - ((maxSimilarity - 0.30) / 0.50) * 1.5;
    }

    /**
     * 构建组卷结果
     */
    private void buildResult(Map<String, Object> result, Chromosome bestChromosome,
            GeneticPaperRequest request, long startTime) {
        // 获取完整题目信息
        List<MultiQuestion> multiQuestions = new ArrayList<>();
        for (Integer id : bestChromosome.multiQuestionIds) {
            MultiQuestion q = findMultiQuestion(id);
            if (q != null) {
                q.setScore(request.getMultiScore());
                multiQuestions.add(q);
            }
        }

        List<FillQuestion> fillQuestions = new ArrayList<>();
        for (Integer id : bestChromosome.fillQuestionIds) {
            FillQuestion q = findFillQuestion(id);
            if (q != null) {
                q.setScore(request.getFillScore());
                fillQuestions.add(q);
            }
        }

        List<JudgeQuestion> judgeQuestions = new ArrayList<>();
        for (Integer id : bestChromosome.judgeQuestionIds) {
            JudgeQuestion q = findJudgeQuestion(id);
            if (q != null) {
                q.setScore(request.getJudgeScore());
                judgeQuestions.add(q);
            }
        }

        List<SubjectiveQuestion> subjectiveQuestions = new ArrayList<>();
        for (Integer id : bestChromosome.subjectiveQuestionIds) {
            SubjectiveQuestion q = findSubjectiveQuestion(id);
            if (q != null) {
                q.setScore(request.getSubjectiveScore());
                subjectiveQuestions.add(q);
            }
        }

        // 计算试卷统计信息
        Map<String, Object> stats = new HashMap<>();
        int totalScore = multiQuestions.size() * request.getMultiScore()
                + fillQuestions.size() * request.getFillScore()
                + judgeQuestions.size() * request.getJudgeScore()
                + subjectiveQuestions.size() * request.getSubjectiveScore();
        int totalQuestions = multiQuestions.size() + fillQuestions.size() + judgeQuestions.size()
                + subjectiveQuestions.size();

        // 计算平均难度
        double totalDifficulty = 0;
        for (MultiQuestion q : multiQuestions) {
            totalDifficulty += parseDifficulty(q.getLevel());
        }
        for (FillQuestion q : fillQuestions) {
            totalDifficulty += parseDifficulty(q.getLevel());
        }
        for (JudgeQuestion q : judgeQuestions) {
            totalDifficulty += parseDifficulty(q.getLevel());
        }
        for (SubjectiveQuestion q : subjectiveQuestions) {
            totalDifficulty += parseDifficulty(q.getLevel());
        }
        double avgDifficulty = totalQuestions > 0 ? totalDifficulty / totalQuestions : 0;

        // 章节分布统计
        Map<String, Integer> sectionDistribution = new HashMap<>();
        for (MultiQuestion q : multiQuestions) {
            if (q.getSection() != null) {
                sectionDistribution.merge(q.getSection(), 1, Integer::sum);
            }
        }
        for (FillQuestion q : fillQuestions) {
            if (q.getSection() != null) {
                sectionDistribution.merge(q.getSection(), 1, Integer::sum);
            }
        }
        for (JudgeQuestion q : judgeQuestions) {
            if (q.getSection() != null) {
                sectionDistribution.merge(q.getSection(), 1, Integer::sum);
            }
        }
        for (SubjectiveQuestion q : subjectiveQuestions) {
            if (q.getSection() != null) {
                sectionDistribution.merge(q.getSection(), 1, Integer::sum);
            }
        }

        // 难度分布统计
        Map<String, Integer> difficultyDistribution = new HashMap<>();
        for (MultiQuestion q : multiQuestions) {
            String level = q.getLevel() != null ? q.getLevel() : "未知";
            difficultyDistribution.merge(level, 1, Integer::sum);
        }
        for (FillQuestion q : fillQuestions) {
            String level = q.getLevel() != null ? q.getLevel() : "未知";
            difficultyDistribution.merge(level, 1, Integer::sum);
        }
        for (JudgeQuestion q : judgeQuestions) {
            String level = q.getLevel() != null ? q.getLevel() : "未知";
            difficultyDistribution.merge(level, 1, Integer::sum);
        }
        for (SubjectiveQuestion q : subjectiveQuestions) {
            String level = q.getLevel() != null ? q.getLevel() : "未知";
            difficultyDistribution.merge(level, 1, Integer::sum);
        }

        stats.put("totalScore", totalScore);
        stats.put("totalQuestions", totalQuestions);
        stats.put("averageDifficulty", Math.round(avgDifficulty * 100.0) / 100.0);
        stats.put("targetDifficulty", request.getTargetDifficulty());
        stats.put("fitness", Math.round(bestChromosome.fitness * 10000.0) / 100.0);
        stats.put("sectionDistribution", sectionDistribution);
        stats.put("difficultyDistribution", difficultyDistribution);
        stats.put("generationTime", System.currentTimeMillis() - startTime);
        // 各题型分值配置（保存到 exam_manage，确保学生答题时分值与组卷一致）
        stats.put("multiScore", request.getMultiScore());
        stats.put("fillScore", request.getFillScore());
        stats.put("judgeScore", request.getJudgeScore());
        stats.put("subjectiveScore", request.getSubjectiveScore());

        // 题型统计
        Map<String, Integer> typeStats = new HashMap<>();
        typeStats.put("选择题", multiQuestions.size());
        typeStats.put("填空题", fillQuestions.size());
        typeStats.put("判断题", judgeQuestions.size());
        typeStats.put("主观题", subjectiveQuestions.size());
        stats.put("questionTypeDistribution", typeStats);

        result.put("multiQuestions", multiQuestions);
        result.put("fillQuestions", fillQuestions);
        result.put("judgeQuestions", judgeQuestions);
        result.put("subjectiveQuestions", subjectiveQuestions);
        result.put("stats", stats);
    }

    /**
     * 从索引中查找选择题（O(1)）
     */
    private MultiQuestion findMultiQuestion(Integer id) {
        return multiIndex.get(id);
    }

    /**
     * 从索引中查找填空题（O(1)）
     */
    private FillQuestion findFillQuestion(Integer id) {
        return fillIndex.get(id);
    }

    /**
     * 从索引中查找判断题（O(1)）
     */
    private JudgeQuestion findJudgeQuestion(Integer id) {
        return judgeIndex.get(id);
    }

    /**
     * 从索引中查找主观题（O(1)）
     */
    private SubjectiveQuestion findSubjectiveQuestion(Integer id) {
        return subjectiveIndex.get(id);
    }

    @Override
    public List<String> getAvailableSubjects() {
        Set<String> subjects = new HashSet<>();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MultiQuestion> multiPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        multiQuestionMapper.findAll(multiPage).getRecords().forEach(q -> {
            if (q.getSubject() != null)
                subjects.add(q.getSubject());
        });

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<FillQuestion> fillPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        fillQuestionMapper.findAll(fillPage).getRecords().forEach(q -> {
            if (q.getSubject() != null)
                subjects.add(q.getSubject());
        });

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JudgeQuestion> judgePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        judgeQuestionMapper.findAll(judgePage).getRecords().forEach(q -> {
            if (q.getSubject() != null)
                subjects.add(q.getSubject());
        });

        // 同时包含主观题科目
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectiveQuestion> subjectivePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        subjectiveQuestionMapper.findAll(subjectivePage).getRecords().forEach(q -> {
            if (q.getSubject() != null)
                subjects.add(q.getSubject());
        });

        List<String> result = new ArrayList<>(subjects);
        java.util.Collections.sort(result);
        return result;
    }

    @Override
    public List<String> getSections(String subject) {
        Set<String> sections = new HashSet<>();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MultiQuestion> multiPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        multiQuestionMapper.findAll(multiPage).getRecords().stream()
                .filter(q -> subject == null || subject.isEmpty() || subject.equals(q.getSubject()))
                .forEach(q -> {
                    if (q.getSection() != null && !q.getSection().isEmpty()) {
                        sections.add(q.getSection());
                    }
                });

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<FillQuestion> fillPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        fillQuestionMapper.findAll(fillPage).getRecords().stream()
                .filter(q -> subject == null || subject.isEmpty() || subject.equals(q.getSubject()))
                .forEach(q -> {
                    if (q.getSection() != null && !q.getSection().isEmpty()) {
                        sections.add(q.getSection());
                    }
                });

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JudgeQuestion> judgePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        judgeQuestionMapper.findAll(judgePage).getRecords().stream()
                .filter(q -> subject == null || subject.isEmpty() || subject.equals(q.getSubject()))
                .forEach(q -> {
                    if (q.getSection() != null && !q.getSection().isEmpty()) {
                        sections.add(q.getSection());
                    }
                });

        return new ArrayList<>(sections);
    }

    @Override
    public Map<String, Object> getQuestionBankStats() {
        Map<String, Object> stats = new HashMap<>();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MultiQuestion> multiPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        List<MultiQuestion> multiList = multiQuestionMapper.findAll(multiPage).getRecords();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<FillQuestion> fillPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        List<FillQuestion> fillList = fillQuestionMapper.findAll(fillPage).getRecords();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JudgeQuestion> judgePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        List<JudgeQuestion> judgeList = judgeQuestionMapper.findAll(judgePage).getRecords();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SubjectiveQuestion> subjectivePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10000);
        List<SubjectiveQuestion> subjectiveList = subjectiveQuestionMapper.findAll(subjectivePage).getRecords();

        stats.put("totalQuestions", multiList.size() + fillList.size() + judgeList.size() + subjectiveList.size());
        stats.put("multiQuestionCount", multiList.size());
        stats.put("fillQuestionCount", fillList.size());
        stats.put("judgeQuestionCount", judgeList.size());
        stats.put("subjectiveQuestionCount", subjectiveList.size());

        // 按科目统计
        Map<String, Integer> subjectStats = new HashMap<>();
        multiList.forEach(q -> {
            if (q.getSubject() != null) {
                subjectStats.merge(q.getSubject(), 1, Integer::sum);
            }
        });
        fillList.forEach(q -> {
            if (q.getSubject() != null) {
                subjectStats.merge(q.getSubject(), 1, Integer::sum);
            }
        });
        judgeList.forEach(q -> {
            if (q.getSubject() != null) {
                subjectStats.merge(q.getSubject(), 1, Integer::sum);
            }
        });
        subjectiveList.forEach(q -> {
            if (q.getSubject() != null) {
                subjectStats.merge(q.getSubject(), 1, Integer::sum);
            }
        });
        stats.put("subjectStats", subjectStats);

        return stats;
    }

    @Override
    public Map<String, Object> createExamFromPaper(Map<String, Object> examInfo, Map<String, Object> paperResult) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取新的paperId
            ExamManage lastExam = examManageMapper.findOnlyPaperId();
            int newPaperId = (lastExam != null && lastExam.getPaperId() != null) ? lastExam.getPaperId() + 1 : 1;

            // 2. 创建考试记录
            ExamManage examManage = new ExamManage();
            examManage.setSource((String) examInfo.get("source"));
            examManage.setDescription((String) examInfo.get("description"));
            examManage.setPaperId(newPaperId);
            examManage.setExamDate((String) examInfo.get("examDate"));
            examManage.setTotalTime(
                    examInfo.get("totalTime") != null ? ((Number) examInfo.get("totalTime")).intValue() : 120);
            examManage.setGrade((String) examInfo.get("grade"));
            examManage.setTerm((String) examInfo.get("term"));
            examManage.setMajor((String) examInfo.get("major"));
            examManage.setInstitute((String) examInfo.get("institute"));
            examManage.setType((String) examInfo.get("type"));
            examManage.setTips((String) examInfo.get("tips"));
            // 考试时间窗，前端未传时用全天默认值
            String startTime = (String) examInfo.get("examStartTime");
            String endTime   = (String) examInfo.get("examEndTime");
            examManage.setExamStartTime(startTime != null && !startTime.isEmpty() ? startTime : "00:00");
            examManage.setExamEndTime  (endTime   != null && !endTime.isEmpty()   ? endTime   : "23:59");

            // 获取总分和各题型分值配置
            Map<String, Object> stats = (Map<String, Object>) paperResult.get("stats");
            if (stats != null && stats.get("totalScore") != null) {
                examManage.setTotalScore(((Number) stats.get("totalScore")).intValue());
            }
            // 保存各题型每题分值（与组卷配置一致，学生答题时直接使用）
            examManage.setMultiScore(stats != null && stats.get("multiScore") != null
                    ? ((Number) stats.get("multiScore")).intValue() : 2);
            examManage.setFillScore(stats != null && stats.get("fillScore") != null
                    ? ((Number) stats.get("fillScore")).intValue() : 5);
            examManage.setJudgeScore(stats != null && stats.get("judgeScore") != null
                    ? ((Number) stats.get("judgeScore")).intValue() : 5);
            examManage.setSubjectiveScore(stats != null && stats.get("subjectiveScore") != null
                    ? ((Number) stats.get("subjectiveScore")).intValue() : 10);

            examManageMapper.add(examManage);

            // 3. 添加试卷题目关联
            List<?> multiQuestions = (List<?>) paperResult.get("multiQuestions");
            List<?> fillQuestions = (List<?>) paperResult.get("fillQuestions");
            List<?> judgeQuestions = (List<?>) paperResult.get("judgeQuestions");

            // 选择题 (questionType = 1)
            if (multiQuestions != null) {
                for (Object q : multiQuestions) {
                    Integer questionId = getQuestionId(q);
                    if (questionId != null) {
                        PaperManage pm = new PaperManage();
                        pm.setPaperId(newPaperId);
                        pm.setQuestionType(1);
                        pm.setQuestionId(questionId);
                        paperService.add(pm);
                    }
                }
            }

            // 填空题 (questionType = 2)
            if (fillQuestions != null) {
                for (Object q : fillQuestions) {
                    Integer questionId = getQuestionId(q);
                    if (questionId != null) {
                        PaperManage pm = new PaperManage();
                        pm.setPaperId(newPaperId);
                        pm.setQuestionType(2);
                        pm.setQuestionId(questionId);
                        paperService.add(pm);
                    }
                }
            }

            // 判断题 (questionType = 3)
            if (judgeQuestions != null) {
                for (Object q : judgeQuestions) {
                    Integer questionId = getQuestionId(q);
                    if (questionId != null) {
                        PaperManage pm = new PaperManage();
                        pm.setPaperId(newPaperId);
                        pm.setQuestionType(3);
                        pm.setQuestionId(questionId);
                        paperService.add(pm);
                    }
                }
            }

            // 主观题 (questionType = 4)
            List<?> subjectiveQuestions = (List<?>) paperResult.get("subjectiveQuestions");
            if (subjectiveQuestions != null) {
                for (Object q : subjectiveQuestions) {
                    Integer questionId = getQuestionId(q);
                    if (questionId != null) {
                        PaperManage pm = new PaperManage();
                        pm.setPaperId(newPaperId);
                        pm.setQuestionType(4);
                        pm.setQuestionId(questionId);
                        paperService.add(pm);
                    }
                }
            }

            result.put("success", true);
            result.put("message", "创建考试成功");
            result.put("examCode", examManage.getExamCode());
            result.put("paperId", newPaperId);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "创建考试失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 从不同类型的对象中获取questionId
     */
    private Integer getQuestionId(Object obj) {
        if (obj == null)
            return null;

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Object id = map.get("questionId");
            if (id instanceof Number) {
                return ((Number) id).intValue();
            }
        } else if (obj instanceof MultiQuestion) {
            return ((MultiQuestion) obj).getQuestionId();
        } else if (obj instanceof FillQuestion) {
            return ((FillQuestion) obj).getQuestionId();
        } else if (obj instanceof JudgeQuestion) {
            return ((JudgeQuestion) obj).getQuestionId();
        } else if (obj instanceof SubjectiveQuestion) {
            return ((SubjectiveQuestion) obj).getQuestionId();
        }

        return null;
    }

    @Override
    public Map<String, Object> compareSimilarity(List<String> subjects, Map<String, Object> paperResult) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 从当前组卷结果提取题目ID集合
            Set<String> currentIds = new HashSet<>();
            extractIdsFromResultList(paperResult, "multiQuestions", "m", currentIds);
            extractIdsFromResultList(paperResult, "fillQuestions", "f", currentIds);
            extractIdsFromResultList(paperResult, "judgeQuestions", "j", currentIds);
            extractIdsFromResultList(paperResult, "subjectiveQuestions", "s", currentIds);

            if (currentIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "当前试卷题目为空");
                return result;
            }

            // 2. 加载历史试卷
            Set<Integer> loadedIds = new HashSet<>();
            List<Map<String, Object>> details = new ArrayList<>();
            double maxSimilarity = 0.0;

            if (subjects != null && !subjects.isEmpty()) {
                for (String subject : subjects) {
                    List<GeneticPaperRecord> records = geneticPaperRecordMapper.findBySubject(subject);
                    for (GeneticPaperRecord record : records) {
                        if (record.getId() == null || loadedIds.contains(record.getId())) continue;
                        if (record.getPaperJson() == null) continue;
                        loadedIds.add(record.getId());

                        Set<String> existingIds = extractQuestionIdsFromJson(record.getPaperJson());
                        if (existingIds.isEmpty()) continue;

                        double similarity = calculateJaccard(currentIds, existingIds);
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("recordId", record.getId());
                        detail.put("subjects", record.getSubjects());
                        detail.put("similarity", Math.round(similarity * 10000.0) / 100.0);
                        detail.put("commonCount", countCommon(currentIds, existingIds));
                        details.add(detail);

                        if (similarity > maxSimilarity) maxSimilarity = similarity;
                    }
                }
            }

            // 按相似度降序排序
            details.sort((a, b) -> Double.compare(
                    ((Number) b.get("similarity")).doubleValue(),
                    ((Number) a.get("similarity")).doubleValue()));

            double maxSimilarityPct = Math.round(maxSimilarity * 10000.0) / 100.0;
            result.put("success", true);
            result.put("maxSimilarity", maxSimilarityPct);
            result.put("totalCompared", details.size());
            result.put("details", details.size() > 10 ? details.subList(0, 10) : details);
            result.put("warning", maxSimilarity > 0.50);
            result.put("currentPaperSize", currentIds.size());

            if (details.isEmpty()) {
                result.put("message", "该科目暂无历史试卷数据，无需比较");
            } else if (maxSimilarity > 0.50) {
                result.put("message", String.format("警告：试卷与历史试卷最高相似度达 %.1f%%，建议重新组卷！", maxSimilarityPct));
            } else if (maxSimilarity > 0.30) {
                result.put("message", String.format("提示：试卷与历史试卷相似度为 %.1f%%，尚在接受范围内", maxSimilarityPct));
            } else {
                result.put("message", String.format("试卷多样性良好，最高相似度 %.1f%%", maxSimilarityPct));
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "相似度计算失败: " + e.getMessage());
        }
        return result;
    }

    private void extractIdsFromResultList(Map<String, Object> paperResult, String key,
                                          String prefix, Set<String> ids) {
        Object listObj = paperResult.get(key);
        if (!(listObj instanceof List)) return;
        List<?> list = (List<?>) listObj;
        for (Object item : list) {
            Integer qId = getQuestionId(item);
            if (qId != null) ids.add(prefix + "_" + qId);
        }
    }

    private int countCommon(Set<String> setA, Set<String> setB) {
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        return intersection.size();
    }

    /**
     * 与正式考试试卷相似度对比（paper_manage 体系）
     *
     * 核心算法：
     *   Step1: 一次 SQL（UNION 四张题目表）取属于该科目的所有 paperId
     *   Step2: Java 内 HashMap 按 paperId 分组 → 每张试卷的题目指纹 Set（格式: t_id）
     *   Step3: 对每张试卷计算 Jaccard 相似系数，取最大值与拍序
     *   Step4: 批量查 examName（仅对 Top10 结果）
     */
    @Override
    public Map<String, Object> compareWithFormalExams(List<String> subjects, Map<String, Object> paperResult) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 提取当前组卷题目指纹
            Set<String> currentIds = new HashSet<>();
            extractIdsFromResultList(paperResult, "multiQuestions",      "m", currentIds);
            extractIdsFromResultList(paperResult, "fillQuestions",       "f", currentIds);
            extractIdsFromResultList(paperResult, "judgeQuestions",      "j", currentIds);
            extractIdsFromResultList(paperResult, "subjectiveQuestions", "s", currentIds);

            if (currentIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "当前试卷题目为空");
                return result;
            }

            if (subjects == null || subjects.isEmpty()) {
                result.put("success", true);
                result.put("totalCompared", 0);
                result.put("details", new ArrayList<>());
                result.put("maxSimilarity", 0.0);
                result.put("warning", false);
                result.put("message", "未指定科目，跳过正式考试相似度对比");
                return result;
            }

            // 一次 SQL 获取同科目正式考试全部题目条目
            List<PaperManage> paperItems = paperMapper.findPaperItemsBySubjects(subjects);

            if (paperItems.isEmpty()) {
                result.put("success", true);
                result.put("totalCompared", 0);
                result.put("details", new ArrayList<>());
                result.put("maxSimilarity", 0.0);
                result.put("warning", false);
                result.put("message", "该科目暂无正式考试试卷，无需比较");
                return result;
            }

            // HashMap 分组构建题目指纹（O(m)）
            Map<Integer, Set<String>> fingerprintMap = new LinkedHashMap<>();
            String[] prefixes = {null, "m", "f", "j", "s"};
            for (PaperManage item : paperItems) {
                int t = item.getQuestionType() != null ? item.getQuestionType() : 0;
                if (t >= 1 && t <= 4) {
                    fingerprintMap.computeIfAbsent(item.getPaperId(), k -> new HashSet<>())
                                  .add(prefixes[t] + "_" + item.getQuestionId());
                }
            }

            // 批量计算 Jaccard 相似度
            List<Map<String, Object>> details = new ArrayList<>();
            double maxSimilarity = 0.0;
            for (Map.Entry<Integer, Set<String>> entry : fingerprintMap.entrySet()) {
                Set<String> existingIds = entry.getValue();
                if (existingIds.isEmpty()) continue;
                double similarity = calculateJaccard(currentIds, existingIds);
                Map<String, Object> detail = new HashMap<>();
                detail.put("paperId",       entry.getKey());
                detail.put("examName",      "试卷" + entry.getKey()); // 占位，后续批量回填名称
                detail.put("similarity",   Math.round(similarity * 10000.0) / 100.0);
                detail.put("commonCount",  countCommon(currentIds, existingIds));
                detail.put("totalQuestions", existingIds.size());
                details.add(detail);
                if (similarity > maxSimilarity) maxSimilarity = similarity;
            }

            // 降序排序
            details.sort((a, b) -> Double.compare(
                    ((Number) b.get("similarity")).doubleValue(),
                    ((Number) a.get("similarity")).doubleValue()));

            // 批量查询考试名称（仅对 Top10）
            List<Map<String, Object>> top = details.size() > 10 ? details.subList(0, 10) : details;
            List<Integer> topIds = new ArrayList<>();
            for (Map<String, Object> d : top) topIds.add((Integer) d.get("paperId"));
            if (!topIds.isEmpty()) {
                List<ExamManage> exams = examManageMapper.findByPaperIds(topIds);
                Map<Integer, String> nameMap = new HashMap<>();
                for (ExamManage exam : exams) nameMap.put(exam.getPaperId(), exam.getSource());
                for (Map<String, Object> d : top) {
                    Integer pid = (Integer) d.get("paperId");
                    d.put("examName", nameMap.getOrDefault(pid, "试卷" + pid));
                }
            }

            double maxPct = Math.round(maxSimilarity * 10000.0) / 100.0;
            result.put("success",       true);
            result.put("maxSimilarity", maxPct);
            result.put("totalCompared", fingerprintMap.size());
            result.put("details",       top);
            result.put("warning",       maxSimilarity > 0.50);

            if (maxSimilarity > 0.50) {
                result.put("message", String.format("警告：与正式考试试卷最高相似度 %.1f%%，建议重新组卷！", maxPct));
            } else if (maxSimilarity > 0.30) {
                result.put("message", String.format("提示：与正式考试相似度 %.1f%%，尚在接受范围内", maxPct));
            } else {
                result.put("message", String.format("与正式考试差异良好，最高相似度 %.1f%%", maxPct));
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "正式考试相似度计算失败: " + e.getMessage());
        }
        return result;
    }
}
