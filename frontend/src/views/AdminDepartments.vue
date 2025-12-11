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

          <div class="sub-departments">
            <div v-for="subDept in (dept.subDepartments || [])" :key="subDept.id" class="sub-dept-card">
              <div class="card-content">
                <h3>{{ subDept.name }}</h3>
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
const departments = ref([])
const availableAreas = ref([])

const selectedParentDeptId = ref(null)
const selectedArea = ref(null)
const newDeptName = ref('')
const newDeptDescription = ref('')

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
      subDepartments: (dept.children || []).map(sub => ({ id: sub.deptId, name: sub.deptName }))
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
.page-container { min-height: 100vh; }
.overview-wrapper { max-width: 1400px; margin: 0 auto; padding: 2rem; min-height: calc(100vh - 140px); }

.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.create-btn { padding: 0.5rem 1rem; border-radius: 8px; background: #667eea; color: white; cursor: pointer; border: none; transition: all 0.2s; }
.create-btn:hover { background: #5563c1; }

.departments-list { display: flex; flex-direction: column; gap: 1.5rem; }
.department-group { background: white; border-radius: 16px; padding: 1.5rem; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.primary-department { display: flex; align-items: center; gap: 0.75rem; padding: 0.875rem 1rem; margin-bottom: 1rem; background: #f7fafc; border-radius: 10px; }
.sub-departments { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 1rem; }
.sub-dept-card { background: #f0f4ff; border-radius: 10px; padding: 1rem; display: flex; align-items: center; justify-content: space-between; }
.card-content h3 { margin: 0; color: #2d3748; font-size: 1rem; font-weight: 600; }

.modal-overlay { position: fixed; top:0; left:0; right:0; bottom:0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.create-form { background: white; border-radius: 12px; padding: 2rem; width: 400px; max-width: 90%; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.create-form h3 { margin-top: 0; margin-bottom: 1rem; color: #2d3748; }
.form-item { margin-bottom: 12px; display: flex; flex-direction: column; gap: 4px; }
.form-item input, .form-item textarea, .form-item select { padding: 6px 10px; border-radius: 6px; border: 1px solid #cbd5e0; font-size: 0.95rem; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 1rem; }
.confirm-btn { background: #667eea; color: white; padding: 6px 12px; border-radius: 6px; border: none; cursor: pointer; transition: all 0.2s; }
.confirm-btn:hover { background: #5563c1; }
.cancel-btn { background: #e2e8f0; padding: 6px 12px; border-radius: 6px; border: none; cursor: pointer; transition: all 0.2s; }
.cancel-btn:hover { background: #cbd5e0; }

.toast { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); padding: 0.75rem 1.5rem; border-radius: 8px; background-color: #667eea; color: white; font-weight: 500; z-index: 2000; }

.empty-dept-alert { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(90deg, #fff8e1, #fff3cd); color: #856404; padding: 12px 16px; border-radius: 12px; margin-bottom: 1.5rem; box-shadow: 0 4px 12px rgba(0,0,0,0.08); font-weight: 500; }
.alert-content { display: flex; align-items: center; gap: 8px; font-size: 0.95rem; }
.assign-btn {
  background: #ffbb33;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.assign-btn:hover {
  background: #ffaa00;
}

.delete-btn {
  background: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.delete-btn:hover {
  background: #a59e9e;
}

</style>
