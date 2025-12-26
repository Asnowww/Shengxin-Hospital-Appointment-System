<template>
  <div class="audit-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2>医生简介修改审核</h2>
        <p class="subtitle">审核医生擅长领域修改申请</p>
      </div>
    </div>
    
    <!-- 标签页筛选 -->
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.value" 
        @click="activeTab = tab.value" 
        :class="['tab-btn', { active: activeTab === tab.value }]"
      >
        {{ tab.label }}
        <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>
    
    <!-- 列表区域 -->
    <div class="audit-list">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="filteredRequests.length === 0" class="empty-state">
        <svg xmlns="https://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <p>暂无{{ getEmptyMessage() }}</p>
      </div>
      
      <table v-else class="audit-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>医生姓名</th>
            <th>原简介</th>
            <th>新简介</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in filteredRequests" :key="req.id">
            <td class="cell-strong">#{{ req.id }}</td>
            <td>{{ req.doctorName }}</td>
            <td class="bio-cell" :title="req.oldBio">{{ truncate(req.oldBio, 20) }}</td>
            <td class="bio-cell highlight" :title="req.newBio">{{ truncate(req.newBio, 20) }}</td>
            <td>
              <span :class="['status-badge', req.status]">
                {{ getStatusText(req.status) }}
              </span>
            </td>
            <td>{{ formatDate(req.createdAt) }}</td>
            <td>
              <div v-if="req.status === 'pending'" class="action-buttons">
                <button class="view-btn" @click="viewDetail(req.id)">
                  <svg xmlns="https://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  查看
                </button>
              </div>
              <div v-else class="reviewed-info">
                <span v-if="req.reviewedAt">{{ formatDate(req.reviewedAt) }}</span>
                <span v-if="req.status === 'rejected' && req.reason" class="rejection-reason">
                  原因: {{ req.reason }}
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情弹窗 -->
    <transition name="modal">
      <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
        <div class="modal-content">
          <div class="modal-header">
            <h3>申请详情 #{{ currentRequest?.id }}</h3>
            <button @click="closeDetail" class="modal-close">
              <svg xmlns="https://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body" v-if="currentRequest">
            <div class="info-group">
              <label>医生姓名</label>
              <div class="info-value">{{ currentRequest.doctorName }}</div>
            </div>
            <div class="info-group">
              <label>原简介</label>
              <div class="info-value old-bio">{{ currentRequest.oldBio }}</div>
            </div>
            <div class="info-group">
              <label>新简介</label>
              <div class="info-value new-bio">{{ currentRequest.newBio }}</div>
            </div>
            <div class="info-group">
              <label>申请时间</label>
              <div class="info-value">{{ formatDate(currentRequest.createdAt) }}</div>
            </div>
            <div class="info-group">
              <label>审批意见（拒绝时必填）</label>
              <textarea v-model="reviewReason" rows="3" placeholder="请输入审批意见..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeDetail" class="cancel-btn">取消</button>
            <button @click="rejectRequest" class="reject-btn" :disabled="processing">
              {{ processing ? '处理中...' : '拒绝' }}
            </button>
            <button @click="approveRequest" class="approve-btn" :disabled="processing">
              {{ processing ? '处理中...' : '通过' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
    
    <!-- Toast 消息 -->
    <transition name="toast">
      <div v-if="showToast" :class="['toast', toastType]">
        <svg v-if="toastType === 'success'" xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
        <svg v-else xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 状态变量
const requests = ref([])
const loading = ref(true)
const activeTab = ref('pending')
const showDetail = ref(false)
const currentRequest = ref(null)
const reviewReason = ref('')
const processing = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 统计数据
const stats = computed(() => ({
  pending: requests.value.filter(r => r.status === 'pending').length,
  approved: requests.value.filter(r => r.status === 'approved').length,
  rejected: requests.value.filter(r => r.status === 'rejected').length
}))

// 标签页配置
const tabs = computed(() => [
  { label: '全部', value: 'all', count: requests.value.length },
  { label: '待审批', value: 'pending', count: stats.value.pending },
  { label: '已批准', value: 'approved', count: stats.value.approved },
  { label: '已拒绝', value: 'rejected', count: stats.value.rejected }
])

// 根据Tab筛选后的列表
const filteredRequests = computed(() => {
  if (activeTab.value === 'all') return requests.value
  return requests.value.filter(r => r.status === activeTab.value)
})

// 获取状态文本
function getStatusText(status) {
  const statusMap = { pending: '待审批', approved: '已批准', rejected: '已拒绝' }
  return statusMap[status] || status
}

// 获取空状态消息
function getEmptyMessage() {
  const messageMap = {
    all: '修改申请',
    pending: '待审批的申请',
    approved: '已批准的申请',
    rejected: '已拒绝的申请'
  }
  return messageMap[activeTab.value] || '数据'
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 截断文本
function truncate(text, maxLen) {
  if (!text) return '-'
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

// 显示Toast消息
function showToastMessage(message, type = 'success') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 3000)
}

// 获取所有申请列表
async function fetchAllRequests() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/admin/doctors/bio/list', {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      requests.value = data.data || []
    } else {
      requests.value = []
      showToastMessage(data?.message || '获取申请列表失败', 'error')
    }
  } catch (e) {
    console.error('获取申请列表失败', e)
    showToastMessage('获取申请列表失败，请检查网络连接', 'error')
  } finally {
    loading.value = false
  }
}

// 查看申请详情
async function viewDetail(requestId) {
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get(`/api/admin/doctors/bio/${requestId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      currentRequest.value = data.data
      reviewReason.value = ''
      showDetail.value = true
    } else {
      showToastMessage(data?.message || '获取申请详情失败', 'error')
    }
  } catch (e) {
    console.error('获取申请详情失败', e)
    showToastMessage('获取申请详情失败', 'error')
  }
}

// 通过申请
async function approveRequest() {
  if (!currentRequest.value) return
  if (!confirm('确认通过该申请吗？')) return
  
  processing.value = true
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('approved', 'true')
    
    const { data } = await axios.post(
      `/api/admin/doctors/bio/review/${currentRequest.value.id}`,
      formData,
      { headers: token ? { Authorization: `Bearer ${token}` } : {} }
    )
    if (data?.code === 200) {
      showToastMessage('审批通过')
      closeDetail()
      await fetchAllRequests()
    } else {
      showToastMessage(data?.msg || data?.message || '审批失败', 'error')
    }
  } catch (e) {
    console.error('审批失败', e)
    showToastMessage('审批失败，请稍后再试', 'error')
  } finally {
    processing.value = false
  }
}

// 拒绝申请
async function rejectRequest() {
  if (!currentRequest.value) return
  
  if (!reviewReason.value.trim()) {
    showToastMessage('请填写拒绝原因', 'error')
    return
  }
  
  if (!confirm('确认拒绝该申请吗？')) return
  
  processing.value = true
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('approved', 'false')
    formData.append('reason', reviewReason.value)
    
    const { data } = await axios.post(
      `/api/admin/doctors/bio/review/${currentRequest.value.id}`,
      formData,
      { headers: token ? { Authorization: `Bearer ${token}` } : {} }
    )
    if (data?.code === 200) {
      showToastMessage('已拒绝申请')
      closeDetail()
      await fetchAllRequests()
    } else {
      showToastMessage(data?.msg || data?.message || '操作失败', 'error')
    }
  } catch (e) {
    console.error('操作失败', e)
    showToastMessage('操作失败，请稍后再试', 'error')
  } finally {
    processing.value = false
  }
}

function closeDetail() {
  showDetail.value = false
  currentRequest.value = null
  reviewReason.value = ''
}

// 初始加载
onMounted(() => {
  fetchAllRequests()
})
</script>

<style scoped>
.audit-container {
  padding: 2rem;
  background: transparent;
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

/* 列表区域 */
.audit-list {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

/* 表格样式 */
.audit-table {
  width: 100%;
  border-collapse: collapse;
}

.audit-table th,
.audit-table td {
  padding: 1rem;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.audit-table th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.audit-table th:first-child {
  border-radius: 8px 0 0 0;
}

.audit-table th:last-child {
  border-radius: 0 8px 0 0;
}

.audit-table tbody tr {
  transition: all 0.3s ease;
}

.audit-table tbody tr:hover {
  background: #f7fafc;
}

.cell-strong {
  font-weight: 600;
  color: #667eea;
}

.bio-cell {
  font-size: 0.9rem;
  color: #4a5568;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bio-cell.highlight {
  color: #10b981;
  font-weight: 500;
}

/* 状态徽章 */
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
  color: #dc3545;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.view-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 已审核信息 */
.reviewed-info {
  font-size: 0.85rem;
  color: #718096;
}

.rejection-reason {
  display: block;
  margin-top: 0.25rem;
  color: #f56565;
  font-size: 0.8rem;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  width: 600px;
  max-width: 90vw;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.modal-close {
  border: none;
  background: transparent;
  cursor: pointer;
  color: white;
  padding: 4px;
  display: flex;
  transition: transform 0.3s;
}

.modal-close:hover {
  transform: rotate(90deg);
}

.modal-body {
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
  max-height: 60vh;
  overflow-y: auto;
}

.info-group {
  display: grid;
  gap: 0.5rem;
}

.info-group label {
  font-weight: 600;
  color: #4a5568;
  font-size: 0.9rem;
}

.info-value {
  padding: 0.75rem 1rem;
  background: #f7fafc;
  border-radius: 8px;
  color: #2d3748;
  border-left: 3px solid #e2e8f0;
}

.info-value.old-bio {
  border-left-color: #f87171;
  background: #fef2f2;
}

.info-value.new-bio {
  border-left-color: #34d399;
  background: #f0fdf4;
  font-weight: 500;
}

textarea {
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.3s;
}

textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 2px solid #f0f0f0;
  justify-content: flex-end;
  background: #f7fafc;
}

.cancel-btn,
.reject-btn,
.approve-btn {
  padding: 0.6rem 1.25rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: white;
  border: 2px solid #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #f7fafc;
}

.reject-btn {
  background: linear-gradient(135deg, #f87171 0%, #dc2626 100%);
  color: white;
}

.reject-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
}

.approve-btn {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.reject-btn:disabled,
.approve-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Toast 消息 */
.toast {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  color: white;
  font-weight: 500;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  z-index: 1001;
}

.toast.success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.toast.error {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>