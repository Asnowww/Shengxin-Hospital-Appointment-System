import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'
import Profile from '@/views/Profile.vue'
import HomePage from '@/views/HomePage.vue'
import Department from '@/views/Department.vue'
import Password from '@/views/Password.vue'
import DoctorProfile from '@/views/DoctorProfile.vue'
import MySchedule from '@/views/MySchedule.vue'
import AdminProfile from '@/views/AdminProfile.vue'
import AdminSchedule from '@/views/AdminSchedule.vue'
import LeaveManagement from '@/views/LeaveManagement.vue'
import DepartmentSchedule from '@/views/DepartmentSchedule.vue'
import MyWaitlist from '@/views/MyWaitlist.vue'
import FullyBookedSchedules from '@/views/FullyBookedSchedules.vue'


const routes = [
  { path: '/', redirect: '/home' },   // 默认定向到首页
  { path: '/home', name: 'home', component: HomePage }, // 首页
  { path: '/login/:role', name: 'login', component: Login },        // 具体登录页
  { path: '/register', name: 'register', component: Register }, // 注册页
  { path: '/patient/profile', name: 'profile', component: Profile },
  { path: '/department', name: 'department', component: Department }, //科室导览页
  { path: '/departmentSchedule', name: 'departmentSchedule', component: DepartmentSchedule }, //科室排班页（接受参数）
  { path: '/doctorSchedule', name: 'doctorSchedule', component: () => import('@/views/DoctorSchedule.vue') }, //医生排班页（接受参数）
  { path: '/password', name: 'password', component: Password }, // 修改密码页

  //候补相关页面
  { path: '/waitlist/my', name: 'myWaitlist', component: MyWaitlist }, //我的候补
  { path: '/waitlist/fully-booked', name: 'fullyBookedSchedules', component: FullyBookedSchedules }, //可候补排班列表

  //医生端界面
  { path: '/doctor/profile', name: 'docProfile', component: DoctorProfile }, //医生个人信息页
  { path: '/doctor/schedules', name: 'doctorSchedules', component: MySchedule }, //医生排班页
  { path: '/mySchedule', name: 'mySchedule', component: MySchedule }, //医生排班页（兼容旧路径）

  //管理端界面
  { path: '/admin/profile', name: 'adminProfile', component: AdminProfile }, //管理员个人信息页
  { path: '/admin/schedules', name: 'adminSchedules', component: AdminSchedule }, //管理员排班管理页
  { path: '/admin/leaves', name: 'adminLeaves', component: LeaveManagement }, //请假审批页
  // { path: '/audit', name: 'audit', component: () => import('@/views/Audit.vue') }, //用户审核页

  { path: '/:pathMatch(.*)*', redirect: '/home' } // 其他未定义路由定向到首页
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
