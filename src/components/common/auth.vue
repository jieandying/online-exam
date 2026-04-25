<template>
  <div class="auth-container">
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="mesh-overlay"></div>
    </div>

    <!-- 顶部导航 -->
    <div class="auth-header">
      <div class="logo-section" @click="goHome">
        <div class="logo-icon">
          <i class="el-icon-document"></i>
        </div>
        <span class="logo-text">在线考试系统</span>
      </div>
      <el-button type="text" class="back-btn" @click="goHome">
        <i class="el-icon-arrow-left"></i> 返回首页
      </el-button>
    </div>

    <!-- 主要内容 -->
    <div class="auth-main">
      <div class="auth-card">
        <!-- 左侧装饰区 -->
        <div class="auth-decoration">
          <div class="decoration-content">
            <div class="deco-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <h2>欢迎使用</h2>
            <p>在线考试系统</p>
            <div class="feature-tags">
              <div class="feature-tag">
                <i class="el-icon-check"></i>
                <span>智能出题</span>
              </div>
              <div class="feature-tag">
                <i class="el-icon-check"></i>
                <span>安全防作弊</span>
              </div>
              <div class="feature-tag">
                <i class="el-icon-check"></i>
                <span>数据分析</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区 -->
        <div class="auth-form-area">
          <!-- Tab切换 -->
          <div class="auth-tabs">
            <div 
              class="tab-item" 
              :class="{ active: authMode === 'login' }"
              @click="switchAuthMode('login')"
            >
              <span>登录</span>
            </div>
            <div 
              class="tab-item" 
              :class="{ active: authMode === 'register' }"
              @click="switchAuthMode('register')"
            >
              <span>注册</span>
            </div>
            <div class="tab-slider" :style="{ transform: `translateX(${authMode === 'login' ? '0' : '100%'})` }"></div>
          </div>

          <!-- 登录表单 -->
          <div v-show="authMode === 'login'" class="form-container">
            <div class="login-type-tabs">
              <div 
                class="type-tab" 
                :class="{ active: loginType === 'password' }"
                @click="loginType = 'password'"
              >
                <i class="el-icon-lock"></i>
                <span>密码登录</span>
              </div>
              <div 
                class="type-tab" 
                :class="{ active: loginType === 'sms' }"
                @click="loginType = 'sms'"
              >
                <i class="el-icon-mobile-phone"></i>
                <span>验证码登录</span>
              </div>
            </div>

            <!-- 密码登录 -->
            <el-form v-if="loginType === 'password'" :model="loginForm" class="auth-form">
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="loginForm.username" 
                  placeholder="用户名/手机号"
                  prefix-icon="el-icon-user"
                  @keyup.enter.native="handleLogin"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码"
                  prefix-icon="el-icon-lock"
                  show-password
                  @keyup.enter.native="handleLogin"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <div class="captcha-row">
                  <el-input 
                    class="ui-input captcha-input"
                    v-model="loginForm.captcha" 
                    placeholder="请输入验证码"
                    prefix-icon="el-icon-picture"
                    maxlength="4"
                    @keyup.enter.native="handleLogin"
                  ></el-input>
                  <img 
                    v-if="captchaImage" 
                    :src="captchaImage" 
                    class="captcha-image"
                    @click="getCaptcha"
                    alt="验证码"
                    title="点击刷新"
                  />
                  <div v-else class="captcha-loading" @click="getCaptcha">
                    <i class="el-icon-loading"></i>
                  </div>
                </div>
              </el-form-item>
              <div class="form-options">
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                <router-link to="/forgotPassword" class="link-text">忘记密码？</router-link>
              </div>
              <el-button 
                type="primary" 
                class="submit-button" 
                :loading="loading"
                @click="handleLogin"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form>

            <!-- 验证码登录 -->
            <el-form v-else :model="smsLoginForm" class="auth-form">
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="smsLoginForm.phone" 
                  placeholder="请输入手机号"
                  maxlength="11"
                  prefix-icon="el-icon-mobile-phone"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <div class="sms-code-row">
                  <el-input 
                    class="ui-input sms-code-input"
                    v-model="smsLoginForm.code" 
                    placeholder="短信验证码"
                    maxlength="6"
                    prefix-icon="el-icon-message"
                    @keyup.enter.native="handleSmsLogin"
                  ></el-input>
                  <button 
                    type="button"
                    class="sms-code-btn"
                    :class="{ 'is-countdown': countdown > 0 }"
                    :disabled="countdown > 0"
                    @click="sendLoginCode"
                  >
                    <i v-if="countdown <= 0" class="el-icon-message"></i>
                    <span>{{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}</span>
                  </button>
                </div>
              </el-form-item>
              <el-button 
                type="primary" 
                class="submit-button" 
                :loading="loading"
                @click="handleSmsLogin"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form>
          </div>

          <!-- 注册表单 -->
          <div v-show="authMode === 'register'" class="form-container">
            <el-form :model="registerForm" class="auth-form">
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="registerForm.phone" 
                  placeholder="请输入手机号"
                  maxlength="11"
                  prefix-icon="el-icon-mobile-phone"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <div class="sms-code-row">
                  <el-input 
                    class="ui-input sms-code-input"
                    v-model="registerForm.code" 
                    placeholder="短信验证码"
                    maxlength="6"
                    prefix-icon="el-icon-message"
                  ></el-input>
                  <button 
                    type="button"
                    class="sms-code-btn"
                    :class="{ 'is-countdown': regCountdown > 0 }"
                    :disabled="regCountdown > 0"
                    @click="sendRegisterCode"
                  >
                    <i v-if="regCountdown <= 0" class="el-icon-message"></i>
                    <span>{{ regCountdown > 0 ? `${regCountdown}s 后重发` : '获取验证码' }}</span>
                  </button>
                </div>
              </el-form-item>
              <el-form-item>
                <div class="role-selector">
                  <div 
                    class="role-option" 
                    :class="{active: registerForm.role === 'student'}"
                    @click="registerForm.role = 'student'"
                  >
                    <i class="el-icon-reading"></i>
                    <span>学生</span>
                  </div>
                  <div 
                    class="role-option" 
                    :class="{active: registerForm.role === 'teacher'}"
                    @click="registerForm.role = 'teacher'"
                  >
                    <i class="el-icon-notebook-2"></i>
                    <span>教师</span>
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="registerForm.username" 
                  placeholder="设置用户名"
                  prefix-icon="el-icon-user"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="registerForm.password" 
                  type="password" 
                  placeholder="设置密码（6-20位）"
                  prefix-icon="el-icon-lock"
                  show-password
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-input 
                  class="ui-input"
                  v-model="registerForm.confirmPassword" 
                  type="password" 
                  placeholder="确认密码"
                  prefix-icon="el-icon-lock"
                  show-password
                  @keyup.enter.native="handleRegister"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="registerForm.agree">
                  我已阅读并同意 <a href="#" class="link-text">《用户协议》</a> 和 <a href="#" class="link-text">《隐私政策》</a>
                </el-checkbox>
              </el-form-item>
              <el-button 
                type="primary" 
                class="submit-button" 
                :loading="loading"
                @click="handleRegister"
              >
                <span v-if="!loading">立即注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="auth-footer">
      <p>© 2024 在线考试系统. All rights reserved.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AuthPage',
  data() {
    return {
      authMode: 'login', // login | register
      loginType: 'password', // password | sms
      loading: false,
      countdown: 0,
      regCountdown: 0,
      countdownTimer: null,
      regCountdownTimer: null,
      loginForm: {
        username: '',
        password: '',
        captcha: '',
        remember: false
      },
      captchaImage: '', // 图形验证码图片,
      smsLoginForm: {
        phone: '',
        code: ''
      },
      registerForm: {
        phone: '',
        code: '',
        role: 'student',
        username: '',
        password: '',
        confirmPassword: '',
        agree: false
      }
    }
  },
  methods: {
    switchAuthMode(mode) {
      this.authMode = mode
    },
    goHome() {
      this.$router.push('/')
    },
    // 获取图形验证码
    getCaptcha() {
      console.log('开始获取验证码...')
      this.$axios.get('/api/captcha/image').then((res) => {
        console.log('验证码响应：', res.data)
        if (res.data.code === 200) {
          // 后端返回的字段是 data 不是 result
          const imageData = res.data.data || res.data.result
          this.captchaImage = imageData.image
          console.log('验证码获取成功，图片长度:', this.captchaImage.length)
        } else {
          this.$message.error(res.data.message || '获取验证码失败')
          console.error('获取验证码失败：', res.data)
        }
      }).catch((error) => {
        console.error('获取验证码错误：', error)
        console.error('错误详情：', error.response)
        this.$message.error('获取验证码失败，请检查后端服务是否启动')
      })
    },
    // 发送登录验证码
    sendLoginCode() {
      if (!this.smsLoginForm.phone) {
        this.$message.warning('请输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.smsLoginForm.phone)) {
        this.$message.error('手机号格式不正确')
        return
      }
      
      // 调用后端接口
      this.$axios.post('/api/captcha/sms/send', {
        phone: this.smsLoginForm.phone
      }).then((res) => {
        if (res.data.code === 200) {
          this.$message.success('验证码已发送')
          // 开发环境下，后端会返回验证码
          if (res.data.result && res.data.result.code) {
            console.log('验证码：', res.data.result.code)
          }
          this.countdown = 60
          this.countdownTimer = setInterval(() => {
            this.countdown--
            if (this.countdown <= 0) {
              clearInterval(this.countdownTimer)
            }
          }, 1000)
        } else {
          this.$message.error(res.data.message || '发送失败')
        }
      }).catch(() => {
        this.$message.error('发送失败，请重试')
      })
    },
    // 发送注册验证码
    sendRegisterCode() {
      if (!this.registerForm.phone) {
        this.$message.warning('请输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.registerForm.phone)) {
        this.$message.error('手机号格式不正确')
        return
      }
      
      // 调用后端接口
      this.$axios.post('/api/captcha/sms/send', {
        phone: this.registerForm.phone
      }).then((res) => {
        if (res.data.code === 200) {
          this.$message.success('验证码已发送')
          // 开发环境下，后端会返回验证码
          if (res.data.result && res.data.result.code) {
            console.log('验证码：', res.data.result.code)
          }
          this.regCountdown = 60
          this.regCountdownTimer = setInterval(() => {
            this.regCountdown--
            if (this.regCountdown <= 0) {
              clearInterval(this.regCountdownTimer)
            }
          }, 1000)
        } else {
          this.$message.error(res.data.message || '发送失败')
        }
      }).catch(() => {
        this.$message.error('发送失败，请重试')
      })
    },
    // 密码登录
    handleLogin() {
      if (!this.loginForm.username) {
        this.$message.warning('请输入用户名')
        return
      }
      if (!this.loginForm.password) {
        this.$message.warning('请输入密码')
        return
      }
      if (!this.loginForm.captcha) {
        this.$message.warning('请输入验证码')
        return
      }

      this.loading = true
      // 调用后端密码登录接口
      this.$axios.post('/api/login', {
        loginType: 'password',
        username: parseInt(this.loginForm.username),
        password: this.loginForm.password,
        captcha: this.loginForm.captcha
      }).then((res) => {
        this.loading = false
        console.log('登录响应：', res.data)
        if (res.data.code === 200) {
          // 支持 data 和 result 两种字段
          const userData = res.data.data || res.data.result
          // 保存用户信息到localStorage
          localStorage.setItem('user', JSON.stringify(userData))
          localStorage.setItem('loginType', 'password')
          
          // 同时保存到Cookie（兼容旧页面）
          const role = String(userData.role)
          const userName = userData.adminName || userData.teacherName || userData.studentName || '用户'
          const userId = userData.adminId || userData.teacherId || userData.studentId
          this.$cookies.set('cname', userName)
          this.$cookies.set('cid', userId)
          this.$cookies.set('role', role)
          
          console.log('密码登录成功，用户数据：', userData)
          console.log('Cookie已保存：', { cname: userName, cid: userId, role: role })
          
          this.$message.success('登录成功！')
          setTimeout(() => {
            // 根据角色跳转
            if (role === '2') {
              console.log('跳转到学生页面 /student')
              this.$router.push('/student')
            } else {
              console.log('跳转到管理页面 /index')
              this.$router.push('/index')
            }
          }, 500)
        } else {
          this.$message.error(res.data.message || '登录失败')
          // 验证码错误后刷新验证码
          this.getCaptcha()
          // 清空验证码输入
          this.loginForm.captcha = ''
        }
      }).catch((error) => {
        this.loading = false
        console.error('登录错误：', error)
        console.error('错误响应：', error.response)
        this.$message.error('登录失败，请重试')
        // 刷新验证码
        this.getCaptcha()
        // 清空验证码输入
        this.loginForm.captcha = ''
      })
    },
    // 验证码登录
    handleSmsLogin() {
      if (!this.smsLoginForm.phone) {
        this.$message.warning('请输入手机号')
        return
      }
      if (!this.smsLoginForm.code) {
        this.$message.warning('请输入验证码')
        return
      }

      this.loading = true
      // 调用后端登录接口
      this.$axios.post('/api/login', {
        loginType: 'sms',
        phone: this.smsLoginForm.phone,
        smsCode: this.smsLoginForm.code
      }).then((res) => {
        this.loading = false
        if (res.data.code === 200) {
          const userData = res.data.data || res.data.result
          // 保存用户信息到localStorage
          localStorage.setItem('user', JSON.stringify(userData))
          localStorage.setItem('loginType', 'sms')
          
          // 同时保存到Cookie（兼容旧页面）
          const role = String(userData.role)
          const userName = userData.adminName || userData.teacherName || userData.studentName || '用户'
          const userId = userData.adminId || userData.teacherId || userData.studentId
          this.$cookies.set('cname', userName)
          this.$cookies.set('cid', userId)
          this.$cookies.set('role', role)
          
          console.log('验证码登录成功，用户数据：', userData)
          console.log('Cookie已保存：', { cname: userName, cid: userId, role: role })
          
          this.$message.success('登录成功！')
          setTimeout(() => {
            // 根据角色跳转
            if (role === '2') {
              console.log('跳转到学生页面 /student')
              this.$router.push('/student')
            } else {
              console.log('跳转到管理页面 /index')
              this.$router.push('/index')
            }
          }, 500)
        } else {
          this.$message.error(res.data.message || '登录失败')
        }
      }).catch((error) => {
        this.loading = false
        this.$message.error('登录失败，请重试')
        console.error('登录错误：', error)
      })
    },
    // 注册
    handleRegister() {
      if (!this.registerForm.phone) {
        this.$message.warning('请输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.registerForm.phone)) {
        this.$message.error('手机号格式不正确')
        return
      }
      if (!this.registerForm.code) {
        this.$message.warning('请输入验证码')
        return
      }
      if (!this.registerForm.role) {
        this.$message.warning('请选择角色（学生/教师）')
        return
      }
      if (!this.registerForm.username) {
        this.$message.warning('请设置用户名')
        return
      }
      if (!this.registerForm.password) {
        this.$message.warning('请设置密码')
        return
      }
      if (this.registerForm.password.length < 6 || this.registerForm.password.length > 20) {
        this.$message.error('密码长度应为6-20位')
        return
      }
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return
      }
      if (!this.registerForm.agree) {
        this.$message.warning('请阅读并同意用户协议和隐私政策')
        return
      }

      this.loading = true
      
      // 调用后端注册接口
      this.$axios.post('/api/register', {
        phone: this.registerForm.phone,
        smsCode: this.registerForm.code,
        role: this.registerForm.role,
        username: this.registerForm.username,
        password: this.registerForm.password
      }).then((res) => {
        this.loading = false
        if (res.data.code === 200) {
          this.$message.success(`注册成功！您的身份为：${this.registerForm.role === 'student' ? '学生' : '教师'}`)
          setTimeout(() => {
            // 注册成功后切换到登录页面
            this.authMode = 'login'
            this.loginType = 'sms'
            this.smsLoginForm.phone = this.registerForm.phone
          }, 1000)
        } else {
          this.$message.error(res.data.message || '注册失败')
        }
      }).catch((error) => {
        this.loading = false
        // 如果是404，说明后端没有注册接口
        if (error.response && error.response.status === 404) {
          this.$message.error('注册功能暂未开放，请联系管理员添加账号')
        } else {
          this.$message.error('注册失败，请重试')
        }
        console.error('注册错误：', error)
      })
    }
  },
  mounted() {
    // 页面加载时获取图形验证码
    this.getCaptcha()
  },
  beforeDestroy() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
    if (this.regCountdownTimer) {
      clearInterval(this.regCountdownTimer)
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: #0f0f23;
}

/* 动态背景 */
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #667eea 0%, transparent 70%);
  top: -250px;
  left: -250px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #764ba2 0%, transparent 70%);
  bottom: -200px;
  right: -200px;
  animation-delay: -7s;
}

.orb-3 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, #f093fb 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(50px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-50px, 50px) scale(0.9);
  }
}

.mesh-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
  background-size: 50px 50px;
}

/* 顶部导航 */
.auth-header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 48px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.logo-section:hover {
  transform: translateX(-4px);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.logo-icon i {
  font-size: 24px;
  color: white;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.back-btn {
  color: rgba(255, 255, 255, 0.7);
  font-size: 15px;
  transition: all 0.3s;
}

.back-btn:hover {
  color: white;
}

/* 主要内容 */
.auth-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  position: relative;
  z-index: 10;
}

.auth-card {
  width: 100%;
  max-width: 1000px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  overflow: hidden;
  min-height: 600px;
}

/* 左侧装饰 */
.auth-decoration {
  flex: 0 0 40%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-decoration::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.decoration-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.deco-icon {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.deco-icon i {
  font-size: 48px;
}

.decoration-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.decoration-content p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.feature-tags {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.feature-tag {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 12px 24px;
  border-radius: 50px;
  font-size: 15px;
  font-weight: 500;
}

.feature-tag i {
  font-size: 18px;
}

/* 右侧表单 */
.auth-form-area {
  flex: 1;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.98);
}

/* Tab切换 */
.auth-tabs {
  display: flex;
  position: relative;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 32px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  z-index: 2;
}

.tab-item.active {
  color: #667eea;
}

.tab-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

/* 登录类型切换 */
.login-type-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.type-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #666;
}

.type-tab:hover {
  background: #e8f0fe;
}

.type-tab.active {
  background: #e8f0fe;
  border-color: #667eea;
  color: #667eea;
}

/* 表单 */
.form-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-form {
  margin-top: 24px;
}

/* 增加表单项间距 */
.auth-form .el-form-item {
  margin-bottom: 20px;
}

/* 图形验证码行 */
.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 48px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid #e8e8e8;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.captcha-loading {
  width: 120px;
  height: 48px;
  border-radius: 8px;
  border: 2px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  cursor: pointer;
  font-size: 24px;
  color: #667eea;
}

.captcha-loading i {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 角色选择器 */
.role-selector {
  display: flex;
  gap: 12px;
}

.role-option {
  flex: 1;
  height: 56px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.role-option i {
  font-size: 18px;
  color: #999;
  margin-bottom: 2px;
  transition: all 0.3s;
  line-height: 1;
  display: block;
}

.role-option span {
  font-size: 13px;
  color: #666;
  font-weight: 600;
  transition: all 0.3s;
  line-height: 1;
}

.role-option:hover {
  border-color: #667eea;
  background: #f5f3ff;
}

.role-option:hover i,
.role-option:hover span {
  color: #667eea;
}

.role-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #f5f3ff, #e8f0fe);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.role-option.active i {
  color: #667eea;
  font-size: 20px;
}

.role-option.active span {
  color: #667eea;
}

/* ===== 统一输入框样式：让文字和图标完美对齐 ===== */
/* 统一 el-input 高度为 48，并让文字垂直居中 */
.ui-input /deep/ .el-input__inner {
  height: 48px !important;
  line-height: 48px !important;
  /* 关键：不要再用 padding-top/bottom=16 这种写法 */
  padding-top: 0 !important;
  padding-bottom: 0 !important;
  /* 只保留左右 padding（左边根据是否有前缀图标决定） */
  padding-right: 14px !important;
  border-radius: 12px;
  border: 2px solid #e8e8e8;
  font-size: 15px;
  transition: all 0.3s;
}

/* 有前缀图标时，文字要给图标让位 */
.ui-input /deep/ .el-input--prefix .el-input__inner {
  padding-left: 44px !important;
}

/* 前缀容器 & 图标：必须和输入框同高同 line-height 才会完美居中 */
.ui-input /deep/ .el-input__prefix,
.ui-input /deep/ .el-input__suffix {
  height: 48px !important;
  line-height: 48px !important;
  display: flex !important;
  align-items: center !important;
}

/* 图标本身也统一一下 */
.ui-input /deep/ .el-input__prefix i,
.ui-input /deep/ .el-input__suffix i {
  font-size: 18px;
  line-height: 1 !important;
}

/* focus 效果 */
.ui-input /deep/ .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

/* ===== 短信验证码行布局 ===== */
.sms-code-row {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.sms-code-input {
  flex: 1;
}

/* ===== 获取验证码按钮 - 独立精致样式 ===== */
.sms-code-btn {
  flex-shrink: 0;
  min-width: 136px;
  height: 48px;
  padding: 0 20px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
}

/* 流光扫过动效 */
.sms-code-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.25),
    transparent
  );
  transition: left 0.5s ease;
}

.sms-code-btn:hover:not(:disabled)::before {
  left: 100%;
}

.sms-code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.sms-code-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.sms-code-btn i {
  font-size: 15px;
}

/* 倒计时状态 */
.sms-code-btn.is-countdown,
.sms-code-btn:disabled {
  background: linear-gradient(135deg, #a8b5e6 0%, #b8a0d2 100%);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
  animation: countdownPulse 2s ease-in-out infinite;
}

@keyframes countdownPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.82; }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.link-text {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.link-text:hover {
  color: #764ba2;
}

.submit-button {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

/* 页脚 */
.auth-footer {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 24px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
  }
  
  .auth-decoration {
    flex: 0 0 auto;
    padding: 40px 24px;
  }
  
  .auth-form-area {
    padding: 32px 24px;
  }
  
  .auth-header {
    padding: 20px 24px;
  }
}
</style>
