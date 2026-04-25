<!-- 现代化交流区页面 -->
<template>
  <div class="message-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <i class="el-icon-chat-dot-round header-icon"></i>
          <h1 class="page-title">交流区</h1>
          <p class="page-subtitle">与同学们分享学习心得，互相交流进步</p>
        </div>
        <div class="stats-section">
          <div class="stat-card">
            <div class="stat-number">{{ msg.length }}</div>
            <div class="stat-label">本页消息</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ getTotalReplies() }}</div>
            <div class="stat-label">回复总数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 发表留言卡片 -->
      <div class="post-card">
        <div class="card-header">
          <div class="card-title">
            <i class="el-icon-edit-outline"></i>
            <span>发表留言</span>
          </div>
        </div>
        
        <div class="post-form">
          <div class="form-group">
            <label class="form-label">
              <i class="el-icon-document"></i>
              留言标题
            </label>
            <el-input
              v-model="title"
              placeholder="请输入您的留言标题..."
              clearable
              class="modern-input">
            </el-input>
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <i class="el-icon-chat-dot-round"></i>
              留言内容
            </label>
            <el-input
              v-model="content"
              type="textarea"
              :rows="4"
              placeholder="分享您的想法、问题或学习心得..."
              clearable
              class="modern-textarea">
            </el-input>
          </div>
          
          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="submit()" 
              class="submit-btn"
              :disabled="!title.trim() || !content.trim()">
              <i class="el-icon-s-promotion"></i>
              发表留言
            </el-button>
          </div>
        </div>
      </div>

      <!-- 留言列表卡片 -->
      <div class="messages-card">
        <div class="card-header">
          <div class="card-title">
            <i class="el-icon-chat-square"></i>
            <span>留言列表</span>
          </div>
          <div class="message-count">
            共 {{ pagination.total || 0 }} 条留言
          </div>
        </div>
        
        <div class="messages-container">
          <div class="message-grid">
            <div 
              v-for="(data, index) in msg" 
              :key="index"
              class="message-item"
              @mouseenter="enter(index)" 
              @mouseleave="leave(index)">
              
              <div class="message-card">
                <div class="message-header">
                  <div class="message-title">
                    <i class="el-icon-chat-dot-round title-icon"></i>
                    <span>{{ data.title }}</span>
                  </div>
                  <div class="message-time">
                    <i class="el-icon-time time-icon"></i>
                    <span>{{ formatTime(data.time) }}</span>
                  </div>
                </div>
                
                <div class="message-content">
                  {{ data.content }}
                </div>
                
                <!-- 回复列表 -->
                <div 
                  v-if="data.replays && data.replays.length > 0" 
                  class="replies-section">
                  <div class="replies-header">
                    <i class="el-icon-chat-line-square"></i>
                    <span>{{ data.replays.length }} 条回复</span>
                  </div>
                  <div 
                    v-for="(replayData, index2) in data.replays" 
                    :key="index2"
                    class="reply-item">
                    <i class="el-icon-right reply-icon"></i>
                    <span class="reply-content">{{ replayData.replay }}</span>
                  </div>
                </div>
                
                <!-- 回复按钮 -->
                <div class="message-actions">
                  <el-button 
                    v-show="flag && index === current"
                    @click="replay(data.id)"
                    type="text" 
                    class="reply-btn">
                    <i class="el-icon-chat-line-square"></i>
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!msg || msg.length === 0" class="empty-state">
            <i class="el-icon-chat-dot-round empty-icon"></i>
            <h3 class="empty-title">暂无留言</h3>
            <p class="empty-description">成为第一个发表留言的同学吧！</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pagination.current"
            :page-size="pagination.size"
            :total="pagination.total"
            @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  // name: 'message'
  data() {
    return {
      flag: false,
      current: 0,
      title: "",
      content: "",
      pagination: { //分页后的留言列表
        current: 1, //当前页
        total: null, //记录条数
        size: 4 //每页条数
      },
      msg: []
    }
  },
  created() {
    this.getMsg()
  },
  // watch: {
    
  // },
  methods: {
    getMsg() {
      this.$axios(`/api/messages/${this.pagination.current}/${this.pagination.size}`).then(res => {
        let status = res.data.code
        if(status == 200) {
          this.msg = res.data.data.records
          this.pagination = res.data.data
        }
      })
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = val
      this.getMsg()
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = val
      this.getMsg()
    },
    // 格式化时间显示
    formatTime(timeStr) {
      if (!timeStr) return '未知时间';
      const date = new Date(timeStr);
      const now = new Date();
      const diff = now - date;
      
      // 小于1分钟
      if (diff < 60000) return '刚刚';
      // 小于1小时
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      // 小于1天
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      // 小于7天
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
      
      // 超过7天显示具体日期
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}年${month}月${day}日`;
    },
    // 计算回复总数
    getTotalReplies() {
      if (!this.msg || this.msg.length === 0) return 0;
      return this.msg.reduce((total, message) => {
        return total + (message.replays ? message.replays.length : 0);
      }, 0);
    },
    submit() {
      let date = new Date()
      if(this.title.trim().length == 0 || this.content.trim().length == 0) { //非空判断
        this.$message({
          type: 'error',
          message: '留言标题或内容不能为空',
        })
        return;
      }
      
      this.$axios({
        url: "/api/message",
        method: "post",
        data: {
          title: this.title.trim(),
          content: this.content.trim(),
          time: date
        }
      }).then(res => {
        let code = res.data.code
        if(code == 200) {
          this.$message({
            type: "success",
            message: "留言发表成功！"
          })
          // 清空表单并刷新数据
          this.title = ""
          this.content = ""
          this.getMsg()
        }
      }).catch(error => {
        this.$message({
          type: "error",
          message: "发表失败，请重试"
        })
      })
    },
    replay(messageId) { //回复留言功能
      this.$prompt('回复留言', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/,
        inputErrorMessage: '回复不能为空'
      }).then(({ value }) => {
        let date = new Date()
        console.log(messageId)
        this.$axios({
          url: '/api/replay',
          method: 'post',
          data: {
            replay: value,
            replayTime: date,
            messageId: messageId
          }
        }).then(res => {
          this.getMsg()
        })
        this.$message({
          type: 'success',
          message: '回复成功'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消输入'
        });       
      });
    },
    enter(index) {
      this.flag = true
      this.current = index
    },
    leave(index) {
      this.flag = false;
      this.current = index;
    }
  }
}
</script>

<style lang="less" scoped>
/* 现代化交流区页面 - 紫色主题 */

// CSS 变量定义
:root {
  --message-primary: #9C27B0;        /* 紫色主色 */
  --message-light: #BA68C8;          /* 浅紫色 */
  --message-dark: #7B1FA2;           /* 深紫色 */
  --message-accent: #E1BEE7;         /* 紫色强调 */
  --chat-primary: #673AB7;           /* 聊天紫色 */
  --chat-light: #9575CD;             /* 浅聊天色 */
  --reply-color: #FF7043;            /* 回复橙色 */
  --text-primary: #2D1B69;           /* 主文字 */
  --text-regular: #5E35B1;           /* 常规文字 */
  --text-secondary: #9E9E9E;         /* 次要文字 */
  --background-light: #F3E5F5;       /* 浅色背景 */
  --border-light: #E1BEE7;           /* 浅色边框 */
}

.message-page {
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

/* 发表留言卡片 */
.post-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  overflow: hidden;
  margin-bottom: 20px;
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

.message-count {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 发表表单 */
.post-form {
  padding: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.form-label i {
  color: #409EFF;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.submit-btn {
  border-radius: 4px;
  font-weight: 500;
  font-size: 14px;
  padding: 10px 20px;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 留言列表卡片 */
.messages-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

.messages-container {
  padding: 25px;
  min-height: 400px;
}

.message-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.message-item {
  transition: all 0.3s ease;
}

.message-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

.message-item:hover .message-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.title-icon {
  color: #409EFF;
  font-size: 16px;
}

.message-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

.time-icon {
  color: #409EFF;
}

.message-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 15px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
}

/* 回复区域 */
.replies-section {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  margin: 12px 0;
  border-left: 3px solid #409EFF;
}

.replies-header {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #409EFF;
  font-weight: 500;
  margin-bottom: 8px;
}

.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 6px 0;
  border-bottom: 1px solid #e0e0e0;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-icon {
  color: #E6A23C;
  font-size: 12px;
  margin-top: 2px;
  flex-shrink: 0;
}

.reply-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

/* 留言操作 */
.message-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.reply-btn {
  font-weight: 500;
  color: #E6A23C !important;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 4px;
}

.reply-btn:hover {
  background: rgba(230, 162, 60, 0.1) !important;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 60px;
  color: #C0C4CC;
  margin-bottom: 15px;
}

.empty-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  margin: 0;
  color: #909399;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
  margin-top: 16px;
}

.pagination-wrapper /deep/ .el-pagination {
  background: #fff;
  border-radius: 12px;
  padding: 10px 20px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.pagination-wrapper /deep/ .el-pagination.is-background .el-pager li {
  background: #f5f7fa;
  border-radius: 8px;
  font-weight: 600;
  margin: 0 4px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-wrapper /deep/ .el-pagination.is-background .el-pager li:not(.disabled).active {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  color: #fff;
}

.pagination-wrapper /deep/ .el-pagination.is-background .btn-prev,
.pagination-wrapper /deep/ .el-pagination.is-background .btn-next {
  background: #f5f7fa;
  border-radius: 8px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-wrapper /deep/ .el-pagination.is-background .btn-prev:hover,
.pagination-wrapper /deep/ .el-pagination.is-background .btn-next:hover,
.pagination-wrapper /deep/ .el-pagination.is-background .el-pager li:not(.disabled):not(.active):hover {
  background: rgba(64, 158, 255, 0.15);
  color: #409EFF;
}

/* Element UI 组件样式覆盖 */
::v-deep .modern-input .el-input__inner,
::v-deep .modern-textarea .el-textarea__inner {
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  padding: 10px 15px;
  font-size: 14px;
}

::v-deep .modern-input .el-input__inner:focus,
::v-deep .modern-textarea .el-textarea__inner:focus {
  border-color: #409EFF;
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
  
  .post-form {
    padding: 20px;
  }
  
  .messages-container {
    padding: 20px;
  }
  
  .form-actions {
    justify-content: center;
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
  
  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  .pagination-wrapper {
    padding: 15px;
  }
}
</style>
