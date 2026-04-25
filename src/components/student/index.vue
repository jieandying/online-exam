<!--学生考试首页-->
<template>
    <div id="student">
        <!-- 现代化导航栏 -->
        <nav class="modern-navbar">
            <div class="nav-container">
                <!-- Logo 区域 -->
                <div class="nav-brand">
                    <div class="brand-icon">
                        <img src="/static/img/exam-system-icon-simple.svg" alt="考试系统" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                        <div class="icon-fallback" style="display:none;">
                            <i class="el-icon-document"></i>
                        </div>
                    </div>
                    <h1 class="brand-title">在线考试系统</h1>
                </div>

                <!-- 导航菜单 -->
                <ul class="nav-menu">
                    <li class="nav-item">
                        <a href="javascript:;" @click="exam()" class="nav-link">
                            <i class="nav-icon el-icon-monitor"></i>
                            <span class="nav-text">考试中心</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a href="javascript:;" @click="practice()" class="nav-link">
                            <i class="nav-icon el-icon-edit-outline"></i>
                            <span class="nav-text">试卷练习</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <router-link to="/smartPractice" class="nav-link">
                            <i class="nav-icon el-icon-aim"></i>
                            <span class="nav-text">练习记录</span>
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link to="/mockExam" class="nav-link">
                            <i class="nav-icon el-icon-trophy"></i>
                            <span class="nav-text">自主测试</span>
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link to="/wrongBook" class="nav-link">
                            <i class="nav-icon el-icon-document-delete"></i>
                            <span class="nav-text">错题本</span>
                        </router-link>
                    </li>
                    <li class="nav-item has-submenu" @mouseenter="showStudyMenu = true" @mouseleave="showStudyMenu = false">
                        <a href="javascript:;" class="nav-link"
                           :class="{ 'router-link-active': isStudyRoute }">
                            <i class="nav-icon el-icon-data-line"></i>
                            <span class="nav-text">学情中心</span>
                            <i class="el-icon-arrow-down submenu-arrow" :class="{ active: showStudyMenu }"></i>
                        </a>
                        <transition name="dropdown">
                            <div class="nav-submenu" v-show="showStudyMenu">
                                <router-link to="/studyCurve" class="submenu-item">
                                    <i class="el-icon-data-line"></i>
                                    <span>学习曲线</span>
                                </router-link>
                                <router-link to="/examHistory" class="submenu-item">
                                    <i class="el-icon-time"></i>
                                    <span>考试档案</span>
                                </router-link>
                                <router-link to="/weakPoints" class="submenu-item">
                                    <i class="el-icon-cpu"></i>
                                    <span>智能分析</span>
                                </router-link>
                                <router-link to="/scoreTable" class="submenu-item">
                                    <i class="el-icon-trophy-1"></i>
                                    <span>我的成绩</span>
                                </router-link>
                            </div>
                        </transition>
                    </li>
                    <li class="nav-item">
                        <router-link to="/studentAnnouncement" class="nav-link">
                            <i class="nav-icon el-icon-bell"></i>
                            <span class="nav-text">通知公告</span>
                        </router-link>
                    </li>
                    <li class="nav-item">
                        <router-link to="/message" class="nav-link">
                            <i class="nav-icon el-icon-chat-line-round"></i>
                            <span class="nav-text">交流区</span>
                        </router-link>
                    </li>
                </ul>

                <!-- 用户区域 -->
                <div class="nav-user" @mouseenter="flag = true" @mouseleave="flag = false">
                    <div class="user-profile">
                        <el-avatar
                            class="user-avatar"
                            :size="42"
                            :src="require('@/assets/img/avatar.jpg')"
                        ></el-avatar>
                        <div class="user-info">
                            <span class="user-name">{{ user.userName }}</span>
                            <span class="user-role">{{ user.clazz ? user.clazz + '班' : '学生' }}</span>
                        </div>
                        <i class="el-icon-arrow-down user-dropdown-icon" :class="{ active: flag }"></i>
                    </div>

                    <!-- 下拉菜单 -->
                    <transition name="dropdown">
                        <div class="user-dropdown" v-show="flag">
                            <div class="dropdown-item" @click="$router.push('/studentCenter')">
                                <i class="el-icon-user"></i>
                                <span>个人中心</span>
                            </div>
                            <div class="dropdown-item" @click="goToFaceManage()">
                                <i class="el-icon-camera"></i>
                                <span>人脸管理</span>
                            </div>
                            <div class="dropdown-item" @click="manage()">
                                <i class="el-icon-key"></i>
                                <span>修改密码</span>
                            </div>
                            <div class="dropdown-divider"></div>
                            <div class="dropdown-item logout" @click="exit()">
                                <i class="el-icon-switch-button"></i>
                                <span>退出登录</span>
                            </div>
                        </div>
                    </transition>
                </div>

                <!-- 移动端菜单按钮 -->
                <div class="mobile-menu-btn" @click="toggleMobileMenu">
                    <span class="menu-line" :class="{ active: mobileMenuOpen }"></span>
                    <span class="menu-line" :class="{ active: mobileMenuOpen }"></span>
                    <span class="menu-line" :class="{ active: mobileMenuOpen }"></span>
                </div>
            </div>

            <!-- 移动端菜单 -->
            <transition name="mobile-menu">
                <div class="mobile-nav" v-show="mobileMenuOpen">
                    <ul class="mobile-menu-list">
                        <li class="mobile-menu-item">
                            <a href="javascript:;" @click="exam(); toggleMobileMenu()" class="mobile-nav-link">
                                <i class="nav-icon el-icon-monitor"></i>
                                <span>考试中心</span>
                            </a>
                        </li>
                        <li class="mobile-menu-item">
                            <a href="javascript:;" @click="practice(); toggleMobileMenu()" class="mobile-nav-link">
                                <i class="nav-icon el-icon-edit-outline"></i>
                                <span>试卷练习</span>
                            </a>
                        </li>
                        <li class="mobile-menu-item">
                            <router-link to="/scoreTable" @click.native="toggleMobileMenu" class="mobile-nav-link">
                                <i class="nav-icon el-icon-trophy-1"></i>
                                <span>我的分数</span>
                            </router-link>
                        </li>
                        <li class="mobile-menu-item">
                            <router-link to="/message" @click.native="toggleMobileMenu" class="mobile-nav-link">
                                <i class="nav-icon el-icon-chat-line-round"></i>
                                <span>交流区</span>
                            </router-link>
                        </li>
                    </ul>
                </div>
            </transition>
        </nav>
        <!--路由区域-->
        <div class="main">
            <router-view></router-view>
        </div>
        <v-footer></v-footer>
    </div>
</template>

<script>
import myFooter from "@/components/student/myFooter.vue";
import { mapState } from "vuex";
export default {
    components: {
        "v-footer": myFooter,
    },
    data() {
        return {
            flag: false,
            user: {},
            mobileMenuOpen: false,
            showStudyMenu: false,
        };
    },
    created() {
        this.userInfo();
    },
    methods: {
        exit() {
            //退出登录
            this.$router.push({ path: "/" }); //跳转到登录页面
            this.$cookies.remove("cname"); //清除cookie
            this.$cookies.remove("cid");
            this.$cookies.remove("rb_token"); //清除cookie
            this.$cookies.remove("rb_role");
            this.$cookies.remove("role");
            // 清除 localStorage
            localStorage.removeItem('user');
            localStorage.removeItem('loginType');
            console.log('学生已退出登录');
        },
        manage() {
            //跳转到修改密码页面
            this.$router.push({ path: "/manager" });
        },
        async goToFaceManage() {
            const studentId = this.$cookies.get('cid');
            try {
                const res = await this.$axios.get(`/api/face/check/${studentId}`);
                const registered = res.data.code === 200 && res.data.data && res.data.data.success;
                if (registered) {
                    // 已注册，直接进管理页（不弹注册框）
                    this.$router.push({ path: '/manager' });
                } else {
                    // 未注册，弹出注册
                    this.$router.push({ path: '/manager', query: { action: 'faceRegister' } });
                }
            } catch (e) {
                this.$router.push({ path: '/manager' });
            }
        },
        userInfo() {
            // 优先从 localStorage 读取用户信息（新登录方式）
            const userStr = localStorage.getItem('user')
            if (userStr) {
                try {
                    const userData = JSON.parse(userStr)
                    this.user.userName = userData.studentName || userData.teacherName || '学生'
                    this.user.studentId = userData.studentId || userData.cardId
                    this.user.clazz = userData.clazz || ''
                    console.log('从 localStorage 读取用户信息：', userData)
                    return
                } catch (e) {
                    console.error('解析用户信息失败：', e)
                }
            }

            // 备用：从 Cookie 读取（旧登录方式）
            let studentName = this.$cookies.get("cname")
            let studentId = this.$cookies.get("cid")
            if (studentName && studentId) {
                console.log(`从 Cookie 读取: studentId=${studentId}, studentName=${studentName}`)
                this.user.userName = studentName
                this.user.studentId = studentId
            } else {
                console.warn('未找到用户信息，请重新登录')
                // 跳转到登录页
                this.$router.push('/auth')
            }
        },
        practice() {
            //跳转练习模式
            let isPractice = true;
            this.$store.commit("practice", isPractice);
            this.$router.push({ path: "/startExam" });
        },
        exam() {
            //跳转考试模式
            let isPractice = false;
            this.$store.commit("practice", isPractice);
            this.$router.push({ path: "/student" });
        },
        toggleMobileMenu() {
            //切换移动端菜单显示状态
            this.mobileMenuOpen = !this.mobileMenuOpen;
        },
    },
    computed: {
        ...mapState(["isPractice"]),
        /**
         * 判断当前路由是否属于学情中心子路由
         * @returns {boolean} 是否在学情中心相关页面
         */
        isStudyRoute() {
            var path = this.$route.path
            return path === '/studyCurve' || path === '/weakPoints' || path === '/scoreTable' || path === '/examHistory'
        }
    },
};
</script>

<style scoped>
/* 现代化导航栏样式 - 主题适配版 */

#student {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
}

/* 导航栏主容器 */
.modern-navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.nav-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

/* Logo 区域 */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.nav-brand:hover {
  transform: translateY(-2px);
}

.brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1), rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(94, 114, 228, 0.1);
}

.brand-icon:hover {
  background: linear-gradient(135deg, rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2), rgba(var(--theme-primary-rgb, 64, 158, 255), 0.25));
  box-shadow: 0 6px 16px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
  transform: scale(1.05);
}

.brand-icon img {
  width: 32px;
  height: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.icon-fallback {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-primary, #409EFF);
  font-size: 24px;
}

.brand-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  background: var(--theme-gradient, linear-gradient(45deg, #409EFF, #66b1ff));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
  white-space: nowrap;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  position: relative;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 8px;
  color: var(--text-regular);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  white-space: nowrap;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--theme-gradient, linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(102, 177, 255, 0.1)));
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.nav-link:hover::before,
.nav-link.router-link-active::before {
  opacity: 1;
}

.nav-link:hover {
  color: var(--text-primary, #303133);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15);
}

.nav-link.router-link-active {
  color: var(--text-primary, #303133);
  background: var(--theme-gradient, linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(102, 177, 255, 0.15)));
  box-shadow: 0 4px 15px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
}

.nav-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.nav-link:hover .nav-icon {
  transform: scale(1.1);
}

.nav-text {
  font-size: 13px;
  font-weight: 500;
}

/* 用户区域 */
.nav-user {
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, rgba(var(--theme-primary-rgb, 64, 158, 255), 0.08), rgba(var(--theme-primary-rgb, 64, 158, 255), 0.08));
  border: 1px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.2);
}

.user-profile:hover {
  background: linear-gradient(135deg, rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15), rgba(var(--theme-primary-rgb, 64, 158, 255), 0.15));
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.25);
}

.user-avatar {
  border: 2px solid rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
  transition: all 0.3s ease;
}

.user-profile:hover .user-avatar {
  border-color: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.6);
  transform: scale(1.05);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.2;
}

.user-dropdown-icon {
  font-size: 14px;
  color: var(--text-regular);
  transition: all 0.3s ease;
}

.user-dropdown-icon.active {
  transform: rotate(180deg);
  color: var(--theme-primary, #409EFF);
}

/* 用户下拉菜单 */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-extra-light);
  overflow: hidden;
  backdrop-filter: blur(20px);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.dropdown-item:hover {
  background: var(--theme-gradient, linear-gradient(90deg, #409EFF, #66b1ff));
  color: white;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
}

.dropdown-item.logout:hover {
  background: linear-gradient(90deg, var(--danger-color), #ff8a95);
}

.dropdown-item i {
  font-size: 16px;
  opacity: 0.8;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-lighter);
  margin: 8px 0;
}

/* 下拉菜单动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 导航子菜单 */
.nav-item.has-submenu {
  position: relative;
}

.submenu-arrow {
  font-size: 12px;
  margin-left: 2px;
  transition: transform 0.3s ease;
  opacity: 0.6;
}

.submenu-arrow.active {
  transform: rotate(180deg);
}

.nav-submenu {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  min-width: 180px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 14px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  padding: 6px;
  z-index: 100;
}

.submenu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: var(--text-primary, #303133);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.submenu-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.08) 100%);
  color: #667eea;
  transform: translateX(4px);
}

.submenu-item.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submenu-item i {
  font-size: 16px;
  opacity: 0.8;
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.3s ease;
}

.mobile-menu-btn:hover {
  background: rgba(var(--theme-primary-rgb, 64, 158, 255), 0.1);
}

.menu-line {
  width: 24px;
  height: 3px;
  background: var(--text-primary);
  border-radius: 2px;
  transition: all 0.3s ease;
}

.menu-line.active:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.menu-line.active:nth-child(2) {
  opacity: 0;
}

.menu-line.active:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

/* 移动端导航菜单 */
.mobile-nav {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-height: calc(100vh - 64px);
  overflow-y: auto;
}

.mobile-menu-list {
  list-style: none;
  margin: 0;
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.mobile-menu-item {
  margin: 0;
}

.mobile-nav-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  border-radius: 12px;
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.02);
  text-align: center;
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  color: white;
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(var(--theme-primary-rgb, 64, 158, 255), 0.3);
}

.mobile-nav-link i {
  font-size: 22px;
}

.mobile-nav-link span {
  font-size: 12px;
  line-height: 1.2;
}

/* 移动端菜单动画 */
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.mobile-menu-enter,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 主内容区域 */
.main {
  min-height: calc(100vh - 70px);
  background: var(--theme-background, linear-gradient(135deg, #e3f2fd 0%, #e8eaf6 50%, #f3e5f5 100%));
  padding: 20px;
  position: relative;
  transition: background 0.3s ease;
}

.main::before {
  content: '';
  position: fixed;
  top: 70px;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(102, 126, 234, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(118, 75, 162, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.main > * {
  position: relative;
  z-index: 1;
}

/* 响应式设计 */

/* 大屏幕 - 隐藏图标文字 */
@media (max-width: 1400px) {
  .nav-link {
    padding: 8px 10px;
  }

  .nav-text {
    font-size: 12px;
  }

  .brand-title {
    font-size: 1.1rem;
  }
}

/* 中等屏幕 - 只显示图标 */
@media (max-width: 1200px) {
  .nav-link {
    padding: 10px;
    border-radius: 10px;
  }

  .nav-text {
    display: none;
  }

  .nav-icon {
    font-size: 18px;
  }

  .nav-menu {
    gap: 6px;
  }

  .brand-title {
    display: none;
  }
}

/* 平板 - 显示移动菜单 */
@media (max-width: 900px) {
  .nav-container {
    padding: 0 1rem;
  }

  .nav-menu {
    display: none;
  }

  .nav-user .user-info {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .user-profile {
    padding: 8px;
    border-radius: 50%;
  }

  .brand-title {
    display: block;
    font-size: 1.1rem;
  }
}

/* 手机端 */
@media (max-width: 480px) {
  .nav-container {
    height: 56px;
    padding: 0 0.75rem;
  }

  .brand-title {
    display: none;
  }

  .brand-icon {
    width: 36px;
    height: 36px;
  }

  .brand-icon img {
    width: 28px;
    height: 28px;
  }

  .main {
    min-height: calc(100vh - 56px);
    padding: 12px;
  }

  .user-avatar {
    width: 32px !important;
    height: 32px !important;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--theme-gradient, linear-gradient(135deg, #3a8ee6, #409EFF));
}

/* 其他全局重置 */
* {
  box-sizing: border-box;
}

li {
  list-style: none;
}

a {
  text-decoration: none;
}
</style>
