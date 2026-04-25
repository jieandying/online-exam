// 点击试卷后的缩略信息 - 现代化设计
<template>
  <div id="msg" class="exam-msg-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item><i class="el-icon-s-home"></i> 首页</el-breadcrumb-item>
        <el-breadcrumb-item>试卷列表</el-breadcrumb-item>
        <el-breadcrumb-item class="active">{{examData.source}}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 试卷信息卡片 -->
    <div class="exam-info-card">
      <div class="card-header">
        <div class="header-left">
          <div class="exam-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="exam-title-section">
            <h1 class="exam-title">{{examData.source}}</h1>
            <div class="exam-meta">
              <span class="meta-item">
                <i class="el-icon-time"></i>
                更新于 {{examData.examDate}}
              </span>
              <span class="meta-item">
                <i class="el-icon-office-building"></i>
                {{examData.institute}}
              </span>
              <el-tag :type="getExamTypeTag(examData.type)" size="small" effect="dark">
                {{examData.type}}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="score-badge">
            <div class="score-label">总分</div>
            <div class="score-value">{{(score[0]||0)+(score[1]||0)+(score[2]||0)+(score[3]||0)}}</div>
            <div class="score-unit">分</div>
          </div>
        </div>
      </div>

      <div class="card-body">
        <div class="exam-stats">
          <div class="stat-item">
            <div class="stat-icon">
              <i class="el-icon-timer"></i>
            </div>
            <div class="stat-content">
              <div class="stat-label">考试时长</div>
              <div class="stat-value">{{examData.totalTime}} 分钟</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">
              <i class="el-icon-edit-outline"></i>
            </div>
            <div class="stat-content">
              <div class="stat-label">题目总数</div>
              <div class="stat-value">{{(topicCount[0]||0) + (topicCount[1]||0) + (topicCount[2]||0) + (topicCount[3]||0)}} 题</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-content">
              <div class="stat-label">总分</div>
              <div class="stat-value">{{(score[0]||0)+(score[1]||0)+(score[2]||0)+(score[3]||0)}} 分</div>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large"
            icon="el-icon-right"
            @click="toAnswer(examData.examCode)"
            class="start-btn">
            {{$store.state.isPractice==true?'开始练习':'开始考试'}}
          </el-button>
          <el-button 
            size="large"
            icon="el-icon-info"
            @click="dialogVisible = true"
            class="info-btn">
            考生须知
          </el-button>
        </div>
      </div>
    </div>

    <!-- 试题详情卡片 -->
    <div class="questions-card">
      <div class="card-title">
        <i class="el-icon-document-checked"></i>
        <span>试题详情</span>
        <div class="title-badge">
          <span>{{(score[0]||0)+(score[1]||0)+(score[2]||0)+(score[3]||0)}}分</span>
          <span class="divider">|</span>
          <span>{{examData.totalTime}}分钟</span>
        </div>
      </div>

      <div class="exam-structure">
        <div class="structure-item">
          <div class="section-icon choice-icon">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="section-info">
            <span class="section-title">选择题</span>
            <span class="section-meta">共{{topicCount[0]}}题 · {{score[0]}}分</span>
          </div>
        </div>
        <div class="structure-item">
          <div class="section-icon fill-icon">
            <i class="el-icon-edit"></i>
          </div>
          <div class="section-info">
            <span class="section-title">填空题</span>
            <span class="section-meta">共{{topicCount[1]}}题 · {{score[1]}}分</span>
          </div>
        </div>
        <div class="structure-item">
          <div class="section-icon judge-icon">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="section-info">
            <span class="section-title">判断题</span>
            <span class="section-meta">共{{topicCount[2]}}题 · {{score[2]}}分</span>
          </div>
        </div>
        <div class="structure-item">
          <div class="section-icon subjective-icon">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="section-info">
            <span class="section-title">主观题</span>
            <span class="section-meta">共{{topicCount[3]}}题 · {{score[3]}}分</span>
          </div>
        </div>
        <div class="structure-notice">
          <i class="el-icon-warning-outline"></i>
          <span>题目内容将在正式开始考试后显示</span>
        </div>
      </div>
    </div>

    <!-- 考生须知对话框 -->
    <el-dialog
      title="考生须知"
      :visible.sync="dialogVisible"
      width="600px"
      custom-class="tips-dialog"
      center>
      <div class="tips-content">
        <div class="tips-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="tips-text">{{examData.tips}}</div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false" size="medium">
          <i class="el-icon-check"></i> 我已知晓
        </el-button>
      </span>
    </el-dialog>
    
    <!-- 人脸识别弹窗 -->
    <face-recognition
      :visible="faceDialogVisible"
      :student-id="studentId"
      :exam-code="currentExamCode"
      :is-register="false"
      @success="onFaceVerifySuccess"
      @close="faceDialogVisible = false"
    />
  </div>
</template>

<script>
import FaceRecognition from '@/components/common/FaceRecognition.vue'

export default {
  components: {
    FaceRecognition
  },
  data() {
    return {
      dialogVisible: false,
      faceDialogVisible: false,
      topicCount: [],
      score: [],
      examData: {},
      topic: {},
      studentId: null,
      currentExamCode: null,
    }
  },
  mounted() {
    this.init()
    this.studentId = this.$cookies.get('cid')
  },
  methods: {
    init() {
      let examCode = this.$route.query.examCode
      this.$axios(`/api/exam/${examCode}`).then(res => {
        res.data.data.examDate = res.data.data.examDate.substr(0,10)
        this.examData = { ...res.data.data}
        // 统一使用 /api/exam-paper/{examCode} 接口，确保题目分值与组卷配置完全一致
        this.$axios(`/api/exam-paper/${examCode}`).then(res => {
          this.topic = {...res.data}
          let keys = Object.keys(this.topic)
          keys.forEach(e => {
            let data = this.topic[e]
            this.topicCount.push(data.length)
            let currentScore = 0
            for(let i = 0; i < data.length; i++) {
              currentScore += (data[i].score || 0)
            }
            this.score.push(currentScore)
          })
          // 如果数据库总分为空，从题目分值奇加回填
          if (!this.examData.totalScore) {
            this.examData.totalScore = this.score.reduce((a, b) => a + b, 0)
          }
        })
      })
    },
    getExamTypeTag(type) {
      const typeMap = {
        '期末考试': 'danger',
        '期中考试': 'warning',
        '随堂测试': '',
        '课程作业': 'info'
      };
      return typeMap[type] || 'info';
    },
    toAnswer(id) {
      if (this.$store.state.isPractice) {
        this.$router.push({path:"/answer",query:{examCode: id, isPractice: 'true'}})
        return
      }
      this.currentExamCode = id
      this.checkBeforeStart(id)
    },
    async checkBeforeStart(examCode) {
      // 1. 检查考试时间窗口
      if (this.examData.totalTime && this.examData.examDate) {
        const startTime = this.examData.examStartTime || '00:00'
        const endTime   = this.examData.examEndTime   || '23:59'
        const examStart = new Date(this.examData.examDate + ' ' + startTime + ':00')
        const examEnd   = new Date(this.examData.examDate + ' ' + endTime   + ':59')
        const now = new Date()
        if (now < examStart) {
          this.$message({ type: 'warning', message: `考试尚未开始，开考时间为 ${startTime}，请稍候再试`, duration: 4000 })
          return
        }
        if (now > examEnd) {
          this.$message({ type: 'error', message: '该考试已经结束，不可再次参加', duration: 3000 })
          return
        }
      }
      // 2. 检查是否已参加
      try {
        const studentId = this.studentId
        const res = await this.$axios.get(`/api/score/${studentId}`)
        if (res.data.code === 200) {
          const scores = res.data.data || []
          const already = scores.some(s => String(s.examCode) === String(examCode))
          if (already) {
            this.$confirm(
              '您已参加过本次考试，不可重复参加。',
              '提示',
              { confirmButtonText: '查看成绩', cancelButtonText: '返回', type: 'warning' }
            ).then(() => {
              this.$router.push({ path: '/scoreTable' })
            }).catch(() => {})
            return
          }
        }
      } catch (e) {
        console.error('检查参考状态失败:', e)
      }
      // 3. 进行人脸验证
      this.checkFaceAndStart(examCode)
    },
    async checkFaceAndStart(examCode) {
      try {
        const res = await this.$axios.get(`/api/face/check/${this.studentId}`)
        if (res.data.code === 200 && res.data.data) {
          if (res.data.data.success) {
            this.faceDialogVisible = true
          } else {
            this.$confirm('您还未注册人脸信息，是否现在注册？', '提示', {
              confirmButtonText: '去注册',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.$router.push({ path: '/manager', query: { action: 'faceRegister' } })
            }).catch(() => {})
          }
        }
      } catch (error) {
        console.error('检查人脸状态失败:', error)
        this.$message.error('检查人脸状态失败，请重试')
      }
    },
    onFaceVerifySuccess(result) {
      this.faceDialogVisible = false
      this.$message.success('身份验证成功，正在进入考试...')
      setTimeout(() => {
        this.$router.push({path:"/answer",query:{examCode: this.currentExamCode}})
      }, 500)
    },
  }
}
</script>

<style lang="less" scoped>
/* 现代化考试信息页面样式 */
.exam-msg-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  ::v-deep .el-breadcrumb {
    font-size: 14px;
    line-height: 1;
    
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: #606266;
        font-weight: 400;
        transition: color 0.3s;
        
        &:hover {
          color: #409eff;
        }
        
        i {
          margin-right: 4px;
        }
      }
      
      &:last-child .el-breadcrumb__inner {
        color: #303133;
        font-weight: 500;
      }
    }
  }
}

/* 试卷信息卡片 */
.exam-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
}

.exam-info-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 32px;
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  gap: 20px;
  flex: 1;
}

.exam-icon {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.exam-title-section {
  flex: 1;
}

.exam-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.exam-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  
  i {
    font-size: 16px;
  }
}

.header-right {
  flex-shrink: 0;
}

.score-badge {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px 24px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 120px;
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  margin-bottom: 4px;
}

.score-unit {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.card-body {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 32px;
  position: relative;
  z-index: 1;
}

/* 统计信息 */
.exam-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.start-btn {
  min-width: 200px;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
  }
}

.info-btn {
  min-width: 140px;
  height: 50px;
  font-size: 16px;
  border-radius: 25px;
  border: 2px solid #667eea;
  color: #667eea;
  background: #fff;
  transition: all 0.3s ease;
  
  &:hover {
    background: #667eea;
    color: #fff;
    transform: translateY(-2px);
  }
}

/* 试题详情卡片 */
.questions-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 28px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e4e7ed;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  
  i {
    font-size: 22px;
    color: #409eff;
  }
}

.title-badge {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  
  .divider {
    color: #dcdfe6;
  }
}

.section-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
}

.choice-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.fill-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.judge-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.subjective-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.section-info {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
}

.section-meta {
  font-size: 13px;
  color: #909399;
}

/* 考试结构概览 */
.exam-structure {
  padding: 20px 28px;
}

.structure-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.structure-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.structure-notice {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff3e0 100%);
  border-radius: 12px;
  border: 1px solid #ffe4b2;
  color: #e6a23c;
  font-size: 14px;
}

.structure-notice i {
  font-size: 18px;
}

/* 对话框样式 */
.tips-content {
  display: flex;
  gap: 20px;
  padding: 20px 0;
}

.tips-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #ffa726 0%, #fb8c00 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  flex-shrink: 0;
}

.tips-text {
  flex: 1;
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-msg-container {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .header-right {
    width: 100%;
  }
  
  .score-badge {
    width: 100%;
  }
  
  .exam-stats {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .start-btn,
  .info-btn {
    width: 100%;
  }
}
</style>
