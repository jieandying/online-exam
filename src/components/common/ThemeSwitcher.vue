<template>
  <div class="theme-switcher">
    <el-dropdown @command="handleThemeChange" trigger="click">
      <div class="theme-trigger">
        <i class="el-icon-brush"></i>
        <span>主题</span>
      </div>
      <el-dropdown-menu slot="dropdown" class="theme-dropdown">
        <div class="theme-header">
          <i class="el-icon-picture-outline"></i>
          <span>选择主题</span>
        </div>
        <el-dropdown-item 
          v-for="(theme, key) in themes" 
          :key="key"
          :command="key"
          class="theme-item"
          :class="{ active: currentTheme === key }"
        >
          <div class="theme-preview" :style="getThemeStyle(theme)"></div>
          <span class="theme-name">{{ theme.name }}</span>
          <i v-if="currentTheme === key" class="el-icon-check theme-check"></i>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { themes, applyTheme, getCurrentTheme } from '@/utils/theme';

export default {
  name: 'ThemeSwitcher',
  data() {
    return {
      themes: themes,
      currentTheme: 'default',
    };
  },
  mounted() {
    this.currentTheme = getCurrentTheme();
  },
  methods: {
    handleThemeChange(themeName) {
      this.currentTheme = themeName;
      applyTheme(themeName);
      this.$message.success(`已切换到${themes[themeName].name}主题`);
      
      // 触发全局事件，通知其他组件主题已更改
      this.$root.$emit('theme-changed', themeName);
    },
    getThemeStyle(theme) {
      return {
        background: theme.gradient,
      };
    },
  },
};
</script>

<style scoped>
/* 高端主题切换器样式 - Premium Edition */
.theme-switcher {
  display: inline-block;
}

.theme-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(240, 147, 251, 0.08) 100%);
  color: #667eea;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(102, 126, 234, 0.15);
  position: relative;
  overflow: hidden;
}

.theme-trigger::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: left 0.5s ease;
}

.theme-trigger:hover::before {
  left: 100%;
}

.theme-trigger:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(240, 147, 251, 0.15) 100%);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.3);
}

.theme-trigger i {
  font-size: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

::v-deep .theme-dropdown {
  min-width: 240px;
  padding: 12px 0;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.theme-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a1a2e 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  margin-bottom: 8px;
}

.theme-header i {
  font-size: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

::v-deep .theme-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  margin: 4px 8px;
  border-radius: 12px;
}

::v-deep .theme-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(240, 147, 251, 0.05) 100%);
  transform: translateX(4px);
}

::v-deep .theme-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(240, 147, 251, 0.1) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.theme-preview {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.theme-preview::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, transparent 50%);
}

::v-deep .theme-item:hover .theme-preview {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

.theme-name {
  flex: 1;
  font-size: 14px;
  color: #374151;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.theme-check {
  color: #10b981;
  font-size: 18px;
  font-weight: bold;
  animation: checkBounce 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes checkBounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

::v-deep .theme-item.active .theme-name {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}
</style>
