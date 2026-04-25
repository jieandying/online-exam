<template>
  <div class="student-practice-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <i class="el-icon-cpu"></i>
        </div>
        <div class="header-text">
          <h2>遗传算法智能组卷</h2>
          <p>基于遗传算法优化，专属于你的个性化练习</p>
        </div>
      </div>
      <div class="header-tips" v-if="!examStarted">
        <i class="el-icon-sunny"></i>
        <span>坚持每天练习，进步看得见！</span>
      </div>
    </div>

    <!-- 组卷配置区 -->
    <div class="config-area" v-if="!examStarted">
      <!-- 快速模式选择 -->
      <div class="quick-mode">
        <h3><i class="el-icon-lightning"></i> 快速开始</h3>
        <div class="mode-cards">
          <div class="mode-card" :class="{ active: quickMode === 'easy' }" @click="setQuickMode('easy')">
            <div class="mode-icon easy"><i class="el-icon-ice-cream-round"></i></div>
            <div class="mode-name">轻松模式</div>
            <div class="mode-desc">10题 · 简单</div>
          </div>
          <div class="mode-card" :class="{ active: quickMode === 'normal' }" @click="setQuickMode('normal')">
            <div class="mode-icon normal"><i class="el-icon-coffee-cup"></i></div>
            <div class="mode-name">标准模式</div>
            <div class="mode-desc">15题 · 中等</div>
          </div>
          <div class="mode-card" :class="{ active: quickMode === 'hard' }" @click="setQuickMode('hard')">
            <div class="mode-icon hard"><i class="el-icon-hot-water"></i></div>
            <div class="mode-name">挑战模式</div>
            <div class="mode-desc">20题 · 困难</div>
          </div>
          <div class="mode-card" :class="{ active: quickMode === 'custom' }" @click="setQuickMode('custom')">
            <div class="mode-icon custom"><i class="el-icon-setting"></i></div>
            <div class="mode-name">自定义</div>
            <div class="mode-desc">随心配置</div>
          </div>
        </div>
      </div>

      <el-row :gutter="24">
        <!-- 左侧配置 -->
        <el-col :span="14">
          <div class="config-card">
            <div class="card-title">
              <i class="el-icon-s-operation"></i>
              <span>组卷配置</span>
            </div>

            <!-- 科目选择 -->
            <div class="config-section">
              <div class="section-label">选择科目</div>
              <div class="subject-tags">
                <el-tag v-for="subject in subjects" :key="subject" 
                        :type="config.subjects.includes(subject) ? '' : 'info'"
                        :effect="config.subjects.includes(subject) ? 'dark' : 'plain'"
                        @click="toggleSubject(subject)" class="subject-tag">
                  {{ subject }}
                </el-tag>
              </div>
            </div>

            <!-- 目标难度 (滑块) -->
            <div class="config-section">
              <div class="section-label">目标难度</div>
              <div class="difficulty-slider-wrapper">
                <el-slider v-model="config.targetDifficulty" :min="1" :max="5" :step="0.1"
                           :marks="difficultyMarks" show-stops class="difficulty-slider"/>
              </div>
            </div>

            <!-- 难度容差 -->
            <div class="config-section" style="margin-top: 50px;">
              <div class="section-label">难度容差</div>
              <div class="tolerance-wrapper">
                <el-input-number v-model="config.difficultyTolerance" :min="0.1" :max="2" :step="0.1" :precision="1"/>
                <span class="tolerance-tip">允许难度在目标值 ±{{ config.difficultyTolerance.toFixed(1) }} 范围内浮动</span>
              </div>
            </div>

            <!-- 题型配置 -->
            <div class="config-section" v-show="quickMode === 'custom'">
              <div class="section-label">题型配置</div>
              <div class="type-config-grid">
                <el-row :gutter="20" style="margin-bottom: 12px;">
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">选择题数量</span>
                      <el-input-number v-model="config.multiCount" :min="0" :max="50" size="small"/>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">每题分值</span>
                      <el-input-number v-model="config.multiScore" :min="1" :max="20" size="small"/>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="20" style="margin-bottom: 12px;">
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">填空题数量</span>
                      <el-input-number v-model="config.fillCount" :min="0" :max="30" size="small"/>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">每题分值</span>
                      <el-input-number v-model="config.fillScore" :min="1" :max="20" size="small"/>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="20" style="margin-bottom: 12px;">
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">判断题数量</span>
                      <el-input-number v-model="config.judgeCount" :min="0" :max="30" size="small"/>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">每题分值</span>
                      <el-input-number v-model="config.judgeScore" :min="1" :max="20" size="small"/>
                    </div>
                  </el-col>
                </el-row>
                <el-row :gutter="20" style="margin-bottom: 0;">
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">主观题数量</span>
                      <el-input-number v-model="config.subjectiveCount" :min="0" :max="20" size="small"/>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="type-item">
                      <span class="type-label">每题分值</span>
                      <el-input-number v-model="config.subjectiveScore" :min="1" :max="50" size="small"/>
                    </div>
                  </el-col>
                </el-row>
              </div>
              <div class="total-score-preview">
                预计总分: <span class="score-num">{{ expectedTotalScore }}</span> 分
              </div>
            </div>

            <!-- 高级配置 -->
            <div class="config-section" v-show="quickMode === 'custom'">
              <el-collapse v-model="activeCollapse" class="advanced-collapse">
                <el-collapse-item name="advanced" title="高级配置">
                  <div class="advanced-form">
                    <div class="adv-form-item">
                      <span class="adv-form-label">种群大小</span>
                      <el-input-number v-model="config.populationSize" :min="20" :max="200" size="small"/>
                      <span class="adv-form-tip">种群越大，搜索空间越广</span>
                    </div>
                    <div class="adv-form-item">
                      <span class="adv-form-label">迭代次数</span>
                      <el-input-number v-model="config.maxGenerations" :min="50" :max="500" size="small"/>
                      <span class="adv-form-tip">迭代次数越多，结果越优</span>
                    </div>
                    <div class="adv-form-item">
                      <span class="adv-form-label">交叉概率</span>
                      <el-slider v-model="config.crossoverRate" :min="0.5" :max="1" :step="0.05" show-input style="width: 200px;"/>
                    </div>
                    <div class="adv-form-item">
                      <span class="adv-form-label">变异概率</span>
                      <el-slider v-model="config.mutationRate" :min="0.01" :max="0.3" :step="0.01" show-input style="width: 200px;"/>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>

            <!-- 题库统计 -->
            <div class="config-section stats-section" v-show="quickMode === 'custom' && questionStats">
              <div class="section-label">题库统计</div>
              <div class="stats-grid-teacher">
                <div class="stat-box">
                  <div class="stat-value">{{ questionStats.totalQuestions || 0 }}</div>
                  <div class="stat-label">总题目数</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value">{{ questionStats.multiQuestionCount || 0 }}</div>
                  <div class="stat-label">选择题</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value">{{ questionStats.fillQuestionCount || 0 }}</div>
                  <div class="stat-label">填空题</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value">{{ questionStats.judgeQuestionCount || 0 }}</div>
                  <div class="stat-label">判断题</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value">{{ questionStats.subjectiveQuestionCount || 0 }}</div>
                  <div class="stat-label">主观题</div>
                </div>
              </div>
            </div>

            <!-- 开始按钮 -->
            <div class="action-area">
              <div class="total-preview">
                <span class="preview-label">本次练习</span>
                <span class="preview-count">{{ totalCount }}</span>
                <span class="preview-unit">题</span>
              </div>
              <el-button type="primary" size="large" :loading="generating" 
                         :disabled="config.subjects.length === 0 || totalCount === 0"
                         @click="generatePaper" class="start-btn">
                <i class="el-icon-cpu" v-if="!generating"></i>
                {{ generating ? '遗传算法进化中...' : '智能组卷' }}
              </el-button>
            </div>
          </div>
        </el-col>

        <!-- 右侧：历史记录 -->
        <el-col :span="10">
          <div class="tips-card">
            <div class="tips-header">
              <i class="el-icon-time"></i>
              <span>组卷记录</span>
              <el-button type="text" size="small" @click="loadHistory" style="margin-left: auto;">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
            <div class="history-list" v-if="historyList.length > 0">
              <div class="history-item" v-for="item in historyList" :key="item.id" @click="practiceFromHistory(item)">
                <div class="history-main">
                  <div class="history-title">{{ item.subjects || '综合练习' }}</div>
                  <div class="history-meta">
                    <span><i class="el-icon-document"></i> {{ item.totalQuestions }}题</span>
                    <span><i class="el-icon-star-on"></i> 难度{{ item.targetDifficulty }}</span>
                    <span><i class="el-icon-aim"></i> {{ item.fitness }}%</span>
                  </div>
                </div>
                <div class="history-time">{{ formatTime(item.createTime) }}</div>
                <div class="history-action">
                  <i class="el-icon-video-play"></i>
                </div>
              </div>
            </div>
            <div class="empty-history" v-else>
              <i class="el-icon-folder-opened"></i>
              <p>暂无组卷记录</p>
              <p class="sub">开始组卷后记录将显示在这里</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 答题区域 -->
    <div class="exam-section" v-if="examStarted">
      <!-- 答题导航 -->
      <div class="exam-nav">
        <div class="nav-header">
          <span>{{ selectedSubject }} - {{ levelName }}</span>
          <div class="timer-display">
            <i class="el-icon-time"></i>
            <span>{{ formattedElapsedTime }}</span>
          </div>
          <el-button type="text" @click="exitExam">
            <i class="el-icon-close"></i> 退出
          </el-button>
        </div>
              
        <!-- 题型分类导航 -->
        <div class="nav-types">
          <!-- 选择题 -->
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
                     answered: answers[getQuestionIndex('multi', idx)] !== '' && answers[getQuestionIndex('multi', idx)] !== undefined
                   }"
                   @click="goToQuestion(getQuestionIndex('multi', idx))">
                {{ idx + 1 }}
              </div>
            </div>
          </div>
                
          <!-- 填空题 -->
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
                     answered: answers[getQuestionIndex('fill', idx)] !== '' && answers[getQuestionIndex('fill', idx)] !== undefined
                   }"
                   @click="goToQuestion(getQuestionIndex('fill', idx))">
                {{ idx + 1 }}
              </div>
            </div>
          </div>
                
          <!-- 判断题 -->
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
                     answered: answers[getQuestionIndex('judge', idx)] !== '' && answers[getQuestionIndex('judge', idx)] !== undefined
                   }"
                   @click="goToQuestion(getQuestionIndex('judge', idx))">
                {{ idx + 1 }}
              </div>
            </div>
          </div>
                
          <!-- 主观题 -->
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
                     answered: answers[getQuestionIndex('subjective', idx)] !== '' && answers[getQuestionIndex('subjective', idx)] !== undefined
                   }"
                   @click="goToQuestion(getQuestionIndex('subjective', idx))">
                {{ idx + 1 }}
              </div>
            </div>
          </div>
        </div>
              
        <div class="nav-stats">
          <span>已答：<strong>{{ answeredCount }}</strong></span>
          <span style="margin-left: 12px;">未答：<strong>{{ allQuestions.length - answeredCount }}</strong></span>
        </div>
      </div>

      <!-- 题目区域 -->
      <div class="question-area">
        <div class="question-header">
          <div class="question-info">
            <el-tag :type="getTypeTagType(currentQuestion.type)" size="medium">{{ getTypeName(currentQuestion.type) }}</el-tag>
            <span class="question-num">第 {{ currentIndex + 1 }} 题 / 共 {{ allQuestions.length }} 题</span>
          </div>
          <div class="question-score">{{ currentQuestion.score || 2 }}分</div>
        </div>

        <div class="question-content">
          <p class="question-text">{{ currentQuestion.question }}</p>

          <!-- 选择题选项 -->
          <div class="options" v-if="currentQuestion.type === 1" :key="'type-'+currentQuestion.type+'-q'+currentIndex">
            <div v-for="opt in ['A', 'B', 'C', 'D']" :key="opt" class="option-item"
                 :class="{ selected: answers[currentIndex] === opt, correct: showResult && currentQuestion.rightAnswer === opt, wrong: showResult && answers[currentIndex] === opt && currentQuestion.rightAnswer !== opt }"
                 @click="selectOption(opt)">
              <span class="option-label">{{ opt }}</span>
              <span class="option-text">{{ currentQuestion['answer' + opt] }}</span>
            </div>
          </div>

          <!-- 判断题选项 -->
          <div class="options judge-options" v-if="currentQuestion.type === 3" :key="'type-'+currentQuestion.type+'-q'+currentIndex">
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
          <div class="fill-input" v-if="currentQuestion.type === 2" :key="'type-'+currentQuestion.type+'-q'+currentIndex">
            <el-input 
              :value="answers[currentIndex]" 
              @input="$set(answers, currentIndex, $event)" 
              placeholder="请输入答案" 
              :disabled="showResult" 
            />
          </div>

          <!-- 主观题输入 -->
          <div class="subjective-input" v-if="currentQuestion.type === 4" :key="'type-'+currentQuestion.type+'-q'+currentIndex">
            <el-input 
              type="textarea" 
              :value="answers[currentIndex]" 
              @input="$set(answers, currentIndex, $event)" 
              :rows="6" 
              placeholder="请输入您的答案" 
              :disabled="showResult" 
            />
          </div>
        </div>

        <!-- 答案解析 -->
        <div class="answer-section" v-if="showResult">
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

    <!-- 结果对话框 -->
    <el-dialog title="练习完成" :visible.sync="resultVisible" width="500px" center>
      <div class="result-content">
        <div class="result-score">
          <div class="score-circle">
            <span class="score-value">{{ correctCount }}</span>
            <span class="score-total">/{{ allQuestions.length }}</span>
          </div>
          <p>正确率 {{ accuracy }}%</p>
        </div>
        <div class="result-stats">
          <div class="stat-item correct">
            <i class="el-icon-check"></i>
            <span>正确 {{ correctCount }} 题</span>
          </div>
          <div class="stat-item wrong">
            <i class="el-icon-close"></i>
            <span>错误 {{ wrongCount }} 题</span>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="reviewExam">查看详情</el-button>
        <el-button type="primary" @click="restartExam">再来一次</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SelfExam',
  data() {
    return {
      subjects: [],
      questionStats: null,
      quickMode: 'normal',
      showAdvanced: false,
      activeCollapse: [],
      config: {
        subjects: [],
        targetDifficulty: 3,
        difficultyTolerance: 0.5,
        multiCount: 8,
        multiScore: 5,
        fillCount: 4,
        fillScore: 5,
        judgeCount: 3,
        judgeScore: 5,
        subjectiveCount: 0,
        subjectiveScore: 20,
        populationSize: 50,
        maxGenerations: 100,
        crossoverRate: 0.8,
        mutationRate: 0.1
      },
      difficultyMarks: {
        1: { style: { whiteSpace: 'nowrap' }, label: '简单' },
        2: { style: { whiteSpace: 'nowrap' }, label: '较易' },
        3: { style: { whiteSpace: 'nowrap' }, label: '中等' },
        4: { style: { whiteSpace: 'nowrap' }, label: '较难' },
        5: { style: { whiteSpace: 'nowrap' }, label: '困难' }
      },
      generating: false,
      paperResult: null,
      previewTab: 'multi',
      historyList: [],
      examStarted: false,
      allQuestions: [],
      currentIndex: 0,
      answers: {},
      showResult: false,
      resultVisible: false,
      correctCount: 0,
      wrongCount: 0,
      elapsedTime: 0, // 用时（秒）
      timer: null, // 计时器
      
      // 题型分类存储
      multiQuestions: [],
      fillQuestions: [],
      judgeQuestions: [],
      subjectiveQuestions: []
    }
  },
  computed: {
    totalCount() {
      return this.config.multiCount + this.config.fillCount + this.config.judgeCount + this.config.subjectiveCount
    },
    expectedTotalScore() {
      return this.config.multiCount * this.config.multiScore +
             this.config.fillCount * this.config.fillScore +
             this.config.judgeCount * this.config.judgeScore +
             this.config.subjectiveCount * this.config.subjectiveScore
    },
    currentQuestion() {
      return this.allQuestions[this.currentIndex] || {}
    },
    answeredCount() {
      return Object.keys(this.answers).length
    },
    selectedSubject() {
      return this.config.subjects.join('、') || '未选择'
    },
    levelName() {
      var d = this.config.targetDifficulty
      if (d <= 1.5) return '简单'
      if (d <= 2.5) return '较易'
      if (d <= 3.5) return '中等'
      if (d <= 4.5) return '较难'
      return '困难'
    },
    accuracy() {
      if (this.allQuestions.length === 0) return 0
      return Math.round(this.correctCount / this.allQuestions.length * 100)
    },
    formattedElapsedTime() {
      const m = Math.floor(this.elapsedTime / 60)
      const s = this.elapsedTime % 60
      return `${m < 10 ? '0' + m : m}:${s < 10 ? '0' + s : s}`
    }
  },
  mounted() {
    this.loadSubjects()
    this.loadQuestionStats()
    this.loadHistory()
    this.checkRouteParams()
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    checkRouteParams() {
      var self = this
      var subject = this.$route.query.subject
      var autoStart = this.$route.query.autoStart
      
      // 检查是否有预组好的试卷（从薄弱科目分析跳转过来）
      if (autoStart === 'true') {
        var practiceData = sessionStorage.getItem('weakPointPractice')
        if (practiceData) {
          try {
            var data = JSON.parse(practiceData)
            self.paperResult = data.paper
            self.config.subjects = [data.subject]
            self.quickMode = 'custom'
            // 自动开始练习
            self.$nextTick(function() {
              self.startPractice()
              self.$message.success('「' + data.subject + '」专项练习已开始，加油！')
            })
            // 清除 sessionStorage
            sessionStorage.removeItem('weakPointPractice')
            return
          } catch (e) {
            console.error('加载预组试卷失败:', e)
          }
        }
      }
      
      // 普通情况：只自动选中科目
      if (subject) {
        var checkInterval = setInterval(function() {
          if (self.subjects.length > 0) {
            clearInterval(checkInterval)
            if (self.subjects.indexOf(subject) > -1) {
              self.config.subjects = [subject]
              self.quickMode = 'custom'
              self.$message.success('已自动选中薄弱科目「' + subject + '」，点击智能组卷开始专项练习')
            }
          }
        }, 100)
        setTimeout(function() {
          clearInterval(checkInterval)
        }, 3000)
      }
    },
    loadSubjects() {
      var self = this
      this.$axios.get('/api/genetic/subjects')
        .then(function(res) {
          if (res.data.code === 200) {
            self.subjects = res.data.data || []
          }
        })
    },
    loadQuestionStats() {
      var self = this
      this.$axios.get('/api/genetic/stats')
        .then(function(res) {
          if (res.data.code === 200) {
            self.questionStats = res.data.data
          }
        })
    },
    loadHistory() {
      var self = this
      this.$axios.get('/api/generation-records/genetic/1/10')
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
    practiceFromHistory(item) {
      var self = this
      if (!item.paperJson) {
        this.$message.warning('记录数据不完整')
        return
      }
      try {
        var paperData = JSON.parse(item.paperJson)
        self.paperResult = paperData
        self.startPractice()
        self.$message.success('已加载历史组卷，开始练习！')
      } catch (e) {
        self.$message.error('加载记录失败')
      }
    },
    toggleSubject(subject) {
      var index = this.config.subjects.indexOf(subject)
      if (index > -1) {
        this.config.subjects.splice(index, 1)
      } else {
        this.config.subjects.push(subject)
      }
    },
    setQuickMode(mode) {
      this.quickMode = mode
      var presets = {
        easy: { multiCount: 5, fillCount: 3, judgeCount: 2, subjectiveCount: 0, targetDifficulty: 2 },
        normal: { multiCount: 8, fillCount: 4, judgeCount: 3, subjectiveCount: 0, targetDifficulty: 3 },
        hard: { multiCount: 10, fillCount: 5, judgeCount: 4, subjectiveCount: 1, targetDifficulty: 4 },
        custom: {}
      }
      var preset = presets[mode]
      if (preset.multiCount !== undefined) {
        this.config.multiCount = preset.multiCount
        this.config.fillCount = preset.fillCount
        this.config.judgeCount = preset.judgeCount
        this.config.subjectiveCount = preset.subjectiveCount
        this.config.targetDifficulty = preset.targetDifficulty
      }
    },
    resetConfig() {
      this.config = {
        subjects: [],
        targetDifficulty: 3,
        difficultyTolerance: 1.0,
        multiCount: 5,
        multiScore: 5,
        fillCount: 3,
        fillScore: 5,
        judgeCount: 5,
        judgeScore: 5,
        subjectiveCount: 0,
        subjectiveScore: 20
      }
    },
    // 组卷并直接开始练习
    generatePaper() {
      var self = this
      
      if (this.config.subjects.length === 0) {
        this.$message.warning('请至少选择一个科目')
        return
      }
      
      var totalCount = this.config.multiCount + this.config.fillCount + this.config.judgeCount + this.config.subjectiveCount
      if (totalCount === 0) {
        this.$message.warning('请至少设置一种题型的数量')
        return
      }
      
      this.generating = true
      
      var requestData = {
        subjects: this.config.subjects,
        targetDifficulty: this.config.targetDifficulty,
        difficultyTolerance: this.config.difficultyTolerance,
        multiCount: this.config.multiCount,
        fillCount: this.config.fillCount,
        judgeCount: this.config.judgeCount,
        subjectiveCount: this.config.subjectiveCount,
        multiScore: this.config.multiScore,
        fillScore: this.config.fillScore,
        judgeScore: this.config.judgeScore,
        subjectiveScore: this.config.subjectiveScore,
        populationSize: this.config.populationSize || 50,
        maxGenerations: this.config.maxGenerations || 100,
        crossoverRate: this.config.crossoverRate || 0.8,
        mutationRate: this.config.mutationRate || 0.1,
        totalScore: this.expectedTotalScore,
        studentId: this.$cookies.get('studentId')
      }
      
      this.$axios.post('/api/genetic/generate', requestData)
        .then(function(res) {
          self.generating = false
          if (res.data.code === 200) {
            self.paperResult = res.data.data
            self.$message.success('遗传算法组卷完成！匹配度: ' + self.paperResult.stats.fitness + '%')
            // 直接开始练习
            self.startPractice()
            // 刷新历史记录
            self.loadHistory()
          } else {
            self.$message.error(res.data.message || '组卷失败')
          }
        }).catch(function() {
          self.generating = false
          self.$message.error('组卷失败')
        })
    },
    // 开始练习
    startPractice() {
      var self = this
      var data = this.paperResult
      
      if (!data) {
        this.$message.error('没有试卷数据')
        return
      }

      // 重置所有状态
      this.allQuestions = []
      this.multiQuestions = []
      this.fillQuestions = []
      this.judgeQuestions = []
      this.subjectiveQuestions = []
      this.answers = {}
      this.showResult = false
      this.elapsedTime = 0
      this.currentIndex = 0

      // 添加选择题
      if (data.multiQuestions) {
        data.multiQuestions.forEach(function(q) {
          q.type = 1
          self.multiQuestions.push(q)
          self.allQuestions.push(q)
        })
      }
      // 添加填空题
      if (data.fillQuestions) {
        data.fillQuestions.forEach(function(q) {
          q.type = 2
          self.fillQuestions.push(q)
          self.allQuestions.push(q)
        })
      }
      // 添加判断题
      if (data.judgeQuestions) {
        data.judgeQuestions.forEach(function(q) {
          q.type = 3
          self.judgeQuestions.push(q)
          self.allQuestions.push(q)
        })
      }
      // 添加主观题
      if (data.subjectiveQuestions) {
        data.subjectiveQuestions.forEach(function(q) {
          q.type = 4
          self.subjectiveQuestions.push(q)
          self.allQuestions.push(q)
        })
      }

      if (this.allQuestions.length === 0) {
        this.$message.warning('没有找到符合条件的题目')
        return
      }

      // 初始化所有答案槽位（Vue2 响应式）
      this.allQuestions.forEach(function(q, idx) {
        self.$set(self.answers, idx, '')
      })

      this.examStarted = true
      this.startTimer()
    },
    // 重新组卷
    resetAll() {
      this.paperResult = null
      this.previewTab = 'multi'
    },
    // 原来的generateExam方法保留兼容
    generateExam() {
      var self = this
      
      if (this.config.subjects.length === 0) {
        this.$message.warning('请至少选择一个科目')
        return
      }
      
      var totalCount = this.config.multiCount + this.config.fillCount + this.config.judgeCount + this.config.subjectiveCount
      if (totalCount === 0) {
        this.$message.warning('请至少设置一种题型的数量')
        return
      }
      
      this.generating = true
      
      var requestData = {
        subjects: this.config.subjects,
        targetDifficulty: this.config.targetDifficulty,
        difficultyTolerance: this.config.difficultyTolerance,
        multiCount: this.config.multiCount,
        fillCount: this.config.fillCount,
        judgeCount: this.config.judgeCount,
        subjectiveCount: this.config.subjectiveCount,
        multiScore: this.config.multiScore,
        fillScore: this.config.fillScore,
        judgeScore: this.config.judgeScore,
        subjectiveScore: this.config.subjectiveScore,
        populationSize: 30,
        maxGenerations: 50
      }
      
      this.$axios.post('/api/genetic/generate', requestData)
        .then(function(res) {
          self.generating = false
          if (res.data.code === 200) {
            var data = res.data.data
            self.allQuestions = []
            
            // 添加选择题
            if (data.multiQuestions) {
              data.multiQuestions.forEach(function(q) {
                q.type = 1
                self.allQuestions.push(q)
              })
            }
            // 添加填空题
            if (data.fillQuestions) {
              data.fillQuestions.forEach(function(q) {
                q.type = 2
                self.allQuestions.push(q)
              })
            }
            // 添加判断题
            if (data.judgeQuestions) {
              data.judgeQuestions.forEach(function(q) {
                q.type = 3
                self.allQuestions.push(q)
              })
            }
            // 添加主观题
            if (data.subjectiveQuestions) {
              data.subjectiveQuestions.forEach(function(q) {
                q.type = 4
                self.allQuestions.push(q)
              })
            }
            
            if (self.allQuestions.length === 0) {
              self.$message.warning('没有找到符合条件的题目')
              return
            }
            
            self.$message.success('AI智能组卷完成！')
            self.examStarted = true
            self.currentIndex = 0
            self.answers = {}
            self.showResult = false
          } else {
            self.$message.error(res.data.message || '组卷失败')
          }
        }).catch(function() {
          self.generating = false
          self.$message.error('组卷失败')
        })
    },
    selectOption(opt) {
      if (this.showResult) return
      this.$set(this.answers, this.currentIndex, opt)
    },
    goToQuestion(index) {
      console.log('跳转到题目:', index, '类型:', this.allQuestions[index] ? this.allQuestions[index].type : '未知')
      this.currentIndex = index
      this.showResult = false
    },
    prevQuestion() {
      if (this.currentIndex > 0) {
        const prevIndex = this.currentIndex - 1
        this.currentIndex = prevIndex
        this.showResult = false
      }
    },
    nextQuestion() {
      if (this.currentIndex < this.allQuestions.length - 1) {
        const nextIndex = this.currentIndex + 1
        console.log('下一题：当前', this.currentIndex, '-> 下一个', nextIndex, '类型:', this.allQuestions[nextIndex] ? this.allQuestions[nextIndex].type : '未知')
        this.currentIndex = nextIndex
        this.showResult = false
      }
    },
    checkAnswer() {
      this.showResult = true
      // 记录错题
      if (this.isWrong(this.currentIndex)) {
        this.recordWrongQuestion()
      }
    },
    isWrong(index) {
      var q = this.allQuestions[index]
      var answer = this.answers[index]
      if (!answer) return true
      var correct = this.getCorrectAnswer(q)
      return answer.toString().trim().toLowerCase() !== correct.toString().trim().toLowerCase()
    },
    getCorrectAnswer(q) {
      if (q.type === 1) return q.rightAnswer
      if (q.type === 4) return q.referenceAnswer
      return q.answer
    },
    recordWrongQuestion() {
      var q = this.currentQuestion
      var studentId = this.$cookies.get('cid')
      this.$axios.post('/api/study/wrong', {
        studentId: parseInt(studentId),
        questionType: q.type,
        questionId: q.questionId,
        questionContent: q.question,
        correctAnswer: this.getCorrectAnswer(q),
        wrongAnswer: this.answers[this.currentIndex] || '未作答'
      }).catch(function() {})
    },
    finishExam() {
      var self = this
      this.correctCount = 0
      this.wrongCount = 0
      
      var wrongQuestions = [] // 收集所有错题
      
      this.allQuestions.forEach(function(q, index) {
        if (!self.isWrong(index)) {
          self.correctCount++
        } else {
          self.wrongCount++
          // 收集错题信息
          wrongQuestions.push({
            questionType: q.type,
            questionId: q.questionId,
            questionContent: q.question,
            correctAnswer: self.getCorrectAnswer(q),
            wrongAnswer: self.answers[index] || '未作答',
            subject: self.selectedSubject
          })
        }
      })
      
      var studentId = this.$cookies.get('cid')
      
      // 批量记录错题
      console.log('=== 自助组卷同步错题 ===')
      console.log('错题数:', wrongQuestions.length)
      
      var wrongPromises = wrongQuestions.map(function(wq, idx) {
        console.log('记录错题 #' + (idx + 1) + ':', wq)
        return self.$axios.post('/api/study/wrong', {
          studentId: parseInt(studentId),
          questionType: wq.questionType,
          questionId: wq.questionId,
          questionContent: wq.questionContent,
          correctAnswer: wq.correctAnswer,
          wrongAnswer: wq.wrongAnswer,
          subject: wq.subject
        }).then(function(res) {
          console.log('错题 #' + (idx + 1) + ' 记录成功')
          return res
        }).catch(function(err) {
          console.error('错题 #' + (idx + 1) + ' 记录失败:', err)
          return null
        })
      })
      
      // 记录学习记录
      var recordPromise = this.$axios.post('/api/study/record', {
        studentId: parseInt(studentId),
        totalQuestions: this.allQuestions.length,
        correctCount: this.correctCount,
        wrongCount: this.wrongCount,
        accuracy: this.accuracy,
        subject: this.selectedSubject
      }).then(function(res) {
        console.log('学习记录成功')
        return res
      }).catch(function(err) {
        console.error('学习记录失败:', err)
        return null
      })
      
      // 等待所有请求完成后显示结果
      Promise.all(wrongPromises.concat([recordPromise])).then(function() {
        console.log('=== 所有数据同步完成 ===')
        self.resultVisible = true
      })
    },
    reviewExam() {
      this.resultVisible = false
      this.showResult = true
    },
    restartExam() {
      this.resultVisible = false
      this.examStarted = false
      this.paperResult = null
      this.allQuestions = []
      this.answers = {}
      this.showResult = false
      this.elapsedTime = 0
      if (this.timer) clearInterval(this.timer)
    },
    exitExam() {
      var self = this
      this.$confirm('确定要退出练习吗？', '提示', {
        type: 'warning'
      }).then(function() {
        self.examStarted = false
        self.paperResult = null
        self.allQuestions = []
        self.answers = {}
      }).catch(function() {})
    },
    getTypeName(type) {
      var types = { 1: '选择题', 2: '填空题', 3: '判断题', 4: '主观题' }
      return types[type] || '未知'
    },
    getTypeTagType(type) {
      var types = { 1: '', 2: 'success', 3: 'warning', 4: 'danger' }
      return types[type] || 'info'
    },
    // 启动计时器
    startTimer() {
      var self = this
      if (this.timer) clearInterval(this.timer)
      this.timer = setInterval(function() {
        self.elapsedTime++
      }, 1000)
    },
    // 获取题目在总列表中的索引
    getQuestionIndex(type, idx) {
      if (type === 'multi') return idx
      if (type === 'fill') return this.multiQuestions.length + idx
      if (type === 'judge') return this.multiQuestions.length + this.fillQuestions.length + idx
      if (type === 'subjective') {
        return this.multiQuestions.length + this.fillQuestions.length + this.judgeQuestions.length + idx
      }
      return 0
    }
  }
}
</script>

<style lang="less" scoped>
.student-practice-container {
  padding: 24px;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 50%, #fce7f3 100%);
  min-height: calc(100vh - 120px);
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 60px;
  height: 60px;
  background: rgba(255,255,255,0.25);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon i {
  font-size: 32px;
  color: #fff;
}

.header-text h2 {
  color: #fff;
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
}

.header-text p {
  color: rgba(255,255,255,0.9);
  margin: 0;
  font-size: 14px;
}

.header-tips {
  background: rgba(255,255,255,0.2);
  padding: 10px 20px;
  border-radius: 25px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* 快速模式 */
.quick-mode {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.quick-mode h3 {
  margin: 0 0 20px;
  font-size: 16px;
  color: #2d3436;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-mode h3 i {
  color: #667eea;
}

.mode-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.mode-card {
  padding: 20px;
  border: 2px solid #e8ecef;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.mode-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.mode-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
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
}

.mode-icon.easy { background: linear-gradient(135deg, #74b9ff, #0984e3); }
.mode-icon.normal { background: linear-gradient(135deg, #667eea, #764ba2); }
.mode-icon.hard { background: linear-gradient(135deg, #fd79a8, #e84393); }
.mode-icon.custom { background: linear-gradient(135deg, #a29bfe, #6c5ce7); }

.mode-name {
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 4px;
}

.mode-desc {
  font-size: 12px;
  color: #b2bec3;
}

/* 配置卡片 */
.config-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 24px;
}

.card-title i {
  color: #667eea;
}

.config-section {
  margin-bottom: 24px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #636e72;
  margin-bottom: 12px;
}

.subject-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.subject-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 20px;
}

.subject-tag:hover {
  transform: scale(1.05);
}

/* 难度选择 */
.difficulty-selector {
  display: flex;
  gap: 12px;
}

.diff-item {
  flex: 1;
  padding: 12px 8px;
  border: 2px solid #e8ecef;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.diff-item:hover {
  border-color: #667eea;
}

.diff-item.active {
  border-color: #667eea;
  background: #e0e7ff;
}

.diff-stars {
  color: #fdcb6e;
  margin-bottom: 4px;
}

.diff-label {
  font-size: 12px;
  color: #636e72;
}

.diff-item.active .diff-label {
  color: #667eea;
  font-weight: 600;
}

/* 难度滑块 */
.difficulty-slider-wrapper {
  padding: 10px 20px 40px;
}

.difficulty-slider {
  width: 100%;
}

::v-deep .difficulty-slider .el-slider__marks-text {
  white-space: nowrap;
  font-size: 12px;
  color: #909399;
}

::v-deep .difficulty-slider .el-slider__bar {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

::v-deep .difficulty-slider .el-slider__button {
  border-color: #667eea;
}

/* 难度容差 */
.tolerance-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tolerance-tip {
  font-size: 13px;
  color: #909399;
}

/* 题型配置 - 教师端风格 */
.type-config-grid {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.type-config-grid .type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0;
}

/* 移除 el-input-number 空白 - 与教师端一致 */
/deep/ .el-input-number {
  line-height: 32px !important;
}

/deep/ .el-input-number .el-input {
  line-height: 32px !important;
}

/deep/ .el-input-number .el-input__inner {
  height: 32px !important;
  line-height: 32px !important;
}

/deep/ .el-input-number__decrease,
/deep/ .el-input-number__increase {
  height: 32px !important;
  line-height: 32px !important;
}

/deep/ .el-input-number--small {
  line-height: 28px !important;
}

/deep/ .el-input-number--small .el-input {
  line-height: 28px !important;
}

/deep/ .el-input-number--small .el-input__inner {
  height: 28px !important;
  line-height: 28px !important;
}

/deep/ .el-input-number--small .el-input-number__decrease,
/deep/ .el-input-number--small .el-input-number__increase {
  height: 28px !important;
  line-height: 28px !important;
}

.type-config-grid .type-label {
  min-width: 80px;
  font-size: 14px;
  color: #606266;
}

.total-score-preview {
  margin-top: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  border-radius: 10px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.total-score-preview .score-num {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin: 0 4px;
}

/* 高级配置 - 折叠面板风格 */
.advanced-collapse {
  margin-top: 16px;
}

::v-deep .advanced-collapse .el-collapse-item__header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 0 16px;
}

::v-deep .advanced-collapse .el-collapse-item__content {
  padding: 16px;
}

.advanced-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.adv-form-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.adv-form-label {
  min-width: 70px;
  font-size: 14px;
  color: #606266;
}

.adv-form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

/* 题库统计 - 教师端风格 */
.stats-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.stats-grid-teacher {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stat-box {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecef 100%);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  transition: all 0.3s;
}

.stat-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.stat-box .stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-box .stat-label {
  font-size: 12px;
  color: #909399;
}

/* 题型配置 */
.type-config {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
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
}

.type-badge.multi { background: #74b9ff; }
.type-badge.fill { background: #55efc4; }
.type-badge.judge { background: #ffeaa7; color: #2d3436; }
.type-badge.subjective { background: #fab1a0; }

.type-name {
  flex: 1;
  font-size: 14px;
  color: #2d3436;
}

/* 操作区域 */
.action-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 24px;
  border-top: 1px solid #eee;
  margin-top: 24px;
}

.total-preview {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.preview-label {
  color: #b2bec3;
  font-size: 14px;
}

.preview-count {
  font-size: 36px;
  font-weight: 700;
  color: #667eea;
}

.preview-unit {
  color: #636e72;
  font-size: 14px;
}

.start-btn {
  padding: 14px 40px;
  font-size: 16px;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.start-btn:hover {
  background: linear-gradient(135deg, #5a6fd6, #6a4190);
}

/* 提示卡片 */
.tips-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  height: 100%;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 24px;
}

.tips-header i {
  color: #667eea;
}

/* 历史记录列表 */
.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.history-item:hover {
  background: #e0e7ff;
  transform: translateX(4px);
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-main {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #b2bec3;
}

.history-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-meta i {
  font-size: 12px;
  color: #667eea;
}

.history-time {
  font-size: 11px;
  color: #b2bec3;
  white-space: nowrap;
}

.history-action {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-action i {
  color: #fff;
  font-size: 14px;
}

.empty-history {
  text-align: center;
  padding: 40px 20px;
  color: #b2bec3;
}

.empty-history i {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-history p {
  margin: 0 0 4px;
  font-size: 14px;
}

.empty-history p.sub {
  font-size: 12px;
  opacity: 0.7;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-radius: 12px;
  margin-bottom: 12px;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-icon i {
  font-size: 20px;
  color: #fff;
}

.tip-text strong {
  display: block;
  font-size: 14px;
  color: #2d3436;
  margin-bottom: 4px;
}

.tip-text p {
  margin: 0;
  font-size: 12px;
  color: #b2bec3;
  line-height: 1.5;
}

.stats-mini {
  background: linear-gradient(135deg, #f8f9fa 0%, #e8ecef 100%);
  border-radius: 12px;
  padding: 16px;
}

.stats-title {
  font-size: 13px;
  color: #636e72;
  margin-bottom: 12px;
  font-weight: 600;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #2d3436;
  margin-bottom: 8px;
}

.stats-row:last-child {
  margin-bottom: 0;
}

/* 答题区域样式 */
.exam-section {
  display: flex;
  gap: 24px;
}

.exam-nav {
  width: 220px;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  height: fit-content;
  position: sticky;
  top: 80px;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  font-size: 14px;
  color: #2d3436;
}

.timer-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  padding: 4px 10px;
  border-radius: 8px;
  font-family: 'Monaco', monospace;
}

.nav-types {
  margin-bottom: 16px;
}

.type-group {
  margin-bottom: 16px;
}

.type-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #636e72;
}

.type-badge {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
  
  &.multi { background: #74b9ff; }
  &.fill { background: #55efc4; }
  &.judge { background: #ffeaa7; color: #2d3436; }
  &.subjective { background: #fab1a0; }
}

.type-name {
  flex: 1;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.nav-item {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover { 
  background: #e0e7ff; 
  transform: scale(1.05);
}
.nav-item.active { 
  background: #667eea; 
  color: #fff; 
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}
.nav-item.answered { 
  background: #818cf8; 
  color: #fff; 
}
.nav-item.wrong { 
  background: #ff7675; 
  color: #fff; 
}
.nav-item.correct { 
  background: #667eea; 
  color: #fff; 
}

.nav-stats {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #eee;
  text-align: center;
  font-size: 13px;
  color: #b2bec3;
}

.question-area {
  flex: 1;
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-num {
  color: #b2bec3;
  font-size: 14px;
}

.question-score {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #2d3436;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
}

.question-text {
  font-size: 18px;
  line-height: 1.8;
  color: #2d3436;
  margin-bottom: 30px;
}

.options {
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
}

.option-item:hover { border-color: #667eea; background: #e0e7ff; }
.option-item.selected { border-color: #667eea; background: #e0e7ff; }
.option-item.correct { border-color: #667eea; background: #e0e7ff; }
.option-item.wrong { border-color: #ff7675; background: #ffe6e6; }

.option-label {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #636e72;
}

.option-item.selected .option-label { background: #667eea; color: #fff; }
.option-item.correct .option-label { background: #667eea; color: #fff; }
.option-item.wrong .option-label { background: #ff7675; color: #fff; }

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

.answer-section {
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 24px;
}

.correct-answer {
  margin-bottom: 12px;
}

.answer-text {
  color: #667eea;
  font-weight: 600;
}

.analysis {
  padding-top: 12px;
  border-top: 1px solid rgba(102,126,234,0.2);
}

.analysis p {
  margin: 8px 0 0;
  color: #636e72;
  line-height: 1.8;
}

.question-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

/* 结果弹窗 */
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
  margin: 0 auto 16px;
}

.score-circle .score-value {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
}

.score-total {
  font-size: 18px;
  opacity: 0.8;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

.result-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.result-stats .stat-item.correct { color: #667eea; }
.result-stats .stat-item.wrong { color: #ff7675; }

@media (max-width: 1200px) {
  .mode-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .exam-section { flex-direction: column; }
  .exam-nav { width: 100%; position: static; }
  .mode-cards { grid-template-columns: 1fr; }
  .type-config { grid-template-columns: 1fr; }
  .difficulty-selector { flex-wrap: wrap; }
}

/* Header Actions */
.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.4);
  color: #fff;
}

.header-actions .el-button--primary {
  background: #fff;
  color: #667eea;
  border-color: #fff;
}

.header-actions .el-button--primary:hover {
  background: #e0e7ff;
}

/* 算法流程展示 */
.algorithm-flow {
  padding: 8px 0;
}

.flow-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px dashed #e8ecef;
}

.flow-item:last-child {
  border-bottom: none;
}

.flow-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.flow-content strong {
  display: block;
  font-size: 14px;
  color: #2d3436;
  margin-bottom: 4px;
}

.flow-content p {
  margin: 0;
  font-size: 12px;
  color: #b2bec3;
}

/* 组卷结果区域 */
.result-area {
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card .stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
}

.stat-card.total-score .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card.total-questions .stat-icon { background: linear-gradient(135deg, #74b9ff, #0984e3); }
.stat-card.avg-difficulty .stat-icon { background: linear-gradient(135deg, #fdcb6e, #f39c12); }
.stat-card.fitness .stat-icon { background: linear-gradient(135deg, #fd79a8, #e84393); }
.stat-card.time-cost .stat-icon { background: linear-gradient(135deg, #a29bfe, #6c5ce7); }

.stat-info .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3436;
  line-height: 1.2;
}

.stat-info .stat-label {
  font-size: 13px;
  color: #b2bec3;
  margin-top: 4px;
}

/* 题目预览 */
.question-preview {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.preview-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2d3436;
  display: flex;
  align-items: center;
  gap: 10px;
}

.preview-header h3 i {
  color: #667eea;
}

.preview-tabs {
  display: flex;
  gap: 8px;
}

.preview-tabs span {
  padding: 8px 16px;
  border-radius: 20px;
  background: #f8f9fa;
  color: #636e72;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.preview-tabs span:hover {
  background: #e0e7ff;
  color: #667eea;
}

.preview-tabs span.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
}

.preview-content .question-list {
  padding-right: 8px;
}

/* 题目卡片 */
.q-item {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  border-left: 4px solid #667eea;
  transition: all 0.3s;
}

.q-item:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}

.q-item:last-child {
  margin-bottom: 0;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.q-num {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.q-diff {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.q-diff.diff-1 { background: #e0e7ff; color: #667eea; }
.q-diff.diff-2 { background: #d6eaf8; color: #3498db; }
.q-diff.diff-3 { background: #fef9e7; color: #f39c12; }
.q-diff.diff-4 { background: #fadbd8; color: #e74c3c; }
.q-diff.diff-5 { background: #f5eef8; color: #9b59b6; }

.q-score {
  margin-left: auto;
  background: #e0e7ff;
  color: #667eea;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.q-content {
  font-size: 14px;
  color: #2d3436;
  line-height: 1.8;
  margin-bottom: 12px;
}

.q-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.q-options .opt {
  padding: 10px 14px;
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
  color: #636e72;
  border: 1px solid #e8ecef;
}

.q-options .opt.correct {
  background: #e0e7ff;
  color: #667eea;
  border-color: #667eea;
  font-weight: 600;
}

.q-answer {
  padding: 12px 16px;
  background: #e0e7ff;
  border-radius: 10px;
  font-size: 13px;
  color: #2d3436;
}

.q-answer span {
  color: #667eea;
  font-weight: 600;
  margin-right: 8px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #b2bec3;
  font-size: 14px;
}

@media (max-width: 1400px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .q-options {
    grid-template-columns: 1fr;
  }
}
</style>
