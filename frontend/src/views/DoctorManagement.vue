<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏导航（与其他管理员页保持一致风格，可后续整合复用） -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="avatar">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"></path>
            </svg>
          </div>
          <h3>管理员中心</h3>
        </div>

        <nav class="sidebar-nav">
          <router-link to="/admin/profile" class="nav-item" :class="{ active: $route.path === '/admin/profile' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人信息</span>
          </router-link>

          <router-link to="/admin/schedules" class="nav-item" :class="{ active: $route.path === '/admin/schedules' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>排班管理</span>
          </router-link>

          <router-link to="/admin/leaves" class="nav-item" :class="{ active: $route.path === '/admin/leaves' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>请假审批</span>
          </router-link>

          <router-link to="/admin/doctors" class="nav-item" :class="{ active: $route.path === '/admin/doctors' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>医生管理</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <div class="doctor-management">
          <!-- 头部操作区 -->
          <div class="header-section">
            <div class="title-group">
              <h2>医生管理</h2>
              <p class="subtitle">新增、查询与管理医生账号</p>
            </div>
            <button @click="toggleCreate = !toggleCreate" class="create-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              {{ toggleCreate ? '收起' : '新建医生账号' }}
            </button>
          </div>

          <!-- 新建表单（复用组件） -->
          <transition name="expand">
            <div v-if="toggleCreate" class="create-panel">
              <CreateDocAccount @created="fetchDoctors" />
            </div>
          </transition>

          <!-- 筛选区 -->
          <div class="filter-section">
            <div class="filter-group">
              <label class="filter-label">科室</label>
              <select v-model="filters.department" @change="fetchDoctors" class="filter-select">
                <option value="">全部科室</option>
                <option value="CARDIO">心内科</option>
                <option value="GASTRO">消化内科</option>
                <option value="RESPIR">呼吸内科</option>
                <option value="ORTHO">骨科</option>
                <option value="NEURO">神经外科</option>
                <option value="GYNEC">妇科</option>
                <option value="PEDIM">小儿内科</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">关键字</label>
              <input v-model="filters.keyword" @keyup.enter="fetchDoctors" class="filter-input" placeholder="姓名 / 工号" />
            </div>

            <button @click="resetFilters" class="reset-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10"></polyline>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
              </svg>
              重置
            </button>
          </div>

          <!-- 列表 -->
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>加载医生账号中...</p>
          </div>

          <div v-else class="doctor-table">
            <div class="table-header">
              <div class="th-name">姓名</div>
              <div class="th-doctorId">工号</div>
              <div class="th-dept">科室</div>
              <div class="th-title">职称</div>
              <div class="th-status">状态</div>
              <div class="th-actions">操作</div>
            </div>

            <div v-if="doctors.length === 0" class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <p>暂无医生账号</p>
            </div>

            <div v-else>
              <div v-for="doc in doctors" :key="doc.id" class="table-row">
                <div class="td-name">
                  <div class="doctor-name">{{ doc.name }}</div>
                </div>
                <div class="td-doctorId">{{ doc.id}}</div>
                <div class="td-dept"><span class="department-badge">{{ doc.departmentName || getDepartmentName(doc.department) }}</span></div>
                <div class="td-title">{{ doc.title || '-' }}</div>
                <div class="td-status">
                  <span :class="['status-badge', doc.enabled ? 'active' : 'disabled']">{{ doc.enabled ? '启用' : '停用' }}</span>
                </div>
                <div class="td-actions">
                  <button class="action-btn view-btn" title="查看" @click="viewDoctor(doc)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                  <button class="action-btn edit-btn" title="编辑" @click="editDoctor(doc)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button class="action-btn reset-btn" title="重置密码" @click="resetPassword(doc)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="3"></circle>
                      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9c0 .69-.28 1.32-.73 1.77A2.5 2.5 0 0 1 19.4 15z"></path>
                    </svg>
                  </button>
                  <button class="action-btn status-btn" :title="doc.enabled ? '停用' : '启用'" @click="toggleStatus(doc)">
                    <svg v-if="doc.enabled" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </button>
                  <button class="action-btn delete-btn" title="删除" @click="removeDoctor(doc)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import CreateDocAccount from '@/components/CreateDocAccount.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const toggleCreate = ref(false)

const filters = reactive({
  department: '',
  keyword: ''
})

const doctors = ref([])

function getDepartmentName(code) {
  const map = {
    'CARDIO': '心内科',
    'GASTRO': '消化内科',
    'RESPIR': '呼吸内科',
    'ORTHO': '骨科',
    'NEURO': '神经外科',
    'GYNEC': '妇科',
    'PEDIM': '小儿内科'
  }
  return map[code] || code
}

async function fetchDoctors() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = {}
    if ((filters.keyword || '').trim()) {
      params.username = filters.keyword.trim()
    }

    const { data } = await axios.get('/api/admin/doctors/list', {
      params,
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })

    if (data && data.code === 200) {
      // 映射后端 DoctorAccountDTO 到前端展示字段
      doctors.value = (data.data || []).map(d => ({
        id: d.doctorId,
        name: d.username,
        departmentName: d.deptName,
        deptId: d.deptId,
        title: d.title,
        enabled: (d.doctorStatus === 'active') && (d.userStatus === 'verified')
      }))
    } else {
      doctors.value = []
    }
  } catch (e) {
    console.error('获取医生账号失败', e)
    alert('获取医生账号失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.department = ''
  filters.keyword = ''
  fetchDoctors()
}

async function viewDoctor(doc) {
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get(`/api/admin/doctors/${doc.id}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data && data.code === 200) {
      const detail = data.data
      alert(`医生详情\n姓名：${detail.username}\n科室：${detail.deptName || ''}\n职称：${detail.title || ''}\n状态：${detail.doctorStatus || ''}`)
    }
  } catch (e) {
    alert('获取医生详情失败')
  }
}

function editDoctor(doc) {
  alert(`编辑医生：${doc.name}（此处可打开编辑弹窗）`)
}

async function resetPassword(doc) {
  if (!confirm(`确定要重置医生【${doc.name}】的登录密码吗？`)) return
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.put(`/api/admin/doctors/reset-password/${doc.id}`, null, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert(data?.message || '密码已重置')
  } catch (e) {
    alert('重置失败，请稍后重试')
  }
}

async function toggleStatus(doctor) {
  const token = localStorage.getItem('token')
  const newStatus = doctor.enabled ? 'rejected' : 'verified'

  try {
    const { data } = await axios.put(
        `/api/admin/doctors/status/${doctor.id}`,
        null,
        {
          params: { status: newStatus },
          headers: { Authorization: `Bearer ${token}` }
        }
    )

    if (data.code === 200) {
      doctor.enabled = !doctor.enabled
      alert(data.message)
    } else {
      alert(data.message || '操作失败')
    }
  } catch (e) {
    console.error('更新医生状态失败', e)
    alert('请求出错')
  }
}

async function removeDoctor(doc) {
  alert('后端暂未提供删除接口，无法删除该医生账号')
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

.profile-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

.sidebar {
  width: 280px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  height: fit-content;
  position: sticky;
  top: calc(var(--nav-height, 110px) + 2rem);
  flex-shrink: 0;
}

.sidebar-header {
  text-align: center;
  padding-bottom: 2rem;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 1.5rem;
}

.avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.avatar svg { color: white; }

.sidebar-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.sidebar-nav { display: flex; flex-direction: column; gap: 0.5rem; }
.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
  font-size: 1rem;
  font-weight: 500;
  text-align: left;
  width: 100%;
  text-decoration: none;
}
.nav-item:hover { background: #f7fafc; color: #f5576c; }
.nav-item.active { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3); }
.nav-item svg { flex-shrink: 0; }

.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.doctor-management { padding: 2.5rem; }

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}
.title-group h2 { margin: 0 0 0.5rem 0; color: #2d3748; font-size: 1.75rem; font-weight: 600; }
.subtitle { margin: 0; color: #718096; font-size: 0.9rem; }
.create-btn {
  display: flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white; border: none; border-radius: 10px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease;
}
.create-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4); }

.create-panel { margin-bottom: 1.5rem; background: #fff; border: 2px solid #f0f0f0; border-radius: 12px; padding: 1rem; }

.filter-section {
  display: flex; gap: 1rem; margin-bottom: 1.5rem; padding: 1.25rem; background: #f7fafc; border-radius: 12px; flex-wrap: wrap;
}
.filter-group { display: flex; flex-direction: column; gap: 0.5rem; flex: 1; min-width: 200px; }
.filter-label { font-size: 0.85rem; font-weight: 600; color: #4a5568; }
.filter-input, .filter-select { padding: 0.625rem; border: 2px solid #e2e8f0; border-radius: 8px; font-size: 0.9rem; transition: all 0.3s ease; }
.filter-input:focus, .filter-select:focus { outline: none; border-color: #f5576c; box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1); }
.reset-btn { display: flex; align-items: center; gap: 0.5rem; padding: 0.625rem 1.25rem; background: white; color: #4a5568; border: 2px solid #e2e8f0; border-radius: 8px; font-size: 0.9rem; font-weight: 500; cursor: pointer; transition: all 0.3s ease; align-self: flex-end; }
.reset-btn:hover { background: #f7fafc; border-color: #cbd5e0; }

.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; color: #718096; }
.spinner { width: 48px; height: 48px; border: 4px solid #e2e8f0; border-top-color: #f5576c; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 1rem; }
@keyframes spin { to { transform: rotate(360deg); } }

.doctor-table { display: flex; flex-direction: column; gap: 0.5rem; }
.table-header { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 1.5fr; gap: 1rem; padding: 1rem; background: #f7fafc; border-radius: 10px; font-weight: 600; color: #4a5568; font-size: 0.9rem; }
.table-row { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 1.5fr; gap: 1rem; padding: 1rem; background: white; border: 2px solid #e2e8f0; border-radius: 10px; align-items: center; transition: all 0.3s; }
.table-row:hover { border-color: #f093fb; box-shadow: 0 2px 8px rgba(240, 147, 251, 0.2); }

.doctor-name { font-weight: 600; color: #2d3748; }
.department-badge { display: inline-block; padding: 0.25rem 0.75rem; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: #fff; border-radius: 12px; font-size: 0.8rem; font-weight: 600; }

.status-badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600; text-align: center; }
.status-badge.active { background: #d4edda; color: #28a745; }
.status-badge.disabled { background: #ffe6e6; color: #d93025; }

.td-actions { display: flex; gap: 0.5rem; justify-content: center; }
.action-btn { width: 32px; height: 32px; border: none; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s ease; }
.view-btn { background: #e3f2fd; color: #2196f3; }
.view-btn:hover { background: #2196f3; color: #fff; }
.edit-btn { background: #fff3e0; color: #ff9800; }
.edit-btn:hover { background: #ff9800; color: #fff; }
.reset-btn { background: #ede7f6; color: #673ab7; }
.reset-btn:hover { background: #673ab7; color: #fff; }
.status-btn { background: #f1f8e9; color: #689f38; }
.status-btn:hover { background: #689f38; color: #fff; }
.delete-btn { background: #ffebee; color: #f44336; }
.delete-btn:hover { background: #f44336; color: #fff; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; color: #a0aec0; }
.empty-state svg { margin-bottom: 1rem; opacity: 0.5; }

/* 展开过渡 */
.expand-enter-active, .expand-leave-active { transition: all .25s ease; }
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-8px); }

/* 响应式 */
@media (max-width: 1024px) {
  .profile-layout { padding: 1.5rem; gap: 1.5rem; }
  .sidebar { width: 240px; }
  .table-header, .table-row { grid-template-columns: 1.2fr 1fr 1fr 1fr 0.8fr 1.6fr; }
}

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; padding: 1rem; gap: 1rem; }
  .sidebar { width: 100%; position: static; padding: 1.5rem; }
  .doctor-management { padding: 1.5rem; }
  .table-header, .table-row { min-width: 900px; }
  .doctor-table { overflow-x: auto; }
}
</style>


