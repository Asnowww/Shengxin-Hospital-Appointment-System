<template>
  <Navigation ref="navRef" />

  <div class="page-container">
    <form @submit.prevent="handleLogin" class="form-box">
      <h2>{{ roleTitle }}登录</h2>

      <div class="form-group">
        <input 
          v-model="account" 
          type="text" 
          placeholder="手机号 / 用户姓名 / 邮箱" 
          required 
        />
      </div>

      <div class="form-group">
        <input 
          v-model="password" 
          type="password" 
          placeholder="密码" 
          required 
        />
      </div>

      <!-- 图形验证码 -->
      <div class="form-group captcha-group">
        <input 
          v-model="captchaCode" 
          type="text" 
          placeholder="请输入验证码" 
          required 
        />
        <img 
          :src="captchaImage" 
          alt="验证码" 
          class="captcha-img" 
          @click="loadCaptcha"
          title="点击刷新验证码"
        />
      </div>

      <button type="submit" class="submit-btn">登录</button>

      <p v-if="currentRole !== 'doctor' && currentRole !== 'admin'" class="switch">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>

      <p v-if="currentRole !== 'admin'" class="switch">
        <router-link to="/password">忘记密码</router-link>
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const route = useRoute()
const router = useRouter()

const account = ref('')
const password = ref('')

const captchaCode = ref('')         // 用户输入验证码
const captchaImage = ref('')        // 验证码图片
const captchaId = ref('')           // 后端返回的唯一标识

const currentRole = computed(() => route.params.role)
const roleTitle = computed(() => {
  const roleMap = { patient: '患者', doctor: '医生', admin: '管理员' }
  return roleMap[currentRole.value] || '用户'
})

// 页面加载时获取验证码
onMounted(() => {
  loadCaptcha()
})

// 获取图形验证码
async function loadCaptcha() {
  try {
    const res = await axios.get('/api/captcha/graph', { responseType: 'blob' })
    // 从响应头获取 captchaId
    captchaId.value = res.headers['captcha-id']

    // 转换图片为可显示 URL
    captchaImage.value = URL.createObjectURL(res.data)
  } catch (err) {
    console.error('获取验证码失败', err)
    alert('验证码加载失败，请刷新页面')
  }
}

// 登录方法
async function handleLogin() {
  if (!account.value || !password.value || !captchaCode.value) {
    alert('请输入账号、密码和验证码')
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
      // 登录成功：保存 token 和用户信息
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('role', res.data.roleType)
      localStorage.setItem('userId', res.data.account)

        // 若是医生，则额外保存 doctorId
      if (res.data.roleType === 'doctor') {
        localStorage.setItem('doctorId', res.data.doctorId)
      }

        if (res.data.roleType === 'patient') {
        localStorage.setItem('patientId', res.data.patientId)
      }

      alert('登录成功！')

      // 根据角色跳转
      switch (res.data.roleType) {
        case 'admin':
          router.push('/admin/dashboard')
          break
        case 'doctor':
          router.push('/doctor/profile')
          break
        default:
          router.push('/home')
      }
    } else {
      alert(res.msg || res.message || '登录失败')
      loadCaptcha() // 验证码错误时刷新
      captchaCode.value = ''
    }
  } catch (err) {
    console.error(err)
    alert(err?.response?.data?.msg || '服务器错误，请稍后再试')
    loadCaptcha()
    captchaCode.value = ''
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;   /* 水平居中 */
  align-items: center;       /* 垂直居中 */
  min-height: calc(100vh - var(--nav-height, 80px));
  padding: 20px;
  box-sizing: border-box;
}

.form-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 16px;
}
.captcha-group {
  display: flex;
  align-items: center;   /* 垂直居中对齐 */
  gap: 10px;             /* 输入框和图片间距 */
}

.captcha-input {
  flex: 1;               /* 输入框占满剩余空间 */
  height: 40px;          /* 高度与图片保持一致 */
  padding: 0 10px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.captcha-img {
  width: 120px;          /* 图片宽度，可根据后端生成大小调整 */
  height: 40px;          /* 图片高度 */
  cursor: pointer;
  border: 1px solid #ddd;
  border-radius: 4px;
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  font-size: 16px;
  margin-top: 8px;
  transition: background-color 0.3s ease;
}

.submit-btn:hover {
  background-color: #337ecc;
}

.switch {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.switch a {
  color: #409eff;
  text-decoration: none;
}

.switch a:hover {
  text-decoration: underline;
}
</style>