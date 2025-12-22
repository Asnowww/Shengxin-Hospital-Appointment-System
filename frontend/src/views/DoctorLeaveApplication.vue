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
          <button class="btn ghost" @click="fetchScheduleOptions" :disabled="scheduleLoading">
            {{ scheduleLoading ? '刷新中…' : '刷新排班' }}
          </button>
        </div>

        <div class="form-grid">
          <div class="form-group full">
            <label>选择排班 <span class="required">*</span></label>
            <div v-if="scheduleLoading" class="loading">加载可请假的排班...</div>
            <div v-else-if="scheduleOptions.length === 0" class="empty">未来30天暂无可请假的排班</div>
            <div v-else class="schedule-grid">
              <label 
                v-for="item in scheduleOptions" 
                :key="item.scheduleId" 
                class="schedule-card">
                <input 
                  type="checkbox" 
                  :value="item.scheduleId" 
                  v-model="form.scheduleIds" />
                <div class="schedule-info">
                  <div class="schedule-date">{{ item.dateLabel }}</div>
                  <div class="schedule-time">{{ item.timeLabel }}</div>
                  <div class="schedule-meta">可预约：{{ item.availableText }}</div>
                </div>
              </label>
            </div>
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
              <div>请假排班</div>
              <div>原因</div>
              <div>状态</div>
              <div>提交时间</div>
            </div>
            <div class="row" v-for="it in history" :key="it.id">
              <div>{{ historyScheduleText(it) }}</div>
              <div class="reason">{{ it.reason }}</div>
              <div><span :class="['status-badge', it.status]">{{ statusText(it.status) }}</span></div>
              <div>{{ (it.appliedAt || '').toString().replace('T', ' ') }}</div>
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

const today = new Date().toISOString().split('T')[0]

const navRef = ref(null)
const navHeight = ref(110)

function updateNavHeight() {
  if (navRef.value?.$el) {
    const h = navRef.value.$el.offsetHeight
    navHeight.value = h + 30
  }
}

// --- 表单与状态 ---
const form = ref({ userId: null, scheduleIds: [], reason: '' })
const loading = ref(false)
const history = ref([])
const submitting = ref(false)
const scheduleOptions = ref([])
const scheduleLoading = ref(false)
const scheduleDetailsMap = ref(new Map()) // 存储排班ID到详情的映射

// helper: 从 localStorage 读 userId（返回 null 或 Number）
function getUserIdFromStorage() {
  const raw = localStorage.getItem('userId')
  if (!raw) return null
  const n = Number(raw)
  return Number.isNaN(n) ? raw : n
}

function statusText(s) {
  return s === 'approved' ? '已通过' : s === 'rejected' ? '已拒绝' : '待审批'
}

function formatTimeSlot(slot) {
  if (slot === 0) return '上午'
  if (slot === 1) return '下午'
  if (slot === 2) return '晚上'
  return '未指定'
}

function formatDateLabel(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weekdayMap = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdayMap[d.getDay()]
  return `${dateStr}（${weekday}）`
}

function mapScheduleOptions(list) {
  return list
    .filter(item => item.status !== 'cancelled')
    .filter(item => !item.leaveApplied)
    .map(item => {
      const patientCount = typeof item.bookedSlots === 'number'
        ? item.bookedSlots
        : (item.maxSlots ?? 0) - (item.availableSlots ?? 0)
      const available = typeof item.availableSlots === 'number'
        ? item.availableSlots
        : Math.max((item.maxSlots ?? 0) - patientCount, 0)
      return {
        scheduleId: item.scheduleId,
        dateLabel: formatDateLabel(item.workDate),
        timeLabel: item.timeSlotName || formatTimeSlot(item.timeSlot),
        availableText: item.maxSlots ? `${available} / ${item.maxSlots}` : '—',
        raw: item
      }
    })
}

async function fetchScheduleOptions() {
  scheduleLoading.value = true
  try {
    const startDate = today
    const end = new Date()
    end.setDate(end.getDate() + 30)
    const endDate = end.toISOString().split('T')[0]
    const { data } = await axios.get('/api/doctor/schedules/my', {
      params: { userId: getUserIdFromStorage(), startDate, endDate }
    })
    if (data?.code !== 200) throw new Error(data?.message || '获取排班失败')
    scheduleOptions.value = mapScheduleOptions(data.data || [])
  } catch (e) {
    console.error('fetchScheduleOptions error:', e)
    scheduleOptions.value = []
  } finally {
    scheduleLoading.value = false
  }
}

function historyScheduleText(it) {
  if (it.scheduleIds) {
    const ids = it.scheduleIds.split(',').filter(Boolean)
    const details = ids.map(id => {
      const detail = scheduleDetailsMap.value.get(Number(id.trim()))
      if (detail) {
        const date = new Date(detail.workDate)
        const dateStr = `${date.getMonth() + 1}月${date.getDate()}日`
        const timeSlot = detail.timeSlotName || formatTimeSlot(detail.timeSlot)
        return `${dateStr} ${timeSlot}`
      }
      return null
    }).filter(Boolean)
    
    if (details.length > 0) {
      return details.join('、')
    }
    return `已选 ${ids.length} 个排班`
  }
  return `${it.fromDate || ''} ~ ${it.toDate || ''}`
}

async function submit() {
  // 校验表单项
  if (!form.value.scheduleIds || form.value.scheduleIds.length === 0) { alert('请选择要请假的排班'); return }
  if (!form.value.reason || !form.value.reason.trim()) { alert('请输入请假事由'); return }
  
  // 确保 userId 已设置（从 form 或 localStorage）
  if (!form.value.userId) {
    form.value.userId = getUserIdFromStorage()
  }
  if (!form.value.userId) {
    alert('无法识别用户，请先登录或在本地保存 userId（localStorage）')
    return
  }

  try {
    submitting.value = true
    const token = localStorage.getItem('token')
    const payload = {
      ...form.value,
      scheduleIds: form.value.scheduleIds.map(id => Number(id))
    }
    await axios.post('/api/doctor/schedules/leave/apply', payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert('已提交，请等待审批')
     // 重置表单
     form.value.scheduleIds = []
    form.value.reason = ''
    // 刷新排班（避免已请假的还能再选）
    fetchScheduleOptions()
    // 提交后刷新历史（等待服务器处理可能需要一点时间）
    fetchHistory()
  } catch (e) {
    console.error(e)
    alert(e?.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function fetchHistory() {
  loading.value = true
  try {
    // 若 form 中没有 userId，尝试从 localStorage 获取并写回 form
    if (!form.value.userId) {
      form.value.userId = getUserIdFromStorage()
    }

    // 如果还是没有 userId：尝试使用 token 请求也可能返回当前用户历史（如果后端支持）
    const token = localStorage.getItem('token')
    const params = {
      userId:getUserIdFromStorage()
    }
    if (form.value.userId) params.userId = form.value.userId

    const { data } = await axios.get('/api/doctor/schedules/leave/history', {
      params,
    })

    if (data?.code === 200) {
      history.value = data.data || []
      
      // 获取所有排班详情
      scheduleDetailsMap.value.clear()
      const allScheduleIds = new Set()
      history.value.forEach(leave => {
        if (leave.scheduleIds) {
          leave.scheduleIds.split(',').forEach(id => {
            const numId = Number(id.trim())
            if (numId) allScheduleIds.add(numId)
          })
        }
      })
      
      if (allScheduleIds.size > 0) {
        const scheduleIdsArray = Array.from(allScheduleIds)
        try {
          const token = localStorage.getItem('token')
          const { data: detailData } = await axios.get('/api/doctor/schedules/batch-details', {
            params: { scheduleIds: scheduleIdsArray },
            headers: token ? { Authorization: `Bearer ${token}` } : {}
          })
          if (detailData?.code === 200) {
            const details = detailData.data || []
            details.forEach(detail => {
              scheduleDetailsMap.value.set(detail.scheduleId, detail)
            })
          }
        } catch (e) {
          console.error('获取排班详情失败', e)
        }
      }
    } else {
      console.warn('fetchHistory 返回非 200：', data)
      history.value = []
    }
  } catch (e) {
    console.error('fetchHistory error:', e)
    history.value = []
  } finally {
    loading.value = false
  }
}

// onMounted 时读取 userId、调整导航高度并首次拉取历史
onMounted(async () => {
  // 先从 localStorage 读取 userId，放到 form 中
  const uid = getUserIdFromStorage()
  if (uid) {
    form.value.userId = uid
  } else {
    // 可选：控制台提示（避免打扰用户 UI）
    console.warn('userId not found in localStorage')
  }

  // 更新导航高度（等 DOM 渲染）
  await nextTick()
  updateNavHeight()

  // 首次尝试拉取历史（如果没有 userId，但后端支持 token 识别，也会返回数据）
  fetchHistory()
  fetchScheduleOptions()
})
</script>


<style scoped>
.page-container { min-height: 100vh; }
.content { max-width: 1200px; margin: 0 auto; padding: 1.5rem; display: grid; gap: 1.5rem; }
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
.thead, .row { display: grid; grid-template-columns: 1.2fr 2fr 1fr 1.2fr; gap: 8px; align-items: center; }
.thead { background: #667eea; padding: 12px; border-radius: 10px; font-weight: 700; color: #fff; }
.row { background: #fff; border: 2px solid #e2e8f0; border-radius: 10px; padding: 12px; }
.status-badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 700; }
.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.approved { background: #d4edda; color: #28a745; }
.status-badge.rejected { background: #f8d7da; color: #721c24; }
.schedule-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 0.75rem; }
.schedule-card { display: grid; grid-template-columns: auto 1fr; gap: 0.75rem; align-items: center; padding: 0.75rem; border: 2px solid #e2e8f0; border-radius: 10px; background: #fff; cursor: pointer; }
.schedule-card input { width: 18px; height: 18px; }
.schedule-card:hover { border-color: #667eea; box-shadow: 0 6px 16px rgba(102, 126, 234, 0.15); }
.schedule-info { display: flex; flex-direction: column; gap: 4px; }
.schedule-date { font-weight: 700; color: #2d3748; }
.schedule-time { color: #4c51bf; font-weight: 600; }
.schedule-meta { color: #718096; font-size: 0.9rem; }
@media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
</style>


