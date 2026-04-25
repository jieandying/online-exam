
<template>
  <div id="forgotPassword" class="forgot-container">
    <!-- 动态粒子背景 -->
    <div class="particles-background">
      <div class="particle" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
    </div>

    <div class="main-content">
      <!-- Logo -->
      <div class="brand-section">
        <div class="brand-logo">
          <i class="el-icon-lock" style="font-size: 40px; color: white;"></i>
        </div>
        <h1 class="brand-title">密码重置</h1>
      </div>

      <!-- 重置卡片 -->
      <div class="reset-card">
        <!-- 步骤指示器 -->
        <div class="steps">
          <div class="step" :class="{ active: step >= 1, done: step > 1 }">
            <div class="step-num">1</div>
            <span>验证身份</span>
          </div>
          <div class="step-line" :class="{ active: step > 1 }"></div>
          <div class="step" :class="{ active: step >= 2, done: step > 2 }">
            <div class="step-num">2</div>
            <span>验证码</span>
          </div>
          <div class="step-line" :class="{ active: step > 2 }"></div>
          <div class="step" :class="{ active: step >= 3 }">
            <div class="step-num">3</div>
            <span>设置密码</span>
          </div>
        </div>

        <!-- 步骤1：验证身份 -->
        <div class="step-content" v-if="step === 1">
          <div class="form-group">
            <label>用户类型</label>
            <div class="type-select">
              <div class="type-item" :class="{ active: formData.userType === 'student' }"
                   @click="formData.userType = 'student'">
                <i class="el-icon-user"></i>
                <span>学生</span>
              </div>
              <div class="type-item" :class="{ active: formData.userType === 'teacher' }"
                   @click="formData.userType = 'teacher'">
                <i class="el-icon-s-custom"></i>
                <span>教师</span>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>{{ formData.userType === 'student' ? '学号' : '工号' }}</label>
            <el-input
              class="ui-input"
              v-model="formData.userId"
              :placeholder="formData.userType === 'student' ? '请输入学号' : '请输入工号'"
              prefix-icon="el-icon-user"
            />
          </div>
          <div class="form-group">
            <label>注册邮箱</label>
            <el-input
              class="ui-input"
              v-model="formData.email"
              placeholder="请输入注册时的邮箱"
              prefix-icon="el-icon-message"
            />
          </div>
          <div class="form-actions">
            <el-button class="main-btn" type="primary" :loading="loading" @click="sendCode">
              发送验证码
            </el-button>
          </div>
          <div class="alt-method">
            <span>或者</span>
            <el-button type="text" @click="useCardIdMethod">使用身份证验证</el-button>
          </div>
        </div>

        <!-- 步骤1b：身份证验证 -->
        <div class="step-content" v-if="step === 11">
          <div class="form-group">
            <label>用户类型</label>
            <div class="type-select">
              <div class="type-item" :class="{ active: formData.userType === 'student' }"
                   @click="formData.userType = 'student'">
                <i class="el-icon-user"></i>
                <span>学生</span>
              </div>
              <div class="type-item" :class="{ active: formData.userType === 'teacher' }"
                   @click="formData.userType = 'teacher'">
                <i class="el-icon-s-custom"></i>
                <span>教师</span>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>{{ formData.userType === 'student' ? '学号' : '工号' }}</label>
            <el-input
              class="ui-input"
              v-model="formData.userId"
              :placeholder="formData.userType === 'student' ? '请输入学号' : '请输入工号'"
              prefix-icon="el-icon-user"
            />
          </div>
          <div class="form-group">
            <label>身份证后6位</label>
            <el-input
              class="ui-input"
              v-model="formData.cardId"
              placeholder="请输入身份证号后6位"
              maxlength="6"
              prefix-icon="el-icon-postcard"
            />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <el-input
              class="ui-input"
              v-model="formData.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              prefix-icon="el-icon-lock"
              show-password
            />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <el-input
              class="ui-input"
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </div>
          <div class="form-actions">
            <el-button @click="step = 1">返回</el-button>
            <el-button class="main-btn" type="primary" :loading="loading" @click="resetByCardId">
              立即重置
            </el-button>
          </div>
        </div>

        <!-- 步骤2：验证码 -->
        <div class="step-content" v-if="step === 2">
          <div class="code-sent-info">
            <i class="el-icon-message"></i>
            <p>验证码已发送至您的邮箱</p>
            <p class="email-hint">{{ maskEmail(formData.email) }}</p>
          </div>
          <div class="form-group">
            <label>验证码</label>
            <el-input
              class="ui-input code-input"
              v-model="formData.code"
              placeholder="请输入6位验证码"
              maxlength="6"
            >
              <template slot="append">
                <el-button :disabled="countdown > 0" @click="sendCode">
                  {{ countdown > 0 ? countdown + 's' : '重新发送' }}
                </el-button>
              </template>
            </el-input>
          </div>
          <div class="form-actions">
            <el-button @click="step = 1">返回</el-button>
            <el-button class="main-btn" type="primary" :loading="loading" @click="verifyCode">
              验证
            </el-button>
          </div>
        </div>

        <!-- 步骤3：设置新密码 -->
        <div class="step-content" v-if="step === 3">
          <div class="form-group">
            <label>新密码</label>
            <el-input
              class="ui-input"
              v-model="formData.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              prefix-icon="el-icon-lock"
              show-password
            />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <el-input
              class="ui-input"
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </div>
          <div class="password-strength" v-if="formData.newPassword">
            <div class="strength-bar">
              <div class="strength-fill" :style="{ width: passwordStrength + '%' }" :class="strengthClass"></div>
            </div>
            <span :class="strengthClass">{{ strengthText }}</span>
          </div>
          <div class="form-actions">
            <el-button class="main-btn" type="primary" :loading="loading" @click="resetPassword">
              重置密码
            </el-button>
          </div>
        </div>

        <!-- 成功页面 -->
        <div class="step-content success" v-if="step === 4">
          <div class="success-icon">
            <i class="el-icon-circle-check"></i>
          </div>
          <h3>密码重置成功！</h3>
          <p>请使用新密码登录系统</p>
          <el-button class="main-btn" type="primary" @click="goLogin">返回登录</el-button>
        </div>

        <!-- 返回登录链接 -->
        <div class="back-link" v-if="step !== 4">
          <router-link to="/">
            <i class="el-icon-arrow-left"></i>
            返回登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ForgotPassword',
  data() {
    return {
      step: 1,
      loading: false,
      countdown: 0,
      countdownTimer: null,
      formData: {
        userType: 'student',
        userId: '',
        email: '',
        code: '',
        cardId: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    passwordStrength() {
      var pwd = this.formData.newPassword
      if (!pwd) return 0
      var score = 0
      if (pwd.length >= 6) score += 25
      if (pwd.length >= 8) score += 15
      if (/[A-Z]/.test(pwd)) score += 20
      if (/[0-9]/.test(pwd)) score += 20
      if (/[^A-Za-z0-9]/.test(pwd)) score += 20
      return Math.min(score, 100)
    },
    strengthClass() {
      if (this.passwordStrength < 40) return 'weak'
      if (this.passwordStrength < 70) return 'medium'
      return 'strong'
    },
    strengthText() {
      if (this.passwordStrength < 40) return '弱'
      if (this.passwordStrength < 70) return '中等'
      return '强'
    }
  },
  beforeDestroy() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  },
  methods: {
    getParticleStyle(index) {
      const size = Math.random() * 4 + 2;
      const duration = Math.random() * 20 + 10;
      const delay = Math.random() * 5;
      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDuration: `${duration}s`,
        animationDelay: `${delay}s`,
      };
    },
    useCardIdMethod() {
      this.step = 11
    },
    maskEmail(email) {
      if (!email) return ''
      var parts = email.split('@')
      if (parts.length !== 2) return email
      var name = parts[0]
      var domain = parts[1]
      var masked = name.length > 2 ? name.substring(0, 2) + '***' : name + '***'
      return masked + '@' + domain
    },
    startCountdown() {
      var self = this
      this.countdown = 60
      this.countdownTimer = setInterval(function() {
        self.countdown--
        if (self.countdown <= 0) {
          clearInterval(self.countdownTimer)
        }
      }, 1000)
    },
    sendCode() {
      var self = this
      if (!this.formData.userId) {
        this.$message.warning('请输入' + (this.formData.userType === 'student' ? '学号' : '工号'))
        return
      }
      if (!this.formData.email) {
        this.$message.warning('请输入邮箱')
        return
      }

      this.loading = true
      this.$axios.post('/password/sendCode', {
        userId: this.formData.userId,
        email: this.formData.email,
        userType: this.formData.userType
      }).then(function(res) {
        self.loading = false
        if (res.data.code === 200) {
          self.$message.success('验证码已发送')
          self.step = 2
          self.startCountdown()
        } else {
          self.$message.error(res.data.message || '发送失败')
        }
      }).catch(function() {
        self.loading = false
        self.$message.error('发送失败，请重试')
      })
    },
    verifyCode() {
      var self = this
      if (!this.formData.code || this.formData.code.length !== 6) {
        this.$message.warning('请输入6位验证码')
        return
      }

      this.loading = true
      this.$axios.post('/password/verifyCode', {
        userId: this.formData.userId,
        code: this.formData.code,
        userType: this.formData.userType
      }).then(function(res) {
        self.loading = false
        if (res.data.code === 200) {
          self.$message.success('验证成功')
          self.step = 3
        } else {
          self.$message.error(res.data.message || '验证失败')
        }
      }).catch(function() {
        self.loading = false
        self.$message.error('验证失败')
      })
    },
    resetPassword() {
      var self = this
      if (this.formData.newPassword.length < 6) {
        this.$message.warning('密码长度不能少于6位')
        return
      }
      if (this.formData.newPassword !== this.formData.confirmPassword) {
        this.$message.warning('两次输入的密码不一致')
        return
      }

      this.loading = true
      this.$axios.post('/password/reset', {
        userId: this.formData.userId,
        newPassword: this.formData.newPassword,
        userType: this.formData.userType
      }).then(function(res) {
        self.loading = false
        if (res.data.code === 200) {
          self.step = 4
        } else {
          self.$message.error(res.data.message || '重置失败')
        }
      }).catch(function() {
        self.loading = false
        self.$message.error('重置失败')
      })
    },
    resetByCardId() {
      var self = this
      if (!this.formData.userId) {
        this.$message.warning('请输入' + (this.formData.userType === 'student' ? '学号' : '工号'))
        return
      }
      if (!this.formData.cardId || this.formData.cardId.length !== 6) {
        this.$message.warning('请输入身份证后6位')
        return
      }
      if (this.formData.newPassword.length < 6) {
        this.$message.warning('密码长度不能少于6位')
        return
      }
      if (this.formData.newPassword !== this.formData.confirmPassword) {
        this.$message.warning('两次输入的密码不一致')
        return
      }

      this.loading = true
      this.$axios.post('/password/resetByQuestion', {
        userId: this.formData.userId,
        cardId: this.formData.cardId,
        newPassword: this.formData.newPassword,
        userType: this.formData.userType
      }).then(function(res) {
        self.loading = false
        if (res.data.code === 200) {
          self.step = 4
        } else {
          self.$message.error(res.data.message || '重置失败')
        }
      }).catch(function() {
        self.loading = false
        self.$message.error('重置失败')
      })
    },
    goLogin() {
      this.$router.push('/')
    }
  }
}
</script>

<style lang="less" scoped>
.forgot-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: var(--theme-background, linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%));
  transition: background 0.3s ease;
}

/* 动态粒子背景 */
.particles-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, rgba(var(--theme-primary-rgb, 64, 158, 255), 0.5), rgba(var(--theme-primary-rgb, 64, 158, 255), 0));
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

@keyframes particleFloat {
  0% {
    transform: translateY(0) translateX(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100vh) translateX(50px) scale(0.5);
    opacity: 0;
  }
}

.main-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
}

.brand-section {
  text-align: center;
  margin-bottom: 40px;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.3);
  position: relative;
}

.brand-logo::before {
  content: '';
  position: absolute;
  inset: -4px;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  border-radius: 20px;
  opacity: 0.3;
  filter: blur(15px);
  animation: logoPulse 2s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.5;
  }
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, #333 0%, var(--theme-primary, #409EFF) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -1px;
}

.reset-card {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
  padding: 40px;
  box-shadow: 0 30px 80px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
}

.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 35px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #999;
  transition: all 0.3s;
}

.step.active {
  color: #333;
}

.step.done .step-num {
  background: linear-gradient(135deg, #67C23A, #85ce61);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.step-num {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  transition: all 0.3s;
  position: relative;
}

.step-num::before {
  content: '';
  position: absolute;
  inset: -3px;
  background: inherit;
  border-radius: 50%;
  opacity: 0;
  filter: blur(8px);
  transition: all 0.3s;
}

.step.active .step-num {
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  color: white;
  transform: scale(1.1);
}

.step.active .step-num::before {
  opacity: 0.4;
}

.step span {
  font-size: 13px;
  font-weight: 600;
}

.step-line {
  width: 60px;
  height: 3px;
  background: #e8e8e8;
  margin: 0 15px 25px;
  transition: all 0.3s;
  border-radius: 2px;
}

.step-line.active {
  background: linear-gradient(90deg, #67C23A, #85ce61);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  color: #333;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}

.type-select {
  display: flex;
  gap: 15px;
}

.type-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  border: 2px solid #e8e8e8;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.type-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  opacity: 0;
  transition: opacity 0.3s;
}

.type-item:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border-color: var(--theme-primary, #409EFF);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15);
}

.type-item.active {
  border-color: var(--theme-primary, #409EFF);
  color: white;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  box-shadow: 0 8px 24px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
}

.type-item.active::before {
  opacity: 1;
}

.type-item i {
  font-size: 28px;
  position: relative;
  z-index: 1;
}

.type-item span {
  position: relative;
  z-index: 1;
  font-weight: 600;
}

/* ===== 统一输入框样式：让文字和图标完美对齐 ===== */
/* 统一 el-input 高度为 48，并让文字垂直居中 */
.ui-input ::v-deep .el-input__inner {
  height: 48px !important;
  line-height: 48px !important;
  /* 关键：不要再用 padding-top/bottom=16 这种写法 */
  padding-top: 0 !important;
  padding-bottom: 0 !important;
  /* 只保留左右 padding（左边根据是否有前缀图标决定） */
  padding-right: 14px !important;
  background: #fafafa;
  border: 2px solid #e8e8e8;
  border-radius: 14px;
  color: #333;
  font-size: 15px;
  transition: all 0.3s;
}

/* 有前缀图标时，文字要给图标让位 */
.ui-input ::v-deep .el-input--prefix .el-input__inner {
  padding-left: 44px !important;
}

/* 前缀容器 & 图标：必须和输入框同高同 line-height 才会完美居中 */
.ui-input ::v-deep .el-input__prefix,
.ui-input ::v-deep .el-input__suffix {
  height: 48px !important;
  line-height: 48px !important;
  display: flex !important;
  align-items: center !important;
}

/* 图标本身也统一一下 */
.ui-input ::v-deep .el-input__prefix i,
.ui-input ::v-deep .el-input__suffix i {
  font-size: 18px;
  line-height: 1 !important;
}

.ui-input ::v-deep .el-input__inner::placeholder {
  color: #999;
}

.ui-input ::v-deep .el-input__inner:hover {
  border-color: #d0d0d0;
  background: white;
}

.ui-input ::v-deep .el-input__inner:focus {
  border-color: var(--theme-primary, #409EFF);
  background: white;
  box-shadow: 0 0 0 4px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1);
}

/* 验证码输入框特别样式 */
.code-input ::v-deep .el-input__inner {
  font-size: 18px !important;
  font-weight: 600;
  letter-spacing: 8px;
  text-align: center;
  padding-left: 20px !important;
}

/* append 按钮样式 */
.ui-input ::v-deep .el-input-group__append {
  border-radius: 0 14px 14px 0;
  border: 2px solid #e8e8e8;
  border-left: none;
  background: #fafafa;
  padding: 0;
}

.ui-input ::v-deep .el-input-group__append .el-button {
  height: 44px;
  border: none;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF 0%, #66b1ff 100%));
  color: white;
  font-weight: 600;
  padding: 0 20px;
  transition: all 0.3s;
}

.ui-input ::v-deep .el-input-group__append .el-button:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.ui-input ::v-deep .el-input-group__append .el-button:disabled {
  background: #dcdfe6;
  color: #909399;
  transform: none;
}

.code-sent-info {
  text-align: center;
  padding: 20px;
  margin-bottom: 20px;
}

.code-sent-info i {
  font-size: 48px;
  color: #67C23A;
  margin-bottom: 12px;
}

.code-sent-info p {
  color: #333;
  margin: 0;
  font-size: 15px;
}

.email-hint {
  font-size: 14px;
  color: #666;
  margin-top: 8px !important;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 25px;
}

.main-btn {
  flex: 1;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF 0%, #66b1ff 100%));
  border: none;
  border-radius: 14px;
  padding: 16px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 8px 24px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.main-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3), transparent);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.main-btn:hover::before {
  width: 400%;
  height: 400%;
}

.main-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.4);
}

.alt-method {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.alt-method .el-button {
  color: var(--theme-primary, #409EFF);
  font-weight: 600;
}

.alt-method .el-button:hover {
  color: var(--theme-primary-dark, #3a8ee6);
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #e8e8e8;
  border-radius: 3px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s;
}

.strength-fill.weak {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}
.strength-fill.medium {
  background: linear-gradient(90deg, #e6a23c, #f0b86e);
}
.strength-fill.strong {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.password-strength span {
  font-size: 13px;
  font-weight: 600;
}

.password-strength span.weak { color: #F56C6C; }
.password-strength span.medium { color: #E6A23C; }
.password-strength span.strong { color: #67C23A; }

.step-content.success {
  text-align: center;
  padding: 30px 0;
}

.success-icon i {
  font-size: 64px;
  color: #67C23A;
}

.step-content.success h3 {
  color: #333;
  margin: 20px 0 10px;
  font-size: 24px;
  font-weight: 700;
}

.step-content.success p {
  color: #666;
  margin-bottom: 30px;
  font-size: 15px;
}

.back-link {
  text-align: center;
  margin-top: 30px;
  padding-top: 25px;
  border-top: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15);
}

.back-link a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 40px;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF 0%, #66b1ff 100%));
  color: white;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.back-link a::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3), transparent);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.back-link a:hover::before {
  width: 400%;
  height: 400%;
}

.back-link a:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.4);
}

.back-link a i {
  font-size: 18px;
  transition: transform 0.3s;
}

.back-link a:hover i {
  transform: translateX(-3px);
}
</style>
