<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="overview-wrapper">
      <!-- 页面标题和搜索 -->
      <div class="header-section">
        <h1>科室预约</h1>
        
        <div class="search-box">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="search-icon">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="搜索科室名称..."
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

      <!-- 科室列表 -->
      <div v-else class="departments-list">
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
            <span class="sub-count">{{ dept.subDepartments.length }}</span>
          </div>

          <!-- 二级科室卡片 -->
          <div class="sub-departments">
            <div 
              v-for="subDept in dept.subDepartments" 
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
                    {{ subDept.doctorCount }}
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
import Navigation from '@/components/Navigation.vue'
// import axios from 'axios'

const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const searchQuery = ref('')
const departments = ref([])

// 模拟数据（后端接口完成后替换）
const mockDepartments = [
  {
    id: 1,
    name: '内科',
    subDepartments: [
      { id: 101, code: 'CARDIO', name: '心内科', doctorCount: 12, available: true },
      { id: 102, code: 'GASTRO', name: '消化内科', doctorCount: 10, available: true },
      { id: 103, code: 'RESPIR', name: '呼吸内科', doctorCount: 8, available: false },
      { id: 104, code: 'ENDOCR', name: '内分泌科', doctorCount: 6, available: true },
      { id: 105, code: 'NEPHRO', name: '肾内科', doctorCount: 7, available: true }
    ]
  },
  {
    id: 2,
    name: '外科',
    subDepartments: [
      { id: 201, code: 'GENSU', name: '普通外科', doctorCount: 15, available: true },
      { id: 202, code: 'ORTHO', name: '骨科', doctorCount: 18, available: true },
      { id: 203, code: 'NEURO', name: '神经外科', doctorCount: 9, available: true },
      { id: 204, code: 'UROLO', name: '泌尿外科', doctorCount: 8, available: false }
    ]
  },
  {
    id: 3,
    name: '妇产科',
    subDepartments: [
      { id: 301, code: 'GYNEC', name: '妇科', doctorCount: 14, available: true },
      { id: 302, code: 'OBSTE', name: '产科', doctorCount: 12, available: true },
      { id: 303, code: 'FAMPL', name: '计划生育科', doctorCount: 5, available: true }
    ]
  },
  {
    id: 4,
    name: '儿科',
    subDepartments: [
      { id: 401, code: 'PEDIM', name: '小儿内科', doctorCount: 16, available: true },
      { id: 402, code: 'PEDSU', name: '小儿外科', doctorCount: 8, available: true },
      { id: 403, code: 'NEONA', name: '新生儿科', doctorCount: 10, available: false }
    ]
  },
  {
    id: 5,
    name: '五官科',
    subDepartments: [
      { id: 501, code: 'OPHTH', name: '眼科', doctorCount: 11, available: true },
      { id: 502, code: 'OTOLA', name: '耳鼻喉科', doctorCount: 9, available: true },
      { id: 503, code: 'DENTA', name: '口腔科', doctorCount: 13, available: true }
    ]
  },
  {
    id: 6,
    name: '皮肤科',
    subDepartments: [
      { id: 601, code: 'DERMA', name: '皮肤科', doctorCount: 7, available: true },
      { id: 602, code: 'COSME', name: '医学美容科', doctorCount: 5, available: true }
    ]
  },
  {
    id: 7,
    name: '中医科',
    subDepartments: [
      { id: 701, code: 'TCMIN', name: '中医内科', doctorCount: 8, available: true },
      { id: 702, code: 'ACUPU', name: '针灸推拿科', doctorCount: 6, available: true },
      { id: 703, code: 'TCMGY', name: '中医妇科', doctorCount: 4, available: false }
    ]
  }
]

// 搜索过滤
const filteredDepartments = computed(() => {
  if (!searchQuery.value.trim()) {
    return departments.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return departments.value
    .map(dept => {
      const filteredSubs = dept.subDepartments.filter(sub => 
        sub.name.toLowerCase().includes(query)
      )
      
      if (filteredSubs.length > 0) {
        return {
          ...dept,
          subDepartments: filteredSubs
        }
      }
      return null
    })
    .filter(dept => dept !== null)
})

// 获取科室数据
async function fetchDepartments() {
  loading.value = true
  try {
    // 实际使用时替换为：
    // const { data } = await axios.get('/api/departments')
    // departments.value = data
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 600))
    departments.value = mockDepartments
  } catch (err) {
    console.error('获取科室列表失败', err)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  // 搜索逻辑已通过computed实现
}

// 清除搜索
function clearSearch() {
  searchQuery.value = ''
}

// 点击科室
function handleDepartmentClick(subDept) {
  console.log('选择科室:', subDept)
  // 跳转到医生列表页，使用 depCode 作为参数
  router.push({
    path: '/departmentDetail',
    query: { 
      depCode: subDept.code,
      depName: subDept.name 
    }
  })
}

// 导航栏高度管理
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
  fetchDepartments()
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
  display: flex;
  align-items: center;
  gap: 2rem;
}

h1 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
  white-space: nowrap;
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
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
    padding: 1.25rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .department-group {
    padding: 1rem;
  }

  .sub-departments {
    grid-template-columns: 1fr;
  }
}
</style>