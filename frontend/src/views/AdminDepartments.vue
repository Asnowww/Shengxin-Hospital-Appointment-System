<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="overview-wrapper">

      <!-- 空科室提示 -->
      <div v-if="emptyDepts.length" class="empty-dept-alert">
        <div class="alert-content">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" stroke="#856404" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <circle cx="12" cy="16" r="1"/>
          </svg>
          <span>
            有 {{ emptyDepts.length }} 个科室尚未分配医生：
            <span v-for="(dept, index) in emptyDepts" :key="dept.deptId" class="empty-dept-item">
              {{ dept.deptName }}
              <button class="delete-btn" @click="deleteDepartment(dept.deptId)">删除</button>
              <span v-if="index < emptyDepts.length - 1">，</span>
            </span>
          </span>
        </div>
        <button class="assign-btn" @click="goAssignDoctors">
          去分配
        </button>
      </div>

      <!-- 标题和创建按钮 -->
      <div class="header-section">
        <h1>科室管理</h1>
        <button class="create-btn" @click="showCreateForm = true">创建二级科室</button>
      </div>

      <!-- 创建科室弹窗 -->
      <div v-if="showCreateForm" class="modal-overlay" @click.self="showCreateForm = false">
        <div class="create-form">
          <h3>创建二级科室</h3>

          <div class="form-item">
            <label>父科室:</label>
            <select v-model="selectedParentDeptId">
              <option value="" disabled>请选择父科室</option>
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                {{ dept.name }}
              </option>
            </select>
          </div>

          <div class="form-item">
            <label>诊室区域:</label>
            <select v-model="selectedArea">
              <option value="" disabled>请选择区域</option>
              <option v-for="area in availableAreas" :key="area.building + '-' + area.floor" :value="area">
                {{ area.building }} - {{ area.floor }}楼 可用: {{ area.availableRooms.join(', ') }}
              </option>
            </select>
          </div>

          <div class="form-item">
            <label>科室名称:</label>
            <input type="text" v-model="newDeptName" placeholder="输入科室名称" />
          </div>

          <div class="form-item">
            <label>科室描述:</label>
            <textarea v-model="newDeptDescription" placeholder="输入科室描述"></textarea>
          </div>

          <div class="form-actions">
            <button class="confirm-btn" @click="createDepartment">确认创建</button>
            <button class="cancel-btn" @click="showCreateForm = false">取消</button>
          </div>
        </div>
      </div>

      <!-- 科室树 -->
      <div class="departments-list">
        <div v-for="dept in departments" :key="dept.id" class="department-group">
          <div class="primary-department">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
            </svg>
            <h2>{{ dept.name }}</h2>
            <span class="sub-count">{{ (dept.subDepartments || []).length }}</span>
          </div>

          <div v-for="subDept in (dept.subDepartments || [])"
               :key="subDept.id"
               class="sub-dept-card">
            <div class="card-content">
              <h3>{{ subDept.name }}</h3>
              <div class="dept-location">
                <span class="location-tag">{{ subDept.building }}</span>
                <span class="location-tag">{{ subDept.floor }}楼</span>
                <span class="location-tag">{{ subDept.room }}室</span>
              </div>
            </div>

            <button class="edit-btn" @click="openEdit(subDept)">
              编辑
            </button>
          </div>

        </div>
      </div>

      <!-- 创建结果反馈弹窗 -->
      <div v-if="toast.visible" class="toast">
        {{ toast.message }}
      </div>

      <!-- 编辑科室弹窗 -->
      <div v-if="showEditForm" class="modal-overlay" @click.self="showEditForm = false">
        <div class="create-form">
          <h3>修改科室信息</h3>

          <div class="form-item">
            <label>科室名称</label>
            <input v-model="editDept.deptName" />
          </div>

          <div class="form-item">
            <label>科室描述</label>
            <textarea v-model="editDept.description" rows="4" />
          </div>

          <div class="form-actions">
            <button class="confirm-btn" @click="submitEdit">保存</button>
            <button class="cancel-btn" @click="showEditForm = false">取消</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const navRef = ref(null)
const navHeight = ref(110)

const showCreateForm = ref(false)
const departments = ref([])
const availableAreas = ref([])

const selectedParentDeptId = ref(null)
const selectedArea = ref(null)
const newDeptName = ref('')
const newDeptDescription = ref('')

const router = useRouter()

const showEditForm = ref(false)
const editDept = ref({})

function goAssignDoctors() {
  // 跳转到医生管理页面
  router.push({ name: 'doctorManagement' })
}

// Toast 弹窗状态
const toast = ref({ visible: false, message: '' })
function showToast(message, duration = 2000) {
  toast.value.message = message
  toast.value.visible = true
  setTimeout(() => { toast.value.visible = false }, duration)
}

// 空科室数组
const emptyDepts = ref([])

// 获取空科室
async function fetchEmptyDepartments() {
  try {
    const res = await axios.get('/api/departments/empty')
    console.log('空科室接口返回:', res.data)

    const msg = res.data?.message || ''
    console.log('原始message:', msg)

    // 正则匹配提取所有 EmptyDeptDTO 对象
    const deptMatches = msg.match(/EmptyDeptDTO\([^)]+\)/g) || []
    console.log('匹配到的dept字符串:', deptMatches)

    // 解析每个科室信息
    emptyDepts.value = deptMatches.map(deptStr => {
      // 提取 deptId
      const idMatch = deptStr.match(/deptId=(\d+)/)
      const deptId = idMatch ? parseInt(idMatch[1]) : Math.random()

      // 提取 deptName
      const nameMatch = deptStr.match(/deptName=([^,]+)/)
      const deptName = nameMatch ? nameMatch[1].trim() : '未知科室'

      return {
        deptId,
        deptName,
      }
    })

    console.log('解析后的空科室数据:', emptyDepts.value)

  } catch (err) {
    console.error('获取空科室失败', err)
    showToast('获取空科室列表失败')
    emptyDepts.value = []
  }
}

async function deleteDepartment(deptId) {
  if (!confirm('确定删除该科室吗？此操作不可恢复！')) return
  try {
    const res = await axios.post(`/api/departments/delete/${deptId}`)
    if (res.data?.code === 200) {
      showToast('删除成功')
      await fetchEmptyDepartments()
      await fetchDepartments()
    } else {
      showToast(res.data?.message || '删除失败')
    }
  } catch (err) {
    console.error(err)
    showToast('删除失败，请重试')
  }
}

async function openEdit(subDept) {
  try {
    // 从接口获取完整的科室信息
    const res = await axios.get(`/api/departments/${subDept.id}`)
    const deptData = res.data?.data

    if (deptData) {
      editDept.value = {
        deptId: deptData.deptId,
        deptName: deptData.deptName,
        description: deptData.description || ''
      }
      showEditForm.value = true
    } else {
      showToast('获取科室信息失败')
    }
  } catch (err) {
    console.error(err)
    showToast('获取科室信息失败')
  }
}

async function submitEdit() {
  try {
    await axios.put(
        `/api/departments/${editDept.value.deptId}`,
        editDept.value
    )
    showToast('修改成功')
    showEditForm.value = false
    await fetchDepartments()
    await fetchEmptyDepartments()
  } catch (err) {
    console.error(err)
    showToast('修改失败')
  }
}

function updateNavHeight() {
  if (navRef.value?.$el) navHeight.value = navRef.value.$el.offsetHeight + 30
}

function handleResize() {
  updateNavHeight()
}

async function fetchDepartments() {
  try {
    const res = await axios.get('/api/departments/all')
    const list = res.data?.data || []
    departments.value = list.map(dept => ({
      id: dept.deptId,
      name: dept.deptName,
      subDepartments: (dept.children || []).map(sub => ({
        id: sub.deptId,
        name: sub.deptName,
        building: sub.building,
        floor: sub.floor,
        room: sub.room
      }))
    }))
  } catch (err) {
    console.error(err)
  }
}

async function fetchAreas() {
  try {
    const res = await axios.get('/api/departments/available-areas')
    availableAreas.value = res.data.data || []
  } catch (err) {
    console.error(err)
  }
}

async function createDepartment() {
  if (!selectedParentDeptId.value || !selectedArea.value || !newDeptName.value.trim()) {
    showToast('请填写完整信息')
    return
  }

  const payload = {
    parentDeptId: selectedParentDeptId.value,
    deptName: newDeptName.value,
    building: selectedArea.value.building,
    floor: selectedArea.value.floor,
    mainRoom: selectedArea.value.availableRooms[0],
    roomNumbers: selectedArea.value.availableRooms.slice(1),
    description: newDeptDescription.value
  }

  try {
    await axios.post('/api/departments/create-with-rooms', payload)
    showCreateForm.value = false
    newDeptName.value = ''
    newDeptDescription.value = ''
    selectedParentDeptId.value = null
    selectedArea.value = null
    await fetchDepartments()
    await fetchAreas()
    await fetchEmptyDepartments()
    showToast('科室创建成功')
  } catch (err) {
    console.error(err)
    showToast('科室创建失败，请重试')
  }
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
  await fetchDepartments()
  await fetchAreas()
  await fetchEmptyDepartments()
})

onUnmounted(() => window.removeEventListener('resize', handleResize))
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f7fafc;
}

.overview-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 140px);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e2e8f0;
}

.header-section h1 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.5rem;
  border-radius: 8px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  cursor: pointer;
  border: none;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.2);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.3);
}

.create-btn:active {
  transform: translateY(0);
}

.departments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.department-group {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.department-group:hover {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
}

.primary-department {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.primary-department svg {
  color: #f5576c;
}

.primary-department h2 {
  margin: 0;
  color: #2d3748;
  font-size: 1.1rem;
  font-weight: 600;
  flex: 1;
}

.sub-count {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.sub-departments {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.sub-dept-card {
  background: linear-gradient(135deg, #fef5f7 0%, #f0f4ff 100%);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.sub-dept-card:hover {
  border-color: #f5576c;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.15);
  transform: translateY(-2px);
}

.card-content {
  flex: 1;
}

.card-content h3 {
  margin: 0 0 0.75rem 0;
  color: #2d3748;
  font-size: 1.05rem;
  font-weight: 600;
}

.dept-location {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.location-tag {
  background: white;
  color: #4a5568;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.location-tag:hover {
  border-color: #cbd5e0;
  background: #f7fafc;
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
  backdrop-filter: blur(4px);
}

.create-form {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  width: 480px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.create-form h3 {
  margin: 0 0 1.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 600;
}

.form-item {
  margin-bottom: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-item label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #4a5568;
}

.form-item input,
.form-item textarea,
.form-item select {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-item input:focus,
.form-item textarea:focus,
.form-item select:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-item textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 2rem;
}

.confirm-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.2);
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.3);
}

.confirm-btn:active {
  transform: translateY(0);
}

.cancel-btn {
  background: #edf2f7;
  color: #4a5568;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #e2e8f0;
}

.toast {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%);
  padding: 1rem 2rem;
  border-radius: 10px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  font-weight: 600;
  font-size: 0.95rem;
  z-index: 2000;
  box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translate(-50%, -20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}

.empty-dept-alert {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #fff8e1 0%, #fff3cd 100%);
  color: #856404;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 12px rgba(133, 100, 4, 0.1);
  font-weight: 500;
  border: 2px solid #ffe9a8;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
}

.alert-content svg {
  flex-shrink: 0;
}

.assign-btn {
  background: linear-gradient(135deg, #ffd700 0%, #ffbb33 100%);
  border: none;
  border-radius: 8px;
  padding: 0.625rem 1.25rem;
  cursor: pointer;
  font-weight: 600;
  color: #856404;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 187, 51, 0.3);
  white-space: nowrap;
}

.assign-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 187, 51, 0.4);
}

.delete-btn {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 0.375rem 0.75rem;
  cursor: pointer;
  font-weight: 600;
  color: #f5576c;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  margin-left: 0.5rem;
}

.delete-btn:hover {
  background: #fff5f7;
  border-color: #f5576c;
}

.edit-btn {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.edit-btn:hover {
  background: white;
  border-color: #f5576c;
  color: #f5576c;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .overview-wrapper {
    padding: 1rem;
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

  .sub-departments {
    grid-template-columns: 1fr;
  }

  .create-form {
    padding: 1.5rem;
    width: 95%;
  }

  .empty-dept-alert {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .assign-btn {
    width: 100%;
  }
}
</style>