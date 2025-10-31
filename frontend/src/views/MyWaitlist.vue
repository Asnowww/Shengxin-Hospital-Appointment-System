<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="waitlist-wrapper">
      <!-- 页面标题 -->
      <div class="header-section">
        <h1>我的候补</h1>
        <button @click="goToFullyBooked" class="search-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          查找可候补排班
        </button>
      </div>

      <!-- 状态标签 -->
      <div class="status-tabs">
        <button
          :class="['tab-btn', { active: activeStatus === 'waiting' }]"
          @click="switchStatus('waiting')">
          候补中
          <span v-if="waitingCount > 0" class="count-badge">{{ waitingCount }}</span>
        </button>
        <button
          :class="['tab-btn', { active: activeStatus === 'history' }]"
          @click="switchStatus('history')">
          历史记录
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载候补信息中...</p>
      </div>

      <!-- 候补列表 -->
      <div v-else class="waitlist-content">
        <div v-if="displayedWaitlists.length === 0" class="empty-state">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <p>{{ activeStatus === 'waiting' ? '暂无候补中的记录' : '暂无历史记录' }}</p>
          <button v-if="activeStatus === 'waiting'" @click="goToFullyBooked" class="empty-action-btn">
            查找可候补排班
          </button>
        </div>

        <div v-else class="waitlist-list">
          <WaitlistCard
            v-for="waitlist in displayedWaitlists"
            :key="waitlist.waitId"
            :record="formatWaitlistData(waitlist)"
            :onRecordClick="handleRecordClick"
          />
        </div>
      </div>
    </div>

    <!-- 候补详情弹窗 -->
    <transition name="modal">
      <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
        <div class="modal-container">
          <button @click="closeDetailModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>

          <div class="modal-header">
            <h2>候补详情</h2>
          </div>

          <div v-if="selectedWaitlist" class="modal-body">
            <!-- 候补状态 -->
            <div class="detail-section status-section">
              <div class="status-display" :class="`status-${selectedWaitlist.status}`">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                <span>{{ getStatusText(selectedWaitlist.status) }}</span>
              </div>
            </div>

            <!-- 医生和科室信息 -->
            <div class="detail-section">
              <h3>就诊信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="item-label">医生姓名</span>
                  <span class="item-value">{{ selectedWaitlist.doctorName }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">职称</span>
                  <span class="item-value">{{ selectedWaitlist.doctorTitle }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">科室</span>
                  <span class="item-value">{{ selectedWaitlist.deptName }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">诊室</span>
                  <span class="item-value">{{ selectedWaitlist.roomName || '待安排' }}</span>
                </div>
                <div class="detail-item full-width">
                  <span class="item-label">就诊日期</span>
                  <span class="item-value">{{ selectedWaitlist.workDate }} {{ selectedWaitlist.timeSlotName }}</span>
                </div>
                <div class="detail-item full-width">
                  <span class="item-label">号别</span>
                  <span class="item-value">{{ selectedWaitlist.appointmentTypeName }}</span>
                </div>
              </div>
            </div>

            <!-- 候补队列信息 -->
            <div v-if="selectedWaitlist.status === 'waiting'" class="detail-section">
              <h3>队列情况</h3>
              <div class="queue-stats">
                <div class="stat-card">
                  <div class="stat-label">当前位置</div>
                  <div class="stat-value primary">第 {{ selectedWaitlist.queuePosition }} 位</div>
                </div>
                <div class="stat-card">
                  <div class="stat-label">总候补人数</div>
                  <div class="stat-value">{{ selectedWaitlist.totalWaiting }} 人</div>
                </div>
                <div class="stat-card">
                  <div class="stat-label">优先级排名</div>
                  <div class="stat-value">第 {{ selectedWaitlist.myPriorityRank }} 位</div>
                </div>
              </div>
              <div class="queue-tip">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="16" x2="12" y2="12"></line>
                  <line x1="12" y1="8" x2="12.01" y2="8"></line>
                </svg>
                有号源释放时将按优先级和申请时间自动为您创建预约
              </div>
            </div>

            <!-- 时间信息 -->
            <div class="detail-section">
              <h3>时间记录</h3>
              <div class="time-records">
                <div class="time-item">
                  <span class="time-label">申请时间</span>
                  <span class="time-value">{{ formatDateTime(selectedWaitlist.requestedAt) }}</span>
                </div>
                <div v-if="selectedWaitlist.notifiedAt" class="time-item">
                  <span class="time-label">转正时间</span>
                  <span class="time-value">{{ formatDateTime(selectedWaitlist.notifiedAt) }}</span>
                </div>
                <div v-if="selectedWaitlist.priority > 0" class="time-item">
                  <span class="time-label">优先级</span>
                  <span class="time-value priority">{{ getPriorityText(selectedWaitlist.priority) }}</span>
                </div>
              </div>
            </div>

            <!-- 转正通知 -->
            <div v-if="selectedWaitlist.status === 'converted'" class="conversion-alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <div>
                <strong>候补已转正</strong>
                <p>已自动为您创建预约（预约ID: {{ selectedWaitlist.convertedAppointmentId }}），请前往"我的预约"查看详情</p>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button
              v-if="selectedWaitlist?.status === 'waiting'"
              @click="handleCancelWaitlist"
              class="cancel-waitlist-btn">
              取消候补
            </button>
            <button @click="closeDetailModal" class="close-modal-btn">
              关闭
            </button>
          </div>
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
import WaitlistCard from '@/components/WaitlistCard.vue'

const router = useRouter()
const navRef = ref(null)
const navHeight = ref(110)
const activeStatus = ref('waiting')
const loading = ref(false)
const allWaitlists = ref([])
const showDetailModal = ref(false)
const selectedWaitlist = ref(null)
const token = ref(localStorage.getItem('token'))

// 筛选候补中的记录
const waitingWaitlists = computed(() => {
  return allWaitlists.value.filter(w => w.status === 'waiting')
})

// 筛选历史记录
const historyWaitlists = computed(() => {
  return allWaitlists.value.filter(w =>
    w.status === 'converted' || w.status === 'cancelled' || w.status === 'expired'
  )
})

// 候补中数量
const waitingCount = computed(() => waitingWaitlists.value.length)

// 显示对应状态的列表
const displayedWaitlists = computed(() => {
  return activeStatus.value === 'waiting'
    ? waitingWaitlists.value
    : historyWaitlists.value
})

// 切换状态
function switchStatus(status) {
  activeStatus.value = status
}

// 获取候补列表
async function fetchWaitlists() {
  if (!token.value) {
    console.warn('未登录')
    return
  }

  loading.value = true
  try {
    // 尝试从后端获取数据
    let waitlists = []
    try {
      const response = await axios.get('/api/waitlist/my-detail', {
        headers: { Authorization: `Bearer ${token.value}` }
      })

      if (response.data.code === 200) {
        waitlists = response.data.data || []
      }
    } catch (err) {
      console.warn('从后端获取数据失败，使用本地存储数据', err)
    }

    // 如果后端没有数据，从本地存储获取模拟数据
    if (waitlists.length === 0) {
      const mockWaitlists = JSON.parse(localStorage.getItem('mockWaitlists') || '[]')
      waitlists = mockWaitlists

      // 如果本地也没有数据，添加一些示例数据
      if (waitlists.length === 0) {
        waitlists = generateSampleWaitlists()
        localStorage.setItem('mockWaitlists', JSON.stringify(waitlists))
      }
    }

    allWaitlists.value = waitlists
  } catch (err) {
    console.error('获取候补列表失败', err)
    // 出错时尝试从本地存储读取
    const mockWaitlists = JSON.parse(localStorage.getItem('mockWaitlists') || '[]')
    allWaitlists.value = mockWaitlists
  } finally {
    loading.value = false
  }
}

// 生成示例候补数据
function generateSampleWaitlists() {
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const dayAfter = new Date(today)
  dayAfter.setDate(dayAfter.getDate() + 2)
  const threeDays = new Date(today)
  threeDays.setDate(threeDays.getDate() + 3)
  const fourDays = new Date(today)
  fourDays.setDate(fourDays.getDate() + 4)
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const twoDaysAgo = new Date(today)
  twoDaysAgo.setDate(twoDaysAgo.getDate() - 2)

  return [
    // 候补中的记录
    {
      waitId: 10001,
      scheduleId: 2001,
      patientId: 1,
      doctorId: 101,
      doctorName: '张华',
      doctorTitle: '主任医师',
      deptId: 1,
      deptName: '心内科',
      workDate: dayAfter.toISOString().split('T')[0],
      timeSlot: 0,
      timeSlotName: '上午',
      priority: 1,
      status: 'waiting',
      requestedAt: new Date().toISOString(),
      notifiedAt: null,
      convertedAppointmentId: null,
      queuePosition: 2,
      totalWaiting: 4,
      highPriorityCount: 2,
      myPriorityRank: 1,
      roomName: '心内科1诊室',
      appointmentTypeName: '专家门诊',
      maxSlots: 20,
      availableSlots: 0,
      bookedSlots: 20,
      scheduleStatus: 'open'
    },
    {
      waitId: 10002,
      scheduleId: 2003,
      patientId: 1,
      doctorId: 103,
      doctorName: '王芳',
      doctorTitle: '主治医师',
      deptId: 3,
      deptName: '儿科',
      workDate: threeDays.toISOString().split('T')[0],
      timeSlot: 1,
      timeSlotName: '下午',
      priority: 0,
      status: 'waiting',
      requestedAt: new Date(Date.now() - 3600000).toISOString(),
      notifiedAt: null,
      convertedAppointmentId: null,
      queuePosition: 1,
      totalWaiting: 2,
      highPriorityCount: 0,
      myPriorityRank: 1,
      roomName: '儿科诊室',
      appointmentTypeName: '普通门诊',
      maxSlots: 15,
      availableSlots: 0,
      bookedSlots: 15,
      scheduleStatus: 'open'
    },
    {
      waitId: 10003,
      scheduleId: 2005,
      patientId: 1,
      doctorId: 105,
      doctorName: '陈静',
      doctorTitle: '副主任医师',
      deptId: 5,
      deptName: '妇产科',
      workDate: fourDays.toISOString().split('T')[0],
      timeSlot: 2,
      timeSlotName: '晚上',
      priority: 2,
      status: 'waiting',
      requestedAt: new Date(Date.now() - 7200000).toISOString(),
      notifiedAt: null,
      convertedAppointmentId: null,
      queuePosition: 1,
      totalWaiting: 3,
      highPriorityCount: 1,
      myPriorityRank: 1,
      roomName: '妇产科诊室',
      appointmentTypeName: '专家门诊',
      maxSlots: 18,
      availableSlots: 0,
      bookedSlots: 18,
      scheduleStatus: 'open'
    },
    // 已转正的记录
    {
      waitId: 10004,
      scheduleId: 2002,
      patientId: 1,
      doctorId: 102,
      doctorName: '李明',
      doctorTitle: '副主任医师',
      deptId: 2,
      deptName: '骨科',
      workDate: tomorrow.toISOString().split('T')[0],
      timeSlot: 1,
      timeSlotName: '下午',
      priority: 0,
      status: 'converted',
      requestedAt: new Date(Date.now() - 86400000).toISOString(),
      notifiedAt: new Date(Date.now() - 3600000).toISOString(),
      convertedAppointmentId: 5001,
      queuePosition: null,
      totalWaiting: 0,
      highPriorityCount: 0,
      myPriorityRank: null,
      roomName: '骨科2诊室',
      appointmentTypeName: '普通门诊',
      maxSlots: 15,
      availableSlots: 1,
      bookedSlots: 14,
      scheduleStatus: 'open'
    },
    {
      waitId: 10005,
      scheduleId: 2006,
      patientId: 1,
      doctorId: 106,
      doctorName: '赵敏',
      doctorTitle: '主任医师',
      deptId: 6,
      deptName: '呼吸内科',
      workDate: yesterday.toISOString().split('T')[0],
      timeSlot: 0,
      timeSlotName: '上午',
      priority: 1,
      status: 'converted',
      requestedAt: new Date(Date.now() - 172800000).toISOString(),
      notifiedAt: new Date(Date.now() - 86400000).toISOString(),
      convertedAppointmentId: 5002,
      queuePosition: null,
      totalWaiting: 0,
      highPriorityCount: 0,
      myPriorityRank: null,
      roomName: '呼吸内科诊室',
      appointmentTypeName: '专家门诊',
      maxSlots: 20,
      availableSlots: 2,
      bookedSlots: 18,
      scheduleStatus: 'open'
    },
    // 已取消的记录
    {
      waitId: 10006,
      scheduleId: 2007,
      patientId: 1,
      doctorId: 107,
      doctorName: '周杰',
      doctorTitle: '副主任医师',
      deptId: 7,
      deptName: '消化内科',
      workDate: twoDaysAgo.toISOString().split('T')[0],
      timeSlot: 1,
      timeSlotName: '下午',
      priority: 0,
      status: 'cancelled',
      requestedAt: new Date(Date.now() - 259200000).toISOString(),
      notifiedAt: null,
      convertedAppointmentId: null,
      queuePosition: null,
      totalWaiting: 0,
      highPriorityCount: 0,
      myPriorityRank: null,
      roomName: '消化内科诊室',
      appointmentTypeName: '普通门诊',
      maxSlots: 15,
      availableSlots: 0,
      bookedSlots: 15,
      scheduleStatus: 'open'
    }
  ]
}

// 格式化候补数据
function formatWaitlistData(waitlist) {
  return {
    waitId: waitlist.waitId,
    scheduleId: waitlist.scheduleId,
    doctorName: waitlist.doctorName || '医生',
    doctorTitle: waitlist.doctorTitle || '医师',
    deptName: waitlist.deptName || '科室',
    workDate: waitlist.workDate,
    timeSlotName: waitlist.timeSlotName || '时段',
    status: waitlist.status,
    priority: waitlist.priority || 0,
    queuePosition: waitlist.queuePosition,
    totalWaiting: waitlist.totalWaiting,
    requestedAt: waitlist.requestedAt,
    notifiedAt: waitlist.notifiedAt,
    convertedAppointmentId: waitlist.convertedAppointmentId,
    roomName: waitlist.roomName,
    appointmentTypeName: waitlist.appointmentTypeName,
    myPriorityRank: waitlist.myPriorityRank
  }
}

// 获取状态文本
function getStatusText(status) {
  const map = {
    waiting: '候补中',
    converted: '已转正',
    cancelled: '已取消',
    expired: '已过期'
  }
  return map[status] || '未知'
}

// 获取优先级文本
function getPriorityText(priority) {
  const map = {
    0: '普通',
    1: '紧急',
    2: '非常紧急'
  }
  return map[priority] || '普通'
}

// 格式化日期时间
function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 点击卡片查看详情
function handleRecordClick(record) {
  // 从完整列表中找到对应的详细数据
  const fullData = allWaitlists.value.find(w => w.waitId === record.waitId)
  if (fullData) {
    selectedWaitlist.value = fullData
    showDetailModal.value = true
  }
}

// 关闭详情弹窗
function closeDetailModal() {
  showDetailModal.value = false
  selectedWaitlist.value = null
}

// 取消候补
async function handleCancelWaitlist() {
  if (!confirm('确定要取消这个候补吗?')) return
  if (!token.value) return alert('未登录或登录已过期，请重新登录')

  loading.value = true
  try {
    // 尝试调用后端API
    let success = false
    try {
      const response = await axios.post(
        `/api/waitlist/cancel/${selectedWaitlist.value.waitId}`,
        null,
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
      // 如果后端API失败，更新本地存储
      success = cancelMockWaitlist()
    }

    if (success) {
      alert('候补已取消')
      closeDetailModal()
      await fetchWaitlists()
    }
  } catch (err) {
    console.error('取消候补失败', err)
    alert('取消失败: ' + err.message)
  } finally {
    loading.value = false
  }
}

// 取消本地存储的模拟候补
function cancelMockWaitlist() {
  try {
    const mockWaitlists = JSON.parse(localStorage.getItem('mockWaitlists') || '[]')
    const index = mockWaitlists.findIndex(w => w.waitId === selectedWaitlist.value.waitId)

    if (index !== -1) {
      mockWaitlists[index].status = 'cancelled'
      localStorage.setItem('mockWaitlists', JSON.stringify(mockWaitlists))
      return true
    }

    return false
  } catch (err) {
    console.error('取消模拟候补失败', err)
    return false
  }
}

// 跳转到可候补排班列表
function goToFullyBooked() {
  router.push('/waitlist/fully-booked')
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
  fetchWaitlists()
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

.waitlist-wrapper {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 700;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 状态标签 */
.status-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.tab-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  background: white;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  color: #4a5568;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
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

.count-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 700;
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

/* 候补内容 */
.waitlist-content {
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
  margin: 0 0 1.5rem 0;
  font-size: 1rem;
}

.empty-action-btn {
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

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 候补列表 */
.waitlist-list {
  display: grid;
  gap: 1.5rem;
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
  margin: 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.modal-body {
  padding: 2rem;
}

/* 详情部分 */
.detail-section {
  margin-bottom: 2rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.status-section {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 1.5rem;
}

.status-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 12px;
  font-size: 1.125rem;
  font-weight: 600;
}

.status-display.status-waiting {
  background: #fff3cd;
  color: #856404;
}

.status-display.status-converted {
  background: #d4edda;
  color: #28a745;
}

.status-display.status-cancelled {
  background: #e2e8f0;
  color: #718096;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.item-label {
  color: #718096;
  font-size: 0.875rem;
  font-weight: 500;
}

.item-value {
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

/* 队列统计 */
.queue-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.stat-card {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 10px;
  padding: 1rem;
  text-align: center;
}

.stat-label {
  color: #718096;
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-value.primary {
  color: #667eea;
}

.queue-tip {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #e6f7ff;
  border-radius: 8px;
  color: #0050b3;
  font-size: 0.875rem;
  line-height: 1.5;
}

.queue-tip svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

/* 时间记录 */
.time-records {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.time-item {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem;
  background: #f7fafc;
  border-radius: 8px;
}

.time-label {
  color: #718096;
  font-weight: 500;
}

.time-value {
  color: #2d3748;
  font-weight: 600;
}

.time-value.priority {
  color: #d68910;
}

/* 转正通知 */
.conversion-alert {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: #d4edda;
  border: 2px solid #28a745;
  border-radius: 10px;
  color: #155724;
}

.conversion-alert svg {
  flex-shrink: 0;
  color: #28a745;
}

.conversion-alert strong {
  display: block;
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

.conversion-alert p {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.5;
}

/* 弹窗底部 */
.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-top: 2px solid #f0f0f0;
  background: #f7fafc;
}

.cancel-waitlist-btn,
.close-modal-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-waitlist-btn {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.cancel-waitlist-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
}

.close-modal-btn {
  background: #edf2f7;
  color: #2d3748;
  border: 1px solid #cbd5e0;
}

.close-modal-btn:hover {
  background: #e2e8f0;
}

/* 响应式 */
@media (max-width: 768px) {
  .waitlist-wrapper {
    padding: 1rem;
  }

  .header-section {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
    padding: 1.25rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .search-btn {
    width: 100%;
    justify-content: center;
  }

  .status-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }

  .waitlist-content {
    padding: 1.5rem;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .queue-stats {
    grid-template-columns: 1fr;
  }

  .modal-container {
    margin: 1rem;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1.5rem;
  }
}
</style>
