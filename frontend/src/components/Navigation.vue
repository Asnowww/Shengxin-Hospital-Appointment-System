<template>
  <div class="navigation">
    <div class="logo-title" @click="goToFirstPage">
      医院预约系统
    </div>

    <div class="items">
      <router-link to="/appointment" class="nav-item" :class="{ active: isActive('/appointment') }">预约挂号</router-link>
      <router-link to="/profile" class="nav-item" :class="{ active: isActive('/profile') }">个人中心</router-link>
      <button class="button login-button" @click="goToLogin">登录 / 注册</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

function goToFirstPage() { router.push('/first_page') }
function goToLogin() { router.push('/login') }
function isActive(path) { return route.path === path }
</script>

<style scoped>
.navigation {
 position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: var(--nav-h);
  z-index: 9999;
  background-color: #EFE7DC;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  box-sizing: border-box;
}

.logo-title {
  font-size: 24px;
  font-weight: 600;
  cursor: pointer;
}

.items {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-item {
  text-decoration: none;
  padding: 8px 14px;
  border-radius: 4px;
  color: #000;
}

.nav-item.active {
  background-color: #F1D06F;
}

.button {
  all: unset;
  padding: 8px 14px;
  background-color: #007bff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}
</style>
