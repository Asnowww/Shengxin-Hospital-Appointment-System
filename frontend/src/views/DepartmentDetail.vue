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
        <div 
          v-for="doctor in filteredDoctors" 
          :key="doctor.id"
          class="doctor-card">
          
          <div class="doctor-avatar">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>

          <div class="doctor-info">
            <div class="doctor-header">
              <div class="name-title">
                <h3>{{ doctor.name }}</h3>
                <span :class="['title-badge', doctor.category]">
                  {{ doctor.title }}
                </span>
              </div>
              <span :class="['category-badge', doctor.category]">
                {{ getCategoryLabel(doctor.category) }}
              </span>
            </div>

            <p class="description">{{ doctor.description }}</p>

            <div class="doctor-footer">
              <div class="stats">
                <!-- <div class="stat-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                    <circle cx="9" cy="7" r="4"></circle>
                    <line x1="19" y1="8" x2="19" y2="14"></line>
                    <line x1="22" y1="11" x2="16" y2="11"></line>
                  </svg>
                  <span>已服务 {{ doctor.patientCount }}+ 人</span>
                </div> -->
                <!-- <div v-if="doctor.goodRate" class="stat-item">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                  </svg>
                  <span>好评率 {{ doctor.goodRate }}%</span>
                </div> -->
              </div>

              <button 
                @click="handleAppointment(doctor)"
                class="appointment-btn">
                预约挂号
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navigation from '@/components/Navigation.vue'
// import axios from 'axios'

const route = useRoute()
const router = useRouter()

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const activeCategory = ref('all')
const departmentName = ref('心内科')
const doctors = ref([])

const categories = [
  { label: '全部', value: 'all' },
  { label: '普通门诊', value: 'normal' },
  { label: '专家门诊', value: 'expert' },
  { label: '特需门诊', value: 'special' }
]

// 模拟数据（后端接口完成后替换）
const mockDoctors = [
  {
    id: 1,
    name: '张明',
    title: '主任医师',
    category: 'expert',
    description: '从事心血管内科临床工作30余年，擅长冠心病、高血压、心力衰竭的诊治，对心律失常有深入研究',
    patientCount: 5000,
    goodRate: 98
  },
  {
    id: 2,
    name: '李华',
    title: '副主任医师',
    category: 'expert',
    description: '专注于心血管疾病的介入治疗，擅长冠脉支架植入、起搏器植入等手术',
    patientCount: 3200,
    goodRate: 96
  },
  {
    id: 3,
    name: '王芳',
    title: '主任医师',
    category: 'special',
    description: '心血管病学博士，博士生导师，擅长复杂冠心病、心肌病、心衰的诊治，在国际期刊发表论文50余篇',
    patientCount: 8000,
    goodRate: 99
  },
  {
    id: 4,
    name: '刘强',
    title: '主治医师',
    category: 'normal',
    description: '擅长高血压、冠心病、心律失常等常见心血管疾病的诊断和治疗',
    patientCount: 2000,
    goodRate: 95
  },
  {
    id: 5,
    name: '陈静',
    title: '主治医师',
    category: 'normal',
    description: '熟练掌握心血管常见病、多发病的诊疗，对高血压、冠心病有丰富的临床经验',
    patientCount: 1800,
    goodRate: 94
  },
  {
    id: 6,
    name: '赵敏',
    title: '副主任医师',
    category: 'expert',
    description: '擅长心血管疾病的超声诊断，对心脏结构和功能评估有深入研究',
    patientCount: 4000,
    goodRate: 97
  },
  {
    id: 7,
    name: '孙伟',
    title: '主任医师',
    category: 'special',
    description: '国内知名心血管专家，擅长疑难复杂心血管疾病的诊治，多次参与国家级科研项目',
    patientCount: 10000,
    goodRate: 99
  },
  {
    id: 8,
    name: '周杰',
    title: '主治医师',
    category: 'normal',
    description: '对心血管常见疾病有扎实的理论基础和丰富的临床经验',
    patientCount: 1500,
    goodRate: 93
  },
  {
    id: 9,
    name: '吴琳',
    title: '副主任医师',
    category: 'expert',
    description: '擅长心血管急危重症的救治，对急性心肌梗死、急性心力衰竭等有丰富经验',
    patientCount: 3500,
    goodRate: 96
  }
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
    departmentName.value = depName || '心内科'
    
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state svg {
  color: #cbd5e0;
  margin-bottom: 1rem;
}

/* 医生列表 */
.doctors-list {
  display: grid;
  gap: 1.25rem;
}

.doctor-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  gap: 1.25rem;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.doctor-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #e2e8f0;
  transform: translateY(-2px);
}

.doctor-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-avatar svg {
  color: #667eea;
}

.doctor-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.doctor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}

.name-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.name-title h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.title-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  background: #edf2f7;
  color: #4a5568;
}

.category-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.category-badge.normal {
  background: #e6f7ff;
  color: #1890ff;
}

.category-badge.expert {
  background: #fff7e6;
  color: #fa8c16;
}

.category-badge.special {
  background: #fff1f0;
  color: #f5222d;
}

.description {
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 1rem 0;
  flex: 1;
}

.doctor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.stats {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #718096;
  font-size: 0.85rem;
}

.stat-item svg {
  color: #667eea;
}

.appointment-btn {
  padding: 0.625rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.appointment-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
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