<!-- 现代化学生成绩页面 -->
<template>
  <div class="score-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <i class="el-icon-trophy header-icon"></i>
          <h1 class="page-title">我的分数</h1>
          <p class="page-subtitle">查看历次考试成绩记录</p>
        </div>
        <div class="stats-section">
          <div class="stat-card">
            <div class="stat-number">{{ score.length }}</div>
            <div class="stat-label">考试次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ getAverageScore() }}</div>
            <div class="stat-label">平均分数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ getPassRate() }}%</div>
            <div class="stat-label">及格率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="content-card">
        <div class="card-header">
          <div class="card-title">
            <i class="el-icon-document"></i>
            <span>成绩详情</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" icon="el-icon-refresh" @click="getScore" :loading="loading">
              刷新数据
            </el-button>
          </div>
        </div>
        
        <div class="table-container">
          <el-table 
            ref="filterTable" 
            :data="score" 
            v-loading="loading"
            class="modern-table"
            stripe
            :header-cell-style="{ background: '#f8fafc', color: '#374151', fontWeight: '600' }">
            <el-table-column
              prop="answerDate"
              label="考试日期"
              sortable
              min-width="160"
              column-key="answerDate"
              :filters="filter"
              :filter-method="filterHandler">
              <template slot-scope="scope">
                <div class="date-cell">
                  <i class="el-icon-calendar"></i>
                  <span>{{ scope.row.answerDate }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="subject"
              label="课程名称"
              min-width="200"
              filter-placement="bottom-end">
              <template slot-scope="scope">
                <el-tag class="subject-tag" type="info">{{ scope.row.subject }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="etScore" label="考试分数" width="120" align="center">
              <template slot-scope="scope">
                <div v-if="scope.row.etScore === -1 || scope.row.etScore === '-1'" class="pending-cell">
                  <i class="el-icon-time"></i>
                  <span>待批阅</span>
                </div>
                <div v-else class="score-cell" :class="getScoreClass(scope.row.etScore)">
                  <span class="score-number">{{ scope.row.etScore }}</span>
                  <span class="score-unit">分</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="是否及格" width="120" align="center">
              <template slot-scope="scope">
                <div v-if="scope.row.etScore === -1 || scope.row.etScore === '-1'"
                  class="result-tag" style="background:#fdf6ec;color:#E6A23C;border:1px solid #FAECD8;">
                  <i class="el-icon-time"></i>
                  <span>待批阅</span>
                </div>
                <div v-else
                  :class="['result-tag', scope.row.etScore >= 60 ? 'score-pass' : 'score-fail']">
                  <i :class="scope.row.etScore >= 60 ? 'el-icon-check' : 'el-icon-close'"></i>
                  <span>{{ scope.row.etScore >= 60 ? "及格" : "不及格" }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button
                  v-if="scope.row.etScore !== -1 && scope.row.etScore !== '-1'"
                  type="primary" size="small" icon="el-icon-view"
                  @click="viewExamDetail(scope.row)">
                  查看答卷
                </el-button>
                <el-tag v-else size="mini" type="info">待批阅中</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            @current-change="handleCurrentChange"
            :current-page="pagination.current"
            :page-size="pagination.size"
            layout="total, prev, pager, next, jumper"
            :total="pagination.total"
            background>
          </el-pagination>
        </div>
      </div>
    </div>

    <!-- 查看答卷对话框 -->
    <el-dialog title="📝 答卷详情" :visible.sync="detailDialogVisible" width="900px" @close="resetDetail">
      <div v-loading="detailLoading">
        <!-- 基本信息 -->
        <div class="exam-info-card" v-if="currentDetail">
          <div class="info-row">
            <span class="label">考试名称：</span>
            <span class="value">{{ currentDetail.subject }}</span>
          </div>
          <div class="info-row">
            <span class="label">考试日期：</span>
            <span class="value">{{ currentDetail.answerDate }}</span>
          </div>
          <div class="info-row score-row">
            <span class="label">得分：</span>
            <span class="value score-value" :class="getScoreClass(currentDetail.studentScore)">
              {{ currentDetail.studentScore != null ? currentDetail.studentScore : '待批阅' }}
              <span v-if="currentDetail.totalScore > 0" style="font-size:14px;color:#909399"> / 满分 {{ currentDetail.totalScore }} 分</span>
            </span>
          </div>
        </div>

        <!-- 提示信息 -->
        <el-alert type="info" :closable="false" style="margin-top: 16px;">
          <span slot="title">
            💡 提示：本功能正在完善中，目前可查看基本信息。详细错题解析即将上线。
          </span>
        </el-alert>

        <!-- TODO: 后续扩展各题型答题情况展示 -->
      </div>

      <div slot="footer">
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: { //分页后的留言列表
        current: 1, //当前页
        total: null, //记录条数
        size: 10 //每页条数
      },
      loading: false, //加载标识符
      score: [], //学生成绩
      filter: null, //过滤参数
      // 查看答卷相关
      detailDialogVisible: false,
      detailLoading: false,
      currentDetail: null
    }
  },
  created() {
    this.getScore()
    this.loading = true //数据加载则遮罩表格
  },
  methods: {
    getScore() {
      let studentId = this.$cookies.get("cid")
      this.$axios(`/api/score/${this.pagination.current}/${this.pagination.size}/${studentId}`).then(res => {
        if(res.data.code == 200) {
          this.loading = false //数据加载完成去掉遮罩
          this.score = res.data.data.records
          this.pagination = {...res.data.data}
          let mapVal = this.score.map((element,index) => { //通过map得到 filter:[{text,value}]形式的数组对象
            let newVal = {}
            newVal.text = element.answerDate
            newVal.value = element.answerDate
            return newVal
          })
          let hash = []
          const newArr = mapVal.reduce((item, next) => { //对新对象进行去重操作
            hash[next.text] ? '' : hash[next.text] = true && item.push(next);
            return item
          }, []);
          this.filter = newArr
        }
      })
    },
    //计算平均分
    getAverageScore() {
      if (!this.score || this.score.length === 0) return 0
      const validScores = this.score.filter(item => item.etScore !== -1 && item.etScore !== '-1')
      if (validScores.length === 0) return 0
      const totalScore = validScores.reduce((sum, item) => sum + (parseFloat(item.etScore) || 0), 0)
      return Math.round(totalScore / validScores.length)
    },
    //计算及格率
    getPassRate() {
      if (!this.score || this.score.length === 0) return 0
      const validScores = this.score.filter(item => item.etScore !== -1 && item.etScore !== '-1')
      if (validScores.length === 0) return 0
      const passCount = validScores.filter(item => (parseFloat(item.etScore) || 0) >= 60).length
      return Math.round((passCount / validScores.length) * 100)
    },
    //获取分数等级样式
    getScoreClass(score) {
      const numScore = parseFloat(score) || 0
      if (numScore >= 90) return 'score-excellent'
      if (numScore >= 80) return 'score-good'
      if (numScore >= 70) return 'score-medium'
      if (numScore >= 60) return 'score-pass'
      return 'score-fail'
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = val
      this.getScore()
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = val
      this.getScore()
    },
    formatter(row, column) {
      return row.address;
    },
    filterTag(value, row) {
      return row.tag === value;
    },
    filterHandler(value, row, column) {
      const property = column["property"];
      return row[property] === value;
    },
    // 查看答卷详情
    viewExamDetail(row) {
      this.detailLoading = true
      this.detailDialogVisible = true
      const studentId = this.$cookies.get('cid')
      this.$axios.get(`/api/score/detail/${row.examCode}/${studentId}`)
        .then(res => {
          this.detailLoading = false
          if (res.data.code === 200) {
            this.currentDetail = res.data.data
          } else {
            this.$message.error('获取答卷详情失败')
          }
        })
        .catch(() => {
          this.detailLoading = false
          this.$message.error('请求失败')
        })
    },
    resetDetail() {
      this.currentDetail = null
    }
  }
};
</script>

<style lang="less" scoped>
/* 现代化成绩页面 - 金色主题 */

// CSS 变量定义
:root {
  --score-primary: #F4A261;      /* 金橙色主色 */
  --score-light: #E9C46A;        /* 浅金色 */
  --score-dark: #E76F51;         /* 深橙色 */
  --excellent-color: #2A9D8F;    /* 优秀：青绿色 */
  --good-color: #67C23A;         /* 良好：绿色 */
  --medium-color: #F4A261;       /* 中等：金色 */
  --pass-color: #E6A23C;         /* 及格：橙色 */
  --fail-color: #F56C6C;         /* 不及格：红色 */
  --text-primary: #264653;       /* 主文字 */
  --text-regular: #2A9D8F;       /* 常规文字 */
  --text-secondary: #8B949E;     /* 次要文字 */
  --background-light: #F8F9FA;   /* 浅色背景 */
  --border-light: #E1E7EE;       /* 浅色边框 */
}

.score-page {
  background: #f5f5f5;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 页面头部 */
.page-header {
  background: #fff;
  padding: 30px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-icon {
  font-size: 32px;
  margin-bottom: 10px;
  color: #409EFF;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.page-subtitle {
  font-size: 14px;
  margin: 0;
  color: #909399;
  font-weight: 400;
}

/* 统计卡片区域 */
.stats-section {
  display: flex;
  gap: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px 24px;
  text-align: center;
  min-width: 100px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
  margin-bottom: 4px;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
}

/* 主内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

.card-header {
  padding: 20px 25px;
  background: #fafafa;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title i {
  font-size: 18px;
  color: #409EFF;
}

/* 表格容器 */
.table-container {
  padding: 0;
  overflow: hidden;
}

.modern-table {
  border-radius: 0;
}

// 深度选择器修改Element UI表格样式
::v-deep .modern-table {
  .el-table__header th {
    background: #fafafa !important;
    border-bottom: 1px solid #e0e0e0;
    font-weight: 600;
    color: #303133;
    font-size: 14px;
  }
  
  .el-table__body tr:hover > td {
    background: #f5f7fa !important;
  }
  
  .el-table__body td {
    border-bottom: 1px solid #ebeef5;
    padding: 12px 0;
    vertical-align: middle;
  }
}

/* 表格单元格样式 */
.date-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #606266;
}

.date-cell i {
  color: #409EFF;
  font-size: 14px;
}

.subject-tag {
  font-weight: 500;
  font-size: 12px;
  border-radius: 4px;
  padding: 4px 8px;
}

/* 分数单元格 */
.score-cell {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: center;
  padding: 4px;
  border-radius: 4px;
  font-weight: 600;
  min-width: 50px;
  gap: 2px;
}

.score-number {
  font-size: 18px;
  line-height: 1;
  font-weight: 600;
}

.score-unit {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 2px;
}

/* 待批阅单元格 */
.pending-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #E6A23C;
  font-weight: 600;
  font-size: 13px;
}

/* 分数等级样式 */
.score-excellent {
  background: transparent;
  color: #409EFF;
}

.score-good {
  background: transparent;
  color: #67C23A;
}

.score-medium {
  background: transparent;
  color: #E6A23C;
}

.score-pass {
  background: transparent;
  color: #67C23A;
}

.score-fail {
  background: transparent;
  color: #F56C6C;
}

.result-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 90px;
  height: 32px;
  border-radius: 8px;
  font-weight: 600;
  gap: 6px;
  border: none;
  font-size: 13px;
  transition: all 0.3s;
  padding: 0;
  line-height: 1;
}

.result-tag i {
  margin-right: 4px; /* Explicit spacing instead of gap for better support */
}

.result-tag span {
  position: relative;
  top: 1px; /* Optical adjustment for Chinese characters */
}

.result-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* 分页样式 */
.pagination-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #fafafa;
  border-top: 1px solid #e0e0e0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .main-content {
    padding: 20px 15px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px 0;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .stats-section {
    flex-wrap: wrap;
    justify-content: center;
    gap: 15px;
  }
  
  .stat-card {
    min-width: 80px;
    padding: 15px 18px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .card-header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .table-container {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .stats-section {
    flex-direction: column;
    align-items: center;
  }
  
  .stat-card {
    width: 100%;
    max-width: 200px;
  }
  
  .pagination-wrapper {
    padding: 15px;
  }
}

/* 查看答卷对话框 */
.exam-info-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #bae6fd;
}
.info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 14px;
}
.info-row:last-child { margin-bottom: 0; }
.label {
  color: #64748b;
  font-weight: 500;
  min-width: 80px;
}
.value {
  color: #1e293b;
  font-weight: 600;
}
.score-row .value {
  font-size: 20px;
}
.score-value {
  color: #409EFF;
}
</style>
