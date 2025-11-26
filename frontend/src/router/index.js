import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/components/Login.vue'
import Register from '@/components/Register.vue'
import Profile from '@/views/Profile.vue'
import HomePage from '@/views/HomePage.vue'
import Department from '@/views/Department.vue'
import Password from '@/views/Password.vue'
import DoctorProfile from '@/views/DoctorProfile.vue'
import DoctorSchedule from '@/views/DoctorSchedule.vue'
import AdminProfile from '@/views/AdminProfile.vue'
import AdminSchedule from '@/views/AdminSchedule.vue'
import LeaveManagement from '@/views/LeaveManagement.vue'
import DepartmentSchedule from '@/views/ScheduleByDepartment.vue'
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
  { path: '/doctorSchedule', name: 'doctorSchedule', component: () => import('@/views/ScheduleByDoctor.vue') }, //医生排班页（接受参数）
  { path: '/password', name: 'password', component: Password }, // 修改密码页

  //候补相关页面
  { path: '/waitlist/my', name: 'myWaitlist', component: MyWaitlist }, //我的候补
  { path: '/waitlist/fully-booked', name: 'fullyBookedSchedules', component: FullyBookedSchedules }, //可候补排班列表

  //医生端界面
  { path: '/doctor/profile', name: 'docProfile', component: DoctorProfile }, //医生个人信息页
  { path: '/doctor/schedules', name: 'doctorSchedules', component: DoctorSchedule }, //医生排班页
  { path: '/doctor/leave/apply', name: 'doctorLeaveApply', component: () => import('@/views/DoctorLeaveApplication.vue') }, // 医生请假申请
  { path: '/doctor/patient-management', name: 'doctorPatientManagement', component: () => import('@/views/DoctorPatientManagement.vue') }, // 医生患者管理

  //管理端界面
  { path: '/admin/profile', name: 'adminProfile', component: AdminProfile }, //管理员个人信息页
  { path: '/admin/schedules', name: 'adminSchedule', component: AdminSchedule }, //管理员排班管理页
  { path: '/admin/leaves', name: 'adminLeaves', component: LeaveManagement }, //请假审批页
  { path: '/admin/audit', name: 'audit', component: () => import('@/views/Audit.vue') }, //用户审核页
  { path: '/admin/doctors', name: 'doctorManagement', component: () => import('@/views/DoctorManagement.vue') }, // 医生管理
  { path: '/admin/statistics', name: 'adminStatistics', component: () => import('@/views/AdminStatistics.vue') }, // 数据统计与分析
  { path: '/test', name: 'test', component: () => import('@/views/Statistics.vue') },
  { path: '/admin/fee', name: 'feeManagement', component: () => import('@/views/AdminFee.vue') }, //费用管理页
  { path: '/reminder', name: 'reminder', component: () => import('@/components/Reminder.vue') }, // 提醒页
  { path: '/:pathMatch(.*)*', redirect: '/home' } // 其他未定义路由定向到首页
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
