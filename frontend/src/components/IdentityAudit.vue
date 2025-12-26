<template>
  <div class="audit-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2>身份认证审核</h2>
        <p class="subtitle">管理患者身份认证申请</p>
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
      
      <div v-else-if="filteredVerifications.length === 0" class="empty-state">
        <svg xmlns="https://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <p>暂无{{ getEmptyMessage() }}</p>
      </div>
      
      <table v-else class="audit-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>用户账号</th>
            <th>身份证号</th>
            <th>身份证照片</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredVerifications" :key="item.verificationId">
            <td>{{ item.username }}</td>
            <td>{{ item.userAccount }}</td>
            <td>{{ maskIdNumber(item.idNumber) }}</td>
            <td>
              <img 
                :src="item.docUrl" 
                alt="身份证照片" 
                class="id-photo" 
                @click="previewImage(item.docUrl)"
              />
            </td>
            <td>
              <span :class="['status-badge', item.status]">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>{{ formatDateTime(item.createdAt) }}</td>
            <td>
              <div v-if="item.status === 'pending'" class="action-buttons">
                <button 
                  @click="review(item.verificationId, 'approved')" 
                  class="approve-btn"
                  :disabled="processingId === item.verificationId"
                >
                  <svg xmlns="https://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  {{ processingId === item.verificationId ? '处理中...' : '通过' }}
                </button>
                <button 
                  @click="review(item.verificationId, 'rejected')" 
                  class="reject-btn"
                  :disabled="processingId === item.verificationId"
                >
                  <svg xmlns="https://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="15" y1="9" x2="9" y2="15"></line>
                    <line x1="9" y1="9" x2="15" y2="15"></line>
                  </svg>
                  拒绝
                </button>
              </div>
              <div v-else class="reviewed-info">
                <span v-if="item.reviewedAt">{{ formatDateTime(item.reviewedAt) }} 审核</span>
                <span v-if="item.status === 'rejected' && item.rejectionReason" class="rejection-reason">
                  原因: {{ item.rejectionReason }}
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 图片预览弹窗 -->
    <div v-if="previewUrl" class="image-preview-overlay" @click="previewUrl = null">
      <div class="image-preview-content" @click.stop>
        <img :src="previewUrl" alt="身份证照片预览" />
        <button @click="previewUrl = null" class="close-preview-btn">
          <svg xmlns="https://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
    </div>
    
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
const verifications = ref([])
const loading = ref(true)
const activeTab = ref('pending')
const processingId = ref(null)
const previewUrl = ref(null)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// 统计数据
const stats = computed(() => ({
  pending: verifications.value.filter(v => v.status === 'pending').length,
  approved: verifications.value.filter(v => v.status === 'approved').length,
  rejected: verifications.value.filter(v => v.status === 'rejected').length
}))

// 标签页配置
const tabs = computed(() => [
  { label: '全部', value: 'all', count: verifications.value.length },
  { label: '待审批', value: 'pending', count: stats.value.pending },
  { label: '已批准', value: 'approved', count: stats.value.approved },
  { label: '已拒绝', value: 'rejected', count: stats.value.rejected }
])

// 根据Tab筛选后的列表
const filteredVerifications = computed(() => {
  if (activeTab.value === 'all') return verifications.value
  return verifications.value.filter(v => v.status === activeTab.value)
})

// 获取状态文本
function getStatusText(status) {
  const statusMap = { pending: '待审批', approved: '已批准', rejected: '已拒绝' }
  return statusMap[status] || status
}

// 获取空状态消息
function getEmptyMessage() {
  const messageMap = {
    all: '认证申请',
    pending: '待审批的申请',
    approved: '已批准的申请',
    rejected: '已拒绝的申请'
  }
  return messageMap[activeTab.value] || '数据'
}

// 格式化日期时间
function formatDateTime(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 遮蔽身份证号
function maskIdNumber(idNumber) {
  if (!idNumber) return ''
  if (idNumber.length <= 6) return idNumber
  return idNumber.substring(0, 3) + '****' + idNumber.substring(idNumber.length - 4)
}

// 预览图片
function previewImage(url) {
  previewUrl.value = url
}

// 显示Toast消息
function showToastMessage(message, type = 'success') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 3000)
}

// 拉取所有认证记录
async function fetchVerifications() {
  loading.value = true
  try {
    const res = await axios.get('/api/verifications/list')
    if (res.data.code === 200) {
      verifications.value = res.data.data.map(item => ({
        verificationId: item.verificationId,
        username: item.userName || item.username,
        userAccount: item.userAccount || item.userId,
        idNumber: item.idNumber,
        docUrl: item.docUrl,
        status: item.status,
        createdAt: item.createdAt,
        reviewedAt: item.reviewedAt,
        rejectionReason: item.rejectionReason
      }))
    } else {
      showToastMessage(res.data.message || '获取数据失败', 'error')
    }
  } catch (err) {
    console.error('获取认证列表失败', err)
    showToastMessage('获取数据失败，请刷新重试', 'error')
  } finally {
    loading.value = false
  }
}

// 审核操作
async function review(verificationId, status) {
  let reason = null
  if (status === 'rejected') {
    reason = prompt('请输入拒绝理由')
    if (reason === null) return // 用户取消
    if (!reason.trim()) {
      showToastMessage('请输入拒绝理由', 'error')
      return
    }
  }
  
  processingId.value = verificationId
  try {
    const token = localStorage.getItem('token')
    if (!verificationId) {
      showToastMessage('缺少审核记录ID', 'error')
      return
    }
    
    const formData = new URLSearchParams()
    formData.append('verificationId', verificationId)
    formData.append('result', status)
    if (reason) formData.append('reason', reason)

    const res = await axios.post('/api/verifications/review', formData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    if (res.data.code === 200) {
      showToastMessage(status === 'approved' ? '已批准该认证申请' : '已拒绝该认证申请')
      await fetchVerifications()
    } else {
      showToastMessage(res.data.message || '操作失败', 'error')
    }
  } catch (err) {
    console.error('审核失败', err)
    showToastMessage(err.response?.data?.message || '审核失败，请重试', 'error')
  } finally {
    processingId.value = null
  }
}

onMounted(() => {
  fetchVerifications()
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

.id-photo {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.id-photo:hover {
  transform: scale(1.1);
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

.approve-btn,
.reject-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.approve-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.approve-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.reject-btn {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
}

.reject-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.4);
}

.approve-btn:disabled,
.reject-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

/* 图片预览 */
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.image-preview-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.image-preview-content img {
  max-width: 100%;
  max-height: 80vh;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.close-preview-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  transition: transform 0.3s ease;
}

.close-preview-btn:hover {
  transform: scale(1.1);
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
