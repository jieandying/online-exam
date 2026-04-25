// 我的试卷页面
<template>
    <div id="myExam">
        <div class="title">考试中心</div>
        <div class="wrapper">
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
                    >
                        <!-- 考试卡片 -->
                        <div class="exam-card">
                            <!-- 卡片头部 -->
                            <div class="card-header">
                                <div class="exam-type-badge" :class="getExamTypeBadgeClass(item.type)">
                                    {{ item.type || '常规考试' }}
                                </div>
                                <div v-if="hasAnswered(item)" class="exam-status status-done">
                                    <i class="el-icon-circle-check"></i> 已参加
                                </div>
                                <div v-else-if="isExpired(item)" class="exam-status status-expired">
                                    <i class="el-icon-time"></i> 已结束
                                </div>
                                <div v-else class="exam-status status-available">
                                    <i class="el-icon-edit-outline"></i> 可参加
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
                                            <span class="info-label">考试时间</span>
                                            <span class="info-value">{{ formatDate(item.examDate) }}</span>
                                        </div>
                                    </div>
                                    
                                    <div class="info-item" v-if="item.totalTime">
                                        <i class="el-icon-timer info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">考试时长</span>
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
                                    
                                    <div class="info-item" v-if="item.grade">
                                        <i class="el-icon-user info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">年级</span>
                                            <span class="info-value">{{ item.grade }}</span>
                                        </div>
                                    </div>
                                    
                                    <div class="info-item" v-if="item.institute">
                                        <i class="el-icon-school info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">学院</span>
                                            <span class="info-value">{{ item.institute }}</span>
                                        </div>
                                    </div>
                                    
                                    <div class="info-item" v-if="item.major">
                                        <i class="el-icon-reading info-icon"></i>
                                        <div class="info-content">
                                            <span class="info-label">专业</span>
                                            <span class="info-value">{{ item.major }}</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 考生须知 -->
                                <div class="exam-tips" v-if="item.tips">
                                    <i class="el-icon-warning-outline tips-icon"></i>
                                    <span class="tips-content">{{ item.tips }}</span>
                                </div>
                            </div>
                            
                            <!-- 卡片底部操作 -->
                            <div class="card-footer">
                                <el-button 
                                    type="primary" 
                                    size="medium"
                                    class="exam-btn"
                                >
                                    <i class="el-icon-edit-outline"></i>
                                    进入考试
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
                size: 8,
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
            } catch (e) {}
        },
        // 是否已参加该考试
        hasAnswered(exam) {
            return this.myAnsweredExamCodes.has(String(exam.examCode));
        },
        // 是否已超过期限
        isExpired(exam) {
            if (!exam.examDate) return false;
            return exam.examDate < this.formatDateNow();
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
            // 已参加过，不可重复
            if (this.hasAnswered(exam)) {
                this.$confirm(
                    '您已参加过本次考试，每份试卷只能正式作答一次。如需复习请前往【试卷练习】。',
                    '已参加',
                    { confirmButtonText: '查看成绩', cancelButtonText: '去练习', type: 'info', distinguishCancelAndClose: true }
                ).then(() => {
                    this.$router.push({ path: '/scoreTable' });
                }).catch((action) => {
                    if (action === 'cancel') {
                        this.$store.commit('practice', true);
                        this.$router.push({ path: '/startExam' });
                    }
                });
                return;
            }
            if (exam.examDate > this.formatDateNow()) {
                this.$message({ message: '考试时间未到', type: 'error' });
                return;
            }
            this.$store.commit('practice', false);
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
            if (!dateStr) return '未设定';
            const date = new Date(dateStr);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
            const weekday = weekdays[date.getDay()];
            return `${year}年${month}月${day}日 ${weekday}`;
        },
        getExamTypeBadgeClass(type) {
            if (!type) return 'badge-default';
            if (type.includes('期末')) return 'badge-important';
            if (type.includes('期中')) return 'badge-warning';
            if (type.includes('练习') || type.includes('测试')) return 'badge-info';
            return 'badge-primary';
        }
    },
};
</script>

<style lang="less" scoped>
@import "../../assets/styles/exam-card.css";

/* 已参加状态标签 */
:deep(.status-done) {
    background: linear-gradient(135deg, #f0f9eb, #e1f3d8) !important;
    color: #67C23A !important;
    border: 1px solid #b3e19d !important;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
}

/* 已结束状态标签 */
:deep(.status-expired) {
    background: linear-gradient(135deg, #f4f4f5, #e9e9eb) !important;
    color: #909399 !important;
    border: 1px solid #d3d4d6 !important;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
}
</style>
