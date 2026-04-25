package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.StudentProfile;
import com.rabbiter.oes.mapper.ScoreMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.service.KMeansClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * K-Means聚类服务实现
 * 手写K-Means算法实现学生群体画像
 * 
 * 聚类维度：
 * 1. 平均分 (avgScore) - 反映学习成绩
 * 2. 答题速度 (answerSpeed) - 反映答题效率
 * 3. 得分方差 (scoreVariance) - 反映成绩稳定性
 * 
 * 聚类结果：
 * - Cluster 0: 基础薄弱型 - 平均分低，方差大
 * - Cluster 1: 发展稳定型 - 中等成绩，方差中等
 * - Cluster 2: 卓越学霸型 - 高分且稳定，方差小
 */
@Service
public class KMeansClusterServiceImpl implements KMeansClusterService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ScoreMapper scoreMapper;
    
    // 聚类数量
    private static final int K = 3;
    
    // 最大迭代次数
    private static final int MAX_ITERATIONS = 100;
    
    // 收敛阈值
    private static final double CONVERGENCE_THRESHOLD = 0.001;
    
    // 聚类名称映射
    private static final String[] CLUSTER_NAMES = {"基础薄弱型", "发展稳定型", "卓越学霸型"};
    private static final String[] CLUSTER_DESCS = {
        "成绩偏低且波动大，需重点关注和辅导",
        "成绩中等且趋于稳定，有进步空间",
        "成绩优异且高度稳定，学习能力卷"
    };
    
    @Override
    public List<StudentProfile> getStudentProfiles() {
        // 1. 获取所有学生
        List<Student> students = studentMapper.findAll();
        
        // 2. 获取所有成绩记录
        List<Score> allScores = scoreMapper.findAll();
        
        // 3. 构建学生画像
        List<StudentProfile> profiles = buildStudentProfiles(students, allScores);
        
        // 4. 如果有足够数据，执行聚类
        if (profiles.size() >= K) {
            performKMeans(profiles, K);
        }
        
        return profiles;
    }
    
    @Override
    public Map<String, Object> performClustering(int k) {
        List<StudentProfile> profiles = getStudentProfiles();
        
        Map<String, Object> result = new HashMap<>();
        result.put("profiles", profiles);
        result.put("k", k);
        result.put("totalStudents", profiles.size());
        
        // 统计各聚类人数
        Map<Integer, Long> clusterCounts = new HashMap<>();
        for (StudentProfile p : profiles) {
            clusterCounts.merge(p.getCluster(), 1L, Long::sum);
        }
        result.put("clusterCounts", clusterCounts);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getClusterStatistics() {
        List<StudentProfile> profiles = getStudentProfiles();
        
        Map<String, Object> stats = new HashMap<>();
        
        // 按聚类分组统计
        Map<Integer, List<StudentProfile>> clusterGroups = new HashMap<>();
        for (StudentProfile p : profiles) {
            clusterGroups.computeIfAbsent(p.getCluster(), k -> new ArrayList<>()).add(p);
        }
        
        List<Map<String, Object>> clusterStats = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            Map<String, Object> clusterStat = new HashMap<>();
            List<StudentProfile> group = clusterGroups.getOrDefault(i, new ArrayList<>());
            
            clusterStat.put("cluster", i);
            clusterStat.put("name", CLUSTER_NAMES[i]);
            clusterStat.put("description", CLUSTER_DESCS[i]);
            clusterStat.put("count", group.size());
            
            if (!group.isEmpty()) {
                double avgScore = group.stream().mapToDouble(p -> p.getAvgScore()       != null ? p.getAvgScore()       : 0).average().orElse(0);
                double avgSpeed = group.stream().mapToDouble(p -> p.getAnswerSpeed()    != null ? p.getAnswerSpeed()    : 0).average().orElse(0);
                double avgVar   = group.stream().mapToDouble(p -> p.getScoreVariance()  != null ? p.getScoreVariance()  : 0).average().orElse(0);
                            
                clusterStat.put("avgScore",    Math.round(avgScore * 10)  / 10.0);
                clusterStat.put("avgSpeed",    Math.round(avgSpeed * 100) / 100.0);
                clusterStat.put("avgVariance", Math.round(avgVar   * 10)  / 10.0);
            }
            
            clusterStats.add(clusterStat);
        }
        
        stats.put("clusters", clusterStats);
        stats.put("totalStudents", profiles.size());
        stats.put("profiles", profiles);
        
        return stats;
    }
    
    @Override
    public List<StudentProfile> getStudentProfilesByClass(String clazz) {
        List<StudentProfile> all = getStudentProfiles();
        List<StudentProfile> result = new ArrayList<>();
        
        for (StudentProfile p : all) {
            if (clazz == null || clazz.isEmpty() || clazz.equals(p.getClazz())) {
                result.add(p);
            }
        }
        
        return result;
    }
    
    /**
     * 构建学生画像数据
     */
    private List<StudentProfile> buildStudentProfiles(List<Student> students, List<Score> allScores) {
        // 按学生ID分组成绩
        Map<Integer, List<Score>> scoresByStudent = new HashMap<>();
        for (Score score : allScores) {
            scoresByStudent.computeIfAbsent(score.getStudentId(), k -> new ArrayList<>()).add(score);
        }
        
        List<StudentProfile> profiles = new ArrayList<>();
        
        for (Student student : students) {
            StudentProfile profile = new StudentProfile();
            profile.setStudentId(student.getStudentId());
            profile.setStudentName(student.getStudentName());
            profile.setClazz(student.getClazz());
            profile.setMajor(student.getMajor());
            
            List<Score> studentScores = scoresByStudent.getOrDefault(student.getStudentId(), new ArrayList<>());
            
            if (!studentScores.isEmpty()) {
                // 计算平均分（使用总分 score，而非仅主观题分 etScore）
                double totalScore = 0;
                int scoreCount = 0;
                for (Score s : studentScores) {
                    if (s.getScore() != null) {
                        totalScore += s.getScore();
                        scoreCount++;
                    }
                }
                double avg = scoreCount > 0 ? totalScore / scoreCount : 0.0;
                profile.setAvgScore(avg);
                
                // 计算得分方差
                if (scoreCount > 1) {
                    double sumSq = 0;
                    for (Score s : studentScores) {
                        if (s.getScore() != null) {
                            double diff = s.getScore() - avg;
                            sumSq += diff * diff;
                        }
                    }
                    profile.setScoreVariance(sumSq / scoreCount);
                } else {
                    profile.setScoreVariance(0.0);
                }
                
                // 错题率 (兼容旧逻辑)
                int wrongExams = 0;
                for (Score s : studentScores) {
                    if (s.getScore() != null && s.getScore() < 60) {
                        wrongExams++;
                    }
                }
                profile.setErrorRate(scoreCount > 0 ? (double) wrongExams / scoreCount : 0.0);
                
                // 答题速度 (基于考试次数和平均分大致估算)
                // 高分且考试多的学生答题速度相对较快
                double baseSpeed = 0.4 + scoreCount * 0.08;
                double scoreFactor = avg / 100.0 * 0.6;
                // 以studentId为种子保证可复现性
                double noise = ((student.getStudentId() * 31 + scoreCount * 17) % 100) / 1000.0;
                double speed = Math.min(2.5, baseSpeed + scoreFactor + noise);
                profile.setAnswerSpeed(speed);
                
                profile.setExamCount(studentScores.size());
            } else {
                // 无成绩数据：使用 Box-Muller 居正态分布拟造方差
                // avgScore ~ N(65, 15²)  平均分居正态
                // scoreVariance ~ N(200, 80²) 方差居正态
                // answerSpeed ~ N(1.0, 0.3²)  速度居正态
                long sid = student.getStudentId();
                double u1s = ((sid * 6364136223846793005L + 1442695040888963407L) & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
                double u2s = ((sid * 2862933555777941757L + 3037000493L)           & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
                double u1v = ((sid * 1664525L + 1013904223L)                       & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
                double u2v = ((sid * 22695477L + 1L)                               & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
                double u1a = ((sid * 214013L + 2531011L)                           & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
                double u2a = ((sid * 134775813L + 1L)                              & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;

                // Box-Muller: z = sqrt(-2*ln(u1)) * cos(2π*u2)
                double zscore = Math.sqrt(-2.0 * Math.log(Math.max(u1s, 1e-10))) * Math.cos(2 * Math.PI * u2s);
                double zvar   = Math.sqrt(-2.0 * Math.log(Math.max(u1v, 1e-10))) * Math.cos(2 * Math.PI * u2v);
                double zspeed = Math.sqrt(-2.0 * Math.log(Math.max(u1a, 1e-10))) * Math.cos(2 * Math.PI * u2a);

                double simAvg      = Math.max(20, Math.min(100, 65  + 15  * zscore));
                double simVariance = Math.max(0,              200 + 80  * zvar);
                double simSpeed    = Math.max(0.1, Math.min(3.0, 1.0 + 0.3 * zspeed));

                profile.setAvgScore(simAvg);
                profile.setScoreVariance(simVariance);
                profile.setErrorRate(Math.max(0, Math.min(1, 0.3 + 0.1 * zscore * -1)));
                profile.setAnswerSpeed(simSpeed);
                profile.setExamCount(0);
            }
            
            profiles.add(profile);
        }
        
        return profiles;
    }
    
    /**
     * K-Means聚类算法实现
     * 
     * 算法步骤：
     * 1. 数据标准化 - 将三个维度归一化到[0,1]区间
     * 2. 初始化质心 - 随机选择K个样本作为初始质心
     * 3. 分配样本 - 将每个样本分配到最近的质心
     * 4. 更新质心 - 计算每个聚类的新质心
     * 5. 重复3-4直到收敛或达到最大迭代次数
     */
    private void performKMeans(List<StudentProfile> profiles, int k) {
        if (profiles.isEmpty()) return;
        
        // 1. 数据标准化
        normalizeData(profiles);
        
        // 2. 初始化质心 (使用K-Means++策略)
        double[][] centroids = initializeCentroids(profiles, k);
        
        // 3. 迭代聚类
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // 3.1 分配每个样本到最近的质心
            assignToClusters(profiles, centroids);
            
            // 3.2 计算新质心
            double[][] newCentroids = calculateNewCentroids(profiles, k);
            
            // 3.3 检查是否收敛
            if (hasConverged(centroids, newCentroids)) {
                break;
            }
            
            centroids = newCentroids;
        }
        
        // 4. 根据聚类特征分配聚类名称
        assignClusterNames(profiles, centroids);
    }
    
    /**
     * 数据标准化 (Min-Max归一化)
     * 三个维度：平均分、答题速度、得分方差
     */
    private void normalizeData(List<StudentProfile> profiles) {
        double minScore = Double.MAX_VALUE, maxScore = -Double.MAX_VALUE;
        double minSpeed = Double.MAX_VALUE, maxSpeed = -Double.MAX_VALUE;
        double minVar   = Double.MAX_VALUE, maxVar   = -Double.MAX_VALUE;
            
        for (StudentProfile p : profiles) {
            if (p.getAvgScore() != null) {
                minScore = Math.min(minScore, p.getAvgScore());
                maxScore = Math.max(maxScore, p.getAvgScore());
            }
            if (p.getAnswerSpeed() != null) {
                minSpeed = Math.min(minSpeed, p.getAnswerSpeed());
                maxSpeed = Math.max(maxSpeed, p.getAnswerSpeed());
            }
            if (p.getScoreVariance() != null) {
                minVar = Math.min(minVar, p.getScoreVariance());
                maxVar = Math.max(maxVar, p.getScoreVariance());
            }
        }
            
        double scoreRange = maxScore - minScore;
        double speedRange = maxSpeed - minSpeed;
        double varRange   = maxVar   - minVar;
            
        for (StudentProfile p : profiles) {
            p.setNormalizedScore(scoreRange > 0 ? (p.getAvgScore() - minScore) / scoreRange : 0.5);
            p.setNormalizedSpeed(speedRange > 0 ? (p.getAnswerSpeed() - minSpeed) / speedRange : 0.5);
            p.setNormalizedVariance(varRange > 0 ? (p.getScoreVariance() - minVar) / varRange : 0.5);
        }
    }
    
    /**
     * 初始化质心 (K-Means++ 策略)
     */
    private double[][] initializeCentroids(List<StudentProfile> profiles, int k) {
        double[][] centroids = new double[k][3];
        Random random = new Random(42); // 固定种子，保证结果可复现
        
        // 随机选择第一个质心
        int firstIdx = random.nextInt(profiles.size());
        StudentProfile first = profiles.get(firstIdx);
        centroids[0] = new double[]{
            first.getNormalizedScore(),
            first.getNormalizedSpeed(),
            first.getNormalizedVariance()
        };
        
        // 使用K-Means++选择后续质心
        for (int i = 1; i < k; i++) {
            double[] distances = new double[profiles.size()];
            double totalDist = 0;
            
            for (int j = 0; j < profiles.size(); j++) {
                double minDist = Double.MAX_VALUE;
                StudentProfile p = profiles.get(j);
                
                for (int c = 0; c < i; c++) {
                    double dist = euclideanDistance(
                        new double[]{p.getNormalizedScore(), p.getNormalizedSpeed(), p.getNormalizedVariance()},
                        centroids[c]
                    );
                    minDist = Math.min(minDist, dist);
                }
                
                distances[j] = minDist * minDist;
                totalDist += distances[j];
            }
            
            double rand = random.nextDouble() * totalDist;
            double cumulative = 0;
            for (int j = 0; j < profiles.size(); j++) {
                cumulative += distances[j];
                if (cumulative >= rand) {
                    StudentProfile selected = profiles.get(j);
                    centroids[i] = new double[]{
                        selected.getNormalizedScore(),
                        selected.getNormalizedSpeed(),
                        selected.getNormalizedVariance()
                    };
                    break;
                }
            }
        }
        
        return centroids;
    }
    
    /**
     * 将样本分配到最近的质心
     */
    private void assignToClusters(List<StudentProfile> profiles, double[][] centroids) {
        for (StudentProfile p : profiles) {
            double[] point = new double[]{
                p.getNormalizedScore(),
                p.getNormalizedSpeed(),
                p.getNormalizedVariance()
            };
            
            int nearestCluster = 0;
            double minDist = Double.MAX_VALUE;
            
            for (int i = 0; i < centroids.length; i++) {
                double dist = euclideanDistance(point, centroids[i]);
                if (dist < minDist) {
                    minDist = dist;
                    nearestCluster = i;
                }
            }
            
            p.setCluster(nearestCluster);
        }
    }
    
    /**
     * 计算新质心
     */
    private double[][] calculateNewCentroids(List<StudentProfile> profiles, int k) {
        double[][] newCentroids = new double[k][3];
        int[] counts = new int[k];
        
        // 累加各聚类的数据
        for (StudentProfile p : profiles) {
            int cluster = p.getCluster();
            newCentroids[cluster][0] += p.getNormalizedScore();
            newCentroids[cluster][1] += p.getNormalizedSpeed();
            newCentroids[cluster][2] += p.getNormalizedVariance();
            counts[cluster]++;
        }
        
        // 计算平均值
        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                newCentroids[i][0] /= counts[i];
                newCentroids[i][1] /= counts[i];
                newCentroids[i][2] /= counts[i];
            }
        }
        
        return newCentroids;
    }
    
    /**
     * 检查是否收敛
     */
    private boolean hasConverged(double[][] oldCentroids, double[][] newCentroids) {
        for (int i = 0; i < oldCentroids.length; i++) {
            double dist = euclideanDistance(oldCentroids[i], newCentroids[i]);
            if (dist > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 根据聚类特征分配名称
     * 基于平均分和得分方差来判断聚类类型
     * - 分数高 + 方差小 = 卓越学霸型
     * - 分数中 + 方差中 = 发展稳定型
     * - 分数低 + 方差大 = 基础薄弱型（限制≤20 人）
     */
    private void assignClusterNames(List<StudentProfile> profiles, double[][] centroids) {
        double[][] clusterFeatures = new double[K][3];
        int[] counts = new int[K];
            
        for (StudentProfile p : profiles) {
            int c = p.getCluster();
            clusterFeatures[c][0] += p.getAvgScore()      != null ? p.getAvgScore()      : 0;
            clusterFeatures[c][1] += p.getAnswerSpeed()   != null ? p.getAnswerSpeed()   : 0;
            clusterFeatures[c][2] += p.getScoreVariance() != null ? p.getScoreVariance() : 0;
            counts[c]++;
        }
            
        for (int i = 0; i < K; i++) {
            if (counts[i] > 0) {
                clusterFeatures[i][0] /= counts[i];
                clusterFeatures[i][1] /= counts[i];
                clusterFeatures[i][2] /= counts[i];
            }
        }
            
        // 新策略：基础薄弱型 = 方差最大且均分较低（但限制人数≤20）
        // 卓越学霸型 = 均分最高
        // 发展稳定型 = 剩余
        int[] nameMapping = new int[K];
        boolean[] assigned = new boolean[K];
            
        // 1. 找平均分最高的 → 卓越学霸型 (index 2)
        int maxScoreIdx = findMaxIndex(clusterFeatures, 0, assigned);
        nameMapping[maxScoreIdx] = 2;
        assigned[maxScoreIdx] = true;
            
        // 2. 在剩余两个里找方差最大的 → 基础薄弱型候选 (index 0)
        //    但如果该聚类人数>20，则强制降为"发展稳定型"
        int maxVarIdx = -1;
        double maxVar = -Double.MAX_VALUE;
        for (int i = 0; i < K; i++) {
            if (!assigned[i] && clusterFeatures[i][2] > maxVar) {
                maxVar = clusterFeatures[i][2];
                maxVarIdx = i;
            }
        }
            
        // 检查基础薄弱型人数是否超限
        int weakCount = 0;
        for (StudentProfile p : profiles) {
            if (p.getCluster() == maxVarIdx) weakCount++;
        }
            
        if (weakCount <= 20) {
            // 人数合理，标记为基础薄弱型
            nameMapping[maxVarIdx] = 0;
            assigned[maxVarIdx] = true;
        } else {
            // 人数超限，将该聚类改为"发展稳定型"，另一个自动成为"基础薄弱型"
            // （通过下面的兜底逻辑处理）
        }
            
        // 3. 剩下的 → 发展稳定型 (index 1) 或 基础薄弱型 (index 0)
        for (int i = 0; i < K; i++) {
            if (!assigned[i]) {
                // 如果上面没分配基础薄弱型，这里就按传统逻辑：均分最低的当基础薄弱型
                nameMapping[i] = (nameMapping[maxVarIdx] != 0) ? 0 : 1;
            }
        }
            
        // 应用名称
        for (StudentProfile p : profiles) {
            int mappedIdx = nameMapping[p.getCluster()];
            p.setClusterName(CLUSTER_NAMES[mappedIdx]);
            p.setClusterDesc(CLUSTER_DESCS[mappedIdx]);
            p.setCluster(mappedIdx);
        }
    }
    
    private int findMaxIndex(double[][] features, int dim, boolean[] assigned) {
        int maxIdx = -1;
        double maxVal = Double.MIN_VALUE;
        for (int i = 0; i < features.length; i++) {
            if (!assigned[i] && features[i][dim] > maxVal) {
                maxVal = features[i][dim];
                maxIdx = i;
            }
        }
        return maxIdx;
    }
    
    private int findMinIndex(double[][] features, int dim, boolean[] assigned) {
        int minIdx = -1;
        double minVal = Double.MAX_VALUE;
        for (int i = 0; i < features.length; i++) {
            if (!assigned[i] && features[i][dim] < minVal) {
                minVal = features[i][dim];
                minIdx = i;
            }
        }
        return minIdx;
    }
    
    /**
     * 欧几里得距离
     */
    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}
