<!-- 系统日志管理（管理员端） - 浅蓝色大气风格 -->
<template>
  <div class="system-log">
    <!-- 页面头部 -->
    <div class="log-header">
      <div class="header-left">
        <div class="header-icon-wrap">
          <i class="el-icon-document"></i>
        </div>
        <div>
          <h2>系统日志</h2>
          <p class="header-desc">查看与管理系统操作日志记录</p>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-group">
          <label class="filter-label">操作类型</label>
          <el-input v-model="searchAction" placeholder="输入操作类型" clearable
                    @clear="loadData" @keyup.enter.native="loadData" prefix-icon="el-icon-search">
          </el-input>
        </div>
        <div class="filter-group">
          <label class="filter-label">操作人</label>
          <el-input v-model="searchName" placeholder="输入操作人姓名" clearable
                    @clear="loadData" @keyup.enter.native="loadData" prefix-icon="el-icon-user">
          </el-input>
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #3B82F6, #60A5FA);">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">日志总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #10B981, #34D399);">
          <i class="el-icon-s-check"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ tableData.length }}</div>
          <div class="stat-label">当前页记录</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #F59E0B, #FBBF24);">
          <i class="el-icon-time"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ latestTime }}</div>
          <div class="stat-label">最新日志时间</div>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card-wrap">
      <div class="table-head">
        <span class="table-title"><i class="el-icon-document-copy"></i> 日志列表</span>
        <el-tooltip content="刷新" placement="top">
          <el-button size="mini" circle icon="el-icon-refresh" @click="loadData"></el-button>
        </el-tooltip>
      </div>
      <el-table :data="tableData" v-loading="loading" style="width: 100%"
                :header-cell-style="{ background: 'linear-gradient(to bottom, #F0F7FF, #E8F1FD)', color: '#1E293B', fontWeight: 600, fontSize: '14px', padding: '14px 0' }"
                :cell-style="{ padding: '14px 0', fontSize: '14px' }">
        <el-table-column prop="id" label="ID" width="70" align="center">
          <template slot-scope="scope">
            <span style="color:#94A3B8;font-weight:600;">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="110" align="center">
          <template slot-scope="scope">
            <div style="display:flex;align-items:center;justify-content:center;gap:6px;">
              <el-avatar :size="28" icon="el-icon-user" style="background:#EFF6FF;color:#3B82F6;font-size:14px;"></el-avatar>
              <span style="font-weight:500;color:#1E293B;">{{ scope.row.operatorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operatorRole" label="角色" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getRoleType(scope.row.operatorRole)" size="mini" effect="light">
              {{ getRoleLabel(scope.row.operatorRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="150" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" effect="plain" type="info" style="font-weight:500;">{{ scope.row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="280" show-overflow-tooltip>
          <template slot-scope="scope">
            <span style="color:#64748B;">{{ scope.row.detail }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" align="center">
          <template slot-scope="scope">
            <span style="font-family:monospace;color:#94A3B8;font-size:13px;">{{ scope.row.ip }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" sortable align="center">
          <template slot-scope="scope">
            <span style="color:#64748B;font-size:13px;">
              <i class="el-icon-time" style="margin-right:4px;"></i>{{ scope.row.createTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template slot-scope="scope">
            <el-tooltip content="删除此日志" placement="top">
              <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="handleDelete(scope.row.id)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="prev, pager, next, total, sizes"
          :total="total" :page-size.sync="pageSize" :current-page.sync="currentPage"
          :page-sizes="[20, 50, 100]" @current-change="loadData" @size-change="loadData">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SystemLog',
  data() {
    return {
      tableData: [],
      loading: false,
      total: 0,
      currentPage: 1,
      pageSize: 20,
      searchAction: '',
      searchName: ''
    }
  },
  computed: {
    latestTime() {
      if (this.tableData.length > 0 && this.tableData[0].createTime) {
        const t = this.tableData[0].createTime
        return t.length > 10 ? t.substring(5, 16) : t
      }
      return '--'
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        let url
        if (this.searchAction || this.searchName) {
          url = `/api/systemLog/search/${this.currentPage}/${this.pageSize}?action=${encodeURIComponent(this.searchAction)}&operatorName=${encodeURIComponent(this.searchName)}`
        } else {
          url = `/api/systemLog/all/${this.currentPage}/${this.pageSize}`
        }
        const res = await this.$axios.get(url)
        if (res.data.code === 200 && res.data.data) {
          this.tableData = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('加载日志失败')
      } finally {
        this.loading = false
      }
    },
    resetSearch() {
      this.searchAction = ''
      this.searchName = ''
      this.currentPage = 1
      this.loadData()
    },
    handleDelete(id) {
      this.$confirm('确定删除此日志？', '提示', { type: 'warning' }).then(async () => {
        try {
          await this.$axios.delete(`/api/systemLog/${id}`)
          this.$message.success('删除成功')
          this.loadData()
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    getRoleType(role) {
      const map = { admin: 'danger', teacher: '', student: 'success' }
      return map[role] || 'info'
    },
    getRoleLabel(role) {
      const map = { admin: '管理员', teacher: '教师', student: '学生' }
      return map[role] || role
    }
  }
}
</script>

<style scoped>
/* ====== 页面头部 ====== */
.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #DBEAFE;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.header-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.header-icon-wrap i {
  font-size: 24px;
  color: #fff;
}

.log-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 4px;
}

.header-desc {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

/* ====== 筛选区 ====== */
.filter-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  padding: 24px 28px;
  margin-bottom: 20px;
  border-top: 3px solid #3B82F6;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  flex: 0 0 220px;
}

.filter-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 8px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 2px;
}

/* ====== 统计卡片 ====== */
.stat-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.15);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #1E293B;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #94A3B8;
  font-weight: 500;
  margin-top: 2px;
}

/* ====== 表格区域 ====== */
.table-card-wrap {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  overflow: hidden;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid #F1F5F9;
  background: linear-gradient(to right, #F8FBFF, #FFF);
}

.table-title {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
}

.table-title i {
  color: #3B82F6;
  margin-right: 8px;
}

.pagination-wrap {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #F1F5F9;
  background: linear-gradient(to top, #FAFCFF, #FFF);
}
</style>
