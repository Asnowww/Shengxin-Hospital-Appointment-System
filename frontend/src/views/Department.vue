<template>
  <Navigation ref="navRef" />
  <Notice :visible="showNotice" @close="handleNoticeClose" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="overview-wrapper">
      <!-- 页面标题和搜索 -->
      <div class="header-section">
        <h1>科室预约</h1>
        
        <div class="search-container">
          <div class="search-box">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="search-icon">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="搜索医生姓名、科室、疾病..."
              @input="handleSearch"
            />
            <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>搜索中...</p>
      </div>

      <!-- 搜索结果 -->
<div v-if="hasSearched" class="search-results">
  <div class="results-grid">
    <!-- 医生列 -->
    <div class="results-column">
      <div class="column-header">
        <h2>医生 ({{ doctorTotal}})</h2>
      </div>

      <div class="column-content">
        <template v-if="matchedDoctors.length > 0">
          <div 
            v-for="doctor in matchedDoctors" 
            :key="doctor.id"
            class="result-card doctor-card"
            @click="handleDoctorClick(doctor)"
          >
            <div class="card-avatar">{{ doctor.name.charAt(0) }}</div>

            <div class="card-main">
              <div class="card-name">{{ doctor.name }}</div>
              <div class="card-title">{{ doctor.title }}</div>
              <div class="card-dept">{{ doctor.deptName }}</div>
              <div v-if="doctor.description" class="card-description">
                {{ truncateText(doctor.description, 60) }}
              </div>
            </div>

            <div class="card-arrow">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18"
                   viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>

          <!-- ⭐ 查看更多医生 -->
          <button
            v-if="doctorTotal > matchedDoctors.length"
            class="load-more-btn"
            type="button"
            @click="loadMoreDoctors"
          >
            <span>查看更多医生</span>
            <span class="count">
              {{ matchedDoctors.length }} / {{ doctorTotal }}
            </span>

            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
        </template>

        <div v-else class="empty-column">
          <p>没有查询到符合的结果</p>
        </div>
      </div>
    </div>

    <!-- 科室列 -->
    <div class="results-column">
      <div class="column-header">
        <h2>科室 ({{ matchedDepartments.length }})</h2>
      </div>
      <div class="column-content">
        <template v-if="matchedDepartments.length > 0">
          <div 
            v-for="dept in matchedDepartments" 
            :key="dept.id"
            class="result-card dept-card"
            @click="handleDepartmentClick(dept)">
            
            <div class="card-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 9h12M6 9a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2M6 9v10a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V9M10 14h4"></path>
              </svg>
            </div>

            <div class="card-main">
              <div class="card-name">{{ dept.deptName }}</div>
              <div class="card-dept-info">
                <span v-if="dept.building" class="dept-detail">{{ dept.building }}</span>
                <span v-if="dept.floor" class="dept-detail">{{ dept.floor }}楼</span>
              </div>
              <div v-if="dept.description" class="card-description">
                {{ truncateText(dept.description, 60) }}
              </div>
            </div>

            <div class="card-arrow">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>
        </template>

        <div v-else class="empty-column">
          <p>没有查询到符合的结果</p>
        </div>
      </div>
    </div>
  </div>
</div>

      <!-- 默认科室列表 -->
      <div v-else class="departments-list">
        <div 
          v-for="dept in departments" 
          :key="dept.id"
          class="department-group">
          
          <!-- 一级科室标题 -->
          <div class="primary-department">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
            </svg>
            <h2>{{ dept.name }}</h2>
            <span class="sub-count">{{ (dept.subDepartments || []).length }}</span>
          </div>

          <!-- 二级科室卡片 -->
          <div class="sub-departments">
            <div 
              v-for="subDept in (dept.subDepartments || [])" 
              :key="subDept.id"
              class="sub-dept-card"
              @click="handleSubDepartmentClick(subDept)">
              
              <div class="card-content">
                <h3>{{ subDept.name }}</h3>
              </div>

              <svg class="arrow" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'
import Notice from '@/components/Notice.vue'

const showNotice = ref(false)

const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const searchQuery = ref('')
const departments = ref([])
const matchedDoctors = ref([])
const matchedDepartments = ref([])
const hasSearched = ref(false)
const doctorDisplayLimit = ref(20)
const doctorTotal = ref(0)

function clearSearch() {
  searchQuery.value = ''
  matchedDoctors.value = []
  matchedDepartments.value = []
  hasSearched.value = false
}

// 文本截断
function truncateText(text, length) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

let searchTimeout = null
function handleSearch() {
  const query = searchQuery.value.trim().toLowerCase()

  if (!query) {
    clearSearch()
    return
  }

  hasSearched.value = true
  clearTimeout(searchTimeout)

  // ⭐ 只有“新搜索”才重置
  doctorDisplayLimit.value = 20

  searchTimeout = setTimeout(fetchSearchResult, 300)
}

async function fetchSearchResult() {
  loading.value = true
  try {
    const res = await axios.get('/api/patient/search', {
      params: { keyword: searchQuery.value.trim() }
    })

    const data = res.data.data || {}

    /* ===== 医生 ===== */
    const doctors = data.doctors || []
    doctorTotal.value = doctors.length

    matchedDoctors.value = doctors
      .slice(0, doctorDisplayLimit.value)
      .map(d => ({
        id: d.doctorId,
        name: d.doctorName,
        deptName: d.deptName,
        title: d.title,
        description: d.bio
      }))

    /* ===== 科室（你原来的逻辑，未动） ===== */
    const rawDepts = data.departments || []

    const matchedPrimaryDeptIds = rawDepts
      .filter(d => !d.parentDeptId)
      .map(d => d.deptId)

    const directMatchedSubDepts = rawDepts.filter(d => d.parentDeptId)

    const expandedSubDepts = departments.value
      .filter(p => matchedPrimaryDeptIds.includes(p.id))
      .flatMap(p => p.subDepartments || [])

    const deptMap = new Map()
    ;[...directMatchedSubDepts, ...expandedSubDepts].forEach(d => {
      deptMap.set(d.id || d.deptId, d)
    })

    matchedDepartments.value = Array.from(deptMap.values()).map(d => ({
      id: d.id || d.deptId,
      deptName: d.name || d.deptName,
      building: d.building,
      floor: d.floor,
      description: d.description
    }))

  } catch (err) {
    console.error('搜索失败', err)
    matchedDoctors.value = []
    matchedDepartments.value = []
  } finally {
    loading.value = false
  }
}


function loadMoreDoctors() {
  doctorDisplayLimit.value += 20
  fetchSearchResult()
}

// 加载所有科室信息（仅在页面初始加载时）
async function fetchDepartments() {
  try {
    const res = await axios.get('/api/departments/all')
    const list = res.data?.data || []

    // 过滤掉没有子科室的顶层部门
    departments.value = list
      .filter(dept => Array.isArray(dept.children) && dept.children.length > 0)
      .map(dept => ({
        id: dept.deptId,
        name: dept.deptName,
        description: dept.description,
        building: dept.building,
        floor: dept.floor,
        subDepartments: dept.children.map(sub => ({
          id: sub.deptId,
          name: sub.deptName,
          description: sub.description,
          building: sub.building,
          floor: sub.floor,
        }))
      }))

    console.log('部门层级数据', departments.value)
  } catch (err) {
    console.error('获取科室失败', err)
  }
}


// 处理医生点击
function handleDoctorClick(doctor) {
  // 导航到该医生的预约页面
  router.push({
    path: '/doctorSchedule',
    query: { deptId: doctor.deptId, deptName: doctor.deptName, doctorId: doctor.id ,doctorName: doctor.name }
  })
}

// 处理科室点击
function handleDepartmentClick(dept) {
  //导航到该科室的预约页面
  router.push({
    path: '/departmentSchedule',
    query: { deptId: dept.id || dept.deptId, deptName: dept.name || dept.deptName }
  })
}

// 处理二级科室点击
function handleSubDepartmentClick(subDept) {
  router.push({
    path: '/departmentSchedule',
    query: { deptId: subDept.id, deptName: subDept.name }
  })
}

const handleNoticeClose = () => {
  showNotice.value = false
}

function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30
  }
}

function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  showNotice.value = true // 这里设置为每次进入页面直接弹出
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)

  // 只加载科室列表，用于默认显示
  await fetchDepartments()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
}

.overview-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 140px);
}

/* 头部区域 */
.header-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

h1 {
  margin: 0 0 1rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.search-container {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 1rem;
  color: #a0aec0;
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-btn {
  position: absolute;
  right: 1rem;
  background: #e2e8f0;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: #cbd5e0;
}

/* 搜索结果网格 */
.search-results {
  margin-bottom: 2rem;
}

.results-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.results-column {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.column-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  flex-shrink: 0;
}

.column-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.column-content {
  flex: 1;
  overflow-y: auto;
  max-height: 600px;
  padding: 1rem;
}

/* 结果卡片 */
.result-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 0.75rem;
}

.result-card:hover {
  background: #f0f4ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateX(4px);
}

.result-card:last-child {
  margin-bottom: 0;
}

/* 医生卡片 */
.doctor-card .card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.25rem;
  flex-shrink: 0;
}

/* 查看更多医生 */
.load-more-btn {
  width: 100%;
  margin-top: 0.75rem;
  padding: 0.75rem 1rem;

  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;

  border: none;
  border-radius: 12px;

  font-size: 0.9rem;
  font-weight: 600;

  cursor: pointer;
  transition: all 0.25s ease;
}

/* hover */
.load-more-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
}

/* 点击反馈 */
.load-more-btn:active {
  transform: translateY(0);
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.25);
}

/* 数量 */
.load-more-btn .count {
  font-size: 0.8rem;
  font-weight: 500;
  opacity: 0.85;
}

/* 小箭头动画 */
.load-more-btn svg {
  transition: transform 0.25s ease;
}

.load-more-btn:hover svg {
  transform: translateY(2px);
}

/* 科室卡片 */
.dept-card .card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 1rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 0.25rem;
}

.card-title {
  font-size: 0.85rem;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.card-dept {
  font-size: 0.8rem;
  color: #718096;
  margin-bottom: 0.5rem;
}

.card-description {
  font-size: 0.8rem;
  color: #718096;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-dept-info {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
}

.dept-detail {
  color: #718096;
  font-weight: 500;
  background: #f0f4ff;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.card-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a0aec0;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.result-card:hover .card-arrow {
  color: #667eea;
  transform: translateX(4px);
}

/* 空状态 */
.empty-column {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #a0aec0;
  font-size: 0.9rem;
  min-height: 150px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  background: white;
  border-radius: 16px;
  color: #718096;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}

.empty-state p {
  margin: 0.5rem 0;
  font-size: 1rem;
}

.empty-hint {
  font-size: 0.85rem !important;
  color: #a0aec0 !important;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  background: white;
  border-radius: 16px;
  color: #718096;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 科室列表（默认显示） */
.departments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.department-group {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.primary-department {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  margin-bottom: 1rem;
  background: #f7fafc;
  border-radius: 10px;
}

.primary-department svg {
  color: #667eea;
}

.primary-department h2 {
  margin: 0;
  color: #4a5568;
  font-size: 1.125rem;
  font-weight: 600;
  flex: 1;
}

.sub-count {
  color: #a0aec0;
  font-size: 0.875rem;
  background: white;
  padding: 0.25rem 0.625rem;
  border-radius: 12px;
  font-weight: 500;
}

.sub-departments {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.sub-dept-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.sub-dept-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-content h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

.arrow {
  color: #a0aec0;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.sub-dept-card:hover .arrow {
  color: #667eea;
  transform: translateX(4px);
}

/* 响应式 */
@media (max-width: 1024px) {
  .results-grid {
    grid-template-columns: 1fr;
  }

  .column-content {
    max-height: 500px;
  }

  .sub-departments {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .overview-wrapper {
    padding: 1rem;
  }

  .header-section {
    padding: 1.25rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .results-grid {
    grid-template-columns: 1fr;
  }

  .column-content {
    max-height: 400px;
  }

  .department-group {
    padding: 1rem;
  }

  .sub-departments {
    grid-template-columns: 1fr;
  }

  .result-card {
    gap: 0.75rem;
    padding: 0.75rem;
  }

  .card-avatar,
  .card-icon {
    width: 40px;
    height: 40px;
    min-width: 40px;
  }
}
</style>