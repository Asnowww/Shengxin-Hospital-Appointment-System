
<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="schedule-wrapper">
      <!-- 页面标题 -->
      <div class="header-section">
        <h1>排班管理</h1>
        <button @click="goToLeaveApplication" class="adjust-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          申请请假
        </button>
      </div>

      <!-- 日期选择标签 -->
      <div class="date-tabs">
        <button 
          :class="['tab-btn', { active: activeDate === 'today' }]"
          @click="switchDate('today')">
          今日排班
        </button>
        <button 
          :class="['tab-btn', { active: activeDate === 'future' }]"
          @click="switchDate('future')">
          未来排班
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载排班信息中...</p>
      </div>

      <!-- 排班列表 -->
      <div v-else class="schedule-content">
        <!-- 今日排班 -->
        <div v-if="activeDate === 'today'" class="schedule-section">
          <div class="schedule-info-card">
            <div class="info-row">
              <span class="label">出诊日期：</span>
              <span class="value">{{ todaySchedule.date }}</span>
            </div>
            <div class="info-row">
              <span class="label">出诊时间：</span>
              <span class="value">{{ todaySchedule.timeRange }}</span>
            </div>
            <div class="info-row">
              <span class="label">预约患者：</span>
              <span class="value highlight">{{ todaySchedule.patients.length }} 人</span>
            </div>
          </div>

          <!-- 患者列表 -->
          <div class="patients-section">
            <h3>预约患者列表</h3>
            <div v-if="todaySchedule.patients.length === 0" class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <p>今日暂无患者预约</p>
            </div>
            <div v-else class="patients-list">
              <div 
                v-for="patient in todaySchedule.patients" 
                :key="patient.id"
                class="patient-card"
                @click="showPatientDetail(patient)">
                <div class="patient-header">
                  <div class="patient-basic">
                    <h4>{{ patient.name }}</h4>
                    <span :class="['status-badge', patient.status]">
                      {{ getStatusText(patient.status) }}
                    </span>
                  </div>
                  <span class="appointment-time">{{ patient.appointmentTime }}</span>
                </div>
                <div class="patient-info">
                  <div class="info-item">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>{{ patient.gender }} / {{ patient.age }}岁</span>
                  </div>
                </div>
                <div v-if="patient.hasVisited" class="history-tag">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                  </svg>
                  曾就诊患者
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 未来排班 -->
        <div v-else class="schedule-section">
          <div v-if="futureSchedules.length === 0" class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <p>暂无未来排班</p>
          </div>
          <div v-else class="future-schedules-list">
            <div 
              v-for="schedule in futureSchedules" 
              :key="schedule.scheduleId || schedule.date"
              class="future-schedule-card">
              <div class="future-card-left">
                <div class="schedule-date">
                  <div class="date-main">{{ schedule.dayOfMonth }}</div>
                  <div class="date-sub">{{ schedule.weekday }}</div>
                </div>
                <div class="time-pill">{{ schedule.timeRange }}</div>
                <div 
                  class="status-chip" 
                  :class="{ full: schedule.patientCount >= schedule.maxPatients && schedule.maxPatients > 0 }">
                  {{ schedule.maxPatients > 0 && schedule.patientCount >= schedule.maxPatients ? '已满' : '可约' }}
                </div>
              </div>
              <div class="schedule-details">
                <div class="detail-row wide">
                  <div class="detail-label">日期</div>
                  <div class="detail-value">{{ schedule.date }}</div>
                </div>
                <div class="detail-row wide stats">
                  <div class="metric">
                    <span class="metric-title">已预约</span>
                    <span class="metric-value">{{ schedule.patientCount }} 人</span>
                  </div>
                  <div class="metric">
                    <span class="metric-title">可预约</span>
                    <span class="metric-value">
                      {{ schedule.maxPatients > 0 ? Math.max(schedule.maxPatients - schedule.patientCount, 0) : '—' }} 人
                    </span>
                  </div>
                  <div class="metric">
                    <span class="metric-title">总号源</span>
                    <span class="metric-value">{{ schedule.maxPatients || '—' }} 人</span>
                  </div>
                </div>
                <div class="progress-row">
                  <div class="progress-bg">
                    <div class="progress-fill" :style="{ width: schedule.capacityPercent + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ schedule.capacityPercent }}%</span>
                </div>
                <div class="actions">
                  <button @click="viewScheduleDetail(schedule)" class="view-btn primary">
                    查看详情
                  </button>
                  <button @click="showLeaveDialog(schedule)" class="view-btn leave-btn">
                    请假
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 患者详情弹窗 -->
    <transition name="modal">
      <div v-if="showPatientModal" class="modal-overlay" @click.self="closePatientModal">
        <div class="modal-container">
          <button @click="closePatientModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>患者详情</h2>
          </div>

          <div v-if="selectedPatient" class="modal-body">
            <div class="patient-detail-section">
              <h3>基本信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="item-label">姓名：</span>
                  <span class="item-value">{{ selectedPatient.name }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">性别：</span>
                  <span class="item-value">{{ selectedPatient.gender }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">年龄：</span>
                  <span class="item-value">{{ selectedPatient.age }}岁</span>
                </div>
                <div class="detail-item full-width">
                  <span class="item-label">预约时间：</span>
                  <span class="item-value">{{ selectedPatient.appointmentTime }}</span>
                </div>
              </div>
            </div>

            <div class="patient-detail-section">
              <h3>就诊历史</h3>
              <div v-if="!selectedPatient.hasVisited" class="no-history">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>该患者暂无就诊记录</p>
              </div>
              <div v-else class="history-list">
                <div 
                  v-for="(visit, index) in selectedPatient.visitHistory" 
                  :key="index"
                  class="history-item">
                  <div class="history-date">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="16" y1="2" x2="16" y2="6"></line>
                      <line x1="8" y1="2" x2="8" y2="6"></line>
                      <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    {{ visit.date }}
                  </div>
                  <div class="history-content">
                    <div class="history-row">
                      <span class="history-label">诊断：</span>
                      <span class="history-value">{{ visit.diagnosis }}</span>
                    </div>
                    <div v-if="visit.notes" class="history-row">
                      <span class="history-label">备注：</span>
                      <span class="history-value">{{ visit.notes }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 排班详情弹窗 -->
    <transition name="modal">
      <div v-if="showScheduleModal" class="modal-overlay" @click.self="closeScheduleModal">
        <div class="modal-container">
          <button @click="closeScheduleModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>排班详情</h2>
            <p class="subtitle">查看当日排班的患者列表</p>
          </div>

          <div class="modal-body">
            <div class="schedule-summary">
              <div class="summary-row">
                <span>日期</span>
                <strong>{{ detailSchedule.date }}</strong>
              </div>
              <div class="summary-row">
                <span>时间段</span>
                <strong>{{ detailSchedule.timeRange }}</strong>
              </div>
              <div class="summary-row">
                <span>预约情况</span>
                <strong>{{ detailSchedule.patientCount }} / {{ detailSchedule.maxPatients || '—' }} 人</strong>
              </div>
            </div>

            <div v-if="detailLoading" class="loading-state small">
              <div class="spinner"></div>
              <p>加载排班详情中...</p>
            </div>
            <div v-else class="patients-section detail">
              <h3>患者列表</h3>
              <div v-if="detailPatients.length === 0" class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                <p>该排班暂无患者预约</p>
              </div>
              <div v-else class="patients-list">
                <div 
                  v-for="patient in detailPatients" 
                  :key="patient.id"
                  class="patient-card compact">
                  <div class="patient-header">
                    <div class="patient-basic">
                      <h4>{{ patient.name }}</h4>
                      <span :class="['status-badge', patient.status]">
                        {{ getStatusText(patient.status) }}
                      </span>
                    </div>
                    <span class="appointment-time">{{ patient.appointmentTime }}</span>
                  </div>
                  <div class="patient-info">
                    <div class="info-item">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                      <span>{{ patient.gender }} / {{ patient.age }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 申请调整时间弹窗 -->
    <transition name="modal">
      <div v-if="showAdjustModal" class="modal-overlay" @click.self="closeAdjustModal">
        <div class="modal-container">
          <button @click="closeAdjustModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>申请调整出诊时间</h2>
            <p class="subtitle">请选择需要调整的日期并说明原因</p>
          </div>

          <form @submit.prevent="submitAdjustRequest" class="modal-body">
            <div class="form-group">
              <label>调整日期</label>
              <input 
                v-model="adjustForm.date" 
                type="date" 
                :min="minDate"
                class="form-input"
                required
              />
            </div>

            <div class="form-group">
              <label>调整原因</label>
              <textarea 
                v-model="adjustForm.reason" 
                class="form-input"
                rows="4"
                placeholder="请说明调整出诊时间的原因"
                required
              ></textarea>
            </div>

            <div v-if="adjustForm.hasPatients" class="warning-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <div>
                <strong>注意：</strong>该日期已有 {{ adjustForm.patientCount }} 位患者预约，
                调整后需要重新安排这些患者的就诊时间
              </div>
            </div>

            <div class="button-group">
              <button type="button" @click="closeAdjustModal" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="submit-btn">
                提交申请
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>

    <!-- 请假弹窗 -->
    <transition name="modal">
      <div v-if="showLeaveModal" class="modal-overlay" @click.self="closeLeaveModal">
        <div class="modal-container">
          <button @click="closeLeaveModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>申请请假</h2>
            <p class="subtitle">请填写请假原因</p>
          </div>

          <form @submit.prevent="submitLeaveRequest" class="modal-body">
            <div class="form-group">
              <label>排班信息</label>
              <div class="schedule-info-display">
                <div class="info-display-row">
                  <span class="info-label">日期：</span>
                  <span class="info-value">{{ leaveForm.scheduleDate }}</span>
                </div>
                <div class="info-display-row">
                  <span class="info-label">时间：</span>
                  <span class="info-value">{{ leaveForm.scheduleTime }}</span>
                </div>
                <div v-if="leaveForm.patientCount > 0" class="info-display-row warning">
                  <span class="info-label">已预约：</span>
                  <span class="info-value">{{ leaveForm.patientCount }} 人</span>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>请假事由 <span class="required">*</span></label>
              <textarea 
                v-model="leaveForm.reason" 
                class="form-input"
                rows="4"
                placeholder="请输入请假原因"
                required
              ></textarea>
            </div>

            <div v-if="leaveForm.patientCount > 0" class="warning-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <div>
                <strong>注意：</strong>该排班已有 {{ leaveForm.patientCount }} 位患者预约，
                请假申请通过后，这些患者需要重新安排就诊时间
              </div>
            </div>

            <div class="button-group">
              <button type="button" @click="closeLeaveModal" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="submit-btn" :disabled="submittingLeave">
                {{ submittingLeave ? '提交中...' : '提交申请' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const activeDate = ref('today')
const loading = ref(false)
const loadError = ref('')

const token = ref(localStorage.getItem('token') || '')
const userId = ref(localStorage.getItem('userId') || '')

const todaySchedule = ref({
  date: '',
  timeRange: '',
  patients: [],
  scheduleId: null
})

const futureSchedules = ref([])

const showPatientModal = ref(false)
const selectedPatient = ref(null)

const showAdjustModal = ref(false)
const adjustForm = ref({
  date: '',
  reason: '',
  hasPatients: false,
  patientCount: 0
})

const showLeaveModal = ref(false)
const submittingLeave = ref(false)
const leaveForm = ref({
  scheduleId: null,
  scheduleDate: '',
  scheduleTime: '',
  patientCount: 0,
  reason: ''
})

const showScheduleModal = ref(false)
const detailSchedule = ref({
  date: '',
  timeRange: '',
  patientCount: 0,
  maxPatients: 0
})
const detailPatients = ref([])
const detailLoading = ref(false)

const minDate = computed(() => {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  return tomorrow.toISOString().split('T')[0]
})

function formatInputDate(date) {
  return date.toISOString().split('T')[0]
}

function addDays(date, days) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

function formatWeekday(dateStr) {
  const weekdayMap = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const d = new Date(dateStr)
  return weekdayMap[d.getDay()]
}

function formatDayOfMonth(dateStr) {
  const d = new Date(dateStr)
  return String(d.getDate()).padStart(2, '0')
}

function formatTimeSlot(timeSlot) {
  if (timeSlot === 0) return '上午'
  if (timeSlot === 1) return '下午'
  if (timeSlot === 2) return '晚上'
  return '未指定'
}

function buildDateLabel(dateStr) {
  return dateStr ? `${dateStr}（${formatWeekday(dateStr)}）` : ''
}

function buildAppointmentTime(slot, slotName, queueNumber) {
  const slotText = typeof slot === 'number' ? formatTimeSlot(slot) : (slotName || '未指定')
  return queueNumber ? `${slotText} · 序号${queueNumber}` : slotText
}

function mapAppointmentStatus(status) {
  if (status === 'completed') return 'completed'
  if (status === 'missed' || status === 'no_show') return 'missed'
  if (status === 'cancelled' || status === 'refunded') return 'cancelled'
  if (status === 'pending') return 'pending'
  return 'confirmed'
}

function transformPatients(patients, slotName) {
  if (!Array.isArray(patients)) return []
  return patients.map(p => {
    const status = mapAppointmentStatus(p.appointmentStatus)
    return {
      id: p.appointmentId || p.patientId,
      name: p.patientName || '患者',
      gender: p.gender === 'M' ? '男' : (p.gender === 'F' ? '女' : '未知'),
      age: p.age ?? '--',
      appointmentTime: buildAppointmentTime(p.bookingTime, slotName, p.queueNumber),
      status,
      hasVisited: status === 'completed',
      visitHistory: []
    }
  })
}

function calculatePatientCount(schedule) {
  if (typeof schedule?.bookedSlots === 'number') return schedule.bookedSlots
  if (typeof schedule?.maxSlots === 'number' && typeof schedule?.availableSlots === 'number') {
    return Math.max(schedule.maxSlots - schedule.availableSlots, 0)
  }
  return 0
}

function sortByTimeSlot(a, b) {
  return (a?.timeSlot ?? 0) - (b?.timeSlot ?? 0)
}

async function loadMySchedules(startDate, endDate) {
  if (!userId.value) {
    throw new Error('未找到登录信息，请重新登录医生账号')
  }
  const { data } = await axios.get('/api/doctor/schedules/my', {
    params: { userId: userId.value, startDate, endDate }
  })
  if (data?.code !== 200) {
    throw new Error(data?.message || '获取排班失败')
  }
  return Array.isArray(data.data) ? data.data : []
}

async function fetchPatientSchedules(dateStr) {
  const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
  const { data } = await axios.get('/api/doctor/patient/schedules-line', {
    params: { date: dateStr },
    headers
  })
  if (data?.code !== 200) {
    throw new Error(data?.message || '获取患者列表失败')
  }
  return Array.isArray(data.data) ? data.data : []
}

// 切换日期标签
function switchDate(date) {
  activeDate.value = date
  if (date === 'today') {
    fetchTodaySchedule()
  } else {
    fetchFutureSchedules()
  }
}

// 获取今日排班
async function fetchTodaySchedule() {
  loading.value = true
  loadError.value = ''
  try {
    const todayStr = formatInputDate(new Date())
    const [scheduleList, patientSchedules] = await Promise.all([
      loadMySchedules(todayStr, todayStr),
      fetchPatientSchedules(todayStr)
    ])

    const candidate =[...patientSchedules].sort(sortByTimeSlot).find(item => Array.isArray(item.patients) && item.patients.length > 0)
  || [...patientSchedules].sort(sortByTimeSlot)[0]
  || (scheduleList.length ? { ...scheduleList.sort(sortByTimeSlot)[0], patients: [] } : null)

    if (!candidate) {
      todaySchedule.value = { date: '', timeRange: '', patients: [], scheduleId: null }
      return
    }

    todaySchedule.value = {
      scheduleId: candidate.scheduleId,
      date: buildDateLabel(candidate.workDate),
      timeRange: candidate.timeSlotName || formatTimeSlot(candidate.timeSlot),
      patients: transformPatients(candidate.patients || [], candidate.timeSlotName || formatTimeSlot(candidate.timeSlot))
    }
  } catch (err) {
    loadError.value = err?.response?.data?.message || err?.message || '获取今日排班失败'
    todaySchedule.value = { date: '', timeRange: '', patients: [], scheduleId: null }
    alert(loadError.value)
  } finally {
    loading.value = false
  }
}

// 获取未来排班
async function fetchFutureSchedules() {
  loading.value = true
  loadError.value = ''
  try {
    const start = formatInputDate(addDays(new Date(), 1))
    const end = formatInputDate(addDays(new Date(), 30))
    const schedules = await loadMySchedules(start, end)
    const todayStr = formatInputDate(new Date())

    futureSchedules.value = schedules
      .filter(item => item.workDate > todayStr)
      .filter(item => item.status !== 'cancelled')
      .sort((a, b) => {
        const diff = new Date(a.workDate) - new Date(b.workDate)
        return diff !== 0 ? diff : sortByTimeSlot(a, b)
      })
      .map(item => ({
        scheduleId: item.scheduleId,
        date: item.workDate,
        dayOfMonth: formatDayOfMonth(item.workDate),
        weekday: formatWeekday(item.workDate),
        timeRange: item.timeSlotName || formatTimeSlot(item.timeSlot),
        timeSlot: item.timeSlot,
        timeSlotName: item.timeSlotName,
        patientCount: calculatePatientCount(item),
        maxPatients: item.maxSlots ?? 0,
        availableSlots: item.availableSlots ?? null,
        capacityPercent: (item.maxSlots ?? 0)
          ? Math.min(100, Math.round((calculatePatientCount(item) / item.maxSlots) * 100))
          : 0
      }))
  } catch (err) {
    loadError.value = err?.response?.data?.message || err?.message || '获取未来排班失败'
    futureSchedules.value = []
    alert(loadError.value)
  } finally {
    loading.value = false
  }
}

// 获取状态文案
function getStatusText(status) {
  const map = {
    confirmed: '已确认',
    waiting: '待就诊',
    completed: '已完成',
    cancelled: '已取消',
    missed: '已过号',
    pending: '待支付'
  }
  return map[status] || '未知'
}

// 显示患者详情
function showPatientDetail(patient) {
  selectedPatient.value = patient
  showPatientModal.value = true
}

// 关闭患者详情
function closePatientModal() {
  showPatientModal.value = false
  selectedPatient.value = null
}

// 查看排班详情
function viewScheduleDetail(schedule) {
  detailSchedule.value = {
    date: buildDateLabel(schedule.date),
    timeRange: schedule.timeRange,
    patientCount: schedule.patientCount,
    maxPatients: schedule.maxPatients
  }
  detailPatients.value = []
  showScheduleModal.value = true
  detailLoading.value = true

  fetchPatientSchedules(schedule.date)
    .then(list => {
      const target = list.find(item => item.scheduleId === schedule.scheduleId) || null
      if (target) {
        detailSchedule.value = {
          date: buildDateLabel(target.workDate),
          timeRange: target.timeSlotName || formatTimeSlot(target.timeSlot),
          patientCount: calculatePatientCount(target),
          maxPatients: target.maxSlots ?? schedule.maxPatients ?? 0
        }
        detailPatients.value = transformPatients(
          target.patients || [],
          target.timeSlotName || formatTimeSlot(target.timeSlot)
        )
      }
    })
    .catch(err => {
      alert(err?.response?.data?.message || err?.message || '获取排班详情失败')
    })
    .finally(() => {
      detailLoading.value = false
    })
}

function closeScheduleModal() {
  showScheduleModal.value = false
}

// 跳转到请假申请界面
function goToLeaveApplication() {
  router.push('/doctor/leave/apply')
}

// 显示请假弹窗
function showLeaveDialog(schedule) {
  leaveForm.value = {
    scheduleId: schedule.scheduleId,
    scheduleDate: schedule.date,
    scheduleTime: schedule.timeRange,
    patientCount: schedule.patientCount,
    reason: ''
  }
  showLeaveModal.value = true
}

// 关闭请假弹窗
function closeLeaveModal() {
  showLeaveModal.value = false
  leaveForm.value = {
    scheduleId: null,
    scheduleDate: '',
    scheduleTime: '',
    patientCount: 0,
    reason: ''
  }
}

// 提交请假申请
async function submitLeaveRequest() {
  const reason = leaveForm.value.reason?.trim()
  if (!reason) {
    alert('请输入请假事由')
    return
  }

  const scheduleId = Number(leaveForm.value.scheduleId)
  if (!scheduleId || Number.isNaN(scheduleId)) {
    alert('排班信息错误，请重试')
    return
  }

  const uid = Number(userId.value)
  if (!uid) {
    alert('无法识别用户，请重新登录')
    return
  }

  const payload = {
    userId: uid,
    scheduleIds: [scheduleId],
    reason
  }

  const headers = token.value
    ? { Authorization: `Bearer ${token.value}` }
    : {}

  try {
    submittingLeave.value = true

    const res = await axios.post(
      '/api/doctor/schedules/leave/apply',
      payload,
      { headers }
    )

    if (res.data?.code && res.data.code !== 200) {
      alert(res.data.message || '提交失败，请重试')
      return
    }

    alert('请假申请已提交，请等待审批')
    closeLeaveModal()
    await fetchFutureSchedules()

  } catch (err) {
    console.error('提交请假申请失败', err)
    alert(err?.response?.data?.message || '提交失败，请重试')
  } finally {
    submittingLeave.value = false
  }
}



// 显示调整时间弹窗
function showAdjustDialog() {
  adjustForm.value = {
    date: '',
    reason: '',
    hasPatients: false,
    patientCount: 0
  }
  showAdjustModal.value = true
}

// 关闭调整时间弹窗
function closeAdjustModal() {
  showAdjustModal.value = false
}

// 提交调整申请
async function submitAdjustRequest() {
  if (!adjustForm.value.date || !adjustForm.value.reason) {
    alert('请填写完整信息')
    return
  }
  
  try {
    // 实际使用时替换为真实请求
    // await axios.post('/api/doctor/schedule/adjust', adjustForm.value)
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 800))
    
    alert('调整申请已提交，等待管理员审核')
    closeAdjustModal()
  } catch (err) {
    alert('提交失败，请重试')
    console.error('提交调整申请失败', err)
  }
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
  await fetchTodaySchedule()
  await fetchFutureSchedules()
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
  max-width: 1400px;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.adjust-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.adjust-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 日期标签 */
.date-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.tab-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  background: white;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.95);
}

.tab-btn.active {
  background: white;
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 16px;
  color: #718096;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 排班内容 */
.schedule-content {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

/* 排班信息卡片 */
.schedule-info-card {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
  font-size: 1rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #4a5568;
  font-weight: 500;
  min-width: 100px;
}

.value {
  color: #2d3748;
  font-weight: 600;
}

.value.highlight {
  color: #667eea;
  font-size: 1.125rem;
}

/* 患者列表 */
.patients-section h3 {
  margin: 0 0 1.5rem 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.patients-list {
  display: grid;
  gap: 1rem;
}

.patient-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.patient-card.compact {
  cursor: default;
}

.patient-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.patient-basic {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.patient-basic h4 {
  margin: 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-badge.confirmed {
  background: #d4edda;
  color: #28a745;
}

.status-badge.waiting {
  background: #fff3cd;
  color: #856404;
}

.status-badge.completed {
  background: #cce5ff;
  color: #004085;
}

.status-badge.cancelled {
  background: #fdecea;
  color: #c53030;
}

.status-badge.missed {
  background: #fff4e5;
  color: #c05621;
}

.status-badge.pending {
  background: #e9ecef;
  color: #495057;
}

.appointment-time {
  color: #667eea;
  font-weight: 600;
  font-size: 1rem;
}

.patient-info {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 0.75rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #718096;
  font-size: 0.9rem;
}

.info-item svg {
  color: #667eea;
}

.history-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: #e6f7ff;
  color: #0050b3;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.history-tag svg {
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
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

.loading-state.small {
  padding: 2rem 1rem;
}

.schedule-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  background: #f8fafc;
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid #e2e8f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  color: #4a5568;
  font-weight: 600;
}

.summary-row strong {
  color: #2d3748;
}
/* 未来排班列表 */
.future-schedules-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.future-schedule-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  transition: all 0.3s ease;
}

.future-schedule-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.future-card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  min-width: 120px;
}

.schedule-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 1rem;
  min-width: 70px;
  color: white;
}

.date-main {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.date-sub {
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.time-pill {
  padding: 0.35rem 0.75rem;
  background: #f0f4ff;
  color: #4c51bf;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.85rem;
  border: 1px solid #e0e7ff;
}

.status-chip {
  padding: 0.35rem 0.85rem;
  border-radius: 10px;
  background: #e6fffa;
  color: #0f766e;
  font-weight: 700;
  font-size: 0.85rem;
  border: 1px solid #b2f5ea;
}

.status-chip.full {
  background: #fff5f5;
  color: #c53030;
  border-color: #fed7d7;
}

.schedule-details {
  flex: 1;
}

.detail-row {
  display: flex;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #718096;
  min-width: 50px;
}

.detail-value {
  color: #2d3748;
  font-weight: 500;
}

.detail-row.wide {
  justify-content: space-between;
  align-items: center;
}

.detail-row.stats {
  gap: 1.25rem;
  flex-wrap: wrap;
}

.metric {
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  min-width: 120px;
  border: 1px solid #edf2f7;
}

.metric-title {
  color: #718096;
  font-size: 0.85rem;
}

.metric-value {
  color: #2d3748;
  font-weight: 700;
  margin-top: 0.25rem;
}

.progress-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 0.5rem 0 0.75rem;
}

.progress-bg {
  flex: 1;
  height: 10px;
  background: #edf2f7;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.progress-text {
  color: #4a5568;
  font-weight: 600;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-top: 0.75rem;
}

.view-btn {
  padding: 0.5rem 1rem;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #667eea;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.view-btn:hover {
  background: #edf2f7;
  border-color: #667eea;
}

.view-btn.primary {
  background: linear-gradient(135deg, #4c6fff 0%, #6b8bff 100%);
  color: white;
  border: none;
}

.view-btn.primary:hover {
  box-shadow: 0 10px 30px rgba(76, 111, 255, 0.25);
}

.view-btn.leave-btn {
  background: #fff5f5;
  border-color: #fc8181;
  color: #c53030;
}

.view-btn.leave-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
  box-shadow: 0 4px 12px rgba(197, 48, 48, 0.2);
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

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.modal-body {
  padding: 2rem;
}

/* 患者详情 */
.patient-detail-section {
  margin-bottom: 2rem;
}

.patient-detail-section:last-child {
  margin-bottom: 0;
}

.patient-detail-section h3 {
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

.no-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #a0aec0;
}

.no-history svg {
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.no-history p {
  margin: 0;
  font-size: 0.95rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  background: #f7fafc;
  border-radius: 10px;
  padding: 1rem;
}

.history-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #667eea;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}

.history-date svg {
  flex-shrink: 0;
}

.history-content {
  padding-left: 1.75rem;
}

.history-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.history-row:last-child {
  margin-bottom: 0;
}

.history-label {
  color: #718096;
  min-width: 50px;
}

.history-value {
  color: #2d3748;
  flex: 1;
}

/* 表单 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #e53e3e;
}

.schedule-info-display {
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 1rem;
}

.info-display-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.info-display-row:last-child {
  margin-bottom: 0;
}

.info-display-row.warning {
  color: #c53030;
}

.info-label {
  color: #718096;
  font-weight: 500;
  min-width: 60px;
}

.info-value {
  color: #2d3748;
  font-weight: 600;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea.form-input {
  resize: vertical;
  font-family: inherit;
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
  padding-top: 1rem;
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
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .schedule-wrapper {
    padding: 1rem;
  }

  .header-section {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
    padding: 1.25rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .adjust-btn {
    width: 100%;
    justify-content: center;
  }

  .date-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }

  .schedule-content {
    padding: 1.5rem;
  }

  .patient-info {
    flex-direction: column;
    gap: 0.5rem;
  }

  .future-schedules-list {
    grid-template-columns: 1fr;
  }

  .future-schedule-card {
    flex-direction: column;
  }

  .schedule-date {
    min-width: auto;
    padding: 0.75rem;
  }

  .view-btn {
    width: 100%;
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
