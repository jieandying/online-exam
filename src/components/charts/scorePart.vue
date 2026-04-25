<template>
  <div class="analytics-page" v-loading="loading">

    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <el-button size="small" icon="el-icon-arrow-left" plain @click="$router.back()">返回列表</el-button>
      <div class="nav-center">
        <i class="el-icon-data-analysis nav-icon"></i>
        <span class="nav-title">{{ examName || '成绩分段分析' }}</span>
      </div>
      <el-tag type="info" size="small" v-if="scores.length > 0">
        共 {{ scores.length }} 人参考
      </el-tag>
      <div v-else></div>
    </div>

    <!-- 空状态 -->
    <div class="empty-wrap" v-if="!loading && (isNull || scores.length === 0)">
      <i class="el-icon-data-board empty-icon"></i>
      <p class="empty-title">暂无成绩数据</p>
      <p class="empty-sub">该考试还没有学生参加，请提醒学生参加考试</p>
    </div>

    <template v-if="!loading && scores.length > 0">

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card" style="--c:#409EFF;--cs:rgba(64,158,255,0.12)">
          <div class="sc-icon-box"><i class="el-icon-user-solid"></i></div>
          <div class="sc-content">
            <div class="sc-value">{{ scores.length }}</div>
            <div class="sc-label">参考人数</div>
          </div>
        </div>
        <div class="stat-card" style="--c:#7C3AED;--cs:rgba(124,58,237,0.12)">
          <div class="sc-icon-box"><i class="el-icon-s-marketing"></i></div>
          <div class="sc-content">
            <div class="sc-value">{{ avgScore }}</div>
            <div class="sc-label">平均分</div>
          </div>
        </div>
        <div class="stat-card" style="--c:#67C23A;--cs:rgba(103,194,58,0.12)">
          <div class="sc-icon-box"><i class="el-icon-circle-check"></i></div>
          <div class="sc-content">
            <div class="sc-value">{{ passRate }}</div>
            <div class="sc-label">及格率</div>
          </div>
        </div>
        <div class="stat-card" style="--c:#E6A23C;--cs:rgba(230,162,60,0.12)">
          <div class="sc-icon-box"><i class="el-icon-trophy"></i></div>
          <div class="sc-content">
            <div class="sc-value">{{ maxScore }}</div>
            <div class="sc-label">最高分</div>
          </div>
        </div>
        <div class="stat-card" style="--c:#F56C6C;--cs:rgba(245,108,108,0.12)" v-if="pendingCount > 0">
          <div class="sc-icon-box"><i class="el-icon-time"></i></div>
          <div class="sc-content">
            <div class="sc-value">{{ pendingCount }}</div>
            <div class="sc-label">待批阅</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-row">
        <!-- 饼图 -->
        <div class="chart-card">
          <div class="chart-header">
            <i class="el-icon-pie-chart"></i>
            <span>分数段占比</span>
          </div>
          <div ref="pieChart" class="chart-box"></div>
        </div>

        <!-- 柱状图 -->
        <div class="chart-card">
          <div class="chart-header">
            <i class="el-icon-s-data"></i>
            <span>分数段人数分布</span>
          </div>
          <div ref="barChart" class="chart-box"></div>
        </div>
      </div>

      <!-- 分段明细表 -->
      <div class="detail-card">
        <div class="detail-header">
          <span>
            <i class="el-icon-s-order"></i> 分段明细
          </span>
          <el-tag v-if="pendingCount > 0" type="warning" size="mini">
            {{ pendingCount }} 人待批阅（已排除在统计外）
          </el-tag>
        </div>

        <el-table :data="tableData" border stripe style="width:100%" size="medium">
          <el-table-column label="分数段" align="center" width="140">
            <template slot-scope="{ row }">
              <el-tag :type="row.tagType" effect="plain" size="medium">{{ row.range }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="等级" align="center" width="80">
            <template slot-scope="{ row }">
              <span class="grade-badge" :style="{ background: row.color + '20', color: row.color }">
                {{ row.grade }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="人数" align="center" width="100">
            <template slot-scope="{ row }">
              <strong :style="{ color: row.count > 0 ? row.color : '#c0c4cc' }">
                {{ row.count }}
              </strong> 人
            </template>
          </el-table-column>

          <el-table-column label="占比分布">
            <template slot-scope="{ row }">
              <div class="progress-cell">
                <el-progress
                  :percentage="row.pct"
                  :color="row.color"
                  :stroke-width="14"
                  :show-text="false"
                  style="flex:1"
                />
                <span class="pct-num" :style="{ color: row.color }">{{ row.pct }}%</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="判定" align="center" width="100">
            <template slot-scope="{ row }">
              <el-tag :type="row.isPass ? 'success' : 'danger'" size="mini" effect="dark">
                {{ row.isPass ? '及格' : '不及格' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 底部合计 -->
        <div class="table-footer">
          <span>统计人数：<strong>{{ validCount }}</strong> 人</span>
          <span>最低分：<strong>{{ minScore }}</strong> 分</span>
          <span>最高分：<strong>{{ maxScore }}</strong> 分</span>
          <span>平均分：<strong>{{ avgScore }}</strong> 分</span>
          <span>及格率：<strong style="color:#67C23A">{{ passRate }}</strong></span>
        </div>
      </div>

    </template>
  </div>
</template>

<script>
const SEGMENTS = [
  { range: '90分及以上', key: '90+',   color: '#67C23A', tagType: 'success', grade: 'A', isPass: true },
  { range: '80-89分',   key: '80-89', color: '#409EFF', tagType: '',        grade: 'B', isPass: true },
  { range: '70-79分',   key: '70-79', color: '#909399', tagType: 'info',    grade: 'C', isPass: true },
  { range: '60-69分',   key: '60-69', color: '#E6A23C', tagType: 'warning', grade: 'D', isPass: true },
  { range: '60分以下',  key: '<60',   color: '#F56C6C', tagType: 'danger',  grade: 'F', isPass: false },
]

export default {
  data() {
    return {
      loading: true,
      isNull: false,
      examName: '',
      scores: [],
      pendingCount: 0,
      category: { '90+': 0, '80-89': 0, '70-79': 0, '60-69': 0, '<60': 0 },
      pieChartInstance: null,
      barChartInstance: null,
    }
  },
  computed: {
    validScores() {
      return this.scores.filter(s => s.etScore !== -1 && s.etScore != null)
    },
    validCount() {
      return this.validScores.length
    },
    avgScore() {
      if (!this.validCount) return '--'
      const sum = this.validScores.reduce((acc, s) => acc + (s.etScore || 0), 0)
      return (sum / this.validCount).toFixed(1)
    },
    maxScore() {
      if (!this.validCount) return '--'
      return Math.max(...this.validScores.map(s => s.etScore || 0))
    },
    minScore() {
      if (!this.validCount) return '--'
      return Math.min(...this.validScores.map(s => s.etScore || 0))
    },
    passRate() {
      if (!this.validCount) return '--'
      const passed = this.validScores.filter(s => s.etScore >= 60).length
      return ((passed / this.validCount) * 100).toFixed(1) + '%'
    },
    tableData() {
      const total = this.validCount || 1
      return SEGMENTS.map(seg => ({
        ...seg,
        count: this.category[seg.key],
        pct: Math.round((this.category[seg.key] / total) * 100),
      }))
    }
  },
  created() {
    this.examName = this.$route.query.source
    this.getScoreInfo()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
    this.pieChartInstance && this.pieChartInstance.dispose()
    this.barChartInstance && this.barChartInstance.dispose()
  },
  methods: {
    getScoreInfo() {
      const examCode = this.$route.query.examCode
      this.$axios(`/api/scores/${examCode}`).then(res => {
        this.loading = false
        const data = res.data.data
        if (!data || data.length === 0) {
          this.isNull = true
          return
        }
        this.scores = data
        data.forEach(s => {
          if (s.etScore === -1 || s.etScore == null) {
            this.pendingCount++
            return
          }
          const v = s.etScore
          const bucket = Math.floor(v / 10)
          if (bucket >= 9)      this.category['90+']++
          else if (bucket === 8) this.category['80-89']++
          else if (bucket === 7) this.category['70-79']++
          else if (bucket === 6) this.category['60-69']++
          else                   this.category['<60']++
        })
        this.$nextTick(() => {
          this.initPieChart()
          this.initBarChart()
          window.addEventListener('resize', this.onResize)
        })
      }).catch(() => {
        this.loading = false
        this.isNull = true
      })
    },

    initPieChart() {
      const el = this.$refs.pieChart
      if (!el) return
      const chart = this.$echarts.init(el)
      this.pieChartInstance = chart
      const total = this.validCount
      chart.setOption({
        color: SEGMENTS.map(s => s.color),
        tooltip: {
          trigger: 'item',
          formatter: params =>
            `<b>${params.name}</b><br/>人数：${params.value} 人<br/>占比：${params.percent}%`
        },
        graphic: [{
          type: 'text',
          left: 'center',
          top: '42%',
          style: {
            text: `${total}\n人参考`,
            textAlign: 'center',
            font: 'bold 22px "PingFang SC", sans-serif',
            fill: '#303133',
            lineHeight: 30,
          }
        }],
        legend: {
          bottom: '4%',
          left: 'center',
          itemGap: 14,
          textStyle: { fontSize: 12, color: '#606266' },
          icon: 'roundRect',
        },
        series: [{
          name: '分数段',
          type: 'pie',
          radius: ['44%', '68%'],
          center: ['50%', '46%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: 'bold', formatter: '{b}\n{c} 人' },
            itemStyle: { shadowBlur: 16, shadowColor: 'rgba(0,0,0,0.18)' }
          },
          labelLine: { show: false },
          data: SEGMENTS.map(seg => ({ name: seg.range, value: this.category[seg.key] }))
        }]
      })
    },

    initBarChart() {
      const el = this.$refs.barChart
      if (!el) return
      const chart = this.$echarts.init(el)
      this.barChartInstance = chart
      // 按分数从低到高排列（60以下在左，90+在右）
      const segs = [...SEGMENTS].reverse()
      chart.setOption({
        grid: { left: '8%', right: '6%', top: '14%', bottom: '16%' },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: params => `${params[0].axisValueLabel}：<b>${params[0].value}</b> 人`
        },
        xAxis: {
          type: 'category',
          data: segs.map(s => s.range),
          axisLabel: { fontSize: 12, color: '#606266', interval: 0 },
          axisTick: { alignWithLabel: true },
          axisLine: { lineStyle: { color: '#e4e7ed' } },
        },
        yAxis: {
          type: 'value',
          name: '人数',
          nameTextStyle: { color: '#909399', fontSize: 12 },
          axisLabel: { formatter: val => val + ' 人', color: '#909399' },
          splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' } },
          minInterval: 1,
        },
        series: [{
          name: '人数',
          type: 'bar',
          barMaxWidth: 52,
          data: segs.map(seg => ({
            value: this.category[seg.key],
            itemStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: seg.color },
                  { offset: 1, color: seg.color + '88' }
                ]
              },
              borderRadius: [6, 6, 0, 0],
            }
          })),
          label: {
            show: true,
            position: 'top',
            color: '#606266',
            fontWeight: 600,
            fontSize: 13,
            formatter: '{c} 人'
          }
        }]
      })
    },

    onResize() {
      this.pieChartInstance && this.pieChartInstance.resize()
      this.barChartInstance && this.barChartInstance.resize()
    }
  }
}
</script>

<style lang="less" scoped>
.analytics-page {
  padding: 24px 32px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 顶部导航 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.nav-center {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-icon {
  font-size: 20px;
  color: #667eea;
}
.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
}

/* 空状态 */
.empty-wrap {
  text-align: center;
  padding: 80px 20px;
  color: #c0c4cc;
}
.empty-icon {
  font-size: 72px;
  display: block;
  margin-bottom: 18px;
}
.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #909399;
  margin: 0 0 8px 0;
}
.empty-sub {
  font-size: 14px;
  color: #c0c4cc;
  margin: 0;
}

/* 统计卡片 */
.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.stat-card {
  flex: 1;
  min-width: 160px;
  background: #fff;
  border-radius: 14px;
  padding: 20px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  border-left: 4px solid var(--c);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  }
}
.sc-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--cs);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--c);
  flex-shrink: 0;
}
.sc-value {
  font-size: 26px;
  font-weight: 800;
  color: #1a1a2e;
  line-height: 1;
  margin-bottom: 4px;
}
.sc-label {
  font-size: 13px;
  color: #909399;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  overflow: hidden;
}
.chart-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #fafafa 0%, #fff 100%);

  i { color: #667eea; font-size: 17px; }
}
.chart-box {
  width: 100%;
  height: 340px;
}

/* 明细表格 */
.detail-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  overflow: hidden;
}
.detail-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fafafa 0%, #fff 100%);

  i { color: #667eea; margin-right: 6px; }
}
.grade-badge {
  display: inline-block;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 50%;
  font-size: 15px;
  font-weight: 700;
}
.progress-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 4px;
}
.pct-num {
  font-size: 13px;
  font-weight: 600;
  width: 44px;
  text-align: right;
  flex-shrink: 0;
}

/* 合计行 */
.table-footer {
  display: flex;
  gap: 28px;
  padding: 16px 20px;
  background: #f8f9fc;
  border-top: 1px solid #f0f2f5;
  font-size: 13px;
  color: #606266;
  flex-wrap: wrap;

  strong { color: #303133; }
}

/* 响应式 */
@media (max-width: 900px) {
  .analytics-page { padding: 16px; }
  .charts-row { grid-template-columns: 1fr; }
  .stat-cards { gap: 10px; }
  .stat-card { min-width: 140px; }
}
</style>
