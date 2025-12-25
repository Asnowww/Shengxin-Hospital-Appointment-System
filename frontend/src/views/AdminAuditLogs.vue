<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏导航 -->
      <AdminSidebar />

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <div class="audit-logs">
          <div class="page-header">
            <h2>审计日志</h2>
            <p class="subtitle">查看系统操作记录和用户行为日志</p>
          </div>

          <!-- 筛选区域 -->
          <div class="filter-section">
            <div class="filter-row">
              <div class="filter-item">
                <label>用户ID</label>
                <input
                  v-model="filters.userId"
                  type="number"
                  placeholder="请输入用户ID"
                  class="filter-input"
                />
              </div>
              <div class="filter-item">
                <label>操作类型</label>
                <select v-model="filters.action" class="filter-select">
                  <option value="">全部</option>
                  <option value="CREATE">创建</option>
                  <option value="UPDATE">更新</option>
                  <option value="DELETE">删除</option>
                  <option value="LOGIN">登录</option>
                  <option value="LOGOUT">登出</option>
                  <option value="VIEW">查看</option>
                </select>
              </div>
              <div class="filter-item">
                <label>资源类型</label>
                <select v-model="filters.resourceType" class="filter-select">
                  <option value="">全部</option>
                  <option value="USER">用户</option>
                  <option value="DOCTOR">医生</option>
                  <option value="PATIENT">患者</option>
                  <option value="APPOINTMENT">预约</option>
                  <option value="SCHEDULE">排班</option>
                  <option value="DEPARTMENT">科室</option>
                  <option value="LEAVE">请假</option>
                </select>
              </div>
            </div>
            <div class="filter-row">
              <div class="filter-item">
                <label>开始时间</label>
                <input
                  v-model="filters.startTime"
                  type="datetime-local"
                  class="filter-input"
                />
              </div>
              <div class="filter-item">
                <label>结束时间</label>
                <input
                  v-model="filters.endTime"
                  type="datetime-local"
                  class="filter-input"
                />
              </div>
              <div class="filter-actions">
                <button @click="handleSearch" class="search-btn">
                  <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                  </svg>
                  查询
                </button>
                <button @click="handleReset" class="reset-btn">
                  <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="1 4 1 10 7 10"></polyline>
                    <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
                  </svg>
                  重置
                </button>
              </div>
            </div>
          </div>

          <!-- 日志表格 -->
          <div class="table-container">
            <div v-if="loading" class="loading-overlay">
              <div class="spinner"></div>
              <span>加载中...</span>
            </div>
            <table class="logs-table">
              <thead>
                <tr>
                  <th>日志ID</th>
                  <th>用户ID</th>
                  <th>操作类型</th>
                  <th>资源类型</th>
                  <th>资源ID</th>
                  <th>消息</th>
                  <th>IP地址</th>
                  <th>操作时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="logs.length === 0 && !loading">
                  <td colspan="8" class="empty-row">
                    <svg xmlns="https://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                    </svg>
                    <span>暂无日志记录</span>
                  </td>
                </tr>
                <tr v-for="log in logs" :key="log.logId">
                  <td>{{ log.logId }}</td>
                  <td>{{ log.userId }}</td>
                  <td>
                    <span :class="['action-tag', getActionClass(log.action)]">
                      {{ formatAction(log.action) }}
                    </span>
                  </td>
                  <td>{{ formatResourceType(log.resourceType) }}</td>
                  <td>{{ log.resourceId || '-' }}</td>
                  <td class="message-cell" :title="log.message">{{ log.message || '-' }}</td>
                  <td>{{ log.ip || '-' }}</td>
                  <td>{{ formatDateTime(log.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="pagination" >
            <div class="pagination-info">
              共 {{ total }} 条记录，当前显示第 {{ (pageNum - 1) * pageSize + 1 }} - {{ Math.min(pageNum * pageSize, total) }} 条
            </div>
            <div class="pagination-controls">
              <button
                @click="changePage(pageNum - 1)"
                :disabled="pageNum <= 1"
                class="page-btn"
              >
                上一页
              </button>
              <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
              <button
                @click="changePage(pageNum + 1)"
                :disabled="pageNum >= totalPages"
                class="page-btn"
              >
                下一页
              </button>
              <div class="page-jump">
                <span>跳至</span>
                <input
                  v-model.number="jumpPageNum"
                  type="number"
                  min="1"
                  :max="totalPages"
                  class="page-jump-input"
                  @keyup.enter="handleJumpPage"
                />
                <span>页</span>
                <button @click="handleJumpPage" class="page-jump-btn">跳转</button>
              </div>
              <select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
                <option :value="10">10条/页</option>
                <option :value="20">20条/页</option>
                <option :value="50">50条/页</option>
                <option :value="100">100条/页</option>
              </select>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import AdminSidebar from '@/components/AdminSidebar.vue'
import axios from 'axios'

// 导航栏高度管理
const navRef = ref(null)
const navHeight = ref(110)

// 数据状态
const loading = ref(false)
const logs = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const jumpPageNum = ref(1)

// 筛选条件
const filters = reactive({
  userId: '',
  action: '',
  resourceType: '',
  startTime: '',
  endTime: ''
})

// 计算总页数
const totalPages = computed(() => {
  if (!pageSize.value) return 1
  return Math.max(1, Math.ceil(total.value / pageSize.value))
})

// 获取token
const token = localStorage.getItem('token')

// 格式化操作类型
function formatAction(action) {
  const actionMap = {
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'LOGIN': '登录',
    'LOGOUT': '登出',
    'VIEW': '查看'
  }
  return actionMap[action] || action || '-'
}

// 获取操作类型样式类
function getActionClass(action) {
  const classMap = {
    'CREATE': 'action-create',
    'UPDATE': 'action-update',
    'DELETE': 'action-delete',
    'LOGIN': 'action-login',
    'LOGOUT': 'action-logout',
    'VIEW': 'action-view'
  }
  return classMap[action] || ''
}

// 格式化资源类型
function formatResourceType(type) {
  const typeMap = {
    'USER': '用户',
    'DOCTOR': '医生',
    'PATIENT': '患者',
    'APPOINTMENT': '预约',
    'SCHEDULE': '排班',
    'DEPARTMENT': '科室',
    'LEAVE': '请假'
  }
  return typeMap[type] || type || '-'
}

// 格式化日期时间
function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  // 处理ISO格式和数组格式的日期
  let date
  if (Array.isArray(dateTime)) {
    // Java LocalDateTime 数组格式 [year, month, day, hour, minute, second]
    date = new Date(dateTime[0], dateTime[1] - 1, dateTime[2], dateTime[3] || 0, dateTime[4] || 0, dateTime[5] || 0)
  } else {
    date = new Date(dateTime)
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 构建查询参数
function buildQueryParams() {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  if (filters.userId) params.userId = filters.userId
  if (filters.action) params.action = filters.action
  if (filters.resourceType) params.resourceType = filters.resourceType
  if (filters.startTime) {
    params.startTime = filters.startTime.replace('T', ' ') + ':00'
  }
  if (filters.endTime) {
    params.endTime = filters.endTime.replace('T', ' ') + ':00'
  }
  return params
}

// 获取日志列表
async function fetchLogs() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const { data } = await axios.get('/api/logs', {
      params,
      headers: { Authorization: `Bearer ${token}` }
    })
    if (data.code === 200 && data.data) {
      logs.value = data.data.records || []
      total.value = data.data.total || 0

      // 补这两行（非常关键）
      pageNum.value = data.data.pageNum
      pageSize.value = data.data.pageSize

      // 同步跳页输入框
      jumpPageNum.value = data.data.pageNum
    } else {
      console.warn('获取日志列表失败', data.message)
      logs.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取日志列表失败', err)
    logs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询
function handleSearch() {
  pageNum.value = 1
  fetchLogs()
}

// 重置筛选
function handleReset() {
  filters.userId = ''
  filters.action = ''
  filters.resourceType = ''
  filters.startTime = ''
  filters.endTime = ''
  pageNum.value = 1
  fetchLogs()
}

// 切换页码
function changePage(page) {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchLogs()
}

// 修改每页条数
function handlePageSizeChange() {
  pageNum.value = 1
  jumpPageNum.value = 1
  fetchLogs()
}

// 跳转到指定页码
function handleJumpPage() {
  const page = parseInt(jumpPageNum.value)
  if (isNaN(page) || page < 1) {
    jumpPageNum.value = 1
    changePage(1)
  } else if (page > totalPages.value) {
    jumpPageNum.value = totalPages.value
    changePage(totalPages.value)
  } else {
    changePage(page)
  }
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
  fetchLogs()
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  min-height: 600px;
  overflow: hidden;
}

.audit-logs {
  padding: 2rem;
  height: 100%;
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.95rem;
}

/* 筛选区域 */
.filter-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.filter-row {
  display: flex;
  gap: 1.5rem;
  align-items: flex-end;
  margin-bottom: 1rem;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-item label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #4a5568;
}

.filter-input,
.filter-select {
  padding: 0.625rem 0.875rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: white;
}

.filter-input:hover,
.filter-select:hover {
  border-color: #cbd5e0;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.filter-actions {
  display: flex;
  gap: 0.75rem;
}

.search-btn,
.reset-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.reset-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.reset-btn:hover {
  background: #cbd5e0;
}

/* 表格容器 */
.table-container {
  position: relative;
  overflow-y: auto;
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #e2e8f0;

  height: 350px;  /* 固定高度 */
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  z-index: 10;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top-color: #f5576c;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 表格样式 */
.logs-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.logs-table th{
  position: sticky;
  top: 0;
  z-index: 2;
  background: #f8fafc;
}
.logs-table td {
  padding: 0.875rem 1rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.logs-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #4a5568;
  white-space: nowrap;
}

.logs-table tbody tr:hover {
  background: #f7fafc;
}

.logs-table tbody tr:nth-child(even) {
  background: #fafbfc;
}

.logs-table tbody tr:nth-child(even):hover {
  background: #f0f4f8;
}

.empty-row {
  text-align: center;
  padding: 3rem !important;
  color: #a0aec0;
}

.empty-row svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-row span {
  display: block;
  font-size: 1rem;
}

/* 操作类型标签 */
.action-tag {
  display: inline-block;
  padding: 0.25rem 0.625rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
}

.action-create {
  background: #c6f6d5;
  color: #276749;
}

.action-update {
  background: #bee3f8;
  color: #2b6cb0;
}

.action-delete {
  background: #fed7d7;
  color: #c53030;
}

.action-login {
  background: #e9d8fd;
  color: #6b46c1;
}

.action-logout {
  background: #feebc8;
  color: #c05621;
}

.action-view {
  background: #e2e8f0;
  color: #4a5568;
}

.message-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  font-size: 0.875rem;
  color: #718096;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #f5576c;
  color: #f5576c;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.875rem;
  color: #4a5568;
  font-weight: 500;
}

.page-size-select {
  padding: 0.5rem 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  background: white;
  cursor: pointer;
}

.page-size-select:focus {
  outline: none;
  border-color: #f5576c;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #4a5568;
}

.page-jump-input {
  width: 60px;
  padding: 0.375rem 0.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.875rem;
  text-align: center;
  transition: all 0.3s ease;
}

.page-jump-input:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.page-jump-input::-webkit-inner-spin-button,
.page-jump-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.page-jump-btn {
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-jump-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.4);
}

/* 响应式 */
@media (max-width: 1024px) {
  .profile-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
    padding: 1rem;
    gap: 1rem;
  }

  .audit-logs {
    padding: 1.5rem;
  }

  .page-header h2 {
    font-size: 1.5rem;
  }

  .filter-row {
    flex-direction: column;
    gap: 1rem;
  }

  .filter-item {
    width: 100%;
  }

  .filter-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .logs-table {
    font-size: 0.8rem;
  }

  .logs-table th,
  .logs-table td {
    padding: 0.625rem 0.5rem;
  }

  .pagination {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .pagination-controls {
    flex-wrap: wrap;
  }
}
</style>
