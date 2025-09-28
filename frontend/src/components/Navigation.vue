<template>
  <div class="navigation">
    <div class="logo-title" @click="goToFirstPage">
      医院预约系统
    </div>

    <div class="items">
      <router-link to="/appointment" class="nav-item" :class="{ active: isActive('/appointment') }">
        预约挂号
      </router-link>
      <router-link to="/profile" class="nav-item" :class="{ active: isActive('/profile') }">
        个人中心
      </router-link>

      <!-- 登录/注册下拉菜单 -->
      <div class="dropdown" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
        <button class="button login-button">登录 / 注册</button>
        <div v-if="showDropdown" class="dropdown-menu">
          <div class="dropdown-item" @click="goToRole('patient')">我是患者</div>
          <div class="dropdown-item" @click="goToRole('doctor')">我是医生</div>
          <div class="dropdown-item" @click="goToRole('admin')">我是管理员</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const showDropdown = ref(false)

function goToFirstPage() {
  router.push('/first_page')
}

function isActive(path) {
  return route.path === path
}

function goToRole(role) {
  router.push(`/login/${role}`)
}
</script>

<style scoped>
.navigation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  min-height: 80px;
  z-index: 1000;
  background-color: #EFE7DC;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  box-sizing: border-box;
}

.logo-title {
  font-size: 24px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
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
  white-space: nowrap;
}

.nav-item.active {
  background-color: #F1D06F;
}

.nav-item:hover {
  background-color: rgba(241, 208, 111, 0.5);
}

.button {
  all: unset;
  padding: 8px 14px;
  background-color: #007bff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s ease;
}

.button:hover {
  background-color: #0056b3;
}

/* 下拉菜单 */
.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  margin-top: 4px;
  min-width: 140px;
}

.dropdown-item {
  padding: 10px 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}
</style>
