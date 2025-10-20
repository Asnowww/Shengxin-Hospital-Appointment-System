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
          <h2 class="title">身份认证申请</h2>
          <p class="subtitle">请上传您的证件照片，供管理员审核</p>
        </div>

        <!-- 表单主体 -->
        <form  class="modal-body">
          <PhotoUploadSection
            v-model:file="formData.credentialPhoto"
            v-model:preview="formData.credentialPhotoPreview"
            :error="errors.credentialPhoto"
            :loading="loading"
          />

          <ButtonGroup
            :loading="loading"
            :can-submit="canSubmit"
            @submit="handleSubmit"
            @cancel="handleCancel"
          />
        </form>

        <!-- 底部提示 -->
        <div class="verify-footer">
          <!-- SVG略 -->
          <span>提交后管理员将在1-3个工作日内完成审核，请耐心等待。请确保上传的证件照片清晰可见，包含完整的证件信息。</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import PhotoUploadSection from '@/components/PhotoUploadSection.vue'
import ButtonGroup from '@/components/ButtonGroup.vue'
import axios from 'axios'

const props = defineProps({
  visible: { type: Boolean, default: false },
  // authStatus: { type: String, default: null, validator: v => [null, 'pending', 'approved', 'rejected'].includes(v) }
})

const emit = defineEmits(['update:visible', 'success', 'cancel'])

const token = localStorage.getItem('token')
const loading = ref(false)

const formData = reactive({
  credentialPhoto: null,
  credentialPhotoPreview: null
})

const errors = reactive({
  credentialPhoto: ''
})

const canSubmit = computed(() => !!formData.credentialPhoto && !loading.value)

const clearErrors = () => errors.credentialPhoto = ''

const handleSubmit = async () => {
  clearErrors()
  if (!formData.credentialPhoto) {
    errors.credentialPhoto = '请上传证件照片'
    return
  }

  loading.value = true

  try {
    const payload = new FormData()
    payload.append('idPhoto', formData.credentialPhoto)

    // 
    const { data } = await axios.post('/api/patient/verify', payload, {
      headers: {  'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data' }
    })

    // 后端返回成功判断，待调整
    if (data.success) {
      emit('success')
      alert('认证申请已提交，请等待管理员审核')
      handleClose()
    } else {
      errors.credentialPhoto = data.message || '上传失败，请稍后重试'
    }
  } catch (err) {
    console.error('提交失败:', err)
    errors.credentialPhoto = err.response?.data?.message || err.message || '上传失败，请稍后重试'
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
    if (confirm('已上传的图片将不会保存，确定要退出吗？')) {
      handleClose()
    }
  } else {
    handleClose()
  }
}

const handleClose = () => {
  formData.credentialPhoto = null
  formData.credentialPhotoPreview = null
  clearErrors()
  emit('update:visible', false)
}
</script>




<style scoped>
.verify-header {
  text-align: center;
  padding: 2.5rem 2rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.icon-wrapper {
  margin-bottom: 1.5rem;
}

.icon-wrapper svg {
  width: 80px;
  height: 80px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: block;
  margin: 0 auto;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.title {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.5;
}

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

@media (max-width: 768px) {
  .verify-footer {
    padding: 1rem 1.5rem;
    gap: 0.5rem;
  }

  .verify-footer span {
    font-size: 0.75rem;
  }
}

@media (max-width: 768px) {
  .verify-header {
    padding: 2rem 1.5rem 1.5rem;
  }

  .icon-wrapper svg {
    width: 64px;
    height: 64px;
    padding: 16px;
  }

  .title {
    font-size: 1.25rem;
  }

  .subtitle {
    font-size: 0.85rem;
  }
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

/* 表单主体 */
.modal-body {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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
}
</style>