<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="doctor-wrapper">
      <div class="header-section">
        <div class="breadcrumb">
          <router-link to="/department" class="back-link">
            <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
            返回
          </router-link>
          <span class="separator">/</span>
          <router-link :to="`/departmentSchedule?deptId=${doctorInfo.deptId}&deptName=${doctorInfo.deptName}`" class="dept-link">
            {{ doctorInfo.deptName }}
          </router-link>
          <span class="separator">/</span>
          <span class="current-doctor">{{ doctorInfo.name }}</span>
        </div>
        <h1>{{ doctorInfo.name }}</h1>
        <p class="doctor-title">{{ doctorInfo.title }}</p>
      </div>

      <div class="doctor-info-card">
        <div class="info-header">
          <div class="avatar-large">{{ doctorInfo.name?.charAt(0) }}</div>
          <div class="info-content">
            <h2>{{ doctorInfo.name }}</h2>
            <p class="title">{{ doctorInfo.title }}</p>
            <p class="dept">{{ doctorInfo.deptName }}</p>
            <div class="intro">{{ doctorInfo.introduction || '暂无介绍' }}</div>
          </div>
        </div>
      </div>

      <div class="filter-section">
        <div class="date-filter">
          <label>选择日期：</label>
          <div class="date-buttons">
            <button v-for="(date, idx) in dateOptions" :key="idx" :class="['date-btn', { active: selectedDate === date.value }]" @click="selectedDate = date.value">
              <span class="date-label">{{ date.label }}</span>
              <span class="date-value">{{ date.display }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="schedules-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <div v-if="dayOverMessage" class="empty-state notice-state">
          <svg xmlns="https://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="orange" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <p>{{ dayOverMessage }}</p>
        </div>

        <div v-else-if="filteredSchedules.length > 0" class="schedules-grid">
          <div v-for="schedule in filteredSchedules" :key="schedule.scheduleId" class="schedule-card">
            <div class="appointment-type-wrapper">
              <span :class="['appointment-type', `type-${schedule.appointmentTypeId}`]">
                {{ schedule.appointmentTypeName }}
              </span>
              <div class="appointment-fee">
                ¥{{ schedule.fee ? schedule.fee.toFixed(2) : '—' }}
              </div>
            </div>

            <div class="card-content">
              <div class="time-section">
                <div class="date-large">{{ formatDate(schedule.workDate) }}</div>
                <div class="time-slot-large">{{ schedule.timeSlotName }}</div>
              </div>
              <div class="divider"></div>
              <div class="details-section">
                <div class="detail-row">
                  <span class="label">诊室：</span>
                  <span class="value">{{ schedule.roomName }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">门诊类型：</span>
                  <span class="value">{{ schedule.appointmentTypeName }}</span>
                </div>
                <div class="slots-section">
                  <div class="slots-header">
                    <span class="label">号源</span>
                    <span class="count">{{ schedule.availableSlots }} / {{ schedule.maxSlots }}</span>
                  </div>
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: calculateProgressPercentage(schedule) + '%' }"></div>
                  </div>
                  <div class="slots-text">
                    <span class="available">可用 {{ schedule.availableSlots }} 个</span>
                    <span class="booked">已预约 {{ schedule.bookedSlots }} 个</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="card-footer">
              <span :class="['status', schedule.status]">
                {{ getStatusText(schedule) }}
              </span>
              <button 
                :disabled="isSubmitting" 
                :class="['appoint-btn', { 'waitlist-btn': schedule.availableSlots === 0 }]" 
                @click="handleAppointment(schedule)">
                <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 5v14M5 12h14"></path>
                </svg>
                {{ schedule.availableSlots > 0 ? '立即预约' : '候补' }}
              </button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <svg xmlns="https://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2"></rect>
            <path d="M16 2v4M8 2v4M3 10h18"></path>
          </svg>
          <p>暂无可预约的排班</p>
        </div>
      </div>
    </div>

    <transition name="modal">
      <div v-if="showAppointModal" class="modal-overlay" @click.self="showAppointModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h2>{{ selectedSchedule?.availableSlots > 0 ? '确认预约' : '确认候补' }}</h2>
            <button class="close-btn" @click="showAppointModal = false">
              <svg xmlns="https://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="selectedSchedule?.availableSlots === 0" class="waitlist-notice">
              <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <p>该号源已满，加入候补队列后有新号源时将自动为您预约</p>
            </div>
            <div class="info-group">
              <span class="label">医生：</span>
              <span class="value">{{ selectedSchedule?.doctorName }}</span>
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
              <span class="label">费用：</span>
              <span class="value">¥{{ selectedSchedule?.fee?.toFixed(2) }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showAppointModal = false">取消</button>
            <button class="btn-confirm" :disabled="isSubmitting" @click="confirmAppointment">
              {{ isSubmitting ? '提交中...' : (selectedSchedule?.availableSlots > 0 ? '确认预约' : '确认候补') }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <Payment 
      :visible="showPaymentModal" 
      :appointment-id="paymentData.appointmentId" 
      @close="showPaymentModal = false" 
      @payment-success="handlePaymentSuccess" 
      @payment-error="handlePaymentError" 
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'
import Payment from '@/components/Payment.vue'

const route = useRoute()
const navRef = ref(null)
const navHeight = ref(110)
const doctorId = ref(route.query.doctorId)
const selectedDate = ref('')
const schedules = ref([])
const loading = ref(false)
const isSubmitting = ref(false)
const showAppointModal = ref(false)
const showPaymentModal = ref(false)
const selectedSchedule = ref(null)
const paymentData = ref({ appointmentId: null })

const MORNING_DEADLINE_HOUR = 11   // 11:00 后禁止上午
const ALLDAY_DEADLINE_HOUR = 16   // 16:00 后禁止当日全部


const dayOverMessage = computed(() => {
  const todayStr = new Date().toISOString().split('T')[0]
  const now = new Date(
  new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
)

  const hours = now.getHours()

  if (selectedDate.value === todayStr) {
    //  当日 16 点后，全部不可预约
    if (hours >= ALLDAY_DEADLINE_HOUR) {
      return '已过当天可预约时间段（每日16:00后停止当日预约）'
    }

    // 当日 11 点后，禁止上午
    if (hours >= MORNING_DEADLINE_HOUR) {
      const hasPM = schedules.value.some(s =>
        s.workDate === todayStr && /下午|PM/i.test(s.timeSlotName)
      )
      if (!hasPM) return '今日上午时段已过，下午暂无排班'
    }
  }

  return ''
})

const doctorInfo = ref({
  name: '', title: '', deptName: '', deptId: '', introduction: ''
})
const dateOptions = ref([])

const filteredSchedules = computed(() => {
  const todayStr = new Date().toISOString().split('T')[0]
  const now = new Date(
    new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
  )
  const hours = now.getHours()

  let filtered = schedules.value.filter(s => {
    // ======= 核心修复：永远不显示晚间排班 =======
    if (
      s.timeSlot === 2 ||
      s.timeSlotName?.includes('晚') ||
      s.timeSlotName?.includes('夜')
    ) {
      return false
    }
    // ==========================================

    if (s.status === 'cancelled') return false
    if (s.workDate !== selectedDate.value) return false
    return true
  })

  // ===== 以下是“当天时间窗口”规则（仍然保留） =====

  if (selectedDate.value === todayStr) {
    // 16:00 后，当天全部不可预约
    if (hours >= ALLDAY_DEADLINE_HOUR) {
      return []
    }

    // 11:00 后，当天禁止上午
    if (hours >= MORNING_DEADLINE_HOUR) {
      filtered = filtered.filter(
        s => !(/上午|AM/i.test(s.timeSlotName))
      )
    }
  }

  return filtered
})


watch(showPaymentModal, async (newVal) => {
  if (newVal === false) {
    await fetchSchedules()
  }
})

function handlePaymentSuccess(data) {
  alert('支付成功！您的预约已完成。')
  showPaymentModal.value = false
}

function handlePaymentError(errorMsg) {
  alert('支付未完成：' + errorMsg)
}

async function fetchSchedules() {
  loading.value = true
  try {
    const startDate = new Date().toISOString().split('T')[0]
    const endDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]

    const [scheduleRes, feeRes] = await Promise.all([
      axios.get(`/api/patient/appointment/all-schedules/doctor/${doctorId.value}`, {
        params: { startDate, endDate }
      }),
      axios.get('/api/fee/type_amount')
    ])

    const feeMap = new Map((feeRes.data.data || []).map(item => [item.appointmentTypeId, item.fee]))
    
    if (scheduleRes.data.code === 200) {
      schedules.value = (scheduleRes.data.data || []).map(s => ({
        ...s,
        fee: feeMap.get(s.appointmentTypeId) || 0
      }))

      const dates = [...new Set(schedules.value.map(s => s.workDate))].sort()
      dateOptions.value = dates.map(date => {
        const d = new Date(date)
        const todayStr = new Date().toISOString().split('T')[0]
        let label = (date === todayStr) ? '今天' : d.toLocaleDateString('zh-CN', { weekday: 'short' })
        return { value: date, label, display: d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) }
      })
      if (!selectedDate.value && dateOptions.value.length > 0) {
        selectedDate.value = dateOptions.value[0].value
      }
    }
  } catch (err) {
    console.error('加载失败', err)
  } finally {
    loading.value = false
  }
}

async function fetchDoctorInfo() {
  try {
    const response = await axios.get(`/api/doctor/doctorId/${doctorId.value}`)
    const data = response.data
    doctorInfo.value = {
      name: route.query.doctorName || data.name,
      title: data.title || route.query.doctorTitle,
      deptName: route.query.deptName || data.deptName,
      deptId: data.deptId || route.query.deptId,
      introduction: data.bio || ''
    }
  } catch (err) { console.error(err) }
}

function handleAppointment(schedule) {
  selectedSchedule.value = schedule
  showAppointModal.value = true
}

async function confirmAppointment() {
  if (!selectedSchedule.value) return
  const token = localStorage.getItem('token')
  if (!token) return alert('请先登录')

  isSubmitting.value = true
  try {
    const isWaitlist = selectedSchedule.value.availableSlots === 0

    if (isWaitlist) {
      // ② 候补逻辑
      const response = await axios.post('/api/waitlist/create', {
        scheduleId: selectedSchedule.value.scheduleId
      }, { headers: { Authorization: `Bearer ${token}` } })

      if (response.data.code === 200) {
        showAppointModal.value = false
        alert('候补成功！有号源时将自动为您预约')
        await fetchSchedules()
      } else {
        alert('候补失败：' + response.data.message)
      }
    } else {
      // 正常预约逻辑
      const response = await axios.post('/api/patient/appointment/create', {
        scheduleId: selectedSchedule.value.scheduleId,
        appointmentTypeId: selectedSchedule.value.appointmentTypeId
      }, { headers: { Authorization: `Bearer ${token}` } })

      if (response.data.code === 200) {
        paymentData.value.appointmentId = response.data.data?.appointmentId
        showAppointModal.value = false
        showPaymentModal.value = true
      } else {
        alert(response.data.message)
      }
    }
  } catch (err) {
    alert('请求失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

const formatDate = (d) => new Date(d).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
const formatFullDate = (d) => new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
const calculateProgressPercentage = (s) => (s.bookedSlots / s.maxSlots) * 100
const getStatusText = (s) => s.availableSlots === 0 ? '可候补' : (s.status === 'open' ? '可预约' : '未开放')

function updateNavHeight() { if (navRef.value?.$el) navHeight.value = navRef.value.$el.offsetHeight + 30 }

onMounted(() => {
  updateNavHeight()
  window.addEventListener('resize', updateNavHeight)
  fetchDoctorInfo()
  fetchSchedules()
})

onUnmounted(() => window.removeEventListener('resize', updateNavHeight))
</script>

<style scoped>
/* 保持原有样式，新增提示状态样式 */
.notice-state {
  flex-direction: column;
  color: #ff9800;
  background: #fff8e1;
  padding: 40px;
  border-radius: 12px;
  border: 1px dashed #ffe082;
}
</style>


<style scoped>
.page-container {
  min-height: 100vh;
}

.appointment-type-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.appointment-type {
  font-weight: 600;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 8px;
  background-color: #eef2ff;
  color: #334155;
}
.appointment-type {
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  flex-shrink: 0;
}
.appointment-fee {
  font-size: 15px;
  color: #64748b;
}
.appointment-type.type-1 {
  background: #d4edda;
  color: #28a745;
}

.appointment-type.type-2 {
  background: #cce5ff;
  color: #0056b3;
}

.appointment-type.type-3 {
  background: #fff3cd;
  color: #856404;
}


.doctor-wrapper {
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
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #718096;
  flex-wrap: wrap;
}

.back-link,
.dept-link {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #667eea;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-link:hover,
.dept-link:hover {
  gap: 0.5rem;
}

.separator {
  color: #cbd5e0;
}

.current-doctor {
  color: #4a5568;
  font-weight: 600;
}

h1 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 2rem;
  font-weight: 700;
}

.doctor-title {
  margin: 0 0 0.25rem 0;
  color: #667eea;
  font-size: 1rem;
  font-weight: 600;
}

.doctor-specialty {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

/* 医生信息卡片 */
.doctor-info-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.info-header {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 2.5rem;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-content {
  flex: 1;
}

.info-content h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.info-content .title {
  margin: 0 0 0.25rem 0;
  color: #667eea;
  font-size: 1.1rem;
  font-weight: 600;
}

.info-content .dept {
  margin: 0 0 1rem 0;
  color: #718096;
  font-size: 0.95rem;
}

.intro {
  margin: 0;
  color: #4a5568;
  line-height: 1.6;
  font-size: 0.95rem;
}

/* 日期筛选 */
.filter-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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

/* 排班卡片 */
.schedules-container {
  min-height: 300px;
}

.schedules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.schedule-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  position: relative;
}

.schedule-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.card-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 10;
}

.badge {
  display: inline-block;
  padding: 0.375rem 0.875rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge.type-1 {
  background: #d4edda;
  color: #28a745;
}

.badge.type-2 {
  background: #cce5ff;
  color: #0056b3;
}

.badge.type-3 {
  background: #fff3cd;
  color: #856404;
}

.card-content {
  padding: 1.5rem;
  flex: 1;
}

.time-section {
  text-align: center;
  margin-bottom: 1.5rem;
}

.date-large {
  font-size: 0.95rem;
  color: #718096;
  margin-bottom: 0.25rem;
}

.time-slot-large {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
}

.divider {
  height: 1px;
  background: #e2e8f0;
  margin: 1.5rem 0;
}

.details-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.detail-row .label {
  color: #718096;
  font-weight: 600;
}

.detail-row .value {
  color: #2d3748;
  font-weight: 500;
}

/* 号源信息 */
.slots-section {
  background: #f7fafc;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.slots-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.slots-header .label {
  color: #718096;
  font-weight: 600;
  font-size: 0.9rem;
}

.slots-header .count {
  font-size: 1.1rem;
  font-weight: 700;
  color: #667eea;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.75rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.slots-text {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
}

.slots-text .available {
  color: #28a745;
  font-weight: 600;
}

.slots-text .booked {
  color: #718096;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f7fafc;
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
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
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

/* 加载状态 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #718096;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
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
  color: #718096;
  background: white;
  border-radius: 16px;
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
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
  .doctor-wrapper {
    padding: 1rem;
  }

  .header-section,
  .doctor-info-card,
  .filter-section {
    padding: 1rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .info-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 1.5rem;
  }

  .avatar-large {
    width: 80px;
    height: 80px;
    font-size: 2rem;
  }

  .schedules-grid {
    grid-template-columns: 1fr;
  }

  .date-buttons {
    justify-content: space-between;
  }

  .date-btn {
    flex: 1;
    min-width: 70px;
  }

  .modal-content {
    width: 95%;
  }
}

@media (max-width: 480px) {
  .doctor-wrapper {
    padding: 0.5rem;
  }

  h1 {
    font-size: 1.25rem;
  }

  .breadcrumb {
    font-size: 0.8rem;
  }

  .card-badge {
    top: 0.75rem;
    right: 0.75rem;
  }

  .badge {
    padding: 0.25rem 0.625rem;
    font-size: 0.7rem;
  }

  .appoint-btn {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }

  .time-slot-large {
    font-size: 1.25rem;
  }
}
</style>