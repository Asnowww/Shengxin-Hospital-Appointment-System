
 <template>
  <transition name="modal">
    <div v-if="visible" class="modal-overlay" @click.self="handleCancel">
      <div class="modal-container">
        <!-- 关闭按钮 -->
        <button @click="handleCancel" class="close-btn" :disabled="loading">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <!-- 弹窗内容 -->
        <div class="modal-header">
          <div class="icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="8.5" cy="7" r="4"></circle>
              <polyline points="17 11 19 13 23 9"></polyline>
            </svg>
          </div>
          <h2>身份认证申请</h2>
          <p class="subtitle">请填写您的真实信息，管理员审核后即可完成认证</p>
        </div>

        <form @submit.prevent="handleSubmit" class="modal-body">
          <!-- 学号/工号输入 -->
          <div class="form-group">
            <label class="form-label required">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                <line x1="12" y1="18" x2="12.01" y2="18"></line>
              </svg>
              学号/教工号
            </label>
            <input 
              v-model="formData.studentId" 
              type="text" 
              placeholder="请输入完整的学号或教工号"
              class="form-input"
              :class="{ error: errors.studentId }"
              :disabled="loading"
              required
            />
            <span v-if="errors.studentId" class="error-text">{{ errors.studentId }}</span>
            <span v-else class="hint-text">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              学生请输入学号，教职工请输入教工号
            </span>
          </div>

          <!-- 真实姓名输入 -->
          <div class="form-group">
            <label class="form-label required">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              真实姓名
            </label>
            <input 
              v-model="formData.realName" 
              type="text" 
              placeholder="请输入您的真实姓名"
              class="form-input"
              :class="{ error: errors.realName }"
              :disabled="loading"
              required
            />
            <span v-if="errors.realName" class="error-text">{{ errors.realName }}</span>
            <span v-else class="hint-text">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              请输入与学号/教工号对应的真实姓名
            </span>
          </div>

          <!-- 审核状态提示 -->
          <div v-if="authStatus" class="status-card" :class="authStatus">
            <div class="status-icon">
              <svg v-if="authStatus === 'pending'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <svg v-else-if="authStatus === 'approved'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
              </svg>
            </div>
            <div class="status-content">
              <h4>{{ getStatusTitle() }}</h4>
              <p>{{ getStatusMessage() }}</p>
            </div>
          </div>

          <!-- 按钮组 -->
          <div class="button-group">
            <button 
              type="button" 
              @click="handleCancel" 
              class="cancel-btn"
              :disabled="loading">
              取消
            </button>
            <button 
              type="submit" 
              class="submit-btn"
              :disabled="loading || !canSubmit">
              <span v-if="loading" class="loading-content">
                <svg class="spinner" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="2" x2="12" y2="6"></line>
                  <line x1="12" y1="18" x2="12" y2="22"></line>
                  <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                  <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                  <line x1="2" y1="12" x2="6" y2="12"></line>
                  <line x1="18" y1="12" x2="22" y2="12"></line>
                  <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                  <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
                </svg>
                提交中...
              </span>
              <span v-else>提交审核</span>
            </button>
          </div>
        </form>

        <!-- 弹窗底部 -->
        <div class="modal-footer">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <span>提交后管理员将在1-3个工作日内完成审核，请耐心等待</span>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'
// import axios from 'axios'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  // 如果有现有的审核状态，可以传入
  authStatus: {
    type: String,
    default: null, // null | 'pending' | 'approved' | 'rejected'
    validator: (value) => [null, 'pending', 'approved', 'rejected'].includes(value)
  }
})

const emit = defineEmits(['update:visible', 'success', 'cancel'])

const formData = ref({
  studentId: '',
  realName: ''
})

const loading = ref(false)

const errors = ref({
  studentId: '',
  realName: ''
})

// 是否可以提交
const canSubmit = computed(() => {
  return formData.value.studentId.trim() && formData.value.realName.trim()
})

// 清除错误信息
function clearErrors() {
  errors.value = {
    studentId: '',
    realName: ''
  }
}

// 验证学号/工号格式
function validateStudentId(id) {
  if (!id.trim()) {
    return '请输入学号或教工号'
  }
  if (id.length < 4) {
    return '学号/教工号至少需要4位'
  }
  if (id.length > 20) {
    return '学号/教工号不能超过20位'
  }
  // 只能包含数字和字母
  if (!/^[a-zA-Z0-9]+$/.test(id)) {
    return '学号/教工号只能包含数字和字母'
  }
  return ''
}

// 验证姓名格式
function validateName(name) {
  if (!name.trim()) {
    return '请输入真实姓名'
  }
  if (name.length < 2) {
    return '姓名至少需要2个字符'
  }
  if (name.length > 20) {
    return '姓名不能超过20个字符'
  }
  // 检查是否包含中文或英文字母
  if (!/^[\u4e00-\u9fa5a-zA-Z\s·]+$/.test(name)) {
    return '姓名只能包含中文、英文字母和空格'
  }
  return ''
}

// 获取状态标题
function getStatusTitle() {
  const statusMap = {
    pending: '审核中',
    approved: '审核通过',
    rejected: '审核未通过'
  }
  return statusMap[props.authStatus] || ''
}

// 获取状态消息
function getStatusMessage() {
  const messageMap = {
    pending: '您的认证申请已提交，管理员正在审核中',
    approved: '恭喜！您的身份认证已通过',
    rejected: '您的认证申请未通过，请检查信息后重新提交'
  }
  return messageMap[props.authStatus] || ''
}

// 提交审核申请
async function handleSubmit() {
  clearErrors()
  
  // 前端验证
  const studentIdError = validateStudentId(formData.value.studentId)
  if (studentIdError) {
    errors.value.studentId = studentIdError
    return
  }
  
  const nameError = validateName(formData.value.realName)
  if (nameError) {
    errors.value.realName = nameError
    return
  }
  
  loading.value = true
  
  try {
    // 实际使用时替换为：
    // const { data } = await axios.post('/api/auth/submit-verification', {
    //   studentId: formData.value.studentId.trim(),
    //   realName: formData.value.realName.trim()
    // })
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 提交成功
    emit('success', {
      studentId: formData.value.studentId.trim(),
      realName: formData.value.realName.trim()
    })
    
    alert('认证申请已提交，请等待管理员审核')
    handleClose()
  } catch (err) {
    console.error('提交认证申请失败', err)
    
    // 如果是后端返回的特定错误
    if (err.response?.data?.field === 'studentId') {
      errors.value.studentId = err.response.data.message || '学号/教工号不存在'
    } else if (err.response?.data?.field === 'realName') {
      errors.value.realName = err.response.data.message || '姓名格式错误'
    } else {
      alert('提交失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 取消
function handleCancel() {
  if (loading.value) return
  emit('cancel')
  handleClose()
}

// 关闭弹窗
function handleClose() {
  formData.value = {
    studentId: '',
    realName: ''
  }
  clearErrors()
  emit('update:visible', false)
}
</script>

<style scoped>
/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
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
  z-index: 1;
}

.close-btn:hover:not(:disabled) {
  background: #e2e8f0;
  color: #2d3748;
}

.close-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 弹窗头部 */
.modal-header {
  text-align: center;
  padding: 2.5rem 2rem 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.icon svg {
  color: white;
}

.modal-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

/* 弹窗主体 */
.modal-body {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.form-label.required::after {
  content: '*';
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-label svg {
  color: #667eea;
}

/* 输入框 */
.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error {
  border-color: #e53e3e;
}

.form-input:disabled {
  background: #f7fafc;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 提示文本 */
.error-text {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.375rem;
}

.hint-text {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #718096;
  font-size: 0.8rem;
  margin-top: 0.375rem;
}

.hint-text svg {
  flex-shrink: 0;
  color: #a0aec0;
}

/* 审核状态卡片 */
.status-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
}

.status-card.pending {
  background: #fffbeb;
  border: 2px solid #fbbf24;
}

.status-card.approved {
  background: #d1fae5;
  border: 2px solid #10b981;
}

.status-card.rejected {
  background: #fee2e2;
  border: 2px solid #ef4444;
}

.status-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-card.pending .status-icon {
  background: #fbbf24;
  color: white;
}

.status-card.approved .status-icon {
  background: #10b981;
  color: white;
}

.status-card.rejected .status-icon {
  background: #ef4444;
  color: white;
}

.status-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
}

.status-card.pending .status-content h4 {
  color: #92400e;
}

.status-card.approved .status-content h4 {
  color: #065f46;
}

.status-card.rejected .status-content h4 {
  color: #991b1b;
}

.status-content p {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.5;
}

.status-card.pending .status-content p {
  color: #b45309;
}

.status-card.approved .status-content p {
  color: #047857;
}

.status-card.rejected .status-content p {
  color: #dc2626;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover:not(:disabled) {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.cancel-btn:disabled,
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 弹窗底部 */
.modal-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2rem 2rem;
  color: #718096;
  font-size: 0.8rem;
  line-height: 1.5;
}

.modal-footer svg {
  flex-shrink: 0;
  color: #f59e0b;
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-container {
    margin: 1rem;
  }

  .modal-header {
    padding: 2rem 1.5rem 1rem;
  }

  .modal-body {
    padding: 1.5rem;
  }

  .modal-footer {
    padding: 1rem 1.5rem 1.5rem;
  }

  .button-group {
    flex-direction: column;
  }

  .icon {
    width: 64px;
    height: 64px;
  }

  .icon svg {
    width: 32px;
    height: 32px;
  }

  .status-card {
    flex-direction: column;
    text-align: center;
  }

  .status-icon {
    margin: 0 auto;
  }
}
</style>