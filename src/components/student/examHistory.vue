<template>
  <div class="exam-history-container">
    <div class="page-header">
       <div class="header-icon">
         <i class="el-icon-time"></i>
       </div>
       <div class="header-content">
         <h2>考试档案</h2>
         <p>查看过往考试记录，支持重做与复习</p>
       </div>
    </div>

    <!-- 列表展示 -->
    <div class="history-card">
        <el-table :data="historyList" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="examDate" label="考试时间" width="200" sortable>
                 <template slot-scope="scope">
                    <div class="date-cell">
                        <i class="el-icon-date"></i>
                        <span>{{ scope.row.examDate }}</span>
                    </div>
                 </template>
            </el-table-column>
            
            <el-table-column prop="subject" label="科目" width="150" sortable>
                <template slot-scope="scope">
                    <el-tag size="medium" effect="plain">{{ scope.row.subject || '综合' }}</el-tag>
                </template>
            </el-table-column>
            
            <el-table-column prop="description" label="试卷描述" min-width="200">
                <template slot-scope="scope">
                    {{ scope.row.description || (scope.row.source ? '来源: ' + scope.row.source : '模拟考试') }}
                </template>
            </el-table-column>
            
            <el-table-column label="客观得分" width="110" align="center">
                <template slot-scope="scope">
                    <span class="score-text score-obj" :class="getScoreClass(scope.row.ptScore)">
                        {{ scope.row.ptScore != null ? scope.row.ptScore : '-' }}
                    </span>
                </template>
            </el-table-column>

            <el-table-column label="主观得分" width="110" align="center">
                <template slot-scope="scope">
                    <template v-if="scope.row.etScore == null || scope.row.etScore == -1">
                        <el-tag size="mini" type="warning">待批阅</el-tag>
                    </template>
                    <template v-else-if="scope.row.ptScore != null && scope.row.etScore > scope.row.ptScore">
                        <span class="score-text score-sub" :class="getScoreClass(scope.row.etScore - scope.row.ptScore)">
                            {{ scope.row.etScore - scope.row.ptScore }}
                        </span>
                    </template>
                    <template v-else>
                        <span class="score-text score-sub">0</span>
                    </template>
                </template>
            </el-table-column>

            <el-table-column label="总分" width="100" align="center">
                <template slot-scope="scope">
                    <template v-if="scope.row.etScore == null || scope.row.etScore == -1">
                        <el-tag size="mini" type="warning">待批阅</el-tag>
                    </template>
                    <template v-else>
                        <span class="score-text" :class="getScoreClass(scope.row.etScore)">
                            {{ scope.row.etScore }}
                        </span>
                    </template>
                </template>
            </el-table-column>

            <el-table-column label="操作" width="150" align="center">
                <template slot-scope="scope">
                    <el-button 
                        size="small" 
                        type="primary" 
                        icon="el-icon-refresh-left"
                        class="redo-btn"
                        @click="redoExam(scope.row.paperId)" 
                        :disabled="!scope.row.paperId"
                    >重做</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div class="empty-state" v-if="!loading && historyList.length === 0">
            <el-empty description="暂无考试记录"></el-empty>
        </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
    name: 'ExamHistory',
    data() {
        return {
            loading: false,
            historyList: []
        }
    },
    computed: {
        ...mapState(['userInfo'])
    },
    created() {
        this.fetchHistory()
    },
    methods: {
        async fetchHistory() {
            this.loading = true
            try {
                // Get studentId from cookie or vuex
                let studentId = this.userInfo ? this.userInfo.id : this.$cookies.get("cid")
                if(!studentId) {
                     // Try local storage if cookie/vuex failed (consistent with index.vue logic)
                     const userStr = localStorage.getItem('user')
                     if(userStr) {
                         const userData = JSON.parse(userStr)
                         studentId = userData.studentId || userData.cardId
                     }
                }

                if(!studentId) {
                    this.$message.warning('无法获取用户信息，请重新登录')
                    return
                }

                let res = await this.$axios.get(`/api/study/history/${studentId}`)
                if(res.data.code === 200) {
                    this.historyList = res.data.data
                } else {
                    this.$message.error(res.data.message || '获取记录失败')
                }
            } catch(err) {
                console.error(err)
                this.$message.error('获取考试记录失败，请稍后重试')
            } finally {
                this.loading = false
            }
        },
        redoExam(paperId) {
            this.$confirm('确定要重做这份试卷吗？这将作为一次新的练习开始。', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
            }).then(() => {
                this.$router.push({
                    path: '/mockExam',
                    query: { paperId: paperId }
                })
            }).catch(() => {})
        },
        getScoreClass(score) {
            if(score == null) return ''
            if(score >= 90) return 'score-excellent'
            if(score >= 60) return 'score-pass'
            return 'score-fail'
        }
    }
}
</script>

<style scoped>
.exam-history-container {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    animation: fadeIn 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.page-header {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 30px;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 30px;
    border-radius: 24px;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.5);
}

.header-icon {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 32px;
    box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.header-content h2 {
    margin: 0 0 8px 0;
    font-size: 26px;
    color: #1a1a2e;
    font-weight: 800;
}

.header-content p {
    margin: 0;
    color: #718096;
    font-size: 15px;
}

.history-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 24px;
    box-shadow: 0 8px 32px rgba(0,0,0,0.05);
    min-height: 400px;
    border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 表格样式优化 */
/deep/ .el-table {
    background: transparent !important;
}

/deep/ .el-table th, /deep/ .el-table tr, /deep/ .el-table td {
    background: transparent !important;
}

/deep/ .el-table__row:hover > td {
    background-color: rgba(102, 126, 234, 0.05) !important;
}

.date-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #4a5568;
    font-weight: 600;
}

.score-text {
    font-weight: 800;
    font-size: 18px;
}

.score-obj { color: #409EFF; }
.score-sub { color: #9b59b6; }

.score-excellent { color: #67C23A; }
.score-pass { color: #409EFF; }
.score-fail { color: #F56C6C; }

.redo-btn {
    border-radius: 10px;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.3s ease;
}

.redo-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
}

.empty-state {
    padding: 60px 0;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
