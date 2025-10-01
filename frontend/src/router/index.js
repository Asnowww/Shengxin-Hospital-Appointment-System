import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Profile from '@/views/Profile.vue'
import HomePage from '@/views/HomePage.vue'
import Department from '@/views/Department.vue'
import DepartmentDetail from '@/views/DepartmentDetail.vue'


const routes = [
  { path: '/', redirect: '/home' },   // 默认定向到首页
  { path: '/home', name: 'home', component: HomePage }, // 首页
  { path: '/login/:role', name: 'login', component: Login },        // 具体登录页
  { path: '/register', name: 'register', component: Register },
  { path: '/profile', name: 'profile', component: Profile },   // 注册页
  { path: '/department', name: 'department', component: Department }, //科室导览页
  { path: '/departmentDetail', name: 'departmentDetail', component: DepartmentDetail }, //科室详情页（接受参数）
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
