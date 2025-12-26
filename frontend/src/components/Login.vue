<template>
  <div class="login-container">
    <div class="glass-layout">
      
      <!-- 顶部导航卡片 -->
      <div class="nav-card">
        <div class="brand" @click="goHome">
          <svg xmlns="https://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="brand-icon">
             <path d="M11.484 2.17a.75.75 0 011.032 0 11.209 11.209 0 007.877 3.08.75.75 0 01.722.515 12.74 12.74 0 01.635 3.985c0 5.942-4.064 10.933-9.563 12.348a.749.749 0 01-.374 0C6.314 20.683 2.25 15.692 2.25 9.75c0-1.39.223-2.73.635-3.985a.75.75 0 01.722-.516 11.209 11.209 0 007.877-3.08zM12 6.972a.75.75 0 01.75.75v2.06h2.06a.75.75 0 010 1.5h-2.06v2.06a.75.75 0 01-1.5 0v-2.06H9.19a.75.75 0 010-1.5h2.06V7.722a.75.75 0 01.75-.75z" />
          </svg>
          <span class="brand-text">圣心医院 | Shengxin Hospital</span>
        </div>
        <button class="home-btn" @click="goHome">
          返回首页
        </button>
      </div>

      <!-- 登录表单卡片 -->
      <div class="login-card">
        <div class="card-header">
           <h2 class="login-title">{{ roleTitle }}登录</h2>
           <p class="login-subtitle">请填写您的账户信息以继续</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="input-group">
            <label>账号</label>
            <input 
              v-model="account" 
              type="text" 
              placeholder="手机号 / 用户姓名 / 邮箱" 
              required 
              class="custom-input"
            />
          </div>

          <div class="input-group">
            <label>密码</label>
            <input 
              v-model="password" 
              type="password" 
              placeholder="请输入密码" 
              required 
              class="custom-input"
            />
          </div>

          <!-- 图形验证码 -->
          <div class="input-group">
            <label>验证码</label>
            <div class="captcha-row">
              <input 
                v-model="captchaCode" 
                type="text" 
                placeholder="请输入验证码，区分大小写" 
                required 
                class="custom-input captcha-input"
              />
              <div class="captcha-box" @click="loadCaptcha" title="点击刷新">
                <img 
                  v-if="captchaImage"
                  :src="captchaImage" 
                  alt="验证码" 
                  class="captcha-img" 
                />
                <span v-else class="captcha-placeholder">加载中...</span>
              </div>
            </div>
          </div>

          <button type="submit" class="submit-btn">立即登录</button>

          <div class="form-footer">
            <p v-if="currentRole !== 'doctor' && currentRole !== 'admin'" class="footer-link">
              没有账号？ <router-link to="/register">立即注册</router-link>
            </p>
            <p v-if="currentRole !== 'admin'" class="footer-link">
              <router-link to="/password">忘记密码？</router-link>
            </p>
          </div>
        </form>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const account = ref('')
const password = ref('')
const captchaCode = ref('')
const captchaImage = ref('')
const captchaId = ref('')

const currentRole = computed(() => route.params.role)
const roleTitle = computed(() => {
  const roleMap = { patient: '患者', doctor: '医生', admin: '管理员' }
  return roleMap[currentRole.value] || '用户'
})

function goHome() {
  router.push('/welcome')
}

onMounted(() => {
  loadCaptcha()
})

async function loadCaptcha() {
  try {
    const res = await axios.get('/api/captcha/graph', { responseType: 'blob' })
    captchaId.value = res.headers['captcha-id']
    captchaImage.value = URL.createObjectURL(res.data)
  } catch (err) {
    console.error('获取验证码失败', err)
  }
}

async function handleLogin() {
  if (!account.value || !password.value || !captchaCode.value) {
    alert('请输入完整信息')
    return
  }

  try {
    const response = await axios.post('/api/auth/login', {
      account: account.value,
      password: password.value,
      roleType: currentRole.value,
      captchaId: captchaId.value,
      captchaCode: captchaCode.value
    })

    const res = response.data

    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.roleType)
      localStorage.setItem('userId', res.data.account)

      if (res.data.roleType === 'doctor') {
        localStorage.setItem('doctorId', res.data.doctorId)
      }
      if (res.data.roleType === 'patient') {
        localStorage.setItem('patientId', res.data.patientId)
      }

      // alert('登录成功！') // Optional: remove alert for smoother UX

      switch (res.data.roleType) {
        case 'admin':
          router.push('/admin/profile')
          break
        case 'doctor':
          router.push('/doctor/profile')
          break
        default:
          router.push('/department')
      }
    } else {
      alert(res.message || '登录失败')
      loadCaptcha()
      captchaCode.value = ''
    }
  } catch (err) {
    console.error(err)
    alert(err?.response?.data?.message || '服务器连接错误')
    loadCaptcha()
    captchaCode.value = ''
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* Make sure bg is transparent if using App.vue fixed background */
  background: transparent; 
  padding: 20px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

.glass-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  max-width: 480px;
  z-index: 10;
}

/* --- Navigation Card --- */
.nav-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 15px 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  animation: fadeInDown 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-icon {
  width: 24px;
  height: 24px;
  color: #ff758c;
}

.brand-text {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
}

.home-btn {
  background: none;
  border: 1px solid #eee;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.home-btn:hover {
  border-color: #ff758c;
  color: #ff758c;
}

/* --- Login Card --- */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px 35px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.08);
  animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.login-subtitle {
  color: #999;
  font-size: 0.95rem;
}

/* --- Form Styles --- */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #555;
  margin-left: 4px;
}

.custom-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s;
  background: #f9f9f9;
  box-sizing: border-box; /* Ensure padding doesn't affect width */
}

.custom-input:focus {
  background: #fff;
  border-color: #ff7eb3;
  outline: none;
  box-shadow: 0 0 0 4px rgba(255, 126, 179, 0.1);
}

/* Captcha Row */
.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-box {
  width: 110px;
  height: 48px;
  display: flex;
  align-items: center;
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  justify-content: center;
  background: #fff;
}

.captcha-box img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}


.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  font-size: 0.8rem;
  color: #999;
}

/* Submit Button */
.submit-btn {
  margin-top: 10px;
  background: linear-gradient(135deg, #ff758c 0%, #ff7eb3 100%);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 117, 140, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 117, 140, 0.4);
}

.submit-btn:active {
  transform: scale(0.98);
}

/* Footer */
.form-footer {
  margin-top: 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-link {
  font-size: 0.9rem;
  color: #888;
}

.footer-link a {
  color: #ff758c;
  text-decoration: none;
  font-weight: 500;
}

.footer-link a:hover {
  text-decoration: underline;
}

/* Animations */
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 480px) {
  .login-container {
    padding: 15px;
    align-items: flex-start; /* Align to top on mobile for better keyboard handling */
    padding-top: 40px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .nav-card {
    padding: 12px 15px;
  }
  
  .brand-text {
    font-size: 0.9rem;
  }
}
</style>