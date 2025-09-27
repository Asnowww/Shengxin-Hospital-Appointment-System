<template>
  <div class="page-container">
    <div class="content-wrapper">
      <h2>{{ roleTitle }}登录</h2>
      <form @submit.prevent="handleLogin">
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
      </form>
      <p class="switch">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

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
  min-height: 100vh;
  padding-top: 82px; /* 导航栏高度 */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.content-wrapper {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 600;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

input {
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #ddd;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.submit-btn {
  margin-top: 10px;
  padding: 12px;
  border: none;
  border-radius: 6px;
  background-color: #409eff;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background-color: #337ecc;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

.switch {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.switch a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.switch a:hover {
  text-decoration: underline;
}
</style>