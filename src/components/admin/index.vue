<!--
 * @Description: 
 * 
 * @Date: 2023-03-08 20:38:49
-->
// 展示组件页面
<template>
  <div id="index">
    <header1 class="topbar"></header1>
    <section class="container">
      <div class="left_side" :class="{collapsed: flag}">
        <mainLeft></mainLeft>
      </div>
      <div class="main_wrapper" :class="{collapsed: flag}">
        <navigator class="nav"></navigator>
        <div class="content-container">
          <router-view></router-view>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import header from '@/components/common/header.vue'
import mainLeft from '@/components/common/mainLeft.vue'
import navigator from '@/components/common/navigator.vue'
import '@/assets/css/admin-theme.css'
export default {
  components:{
    header1: header,
    mainLeft: mainLeft,
    navigator:navigator
  },
  data() {
    return {
      username: '许如梦'
    }
  },
  computed: mapState(['flag']),
  methods: {
    
  }
}
</script>

<style lang="less" scoped>
#index {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

#index .topbar {
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}

#index .nav {
  margin-bottom: 20px;
}

.content-container {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.5);
  min-height: calc(100vh - 200px);
  transition: all 0.3s ease;
}

.content-container:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
}

.container {
  display: flex;
  flex: 1;
  margin-top: 60px;
  background: linear-gradient(160deg, #E8F4FD 0%, #EEF2FF 30%, #F0F7FF 60%, #E8F4FD 100%);
  position: relative;
  transition: background 0.3s ease;
}

.container::before {
  content: '';
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 15% 25%, rgba(59, 130, 246, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 85% 75%, rgba(99, 102, 241, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(59, 130, 246, 0.03) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.left_side {
  width: 240px;
  height: calc(100vh - 60px);
  position: fixed;
  left: 0;
  top: 60px;
  z-index: 50;
  transition: width 0.3s;
}

.left_side.collapsed {
  width: 64px;
}

.main_wrapper {
  flex: 1;
  margin-left: 240px;
  min-height: calc(100vh - 60px);
  background: transparent;
  padding: 20px;
  transition: margin-left 0.3s;
  position: relative;
  z-index: 1;
}

/* 当菜单收缩时的样式 */
.main_wrapper.collapsed {
  margin-left: 64px;
}
</style>

