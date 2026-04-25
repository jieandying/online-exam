<!--管理中心-->
<template>
  <div id='manager'>
    <!-- 人脸注册卡片 -->
    <div class="manager-card face-card">
      <div class="card-header">
        <div class="card-icon face-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="card-title-area">
          <h3>人脸识别管理</h3>
          <p class="card-desc">注册人脸后可用于考试身份验证</p>
        </div>
      </div>

      <!-- 状态区 -->
      <div class="face-status">
        <div class="status-item">
          <span class="status-label">注册状态</span>
          <el-tag :type="faceRegistered ? 'success' : 'info'" effect="dark">
            {{ faceRegistered ? '已注册' : '未注册' }}
          </el-tag>
        </div>
        <div class="status-item" v-if="latestRequest" style="margin-top: 12px;">
          <span class="status-label">最新申请</span>
          <div style="display:flex;align-items:center;gap:8px;">
            <el-tag :type="requestStatusType" effect="dark" size="small">{{ requestStatusLabel }}</el-tag>
            <span class="status-time" v-if="latestRequest.createTime">{{ formatTime(latestRequest.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="face-actions">
        <!-- 审批通过待重新注册横幅 -->
        <div v-if="approvedPendingRegister" class="approved-banner">
          <i class="el-icon-success"></i>
          <span>申请已通过！请点击下方按钮重新注册人脸</span>
        </div>

        <!-- 注册按钮 -->
        <el-button
          :type="approvedPendingRegister ? 'success' : 'primary'"
          :icon="faceRegistered && !approvedPendingRegister ? 'el-icon-check' : 'el-icon-camera'"
          @click="openFaceRegister"
          class="face-btn"
          :disabled="faceRegistered && !approvedPendingRegister"
          :class="{ 'pulse-btn': approvedPendingRegister }"
        >
          <template v-if="faceRegistered && !approvedPendingRegister">已完成注册</template>
          <template v-else-if="approvedPendingRegister">立即重新注册</template>
          <template v-else>注册人脸</template>
        </el-button>

        <!-- 申请修改按钮：已注册 且 非"审批通过待注册"状态 -->
        <el-button
          v-if="faceRegistered && !approvedPendingRegister"
          type="warning"
          icon="el-icon-edit"
          @click="showApplyDialog"
          class="face-change-btn"
          :disabled="hasPendingRequest"
        >
          {{ hasPendingRequest ? '申请审批中...' : '申请修改人脸' }}
        </el-button>

        <!-- 状态提示文字 -->
        <p v-if="latestRequest && latestRequest.status === 'rejected'" class="face-tip reject-tip">
          <i class="el-icon-warning"></i>
          申请被拒绝：{{ latestRequest.reviewComment || '无原因' }}
          <el-button type="text" size="mini" @click="showApplyDialog" style="margin-left:6px">重新申请</el-button>
        </p>
        <p v-else-if="hasPendingRequest" class="face-tip pending-tip">
          <i class="el-icon-time"></i> 申请已提交，等待管理员审批
        </p>
        <p v-else-if="faceRegistered && !hasPendingRequest && !approvedPendingRegister" class="face-tip">
          如需更换人脸照片，请点击上方按钮提交申请
        </p>
      </div>

      <!-- 申请历史记录 -->
      <div class="history-section" v-if="allRequests.length > 0">
        <div class="history-title" @click="showHistory = !showHistory">
          <i :class="showHistory ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
          <span>申请历史（共 {{ allRequests.length }} 条）</span>
        </div>
        <transition name="slide">
          <div v-show="showHistory" class="history-list">
            <div class="history-item" v-for="req in allRequests" :key="req.id">
              <div class="history-left">
                <el-tag :type="getStatusType(req.status)" effect="plain" size="mini">{{ getStatusLabel(req.status) }}</el-tag>
                <span class="history-reason">{{ req.reason }}</span>
              </div>
              <div class="history-right">
                <span class="history-time">{{ formatTime(req.createTime) }}</span>
                <span class="history-comment" v-if="req.reviewComment">{{ req.reviewComment }}</span>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 修改密码卡片 -->
    <div class="manager-card password-card">
      <div class="card-header">
        <div class="card-icon password-icon">
          <i class="el-icon-lock"></i>
        </div>
        <div class="card-title-area">
          <h3>修改密码</h3>
          <p class="card-desc">定期修改密码以保护账户安全</p>
        </div>
      </div>

      <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" label-width="100px" class="password-form">
        <el-form-item label="新密码" prop="pass">
          <el-input type="password" v-model="ruleForm2.pass" autocomplete="off" placeholder="请输入新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPass">
          <el-input type="password" v-model="ruleForm2.checkPass" autocomplete="off" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button type="primary" @click="submitForm('ruleForm2')" icon="el-icon-check">确认修改</el-button>
          <el-button @click="resetForm('ruleForm2')" icon="el-icon-refresh-left">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 人脸注册弹窗 -->
    <face-recognition
      :visible="faceDialogVisible"
      :student-id="studentId"
      :is-register="true"
      @success="onFaceRegisterSuccess"
      @close="faceDialogVisible = false"
    />

    <!-- 申请修改人脸弹窗 -->
    <el-dialog title="申请修改人脸" :visible.sync="applyDialogVisible" width="480px" class="apply-dialog">
      <div class="apply-notice">
        <i class="el-icon-info-solid"></i>
        <span>提交申请后，需等待管理员审批。审批通过后，您的旧人脸数据将被清除，届时可重新注册。</span>
      </div>
      <el-form label-width="80px">
        <el-form-item label="申请原因" required>
          <el-input
            type="textarea"
            v-model="applyReason"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请填写修改人脸的原因，如：照片模糊、外貌有较大变化等"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitApply" :loading="applyLoading">提交申请</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import FaceRecognition from '@/components/common/FaceRecognition.vue'

export default {
  components: { FaceRecognition },
  data() {
    var validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (this.ruleForm2.checkPass !== '') this.$refs.ruleForm2.validateField('checkPass')
        callback()
      }
    }
    var validatePass2 = (rule, value, callback) => {
      if (value === '') callback(new Error('请再次输入密码'))
      else if (value !== this.ruleForm2.pass) callback(new Error('两次输入密码不一致!'))
      else callback()
    }
    return {
      faceDialogVisible: false,
      faceRegistered: false,
      studentId: null,
      applyDialogVisible: false,
      applyReason: '',
      applyLoading: false,
      latestRequest: null,
      hasPendingRequest: false,
      allRequests: [],
      showHistory: false,
      ruleForm2: { pass: '', checkPass: '' },
      rules2: {
        pass: [{ validator: validatePass, trigger: 'blur' }],
        checkPass: [{ validator: validatePass2, trigger: 'blur' }]
      }
    }
  },
  computed: {
    requestStatusType() {
      if (!this.latestRequest) return 'info'
      return { pending: 'warning', approved: 'success', rejected: 'danger', completed: 'info' }[this.latestRequest.status] || 'info'
    },
    requestStatusLabel() {
      if (!this.latestRequest) return ''
      return { pending: '待审批', approved: '已通过', rejected: '已拒绝', completed: '已完成' }[this.latestRequest.status] || '未知'
    },
    // 审批通过但人脸未重新注册（需要引导立即注册）
    approvedPendingRegister() {
      return !this.faceRegistered && this.latestRequest && this.latestRequest.status === 'approved'
    }
  },
  mounted() {
    this.studentId = this.$cookies.get('cid')
    this.checkFaceStatus()
    this.loadMyRequests()
    if (this.$route.query.action === 'faceRegister') {
      this.$nextTick(() => this.openFaceRegister())
    }
  },
  methods: {
    async checkFaceStatus() {
      try {
        const res = await this.$axios.get(`/api/face/check/${this.studentId}`)
        if (res.data.code === 200 && res.data.data) {
          this.faceRegistered = res.data.data.success
        }
      } catch (e) {}
    },

    async loadMyRequests() {
      try {
        const res = await this.$axios.get(`/api/faceChange/myRequests/${this.studentId}`)
        if (res.data.code === 200 && Array.isArray(res.data.data)) {
          this.allRequests = res.data.data
          if (this.allRequests.length > 0) {
            this.latestRequest = this.allRequests[0]
            this.hasPendingRequest = this.latestRequest.status === 'pending'
          } else {
            this.latestRequest = null
            this.hasPendingRequest = false
          }
        }
      } catch (e) {}
    },

    openFaceRegister() {
      // 已注册且非审批通过状态时，不允许直接注册
      if (this.faceRegistered && !this.approvedPendingRegister) {
        this.$message.info('您已注册人脸，如需修改请提交申请')
        return
      }
      this.faceDialogVisible = true
    },

    onFaceRegisterSuccess() {
      this.faceDialogVisible = false
      this.faceRegistered = true
      this.$message.success('人脸注册成功！')
      // 刷新申请记录，审批通过状态将因已重新注册而不再显示
      this.loadMyRequests()
    },

    showApplyDialog() {
      this.applyReason = ''
      this.applyDialogVisible = true
    },

    async submitApply() {
      if (!this.applyReason.trim()) {
        this.$message.warning('请填写申请原因')
        return
      }
      this.applyLoading = true
      try {
        const res = await this.$axios.post('/api/faceChange/apply', {
          studentId: parseInt(this.studentId),
          reason: this.applyReason.trim()
        })
        if (res.data.code === 200) {
          this.$message.success(res.data.message || '申请提交成功，请等待审批')
          this.applyDialogVisible = false
          this.loadMyRequests()
        } else {
          this.$message.error(res.data.message || '提交失败')
        }
      } catch (e) {
        this.$message.error('网络请求失败')
      } finally {
        this.applyLoading = false
      }
    },

    getStatusType(status) {
      return { pending: 'warning', approved: 'success', rejected: 'danger', completed: 'info' }[status] || 'info'
    },

    getStatusLabel(status) {
      return { pending: '待审批', approved: '已通过', rejected: '已拒绝', completed: '已完成' }[status] || '未知'
    },

    formatTime(time) {
      if (!time) return '-'
      var d = new Date(time)
      var p = n => n < 10 ? '0' + n : n
      return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
    },

    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let studentId = this.$cookies.get("cid")
          this.$axios({ url: '/api/studentPWD', method: 'put', data: { pwd: this.ruleForm2.pass, studentId } })
            .then(res => {
              if (res.data != null) this.$message({ message: '密码修改成功', type: 'success' })
            })
        }
      })
    },

    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>

<style lang="less" scoped>
#manager {
  max-width: 620px;
  margin: 40px auto;
  padding: 0 20px;
}

.manager-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  &:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  flex-shrink: 0;
  &.face-icon { background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%); }
  &.password-icon { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); }
}

.card-title-area {
  h3 { margin: 0 0 6px; font-size: 20px; font-weight: 600; color: #303133; }
  .card-desc { margin: 0; font-size: 14px; color: #909399; }
}

.face-status {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-label { font-size: 14px; color: #606266; font-weight: 500; }
.status-time { font-size: 12px; color: #c0c4cc; }

// 审批通过横幅
.approved-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #f0f9eb, #e1f3d8);
  border: 1px solid #b3e19d;
  border-radius: 10px;
  color: #67C23A;
  font-size: 14px;
  font-weight: 600;
  animation: fadeIn 0.5s ease;
  i { font-size: 20px; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

.face-actions { text-align: center; }

.face-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
  transition: all 0.3s ease;
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(64, 158, 255, 0.45);
  }
  &:disabled {
    background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
    box-shadow: 0 4px 15px rgba(103, 194, 58, 0.25);
    cursor: not-allowed;
  }
  &.pulse-btn {
    background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
    box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
    animation: pulse 1.5s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4); }
  50% { box-shadow: 0 12px 32px rgba(103, 194, 58, 0.7); }
}

.face-change-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 12px;
  margin-left: 0 !important;
  background: linear-gradient(135deg, #E6A23C 0%, #F5D79E 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 15px rgba(230, 162, 60, 0.3);
  transition: all 0.3s ease;
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(230, 162, 60, 0.4);
  }
  &:disabled { opacity: 0.7; cursor: not-allowed; }
}

.face-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  &.reject-tip { color: #F56C6C; }
  &.pending-tip { color: #E6A23C; }
}

// 申请历史
.history-section {
  margin-top: 20px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  user-select: none;
  &:hover { color: #409EFF; }
}

.history-list { margin-top: 10px; }

.history-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fafafa;
  margin-bottom: 8px;
  font-size: 13px;
  &:last-child { margin-bottom: 0; }
}

.history-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.history-reason {
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-right {
  text-align: right;
  flex-shrink: 0;
}

.history-time { color: #c0c4cc; font-size: 12px; display: block; }
.history-comment { color: #909399; font-size: 12px; display: block; margin-top: 2px; }

// 滑动动画
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter, .slide-leave-to { opacity: 0; transform: translateY(-8px); }

// 申请弹窗
.apply-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  background: #f0f9ff;
  border-radius: 10px;
  border: 1px solid #d0e8ff;
  margin-bottom: 20px;
  i { color: #409EFF; font-size: 18px; margin-top: 2px; }
  span { font-size: 13px; color: #606266; line-height: 1.6; }
}

// 密码表单
.password-form {
  .el-form-item { margin-bottom: 24px; }
}

.form-actions {
  margin-top: 32px;
  .el-button { height: 44px; border-radius: 10px; font-size: 15px; font-weight: 500; padding: 0 28px; }
  .el-button--primary {
    background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(103, 194, 58, 0.35);
    &:hover { transform: translateY(-2px); }
  }
}

@media (max-width: 480px) {
  #manager { padding: 0 16px; margin: 20px auto; }
  .manager-card { padding: 20px; }
  .card-icon { width: 48px; height: 48px; font-size: 22px; }
  .card-title-area h3 { font-size: 18px; }
}
</style>
