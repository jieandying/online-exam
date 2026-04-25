<!-- 顶级现代化顶部导航栏 - 玻璃拟态设计 -->
<template>
    <header id="topbar">
        <!-- 修改密码对话框 -->
        <el-dialog
            :append-to-body="true"
            class="modern-dialog"
            title="修改密码"
            :visible.sync="dialogVisible"
            width="480px"
            :close-on-click-modal="false"
        >
            <el-form
                status-icon
                ref="ruleForm2"
                label-width="100px"
                class="password-form"
            >
                <el-form-item label="旧密码" prop="pass">
                    <el-input
                        type="password"
                        v-model="oldPsw"
                        placeholder="请输入旧密码"
                        autocomplete="off"
                        prefix-icon="el-icon-lock"
                    ></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="pass">
                    <el-input
                        type="password"
                        v-model="newPsw"
                        placeholder="请输入新密码"
                        autocomplete="off"
                        prefix-icon="el-icon-key"
                    ></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="checkPass">
                    <el-input
                        type="password"
                        v-model="confirmNewPsw"
                        placeholder="请再次输入新密码"
                        autocomplete="off"
                        prefix-icon="el-icon-check"
                    ></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false" class="cancel-btn">取 消</el-button>
                <el-button type="primary" @click="resetPsw" class="confirm-btn">确 定</el-button>
            </span>
        </el-dialog>

        <!-- 顶部导航栏 -->
        <div class="premium-header">
            <!-- 左侧区域 -->
            <div class="header-left">
                <!-- Logo区域 -->
                <div class="logo-section" @click="index()">
                    <div class="logo-wrapper">
                        <div class="logo-ring"></div>
                        <i class="el-icon-monitor logo-icon"></i>
                    </div>
                    <div class="brand-info">
                        <span class="system-title">在线考试系统</span>
                        <span class="system-subtitle">Exam Management</span>
                    </div>
                </div>
                
                <!-- 菜单切换按钮 -->
                <div class="menu-toggle" @click="toggle()">
                    <div class="toggle-btn">
                        <i class="el-icon-s-fold toggle-icon"></i>
                    </div>
                </div>
            </div>

            <!-- 右侧区域 -->
            <div class="header-right">
                <!-- 快捷操作 -->
                <div class="quick-actions">
                    <!-- 主题切换 -->
                    <theme-switcher></theme-switcher>
                    
                    <!-- 通知中心 -->
                    <div class="action-item notification" @click="markScoresRead" style="cursor:pointer;">
                        <el-badge :value="newScoreCount" :hidden="newScoreCount === 0" class="badge-item">
                            <i class="el-icon-bell"></i>
                        </el-badge>
                    </div>
                    
                    <!-- 帮助中心 -->
                    <div class="action-item help">
                        <i class="el-icon-question"></i>
                    </div>
                </div>

                <!-- 用户信息下拉菜单 -->
                <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
                    <div class="user-profile">
                        <el-avatar class="user-avatar" :size="40">
                            <i class="el-icon-user-solid"></i>
                        </el-avatar>
                        <div class="user-details">
                            <span class="user-name">{{ user.userName }}</span>
                            <span class="user-role">{{ getRoleText() }}</span>
                        </div>
                        <i class="el-icon-arrow-down dropdown-arrow"></i>
                    </div>
                    <el-dropdown-menu slot="dropdown" class="premium-dropdown">
                        <el-dropdown-item command="profile" class="dropdown-item">
                            <i class="el-icon-user"></i>
                            <span>个人中心</span>
                        </el-dropdown-item>
                        <el-dropdown-item command="password" v-if="role == 0" class="dropdown-item">
                            <i class="el-icon-key"></i>
                            <span>修改密码</span>
                        </el-dropdown-item>
                        <el-dropdown-item command="settings" class="dropdown-item">
                            <i class="el-icon-setting"></i>
                            <span>系统设置</span>
                        </el-dropdown-item>
                        <el-dropdown-item divided command="logout" class="dropdown-item logout-item">
                            <i class="el-icon-switch-button"></i>
                            <span>退出登录</span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </div>
        </div>
    </header>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import ThemeSwitcher from './ThemeSwitcher.vue';

export default {
    components: {
        ThemeSwitcher
    },
    data() {
        return {
            login_flag: false,
            user: {
                userName: null,
                userId: null,
            },
            dialogVisible: false,
            oldPsw: "",
            newPsw: "",
            confirmNewPsw: "",
            role: 0,
            newScoreCount: 0,   // 新成绩数量（未读）
        };
    },
    created() {
        this.getUserInfo();
        // 优先从 localStorage 读取角色
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                const userData = JSON.parse(userStr);
                // role: "1" = 教师, "2" = 学生, "0" = 管理员
                this.role = parseInt(userData.role) || 0;
            } catch (e) {
                this.role = this.$cookies.get("role") || 0;
            }
        } else {
            this.role = this.$cookies.get("role") || 0;
        }
        console.log('当前角色：', this.role);
        // 学生角色才检测新成绩
        if (parseInt(this.role) === 2) {
            this.$nextTick(() => this.checkNewScores())
        }
    },
    computed: mapState(["flag", "menu"]),
    methods: {
        getRoleText() {
            const roleMap = {
                0: "管理员",
                1: "教师",
                2: "学生"
            };
            return roleMap[this.role] || "用户";
        },
        // 检测学生新成绩通知
        checkNewScores() {
            const studentId = this.$cookies.get('cid') || (localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).studentId : null)
            if (!studentId) return
            this.$axios.get(`/api/score/${studentId}`).then(res => {
                if (res.data.code === 200) {
                    const scores = res.data.data || []
                    const key = `lastScoreCount_${studentId}`
                    const lastCount = parseInt(localStorage.getItem(key) || '0')
                    if (scores.length > lastCount) {
                        this.newScoreCount = scores.length - lastCount
                    }
                }
            }).catch(() => {})
        },
        // 点击钓钉标记已读
        markScoresRead() {
            const studentId = this.$cookies.get('cid') || (localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).studentId : null)
            if (!studentId) return
            this.$axios.get(`/api/score/${studentId}`).then(res => {
                if (res.data.code === 200) {
                    const key = `lastScoreCount_${studentId}`
                    localStorage.setItem(key, (res.data.data || []).length)
                    this.newScoreCount = 0
                }
            }).catch(() => {})
        },
        handleCommand(command) {
            switch (command) {
                case 'profile':
                    this.$router.push({ path: '/teacherCenter' });
                    break;
                case 'password':
                    this.dialogVisible = true;
                    break;
                case 'settings':
                    this.$router.push({ path: '/systemSettings' });
                    break;
                case 'logout':
                    this.exit();
                    break;
            }
        },
        resetPsw() {
            if (this.oldPsw == "") {
                this.$message.warning("请输入旧密码");
                return;
            }
            if (this.newPsw == "") {
                this.$message.warning("请输入新密码");
                return;
            }
            if (this.confirmNewPsw != this.newPsw) {
                this.$message.warning("两次新密码不一致");
                return;
            }
            this.$axios(
                `/api/admin/resetPsw/${this.user.userId}/${this.oldPsw}/${this.newPsw}`
            ).then((res) => {
                let status = res.data.code;
                if (status == 200) {
                    if (res.data.data != true) {
                        this.$message.error(res.data.data);
                    } else {
                        this.$message.success("修改成功");
                        this.dialogVisible = false;
                        this.oldPsw = "";
                        this.newPsw = "";
                        this.confirmNewPsw = "";
                    }
                }
            });
        },
        showSetting() {
            this.login_flag = !this.login_flag;
        },
        ...mapMutations(["toggle"]),
        getUserInfo() {
            // 优先从 localStorage 读取用户信息（新登录方式）
            const userStr = localStorage.getItem('user');
            if (userStr) {
                try {
                    const userData = JSON.parse(userStr);
                    // 支持学生和教师两种角色
                    this.user.userName = userData.studentName || userData.teacherName || '用户';
                    this.user.userId = userData.studentId || userData.teacherId;
                    console.log('从 localStorage 读取用户信息：', userData);
                    return;
                } catch (e) {
                    console.error('解析用户信息失败：', e);
                }
            }
            
            // 备用：从 Cookie 读取（旧登录方式）
            let userName = this.$cookies.get("cname");
            let userId = this.$cookies.get("cid");
            if (userName && userId) {
                this.user.userName = userName;
                this.user.userId = userId;
                console.log('从 Cookie 读取用户信息');
            } else {
                console.warn('未找到用户信息');
            }
        },
        index() {
            this.$router.push({ path: "/index" });
        },
        exit() {
            let role = this.role;
            this.$router.push({ path: "/" });
            // 清除 Cookie
            this.$cookies.remove("cname");
            this.$cookies.remove("cid");
            this.$cookies.remove("role");
            this.$cookies.remove("rb_token");
            this.$cookies.remove("rb_role");
            // 清除 localStorage
            localStorage.removeItem('user');
            localStorage.removeItem('loginType');
            console.log('用户已退出登录');
            if (role == 0) {
                this.menu.pop();
            }
        },
    },
};
</script>

<style scoped>
/* ==================== 顶级现代化顶部导航栏 ==================== */
#topbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(20px) saturate(180%);
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
}

.premium-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 24px;
    max-width: 100%;
}

/* ==================== 左侧区域 ==================== */
.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
}

/* Logo区域 */
.logo-section {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.logo-section:hover {
    transform: translateY(-2px);
}

.logo-wrapper {
    position: relative;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-ring {
    position: absolute;
    inset: 0;
    border-radius: 12px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    opacity: 0.1;
    transition: all 0.3s ease;
}

.logo-section:hover .logo-ring {
    opacity: 0.2;
    transform: scale(1.1);
}

.logo-icon {
    position: relative;
    font-size: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: all 0.3s ease;
}

.logo-section:hover .logo-icon {
    transform: scale(1.1);
}

.brand-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.system-title {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: -0.5px;
}

.system-subtitle {
    font-size: 11px;
    color: #909399;
    font-weight: 500;
    letter-spacing: 0.5px;
    text-transform: uppercase;
}

/* 菜单切换按钮 */
.menu-toggle {
    display: flex;
    align-items: center;
}

.toggle-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: rgba(102, 126, 234, 0.08);
    cursor: pointer;
    transition: all 0.3s ease;
}

.toggle-btn:hover {
    background: rgba(102, 126, 234, 0.15);
    transform: scale(1.05);
}

.toggle-icon {
    font-size: 20px;
    color: #667eea;
    transition: transform 0.3s ease;
}

.toggle-btn:hover .toggle-icon {
    transform: rotate(180deg);
}

/* ==================== 右侧区域 ==================== */
.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

/* 快捷操作 */
.quick-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.action-item {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: rgba(102, 126, 234, 0.08);
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
}

.action-item:hover {
    background: rgba(102, 126, 234, 0.15);
    transform: translateY(-2px);
}

.action-item i {
    font-size: 18px;
    color: #667eea;
}

.badge-item {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 用户信息 */
.user-dropdown {
    cursor: pointer;
}

.user-profile {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 6px 12px 6px 6px;
    border-radius: 50px;
    background: rgba(102, 126, 234, 0.08);
    transition: all 0.3s ease;
}

.user-profile:hover {
    background: rgba(102, 126, 234, 0.15);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.user-avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    transition: all 0.3s ease;
}

.user-profile:hover .user-avatar {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.user-details {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.user-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
}

.user-role {
    font-size: 12px;
    color: #909399;
    line-height: 1.2;
}

.dropdown-arrow {
    font-size: 12px;
    color: #909399;
    transition: transform 0.3s ease;
}

.user-profile:hover .dropdown-arrow {
    transform: rotate(180deg);
    color: #667eea;
}

/* ==================== 下拉菜单样式 ==================== */
::v-deep .premium-dropdown {
    margin-top: 8px;
    border-radius: 12px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    overflow: hidden;
}

::v-deep .premium-dropdown .dropdown-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    font-size: 14px;
    color: #606266;
    transition: all 0.2s ease;
}

::v-deep .premium-dropdown .dropdown-item i {
    font-size: 16px;
    width: 20px;
    text-align: center;
}

::v-deep .premium-dropdown .dropdown-item:hover {
    background: linear-gradient(90deg, rgba(102, 126, 234, 0.1), transparent);
    color: #667eea;
    transform: translateX(4px);
}

::v-deep .premium-dropdown .logout-item:hover {
    background: linear-gradient(90deg, rgba(245, 108, 108, 0.1), transparent);
    color: #F56C6C;
}

::v-deep .premium-dropdown .popper__arrow {
    display: none;
}

/* ==================== 对话框样式 ==================== */
::v-deep .modern-dialog {
    border-radius: 16px;
    overflow: hidden;
}

::v-deep .modern-dialog .el-dialog {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

::v-deep .modern-dialog .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px 24px;
    border-bottom: none;
}

::v-deep .modern-dialog .el-dialog__title {
    color: #fff;
    font-size: 18px;
    font-weight: 600;
}

::v-deep .modern-dialog .el-dialog__headerbtn .el-dialog__close {
    color: #fff;
    font-size: 20px;
}

::v-deep .modern-dialog .el-dialog__body {
    padding: 30px 24px;
}

.password-form {
    padding: 0;
}

::v-deep .password-form .el-form-item {
    margin-bottom: 24px;
}

::v-deep .password-form .el-form-item__label {
    font-weight: 500;
    color: #606266;
}

::v-deep .password-form .el-input__inner {
    height: 44px;
    border-radius: 10px;
    border: 1.5px solid #DCDFE6;
    transition: all 0.3s ease;
}

::v-deep .password-form .el-input__inner:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

::v-deep .password-form .el-input__prefix {
    left: 12px;
}

::v-deep .password-form .el-input__prefix i {
    color: #909399;
    font-size: 16px;
}

.dialog-footer {
    display: flex;
    justify-content: center;
    gap: 12px;
    padding: 20px 24px;
    border-top: 1px solid #F2F6FC;
}

.cancel-btn,
.confirm-btn {
    min-width: 100px;
    height: 40px;
    border-radius: 10px;
    font-weight: 500;
}

.confirm-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
}

.confirm-btn:hover {
    background: linear-gradient(135deg, #7889e8 0%, #8b6ee8 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 768px) {
    .premium-header {
        padding: 0 16px;
    }
    
    .brand-info {
        display: none;
    }
    
    .quick-actions {
        display: none;
    }
    
    .user-details {
        display: none;
    }
    
    .user-profile {
        padding: 6px;
        border-radius: 50%;
    }
}
</style>
