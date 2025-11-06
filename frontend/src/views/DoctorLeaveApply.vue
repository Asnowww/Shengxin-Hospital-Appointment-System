<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="content">
      <div class="card">
        <div class="card-header">
          <div class="title-group">
            <h2>请假申请</h2>
            <p class="subtitle">提交请假申请，待管理员审批</p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label>开始日期 <span class="required">*</span></label>
            <input v-model="form.fromDate" type="date" />
          </div>
          <div class="form-group">
            <label>结束日期 <span class="required">*</span></label>
            <input v-model="form.toDate" type="date" />
          </div>
          <div class="form-group full">
            <label>请假事由 <span class="required">*</span></label>
            <textarea v-model="form.reason" rows="3" placeholder="请输入请假原因"></textarea>
          </div>
        </div>

        <div class="actions">
          <button class="btn primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中…' : '提交申请' }}</button>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="title-group">
            <h2>我的请假记录</h2>
          </div>
          <button class="btn ghost" @click="fetchHistory">刷新</button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else>
          <div v-if="history.length === 0" class="empty">暂无记录</div>
          <div v-else class="table">
            <div class="thead">
              <div>开始</div>
              <div>结束</div>
              <div>原因</div>
              <div>状态</div>
            </div>
            <div class="row" v-for="it in history" :key="it.id">
              <div>{{ it.startDate || it.fromDate }}</div>
              <div>{{ it.endDate || it.toDate }}</div>
              <div class="reason">{{ it.reason }}</div>
              <div><span :class="['status-badge', it.status]">{{ statusText(it.status) }}</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const navRef = ref(null)
const navHeight = ref(110)

function updateNavHeight() {
  if (navRef.value?.$el) {
    const h = navRef.value.$el.offsetHeight
    navHeight.value = h + 30
  }
}

const form = ref({ doctorId: null, fromDate: '', toDate: '', reason: '' })
const loading = ref(false)
const history = ref([])
const submitting = ref(false)

function statusText(s) {
  return s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : '待审批'
}

async function submit() {
  // 校验表单项（逐项提示更友好）
  if (!form.value.fromDate) { alert('请选择开始日期'); return }
  if (!form.value.toDate) { alert('请选择结束日期'); return }
  if (!form.value.reason || !form.value.reason.trim()) { alert('请输入请假事由'); return }

  if (!form.value.doctorId) { await ensureDoctorId() }
  if (!form.value.doctorId) { alert('无法识别医生身份，请重新登录医生账号或稍后再试'); return }

  try {
    submitting.value = true
    const token = localStorage.getItem('token')
    await axios.post('/api/doctor/schedules/leave/apply', form.value, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert('已提交，请等待审批')
    fetchHistory()
  } catch (e) {
    console.error(e)
    alert(e?.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function fetchHistory() {
  if (!form.value.doctorId) {
    await ensureDoctorId()
    if (!form.value.doctorId) return
  }
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/doctor/schedules/leave/history', {
      params: { doctorId: form.value.doctorId },
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      history.value = data.data || []
    } else {
      history.value = []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function ensureDoctorId() {
  // 1) 本地已有缓存
  const cached = localStorage.getItem('doctorId')
  if (cached) { form.value.doctorId = Number(cached); return }

  // 2) 通过医生列表匹配用户名
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/doctor/list', {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    const username = localStorage.getItem('username') || localStorage.getItem('name')
    if (data?.code === 200 && Array.isArray(data.data)) {
      // 尝试按 name/username 匹配
      const hit = data.data.find(d => d.name === username || d.username === username)
      if (hit && (hit.id || hit.doctorId)) {
        form.value.doctorId = Number(hit.id || hit.doctorId)
        localStorage.setItem('doctorId', String(form.value.doctorId))
      }
    }
  } catch (e) {
    // 忽略获取失败
  }
}

onMounted(async () => {
  await nextTick(); updateNavHeight(); await ensureDoctorId()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f7fafc; }
.content { max-width: 1000px; margin: 0 auto; padding: 1.5rem; display: grid; gap: 1.5rem; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,.1); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #f0f0f0; }
.title-group h2 { margin: 0; color: #2d3748; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: .25rem 0 0 0; color: #718096; font-size: .9rem; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; padding: 1.25rem 1.5rem; }
.form-group { display: flex; flex-direction: column; gap: .5rem; }
.form-group.full { grid-column: 1 / -1; }
input, textarea { padding: 10px 12px; border: 2px solid #e2e8f0; border-radius: 8px; }
.required { color: #e53e3e; }
.actions { display: flex; justify-content: flex-end; gap: .75rem; padding: 0 1.5rem 1.25rem; }
.btn { padding: 10px 14px; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; }
.btn.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.btn.ghost { background: #fff; border: 2px solid #e2e8f0; color: #4a5568; }
.loading, .empty { padding: 1.5rem; text-align: center; color: #718096; }
.table { display: grid; gap: 8px; padding: 1rem 1.5rem 1.5rem; }
.thead, .row { display: grid; grid-template-columns: 1fr 1fr 2fr 1fr; gap: 8px; align-items: center; }
.thead { background: #667eea; padding: 12px; border-radius: 10px; font-weight: 700; color: #fff; }
.row { background: #fff; border: 2px solid #e2e8f0; border-radius: 10px; padding: 12px; }
.status-badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 700; }
.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.approved { background: #d4edda; color: #28a745; }
.status-badge.rejected { background: #f8d7da; color: #721c24; }
@media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
</style>


