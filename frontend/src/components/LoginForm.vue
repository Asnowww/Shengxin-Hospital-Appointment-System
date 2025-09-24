
<template>
  <div class="form-container">
    <h2>登录</h2>
    <form @submit.prevent="handleLogin">
      <div v-if="loginMethod === 'password'">
        <div class="form-group">
          <label for="account">账号</label>
          <input type="text" id="account" v-model="account" required />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <div class="form-group code-group">
          <label for="captcha">验证码</label>
          <input type="text" id="captcha" v-model="captcha" maxlength="4" required autocomplete="off" />
          <img :src="captchaImgUrl" @click="refreshCaptcha" alt="验证码" />
        </div>
      </div>

      <div v-else-if="loginMethod === 'sms'">
        <div class="form-group">
          <label for="email">邮箱</label>
          <input type="email" id="email" v-model="email" required />
        </div>
        <div class="form-group code-group">
          <label for="smsCode">验证码</label>
          <input type="text" id="smsCode" v-model="smsCode" required />
          <button type="button" :disabled="countdown > 0" @click="sendSmsCode">
            {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
          </button>
        </div>
      </div>

      <button type="submit">登录</button>
      <p :style="{ color: messageColor }">{{ message }}</p>
    </form>

    <div class="other-login-methods">
      <span>其他登录方式</span>
      <div class="login-method-buttons">
        <button :class="{ active: loginMethod === 'password' }" @click="loginMethod = 'password'">账号密码登录</button>
        <button :class="{ active: loginMethod === 'sms' }" @click="loginMethod = 'sms'">邮箱验证码登录</button>
      </div>
      <div class="register-hint">
        <span>没有账号？</span>
        <button type="button" class="link-button" @click="$emit('switch-tab', 'register')">去注册</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
// import http from '@/utils/http'

const router = useRouter()

// props & emits
const emit = defineEmits(['switch-tab'])

// 登录状态
const loginMethod = ref('password')
const account = ref('')
const password = ref('')
const email = ref('')
const smsCode = ref('')
const captcha = ref('')
const captchaImgUrl = ref('http://127.0.0.1:8000/api/auth/captcha?' + Date.now())

const countdown = ref(0)
let timer = null

const message = ref('')
const messageColor = ref('red')

function refreshCaptcha() {
  captchaImgUrl.value = 'http://127.0.0.1:8000/api/auth/captcha?' + Date.now()
}

function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

function sendSmsCode() {
  if (!email.value) {
    message.value = '请输入邮箱'
    messageColor.value = 'red'
    return
  }
  if (!isValidEmail(email.value)) {
    message.value = '邮箱格式不正确'
    messageColor.value = 'red'
    return
  }

  fetch('http://127.0.0.1:8000/api/auth/send_email_code', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: email.value })
  })
    .then(res => res.json())
    .then(data => {
      message.value = data.message
      messageColor.value = data.success ? 'green' : 'red'
      if (data.success) startCountdown()
    })
    .catch(() => {
      message.value = '请求失败'
      messageColor.value = 'red'
    })
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

async function handleLogin() {
  message.value = ''
  try {
    const url = loginMethod.value === 'password' ? '/auth/login' : '/auth/login_email'
    const payload = loginMethod.value === 'password'
      ? { username: account.value, password: password.value, captcha: captcha.value }
      : { email: email.value, code: smsCode.value }

    const res = await http.post(url, payload)

    message.value = res.data.message
    messageColor.value = res.data.success ? 'green' : 'red'

    if (res.data.success) {
      const token = res.data.access_token
      localStorage.setItem('token', token)
      const decoded = jwtDecode(token)
      localStorage.setItem('userInfo', JSON.stringify(decoded))
      router.push('/first_page')
    }

  } catch (err) {
    // 优先显示后端返回的详细错误信息
    if (err.response && err.response.data && err.response.data.message) {
      message.value = err.response.data.message
    } else {
      message.value = '登录失败，请检查服务'
    }
    messageColor.value = 'red'
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

.code-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-group input {
  flex: 1;
  min-width: 0;
}

.code-group img {
  height: 40px;
  border-radius: 4px;
  border: 1px solid #ccc;
  cursor: pointer;
}

.code-group button {
  flex: 0 0 auto;
  width: 110px;
  padding: 6px 10px;
  font-size: 14px;
  border-radius: 6px;
  border: 1px solid #000;
  background-color: #eee;
  cursor: pointer;
  white-space: nowrap;
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

button[type="button"] {
  background-color: #ccc;
  color: #000;
}

button:hover {
  opacity: 0.9;
}

button:disabled {
  background-color: #ccc !important;
  color: #fff !important;
  cursor: not-allowed !important;
  border: none !important;
  opacity: 0.7;
}

.other-login-methods {
  margin-top: 20px;
}

.login-method-buttons {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.login-method-buttons button {
  flex: 1;
  padding: 10px;
  background-color: #ccc;
  border: none;
  border-radius: 6px;
  color: #fff;
  font-weight: bold;
  cursor: pointer;
}

.login-method-buttons button.active {
  background-color: #000;
  color: #fff;
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
