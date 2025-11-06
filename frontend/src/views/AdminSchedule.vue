<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏导航 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="avatar">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"></path>
            </svg>
          </div>
          <h3>管理员中心</h3>
        </div>

        <nav class="sidebar-nav">
          <router-link to="/admin/profile" class="nav-item" :class="{ active: $route.path === '/admin/profile' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人信息</span>
          </router-link>

          <router-link to="/admin/schedules" class="nav-item" :class="{ active: $route.path === '/admin/schedules' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>排班管理</span>
          </router-link>

          <router-link to="/admin/leaves" class="nav-item" :class="{ active: $route.path === '/admin/leaves' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>请假审批</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <div class="schedule-management">
          <!-- 头部操作区 -->
          <div class="header-section">
            <div class="title-group">
              <h2>排班管理</h2>
              <p class="subtitle">管理所有医生的排班信息</p>
            </div>
            <button @click="openCreateDialog" class="create-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              创建排班
            </button>
          </div>

          <!-- 筛选区 -->
          <div class="filter-section">
            <div class="filter-group">
              <label class="filter-label">日期范围</label>
              <div class="date-range">
                <input
                  v-model="filters.startDate"
                  type="date"
                  class="filter-input"
                  @change="fetchSchedules" />
                <span class="date-separator">至</span>
                <input
                  v-model="filters.endDate"
                  type="date"
                  class="filter-input"
                  @change="fetchSchedules" />
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">科室</label>
              <select v-model="filters.department" @change="fetchSchedules" class="filter-select">
                <option value="">全部科室</option>
                <option value="CARDIO">心内科</option>
                <option value="GASTRO">消化内科</option>
                <option value="RESPIR">呼吸内科</option>
                <option value="ORTHO">骨科</option>
                <option value="NEURO">神经外科</option>
                <option value="GYNEC">妇科</option>
                <option value="PEDIM">小儿内科</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">医生</label>
              <select v-model="filters.doctorId" @change="fetchSchedules" class="filter-select">
                <option value="">全部医生</option>
                <option v-for="doctor in doctors" :key="doctor.doctorId" :value="doctor.doctorId">
                  {{ doctor.doctorName }} - {{ doctor.title }}
                </option>
              </select>
            </div>

            <button @click="resetFilters" class="reset-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10"></polyline>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
              </svg>
              重置
            </button>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>加载排班数据中...</p>
          </div>

          <!-- 排班列表 -->
          <div v-else class="schedule-list">
            <div v-if="schedules.length === 0" class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              <p>暂无排班数据</p>
            </div>

            <div v-else class="schedule-table">
              <div class="table-header">
                <div class="th-date">日期</div>
                <div class="th-doctor">医生</div>
                <div class="th-department">科室</div>
                <div class="th-time">时间段</div>
                <div class="th-patients">预约情况</div>
                <div class="th-status">状态</div>
                <div class="th-actions">操作</div>
              </div>

              <div
                v-for="schedule in schedules"
                :key="schedule.scheduleId"
                class="table-row"
                :class="{ conflict: schedule.hasConflict }">
                <div class="td-date">
                  <div class="date-main">{{ formatDate(schedule.date) }}</div>
                  <div class="date-sub">{{ getWeekday(schedule.date) }}</div>
                </div>
                <div class="td-doctor">
                  <div class="doctor-name">{{ schedule.doctorName }}</div>
                  <div class="doctor-title">{{ schedule.doctorTitle }}</div>
                </div>
                <div class="td-department">
                  <span class="department-badge">{{ schedule.deptName }}</span>
                </div>
                <div class="td-time">
                  <div class="time-info">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    {{ schedule.startTime }} - {{ schedule.endTime }}
                  </div>
                </div>
                <div class="td-patients">
                  <div class="patient-count">
                    <span class="count-current">{{ schedule.appointedCount }}</span>
                    <span class="count-separator">/</span>
                    <span class="count-max">{{ schedule.maxPatients }}</span>
                  </div>
                  <div class="patient-bar">
                    <div
                      class="patient-bar-fill"
                      :style="{ width: getPatientPercentage(schedule) + '%' }"
                      :class="{ full: schedule.appointedCount >= schedule.maxPatients }">
                    </div>
                  </div>
                </div>
                <div class="td-status">
                  <span :class="['status-badge', schedule.status]">
                    {{ getStatusText(schedule.status) }}
                  </span>
                  <div v-if="schedule.hasConflict" class="conflict-tag">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                      <line x1="12" y1="9" x2="12" y2="13"></line>
                      <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    冲突
                  </div>
                </div>
                <div class="td-actions">
                  <button @click="viewSchedule(schedule)" class="action-btn view-btn" title="查看详情">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                  <button @click="editSchedule(schedule)" class="action-btn edit-btn" title="编辑">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button @click="confirmDelete(schedule)" class="action-btn delete-btn" title="删除">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>

  <!-- 创建/编辑排班弹窗 -->
<transition name="modal">
  <div v-if="showScheduleModal" class="modal-overlay" @click.self="closeScheduleModal">
    <div class="modal-container large">
      <button @click="closeScheduleModal" class="close-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>

      <div class="modal-header">
        <h2>{{ isEditing ? '编辑排班' : '创建排班' }}</h2>
        <p class="subtitle">{{ isEditing ? '修改排班信息' : '为医生创建新的排班' }}</p>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <!-- 批量创建选项 -->
        <div v-if="!isEditing" class="batch-section">
          <label class="checkbox-label">
            <input type="checkbox" v-model="scheduleForm.isBatch" />
            <span>批量创建排班</span>
          </label>
        </div>

        <div class="form-grid">
          <!-- 医生 -->
          <div class="form-group">
            <label class="form-label">
              医生 <span class="required">*</span>
            </label>
            <select
              v-model="scheduleForm.doctorId"
              :disabled="isEditing"
              @change="checkConflict"
              :class="['form-control', { error: errors.doctorId }]"
              required
            >
              <option value="">请选择医生</option>
              <option v-for="doctor in doctors" :key="doctor.doctorId" :value="doctor.doctorId">
                {{ doctor.doctorName }} - {{ doctor.deptName }} - {{ doctor.title }}
              </option>
            </select>
            <span v-if="errors.doctorId" class="error-text">{{ errors.doctorId }}</span>
          </div>

          <!-- 日期 / 日期范围 -->
          <div v-if="!scheduleForm.isBatch" class="form-group">
            <label class="form-label">日期 <span class="required">*</span></label>
            <input
              v-model="scheduleForm.date"
              type="date"
              @change="checkConflict"
              :min="minDate"
              :class="['form-control', { error: errors.date }]"
              required
            />
            <span v-if="errors.date" class="error-text">{{ errors.date }}</span>
          </div>

          <div v-else class="form-group full-width">
            <label class="form-label">日期范围 <span class="required">*</span></label>
            <div class="date-range-input">
              <input v-model="scheduleForm.startDate" type="date" :min="minDate" class="form-control" required />
              <span class="date-separator">至</span>
              <input v-model="scheduleForm.endDate" type="date" :min="scheduleForm.startDate || minDate" class="form-control" required />
            </div>
          </div>

          <!-- 时间段选择 -->
          <div class="form-group full-width">
            <label class="form-label">时段 <span class="required">*</span></label>
            <div class="time-period-selector">
              <label class="time-period-option">
                <input type="checkbox" :value="0" v-model="scheduleForm.timeSlots" />
                <span>上午（08:00 - 12:00）</span>
              </label>
              <label class="time-period-option">
                <input type="checkbox" :value="1" v-model="scheduleForm.timeSlots" />
                <span>下午（13:00 - 17:00）</span>
              </label>
              <label class="time-period-option">
                <input type="checkbox" :value="2" v-model="scheduleForm.timeSlots" />
                <span>夜班（18:00 - 22:00）</span>
              </label>
            </div>
          </div>
          
          <!-- 最大接诊人数 -->
          <div class="form-group">
            <label class="form-label">最大接诊人数 <span class="required">*</span></label>
            <input
              v-model.number="scheduleForm.maxPatients"
              type="number"
              min="1"
              max="100"
              :class="['form-control', { error: errors.maxPatients }]"
              required
            />
            <span v-if="errors.maxPatients" class="error-text">{{ errors.maxPatients }}</span>
          </div>

          <!-- 批量：工作日 -->
          <div v-if="scheduleForm.isBatch" class="form-group full-width">
            <label class="form-label">工作日 <span class="required">*</span></label>
            <div class="weekday-selector">
              <label
                v-for="day in weekDays"
                :key="day.value"
                class="weekday-checkbox"
              >
                <input type="checkbox" :value="day.value" v-model="scheduleForm.weekdays" />
                <span class="checkbox-label">{{ day.label }}</span>
              </label>
            </div>
          </div>

          <!-- 备注 -->
          <div class="form-group full-width">
            <label class="form-label">备注</label>
            <textarea
              v-model="scheduleForm.notes"
              class="form-control textarea"
              rows="3"
              placeholder="备注信息（选填）"
            ></textarea>
          </div>
        </div>

        <!-- 冲突警告 -->
        <div v-if="hasConflict" class="warning-box">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <div>
            <strong>排班冲突：</strong>该医生在此时间段已有排班，请调整时间。
          </div>
        </div>

        <!-- 按钮 -->
        <div class="button-group">
          <button type="button" @click="closeScheduleModal" class="cancel-btn">取消</button>
          <button type="submit" class="submit-btn" :disabled="hasConflict || scheduleForm.timeSlots.length === 0">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            {{ isEditing ? '保存修改' : (scheduleForm.isBatch ? '批量创建' : '创建排班') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</transition>


  <!-- 查看详情弹窗 -->
  <transition name="modal">
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-container">
        <button @click="closeDetailModal" class="close-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <div class="modal-header">
          <h2>排班详情</h2>
        </div>

        <div v-if="selectedSchedule" class="modal-body">
          <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="item-label">医生：</span>
                <span class="item-value">{{ selectedSchedule.doctorName }}</span>
              </div>
              <div class="detail-item">
                <span class="item-label">职称：</span>
                <span class="item-value">{{ selectedSchedule.doctorTitle }}</span>
              </div>
              <div class="detail-item">
                <span class="item-label">科室：</span>
                <span class="item-value">{{ selectedSchedule.deptName }}</span>
              </div>
              <div class="detail-item">
                <span class="item-label">日期：</span>
                <span class="item-value">{{ formatDate(selectedSchedule.date) }}</span>
              </div>
              <div class="detail-item">
                <span class="item-label">时间：</span>
                <span class="item-value">{{ selectedSchedule.startTime }} - {{ selectedSchedule.endTime }}</span>
              </div>
              <div class="detail-item">
                <span class="item-label">状态：</span>
                <span class="item-value">
                  <span :class="['status-badge', selectedSchedule.status]">
                    {{ getStatusText(selectedSchedule.status) }}
                  </span>
                </span>
              </div>
              <div class="detail-item full-width">
                <span class="item-label">预约情况：</span>
                <span class="item-value">{{ selectedSchedule.appointedCount }} / {{ selectedSchedule.maxPatients }} 人</span>
              </div>
              <div v-if="selectedSchedule.notes" class="detail-item full-width">
                <span class="item-label">备注：</span>
                <span class="item-value">{{ selectedSchedule.notes }}</span>
              </div>
            </div>
          </div>

          <div v-if="selectedSchedule.appointments && selectedSchedule.appointments.length > 0" class="detail-section">
            <h3>预约患者列表</h3>
            <div class="appointment-list">
              <div
                v-for="appointment in selectedSchedule.appointments"
                :key="appointment.id"
                class="appointment-item">
                <div class="appointment-info">
                  <div class="patient-name">{{ appointment.patientName }}</div>
                  <div class="patient-meta">{{ appointment.gender }} / {{ appointment.age }}岁</div>
                </div>
                <div class="appointment-time">{{ appointment.appointmentTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const showScheduleModal = ref(false)
const showDetailModal = ref(false)
const isEditing = ref(false)
const hasConflict = ref(false)

const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

// 筛选条件
const filters = reactive({
  startDate: '',
  endDate: '',
  department: '',
  doctorId: ''
})

// 医生列表
const doctors = ref([])

// 排班列表
const schedules = ref([])

// 选中的排班
const selectedSchedule = ref(null)

// 排班表单
const scheduleForm = reactive({
  id: null,
  doctorId: '',
  deptId: '',
  date: '',
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: '',
  maxPatients: 30,
  notes: '',
  isBatch: false,
  weekdays: []
})

// 表单错误
const errors = reactive({
  doctorId: '',
  date: '',
  startTime: '',
  endTime: '',
  maxPatients: ''
})

// 最小日期（今天）
const minDate = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// 获取排班数据
async function fetchSchedules() {
  loading.value = true
  try {
    const params = {
      startDate: filters.startDate,
      endDate: filters.endDate,
      department: filters.department,
      doctorId: filters.doctorId
    }
    const { data } = await axios.get('/api/admin/schedules', { params })

    模拟API请求
    await new Promise(resolve => setTimeout(resolve, 600))

    // const mockData = [
    //   {
    //     id: 1,
    //     doctorId: 1,
    //     doctorName: '张明',
    //     doctorTitle: '主任医师',
    //     department: 'CARDIO',
    //     date: '2025-10-23',
    //     startTime: '08:00',
    //     endTime: '12:00',
    //     maxPatients: 30,
    //     appointedCount: 15,
    //     status: 'active',
    //     hasConflict: false,
    //     notes: '',
    //     appointments: [
    //       { id: 1, patientName: '李四', gender: '男', age: 45, appointmentTime: '08:30' },
    //       { id: 2, patientName: '王五', gender: '女', age: 32, appointmentTime: '09:00' }
    //     ]
    //   },
    //   {
    //     id: 2,
    //     doctorId: 2,
    //     doctorName: '李红',
    //     doctorTitle: '副主任医师',
    //     department: 'GASTRO',
    //     date: '2025-10-23',
    //     startTime: '14:00',
    //     endTime: '17:00',
    //     maxPatients: 25,
    //     appointedCount: 25,
    //     status: 'active',
    //     hasConflict: false,
    //     notes: '',
    //     appointments: []
    //   },
    //   {
    //     id: 3,
    //     doctorId: 1,
    //     doctorName: '张明',
    //     doctorTitle: '主任医师',
    //     department: 'CARDIO',
    //     date: '2025-10-24',
    //     startTime: '08:00',
    //     endTime: '12:00',
    //     maxPatients: 30,
    //     appointedCount: 8,
    //     status: 'active',
    //     hasConflict: true,
    //     notes: '与其他排班时间重叠',
    //     appointments: []
    //   },
    //   {
    //     id: 4,
    //     doctorId: 3,
    //     doctorName: '王强',
    //     doctorTitle: '主治医师',
    //     department: 'ORTHO',
    //     date: '2025-10-25',
    //     startTime: '08:00',
    //     endTime: '17:00',
    //     maxPatients: 20,
    //     appointedCount: 0,
    //     status: 'pending',
    //     hasConflict: false,
    //     notes: '新增排班',
    //     appointments: []
    //   }
    // ]

    schedules.value = data.data 
  } catch (err) {
    console.error('获取排班数据失败', err)
    alert('获取排班数据失败')
  } finally {
    loading.value = false
  }
}

// 获取医生列表
async function fetchDoctors() {
  try {
   
    const { data } = await axios.get('/api/doctor/list')
    doctors.value = data.data
  } catch (err) {
    console.error('获取医生列表失败', err)
  }
}

// 重置筛选
function resetFilters() {
  filters.startDate = ''
  filters.endDate = ''
  filters.department = ''
  filters.doctorId = ''
  fetchSchedules()
}

// 打开创建对话框
function openCreateDialog() {
  isEditing.value = false
  resetForm()
  showScheduleModal.value = true
}

// 编辑排班
function editSchedule(schedule) {
  isEditing.value = true
  scheduleForm.id = schedule.scheduleId
  scheduleForm.doctorId = schedule.doctorId
  scheduleForm.date = schedule.date
  scheduleForm.startTime = schedule.startTime
  scheduleForm.endTime = schedule.endTime
  scheduleForm.maxPatients = schedule.maxPatients
  scheduleForm.notes = schedule.notes
  scheduleForm.isBatch = false
  scheduleForm.weekdays = []
  showScheduleModal.value = true
}

// 查看排班详情
function viewSchedule(schedule) {
  selectedSchedule.value = schedule
  showDetailModal.value = true
}

// 确认删除
function confirmDelete(schedule) {
  if (schedule.appointedCount > 0) {
    if (!confirm(`该排班已有 ${schedule.appointedCount} 位患者预约，删除后预约将被取消。确定要删除吗？`)) {
      return
    }
  } else {
    if (!confirm('确定要删除这个排班吗？')) {
      return
    }
  }
  deleteSchedule(schedule.scheduleId)
}

// 删除排班
async function deleteSchedule(id) {
  try {
    // 实际使用时替换为：
    // await axios.delete(`/api/admin/schedules/${id}`)

    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 500))

    alert('删除成功')
    fetchSchedules()
  } catch (err) {
    console.error('删除失败', err)
    alert('删除失败')
  }
}

// 关闭弹窗
function closeScheduleModal() {
  showScheduleModal.value = false
  resetForm()
}

function closeDetailModal() {
  showDetailModal.value = false
  selectedSchedule.value = null
}

// 重置表单
function resetForm() {
  scheduleForm.id = null
  scheduleForm.doctorId = ''
  scheduleForm.date = ''
  scheduleForm.startDate = ''
  scheduleForm.endDate = ''
  scheduleForm.startTime = ''
  scheduleForm.endTime = ''
  scheduleForm.maxPatients = 30
  scheduleForm.notes = ''
  scheduleForm.isBatch = false
  scheduleForm.timeSlots =[],
  scheduleForm.weekdays = []
  clearErrors()
  hasConflict.value = false
}

// 清除错误
function clearErrors() {
  errors.doctorId = ''
  errors.date = ''
  errors.startTime = ''
  errors.endTime = ''
  errors.maxPatients = ''
}

// 验证表单
function validateForm() {
  clearErrors()
  let isValid = true

  if (!scheduleForm.doctorId) {
    console.log('doctorId missing')
    errors.doctorId = '请选择医生'
    isValid = false
  }

  if (!scheduleForm.isBatch && !scheduleForm.date) {
    errors.date = '请选择日期'
    isValid = false
  }

  // if (!scheduleForm.startTime) {
  //   errors.startTime = '请选择开始时间'
  //   isValid = false
  // }

  // if (!scheduleForm.endTime) {
  //   errors.endTime = '请选择结束时间'
  //   isValid = false
  // }

  // if (scheduleForm.startTime && scheduleForm.endTime && scheduleForm.startTime >= scheduleForm.endTime) {
  //   errors.endTime = '结束时间必须晚于开始时间'
  //   isValid = false
  // }

  if (!scheduleForm.maxPatients || scheduleForm.maxPatients < 1) {
    errors.maxPatients = '请输入有效的接诊人数'
    isValid = false
  }

  if (scheduleForm.isBatch && scheduleForm.weekdays.length === 0) {
    alert('请至少选择一个工作日')
    isValid = false
  }

  return isValid
}

// 检查冲突
async function checkConflict() {
  if (!scheduleForm.doctorId || !scheduleForm.date || !scheduleForm.startTime || !scheduleForm.endTime) {
    hasConflict.value = false
    return
  }

  try {
    // 实际使用时替换为：
    // const { data } = await axios.post('/api/admin/schedules/check-conflict', {
    //   doctorId: scheduleForm.doctorId,
    //   date: scheduleForm.date,
    //   startTime: scheduleForm.startTime,
    //   endTime: scheduleForm.endTime,
    //   excludeId: scheduleForm.id
    // })
    // hasConflict.value = data.hasConflict

    // 模拟检查
    await new Promise(resolve => setTimeout(resolve, 200))
    hasConflict.value = false
  } catch (err) {
    console.error('检查冲突失败', err)
  }
}

// 提交表单
async function handleSubmit() {
  if (!validateForm()) {
    console.log('表单验证失败')
    return
  }

  if (hasConflict.value) {
    alert('存在排班冲突，请调整时间')
    return
  }

  try {
    if (isEditing.value) {
      // 实际使用时替换为：
      // await axios.put(`/api/admin/schedules/${scheduleForm.id}`, scheduleForm)

      // 模拟API请求
      await new Promise(resolve => setTimeout(resolve, 800))
      alert('修改成功')
    } else {
      
      await axios.post('/api/admin/schedules/create', scheduleForm)


      if (scheduleForm.isBatch) {
        alert('批量创建成功')
      } else {
        alert('创建成功')
      }
    }

    closeScheduleModal()
    fetchSchedules()
  } catch (err) {
    console.error('操作失败', err)
    alert('操作失败')
  }
}

// 工具函数
function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function getWeekday(dateStr) {
  const date = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[date.getDay()]
}

// function getDepartmentName(code) {
//   const map = {
//     'CARDIO': '心内科',
//     'GASTRO': '消化内科',
//     'RESPIR': '呼吸内科',
//     'ORTHO': '骨科',
//     'NEURO': '神经外科',
//     'GYNEC': '妇科',
//     'PEDIM': '小儿内科'
//   }
//   return map[code] || code
// }

function getStatusText(status) {
  const map = {
    'active': '正常',
    'pending': '待确认',
    'cancelled': '已取消'
  }
  return map[status] || '未知'
}

function getPatientPercentage(schedule) {
  return Math.min((schedule.appointedCount / schedule.maxPatients) * 100, 100)
}

// 导航栏高度管理
function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30
  }
}

function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)

  // 初始化日期范围为未来7天
  const today = new Date()
  filters.startDate = today.toISOString().split('T')[0]
  const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
  filters.endDate = nextWeek.toISOString().split('T')[0]

  fetchDoctors()
  fetchSchedules()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f7fafc;
}

.profile-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  height: fit-content;
  position: sticky;
  top: calc(var(--nav-height, 110px) + 2rem);
  flex-shrink: 0;
}

.sidebar-header {
  text-align: center;
  padding-bottom: 2rem;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 1.5rem;
}

.avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.avatar svg {
  color: white;
}

.sidebar-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
  font-size: 1rem;
  font-weight: 500;
  text-align: left;
  width: 100%;
  text-decoration: none;
}

.nav-item:hover {
  background: #f7fafc;
  color: #f5576c;
}

.nav-item.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.nav-item svg {
  flex-shrink: 0;
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.schedule-management {
  padding: 2.5rem;
}

/* 头部区域 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.title-group h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

/* 筛选区 */
.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f7fafc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 200px;
}

.filter-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5568;
}

.filter-input,
.filter-select {
  padding: 0.625rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-separator {
  color: #718096;
  font-size: 0.9rem;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  align-self: flex-end;
}

.reset-btn:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #718096;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #f5576c;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #a0aec0;
}

.empty-state svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

/* 排班表格 */
.schedule-table {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.table-header {
  display: grid;
  grid-template-columns: 120px 150px 100px 140px 140px 100px 140px;
  gap: 1rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 10px;
  font-weight: 600;
  color: #4a5568;
  font-size: 0.9rem;
}

.table-row {
  display: grid;
  grid-template-columns: 120px 150px 100px 140px 140px 100px 140px;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  transition: all 0.3s ease;
  align-items: center;
}

.table-row:hover {
  border-color: #f093fb;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.2);
}

.table-row.conflict {
  border-color: #ffc107;
  background: #fffbf0;
}

.td-date {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date-main {
  font-weight: 600;
  color: #2d3748;
  font-size: 0.95rem;
}

.date-sub {
  font-size: 0.8rem;
  color: #718096;
}

.td-doctor {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.doctor-name {
  font-weight: 600;
  color: #2d3748;
}

.doctor-title {
  font-size: 0.8rem;
  color: #718096;
}

.department-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4a5568;
  font-size: 0.9rem;
}

.time-info svg {
  color: #f5576c;
}

.td-patients {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.patient-count {
  font-size: 0.9rem;
  font-weight: 600;
}

.count-current {
  color: #f5576c;
}

.count-separator {
  color: #cbd5e0;
  margin: 0 0.25rem;
}

.count-max {
  color: #718096;
}

.patient-bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.patient-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.patient-bar-fill.full {
  background: linear-gradient(90deg, #ffc107 0%, #ff9800 100%);
}

.td-status {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-align: center;
}

.status-badge.active {
  background: #d4edda;
  color: #28a745;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.conflict-tag {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #ffc107;
  font-size: 0.75rem;
  font-weight: 600;
}

.conflict-tag svg {
  flex-shrink: 0;
}

.td-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-btn {
  background: #e3f2fd;
  color: #2196f3;
}

.view-btn:hover {
  background: #2196f3;
  color: white;
}

.edit-btn {
  background: #fff3e0;
  color: #ff9800;
}

.edit-btn:hover {
  background: #ff9800;
  color: white;
}

.delete-btn {
  background: #ffebee;
  color: #f44336;
}

.delete-btn:hover {
  background: #f44336;
  color: white;
}

/* 弹窗样式 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-container.large {
  max-width: 800px;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 32px;
  height: 32px;
  background: #f7fafc;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #718096;
  z-index: 1;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #2d3748;
}

.modal-header {
  padding: 2rem 2rem 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.modal-body {
  padding: 2rem;
}

/* 表单样式 */
.batch-section {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 500;
  color: #4a5568;
}

.checkbox-label input[type="checkbox"] {
  cursor: pointer;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-control {
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
}

.form-control:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-control.error {
  border-color: #e53e3e;
}

.error-text {
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.date-range-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.weekday-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 0.75rem;
}

.weekday-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.weekday-checkbox:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.weekday-checkbox input[type="checkbox"]:checked ~ .checkbox-label {
  color: #f5576c;
  font-weight: 600;
}

.warning-box {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 10px;
  color: #856404;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.warning-box svg {
  flex-shrink: 0;
  color: #ffc107;
}

.warning-box strong {
  font-weight: 700;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.time-period-selector {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  padding: 0.75rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
}

.time-period-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  color: #4a5568;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.time-period-option:hover {
  background: white;
}

.time-period-option input[type="checkbox"] {
  accent-color: #f5576c;
  cursor: pointer;
  width: 18px;
  height: 18px;
}

.time-period-option input[type="checkbox"]:checked {
  accent-color: #f093fb;
}

/* 详情区域 */
.detail-section {
  margin-bottom: 2rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.detail-item {
  display: flex;
  gap: 0.5rem;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.item-label {
  color: #718096;
  font-weight: 500;
  min-width: 80px;
}

.item-value {
  color: #2d3748;
  font-weight: 600;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 8px;
}

.appointment-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.patient-name {
  font-weight: 600;
  color: #2d3748;
}

.patient-meta {
  font-size: 0.85rem;
  color: #718096;
}

.appointment-time {
  color: #f5576c;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 1400px) {
  .table-header,
  .table-row {
    grid-template-columns: 100px 130px 90px 120px 120px 90px 130px;
    gap: 0.75rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 1024px) {
  .profile-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .sidebar {
    width: 240px;
  }

  .filter-section {
    flex-direction: column;
  }

  .filter-group {
    min-width: auto;
  }

  .schedule-table {
    overflow-x: auto;
  }

  .table-header,
  .table-row {
    min-width: 900px;
  }
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
    padding: 1rem;
    gap: 1rem;
  }

  .sidebar {
    width: 100%;
    position: static;
    padding: 1.5rem;
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    text-align: left;
    padding-bottom: 1rem;
  }

  .avatar {
    width: 60px;
    height: 60px;
    margin: 0;
  }

  .avatar svg {
    width: 32px;
    height: 32px;
  }

  .sidebar-header h3 {
    font-size: 1.1rem;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem;
    min-width: 100px;
    text-align: center;
  }

  .nav-item span {
    font-size: 0.875rem;
  }

  .schedule-management {
    padding: 1.5rem;
  }

  .header-section {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .create-btn {
    width: 100%;
    justify-content: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column-reverse;
  }

  .modal-container {
    margin: 1rem;
  }

  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }
}
</style>
