<template>
  <div class="mock-exam-container">
    <!-- 1. 考试配置页 -->
    <div class="exam-config" v-if="status === 'config'">
      <div class="config-card">
        <div class="config-header">
          <i class="el-icon-trophy"></i>
          <h2>自主测试</h2>
          <p>遗传算法智能组卷，个性化难度控制，全面提升你的能力</p>
        </div>
        
        <!-- 题型配置说明 -->
        <div class="type-config-fixed">
          <div class="type-fixed-row">
            <div class="type-fixed-item">
              <span class="type-badge multi">选</span>
              <span class="type-label">选择题</span>
              <span class="type-count">15 道</span>
              <span class="type-score">× 2 分</span>
            </div>
            <div class="type-fixed-item">
              <span class="type-badge fill">填</span>
              <span class="type-label">填空题</span>
              <span class="type-count">4 道</span>
              <span class="type-score">× 5 分</span>
            </div>
            <div class="type-fixed-item">
              <span class="type-badge judge">判</span>
              <span class="type-label">判断题</span>
              <span class="type-count">10 道</span>
              <span class="type-score">× 2 分</span>
            </div>
            <div class="type-fixed-item">
              <span class="type-badge subjective">主</span>
              <span class="type-label">主观题</span>
              <span class="type-count">2 道</span>
              <span class="type-score">× 15 分</span>
            </div>
          </div>
          <div class="type-fixed-tip"><i class="el-icon-info"></i> 题目数量已固定，仅需选择科目和目标难度即可开始测试</div>
        </div>

        <el-form label-position="top" size="medium">
          <el-form-item label="考试科目">
            <el-select v-model="config.subject" placeholder="请选择考试科目 (必选)" filterable style="width: 100%">
              <el-option v-for="sub in subjectOptions" :key="sub" :label="sub" :value="sub"></el-option>
            </el-select>
          </el-form-item>
          
          <!-- 目标难度 -->
          <el-form-item label="目标难度">
            <div class="difficulty-slider-wrapper">
              <el-slider v-model="config.targetDifficulty" :min="1" :max="5" :step="0.1"
                         :marks="difficultyMarks" show-stops class="difficulty-slider"/>
              <div class="difficulty-coefficient">
                <span class="coef-label">难度系数：</span>
                <span class="coef-value">{{ difficultyCoefficient.toFixed(2) }}</span>
                <span class="coef-tip">(1.0 最简单 / 5.0 最困难)</span>
              </div>
            </div>
          </el-form-item>

          <!-- 题型配置 (已固定，不显示) -->

          <div class="total-preview-area">
            <div class="total-info">
              <span class="preview-label">本次测试</span>
              <span class="preview-count">31</span>
              <span class="preview-unit">题</span>
              <span class="divider">|</span>
              <span class="score-label">满分</span>
              <span class="score-value">100</span>
              <span class="score-unit">分</span>
            </div>
          </div>

          <el-button type="primary" native-type="button" class="start-btn" @click.prevent="startExam" :loading="loading">
            <i class="el-icon-play-outline" v-if="!loading"></i>
            {{ loading ? '遗传算法组卷中...' : '立即开始测试' }}
          </el-button>
        </el-form>
      </div>
    </div>

    <!-- 2. 考试进行中 -->
    <div class="exam-paper" v-if="status === 'exam'">
      <div class="paper-layout">
        <!-- 左侧导航面板 -->
        <div class="nav-panel">
          <div class="nav-header">
            <span class="nav-title">答题卡</span>
            <el-button type="text" size="mini" @click="exitExam">
              <i class="el-icon-close"></i> 退出
            </el-button>
          </div>
          
          <div class="nav-types">
            <div class="type-group" v-if="multiQuestions.length > 0">
              <div class="type-title">
                <span class="type-badge multi">选</span>
                <span class="type-name">选择题</span>
              </div>
              <div class="type-grid">
                <div v-for="(q, idx) in multiQuestions" :key="'multi-'+idx" 
                     class="nav-item"
                     :class="{ 
                       active: currentIndex === getQuestionIndex('multi', idx),
                       answered: userAnswers[getQuestionId(q.questionId)] !== undefined && userAnswers[getQuestionId(q.questionId)] !== ''
                     }"
                     @click="goToQuestion(getQuestionIndex('multi', idx))">
                  {{ idx + 1 }}
                </div>
              </div>
            </div>
            
            <div class="type-group" v-if="fillQuestions.length > 0">
              <div class="type-title">
                <span class="type-badge fill">填</span>
                <span class="type-name">填空题</span>
              </div>
              <div class="type-grid">
                <div v-for="(q, idx) in fillQuestions" :key="'fill-'+idx" 
                     class="nav-item"
                     :class="{ 
                       active: currentIndex === getQuestionIndex('fill', idx),
                       answered: userAnswers[getQuestionId(q.questionId)] !== undefined && userAnswers[getQuestionId(q.questionId)] !== ''
                     }"
                     @click="goToQuestion(getQuestionIndex('fill', idx))">
                  {{ idx + 1 }}
                </div>
              </div>
            </div>
            
            <div class="type-group" v-if="judgeQuestions.length > 0">
              <div class="type-title">
                <span class="type-badge judge">判</span>
                <span class="type-name">判断题</span>
              </div>
              <div class="type-grid">
                <div v-for="(q, idx) in judgeQuestions" :key="'judge-'+idx" 
                     class="nav-item"
                     :class="{ 
                       active: currentIndex === getQuestionIndex('judge', idx),
                       answered: userAnswers[getQuestionId(q.questionId)] !== undefined && userAnswers[getQuestionId(q.questionId)] !== ''
                     }"
                     @click="goToQuestion(getQuestionIndex('judge', idx))">
                  {{ idx + 1 }}
                </div>
              </div>
            </div>
            
            <div class="type-group" v-if="subjectiveQuestions.length > 0">
              <div class="type-title">
                <span class="type-badge subjective">主</span>
                <span class="type-name">主观题</span>
              </div>
              <div class="type-grid">
                <div v-for="(q, idx) in subjectiveQuestions" :key="'subjective-'+idx" 
                     class="nav-item"
                     :class="{ 
                       active: currentIndex === getQuestionIndex('subjective', idx),
                       answered: userAnswers[getQuestionId(q.questionId)] !== undefined && userAnswers[getQuestionId(q.questionId)] !== ''
                     }"
                     @click="goToQuestion(getQuestionIndex('subjective', idx))">
                  {{ idx + 1 }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="nav-footer">
            <div class="nav-stats">
              <span class="answered-count">已答：<strong>{{ answeredCount }}</strong></span>
              <span class="unanswered-count">未答：<strong>{{ allQuestions.length - answeredCount }}</strong></span>
            </div>
          </div>
        </div>

        <!-- 右侧答题区域 -->
        <div class="answer-area">
          <div class="answer-header">
            <div class="header-left">
              <span class="paper-title">自主测试 [{{ config.subject }}]</span>
              <el-tag size="small" type="info">第 {{ currentIndex + 1 }} / {{ allQuestions.length }} 题</el-tag>
            </div>
            <div class="header-right">
              <div class="timer" :class="{ warning: timeLeft <= 300 }">
                <i class="el-icon-time"></i>
                <span>{{ formatTime(timeLeft) }}</span>
              </div>
              <el-button type="success" size="small" @click="submitExam">交卷</el-button>
            </div>
          </div>

          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: ((currentIndex + 1) / allQuestions.length * 100) + '%' }"></div>
          </div>

          <div class="question-card">
            <div class="q-header">
              <div class="q-info">
                <el-tag :type="getTypeTagType(currentQuestion.type)" size="medium">
                  {{ getTypeName(currentQuestion.type) }}
                </el-tag>
                <span class="q-score">{{ currentQuestion.score }}分</span>
              </div>
            </div>

            <div class="q-content">
              <p class="q-text">{{ currentQuestion.question }}</p>

              <!-- 选择题选项 -->
              <div class="q-options" v-if="currentQuestion.type === 1">
                <div v-for="opt in ['A', 'B', 'C', 'D']" :key="opt" 
                     class="option-item"
                     :class="{ selected: userAnswers[currentQuestion.questionId] === opt }"
                     @click="selectOption(opt)">
                  <span class="option-label">{{ opt }}</span>
                  <span class="option-text">{{ currentQuestion['answer' + opt] }}</span>
                </div>
              </div>

              <!-- 填空题输入 -->
              <div class="q-input" v-if="currentQuestion.type === 2">
                <el-input 
                  v-model="userAnswers[currentQuestion.questionId]" 
                  placeholder="请输入答案，多个答案用逗号分隔"
                  size="large"
                />
              </div>

              <!-- 判断题选项 -->
              <div class="q-options judge-options" v-if="currentQuestion.type === 3">
                <div class="option-item"
                     :class="{ selected: userAnswers[currentQuestion.questionId] === 'T' }"
                     @click="selectOption('T')">
                  <span class="option-label">T</span>
                  <span class="option-text">正确</span>
                </div>
                <div class="option-item"
                     :class="{ selected: userAnswers[currentQuestion.questionId] === 'F' }"
                     @click="selectOption('F')">
                  <span class="option-label">F</span>
                  <span class="option-text">错误</span>
                </div>
              </div>

              <!-- 主观题输入 -->
              <div class="q-subjective" v-if="currentQuestion.type === 4">
                <el-input 
                  type="textarea"
                  v-model="userAnswers[currentQuestion.questionId]"
                  :rows="8"
                  placeholder="请输入您的答案（交卷后系统将自动评分）"
                />
                <div class="subjective-tip">
                  <i class="el-icon-cpu"></i>
                  主观题将在交卷后由算法自动评分，分数计入总分
                </div>
              </div>
            </div>

            <div class="q-actions">
              <el-button @click="prevQuestion" :disabled="currentIndex === 0" icon="el-icon-arrow-left">上一题</el-button>
              <el-button type="primary" @click="nextQuestion" v-if="currentIndex < allQuestions.length - 1">
                下一题 <i class="el-icon-arrow-right"></i>
              </el-button>
              <el-button type="success" @click="submitExam" v-if="currentIndex === allQuestions.length - 1">
                <i class="el-icon-check"></i> 提交试卷
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 考试结果分析 -->
    <div class="exam-result" v-if="status === 'result'">
      <div class="result-summary">
        <div class="score-circle">
          <span class="score-num">{{ result.earnedScore }}</span>
          <span class="score-total">/ {{ result.totalScore }}</span>
        </div>
        <div class="result-meta">
          <p class="accuracy">正确率：<strong>{{ result.accuracy }}%</strong></p>
          <p class="correct-count">答对：{{ result.correctCount }} / {{ result.totalQuestions }}</p>
          <el-button type="primary" plain @click="restart">再测一次</el-button>
        </div>
      </div>

      <div class="result-details">
        <h3 class="details-title">答卷解析</h3>
        <div v-for="(res, index) in result.results" :key="index" class="result-card" :class="{ 'is-wrong': !res.correct }">
          <div class="res-title">
            <i class="el-icon-check" v-if="res.correct" style="color: #67C23A"></i>
            <i class="el-icon-close" v-else style="color: #F56C6C"></i>
            <span class="res-q">{{ res.question }}</span>
          </div>
          
          <div class="res-info">
            <div class="res-item">
              <span class="label">你的答案：</span>
              <span class="value" :class="res.correct ? 'green' : 'red'">{{ res.userAnswer || '未作答' }}</span>
            </div>
            <!-- 主观题显示算法评分 -->
            <div class="res-item" v-if="res.type === 4">
              <span class="label">系统评分：</span>
              <span class="value" :class="res.correct ? 'green' : 'red'">{{ res.subjectiveScore !== undefined ? res.subjectiveScore : 0 }} / {{ res.score }} 分</span>
            </div>
            <div class="res-item" v-if="res.type === 4 && res.correctAnswer">
              <span class="label">参考答案：</span>
              <span class="value green">{{ res.correctAnswer }}</span>
            </div>
            <div class="res-item" v-if="res.type !== 4 && !res.correct">
              <span class="label">正确答案：</span>
              <span class="value green">{{ res.correctAnswer }}</span>
            </div>
          </div>

          <!-- 解析区域 -->
          <div class="res-analysis" v-if="res.analysis">
            <div class="analysis-title"><i class="el-icon-reading"></i> 解析</div>
            <div class="analysis-content">{{ res.analysis }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  data() {
    return {
      status: 'config', // config, exam, result
      loading: false,
      quickMode: 'normal',
      config: {
        subject: '',
        targetDifficulty: 3,
        multiCount: 15,
        fillCount: 4,
        judgeCount: 10,
        subjectiveCount: 2
      },
      subjectOptions: [],
      difficultyMarks: {
        1: { style: { whiteSpace: 'nowrap' }, label: '简单' },
        2: { style: { whiteSpace: 'nowrap' }, label: '较易' },
        3: { style: { whiteSpace: 'nowrap' }, label: '中等' },
        4: { style: { whiteSpace: 'nowrap' }, label: '较难' },
        5: { style: { whiteSpace: 'nowrap' }, label: '困难' }
      },
      paper: null,
      timeLeft: 0,
      timer: null,
      userAnswers: {}, // key: questionId, value: answer
      result: null,
      
      // 题目分类存储
      multiQuestions: [],
      fillQuestions: [],
      judgeQuestions: [],
      subjectiveQuestions: [],
      allQuestions: [],
      currentIndex: 0
    }
  },
  computed: {
    ...mapState(['userInfo']),
    totalCount() {
      return this.config.multiCount + this.config.fillCount + this.config.judgeCount + this.config.subjectiveCount
    },
    expectedTotalScore() {
      return this.config.multiCount * 5 +
             this.config.fillCount * 5 +
             this.config.judgeCount * 5 +
             this.config.subjectiveCount * 10
    },
    difficultyCoefficient() {
      return this.config.targetDifficulty
    },
    answeredCount() {
      return Object.keys(this.userAnswers).filter(key => {
        const val = this.userAnswers[key]
        return val !== undefined && val !== ''
      }).length
    },
    currentQuestion() {
      return this.allQuestions[this.currentIndex] || {}
    }
  },
  created() {
    this.fetchSubjects()
    const paperId = this.$route.query.paperId
    if (paperId) {
      this.loadRedoPaper(paperId)
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async loadRedoPaper(paperId) {
      this.loading = true
      try {
        let res = await this.$axios.get(`/api/study/redo/${paperId}`)
        if (res.data.code === 200) {
          this.paper = res.data.data
          if (!this.paper.totalQuestions) {
            this.paper.totalQuestions = this.paper.questions.length
          }
          this.timeLeft = this.paper.timeLimit * 60
          this.userAnswers = {}
          this.paper.questions.forEach(q => {
            this.$set(this.userAnswers, q.questionId, '')
          })
          if(this.paper.questions.length > 0) {
            this.config.subject = this.paper.questions[0].subject
          }
          
          this.status = 'exam'
          this.startTimer()
          this.$message.success('已加载历史试卷，请开始答题')
        } else {
          this.$message.error(res.data.message || '加载试卷失败')
        }
      } catch (err) {
        console.error(err)
        this.$message.error('加载试卷失败')
      } finally {
        this.loading = false
      }
    },
    
    async fetchSubjects() {
      try {
        let res = await this.$axios.get('/api/genetic/subjects')
        if (res.data.code === 200) {
          this.subjectOptions = res.data.data
        }
      } catch (err) {
        console.error('获取科目失败', err)
      }
    },
    
    setQuickMode(mode) {
      this.quickMode = mode
      const presets = {
        easy: { multiCount: 5, fillCount: 3, judgeCount: 2, subjectiveCount: 0, targetDifficulty: 2 },
        normal: { multiCount: 8, fillCount: 4, judgeCount: 3, subjectiveCount: 0, targetDifficulty: 3 },
        hard: { multiCount: 10, fillCount: 5, judgeCount: 4, subjectiveCount: 1, targetDifficulty: 4 },
        custom: {}
      }
      const preset = presets[mode]
      if (preset.multiCount !== undefined) {
        this.config.multiCount = preset.multiCount
        this.config.fillCount = preset.fillCount
        this.config.judgeCount = preset.judgeCount
        this.config.subjectiveCount = preset.subjectiveCount
        this.config.targetDifficulty = preset.targetDifficulty
      }
    },
    
    async startExam() {
      if (!this.config.subject) {
        this.$message.warning('请选择考试科目')
        return
      }
      
      this.loading = true
      try {
        let res = await this.$axios.post('/api/genetic/generate', {
          subjects: [this.config.subject],
          targetDifficulty: this.config.targetDifficulty,
          difficultyTolerance: 0.5,
          multiCount: 15,
          fillCount: 4,
          judgeCount: 10,
          subjectiveCount: 2,
          multiScore: 2,
          fillScore: 5,
          judgeScore: 2,
          subjectiveScore: 15,
          populationSize: 50,
          maxGenerations: 100,
          studentId: parseInt((this.userInfo && this.userInfo.id) || this.$cookies.get('cid') || 0)
        })
        
        if (res.data.code === 200) {
          // 题库不足时显示警告但继续组卷
          if (res.data.data && res.data.data.warning) {
            this.$notify({ title: '题库提示', message: res.data.data.warning, type: 'warning', duration: 4000 })
          }
          this.paper = res.data.data
          this.processPaperData()
          if (this.allQuestions.length === 0) {
            this.$alert('该科目题库暂无题目，请在题库管理中添加题目后再试。', '题库为空', { type: 'warning' })
            return
          }
          // 固定时间 60 分钟
          this.timeLeft = 60 * 60
          this.userAnswers = {}
          this.allQuestions.forEach(q => {
            this.$set(this.userAnswers, q.questionId, '')
          })
          
          this.status = 'exam'
          this.currentIndex = 0
          this.startTimer()
        } else {
          this.$alert(res.data.message || '生成试卷失败', '组卷失败', { type: 'error' })
        }
      } catch (err) {
        const msg = err && err.message ? err.message : ''
        if (msg.includes('Network') || msg.includes('network') || err.code === 'ECONNREFUSED') {
          this.$alert('无法连接到服务器，请确认后端服务已启动', '连接失败', { type: 'error' })
        } else {
          this.$alert('生成试卷失败，请稍后重试', '错误', { type: 'error' })
        }
        console.error('[startExam error]', err)
      } finally {
        this.loading = false
      }
    },
    
    processPaperData() {
      this.multiQuestions = []
      this.fillQuestions = []
      this.judgeQuestions = []
      this.subjectiveQuestions = []
      this.allQuestions = []
      
      if (this.paper.multiQuestions) {
        this.paper.multiQuestions.forEach(q => {
          q.type = 1
          this.multiQuestions.push(q)
          this.allQuestions.push(q)
        })
      }
      if (this.paper.fillQuestions) {
        this.paper.fillQuestions.forEach(q => {
          q.type = 2
          this.fillQuestions.push(q)
          this.allQuestions.push(q)
        })
      }
      if (this.paper.judgeQuestions) {
        this.paper.judgeQuestions.forEach(q => {
          q.type = 3
          this.judgeQuestions.push(q)
          this.allQuestions.push(q)
        })
      }
      if (this.paper.subjectiveQuestions) {
        this.paper.subjectiveQuestions.forEach(q => {
          q.type = 4
          this.subjectiveQuestions.push(q)
          this.allQuestions.push(q)
        })
      }
    },
    
    getQuestionId(baseId) {
      return baseId
    },
    
    getQuestionIndex(type, idx) {
      if (type === 'multi') return idx
      if (type === 'fill') return this.multiQuestions.length + idx
      if (type === 'judge') return this.multiQuestions.length + this.fillQuestions.length + idx
      if (type === 'subjective') return this.multiQuestions.length + this.fillQuestions.length + this.judgeQuestions.length + idx
      return 0
    },
    
    goToQuestion(index) {
      this.currentIndex = index
    },
    
    prevQuestion() {
      if (this.currentIndex > 0) {
        this.currentIndex--
      }
    },
    
    nextQuestion() {
      if (this.currentIndex < this.allQuestions.length - 1) {
        this.currentIndex++
      }
    },
    
    selectOption(opt) {
      this.$set(this.userAnswers, this.currentQuestion.questionId, opt)
    },
    
    startTimer() {
      this.timer = setInterval(() => {
        if (this.timeLeft > 0) {
          this.timeLeft--
        } else {
          this.submitExam()
        }
      }, 1000)
    },
    
    formatTime(seconds) {
      const m = Math.floor(seconds / 60)
      const s = seconds % 60
      return `${m < 10 ? '0' + m : m}:${s < 10 ? '0' + s : s}`
    },
    
    async submitExam() {
      if (this.timer) clearInterval(this.timer)
      this.loading = true
      
      const answersList = this.allQuestions.map(q => ({
        questionId: q.questionId,
        type: q.type,
        answer: this.userAnswers[q.questionId],
        score: q.score
      }))

      try {
        let studentId = (this.userInfo && this.userInfo.id) ? this.userInfo.id : this.$cookies.get('cid')
        if (studentId) studentId = parseInt(studentId)
        let res = await this.$axios.post('/api/study/mock/grade', {
          studentId: studentId,
          subject: this.config.subject,
          answers: answersList
        })

        if (res.data.code === 200) {
          this.result = res.data.data
          this.status = 'result'
          window.scrollTo(0, 0)
        } else {
          this.$message.error(res.data.message || '交卷失败')
        }
      } catch (err) {
        this.$message.error('交卷发生错误：' + (err.message || '未知错误'))
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    
    exitExam() {
      this.$confirm('确定要退出测试吗？', '提示', {
        type: 'warning'
      }).then(() => {
        if (this.timer) clearInterval(this.timer)
        this.status = 'config'
        this.paper = null
        this.allQuestions = []
        this.userAnswers = {}
      }).catch(() => {})
    },
    
    restart() {
      if(this.$route.query.paperId) {
        this.$router.replace('/mockExam')
      }
      this.status = 'config'
      this.paper = null
      this.result = null
      this.userAnswers = {}
      this.multiQuestions = []
      this.fillQuestions = []
      this.judgeQuestions = []
      this.subjectiveQuestions = []
      this.allQuestions = []
      this.currentIndex = 0
    },
    
    getTypeName(type) {
      const types = { 1: '选择题', 2: '填空题', 3: '判断题', 4: '主观题' }
      return types[type] || '未知'
    },
    
    getTypeTagType(type) {
      const types = { 1: '', 2: 'success', 3: 'warning', 4: 'danger' }
      return types[type] || 'info'
    }
  }
}
</script>

<style scoped lang="less">
.mock-exam-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

/* Config Card */
.config-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.config-header {
  text-align: center;
  margin-bottom: 40px;
  
  i {
    font-size: 64px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 16px;
  }
  
  h2 {
    font-size: 28px;
    color: #1a1a2e;
    font-weight: 800;
    margin: 12px 0;
  }
  
  p {
    color: #6b7280;
    font-size: 15px;
  }
}

/* Quick Mode */
.quick-mode {
  margin-bottom: 30px;
  
  h3 {
    font-size: 16px;
    color: #2d3436;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
    
    i {
      color: #667eea;
    }
  }
}

.mode-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.mode-card {
  padding: 20px;
  border: 2px solid #e8ecef;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    transform: translateY(-2px);
  }
  
  &.active {
    border-color: #667eea;
    background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  }
}

.mode-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 28px;
  color: #fff;
  
  &.easy { background: linear-gradient(135deg, #74b9ff, #0984e3); }
  &.normal { background: linear-gradient(135deg, #667eea, #764ba2); }
  &.hard { background: linear-gradient(135deg, #fd79a8, #e84393); }
  &.custom { background: linear-gradient(135deg, #a29bfe, #6c5ce7); }
}

.mode-name {
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 4px;
}

.mode-desc {
  font-size: 12px;
  color: #b2bec3;
}

/* Difficulty Slider */
.difficulty-slider-wrapper {
  padding: 10px 20px 40px;
}

.difficulty-slider {
  width: 100%;
}

::v-deep .difficulty-slider .el-slider__bar {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

::v-deep .difficulty-slider .el-slider__button {
  border-color: #667eea;
}

.difficulty-coefficient {
  margin-top: 16px;
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8ecef 100%);
  border-radius: 12px;
}

.coef-label {
  font-size: 14px;
  color: #636e72;
  margin-right: 8px;
}

.coef-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin: 0 8px;
}

.coef-tip {
  font-size: 12px;
  color: #909399;
}

/* Type Config Fixed */
.type-config-fixed {
  margin-bottom: 28px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 16px;
  border: 1px solid #e8ecef;
}

.type-fixed-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.type-fixed-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 10px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.type-count {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.type-score {
  font-size: 12px;
  color: #909399;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.type-fixed-tip {
  font-size: 13px;
  color: #909399;
  text-align: center;
  
  i { margin-right: 4px; color: #409eff; }
}

/* Type Config */
.type-config-section {
  margin: 30px 0;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 16px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.type-badge {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  
  &.multi { background: #74b9ff; }
  &.fill { background: #55efc4; }
  &.judge { background: #ffeaa7; color: #2d3436; }
  &.subjective { background: #fab1a0; }
}

.type-label {
  font-size: 14px;
  color: #2d3436;
  min-width: 60px;
}

/* Total Preview */
.total-preview-area {
  margin: 30px 0;
  padding: 20px;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  border-radius: 16px;
  text-align: center;
}

.total-info {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}

.preview-label, .score-label {
  color: #636e72;
  font-size: 14px;
}

.preview-count {
  font-size: 36px;
  font-weight: 700;
  color: #667eea;
}

.preview-unit, .score-unit {
  color: #636e72;
  font-size: 14px;
}

.divider {
  color: #b2bec3;
  margin: 0 8px;
}

.score-value {
  font-size: 28px;
  font-weight: 700;
  color: #667eea;
}

.start-btn {
  width: 100%;
  height: 56px;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.35);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.45);
  }
}

/* Paper Layout */
.paper-layout {
  display: flex;
  gap: 20px;
}

/* Nav Panel */
.nav-panel {
  width: 260px;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8ecef;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3436;
}

.nav-types {
  margin-bottom: 20px;
}

.type-group {
  margin-bottom: 20px;
}

.type-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #636e72;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.nav-item {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #636e72;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #e0e7ff;
  }
  
  &.active {
    background: #667eea;
    color: #fff;
  }
  
  &.answered {
    background: #818cf8;
    color: #fff;
  }
}

.nav-footer {
  padding-top: 16px;
  border-top: 1px solid #e8ecef;
}

.nav-stats {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.answered-count, .unanswered-count {
  color: #636e72;
  
  strong {
    color: #667eea;
    font-size: 16px;
  }
}

/* Answer Area */
.answer-area {
  flex: 1;
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.paper-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3436;
}

.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 800;
  color: #ef4444;
  margin-right: 20px;
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 12px;
  font-family: 'Monaco', monospace;
  
  &.warning {
    animation: pulse 1s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.progress-bar {
  height: 6px;
  background: #e8ecef;
  border-radius: 3px;
  margin-bottom: 30px;
  overflow: hidden;
  
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    transition: width 0.3s ease;
  }
}

/* Question Card */
.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.q-header {
  margin-bottom: 20px;
}

.q-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.q-score {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #2d3436;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.q-text {
  font-size: 17px;
  line-height: 1.8;
  color: #2d3436;
  margin-bottom: 30px;
}

/* Options */
.q-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border: 2px solid #e8ecef;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: #667eea;
    background: #e0e7ff;
  }
  
  &.selected {
    border-color: #667eea;
    background: #e0e7ff;
  }
}

.option-label {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #636e72;
  flex-shrink: 0;
}

.option-item.selected .option-label {
  background: #667eea;
  color: #fff;
}

.judge-options {
  flex-direction: row;
  
  .option-item {
    flex: 1;
    justify-content: center;
  }
}

/* Input */
.q-input, .q-subjective {
  margin-bottom: 20px;
}

.subjective-tip {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 13px;
  color: #636e72;
  display: flex;
  align-items: center;
  gap: 8px;
  
  i {
    color: #667eea;
  }
}

/* Actions */
.q-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e8ecef;
}

/* Results */
.result-summary {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px;
  margin-bottom: 30px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.1);
}

.score-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 40px rgba(118, 75, 162, 0.4);
}

.score-num {
  font-size: 48px;
  font-weight: 800;
}

.result-meta {
  p {
    margin: 8px 0;
    color: #636e72;
  }
  
  .accuracy {
    font-size: 16px;
    
    strong {
      color: #667eea;
      font-size: 20px;
    }
  }
}

.details-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3436;
  margin: 30px 0 20px;
}

.result-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  border-left: 6px solid #67C23A;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  
  &.is-wrong {
    border-left-color: #F56C6C;
  }
}

.res-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.res-q {
  font-size: 15px;
  color: #2d3436;
}

.res-info {
  .res-item {
    margin-bottom: 8px;
    font-size: 14px;
    
    .label {
      color: #636e72;
      margin-right: 8px;
    }
    
    .value {
      font-weight: 600;
      
      &.green {
        color: #67C23A;
      }
      
      &.red {
        color: #F56C6C;
      }
    }
  }
}

.res-analysis {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}

.analysis-title {
  color: #4a5568;
  font-weight: 700;
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
