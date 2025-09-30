<template>
  <div class="appointments-container">
    <div class="appointments-header">
      <h2>我的预约</h2>
      <div class="status-tabs">
        <button 
          :class="['tab-btn', { active: activeStatus === 'current' }]"
          @click="activeStatus = 'current'">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          当前预约
        </button>
        <button 
          :class="['tab-btn', { active: activeStatus === 'history' }]"
          @click="activeStatus = 'history'">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
            <path d="M3 3v5h5"></path>
            <path d="M12 7v5l4 2"></path>
          </svg>
          历史预约
        </button>
      </div>
    </div>

    <div class="appointments-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="displayedAppointments.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
          <line x1="16" y1="2" x2="16" y2="6"></line>
          <line x1="8" y1="2" x2="8" y2="6"></line>
          <line x1="3" y1="10" x2="21" y2="10"></line>
        </svg>
        <p>暂无{{ activeStatus === 'current' ? '当前' : '历史' }}预约</p>
      </div>

      <!-- 预约列表 -->
      <transition-group v-else name="list" tag="div" class="appointments-list">
        <div 
          v-for="appt in displayedAppointments" 
          :key="appt.id"
          class="appointment-card">
          <div class="card-header">
            <div class="appointment-type">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
              </svg>
              <span>{{ appt.type || '健康咨询' }}</span>
            </div>
            <span :class="['status-badge', appt.status]">
              {{ getStatusText(appt.status) }}
            </span>
          </div>

          <div class="card-body">
            <div class="info-row">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              <span>{{ formatDate(appt.date) }}</span>
            </div>

            <div class="info-row">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ appt.time || '未指定时间' }}</span>
            </div>

            <div v-if="appt.doctor" class="info-row">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <span>{{ appt.doctor }}</span>
            </div>

            <div v-if="appt.location" class="info-row">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>{{ appt.location }}</span>
            </div>

            <div v-if="appt.description" class="description">
              <p>{{ appt.description }}</p>
            </div>
          </div>

          <div class="card-footer">
              <!-- 改约按钮 -->
            <button 
                v-if="activeStatus === 'current' && appt.status !== 'cancelled'"
                @click="rescheduleAppointment(appt)"
                class="reschedule-btn">
                改约
            </button>
                <!-- 取消预约按钮 -->
            <button 
              v-if="activeStatus === 'current' && appt.status !== 'cancelled'"
              @click="cancelAppointment(appt.id)"
              class="cancel-btn">
              取消预约
            </button>
            <button 
              v-if="activeStatus === 'history'"
              @click="rebookAppointment(appt)"
              class="rebook-btn">
              再次预约
            </button>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const activeStatus = ref('current')
const loading = ref(false)
const currentAppointments = ref([])
const historyAppointments = ref([])

// 计算显示的预约列表
const displayedAppointments = computed(() => {
  return activeStatus.value === 'current' 
    ? currentAppointments.value 
    : historyAppointments.value
})

// 获取当前预约
async function fetchCurrentAppointments() {
  loading.value = true
  try {
    const { data } = await axios.get('/api/appointments/current')
    currentAppointments.value = data
  } catch (err) {
    console.error('获取当前预约失败', err)
  } finally {
    loading.value = false
  }
}

// 获取历史预约
async function fetchHistoryAppointments() {
  loading.value = true
  try {
    const { data } = await axios.get('/api/appointments/history')
    historyAppointments.value = data
  } catch (err) {
    console.error('获取历史预约失败', err)
  } finally {
    loading.value = false
  }
}

// 切换状态时加载对应数据
async function loadAppointments() {
  if (activeStatus.value === 'current' && currentAppointments.value.length === 0) {
    await fetchCurrentAppointments()
  } else if (activeStatus.value === 'history' && historyAppointments.value.length === 0) {
    await fetchHistoryAppointments()
  }
}

// 监听状态切换
const unwatchStatus = computed(() => activeStatus.value)
unwatchStatus.value // 触发计算
const originalActiveStatus = activeStatus.value
if (activeStatus.value !== originalActiveStatus) {
  loadAppointments()
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '未指定日期'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'short'
  })
}

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

// 改约预约
function rescheduleAppointment(appt) {
  // 这里可以跳转到改约页面并预填信息
  console.log('改约预约:', appt)
  alert('跳转到改约页面...')
}

// 取消预约
async function cancelAppointment(id) {
  if (!confirm('确定要取消这个预约吗？')) return
  
  try {
    await axios.post(`/api/appointments/${id}/cancel`)
    alert('预约已取消')
    await fetchCurrentAppointments()
  } catch (err) {
    alert('取消失败，请重试')
    console.error('取消预约失败', err)
  }
}

// 再次预约
function rebookAppointment(appt) {
  // 这里可以跳转到预约页面并预填信息
  console.log('再次预约:', appt)
  alert('跳转到预约页面...')
}

onMounted(() => {
  fetchCurrentAppointments()
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