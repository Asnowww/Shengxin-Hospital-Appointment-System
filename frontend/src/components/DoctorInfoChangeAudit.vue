<template>
  <div class="audit-container">
    <div class="card">
      <div class="card-header">
        <div class="title-group">
          <h2>医生简介修改申请审核</h2>
          <p class="subtitle">审核医生擅长领域修改申请</p>
        </div>
        <div class="filters">
          <button class="btn primary" @click="fetchPendingRequests">
            <span>🔄</span> 刷新列表
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else>
        <div v-if="requests.length === 0" class="empty">暂无待审批申请</div>
        <div v-else class="table">
          <div class="thead">
            <div>申请ID</div>
            <div>医生姓名</div>
            <div>原简介</div>
            <div>新简介</div>
            <div>申请时间</div>
            <div>操作</div>
          </div>
          <div class="row" v-for="req in requests" :key="req.id">
            <div class="cell-strong">#{{ req.id }}</div>
            <div>{{ req.doctorName }}</div>
            <div class="bio-cell" :title="req.oldBio">{{ truncate(req.oldBio, 20) }}</div>
            <div class="bio-cell highlight" :title="req.newBio">{{ truncate(req.newBio, 20) }}</div>
            <div>{{ formatDate(req.createdAt) }}</div>
            <div class="actions">
              <button class="btn view" @click="viewDetail(req.id)">查看</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="modal">
      <div v-if="showDetail" class="modal" @click.self="closeDetail">
        <div class="dialog">
          <div class="dialog-header">
            <h3>申请详情 #{{ currentRequest?.id }}</h3>
            <button class="close" @click="closeDetail">✕</button>
          </div>
          <div class="detail-content" v-if="currentRequest">
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
          <div class="dialog-actions">
            <button class="btn ghost" @click="closeDetail">取消</button>
            <button class="btn danger" @click="rejectRequest">拒绝</button>
            <button class="btn success" @click="approveRequest">通过</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const requests = ref([])
const loading = ref(false)
const showDetail = ref(false)
const currentRequest = ref(null)
const reviewReason = ref('')

// 获取待审批申请列表
async function fetchPendingRequests() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/admin/doctors/bio/pending', {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      requests.value = data.data || []
    } else {
      requests.value = []
      alert(data?.message || '获取申请列表失败')
    }
  } catch (e) {
    console.error('获取申请列表失败', e)
    alert('获取申请列表失败，请检查网络连接')
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
      alert(data?.message || '获取申请详情失败')
    }
  } catch (e) {
    console.error('获取申请详情失败', e)
    alert('获取申请详情失败')
  }
}

// 通过申请
async function approveRequest() {
  if (!currentRequest.value) return
  
  if (!confirm('确认通过该申请吗？')) return
  
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('approved', 'true')
    
    const { data } = await axios.post(
      `/api/admin/doctors/bio/review/${currentRequest.value.id}`,
      formData,
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      }
    )
    if (data?.code === 200) {
      alert(data?.msg || '审批通过')
      closeDetail()
      fetchPendingRequests()
    } else {
      alert(data?.msg || data?.message || '审批失败')
    }
  } catch (e) {
    console.error('审批失败', e)
    alert('审批失败，请稍后再试')
  }
}

// 拒绝申请
async function rejectRequest() {
  if (!currentRequest.value) return
  
  if (!reviewReason.value.trim()) {
    alert('请填写拒绝原因')
    return
  }
  
  if (!confirm('确认拒绝该申请吗？')) return
  
  try {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('approved', 'false')
    formData.append('reason', reviewReason.value)
    
    const { data } = await axios.post(
      `/api/admin/doctors/bio/review/${currentRequest.value.id}`,
      formData,
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      }
    )
    if (data?.code === 200) {
      alert(data?.msg || '已拒绝申请')
      closeDetail()
      fetchPendingRequests()
    } else {
      alert(data?.msg || data?.message || '操作失败')
    }
  } catch (e) {
    console.error('操作失败', e)
    alert('操作失败,请稍后再试')
  }
}

function closeDetail() {
  showDetail.value = false
  currentRequest.value = null
  reviewReason.value = ''
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

// 初始加载
fetchPendingRequests()
</script>

<style scoped>
.audit-container { padding: 2rem; background: #f7fafc; min-height: 100vh; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #f0f0f0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.title-group h2 { margin: 0; color: #fff; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: .25rem 0 0 0; color: rgba(255,255,255,0.9); font-size: .9rem; }
.filters { display: flex; gap: .5rem; align-items: center; }

.btn { padding: 8px 16px; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; transition: all .2s; display: flex; align-items: center; gap: 4px; }
.btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.btn.primary { background: #fff; color: #667eea; }
.btn.view { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; font-size: 0.85rem; padding: 6px 12px; }
.btn.ghost { background: #fff; border: 2px solid #e2e8f0; color: #4a5568; }
.btn.success { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); color: #fff; }
.btn.danger { background: linear-gradient(135deg, #f87171 0%, #dc2626 100%); color: #fff; }

.loading, .empty { padding: 3rem; color: #888; text-align: center; font-size: 1.1rem; }
.table { display: grid; gap: 10px; padding: 1rem 1.5rem 1.5rem; }
.thead, .row { display: grid; grid-template-columns: 0.8fr 0.8fr 1.5fr 1.5fr 1.2fr 1fr; gap: 12px; align-items: center; }
.thead { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 14px 16px; border-radius: 10px; font-weight: 700; color: #fff; font-size: 0.9rem; }
.row { background: #fff; border: 2px solid #e2e8f0; border-radius: 10px; padding: 14px 16px; transition: all .2s; }
.row:hover { border-color: #667eea; box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15); transform: translateY(-2px); }
.cell-strong { font-weight: 600; color: #667eea; }
.bio-cell { font-size: 0.9rem; color: #4a5568; }
.bio-cell.highlight { color: #10b981; font-weight: 500; }
.actions { display: flex; gap: 8px; justify-content: center; }

/* Modal */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,.6); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(4px); }
.dialog { background: #fff; width: 600px; max-width: 90vw; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,.3); overflow: hidden; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 2px solid #f0f0f0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.dialog-header h3 { margin: 0; font-size: 1.2rem; }
.close { border: none; background: transparent; font-size: 24px; cursor: pointer; color: #fff; line-height: 1; }
.close:hover { transform: rotate(90deg); transition: .3s; }

.detail-content { padding: 20px; display: grid; gap: 16px; max-height: 60vh; overflow-y: auto; }
.info-group { display: grid; gap: 6px; }
.info-group label { font-weight: 600; color: #4a5568; font-size: 0.9rem; }
.info-value { padding: 10px 12px; background: #f7fafc; border-radius: 8px; color: #2d3748; border-left: 3px solid #e2e8f0; }
.info-value.old-bio { border-left-color: #f87171; background: #fef2f2; }
.info-value.new-bio { border-left-color: #34d399; background: #f0fdf4; font-weight: 500; }
textarea { border: 2px solid #e2e8f0; border-radius: 8px; padding: 10px 12px; font-family: inherit; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }

.dialog-actions { display: flex; gap: 10px; padding: 16px 20px; border-top: 2px solid #f0f0f0; justify-content: flex-end; background: #f7fafc; }

.modal-enter-active, .modal-leave-active { transition: opacity .3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-active .dialog, .modal-leave-active .dialog { transition: transform .3s ease; }
.modal-enter-from .dialog, .modal-leave-to .dialog { transform: scale(0.9); }
</style>