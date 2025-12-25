<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="overview-wrapper">

      <!-- 空科室提示 -->
      <div v-if="emptyDepts.length" class="empty-dept-alert">
        <div class="alert-content">
          <svg xmlns="https://www.w3.org/2000/svg" width="24" height="24" fill="none" stroke="#856404" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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

      <!-- 编辑科室弹窗 -->
      <div v-if="showEditForm" class="modal-overlay" @click.self="showEditForm = false">
        <div class="create-form">
          <h3>编辑科室信息</h3>

          <div class="form-item">
            <label>科室名称:</label>
            <input type="text" v-model="editDeptName" placeholder="输入科室名称" />
          </div>

          <div class="form-item">
            <label>科室描述:</label>
            <textarea v-model="editDeptDescription" placeholder="输入科室描述"></textarea>
          </div>

          <div class="form-actions">
            <button class="confirm-btn" @click="handleUpdateDepartment">保存修改</button>
            <button class="cancel-btn" @click="showEditForm = false">取消</button>
          </div>
        </div>
      </div>

      <!-- 科室树 -->
      <div class="departments-list">
        <div v-for="dept in departments" :key="dept.id" class="department-group">
          <div class="primary-department">
            <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
            </svg>
            <h2>{{ dept.name }}</h2>
            <span class="sub-count">{{ (dept.subDepartments || []).length }}</span>
          </div>

          <div class="sub-departments">
            <div v-for="subDept in (dept.subDepartments || [])" :key="subDept.id" class="sub-dept-card">
              <div class="card-content">
                <h3>{{ subDept.name }}</h3>
                <div class="location-info">
                  <span class="location-tag floor-tag">
                    {{ subDept.floor }}楼
                  </span>
                  <span class="location-tag room-tag">
                    主诊室：{{ subDept.room }}
                  </span>
                </div>
                <p class="dept-desc" v-if="subDept.description" :title="subDept.description">{{ subDept.description }}</p>
              </div>
              <div class="card-actions">
                <button class="action-btn edit-btn" @click="openEditModal(subDept)">编辑</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建结果反馈弹窗 -->
      <div v-if="toast.visible" class="toast">
        {{ toast.message }}
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
const showEditForm = ref(false)
const departments = ref([])
const availableAreas = ref([])

const selectedParentDeptId = ref(null)
const selectedArea = ref(null)
const newDeptName = ref('')
const newDeptDescription = ref('')

// 编辑相关状态
const editingDeptId = ref(null)
const editDeptName = ref('')
const editDeptDescription = ref('')

const router = useRouter()

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

async function confirmDelete(dept) {
  await deleteDepartment(dept.id)
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


function updateNavHeight() {
  if (navRef.value?.$el) navHeight.value = navRef.value.$el.offsetHeight + 30
}
function handleResize() { updateNavHeight() }

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
        description: sub.description,
        floor:sub.floor,
        room: sub.room
      }))
    }))
  } catch (err) { console.error(err) }
}

async function fetchAreas() {
  try {
    const res = await axios.get('/api/departments/available-areas')
    availableAreas.value = res.data.data || []
  } catch (err) { console.error(err) }
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
    await fetchEmptyDepartments() // 刷新空科室
    showToast('科室创建成功')
  } catch (err) {
    console.error(err)
    showToast('科室创建失败，请重试')
  }
}

function openEditModal(dept) {
  editingDeptId.value = dept.id
  editDeptName.value = dept.name
  editDeptDescription.value = dept.description || ''
  showEditForm.value = true
}

async function handleUpdateDepartment() {
  if (!editDeptName.value.trim()) {
    showToast('科室名称不能为空')
    return
  }

  try {
    const payload = {
      deptName: editDeptName.value,
      description: editDeptDescription.value
    }
    // 注意：这里只更新名称和描述，位置信息（building/floor等）保持不变或由后端处理
    await axios.put(`/api/departments/${editingDeptId.value}`, payload)
    
    showToast('修改成功')
    showEditForm.value = false
    await fetchDepartments()
  } catch (err) {
    console.error(err)
    showToast('修改失败，请重试')
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
/* 全局容器：柔和的渐变背景 */
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  padding-bottom: 2rem;
}

.overview-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  /* 确保内容在导航栏下方 */
  padding-top: 1rem;
}

/* 顶部 Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
  animation: slideDown 0.6s ease-out;
}

.header-section h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  letter-spacing: -0.5px;
  position: relative;
  padding-left: 1rem;
}

.header-section h1::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 24px;
  background: #667eea;
  border-radius: 2px;
}

/* 按钮通用样式 */
button {
  font-family: inherit;
}

.create-btn {
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  cursor: pointer;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.create-btn:active {
  transform: translateY(0);
}

/* 空科室提示条 - 磨砂效果 */
.empty-dept-alert {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 187, 51, 0.3);
  color: #b7791f;
  padding: 1rem 1.5rem;
  border-radius: 16px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.8s ease-out;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 500;
}

.empty-dept-item {
  background: #fff3cd;
  padding: 2px 8px;
  border-radius: 6px;
  margin: 0 2px;
  font-weight: 600;
}

.assign-btn {
  background: linear-gradient(to right, #f6ad55, #ed8936);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1.25rem;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(237, 137, 54, 0.3);
  transition: all 0.2s;
}

.assign-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(237, 137, 54, 0.4);
}

.delete-btn {
  background: transparent;
  color: #e53e3e;
  border: 1px solid #e53e3e;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 0.75rem;
  cursor: pointer;
  margin-left: 4px;
  transition: all 0.2s;
}

.delete-btn:hover {
  background: #e53e3e;
  color: white;
}


/* 科室列表与卡片 */
.departments-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.department-group {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.5);
  animation: fadeInUp 0.6s ease-out both;
}

.department-group:nth-child(2) { animation-delay: 0.1s; }
.department-group:nth-child(3) { animation-delay: 0.2s; }

.primary-department {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #edafaf20;
}

.primary-department h2 {
  font-size: 1.5rem;
  color: #2d3748;
  margin: 0;
  font-weight: 700;
}

.primary-department svg {
  color: #667eea;
  background: #ebf4ff;
  padding: 8px;
  border-radius: 12px;
  box-sizing: content-box;
}

.sub-count {
  background: #edf2f7;
  color: #4a5568;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.sub-departments {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.sub-dept-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.sub-dept-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
  border-color: #667eea;
}

.sub-dept-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s;
}

.sub-dept-card:hover::before {
  opacity: 1;
}

.card-content h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.dept-desc {
  margin: 0;
  font-size: 0.9rem;
  color: #718096;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 2.7em; /* 保持卡片高度一致 */
}

/* 卡片操作按钮 - 悬浮滑出效果 */
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px dashed #e2e8f0;
  opacity: 0.6;
  transform: translateY(5px);
  transition: all 0.3s ease;
}

.sub-dept-card:hover .card-actions {
  opacity: 1;
  transform: translateY(0);
}

.action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-btn {
  background: #edf2f7;
  color: #4a5568;
}

.edit-btn:hover {
  background: #cbd5e0;
  color: #2d3748;
}

/* 弹窗样式 - 玻璃拟态 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(20, 25, 40, 0.4);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.3s;
}

.create-form {
  background: white;
  border-radius: 24px;
  padding: 2.5rem;
  width: 480px;
  max-width: 90%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.create-form h3 {
  margin-top: 0;
  margin-bottom: 2rem;
  color: #1a202c;
  font-size: 1.5rem;
  text-align: center;
  font-weight: 800;
}

.form-item {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-weight: 600;
  color: #4a5568;
  font-size: 0.95rem;
}

.form-item input,
.form-item textarea,
.form-item select {
  padding: 12px 16px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  font-size: 1rem;
  transition: all 0.2s;
  background: #f7fafc;
}

.form-item input:focus,
.form-item textarea:focus,
.form-item select:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-item textarea {
  min-height: 100px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 2.5rem;
}

.confirm-btn {
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  padding: 10px 24px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.cancel-btn {
  background: #edf2f7;
  color: #4a5568;
  padding: 10px 24px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #e2e8f0;
  color: #2d3748;
}

/* Toast */
.toast {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%);
  padding: 1rem 2rem;
  border-radius: 50px;
  background: rgba(45, 55, 72, 0.95);
  color: white;
  font-weight: 600;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 3000;
  animation: slideDownFade 0.4s ease-out;
  backdrop-filter: blur(4px);
}

/* 动画定义 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes slideDownFade {
  from { opacity: 0; transform: translate(-50%, -20px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}
</style>
