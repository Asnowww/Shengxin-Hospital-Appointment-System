<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="schedule-wrapper">
      <!-- 返回和标题 -->
      <div class="header-section">
        <div class="breadcrumb">
          <router-link to="/department" class="back-link">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
            返回
          </router-link>
          <span class="separator">/</span>
          <span class="current-dept">{{ departmentName }}</span>
        </div>
        <h1>{{ departmentName }} - 预约挂号</h1>
        <p class="dept-desc">选择合适的医生和时间进行预约</p>
      </div>

      <!-- 标签页切换 -->
      <div class="tabs-section">
        <div class="tabs-container">
          <button 
            v-for="tab in appointmentTabs" 
            :key="tab.id"
            :class="['tab', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id">
            {{ tab.label }}
          </button>
        </div>
        
        <!-- 日期筛选 -->
        <div class="filter-section">
          <div class="date-filter">
            <label>选择日期：</label>
            <div class="date-buttons">
              <button 
                v-for="(date, idx) in dateOptions" 
                :key="idx"
                :class="['date-btn', { active: selectedDate === date.value }]"
                @click="selectedDate = date.value">
                <span class="date-label">{{ date.label }}</span>
                <span class="date-value">{{ date.display }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 普通门诊 -->
      <div v-if="activeTab === 'general'" class="content-section">
        <!-- 时间段筛选 -->
        <div class="time-filter">
          <button 
            v-for="slot in timeSlots" 
            :key="slot.id"
            :class="['time-btn', { active: selectedTimeSlot === slot.id }]"
            @click="selectedTimeSlot = slot.id">
            {{ slot.name }}
          </button>
        </div>

        <!-- 普通门诊排班卡片 -->
        <div class="schedules-grid">
          <div 
            v-for="schedule in filteredGeneralSchedules" 
            :key="schedule.scheduleId"
            class="schedule-card">
            
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="doctor-brief">
                <div class="doctor-avatar">{{ schedule.doctorName.charAt(0) }}</div>
                <div class="doctor-info">
                  <h3>{{ schedule.doctorName }}</h3>
                  <p class="doctor-title">{{ schedule.doctorTitle }}</p>
                </div>
              </div>
              <span :class="['appointment-type', `type-${schedule.appointmentTypeId}`]">
                {{ schedule.appointmentTypeName }}
              </span>
            </div>

            <!-- 卡片内容 -->
            <div class="card-body">
              <div class="info-row">
                <span class="label">诊室：</span>
                <span class="value">{{ schedule.roomName }}</span>
              </div>
              <div class="info-row">
                <span class="label">时间：</span>
                <span class="value">{{ formatDateTime(schedule.workDate, schedule.timeSlotName) }}</span>
              </div>
              
              <!-- 号源信息 -->
              <div class="slots-info">
                <div class="slot-row">
                  <span class="slot-label">号源</span>
                  <div class="slot-numbers">
                    <span class="available">可用: {{ schedule.availableSlots }}</span>
                    <span class="total">/ {{ schedule.maxSlots }}</span>
                  </div>
                </div>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: calculateProgressPercentage(schedule) + '%' }"></div>
                </div>
              </div>

              <!-- 状态和按钮 -->
              <div class="card-footer">
                <span :class="['status', schedule.status]">
                  {{ getStatusText(schedule) }}
                </span>
                <button 
                  :disabled="schedule.availableSlots === 0"
                  :class="['appoint-btn', { disabled: schedule.availableSlots === 0 }]"
                  @click="handleAppointment(schedule)">
                  {{ schedule.availableSlots > 0 ? '立即预约' : '已满' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredGeneralSchedules.length === 0" class="empty-state">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
          </svg>
          <p>暂无排班信息</p>
        </div>
      </div>

      <!-- 专家门诊 -->
      <div v-else class="content-section">
        <div class="expert-list">
          <div 
            v-for="doctor in filteredExpertDoctors" 
            :key="doctor.id"
            class="doctor-card">
            
            <!-- 医生头部 -->
            <div class="doctor-header">
              <div class="doctor-avatar-large">{{ doctor.name.charAt(0) }}</div>
              <div class="doctor-header-info">
                <h2>{{ doctor.name }}</h2>
                <p class="title">{{ doctor.title }}</p>
                <p class="specialty">{{ doctor.specialty || '医疗专长待补充' }}</p>
              </div>
            </div>

            <!-- 医生排班信息 -->
            <div class="doctor-schedules">
              <div class="schedules-title">出诊时间安排</div>
              
              <div class="schedule-items">
                <div 
                  v-for="schedule in getExpertSchedules(doctor.id)" 
                  :key="schedule.scheduleId"
                  class="schedule-item">
                  
                  <div class="schedule-time">
                    <span class="date">{{ formatDate(schedule.workDate) }}</span>
                    <span class="time-slot">{{ schedule.timeSlotName }}</span>
                  </div>

                  <div class="schedule-details">
                    <span class="room">{{ schedule.roomName }}</span>
                    <span :class="['slots', { available: schedule.availableSlots > 0 }]">
                      {{ schedule.availableSlots > 0 ? `${schedule.availableSlots}个号` : '已满' }}
                    </span>
                  </div>

                  <button 
                    :disabled="schedule.availableSlots === 0"
                    :class="['quick-appoint', { disabled: schedule.availableSlots === 0 }]"
                    @click="handleAppointment(schedule)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="12 5 19 12 12 19"></polyline>
                      <path d="M19 12H5"></path>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- 专家门诊说明 -->
              <div class="expert-note">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="16" x2="12" y2="12"></line>
                  <line x1="12" y1="8" x2="12.01" y2="8"></line>
                </svg>
                专家门诊需提前48小时预约
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredExpertDoctors.length === 0" class="empty-state">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          <p>暂无专家挂号</p>
        </div>
      </div>
    </div>

    <!-- 预约模态框 -->
    <transition name="modal">
      <div v-if="showAppointModal" class="modal-overlay" @click.self="showAppointModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h2>确认预约</h2>
            <button class="close-btn" @click="showAppointModal = false">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <div class="modal-body">
            <div class="info-group">
              <span class="label">医生：</span>
              <span class="value">{{ selectedSchedule?.doctorName }} ({{ selectedSchedule?.doctorTitle }})</span>
            </div>
            <div class="info-group">
              <span class="label">科室：</span>
              <span class="value">{{ selectedSchedule?.deptName }}</span>
            </div>
            <div class="info-group">
              <span class="label">日期：</span>
              <span class="value">{{ formatFullDate(selectedSchedule?.workDate) }}</span>
            </div>
            <div class="info-group">
              <span class="label">时间：</span>
              <span class="value">{{ selectedSchedule?.timeSlotName }}</span>
            </div>
            <div class="info-group">
              <span class="label">诊室：</span>
              <span class="value">{{ selectedSchedule?.roomName }}</span>
            </div>
            <div class="info-group">
              <span class="label">预约类型：</span>
              <span class="value">{{ selectedSchedule?.appointmentTypeName }}</span>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-cancel" @click="showAppointModal = false">取消</button>
            <button class="btn-confirm" @click="confirmAppointment">确认预约</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const route = useRoute()
const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const departmentId = ref(route.query.deptId)
const departmentName = ref(route.query.deptName)
const doctorId = ref(route.query.doctorId || null)


const activeTab = ref('general')
const selectedDate = ref('')
const selectedTimeSlot = ref('')
const schedules = ref([])
const doctors = ref([])
const loading = ref(false)
const showAppointModal = ref(false)
const selectedSchedule = ref(null)

const appointmentTabs = [
  { id: 'general', label: '普通门诊' },
  {id:'special', label: '特需门诊' },
  { id: 'expert', label: '专家门诊' }
]

const timeSlots = [
  { id: 'all', name: '全天' },
  { id: 'morning', name: '上午' },
  { id: 'afternoon', name: '下午' }
]

// 日期选项
const dateOptions = computed(() => {
  const options = []
  const today = new Date()
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    
    const dateStr = date.toISOString().split('T')[0]
    let label = ''
    
    if (i === 0) label = '今天'
    else if (i === 1) label = '明天'
    else label = '其他'
    
    options.push({
      value: dateStr,
      label,
      display: date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    })
  }
  
  return options
})

// 初始化日期
function initializeDate() {
  selectedDate.value = dateOptions.value[0].value
}

// 格式化日期时间
function formatDateTime(date, timeSlot) {
  const d = new Date(date)
  const dateStr = d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  return `${dateStr} ${timeSlot}`
}

function formatDate(date) {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
}

function formatFullDate(date) {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 计算进度百分比
function calculateProgressPercentage(schedule) {
  return (schedule.bookedSlots / schedule.maxSlots) * 100
}

// 获取状态文本
function getStatusText(schedule) {
  if (schedule.availableSlots === 0) return '已满'
  if (schedule.status === 'open') return '可预约'
  return '未开放'
}

// 普通门诊过滤
const filteredGeneralSchedules = computed(() => {
  return schedules.value.filter(s => {
    const dateMatch = selectedDate.value === '' || s.workDate === selectedDate.value
    
    let timeMatch = true
    if (selectedTimeSlot.value === 'morning') {
      timeMatch = s.timeSlotName.includes('上午') || s.timeSlot === 0
    } else if (selectedTimeSlot.value === 'afternoon') {
      timeMatch = s.timeSlotName.includes('下午') || s.timeSlot === 1
    }
    
    return dateMatch && timeMatch && s.appointmentTypeId === 1
  })
})

// 专家门诊过滤
const filteredExpertDoctors = computed(() => {
  const expertSchedules = schedules.value.filter(s => s.appointmentTypeId === 2)
  const uniqueDoctors = [...new Set(expertSchedules.map(s => s.doctorId))]
  
  return uniqueDoctors.map(docId => {
    const doc = doctors.value.find(d => d.id === docId)
    return doc || { id: docId, name: '待加载', title: '' }
  }).filter(doc => doc)
})

// 获取医生的专家排班
function getExpertSchedules(doctorId) {
  return schedules.value.filter(s => 
    s.doctorId === doctorId && 
    s.appointmentTypeId === 2 &&
    (selectedDate.value === '' || s.workDate === selectedDate.value)
  )
}

//加载排班信息
async function fetchSchedules() {
  loading.value = true
  try {
    const { data } = await axios.get('/api/patient/schedules/available', {
      params: {
        deptId: departmentId.value,         
        startDate: dateOptions.value[0].value,
        endDate: dateOptions.value[6].value
      }
    })
    schedules.value = data.data || []
  } catch (err) {
    console.error('加载排班失败', err)
  } finally {
    loading.value = false
  }
}



// 处理预约
function handleAppointment(schedule) {
  selectedSchedule.value = schedule
  showAppointModal.value = true
}

async function confirmAppointment() {
  if (!selectedSchedule.value) {
    alert('请选择排班')
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    alert('未登录或登录已过期，请重新登录')
    return
  }

  try {
    const response = await axios.post(
      '/api/patient/appointment/create',
      {
        scheduleId: selectedSchedule.value.scheduleId,
        appointmentTypeId: selectedSchedule.value.appointmentTypeId
      },
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    )

    const resData = response.data
    if (resData.code !== 200) {
      alert('预约失败：' + resData.message)
      return
    }

    showAppointModal.value = false
    alert('预约成功！')
    await fetchSchedules()
  } catch (err) {
    console.error('预约失败', err)
    alert('预约失败，请重试')
  }
}



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
  
  initializeDate()
  selectedTimeSlot.value = 'all'
  
  await Promise.all([fetchSchedules()])
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
}

.schedule-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 140px);
}

/* 头部区域 */
.header-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #718096;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #667eea;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-link:hover {
  gap: 0.5rem;
}

.separator {
  color: #cbd5e0;
}

.current-dept {
  color: #4a5568;
  font-weight: 600;
}

h1 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.dept-desc {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

/* 标签页 */
.tabs-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tabs-container {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e2e8f0;
}

.tab {
  padding: 0.875rem 1.5rem;
  background: none;
  border: none;
  color: #718096;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  margin-bottom: -1.5rem;
  position: relative;
  bottom: 2px;
}

.tab:hover {
  color: #667eea;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

/* 日期筛选 */
.filter-section {
  padding-top: 1rem;
}

.date-filter {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.date-filter label {
  font-weight: 600;
  color: #4a5568;
}

.date-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.date-btn {
  padding: 0.625rem 1rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.date-btn:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.date-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

.date-label {
  font-size: 0.75rem;
  opacity: 0.9;
}

.date-value {
  font-size: 0.9rem;
  font-weight: 600;
}

/* 内容区域 */
.content-section {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 时间筛选 */
.time-filter {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.time-btn {
  padding: 0.625rem 1.25rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  color: #4a5568;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.time-btn:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.time-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

/* 排班卡片网格 */
.schedules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.schedule-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.schedule-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  transform: translateY(-4px);
}

.card-header {
  background: linear-gradient(135deg, #f7fafc 0%, #f0f4ff 100%);
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: start;
}

.doctor-brief {
  display: flex;
  gap: 0.75rem;
  flex: 1;
}

.doctor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.doctor-info h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

.doctor-title {
  margin: 0.25rem 0 0 0;
  color: #718096;
  font-size: 0.85rem;
}

.appointment-type {
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  flex-shrink: 0;
}

.appointment-type.type-1 {
  background: #d4edda;
  color: #28a745;
}

.appointment-type.type-2 {
  background: #cce5ff;
  color: #0056b3;
}

/* 卡片主体 */
.card-body {
  padding: 1rem;
}

.info-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.info-row .label {
  color: #718096;
  min-width: 50px;
}

.info-row .value {
  color: #2d3748;
  font-weight: 500;
}

/* 号源信息 */
.slots-info {
  background: #f7fafc;
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
}

.slot-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.slot-label {
  color: #718096;
  font-weight: 600;
  font-size: 0.9rem;
}

.slot-numbers {
  display: flex;
  gap: 0.25rem;
  font-size: 0.9rem;
  font-weight: 600;
}

.slot-numbers .available {
  color: #28a745;
}

.slot-numbers .total {
  color: #718096;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.status {
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status.open {
  background: #d4edda;
  color: #28a745;
}

.status.closed {
  background: #f8d7da;
  color: #721c24;
}

.appoint-btn {
  padding: 0.625rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.appoint-btn:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.appoint-btn.disabled {
  background: #cbd5e0;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 专家门诊 */
.expert-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.doctor-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.doctor-card:hover {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
}

.doctor-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.doctor-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 2rem;
  flex-shrink: 0;
  border: 3px solid rgba(255, 255, 255, 0.4);
}

.doctor-header-info {
  flex: 1;
}

.doctor-header-info h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.doctor-header-info .title {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  opacity: 0.95;
}

.doctor-header-info .specialty {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.85;
}

/* 医生排班 */
.doctor-schedules {
  padding: 1.5rem;
}

.schedules-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.schedule-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem;
  background: #f7fafc;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.schedule-item:hover {
  background: #f0f4ff;
  border-left: 4px solid #667eea;
  padding-left: calc(0.875rem - 4px);
}

.schedule-time {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 70px;
}

.schedule-time .date {
  font-size: 0.85rem;
  color: #718096;
  font-weight: 600;
}

.schedule-time .time-slot {
  font-size: 0.9rem;
  color: #2d3748;
  font-weight: 700;
}

.schedule-details {
  flex: 1;
  display: flex;
  gap: 1rem;
  align-items: center;
  min-width: 0;
}

.schedule-details .room {
  flex: 1;
  font-size: 0.9rem;
  color: #4a5568;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.schedule-details .slots {
  font-size: 0.9rem;
  font-weight: 600;
  color: #718096;
  padding: 0.375rem 0.75rem;
  background: white;
  border-radius: 6px;
  white-space: nowrap;
}

.schedule-details .slots.available {
  color: #28a745;
  background: #d4edda;
}

.quick-appoint {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.quick-appoint:hover:not(.disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.quick-appoint.disabled {
  background: #cbd5e0;
  cursor: not-allowed;
  opacity: 0.6;
}

.expert-note {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #cce5ff;
  color: #0056b3;
  border-radius: 6px;
  font-size: 0.85rem;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  color: #718096;
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
  color: #718096;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 90%;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  color: #2d3748;
}

.modal-body {
  padding: 1.5rem;
}

.info-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 0;
  border-bottom: 1px solid #e2e8f0;
}

.info-group:last-child {
  border-bottom: none;
}

.info-group .label {
  color: #718096;
  font-weight: 600;
  min-width: 80px;
}

.info-group .value {
  color: #2d3748;
  font-weight: 500;
  text-align: right;
  flex: 1;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f7fafc;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 响应式 */
@media (max-width: 768px) {
  .schedule-wrapper {
    padding: 1rem;
  }

  .header-section,
  .tabs-section,
  .content-section {
    padding: 1rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .schedules-grid {
    grid-template-columns: 1fr;
  }

  .date-buttons {
    justify-content: space-between;
  }

  .date-btn {
    flex: 1;
    min-width: 80px;
  }

  .doctor-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .doctor-header-info {
    width: 100%;
  }

  .schedule-item {
    flex-wrap: wrap;
  }

  .schedule-details {
    width: 100%;
    order: 3;
  }

  .modal-content {
    width: 95%;
  }
}
</style>