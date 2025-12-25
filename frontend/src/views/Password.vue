<template>
    <Navigation ref="navRef" />

  <div class="forget-password-container">
    <div class="forget-password-card">
      <!-- 返回按钮 -->
      <button @click="goBack" class="back-btn">
        <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        返回登录
      </button>

      <!-- 标题 -->
      <div class="header">
        <div class="icon">
          <svg xmlns="https://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
        </div>
        <h1>找回密码</h1>
        <p class="subtitle">通过邮箱验证码重置您的密码</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="steps">
        <div :class="['step', { active: currentStep >= 1, completed: currentStep > 1 }]">
          <div class="step-number">1</div>
          <span>验证邮箱</span>
        </div>
        <div class="step-line" :class="{ active: currentStep > 1 }"></div>
        <div :class="['step', { active: currentStep >= 2, completed: currentStep > 2 }]">
          <div class="step-number">2</div>
          <span>设置新密码</span>
        </div>
        <div class="step-line" :class="{ active: currentStep > 2 }"></div>
        <div :class="['step', { active: currentStep >= 3 }]">
          <div class="step-number">3</div>
          <span>完成</span>
        </div>
      </div>

      <!-- 步骤1: 验证邮箱 -->
      <form v-if="currentStep === 1" @submit.prevent="handleSendCode" class="form">
        <div class="form-group">
          <label>邮箱地址</label>
          <div class="input-wrapper">
            <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
              <polyline points="22,6 12,13 2,6"></polyline>
            </svg>
            <input 
              v-model="email" 
              type="email" 
              placeholder="请输入您的注册邮箱"
              :class="{ error: errors.email }"
              required
            />
          </div>
          <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
        </div>

        <div class="form-group">
          <label>验证码</label>
          <div class="code-input-group">
            <div class="input-wrapper flex-1">
              <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                v-model="code" 
                type="text" 
                placeholder="请输入6位验证码"
                maxlength="6"
                :class="{ error: errors.code }"
                required
              />
            </div>
            <button 
              type="button"
              @click="sendVerificationCode"
              :disabled="countdown > 0 || !email"
              class="code-btn">
              {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
            </button>
          </div>
          <span v-if="errors.code" class="error-text">{{ errors.code }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading">验证中...</span>
          <span v-else>下一步</span>
        </button>
      </form>

      <!-- 步骤2: 设置新密码 -->
      <form v-else-if="currentStep === 2" @submit.prevent="handleResetPassword" class="form">
        <div class="form-group">
          <label>新密码</label>
          <div class="input-wrapper">
            <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <input 
              v-model="newPassword" 
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入新密码（至少6位）"
              :class="{ error: errors.newPassword }"
              required
            />
            <button 
              type="button" 
              @click="showPassword = !showPassword"
              class="toggle-password">
              <svg v-if="showPassword" xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
              <svg v-else xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span v-if="errors.newPassword" class="error-text">{{ errors.newPassword }}</span>
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <div class="input-wrapper">
            <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <input 
              v-model="confirmPassword" 
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入新密码"
              :class="{ error: errors.confirmPassword }"
              required
            />
            <button 
              type="button" 
              @click="showConfirmPassword = !showConfirmPassword"
              class="toggle-password">
              <svg v-if="showConfirmPassword" xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
              <svg v-else xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
        </div>

        <div class="password-strength">
          <div class="strength-bar">
            <div :class="['strength-fill', passwordStrength]"></div>
          </div>
          <span class="strength-text">密码强度: {{ getPasswordStrengthText() }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading">重置中...</span>
          <span v-else>重置密码</span>
        </button>
      </form>

      <!-- 步骤3: 完成 -->
      <div v-else-if="currentStep === 3" class="success-state">
        <div class="success-icon">
          <svg xmlns="https://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <h2>密码重置成功！</h2>
        <p>您的密码已成功重置，请使用新密码登录</p>
        <button @click="goToLogin" class="submit-btn">
          前往登录
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const router = useRouter()

const currentStep = ref(1)
const loading = ref(false)
const countdown = ref(0)

// 步骤1数据
const email = ref('')
const code = ref('')

// 步骤2数据
const newPassword = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 错误信息
const errors = ref({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算密码强度
const passwordStrength = computed(() => {
  const pwd = newPassword.value
  if (!pwd) return 'weak'
  
  let strength = 0
  if (pwd.length >= 6) strength++
  if (pwd.length >= 10) strength++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
  if (/\d/.test(pwd)) strength++
  if (/[^a-zA-Z0-9]/.test(pwd)) strength++
  
  if (strength <= 2) return 'weak'
  if (strength <= 3) return 'medium'
  return 'strong'
})

// 获取密码强度文本
function getPasswordStrengthText() {
  const map = {
    weak: '弱',
    medium: '中',
    strong: '强'
  }
  return map[passwordStrength.value] || '弱'
}

// 清除错误
function clearErrors() {
  errors.value = {
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 验证邮箱
function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

// 发送验证码
let countdownTimer = null

async function sendVerificationCode() {
  clearErrors()

  // 基础校验
  if (!email.value) {
    errors.value.email = '请输入邮箱地址'
    return
  }

  if (!validateEmail(email.value)) {
    errors.value.email = '请输入有效的邮箱地址'
    return
  }

  // 防重复点击：已有倒计时禁止再次发送
  if (countdown.value > 0) {
    return
  }

  try {
    const res = await axios.post('/api/user/sendPasswordResetCode', { email: email.value })

    if (!res || !res.data) {
      errors.value.email = '服务器无响应'
      return
    }

    if (res.data.code !== 200) {
      errors.value.email = res.data.message || '发送失败，请稍后重试'
      return
    }

    alert('验证码已发送到您的邮箱')

    // 启动倒计时
    startCountdown()
  } catch (err) {
    errors.value.email = '发送失败，请检查您的邮箱是否正确'
    console.error('发送验证码失败:', err)
  }
}

// 启动倒计时函数，避免重复 setInterval
function startCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }

  countdown.value = 60

  countdownTimer = setInterval(() => {
    countdown.value--

    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}


// 验证验证码并进入下一步
async function handleSendCode() {
  clearErrors()
  
  if (!email.value) {
    errors.value.email = '请输入邮箱地址'
    return
  }
  
  if (!validateEmail(email.value)) {
    errors.value.email = '请输入有效的邮箱地址'
    return
  }
  
  if (!code.value) {
    errors.value.code = '请输入验证码'
    return
  }
  
  if (code.value.length !== 6) {
    errors.value.code = '请输入6位验证码'
    return
  }
  
  loading.value = true
  
  try {

    await axios.post('/api/user/verifyEmailCode', { 
      email: email.value,
      captcha: code.value 
    })
 
    // 验证成功，进入下一步
    currentStep.value = 2
  } catch (err) {
    errors.value.code = '验证码错误或已过期'
    console.error('验证失败', err)
  } finally {
    loading.value = false
  }
}

// 重置密码
async function handleResetPassword() {
  clearErrors()
  
  if (!newPassword.value) {
    errors.value.newPassword = '请输入新密码'
    return
  }
  
  if (newPassword.value.length < 6) {
    errors.value.newPassword = '密码至少需要6位'
    return
  }
  
  if (!confirmPassword.value) {
    errors.value.confirmPassword = '请确认新密码'
    return
  }
  
  if (newPassword.value !== confirmPassword.value) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    return
  }
  
  loading.value = true
  
  try {
  
    await axios.post('/api/user/resetPassword', {
      email: email.value,
      captcha: code.value,
      newPassword: newPassword.value
    })
    
    
    // 重置成功
    currentStep.value = 3
  } catch (err) {
    alert('密码重置失败，请重试')
    console.error('重置密码失败', err)
  } finally {
    loading.value = false
  }
}

// 返回登录
function goBack() {
  router.push('/login/patient')
}

// 前往登录
function goToLogin() {
  router.push('/login/patient')
}
</script>

<style scoped>
.forget-password-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.forget-password-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 2.5rem;
  width: 100%;
  max-width: 500px;
  position: relative;
}

/* 返回按钮 */
.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: none;
  color: #667eea;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 1rem;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
}

/* 头部 */
.header {
  text-align: center;
  margin-bottom: 2rem;
}

.icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.icon svg {
  color: white;
}

h1 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 2rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

/* 步骤指示器 */
.steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  padding: 0 1rem;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  flex: 0 0 auto;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #a0aec0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.step.completed .step-number {
  background: #48bb78;
  color: white;
}

.step span {
  font-size: 0.8rem;
  color: #a0aec0;
  font-weight: 500;
  white-space: nowrap;
}

.step.active span {
  color: #667eea;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e2e8f0;
  margin: 0 0.5rem;
  transition: all 0.3s ease;
}

.step-line.active {
  background: #667eea;
}

/* 表单 */
.form {
  margin-top: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  color: #4a5568;
  font-weight: 500;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 1rem;
  color: #a0aec0;
}

.input-wrapper input {
  width: 100%;
  padding: 0.875rem 1rem 0.875rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper input.error {
  border-color: #e53e3e;
}

.toggle-password {
  position: absolute;
  right: 1rem;
  background: transparent;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: #667eea;
}

.error-text {
  display: block;
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.375rem;
}

/* 验证码输入组 */
.code-input-group {
  display: flex;
  gap: 0.75rem;
}

.flex-1 {
  flex: 1;
}

.code-btn {
  padding: 0.875rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.code-btn:disabled {
  background: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}

/* 密码强度 */
.password-strength {
  margin-bottom: 1.5rem;
}

.strength-bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  width: 33%;
  background: #e53e3e;
}

.strength-fill.medium {
  width: 66%;
  background: #f59e0b;
}

.strength-fill.strong {
  width: 100%;
  background: #10b981;
}

.strength-text {
  font-size: 0.85rem;
  color: #718096;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 成功状态 */
.success-state {
  text-align: center;
  padding: 2rem 0;
}

.success-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  animation: scaleIn 0.5s ease;
}

.success-icon svg {
  color: white;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.success-state h2 {
  margin: 0 0 0.75rem 0;
  color: #2d3748;
  font-size: 1.5rem;
}

.success-state p {
  color: #718096;
  font-size: 0.95rem;
  margin: 0 0 2rem 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .forget-password-container {
    padding: 1rem;
  }

  .forget-password-card {
    padding: 2rem 1.5rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .steps {
    padding: 0;
  }

  .step span {
    font-size: 0.7rem;
  }

  .step-number {
    width: 32px;
    height: 32px;
    font-size: 0.85rem;
  }

  .code-input-group {
    flex-direction: column;
  }

  .code-btn {
    width: 100%;
  }
}
</style>