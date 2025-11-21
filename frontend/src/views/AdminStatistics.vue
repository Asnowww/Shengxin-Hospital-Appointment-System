<template>
  <Navigation ref="navRef" />
  <div class="page" :style="{ paddingTop: navHeight + 'px' }">
    <header class="page-header">
      <div>
        <p class="subtitle">数据统计与分析</p>
        <h1>挂号总量 · 退号监测 · 收入概览</h1>
        <p class="muted">按时间范围查看历史挂号与退号数据，支持导出 CSV / Excel，辅助管理员运营决策。</p>
      </div>
      <div class="header-actions">
        <button class="btn ghost" :disabled="loading" @click="exportCSV">导出 CSV</button>
        <button class="btn primary" :disabled="loading" @click="exportExcel">导出 Excel</button>
      </div>
    </header>

    <section class="filters">
      <div class="filter-item">
        <label>开始日期</label>
        <input type="date" v-model="filters.startDate" />
      </div>
      <div class="filter-item">
        <label>结束日期</label>
        <input type="date" v-model="filters.endDate" />
      </div>
      <div class="filter-item">
        <label>科室</label>
        <select v-model="filters.deptId">
          <option value="">全部科室</option>
          <option
            v-for="dept in departmentOptions"
            :key="dept.deptId"
            :value="String(dept.deptId)">
            {{ dept.deptName }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label>医生</label>
        <select v-model="filters.doctorId">
          <option value="">全部医生</option>
          <option
            v-for="doc in doctorOptions"
            :key="doc.doctorId"
            :value="String(doc.doctorId)">
            {{ doc.doctorName }}
          </option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn primary" :disabled="loading" @click="applyFilters">应用筛选</button>
        <button class="btn ghost" :disabled="loading" @click="resetFilters">重置</button>
      </div>
    </section>

    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <section class="summary-grid">
      <div class="card" v-for="card in summaryCards" :key="card.title">
        <div class="card-header">
          <p class="card-label">{{ card.subtitle }}</p>
          <p class="card-title">{{ card.title }}</p>
        </div>
        <div class="card-value">
          {{ card.value }}
          <span v-if="card.suffix" class="suffix">{{ card.suffix }}</span>
        </div>
        <div class="card-footer">
          <span>{{ card.detail }}</span>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>每日新增患者</h3>
          <p class="muted">来源 /api/statistics/daily-new-patients，仅统计首次就诊患者</p>
        </div>
        <div class="chip">总计 {{ formatNumber(totalNewPatients) }} 人</div>
      </div>
      <div v-if="barChartData.length" class="bar-chart">
        <div
          v-for="item in barChartData"
          :key="item.date"
          class="bar"
          :style="{ height: item.height + '%' }"
          :title="`${item.date}：${item.count} 人`">
          <span class="bar-value">{{ item.count }}</span>
          <span class="bar-label">{{ item.date }}</span>
        </div>
      </div>
      <div v-else class="empty">暂无新增患者数据</div>
    </section>

    <section class="panel duo">
      <div class="panel-block">
        <div class="panel-header">
          <div>
            <h3>退号 TOP 5（医生）</h3>
            <p class="muted">按退号数排序，用于快速定位异常波动</p>
          </div>
        </div>
        <div v-if="topCancelledDoctors.length" class="list">
          <div v-for="doc in topCancelledDoctors" :key="doc.doctorId" class="list-row">
            <div>
              <p class="list-title">{{ doc.doctorName }}</p>
              <p class="muted">{{ doc.deptName }}</p>
            </div>
            <div class="list-metric">
              <strong>{{ formatNumber(doc.cancelledCount) }}</strong>
              <span class="muted">退号</span>
            </div>
            <div class="list-metric">
              <strong>{{ formatPercent(doc.cancelledCount, doc.appointmentCount) }}</strong>
              <span class="muted">退号率</span>
            </div>
          </div>
        </div>
        <div v-else class="empty">暂无退号数据</div>
      </div>
      <div class="panel-block">
        <div class="panel-header">
          <div>
            <h3>退号分布（科室）</h3>
            <p class="muted">展示科室退号情况与退号率</p>
          </div>
        </div>
        <div v-if="filteredDeptStats.length" class="list">
          <div v-for="dept in deptCancelDistribution" :key="dept.deptId" class="list-row">
            <div>
              <p class="list-title">{{ dept.deptName }}</p>
              <p class="muted">{{ formatNumber(dept.appointmentCount) }} 总挂号</p>
            </div>
            <div class="list-metric">
              <strong>{{ formatNumber(dept.cancelledCount) }}</strong>
              <span class="muted">退号</span>
            </div>
            <div class="progress">
              <div
                class="progress-bar"
                :style="{ width: dept.cancelRate + '%' }">
              </div>
            </div>
            <span class="progress-num">{{ formatPercent(dept.cancelledCount, dept.appointmentCount) }}</span>
          </div>
        </div>
        <div v-else class="empty">暂无科室退号数据</div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>科室挂号与退号详情</h3>
          <p class="muted">包含总体挂号、退号、完成数与收入</p>
        </div>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>科室</th>
              <th>总挂号</th>
              <th>已完成</th>
              <th>退号</th>
              <th>退号率</th>
              <th>总收入</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="dept in filteredDeptStats" :key="dept.deptId">
              <td>{{ dept.deptName }}</td>
              <td>{{ formatNumber(dept.appointmentCount) }}</td>
              <td>{{ formatNumber(dept.completedCount) }}</td>
              <td class="warning">{{ formatNumber(dept.cancelledCount) }}</td>
              <td>{{ formatPercent(dept.cancelledCount, dept.appointmentCount) }}</td>
              <td>{{ formatCurrency(dept.totalRevenue) }}</td>
            </tr>
            <tr v-if="!filteredDeptStats.length">
              <td colspan="6" class="empty">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>医生挂号与退号详情</h3>
          <p class="muted">可按科室、医生筛选，便于定位医生层面的退号异常</p>
        </div>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>医生</th>
              <th>科室</th>
              <th>总挂号</th>
              <th>已完成</th>
              <th>退号</th>
              <th>退号率</th>
              <th>总收入</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="doc in filteredDoctorStats" :key="doc.doctorId">
              <td>{{ doc.doctorName }}</td>
              <td>{{ doc.deptName }}</td>
              <td>{{ formatNumber(doc.appointmentCount) }}</td>
              <td>{{ formatNumber(doc.completedCount) }}</td>
              <td class="warning">{{ formatNumber(doc.cancelledCount) }}</td>
              <td>{{ formatPercent(doc.cancelledCount, doc.appointmentCount) }}</td>
              <td>{{ formatCurrency(doc.totalRevenue) }}</td>
            </tr>
            <tr v-if="!filteredDoctorStats.length">
              <td colspan="7" class="empty">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const errorMessage = ref('')
const token = localStorage.getItem('token') || ''

const today = new Date()
const defaultEnd = formatDate(today)
const defaultStart = formatDate(new Date(today.getTime() - 29 * 24 * 60 * 60 * 1000))

const filters = reactive({
  startDate: defaultStart,
  endDate: defaultEnd,
  deptId: '',
  doctorId: ''
})

const departmentStats = ref([])
const doctorStats = ref([])
const dailyNewPatients = ref([])

const departmentOptions = computed(() => {
  const seen = new Set()
  return departmentStats.value
    .filter(item => {
      if (seen.has(item.deptId)) return false
      seen.add(item.deptId)
      return true
    })
    .map(item => ({ deptId: item.deptId, deptName: item.deptName }))
})

const doctorOptions = computed(() => {
  const list = filters.deptId
    ? doctorStats.value.filter(item => String(item.deptId) === filters.deptId)
    : doctorStats.value
  const seen = new Set()
  return list
    .filter(item => {
      if (seen.has(item.doctorId)) return false
      seen.add(item.doctorId)
      return true
    })
    .map(item => ({ doctorId: item.doctorId, doctorName: item.doctorName }))
})

const filteredDeptStats = computed(() => {
  if (!filters.deptId) {
    return departmentStats.value
  }
  return departmentStats.value.filter(item => String(item.deptId) === filters.deptId)
})

const filteredDoctorStats = computed(() => {
  let result = doctorStats.value
  if (filters.deptId) {
    result = result.filter(item => String(item.deptId) === filters.deptId)
  }
  if (filters.doctorId) {
    result = result.filter(item => String(item.doctorId) === filters.doctorId)
  }
  return result
})

const totalAppointments = computed(() =>
  filteredDeptStats.value.reduce((sum, item) => sum + toNumber(item.appointmentCount), 0)
)
const totalCancelled = computed(() =>
  filteredDeptStats.value.reduce((sum, item) => sum + toNumber(item.cancelledCount), 0)
)
const totalRevenue = computed(() =>
  filteredDeptStats.value.reduce((sum, item) => sum + toNumber(item.totalRevenue), 0)
)
const totalCompleted = computed(() =>
  filteredDeptStats.value.reduce((sum, item) => sum + toNumber(item.completedCount), 0)
)
const totalNewPatients = computed(() =>
  dailyNewPatients.value.reduce((sum, item) => sum + toNumber(item.newPatientCount), 0)
)

const summaryCards = computed(() => [
  {
    title: '挂号总量',
    subtitle: '预约总数',
    value: formatNumber(totalAppointments.value),
    detail: `${filters.startDate} 至 ${filters.endDate}`,
    suffix: '单'
  },
  {
    title: '退号总量',
    subtitle: '已退号',
    value: formatNumber(totalCancelled.value),
    detail: `退号率 ${formatPercent(totalCancelled.value, totalAppointments.value)}`,
    suffix: '单'
  },
  {
    title: '完成总量',
    subtitle: '已完成',
    value: formatNumber(totalCompleted.value),
    detail: totalAppointments.value
      ? `完成率 ${formatPercent(totalCompleted.value, totalAppointments.value)}`
      : '暂无数据',
    suffix: '单'
  },
  {
    title: '收入合计',
    subtitle: '已支付',
    value: formatCurrency(totalRevenue.value),
    detail: '仅统计支付完成的费用'
  }
])

const barChartData = computed(() => {
  if (!dailyNewPatients.value.length) return []
  const max = Math.max(...dailyNewPatients.value.map(item => toNumber(item.newPatientCount)), 1)
  return dailyNewPatients.value.map(item => ({
    date: item.date,
    count: toNumber(item.newPatientCount),
    height: Math.max(5, Math.round((toNumber(item.newPatientCount) / max) * 100))
  }))
})

const topCancelledDoctors = computed(() =>
  [...filteredDoctorStats.value]
    .sort((a, b) => toNumber(b.cancelledCount) - toNumber(a.cancelledCount))
    .slice(0, 5)
)

const deptCancelDistribution = computed(() =>
  filteredDeptStats.value.map(item => ({
    ...item,
    cancelRate: Math.min(
      100,
      Math.round(
        (toNumber(item.cancelledCount) / Math.max(1, toNumber(item.appointmentCount))) * 100
      )
    )
  }))
)

function formatDate(dateObj) {
  const year = dateObj.getFullYear()
  const month = `${dateObj.getMonth() + 1}`.padStart(2, '0')
  const day = `${dateObj.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function toNumber(val) {
  const num = Number(val)
  return Number.isFinite(num) ? num : 0
}

function formatNumber(val) {
  return toNumber(val).toLocaleString()
}

function formatCurrency(val) {
  return `¥${toNumber(val).toFixed(2)}`
}

function formatPercent(numerator, denominator) {
  const num = toNumber(numerator)
  const den = toNumber(denominator)
  if (!den) return '0%'
  return `${((num * 100) / den).toFixed(2)}%`
}

function validateDates() {
  if (!filters.startDate || !filters.endDate) {
    errorMessage.value = '请选择开始和结束日期'
    return false
  }
  if (filters.startDate > filters.endDate) {
    errorMessage.value = '开始日期不能晚于结束日期'
    return false
  }
  return true
}

function buildHeaders() {
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function fetchStatistics() {
  if (!validateDates()) {
    return
  }
  loading.value = true
  errorMessage.value = ''
  const params = {
    startDate: filters.startDate,
    endDate: filters.endDate
  }

  try {
    const [deptRes, doctorRes, newPatientRes] = await Promise.all([
      axios.get('/api/statistics/department-appointments', { params, headers: buildHeaders() }),
      axios.get('/api/statistics/doctor-appointments', { params, headers: buildHeaders() }),
      axios.get('/api/statistics/daily-new-patients', { params, headers: buildHeaders() })
    ])

    departmentStats.value = extractList(deptRes)
    doctorStats.value = extractList(doctorRes)
    dailyNewPatients.value = extractList(newPatientRes)
  } catch (error) {
    console.error(error)
    errorMessage.value = error?.response?.data?.message || '获取统计数据失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

function extractList(res) {
  const data = res?.data
  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.data)) return data.data
  return []
}

function applyFilters() {
  fetchStatistics()
}

function resetFilters() {
  filters.startDate = defaultStart
  filters.endDate = defaultEnd
  filters.deptId = ''
  filters.doctorId = ''
  fetchStatistics()
}

function exportCSV() {
  const rows = buildExportRows()
  const csv = rows
    .map(row =>
      row
        .map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`)
        .join(',')
    )
    .join('\n')
  downloadFile(csv, 'statistics_report.csv', 'text/csv')
}

function exportExcel() {
  const rows = buildExportRows()
  const content = rows.map(row => row.join('\t')).join('\n')
  downloadFile(content, 'statistics_report.xlsx', 'application/vnd.ms-excel')
}

function buildExportRows() {
  const head = [
    '维度',
    '名称',
    '科室',
    '总挂号',
    '已完成',
    '退号',
    '退号率',
    '总收入'
  ]
  const deptRows = filteredDeptStats.value.map(item => [
    '科室',
    item.deptName,
    item.deptName,
    toNumber(item.appointmentCount),
    toNumber(item.completedCount),
    toNumber(item.cancelledCount),
    formatPercent(item.cancelledCount, item.appointmentCount),
    toNumber(item.totalRevenue).toFixed(2)
  ])
  const doctorRows = filteredDoctorStats.value.map(item => [
    '医生',
    item.doctorName,
    item.deptName,
    toNumber(item.appointmentCount),
    toNumber(item.completedCount),
    toNumber(item.cancelledCount),
    formatPercent(item.cancelledCount, item.appointmentCount),
    toNumber(item.totalRevenue).toFixed(2)
  ])
  return [head, ...deptRows, ...doctorRows]
}

function downloadFile(content, filename, type) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function updateNavHeight() {
  const height = navRef.value?.$el?.offsetHeight
  if (height) {
    navHeight.value = height + 30
  }
}

onMounted(() => {
  updateNavHeight()
  window.addEventListener('resize', updateNavHeight)
  fetchStatistics()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateNavHeight)
})
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.subtitle {
  color: #6b7280;
  margin: 0 0 4px;
  font-size: 14px;
}

.page-header h1 {
  margin: 0;
  font-size: 26px;
}

.muted {
  color: #6b7280;
  margin: 4px 0 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn {
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
  transition: transform 0.1s ease, box-shadow 0.2s ease;
}

.btn:active {
  transform: translateY(1px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn.primary {
  background: linear-gradient(135deg, #6ba0ff, #1f7aff);
  color: #fff;
  box-shadow: 0 6px 18px rgba(31, 122, 255, 0.25);
}

.btn.ghost {
  background: #f3f4f6;
  color: #111827;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.filter-item label {
  display: block;
  margin-bottom: 6px;
  color: #374151;
  font-weight: 600;
  font-size: 14px;
}

.filter-item input,
.filter-item select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-sizing: border-box;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.error-banner {
  background: #fef2f2;
  color: #b91c1c;
  padding: 12px 14px;
  border-radius: 10px;
  margin-bottom: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.card-title {
  margin: 0;
  font-size: 18px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
}

.suffix {
  font-size: 14px;
  color: #6b7280;
  margin-left: 6px;
}

.card-footer {
  color: #6b7280;
  font-size: 13px;
}

.panel {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.panel.duo {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.panel-block {
  background: #f9fafb;
  border-radius: 10px;
  padding: 14px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-header h3 {
  margin: 0;
}

.chip {
  padding: 6px 10px;
  background: #eef2ff;
  color: #1f3b82;
  border-radius: 999px;
  font-weight: 600;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  height: 220px;
  border-bottom: 1px dashed #e5e7eb;
}

.bar {
  flex: 1;
  background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
  border-radius: 8px 8px 4px 4px;
  position: relative;
  min-height: 40px;
}

.bar-value {
  position: absolute;
  top: -22px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #111827;
}

.bar-label {
  position: absolute;
  bottom: -26px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  color: #6b7280;
  white-space: nowrap;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr 0.8fr auto;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #eef2f7;
}

.list-title {
  margin: 0 0 4px;
  font-weight: 600;
}

.list-metric {
  text-align: center;
}

.progress {
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
  height: 10px;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #f97316, #ef4444);
}

.progress-num {
  font-size: 13px;
  color: #111827;
  text-align: right;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f2f5;
  text-align: left;
}

th {
  background: #f9fafb;
  font-weight: 700;
}

.warning {
  color: #c2410c;
  font-weight: 700;
}

.empty {
  color: #6b7280;
  text-align: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .page {
    padding: 16px 12px 30px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .bar-label {
    transform: translate(-50%, 10px) rotate(-45deg);
    transform-origin: center;
  }

  .list-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
