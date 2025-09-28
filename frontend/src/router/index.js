import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

const routes = [
  { path: '/login/:role', name: 'login', component: Login },        // 具体登录页
  { path: '/register', name: 'register', component: Register },     // 注册页
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
