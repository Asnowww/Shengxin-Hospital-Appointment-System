<template>
  <teleport to="body">
    <transition name="modal-fade">
      <div v-if="visible" class="modal-overlay" @click.self="handleClose">
        <div class="modal-container">
          <!-- 头部 -->
          <div class="modal-header">
            <h2>改约</h2>
            <button class="close-btn" @click="handleClose">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <!-- 当前预约信息 -->
          <div class="current-info">
            <div class="info-label">当前预约</div>
            <div class="info-content">
              <span class="info-doctor">{{ appointmentInfo.currentDoctorName }}</span>
              <span class="info-appointmentType">{{ appointmentInfo.currentAppointmentTypeName }}</span>
              <span class="info-time">{{ appointmentInfo.currentTime }}</span>
            </div>
          </div>

          <!-- 筛选区域 -->
          <div class="filter-section">
            <div class="filter-row">
              <div class="filter-item">
                <label>日期</label>
                <input 
                  type="date" 
                  v-model="filterDate" 
                  :min="minDate" 
                  :max="maxDate"
                  class="date-input"
                />
              </div>
              <div class="filter-item">
                <label>时间段</label>
                <select v-model="filterTimeSlot" class="select-input">
                  <option value="">全部</option>
                  <option value="0">上午</option>
                  <option value="1">下午</option>
                </select>
              </div>
            </div>
            <div class="filter-row">
              <div class="filter-item full-width">
                <label>医生</label>
                <select v-model="filterDoctor" class="select-input">
                  <option value="">全部医生</option>
                  <option 
                    v-for="doc in availableDoctors" 
                    :key="doc.doctorId" 
                    :value="doc.doctorId"
                  >
                    {{ doc.doctorName }} - {{ doc.title }}
                  </option>
                </select>
              </div>
              <div class="filter-item full-width">
    <label>科室</label>
    <select v-model="filterDept" class="select-input">
      <option value="">全部科室</option>
      <option
        v-for="dept in availableDepts"
        :key="dept.deptId"
        :value="dept.deptId"
      >
        {{ dept.deptName }}
      </option>
    </select>
  </div>
            </div>
          </div>

          <!-- 号源列表 -->
          <div class="schedules-section">
            <div v-if="loading" class="loading-state">
              <div class="spinner"></div>
              <p>加载中...</p>
            </div>

            <div v-else-if="filteredSchedules.length === 0" class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2"></rect>
                <path d="M16 2v4M8 2v4M3 10h18"></path>
              </svg>
              <p>暂无可用号源</p>
              <span class="empty-hint">请尝试选择其他日期或时间段</span>
            </div>

            <div v-else class="schedules-list">
              <div 
                v-for="schedule in filteredSchedules" 
                :key="schedule.scheduleId"
                :class="['schedule-card', { selected: selectedScheduleId === schedule.scheduleId }]"
                @click="selectSchedule(schedule)"
              >
                <div class="schedule-main">
                  <div class="schedule-doctor">
                    <span class="doctor-name">{{ schedule.doctorName }}</span>
                    <span class="doctor-title">{{ schedule.doctorTitle }}</span>
                    <span class="appointment-type-tag">{{ schedule.appointmentTypeName }}</span>
                  </div>
                  <div class="schedule-time">
                    <span class="date">{{ formatDate(schedule.workDate) }}</span>
                    <span class="time-slot">{{ getTimeSlotName(schedule.timeSlot) }}</span>
                  </div>
                </div>
                <div class="schedule-meta">
                  <div class="schedule-dept">{{ schedule.deptName }}</div>
                  <div class="schedule-slots">
                    <span class="slots-available">余 {{ schedule.availableSlots }}</span>
                  </div>
                  <div class="schedule-fee">¥{{ schedule.fee?.toFixed(2) || '—' }}</div>
                </div>
                <div v-if="selectedScheduleId === schedule.scheduleId" class="selected-indicator">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="modal-footer">
            <button class="btn-cancel" @click="handleClose">取消</button>
            <button 
              class="btn-confirm" 
              :disabled="!selectedScheduleId || submitting"
              @click="handleConfirm"
            >
              {{ submitting ? '改约中...' : '确认改约' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  appointmentId: {
    type: [Number, String],
    required: true
  },
  appointmentInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'success'])
// 科室筛选
const filterDept = ref('')

// 状态
const loading = ref(false)
const submitting = ref(false)
const schedules = ref([])
const selectedScheduleId = ref(null)

// 筛选条件
const filterDate = ref('')
const filterTimeSlot = ref('')
const filterDoctor = ref('')

// 日期范围（今天到14天后）
const today = new Date()
const minDate = today.toISOString().split('T')[0]
const maxDateObj = new Date(today)
maxDateObj.setDate(maxDateObj.getDate() + 14)
const maxDate = maxDateObj.toISOString().split('T')[0]

// Token
const token = localStorage.getItem('token')

// 可选医生列表（从号源中提取）
const availableDoctors = computed(() => {
  const doctorMap = new Map()
  schedules.value.forEach(s => {
    if (s.availableSlots > 0 && !doctorMap.has(s.doctorId)) {
      doctorMap.set(s.doctorId, {
        doctorId: s.doctorId,
        doctorName: s.doctorName,
        title: s.doctorTitle
      })
    }
  })
  return Array.from(doctorMap.values())
})

// 可选科室列表（从号源中提取）
const availableDepts = computed(() => {
  const deptMap = new Map()
  schedules.value.forEach(s => {
    if (s.availableSlots > 0 && !deptMap.has(s.deptId)) {
      deptMap.set(s.deptId, {
        deptId: s.deptId,
        deptName: s.deptName
      })
    }
  })
  return Array.from(deptMap.values())
})

// 筛选后的号源
const filteredSchedules = computed(() => {
  return schedules.value.filter(s => {
    // 1. 基础状态过滤：只显示有余量的号源
    if (s.availableSlots <= 0) return false
    
    // 2. 核心逻辑：同级别改约校验
    // 假设 props.appointmentInfo 中包含了当前预约的 appointmentTypeName 或 ID
    // 如果后端没给，建议在打开弹窗请求号源前，先确认当前预约的级别
    if (props.appointmentInfo.currentAppointmentTypeName && 
        s.appointmentTypeName !== props.appointmentInfo.currentAppointmentTypeName) {
      return false
    }

    // 3. 既有的 UI 筛选条件
    if (filterDate.value && s.workDate !== filterDate.value) return false
    if (filterTimeSlot.value !== '' && String(s.timeSlot) !== filterTimeSlot.value) return false
    if (filterDoctor.value && s.doctorId !== filterDoctor.value) return false
    if (filterDept.value && s.deptId !== filterDept.value) return false

    return true
  })
})

// 获取可用号源
async function fetchAvailableSchedules() {
  if (!token) return
  
  loading.value = true
  try {
    // 动态计算日期范围（确保每次都是最新的）
    const now = new Date()
    const currentMinDate = now.toISOString().split('T')[0]
    const maxDateCalc = new Date(now)
    maxDateCalc.setDate(maxDateCalc.getDate() + 14)
    const currentMaxDate = maxDateCalc.toISOString().split('T')[0]
    
    const params = {
      startDate: currentMinDate,
      endDate: currentMaxDate
    }
    
    console.log('[RescheduleModal] 请求号源参数:', params)
    
    const response = await axios.get('/api/patient/appointment/available-schedules', {
      headers: { Authorization: `Bearer ${token}` },
      params
    })
    
    console.log('[RescheduleModal] 响应数据:', response.data)
    
    if (response.data.code === 200) {
      schedules.value = response.data.data || []
      console.log('[RescheduleModal] 获取到的号源数量:', schedules.value.length)
      // 打印所有号源的日期，方便调试
      const dates = [...new Set(schedules.value.map(s => s.workDate))].sort()
      console.log('[RescheduleModal] 包含的日期:', dates)
    } else {
      console.warn('获取号源失败：', response.data.message)
      schedules.value = []
    }
  } catch (err) {
    console.error('获取号源失败', err)
    schedules.value = []
  } finally {
    loading.value = false
  }
}

// 选择号源
function selectSchedule(schedule) {
  selectedScheduleId.value = schedule.scheduleId
}

// 确认改约
async function handleConfirm() {
  if (!selectedScheduleId.value || submitting.value) return
  
  submitting.value = true
  try {
    const patientId = localStorage.getItem('patientId') || localStorage.getItem('userId')
    
    const response = await axios.put('/api/patient/appointment/update', {
      appointmentId: props.appointmentId,
      patientId: Number(patientId),
      newScheduleId: selectedScheduleId.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    if (response.data.code === 200) {
      emit('success')
    } else {
      alert('改约失败：' + (response.data.message || '未知错误'))
    }
  } catch (err) {
    console.error('改约失败', err)
    alert('改约失败：' + (err.response?.data?.message || '请稍后重试'))
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
function handleClose() {
  selectedScheduleId.value = null
  filterDate.value = ''
  filterTimeSlot.value = ''
  filterDoctor.value = ''
  filterDept.value = ''
  emit('close')
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${month}月${day}日 ${weekDay}`
}

// 获取时间段名称
function getTimeSlotName(slot) {
  const names = { 0: '上午', 1: '下午', 2: '晚上' }
  return names[slot] || ''
}

// 监听弹窗打开
watch(() => props.visible, (newVal) => {
  if (newVal) {
    fetchAvailableSchedules()
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.modal-container {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s ease;
}

/* 新增号别标签样式 */
.appointment-type-tag {
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  background: #e6fffa; /* 浅绿色背景 */
  color: #2c7a7b; /* 深青色文字 */
  border: 1px solid #81e6d9;
  border-radius: 4px;
  margin-left: 4px;
  font-weight: 500;
}

/* 调整医生信息容器，防止溢出 */
.schedule-doctor {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap; /* 手机端自动换行 */
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  padding: 0.5rem;
  cursor: pointer;
  color: #fff;
  transition: background 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.current-info {
  padding: 1rem 1.5rem;
  background: #f7fafc;
  border-bottom: 1px solid #e2e8f0;
}

.info-label {
  font-size: 0.75rem;
  color: #718096;
  margin-bottom: 0.25rem;
}

.info-content {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.info-doctor {
  font-weight: 600;
  color: #2d3748;
}
.info-appointmentType {
  font-size: 0.875rem;
  color: #4a5568;
}
.info-time {
  font-size: 0.875rem;
  color: #4a5568;
}

.filter-section {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.filter-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.filter-item.full-width {
  flex: 1;
}

.filter-item label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.date-input,
.select-input {
  padding: 0.6rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #2d3748;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.date-input:focus,
.select-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.schedules-section {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.5rem;
  min-height: 200px;
  max-height: 300px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: #a0aec0;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 0.5rem;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
  color: #718096;
}

.empty-hint {
  font-size: 0.8rem;
  color: #a0aec0;
  margin-top: 0.25rem;
}

.schedules-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.schedule-card {
  position: relative;
  padding: 1rem;
  background: #fff;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.schedule-card:hover {
  border-color: #cbd5e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.schedule-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.schedule-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.schedule-doctor {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.doctor-name {
  font-weight: 600;
  color: #2d3748;
}

.doctor-title {
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  background: #edf2f7;
  color: #4a5568;
  border-radius: 4px;
}

.schedule-time {
  text-align: right;
}

.schedule-time .date {
  font-size: 0.875rem;
  color: #4a5568;
}

.schedule-time .time-slot {
  display: block;
  font-size: 0.75rem;
  color: #718096;
}

.schedule-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.8rem;
}

.schedule-dept {
  color: #718096;
}

.schedule-slots {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.slots-available {
  color: #38a169;
  font-weight: 600;
}

.schedule-fee {
  margin-left: auto;
  font-weight: 600;
  color: #667eea;
}

.selected-indicator {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f7fafc;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 0.875rem 1rem;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #edf2f7;
  color: #4a5568;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 480px) {
  .modal-container {
    max-height: 90vh;
    border-radius: 16px 16px 0 0;
  }
  
  .filter-row {
    flex-direction: column;
    gap: 0.75rem;
  }
}
</style>
