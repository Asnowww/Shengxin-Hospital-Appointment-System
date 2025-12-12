<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <AdminSidebar />

      <main class="main-content">
        <div class="doctor-management">
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

          <transition name="expand">
            <div v-if="toggleCreate" class="create-panel">
              <CreateDocAccount @created="fetchDoctors" />
            </div>
          </transition>

          <div class="filter-section">
            <div class="filter-group">
              <label class="filter-label">科室</label>
              <div class="custom-select-container" @click.stop="toggleDropdown">
                <div class="select-trigger" :class="{ 'is-open': dropdownVisible }">
                  <span :class="{ 'placeholder-text': !selectedDeptName }">
                    {{ selectedDeptName || '请选择科室' }}
                  </span>
                  <svg class="arrow-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>

                <transition name="fade">
                  <div v-show="dropdownVisible" class="select-options">
                    <div v-for="dept in departments" :key="dept.deptId" class="dept-group">
                      <div class="dept-group-title">{{ dept.deptName }}</div>
                      <div 
                        v-for="child in dept.children || []" 
                        :key="child.deptId" 
                        class="dept-option"
                        :class="{ selected: filters.department === child.deptId }"
                        @click.stop="selectChild(child)"
                      >
                        {{ child.deptName }}
                      </div>
                    </div>
                    <div v-if="departments.length === 0" class="no-data">暂无科室数据</div>
                  </div>
                </transition>
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">关键字</label>
              <input
                v-model="filters.keyword"
                @keyup.enter="fetchDoctors"
                class="filter-input"
                placeholder="姓名 / 工号"
              />
            </div>
            
            <button @click="resetFilters" class="reset-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10"></polyline>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
              </svg>
              重置
            </button>
          </div>

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

          <div v-if="doctors.length > 0 && pagination.totalPages > 1" class="pagination-controls">
              <div class="pagination-info">
                  显示第 {{ ((pagination.pageNum - 1) * pagination.pageSize) + 1 }} - {{ Math.min(pagination.pageNum * pagination.pageSize, pagination.total) }} 条，共 {{ pagination.total }} 条
              </div>
              <div class="pagination-buttons">
                  <button 
                      :disabled="pagination.pageNum === 1" 
                      @click="handlePageChange(pagination.pageNum - 1)" 
                      class="page-btn"
                  >
                      &larr; 上一页
                  </button>
                  
                  <span class="page-num-display">
                      {{ pagination.pageNum }} / {{ pagination.totalPages }}
                  </span>
                  
                  <button 
                      :disabled="pagination.pageNum === pagination.totalPages" 
                      @click="handlePageChange(pagination.pageNum + 1)" 
                      class="page-btn"
                  >
                      下一页 &rarr;
                  </button>
              </div>
          </div>
        </div>
      </main>
    </div>
  </div>

  <EditDoctorModal 
    v-model="showEditModal" 
    :doctor="editingDoctor" 
    :departments="departments"
    @success="handleEditSuccess"
  />
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import AdminSidebar from '@/components/AdminSidebar.vue'
import CreateDocAccount from '@/components/CreateDocAccount.vue'
import EditDoctorModal from '@/components/EditDoctorModal.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const toggleCreate = ref(false)
const selectedDeptId = ref('')
const selectedDeptName = ref('')
const dropdownVisible = ref(false)
const departments = ref([])
const doctors = ref([])

// 改造点 1: 新增分页相关状态
const pagination = reactive({
  pageNum: 1, // 当前页码
  pageSize: 10, // 每页数量 (默认 10)
  total: 0, // 总记录数
  totalPages: 0, // 总页数
})

// 编辑相关状态
const showEditModal = ref(false)
const editingDoctor = ref(null)

const filters = reactive({
  department: '',
  keyword: ''
})

function toggleDropdown() {
  dropdownVisible.value = !dropdownVisible.value
}

function selectChild(child) {
  selectedDeptId.value = child.deptId
  selectedDeptName.value = child.deptName
  filters.department = child.deptId
  dropdownVisible.value = false
  // 筛选条件变化时，重置页码为 1
  pagination.pageNum = 1
  fetchDoctors()
}

function closeDropdown() {
  dropdownVisible.value = false
}

async function fetchDepartments() {
  try {
    const res = await axios.get('/api/departments/all')
    if (res.data.code === 200) {
      departments.value = res.data.data
    }
  } catch (e) {
    console.error('加载科室失败', e)
  }
}

async function fetchDoctors() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = {}

    if ((filters.keyword || '').trim()) {
      // 对应后端 DoctorQueryDTO 的 username 字段
      params.username = filters.keyword.trim()
    }

    if (filters.department) {
      // 对应后端 DoctorQueryDTO 的 deptId 字段
      params.deptId = filters.department
    }
    
    // 改造点 2: 添加分页参数
    params.pageNum = pagination.pageNum
    params.pageSize = pagination.pageSize

    const { data } = await axios.get('/api/admin/doctors/list', {
      params,
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })

    if (data && data.code === 200 && data.data) {
      // 改造点 3: 处理 PageResult 结构
      const pageResult = data.data
      
      doctors.value = (pageResult.records || []).map(d => ({
        id: d.doctorId,
        name: d.username,
        departmentName: d.deptName,
        deptId: d.deptId,
        title: d.title,
        gender: d.gender,
        phone: d.phone,
        email: d.email,
        specialty: d.specialty,
        bio: d.bio,
        // 根据后端字段判断状态，确保逻辑不变
        enabled: (d.doctorStatus === 'active') && (d.userStatus === 'verified')
      }))
      
      // 更新分页状态
      pagination.total = pageResult.total || 0
      pagination.pageNum = pageResult.pageNum || 1
      pagination.pageSize = pageResult.pageSize || 10
      // 计算总页数
      pagination.totalPages = Math.ceil(pagination.total / pagination.pageSize)
      
    } else {
      doctors.value = []
      // 错误时重置分页信息
      pagination.total = 0
      pagination.pageNum = 1
      pagination.totalPages = 0
    }
  } catch (e) {
    console.error('获取医生账号失败', e)
    alert('获取医生账号失败')
    doctors.value = []
    pagination.total = 0
    pagination.pageNum = 1
    pagination.totalPages = 0
  } finally {
    loading.value = false
  }
}

// 改造点 4: 重置筛选条件时，将页码重置为 1
function resetFilters() {
  filters.department = ''
  filters.keyword = ''
  selectedDeptName.value = ''
  selectedDeptId.value = ''
  pagination.pageNum = 1 // 重置页码
  fetchDoctors()
}

// 改造点 5: 新增处理页码变化
function handlePageChange(newPage) {
  if (newPage > 0 && newPage <= pagination.totalPages && newPage !== pagination.pageNum) {
    // 滚动到顶部或表格顶部，提供更好的用户体验
    window.scrollTo({ top: 0, behavior: 'smooth' })
    pagination.pageNum = newPage
    fetchDoctors()
  }
}

// 保持不变：查找科室名称的逻辑 (如果需要)
function getDepartmentName(deptId) {
    // 假设这个函数遍历 departments 查找名称，如果不需要可以移除
    // 由于后端返回的 DTO 中已经包含了 deptName，此处可能不再需要
    return deptId
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

// 编辑医生 - 打开弹窗
function editDoctor(doc) {
  editingDoctor.value = doc
  showEditModal.value = true
}

// 编辑成功回调
function handleEditSuccess() {
  fetchDoctors()
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
  document.addEventListener('click', closeDropdown)
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
  await fetchDepartments()
  // 初始加载时，pageNum 默认为 1
  fetchDoctors()
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
}
/* ---------------------------------- */
/* 新增：分页控制样式 */
/* ---------------------------------- */
.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding: 1rem 1.5rem;
  background: #fff;
  border-top: 2px solid #f0f0f0;
  border-radius: 0 0 16px 16px; /* 确保底部圆角在 main-content 中正确显示 */
}

.pagination-info {
  font-size: 0.9rem;
  color: #718096;
  font-weight: 500;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #e2e8f0;
  border-color: #cbd5e1;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-num-display {
  font-size: 1rem;
  font-weight: 600;
  color: #2d3748;
}

/* 下拉框容器 */
.custom-select-container {
  position: relative;
  width: 200px;
  font-size: 14px;
  cursor: pointer;
}

/* 触发器（输入框样子） */
.select-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  transition: all 0.2s;
  color: #333;
}

.select-trigger:hover {
  border-color: #3b82f6;
}

.select-trigger.is-open {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.placeholder-text {
  color: #94a3b8;
}

.arrow-icon {
  color: #94a3b8;
  transition: transform 0.2s;
}

.select-trigger.is-open .arrow-icon {
  transform: rotate(180deg);
}

/* 下拉列表面板 */
.select-options {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 6px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

/* 科室分组 */
.dept-group {
  border-bottom: 1px solid #f1f5f9;
}

.dept-group:last-child {
  border-bottom: none;
}

/* 一级科室标题 */
.dept-group-title {
  padding: 8px 12px;
  font-size: 12px;
  color: #94a3b8;
  background-color: #f8fafc;
  font-weight: 600;
}

/* 二级科室选项 */
.dept-option {
  padding: 10px 12px 10px 24px;
  color: #475569;
  transition: background 0.2s;
}

.dept-option:hover {
  background-color: #eff6ff;
  color: #3b82f6;
}

.dept-option.selected {
  background-color: #eff6ff;
  color: #3b82f6;
  font-weight: 500;
}

/* 滚动条美化 */
.select-options::-webkit-scrollbar {
  width: 6px;
}
.select-options::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

/* 简单的淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
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

.doctor-management { 
  padding: 2.5rem; 
  padding-bottom: 0; /* 移除底部 padding */
}

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
.doctor-table { 
  padding-bottom: 1.5rem; /* 在列表下方增加一些间隔 */
}
.table-header { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 1.5fr; gap: 1rem; padding: 1rem; background: #f7fafc; border-radius: 10px; font-weight: 600; color: #4a5568; font-size: 0.9rem; }
.table-row { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 1.5fr; gap: 1rem; padding: 1rem; background: white; border: 2px solid #e2e8f0; border-radius: 10px; align-items: center; transition: all 0.3s; }
.table-row:hover { border-color: #f093fb; box-shadow: 0 2px 8px rgba(240, 147, 251, 0.2); }
.table-row {
    margin-bottom: 0;
}
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
.action-btn.reset-btn { background: #ede7f6; color: #673ab7; }
.action-btn.reset-btn:hover { background: #673ab7; color: #fff; }
.status-btn { background: #f1f8e9; color: #689f38; }
.status-btn:hover { background: #689f38; color: #fff; }
.delete-btn { background: #ffebee; color: #f44336; }
.delete-btn:hover { background: #f44336; color: #fff; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; color: #a0aec0; }
.empty-state svg { margin-bottom: 1rem; opacity: 0.5; }

/* 展开过渡 */
.expand-enter-active, .expand-leave-active { transition: all .25s ease; }
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-8px); }

/* 模态框样式 */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f7fafc;
  color: #2d3748;
}

.modal-body {
  padding: 2rem;
}

.readonly-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group.required .form-label::after {
  content: '*';
  color: #f44336;
  margin-left: 4px;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 600;
  font-size: 0.9rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-input.readonly {
  background: #f7fafc;
  color: #718096;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.btn-cancel {
  background: #f7fafc;
  color: #4a5568;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-submit {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.no-data {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
}

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
  
  .readonly-section {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }
}
</style>