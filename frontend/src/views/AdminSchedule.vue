<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏 -->
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
          <router-link to="/admin/profile" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人信息</span>
          </router-link>

          <router-link to="/admin/schedules" class="nav-item active">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>排班管理</span>
          </router-link>

          <router-link to="/admin/leaves" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>请假审批</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <div class="schedule-management">
          <!-- 头部操作区 -->
          <div class="header-section">
            <div class="title-group">
              <h2>排班管理</h2>
              <p class="subtitle">按科室、诊室、日期、时间段展示排班信息</p>
            </div>
          </div>

          <div class="top-actions">
  <button class="add-schedule-btn" @click="openCreateSchedule">
    + 新增排班
  </button>
</div>


          <!-- 科室选择区 -->
          <div class="filter-section">
  <div class="filter-group">
    <label class="filter-label">选择科室</label>

    <!-- 自定义两级下拉 -->
    <div class="dept-dropdown" @click.stop="toggleDropdown">
      <div class="dept-selected">
        {{ selectedDeptName || '请选择科室' }}
      </div>

      <div class="dropdown-menu" v-if="dropdownVisible">
        <!-- 一级科室 -->
        <div
          v-for="dept in departments"
          :key="dept.deptId"
          class="menu-item"
          @mouseenter="hoverParent = dept"
        >
          {{ dept.deptName }}

          <!-- 二级科室悬挂层 -->
          <div class="submenu" v-if="hoverParent === dept">
            <div
              v-for="child in (dept.children || [])"
              :key="child.deptId"
              class="submenu-item"
              @click.stop="selectChild(child)"
            >
              {{ child.deptName }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>


          <!-- 排班表格组件 -->
          <ScheduleTable 
            v-if="selectedDeptId"
            :key="selectedDeptId"
            :deptId="selectedDeptId"
            :deptName="currentDepartment?.deptName"
          />

          <!-- 未选择科室的提示 -->
          <div v-else class="empty-state">
            <p>请在上方选择科室后查看排班表</p>
          </div>
        </div>
      </main>
    </div>
    <!-- 排班弹窗 -->
    <AdminCreateSchedule
      :show="showScheduleModal"
      :isEditing="scheduleEditing"
      :initialData="scheduleInitialData"
      :doctors="doctors"
      :rooms="rooms"
      @close="handleModalClose"
      @submit="handleScheduleSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import ScheduleTable from '@/components/ScheduleTable.vue'
import axios from 'axios'
import AdminCreateSchedule from '@/components/AdminCreateSchedule.vue'

const navRef = ref(null)
const navHeight = ref(110)
const selectedDeptId = ref('')
const departments = ref([])


const dropdownVisible = ref(false)
const hoverParent = ref(null)
const selectedDeptName = ref('')

const showScheduleModal = ref(false)
const scheduleEditing = ref(false)
const scheduleInitialData = ref(null)

function openCreateSchedule() {
  scheduleEditing.value = false
  scheduleInitialData.value = null
  showScheduleModal.value = true
}

function handleModalClose() {
  showScheduleModal.value = false
}

function handleScheduleSubmit() {
  showScheduleModal.value = false
  loadScheduleData()
}

function toggleDropdown() {
  dropdownVisible.value = !dropdownVisible.value
}

function selectChild(child) {
  selectedDeptId.value = child.deptId
  selectedDeptName.value = child.deptName
  dropdownVisible.value = false
}

// 点击页面任意处关闭下拉
document.addEventListener('click', () => {
  dropdownVisible.value = false
})

const currentDepartment = computed(() => {
  if (!selectedDeptId.value) return null
  return departments.value.find(d => d.deptId == selectedDeptId.value)
})

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

function onDepartmentChange() {
  // 科室改变时，ScheduleTable 会自动更新
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
.add-schedule-btn {
  background: #f5576c;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.add-schedule-btn:hover {
  background: #d64559;
}

.dept-dropdown {
  position: relative;
  width: 250px;
  cursor: pointer;
}

.dept-selected {
  padding: 0.625rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: white;
}

.dropdown-menu {
  position: absolute;
  top: 110%;
  left: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
  width: 250px;
  z-index: 10;
}

.menu-item {
  padding: 0.6rem 1rem;
  position: relative;
  background: white;
}

.menu-item:hover {
  background: #f7fafc;
}

/* 二级菜单 */
.submenu {
  position: absolute;
  top: 0;
  left: 100%;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
  width: 200px;
}

.submenu-item {
  padding: 0.6rem 1rem;
}

.submenu-item:hover {
  background: #ffe5ea;
}

.profile-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  max-width: 1800px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

/* 左侧边栏 */
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

.avatar svg {
  color: white;
}

.sidebar-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

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
  text-decoration: none;
}

.nav-item:hover {
  background: #f7fafc;
  color: #f5576c;
}

.nav-item.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.schedule-management {
  padding: 2.5rem;
}

/* 头部区域 */
.header-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.title-group h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

/* 筛选区 */
.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f7fafc;
  border-radius: 12px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 200px;
}

.filter-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5568;
}

.filter-select {
  padding: 0.625rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #a0aec0;
  font-size: 1rem;
}

@media (max-width: 1024px) {
  .profile-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .sidebar {
    width: 240px;
  }

  .schedule-management {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
    padding: 1rem;
    gap: 1rem;
  }

  .sidebar {
    width: 100%;
    position: static;
    padding: 1.5rem;
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    text-align: left;
    padding-bottom: 1rem;
  }

  .avatar {
    width: 60px;
    height: 60px;
    margin: 0;
    flex-shrink: 0;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem;
    min-width: 100px;
    text-align: center;
    font-size: 0.9rem;
  }

  .schedule-management {
    padding: 1rem;
  }
}
</style>
