import { createRouter, createWebHistory } from 'vue-router'
import LoginSelect from '@/components/LoginSelect.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
  { path: '/login', name: 'login-select', component: LoginSelect }, // 登录入口页
  { path: '/login/:role', name: 'login', component: Login },        // 具体登录页
  { path: '/register', name: 'register', component: Register },     // 注册页
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
