<!-- 高端现代化面包屑导航 -->
<template>
  <div id="premium-nav">
    <div class="breadcrumb-wrapper">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator-class="el-icon-arrow-right" class="premium-breadcrumb">
        <el-breadcrumb-item v-if="active.title" class="breadcrumb-category">
          <div class="category-item">
            <i :class="active.icon || 'el-icon-folder'"></i>
            <span>{{active.title}}</span>
          </div>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="active.item1 != null" class="breadcrumb-page">
          {{active.item1}}
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="active.item2 != null" class="breadcrumb-page">
          {{active.item2}}
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="active.item3 != null" class="breadcrumb-page">
          {{active.item3}}
        </el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 右侧操作区 -->
      <div class="nav-actions">
        <el-tooltip content="刷新页面" placement="bottom">
          <div class="action-btn refresh-btn" @click="refreshPage">
            <i class="el-icon-refresh-right"></i>
          </div>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
export default {
  data() {
    return {
      active: [],
      index1: null,
    }
  },
  computed: mapState(["menu"]),
  methods: {
    getIndex() {
      this.bus.$on('sendIndex',(data)=>{
        this.index1 = data
        this.active = this.menu[data-1]
      })
    },
    refreshPage() {
      this.$router.go(0);
    }
  },
  created() {
    this.getIndex()
  },
  beforeDestroy() {
    // this.bus.$off('sendIndex')
  },
}
</script>

<style scoped>
/* ==================== 顶级华丽面包屑导航 ==================== */
#premium-nav {
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 20px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
#premium-nav::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 200%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.05),
    transparent
  );
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 顶部装饰线 */
#premium-nav::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--theme-gradient, linear-gradient(90deg, #409EFF 0%, #66b1ff 50%, #409EFF 100%));
  background-size: 200% 100%;
  animation: gradientMove 3s ease infinite;
  border-radius: 20px 20px 0 0;
}

@keyframes gradientMove {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

#premium-nav:hover {
  box-shadow:
    0 8px 32px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border-color: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
  transform: translateY(-2px);
}

.breadcrumb-wrapper {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

/* ==================== 华丽面包屑样式 ==================== */
.premium-breadcrumb {
  font-size: 15px;
  flex: 1;
  display: flex;
  align-items: center;
}

/* 分类项 */
.breadcrumb-category {
  cursor: pointer;
  position: relative;
}

.category-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 12px;
  background: linear-gradient(135deg,
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.08),
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.06));
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1);
}

/* 分类项光晕 */
.category-item::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15),
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1));
  opacity: 0;
  transition: opacity 0.4s ease;
  border-radius: 12px;
}

.category-item:hover::before {
  opacity: 1;
}

.category-item:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 6px 16px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.25);
  border-color: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
}

.category-item i {
  font-size: 17px;
  color: var(--theme-primary, #409EFF);
  position: relative;
  z-index: 1;
  transition: transform 0.4s ease;
}

.category-item:hover i {
  transform: scale(1.15) rotate(5deg);
}

.category-item span {
  font-weight: 600;
  font-size: 14px;
  color: #606266;
  position: relative;
  z-index: 1;
  letter-spacing: 0.2px;
  white-space: nowrap;
  writing-mode: horizontal-tb;
  text-orientation: mixed;
}

.category-item:hover span {
  color: var(--theme-primary, #409EFF);
}

/* 页面项 */
.breadcrumb-page {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  padding: 10px 16px;
  border-radius: 12px;
  background: linear-gradient(135deg,
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1),
    rgba(var(--theme-primary-rgb, 64, 158, 255), 0.08));
  border: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15);
  box-shadow: 0 2px 8px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1);
  letter-spacing: 0.3px;
  white-space: nowrap;
  writing-mode: horizontal-tb;
  text-orientation: mixed;
}

/* Element UI 面包屑组件样式重写 */
::v-deep .premium-breadcrumb .el-breadcrumb__item {
  font-size: 15px;
  display: inline-flex;
  align-items: center;
}

::v-deep .premium-breadcrumb .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #303133;
  font-weight: 700;
}

/* 分隔符样式 */
::v-deep .premium-breadcrumb .el-breadcrumb__separator {
  color: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.4);
  margin: 0 12px;
  font-weight: 700;
  font-size: 16px;
  transition: all 0.3s ease;
}

::v-deep .premium-breadcrumb .el-breadcrumb__separator:hover {
  color: var(--theme-primary, #409EFF);
  transform: scale(1.2);
}

::v-deep .premium-breadcrumb .el-breadcrumb__inner {
  color: #606266;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  writing-mode: horizontal-tb;
  text-orientation: mixed;
}

::v-deep .premium-breadcrumb .el-breadcrumb__inner:hover {
  color: var(--theme-primary, #409EFF);
}

/* ==================== 华丽右侧操作区 ==================== */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-btn {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 按钮光晕效果 */
.action-btn::before {
  content: '';
  position: absolute;
  inset: -2px;
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  opacity: 0;
  filter: blur(8px);
  transition: opacity 0.4s ease;
  border-radius: 12px;
}

.action-btn:hover::before {
  opacity: 0.4;
}

.action-btn:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  transform: translateY(-3px) scale(1.1);
  box-shadow:
    0 8px 20px rgba(64, 158, 255, 0.5),
    0 2px 8px rgba(64, 158, 255, 0.3);
}

.action-btn i {
  font-size: 18px;
  color: #ffffff;
  position: relative;
  z-index: 1;
  transition: transform 0.4s ease;
}

.action-btn:hover i {
  transform: rotate(180deg) scale(1.1);
}

.refresh-btn:active {
  transform: translateY(-1px) scale(1.05);
}

.refresh-btn:active i {
  transform: rotate(360deg) scale(1.2);
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 768px) {
  #premium-nav {
    border-radius: 16px;
  }
  
  .breadcrumb-wrapper {
    height: 56px;
    padding: 0 16px;
  }
  
  .category-item span {
    display: none;
  }
  
  .category-item {
    padding: 10px;
  }
  
  .action-btn {
    width: 38px;
    height: 38px;
  }
  
  .action-btn i {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .breadcrumb-wrapper {
    height: 52px;
    padding: 0 12px;
  }
  
  .category-item,
  .action-btn {
    border-radius: 10px;
  }
}
</style>
