<template>
  <div id="smartPractice" class="page-container">
    <!-- 加载中 -->
    <div class="loading-container" v-if="loading">
      <i class="el-icon-loading"></i>
      <p>正在加载试卷...</p>
    </div>

    <!-- 答题区域 -->
    <div class="exam-section" v-if="!loading && allQuestions.length > 0">
      <!-- 答题导航 -->
      <div class="exam-nav">
        <div class="nav-header">
          <div class="nav-title">
            <span class="nav-title-icon">&#9889;</span>
            <span>智能专项练习</span>
          </div>
          <el-button class="exit-btn" size="mini" @click="exitExam">
            <i class="el-icon-close"></i> 退出
          </el-button>
        </div>
        <div class="nav-progress-wrap">
          <div class="nav-progress-bar" :style="{ width: (answeredCount / allQuestions.length * 100) + '%' }"></div>
        </div>
        <div class="nav-grid">
          <div v-for="(q, index) in allQuestions" :key="index" class="nav-item"
               :class="{ active: currentIndex === index, answered: answers[index] !== undefined && answers[index] !== '', wrong: showResult && isWrong(index), correct: showResult && !isWrong(index) && answers[index] !== undefined }"
               @click="goToQuestion(index)">
            {{ index + 1 }}
          </div>
        </div>
        <div class="nav-footer">
          <div class="nav-answered-info">
            <span class="answered-num">{{ answeredCount }}</span>
            <span class="answered-sep">/</span>
            <span class="total-num">{{ allQuestions.length }}</span>
            <span class="answered-label">&nbsp;已作答</span>
          </div>
          <div class="nav-legend">
            <span class="legend-dot dot-active"></span><span class="legend-text">当前</span>
            <span class="legend-dot dot-answered"></span><span class="legend-text">已答</span>
            <span class="legend-dot dot-none"></span><span class="legend-text">未答</span>
          </div>
        </div>
      </div>

      <!-- 题目区域 -->
      <div class="question-area">
        <div class="question-header">
          <div class="question-info">
            <el-tag :type="getTypeTagType(currentQuestion.type)" size="medium">{{ getTypeName(currentQuestion.type) }}</el-tag>
            <span class="question-num">第 {{ currentIndex + 1 }} 题 / 共 {{ allQuestions.length }} 题</span>
          </div>
          <div class="question-score">{{ currentQuestion.score || 5 }}分</div>
        </div>

        <div class="question-content">
          <p class="question-text">{{ currentQuestion.question }}</p>

          <!-- 选择题选项 -->
          <div class="options" v-if="currentQuestion.type === 1">
            <div v-for="opt in ['A', 'B', 'C', 'D']" :key="opt" class="option-item"
                 :class="{ selected: answers[currentIndex] === opt, correct: showResult && currentQuestion.rightAnswer === opt, wrong: showResult && answers[currentIndex] === opt && currentQuestion.rightAnswer !== opt }"
                 @click="selectOption(opt)">
              <span class="option-label">{{ opt }}</span>
              <span class="option-text">{{ currentQuestion['answer' + opt] }}</span>
            </div>
          </div>

          <!-- 判断题选项 -->
          <div class="options judge-options" v-if="currentQuestion.type === 3">
            <div class="option-item" :class="{ selected: answers[currentIndex] === 'T', correct: showResult && currentQuestion.answer === 'T', wrong: showResult && answers[currentIndex] === 'T' && currentQuestion.answer !== 'T' }" @click="selectOption('T')">
              <span class="option-label">T</span>
              <span class="option-text">正确</span>
            </div>
            <div class="option-item" :class="{ selected: answers[currentIndex] === 'F', correct: showResult && currentQuestion.answer === 'F', wrong: showResult && answers[currentIndex] === 'F' && currentQuestion.answer !== 'F' }" @click="selectOption('F')">
              <span class="option-label">F</span>
              <span class="option-text">错误</span>
            </div>
          </div>

          <!-- 填空题输入 -->
          <div class="fill-input" v-if="currentQuestion.type === 2">
            <el-input v-model="answers[currentIndex]" placeholder="请输入答案" :disabled="showResult" />
          </div>

          <!-- 主观题输入 -->
          <div class="subjective-input" v-if="currentQuestion.type === 4">
            <el-input type="textarea" v-model="answers[currentIndex]" :rows="6" placeholder="请输入您的答案" :disabled="showResult" />
            <div class="reference-answer" v-if="showResult">
              <strong>参考答案：</strong>
              <p>{{ currentQuestion.referenceAnswer || currentQuestion.answer }}</p>
            </div>
          </div>
        </div>

        <!-- 答案解析 -->
        <div class="answer-section" v-if="showResult && currentQuestion.type !== 4">
          <div class="correct-answer">
            <strong>正确答案：</strong>
            <span class="answer-text">{{ getCorrectAnswer(currentQuestion) }}</span>
          </div>
          <div class="analysis" v-if="currentQuestion.analysis">
            <strong>解析：</strong>
            <p>{{ currentQuestion.analysis }}</p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="question-actions">
          <el-button @click="prevQuestion" :disabled="currentIndex === 0" icon="el-icon-arrow-left">上一题</el-button>
          <el-button type="warning" @click="checkAnswer" v-if="!showResult">查看答案</el-button>
          <el-button type="primary" @click="nextQuestion" v-if="currentIndex < allQuestions.length - 1">下一题 <i class="el-icon-arrow-right"></i></el-button>
          <el-button type="success" @click="finishExam" v-if="currentIndex === allQuestions.length - 1">完成练习</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 - 显示历史记录 -->
    <div class="history-section" v-if="!loading && allQuestions.length === 0">
      <div class="history-header">
        <h2><i class="el-icon-time"></i> 专项练习记录</h2>
        <p>点击历史记录可以重新做一遍</p>
      </div>

      <div class="history-list" v-if="historyList.length > 0">
        <div class="history-item" v-for="item in historyList" :key="item.id" @click="retryPractice(item)">
          <div class="history-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="history-info">
            <div class="history-title">{{ item.subjects || '智能专项练习' }}</div>
            <div class="history-meta">
              <span><i class="el-icon-document"></i> {{ item.totalQuestions }}题</span>
              <span><i class="el-icon-star-on"></i> 难度{{ item.targetDifficulty }}</span>
              <span><i class="el-icon-aim"></i> 匹配{{ item.fitness }}%</span>
            </div>
          </div>
          <div class="history-time">{{ formatTime(item.createTime) }}</div>
          <div class="history-action">
            <el-button type="primary" size="small" icon="el-icon-refresh-right">重做</el-button>
          </div>
        </div>
      </div>

      <div class="empty-history" v-else>
        <i class="el-icon-folder-opened"></i>
        <p>暂无专项练习记录</p>
        <p class="sub">去「智能分析」开始专项练习吧</p>
        <el-button type="primary" @click="goToWeakPoints">去分析</el-button>
      </div>
    </div>

    <!-- 结果对话框 -->
    <el-dialog :visible.sync="resultVisible" title="练习结果" width="400px" center>
      <div class="result-content">
        <div class="score-circle">
          <span class="score-value">{{ score }}</span>
          <span class="score-total">/ {{ totalScore }}</span>
        </div>
        <div class="result-stats">
          <div class="result-item">
            <span class="item-value correct-color">{{ correctCount }}</span>
            <span class="item-label">正确</span>
          </div>
          <div class="result-item">
            <span class="item-value wrong-color">{{ wrongCount }}</span>
            <span class="item-label">错误</span>
          </div>
          <div class="result-item">
            <span class="item-value">{{ accuracy }}%</span>
            <span class="item-label">正确率</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewExam">查看解析</el-button>
        <el-button type="primary" @click="goBack">返回</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SmartPractice',
  data() {
    return {
      loading: true,
      paperData: null,
      paperStats: null,
      allQuestions: [],
      answers: {},
      currentIndex: 0,
      showResult: false,
      resultVisible: false,
      score: 0,
      totalScore: 0,
      correctCount: 0,
      wrongCount: 0,
      studentId: null,
      historyList: []  // 历史记录
    }
  },
  computed: {
    currentQuestion() {
      return this.allQuestions[this.currentIndex] || {}
    },
    answeredCount() {
      return Object.keys(this.answers).filter(k => this.answers[k] !== undefined && this.answers[k] !== '').length
    },
    accuracy() {
      if (this.correctCount + this.wrongCount === 0) return 0
      return Math.round(this.correctCount / (this.correctCount + this.wrongCount) * 100)
    }
  },
  created() {
    this.studentId = this.$cookies.get('cid')
    this.loadPaper()
    this.loadHistory()
  },
  methods: {
    loadPaper() {
      this.loading = true
      try {
        var data = sessionStorage.getItem('smartPractice')
        if (data) {
          this.paperData = JSON.parse(data)
          this.paperStats = this.paperData.stats
          this.buildQuestionList()
        }
      } catch (e) {
        console.error('解析试卷数据失败', e)
      }
      this.loading = false
    },
    buildQuestionList() {
      var questions = []
      var data = this.paperData

      // 选择题
      if (data.multiQuestions) {
        data.multiQuestions.forEach(function(q) {
          q.type = 1
          questions.push(q)
        })
      }
      // 填空题
      if (data.fillQuestions) {
        data.fillQuestions.forEach(function(q) {
          q.type = 2
          questions.push(q)
        })
      }
      // 判断题
      if (data.judgeQuestions) {
        data.judgeQuestions.forEach(function(q) {
          q.type = 3
          questions.push(q)
        })
      }
      // 主观题
      if (data.subjectiveQuestions) {
        data.subjectiveQuestions.forEach(function(q) {
          q.type = 4
          questions.push(q)
        })
      }

      this.allQuestions = questions
      this.totalScore = this.paperStats ? this.paperStats.totalScore : 0
    },
    getTypeName(type) {
      var names = { 1: '选择题', 2: '填空题', 3: '判断题', 4: '主观题' }
      return names[type] || '未知'
    },
    getTypeTagType(type) {
      var types = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' }
      return types[type] || 'info'
    },
    selectOption(opt) {
      if (this.showResult) return
      this.$set(this.answers, this.currentIndex, opt)
    },
    goToQuestion(index) {
      this.currentIndex = index
      this.showResult = false
    },
    prevQuestion() {
      if (this.currentIndex > 0) {
        this.currentIndex--
        this.showResult = false
      }
    },
    nextQuestion() {
      if (this.currentIndex < this.allQuestions.length - 1) {
        this.currentIndex++
        this.showResult = false
      }
    },
    checkAnswer() {
      this.showResult = true
      this.recordWrongQuestion()
    },
    getCorrectAnswer(q) {
      if (q.type === 1) return q.rightAnswer
      if (q.type === 2) return q.answer
      if (q.type === 3) return q.answer === 'T' ? '正确' : '错误'
      return q.referenceAnswer || q.answer
    },
    isWrong(index) {
      var q = this.allQuestions[index]
      var ans = this.answers[index]
      if (ans === undefined || ans === '') return false
      if (q.type === 1) return ans !== q.rightAnswer
      if (q.type === 2) return ans.trim() !== q.answer.trim()
      if (q.type === 3) return ans !== q.answer
      return false
    },
    recordWrongQuestion() {
      var q = this.currentQuestion
      var ans = this.answers[this.currentIndex]
      if (!ans) return // 不答也记为错题

      var isWrong = this.isWrong(this.currentIndex)
      if (isWrong) {
        var self = this
        this.$axios.post('/api/study/wrong', {
          studentId: parseInt(this.studentId),
          questionType: q.type,
          questionId: q.questionId,
          questionContent: q.question,
          correctAnswer: this.getCorrectAnswer(q),
          wrongAnswer: ans,
          subject: q.subject
        }).catch(function(e) {
          console.error('记录错题失败', e)
        })
      }
    },
    async finishExam() {
      var self = this
      this.correctCount = 0
      this.wrongCount = 0
      this.score = 0

      // 先处理客观题评分
      this.allQuestions.forEach(function(q, index) {
        var ans = self.answers[index]
        if (ans === undefined || ans === '') return

        if (q.type !== 4) {
          if (!self.isWrong(index)) {
            self.correctCount++
            self.score += q.score || 5
          } else {
            self.wrongCount++
          }
        }
      })

      // 主观题调用后端算法自动评分（而非直接给满分）
      var subjectiveQuestions = this.allQuestions.filter(function(q) { return q.type === 4 })
      if (subjectiveQuestions.length > 0) {
        var scorePromises = subjectiveQuestions.map(function(q) {
          var idx = self.allQuestions.indexOf(q)
          var ans = self.answers[idx] || ''
          if (!ans.trim()) return Promise.resolve(0)
          return self.$axios.post('/api/scoring/auto/compute', {
            studentAnswer: ans,
            referenceAnswer: q.referenceAnswer || q.answer || '',
            maxScore: q.score || 10
          }).then(function(res) {
            if (res.data.code === 200) return res.data.data.score || 0
            return 0
          }).catch(function() { return 0 })
        })
        try {
          var subScores = await Promise.all(scorePromises)
          var subTotal = subScores.reduce(function(a, b) { return a + b }, 0)
          self.score += subTotal
          // 主观题得分达到60%以上计为正确
          subjectiveQuestions.forEach(function(q, i) {
            var maxS = q.score || 10
            if (subScores[i] >= Math.ceil(maxS * 0.6)) self.correctCount++
            else self.wrongCount++
          })
        } catch (e) {
          console.error('主观题评分失败:', e)
        }
      }
      
      // ========== 记录错题到错题本 ==========
      var wrongPromises = []
      this.allQuestions.forEach(function(q, index) {
        var ans = self.answers[index]
        if (q.type !== 4 && (!ans || self.isWrong(index))) {
          // 客观题错题
          wrongPromises.push(self.$axios.post('/api/study/wrong', {
            studentId: parseInt(self.studentId),
            questionType: q.type,
            questionId: q.questionId,
            questionContent: q.question,
            correctAnswer: self.getCorrectAnswer(q),
            wrongAnswer: ans || '未作答',
            subject: q.subject
          }).catch(function(e) { console.error('错题 #' + (index+1) + ' 记录失败:', e) }))
        }
      })
      Promise.all(wrongPromises).then(function() {
        console.log('[SmartPractice] 错题记录完成')
      })

      // 记录学习记录
      this.$axios.post('/api/study/record', {
        studentId: parseInt(this.studentId),
        totalQuestions: this.allQuestions.length,
        correctCount: this.correctCount,
        wrongCount: this.wrongCount,
        accuracy: this.accuracy,
        subject: '智能专项练习',
        score: this.score
      }).catch(function(e) {
        console.error('记录学习记录失败', e)
      })

      this.resultVisible = true
    },
    reviewExam() {
      this.resultVisible = false
      this.showResult = true
    },
    exitExam() {
      this.$confirm('确定要退出练习吗？', '提示', { type: 'warning' }).then(() => {
        this.goBack()
      }).catch(() => {})
    },
    goBack() {
      sessionStorage.removeItem('smartPractice')
      this.$router.push('/weakPoints')
    },
    goToWeakPoints() {
      this.$router.push('/weakPoints')
    },
    loadHistory() {
      var self = this
      this.$axios.get('/api/generation-records/genetic/1/20')
        .then(function(res) {
          if (res.data.code === 200 && res.data.data) {
            self.historyList = res.data.data.records || []
          }
        }).catch(function() {
          self.historyList = []
        })
    },
    formatTime(time) {
      if (!time) return ''
      var date = new Date(time)
      var month = date.getMonth() + 1
      var day = date.getDate()
      var hour = date.getHours()
      var min = date.getMinutes()
      return month + '/' + day + ' ' + (hour < 10 ? '0' : '') + hour + ':' + (min < 10 ? '0' : '') + min
    },
    retryPractice(item) {
      var self = this
      if (!item.paperJson) {
        this.$message.warning('记录数据不完整')
        return
      }
      try {
        var paperData = JSON.parse(item.paperJson)
        self.paperData = paperData
        self.paperStats = paperData.stats
        self.buildQuestionList()
        self.answers = {}
        self.currentIndex = 0
        self.showResult = false
        self.$message.success('已加载历史记录，开始练习！')
      } catch (e) {
        self.$message.error('加载记录失败')
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ee 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 25px 30px;
  color: #fff;
  margin-bottom: 25px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header p {
  margin: 0;
  opacity: 0.9;
}

.header-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
}

.loading-container, .empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
}

.loading-container i, .empty-state i {
  font-size: 48px;
  color: #667eea;
}

.exam-section {
  display: flex;
  gap: 25px;
}

.exam-nav {
  width: 220px;
  background: #fff;
  border-radius: 18px;
  padding: 0;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.13), 0 2px 8px rgba(0,0,0,0.07);
  height: fit-content;
  position: sticky;
  top: 20px;
  overflow: hidden;
  border: 1.5px solid rgba(102, 126, 234, 0.12);
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.nav-title-icon {
  font-size: 16px;
  filter: drop-shadow(0 0 4px rgba(255,255,255,0.6));
}

.exit-btn {
  background: rgba(255,255,255,0.18) !important;
  border: 1px solid rgba(255,255,255,0.35) !important;
  color: #fff !important;
  border-radius: 20px !important;
  padding: 4px 10px !important;
  font-size: 12px !important;
  transition: all 0.25s !important;
}
.exit-btn:hover {
  background: rgba(255,255,255,0.3) !important;
}

.nav-progress-wrap {
  height: 4px;
  background: #eef0f7;
  border-radius: 0;
}
.nav-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #67C23A, #a8e063);
  border-radius: 0 2px 2px 0;
  transition: width 0.4s ease;
  min-width: 4px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  padding: 16px 14px 10px;
}

.nav-item {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f8;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: #555;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.nav-item:hover {
  background: #e0e7ff;
  color: #667eea;
  transform: scale(1.08);
}
.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 3px 10px rgba(102,126,234,0.45);
  transform: scale(1.12);
}
.nav-item.answered {
  background: #ecfdf5;
  color: #059669;
  border-color: #a7f3d0;
}
.nav-item.wrong {
  background: #fff0f0;
  color: #e53e3e;
  border-color: #fca5a5;
}
.nav-item.correct {
  background: #ecfdf5;
  color: #059669;
  border-color: #a7f3d0;
}

.nav-footer {
  padding: 10px 14px 14px;
  border-top: 1px solid #f0f2f8;
}

.nav-answered-info {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}
.answered-num {
  font-size: 22px;
  font-weight: 700;
  color: #667eea;
  line-height: 1;
}
.answered-sep {
  color: #bbb;
  margin: 0 3px;
  font-size: 16px;
}
.total-num {
  font-size: 16px;
  font-weight: 600;
  color: #909399;
}
.answered-label {
  font-size: 12px;
  color: #aaa;
}

.nav-legend {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}
.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-active { background: linear-gradient(135deg, #667eea, #764ba2); }
.dot-answered { background: #34d399; border: 1px solid #a7f3d0; }
.dot-none { background: #f0f2f8; border: 1px solid #dde1f0; }
.legend-text {
  font-size: 11px;
  color: #999;
  margin-right: 6px;
}

.question-area {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-num {
  color: #909399;
  font-size: 14px;
}

.question-score {
  font-size: 18px;
  font-weight: 700;
  color: #E6A23C;
}

.question-text {
  font-size: 17px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 25px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.option-item:hover { background: #e8f4ff; }
.option-item.selected { border-color: #667eea; background: #f0f5ff; }
.option-item.correct { border-color: #67C23A; background: #f0f9eb; }
.option-item.wrong { border-color: #F56C6C; background: #fef0f0; }

.option-label {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  border-radius: 50%;
  margin-right: 14px;
  font-weight: 600;
}

.option-item.selected .option-label { background: #667eea; color: #fff; }
.option-item.correct .option-label { background: #67C23A; color: #fff; }
.option-item.wrong .option-label { background: #F56C6C; color: #fff; }

.judge-options {
  flex-direction: row;
  gap: 20px;
}

.judge-options .option-item {
  flex: 1;
  justify-content: center;
}

.fill-input, .subjective-input {
  margin-bottom: 20px;
}

.reference-answer {
  margin-top: 15px;
  padding: 15px;
  background: #f0f9eb;
  border-radius: 8px;
  border-left: 4px solid #67C23A;
}

.answer-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
}

.correct-answer {
  margin-bottom: 12px;
}

.answer-text {
  color: #67C23A;
  font-weight: 600;
  font-size: 16px;
}

.analysis p {
  margin: 8px 0 0;
  color: #606266;
  line-height: 1.8;
}

.question-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.result-content {
  text-align: center;
  padding: 20px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 20px;
}

.score-circle .score-value {
  font-size: 36px;
  font-weight: 700;
}

.score-circle .score-total {
  font-size: 16px;
  opacity: 0.8;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.result-item {
  text-align: center;
}

.item-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
}

.item-label {
  font-size: 13px;
  color: #909399;
}

.correct-color { color: #67C23A; }
.wrong-color { color: #F56C6C; }

/* 历史记录样式 */
.history-section {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.history-header {
  text-align: center;
  margin-bottom: 30px;
}

.history-header h2 {
  font-size: 22px;
  color: #303133;
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.history-header h2 i {
  color: #667eea;
}

.history-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.history-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.history-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.history-icon i {
  font-size: 24px;
  color: #fff;
}

.history-info {
  flex: 1;
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.history-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.history-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-meta i {
  color: #667eea;
}

.history-time {
  font-size: 13px;
  color: #b2bec3;
  margin-right: 16px;
}

.history-action {
  flex-shrink: 0;
}

.empty-history {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-history i {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.empty-history p {
  margin: 0 0 8px;
  font-size: 16px;
}

.empty-history p.sub {
  font-size: 14px;
  color: #c0c4cc;
  margin-bottom: 20px;
}
</style>
