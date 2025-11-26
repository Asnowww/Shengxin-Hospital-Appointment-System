<template>
  <Navigation ref="navRef" />
  <div class="page" :style="{ paddingTop: navHeight + 'px' }">
    <div class="page-head">
      <div>
        <h1>患者管理</h1>
        <p class="sub">查看当日及未来的挂号患者列表，提前为接诊做准备</p>
      </div>
      <div class="filters">
        <label class="filter-item">
          <span>开始日期</span>
          <input type="date" v-model="startDate" />
        </label>
        <label class="filter-item">
          <span>结束日期</span>
          <input type="date" v-model="endDate" />
        </label>
        <button class="btn primary" :disabled="loading" @click="refresh">
          {{ loading ? '加载中...' : '查询' }}
        </button>
      </div>
    </div>

    <div v-if="!token" class="hint">
      请先登录医生账号，再查看患者列表。
    </div>

    <div v-else class="content">
      <div v-if="error" class="alert error">{{ error }}</div>
      <div v-if="loading" class="loading">数据加载中，请稍候...</div>
      <div v-else-if="cards.length === 0" class="empty">所选日期内暂无患者预约</div>
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

            <div v-for="p in card.patients" :key="p.appointmentId" class="row">
              <div>{{ p.patientName || '未知' }}</div>
              <div>{{ p.gender === 'M' ? '男' : p.gender === 'F' ? '女' : '未知' }}</div>
              <div>{{ p.age ?? '—' }}</div>
              <div>{{ p.phone || '—' }}</div>
              <div>{{ p.queueNumber ?? '—' }}</div>
              <div>{{ p.waitingNumber || 0 }}</div>
              <div>{{ p.appointmentTypeName || '—' }}</div>
              <div>
                <span :class="['status', statusClass(p.appointmentStatus)]">
                  {{ statusLabel(p.appointmentStatus) }}
                </span>
              </div>
              <div class="actions-col actions">
                <button
                    class="mini-btn primary"
                    :disabled="actionLoading === p.appointmentId || disableCall(p.appointmentStatus)"
                    @click="callPatient(p)"
                >
                  叫号
                </button>
                <button
                  class="mini-btn success"
                  :disabled="actionLoading === p.appointmentId || disableMark(p.appointmentStatus)"
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
          </div>

          <div
            v-for="p in card.patients"
            :key="`${p.appointmentId}-history`"
            v-show="historyVisible[p.patientId]"
            class="history"
          >
            <div class="history-head">
              <div>历史就诊记录（最多 50 条）</div>
              <div v-if="historyLoading === p.patientId" class="loading small">加载中...</div>
            </div>
            <div v-if="historyError[p.patientId]" class="alert warn">{{ historyError[p.patientId] }}</div>
            <div v-else-if="!historyCache[p.patientId] || historyCache[p.patientId].length === 0" class="empty">
              暂无历史记录
            </div>
            <div v-else class="history-list">
              <div v-for="item in historyCache[p.patientId]" :key="item.appointmentId" class="history-row">
                <div class="title">{{ item.deptName }} · {{ item.doctorName }} {{ item.doctorTitle }}</div>
                <div class="time">{{ item.appointmentTime }}</div>
                <div class="status-tag">{{ historyStatusLabel(item.status) }}</div>
                <div class="note">{{ item.typeName }} | 费用 ¥{{ item.feeFinal ?? 0 }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const navRef = ref(null)
const navHeight = ref(110)
const token = ref(localStorage.getItem('token'))

const today = new Date()
const startDate = ref(formatInputDate(today))
const endDate = ref(formatInputDate(addDays(today, 29)))

const loading = ref(false)
const error = ref('')
const schedules = ref([])
const actionLoading = ref(null)

const historyCache = reactive({})
const historyVisible = reactive({})
const historyLoading = ref(null)
const historyError = reactive({})

function formatInputDate(date) {
  return date.toISOString().split('T')[0]
}

function addDays(date, delta) {
  const d = new Date(date)
  d.setDate(d.getDate() + delta)
  return d
}

function updateNavHeight() {
  if (navRef.value?.$el) {
    navHeight.value = navRef.value.$el.offsetHeight + 20
  }
}

const cards = computed(() => {
  // 排序：日期升序，时间段升序
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

function disableMark(status) {
  return status === 'completed' || status === 'no_show' || status === 'cancelled'
}

function disableCall(status) {
  return status === 'completed' || status === 'no_show' || status === 'cancelled'
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
  if (!startDate.value || !endDate.value) {
    error.value = '请先选择起止日期'
    return
  }
  const start = new Date(startDate.value)
  const end = new Date(endDate.value)
  if (start > end) {
    error.value = '开始日期不能晚于结束日期'
    return
  }
  const dayCount = Math.floor((end - start) / (24 * 3600 * 1000)) + 1
  if (dayCount > 60) {
    error.value = '查询范围过大，请将时间范围控制在 60 天内'
    return
  }

  loading.value = true
  error.value = ''
  schedules.value = []
  try {
    const dates = []
    for (let i = 0; i < dayCount; i++) {
      dates.push(formatInputDate(addDays(start, i)))
    }
    const results = await Promise.allSettled(dates.map(d => fetchSchedulesForDate(d)))
    const merged = []
    results.forEach(res => {
      if (res.status === 'fulfilled' && Array.isArray(res.value)) {
        merged.push(...res.value)
      }
    })
    schedules.value = merged
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
    // 回退到完整刷新
    await refresh()
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
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f7fafc; padding: 20px; box-sizing: border-box; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; flex-wrap: wrap; margin-bottom: 16px; }
.page-head h1 { margin: 0; font-size: 24px; }
.sub { margin: 4px 0 0; color: #6b7280; }
.filters { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
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
.status { padding: 4px 8px; border-radius: 999px; font-size: 12px; }
.status.booked { background: #e0f2fe; color: #0369a1; }
.status.completed { background: #dcfce7; color: #15803d; }
.status.cancelled { background: #f3f4f6; color: #6b7280; }
.status.pending { background: #fef9c3; color: #92400e; }
.status.missed { background: #fee2e2; color: #b91c1c; }
.status.unknown { background: #e5e7eb; color: #4b5563; }
.history { background: #f9fafb; border-radius: 12px; padding: 12px; margin-top: 8px; }
.history-head { display: flex; justify-content: space-between; align-items: center; font-weight: 600; color: #374151; }
.history-list { display: grid; gap: 8px; margin-top: 8px; }
.history-row { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px; display: grid; gap: 6px; }
.history-row .title { font-weight: 600; color: #111827; }
.history-row .time { color: #6b7280; font-size: 13px; }
.history-row .status-tag { display: inline-block; padding: 2px 8px; background: #eef2ff; color: #4338ca; border-radius: 999px; font-size: 12px; width: fit-content; }
.history-row .note { color: #6b7280; font-size: 13px; }
.hint { background: #fff7ed; color: #9a3412; padding: 12px; border-radius: 10px; }
@media (max-width: 900px) {
  .thead, .row { grid-template-columns: repeat(3, 1fr); grid-auto-rows: minmax(20px, auto); }
  .actions-col { grid-column: 1 / -1; }
}
</style>
