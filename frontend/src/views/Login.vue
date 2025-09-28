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

      <p class="switch">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import Navigation from '@/components/Navigation.vue'

const route = useRoute()
const account = ref('')
const password = ref('')

const roleTitle = computed(() => {
  const roleMap = { patient: '患者', doctor: '医生', admin: '管理员' }
  return roleMap[route.params.role] || '用户'
})

function handleLogin() {
  console.log('登录身份:', route.params.role, '账号:', account.value, '密码:', password.value)
  alert(`${roleTitle.value}登录成功！`)
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
