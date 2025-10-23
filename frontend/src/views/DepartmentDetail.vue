<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="appointment-wrapper">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <span @click="goBack" class="back-link">科室列表</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
        <span class="current">{{ departmentName }}</span>
      </div>

      <!-- 科室信息卡片 -->
      <div class="department-card">
        <div class="dept-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
          </svg>
        </div>
        <div class="dept-info">
          <h1>{{ departmentName }}</h1>
          <p>共 {{ totalDoctors }} 位医生</p>
        </div>
      </div>

      <!-- 医生分类标签 -->
      <div class="category-tabs">
        <button 
          v-for="cat in categories" 
          :key="cat.value"
          :class="['tab-btn', { active: activeCategory === cat.value }]"
          @click="activeCategory = cat.value">
          {{ cat.label }}
          <span class="count">{{ getDoctorCount(cat.value) }}</span>
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载医生信息中...</p>
      </div>

      <!-- 医生列表为空 -->
      <div v-else-if="filteredDoctors.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <p>暂无{{ getCategoryLabel(activeCategory) }}医生</p>
      </div>

      <!-- 医生卡片列表 -->
        <div v-else class="doctors-list">
        <DoctorCard
      v-for="doctor in filteredDoctors"
      :key="doctor.id"
      :doctor="doctor"
      :getCategoryLabel="getCategoryLabel"
      :onAppointment="handleAppointment"
    />
         </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navigation from '@/components/Navigation.vue'
import DoctorCard from '@/components/DoctorCard.vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const activeCategory = ref('all')
const departmentName = ref('')
const doctors = ref([])

const categories = [
  { label: '全部', value: 'all' },
  { label: '普通门诊', value: 'normal' },
  { label: '专家门诊', value: 'expert' },
  { label: '特需门诊', value: 'special' }
]



// 计算总医生数
const totalDoctors = computed(() => doctors.value.length)

// 过滤医生列表
const filteredDoctors = computed(() => {
  if (activeCategory.value === 'all') {
    return doctors.value
  }
  return doctors.value.filter(doctor => doctor.category === activeCategory.value)
})

// 获取分类医生数量
function getDoctorCount(category) {
  if (category === 'all') {
    return doctors.value.length
  }
  return doctors.value.filter(d => d.category === category).length
}

// 获取分类标签
function getCategoryLabel(category) {
  const cat = categories.find(c => c.value === category)
  return cat ? cat.label : ''
}

// 获取医生列表
async function fetchDoctors() {
  loading.value = true
  try {
    // 从路由参数获取科室代码
    const depCode = route.query.depCode
    const depName = route.query.depName
    
    if (!depCode) {
      console.error('缺少科室代码参数')
      loading.value = false
      return
    }
    
    // 实际使用时替换为：
    // const { data } = await axios.get('/api/doctors', {
    //   params: { depCode }
    // })
    // doctors.value = data.doctors
    // departmentName.value = data.departmentName
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 600))
    doctors.value = mockDoctors
    departmentName.value = depName || '未知科室'
    
    console.log('科室代码:', depCode)
    console.log('科室名称:', departmentName.value)
  } catch (err) {
    console.error('获取医生列表失败', err)
  } finally {
    loading.value = false
  }
}

// 返回科室列表
function goBack() {
  router.push('/department')
}

// 预约医生
function handleAppointment(doctor) {
  console.log('预约医生:', doctor)
  // 跳转到时间选择页面
  // router.push(`/appointment/time/${doctor.id}`)
  alert(`即将进入 ${doctor.name} 医生的时间选择页面...`)
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
  fetchDoctors()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.appointment-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 140px);
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  color: white;
  font-size: 0.9rem;
}

.back-link {
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0.8;
}

.back-link:hover {
  opacity: 1;
  text-decoration: underline;
}

.breadcrumb svg {
  opacity: 0.6;
}

.current {
  font-weight: 600;
}

/* 科室信息卡片 */
.department-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.dept-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dept-icon svg {
  color: white;
}

.dept-info h1 {
  margin: 0 0 0.25rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.dept-info p {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

/* 分类标签 */
.category-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  overflow-x: auto;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: white;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.95);
}

.tab-btn.active {
  background: white;
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.count {
  padding: 0.125rem 0.5rem;
  background: #f7fafc;
  border-radius: 10px;
  font-size: 0.8rem;
  color: #718096;
}

.tab-btn.active .count {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 16px;
  color: #718096;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.doctors-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}
/* 响应式 */
@media (max-width: 768px) {
  .appointment-wrapper {
    padding: 1rem;
  }

  .department-card {
    padding: 1.25rem;
  }

  .dept-icon {
    width: 56px;
    height: 56px;
  }

  .dept-info h1 {
    font-size: 1.5rem;
  }

  .category-tabs {
    gap: 0.5rem;
  }

  .doctor-card {
    flex-direction: column;
    padding: 1.25rem;
  }

  .doctor-avatar {
    width: 64px;
    height: 64px;
    margin: 0 auto;
  }

  .doctor-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .doctor-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .stats {
    justify-content: center;
  }

  .appointment-btn {
    width: 100%;
    text-align: center;
  }
}
</style>