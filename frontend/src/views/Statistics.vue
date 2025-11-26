<template>
  <Navigation ref="navRef" />
  <div class="page" :style="{ paddingTop: navHeight + 'px' }">
    <header class="page-header">
      <div>
        <p class="subtitle">数据统计与分析</p>
        <h1>挂号总量 · 退号监测 · 资源效率</h1>
        <p class="muted">全院运营数据监控，支持多维度挂号率与退号率分析，辅助资源调配决策。</p>
      </div>
      <div class="header-actions">
        <button class="btn ghost" :disabled="loading" @click="fetchStatistics">
          <span v-if="loading">加载中...</span>
          <span v-else>刷新数据</span>
        </button>
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
          <option v-for="dept in departmentOptions" :key="dept.deptId" :value="String(dept.deptId)">
            {{ dept.deptName }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <label>医生</label>
        <select v-model="filters.doctorId">
          <option value="">全部医生</option>
          <option v-for="doc in doctorOptions" :key="doc.doctorId" :value="String(doc.doctorId)">
            {{ doc.doctorName }}
          </option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn primary" :disabled="loading" @click="applyFilters">查询</button>
        <button class="btn ghost" :disabled="loading" @click="resetFilters">重置</button>
      </div>
    </section>

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <section class="summary-grid">
      <div class="card" v-for="(card, index) in summaryCards" :key="index">
        <div class="card-header">
          <p class="card-label">{{ card.subtitle }}</p>
          <p class="card-title">{{ card.title }}</p>
        </div>
        <div class="card-value" :class="card.colorClass">
          {{ card.value }}<span class="suffix">{{ card.suffix }}</span>
        </div>
        <div class="card-footer">{{ card.detail }}</div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>核心指标趋势分析</h3>
          <p class="muted">双轴对比：左轴为人数，右轴为比率 (挂号率/退号率)</p>
        </div>
        <div class="toggle-group">
          <button 
            v-for="mode in ['trend', 'dept', 'doc']" 
            :key="mode"
            class="toggle-btn" 
            :class="{ active: chartViewMode === mode }"
            @click="switchChartView(mode)">
            {{ mode === 'trend' ? '时间趋势' : (mode === 'dept' ? '科室对比' : '医生对比') }}
          </button>
        </div>
      </div>
      <div class="chart-container" ref="mainChartRef"></div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>详细数据报表</h3>
          <p class="muted">包含挂号数、排班数、挂号率(利用率)、退号数、退号率</p>
        </div>
      </div>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>总挂号数</th>
              <th>排班限额</th>
              <th>挂号率 (利用率)</th>
              <th>退号数</th>
              <th>退号率</th>
              <th>总收入</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in detailedTableData" :key="row.id">
              <td class="font-medium">{{ row.name }}</td>
              <td><span class="badge" :class="row.type === '科室' ? 'badge-blue' : 'badge-green'">{{ row.type }}</span></td>
              <td>{{ formatNumber(row.appointments) }}</td>
              <td>{{ formatNumber(row.capacity) }}</td>
              <td :class="row.utilizationRate > 90 ? 'text-warning' : ''">
                {{ row.utilizationRate }}%
              </td>
              <td class="text-danger">{{ formatNumber(row.cancelled) }}</td>
              <td :class="row.cancellationRate > 15 ? 'text-danger-bold' : ''">
                {{ row.cancellationRate }}%
              </td>
              <td>{{ formatCurrency(row.revenue) }}</td>
            </tr>
            <tr v-if="!detailedTableData.length">
              <td colspan="8" class="empty">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, nextTick, watch } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import Navigation from '@/components/Navigation.vue'

// --- 基础状态 ---
const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const errorMessage = ref('')
const token = localStorage.getItem('token') || ''
const chartViewMode = ref('trend') // 'trend' | 'dept' | 'doc'

// --- 日期处理 ---
const today = new Date()
const formatDate = (date) => date.toISOString().split('T')[0]
const defaultEnd = formatDate(today)
const defaultStart = formatDate(new Date(today.getTime() - 29 * 24 * 60 * 60 * 1000))

const filters = reactive({
  startDate: defaultStart,
  endDate: defaultEnd,
  deptId: '',
  doctorId: ''
})

// --- 数据源 ---
const departmentStats = ref([])
const doctorStats = ref([])
const utilizationStats = ref([]) // 新增：排班利用率数据
const dailyNewPatients = ref([])

// --- ECharts 实例 ---
const mainChartRef = ref(null)
let chartInstance = null

// --- 下拉选项计算 ---
const departmentOptions = computed(() => {
  const seen = new Set()
  return departmentStats.value
    .filter(item => !seen.has(item.deptId) && seen.add(item.deptId))
    .map(item => ({ deptId: item.deptId, deptName: item.deptName }))
})

const doctorOptions = computed(() => {
  let list = doctorStats.value
  if (filters.deptId) {
    list = list.filter(item => String(item.deptId) === filters.deptId)
  }
  const seen = new Set()
  return list
    .filter(item => !seen.has(item.doctorId) && seen.add(item.doctorId))
    .map(item => ({ doctorId: item.doctorId, doctorName: item.doctorName }))
})

// --- 核心数据过滤与计算 ---
const filteredData = computed(() => {
  const isDeptFilter = !!filters.deptId
  const isDocFilter = !!filters.doctorId

  // 1. 基础挂号数据
  let deptData = departmentStats.value
  let docData = doctorStats.value
  let utilData = utilizationStats.value

  if (isDeptFilter) {
    deptData = deptData.filter(i => String(i.deptId) === filters.deptId)
    docData = docData.filter(i => String(i.deptId) === filters.deptId)
    utilData = utilData.filter(i => String(i.deptId) === filters.deptId)
  }
  if (isDocFilter) {
    docData = docData.filter(i => String(i.doctorId) === filters.doctorId)
    // 医生筛选下，utilData 也需要过滤 (假设 utilizationStats 有 doctorId 字段)
    utilData = utilData.filter(i => String(i.doctorId) === filters.doctorId)
  }

  return { deptData, docData, utilData }
})

// --- KPI 卡片计算 ---
const summaryCards = computed(() => {
  const { deptData, utilData } = filteredData.value
  
  const totalAppts = deptData.reduce((sum, i) => sum + Number(i.appointmentCount || 0), 0)
  const totalCancel = deptData.reduce((sum, i) => sum + Number(i.cancelledCount || 0), 0)
  const totalRev = deptData.reduce((sum, i) => sum + Number(i.totalRevenue || 0), 0)
  
  // 计算总排班容量 (从 utilizationStats 获取)
  const totalCapacity = utilData.reduce((sum, i) => sum + Number(i.totalCapacity || 0), 0) // 假设后端字段名为 totalCapacity
  
  // 挂号率 (利用率)
  const utilRate = totalCapacity > 0 ? ((totalAppts / totalCapacity) * 100).toFixed(1) : '0.0'
  // 退号率
  const cancelRate = totalAppts > 0 ? ((totalCancel / totalAppts) * 100).toFixed(1) : '0.0'

  return [
    { title: '挂号总量', subtitle: '总预约人次', value: formatNumber(totalAppts), detail: '统计周期内数据', suffix: '人' },
    { title: '挂号率 (利用率)', subtitle: '资源饱和度', value: utilRate + '%', detail: `排班总限号 ${formatNumber(totalCapacity)}`, colorClass: 'text-blue' },
    { title: '退号率', subtitle: '流失占比', value: cancelRate + '%', detail: `退号数 ${formatNumber(totalCancel)}`, colorClass: 'text-red' },
    { title: '总收入', subtitle: '实收金额', value: formatCurrency(totalRev), detail: '已扣除退款', suffix: '' }
  ]
})

// --- 表格数据聚合 ---
const detailedTableData = computed(() => {
  const { deptData, docData, utilData } = filteredData.value
  
  // 如果选择了医生，只显示医生列表；否则显示科室列表（或根据需求混合）
  // 这里逻辑：默认显示科室，如果筛选了科室则显示该科室下的医生
  
  let result = []
  
  if (!filters.deptId && !filters.doctorId) {
    // 1. 全院模式：显示各科室数据
    // 需要将 utilData (利用率) 合并到 deptData (挂号/退号)
    result = deptData.map(dept => {
      const util = utilData.find(u => u.deptId === dept.deptId) || {}
      const capacity = Number(util.totalCapacity || dept.appointmentCount) // 兜底
      return {
        id: 'dept-' + dept.deptId,
        name: dept.deptName,
        type: '科室',
        appointments: dept.appointmentCount,
        capacity: capacity,
        cancelled: dept.cancelledCount,
        revenue: dept.totalRevenue,
        utilizationRate: util.utilizationRate || (capacity ? ((dept.appointmentCount/capacity)*100).toFixed(1) : 0),
        cancellationRate: dept.appointmentCount ? ((dept.cancelledCount/dept.appointmentCount)*100).toFixed(1) : 0
      }
    })
  } else {
    // 2. 筛选模式：显示医生数据
    result = docData.map(doc => {
      // 尝试在 utilData 中找到医生的排班数据
      const util = utilData.find(u => u.doctorId === doc.doctorId) || {}
      const capacity = Number(util.totalCapacity || doc.appointmentCount)
      return {
        id: 'doc-' + doc.doctorId,
        name: doc.doctorName,
        type: '医生',
        appointments: doc.appointmentCount,
        capacity: capacity,
        cancelled: doc.cancelledCount,
        revenue: doc.totalRevenue,
        utilizationRate: util.utilizationRate || (capacity ? ((doc.appointmentCount/capacity)*100).toFixed(1) : 0),
        cancellationRate: doc.appointmentCount ? ((doc.cancelledCount/doc.appointmentCount)*100).toFixed(1) : 0
      }
    })
  }
  
  // 按挂号量排序
  return result.sort((a, b) => b.appointments - a.appointments)
})

// --- API 请求 ---
function buildHeaders() { return token ? { Authorization: `Bearer ${token}` } : {} }

async function fetchStatistics() {
  if (filters.startDate > filters.endDate) {
    errorMessage.value = '开始日期不能晚于结束日期'; return
  }
  loading.value = true
  errorMessage.value = ''
  
  const params = { startDate: filters.startDate, endDate: filters.endDate }
  
  try {
    const [deptRes, docRes, utilRes, newPatientRes] = await Promise.all([
      axios.get('/api/statistics/department-appointments', { params, headers: buildHeaders() }),
      axios.get('/api/statistics/doctor-appointments', { params, headers: buildHeaders() }),
      axios.get('/api/statistics/schedule-utilization', { params, headers: buildHeaders() }), // 新增接口
      axios.get('/api/statistics/daily-new-patients', { params, headers: buildHeaders() })
    ])
    
    departmentStats.value = extractList(deptRes)
    doctorStats.value = extractList(docRes)
    utilizationStats.value = extractList(utilRes)
    dailyNewPatients.value = extractList(newPatientRes)
    
    updateChart()
  } catch (error) {
    console.error(error)
    errorMessage.value = '获取数据失败，请检查网络或时间范围'
  } finally {
    loading.value = false
  }
}

function extractList(res) {
  return Array.isArray(res?.data?.data) ? res.data.data : (Array.isArray(res?.data) ? res.data : [])
}

// --- ECharts 逻辑 ---
function switchChartView(mode) {
  chartViewMode.value = mode
  updateChart()
}

function updateChart() {
  if (!chartInstance) chartInstance = echarts.init(mainChartRef.value)
  
  const mode = chartViewMode.value
  let xAxisData = []
  let seriesData = []
  
  if (mode === 'trend') {
    // 趋势模式：使用 dailyNewPatients 或构造每日聚合数据
    // 这里为了演示挂号/退号趋势，我们假设 dailyNewPatients 只包含新增。
    // 实际项目中最好有一个 /daily-stats 接口。此处用 dailyNewPatients 模拟时间轴
    xAxisData = dailyNewPatients.value.map(i => i.date)
    const counts = dailyNewPatients.value.map(i => i.newPatientCount) // 仅作示例
    
    seriesData = [
      { name: '新增患者', type: 'line', smooth: true, data: counts, itemStyle: { color: '#3b82f6' }, areaStyle: { opacity: 0.1 } }
    ]
  } else {
    // 科室或医生对比模式
    const data = detailedTableData.value.slice(0, 15) // 取前15个避免拥挤
    xAxisData = data.map(i => i.name)
    
    seriesData = [
      {
        name: '总挂号数',
        type: 'bar',
        data: data.map(i => i.appointments),
        itemStyle: { color: '#3b82f6', borderRadius: [4, 4, 0, 0] },
        yAxisIndex: 0
      },
      {
        name: '退号数',
        type: 'bar',
        data: data.map(i => i.cancelled),
        itemStyle: { color: '#ef4444', borderRadius: [4, 4, 0, 0] },
        yAxisIndex: 0
      },
      {
        name: '挂号率(%)',
        type: 'line',
        yAxisIndex: 1, // 右轴
        data: data.map(i => i.utilizationRate),
        itemStyle: { color: '#f59e0b' },
        lineStyle: { type: 'dashed' }
      },
      {
        name: '退号率(%)',
        type: 'line',
        yAxisIndex: 1, // 右轴
        data: data.map(i => i.cancellationRate),
        itemStyle: { color: '#ef4444' }
      }
    ]
  }

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0 },
    grid: { left: '3%', right: '3%', bottom: '10%', top: '15%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: xAxisData, 
      axisLabel: { interval: 0, rotate: mode === 'doc' ? 45 : 0, color: '#6b7280' } 
    },
    yAxis: [
      { type: 'value', name: '人数', splitLine: { lineStyle: { type: 'dashed' } } },
      { type: 'value', name: '比率 (%)', max: 100, splitLine: { show: false }, axisLabel: { formatter: '{value}%' } }
    ],
    series: seriesData
  }
  
  chartInstance.setOption(option, true)
}

// --- 通用工具 ---
function toNumber(v) { return Number(v) || 0 }
function formatNumber(v) { return toNumber(v).toLocaleString() }
function formatCurrency(v) { return '¥' + toNumber(v).toFixed(2) }

function applyFilters() { fetchStatistics() }
function resetFilters() {
  filters.startDate = defaultStart
  filters.endDate = defaultEnd
  filters.deptId = ''; filters.doctorId = ''
  fetchStatistics()
}
function exportExcel() { /* 保持原逻辑 */ alert('导出功能触发') }

// --- 生命周期 ---
function updateNavHeight() {
  if (navRef.value?.$el) navHeight.value = navRef.value.$el.offsetHeight + 24
}

onMounted(() => {
  updateNavHeight()
  window.addEventListener('resize', () => {
    updateNavHeight()
    chartInstance?.resize()
  })
  fetchStatistics()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateNavHeight)
  chartInstance?.dispose()
})
</script>

<style scoped>
/* 保持你原有的 CSS 风格，增加少量新样式 */
.page { max-width: 1200px; margin: 0 auto; padding: 24px; box-sizing: border-box; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.subtitle { color: #6b7280; margin: 0 0 4px; font-size: 14px; }
.page-header h1 { margin: 0; font-size: 26px; color: #111827; }
.muted { color: #6b7280; margin: 4px 0 0; font-size: 13px; }
.header-actions { display: flex; gap: 10px; }

/* 按钮通用 */
.btn { border: none; border-radius: 6px; padding: 8px 16px; cursor: pointer; font-weight: 500; font-size: 14px; transition: all 0.2s; }
.btn.primary { background: #3b82f6; color: white; box-shadow: 0 2px 4px rgba(59,130,246,0.3); }
.btn.primary:hover { background: #2563eb; }
.btn.ghost { background: #f3f4f6; color: #374151; }
.btn.ghost:hover { background: #e5e7eb; }

/* 筛选区 */
.filters { display: flex; flex-wrap: wrap; gap: 16px; background: white; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; align-items: flex-end; }
.filter-item { flex: 1; min-width: 180px; }
.filter-item label { display: block; font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 6px; }
.filter-item input, .filter-item select { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
.filter-actions { display: flex; gap: 10px; }

/* KPI 卡片 */
.summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 20px; }
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border: 1px solid #f3f4f6; }
.card-label { font-size: 12px; color: #6b7280; margin: 0 0 4px 0; }
.card-title { font-size: 16px; font-weight: 600; color: #111827; margin: 0; }
.card-value { font-size: 28px; font-weight: 700; color: #111827; margin: 12px 0; }
.card-value.text-blue { color: #3b82f6; }
.card-value.text-red { color: #ef4444; }
.suffix { font-size: 14px; color: #9ca3af; margin-left: 4px; font-weight: 400; }
.card-footer { font-size: 12px; color: #9ca3af; }

/* 图表与面板 */
.panel { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.panel-header h3 { margin: 0; font-size: 18px; color: #111827; }

/* 切换按钮组 */
.toggle-group { display: flex; background: #f3f4f6; padding: 4px; border-radius: 8px; }
.toggle-btn { border: none; background: transparent; padding: 6px 12px; font-size: 13px; color: #6b7280; cursor: pointer; border-radius: 6px; }
.toggle-btn.active { background: white; color: #3b82f6; font-weight: 600; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }

.chart-container { width: 100%; height: 350px; }

/* 表格样式 */
.table-wrapper { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th { text-align: left; padding: 12px 16px; background: #f9fafb; color: #4b5563; font-weight: 600; border-bottom: 1px solid #e5e7eb; }
td { padding: 12px 16px; border-bottom: 1px solid #f3f4f6; color: #1f2937; }
.font-medium { font-weight: 500; }
.text-warning { color: #f59e0b; font-weight: 600; }
.text-danger { color: #ef4444; }
.text-danger-bold { color: #dc2626; font-weight: 700; }
.badge { padding: 2px 8px; border-radius: 99px; font-size: 12px; }
.badge-blue { background: #eff6ff; color: #1d4ed8; }
.badge-green { background: #ecfdf5; color: #047857; }
.empty { text-align: center; color: #9ca3af; padding: 30px; }
.error-banner { background: #fee2e2; color: #b91c1c; padding: 12px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; }
</style>