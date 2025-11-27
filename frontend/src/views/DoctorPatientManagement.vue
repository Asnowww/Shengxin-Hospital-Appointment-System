<template>
  <Navigation ref="navRef" />
  <div class="page" :style="{ paddingTop: navHeight + 'px' }">
    <div class="page-head">
      <div>
        <h1>今日就诊队列</h1>
        <p class="sub">每30秒自动刷新</p>
      </div>
      <div class="filters">
        <div class="refresh-info">
          <span class="auto-refresh-badge">自动刷新中</span>
        </div>
        <button class="btn primary" :disabled="loading" @click="refresh">
          {{ loading ? '加载中...' : '立即刷新' }}
        </button>
      </div>
    </div>

    <div v-if="!token" class="hint">
      请先登录医生账号，再查看患者列表。
    </div>

    <div v-else class="content">
      <div v-if="error" class="alert error">{{ error }}</div>
      <div v-if="loading" class="loading">数据加载中，请稍候...</div>
      <div v-else-if="cards.length === 0" class="empty">今日暂无患者预约</div>
      <div v-else class="grid">
        <div v-for="card in cards" :key="card.key" class="card">
          <div class="card-head">
            <div>
              <div class="date">{{ formatDate(card.workDate) }}</div>
              <div class="slot">{{ card.timeSlotName }}</div>
            </div>
            <div class="meta">
              <span>{{ card.deptName || '未分配科室' }}</span>
              <span>·</span>
              <span>{{ card.roomName || '未分配诊室' }}</span>
              <span class="count">{{ card.patients.length }} 人</span>
            </div>
          </div>

          <div class="table">
            <div class="thead">
              <div>姓名</div>
              <div>性别</div>
              <div>年龄</div>
              <div>电话</div>
              <div>排队号</div>
              <div>候诊序号</div>
              <div>预约类型</div>
              <div>状态</div>
              <div class="actions-col">操作</div>
            </div>

            <template v-for="p in card.patients" :key="p.appointmentId">
              <div class="row">
                <div>{{ p.patientName || '未知' }}</div>
                <div>{{ p.gender === 'M' ? '男' : p.gender === 'F' ? '女' : '未知' }}</div>
                <div>{{ p.age ?? '—' }}</div>
                <div>{{ p.phone || '—' }}</div>
                <div>{{ p.queueNumber ?? '—' }}</div>
                <div>
                  <span class="queue-position" :class="{ 'first-in-queue': p.waitingNumber === 1 }">
                    {{ p.waitingNumber || 0 }}
                  </span>
                </div>
                <div>{{ p.appointmentTypeName || '—' }}</div>
                <div>
                  <span :class="['status', statusClass(p.appointmentStatus)]">
                    {{ statusLabel(p.appointmentStatus) }}
                  </span>
                </div>
                <div class="actions-col actions">
                  <button
                      class="mini-btn primary"
                      :disabled="actionLoading === p.appointmentId || disableCall(p.appointmentStatus, p.waitingNumber === 1)"
                      :title="p.waitingNumber !== 1 ? '仅能对第一位患者叫号' : ''"
                      @click="callPatient(p)"
                  >
                    叫号
                  </button>
                  <button
                      class="mini-btn success"
                      :disabled="actionLoading === p.appointmentId || disableMark(p.appointmentStatus, p.waitingNumber === 1)"
                      :title="p.waitingNumber !== 1 ? '仅能对第一位患者操作' : ''"
                      @click="markCompleted(p, card.workDate)"
                  >
                    已就诊
                  </button>
                  <button
                      class="mini-btn ghost"
                      :disabled="historyLoading === p.patientId"
                      @click="toggleHistory(p.patientId)"
                  >
                    {{ historyVisible[p.patientId] ? '收起历史' : '查看历史' }}
                  </button>
                </div>
              </div>

              <!-- 历史记录紧跟在当前患者行后面 -->
              <div v-show="historyVisible[p.patientId]" class="history-row-wrapper">
                <div class="history">
                  <div class="history-head">
                    <div>历史就诊记录（最多 50 条）</div>
                    <div v-if="historyLoading === p.patientId" class="loading small">加载中...</div>
                  </div>
                  <div v-if="historyError[p.patientId]" class="alert warn">{{ historyError[p.patientId] }}</div>
                  <div v-else-if="!historyCache[p.patientId] || historyCache[p.patientId].length === 0" class="empty">
                    暂无历史记录
                  </div>
                  <div v-else class="history-list">
                    <div v-for="item in historyCache[p.patientId]" :key="item.appointmentId" class="history-item">
                      <div class="title">{{ item.deptName }} · {{ item.doctorName }} {{ item.doctorTitle }}</div>
                      <div class="time">{{ item.appointmentTime }}</div>
                      <div class="status-tag">{{ historyStatusLabel(item.status) }}</div>
                      <div class="note">{{ item.typeName }} | 费用 ¥{{ item.feeFinal ?? 0 }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const navRef = ref(null)
const navHeight = ref(110)
const token = ref(localStorage.getItem('token'))

const loading = ref(false)
const error = ref('')
const schedules = ref([])
const actionLoading = ref(null)

const historyCache = reactive({})
const historyVisible = reactive({})
const historyLoading = ref(null)
const historyError = reactive({})

// 自动刷新定时器
const autoRefreshTimer = ref(null)
const AUTO_REFRESH_INTERVAL = 30000 // 30秒自动刷新

function formatInputDate(date) {
  return date.toISOString().split('T')[0]
}

function updateNavHeight() {
  if (navRef.value?.$el) {
    navHeight.value = navRef.value.$el.offsetHeight + 20
  }
}

const cards = computed(() => {
  return [...schedules.value].sort((a, b) => {
    const da = new Date(a.workDate)
    const db = new Date(b.workDate)
    if (da.getTime() !== db.getTime()) return da - db
    return (a.timeSlot ?? 0) - (b.timeSlot ?? 0)
  })
})

function statusLabel(status) {
  if (status === 'no_show' || status === 'missed') return '过号'
  if (status === 'booked') return '待就诊'
  if (status === 'completed') return '已就诊'
  if (status === 'cancelled') return '已取消'
  if (status === 'pending') return '待支付'
  return '未知'
}

function statusClass(status) {
  if (status === 'no_show' || status === 'missed') return 'missed'
  if (status === 'booked') return 'booked'
  if (status === 'completed') return 'completed'
  if (status === 'cancelled') return 'cancelled'
  if (status === 'pending') return 'pending'
  return 'unknown'
}

function historyStatusLabel(status) {
  if (status === 'no_show' || status === 'missed') return '过号'
  if (status === 'booked') return '待就诊'
  if (status === 'completed') return '已就诊'
  if (status === 'cancelled') return '已取消'
  if (status === 'pending') return '待支付'
  return status || '未知'
}

function disableMark(status, isFirst) {
  return !isFirst || status === 'completed' || status === 'no_show' || status === 'cancelled'
}

function disableCall(status, isFirst) {
  return !isFirst || status === 'completed' || status === 'no_show' || status === 'cancelled'
}

function formatDate(dateStr) {
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function fetchSchedulesForDate(dateStr) {
  const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
  const { data } = await axios.get('/api/doctor/patient/schedules-line', {
    params: { date: dateStr },
    headers
  })
  if (data?.code !== 200) {
    throw new Error(data?.message || '获取排班失败')
  }
  return Array.isArray(data.data) ? data.data : []
}

async function refresh() {
  if (!token.value) {
    error.value = '未登录或登录已过期，请先登录医生账号'
    return
  }

  const todayStr = formatInputDate(new Date())

  loading.value = true
  error.value = ''
  schedules.value = []
  try {
    const dayData = await fetchSchedulesForDate(todayStr)
    schedules.value = dayData
  } catch (e) {
    error.value = e?.message || '数据加载失败'
  } finally {
    loading.value = false
  }
}

async function markCompleted(p, workDate) {
  if (!p?.appointmentId) return
  if (!confirm('确认将该患者标记为已就诊？')) return
  actionLoading.value = p.appointmentId
  error.value = ''
  try {
    const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
    const { data } = await axios.put(`/api/doctor/patient/${p.appointmentId}/completed`, null, { headers })
    if (data?.code !== 200) throw new Error(data?.message || '操作失败')
    await refreshSingleDate(workDate)
    alert('已标记为已就诊')
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

async function callPatient(p) {
  if (!p?.appointmentId) return
  if (!confirm(`确认叫号：${p.patientName}？`)) return

  actionLoading.value = p.appointmentId
  try {
    const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
    const { data } = await axios.post(
        `/api/doctor/patient/${p.appointmentId}/call`,
        null,
        { headers }
    )
    if (data?.code !== 200) throw new Error(data?.message || '叫号失败')
    alert('已叫号，15分钟内未就诊将自动过号')
  } catch (e) {
    alert(e?.response?.data?.message || e?.message || '叫号失败')
  } finally {
    actionLoading.value = null
  }
}

async function refreshSingleDate(workDate) {
  if (!workDate) return refresh()
  try {
    const dateStr = formatInputDate(new Date(workDate))
    const dayData = await fetchSchedulesForDate(dateStr)
    const others = schedules.value.filter(item => formatInputDate(new Date(item.workDate)) !== dateStr)
    schedules.value = [...others, ...dayData]
  } catch (e) {
    await refresh()
  }
}

function startAutoRefresh() {
  stopAutoRefresh()
  autoRefreshTimer.value = setInterval(() => {
    if (token.value) {
      refresh()
    }
  }, AUTO_REFRESH_INTERVAL)
}

function stopAutoRefresh() {
  if (autoRefreshTimer.value) {
    clearInterval(autoRefreshTimer.value)
    autoRefreshTimer.value = null
  }
}

async function toggleHistory(patientId) {
  historyError[patientId] = ''
  if (historyVisible[patientId]) {
    historyVisible[patientId] = false
    return
  }
  if (!historyCache[patientId]) {
    historyLoading.value = patientId
    try {
      const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
      const { data } = await axios.get(`/api/doctor/patient/${patientId}/history`, {
        params: { limit: 50 },
        headers
      })
      if (data?.code !== 200) throw new Error(data?.message || '获取历史失败')
      historyCache[patientId] = data.data || []
    } catch (e) {
      historyError[patientId] = e?.response?.data?.message || e?.message || '获取历史失败'
    } finally {
      historyLoading.value = null
    }
  }
  historyVisible[patientId] = true
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  await refresh()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7fafc; padding: 20px; box-sizing: border-box; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
.page-head h1 { margin: 0; font-size: 24px; }
.sub { margin: 4px 0 0; color: #6b7280; }
.filters { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.refresh-info { display: flex; align-items: center; gap: 8px; }
.auto-refresh-badge { background: #dcfce7; color: #15803d; padding: 6px 12px; border-radius: 999px; font-size: 13px; font-weight: 600; animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
.filter-item { display: flex; flex-direction: column; font-size: 14px; color: #374151; }
.filter-item input { padding: 8px; border: 1px solid #d1d5db; border-radius: 6px; }
.btn { border: none; padding: 10px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; }
.btn.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.content { max-width: 1200px; margin: 0 auto; }
.alert { padding: 10px 12px; border-radius: 8px; margin-bottom: 12px; }
.alert.error { background: #fdecea; color: #b91c1c; }
.alert.warn { background: #fff7ed; color: #9a3412; }
.loading { padding: 16px; text-align: center; color: #6b7280; }
.loading.small { padding: 0; font-size: 13px; }
.empty { padding: 16px; text-align: center; color: #9ca3af; }
.grid { display: grid; gap: 16px; }
.card { background: #fff; border-radius: 14px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); padding: 16px; }
.card-head { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f3f4f6; padding-bottom: 10px; margin-bottom: 12px; }
.date { font-weight: 700; font-size: 16px; }
.slot { color: #6b7280; font-size: 14px; }
.meta { display: flex; gap: 6px; align-items: center; color: #6b7280; flex-wrap: wrap; }
.count { background: #eef2ff; color: #4338ca; padding: 2px 8px; border-radius: 999px; font-size: 12px; }
.table { width: 100%; overflow-x: auto; }
.thead, .row { display: grid; grid-template-columns: repeat(8, 1fr) 1.5fr; gap: 8px; align-items: center; padding: 10px 8px; }
.thead { background: #eef2ff; color: #4338ca; font-weight: 700; border-radius: 10px; }
.row { border-bottom: 1px solid #f3f4f6; }
.actions-col { display: flex; justify-content: flex-start; gap: 8px; flex-wrap: wrap; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.mini-btn { padding: 6px 10px; border-radius: 6px; border: none; cursor: pointer; font-size: 13px; }
.mini-btn.success { background: #dcfce7; color: #15803d; }
.mini-btn.primary { background: #fff7ed; color: #c2410c; }
.mini-btn.ghost { background: #f3f4f6; color: #374151; }
.mini-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.queue-position { padding: 4px 10px; border-radius: 999px; font-weight: 600; background: #f3f4f6; color: #374151; }
.queue-position.first-in-queue { background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%); color: #fff; animation: glow 1.5s ease-in-out infinite; }
@keyframes glow { 0%, 100% { box-shadow: 0 0 5px rgba(239, 68, 68, 0.5); } 50% { box-shadow: 0 0 15px rgba(239, 68, 68, 0.8); } }
.status { padding: 4px 8px; border-radius: 999px; font-size: 12px; }
.status.booked { background: #e0f2fe; color: #0369a1; }
.status.completed { background: #dcfce7; color: #15803d; }
.status.cancelled { background: #f3f4f6; color: #6b7280; }
.status.pending { background: #fef9c3; color: #92400e; }
.status.missed { background: #fee2e2; color: #b91c1c; }
.status.unknown { background: #e5e7eb; color: #4b5563; }
.history-row-wrapper { grid-column: 1 / -1; }
.history { background: #f9fafb; border-radius: 12px; padding: 12px; margin: 8px 0; }
.history-head { display: flex; justify-content: space-between; align-items: center; font-weight: 600; color: #374151; margin-bottom: 8px; }
.history-list { display: grid; gap: 8px; }
.history-item { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px; display: grid; gap: 6px; }
.history-item .title { font-weight: 600; color: #111827; }
.history-item .time { color: #6b7280; font-size: 13px; }
.history-item .status-tag { display: inline-block; padding: 2px 8px; background: #eef2ff; color: #4338ca; border-radius: 999px; font-size: 12px; width: fit-content; }
.history-item .note { color: #6b7280; font-size: 13px; }
.hint { background: #fff7ed; color: #9a3412; padding: 12px; border-radius: 10px; }
@media (max-width: 900px) {
  .thead, .row { grid-template-columns: repeat(3, 1fr); grid-auto-rows: minmax(20px, auto); }
  .actions-col { grid-column: 1 / -1; }
  .history-row-wrapper { grid-column: 1 / -1; }
}
</style>