<template>
  <div class="audit-container">
    <div class="card">
      <div class="card-header">
        <div class="title-group">
          <h2>请假审批</h2>
          <p class="subtitle">审批医生的请假申请</p>
        </div>
        <div class="filters">
          <select v-model="status" @change="fetchLeaves" class="select">
            <option value="">全部状态</option>
            <option value="pending">待审批</option>
            <option value="approved">已通过</option>
            <option value="rejected">已拒绝</option>
          </select>
          <button class="btn ghost" @click="fetchLeaves">刷新</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else>
        <div v-if="leaves.length === 0" class="empty">暂无请假申请</div>
        <div v-else class="table">
          <div class="thead">
            <div>医生</div>
            <div>科室</div>
            <div>开始日期</div>
            <div>结束日期</div>
            <div>原因</div>
            <div>状态</div>
            <div>操作</div>
          </div>
          <div class="row" v-for="item in leaves" :key="item.id">
            <div class="cell-strong">{{ item.doctorName }}</div>
            <div>{{ item.deptName }}</div>
            <div>{{ item.startDate }}</div>
            <div>{{ item.endDate }}</div>
            <div class="reason" :title="item.reason">{{ item.reason }}</div>
            <div>
              <span :class="['status-badge', item.status]">{{ statusText(item.status) }}</span>
            </div>
            <div class="actions">
              <button class="btn success" :disabled="item.status !== 'pending'" @click="review(item, 'approved')">通过</button>
              <button class="btn danger" :disabled="item.status !== 'pending'" @click="review(item, 'rejected')">拒绝</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
 </template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const leaves = ref([])
const loading = ref(false)
const status = ref('pending')

function statusText(s) {
  return s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : '待审批'
}

async function fetchLeaves() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    // 注意：后端控制器前缀是 "/api/admin/leaves"，vite 代理会把前缀 "/api" 重写掉，
    // 所以前端需要以 "/api/api/..." 访问，最终转发到后端 "/api/..."
    const { data } = await axios.get('/api/api/admin/leaves/list', {
      params: { status: status.value || undefined },
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      leaves.value = data.data || []
    } else {
      leaves.value = []
    }
  } catch (e) {
    console.error('获取请假列表失败', e)
    alert('获取请假列表失败')
  } finally {
    loading.value = false
  }
}

async function review(item, decision) {
  if (!confirm(`确定将【${item.doctorName}】的请假申请标记为${decision === 'approved' ? '通过' : '拒绝'}吗？`)) return
  try {
    const token = localStorage.getItem('token')
    const payload = { id: item.id, action: decision }
    await axios.put('/api/api/admin/leaves/review', payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert('操作成功')
    fetchLeaves()
  } catch (e) {
    console.error('审批失败', e)
    alert('审批失败，请稍后再试')
  }
}

onMounted(fetchLeaves)
</script>

<style scoped>
.audit-container { padding: 2rem; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #f0f0f0; }
.title-group h2 { margin: 0; color: #2d3748; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: .25rem 0 0 0; color: #718096; font-size: .9rem; }
.filters { display: flex; gap: .5rem; align-items: center; }
.select { padding: 8px 10px; border: 2px solid #e2e8f0; border-radius: 8px; }
.btn { padding: 8px 12px; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.btn.ghost { background: #fff; border: 2px solid #e2e8f0; color: #4a5568; }
.btn.success { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); color: #fff; }
.btn.danger { background: linear-gradient(135deg, #f97393 0%, #ef4444 100%); color: #fff; }
.btn:disabled { opacity: .5; cursor: not-allowed; }

.loading, .empty { padding: 2rem; color: #888; text-align: center; }
.table { display: grid; gap: 8px; padding: 1rem 1.5rem 1.5rem; }
.thead, .row { display: grid; grid-template-columns: 1.2fr 1fr 1fr 1fr 2fr 0.8fr 1.2fr; gap: 8px; align-items: center; }
.thead { background: #667eea; padding: 12px; border-radius: 10px; font-weight: 700; color: #fff; }
.row { background: #fff; border: 2px solid #e2e8f0; border-radius: 10px; padding: 12px; transition: .2s; }
.row:hover { border-color: #cbd5e0; box-shadow: 0 4px 12px rgba(0,0,0,.05); }
.cell-strong { font-weight: 600; color: #2d3748; }
.reason { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.status-badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 700; }
.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.approved { background: #d4edda; color: #28a745; }
.status-badge.rejected { background: #f8d7da; color: #721c24; }
.actions { display: flex; gap: 8px; justify-content: center; }
</style>


