<template>
  <transition name="face-modal">
    <div v-if="visible" class="face-recognition-overlay" @click.self="handleClose">
      <div class="face-recognition-modal">
        <!-- 头部 -->
        <div class="modal-header">
          <div class="header-icon-wrapper">
            <div class="header-icon" :class="{ 'success': verifySuccess, 'error': verifyError }">
              <i v-if="verifySuccess" class="el-icon-check"></i>
              <i v-else-if="verifyError" class="el-icon-close"></i>
              <i v-else class="el-icon-user"></i>
            </div>
            <div class="icon-ring" :class="{ 'scanning': isScanning }"></div>
          </div>
          <h3 class="modal-title">{{ isRegister ? '人脸注册' : '人脸验证' }}</h3>
          <p class="modal-subtitle">{{ isRegister ? '请正对摄像头，保持面部清晰' : '验证身份后方可进入考试' }}</p>
        </div>

        <!-- 摄像头区域 -->
        <div class="camera-container">
          <div class="camera-frame" :class="{ 
            'scanning': isScanning, 
            'success': verifySuccess, 
            'error': verifyError 
          }">
            <video ref="videoElement" autoplay playsinline muted 
                   @error="handleVideoError" 
                   @loadeddata="handleVideoLoad"
                   @canplay="handleCanPlay"
                   style="position: absolute; width: 100%; height: 100%; object-fit: cover; z-index: 1;"
            ></video>
            <canvas ref="canvasElement" class="video-canvas" style="position: absolute; width: 100%; height: 100%; object-fit: cover; z-index: -1; opacity: 0;"></canvas>
            
            <!-- 人脸检测框 -->
            <div class="face-overlay">
              <svg class="face-guide" viewBox="0 0 200 200">
                <path class="corner tl" d="M50,10 L10,10 L10,50" />
                <path class="corner tr" d="M150,10 L190,10 L190,50" />
                <path class="corner bl" d="M50,190 L10,190 L10,150" />
                <path class="corner br" d="M150,190 L190,190 L190,150" />
              </svg>
            </div>
            
            <!-- 扫描线动画 -->
            <div v-if="isScanning" class="scan-line"></div>
            
            <!-- 成功/失败遮罩 -->
            <div v-if="verifySuccess" class="result-overlay success">
              <i class="el-icon-circle-check"></i>
            </div>
            <div v-if="verifyError" class="result-overlay error">
              <i class="el-icon-circle-close"></i>
            </div>
            
            <!-- Camera Selection -->
            <div class="camera-controls" v-if="videoDevices && videoDevices.length > 0" style="position: absolute; top: 10px; right: 10px; z-index: 20;">
               <el-select v-model="selectedDeviceId" @change="handleDeviceChange" size="mini" placeholder="切换摄像头" style="width: 140px; opacity: 0.9;">
                   <el-option v-for="(device, index) in videoDevices" :key="device.deviceId" :label="device.label || 'Camera ' + (index + 1)" :value="device.deviceId"></el-option>
               </el-select>
            </div>
          </div>
        </div>

        <!-- 状态提示 -->
        <div class="status-section">
          <div class="status-indicator" :class="statusClass">
            <span class="status-dot"></span>
            <span class="status-text">{{ statusText }}</span>
          </div>
          
          <!-- 相似度进度条 -->
          <div v-if="showSimilarity" class="similarity-section">
            <div class="similarity-bar">
              <div class="similarity-fill" :style="{ width: similarity + '%' }" :class="similarityClass"></div>
            </div>
            <span class="similarity-label">相似度: {{ similarity.toFixed(1) }}%</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-footer">
          <el-button 
            class="btn-cancel" 
            @click="handleClose" 
            :disabled="isScanning"
          >
            取消
          </el-button>
          <el-button 
            class="btn-primary" 
            type="primary" 
            @click="captureAndVerify" 
            :disabled="isScanning || !cameraReady"
            :loading="isScanning"
          >
            {{ isScanning ? '识别中...' : (isRegister ? '注册人脸' : '开始验证') }}
          </el-button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'FaceRecognition',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    studentId: {
      type: [Number, String],
      required: true
    },
    examCode: {
      type: [Number, String],
      default: null
    },
    isRegister: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      cameraReady: false,
      isScanning: false,
      verifySuccess: false,
      verifyError: false,
      statusText: '正在启动摄像头...',
      similarity: 0,
      showSimilarity: false,
      stream: null,
      videoDevices: [],
      selectedDeviceId: null
    }
  },
  computed: {
    statusClass() {
      if (this.verifySuccess) return 'success'
      if (this.verifyError) return 'error'
      if (this.isScanning) return 'scanning'
      if (this.cameraReady) return 'ready'
      return 'loading'
    },
    similarityClass() {
      if (this.similarity >= 75) return 'high'
      if (this.similarity >= 50) return 'medium'
      return 'low'
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.$nextTick(() => {
          this.initCamera()
        })
      } else {
        this.stopCamera()
      }
    }
  },
  methods: {
    async initCamera() {
      this.resetState()
      this.statusText = '正在启动摄像头...'
      
      try {
        const constraints = {
          video: {
            width: { ideal: 640 },
            height: { ideal: 480 },
            facingMode: 'user'
          },
          audio: false
        }
        
        this.stream = await navigator.mediaDevices.getUserMedia(constraints)
        
        // 摄像头流获取成功后，枚举可用设备
        this.listVideoDevices()
        
        if (this.$refs.videoElement) {
          this.$refs.videoElement.srcObject = this.stream
          this.$refs.videoElement.onloadedmetadata = () => {
            this.cameraReady = true
            this.statusText = '摄像头已就绪，请保持面部在框内'
          }
        }
      } catch (err) {
        console.error('摄像头启动失败:', err)
        this.verifyError = true
        if (err.name === 'NotAllowedError') {
          this.statusText = '摄像头权限被拒绝，请在浏览器设置中允许访问'
        } else if (err.name === 'NotFoundError') {
          this.statusText = '未检测到摄像头设备'
        } else {
          this.statusText = '摄像头启动失败: ' + err.message
        }
      }
    },
    
    stopCamera() {
      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop())
        this.stream = null
      }
      if (this.$refs.videoElement) {
        this.$refs.videoElement.srcObject = null
      }
      this.resetState()
    },
    
    resetState() {
      this.cameraReady = false
      this.isScanning = false
      this.verifySuccess = false
      this.verifyError = false
      this.showSimilarity = false
      this.similarity = 0
      this.statusText = '正在启动摄像头...'
    },
    
    captureImage() {
      const video = this.$refs.videoElement
      const canvas = this.$refs.canvasElement
      
      if (!video || !canvas) return null
      
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      
      const ctx = canvas.getContext('2d')
      // 镜像翻转
      ctx.translate(canvas.width, 0)
      ctx.scale(-1, 1)
      ctx.drawImage(video, 0, 0)
      
      return canvas.toDataURL('image/jpeg', 0.8)
    },
    
    async captureAndVerify() {
      this.isScanning = true
      this.verifyError = false
      this.verifySuccess = false
      this.showSimilarity = false
      this.statusText = '正在识别人脸...'
      
      try {
        const faceImage = this.captureImage()
        
        if (!faceImage) {
          throw new Error('图像捕获失败')
        }
        
        const url = this.isRegister ? '/api/face/register' : '/api/face/verify'
        const requestData = {
          studentId: parseInt(this.studentId),
          faceImage: faceImage
        }
        
        if (!this.isRegister && this.examCode) {
          requestData.examCode = parseInt(this.examCode)
        }
        
        const response = await this.$axios.post(url, requestData)
        const result = response.data
        
        if (result.code === 200) {
          this.verifySuccess = true
          this.statusText = result.message || (this.isRegister ? '人脸注册成功' : '验证通过')
          
          if (result.data && result.data.similarity !== undefined) {
            this.similarity = result.data.similarity
            this.showSimilarity = true
          }
          
          // 成功后延迟关闭并触发事件
          setTimeout(() => {
            this.$emit('success', result.data)
          }, 1500)
        } else {
          this.verifyError = true
          this.statusText = result.message || '验证失败'
          
          if (result.data && result.data.similarity !== undefined) {
            this.similarity = result.data.similarity
            this.showSimilarity = true
          }
        }
      } catch (error) {
        console.error('人脸识别请求失败:', error)
        this.verifyError = true
        var errorMsg = '网络请求失败，请重试'
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message
        }
        this.statusText = errorMsg
      } finally {
        this.isScanning = false
      }
    },
    
    handleClose() {
      if (this.isScanning) return
      this.stopCamera()
      this.$emit('close')
    },
    handleVideoError(e) {
      console.error('Video element error:', e)
      this.statusText = '视频加载错误: ' + (e.target.error ? e.target.error.message : '未知错误')
      this.verifyError = true
    },
    handleVideoLoad() {
      console.log('Video data loaded')
    },
    handleCanPlay() {
      console.log('Video can play')
      this.cameraReady = true
      this.statusText = '摄像头已就绪，请保持面部在框内'
      if (this.$refs.videoElement && this.$refs.videoElement.paused) {
          this.$refs.videoElement.play().catch(e => console.error('Play failed in canplay', e))
      }
      this.drawVideoFrame()
    },
    drawVideoFrame() {
      if (!this.visible || !this.stream) return;
      
      const video = this.$refs.videoElement
      const canvas = this.$refs.canvasElement
      
      if (video && canvas && !video.paused && !video.ended) {
          const ctx = canvas.getContext('2d')
          const vw = video.videoWidth || 640
          const vh = video.videoHeight || 480
          
          if (canvas.width !== vw || canvas.height !== vh) {
              canvas.width = vw
              canvas.height = vh
          }
          
          ctx.save()
          // Mirror
          ctx.translate(canvas.width, 0)
          ctx.scale(-1, 1)
          ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
          ctx.restore()
      }
      
      requestAnimationFrame(this.drawVideoFrame)
    },
    async listVideoDevices() {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices()
        console.log('Enumerated devices:', devices)
        this.videoDevices = devices.filter(device => device.kind === 'videoinput')
        console.log('Video devices:', this.videoDevices)
        
        // Auto-select preferred camera
        if (!this.selectedDeviceId && this.videoDevices.length > 0) {
             const preferredLabel = 'USB2.0 HD UVC WebCam'
             const target = this.videoDevices.find(d => d.label.includes(preferredLabel) || d.label.includes('USB') || d.label.includes('3277:0029'))
             
             if (target) {
                 this.selectedDeviceId = target.deviceId
                 console.log('Auto-selected preferred device:', target.label)
                 this.statusText = '已连接设备: ' + (target.label || 'USB Camera')
                 
                 // If current stream is NOT this device, restart
                 if (this.stream) {
                     const track = this.stream.getVideoTracks()[0]
                     const settings = track.getSettings()
                     if (settings.deviceId !== target.deviceId) {
                         console.log('Switching to preferred device...')
                         this.stopCamera()
                         this.initCamera()
                     }
                 }
             } else {
                 // Fallback: check current if valid
                 const track = this.stream && this.stream.getVideoTracks()[0]
                 if (track) {
                     const settings = track.getSettings()
                     this.selectedDeviceId = settings.deviceId
                 }
             }
        }
      } catch (e) {
        console.error('Error listing devices:', e)
      }
    },
    handleDeviceChange(deviceId) {
      this.selectedDeviceId = deviceId
      this.stopCamera() // Stop current
      this.initCamera() // Restart with new ID
    }
  },
  
  beforeDestroy() {
    this.stopCamera()
  }
}
</script>

<style lang="less" scoped>
// 变量定义
@primary-color: #409EFF;
@success-color: #67C23A;
@error-color: #F56C6C;
@warning-color: #E6A23C;

.face-recognition-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.face-recognition-modal {
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 24px;
  padding: 32px;
  width: 440px;
  max-width: 95vw;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  animation: modalIn 0.3s ease-out;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 头部样式
.modal-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-icon-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, @primary-color 0%, #66b1ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  position: relative;
  z-index: 1;
  transition: all 0.4s ease;
  
  &.success {
    background: linear-gradient(135deg, @success-color 0%, #85ce61 100%);
  }
  
  &.error {
    background: linear-gradient(135deg, @error-color 0%, #f89898 100%);
  }
}

.icon-ring {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 3px solid rgba(64, 158, 255, 0.3);
  border-radius: 50%;
  
  &.scanning {
    animation: pulse 1.5s ease-in-out infinite;
    border-color: @primary-color;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.5;
  }
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.modal-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

// 摄像头区域
.camera-container {
  margin-bottom: 20px;
}

.camera-frame {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 16px;
  overflow: hidden;
  background: #1a1a2e;
  border: 3px solid #e4e7ed;
  transition: all 0.3s ease;
  
  &.scanning {
    border-color: @primary-color;
    box-shadow: 0 0 30px rgba(64, 158, 255, 0.3);
  }
  
  &.success {
    border-color: @success-color;
    box-shadow: 0 0 30px rgba(103, 194, 58, 0.3);
  }
  
  &.error {
    border-color: @error-color;
    box-shadow: 0 0 30px rgba(245, 108, 108, 0.3);
  }
  
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transform: scaleX(-1);
  }
}

.face-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60%;
  height: 75%;
  pointer-events: none;
}

.face-guide {
  width: 100%;
  height: 100%;
  
  .corner {
    fill: none;
    stroke: rgba(64, 158, 255, 0.8);
    stroke-width: 4;
    stroke-linecap: round;
    transition: stroke 0.3s ease;
  }
}

.camera-frame.success .face-guide .corner {
  stroke: @success-color;
}

.camera-frame.error .face-guide .corner {
  stroke: @error-color;
}

.scan-line {
  position: absolute;
  left: 10%;
  right: 10%;
  height: 3px;
  background: linear-gradient(90deg, transparent, @primary-color, transparent);
  animation: scan 2s linear infinite;
  box-shadow: 0 0 15px @primary-color;
}

@keyframes scan {
  0% { top: 15%; }
  50% { top: 80%; }
  100% { top: 15%; }
}

.result-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  animation: resultIn 0.4s ease-out;
  
  &.success {
    background: rgba(103, 194, 58, 0.85);
    color: #fff;
  }
  
  &.error {
    background: rgba(245, 108, 108, 0.85);
    color: #fff;
  }
}

@keyframes resultIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

// 状态区域
.status-section {
  margin-bottom: 24px;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 12px;
  background: #f5f7fa;
  transition: all 0.3s ease;
  
  &.loading {
    background: #f5f7fa;
    .status-dot { background: #909399; }
    .status-text { color: #909399; }
  }
  
  &.ready {
    background: rgba(64, 158, 255, 0.1);
    .status-dot { background: @primary-color; }
    .status-text { color: @primary-color; }
  }
  
  &.scanning {
    background: rgba(64, 158, 255, 0.1);
    .status-dot { 
      background: @primary-color;
      animation: blink 1s infinite;
    }
    .status-text { color: @primary-color; }
  }
  
  &.success {
    background: rgba(103, 194, 58, 0.1);
    .status-dot { background: @success-color; }
    .status-text { color: @success-color; }
  }
  
  &.error {
    background: rgba(245, 108, 108, 0.1);
    .status-dot { background: @error-color; }
    .status-text { color: @error-color; }
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.status-text {
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

// 相似度
.similarity-section {
  margin-top: 16px;
}

.similarity-bar {
  height: 8px;
  background: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.similarity-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
  
  &.low { background: linear-gradient(90deg, @error-color, #f89898); }
  &.medium { background: linear-gradient(90deg, @warning-color, #f3d19e); }
  &.high { background: linear-gradient(90deg, @success-color, #85ce61); }
}

.similarity-label {
  display: block;
  text-align: center;
  font-size: 13px;
  color: #606266;
  margin-top: 8px;
}

// 按钮区域
.modal-footer {
  display: flex;
  gap: 16px;
  
  .el-button {
    flex: 1;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
  }
  
  .btn-cancel {
    background: #f5f7fa;
    border-color: #dcdfe6;
    color: #606266;
    
    &:hover:not(:disabled) {
      background: #e4e7ed;
      border-color: #c0c4cc;
    }
  }
  
  .btn-primary {
    background: linear-gradient(135deg, @primary-color 0%, #66b1ff 100%);
    border: none;
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 12px 30px rgba(64, 158, 255, 0.45);
    }
  }
}

// 过渡动画
.face-modal-enter-active,
.face-modal-leave-active {
  transition: all 0.3s ease;
}

.face-modal-enter,
.face-modal-leave-to {
  opacity: 0;
  
  .face-recognition-modal {
    transform: scale(0.95) translateY(20px);
  }
}
</style>
