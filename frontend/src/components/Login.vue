<template>
  <Navigation ref="navRef" />

  <div class="page-container">
    <form @submit.prevent="handleLogin" class="form-box">
      <h2>{{ roleTitle }}登录</h2>

      <div class="form-group">
        <input 
          v-model="account" 
          type="text" 
          placeholder="手机号 / 学工号 / 教工号" 
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

      <button type="submit" class="submit-btn">登录</button>

      <!-- 注册链接：医生和管理员不显示 -->
      <p v-if="currentRole !== 'doctor' && currentRole !== 'admin'" class="switch">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>


      <!-- 忘记密码：管理员不显示 -->
      <p v-if="currentRole !== 'admin'" class="switch">
        <router-link to="/password">忘记密码</router-link>
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const route = useRoute()
const router = useRouter()

const account = ref('')
const password = ref('')

const currentRole = computed(() => route.params.role)
const roleTitle = computed(() => {
  const roleMap = { patient: '患者', doctor: '医生', admin: '管理员' }
  return roleMap[currentRole.value] || '用户'
})

// 登录方法
async function handleLogin() {
  if (!account.value || !password.value) {
    alert('请输入账号和密码')
    return
  }

  try {
    // 统一调用后端接口
    const response = await axios.post('/api/auth/login', {
      account: account.value,
      password: password.value,
      // role: currentRole.value   // 🔹附带角色信息
    })

    const res = response.data

    if (res.code === 200) {
      // 登录成功：保存 token 和用户信息
      const token = res.data.token
      localStorage.setItem('token', token)
      localStorage.setItem('role', currentRole.value)
      localStorage.setItem('account', res.data.account)

      alert(`${roleTitle.value}登录成功！`)
      // 根据角色跳转不同页面
      switch (currentRole.value) {
        case 'admin':
          router.push('/admin/dashboard')
          break
        case 'doctor':
          router.push('/doctorProfile')
          break
        default:
          router.push('/home')
      }
    } else {
      alert(res.msg || res.message || '登录失败')
    }
  } catch (err) {
    console.error(err)
    alert(err?.response?.data?.msg || '服务器错误，请稍后再试')
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