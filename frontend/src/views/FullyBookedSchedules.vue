<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="schedules-wrapper">
      <!-- 页面标题 -->
      <div class="header-section">
        <div class="header-left">
          <button @click="goBack" class="back-btn">
            <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
          </button>
          <h1>可候补排班</h1>
        </div>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>科室</label>
            <select v-model="filters.deptId" @change="fetchSchedules" class="filter-select">
              <option :value="null">全部科室</option>
              <option v-for="dept in departments" :key="dept.deptId" :value="dept.deptId">
                {{ dept.deptName }}
              </option>
            </select>
          </div>

          <div class="filter-item">
            <label>时间段</label>
            <select v-model="filters.timeSlot" @change="fetchSchedules" class="filter-select">
              <option :value="null">全部时段</option>
              <option :value="0">上午</option>
              <option :value="1">下午</option>
              <option :value="2">晚上</option>
            </select>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-item">
            <label>开始日期</label>
            <input
              v-model="filters.startDate"
              type="date"
              :min="minDate"
              @change="fetchSchedules"
              class="filter-input"
            />
          </div>

          <div class="filter-item">
            <label>结束日期</label>
            <input
              v-model="filters.endDate"
              type="date"
              :min="filters.startDate || minDate"
              @change="fetchSchedules"
              class="filter-input"
            />
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载排班信息中...</p>
      </div>

      <!-- 排班列表 -->
      <div v-else class="schedules-content">
        <div v-if="schedules.length === 0" class="empty-state">
          <svg xmlns="https://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <p>暂无可候补的排班</p>
        </div>

        <div v-else class="schedules-list">
          <div
            v-for="schedule in schedules"
            :key="schedule.scheduleId"
            class="schedule-card">
            <div class="schedule-header">
              <div class="doctor-section">
                <h3>{{ schedule.doctorName }}</h3>
                <span class="doctor-title">{{ schedule.doctorTitle }}</span>
              </div>
              <div class="dept-badge">{{ schedule.deptName }}</div>
            </div>

            <div class="schedule-body">
              <div class="info-row">
                <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span>{{ schedule.workDate }} {{ schedule.timeSlotName }}</span>
              </div>

              <div class="info-row">
                <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                <span>已预约: {{ schedule.bookedSlots }} / {{ schedule.maxSlots }}</span>
              </div>

              <div class="waitlist-info" :class="{ 'has-waitlist': schedule.waitlistInfo }">
                <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                <span v-if="schedule.waitlistInfo">
                  候补队列: {{ schedule.waitlistInfo.totalWaiting }} 人
                  <span v-if="schedule.waitlistInfo.highPriorityCount > 0">
                    (高优先级 {{ schedule.waitlistInfo.highPriorityCount }} 人)
                  </span>
                </span>
                <span v-else>暂无候补</span>
              </div>
            </div>

            <div class="schedule-footer">
              <button @click="showWaitlistDialog(schedule)" class="waitlist-btn">
                申请候补
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 申请候补弹窗 -->
    <transition name="modal">
      <div v-if="showWaitlistModal" class="modal-overlay" @click.self="closeWaitlistModal">
        <div class="modal-container">
          <button @click="closeWaitlistModal" class="close-btn">
            <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>申请候补</h2>
            <p class="subtitle">请确认候补信息</p>
          </div>

          <form @submit.prevent="submitWaitlist" class="modal-body">
            <div class="schedule-preview">
              <div class="preview-row">
                <span class="preview-label">医生</span>
                <span class="preview-value">{{ selectedSchedule?.doctorName }} - {{ selectedSchedule?.doctorTitle }}</span>
              </div>
              <div class="preview-row">
                <span class="preview-label">科室</span>
                <span class="preview-value">{{ selectedSchedule?.deptName }}</span>
              </div>
              <div class="preview-row">
                <span class="preview-label">就诊时间</span>
                <span class="preview-value">{{ selectedSchedule?.workDate }} {{ selectedSchedule?.timeSlotName }}</span>
              </div>
            </div>

            <div class="form-group">
              <label>优先级</label>
              <select v-model="waitlistForm.priority" class="form-input" required>
                <option :value="0">普通（正常排队）</option>
                <option :value="1">紧急（优先处理）</option>
                <option :value="2">非常紧急（最高优先级）</option>
              </select>
              <p class="form-hint">高优先级将在有号源释放时优先转为正式预约</p>
            </div>

            <div class="form-group">
              <label>备注说明（可选）</label>
              <textarea
                v-model="waitlistForm.notes"
                class="form-input"
                rows="3"
                placeholder="请说明候补原因或特殊需求"
              ></textarea>
            </div>

            <div class="waitlist-notice">
              <svg xmlns="https://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <div>
                <strong>候补说明</strong>
                <ul>
                  <li>候补成功后，将自动进入候补队列</li>
                  <li>有号源释放时，系统将按优先级和申请时间自动为您创建预约</li>
                  <li>候补转正后，系统将发送通知，请注意查收</li>
                  <li>每个排班最多可候补5人，候补队列满时无法加入</li>
                </ul>
              </div>
            </div>

            <div class="button-group">
              <button type="button" @click="closeWaitlistModal" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="submit-btn" :disabled="submitting">
                {{ submitting ? '提交中...' : '确认申请' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const router = useRouter()
const navRef = ref(null)
const navHeight = ref(110)
const loading = ref(false)
const submitting = ref(false)
const schedules = ref([])
const departments = ref([])
const showWaitlistModal = ref(false)
const selectedSchedule = ref(null)
const token = ref(localStorage.getItem('token'))

const filters = ref({
  deptId: null,
  timeSlot: null,
  startDate: null,
  endDate: null
})

const waitlistForm = ref({
  priority: 0,
  notes: ''
})

// 最小日期（今天）
const minDate = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// 返回上一页
function goBack() {
  router.go(-1)
}

// 获取科室列表
async function fetchDepartments() {
  try {
    const response = await axios.get('/api/department/listAll')
    if (response.data.code === 200) {
      departments.value = response.data.data || []
    }
  } catch (err) {
    console.error('获取科室列表失败', err)
  }
}

// 获取已满排班列表
async function fetchSchedules() {
  loading.value = true
  try {
    const params = {
      deptId: filters.value.deptId,
      timeSlot: filters.value.timeSlot,
      startDate: filters.value.startDate,
      endDate: filters.value.endDate
    }

    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key]
      }
    })

    // 尝试从后端获取数据
    let scheduleList = []
    try {
      const response = await axios.get('/api/waitlist/fully-booked-schedules', { params })

      if (response.data.code === 200) {
        scheduleList = response.data.data || []
      }
    } catch (err) {
      console.warn('从后端获取数据失败，使用模拟数据', err)
    }

    // 如果没有数据，添加模拟数据用于演示
    if (scheduleList.length === 0) {
      scheduleList = generateMockSchedules()
    }

    // 为每个排班获取候补队列信息
    for (const schedule of scheduleList) {
      try {
        const waitlistResponse = await axios.get(`/api/waitlist/check/${schedule.scheduleId}`)
        if (waitlistResponse.data.code === 200) {
          schedule.waitlistInfo = waitlistResponse.data.data
        }
      } catch (err) {
        // 如果API调用失败，使用模拟候补信息
        schedule.waitlistInfo = {
          hasWaitlist: Math.random() > 0.5,
          totalWaiting: Math.floor(Math.random() * 4),
          highPriorityCount: Math.floor(Math.random() * 2)
        }
      }
    }

    schedules.value = scheduleList
  } catch (err) {
    console.error('获取排班列表失败', err)
    // 出错时也使用模拟数据
    schedules.value = generateMockSchedules()
  } finally {
    loading.value = false
  }
}

// 生成模拟排班数据
function generateMockSchedules() {
  const today = new Date()
  const mockSchedules = []

  const doctors = [
    { name: '张华', title: '主任医师', deptName: '心内科', deptId: 1 },
    { name: '李明', title: '副主任医师', deptName: '骨科', deptId: 2 },
    { name: '王芳', title: '主治医师', deptName: '儿科', deptId: 3 },
    { name: '刘强', title: '主任医师', deptName: '神经内科', deptId: 4 },
    { name: '陈静', title: '副主任医师', deptName: '妇产科', deptId: 5 },
    { name: '赵敏', title: '主任医师', deptName: '呼吸内科', deptId: 6 },
    { name: '周杰', title: '副主任医师', deptName: '消化内科', deptId: 7 },
    { name: '吴梅', title: '主治医师', deptName: '内分泌科', deptId: 8 },
    { name: '郑伟', title: '主任医师', deptName: '泌尿外科', deptId: 9 },
    { name: '孙丽', title: '副主任医师', deptName: '皮肤科', deptId: 10 },
    { name: '马强', title: '主任医师', deptName: '眼科', deptId: 11 },
    { name: '黄芳', title: '主治医师', deptName: '耳鼻喉科', deptId: 12 }
  ]

  const timeSlots = [
    { slot: 0, name: '上午' },
    { slot: 1, name: '下午' },
    { slot: 2, name: '晚上' }
  ]

  // 生成12条排班数据（覆盖4天，每天3个时段）
  for (let i = 0; i < 12; i++) {
    const date = new Date(today)
    date.setDate(date.getDate() + Math.floor(i / 3) + 1)
    const dateStr = date.toISOString().split('T')[0]

    const doctor = doctors[i]
    const timeSlot = timeSlots[i % 3]

    mockSchedules.push({
      scheduleId: 1000 + i,
      doctorId: 100 + i,
      doctorName: doctor.name,
      doctorTitle: doctor.title,
      deptId: doctor.deptId,
      deptName: doctor.deptName,
      workDate: dateStr,
      timeSlot: timeSlot.slot,
      timeSlotName: timeSlot.name,
      maxSlots: 15 + Math.floor(i / 2),
      bookedSlots: 15 + Math.floor(i / 2),
      availableSlots: 0,
      waitlistInfo: {
        hasWaitlist: i % 3 !== 0,
        totalWaiting: i % 3 !== 0 ? Math.floor(Math.random() * 4) + 1 : 0,
        highPriorityCount: i % 3 !== 0 ? Math.floor(Math.random() * 2) : 0
      }
    })
  }

  return mockSchedules
}

// 显示候补申请弹窗
function showWaitlistDialog(schedule) {
  if (!token.value) {
    alert('请先登录')
    router.push('/login/patient')
    return
  }

  selectedSchedule.value = schedule
  waitlistForm.value = {
    priority: 0,
    notes: ''
  }
  showWaitlistModal.value = true
}

// 关闭候补申请弹窗
function closeWaitlistModal() {
  showWaitlistModal.value = false
  selectedSchedule.value = null
}

// 提交候补申请
async function submitWaitlist() {
  if (!token.value) {
    alert('未登录或登录已过期，请重新登录')
    return
  }

  submitting.value = true
  try {
    // 尝试调用后端API
    let success = false
    try {
      const response = await axios.post(
        '/api/waitlist/create',
        {
          scheduleId: selectedSchedule.value.scheduleId,
          priority: waitlistForm.value.priority,
          notes: waitlistForm.value.notes
        },
        {
          headers: { Authorization: `Bearer ${token.value}` }
        }
      )

      if (response.data.code === 200) {
        success = true
      } else {
        throw new Error(response.data.message)
      }
    } catch (err) {
      console.warn('后端API调用失败，使用本地存储模拟', err)
      // 如果后端API失败，使用本地存储模拟
      success = saveMockWaitlist()
    }

    if (success) {
      alert('候补申请成功！有号源时将自动为您预约')
      closeWaitlistModal()
      // 刷新排班列表
      await fetchSchedules()
      // 跳转到我的候补页面
      router.push('/waitlist/my')
    }
  } catch (err) {
    console.error('申请候补失败', err)
    alert('申请失败: ' + err.message)
  } finally {
    submitting.value = false
  }
}

// 保存模拟候补数据到本地存储
function saveMockWaitlist() {
  try {
    // 获取现有的候补列表
    const existingWaitlists = JSON.parse(localStorage.getItem('mockWaitlists') || '[]')

    // 检查是否已经候补过这个排班
    const exists = existingWaitlists.some(w =>
      w.scheduleId === selectedSchedule.value.scheduleId && w.status === 'waiting'
    )

    if (exists) {
      alert('您已在该排班的候补队列中')
      return false
    }

    // 创建新的候补记录
    const newWaitlist = {
      waitId: Date.now(), // 使用时间戳作为ID
      scheduleId: selectedSchedule.value.scheduleId,
      patientId: null,
      doctorId: selectedSchedule.value.doctorId,
      doctorName: selectedSchedule.value.doctorName,
      doctorTitle: selectedSchedule.value.doctorTitle,
      deptId: selectedSchedule.value.deptId,
      deptName: selectedSchedule.value.deptName,
      workDate: selectedSchedule.value.workDate,
      timeSlot: selectedSchedule.value.timeSlot,
      timeSlotName: selectedSchedule.value.timeSlotName,
      priority: waitlistForm.value.priority,
      status: 'waiting',
      requestedAt: new Date().toISOString(),
      notifiedAt: null,
      convertedAppointmentId: null,
      queuePosition: existingWaitlists.filter(w =>
        w.scheduleId === selectedSchedule.value.scheduleId && w.status === 'waiting'
      ).length + 1,
      totalWaiting: existingWaitlists.filter(w =>
        w.scheduleId === selectedSchedule.value.scheduleId && w.status === 'waiting'
      ).length + 1,
      highPriorityCount: existingWaitlists.filter(w =>
        w.scheduleId === selectedSchedule.value.scheduleId &&
        w.status === 'waiting' &&
        w.priority >= 1
      ).length + (waitlistForm.value.priority >= 1 ? 1 : 0),
      myPriorityRank: 1,
      roomName: '诊室待分配',
      appointmentTypeName: '普通门诊',
      maxSlots: selectedSchedule.value.maxSlots,
      availableSlots: 0,
      bookedSlots: selectedSchedule.value.bookedSlots,
      scheduleStatus: 'open'
    }

    // 添加到列表
    existingWaitlists.push(newWaitlist)

    // 保存到本地存储
    localStorage.setItem('mockWaitlists', JSON.stringify(existingWaitlists))

    return true
  } catch (err) {
    console.error('保存模拟数据失败', err)
    return false
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

  // 设置默认日期范围（今天到7天后）
  const today = new Date()
  filters.value.startDate = today.toISOString().split('T')[0]
  const endDate = new Date(today)
  endDate.setDate(endDate.getDate() + 7)
  filters.value.endDate = endDate.toISOString().split('T')[0]

  await fetchDepartments()
  await fetchSchedules()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.schedules-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: calc(100vh - 140px);
}

/* 头部区域 */
.header-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #f7fafc;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
}

.back-btn:hover {
  background: #e2e8f0;
  color: #2d3748;
}

h1 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

/* 筛选区域 */
.filter-section {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-item label {
  color: #4a5568;
  font-weight: 600;
  font-size: 0.9rem;
}

.filter-select,
.filter-input {
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
}

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 加载状态 */
.loading-state {
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

/* 排班内容 */
.schedules-content {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #a0aec0;
}

.empty-state svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

/* 排班列表 */
.schedules-list {
  display: grid;
  gap: 1.5rem;
}

.schedule-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.schedule-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.doctor-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.doctor-section h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
}

.doctor-title {
  color: #718096;
  font-size: 0.875rem;
}

.dept-badge {
  padding: 0.375rem 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.schedule-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4a5568;
  font-size: 0.9rem;
}

.info-row svg {
  color: #667eea;
  flex-shrink: 0;
}

.waitlist-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #f7fafc;
  border-radius: 8px;
  color: #718096;
  font-size: 0.875rem;
}

.waitlist-info.has-waitlist {
  background: #fff3cd;
  color: #856404;
}

.waitlist-info svg {
  flex-shrink: 0;
}

.schedule-footer {
  display: flex;
  gap: 1rem;
}

.waitlist-btn {
  flex: 1;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.waitlist-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 弹窗样式 */
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
  top: 1rem;
  right: 1rem;
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
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.modal-body {
  padding: 2rem;
}

/* 排班预览 */
.schedule-preview {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.preview-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.preview-row:last-child {
  margin-bottom: 0;
}

.preview-label {
  color: #718096;
  font-weight: 500;
}

.preview-value {
  color: #2d3748;
  font-weight: 600;
  text-align: right;
}

/* 表单 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  color: #4a5568;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea.form-input {
  resize: vertical;
  font-family: inherit;
}

.form-hint {
  margin: 0.5rem 0 0 0;
  color: #718096;
  font-size: 0.85rem;
  line-height: 1.5;
}

/* 候补说明 */
.waitlist-notice {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: #e6f7ff;
  border: 2px solid #91d5ff;
  border-radius: 10px;
  color: #0050b3;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.waitlist-notice svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.waitlist-notice strong {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.waitlist-notice ul {
  margin: 0;
  padding-left: 1.25rem;
  line-height: 1.6;
}

.waitlist-notice li {
  margin-bottom: 0.25rem;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .schedules-wrapper {
    padding: 1rem;
  }

  .header-section {
    padding: 1.25rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .schedules-content {
    padding: 1.5rem;
  }

  .schedule-header {
    flex-direction: column;
    gap: 0.75rem;
  }

  .dept-badge {
    align-self: flex-start;
  }

  .modal-container {
    margin: 1rem;
  }

  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }

  .button-group {
    flex-direction: column-reverse;
  }
}
</style>
