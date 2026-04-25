<template>
  <div class="demo-container">
    <!-- 顶部提示横幅 -->
    <div class="demo-banner">
      <div class="banner-content">
        <i class="el-icon-view"></i>
        <span>您正在浏览<strong>演示模式</strong>，所有数据为示例数据，不支持提交和修改操作</span>
        <el-button size="small" type="primary" @click="goLogin">返回登录</el-button>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="demo-main">
      <!-- 侧边栏 -->
      <div class="demo-sidebar">
        <div class="demo-logo">
          <i class="el-icon-document"></i>
          <span>在线考试系统</span>
        </div>
        <el-menu :default-active="activeMenu" @select="handleMenuSelect" class="demo-menu">
          <el-menu-item index="dashboard">
            <i class="el-icon-s-home"></i>
            <span>系统首页</span>
          </el-menu-item>
          <el-menu-item index="exams">
            <i class="el-icon-document"></i>
            <span>考试列表</span>
          </el-menu-item>
          <el-menu-item index="questions">
            <i class="el-icon-edit-outline"></i>
            <span>题库浏览</span>
          </el-menu-item>
          <el-menu-item index="scores">
            <i class="el-icon-s-data"></i>
            <span>成绩统计</span>
          </el-menu-item>
          <el-menu-item index="ai">
            <i class="el-icon-magic-stick"></i>
            <span>智能出题</span>
          </el-menu-item>
          <el-menu-item index="analysis">
            <i class="el-icon-data-analysis"></i>
            <span>学习分析</span>
          </el-menu-item>
          <el-menu-item index="students">
            <i class="el-icon-user"></i>
            <span>学生管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 内容区 -->
      <div class="demo-content">
        <!-- 首页 -->
        <div v-if="activeMenu === 'dashboard'" class="content-section">
          <h2><i class="el-icon-s-home"></i> 系统概览</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409EFF;">
                <i class="el-icon-document"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">128</div>
                <div class="stat-label">试卷总数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: #67C23A;">
                <i class="el-icon-edit"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">2,580</div>
                <div class="stat-label">题目总数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: #E6A23C;">
                <i class="el-icon-user"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">456</div>
                <div class="stat-label">学生人数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: #F56C6C;">
                <i class="el-icon-medal"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">1,234</div>
                <div class="stat-label">考试记录</div>
              </div>
            </div>
          </div>

          <div class="feature-intro">
            <h3>核心功能</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="feature-box">
                  <i class="el-icon-magic-stick"></i>
                  <h4>智能出题</h4>
                  <p>基于Spring AI自动生成高质量试题</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="feature-box">
                  <i class="el-icon-camera"></i>
                  <h4>人脸识别</h4>
                  <p>防作弊监控，确保考试公平公正</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="feature-box">
                  <i class="el-icon-data-analysis"></i>
                  <h4>智能分析</h4>
                  <p>详细的学习报告和成绩分析</p>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 考试列表 -->
        <div v-if="activeMenu === 'exams'" class="content-section">
          <h2><i class="el-icon-document"></i> 考试列表</h2>
          <el-table :data="demoExams" style="width: 100%" class="demo-table">
            <el-table-column prop="name" label="考试名称" width="250"></el-table-column>
            <el-table-column prop="subject" label="科目" width="120"></el-table-column>
            <el-table-column prop="time" label="考试时间" width="180"></el-table-column>
            <el-table-column prop="duration" label="时长" width="100"></el-table-column>
            <el-table-column prop="totalScore" label="总分" width="100"></el-table-column>
            <el-table-column label="状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '进行中' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template>
                <el-button size="mini" type="text" @click="showDemoTip">查看详情</el-button>
                <el-button size="mini" type="text" disabled>参加考试</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 题库浏览 -->
        <div v-if="activeMenu === 'questions'" class="content-section">
          <h2><i class="el-icon-edit-outline"></i> 题库浏览</h2>
          <el-tabs v-model="questionTab">
            <el-tab-pane label="选择题" name="choice">
              <div class="question-list">
                <div class="question-item" v-for="(q, index) in demoQuestions.choice" :key="index">
                  <div class="question-header">
                    <span class="question-no">{{ index + 1 }}.</span>
                    <span class="question-type">单选题</span>
                    <span class="question-score">5分</span>
                  </div>
                  <div class="question-content">{{ q.question }}</div>
                  <div class="question-options">
                    <div v-for="(opt, i) in q.options" :key="i" class="option-item">
                      <span>{{ String.fromCharCode(65 + i) }}. {{ opt }}</span>
                    </div>
                  </div>
                  <div class="question-answer">
                    <el-tag type="success" size="small">正确答案：{{ q.answer }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="填空题" name="fill">
              <div class="question-list">
                <div class="question-item" v-for="(q, index) in demoQuestions.fill" :key="index">
                  <div class="question-header">
                    <span class="question-no">{{ index + 1 }}.</span>
                    <span class="question-type">填空题</span>
                    <span class="question-score">10分</span>
                  </div>
                  <div class="question-content">{{ q.question }}</div>
                  <div class="question-answer">
                    <el-tag type="success" size="small">参考答案：{{ q.answer }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="判断题" name="judge">
              <div class="question-list">
                <div class="question-item" v-for="(q, index) in demoQuestions.judge" :key="index">
                  <div class="question-header">
                    <span class="question-no">{{ index + 1 }}.</span>
                    <span class="question-type">判断题</span>
                    <span class="question-score">3分</span>
                  </div>
                  <div class="question-content">{{ q.question }}</div>
                  <div class="question-answer">
                    <el-tag :type="q.answer === '正确' ? 'success' : 'danger'" size="small">{{ q.answer }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 成绩统计 -->
        <div v-if="activeMenu === 'scores'" class="content-section">
          <h2><i class="el-icon-s-data"></i> 成绩统计</h2>

          <!-- 成绩分析卡片 -->
          <div class="score-analysis">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="analysis-card">
                  <div class="analysis-icon" style="background: linear-gradient(135deg, #67C23A, #85ce61);">
                    <i class="el-icon-trophy"></i>
                  </div>
                  <div class="analysis-info">
                    <div class="analysis-label">平均分</div>
                    <div class="analysis-value">81.8</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="analysis-card">
                  <div class="analysis-icon" style="background: linear-gradient(135deg, #409EFF, #66b1ff);">
                    <i class="el-icon-star-on"></i>
                  </div>
                  <div class="analysis-info">
                    <div class="analysis-label">最高分</div>
                    <div class="analysis-value">95</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="analysis-card">
                  <div class="analysis-icon" style="background: linear-gradient(135deg, #E6A23C, #f0b86e);">
                    <i class="el-icon-medal"></i>
                  </div>
                  <div class="analysis-info">
                    <div class="analysis-label">及格率</div>
                    <div class="analysis-value">92.3%</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="analysis-card">
                  <div class="analysis-icon" style="background: linear-gradient(135deg, #F56C6C, #f78989);">
                    <i class="el-icon-warning"></i>
                  </div>
                  <div class="analysis-info">
                    <div class="analysis-label">不及格人数</div>
                    <div class="analysis-value">12</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <el-table :data="demoScores" style="width: 100%" class="demo-table">
            <el-table-column prop="name" label="学生姓名" width="150"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="150"></el-table-column>
            <el-table-column prop="exam" label="考试名称" width="200"></el-table-column>
            <el-table-column prop="score" label="成绩" width="100">
              <template slot-scope="scope">
                <span :style="{color: scope.row.score >= 60 ? '#67C23A' : '#F56C6C', fontWeight: 'bold'}">
                  {{ scope.row.score }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" width="100"></el-table-column>
            <el-table-column prop="time" label="完成时间"></el-table-column>
            <el-table-column label="操作">
              <template>
                <el-button size="mini" type="text" @click="showDemoTip">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- AI智能出题 -->
        <div v-if="activeMenu === 'ai'" class="content-section">
          <h2><i class="el-icon-magic-stick"></i> AI智能出题</h2>

          <div class="ai-intro">
            <el-alert
              title="AI智能出题功能"
              type="success"
              description="基于先进的AI技术，自动生成高质量试题，支持多种题型和难度级别，大幅提升出题效率。"
              :closable="false"
              show-icon>
            </el-alert>
          </div>

          <div class="ai-demo-form">
            <el-card shadow="hover">
              <div slot="header">
                <span style="font-weight: 600;"><i class="el-icon-edit"></i> 智能出题配置</span>
              </div>
              <el-form label-width="120px">
                <el-form-item label="科目">
                  <el-select v-model="aiForm.subject" placeholder="请选择科目" style="width: 100%;">
                    <el-option label="数学" value="math"></el-option>
                    <el-option label="语文" value="chinese"></el-option>
                    <el-option label="英语" value="english"></el-option>
                    <el-option label="物理" value="physics"></el-option>
                    <el-option label="计算机" value="computer"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="题型">
                  <el-checkbox-group v-model="aiForm.questionTypes">
                    <el-checkbox label="choice">选择题</el-checkbox>
                    <el-checkbox label="fill">填空题</el-checkbox>
                    <el-checkbox label="judge">判断题</el-checkbox>
                    <el-checkbox label="essay">简答题</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="难度级别">
                  <el-radio-group v-model="aiForm.difficulty">
                    <el-radio label="easy">简单</el-radio>
                    <el-radio label="medium">中等</el-radio>
                    <el-radio label="hard">困难</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="生成数量">
                  <el-slider v-model="aiForm.count" :min="5" :max="50" show-input></el-slider>
                </el-form-item>
                <el-form-item label="知识点">
                  <el-input v-model="aiForm.keywords" placeholder="输入相关知识点，用逗号分隔" type="textarea" :rows="2"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" icon="el-icon-magic-stick" @click="showDemoTip">开始生成</el-button>
                  <el-button icon="el-icon-refresh">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>

          <div class="ai-sample" style="margin-top: 24px;">
            <h3 style="margin-bottom: 16px;"><i class="el-icon-document-checked"></i> AI生成示例</h3>
            <el-card shadow="hover">
              <div class="sample-question">
                <div class="question-tag">选择题 · 中等难度</div>
                <p class="question-text">在Python中，下列哪个函数用于获取列表的长度？</p>
                <div class="question-options">
                  <div class="option">A. size()</div>
                  <div class="option">B. length()</div>
                  <div class="option correct">C. len()</div>
                  <div class="option">D. count()</div>
                </div>
                <div class="ai-tag">
                  <el-tag type="success" size="small"><i class="el-icon-cpu"></i> AI生成</el-tag>
                  <el-tag type="info" size="small">置信度: 95%</el-tag>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 学习分析 -->
        <div v-if="activeMenu === 'analysis'" class="content-section">
          <h2><i class="el-icon-data-analysis"></i> 学习分析报告</h2>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span style="font-weight: 600;"><i class="el-icon-trend-charts"></i> 成绩趋势</span>
                </div>
                <div class="chart-placeholder">
                  <i class="el-icon-data-line" style="font-size: 80px; color: #409EFF; opacity: 0.3;"></i>
                  <p style="color: #999; margin-top: 16px;">成绩变化趋势图</p>
                  <div class="trend-data">
                    <div class="trend-item">
                      <span class="trend-label">本月平均分</span>
                      <span class="trend-value" style="color: #67C23A;">85.2 <i class="el-icon-top"></i></span>
                    </div>
                    <div class="trend-item">
                      <span class="trend-label">上月平均分</span>
                      <span class="trend-value" style="color: #909399;">82.5</span>
                    </div>
                    <div class="trend-item">
                      <span class="trend-label">提升幅度</span>
                      <span class="trend-value" style="color: #67C23A;">+3.3%</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span style="font-weight: 600;"><i class="el-icon-pie-chart"></i> 知识点掌握度</span>
                </div>
                <div class="knowledge-mastery">
                  <div class="mastery-item" v-for="item in knowledgeData" :key="item.name">
                    <div class="mastery-header">
                      <span>{{ item.name }}</span>
                      <span style="font-weight: 600; color: #409EFF;">{{ item.percent }}%</span>
                    </div>
                    <el-progress :percentage="item.percent" :color="getProgressColor(item.percent)" :stroke-width="12"></el-progress>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span style="font-weight: 600;"><i class="el-icon-warning"></i> 薄弱知识点</span>
                </div>
                <div class="weak-points">
                  <el-tag type="danger" v-for="point in weakPoints" :key="point" style="margin: 4px;">{{ point }}</el-tag>
                </div>
                <el-button type="primary" size="small" style="margin-top: 16px;" @click="showDemoTip">
                  <i class="el-icon-share"></i> 生成针对性练习
                </el-button>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <div slot="header">
                  <span style="font-weight: 600;"><i class="el-icon-medal-1"></i> 优势知识点</span>
                </div>
                <div class="strong-points">
                  <el-tag type="success" v-for="point in strongPoints" :key="point" style="margin: 4px;">{{ point }}</el-tag>
                </div>
                <el-button type="success" size="small" style="margin-top: 16px;" @click="showDemoTip">
                  <i class="el-icon-star-on"></i> 挑战更高难度
                </el-button>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 学生管理 -->
        <div v-if="activeMenu === 'students'" class="content-section">
          <h2><i class="el-icon-user"></i> 学生管理</h2>

          <div class="table-toolbar">
            <el-input placeholder="搜索学生姓名或学号" prefix-icon="el-icon-search" style="width: 300px;"></el-input>
            <div>
              <el-button type="primary" icon="el-icon-plus" @click="showDemoTip">添加学生</el-button>
              <el-button icon="el-icon-upload" @click="showDemoTip">批量导入</el-button>
              <el-button icon="el-icon-download" @click="showDemoTip">导出数据</el-button>
            </div>
          </div>

          <el-table :data="demoStudents" style="width: 100%" class="demo-table">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
            <el-table-column prop="name" label="姓名" width="120"></el-table-column>
            <el-table-column prop="class" label="班级" width="150"></el-table-column>
            <el-table-column prop="major" label="专业" width="150"></el-table-column>
            <el-table-column prop="avgScore" label="平均分" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.avgScore >= 85 ? 'success' : scope.row.avgScore >= 70 ? '' : 'warning'">
                  {{ scope.row.avgScore }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="examCount" label="考试次数" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '正常' ? 'success' : 'info'" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="200">
              <template>
                <el-button size="mini" type="text" @click="showDemoTip">详情</el-button>
                <el-button size="mini" type="text" @click="showDemoTip">编辑</el-button>
                <el-button size="mini" type="text" style="color: #F56C6C;" @click="showDemoTip">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="text-align: right; margin-top: 16px;">
            <el-pagination
              @current-change="showDemoTip"
              :current-page="1"
              :page-size="10"
              layout="total, prev, pager, next, jumper"
              :total="156">
            </el-pagination>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DemoPage',
  data() {
    return {
      activeMenu: 'dashboard',
      questionTab: 'choice',
      aiForm: {
        subject: 'computer',
        questionTypes: ['choice'],
        difficulty: 'medium',
        count: 10,
        keywords: ''
      },
      knowledgeData: [
        { name: '数据结构', percent: 85 },
        { name: '算法设计', percent: 72 },
        { name: '数据库原理', percent: 90 },
        { name: '操作系统', percent: 68 },
        { name: '计算机网络', percent: 78 }
      ],
      weakPoints: ['指针与内存管理', '递归算法', '线程同步', '数据库事务'],
      strongPoints: ['循环结构', '数组操作', 'SQL查询', 'HTTP协议', '面向对象'],
      demoStudents: [
        { studentId: '20210001', name: '张三', class: '计算机2021-1班', major: '计算机科学与技术', avgScore: 88, examCount: 12, status: '正常' },
        { studentId: '20210002', name: '李四', class: '计算机2021-1班', major: '计算机科学与技术', avgScore: 92, examCount: 15, status: '正常' },
        { studentId: '20210003', name: '王五', class: '计算机2021-2班', major: '软件工程', avgScore: 76, examCount: 10, status: '正常' },
        { studentId: '20210004', name: '赵六', class: '计算机2021-2班', major: '软件工程', avgScore: 85, examCount: 14, status: '正常' },
        { studentId: '20210005', name: '孙七', class: '计算机2021-3班', major: '网络工程', avgScore: 68, examCount: 8, status: '正常' },
        { studentId: '20210006', name: '周八', class: '计算机2021-3班', major: '网络工程', avgScore: 90, examCount: 16, status: '正常' },
        { studentId: '20210007', name: '吴九', class: '计算机2021-1班', major: '计算机科学与技术', avgScore: 82, examCount: 11, status: '正常' },
        { studentId: '20210008', name: '郑十', class: '计算机2021-2班', major: '软件工程', avgScore: 95, examCount: 18, status: '正常' }
      ],
      demoExams: [
        { name: '2024年春季期末考试', subject: '数学', time: '2024-06-20 09:00', duration: '120分钟', totalScore: 100, status: '已结束' },
        { name: 'Python程序设计测试', subject: '计算机', time: '2024-06-18 14:00', duration: '90分钟', totalScore: 100, status: '已结束' },
        { name: '大学英语四级模拟', subject: '英语', time: '2024-06-25 10:00', duration: '120分钟', totalScore: 100, status: '进行中' },
        { name: '数据结构与算法', subject: '计算机', time: '2024-06-28 15:00', duration: '120分钟', totalScore: 100, status: '未开始' },
        { name: '线性代数期中测试', subject: '数学', time: '2024-06-30 09:00', duration: '100分钟', totalScore: 100, status: '未开始' }
      ],
      demoQuestions: {
        choice: [
          { question: '下列哪个是Python的正确变量命名？', options: ['2nd_var', 'second-var', 'second_var', 'second var'], answer: 'C' },
          { question: 'HTTP协议默认使用的端口号是？', options: ['21', '80', '443', '8080'], answer: 'B' },
          { question: '以下哪个不是面向对象编程的特征？', options: ['封装', '继承', '多态', '编译'], answer: 'D' }
        ],
        fill: [
          { question: 'Python中用于定义函数的关键字是 _______。', answer: 'def' },
          { question: '数据库中，用于查询数据的SQL语句是 _______。', answer: 'SELECT' },
          { question: 'TCP/IP协议中，TCP代表 _______。', answer: '传输控制协议' }
        ],
        judge: [
          { question: 'Python是一种编译型语言。', answer: '错误' },
          { question: 'HTTP是一种安全的传输协议。', answer: '错误' },
          { question: 'HTML是一种标记语言。', answer: '正确' }
        ]
      },
      demoScores: [
        { name: '张三', studentId: '20210001', exam: '2024年春季期末考试', score: 95, rank: 1, time: '2024-06-20 10:45' },
        { name: '李四', studentId: '20210002', exam: '2024年春季期末考试', score: 88, rank: 5, time: '2024-06-20 10:38' },
        { name: '王五', studentId: '20210003', exam: 'Python程序设计测试', score: 76, rank: 12, time: '2024-06-18 15:22' },
        { name: '赵六', studentId: '20210004', exam: 'Python程序设计测试', score: 92, rank: 2, time: '2024-06-18 15:15' },
        { name: '孙七', studentId: '20210005', exam: '2024年春季期末考试', score: 58, rank: 28, time: '2024-06-20 10:52' }
      ]
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
    },
    showDemoTip() {
      this.$message.info('演示模式下无法查看详细信息，请登录后体验完整功能')
    },
    goLogin() {
      this.$router.push('/')
    },
    getProgressColor(percent) {
      if (percent >= 80) return '#67C23A'
      if (percent >= 60) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style scoped>
.demo-container {
  min-height: 100vh;
  background: #f0f2f5;
}

.demo-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.banner-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
  font-size: 15px;
}

.banner-content i {
  font-size: 20px;
}

.banner-content strong {
  font-weight: 700;
  color: #FFD700;
}

.demo-main {
  display: flex;
  max-width: 1400px;
  margin: 24px auto;
  gap: 24px;
  padding: 0 24px;
}

.demo-sidebar {
  width: 240px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 80px;
}

.demo-logo {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.demo-logo i {
  font-size: 28px;
  color: #409EFF;
}

.demo-logo span {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.demo-menu {
  border: none;
}

.demo-content {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 32px;
  min-height: calc(100vh - 120px);
}

.content-section h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-section h2 i {
  color: #409EFF;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.feature-intro {
  margin-top: 32px;
}

.feature-intro h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.feature-box {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #e8e8e8;
}

.feature-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
  border-color: #409EFF;
}

.feature-box i {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.feature-box h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.feature-box p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.demo-table {
  margin-top: 16px;
}

.question-list {
  padding: 16px 0;
}

.question-item {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #e8e8e8;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.question-no {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}

.question-type {
  background: #409EFF;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.question-score {
  background: #67C23A;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.question-content {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.question-options {
  padding-left: 20px;
}

.option-item {
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.question-answer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

@media (max-width: 768px) {
  .demo-main {
    flex-direction: column;
  }

  .demo-sidebar {
    width: 100%;
    position: static;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 成绩分析样式 */
.score-analysis {
  margin-bottom: 24px;
}

.analysis-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.analysis-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.analysis-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.analysis-info {
  flex: 1;
}

.analysis-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.analysis-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

/* AI出题样式 */
.ai-intro {
  margin-bottom: 24px;
}

.ai-demo-form {
  margin-bottom: 24px;
}

.sample-question {
  padding: 8px 0;
}

.question-tag {
  display: inline-block;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 12px;
}

.question-text {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  margin: 12px 0;
}

.question-options .option {
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin: 8px 0;
  transition: all 0.3s;
}

.question-options .option:hover {
  background: #e8f4ff;
}

.question-options .option.correct {
  background: #f0f9ff;
  border: 2px solid #67C23A;
  color: #67C23A;
  font-weight: 600;
}

.ai-tag {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

/* 学习分析样式 */
.chart-placeholder {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 12px;
}

.trend-data {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px dashed #e8e8e8;
}

.trend-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.trend-label {
  font-size: 13px;
  color: #666;
}

.trend-value {
  font-size: 20px;
  font-weight: 700;
}

.knowledge-mastery {
  padding: 20px 0;
}

.mastery-item {
  margin-bottom: 20px;
}

.mastery-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.weak-points, .strong-points {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 80px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

/* 学生管理样式 */
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.table-toolbar > div {
  display: flex;
  gap: 8px;
}
</style>
