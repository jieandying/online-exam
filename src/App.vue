
<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
import { initTheme } from '@/utils/theme';

export default {
  name: 'App',
  created() {
    // 初始化主题
    initTheme();
    
    var token = this.$cookies.get("rb_token");
    var role = this.$cookies.get("rb_role");
    if(token == null || token == "" || role == null || role == null) {
      this.$router.push({path: '/' }) //跳转到首页
    }
  }
}
</script>

<style>
/* 高端现代化全局样式 - Premium Edition */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

ul {
  list-style: none;
}

a {
  text-decoration: none;
}

#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: var(--theme-background, linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%));
  color: var(--text-primary, #1a1a2e);
  font-size: 14px;
  line-height: 1.7;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

#app::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 20% 30%, rgba(102, 126, 234, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(240, 147, 251, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(118, 75, 162, 0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
  animation: ambientShift 20s ease-in-out infinite;
}

@keyframes ambientShift {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.02); }
}

/* 高端配色变量系统 */
:root {
  /* 主色调 */
  --primary-color: #667eea;
  --primary-light: #818cf8;
  --primary-lighter: #a5b4fc;
  --primary-dark: #4f46e5;
  --primary-darker: #4338ca;
  
  /* 成功色 */
  --success-color: #10b981;
  --success-light: #34d399;
  --success-dark: #059669;
  
  /* 警告色 */
  --warning-color: #f59e0b;
  --warning-light: #fbbf24;
  --warning-dark: #d97706;
  
  /* 危险色 */
  --danger-color: #ef4444;
  --danger-light: #f87171;
  --danger-dark: #dc2626;
  
  /* 信息色 */
  --info-color: #6366f1;
  --info-light: #818cf8;
  --info-dark: #4f46e5;
  
  /* 文字颜色 */
  --text-primary: #1a1a2e;
  --text-regular: #374151;
  --text-secondary: #6b7280;
  --text-placeholder: #9ca3af;
  --text-white: #ffffff;
  
  /* 边框颜色 */
  --border-lighter: #f3f4f6;
  --border-light: #e5e7eb;
  --border-base: #d1d5db;
  --border-dark: #9ca3af;
  --border-darker: #6b7280;
  
  /* 背景颜色 */
  --background-color: #f8fafc;
  --background-white: #ffffff;
  --background-light: #f1f5f9;
  --background-lighter: #f8fafc;
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.3);
  
  /* 高级阴影系统 */
  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-base: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.12);
  --shadow-xl: 0 20px 60px rgba(0, 0, 0, 0.15);
  --shadow-2xl: 0 25px 80px rgba(0, 0, 0, 0.2);
  --shadow-glow: 0 0 40px rgba(102, 126, 234, 0.3);
  --shadow-inner: inset 0 2px 4px rgba(0, 0, 0, 0.06);
  
  /* 圆角系统 */
  --radius-xs: 4px;
  --radius-sm: 6px;
  --radius-base: 10px;
  --radius-md: 14px;
  --radius-lg: 18px;
  --radius-xl: 24px;
  --radius-2xl: 32px;
  --radius-full: 9999px;
  
  /* 过渡动画 */
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-base: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-bounce: 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

/* Element UI 高端组件样式 */
.el-button {
  border-radius: var(--radius-base);
  font-weight: 600;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
  border: none;
  position: relative;
  overflow: hidden;
  letter-spacing: 0.5px;
}

.el-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
  border-radius: 50%;
}

.el-button:hover::before {
  width: 300px;
  height: 300px;
}

.el-button:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.el-button:active {
  transform: translateY(-1px);
  box-shadow: var(--shadow-base);
}

.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #7c8ff2 0%, #8a5fb3 100%);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.45);
}

.el-button--success {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border: none;
}

.el-button--success:hover {
  background: linear-gradient(135deg, #22c997 0%, #4ade80 100%);
  box-shadow: 0 8px 30px rgba(16, 185, 129, 0.45);
}

.el-button--warning {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  border: none;
}

.el-button--warning:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #fcd34d 100%);
  box-shadow: 0 8px 30px rgba(245, 158, 11, 0.45);
}

.el-button--danger {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  border: none;
}

.el-button--danger:hover {
  background: linear-gradient(135deg, #f87171 0%, #fca5a5 100%);
  box-shadow: 0 8px 30px rgba(239, 68, 68, 0.45);
}

.el-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-base);
  transition: all var(--transition-base);
  overflow: hidden;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.el-card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-6px);
  border-color: rgba(102, 126, 234, 0.2);
}

.el-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.el-card:hover::before {
  opacity: 1;
}

.el-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-base);
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
}

.el-table th {
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.08) 0%, rgba(102, 126, 234, 0.04) 100%) !important;
  color: var(--text-primary);
  font-weight: 700;
  border-bottom: 2px solid rgba(102, 126, 234, 0.15) !important;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.el-table td, .el-table th {
  padding: 16px 12px !important;
}

.el-table tr {
  transition: all var(--transition-base);
}

.el-table tr:hover > td {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.06) 0%, rgba(240, 147, 251, 0.04) 100%) !important;
}

.el-table--striped .el-table__body tr.el-table__row--striped td {
  background: rgba(102, 126, 234, 0.02) !important;
}

.el-input .el-input__inner {
  border-radius: var(--radius-base);
  border: 2px solid var(--border-light);
  transition: all var(--transition-base);
  background: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  padding: 12px 16px;
  height: auto;
}

.el-input .el-input__inner:hover {
  border-color: rgba(102, 126, 234, 0.4);
  background: rgba(255, 255, 255, 1);
}

.el-input .el-input__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.12), var(--shadow-base);
  background: rgba(255, 255, 255, 1);
}

.el-select .el-input .el-input__inner:focus {
  border-color: var(--primary-color);
}

.el-textarea__inner {
  border-radius: var(--radius-base);
  border: 2px solid var(--border-light);
  transition: all var(--transition-base);
  background: rgba(255, 255, 255, 0.9);
  padding: 14px 16px;
}

.el-textarea__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.12);
}

.el-dialog {
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  overflow: hidden;
  background: #ffffff;
  border: 1px solid var(--glass-border);
}

.el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-bottom: none;
  padding: 28px 32px 24px;
  position: relative;
  overflow: hidden;
}

.el-dialog__header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(180deg); }
}

.el-dialog__title {
  color: white !important;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.el-dialog__headerbtn {
  top: 24px;
  right: 28px;
}

.el-dialog__headerbtn .el-dialog__close {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 24px;
  font-weight: 700;
  transition: all var(--transition-base);
}

.el-dialog__headerbtn .el-dialog__close:hover {
  color: white !important;
  transform: rotate(90deg) scale(1.1);
}

.el-dialog__body {
  padding: 32px;
  background: #ffffff;
}

.el-dialog__footer {
  padding: 20px 32px 28px;
  border-top: 1px solid var(--border-light);
  background: #f8fafc;
}

/* 高端 Tag 标签样式 */
.el-tag {
  border-radius: var(--radius-full);
  padding: 6px 16px;
  font-weight: 600;
  border: none;
  box-shadow: var(--shadow-sm);
  font-size: 12px;
  letter-spacing: 0.3px;
  transition: all var(--transition-base);
}

.el-tag:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-base);
}

.el-tag--primary {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  color: #667eea;
}

.el-tag--success {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(52, 211, 153, 0.15) 100%);
  color: #10b981;
}

.el-tag--warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.12) 0%, rgba(251, 191, 36, 0.15) 100%);
  color: #f59e0b;
}

.el-tag--danger {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(248, 113, 113, 0.15) 100%);
  color: #ef4444;
}

.el-tag--info {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(129, 140, 248, 0.15) 100%);
  color: #6366f1;
}

/* 高端 Message 消息提示 */
.el-message {
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-xl) !important;
  padding: 18px 24px !important;
  min-width: 360px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  animation: messageSlideIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.el-message .el-message__content {
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.3px;
}

.el-message--success {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.95) 0%, rgba(52, 211, 153, 0.95) 100%) !important;
  color: white !important;
}

.el-message--success .el-message__content { color: white !important; }
.el-message--success .el-icon-success { color: white !important; }

.el-message--warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.95) 0%, rgba(251, 191, 36, 0.95) 100%) !important;
  color: white !important;
}

.el-message--warning .el-message__content { color: white !important; }
.el-message--warning .el-icon-warning { color: white !important; }

.el-message--error {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.95) 0%, rgba(248, 113, 113, 0.95) 100%) !important;
  color: white !important;
}

.el-message--error .el-message__content { color: white !important; }
.el-message--error .el-icon-error { color: white !important; }

.el-message--info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.95) 0%, rgba(118, 75, 162, 0.95) 100%) !important;
  color: white !important;
}

.el-message--info .el-message__content { color: white !important; }
.el-message--info .el-icon-info { color: white !important; }

/* 高端滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  border-radius: var(--radius-full);
  border: 2px solid transparent;
  background-clip: padding-box;
  transition: all var(--transition-base);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #7c8ff2 0%, #8a5fb3 50%, #f5a8fb 100%);
  background-clip: padding-box;
}

::-webkit-scrollbar-track {
  background: rgba(102, 126, 234, 0.05);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-corner {
  background-color: transparent;
}

/* 高端页面布局工具类 */
.ruoyi-container {
  padding: 24px;
  animation: pageSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

@keyframes pageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ruoyi-card {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  padding: 32px;
  box-shadow: var(--shadow-lg);
  margin-bottom: 24px;
  border: 1px solid var(--glass-border);
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.ruoyi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.ruoyi-card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-4px);
  border-color: rgba(102, 126, 234, 0.2);
}

.ruoyi-card:hover::before {
  opacity: 1;
}

.ruoyi-flex {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ruoyi-flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.ruoyi-flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.ruoyi-text-primary {
  color: var(--primary-color);
  font-weight: 500;
}

.ruoyi-text-success {
  color: var(--success-color);
  font-weight: 500;
}

.ruoyi-text-warning {
  color: var(--warning-color);
  font-weight: 500;
}

.ruoyi-text-danger {
  color: var(--danger-color);
  font-weight: 500;
}

.ruoyi-text-info {
  color: var(--info-color);
}

/* 间距工具类 */
.ruoyi-mb-8 { margin-bottom: 8px; }
.ruoyi-mb-12 { margin-bottom: 12px; }
.ruoyi-mb-16 { margin-bottom: 16px; }
.ruoyi-mb-20 { margin-bottom: 20px; }
.ruoyi-mb-24 { margin-bottom: 24px; }

.ruoyi-mt-8 { margin-top: 8px; }
.ruoyi-mt-12 { margin-top: 12px; }
.ruoyi-mt-16 { margin-top: 16px; }
.ruoyi-mt-20 { margin-top: 20px; }
.ruoyi-mt-24 { margin-top: 24px; }

.ruoyi-p-16 { padding: 16px; }
.ruoyi-p-20 { padding: 20px; }
.ruoyi-p-24 { padding: 24px; }

/* 动画工具类 */
.fade-enter-active, .fade-leave-active {
  transition: all var(--transition-base) ease;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.slide-enter-active, .slide-leave-active {
  transition: all var(--transition-base) ease;
}

.slide-enter, .slide-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

/* 加载动画 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 响应式工具类 */
@media (max-width: 768px) {
  .ruoyi-container {
    padding: 12px;
  }
  
  .ruoyi-card {
    padding: 16px;
    margin-bottom: 12px;
  }
}

/* 选择状态美化 */
::selection {
  background-color: rgba(102, 126, 234, 0.3);
  color: var(--text-primary);
}

::-moz-selection {
  background-color: rgba(102, 126, 234, 0.3);
  color: var(--text-primary);
}

/* 焦点状态美化 */
*:focus {
  outline: none;
}

*:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

/* 完全消除弹窗遮罩闪烁 */
.v-modal,
.v-modal-enter,
.v-modal-leave {
  animation: none !important;
  transition: none !important;
}
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  animation-duration: 0s !important;
}
.el-dialog__wrapper {
  outline: none;
}
</style>
