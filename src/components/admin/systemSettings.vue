<!-- 系统设置（管理员端） -->
<template>
  <div class="system-settings">
    <!-- 页面头部 -->
    <div class="settings-header">
      <div class="header-left">
        <div class="header-icon-wrap">
          <i class="el-icon-setting"></i>
        </div>
        <div>
          <h2>系统设置</h2>
          <p class="header-desc">管理系统全局参数与配置信息</p>
        </div>
      </div>
    </div>

    <!-- 设置区域 -->
    <el-row :gutter="24">
      <!-- 左侧导航 -->
      <el-col :span="6">
        <div class="nav-card">
          <div class="nav-title">设置分类</div>
          <div class="nav-list">
            <div class="nav-item" :class="{ active: activeSection === 'basic' }" @click="activeSection = 'basic'">
              <i class="el-icon-house"></i><span>基本设置</span>
            </div>
            <div class="nav-item" :class="{ active: activeSection === 'exam' }" @click="activeSection = 'exam'">
              <i class="el-icon-reading"></i><span>考试设置</span>
            </div>
            <div class="nav-item" :class="{ active: activeSection === 'security' }" @click="activeSection = 'security'">
              <i class="el-icon-lock"></i><span>安全设置</span>
            </div>
            <div class="nav-item" :class="{ active: activeSection === 'notify' }" @click="activeSection = 'notify'">
              <i class="el-icon-bell"></i><span>通知设置</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="18">
        <!-- 基本设置 -->
        <div class="setting-card" v-if="activeSection === 'basic'">
          <div class="card-head">
            <i class="el-icon-house"></i>
            <span>基本设置</span>
          </div>
          <el-form :model="basicForm" label-width="140px" class="setting-form">
            <el-form-item label="系统名称">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称"></el-input>
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="basicForm.version" disabled></el-input>
            </el-form-item>
            <el-form-item label="学校名称">
              <el-input v-model="basicForm.schoolName" placeholder="请输入学校名称"></el-input>
            </el-form-item>
            <el-form-item label="管理员邮箱">
              <el-input v-model="basicForm.adminEmail" placeholder="请输入管理员邮箱" prefix-icon="el-icon-message"></el-input>
            </el-form-item>
            <el-form-item label="系统公告">
              <el-input v-model="basicForm.notice" type="textarea" :rows="4" placeholder="输入系统全局公告内容" maxlength="500" show-word-limit></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-check" @click="saveBasic">保存设置</el-button>
              <el-button icon="el-icon-refresh" @click="resetBasic">恢复默认</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 考试设置 -->
        <div class="setting-card" v-if="activeSection === 'exam'">
          <div class="card-head">
            <i class="el-icon-reading"></i>
            <span>考试设置</span>
          </div>
          <el-form :model="examForm" label-width="140px" class="setting-form">
            <el-form-item label="默认考试时长">
              <el-input-number v-model="examForm.defaultDuration" :min="10" :max="300" :step="10"></el-input-number>
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="交卷最短时间">
              <el-input-number v-model="examForm.minSubmitTime" :min="0" :max="60" :step="5"></el-input-number>
              <span class="form-tip">分钟（0表示不限制）</span>
            </el-form-item>
            <el-form-item label="及格分数线">
              <el-input-number v-model="examForm.passScore" :min="0" :max="100" :step="5"></el-input-number>
              <span class="form-tip">分（百分制）</span>
            </el-form-item>
            <el-form-item label="允许查看答案">
              <el-switch v-model="examForm.showAnswer" active-text="允许" inactive-text="禁止"></el-switch>
            </el-form-item>
            <el-form-item label="考试防作弊">
              <el-switch v-model="examForm.antiCheat" active-text="开启" inactive-text="关闭"></el-switch>
              <span class="form-tip" v-if="examForm.antiCheat">开启后将监控切屏行为</span>
            </el-form-item>
            <el-form-item label="自动阅卷">
              <el-switch v-model="examForm.autoGrade" active-text="开启" inactive-text="关闭"></el-switch>
              <span class="form-tip">客观题自动批改</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-check" @click="saveExam">保存设置</el-button>
              <el-button icon="el-icon-refresh" @click="resetExam">恢复默认</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 安全设置 -->
        <div class="setting-card" v-if="activeSection === 'security'">
          <div class="card-head">
            <i class="el-icon-lock"></i>
            <span>安全设置</span>
          </div>
          <el-form :model="securityForm" label-width="140px" class="setting-form">
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityForm.minPwdLen" :min="4" :max="20"></el-input-number>
              <span class="form-tip">个字符</span>
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-input-number v-model="securityForm.maxLoginFail" :min="0" :max="10"></el-input-number>
              <span class="form-tip">次（0表示不锁定）</span>
            </el-form-item>
            <el-form-item label="会话超时时间">
              <el-input-number v-model="securityForm.sessionTimeout" :min="10" :max="480" :step="10"></el-input-number>
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="开启人脸识别">
              <el-switch v-model="securityForm.faceRecognition" active-text="开启" inactive-text="关闭"></el-switch>
            </el-form-item>
            <el-form-item label="允许学生注册">
              <el-switch v-model="securityForm.allowRegister" active-text="允许" inactive-text="禁止"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-check" @click="saveSecurity">保存设置</el-button>
              <el-button icon="el-icon-refresh" @click="resetSecurity">恢复默认</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 通知设置 -->
        <div class="setting-card" v-if="activeSection === 'notify'">
          <div class="card-head">
            <i class="el-icon-bell"></i>
            <span>通知设置</span>
          </div>
          <el-form :model="notifyForm" label-width="140px" class="setting-form">
            <el-form-item label="考试提醒通知">
              <el-switch v-model="notifyForm.examRemind" active-text="开启" inactive-text="关闭"></el-switch>
              <span class="form-tip">考试开始前推送提醒</span>
            </el-form-item>
            <el-form-item label="提前提醒时间">
              <el-input-number v-model="notifyForm.remindMinutes" :min="5" :max="120" :step="5" :disabled="!notifyForm.examRemind"></el-input-number>
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="成绩发布通知">
              <el-switch v-model="notifyForm.scoreNotify" active-text="开启" inactive-text="关闭"></el-switch>
            </el-form-item>
            <el-form-item label="系统公告推送">
              <el-switch v-model="notifyForm.announcePush" active-text="开启" inactive-text="关闭"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-check" @click="saveNotify">保存设置</el-button>
              <el-button icon="el-icon-refresh" @click="resetNotify">恢复默认</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
const DEFAULT_BASIC = {
  systemName: '在线考试系统',
  version: 'V2.0.0',
  schoolName: '',
  adminEmail: '',
  notice: ''
}
const DEFAULT_EXAM = {
  defaultDuration: 120,
  minSubmitTime: 10,
  passScore: 60,
  showAnswer: true,
  antiCheat: true,
  autoGrade: true
}
const DEFAULT_SECURITY = {
  minPwdLen: 6,
  maxLoginFail: 5,
  sessionTimeout: 60,
  faceRecognition: true,
  allowRegister: true
}
const DEFAULT_NOTIFY = {
  examRemind: true,
  remindMinutes: 30,
  scoreNotify: true,
  announcePush: true
}

export default {
  name: 'SystemSettings',
  data() {
    return {
      activeSection: 'basic',
      basicForm: { ...DEFAULT_BASIC },
      examForm: { ...DEFAULT_EXAM },
      securityForm: { ...DEFAULT_SECURITY },
      notifyForm: { ...DEFAULT_NOTIFY }
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    loadSettings() {
      // 从 localStorage 读取已保存的设置
      try {
        const saved = JSON.parse(localStorage.getItem('systemSettings') || '{}')
        if (saved.basic) this.basicForm = { ...DEFAULT_BASIC, ...saved.basic }
        if (saved.exam) this.examForm = { ...DEFAULT_EXAM, ...saved.exam }
        if (saved.security) this.securityForm = { ...DEFAULT_SECURITY, ...saved.security }
        if (saved.notify) this.notifyForm = { ...DEFAULT_NOTIFY, ...saved.notify }
      } catch (e) { /* ignore */ }
    },
    saveToStorage() {
      localStorage.setItem('systemSettings', JSON.stringify({
        basic: this.basicForm,
        exam: this.examForm,
        security: this.securityForm,
        notify: this.notifyForm
      }))
    },
    saveBasic() {
      this.saveToStorage()
      this.$message.success('基本设置保存成功')
    },
    saveExam() {
      this.saveToStorage()
      this.$message.success('考试设置保存成功')
    },
    saveSecurity() {
      this.saveToStorage()
      this.$message.success('安全设置保存成功')
    },
    saveNotify() {
      this.saveToStorage()
      this.$message.success('通知设置保存成功')
    },
    resetBasic() {
      this.basicForm = { ...DEFAULT_BASIC }
      this.$message.info('已恢复默认基本设置')
    },
    resetExam() {
      this.examForm = { ...DEFAULT_EXAM }
      this.$message.info('已恢复默认考试设置')
    },
    resetSecurity() {
      this.securityForm = { ...DEFAULT_SECURITY }
      this.$message.info('已恢复默认安全设置')
    },
    resetNotify() {
      this.notifyForm = { ...DEFAULT_NOTIFY }
      this.$message.info('已恢复默认通知设置')
    }
  }
}
</script>

<style scoped>
/* ====== 页面头部 ====== */
.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 2px solid #DBEAFE;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.header-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.header-icon-wrap i {
  font-size: 28px;
  color: #fff;
}

.settings-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 4px;
}

.header-desc {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

/* ====== 左侧导航卡片 ====== */
.nav-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  overflow: hidden;
}

.nav-title {
  font-size: 13px;
  font-weight: 700;
  color: #94A3B8;
  padding: 20px 24px 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-list {
  padding: 0 12px 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 15px;
  font-weight: 500;
  color: #64748B;
}

.nav-item:hover {
  background: #F0F7FF;
  color: #3B82F6;
}

.nav-item.active {
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  color: #fff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.nav-item i {
  font-size: 18px;
  width: 22px;
  text-align: center;
}

/* ====== 设置内容卡片 ====== */
.setting-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  overflow: hidden;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 32px;
  background: linear-gradient(to right, #F8FBFF, #FFFFFF);
  border-bottom: 1px solid #F1F5F9;
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
}

.card-head i {
  color: #3B82F6;
  font-size: 20px;
}

.setting-form {
  padding: 32px;
  max-width: 700px;
}

.setting-form .el-form-item {
  margin-bottom: 28px;
}

.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #94A3B8;
}

/* ====== 响应式 ====== */
@media (max-width: 992px) {
  .el-col-6, .el-col-18 {
    width: 100%;
    max-width: 100%;
    flex: 0 0 100%;
  }
  .nav-card {
    margin-bottom: 20px;
  }
  .nav-list {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding: 0 12px 12px;
  }
  .nav-item {
    white-space: nowrap;
    margin-bottom: 0;
  }
}
</style>
