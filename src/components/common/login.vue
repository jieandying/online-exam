
<!-- 专业简洁型落地页 - 在线考试系统 -->
<template>
    <div id="landing-page">
        <!-- 动态粒子背景 -->
        <div class="particles-background">
            <div class="particle" v-for="i in 30" :key="i" :style="getParticleStyle(i)"></div>
        </div>

        <!-- 动态波浪背景 -->
        <div class="wave-background">
            <svg class="wave" viewBox="0 0 1440 320" preserveAspectRatio="none">
                <path fill="rgba(33, 150, 243, 0.05)" d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,138.7C960,139,1056,117,1152,106.7C1248,96,1344,96,1392,96L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
            </svg>
            <svg class="wave wave-2" viewBox="0 0 1440 320" preserveAspectRatio="none">
                <path fill="rgba(33, 150, 243, 0.03)" d="M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,213.3C672,224,768,224,864,208C960,192,1056,160,1152,154.7C1248,149,1344,171,1392,181.3L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
            </svg>
        </div>

        <!-- 顶部导航栏 -->
        <header class="top-header">
            <div class="header-container">
                <div class="logo-area">
                    <div class="logo-icon">
                        <i class="el-icon-document"></i>
                    </div>
                    <span class="logo-text">在线考试系统</span>
                </div>
                <nav class="nav-menu">
                    <a href="#features" class="nav-item">功能特色</a>
                    <a href="#about" class="nav-item">关于我们</a>
                    <a href="#contact" class="nav-item">联系我们</a>
                </nav>
                <div class="header-actions">
                    <button class="btn-login" @click="goToAuth">登录</button>
                    <button class="btn-trial" @click="goToDemo">立即试用</button>
                </div>
            </div>
        </header>

        <!-- 题库建设区域 -->
        <section class="question-bank-section" id="features">
            <div class="content-container">
                <h1 class="main-title">题库建设</h1>
                <p class="main-subtitle">强大的题库管理系统，支持多种题型和智能组卷</p>

                <!-- Tab导航 -->
                <div class="tab-navigation">
                    <div
                        v-for="(tab, index) in tabs"
                        :key="index"
                        class="tab-item"
                        :class="{ active: activeTab === index }"
                        @click="activeTab = index"
                    >
                        <i :class="tab.icon"></i>
                        <span>{{ tab.name }}</span>
                    </div>
                </div>

                <!-- 内容展示区 -->
                <div class="content-display">
                    <div class="content-left">
                        <div class="feature-list">
                            <div class="feature-item" v-for="(item, index) in currentFeatures" :key="index">
                                <div class="feature-icon">
                                    <i :class="item.icon"></i>
                                </div>
                                <div class="feature-info">
                                    <h3>{{ item.title }}</h3>
                                    <p>{{ item.desc }}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="content-right">
                        <div class="illustration-box">
                            <!-- 3D插图 -->
                            <div class="illustration-3d">
                                <div class="cube-container">
                                    <div class="cube cube-1">
                                        <i class="el-icon-document"></i>
                                    </div>
                                    <div class="cube cube-2">
                                        <i class="el-icon-edit"></i>
                                    </div>
                                    <div class="cube cube-3">
                                        <i class="el-icon-s-order"></i>
                                    </div>
                                    <div class="main-screen">
                                        <div class="screen-header"></div>
                                        <div class="screen-content">
                                            <div class="content-line"></div>
                                            <div class="content-line"></div>
                                            <div class="content-line short"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- 为什么选择我们 -->
        <section class="why-choose-section">
            <div class="content-container">
                <h2 class="section-title">为什么选择我们</h2>
                <p class="section-subtitle">专业、安全、高效的在线考试解决方案</p>

                <div class="advantages-grid">
                    <div class="advantage-card" v-for="(adv, index) in advantages" :key="index">
                        <div class="adv-icon">
                            <i :class="adv.icon"></i>
                        </div>
                        <h3>{{ adv.title }}</h3>
                        <p>{{ adv.desc }}</p>
                    </div>
                </div>
            </div>
        </section>



        <!-- 登录对话框 -->
        <el-dialog
            :visible.sync="showLoginDialog"
            width="480px"
            :show-close="true"
            custom-class="modern-login-dialog"
            center
        >
            <div slot="title" class="dialog-title">
                <div class="title-icon-wrapper">
                    <div class="title-icon">
                        <i class="el-icon-user"></i>
                    </div>
                    <div class="icon-rings">
                        <div class="ring ring-1"></div>
                        <div class="ring ring-2"></div>
                        <div class="ring ring-3"></div>
                    </div>
                </div>
                <h3>欢迎登录</h3>
                <p>在线考试系统</p>
            </div>

            <div class="login-form-wrapper">
                <!-- 智能提示 -->
                <transition name="fade">
                    <div class="smart-tip" v-if="smartTip">
                        <i class="el-icon-info"></i>
                        <span>{{ smartTip }}</span>
                    </div>
                </transition>

                <!-- 登录方式切换 -->
                <div class="login-type-switch">
                    <div
                        class="switch-item"
                        :class="{ active: loginType === 'password' }"
                        @click="loginType = 'password'"
                    >
                        <i class="el-icon-lock"></i>
                        <span>密码登录</span>
                    </div>
                    <div
                        class="switch-item"
                        :class="{ active: loginType === 'sms' }"
                        @click="loginType = 'sms'"
                    >
                        <i class="el-icon-mobile-phone"></i>
                        <span>短信登录</span>
                    </div>
                </div>

                <!-- 登录表单 -->
                <el-form :model="formData" class="login-form" ref="loginForm">
                    <template v-if="loginType === 'password'">
                        <el-form-item>
                            <el-input
                                v-model.number="formData.username"
                                placeholder="请输入用户名"
                                prefix-icon="el-icon-user"
                                size="large"
                                @focus="handleInputFocus('username')"
                                @blur="validateUsername"
                            />
                            <transition name="slide-down">
                                <div class="field-tip error" v-if="errors.username">
                                    <i class="el-icon-warning"></i>
                                    {{ errors.username }}
                                </div>
                            </transition>
                        </el-form-item>
                        <el-form-item>
                            <el-input
                                v-model="formData.password"
                                type="password"
                                placeholder="请输入密码"
                                prefix-icon="el-icon-lock"
                                show-password
                                size="large"
                                @focus="handleInputFocus('password')"
                                @blur="validatePassword"
                            />
                            <transition name="slide-down">
                                <div class="field-tip error" v-if="errors.password">
                                    <i class="el-icon-warning"></i>
                                    {{ errors.password }}
                                </div>
                            </transition>
                            <transition name="slide-down">
                                <div class="password-strength" v-if="formData.password && !errors.password">
                                    <div class="strength-bar">
                                        <div class="strength-fill" :class="passwordStrength.class" :style="{width: passwordStrength.width}"></div>
                                    </div>
                                    <span class="strength-text" :class="passwordStrength.class">{{ passwordStrength.text }}</span>
                                </div>
                            </transition>
                        </el-form-item>
                        <el-form-item class="captcha-form-item">
                            <div class="captcha-row">
                                <el-input
                                    v-model="formData.captcha"
                                    placeholder="验证码"
                                    prefix-icon="el-icon-picture"
                                    size="large"
                                    @focus="handleInputFocus('captcha')"
                                />
                                <div class="captcha-image" @click="refreshCaptcha">
                                    <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                                    <div v-else class="captcha-placeholder">
                                        <div class="captcha-code">{{ fallbackCaptcha }}</div>
                                        <i class="el-icon-refresh"></i>
                                    </div>
                                </div>
                            </div>
                        </el-form-item>
                    </template>

                    <template v-else>
                        <el-form-item>
                            <el-input
                                v-model="formData.phone"
                                placeholder="请输入手机号"
                                prefix-icon="el-icon-mobile-phone"
                                maxlength="11"
                                size="large"
                                @focus="handleInputFocus('phone')"
                                @blur="validatePhone"
                            />
                            <transition name="slide-down">
                                <div class="field-tip error" v-if="errors.phone">
                                    <i class="el-icon-warning"></i>
                                    {{ errors.phone }}
                                </div>
                            </transition>
                            <transition name="slide-down">
                                <div class="field-tip success" v-if="formData.phone && !errors.phone && formData.phone.length === 11">
                                    <i class="el-icon-success"></i>
                                    手机号格式正确
                                </div>
                            </transition>
                        </el-form-item>
                        <el-form-item class="sms-form-item">
                            <div class="sms-wrapper">
                                <el-input
                                    v-model="formData.smsCode"
                                    placeholder="短信验证码"
                                    prefix-icon="el-icon-message"
                                    maxlength="6"
                                    size="large"
                                />
                                <el-button
                                    class="sms-btn"
                                    :disabled="smsCountdown > 0"
                                    @click="sendSms"
                                >
                                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                                </el-button>
                            </div>
                        </el-form-item>
                    </template>

                    <el-form-item class="submit-form-item">
                        <el-button
                            type="primary"
                            class="submit-btn"
                            :loading="isLoading"
                            @click="handleLogin"
                            size="large"
                        >
                            <span v-if="!isLoading" class="btn-content">
                                <span class="btn-text">立即登录</span>
                                <i class="el-icon-right btn-arrow"></i>
                            </span>
                            <span v-else class="btn-content">登录中...</span>
                        </el-button>
                    </el-form-item>

                    <div class="form-links">
                        <router-link to="/forgotPassword">忘记密码？</router-link>
                    </div>
                </el-form>
            </div>
        </el-dialog>

        <!-- 页脚 -->
        <footer class="page-footer">
            <div class="footer-content">
                <p>© 2024 在线考试系统 - 专业的在线考试解决方案</p>
            </div>
        </footer>

        <!-- 登录成功动画遮罩 -->
        <transition name="success-fade">
            <div v-if="showSuccessOverlay" class="success-overlay">
                <div class="success-content">
                    <!-- SVG 对勾动画 -->
                    <div class="success-icon-wrapper">
                        <svg class="checkmark-svg" viewBox="0 0 100 100">
                            <circle class="checkmark-circle" cx="50" cy="50" r="45" />
                            <path class="checkmark-check" d="M25,50 L40,65 L75,30" />
                        </svg>
                    </div>

                    <!-- 成功信息 -->
                    <div class="success-info">
                        <h2 class="success-title">登录成功！</h2>
                        <p class="success-welcome">欢迎回来，{{ successUserName }}</p>
                        <span class="role-badge" :class="getRoleBadgeClass(successRole)">
                            <i :class="getRoleIcon(successRole)"></i>
                            {{ getRoleText(successRole) }}
                        </span>
                    </div>

                    <!-- 倒计时进度条 -->
                    <div class="countdown-wrapper">
                        <div class="countdown-text">{{ countdownSeconds }}秒后自动跳转</div>
                        <div class="countdown-progress">
                            <div class="progress-bar" :style="{ width: countdownProgress + '%' }"></div>
                        </div>
                    </div>
                </div>

                <!-- 庆祝彩纸 -->
                <div class="confetti-container">
                    <div
                        v-for="i in 30"
                        :key="i"
                        class="confetti"
                        :style="getConfettiStyle(i)"
                    ></div>
                </div>
            </div>
        </transition>
    </div>
</template>

<script>
export default {
    name: "LandingPage",
    data() {
        return {
            showLoginDialog: false,
            showSuccessOverlay: false,
            successUserName: '',
            successRole: '',
            countdownSeconds: 2,
            countdownProgress: 100,
            countdownTimer: null,

            loginType: "password",
            activeTab: 0,
            smartTip: "",
            errors: {
                username: "",
                password: "",
                phone: "",
            },
            tabs: [
                { name: "题库管理", icon: "el-icon-folder-opened" },
                { name: "试题管理", icon: "el-icon-document" },
                { name: "试题类型", icon: "el-icon-files" },
                { name: "刷题训练", icon: "el-icon-reading" },
            ],
            featuresData: [
                [
                    { icon: "el-icon-folder", title: "作用范围", desc: "用于训练、开放题库进行训练等" },
                    { icon: "el-icon-edit", title: "试题标签", desc: "题库与知识点绑定，标识知识点" },
                    { icon: "el-icon-s-custom", title: "训练权限", desc: "完全公开、部分学员、指定部门、指定人员" },
                    { icon: "el-icon-document-copy", title: "题库付费", desc: "知识付费，付费后进行题库训练" },
                ],
                [
                    { icon: "el-icon-edit-outline", title: "多种题型", desc: "支持单选、多选、判断、填空、问答" },
                    { icon: "el-icon-upload", title: "批量导入", desc: "支持Excel批量导入题目" },
                    { icon: "el-icon-picture", title: "富文本编辑", desc: "支持图片、公式等富文本内容" },
                    { icon: "el-icon-star-on", title: "难度设置", desc: "可设置题目难度等级" },
                ],
                [
                    { icon: "el-icon-circle-check", title: "单选题", desc: "标准单选题型，支持图文混排" },
                    { icon: "el-icon-finished", title: "多选题", desc: "多项选择题型，灵活配置" },
                    { icon: "el-icon-success", title: "判断题", desc: "是非判断题型" },
                    { icon: "el-icon-edit", title: "问答题", desc: "主观题型，支持人工阅卷" },
                ],
                [
                    { icon: "el-icon-magic-stick", title: "针对练习", desc: "分析科目薄弱点智能组卷进行专项练习" },
                    { icon: "el-icon-document-delete", title: "错题本", desc: "自动收集错题，针对性训练" },
                    { icon: "el-icon-data-line", title: "学习曲线", desc: "记录学习进度，可视化展示" },
                    { icon: "el-icon-trophy", title: "智能推荐", desc: "通过分析智能推荐薄弱知识点" },
                ],
            ],
            advantages: [
                { icon: "el-icon-lock", title: "安全可靠", desc: "多重加密保护，数据安全有保障" },
                { icon: "el-icon-lightning", title: "高效稳定", desc: "支持大规模并发，系统稳定流畅" },
                { icon: "el-icon-cpu", title: "智能AI", desc: "AI智能出题，自动组卷评分" },
            ],
            formData: {
                username: "",
                password: "",
                captcha: "",
                phone: "",
                smsCode: "",
            },
            captchaImage: "",
            fallbackCaptcha: "",
            smsCountdown: 0,
            isLoading: false,
        };
    },
    computed: {
        currentFeatures() {
            return this.featuresData[this.activeTab] || [];
        },
        passwordStrength() {
            const pwd = this.formData.password;
            if (!pwd) return { width: '0%', class: '', text: '' };

            let strength = 0;
            if (pwd.length >= 6) strength++;
            if (pwd.length >= 10) strength++;
            if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++;
            if (/\d/.test(pwd)) strength++;
            if (/[^a-zA-Z0-9]/.test(pwd)) strength++;

            if (strength <= 1) return { width: '33%', class: 'weak', text: '弱' };
            if (strength <= 3) return { width: '66%', class: 'medium', text: '中' };
            return { width: '100%', class: 'strong', text: '强' };
        }
    },
    mounted() {
        this.refreshCaptcha();
        this.showSmartTip();
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
        showSmartTip() {
            const tips = [
                "💡 支持密码和短信两种登录方式",
                "🔒 您的数据经过加密传输，安全可靠",
                "⚡ 首次登录建议使用密码方式",
                "📱 短信登录更快捷，适合移动端使用",
            ];
            this.smartTip = tips[Math.floor(Math.random() * tips.length)];

            setInterval(() => {
                this.smartTip = tips[Math.floor(Math.random() * tips.length)];
            }, 8000);
        },
        handleInputFocus(field) {
            this.errors[field] = "";

            const tips = {
                username: "💡 请输入您的用户名或学号",
                password: "🔒 密码长度建议6-20位，包含字母和数字",
                phone: "📱 请输入11位手机号码",
            };
            this.smartTip = tips[field] || "";
        },
        validateUsername() {
            if (!this.formData.username) {
                this.errors.username = "用户名不能为空";
                return false;
            }
            if (this.formData.username.toString().length < 2) {
                this.errors.username = "用户名长度至少2位";
                return false;
            }
            this.errors.username = "";
            return true;
        },
        validatePassword() {
            if (!this.formData.password) {
                this.errors.password = "密码不能为空";
                return false;
            }
            if (this.formData.password.length < 6) {
                this.errors.password = "密码长度至少6位";
                return false;
            }
            this.errors.password = "";
            return true;
        },
        validatePhone() {
            if (!this.formData.phone) {
                this.errors.phone = "手机号不能为空";
                return false;
            }
            if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
                this.errors.phone = "请输入正确的手机号格式";
                return false;
            }
            this.errors.phone = "";
            return true;
        },

        refreshCaptcha() {
            this.$axios({
                url: `/api/captcha/image`,
                method: "get",
            }).then((res) => {
                if (res.data.code === 200 && res.data.data) {
                    this.captchaImage = res.data.data.image;
                    this.fallbackCaptcha = '';
                }
            }).catch((err) => {
                console.error('验证码加载失败:', err);
                // 如果API失败，生成备用验证码
                this.captchaImage = '';
                this.generateFallbackCaptcha();
            });
        },
        generateFallbackCaptcha() {
            // 生成4位随机验证码
            const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
            let code = '';
            for (let i = 0; i < 4; i++) {
                code += chars.charAt(Math.floor(Math.random() * chars.length));
            }
            this.fallbackCaptcha = code;
        },
        sendSms() {
            const phone = this.formData.phone;
            if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
                this.$message.warning("请输入正确的手机号");
                return;
            }

            this.$axios({
                url: `/api/captcha/sms/send`,
                method: "post",
                data: { phone: phone },
            }).then((res) => {
                if (res.data.code === 200) {
                    this.$message.success("验证码已发送");
                    this.smsCountdown = 60;
                    const timer = setInterval(() => {
                        this.smsCountdown--;
                        if (this.smsCountdown <= 0) {
                            clearInterval(timer);
                        }
                    }, 1000);
                } else {
                    this.$message.error(res.data.message || "发送失败");
                }
            });
        },
        handleLogin() {
            // 验证表单
            if (this.loginType === "password") {
                if (!this.validateUsername() || !this.validatePassword()) {
                    this.$message.warning("请检查输入信息");
                    return;
                }
                if (!this.formData.captcha) {
                    this.$message.warning("请输入验证码");
                    return;
                }
            } else {
                if (!this.validatePhone()) {
                    return;
                }
                if (!this.formData.smsCode) {
                    this.$message.warning("请输入短信验证码");
                    return;
                }
            }

            this.isLoading = true;
            this.$axios({
                url: `/api/login`,
                method: "post",
                data: {
                    loginType: this.loginType,
                    username: this.formData.username,
                    password: this.formData.password,
                    captcha: this.formData.captcha,
                    phone: this.formData.phone,
                    smsCode: this.formData.smsCode,
                },
            }).then((res) => {
                this.isLoading = false;
                let resData = res.data.data;
                if (res.data.code === 200 && resData != null) {
                    // 关闭登录对话框
                    this.showLoginDialog = false;

                    // 获取用户名和角色
                    let userName = '';
                    let targetPath = '/index';
                    let role = String(resData.role);

                    switch (role) {
                        case "0":
                            userName = resData.adminName;
                            this.$cookies.set("cname", resData.adminName);
                            this.$cookies.set("cid", resData.adminId);
                            this.$cookies.set("role", 0);
                            targetPath = "/index";
                            break;
                        case "1":
                            userName = resData.teacherName;
                            this.$cookies.set("cname", resData.teacherName);
                            this.$cookies.set("cid", resData.teacherId);
                            this.$cookies.set("role", 1);
                            targetPath = "/index";
                            break;
                        case "2":
                            userName = resData.studentName;
                            this.$cookies.set("cname", resData.studentName);
                            this.$cookies.set("cid", resData.studentId);
                            this.$cookies.set("role", 2);
                            targetPath = "/student";
                            break;
                        default:
                            userName = resData.adminName || resData.teacherName || resData.studentName || '用户';
                            targetPath = "/index";
                            break;
                    }

                    // 启动成功动画
                    this.startSuccessAnimation(userName, resData.role);

                    // 2秒后跳转
                    setTimeout(() => {
                        this.$router.push({ path: targetPath });
                    }, 2000);
                } else {
                    this.$message.error(res.data.message || "登录失败");
                    this.refreshCaptcha();
                }
            }).catch(() => {
                this.isLoading = false;
                this.$message.error("网络错误");
                this.refreshCaptcha();
            });
        },
        getRoleText(role) {
            const roleMap = {
                '0': '管理员',
                '1': '教师',
                '2': '学生'
            };
            return roleMap[role] || '用户';
        },
        getRoleBadgeClass(role) {
            const classMap = {
                '0': 'role-admin',
                '1': 'role-teacher',
                '2': 'role-student'
            };
            return classMap[role] || '';
        },
        getRoleIcon(role) {
            const iconMap = {
                '0': 'el-icon-s-custom',
                '1': 'el-icon-user',
                '2': 'el-icon-reading'
            };
            return iconMap[role] || 'el-icon-user';
        },
        getConfettiStyle(index) {
            const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'];
            const color = colors[index % colors.length];
            const left = Math.random() * 100;
            const animationDelay = Math.random() * 0.5;
            const animationDuration = 2 + Math.random() * 2;

            return {
                left: `${left}%`,
                backgroundColor: color,
                animationDelay: `${animationDelay}s`,
                animationDuration: `${animationDuration}s`
            };
        },
        startSuccessAnimation(userName, role) {
            this.successUserName = userName;
            this.successRole = role;
            this.showSuccessOverlay = true;
            this.countdownSeconds = 2;
            this.countdownProgress = 100;

            // 倒计时
            this.countdownTimer = setInterval(() => {
                this.countdownSeconds--;
                this.countdownProgress = (this.countdownSeconds / 2) * 100;

                if (this.countdownSeconds <= 0) {
                    clearInterval(this.countdownTimer);
                }
            }, 1000);
        },
        goToDemo() {
            this.$router.push('/demo');
        },
        goToAuth() {
            this.$router.push('/auth');
        }
    },
    beforeDestroy() {
        if (this.countdownTimer) {
            clearInterval(this.countdownTimer);
        }
    }
};
</script>

<style scoped>
:root {
    --theme-primary: #409EFF;
    --theme-gradient: linear-gradient(135deg, #409EFF, #66b1ff);
}

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

/* 全局平滑滚动 */
html {
    scroll-behavior: smooth;
}

#landing-page {
    width: 100%;
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
    position: relative;
}

#landing-page::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image:
        radial-gradient(circle at 20% 30%, rgba(33, 150, 243, 0.05) 0%, transparent 50%),
        radial-gradient(circle at 80% 70%, rgba(33, 150, 243, 0.05) 0%, transparent 50%),
        radial-gradient(circle at 50% 50%, rgba(33, 150, 243, 0.03) 0%, transparent 70%);
    pointer-events: none;
    animation: bgPulse 15s ease-in-out infinite;
}

@keyframes bgPulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.8; }
}

/* 波浪背景 */
.wave-background {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 300px;
    pointer-events: none;
    z-index: 0;
}

.wave {
    position: absolute;
    bottom: 0;
    width: 100%;
    height: 100%;
}

.wave-2 {
    animation: waveMove 12s ease-in-out infinite;
    animation-delay: -6s;
}

@keyframes waveMove {
    0%, 100% { transform: translateX(0); }
    50% { transform: translateX(-50px); }
}

/* ==================== 顶部导航 ==================== */
.top-header {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
    position: sticky;
    top: 0;
    z-index: 1000;
    border-bottom: 1px solid rgba(74, 144, 226, 0.1);
}

.header-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 40px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.logo-area {
    display: flex;
    align-items: center;
    gap: 12px;
}

.logo-icon {
    width: 40px;
    height: 40px;
    background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transition: all 0.3s;
}

.logo-icon::before {
    content: '';
    position: absolute;
    inset: -3px;
    background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
    border-radius: 10px;
    opacity: 0.3;
    filter: blur(8px);
    transition: all 0.3s;
}

.logo-area:hover .logo-icon::before {
    opacity: 0.5;
    filter: blur(12px);
}

.logo-area:hover .logo-icon {
    transform: scale(1.05);
}

.logo-icon i {
    font-size: 24px;
    color: white;
}

.logo-text {
    font-size: 20px;
    font-weight: 600;
    color: #333;
}

.nav-menu {
    display: flex;
    gap: 40px;
}

.nav-item {
    color: #666;
    text-decoration: none;
    font-size: 15px;
    transition: color 0.3s;
}

.nav-item:hover {
    color: #4A90E2;
}

.header-actions {
    display: flex;
    gap: 12px;
}

.btn-login,
.btn-trial {
    padding: 12px 28px;
    border-radius: 10px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    position: relative;
    overflow: hidden;
}

.btn-login {
    background: transparent;
    color: var(--theme-primary, #409EFF);
    border: 2px solid var(--theme-primary, #409EFF);
}

.btn-login::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(74, 144, 226, 0.1);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.4s, height 0.4s;
}

.btn-login:hover::before {
    width: 200%;
    height: 200%;
}

.btn-login:hover {
    background: rgba(64, 158, 255, 0.1);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.btn-trial {
    background: var(--theme-gradient, linear-gradient(135deg, #409EFF, #66b1ff));
    color: white;
}

.btn-trial::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.4s, height 0.4s;
}

.btn-trial:hover::before {
    width: 200%;
    height: 200%;
}

.btn-trial:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

/* ==================== 题库建设区域 ==================== */
.question-bank-section {
    padding: 100px 0;
    background: white;
    position: relative;
}

.question-bank-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(74, 144, 226, 0.3), transparent);
}

.content-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 40px;
    position: relative;
    z-index: 1;
}

.main-title {
    text-align: center;
    font-size: 48px;
    font-weight: 800;
    background: linear-gradient(135deg, #1976D2 0%, #2196F3 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 16px;
    letter-spacing: -1px;
}

.main-subtitle {
    text-align: center;
    font-size: 16px;
    color: #999;
    margin-bottom: 50px;
}

/* Tab导航 */
.tab-navigation {
    display: flex;
    justify-content: center;
    gap: 0;
    margin-bottom: 60px;
    border-bottom: 2px solid #e8e8e8;
    position: relative;
}

.tab-navigation::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    height: 3px;
    background: linear-gradient(90deg, #4A90E2, #357ABD);
    transition: all 0.3s;
    border-radius: 2px 2px 0 0;
}

.tab-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px 32px;
    cursor: pointer;
    color: #666;
    font-size: 15px;
    border-bottom: 3px solid transparent;
    margin-bottom: -2px;
    transition: all 0.3s;
}

.tab-item:hover {
    color: #2196F3;
}

.tab-item.active {
    color: #2196F3;
    border-bottom-color: #2196F3;
    font-weight: 600;
}

.tab-item i {
    font-size: 18px;
}

/* 内容展示 */
.content-display {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 60px;
    align-items: center;
}

.feature-list {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.feature-item {
    display: flex;
    gap: 16px;
    padding: 24px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 16px;
    transition: all 0.3s;
    border: 1px solid transparent;
    position: relative;
    overflow: hidden;
}

.feature-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #4A90E2, #357ABD);
    opacity: 0;
    transition: opacity 0.3s;
}

.feature-item:hover {
    background: linear-gradient(135deg, rgba(33, 150, 243, 0.08) 0%, #ffffff 100%);
    transform: translateX(12px);
    border-color: rgba(33, 150, 243, 0.25);
    box-shadow: 0 8px 24px rgba(33, 150, 243, 0.2);
}

.feature-item:hover::before {
    opacity: 1;
}

.feature-icon {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, rgba(33, 150, 243, 0.15) 0%, rgba(33, 150, 243, 0.25) 100%);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    position: relative;
    transition: all 0.3s;
}

.feature-icon::before {
    content: '';
    position: absolute;
    inset: -2px;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 14px;
    opacity: 0;
    filter: blur(8px);
    transition: all 0.3s;
}

.feature-item:hover .feature-icon {
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    transform: scale(1.1) rotate(5deg);
}

.feature-item:hover .feature-icon::before {
    opacity: 0.4;
}

.feature-icon i {
    font-size: 28px;
    color: #2196F3;
    position: relative;
    z-index: 1;
    transition: color 0.3s;
}

.feature-item:hover .feature-icon i {
    color: white;
}

.feature-info h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 8px;
}

.feature-info p {
    font-size: 14px;
    color: #999;
    line-height: 1.6;
}

/* 3D插图 */
.illustration-box {
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    border-radius: 24px;
    padding: 60px;
    min-height: 500px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(74, 144, 226, 0.15);
}

.illustration-box::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3), transparent);
    animation: illustrationGlow 8s ease-in-out infinite;
}

@keyframes illustrationGlow {
    0%, 100% { transform: translate(0, 0); }
    50% { transform: translate(-20%, -20%); }
}

.illustration-3d {
    position: relative;
    width: 100%;
    height: 400px;
}

.cube-container {
    position: relative;
    width: 100%;
    height: 100%;
}

.cube {
    position: absolute;
    width: 80px;
    height: 80px;
    background: white;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    animation: float 3s ease-in-out infinite;
}

.cube i {
    font-size: 36px;
    color: #4A90E2;
}

.cube-1 {
    top: 50px;
    left: 50px;
    background: linear-gradient(135deg, #FFB74D, #FFA726);
    animation-delay: 0s;
}

.cube-1 i {
    color: white;
}

.cube-2 {
    top: 100px;
    right: 80px;
    background: linear-gradient(135deg, #4A90E2, #357ABD);
    animation-delay: 1s;
}

.cube-2 i {
    color: white;
}

.cube-3 {
    bottom: 80px;
    left: 80px;
    background: linear-gradient(135deg, #66BB6A, #4CAF50);
    animation-delay: 2s;
}

.cube-3 i {
    color: white;
}

.main-screen {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 300px;
    height: 220px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.2);
    padding: 24px;
    position: relative;
    z-index: 1;
}

.main-screen::before {
    content: '';
    position: absolute;
    inset: -4px;
    background: linear-gradient(135deg, #4A90E2, #357ABD);
    border-radius: 20px;
    opacity: 0.2;
    filter: blur(20px);
}

.screen-header {
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    margin-bottom: 16px;
}

.screen-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.content-line {
    height: 12px;
    background: #f0f0f0;
    border-radius: 6px;
}

.content-line.short {
    width: 60%;
}

@keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-20px); }
}

/* ==================== 为什么选择我们 ==================== */
.why-choose-section {
    padding: 100px 0;
    background: linear-gradient(135deg, #f8f9fa 0%, #e8eef5 100%);
    position: relative;
}

.why-choose-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(74, 144, 226, 0.3), transparent);
}

.section-title {
    text-align: center;
    font-size: 42px;
    font-weight: 800;
    background: linear-gradient(135deg, #1976D2 0%, #2196F3 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 16px;
    letter-spacing: -1px;
}

.section-subtitle {
    text-align: center;
    font-size: 17px;
    color: #999;
    margin-bottom: 60px;
}

.advantages-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
}

.advantage-card {
    background: white;
    padding: 48px 36px;
    border-radius: 20px;
    text-align: center;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(74, 144, 226, 0.1);
    position: relative;
    overflow: hidden;
}

.advantage-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #2196F3, #42A5F5);
    transform: scaleX(0);
    transition: transform 0.4s;
}

.advantage-card:hover {
    transform: translateY(-12px);
    box-shadow: 0 20px 48px rgba(33, 150, 243, 0.25);
    border-color: rgba(33, 150, 243, 0.35);
}

.advantage-card:hover::before {
    transform: scaleX(1);
}

.adv-icon {
    width: 96px;
    height: 96px;
    margin: 0 auto 28px;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transition: all 0.4s;
}

.adv-icon::before {
    content: '';
    position: absolute;
    inset: -8px;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 50%;
    opacity: 0.2;
    filter: blur(20px);
    transition: all 0.4s;
}

.advantage-card:hover .adv-icon {
    transform: scale(1.1) rotate(10deg);
}

.advantage-card:hover .adv-icon::before {
    opacity: 0.4;
    filter: blur(30px);
}

.adv-icon i {
    font-size: 48px;
    color: white;
    position: relative;
    z-index: 1;
}

.advantage-card h3 {
    font-size: 20px;
    color: #333;
    margin-bottom: 12px;
}

.advantage-card p {
    font-size: 14px;
    color: #999;
    line-height: 1.6;
}



/* ==================== 登录对话框 ==================== */
::v-deep .modern-login-dialog {
    border-radius: 28px;
    overflow: hidden;
    box-shadow: 0 30px 80px rgba(0, 0, 0, 0.25);
    border: 1px solid rgba(33, 150, 243, 0.2);
}

::v-deep .modern-login-dialog .el-dialog__header {
    padding: 50px 40px 30px;
    background: linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%);
    border-bottom: 1px solid rgba(33, 150, 243, 0.15);
    position: relative;
}

::v-deep .modern-login-dialog .el-dialog__header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 4px;
    background: linear-gradient(90deg, #2196F3, #42A5F5);
    border-radius: 0 0 4px 4px;
}

::v-deep .modern-login-dialog .el-dialog__body {
    padding: 40px;
    background: white;
}

::v-deep .el-dialog__wrapper {
    backdrop-filter: blur(10px);
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
}

.dialog-title {
    text-align: center;
    position: relative;
}

.title-icon-wrapper {
    position: relative;
    width: 100px;
    height: 100px;
    margin: 0 auto 24px;
}

.title-icon {
    width: 80px;
    height: 80px;
    margin: 10px auto;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 2;
    animation: iconFloat 3s ease-in-out infinite;
    box-shadow: 0 10px 30px rgba(33, 150, 243, 0.5);
}

@keyframes iconFloat {
    0%, 100% {
        transform: translateY(0) scale(1);
    }
    50% {
        transform: translateY(-8px) scale(1.05);
    }
}

.title-icon::before {
    content: '';
    position: absolute;
    inset: -8px;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 50%;
    opacity: 0.4;
    filter: blur(20px);
    animation: iconGlow 2s ease-in-out infinite;
}

@keyframes iconGlow {
    0%, 100% {
        opacity: 0.4;
        transform: scale(1);
    }
    50% {
        opacity: 0.6;
        transform: scale(1.15);
    }
}

.title-icon i {
    font-size: 40px;
    color: white;
    position: relative;
    z-index: 1;
    animation: iconRotate 4s ease-in-out infinite;
}

@keyframes iconRotate {
    0%, 100% { transform: rotate(0deg); }
    25% { transform: rotate(-5deg); }
    75% { transform: rotate(5deg); }
}

.icon-rings {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    z-index: 1;
}

.ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    border: 2px solid rgba(33, 150, 243, 0.3);
    border-radius: 50%;
    animation: ringExpand 3s ease-out infinite;
}

.ring-1 {
    width: 90px;
    height: 90px;
    animation-delay: 0s;
}

.ring-2 {
    width: 90px;
    height: 90px;
    animation-delay: 1s;
}

.ring-3 {
    width: 90px;
    height: 90px;
    animation-delay: 2s;
}

@keyframes ringExpand {
    0% {
        width: 90px;
        height: 90px;
        opacity: 0.8;
    }
    100% {
        width: 140px;
        height: 140px;
        opacity: 0;
    }
}

.dialog-title h3 {
    font-size: 32px;
    font-weight: 800;
    background: linear-gradient(135deg, #2196F3 0%, #42A5F5 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 8px;
    letter-spacing: -0.5px;
}

.dialog-title p {
    font-size: 16px;
    color: #999;
    font-weight: 500;
}

.login-form-wrapper {
    position: relative;
}

.login-type-switch {
    display: flex;
    gap: 16px;
    margin-bottom: 36px;
    padding: 8px;
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
    border-radius: 16px;
    box-shadow: inset 0 2px 8px rgba(33, 150, 243, 0.1);
}

.switch-item {
    flex: 1;
    padding: 14px 20px;
    border-radius: 10px;
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-size: 15px;
    font-weight: 600;
    color: #666;
    position: relative;
}

.switch-item::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    border-radius: 10px;
    opacity: 0;
    transition: opacity 0.3s;
}

.switch-item i,
.switch-item span {
    position: relative;
    z-index: 1;
    transition: all 0.3s;
}

.switch-item.active {
    color: white;
}

.switch-item.active::before {
    opacity: 1;
}

.switch-item:not(.active):hover {
    background: rgba(33, 150, 243, 0.15);
}

::v-deep .login-form .el-form-item {
    margin-bottom: 24px;
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
    height: auto !important;
}

::v-deep .login-form .captcha-form-item,
::v-deep .login-form .sms-form-item {
    margin-bottom: 24px;
}

::v-deep .login-form .submit-form-item {
    margin-bottom: 0;
}

::v-deep .submit-form-item .el-form-item__content {
    display: flex !important;
    justify-content: center;
    align-items: center;
}

::v-deep .login-form .el-form-item__content {
    line-height: normal;
    width: 100%;
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
}

::v-deep .login-form .captcha-form-item .el-form-item__content,
::v-deep .login-form .sms-form-item .el-form-item__content {
    display: block !important;
}

/* 输入框占位符颜色 */
::v-deep .login-form .el-input__inner::placeholder {
    color: rgba(0, 0, 0, 0.25);
    font-weight: 400;
}

::v-deep .login-form .el-input__inner::-webkit-input-placeholder {
    color: rgba(0, 0, 0, 0.25);
    font-weight: 400;
}

::v-deep .login-form .el-input__inner::-moz-placeholder {
    color: rgba(0, 0, 0, 0.25);
    font-weight: 400;
}

::v-deep .login-form .el-input__inner:-ms-input-placeholder {
    color: rgba(0, 0, 0, 0.25);
    font-weight: 400;
}

/* 输入框样式 */
::v-deep .login-form .el-input__inner:hover {
    border-color: #d0d0d0;
    background: white;
}

::v-deep .login-form .el-input__inner:focus {
    border-color: #2196F3;
    background: white;
    box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.15);
}

.captcha-row {
    display: flex;
    gap: 12px;
    align-items: stretch;
    width: 100%;
}

.captcha-row ::v-deep .el-input {
    flex: 1;
    min-width: 0;
}

.sms-wrapper {
    display: flex;
    gap: 12px;
    align-items: stretch;
    width: 100%;
}

.sms-wrapper ::v-deep .el-input {
    flex: 1;
    min-width: 0;
}

.captcha-image {
    width: 130px;
    height: 50px;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #e8e8e8;
    transition: all 0.3s;
    flex-shrink: 0;
    position: relative;
}

.captcha-image:hover {
    border-color: #2196F3;
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(33, 150, 243, 0.25);
}

.captcha-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.captcha-image span {
    font-size: 12px;
    color: #2196F3;
    font-weight: 600;
    text-align: center;
    padding: 0 8px;
}

.captcha-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
}

.captcha-code {
    font-size: 20px;
    font-weight: 800;
    letter-spacing: 4px;
    color: #2196F3;
    text-shadow: 2px 2px 4px rgba(33, 150, 243, 0.3);
    font-family: 'Courier New', monospace;
    transform: skew(-5deg);
}

.captcha-placeholder i {
    font-size: 16px;
    color: #2196F3;
    animation: rotate 2s linear infinite;
}

@keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

::v-deep .sms-btn {
    width: 130px;
    height: 50px;
    background: linear-gradient(135deg, #2196F3, #42A5F5);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;
}

::v-deep .sms-btn::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.4s, height 0.4s;
}

::v-deep .sms-btn:hover:not(.is-disabled)::before {
    width: 200%;
    height: 200%;
}

::v-deep .sms-btn:hover:not(.is-disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(33, 150, 243, 0.5);
}

::v-deep .sms-btn.is-disabled {
    background: linear-gradient(135deg, #ccc, #aaa);
    cursor: not-allowed;
}

::v-deep .submit-btn {
    width: 100%;
    height: 52px;
    background: linear-gradient(135deg, #2196F3 0%, #42A5F5 100%);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    margin: 28px 0 0 0;
    position: relative;
    overflow: hidden;
    letter-spacing: 0.5px;
    box-shadow: 0 4px 16px rgba(33, 150, 243, 0.3);
}

::v-deep .submit-btn span {
    position: relative;
    z-index: 2;
}

.btn-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
}

.btn-text {
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.btn-arrow {
    font-size: 16px;
    transition: transform 0.3s ease;
    display: inline-flex;
    align-items: center;
}

::v-deep .submit-btn:hover .btn-arrow {
    transform: translateX(4px);
}

::v-deep .submit-btn::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.4), transparent);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
}

::v-deep .submit-btn::after {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.15), transparent);
    transform: rotate(45deg);
    animation: shimmer 3s infinite;
}

@keyframes shimmer {
    0% {
        transform: translateX(-100%) translateY(-100%) rotate(45deg);
    }
    100% {
        transform: translateX(100%) translateY(100%) rotate(45deg);
    }
}

::v-deep .submit-btn:hover::before {
    width: 400%;
    height: 400%;
}

::v-deep .submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(33, 150, 243, 0.5);
}

::v-deep .submit-btn:active {
    transform: translateY(0);
}

::v-deep .submit-btn.is-loading {
    opacity: 0.8;
    cursor: not-allowed;
}

.form-links {
    text-align: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
}

.form-links a {
    color: #2196F3;
    text-decoration: none;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.3s;
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.form-links a::before {
    content: '→';
    font-size: 16px;
    transition: transform 0.3s;
}

.form-links a::after {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, #2196F3, #42A5F5);
    transition: width 0.3s;
}

.form-links a:hover {
    color: #1976D2;
}

.form-links a:hover::before {
    transform: translateX(4px);
}

.form-links a:hover::after {
    width: 100%;
}

/* ==================== 页脚 ==================== */
.page-footer {
    background: #333;
    color: white;
    padding: 40px 0;
    text-align: center;
}

.footer-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 40px;
}

/* 智能提示 */
.smart-tip {
    background: linear-gradient(135deg, rgba(33, 150, 243, 0.12) 0%, rgba(33, 150, 243, 0.06) 100%);
    border-left: 4px solid #2196F3;
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #333;
    animation: tipSlideIn 0.5s ease-out;
}

@keyframes tipSlideIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.smart-tip i {
    font-size: 18px;
    color: #2196F3;
}

.fade-enter-active, .fade-leave-active {
    transition: all 0.3s;
}

.fade-enter, .fade-leave-to {
    opacity: 0;
    transform: translateY(-5px);
}

/* 字段提示 */
.field-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    margin-top: 6px;
    padding: 6px 12px;
    border-radius: 6px;
    animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-5px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.field-tip.error {
    background: #fef0f0;
    color: #f56c6c;
    border: 1px solid #fde2e2;
}

.field-tip.error i {
    color: #f56c6c;
}

.field-tip.success {
    background: #f0f9ff;
    color: #67c23a;
    border: 1px solid #e1f3d8;
}

.field-tip.success i {
    color: #67c23a;
}

.slide-down-enter-active, .slide-down-leave-active {
    transition: all 0.3s;
}

.slide-down-enter, .slide-down-leave-to {
    opacity: 0;
    transform: translateY(-5px);
}

/* 密码强度指示器 */
.password-strength {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
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
    transition: all 0.3s;
    border-radius: 3px;
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

.strength-text {
    font-size: 12px;
    font-weight: 600;
    min-width: 30px;
}

.strength-text.weak {
    color: #f56c6c;
}

.strength-text.medium {
    color: #e6a23c;
}

.strength-text.strong {
    color: #67c23a;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
    .content-display {
        grid-template-columns: 1fr;
    }

    .illustration-box {
        display: none;
    }
}

@media (max-width: 768px) {
    .nav-menu {
        display: none;
    }

    .advantages-grid {
        grid-template-columns: 1fr;
    }

    .tab-navigation {
        flex-wrap: wrap;
    }
}

/* ==================== 登录成功动画 ==================== */
.success-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg,
        rgba(33, 150, 243, 0.95) 0%,
        rgba(66, 165, 245, 0.95) 50%,
        rgba(33, 150, 243, 0.95) 100%);
    backdrop-filter: blur(20px);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: overlayAppear 0.5s ease-out;
}

@keyframes overlayAppear {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

.success-content {
    text-align: center;
    position: relative;
    z-index: 2;
    animation: contentSlideUp 0.6s ease-out 0.2s both;
}

@keyframes contentSlideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.success-icon-wrapper {
    width: 160px;
    height: 160px;
    margin: 0 auto 40px;
    position: relative;
    animation: iconBounce 0.8s ease-out 0.4s both;
}

@keyframes iconBounce {
    0% {
        transform: scale(0);
        opacity: 0;
    }
    50% {
        transform: scale(1.1);
    }
    100% {
        transform: scale(1);
        opacity: 1;
    }
}

.checkmark-svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 10px 40px rgba(255, 255, 255, 0.5));
}

.checkmark-circle {
    fill: none;
    stroke: white;
    stroke-width: 4;
    stroke-linecap: round;
    stroke-dasharray: 283;
    stroke-dashoffset: 283;
    animation: drawCircle 0.8s ease-out 0.6s forwards;
}

@keyframes drawCircle {
    to {
        stroke-dashoffset: 0;
    }
}

.checkmark-check {
    fill: none;
    stroke: white;
    stroke-width: 6;
    stroke-linecap: round;
    stroke-linejoin: round;
    stroke-dasharray: 70;
    stroke-dashoffset: 70;
    animation: drawCheck 0.6s ease-out 1.2s forwards;
}

@keyframes drawCheck {
    to {
        stroke-dashoffset: 0;
    }
}

.success-info {
    margin-bottom: 50px;
}

.success-title {
    font-size: 48px;
    font-weight: 800;
    color: white;
    margin-bottom: 16px;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    animation: titleFadeIn 0.6s ease-out 0.8s both;
}

@keyframes titleFadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.success-welcome {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.95);
    margin-bottom: 24px;
    font-weight: 500;
    animation: welcomeFadeIn 0.6s ease-out 1s both;
}

@keyframes welcomeFadeIn {
    from {
        opacity: 0;
        transform: translateY(15px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.role-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 28px;
    border-radius: 50px;
    font-size: 16px;
    font-weight: 600;
    color: white;
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    animation: badgeZoomIn 0.5s ease-out 1.2s both;
}

@keyframes badgeZoomIn {
    from {
        opacity: 0;
        transform: scale(0.8);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}

.role-badge.role-admin {
    background: linear-gradient(135deg, rgba(255, 107, 107, 0.9), rgba(255, 87, 87, 0.9));
    border-color: rgba(255, 255, 255, 0.6);
}

.role-badge.role-teacher {
    background: linear-gradient(135deg, rgba(102, 187, 106, 0.9), rgba(76, 175, 80, 0.9));
    border-color: rgba(255, 255, 255, 0.6);
}

.role-badge.role-student {
    background: linear-gradient(135deg, rgba(255, 183, 77, 0.9), rgba(255, 167, 38, 0.9));
    border-color: rgba(255, 255, 255, 0.6);
}

.role-badge i {
    font-size: 20px;
}

.countdown-wrapper {
    margin-top: 60px;
    animation: countdownFadeIn 0.6s ease-out 1.4s both;
}

@keyframes countdownFadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.countdown-text {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 16px;
    font-weight: 500;
}

.countdown-progress {
    width: 300px;
    height: 6px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 3px;
    overflow: hidden;
    margin: 0 auto;
    box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-bar {
    height: 100%;
    background: linear-gradient(90deg, white, rgba(255, 255, 255, 0.8));
    border-radius: 3px;
    transition: width 1s linear;
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

/* 彩纸动画 */
.confetti-container {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    overflow: hidden;
}

.confetti {
    position: absolute;
    top: -10%;
    width: 10px;
    height: 10px;
    border-radius: 2px;
    animation: confettiFall linear forwards;
    opacity: 0.9;
}

@keyframes confettiFall {
    0% {
        transform: translateY(0) rotate(0deg);
        opacity: 1;
    }
    100% {
        transform: translateY(110vh) rotate(720deg);
        opacity: 0;
    }
}

.success-fade-enter-active {
    animation: successFadeIn 0.5s ease-out;
}

.success-fade-leave-active {
    animation: successFadeOut 0.4s ease-in;
}

@keyframes successFadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

@keyframes successFadeOut {
    from {
        opacity: 1;
    }
    to {
        opacity: 0;
    }
}
</style>
