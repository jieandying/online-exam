<!-- 学生通知公告 -->
<template>
  <div class="announcement-page">
    <div class="page-header">
      <h2><i class="el-icon-bell"></i> 通知公告</h2>
      <p class="subtitle">查看系统最新公告和通知</p>
    </div>

    <div class="announcement-list">
      <div v-if="loading" class="loading-container">
        <i class="el-icon-loading"></i> 加载中...
      </div>
      <div v-else-if="announcements.length === 0" class="empty-container">
        <i class="el-icon-document-delete"></i>
        <p>暂无公告</p>
      </div>
      <template v-else>
        <div class="announcement-item" v-for="item in announcements" :key="item.id"
             :class="{ expanded: expandedId === item.id }" @click="toggleExpand(item.id)">
          <div class="item-header">
            <div class="item-left">
              <el-tag :type="getTagType(item.targetRole)" size="mini" class="role-tag">
                {{ getTargetLabel(item.targetRole) }}
              </el-tag>
              <h3 class="item-title">{{ item.title }}</h3>
            </div>
            <div class="item-right">
              <span class="item-time"><i class="el-icon-time"></i> {{ item.createTime }}</span>
              <span class="item-publisher">{{ item.publisherName }}</span>
              <i class="el-icon-arrow-down expand-icon" :class="{ rotated: expandedId === item.id }"></i>
            </div>
          </div>
          <transition name="slide">
            <div class="item-content" v-show="expandedId === item.id">
              <div class="content-text">{{ item.content }}</div>
            </div>
          </transition>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            background layout="prev, pager, next, total"
            :total="total" :page-size="pageSize" :current-page.sync="currentPage"
            @current-change="loadAnnouncements">
          </el-pagination>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentAnnouncement',
  data() {
    return {
      announcements: [],
      loading: false,
      total: 0,
      currentPage: 1,
      pageSize: 10,
      expandedId: null
    }
  },
  created() {
    this.loadAnnouncements()
  },
  methods: {
    async loadAnnouncements() {
      this.loading = true
      try {
        const res = await this.$axios.get(`/api/announcement/list/student/${this.currentPage}/${this.pageSize}`)
        if (res.data.code === 200 && res.data.data) {
          this.announcements = res.data.data.records || []
          this.total = res.data.data.total || 0
        }
      } catch (e) {
        this.$message.error('加载公告失败')
      } finally {
        this.loading = false
      }
    },
    toggleExpand(id) {
      this.expandedId = this.expandedId === id ? null : id
    },
    getTagType(role) {
      const map = { all: '', student: 'success', teacher: 'warning' }
      return map[role] || 'info'
    },
    getTargetLabel(role) {
      const map = { all: '全体', student: '学生', teacher: '教师' }
      return map[role] || role
    }
  }
}
</script>

<style scoped>
.announcement-page { max-width: 900px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; color: #303133; margin: 0 0 8px 0; display: flex; align-items: center; gap: 8px; }
.page-header .subtitle { color: #909399; font-size: 14px; margin: 0; }

.loading-container, .empty-container {
  text-align: center; padding: 60px 0; color: #909399; font-size: 16px;
}
.empty-container i { font-size: 48px; display: block; margin-bottom: 12px; }

.announcement-item {
  background: #fff; border-radius: 12px; padding: 20px 24px; margin-bottom: 12px;
  cursor: pointer; transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04); border: 1px solid #f0f0f0;
}
.announcement-item:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.announcement-item.expanded { border-color: var(--theme-primary, #409EFF); }

.item-header { display: flex; justify-content: space-between; align-items: center; }
.item-left { display: flex; align-items: center; gap: 12px; flex: 1; }
.item-title { margin: 0; font-size: 15px; color: #303133; font-weight: 500; }
.item-right { display: flex; align-items: center; gap: 16px; color: #909399; font-size: 13px; }
.expand-icon { transition: transform 0.3s ease; }
.expand-icon.rotated { transform: rotate(180deg); }

.item-content { padding-top: 16px; margin-top: 16px; border-top: 1px solid #f0f0f0; }
.content-text { color: #606266; font-size: 14px; line-height: 1.8; white-space: pre-wrap; }

.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; overflow: hidden; }
.slide-enter, .slide-leave-to { opacity: 0; max-height: 0; padding-top: 0; margin-top: 0; }

.pagination-wrap { text-align: center; margin-top: 24px; }
</style>
