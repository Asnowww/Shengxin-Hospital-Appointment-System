<template>
  <div class="page-container" >
    <div class="leave-layout">
     
      <main class="main-content">
        <div class="leave-management">
          <div class="page-header">
            <div class="header-content">
              <h2>请假审批</h2>
              <p class="subtitle">管理医生请假申请和排班变更</p>
            </div>
          </div>
          <div class="tabs">
            <button v-for="tab in tabs" :key="tab.value" @click="activeTab = tab.value" :class="['tab-btn', { active: activeTab === tab.value }]">
              {{ tab.label }}
              <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
            </button>
          </div>
          <div class="leave-list">
            <div v-if="loading" class="loading-state">
              <div class="spinner"></div>
              <p>加载中...</p>
            </div>
            <div v-else-if="filteredLeaves.length === 0" class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <p>暂无{{ getEmptyMessage() }}</p>
            </div>
            <div v-else class="leave-cards">
              <div v-for="leave in filteredLeaves" :key="leave.leaveId" class="leave-card">
                <div class="card-header">
                  <div class="doctor-info">
                    <div class="doctor-avatar">
                      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                    </div>
                    <div class="doctor-details">
                      <h3>{{ leave.doctorName }}</h3>
                      <p>{{ leave.deptName }}</p>
                    </div>
                  </div>
                  <span :class="['status-badge', leave.status]">{{ getStatusText(leave.status) }}</span>
                </div>
                <div class="card-body">
                  <div class="schedule-details">
                    <div class="schedule-header">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                      </svg>
                      <span class="schedule-title">请假时间段</span>
                      <span class="schedule-count">共{{ leave.affectedScheduleCount }}个班次</span>
                    </div>
                    <div class="schedule-list">
                      <div v-for="schedule in leave.scheduleInfos" :key="schedule.scheduleId" class="schedule-item">
                        <div class="schedule-date">{{ formatChineseDate(schedule.date) }}</div>
                        <span :class="['time-slot-badge', schedule.timeSlot === 0 ? 'morning' : 'afternoon']">
                          {{ getTimeSlotText(schedule.timeSlot) }}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="info-bar">
                    <div class="info-item-compact">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                      <span>申请人: {{ leave.appliedByName || leave.doctorName }}</span>
                    </div>
                    <div class="info-item-compact">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                      </svg>
                      <span>影响预约: {{ leave.affectedAppointmentCount }}人</span>
                    </div>
                    <div class="info-item-compact">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                      </svg>
                      <span>{{ formatDateTime(leave.appliedAt) }}</span>
                    </div>
                  </div>
                  <div class="reason-section">
                    <div class="reason-label">
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                      </svg>
                      请假原因
                    </div>
                    <div class="reason-text">{{ leave.reason || '未填写' }}</div>
                  </div>
                  <div v-if="leave.affectedAppointmentCount > 0 && leave.status === 'pending'" class="warning-banner">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                      <line x1="12" y1="9" x2="12" y2="13"></line>
                      <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    <span>批准此请假将影响 <strong>{{ leave.affectedAppointmentCount }}</strong> 个患者预约，请谨慎处理</span>
                  </div>
                  <div v-if="leave.status !== 'pending' && leave.reviewedAt" class="review-note">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <div class="review-info">
                      由 <strong>{{ leave.reviewedByName || '管理员' }}</strong> 于 {{ formatDateTime(leave.reviewedAt) }} 处理
                    </div>
                  </div>
                </div>
                <div v-if="leave.status === 'pending'" class="card-actions">
                  <button @click="openRejectModal(leave)" class="reject-btn" :disabled="processingId === leave.leaveId">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    {{ processingId === leave.leaveId && processingAction === 'reject' ? '处理中...' : '拒绝' }}
                  </button>
                  <button @click="openApproveModal(leave)" class="approve-btn" :disabled="processingId === leave.leaveId">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    {{ processingId === leave.leaveId && processingAction === 'approve' ? '处理中...' : '批准' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
    <div v-if="showApproveModal" class="modal-overlay" @click="closeApproveModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认批准请假申请</h3>
          <button @click="closeApproveModal" class="modal-close">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="selectedLeave" class="modal-info">
            <p><strong>医生:</strong> {{ selectedLeave.doctorName }} - {{ selectedLeave.deptName }}</p>
            <p><strong>班次:</strong> {{ selectedLeave.affectedScheduleCount }}个</p>
            <p><strong>原因:</strong> {{ selectedLeave.reason || '未填写' }}</p>
            <div v-if="selectedLeave.affectedAppointmentCount > 0" class="modal-warning">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <span>此操作将影响 {{ selectedLeave.affectedAppointmentCount }} 个患者预约</span>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">审批备注(可选)</label>
            <textarea v-model="approveNote" class="form-textarea" rows="3" placeholder="请输入审批备注..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeApproveModal" class="cancel-btn">取消</button>
          <button @click="handleApprove" class="confirm-approve-btn" :disabled="processing">
            {{ processing ? '处理中...' : '确认批准' }}
          </button>
        </div>
      </div>
    </div>
    <div v-if="showRejectModal" class="modal-overlay" @click="closeRejectModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认拒绝请假申请</h3>
          <button @click="closeRejectModal" class="modal-close">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="selectedLeave" class="modal-info">
            <p><strong>医生:</strong> {{ selectedLeave.doctorName }} - {{ selectedLeave.deptName }}</p>
            <p><strong>班次:</strong> {{ selectedLeave.affectedScheduleCount }}个</p>
            <p><strong>原因:</strong> {{ selectedLeave.reason || '未填写' }}</p>
          </div>
          <div class="form-group">
            <label class="form-label">拒绝原因 <span class="required">*</span></label>
            <textarea v-model="rejectNote" class="form-textarea" :class="{ error: rejectError }" rows="3" placeholder="请说明拒绝原因..."></textarea>
            <span v-if="rejectError" class="error-text">{{ rejectError }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeRejectModal" class="cancel-btn">取消</button>
          <button @click="handleReject" class="confirm-reject-btn" :disabled="processing">
            {{ processing ? '处理中...' : '确认拒绝' }}
          </button>
        </div>
      </div>
    </div>
    <transition name="toast">
      <div v-if="showToast" :class="['toast', toastType]">
        <svg v-if="toastType === 'success'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
        <span>{{ toastMessage }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const activeTab = ref('pending')
const loading = ref(true)
const processingId = ref(null)
const processingAction = ref(null)
const processing = ref(false)
const showApproveModal = ref(false)
const showRejectModal = ref(false)
const selectedLeave = ref(null)
const approveNote = ref('')
const rejectNote = ref('')
const rejectError = ref('')
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')
const leaves = ref([])

const stats = computed(() => ({
  pending: leaves.value.filter(l => l.status === 'pending').length,
  approved: leaves.value.filter(l => l.status === 'approved').length,
  rejected: leaves.value.filter(l => l.status === 'rejected').length
}))

const tabs = computed(() => [
  { label: '全部', value: 'all', count: leaves.value.length },
  { label: '待审批', value: 'pending', count: stats.value.pending },
  { label: '已批准', value: 'approved', count: stats.value.approved },
  { label: '已拒绝', value: 'rejected', count: stats.value.rejected }
])

const filteredLeaves = computed(() => {
  if (activeTab.value === 'all') return leaves.value
  return leaves.value.filter(leave => leave.status === activeTab.value)
})

function getTimeSlotText(timeSlot) {
  return timeSlot === 0 ? '上午' : '下午'
}

function formatChineseDate(dateStr) {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const weekday = weekdays[date.getDay()]
  return `${month}月${day}日 周${weekday}`
}

function formatDateTime(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getStatusText(status) {
  const statusMap = { pending: '待审批', approved: '已批准', rejected: '已拒绝' }
  return statusMap[status] || status
}

function getEmptyMessage() {
  const messageMap = {
    all: '请假申请',
    pending: '待审批的申请',
    approved: '已批准的申请',
    rejected: '已拒绝的申请'
  }
  return messageMap[activeTab.value] || '数据'
}

function openApproveModal(leave) {
  selectedLeave.value = leave
  approveNote.value = ''
  showApproveModal.value = true
}

function closeApproveModal() {
  showApproveModal.value = false
  selectedLeave.value = null
  approveNote.value = ''
}

function openRejectModal(leave) {
  selectedLeave.value = leave
  rejectNote.value = ''
  rejectError.value = ''
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  selectedLeave.value = null
  rejectNote.value = ''
  rejectError.value = ''
}

function showToastMessage(message, type = 'success') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 3000)
}

async function handleApprove() {
  if (!selectedLeave.value) return
  processing.value = true
  processingId.value = selectedLeave.value.leaveId
  processingAction.value = 'approve'
  try {
    const reviewedBy = parseInt(localStorage.getItem('userId')) || 1
    const response = await axios.put('/api/admin/leaves/review', {
      leaveId: selectedLeave.value.leaveId,
      action: 'approve',
      reviewedBy: reviewedBy
    })
    if (response.data.code === 200) {
      showToastMessage('已批准该请假申请')
      closeApproveModal()
      await fetchLeaves()
    } else {
      showToastMessage(response.data.message || '操作失败', 'error')
    }
  } catch (err) {
    console.error('批准失败', err)
    showToastMessage(err.response?.data?.message || '操作失败,请重试', 'error')
  } finally {
    processing.value = false
    processingId.value = null
    processingAction.value = null
  }
}

async function handleReject() {
  if (!selectedLeave.value) return
  if (!rejectNote.value.trim()) {
    rejectError.value = '请输入拒绝原因'
    return
  }
  rejectError.value = ''
  processing.value = true
  processingId.value = selectedLeave.value.leaveId
  processingAction.value = 'reject'
  try {
    const reviewedBy = parseInt(localStorage.getItem('userId')) || 1
    const response = await axios.put('/api/admin/leaves/review', {
      leaveId: selectedLeave.value.leaveId,
      action: 'reject',
      reviewedBy: reviewedBy
    })
    if (response.data.code === 200) {
      showToastMessage('已拒绝该请假申请')
      closeRejectModal()
      await fetchLeaves()
    } else {
      showToastMessage(response.data.message || '操作失败', 'error')
    }
  } catch (err) {
    console.error('拒绝失败', err)
    showToastMessage(err.response?.data?.message || '操作失败,请重试', 'error')
  } finally {
    processing.value = false
    processingId.value = null
    processingAction.value = null
  }
}

async function fetchLeaves() {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/leaves/list')
    if (response.data.code === 200) {
      leaves.value = response.data.data || []
    } else {
      showToastMessage(response.data.message || '获取数据失败', 'error')
    }
  } catch (err) {
    console.error('获取请假列表失败', err)
    showToastMessage('获取数据失败,请刷新重试', 'error')
  } finally {
    loading.value = false
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
  fetchLeaves()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background:transparent;
}

.leave-layout {
  display: flex;
  min-height: calc(100vh - 110px);
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: white;
  border-right: 2px solid #e2e8f0;
  padding: 1.5rem 0;
}

.sidebar-header {
  padding: 0 1.5rem 1.5rem;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 1rem;
}

.avatar {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 1rem;
}

.sidebar-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 700;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0 1rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: #f7fafc;
  color: #667eea;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.nav-item svg {
  flex-shrink: 0;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 2rem;
  overflow-x: hidden;
}

.leave-management {
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.header-content h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.header-content .subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  border-radius: 12px;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-color: #ffc107;
  background: #fff3cd;
}

.stat-card.approved {
  border-color: #28a745;
  background: #d4edda;
}

.stat-card.rejected {
  border-color: #dc3545;
  background: #f8d7da;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card.pending .stat-icon {
  background: #ffc107;
  color: white;
}

.stat-card.approved .stat-icon {
  background: #28a745;
  color: white;
}

.stat-card.rejected .stat-icon {
  background: #dc3545;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.9rem;
  color: #718096;
  margin-top: 0.25rem;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  background: white;
  padding: 0.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1.25rem;
  background: transparent;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  background: #f7fafc;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tab-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.85rem;
}

.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
}

/* 加载和空状态 */
.loading-state,
.empty-state {
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

.empty-state svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

/* 请假卡片列表 */
.leave-list {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.leave-cards {
  display: grid;
  gap: 1.25rem;
}

.leave-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.leave-card:hover {
  border-color: #667eea;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.doctor-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.doctor-details h3 {
  margin: 0 0 0.25rem 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 700;
}

.doctor-details p {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.status-badge {
  display: inline-block;
  padding: 0.375rem 0.875rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background: #d4edda;
  color: #28a745;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

/* 卡片内容 */
.card-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-details {
  background: #f7fafc;
  border-radius: 10px;
  padding: 1rem;
  border: 1px solid #e2e8f0;
}

.schedule-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  color: #2d3748;
  font-weight: 600;
}

.schedule-title {
  flex: 1;
}

.schedule-count {
  color: #667eea;
  font-size: 0.9rem;
}

.schedule-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem;
}

.schedule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.625rem 0.875rem;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.schedule-date {
  color: #2d3748;
  font-weight: 500;
  font-size: 0.9rem;
}

.time-slot-badge {
  padding: 0.25rem 0.625rem;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
}

.time-slot-badge.morning {
  background: #e6f7ff;
  color: #0050b3;
}

.time-slot-badge.afternoon {
  background: #fff7e6;
  color: #ad6800;
}

.info-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 0.75rem 0;
}

.info-item-compact {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #718096;
  font-size: 0.9rem;
}

.info-item-compact svg {
  color: #667eea;
  flex-shrink: 0;
}

.reason-section {
  padding: 1rem;
  background: #f7fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.reason-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.reason-label svg {
  color: #667eea;
}

.reason-text {
  color: #4a5568;
  line-height: 1.6;
  font-size: 0.9rem;
}

.warning-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 10px;
  color: #856404;
  font-size: 0.9rem;
}

.warning-banner svg {
  flex-shrink: 0;
  color: #ffc107;
}

.warning-banner strong {
  font-weight: 700;
}

.review-note {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;
  background: #e6f7ff;
  border-radius: 10px;
  color: #0050b3;
  font-size: 0.9rem;
}

.review-note svg {
  flex-shrink: 0;
  color: #0050b3;
}

.review-info strong {
  font-weight: 700;
}

/* 卡片操作按钮 */
.card-actions {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 2px solid #f0f0f0;
  margin-top: 1rem;
}

.reject-btn,
.approve-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1.25rem;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.reject-btn {
  background: #fff;
  border: 2px solid #dc3545;
  color: #dc3545;
}

.reject-btn:hover:not(:disabled) {
  background: #dc3545;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.approve-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 2px solid transparent;
}

.approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.reject-btn:disabled,
.approve-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 模态框 */
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

.modal-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 700;
}

.modal-close {
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
}

.modal-close:hover {
  background: #e2e8f0;
  color: #2d3748;
}

.modal-body {
  padding: 2rem;
}

.modal-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 10px;
}

.modal-info p {
  margin: 0.5rem 0;
  color: #4a5568;
  font-size: 0.95rem;
}

.modal-info strong {
  color: #2d3748;
  font-weight: 600;
}

.modal-warning {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 10px;
  color: #856404;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.modal-warning svg {
  flex-shrink: 0;
  color: #ffc107;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-family: inherit;
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea.error {
  border-color: #dc3545;
}

.error-text {
  display: block;
  color: #dc3545;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.required {
  color: #e53e3e;
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1.5rem 2rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn,
.confirm-approve-btn,
.confirm-reject-btn {
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

.confirm-approve-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.confirm-approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.confirm-reject-btn {
  background: #dc3545;
  color: white;
}

.confirm-reject-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.3);
}

.cancel-btn:disabled,
.confirm-approve-btn:disabled,
.confirm-reject-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 提示消息 */
.toast {
  position: fixed;
  top: 100px;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  z-index: 2000;
  min-width: 280px;
}

.toast.success {
  border-left: 4px solid #28a745;
}

.toast.error {
  border-left: 4px solid #dc3545;
}

.toast svg {
  flex-shrink: 0;
}

.toast.success svg {
  color: #28a745;
}

.toast.error svg {
  color: #dc3545;
}

.toast span {
  color: #2d3748;
  font-weight: 500;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* 响应式 */
@media (max-width: 1024px) {
  .leave-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 2px solid #e2e8f0;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
    padding: 0.5rem 1rem;
  }

  .nav-item {
    white-space: nowrap;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .tabs {
    flex-wrap: wrap;
  }

  .tab-btn {
    min-width: calc(50% - 0.375rem);
  }

  .schedule-list {
    grid-template-columns: 1fr;
  }

  .info-bar {
    flex-direction: column;
    gap: 0.75rem;
  }

  .card-actions {
    flex-direction: column;
  }

  .modal-content {
    margin: 1rem;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1.25rem;
  }

  .toast {
    right: 1rem;
    left: 1rem;
    min-width: auto;
  }
}
</style>
