<template>
  <div class="navigation">
    <div class="logo-title" @click="goToHomePage">
      医院预约系统
    </div>

    <div class="items">
      <router-link 
      v-if="isLoggedIn && currentRole === 'patient'"
      to="/department" class="nav-item" :class="{ active: isActive('/department') }">
        预约挂号
      </router-link>

      <router-link 
      v-if="isLoggedIn && currentRole === 'patient'"
      to="/consultation" class="nav-item" :class="{ active: isActive('/consultation') }">
        智能问诊
      </router-link>

      <router-link
    v-if="isLoggedIn && currentRole === 'patient'|| isLoggedIn && currentRole === 'doctor'"
    :to="ChatPath"
    class="nav-item"
    :class="{ active: isActive(ChatPath) }">
    在线问诊
  </router-link>

      <!-- 医生端：仅医生登录后显示 -->
      <router-link
        v-if="isLoggedIn && currentRole === 'doctor'"
        to="/doctor/schedules"
        class="nav-item"
        :class="{ active: isActive('/doctor/schedules') }">
        我的排班
      </router-link>

      <router-link
        v-if="isLoggedIn && currentRole === 'doctor'"
        to="/doctor/leave/apply"
        class="nav-item"
        :class="{ active: isActive('/doctor/leave/apply') }">
        请假申请
      </router-link>

      <router-link
          v-if="isLoggedIn && currentRole === 'doctor'"
          to="/doctor/patient-management"
          class="nav-item"
          :class="{ active: isActive('/doctor/patient-management') }">
        管理病患
      </router-link>


      <!--管理端：审批-->
        <router-link
        v-if="isLoggedIn && currentRole === 'admin'"
        to="/admin/audit"
        class="nav-item"
        :class="{ active: isActive('/admin/audit') }">
        审核中心
      </router-link>

       <router-link
        v-if="isLoggedIn && currentRole === 'admin'"
        to="/admin/fee"
        class="nav-item"
        :class="{ active: isActive('/admin/fee') }">
        号别管理
      </router-link>
      <!-- 管理端：医生管理 -->
      <router-link
        v-if="isLoggedIn && currentRole === 'admin'"
        to="/admin/doctors"
        class="nav-item"
        :class="{ active: isActive('/admin/doctors') }">
        医生管理
      </router-link>

      <!-- 登录状态判断：已登录显示"个人中心"，未登录显示"登录/注册" -->
      <template v-if="isLoggedIn">
        <router-link
          v-if="currentRole === 'admin'"
          to="/admin/statistics"
          class="nav-item"
          :class="{ active: isActive('/admin/statistics') }">
          数据分析
        </router-link>
        <router-link 
          :to="roleRoutes[currentRole]" 
          class="nav-item" 
          :class="{ active: isActive(roleRoutes[currentRole]) }"
        >
          个人中心
        </router-link>
        <button class="button logout-button" @click="handleLogout">
          退出登录
        </button>
      </template>

      <template v-else>
        <!-- 登录/注册下拉菜单 -->
        <div class="dropdown">
          <button class="button login-button">登录 / 注册</button>
          <div class="dropdown-menu">
            <div class="dropdown-item" @click="goToRole('patient')">我是患者</div>
            <div class="dropdown-item" @click="goToRole('doctor')">我是医生</div>
            <div class="dropdown-item" @click="goToRole('admin')">我是管理员</div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

function goToHomePage() {
  router.push('/home')
}

function goToRole(role) {
  router.push(`/login/${role}`)
}

// 从 localStorage 获取用户角色和 token
const currentRole = ref(localStorage.getItem('role'))
const token = ref(localStorage.getItem('token'))

// 判断是否已登录
const isLoggedIn = computed(() => !!token.value && !!currentRole.value)

// 定义角色对应的个人中心路由表
const roleRoutes = {
  patient: '/patient/profile',
  doctor: '/doctor/profile',
  admin: '/admin/profile'
}

const ChatPath = computed(() => {
  switch (currentRole.value) {
    case 'doctor':
      return '/doctor/chat'
    case 'patient':
      return '/patient/chat'
    default:
      return '/home'
  }
})

// 退出登录
function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    
    // 更新响应式数据
    token.value = null
    currentRole.value = null
    
    // 跳转到首页
    router.push('/home')
    
    alert('已退出登录')
  }
}

// 判断是否激活
function isActive(path) {
  return route.path === path
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
  transition: background-color 0.3s ease;
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

.logout-button {
  background-color: #dc3545;
}

.logout-button:hover {
  background-color: #c82333;
}

/* 下拉菜单 */
.dropdown {
  position: relative;
}

/* 下拉菜单：默认隐藏 */
.dropdown-menu {
  opacity: 0;
  visibility: hidden;
  transform: translateY(8px);
  transition: all 0.25s ease;
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  margin-top: 4px;
  min-width: 140px;
  z-index: 1000;
}

/* 悬停 dropdown 时，菜单显示并淡入 */
.dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
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

/* 响应式设计 */
@media (max-width: 768px) {
  .navigation {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
    min-height: auto;
  }

  .items {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .nav-item,
  .button {
    font-size: 14px;
    padding: 6px 12px;
  }

  .logo-title {
    font-size: 20px;
  }
}
</style>
