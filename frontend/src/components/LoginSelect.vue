<template>
  <div>
    <h2>请选择您的身份</h2>
    <div class="buttons">
      <button @click="goToLogin('patient')">我是患者</button>
      <button @click="goToLogin('doctor')">我是医生</button>
      <button @click="goToLogin('admin')">我是管理员</button>
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
.buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

button {
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  background-color: #409eff;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover {
  background-color: #337ecc;
}
</style>
