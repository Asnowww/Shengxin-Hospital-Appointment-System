<template>
  <!-- 修改密码弹窗 -->
  <div v-if="visible" class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <div class="modal-header">
        <h3>修改密码</h3>
        <button @click="handleClose" class="close-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>旧密码 *</label>
          <input 
            v-model="form.oldPassword" 
            type="password"
            class="form-control"
            placeholder="请输入旧密码"
            required
          />
          <p v-if="errors.oldPassword" class="error-text">
            {{ errors.oldPassword }}
          </p>
        </div>
        
        <div class="form-group">
          <label>新密码 *</label>
          <input 
            v-model="form.newPassword" 
            type="password"
            class="form-control"
            placeholder="请输入新密码"
            required
          />
          <p v-if="errors.newPassword" class="error-text">
            {{ errors.newPassword }}
          </p>
        </div>

        <div class="form-group">
          <label>确认密码 *</label>
          <input 
            v-model="form.confirmPassword" 
            type="password"
            class="form-control"
            placeholder="请再次输入新密码"
            required
          />
          <p v-if="errors.confirmPassword" class="error-text">
            {{ errors.confirmPassword }}
          </p>
        </div>

        <div class="modal-footer">
          <button type="button" @click="handleClose" class="cancel-btn">取消</button>
          <button type="submit" class="submit-btn">确认修改</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import axios from 'axios'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  userId: {
    type: String,
    required: false,
    default: () => localStorage.getItem('userId')
  },
  apiUrl: {
    type: String,
    default: '/api/user/changePassword'
  }
})

// Emits
const emit = defineEmits(['close', 'success'])

// 表单数据
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 错误信息
const errors = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证
function validateForm() {
  // 清空之前的错误
  errors.oldPassword = ''
  errors.newPassword = ''
  errors.confirmPassword = ''

  let isValid = true

  if (!form.oldPassword) {
    errors.oldPassword = '请输入旧密码'
    isValid = false
  }

  if (!form.newPassword) {
    errors.newPassword = '新密码不能为空'
    isValid = false
  } else if (form.newPassword.length < 6) {
    errors.newPassword = '密码长度至少6位'
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 重置表单
function resetForm() {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  errors.oldPassword = ''
  errors.newPassword = ''
  errors.confirmPassword = ''
}

// 关闭弹窗
function handleClose() {
  resetForm()
  emit('close')
}

// 提交表单
async function handleSubmit() {
  if (!validateForm()) return

  try {
    const res = await axios.put(props.apiUrl, {
      userId: props.userId,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })

    const { code, message } = res.data

    if (code === 200) {
      alert(message || '密码修改成功')
      emit('success')
      handleClose()
    } else {
      alert(message || '密码修改失败')
    }
  } catch (err) {
    alert('网络异常或服务器错误')
    console.error(err)
  }
}

// 监听 visible 变化，关闭时重置表单
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>

<style scoped>
/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #718096;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f7fafc;
  color: #2d3748;
}

form {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
  box-sizing: border-box;
}

.form-control:hover {
  border-color: #cbd5e0;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.error-text {
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
  margin-top: 1rem;
}

.cancel-btn,
.submit-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 1rem;
  }

  form {
    padding: 1.5rem;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>