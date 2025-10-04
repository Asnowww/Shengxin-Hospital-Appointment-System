<template>
  <div class="login-select">
    <h2 class="title">请选择登录身份</h2>
    <div class="options">
      <button class="option" @click="goToLogin('patient')">我是患者</button>
      <button class="option" @click="goToLogin('doctor')">我是医生</button>
      <button class="option" @click="goToLogin('admin')">我是管理员</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goToLogin(role) {
  router.push(`/login/${role}`)
}
</script>

<style scoped>
.login-select {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  background-color: #007bff;
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.option:hover {
  background-color: #0056b3;
}
</style>
