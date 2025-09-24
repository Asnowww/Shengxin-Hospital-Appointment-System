
<template>
  <div class="form-container">
    <h2>注册</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
  <label for="reg-account">账号</label>
  <input type="text" id="reg-account" v-model="regAccount" required 
  placeholder="至少3个字符长度"/>
  <div v-if="regAccount && !isAccountValid" style="color: red;">账号不能少于3个字符</div>
</div>

<!-- 邮箱 -->
<div class="form-group">
  <label for="reg-email">邮箱</label>
  <input
    type="email"
    id="reg-email"
    v-model="regEmail"
    required
    @input="checkEmailFormat"
  />
  <div v-if="regEmail && !isEmailValid" style="color: red;">邮箱格式不正确</div>
</div>

<!-- 密码 -->
<div class="form-group">
  <label for="reg-password">密码</label>
  <input
    type="password"
    id="reg-password"
    v-model="regPassword"
    required
    @input="checkStrength"
    placeholder="至少8位，包含字母、数字和特殊字符中的两种"
  />
  <div class="password-hint">至少8位，包含字母、数字、特殊字符中的两种</div>

  <!-- 只有密码有输入时才显示密码强度 -->
  <div v-if="regPassword" :style="{ color: strengthColor }">密码强度：{{ passwordStrength }}</div>
</div>



      <div class="form-group">
        <label for="reg-confirm-password">确认密码</label>
        <input type="password" id="reg-confirm-password" v-model="regConfirmPassword" required />
      </div>
      <button type="submit" :disabled="passwordStrength === '弱'">注册</button>
      <p :style="{ color: regMessageColor }">{{ regMessage }}</p>
    </form>
    <div class="register-hint">
      <span>已有账号？</span>
      <button type="button" class="link-button" @click="$emit('switch-tab', 'login')">去登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['switch-tab'])
const isAccountValid = computed(() => regAccount.value.length >= 3)

const regAccount = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regConfirmPassword = ref('')
const regMessage = ref('')
const regMessageColor = ref('red')
const passwordStrength = ref('弱')
const isEmailValid = ref(true)

function checkEmailFormat() {
  isEmailValid.value = isValidEmail(regEmail.value)
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function getPasswordStrength(password) {
  if (password.length < 8) return '弱'
  const hasLower = /[a-z]/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasDigit = /\d/.test(password)
  const hasSpecial = /\W/.test(password)
  const count = [hasLower, hasUpper, hasDigit, hasSpecial].filter(Boolean).length
  return count >= 3 ? '强' : count === 2 ? '中' : '弱'
}

const strengthColor = computed(() => {
  return passwordStrength.value === '强' ? 'green' : passwordStrength.value === '中' ? 'orange' : 'red'
})

function checkStrength() {
  passwordStrength.value = getPasswordStrength(regPassword.value)
}

async function handleRegister() {
  regMessage.value = ''
  if (!regAccount.value || !regEmail.value || !regPassword.value || !regConfirmPassword.value) {
    regMessage.value = '请填写所有字段'
    return
  }

  if (regAccount.value.length < 3) {
  regMessage.value = '账号长度不能少于3个字符'
  return
}


  if (!isValidEmail(regEmail.value)) {
    regMessage.value = '邮箱格式不正确'
    return
  }

  if (regPassword.value !== regConfirmPassword.value) {
    regMessage.value = '两次密码不一致'
    return
  }

  try {
    const res = await fetch('http://127.0.0.1:8000/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: regAccount.value,
        email: regEmail.value,
        password: regPassword.value
      })
    })
    const data = await res.json()
    regMessage.value = data.message
    regMessageColor.value = data.success ? 'green' : 'red'
    if (data.success) {
      setTimeout(() => emit('switch-tab', 'login'), 1500)
    }
  } catch (error) {
    regMessage.value = '注册失败，请检查服务'
  }
}
</script>
<style scoped>
.form-container {
  width: 100%;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  box-sizing: border-box;
  box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.3);
  transition: box-shadow 0.3s ease;
}

button {
  width: 100%;
  padding: 12px;
  margin-top: 10px;
  background-color: #000;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  color: #fff;
  opacity: 0.7;
}

.register-hint {
  margin: 24px 0;
  text-align: center;
  font-size: 14px;
  color: #333;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
white-space: nowrap; 
}
.password-hint {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

.link-button {
  background: none;
  border: none;
  color: #007bff;
  cursor: pointer;
  text-decoration: underline;
  font-size: 14px;
  padding: 0;
  margin: 0;
}

.link-button:hover {
  color: #0056b3;
}
</style>
