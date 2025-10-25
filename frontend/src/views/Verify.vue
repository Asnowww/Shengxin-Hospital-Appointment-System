<template>
  <transition name="modal-fade">
    <div v-if="props.visible" class="modal-overlay">
      <div class="modal-container">
        <!-- 关闭按钮 -->
        <button
          class="close-btn"
          @click="handleCloseClick"
          :disabled="loading"
          aria-label="关闭弹窗"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
            fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round"
          >
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <!-- 头部 -->
        <div class="verify-header">
          <h2 class="title">身份认证</h2>
          <p class="subtitle">请使用摄像头拍照上传您的证件</p>
        </div>

        <!-- 表单主体 -->
        <form class="modal-body">
          <!-- 摄像头预览 -->
          <div v-if="!formData.credentialPhotoPreview" class="camera-section">
            <div class="camera-wrapper">
              <video 
                v-show="showCamera"
                ref="videoRef"
                class="camera-video"
                autoplay
                muted
                playsinline
              ></video>
              <div v-if="!showCamera" class="camera-placeholder">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 11l-3.5 3.5a4 4 0 0 1-5.656 0l-.707-.707a4 4 0 0 1 0-5.656l3.5-3.5"></path>
                  <circle cx="12" cy="12" r="10"></circle>
                </svg>
                <p>摄像头已关闭</p>
              </div>
            </div>
            <canvas ref="canvasRef" style="display: none;"></canvas>

            <div class="camera-controls">
              <button 
                v-if="!showCamera"
                type="button"
                @click="initCamera"
                :disabled="loading"
                class="btn btn-primary"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
                  <circle cx="12" cy="13" r="4"></circle>
                </svg>
                启动摄像头
              </button>
              <button 
                v-else
                type="button"
                @click="capturePhoto"
                :disabled="loading"
                class="btn btn-capture"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <circle cx="12" cy="12" r="10"></circle>
                </svg>
                拍照
              </button>
            </div>

            <div v-if="error" class="error-message">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ error }}
            </div>
          </div>

          <!-- 照片预览 -->
          <div v-else class="preview-section">
            <div class="preview-wrapper">
              <img :src="formData.credentialPhotoPreview" alt="证件照片预览" class="preview-image">
            </div>

            <div class="preview-actions">
              <button
                type="button"
                @click="retakePhoto"
                :disabled="loading"
                class="btn btn-secondary"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="1 4 1 10 7 10"></polyline>
                  <path d="M3.51 15a9 9 0 0 1 14.85-4.95M23 20v-6h-6"></path>
                  <path d="M20.49 9A9 9 0 0 0 5.64 5.64"></path>
                </svg>
                重新拍照
              </button>
              <button
                type="button"
                @click="handleSubmit"
                :disabled="loading"
                class="btn btn-primary"
              >
                <span v-if="!loading">提交认证</span>
                <span v-else>提交中...</span>
              </button>
            </div>
          </div>
        </form>

        <!-- 底部提示 -->
        <div class="verify-footer">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="16" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12.01" y2="8"></line>
          </svg>
          <span>请确保证件照片清晰可见，包含完整的证件信息。提交后管理员将在1-3个工作日内完成审核。</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'success', 'cancel'])

const token = localStorage.getItem('token')
const loading = ref(false)
const showCamera = ref(false)
const error = ref('')
const videoRef = ref(null)
const canvasRef = ref(null)
let stream = null

const formData = reactive({
  credentialPhoto: null,
  credentialPhotoPreview: null
})

const initCamera = async () => {
  error.value = ''
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { 
        facingMode: 'environment',
        width: { ideal: 1280 },
        height: { ideal: 720 }
      },
      audio: false
    })
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      showCamera.value = true
    }
  } catch (err) {
    console.error('摄像头初始化失败:', err)
    if (err.name === 'NotAllowedError') {
      error.value = '您拒绝了摄像头访问权限，请在浏览器设置中允许'
    } else if (err.name === 'NotFoundError') {
      error.value = '未找到摄像头设备'
    } else {
      error.value = '摄像头初始化失败，请检查权限设置'
    }
  }
}

const handleVideoLoaded = () => {
  if (canvasRef.value && videoRef.value) {
    canvasRef.value.width = videoRef.value.videoWidth
    canvasRef.value.height = videoRef.value.videoHeight
  }
}

const capturePhoto = () => {
  if (!canvasRef.value || !videoRef.value) return
  
  const ctx = canvasRef.value.getContext('2d')
  ctx.drawImage(videoRef.value, 0, 0, canvasRef.value.width, canvasRef.value.height)
  
  canvasRef.value.toBlob((blob) => {
    if (blob) {
      formData.credentialPhoto = blob
      formData.credentialPhotoPreview = URL.createObjectURL(blob)
      stopCamera()
    }
  }, 'image/jpeg', 0.95)
}

const retakePhoto = () => {
  formData.credentialPhoto = null
  formData.credentialPhotoPreview = null
  error.value = ''
  initCamera()
}

const stopCamera = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  showCamera.value = false
}

const handleSubmit = async () => {
  if (!formData.credentialPhoto) {
    error.value = '请先拍照'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const payload = new FormData()
    payload.append('idPhoto', formData.credentialPhoto)

    const { data } = await axios.post('/api/patient/verify', payload, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    })

    if (data.success) {
      emit('success')
      alert('认证申请已提交，请等待管理员审核')
      handleClose()
    } else {
      error.value = data.message || '上传失败，请稍后重试'
    }
  } catch (err) {
    console.error('提交失败:', err)
    error.value = err.response?.data?.message || err.message || '上传失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  if (loading.value) return
  emit('cancel')
  handleClose()
}

const handleCloseClick = () => {
  if (formData.credentialPhoto) {
    if (confirm('已拍摄的照片将不会保存，确定要退出吗？')) {
      handleClose()
    }
  } else {
    handleClose()
  }
}

const handleClose = () => {
  stopCamera()
  formData.credentialPhoto = null
  formData.credentialPhotoPreview = null
  error.value = ''
  emit('update:visible', false)
}

onBeforeUnmount(() => {
  stopCamera()
})
</script>

<style scoped>
.verify-header {
  text-align: center;
  padding: 2.5rem 2rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.title {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.5;
}

.modal-body {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 摄像头部分 */
.camera-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.camera-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 75%;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
}

.camera-video,
.camera-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.camera-video {
  object-fit: cover;
  width: 100%;
  height: 100%;
  display: block;
}

.camera-placeholder {
  background: #f7fafc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a0aec0;
}

.camera-placeholder svg {
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.camera-placeholder p {
  margin: 0;
  font-size: 0.9rem;
}

.camera-controls {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

/* 预览部分 */
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.preview-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 75%;
  border-radius: 12px;
  overflow: hidden;
  background: #f7fafc;
}

.preview-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-actions {
  display: flex;
  gap: 1rem;
}

/* 按钮样式 */
.btn {
  flex: 1;
  padding: 0.875rem 1.25rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.btn-capture {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  padding: 0;
  flex: none;
}

.btn-capture:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-secondary:hover:not(:disabled) {
  background: #cbd5e0;
}

/* 错误消息 */
.error-message {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: #fff5f5;
  border: 1px solid #feb2b2;
  border-radius: 8px;
  color: #c53030;
  font-size: 0.85rem;
  line-height: 1.5;
}

.error-message svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

/* 底部提示 */
.verify-footer {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.05) 0%, rgba(251, 146, 60, 0.03) 100%);
  border-top: 1px solid #fef3c7;
}

.verify-footer svg {
  flex-shrink: 0;
  color: #f59e0b;
  margin-top: 0.125rem;
}

.verify-footer span {
  color: #78350f;
  font-size: 0.8rem;
  line-height: 1.6;
}

/* 过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-container,
.modal-fade-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-fade-enter-from .modal-container,
.modal-fade-leave-to .modal-container {
  transform: scale(0.95);
}

/* 遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

/* 弹窗容器 */
.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 500px;
  position: relative;
  max-height: 90vh;
  overflow-y: auto;
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 32px;
  height: 32px;
  background: #f7fafc;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #718096;
  z-index: 10;
}

.close-btn:hover:not(:disabled) {
  background: #e2e8f0;
  color: #2d3748;
  transform: rotate(90deg);
}

.close-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-container {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }

  .modal-body {
    padding: 1.5rem;
    gap: 1rem;
  }

  .verify-header {
    padding: 2rem 1.5rem 1.5rem;
  }

  .verify-footer {
    padding: 1rem 1.5rem;
    gap: 0.5rem;
  }

  .verify-footer span {
    font-size: 0.75rem;
  }

  .camera-controls {
    flex-direction: column;
  }

  .preview-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .btn-capture {
    width: 100%;
    border-radius: 10px;
  }
}
</style>