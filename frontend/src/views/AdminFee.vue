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

          <router-link to="/admin/schedules" class="nav-item">
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

          <router-link to="/admin/fees" class="nav-item active">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            <span>号别管理</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <div class="fee-management">
          <!-- 头部操作区 -->
          <div class="header-section">
            <div class="title-group">
              <h2>号别管理</h2>
              <p class="subtitle">管理号别类型、挂号人数、费用等信息</p>
            </div>
            <button @click="openCreateDialog" class="create-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              新增号别
            </button>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
           
            </div>

          </div>

          <!-- 表格区域 -->
          <div class="table-container">
            <table class="fee-table">
              <thead>
                <tr>
                  <th>序号</th>
                  <th>号别名称</th>
                  <th>号别类型</th>
                  <th>默认挂号人数</th>
                  <th>费用（元）</th>
                  <th>描述</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="feeList.length === 0" class="empty-row">
                  <td colspan="8" class="empty-cell">
                    <div class="empty-state">
                      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="12" cy="12" r="1"></circle>
                        <circle cx="19" cy="12" r="1"></circle>
                        <circle cx="5" cy="12" r="1"></circle>
                      </svg>
                      <p>暂无号别信息，请新增号别</p>
                    </div>
                  </td>
                </tr>
                <tr v-for="(item, index) in feeList" :key="item.appointmentTypeId" class="table-row">
                  <td class="text-center">{{ index + 1 }}</td>
                  <td class="font-medium">{{ item.typeName }}</td>
                  <td>
                    <span class="type-badge" :class="`type-${item.typeKey}`">
                      {{ item.typeKey }}
                    </span>
                  </td>
                  <td class="text-center">{{ item.maxSlots }}</td>
                  <td class="text-center font-medium">¥{{ item.feeAmount.toFixed(2) }}</td>
                  <td class="desc-text">{{ item.description || '-' }}</td>
                  <td class="text-center">
                    <span class="status-badge" :class="`status-1`">
                      启用
                    </span>
                  </td>
                  <td class="action-buttons">
                    <button @click="editFee(item)" class="btn-edit">编辑</button>
                    <button @click="deleteFee(item)" class="btn-delete">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  </div>

  <!-- 新增/编辑弹窗 -->
  <transition name="modal">
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <button @click="closeModal" class="close-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <div class="modal-header">
          <h2>{{ isEditing ? '编辑号别' : '新增号别' }}</h2>
        </div>

        <form @submit.prevent="submitForm" class="modal-body">
          <div class="form-group">
            <label class="form-label">号别名称 <span class="required">*</span></label>
            <input
              v-model="formData.typeName"
              type="text"
              placeholder="例如：普通号、专家号"
              class="form-input"
              required />
          </div>

          <div class="form-group">
            <label class="form-label">号别标识 <span class="required">*</span></label>
            <input
              v-model="formData.typeKey"
              type="text"
              placeholder="例如：normal、expert、special"
              class="form-input"
              required />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">默认挂号人数 <span class="required">*</span></label>
              <input
                v-model.number="formData.maxSlots"
                type="number"
                min="1"
                max="100"
                placeholder="0"
                class="form-input"
                required />
            </div>

            <div class="form-group">
              <label class="form-label">费用（元） <span class="required">*</span></label>
              <input
                v-model.number="formData.feeAmount"
                type="number"
                min="0"
                step="0.01"
                placeholder="0.00"
                class="form-input"
                required />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">描述</label>
            <textarea
              v-model="formData.description"
              placeholder="请输入号别描述信息"
              class="form-textarea"
              rows="3"></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" @click="closeModal" class="btn-cancel">取消</button>
            <button type="submit" class="btn-submit">{{ isEditing ? '更新' : '创建' }}</button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const showModal = ref(false)
const isEditing = ref(false)
const feeList = ref([])

const formData = reactive({
  appointmentTypeId: null,
  typeName: '',
  typeKey: '',
  maxSlots: null,
  feeAmount: null,
  description: ''
})

const initialFormData = {
  appointmentTypeId: null,
  typeName: '',
  typeKey: '',
  maxSlots: null,
  feeAmount: null,
  description: ''
}

const averageFee = computed(() => {
  if (feeList.value.length === 0) return '0.00'
  const total = feeList.value.reduce((sum, item) => sum + item.feeAmount, 0)
  return (total / feeList.value.length).toFixed(2)
})

const totalCapacity = computed(() => {
  return feeList.value.reduce((sum, item) => sum + item.maxSlots, 0)
})

function getFeeTypeLabel(type) {
  const typeMap = {
    normal: '普通',
    expert: '专家',
    special: '特需'
  }
  return typeMap[type] || '-'
}

function openCreateDialog() {
  isEditing.value = false
  Object.assign(formData, initialFormData)
  showModal.value = true
}

function editFee(item) {
  isEditing.value = true
  Object.assign(formData, {
    appointmentTypeId: item.appointmentTypeId,
    typeName: item.typeName,
    typeKey: item.typeKey,
    maxSlots: item.maxSlots,
    feeAmount: item.feeAmount,
    description: item.description
  })
  showModal.value = true
}

async function submitForm() {
  try {
    if (isEditing.value) {
      const response = await axios.put(`/api/admin/appointment-types/${formData.appointmentTypeId}`, {
        typeName: formData.typeName,
        typeKey: formData.typeKey,
        maxSlots: formData.maxSlots,
        feeAmount: formData.feeAmount,
        description: formData.description
      })
      if (response.data.code === 200) {
        alert('更新成功')
        closeModal()
        fetchFeeList()
      }
    } else {
      const response = await axios.post('/api/admin/appointment-types', {
        typeName: formData.typeName,
        typeKey: formData.typeKey,
        maxSlots: formData.maxSlots,
        feeAmount: formData.feeAmount,
        description: formData.description
      })
      if (response.data.code === 200) {
        alert('创建成功')
        closeModal()
        fetchFeeList()
      }
    }
  } catch (err) {
    alert('操作失败：' + (err.response?.data?.message || err.message))
  }
}

async function toggleStatus(item) {
  try {
    const newStatus = item.status === 1 ? 0 : 1
    const response = await axios.put(`/api/admin/appointment-types/${item.appointmentTypeId}/status`, {
      status: newStatus
    })
    if (response.data.code === 200) {
      item.status = newStatus
      alert(newStatus === 1 ? '已启用' : '已禁用')
    }
  } catch (err) {
    alert('操作失败：' + (err.response?.data?.message || err.message))
  }
}

async function deleteFee(item) {
  if (!confirm(`确定要删除"${item.typeName}"吗？`)) return

  try {
    const response = await axios.delete(`/api/admin/appointment-types/${item.appointmentTypeId}`)
    if (response.data.code === 200) {
      alert('删除成功')
      fetchFeeList()
    }
  } catch (err) {
    alert('删除失败：' + (err.response?.data?.message || err.message))
  }
}

function closeModal() {
  showModal.value = false
  Object.assign(formData, initialFormData)
}

async function fetchFeeList() {
  try {
    const response = await axios.get('/api/admin/appointment-types/list')
    if (response.data.code === 200) {
      feeList.value = response.data.data || []
    }
  } catch (err) {
    console.error('获取号别列表失败', err)
  }
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
  await fetchFeeList()
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
  max-width: 1800px;
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

.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.fee-management {
  padding: 2.5rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.create-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: #f7fafc;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  border-color: #f5576c;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.stat-value {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.fee-table {
  width: 100%;
  border-collapse: collapse;
}

.fee-table thead {
  background: #f7fafc;
}

.fee-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  font-size: 0.95rem;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.fee-table tbody tr {
  border-bottom: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.fee-table tbody tr:hover {
  background: #f7fafc;
}

.fee-table tbody tr:last-child {
  border-bottom: none;
}

.fee-table td {
  padding: 1.25rem 1rem;
  color: #2d3748;
  font-size: 0.95rem;
  vertical-align: middle;
}

.text-center {
  text-align: center;
}

.font-medium {
  font-weight: 500;
}

.desc-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #718096;
  font-size: 0.9rem;
}

.type-badge {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

.type-normal {
  background: #e0f2fe;
  color: #0369a1;
}

.type-expert {
  background: #fef3c7;
  color: #d97706;
}

.type-special {
  background: #fce7f3;
  color: #be185d;
}

.status-badge {
  display: inline-block;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-1 {
  background: #dcfce7;
  color: #166534;
}

.status-0 {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  white-space: nowrap;
}

.action-buttons button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-edit {
  background: #dbeafe;
  color: #0369a1;
}

.btn-edit:hover {
  background: #bfdbfe;
}

.btn-disable {
  background: #fed7aa;
  color: #92400e;
}

.btn-disable:hover {
  background: #fdba74;
}

.btn-enable {
  background: #dcfce7;
  color: #166534;
}

.btn-enable:hover {
  background: #bbf7d0;
}

.btn-delete {
  background: #fee2e2;
  color: #991b1b;
}

.btn-delete:hover {
  background: #fecaca;
}

.empty-row {
  height: 300px;
}

.empty-cell {
  text-align: center;
  padding: 0 !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a0aec0;
}

.empty-state svg {
  margin-bottom: 1rem;
  color: #cbd5e0;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  width: 32px;
  height: 32px;
  background: #f7fafc;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #718096;
  z-index: 1;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #2d3748;
}

.modal-header {
  padding: 2rem 2rem 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.modal-body {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-size: 0.95rem;
  font-weight: 600;
}

.required {
  color: #f5576c;
  margin-left: 0.25rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  font-family: inherit;
  transition: all 0.3s ease;
  background: white;
  color: #2d3748;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #a0aec0;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  color: #2d3748;
  font-weight: 500;
  margin-bottom: 0;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #f5576c;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding-top: 2rem;
  border-top: 2px solid #f0f0f0;
  margin-top: 2rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn-cancel {
  background: #f7fafc;
  color: #4a5568;
  border: 2px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.btn-submit {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

@media (max-width: 1024px) {
  .profile-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .sidebar {
    width: 240px;
  }

  .fee-management {
    padding: 1.5rem;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }

  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }

  .action-buttons button {
    font-size: 0.8rem;
    padding: 0.4rem 0.75rem;
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

  .avatar svg {
    width: 32px;
    height: 32px;
  }

  .sidebar-header h3 {
    font-size: 1.1rem;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 0.5rem;
  }

  .nav-item {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem;
    min-width: 90px;
    text-align: center;
    font-size: 0.9rem;
  }

  .nav-item svg {
    width: 16px;
    height: 16px;
  }

  .header-section {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .create-btn {
    width: 100%;
    justify-content: center;
  }

  .fee-management {
    padding: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .stat-card {
    gap: 1rem;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .table-container {
    font-size: 0.85rem;
  }

  .fee-table th,
  .fee-table td {
    padding: 0.75rem 0.5rem;
  }

  .fee-table th {
    font-size: 0.8rem;
  }

  .type-badge {
    padding: 0.35rem 0.75rem;
    font-size: 0.75rem;
  }

  .status-badge {
    padding: 0.3rem 0.6rem;
    font-size: 0.75rem;
  }

  .action-buttons {
    gap: 0.25rem;
  }

  .action-buttons button {
    padding: 0.3rem 0.5rem;
    font-size: 0.7rem;
  }

  .modal-container {
    margin: 1rem;
  }

  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }

  .desc-text {
    max-width: 100px;
  }
}

@media (max-width: 480px) {
  .profile-layout {
    padding: 0.75rem;
    gap: 0.75rem;
  }

  .sidebar {
    padding: 1rem;
  }

  .sidebar-nav {
    gap: 0.25rem;
  }

  .nav-item {
    min-width: 70px;
    padding: 0.5rem;
    gap: 0.25rem;
  }

  .fee-management {
    padding: 0.75rem;
  }

  .header-section {
    gap: 0.5rem;
  }

  .title-group h2 {
    font-size: 1.5rem;
  }

  .create-btn {
    font-size: 0.85rem;
    padding: 0.6rem 1rem;
  }

  .stats-grid {
    gap: 0.75rem;
  }

  .stat-card {
    padding: 1rem;
    gap: 0.75rem;
  }

  .stat-label {
    font-size: 0.8rem;
  }

  .stat-value {
    font-size: 1.25rem;
  }

  .fee-table th,
  .fee-table td {
    padding: 0.5rem 0.25rem;
    font-size: 0.75rem;
  }

  .type-badge,
  .status-badge,
  .action-buttons button {
    font-size: 0.7rem;
    padding: 0.25rem 0.5rem;
  }

  .empty-state p {
    font-size: 0.9rem;
  }
}
</style>