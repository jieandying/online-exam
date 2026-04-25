<!--顶级现代化侧边栏 - 玻璃拟态 + 3D效果-->
<template>
  <div id="premium-sidebar">
    <!-- Logo区域 -->
    <div class="sidebar-header" v-if="!flag">
      <div class="logo-container">
        <div class="logo-ring">
          <div class="logo-inner">
            <img class="logo-img" src="/static/img/exam-system-icon-simple.svg" alt="考试系统" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
            <div class="logo-fallback" style="display:none;">
              <i class="el-icon-document"></i>
            </div>
          </div>
        </div>
        <div class="brand-text">
          <h1 class="brand-title">考试系统</h1>
          <p class="brand-subtitle">Exam System</p>
        </div>
      </div>
    </div>
    
    <!-- 收缩状态Logo -->
    <div class="sidebar-header-collapsed" v-else>
      <div class="logo-mini">
        <img class="logo-img-small" src="/static/img/exam-system-icon-simple.svg" alt="考试系统" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
        <div class="logo-fallback-small" style="display:none;">
          <i class="el-icon-document"></i>
        </div>
      </div>
    </div>
    
    <!-- 菜单区域 -->
    <el-menu
      :default-active="this.$route.path"
      class="premium-menu" 
      @open="handleOpen" 
      @close="handleClose" 
      :collapse="flag"
      background-color="transparent"
      text-color="rgba(255, 255, 255, 0.8)"
      active-text-color="#ffffff"
      menu-trigger="click" 
      router
      unique-opened>
      
      <el-submenu v-for="(item,index) in menu" :index='item.index' :key="index" class="premium-submenu">
        <template slot="title">
          <div class="submenu-title-wrapper">
            <i :class="item.icon" class="submenu-icon"></i>
            <span slot="title" class="submenu-text">{{item.title}}</span>
          </div>
        </template>
        
        <template v-for="(list,index1) in item.content">
          <el-menu-item 
            :key="index1"
            v-if="list.item1 != null || list.item2 != null || list.item3 != null"
            @click="handleTitle(item.index)" 
            :index="list.path" 
            class="premium-menu-item">
            <div class="menu-item-wrapper">
              <i :class="list.icon" class="menu-item-icon"></i>
              <span class="menu-item-text">{{list.item1 || list.item2 || list.item3}}</span>
            </div>
          </el-menu-item>
        </template>
        
      </el-submenu>
    </el-menu>

    <!-- 底部装饰 -->
    <div class="sidebar-footer" v-if="!flag">
      <div class="footer-decoration">
        <div class="deco-line"></div>
        <div class="deco-dot"></div>
        <div class="deco-line"></div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
export default {
  name: "mainLeft",
  data() {
    return {
      
    }
  },
  computed: mapState(["flag","menu"]),
  created() {
    this.addData()
  },
  methods: {
    handleOpen(key, keyPath) {
      // console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      // console.log(key, keyPath);
    },
    handleTitle(index) {
      this.bus.$emit('sendIndex',index)
    },
    addData() {
      let role = this.$cookies.get("role")
      if(role == 0) {
        this.menu.push({
          index: '5',
          title: '教师管理',
          icon: 'el-icon-a-05',
          content:[{item1:'教师管理',path:'/teacherManage',icon:"el-icon-a-041"},{item2: '添加教师',path: '/addTeacher',icon:"el-icon-a-07"}],
        })
        this.menu.push({
          index: '11',
          title: '系统管理',
          icon: 'el-icon-setting',
          content:[{item1:'系统日志',path:'/systemLog',icon:"el-icon-document"},{item2:'系统设置',path:'/systemSettings',icon:"el-icon-s-tools"}],
        })
      }
    }
  },
}
</script>

<style scoped>
/* ==================== 顶级现代化侧边栏 ==================== */
#premium-sidebar {
  height: 100%;
  background: linear-gradient(180deg,
    #1E40AF 0%,
    #2563EB 40%,
    #3B82F6 100%);
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 4px 0 24px rgba(37, 99, 235, 0.25);
  backdrop-filter: blur(10px);
}

/* 背景装饰 */
#premium-sidebar::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.15) 0%, transparent 70%);
  animation: bgRotate 20s linear infinite;
}

@keyframes bgRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ==================== Logo区域 ==================== */
.sidebar-header {
  padding: 24px 20px;
  background: linear-gradient(135deg,
    rgba(30, 64, 175, 0.5) 0%,
    rgba(37, 99, 235, 0.4) 100%);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  position: relative;
  overflow: hidden;
}

.sidebar-header::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.05) 50%,
    transparent 70%
  );
  animation: headerShine 8s ease-in-out infinite;
}

@keyframes headerShine {
  0%, 100% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  50% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

.logo-container {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-ring {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.logo-ring::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 14px;
  background: linear-gradient(135deg, #93C5FD, #BFDBFE);
  opacity: 0.5;
  filter: blur(8px);
  transition: all 0.3s ease;
}

.sidebar-header:hover .logo-ring::before {
  opacity: 0.8;
  filter: blur(12px);
}

.logo-inner {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
}

.logo-img {
  width: 32px;
  height: 32px;
  filter: drop-shadow(0 2px 8px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.5));
  transition: transform 0.3s ease;
}

.sidebar-header:hover .logo-img {
  transform: scale(1.1) rotate(5deg);
}

.logo-fallback {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-primary, #409EFF);
  font-size: 24px;
}

.brand-text {
  flex: 1;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px 0;
  background: linear-gradient(135deg, #fff 0%, #e3f2fd 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.brand-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 500;
}

/* 收缩状态Logo */
.sidebar-header-collapsed {
  padding: 20px 12px;
  display: flex;
  justify-content: center;
  background: linear-gradient(135deg, 
    rgba(30, 64, 175, 0.5) 0%, 
    rgba(37, 99, 235, 0.4) 100%);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.logo-mini {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
}

.logo-mini::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 12px;
  background: linear-gradient(135deg, #93C5FD, #BFDBFE);
  opacity: 0.5;
  filter: blur(6px);
}

.logo-mini:hover {
  transform: scale(1.1);
}

.logo-mini:hover::before {
  opacity: 0.8;
  filter: blur(10px);
}

.logo-img-small {
  position: relative;
  width: 28px;
  height: 28px;
  filter: drop-shadow(0 2px 8px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.5));
}

.logo-fallback-small {
  position: relative;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-primary, #409EFF);
  font-size: 20px;
}

/* ==================== 菜单样式 ==================== */
.premium-menu {
  border-right: none;
  width: 100%;
  padding: 12px 8px;
}

.premium-menu:not(.el-menu--collapse) {
  width: 240px;
}

.premium-menu.el-menu--collapse {
  width: 64px;
}

/* 子菜单样式 */
.premium-submenu {
  margin-bottom: 8px;
}

::v-deep .premium-submenu .el-submenu__title {
  height: 48px;
  line-height: 48px;
  padding: 0 16px;
  border-radius: 12px;
  background: transparent;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

::v-deep .premium-submenu .el-submenu__title::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg,
    rgba(191, 219, 254, 0.2),
    rgba(147, 197, 253, 0.15));
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

::v-deep .premium-submenu .el-submenu__title:hover::before {
  opacity: 1;
}

::v-deep .premium-submenu .el-submenu__title:hover {
  background: transparent;
  transform: translateX(4px);
}

::v-deep .premium-submenu.is-active .el-submenu__title {
  background: linear-gradient(90deg,
    rgba(191, 219, 254, 0.3),
    rgba(147, 197, 253, 0.25));
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

::v-deep .premium-submenu.is-opened .el-submenu__title {
  background: linear-gradient(90deg,
    rgba(191, 219, 254, 0.2),
    transparent);
}

.submenu-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.submenu-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  width: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

::v-deep .premium-submenu .el-submenu__title:hover .submenu-icon {
  transform: scale(1.1);
  color: #fff;
}

.submenu-text {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

/* 菜单项样式 */
.premium-menu-item {
  height: 44px;
  line-height: 44px;
  padding: 0 16px 0 48px;
  margin: 4px 0;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.premium-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, #BFDBFE, #93C5FD);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.premium-menu-item:hover {
  background: linear-gradient(90deg,
    rgba(191, 219, 254, 0.2),
    transparent);
  transform: translateX(4px);
}

.premium-menu-item:hover::before {
  opacity: 1;
}

::v-deep .premium-menu-item.is-active {
  background: linear-gradient(90deg,
    rgba(191, 219, 254, 0.35),
    rgba(147, 197, 253, 0.3));
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
}

::v-deep .premium-menu-item.is-active::before {
  opacity: 1;
  box-shadow: 0 0 10px rgba(96, 165, 250, 0.8);
}

.menu-item-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-item-icon {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  width: 16px;
  text-align: center;
  transition: all 0.3s ease;
}

.premium-menu-item:hover .menu-item-icon {
  color: #fff;
  transform: scale(1.1);
}

::v-deep .premium-menu-item.is-active .menu-item-icon {
  color: #fff;
}

.menu-item-text {
  font-size: 14px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.8);
}

::v-deep .premium-menu-item.is-active .menu-item-text {
  color: #fff;
  font-weight: 500;
}

/* 收缩状态调整 */
::v-deep .premium-menu.el-menu--collapse .submenu-icon,
::v-deep .premium-menu.el-menu--collapse .menu-item-icon {
  margin-right: 0;
}

/* Element UI 默认样式重写 */
::v-deep .el-submenu__icon-arrow {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  transition: all 0.3s ease;
}

::v-deep .premium-submenu .el-submenu__title:hover .el-submenu__icon-arrow {
  color: #fff;
}

::v-deep .el-menu-item-group__title {
  display: none;
}

/* ==================== 底部装饰 ==================== */
.sidebar-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  padding: 0 20px;
}

.footer-decoration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.deco-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.2), 
    transparent);
}

.deco-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: linear-gradient(135deg, #93C5FD, #BFDBFE);
  box-shadow: 0 0 10px rgba(96, 165, 250, 0.6);
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.3); opacity: 1; }
}

/* ==================== 滚动条样式 ==================== */
#premium-sidebar::-webkit-scrollbar {
  width: 6px;
}

#premium-sidebar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg,
    rgba(147, 197, 253, 0.4),
    rgba(191, 219, 254, 0.4));
  border-radius: 3px;
  transition: background 0.3s ease;
}

#premium-sidebar::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg,
    rgba(147, 197, 253, 0.6),
    rgba(191, 219, 254, 0.6));
}

#premium-sidebar::-webkit-scrollbar-track {
  background: transparent;
}
</style>
