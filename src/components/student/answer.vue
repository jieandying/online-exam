<template>
  <div id="answer">
    <!-- 顶部信息栏 -->
    <div class="top">
      <ul class="item">
        <li class="top-item">
          <i class="el-icon-collection-tag" ref="toggle"></i>
          <span>{{ title }}</span>
        </li>
        <li class="top-item">
          <span class="label">当前题号:</span>
          <span class="value">{{ number }}</span>
          <span class="separator">/</span>
           <span class="total">{{ (topicCount[0] || 0) + (topicCount[1] || 0) + (topicCount[2] || 0) + (topicCount[3] || 0) }}</span>
        </li>
        <li class="top-item user-info" @click="flag = !flag">
          <i class="el-icon-user"></i>
          <span class="username">{{ userInfo.name }}</span>
          <i class="el-icon-caret-bottom"></i>
          <div class="msg" v-if="flag" @click="flag = !flag">
            <p>姓名：{{ userInfo.name }}</p>
            <p>准考证号：{{ userInfo.id }}</p>
          </div>
        </li>
        <li class="top-item timer-box">
          <i class="el-icon-time"></i>
          <span class="countdown" :class="{ 'urgent': time < 600 }">{{ time | timeFormat }}</span>
        </li>
      </ul>
      <!-- 进度条 -->
      <div class="progress-bar-container">
        <div class="progress-bar" :style="{ width: progress + '%' }"></div>
      </div>
    </div>

    <div class="flexarea">
      <!-- 左侧题号导航 -->
      <transition name="slide-fade">
        <div class="left" v-if="slider_flag">
          <!-- 头部 -->
          <div class="nav-header">
            <span class="nav-panel-title">答题卡</span>
            <el-button type="text" size="mini" @click="slider_flag = !slider_flag">
              <i class="el-icon-close"></i> 收起
            </el-button>
          </div>

          <!-- 选择题 -->
          <div class="type-group" v-if="topic[1] && topic[1].length > 0">
            <div class="type-title">
              <span class="type-badge multi">选</span>
              <span class="type-name">选择题</span>
            </div>
            <div class="type-grid">
              <div v-for="(item, idx) in (topic[1] || [])" :key="'multi-'+idx"
                   class="nav-item"
                   :class="{
                     active: currentType === 1 && idx === index,
                     answered: bg_flag && topic1Answer[idx] != null
                   }"
                   @click="change(0, idx)">
                {{ idx + 1 }}
                <span v-if="markedSet['1_' + (topic[1][idx] && topic[1][idx].questionId)]" class="mark-star">★</span>
              </div>
            </div>
          </div>

          <!-- 填空题 -->
          <div class="type-group" v-if="topic[2] && topic[2].length > 0">
            <div class="type-title">
              <span class="type-badge fill">填</span>
              <span class="type-name">填空题</span>
            </div>
            <div class="type-grid">
              <div v-for="(item, idx) in (topic[2] || [])" :key="'fill-'+idx"
                   class="nav-item"
                   :class="{
                     active: currentType === 2 && idx === index,
                     answered: fillAnswer[idx] && fillAnswer[idx].some(function(v){ return v !== null && v !== '' })
                   }"
                   @click="change(1, idx)">
                {{ (topicCount[0] || 0) + idx + 1 }}
                <span v-if="markedSet['2_' + (topic[2][idx] && topic[2][idx].questionId)]" class="mark-star">★</span>
              </div>
            </div>
          </div>

          <!-- 判断题 -->
          <div class="type-group" v-if="topic[3] && topic[3].length > 0">
            <div class="type-title">
              <span class="type-badge judge">判</span>
              <span class="type-name">判断题</span>
            </div>
            <div class="type-grid">
              <div v-for="(item, idx) in (topic[3] || [])" :key="'judge-'+idx"
                   class="nav-item"
                   :class="{
                     active: currentType === 3 && idx === index,
                     answered: judgeAnswer[idx] != null
                   }"
                   @click="change(2, idx)">
                {{ (topicCount[0] || 0) + (topicCount[1] || 0) + idx + 1 }}
                <span v-if="markedSet['3_' + (topic[3][idx] && topic[3][idx].questionId)]" class="mark-star">★</span>
              </div>
            </div>
          </div>

          <!-- 主观题 -->
          <div class="type-group" v-if="topic[4] && topic[4].length > 0">
            <div class="type-title">
              <span class="type-badge subjective">主</span>
              <span class="type-name">主观题</span>
            </div>
            <div class="type-grid">
              <div v-for="(item, idx) in (topic[4] || [])" :key="'subjective-'+idx"
                   class="nav-item"
                   :class="{
                     active: currentType === 4 && idx === index,
                     answered: subjectiveAnswer[idx] && subjectiveAnswer[idx].trim() !== ''
                   }"
                   @click="change(3, idx)">
                {{ (topicCount[0] || 0) + (topicCount[1] || 0) + (topicCount[2] || 0) + idx + 1 }}
                <span v-if="markedSet['4_' + (topic[4][idx] && topic[4][idx].questionId)]" class="mark-star">★</span>
              </div>
            </div>
          </div>

          <!-- 底部统计 -->
          <div class="nav-footer">
            <span class="answered-count">已答：<strong>{{ navAnsweredCount }}</strong></span>
            <span class="unanswered-count">未答：<strong>{{ navTotalCount - navAnsweredCount }}</strong></span>
          </div>
        </div>
      </transition>

      <!-- 右侧答题区 -->
      <transition name="slider-right">
        <div class="right">
          <!-- 标题区 -->
          <div class="title">
            <p>{{ title }}</p>
            <i class="iconfont icon-right auto-right"></i>
            <span class="toggle-btn" @click="slider_flag = !slider_flag">
                <i class="el-icon-menu"></i> 题号卡
            </span>
          </div>
          
          <!-- 题目内容 -->
          <div class="content card-box">
             <div class="question-header">
                <span class="q-number">{{ number }}</span>
                <span class="q-text">{{ showQuestion }}</span>
                <span class="q-score" v-if="showAnswer && showAnswer.score">({{ showAnswer.score }}分)</span>
             </div>

             <!-- 选择题 -->
            <div v-if="currentType == 1" class="options-area">
              <el-radio-group v-model="radio[index]" @change="getChangeLabel">
                <el-radio :label="1" class="option-item">A. {{ showAnswer.answerA }}</el-radio>
                <el-radio :label="2" class="option-item">B. {{ showAnswer.answerB }}</el-radio>
                <el-radio :label="3" class="option-item">C. {{ showAnswer.answerC }}</el-radio>
                <el-radio :label="4" class="option-item">D. {{ showAnswer.answerD }}</el-radio>
              </el-radio-group>

              <!-- 解析 -->
              <div class="analysis-box" v-if="isPractice">
                 <div class="analysis-title">解析信息</div> 
                 <div class="analysis-row">
                    <span class="label">正确答案：</span>
                    <span class="value green">{{ reduceAnswer.rightAnswer }}</span>
                 </div>
                 <div class="analysis-row">
                    <span class="label">题目解析：</span>
                    <span class="value">{{ reduceAnswer.analysis || '暂无解析' }}</span>
                 </div>
              </div>
            </div>

            <!-- 填空题 -->
            <div class="fill" v-if="currentType == 2">
              <div v-for="(item, currentIndex) in (part || [])" :key="currentIndex" class="fill-item">
                <span class="fill-index">{{currentIndex + 1}}.</span>
                <el-input 
                    placeholder="请输入答案"
                    :value="fillAnswer[index] && fillAnswer[index][currentIndex] !== undefined ? fillAnswer[index][currentIndex] : ''"
                    @input="(val) => $set(fillAnswer[index], currentIndex, val)"
                    clearable
                    @blur="fillBG"
                    class="fill-input"
                    :disabled="!topic[2] || !topic[2][index]"
                ></el-input>
              </div>

               <div class="analysis-box" v-if="isPractice && topic[2] && topic[2][index]">
                 <div class="analysis-title">解析信息</div> 
                 <div class="analysis-row">
                    <span class="label">正确答案：</span>
                    <span class="value green">{{ topic[2][index].answer }}</span>
                 </div>
                 <div class="analysis-row">
                    <span class="label">题目解析：</span>
                    <span class="value">{{ topic[2][index].analysis || '暂无解析' }}</span>
                 </div>
              </div>
            </div>

            <!-- 判断题 -->
            <div class="judge" v-if="currentType == 3">
              <el-radio-group v-model="judgeAnswer[index]" @change="getJudgeLabel" class="judge-group" v-if="showAnswer && topic[3] && topic[3][index]">
                <el-radio :label="1" border>正确</el-radio>
                <el-radio :label="2" border>错误</el-radio>
              </el-radio-group>

               <div class="analysis-box" v-if="isPractice && topic[3] && topic[3][index]">
                 <div class="analysis-title">解析信息</div> 
                 <div class="analysis-row">
                    <span class="label">正确答案：</span>
                    <span class="value green">{{ topic[3][index].answer == 'T' ? '正确' : '错误' }}</span>
                 </div>
                 <div class="analysis-row">
                    <span class="label">题目解析：</span>
                    <span class="value">{{ topic[3][index].analysis || '暂无解析' }}</span>
                 </div>
              </div>
            </div>

            <!-- 主观题 -->
            <div class="subjective" v-if="currentType == 4">
              <div v-for="(item, currentIndex) in (topic[4] || [])" :key="currentIndex" class="subjective-item" v-show="index == currentIndex">
                <el-input 
                    type="textarea"
                    :rows="8"
                    placeholder="请输入你的答案..."
                    v-model="subjectiveAnswer[currentIndex]"
                    @input="handleSubjectiveInput"
                    class="subjective-input"
                    :disabled="!topic[4] || !topic[4][currentIndex]"
                ></el-input>
                
                 <div class="analysis-box" v-if="isPractice">
                  <div class="analysis-title">解析信息</div> 
                  <div class="analysis-row">
                     <span class="label">参考答案：</span>
                     <span class="value green">{{ item.referenceAnswer }}</span>
                  </div>
                  <div class="analysis-row">
                     <span class="label">题目解析：</span>
                     <span class="value">{{ item.analysis || '暂无解析' }}</span>
                  </div>
                  <!-- 系统评分区域 -->
                  <div class="practice-auto-score">
                    <el-button
                      v-if="!practiceScores[currentIndex]"
                      size="small" type="info" icon="el-icon-cpu"
                      :loading="practiceScoreLoading"
                      @click="calcPracticeScore(item, currentIndex)">
                      查看系统评分
                    </el-button>
                    <div v-else class="practice-score-result">
                      <div class="ps-header">
                        <i class="el-icon-cpu"></i>系统评分结果
                        <el-tag size="mini" :type="getPracticeScoreTag(practiceScores[currentIndex].score, item.score)">
                          {{ practiceScores[currentIndex].score }} / {{ item.score }} 分
                        </el-tag>
                      </div>
                      <div class="ps-bars">
                        <div class="ps-bar-item" v-for="(val, key) in practiceScores[currentIndex].detail" :key="key" v-if="key !== 'total'">
                          <span class="ps-label">{{ dimLabel(key) }}</span>
                          <div class="ps-bar"><div class="ps-fill" :style="{ width: (val * 100) + '%', background: getDimColor(val) }"></div></div>
                          <span class="ps-val">{{ (val * 100).toFixed(0) }}%</span>
                        </div>
                      </div>
                      <div class="ps-comment">{{ practiceScores[currentIndex].comment }}</div>
                    </div>
                  </div>
               </div>
              </div>
            </div>
          </div>

          <!-- 底部操作栏 -->
          <div class="operation">
            <el-button @click="previous()" :disabled="index == 0 && currentType == 1" icon="el-icon-arrow-left">上一题</el-button>
            <el-button :type="isMarked ? 'success' : 'warning'" @click="mark()" :icon="isMarked ? 'el-icon-star-on' : 'el-icon-star-off'">{{ isMarked ? '取消标记' : '标记题目' }}</el-button>
            <el-button type="primary" @click="finishExam()" v-if="isLastQuestion">提交试卷</el-button>
            <el-button @click="next()" v-else>下一题 <i class="el-icon-arrow-right el-icon--right"></i></el-button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  filters: {
      timeFormat(value) {
          if (!value) return '00:00:00';
          let hours = Math.floor(value / 3600);
          let minutes = Math.floor((value % 3600) / 60);
          let seconds = value % 60;
          return `${hours < 10 ? '0'+hours : hours}:${minutes < 10 ? '0'+minutes : minutes}:${seconds < 10 ? '0'+seconds : seconds}`;
      }
  },
  data() {
    return {
      startTime: null,
      endTime: null,
      time: null,
      reduceAnswer: [],
      answerScore: 0,
      bg_flag: false,
      isFillClick: false,
      slider_flag: true,
      flag: false,
      currentType: 1, 
      radio: [], 
      title: "请选择正确的选项",
      index: 0, 
      userInfo: {
        name: null,
        id: null
      },
      topicCount: [],
      score: [],
      examData: {},
      topic: {},
      showQuestion: '',
      showAnswer: {},
      number: 1,
      part: null,
      fillAnswer: [[]],
      judgeAnswer: [],
      subjectiveAnswer: [],
      topic1Answer: [],
      rightAnswer: '',
      timer: null,
      // 防切屏
      switchCount: 0,
      switchLimit: 3,
      // 自动保存
      autoSaveKey: '',
      autoSaveTimer: null,
      // 练习模式 - 系统评分
      practiceScores: {},
      practiceScoreLoading: false,
      // 全屏监控
      fullscreenViolations: 0,
      fullscreenLimit: 2,
      // 标记题目（key: '题型_questionId'）
      markedSet: {}
    }
  },
  computed: {
      navTotalCount() {
          return (this.topicCount[0] || 0) + (this.topicCount[1] || 0) + (this.topicCount[2] || 0) + (this.topicCount[3] || 0)
      },
      navAnsweredCount() {
          let cnt = 0
          ;(this.topic1Answer || []).forEach(v => { if (v != null) cnt++ })
          ;(this.fillAnswer || []).forEach(arr => { if (arr && arr.some(v => v !== null && v !== '')) cnt++ })
          ;(this.judgeAnswer || []).forEach(v => { if (v != null) cnt++ })
          ;(this.subjectiveAnswer || []).forEach(v => { if (v && v.trim() !== '') cnt++ })
          return cnt
      },
      progress() {
          let total = 0;
          if(this.topicCount && this.topicCount.length > 0) {
              total = this.topicCount.reduce((a, b) => a + b, 0);
          }
          if(total === 0) return 0;
          return Math.round((this.number / total) * 100);
      },
      isPractice() {
          return this.$route.query.isPractice === 'true'
      },
      isLastQuestion() {
          // 判题最后一题：仅当之后没有主观题时才显示提交按钮
          if(this.currentType === 3 && this.index === (this.topicCount[2] || 1) - 1
              && (!this.topicCount[3] || this.topicCount[3] === 0)) return true;
          // 填空最后一题：仅当之后没有判断题和主观题时才显示提交按钮
          if(this.currentType === 2 && (!this.topicCount[2] || this.topicCount[2] === 0)
              && (!this.topicCount[3] || this.topicCount[3] === 0)
              && this.index === (this.topicCount[1] || 1) - 1) return true;
          let total = 0;
          this.topicCount.forEach(c => total += c);
          return this.number === total;
      },
      isMarked() {
          if (!this.showAnswer || !this.showAnswer.questionId) return false
          return !!this.markedSet[`${this.currentType}_${this.showAnswer.questionId}`]
      }
  },
  created() {
    this.getCookies()
    this.getExamData()
    if (!this.isPractice) {
      this.showTime()
      document.addEventListener('visibilitychange', this.handleVisibilityChange)
      // 全屏监控
      document.addEventListener('fullscreenchange', this.handleFullscreenChange)
      // 考试开始时请求全屏
      this.$nextTick(() => {
        const el = document.documentElement
        if (el.requestFullscreen) el.requestFullscreen().catch(() => {})
      })
    }
  },
  beforeDestroy() {
    clearInterval(this.timer)
    if (this.autoSaveTimer) clearInterval(this.autoSaveTimer)
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
  },
  methods: {
    getTime(date) {
      let year = date.getFullYear()
      let month = date.getMonth()+ 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
      let day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
      let hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
      let minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
      let seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
      return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
    },
    getCookies() {
      this.userInfo.name = this.$cookies.get("cname")
      this.userInfo.id = this.$cookies.get("cid")
    },
    getExamData() {
      let date = new Date()
      this.startTime = this.getTime(date)
      let examCode = this.$route.query.examCode
      this.$axios(`/api/exam/${examCode}`).then(res => {
        this.examData = { ...res.data.data }
        console.log('[DEBUG] examData:', this.examData)
        this.index = 0
        this.time = this.examData.totalTime * 60
        let paperId = this.examData.paperId
        console.log('[DEBUG] paperId:', paperId)
        this.$axios(`/api/exam-paper/${examCode}`).then(res => {
          console.log('[DEBUG] paper res.data:', res.data)
          this.topic = { ...res.data }
          console.log('[DEBUG] topic[1] length:', this.topic[1] ? this.topic[1].length : 'undefined')
          // 正式考试：对每题型内题目随机排序（防抄袭）
          if (!this.isPractice) {
            const seed = parseInt(this.userInfo.id || 1) * 1000 + parseInt(examCode || 1)
            ;[1, 2, 3, 4].forEach(k => {
              if (Array.isArray(this.topic[k])) {
                this.topic[k] = this.seededShuffle(this.topic[k], seed + k)
              }
            })
          }
          
          // 明确按题型index构建 topicCount，保证顺序
          this.topicCount = [
            (this.topic[1] || []).length,
            (this.topic[2] || []).length,
            (this.topic[3] || []).length,
            (this.topic[4] || []).length
          ]
          
          // 计算各题型总分
          this.score = [1, 2, 3, 4].map(k => {
            return (this.topic[k] || []).reduce((s, q) => s + (q.score || 0), 0)
          })
          
          // 初始化填空题答案数组：每题固定 1 个空白，答案的逗号分隔表示多个可接受答案（不是多个空白）
          const fillLen = this.topicCount[1]
          this.fillAnswer = Array.from({ length: fillLen }, () => [null])
          
          // 初始化主观题答案数组
          this.subjectiveAnswer = new Array(this.topicCount[3]).fill(null)
          
          // 初始化选择题/判断题答案数组（防止 radio[index] 为 undefined 导致 el-radio-group 崩溃）
          this.radio = new Array(this.topicCount[0]).fill(null)
          this.topic1Answer = new Array(this.topicCount[0]).fill(null)
          this.judgeAnswer = new Array(this.topicCount[2]).fill(null)
          
          // 初始化显示第一道题
          const firstType = [1, 2, 3, 4].find(k => this.topic[k] && this.topic[k].length > 0)
          if (firstType) {
            this.currentType = firstType
            this.index = 0
            this.showQuestion = this.topic[firstType][0].question
            this.showAnswer = this.topic[firstType][0]
            this.reduceAnswer = this.topic[firstType][0]
            this.number = 1
            if (firstType === 2) this.part = this.fillAnswer[0]
          }
          // 自动保存初始化
          if (!this.isPractice) {
            this.autoSaveKey = `exam_${examCode}_${this.userInfo.id}`
            this.$nextTick(() => { this.restoreProgress() })
            this.autoSaveTimer = setInterval(() => { this.saveProgress() }, 30000)
          }
        })
      })
    },
    change(index, childIndex) {
      this.index = childIndex || 0
      this.currentType = index + 1
      let topicArray = this.topic[index+1]
      let len = topicArray ? topicArray.length : 0
      if(this.currentType == 1) {
          this.number = 1 + this.index
          this.title = "请选择正确的选项"
      } else if(this.currentType == 2) {
          this.number = (this.topicCount[0]||0) + 1 + this.index
          this.title = "请在横线处填写答案"
      } else if(this.currentType == 3) {
          this.number = (this.topicCount[0]||0) + (this.topicCount[1]||0) + 1 + this.index
          this.title = "请判断正确或错误"
      } else if(this.currentType == 4) {
          this.number = (this.topicCount[0]||0) + (this.topicCount[1]||0) + (this.topicCount[2]||0) + 1 + this.index
          this.title = "请填写主观题答案"
      }
      if (this.topic[this.currentType] && this.topic[this.currentType][this.index]) {
        this.showQuestion = this.topic[this.currentType][this.index].question
        this.showAnswer = this.topic[this.currentType][this.index]
        this.reduceAnswer = this.topic[this.currentType][this.index]
      }
      
      // Update part for fill questions
      if(this.currentType == 2) {
           let curr = this.topic[2][this.index];
           if(curr && curr.question) {
                // Determine number of blanks based on question? 
                // Original code assumed logic. Here we assume 4 blanks max for simplicity from original code
                 this.part = this.fillAnswer[this.index]
           }
      }
    },
    getChangeLabel(val) {
      this.radio[this.index] = val
      if(val) {
        this.topic1Answer[this.index] = val 
        this.bg_flag = true
      }
      this.reduceAnswer = this.topic[1][this.index]
      this.saveProgress()
    },
    getJudgeLabel(val) {
        this.judgeAnswer[this.index] = val
        if(val) this.bg_flag = true
        this.saveProgress()
    },
    handleSubjectiveInput(val) {
        if(val) this.bg_flag = true
        this.saveProgress()
    },
    previous() {
      if (this.index > 0) {
        this.index--
        this.number--
        this.showQuestion = this.topic[this.currentType][this.index].question
        this.showAnswer = this.topic[this.currentType][this.index]
        this.reduceAnswer = this.topic[this.currentType][this.index]
        
        if(this.currentType == 2) this.part = this.fillAnswer[this.index]
      }
    },
    next() {
      // 先检查当前题型数组是否存在
      if (!this.topic[this.currentType]) {
        this.$message.error('题目数据加载失败')
        return
      }
      
      if (this.index >= this.topic[this.currentType].length - 1) {
        // 已经是最后一题，切换到下一题型
        if (this.currentType === 1 && this.topic[2] && this.topic[2].length > 0) {
          // 选择题 -> 填空题
          this.currentType = 2
          this.index = 0
        } else if (this.currentType === 2 && this.topic[3] && this.topic[3].length > 0) {
          // 填空题 -> 判断题
          this.currentType = 3
          this.index = 0
        } else if (this.currentType === 3 && this.topic[4] && this.topic[4].length > 0) {
          // 判断题 -> 主观题
          this.currentType = 4
          this.index = 0
        } else {
          // 已经是所有题目的最后一题
          this.$message.info('已经是最后一题了')
          return
        }
      } else {
        this.index++
      }
      
      this.number++
      this.showQuestion = this.topic[this.currentType][this.index].question
      this.showAnswer = this.topic[this.currentType][this.index]
      this.reduceAnswer = this.topic[this.currentType][this.index]
      
      if(this.currentType == 2) this.part = this.fillAnswer[this.index]
    },
    mark() {
      const q = this.showAnswer
      if (!q || !q.questionId) { this.$message.warning('题目数据加载中'); return }
      const studentId = parseInt((this.userInfo && this.userInfo.id) || this.$cookies.get('cid') || 0)
      const key = `${this.currentType}_${q.questionId}`
      if (this.markedSet[key]) {
        // 取消标记，从错题本删除
        this.$axios.delete(`/api/study/wrong/${studentId}/${this.currentType}/${q.questionId}`)
          .then(() => {
            this.$delete(this.markedSet, key)
            this.$message.success('已取消标记')
          }).catch(() => this.$message.error('取消失败'))
      } else {
        // 标记，加入错题本
        const answerMap = { 1: 'A', 2: 'B', 3: 'C', 4: 'D' }
        const correctAnswer = this.currentType === 1 ? (answerMap[q.rightAnswer] || q.rightAnswer || '') : (q.answer || q.referenceAnswer || '')
        this.$axios.post('/api/study/wrong', {
          studentId: studentId,
          questionType: this.currentType,
          questionId: q.questionId,
          questionContent: q.question || '',
          correctAnswer: correctAnswer,
          wrongAnswer: '',
          wrongCount: 1,
          mastered: 0,
          score: q.score || 2,
          subject: q.source || this.examData.description || '未知',
          analysis: q.analysis || '',
          lastWrongTime: new Date()
        }).then(() => {
          this.$set(this.markedSet, key, true)
          this.$message.success('已标记，已加入错题本★')
        }).catch(() => this.$message.error('标记失败'))
      }
    },
    fillBG() {
      if(this.fillAnswer[this.index][0] != null) {
        this.bg_flag = true
      }
      this.saveProgress()
    },
    showTime() { 
      this.timer = setInterval(() => {
        this.time -= 1
        if(this.time == 10) {
          this.$message({
            showClose: true,
            type: 'error',
            message: '考生注意,考试时间还剩10分钟！！！'
          })
        }
        if(this.time <= 0) {
          this.$message({
            showClose: true,
            type: 'error',
            message: '考试时间到，系统自动交卷'
          })
          clearInterval(this.timer)
          this.timer = null
          this.commit()
        }
      }, 1000)
    },
    async commit() {
        // 计算客观题分数，同时精确统计正确题数
        let objScore = 0
        let objCorrectCount = 0
        let topic1Answer = this.topic1Answer
        topic1Answer.forEach((element, index) => {
          let right = null
          if(element != null) {
            switch(element) {
              case 1: right = "A"; break
              case 2: right = "B"; break
              case 3: right = "C"; break
              case 4: right = "D"; break
            }
            if(right == this.topic[1][index].rightAnswer) {
              objScore += this.topic[1][index].score
              objCorrectCount++
            }
          }
        })
        
        let fillAnswer = this.fillAnswer
        fillAnswer.forEach((element, index) => {
          const q = this.topic[2] && this.topic[2][index]
          if (!q || !q.answer) return  // null 防护
          // 答案用逗号分隔多个可接受答案（匹配任意一个即得满分）
          const correctOptions = q.answer.split(',').map(a => a.trim().toLowerCase()).filter(a => a)
          const userInput = (element[0] || '').trim().toLowerCase()
          console.log(`[FILL DEBUG] 第${index+1}题: 正确答案候选=`, correctOptions, '用户输入=', JSON.stringify(userInput), '分值=', q.score)
          if (userInput && correctOptions.some(opt => opt === userInput)) {
            objScore += q.score || 0
            objCorrectCount++
            console.log(`[FILL DEBUG] 第${index+1}题 得分! +${q.score}, 累计=${objScore}`)
          }
        })
        
        let judgeAnswer = this.judgeAnswer
        judgeAnswer.forEach((element, index) => {
          let right = null
          switch(element) {
            case 1: right = "T"; break
            case 2: right = "F"; break
          }
          if(right == this.topic[3][index].answer) {
            objScore += this.topic[3][index].score
            objCorrectCount++
          }
        })
        
        const hasSubjective = this.topic[4] && this.topic[4].length > 0
        let subjectiveAutoScore = 0

        let date = new Date()
        this.endTime = this.getTime(date)
        let answerDate = this.endTime.substr(0, 10)

        // 主观题处理：练习模式由算法自动批阅，正式考试提交给老师
        if (hasSubjective) {
          if (this.isPractice) {
            // 练习模式：并发调用算法自动评分，立即得到主观题分数
            this.$message({ type: 'info', message: '正在自动批阅主观题...', duration: 2000 })
            const autoScorePromises = this.topic[4].map((q, idx) => {
              const answer = this.subjectiveAnswer[idx] || ''
              if (!answer.trim()) return Promise.resolve(0)
              return this.$axios.post('/api/scoring/auto/compute', {
                studentAnswer: answer,
                referenceAnswer: q.referenceAnswer || '',
                maxScore: q.score || 10
              }).then(res => {
                if (res.data.code === 200) return res.data.data.score || 0
                return 0
              }).catch(() => 0)
            })
            const scores = await Promise.all(autoScorePromises)
            subjectiveAutoScore = scores.reduce((a, b) => a + b, 0)
          } else {
            // 正式考试：提交主观题答案，等待老师批阅
            const subjectivePayload = this.topic[4].map((q, idx) => ({
              questionId: q.questionId,
              examCode: parseInt(this.examData.examCode),
              studentId: parseInt(this.userInfo.id),
              studentAnswer: this.subjectiveAnswer[idx] || ''
            }))
            try {
              await this.$axios.post('/api/scoring/answers/batch', subjectivePayload)
            } catch (e) {
              this.$message.error('主观题提交失败，请重试')
              return
            }
          }
        }

        // 最终得分：练习模式 = 客观分 + 算法主观分；正式考试有主观题则 -1 等待老师批阅
        const finalScore = Math.round(objScore) + subjectiveAutoScore
        const etScore = (hasSubjective && !this.isPractice) ? -1 : finalScore
        
        this.$axios({
          url: '/api/score',
          method: 'post',
          data: {
            examCode: this.examData.examCode,
            studentId: this.userInfo.id,
            subject: this.examData.source,
            ptScore: Math.round(objScore),
            etScore: etScore,
            score: finalScore,
            answerDate: answerDate,
          }
        }).then(res => {
          if(res.data.code == 200) {
            // 交卷成功清除本地保存
            if (this.autoSaveKey) localStorage.removeItem(this.autoSaveKey)
            
            // ========== 记录错题到错题本 ==========
            this.recordAllWrongQuestions(subjectiveAutoScore)
            
            // 记录学习曲线（正式考试 type=3，试卷练习 type=4）
            const practiceType = this.isPractice ? 4 : 3
            const totalQuestions = (this.topicCount[0] || 0) + (this.topicCount[1] || 0) + (this.topicCount[2] || 0) + (this.topicCount[3] || 0)
            const correctCount = objCorrectCount // 使用精确统计的客观题正确数（主观题由后端单独评分）
            const studyRecord = {
              studentId: parseInt(this.userInfo.id || this.$cookies.get('cid') || 0),
              practiceDate: new Date(),
              practiceType: practiceType,
              totalQuestions: totalQuestions,
              correctCount: correctCount,
              wrongCount: totalQuestions - correctCount,
              score: finalScore,
              totalScore: this.score.reduce((a, b) => a + b, 0),
              accuracy: parseFloat(((correctCount / totalQuestions) * 100).toFixed(2)),
              duration: (this.examData.totalTime * 60 - this.time) || 0,
              subject: this.examData.source || '',
              examCode: parseInt(this.examData.examCode || 0)
            }
            this.$axios.post('/api/study/record', studyRecord).catch(() => {})
            
            // 正式考试有主观题 → 等待老师批阅；其余情况（练习模式 / 无主观题）→ 直接显示得分
            if (hasSubjective && !this.isPractice) {
              this.$router.push({
                path: '/studentScore',
                query: {
                  score: -1,
                  startTime: this.startTime,
                  endTime: this.endTime,
                  hasSubjective: 'true'
                }
              })
            } else {
              this.$router.push({
                path: '/studentScore',
                query: {
                  score: finalScore,
                  startTime: this.startTime,
                  endTime: this.endTime
                }
              })
            }
          }
        })
    },
    finishExam() {
       const unanswered = this.getUnansweredCount()
       const msg = unanswered > 0
         ? `还有 <strong>${unanswered}</strong> 道题未作答，确定要交卷吗？`
         : '所有题目均已作答，确定交卷？'
       this.$confirm(msg, '提交试卷', {
           confirmButtonText: '确定交卷',
           cancelButtonText: '继续答题',
           type: unanswered > 0 ? 'warning' : 'success',
           dangerouslyUseHTMLString: true
       }).then(() => {
           this.commit()
       }).catch(() => {})
    },
    // 防切屏检测
    handleVisibilityChange() {
      if (!document.hidden || this.isPractice) return
      this.switchCount++
      if (this.switchCount >= this.switchLimit) {
        this.$message({ type: 'error', message: '切屏次数过多，系统自动交卷！', duration: 0 })
        clearInterval(this.timer)
        this.commit()
      } else {
        const left = this.switchLimit - this.switchCount
        this.$message({
          type: 'warning',
          message: `警告：检测到您切换了屏幕（第 ${this.switchCount} 次），还剩 ${left} 次机会，超出将自动交卷！`,
          duration: 5000
        })
      }
    },
    // 保存答题进度
    saveProgress() {
      if (!this.autoSaveKey || this.isPractice) return
      const data = {
        topic1Answer: this.topic1Answer,
        fillAnswer: this.fillAnswer,
        judgeAnswer: this.judgeAnswer,
        subjectiveAnswer: this.subjectiveAnswer,
        time: this.time,
        savedAt: Date.now()
      }
      try { localStorage.setItem(this.autoSaveKey, JSON.stringify(data)) } catch (e) {}
    },
    // 恢复答题进度
    restoreProgress() {
      if (!this.autoSaveKey || this.isPractice) return
      const saved = localStorage.getItem(this.autoSaveKey)
      if (!saved) return
      try {
        const data = JSON.parse(saved)
        if (Date.now() - data.savedAt > 24 * 60 * 60 * 1000) {
          localStorage.removeItem(this.autoSaveKey)
          return
        }
        this.$confirm('检测到您有未完成的答题记录，是否恢复？', '继续上次的答题', {
          confirmButtonText: '恢复答题',
          cancelButtonText: '重新开始',
          type: 'info'
        }).then(() => {
          if (data.topic1Answer) {
            this.topic1Answer = data.topic1Answer
            this.radio = data.topic1Answer.slice()
          }
          if (data.fillAnswer) this.fillAnswer = data.fillAnswer
          if (data.judgeAnswer) this.judgeAnswer = data.judgeAnswer
          if (data.subjectiveAnswer) this.subjectiveAnswer = data.subjectiveAnswer
          if (data.time != null && data.time > 0) this.time = data.time
          this.$message.success('答题记录已恢复')
        }).catch(() => {
          localStorage.removeItem(this.autoSaveKey)
        })
      } catch (e) {
        localStorage.removeItem(this.autoSaveKey)
      }
    },
    // 计算未作答题数
    getUnansweredCount() {
      let count = 0
      const t1 = this.topicCount[0] || 0
      for (let i = 0; i < t1; i++) {
        if (!this.topic1Answer[i]) count++
      }
      const t2 = this.topicCount[1] || 0
      for (let i = 0; i < t2; i++) {
        if (!this.fillAnswer[i] || !this.fillAnswer[i].some(v => v !== null && v !== '')) count++
      }
      const t3 = this.topicCount[2] || 0
      for (let i = 0; i < t3; i++) {
        if (this.judgeAnswer[i] == null) count++
      }
      const t4 = this.topicCount[3] || 0
      for (let i = 0; i < t4; i++) {
        if (!this.subjectiveAnswer[i] || !this.subjectiveAnswer[i].trim()) count++
      }
      return count
    },
    // 练习模式 - 调用后端计算系统评分
    calcPracticeScore(item, idx) {
      var self = this
      const answer = this.subjectiveAnswer[idx]
      if (!answer || !answer.trim()) {
        this.$message.warning('请先输入答案再查看系统评分')
        return
      }
      this.practiceScoreLoading = true
      this.$axios.post('/api/scoring/auto/compute', {
        studentAnswer:   answer,
        referenceAnswer: item.referenceAnswer || '',
        maxScore:        item.score || 10
      }).then(function(res) {
        self.practiceScoreLoading = false
        if (res.data.code === 200) {
          self.$set(self.practiceScores, idx, res.data.data)
        } else {
          self.$message.error('系统评分请求失败')
        }
      }).catch(function() {
        self.practiceScoreLoading = false
        self.$message.error('系统评分请求失败')
      })
    },
    // 评分维度名称
    dimLabel(key) {
      const map = { keyword: '🔑关键词', bigram: '📊相似度', length: '📍长度', phrase: '🎯短语' }
      return map[key] || key
    },
    // 维度进度条颜色
    getDimColor(val) {
      if (val >= 0.8) return '#67C23A'
      if (val >= 0.6) return '#409EFF'
      if (val >= 0.4) return '#E6A23C'
      return '#F56C6C'
    },
    // 练习评分标签类型
    getPracticeScoreTag(score, maxScore) {
      const r = maxScore > 0 ? score / maxScore : 0
      if (r >= 0.8) return 'success'
      if (r >= 0.6) return ''
      if (r >= 0.4) return 'warning'
      return 'danger'
    },
    // 全屏切换事件
    handleFullscreenChange() {
      if (this.isPractice) return
      const isFullscreen = !!(document.fullscreenElement || document.webkitFullscreenElement)
      if (!isFullscreen) {
        this.fullscreenViolations++
        if (this.fullscreenViolations >= this.fullscreenLimit) {
          this.$message({ type: 'error', message: '多次退出全屏，系统将自动交卷！', duration: 2000 })
          setTimeout(() => this.commit(), 2200)
        } else {
          const left = this.fullscreenLimit - this.fullscreenViolations
          this.$alert(`警告：您退出全屏（第 ${this.fullscreenViolations} 次），还剩 ${left} 次机会，请立即回到考试页面。`, '警告', {
            confirmButtonText: '返回考试',
            type: 'warning',
            callback: () => {
              const el = document.documentElement
              if (el.requestFullscreen) el.requestFullscreen().catch(() => {})
            }
          })
        }
      }
    },
    // 种子随机 Fisher-Yates 洗牌（防抄袭）
    seededShuffle(array, seed) {
      let a = seed | 0
      const rand = () => {
        a = (a + 0x6D2B79F5) | 0
        let t = Math.imul(a ^ (a >>> 15), 1 | a)
        t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
        return ((t ^ (t >>> 14)) >>> 0) / 0x100000000
      }
      const arr = [...array]
      for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(rand() * (i + 1));        [arr[i], arr[j]] = [arr[j], arr[i]]
      }
      return arr
    },
    /**
     * 交卷后收集所有错题并批量提交到错题本
     * 支持正式考试 + 试卷练习，包含选择/填空/判断/主观题
     */
    recordAllWrongQuestions(subjectiveAutoScore) {
      const wrongList = []
      const studentId = parseInt(this.userInfo.id || this.$cookies.get('cid') || 0)
      const subject = this.examData.source || ''
      
      // 1. 选择题错题
      if (this.topic[1]) {
        this.topic[1].forEach((q, idx) => {
          const userVal = this.topic1Answer[idx]
          if (userVal == null) return // 未作答也记为错题
          let userAnswer = ''
          switch(userVal) {
            case 1: userAnswer = 'A'; break
            case 2: userAnswer = 'B'; break
            case 3: userAnswer = 'C'; break
            case 4: userAnswer = 'D'; break
          }
          if (userAnswer !== q.rightAnswer) {
            wrongList.push({
              studentId: studentId,
              questionType: 1,
              questionId: q.questionId,
              questionContent: q.question,
              correctAnswer: q.rightAnswer,
              wrongAnswer: userAnswer || '未作答',
              score: q.score,
              subject: subject,
              analysis: q.analysis || ''
            })
          }
        })
      }
      
      // 2. 填空题错题
      if (this.topic[2]) {
        this.topic[2].forEach((q, idx) => {
          if (!q || !q.answer) return
          const correctOptions = q.answer.split(',').map(a => a.trim().toLowerCase()).filter(a => a)
          const userInput = (this.fillAnswer[idx] && this.fillAnswer[idx][0] || '').trim().toLowerCase()
          if (!userInput || !correctOptions.some(opt => opt === userInput)) {
            wrongList.push({
              studentId: studentId,
              questionType: 2,
              questionId: q.questionId,
              questionContent: q.question,
              correctAnswer: q.answer,
              wrongAnswer: this.fillAnswer[idx] && this.fillAnswer[idx][0] || '未作答',
              score: q.score,
              subject: subject,
              analysis: q.analysis || ''
            })
          }
        })
      }
      
      // 3. 判断题错题
      if (this.topic[3]) {
        this.topic[3].forEach((q, idx) => {
          const userVal = this.judgeAnswer[idx]
          let userAnswer = ''
          switch(userVal) {
            case 1: userAnswer = 'T'; break
            case 2: userAnswer = 'F'; break
          }
          if (userAnswer !== q.answer) {
            wrongList.push({
              studentId: studentId,
              questionType: 3,
              questionId: q.questionId,
              questionContent: q.question,
              correctAnswer: q.answer === 'T' ? '正确' : '错误',
              wrongAnswer: userAnswer === 'T' ? '正确' : (userAnswer === 'F' ? '错误' : '未作答'),
              score: q.score,
              subject: subject,
              analysis: q.analysis || ''
            })
          }
        })
      }
      
      // 4. 主观题错题（仅练习模式时即时判定，正式考试由后端评分后自动处理）
      if (this.isPractice && this.topic[4]) {
        this.topic[4].forEach((q, idx) => {
          const answer = this.subjectiveAnswer[idx] || ''
          if (!answer.trim()) {
            // 未作答，记为错题
            wrongList.push({
              studentId: studentId,
              questionType: 4,
              questionId: q.questionId,
              questionContent: q.question,
              correctAnswer: q.referenceAnswer || '',
              wrongAnswer: '未作答',
              score: q.score,
              subject: subject,
              analysis: q.analysis || ''
            })
          } else if (this.practiceScores[idx] && this.practiceScores[idx].score < 5) {
            // 练习模式已评分且得分<5，记为错题
            wrongList.push({
              studentId: studentId,
              questionType: 4,
              questionId: q.questionId,
              questionContent: q.question,
              correctAnswer: q.referenceAnswer || '',
              wrongAnswer: answer,
              score: q.score,
              subject: subject,
              analysis: q.analysis || ''
            })
          }
        })
      }
      
      // 批量提交错题
      if (wrongList.length > 0) {
        console.log('[WrongBook] 记录错题:', wrongList.length, '题')
        this.$axios.post('/api/study/wrong/batch', wrongList).then(res => {
          console.log('[WrongBook] 错题记录成功:', res.data)
        }).catch(err => {
          console.error('[WrongBook] 错题记录失败:', err)
        })
      }
    }
  }
}
</script>

<style lang="less" scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

/* 全局样式覆盖 */
.iconfont.icon-right {
  font-size: 20px;
  margin-right: 10px;
}

#answer {
  background: #f5f7fa;
  min-height: 100vh;
  padding-top: 80px; /* Space for fixed header */
  padding-bottom: 20px;
  font-family: 'Inter', sans-serif;
}

/* 顶部栏 */
.top {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  height: 80px; /* Increased height for progress bar */
  display: flex;
  flex-direction: column;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 60px;
  margin: 0;
  list-style: none;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1a1a2e;
  font-weight: 600;
  font-size: 15px;
}

.separator {
  margin: 0 4px;
  color: #ccc;
}

.user-info {
  position: relative;
  cursor: pointer;
}

.user-info .msg {
  position: absolute;
  top: 50px;
  right: 0;
  width: 240px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  z-index: 1001;
  border: 1px solid #f0f0f0;
}

.msg p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

/* 倒计时 */
.countdown {
  font-family: 'Monaco', monospace;
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.countdown.urgent {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  animation: blink 1s infinite;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* 进度条 */
.progress-bar-container {
  width: 100%;
  height: 4px;
  background: rgba(102, 126, 234, 0.1);
  position: relative;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.5s ease;
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

/* 主要内容区 */
.flexarea {
  display: flex;
  width: 98%;
  margin: 20px auto;
  gap: 20px;
  padding: 0 10px;
}

/* 左侧导航 */
.left {
  width: 220px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.05);
  padding: 20px 16px;
  border: 1px solid rgba(255,255,255,0.6);
  height: fit-content;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  position: sticky;
  top: 100px;
}

.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.nav-panel-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
}

.type-group {
  margin-bottom: 18px;
}

.type-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.type-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.type-badge.multi     { background: linear-gradient(135deg, #74b9ff, #0984e3); }
.type-badge.fill      { background: linear-gradient(135deg, #55efc4, #00b894); }
.type-badge.judge     { background: linear-gradient(135deg, #ffeaa7, #fdcb6e); color: #2d3436; }
.type-badge.subjective{ background: linear-gradient(135deg, #fab1a0, #e17055); }

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.nav-item {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1.5px solid #e0e0e0;
  background: #fff;
  color: #555;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  border-color: #667eea;
  color: #667eea;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.4);
}

.nav-item.answered {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.35);
}

.mark-star {
  position: absolute;
  top: -3px;
  right: -3px;
  font-size: 9px;
  color: #f59e0b;
  line-height: 1;
  pointer-events: none;
}

.nav-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.answered-count strong { color: #10b981; }
.unanswered-count strong { color: #f59e0b; }

/* 右侧答题区 */
.right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}

.toggle-btn {
  font-size: 14px;
  color: #667eea;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-box {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  margin-bottom: 24px;
  border: 1px solid rgba(255,255,255,0.6);
  min-height: 400px;
}

.question-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 32px;
  border-bottom: 1px dashed #eee;
  padding-bottom: 24px;
}

.q-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 8px;
  font-weight: 800;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.q-text {
  flex: 1;
  font-size: 18px;
  line-height: 1.6;
  color: #2d3748;
  font-weight: 500;
}

.q-score {
  color: #718096;
  font-size: 14px;
}

/* 选项样式 */
.option-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 16px;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  background: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.option-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.1);
  border-color: #667eea;
  background: #f8fafc;
}

.option-item.is-checked {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

/deep/ .el-radio__label {
  font-size: 16px;
  color: #4a5568;
}

/* 填空题样式 */
.fill-item {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.fill-index {
  font-weight: 700;
  color: #667eea;
}

.fill-input {
  max-width: 400px;
}

/* 判断题样式 */
.judge-group {
    display: flex;
    gap: 30px;
    margin-top: 20px;
}

/* 解析框 */
.analysis-box {
  margin-top: 40px;
  background: #f8fafc;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.analysis-title {
  font-size: 15px;
  font-weight: 700;
  color: #4a5568;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #667eea;
  border-radius: 2px;
}

.analysis-row {
  margin-bottom: 12px;
  line-height: 1.6;
}

.analysis-row .label {
  color: #718096;
  font-weight: 600;
}

.analysis-row .value {
  color: #2d3748;
}

.analysis-row .value.green {
  color: #10b981;
  font-weight: 700;
}

/* 底部按钮栏 */
.operation {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.operation .el-button {
  padding: 12px 30px;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.3s;
}

.operation .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.operation .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 主观题输入框 */
.subjective-item {
  margin-bottom: 24px;
}

.subjective-input /deep/ textarea {
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: #2d3748;
  background: #f8fafc;
  transition: all 0.3s;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

.subjective-input /deep/ textarea:focus {
  background: #fff;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

/* 练习模式 - 系统评分卡片 */
.practice-auto-score {
  margin-top: 14px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 12px;
}
.practice-score-result {
  background: #f0f5ff;
  border-radius: 10px;
  padding: 14px 16px;
}
.ps-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 14px;
  color: #1a1a2e;
  margin-bottom: 12px;
}
.ps-bars { display: flex; flex-direction: column; gap: 7px; margin-bottom: 10px; }
.ps-bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.ps-label { width: 72px; color: #606266; flex-shrink: 0; }
.ps-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}
.ps-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.ps-val { width: 36px; text-align: right; color: #303133; font-weight: 600; }
.ps-comment {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
  background: rgba(255,255,255,0.7);
  border-radius: 6px;
  padding: 8px 10px;
}

/* 动画效果 */
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter, .slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.slider-right-enter-active {
  transition: all 0.4s ease;
}
.slider-right-enter {
  transform: translateY(20px);
  opacity: 0;
}
</style>
