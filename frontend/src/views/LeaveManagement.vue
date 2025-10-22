<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="leave-layout">
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
        <div class="leave-management">
          <!-- 页面标题和统计 -->
          <div class="page-header">
            <div class="header-content">
              <h2>请假审批</h2>
              <p class="subtitle">管理医生请假申请和排班变更</p>
            </div>
            <div class="stats-grid">
              <div class="stat-card pending">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.pending }}</div>
                  <div class="stat-label">待审批</div>
                </div>
              </div>
              <div class="stat-card approved">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.approved }}</div>
                  <div class="stat-label">已批准</div>
                </div>
              </div>
              <div class="stat-card rejected">
                <div class="stat-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="15" y1="9" x2="9" y2="15"></line>
                    <line x1="9" y1="9" x2="15" y2="15"></line>
                  </svg>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.rejected }}</div>
                  <div class="stat-label">已拒绝</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 筛选标签页 -->
          <div class="tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              @click="activeTab = tab.value"
              :class="['tab-btn', { active: activeTab === tab.value }]">
              {{ tab.label }}
              <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
            </button>
          </div>

          <!-- 请假申请列表 -->
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
              <div
                v-for="leave in filteredLeaves"
                :key="leave.id"
                class="leave-card">
                <!-- 卡片头部 -->
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
                      <p>{{ leave.department }} - {{ leave.title }}</p>
                    </div>
                  </div>
                  <span :class="['status-badge', leave.status]">
                    {{ getStatusText(leave.status) }}
                  </span>
                </div>

                <!-- 请假信息 -->
                <div class="card-body">
                  <div class="info-row">
                    <div class="info-item">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                      </svg>
                      <div>
                        <div class="info-label">请假日期</div>
                        <div class="info-value">{{ leave.date }}</div>
                      </div>
                    </div>
                    <div class="info-item">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                      </svg>
                      <div>
                        <div class="info-label">请假时间</div>
                        <div class="info-value">{{ leave.timeSlot }}</div>
                      </div>
                    </div>
                    <div class="info-item">
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                      </svg>
                      <div>
                        <div class="info-label">影响预约数</div>
                        <div class="info-value">{{ leave.affectedAppointments }}人</div>
                      </div>
                    </div>
                  </div>

                  <div class="reason-section">
                    <div class="reason-label">请假原因</div>
                    <div class="reason-text">{{ leave.reason }}</div>
                  </div>

                  <!-- 警告信息 -->
                  <div v-if="leave.affectedAppointments > 0 && leave.status === 'pending'" class="warning-banner">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                      <line x1="12" y1="9" x2="12" y2="13"></line>
                      <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    <span>批准此请假将影响 {{ leave.affectedAppointments }} 个患者预约，请谨慎处理</span>
                  </div>

                  <!-- 申请时间 -->
                  <div class="request-time">
                    申请时间：{{ leave.requestTime }}
                  </div>

                  <!-- 审批备注（如果有） -->
                  <div v-if="leave.reviewNote" class="review-note">
                    <div class="review-note-label">审批备注</div>
                    <div class="review-note-text">{{ leave.reviewNote }}</div>
                    <div class="review-info">
                      由 {{ leave.reviewerName }} 于 {{ leave.reviewTime }} 处理
                    </div>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div v-if="leave.status === 'pending'" class="card-actions">
                  <button
                    @click="openRejectModal(leave)"
                    class="reject-btn"
                    :disabled="processingId === leave.id">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    {{ processingId === leave.id && processingAction === 'reject' ? '处理中...' : '拒绝' }}
                  </button>
                  <button
                    @click="openApproveModal(leave)"
                    class="approve-btn"
                    :disabled="processingId === leave.id">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    {{ processingId === leave.id && processingAction === 'approve' ? '处理中...' : '批准' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 批准确认模态框 -->
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
            <p><strong>医生：</strong>{{ selectedLeave.doctorName }} - {{ selectedLeave.department }}</p>
            <p><strong>日期：</strong>{{ selectedLeave.date }} {{ selectedLeave.timeSlot }}</p>
            <p><strong>原因：</strong>{{ selectedLeave.reason }}</p>
            <div v-if="selectedLeave.affectedAppointments > 0" class="modal-warning">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <span>此操作将影响 {{ selectedLeave.affectedAppointments }} 个患者预约</span>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">审批备注（可选）</label>
            <textarea
              v-model="approveNote"
              class="form-textarea"
              rows="3"
              placeholder="请输入审批备注..."
            ></textarea>
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

    <!-- 拒绝确认模态框 -->
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
            <p><strong>医生：</strong>{{ selectedLeave.doctorName }} - {{ selectedLeave.department }}</p>
            <p><strong>日期：</strong>{{ selectedLeave.date }} {{ selectedLeave.timeSlot }}</p>
            <p><strong>原因：</strong>{{ selectedLeave.reason }}</p>
          </div>
          <div class="form-group">
            <label class="form-label">
              拒绝原因 <span class="required">*</span>
            </label>
            <textarea
              v-model="rejectNote"
              class="form-textarea"
              :class="{ error: rejectError }"
              rows="3"
              placeholder="请说明拒绝原因..."
            ></textarea>
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

    <!-- 成功提示 -->
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
import { reactive, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const activeTab = ref('all')
const loading = ref(true)
const processingId = ref(null)
const processingAction = ref(null)
const processing = ref(false)

// 模态框状态
const showApproveModal = ref(false)
const showRejectModal = ref(false)
const selectedLeave = ref(null)
const approveNote = ref('')
const rejectNote = ref('')
const rejectError = ref('')

// Toast提示
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 请假申请列表
const leaves = ref([])

// 统计数据
const stats = computed(() => ({
  pending: leaves.value.filter(l => l.status === 'pending').length,
  approved: leaves.value.filter(l => l.status === 'approved').length,
  rejected: leaves.value.filter(l => l.status === 'rejected').length
}))

// 标签页配置
const tabs = computed(() => [
  { label: '全部', value: 'all', count: leaves.value.length },
  { label: '待审批', value: 'pending', count: stats.value.pending },
  { label: '已批准', value: 'approved', count: stats.value.approved },
  { label: '已拒绝', value: 'rejected', count: stats.value.rejected }
])

// 筛选后的请假列表
const filteredLeaves = computed(() => {
  if (activeTab.value === 'all') {
    return leaves.value
  }
  return leaves.value.filter(leave => leave.status === activeTab.value)
})

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    pending: '待审批',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

// 获取空状态消息
function getEmptyMessage() {
  const messageMap = {
    all: '请假申请',
    pending: '待审批的申请',
    approved: '已批准的申请',
    rejected: '已拒绝的申请'
  }
  return messageMap[activeTab.value] || '数据'
}

// 打开批准模态框
function openApproveModal(leave) {
  selectedLeave.value = leave
  approveNote.value = ''
  showApproveModal.value = true
}

// 关闭批准模态框
function closeApproveModal() {
  showApproveModal.value = false
  selectedLeave.value = null
  approveNote.value = ''
}

// 打开拒绝模态框
function openRejectModal(leave) {
  selectedLeave.value = leave
  rejectNote.value = ''
  rejectError.value = ''
  showRejectModal.value = true
}

// 关闭拒绝模态框
function closeRejectModal() {
  showRejectModal.value = false
  selectedLeave.value = null
  rejectNote.value = ''
  rejectError.value = ''
}

// 显示提示
function showToastMessage(message, type = 'success') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

// 批准请假
async function handleApprove() {
  if (!selectedLeave.value) return

  processing.value = true
  processingId.value = selectedLeave.value.id
  processingAction.value = 'approve'

  try {
    // 实际使用时替换为：
    // await axios.post(`/api/admin/leaves/${selectedLeave.value.id}/approve`, {
    //   note: approveNote.value
    // })

    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 更新本地数据
    const leave = leaves.value.find(l => l.id === selectedLeave.value.id)
    if (leave) {
      leave.status = 'approved'
      leave.reviewNote = approveNote.value || '已批准'
      leave.reviewerName = '管理员'
      leave.reviewTime = new Date().toLocaleString('zh-CN')
    }

    showToastMessage('已批准该请假申请')
    closeApproveModal()
  } catch (err) {
    console.error('批准失败', err)
    showToastMessage('操作失败，请重试', 'error')
  } finally {
    processing.value = false
    processingId.value = null
    processingAction.value = null
  }
}

// 拒绝请假
async function handleReject() {
  if (!selectedLeave.value) return

  // 验证拒绝原因
  if (!rejectNote.value.trim()) {
    rejectError.value = '请输入拒绝原因'
    return
  }
  rejectError.value = ''

  processing.value = true
  processingId.value = selectedLeave.value.id
  processingAction.value = 'reject'

  try {
    // 实际使用时替换为：
    // await axios.post(`/api/admin/leaves/${selectedLeave.value.id}/reject`, {
    //   note: rejectNote.value
    // })

    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 更新本地数据
    const leave = leaves.value.find(l => l.id === selectedLeave.value.id)
    if (leave) {
      leave.status = 'rejected'
      leave.reviewNote = rejectNote.value
      leave.reviewerName = '管理员'
      leave.reviewTime = new Date().toLocaleString('zh-CN')
    }

    showToastMessage('已拒绝该请假申请')
    closeRejectModal()
  } catch (err) {
    console.error('拒绝失败', err)
    showToastMessage('操作失败，请重试', 'error')
  } finally {
    processing.value = false
    processingId.value = null
    processingAction.value = null
  }
}

// 获取请假列表
async function fetchLeaves() {
  loading.value = true
  try {
    // 实际使用时替换为：
    // const { data } = await axios.get('/api/admin/leaves')
    // leaves.value = data

    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 800))

    // 模拟数据
    leaves.value = [
      {
        id: 1,
        doctorName: '张医生',
        department: '内科',
        title: '主任医师',
        date: '2025-10-25',
        timeSlot: '上午 08:00-12:00',
        reason: '家中有急事需要处理',
        affectedAppointments: 8,
        status: 'pending',
        requestTime: '2025-10-22 09:30:00'
      },
      {
        id: 2,
        doctorName: '李医生',
        department: '外科',
        title: '副主任医师',
        date: '2025-10-26',
        timeSlot: '下午 14:00-17:00',
        reason: '参加学术会议',
        affectedAppointments: 5,
        status: 'pending',
        requestTime: '2025-10-22 10:15:00'
      },
      {
        id: 3,
        doctorName: '王医生',
        department: '儿科',
        title: '主治医师',
        date: '2025-10-24',
        timeSlot: '全天',
        reason: '身体不适需要休息',
        affectedAppointments: 12,
        status: 'approved',
        requestTime: '2025-10-21 16:20:00',
        reviewNote: '同意请假，注意休息',
        reviewerName: '管理员',
        reviewTime: '2025-10-21 17:00:00'
      },
      {
        id: 4,
        doctorName: '赵医生',
        department: '骨科',
        title: '主任医师',
        date: '2025-10-23',
        timeSlot: '上午 08:00-12:00',
        reason: '个人原因',
        affectedAppointments: 15,
        status: 'rejected',
        requestTime: '2025-10-20 14:30:00',
        reviewNote: '该时段预约较多，建议调整至其他时间',
        reviewerName: '管理员',
        reviewTime: '2025-10-20 15:45:00'
      },
      {
        id: 5,
        doctorName: '刘医生',
        department: '妇产科',
        title: '主治医师',
        date: '2025-10-27',
        timeSlot: '上午 08:00-12:00',
        reason: '培训学习',
        affectedAppointments: 0,
        status: 'pending',
        requestTime: '2025-10-22 11:00:00'
      },
      {
        id: 6,
        doctorName: '陈医生',
        department: '眼科',
        title: '副主任医师',
        date: '2025-10-28',
        timeSlot: '下午 14:00-17:00',
        reason: '科研项目需要',
        affectedAppointments: 3,
        status: 'approved',
        requestTime: '2025-10-21 09:00:00',
        reviewNote: '已批准',
        reviewerName: '管理员',
        reviewTime: '2025-10-21 10:30:00'
      }
    ]
  } catch (err) {
    console.error('获取请假列表失败', err)
    showToastMessage('获取数据失败，请刷新重试', 'error')
  } finally {
    loading.value = false
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
  fetchLeaves()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f8f9fa;
}

.leave-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
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
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  min-height: 600px;
  overflow: hidden;
}

.leave-management {
  height: 100%;
}

/* 页面头部 */
.page-header {
  padding: 2.5rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.header-content h2 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  font-weight: 600;
}

.subtitle {
  margin: 0 0 2rem 0;
  opacity: 0.95;
  font-size: 1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.stat-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 0.5rem;
  padding: 1.5rem 2.5rem;
  border-bottom: 2px solid #f0f0f0;
  overflow-x: auto;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  color: #4a5568;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  border-color: #f093fb;
  color: #f5576c;
}

.tab-btn.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.tab-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

/* 请假列表 */
.leave-list {
  padding: 2.5rem;
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
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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
  font-size: 1.1rem;
  margin: 0;
}

/* 请假卡片 */
.leave-cards {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.leave-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.leave-card:hover {
  border-color: #f093fb;
  box-shadow: 0 8px 24px rgba(240, 147, 251, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: #f8f9fa;
  border-bottom: 2px solid #e2e8f0;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.doctor-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.doctor-details h3 {
  margin: 0 0 0.25rem 0;
  color: #2d3748;
  font-size: 1.1rem;
  font-weight: 600;
}

.doctor-details p {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.card-body {
  padding: 1.5rem;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-item {
  display: flex;
  gap: 0.75rem;
}

.info-item svg {
  color: #f5576c;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.info-label {
  color: #718096;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
}

.info-value {
  color: #2d3748;
  font-weight: 600;
  font-size: 0.95rem;
}

.reason-section {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 1rem;
}

.reason-label {
  color: #718096;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.reason-text {
  color: #2d3748;
  font-size: 0.95rem;
  line-height: 1.6;
}

.warning-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  border-radius: 8px;
  color: #856404;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.warning-banner svg {
  flex-shrink: 0;
}

.request-time {
  color: #a0aec0;
  font-size: 0.85rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.review-note {
  margin-top: 1rem;
  padding: 1rem;
  background: #e6f7ff;
  border-left: 4px solid #1890ff;
  border-radius: 8px;
}

.review-note-label {
  color: #0050b3;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.review-note-text {
  color: #2d3748;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
}

.review-info {
  color: #69c0ff;
  font-size: 0.8rem;
}

.card-actions {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-top: 2px solid #e2e8f0;
}

.reject-btn,
.approve-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reject-btn {
  background: #fff;
  color: #e53e3e;
  border: 2px solid #e53e3e;
}

.reject-btn:hover:not(:disabled) {
  background: #e53e3e;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.3);
}

.approve-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: 2px solid transparent;
}

.approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.reject-btn:disabled,
.approve-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
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
  font-weight: 600;
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #718096;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: #f0f0f0;
  color: #2d3748;
}

.modal-body {
  padding: 2rem;
}

.modal-info {
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.modal-info p {
  margin: 0 0 0.75rem 0;
  color: #2d3748;
  font-size: 0.95rem;
  line-height: 1.6;
}

.modal-info p:last-child {
  margin-bottom: 0;
}

.modal-warning {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  border-radius: 8px;
  color: #856404;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.modal-warning svg {
  flex-shrink: 0;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #4a5568;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  font-family: inherit;
  resize: vertical;
  transition: all 0.3s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-textarea.error {
  border-color: #e53e3e;
}

.error-text {
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.25rem;
  display: block;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn {
  padding: 0.75rem 1.5rem;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.confirm-approve-btn,
.confirm-reject-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
}

.confirm-approve-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.confirm-approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.confirm-reject-btn {
  background: #e53e3e;
}

.confirm-reject-btn:hover:not(:disabled) {
  background: #c53030;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.4);
}

.confirm-approve-btn:disabled,
.confirm-reject-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Toast提示 */
.toast {
  position: fixed;
  top: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  color: white;
  font-weight: 500;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  z-index: 3000;
}

.toast.success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.toast.error {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .leave-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .sidebar {
    width: 240px;
  }

  .stats-grid {
    gap: 1rem;
  }

  .info-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .leave-layout {
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

  .page-header {
    padding: 1.5rem;
  }

  .header-content h2 {
    font-size: 1.5rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .tabs {
    padding: 1rem;
  }

  .leave-list {
    padding: 1.5rem;
  }

  .leave-cards {
    gap: 1rem;
  }

  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .status-badge {
    align-self: flex-start;
  }

  .info-row {
    grid-template-columns: 1fr;
  }

  .card-actions {
    flex-direction: column;
  }

  .reject-btn,
  .approve-btn {
    width: 100%;
  }

  .modal-content {
    margin: 1rem;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1rem;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .confirm-approve-btn,
  .confirm-reject-btn {
    width: 100%;
  }

  .toast {
    top: 1rem;
    right: 1rem;
    left: 1rem;
  }
}
</style>
