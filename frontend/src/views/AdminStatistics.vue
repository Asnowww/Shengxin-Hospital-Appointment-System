<template>
  <Navigation ref="navRef" />
  
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <header class="dashboard-header">
      <div class="header-content">
        <div class="header-titles">
          <div class="badge">数据统计与分析</div>
          <h1>挂号运营数据大屏</h1>
          <p class="description">
            实时监测挂号总量、退号波动及收入营收情况，辅助管理层进行精细化运营决策。
          </p>
        </div>
        <div class="header-actions">
          <button class="btn ghost" :disabled="loading" @click="exportCSV">
         导出 CSV
          </button>
          <button class="btn primary" :disabled="loading" @click="exportExcel">
            <span v-if="loading" class="spinner"></span>
         导出 Excel
          </button>
        </div>
      </div>

      <section class="filters-bar">
        <div class="filter-group">
          <div class="filter-item date-range">
            <label>统计周期</label>
            <div class="date-inputs">
              <input type="date" v-model="filters.startDate" :max="filters.endDate" />
              <span class="separator">至</span>
              <input type="date" v-model="filters.endDate" :min="filters.startDate" />
            </div>
          </div>
          <div class="filter-item">
            <label>科室筛选</label>
            <div class="select-wrapper">
              <select v-model="filters.deptId">
                <option value="">全部科室</option>
                <option v-for="dept in departmentOptions" :key="dept.deptId" :value="String(dept.deptId)">
                  {{ dept.deptName }}
                </option>
              </select>
            </div>
          </div>
          <div class="filter-item">
            <label>医生筛选</label>
            <div class="select-wrapper">
              <select v-model="filters.doctorId">
                <option value="">全部医生</option>
                <option v-for="doc in doctorOptions" :key="doc.doctorId" :value="String(doc.doctorId)">
                  {{ doc.doctorName }}
                </option>
              </select>
            </div>
          </div>
        </div>
        <div class="filter-buttons">
          <button class="btn primary small" :disabled="loading" @click="applyFilters">查询</button>
          <button class="btn text small" :disabled="loading" @click="resetFilters">重置</button>
        </div>
      </section>
    </header>

    <div v-if="errorMessage" class="alert error">
      <span class="icon">⚠️</span> {{ errorMessage }}
    </div>

    <section class="kpi-grid">
      <div class="kpi-card" v-for="(card, index) in summaryCards" :key="card.title">
        <div class="kpi-icon" :class="'icon-' + index">
          <component :is="card.icon" />
        </div>
        <div class="kpi-data">
          <p class="kpi-label">{{ card.title }}</p>
          <div class="kpi-value-group">
            <span class="kpi-value">{{ card.value }}</span>
            <span v-if="card.suffix" class="kpi-unit">{{ card.suffix }}</span>
          </div>
          <p class="kpi-footer" :class="{ 'warning-text': card.isWarning }">
            {{ card.detail }}
          </p>
        </div>
      </div>
    </section>

    <section class="charts-grid">
      <div class="panel chart-panel">
        <div class="panel-header">
          <div>
            <h3>每日新增患者趋势</h3>
            <p class="muted">统计首次就诊患者数量</p>
          </div>
          <div class="stat-tag">总计 {{ formatNumber(totalNewPatients) }} 人</div>
        </div>
        
        <div v-if="barChartData.length" class="chart-container">
          <div class="simple-bar-chart">
            <div v-for="item in barChartData" :key="item.date" class="chart-column-wrapper">
              <div 
                class="chart-column" 
                :style="{ height: item.height + '%' }"
                :class="{ 'high-value': item.height > 80 }"
              >
                <div class="tooltip">
                  <div class="tooltip-date">{{ item.date }}</div>
                  <div class="tooltip-val">{{ item.count }} 人</div>
                </div>
              </div>
              <span class="x-axis-label">{{ formatShortDate(item.date) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <span class="empty-icon">📉</span>
          暂无趋势数据
        </div>
      </div>

      <div class="panel list-panel">
        <div class="panel-header">
          <div>
            <h3>退号异常监测 (TOP 5)</h3>
            <p class="muted">医生退号数排行</p>
          </div>
        </div>
        <div v-if="topCancelledDoctors.length" class="ranking-list">
          <div v-for="(doc, idx) in topCancelledDoctors" :key="doc.doctorId" class="rank-item">
            <div class="rank-index" :class="'top-' + (idx + 1)">{{ idx + 1 }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ doc.doctorName }}</div>
              <div class="rank-dept">{{ doc.deptName }}</div>
            </div>
            <div class="rank-stat">
              <div class="stat-val warning">{{ formatNumber(doc.cancelledCount) }} <span class="unit">退</span></div>
              <div class="stat-sub">率 {{ formatPercent(doc.cancelledCount, doc.appointmentCount) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无退号异常</div>
      </div>
    </section>

    <div class="details-grid">
      <section class="panel table-panel">
        <div class="panel-header">
          <h3>科室数据详情</h3>
          <div class="legend">
            <span class="dot warning"></span> 退号率 > 15% 预警
          </div>
        </div>
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>科室名称</th>
                <th class="text-right">总挂号</th>
                <th class="text-right">已完成</th>
                <th class="text-right">退号量</th>
                <th class="text-right">退号率</th>
                <th class="text-right">总收入</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="dept in filteredDeptStats" :key="dept.deptId">
                <td class="font-medium">{{ dept.deptName }}</td>
                <td class="text-right">{{ formatNumber(dept.appointmentCount) }}</td>
                <td class="text-right">{{ formatNumber(dept.completedCount) }}</td>
                <td class="text-right" :class="{ 'text-warning': isHighCancelRate(dept.cancelledCount, dept.appointmentCount) }">
                  {{ formatNumber(dept.cancelledCount) }}
                </td>
                <td class="text-right">
                  <div class="progress-cell">
                    <span>{{ formatPercent(dept.cancelledCount, dept.appointmentCount) }}</span>
                    <div class="mini-bar-bg">
                      <div class="mini-bar" :style="{ width: formatPercent(dept.cancelledCount, dept.appointmentCount), backgroundColor: isHighCancelRate(dept.cancelledCount, dept.appointmentCount) ? '#ef4444' : '#10b981' }"></div>
                    </div>
                  </div>
                </td>
                <td class="text-right font-num">{{ formatCurrency(dept.totalRevenue) }}</td>
              </tr>
              <tr v-if="!filteredDeptStats.length">
                <td colspan="6" class="empty-row">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel table-panel">
        <div class="panel-header">
          <h3>医生数据详情</h3>
        </div>
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>医生</th>
                <th>所属科室</th>
                <th class="text-right">总挂号</th>
                <th class="text-right">已完成</th>
                <th class="text-right">退号量</th>
                <th class="text-right">退号率</th>
                <th class="text-right">总收入</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in filteredDoctorStats" :key="doc.doctorId">
                <td class="font-medium">{{ doc.doctorName }}</td>
                <td class="muted-text">{{ doc.deptName }}</td>
                <td class="text-right">{{ formatNumber(doc.appointmentCount) }}</td>
                <td class="text-right">{{ formatNumber(doc.completedCount) }}</td>
                <td class="text-right" :class="{ 'text-warning': isHighCancelRate(doc.cancelledCount, doc.appointmentCount) }">
                  {{ formatNumber(doc.cancelledCount) }}
                </td>
                <td class="text-right">{{ formatPercent(doc.cancelledCount, doc.appointmentCount) }}</td>
                <td class="text-right font-num">{{ formatCurrency(doc.totalRevenue) }}</td>
              </tr>
              <tr v-if="!filteredDoctorStats.length">
                <td colspan="7" class="empty-row">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, h } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

// --- SVG Icons Components (内联以减少依赖) ---
const IconCalendar = { render: () => h('span', { innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>' }) }
const IconCancel = { render: () => h('span', { innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>' }) }
const IconCheck = { render: () => h('span', { innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>' }) }
const IconWallet = { render: () => h('span', { innerHTML: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>' }) }

// --- State ---
const navRef = ref(null)
const navHeight = ref(80)
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

// Data Containers
const departmentStats = ref([])
const doctorStats = ref([])
const dailyNewPatients = ref([])

// --- Options (Memoized) ---
const departmentOptions = computed(() => {
  const seen = new Set()
  return departmentStats.value.reduce((acc, item) => {
    if (!seen.has(item.deptId)) {
      seen.add(item.deptId)
      acc.push({ deptId: item.deptId, deptName: item.deptName })
    }
    return acc
  }, [])
})

const doctorOptions = computed(() => {
  const list = filters.deptId
    ? doctorStats.value.filter(item => String(item.deptId) === filters.deptId)
    : doctorStats.value
  const seen = new Set()
  return list.reduce((acc, item) => {
    if (!seen.has(item.doctorId)) {
      seen.add(item.doctorId)
      acc.push({ doctorId: item.doctorId, doctorName: item.doctorName })
    }
    return acc
  }, [])
})

// --- Filtered Data ---
const filteredDeptStats = computed(() => {
  if (!filters.deptId) return departmentStats.value
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

// --- Aggregated Metrics ---
const totalAppointments = computed(() => sumBy(filteredDeptStats.value, 'appointmentCount'))
const totalCancelled = computed(() => sumBy(filteredDeptStats.value, 'cancelledCount'))
const totalRevenue = computed(() => sumBy(filteredDeptStats.value, 'totalRevenue'))
const totalCompleted = computed(() => sumBy(filteredDeptStats.value, 'completedCount'))
const totalNewPatients = computed(() => sumBy(dailyNewPatients.value, 'newPatientCount'))

const summaryCards = computed(() => {
  const cancelRate = calcRate(totalCancelled.value, totalAppointments.value)
  const completeRate = calcRate(totalCompleted.value, totalAppointments.value)
  
  return [
    {
      title: '总挂号量',
      value: formatNumber(totalAppointments.value),
      suffix: '单',
      detail: '全院预约总量',
      icon: IconCalendar,
      isWarning: false
    },
    {
      title: '退号总量',
      value: formatNumber(totalCancelled.value),
      suffix: '单',
      detail: `退号率 ${formatPercentStr(cancelRate)}`,
      icon: IconCancel,
      isWarning: cancelRate > 0.15 // 简单的逻辑：超过15%预警
    },
    {
      title: '完成就诊',
      value: formatNumber(totalCompleted.value),
      suffix: '单',
      detail: `完成率 ${formatPercentStr(completeRate)}`,
      icon: IconCheck,
      isWarning: false
    },
    {
      title: '总营收',
      value: formatCurrency(totalRevenue.value),
      suffix: '',
      detail: '实收金额',
      icon: IconWallet,
      isWarning: false
    }
  ]
})

const barChartData = computed(() => {
  if (!dailyNewPatients.value.length) return []
  const max = Math.max(...dailyNewPatients.value.map(item => toNumber(item.newPatientCount)), 1)
  return dailyNewPatients.value.map(item => ({
    date: item.date,
    count: toNumber(item.newPatientCount),
    height: Math.max(4, Math.round((toNumber(item.newPatientCount) / max) * 100))
  }))
})

const topCancelledDoctors = computed(() =>
  [...filteredDoctorStats.value]
    .sort((a, b) => toNumber(b.cancelledCount) - toNumber(a.cancelledCount))
    .slice(0, 5)
)

// --- Helpers ---
function formatDate(dateObj) {
  const year = dateObj.getFullYear()
  const month = `${dateObj.getMonth() + 1}`.padStart(2, '0')
  const day = `${dateObj.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatShortDate(dateStr) {
  // 2023-11-27 -> 11/27
  if(!dateStr) return ''
  const parts = dateStr.split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : dateStr
}

function toNumber(val) {
  const num = Number(val)
  return Number.isFinite(num) ? num : 0
}

function sumBy(arr, key) {
  return arr.reduce((sum, item) => sum + toNumber(item[key]), 0)
}

function calcRate(num, den) {
  const d = toNumber(den)
  return d === 0 ? 0 : toNumber(num) / d
}

function formatPercentStr(rate) {
  return `${(rate * 100).toFixed(2)}%`
}

function formatNumber(val) {
  return toNumber(val).toLocaleString()
}

function formatCurrency(val) {
  // 使用 CN 格式更专业
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(toNumber(val))
}

function formatPercent(numerator, denominator) {
  return formatPercentStr(calcRate(numerator, denominator))
}

function isHighCancelRate(cancelled, total) {
  return calcRate(cancelled, total) > 0.15
}

function validateDates() {
  if (!filters.startDate || !filters.endDate) {
    errorMessage.value = '请选择完整的统计周期'
    return false
  }
  if (filters.startDate > filters.endDate) {
    errorMessage.value = '开始日期不能晚于结束日期'
    return false
  }
  return true
}

// --- API ---
async function fetchStatistics() {
  if (!validateDates()) return
  
  loading.value = true
  errorMessage.value = ''
  
  const params = {
    startDate: filters.startDate,
    endDate: filters.endDate
  }
  const headers = token ? { Authorization: `Bearer ${token}` } : {}

  try {
    // 优化：使用 /api/statistics/dashboard 一次性获取所有数据
    const { data: res } = await axios.get('/api/statistics/dashboard', { params, headers })
    
    if (res.code === 200 || res.success) { // 兼容通用 Result 结构
      const d = res.data || {}
      // 映射后端字段到前端变量
      departmentStats.value = d.departmentAppointments || []
      doctorStats.value = d.doctorAppointments || []
      dailyNewPatients.value = d.dailyNewPatients || []
    } else {
      throw new Error(res.message || '请求未成功')
    }
  } catch (error) {
    console.error(error)
    errorMessage.value = error.message || '获取数据失败，请检查网络'
  } finally {
    loading.value = false
  }
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

// --- Export Logic (Same as before, just kept concise) ---
function exportCSV() {
  const rows = buildExportRows()
  const csv = rows.map(r => r.map(c => `"${String(c || '').replace(/"/g, '""')}"`).join(',')).join('\n')
  downloadFile(csv, `data_report_${filters.startDate}.csv`, 'text/csv')
}

function exportExcel() {
  const rows = buildExportRows()
  const content = rows.map(r => r.join('\t')).join('\n')
  downloadFile(content, `data_report_${filters.startDate}.xls`, 'application/vnd.ms-excel')
}

function buildExportRows() {
  const head = ['类型', '名称', '科室', '总挂号', '已完成', '退号', '退号率', '总收入']
  const dRows = filteredDeptStats.value.map(i => [
    '科室', i.deptName, '-', i.appointmentCount, i.completedCount, i.cancelledCount, formatPercent(i.cancelledCount, i.appointmentCount), i.totalRevenue
  ])
  const docRows = filteredDoctorStats.value.map(i => [
    '医生', i.doctorName, i.deptName, i.appointmentCount, i.completedCount, i.cancelledCount, formatPercent(i.cancelledCount, i.appointmentCount), i.totalRevenue
  ])
  return [head, ...dRows, ...docRows]
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
  navHeight.value = (navRef.value?.$el?.offsetHeight || 60) + 20
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
/* 全局容器 */
.page-container {
  max-width: 1400px; /* 更宽的视野 */
  margin: 0 auto;
  padding: 20px 24px 40px;
  color: #1e293b;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 头部样式 */
.dashboard-header {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.badge {
  display: inline-block;
  background: #e0f2fe;
  color: #0284c7;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 99px;
  margin-bottom: 8px;
}

h1 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #0f172a;
  font-weight: 700;
}

.description {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

/* 按钮组 */
.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn.primary {
  background: #0f172a; /* 深色沉稳 */
  color: white;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.15);
}
.btn.primary:hover { background: #1e293b; transform: translateY(-1px); }

.btn.ghost {
  background: #f1f5f9;
  color: #475569;
}
.btn.ghost:hover { background: #e2e8f0; }

.btn.text { background: transparent; color: #64748b; }
.btn.small { height: 32px; padding: 0 12px; font-size: 13px; }

/* 筛选栏 */
.filters-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
  font-weight: 600;
}

.date-inputs {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 4px 8px;
}
.date-inputs input {
  border: none;
  outline: none;
  font-family: inherit;
  color: #334155;
  font-size: 13px;
}
.separator { margin: 0 8px; color: #94a3b8; font-size: 12px; }

.select-wrapper select {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #fff;
  min-width: 140px;
  font-size: 13px;
  color: #334155;
}

/* KPI 卡片 */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.kpi-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  transition: transform 0.2s;
}
.kpi-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.04); }

.kpi-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.icon-0 { background: linear-gradient(135deg, #3b82f6, #2563eb); box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2); }
.icon-1 { background: linear-gradient(135deg, #f97316, #ea580c); box-shadow: 0 4px 12px rgba(234, 88, 12, 0.2); }
.icon-2 { background: linear-gradient(135deg, #10b981, #059669); box-shadow: 0 4px 12px rgba(5, 150, 105, 0.2); }
.icon-3 { background: linear-gradient(135deg, #8b5cf6, #7c3aed); box-shadow: 0 4px 12px rgba(124, 58, 237, 0.2); }

.kpi-data { flex: 1; }
.kpi-label { margin: 0; color: #64748b; font-size: 13px; font-weight: 500; }
.kpi-value-group { display: flex; align-items: baseline; gap: 4px; margin: 4px 0; }
.kpi-value { font-size: 26px; font-weight: 700; color: #0f172a; line-height: 1.2; }
.kpi-unit { font-size: 13px; color: #94a3b8; }
.kpi-footer { font-size: 12px; color: #94a3b8; margin: 0; }
.kpi-footer.warning-text { color: #ea580c; font-weight: 600; }

/* 图表与列表 */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}
@media (max-width: 900px) { .charts-grid { grid-template-columns: 1fr; } }

.panel {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.panel-header h3 { margin: 0; font-size: 16px; color: #0f172a; font-weight: 700; }
.muted { color: #94a3b8; font-size: 12px; margin: 2px 0 0; font-weight: 400; }
.stat-tag { background: #f1f5f9; color: #475569; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

/* CSS 柱状图优化 */
.chart-container { height: 240px; width: 100%; }
.simple-bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 100%;
  padding-bottom: 24px; /* Space for labels */
  gap: 6px;
}
.chart-column-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  position: relative;
}
.chart-column {
  width: 100%;
  max-width: 24px;
  background: #cbd5e1;
  border-radius: 6px 6px 2px 2px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  cursor: pointer;
}
.chart-column:hover {
  background: #3b82f6;
  transform: scaleY(1.05);
}
.chart-column.high-value { background: #93c5fd; }
.chart-column.high-value:hover { background: #2563eb; }

.x-axis-label {
  position: absolute;
  bottom: -24px;
  font-size: 10px;
  color: #94a3b8;
  white-space: nowrap;
  transform: scale(0.9);
}

/* Tooltip */
.tooltip {
  position: absolute;
  bottom: 110%;
  left: 50%;
  transform: translateX(-50%);
  background: #1e293b;
  color: #fff;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 11px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
  text-align: center;
}
.chart-column:hover .tooltip { opacity: 1; }
.tooltip-date { opacity: 0.7; margin-bottom: 2px; font-size: 10px; }
.tooltip-val { font-weight: 700; }

/* 排行榜 */
.ranking-list { display: flex; flex-direction: column; gap: 12px; }
.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid transparent;
  transition: background 0.2s;
}
.rank-item:hover { background: #fff; border-color: #e2e8f0; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }

.rank-index {
  width: 24px;
  height: 24px;
  background: #e2e8f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}
.rank-index.top-1 { background: #fef3c7; color: #d97706; }
.rank-index.top-2 { background: #e0f2fe; color: #0284c7; }
.rank-index.top-3 { background: #ffedd5; color: #c2410c; }

.rank-info { flex: 1; overflow: hidden; }
.rank-name { font-weight: 600; font-size: 14px; color: #334155; }
.rank-dept { font-size: 12px; color: #94a3b8; }
.rank-stat { text-align: right; }
.stat-val { font-weight: 700; font-size: 15px; color: #0f172a; }
.stat-val.warning { color: #ef4444; }
.stat-val .unit { font-size: 11px; color: #94a3b8; font-weight: 400; }
.stat-sub { font-size: 11px; color: #94a3b8; }

/* 表格详情 */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}
.table-panel { min-height: 400px; }

.table-container { overflow-x: auto; flex: 1; }
table { width: 100%; border-collapse: separate; border-spacing: 0; }
th {
  background: #f8fafc;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  padding: 12px 16px;
  text-align: left;
  position: sticky;
  top: 0;
  z-index: 1;
  border-bottom: 1px solid #e2e8f0;
}
td {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
  color: #475569;
}
tr:last-child td { border-bottom: none; }
tr:hover td { background: #fcfcfc; }

.text-right { text-align: right; }
.font-medium { font-weight: 600; color: #334155; }
.font-num { font-family: 'SF Mono', Consolas, monospace; letter-spacing: -0.5px; }
.text-warning { color: #ef4444; font-weight: 600; }
.muted-text { color: #94a3b8; }

.progress-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.mini-bar-bg { width: 50px; height: 4px; background: #e2e8f0; border-radius: 2px; overflow: hidden; }
.mini-bar { height: 100%; border-radius: 2px; }

/* 状态点 */
.dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
.dot.warning { background: #ef4444; }

/* 通用状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 14px;
  min-height: 150px;
}
.empty-icon { font-size: 32px; margin-bottom: 8px; opacity: 0.5; }
.empty-row { text-align: center; padding: 40px; color: #94a3b8; }
.alert.error { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 12px; border-radius: 8px; margin-bottom: 16px; font-size: 13px; display: flex; align-items: center; gap: 8px; }

@media (max-width: 600px) {
  .header-content { flex-direction: column; }
  .header-actions { margin-top: 12px; width: 100%; }
  .btn { flex: 1; justify-content: center; }
  .kpi-grid { grid-template-columns: 1fr 1fr; }
  .chart-column { max-width: 12px; }
}
</style>