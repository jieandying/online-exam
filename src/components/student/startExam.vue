// 我的考试页面
<template>
    <div id="myExam">
        <div class="title">试卷练习</div>
        <div class="wrapper">
            <!-- 说明提示 -->
            <div class="practice-notice">
                <i class="el-icon-info-solid"></i>
                <span>试卷练习仅限<strong>考试截止日期已过</strong>或<strong>您已完成该考试</strong>的试卷，练习时可查看答案解析。</span>
            </div>
            <!-- 搜索表单 - 若依风格 -->
            <div class="ruoyi-card search-card">
                <el-form :inline="true" class="search-form">
                    <el-form-item label="试卷名称">
                        <el-input
                            v-model="key"
                            placeholder="请输入试卷名称"
                            clearable
                            style="width: 200px"
                            @keyup.enter.native="search"
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" icon="el-icon-search" @click="search()">搜索</el-button>
                        <el-button icon="el-icon-refresh" @click="resetSearch()">重置</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <!-- 网格布局容器 -->
            <div class="exam-grid-container" v-loading="loading">
                <div class="exam-grid">
                    <div
                        class="exam-item"
                        v-for="(item, index) in pagination.records"
                        :key="index"
                        @click="toExamMsg(item)"
                        :style="{ animationDelay: (index * 100) + 'ms' }"
                        :class="{ 'exam-item-locked': !isPracticeAvailable(item) }"
                    >
                        <!-- 考试卡片 -->
                        <div class="exam-card" :class="{ 'card-locked': !isPracticeAvailable(item) }">
                            <!-- 卡片头部 -->
                            <div class="card-header">
                                <div class="practice-type-badge" :class="getPracticeTypeBadgeClass(item.type)">
                                    {{ item.type || '练习模式' }}
                                </div>
                                <div v-if="isPracticeAvailable(item)" class="exam-status status-available">
                                    <i class="el-icon-check"></i> 可练习
                                </div>
                                <div v-else class="exam-status status-locked">
                                    <i class="el-icon-lock"></i> 考试中
                                </div>
                            </div>
                            
                            <!-- 锁定遮罩 -->
                            <div class="lock-overlay" v-if="!isPracticeAvailable(item)">
                                <div class="lock-content">
                                    <i class="el-icon-lock lock-icon"></i>
                                    <p class="lock-tip">该考试仍在进行中</p>
                                    <p class="lock-sub">完成考试或截止日期过后可练习</p>
                                </div>
                            </div>

                            <!-- 主要内容 -->
                            <div class="card-content">
                                <h3 class="exam-title">{{ item.source }}</h3>
                                <p class="exam-description">{{ item.description }}</p>
                                
                                <!-- 考试信息 -->
                                <div class="exam-info-grid">
                                    <div class="info-item">
                                        <i class="el-icon-date info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">考试日期</span>
                                            <span class="info-value">{{ formatDate(item.examDate) }}</span>
                                        </div>
                                    </div>
                                    
                                    <div class="info-item" v-if="item.totalTime">
                                        <i class="el-icon-timer info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">建议时长</span>
                                            <span class="info-value">{{ item.totalTime }}分钟</span>
                                        </div>
                                    </div>
                                    
                                    <div class="info-item" v-if="item.totalScore">
                                        <i class="el-icon-trophy info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">总分</span>
                                            <span class="info-value">{{ item.totalScore }}分</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 考生须知 -->
                                <div class="exam-tips" v-if="item.tips">
                                    <i class="el-icon-info tips-icon"></i>
                                    <span class="tips-content">{{ item.tips }}</span>
                                </div>
                            </div>
                            
                            <!-- 卡片底部操作 -->
                            <div class="card-footer">
                                <el-button 
                                    :type="isPracticeAvailable(item) ? 'success' : 'info'"
                                    size="medium"
                                    class="exam-btn"
                                    :disabled="!isPracticeAvailable(item)"
                                >
                                    <i :class="isPracticeAvailable(item) ? 'el-icon-video-play' : 'el-icon-lock'"></i>
                                    {{ isPracticeAvailable(item) ? (myAnsweredExamCodes.has(String(item.examCode)) ? '再次练习' : '开始练习') : '尚未解锁' }}
                                </el-button>
                                <div class="exam-code">编号: {{ item.examCode }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="pagination-wrapper">
                <el-pagination
                    background
                    layout="prev, pager, next"
                    :current-page="pagination.current"
                    :page-size="pagination.size"
                    :total="pagination.total"
                    @current-change="handleCurrentChange"
                >
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            loading: false,
            key: null,
            allExam: null,
            pagination: {
                current: 1,
                total: null,
                size: 6,
            },
            myAnsweredExamCodes: new Set(), // 已作答的 examCode 集合
        };
    },
    created() {
        this.loading = true;
        this.getExamInfo();
        this.loadMyScores();
    },
    methods: {
        getExamInfo() {
            this.$axios(`/api/exams/${this.pagination.current}/${this.pagination.size}`)
                .then((res) => {
                    this.pagination = res.data.data;
                    this.loading = false;
                })
                .catch((error) => {
                    console.log(error);
                    this.loading = false;
                });
        },
        // 加载已作答记录
        async loadMyScores() {
            const studentId = this.$cookies.get('cid');
            if (!studentId) return;
            try {
                const res = await this.$axios.get(`/api/score/${studentId}`);
                if (res.data.code === 200) {
                    const codes = (res.data.data || []).map(s => String(s.examCode));
                    this.myAnsweredExamCodes = new Set(codes);
                }
            } catch (e) {
                console.error('加载成绩失败:', e);
            }
        },
        // 判断是否可以练习：考试截止日期已过 OR 已作答
        isPracticeAvailable(exam) {
            if (!exam) return false;
            const today = this.formatDateNow();
            // 已作答
            if (this.myAnsweredExamCodes.has(String(exam.examCode))) return true;
            // 考试日期严格早于今天（截止日期已过）
            const examDate = exam.examDate ? String(exam.examDate).substr(0, 10) : '';
            return examDate < today;
        },
        handleSizeChange(val) {
            this.pagination.size = val;
            this.getExamInfo();
        },
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.getExamInfo();
        },
        search() {
            if (!this.key || this.key.trim() === '') {
                this.getExamInfo();
                return;
            }
            this.$axios("/api/exams").then((res) => {
                if (res.data.code == 200) {
                    let allExam = res.data.data;
                    let newPage = allExam.filter((item) => {
                        return item.source.includes(this.key.trim());
                    });
                    this.pagination.records = newPage;
                    this.pagination.total = newPage.length;
                }
            });
        },
        resetSearch() {
            this.key = '';
            this.getExamInfo();
        },
        toExamMsg(exam) {
            if (!this.isPracticeAvailable(exam)) {
                this.$message({
                    message: '该考试仍在进行中，完成考试或截止日期过后方可练习',
                    type: 'warning',
                    duration: 3000
                });
                return;
            }
            this.$store.commit('practice', true);
            this.$router.push({
                path: "/examMsg",
                query: { examCode: exam.examCode },
            });
        },
        formatDateNow() {
            var date = new Date();
            var year = date.getFullYear();
            var month = ("0" + (date.getMonth() + 1)).slice(-2);
            var day = ("0" + date.getDate()).slice(-2);
            return year + "-" + month + "-" + day;
        },
        formatDate(dateStr) {
            if (!dateStr) return '随时可练';
            const date = new Date(dateStr);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
            const weekday = weekdays[date.getDay()];
            return `${year}年${month}月${day}日 ${weekday}`;
        },
        getPracticeTypeBadgeClass(type) {
            if (!type) return 'badge-practice';
            if (type.includes('模拟')) return 'badge-important';
            if (type.includes('专项')) return 'badge-warning';
            if (type.includes('练习') || type.includes('测试')) return 'badge-success';
            return 'badge-practice';
        }
    },
};
</script>

<style lang="less" scoped>
@import "../../assets/styles/exam-card.css";

.practice-notice {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    background: linear-gradient(135deg, #e8f4ff, #f0f7ff);
    border: 1px solid #b3d8ff;
    border-radius: 10px;
    color: #409EFF;
    font-size: 14px;
    margin-bottom: 16px;
    i { font-size: 18px; flex-shrink: 0; }
    strong { color: #2c6eb5; }
}

.exam-item-locked {
    cursor: not-allowed !important;
}

.card-locked {
    position: relative;
    opacity: 0.75;
}

.lock-overlay {
    position: absolute;
    inset: 0;
    z-index: 10;
    background: rgba(240, 242, 245, 0.7);
    border-radius: inherit;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(2px);
    cursor: not-allowed;
}

.lock-content {
    text-align: center;
    color: #909399;
}

.lock-icon {
    font-size: 36px;
    color: #c0c4cc;
    margin-bottom: 8px;
    display: block;
}

.lock-tip {
    font-size: 15px;
    font-weight: 600;
    color: #606266;
    margin: 0 0 4px;
}

.lock-sub {
    font-size: 12px;
    color: #909399;
    margin: 0;
}

.status-locked {
    background: #f5f7fa;
    color: #909399;
    border: 1px solid #e4e7ed;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
}
</style>
