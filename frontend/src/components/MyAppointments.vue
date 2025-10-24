<template>
  <div class="appointments-container">
    <div class="appointments-header">
      <h2>我的预约</h2>
      <div class="status-tabs">
        <button 
          :class="['tab-btn', { active: activeStatus === 'current' }]"
          @click="switchStatus('current')">
          当前预约
        </button>
        <button 
          :class="['tab-btn', { active: activeStatus === 'history' }]"
          @click="switchStatus('history')">
          历史预约
        </button>
      </div>
    </div>

    <div class="appointments-content">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="displayedAppointments.length === 0" class="empty-state">
        <p>暂无{{ activeStatus === 'current' ? '当前' : '历史' }}预约</p>
      </div>

      <transition-group v-else name="list" tag="div" class="appointments-list">
        <div 
          v-for="appt in displayedAppointments" 
          :key="appt.appointmentId"
          class="appointment-card">
          <div class="card-header">
            <span>{{ appt.appointmentTypeId || '健康咨询' }}</span>
            <span>{{ getStatusText(appt.appointmentStatus) }}</span>
          </div>
          <div class="card-body">
            <div>预约时间: {{ formatDate(appt.bookingTime) }}</div>
            <div>支付状态: {{ appt.paymentStatus }}</div>
          </div>
          <div class="card-footer">
            <button 
              v-if="activeStatus === 'current' && appt.appointmentStatus !== 'cancelled'"
              @click="cancelAppointment(appt.appointmentId)"
              class="cancel-btn">
              取消预约
            </button>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

const activeStatus = ref('current')
const loading = ref(false)
const currentAppointments = ref([])
const historyAppointments = ref([])
const token = localStorage.getItem('token')

// 显示对应状态列表
const displayedAppointments = computed(() => {
  return activeStatus.value === 'current'
    ? currentAppointments.value
    : historyAppointments.value
})

// 切换状态
function switchStatus(status) {
  activeStatus.value = status
  loadAppointments()
}

// 获取预约列表（当前或历史）
async function fetchAppointments() {
  if (!token) return

  loading.value = true
  try {
    const response = await axios.get('/api/patient/appointment/list', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const resData = response.data
    if (resData.code === 200) {
      currentAppointments.value = resData.data || []
    } else {
      console.warn('获取预约失败：', resData.message)
      currentAppointments.value = []
    }
  } catch (err) {
    console.error('获取预约失败', err)
    currentAppointments.value = []
  } finally {
    loading.value = false
  }
}

// 加载当前或历史预约
async function loadAppointments() {
  if (activeStatus.value === 'current' && currentAppointments.value.length === 0) {
    await fetchAppointments()
  } else if (activeStatus.value === 'history' && historyAppointments.value.length === 0) {
    // 如果你有历史接口，可在这里调用
    historyAppointments.value = []
  }
}

// 取消预约
async function cancelAppointment(appointmentId) {
  if (!confirm('确定要取消这个预约吗？')) return
  if (!token) return alert('未登录或登录已过期，请重新登录')

  loading.value = true
  try {
    const { data } = await axios.put('/api/patient/appointment/cancel', null, {
      headers: { Authorization: `Bearer ${token}` },
      params: { appointmentId }
    })

    if (data.code === 200) {
      alert('预约已取消')
      await fetchAppointments() // 刷新列表
    } else {
      alert('取消失败：' + data.message)
    }
  } catch (err) {
    console.error('取消预约失败', err)
    alert('取消失败，请重试')
  } finally {
    loading.value = false
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '未指定日期'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

// 状态文本
function getStatusText(status) {
  const map = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || '未知'
}

onMounted(() => {
  fetchAppointments()
})
</script>


<style scoped>
.appointments-container {
  padding: 2.5rem;
  height: 100%;
  overflow-y: auto;
}

.appointments-header {
  margin-bottom: 2rem;
}

h2 {
  margin: 0 0 1.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.status-tabs {
  display: flex;
  gap: 0.75rem;
  padding: 0.5rem;
  background: #f7fafc;
  border-radius: 12px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #4a5568;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.appointments-content {
  min-height: 400px;
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

/* 预约列表 */
.appointments-list {
  display: grid;
  gap: 1.5rem;
}

.appointment-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.appointment-card:hover {
  border-color: #cbd5e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-bottom: 2px solid #e2e8f0;
}

.appointment-type {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #2d3748;
  font-weight: 600;
  font-size: 1rem;
}

.appointment-type svg {
  color: #667eea;
}

.status-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.pending {
  background: #fef5e7;
  color: #f39c12;
}

.status-badge.confirmed {
  background: #d4edda;
  color: #28a745;
}

.status-badge.completed {
  background: #cce5ff;
  color: #004085;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.card-body {
  padding: 1.5rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  color: #4a5568;
  font-size: 0.95rem;
}

.info-row svg {
  color: #667eea;
  flex-shrink: 0;
}

.info-row:last-child {
  margin-bottom: 0;
}

.description {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.description p {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: #f7fafc;
  border-top: 2px solid #e2e8f0;
}

.cancel-btn,
.rebook-btn {
  flex: 1;
  padding: 0.625rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.cancel-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
}

.rebook-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.rebook-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
.reschedule-btn {
  flex: 1;
  padding: 0.625rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #38b2ac 0%, #319795 100%);
  color: white;
}

.reschedule-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(56, 178, 172, 0.4);
}



/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .appointments-container {
    padding: 1.5rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  .status-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }

  .card-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }

  .card-footer {
    flex-direction: column;
  }
}
</style>