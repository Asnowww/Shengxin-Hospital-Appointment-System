<template>
  <div id="app">
    <Header />
    <main>
      <div class="auth-wrapper">
        <div class="auth-container">
          <div class="main-tabs">
            <button :class="{ active: mainTab === 'login' }" @click="mainTab = 'login'">登录</button>
            <button :class="{ active: mainTab === 'register' }" @click="mainTab = 'register'">注册</button>
          </div>

          <LoginForm v-if="mainTab === 'login'" @switch-tab="mainTab = $event" />
          <RegisterForm v-else @switch-tab="mainTab = $event" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginForm from '@/components/LoginForm.vue'
import RegisterForm from '@/components/RegisterForm.vue'

const mainTab = ref('login')
</script>
<style scoped>
main {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 120px 20px 60px;
  min-height: calc(100vh - 80px);
  background-color: transparent;
}

.auth-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
}

.auth-container {
  background-color: rgb(250, 250, 250, 0.8);
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  width: 100%;
}

.main-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.main-tabs button {
  flex: 1;
  padding: 12px 0;
  border: none;
  background-color: transparent;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  color: #888;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.main-tabs button.active {
  color: #000;
  border-bottom-color: #000;
}
</style>
