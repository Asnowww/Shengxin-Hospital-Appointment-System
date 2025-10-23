
<template>
  <Navigation ref="navRef" />
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
              placeholder="搜索科室名称或医生姓名..."
              @input="handleSearch"
            />
            <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <button @click="showAdvancedSearch = !showAdvancedSearch" class="filter-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
            </svg>
            高级筛选
          </button>
        </div>

        <!-- 高级搜索面板 -->
        <transition name="slide">
          <div v-if="showAdvancedSearch" class="advanced-search">
            <div class="search-row">
              <div class="search-field">
                <label>就诊日期</label>
                <input v-model="searchFilters.date" type="date" :min="minDate" />
              </div>
              <div class="search-field">
                <label>时间段</label>
                <select v-model="searchFilters.timeSlot">
                  <option value="">不限</option>
                  <option value="morning">上午 (08:00-12:00)</option>
                  <option value="afternoon">下午 (14:00-17:00)</option>
                </select>
              </div>
              <div class="search-field">
                <label>医生职称</label>
                <select v-model="searchFilters.title">
                  <option value="">不限</option>
                  <option value="主治医师">主治医师</option>
                  <option value="副主任医师">副主任医师</option>
                  <option value="主任医师">主任医师</option>
                </select>
              </div>
            </div>
            <div class="search-actions">
              <button @click="resetFilters" class="reset-btn">重置</button>
              <button @click="applyFilters" class="apply-btn">应用筛选</button>
            </div>
          </div>
        </transition>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 搜索结果为空 -->
      <div v-else-if="searchQuery && filteredDepartments.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <p>未找到相关科室</p>
      </div>

      <!-- 医生搜索结果 -->
      <div v-if="searchMode === 'doctor'" class="doctor-list">
        <DoctorCard
          v-for="doc in matchedDoctors"
          :key="doc.id"
          :doctor="doc"
          :getCategoryLabel="getCategoryLabel"
          :onAppointment="handleAppointment"
        />
      </div>

      <!-- 科室列表 -->
      <div v-else-if="searchMode === 'department'" class="departments-list">
        <div 
          v-for="dept in filteredDepartments" 
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
              @click="handleDepartmentClick(subDept)">
              
              <div class="card-content">
                <h3>{{ subDept.name }}</h3>
                <div class="card-info">
                  <span class="doctor-count">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </span>
                  <span :class="['status', subDept.available ? 'available' : 'unavailable']">
                    {{ subDept.available ? '可预约' : '暂满' }}
                  </span>
                </div>
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
import DoctorCard from '@/components/DoctorCard.vue'

const router = useRouter()
const searchMode = ref('department')
const matchedDoctors = ref([])

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const searchQuery = ref('')
const departments = ref([])
const doctorIndex = ref([])
const showAdvancedSearch = ref(false)

const searchFilters = ref({
  date: '',
  timeSlot: '',
  title: ''
})

const minDate = computed(() => new Date().toISOString().split('T')[0])

function clearSearch() {
  searchQuery.value = ''
  searchMode.value = 'department'
  matchedDoctors.value = []
}

let searchTimeout = null
function handleSearch() {
  const query = searchQuery.value.trim()
  if (!query) {
    clearSearch()
    return
  }

  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    loading.value = true
    try {
      const { data } = await axios.get('/api/doctors/search', {
        params: {
          keyword: query,
          ...searchFilters.value
        }
      })
      matchedDoctors.value = data || []
      searchMode.value = matchedDoctors.value.length > 0 ? 'doctor' : 'department'
    } catch (err) {
      console.error('搜索医生失败', err)
    } finally {
      loading.value = false
    }
  }, 400)
}

//用于初始加载所有一级及二级科室信息
async function fetchDepartments() {
  loading.value = true
  try {
    const res = await axios.get('/api/departments/all')
    const depts = res.data?.data || []
    // 统一处理 subDepartments 为 null
    departments.value = depts.map(d => ({
      ...d,
      subDepartments: d.subDepartments || []
    }))
  } catch (err) {
    console.error('获取科室列表失败', err)
  } finally {
    loading.value = false
  }
}

// async function fetchDoctorIndex() {
//   try {
//     const { data } = await axios.get('/api/doctors/brief')
//     doctorIndex.value = data
//   } catch (err) {
//     console.error('加载医生索引失败', err)
//   }
// }

async function applyFilters() {
  await fetchDepartments()
}

//点击看详细信息
function handleDepartmentClick(subDept) {
  router.push({
    path: '/departmentSchedule',
    query: { deptId: subDept.id, deptName: subDept.name }
  })
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
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)

  await Promise.all([fetchDepartments(), fetchDoctorIndex()])
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 计算过滤后的科室列表
const filteredDepartments = computed(() => {
  if (!searchQuery.value) return departments.value
  return departments.value.filter(dept => dept.name.includes(searchQuery.value))
})
</script>


<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.overview-wrapper {
  max-width: 1200px;
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

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 高级搜索面板 */
.advanced-search {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: #f7fafc;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.search-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.search-field label {
  display: block;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.search-field input,
.search-field select {
  width: 100%;
  padding: 0.625rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.search-field input:focus,
.search-field select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.reset-btn,
.apply-btn {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-btn {
  background: white;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.reset-btn:hover {
  background: #f7fafc;
}

.apply-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.apply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 滑入动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  background: white;
  border-radius: 16px;
  color: #718096;
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

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}

/* 科室列表 */
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

/* 一级科室 */
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

/* 二级科室网格 */
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
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
}

.doctor-count {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #718096;
}

.doctor-count svg {
  color: #667eea;
}

.status {
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.75rem;
}

.status.available {
  background: #d4edda;
  color: #28a745;
}

.status.unavailable {
  background: #f8d7da;
  color: #721c24;
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

  .search-container {
    flex-direction: column;
  }

  .filter-btn {
    width: 100%;
    justify-content: center;
  }

  .search-row {
    grid-template-columns: 1fr;
  }

  .department-group {
    padding: 1rem;
  }

  .sub-departments {
    grid-template-columns: 1fr;
  }
}
</style>