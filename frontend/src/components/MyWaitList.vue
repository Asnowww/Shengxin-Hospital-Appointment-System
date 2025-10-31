<template>
  <div class="waitlist-container">
    <div class="waitlist-header">
      <h2>我的候补</h2>
      <div class="status-tabs">
        <button 
          :class="['tab-btn', { active: activeStatus === 'waiting' }]"
          @click="switchStatus('waiting')">
          等待中
        </button>
        <button 
          :class="['tab-btn', { active: activeStatus === 'completed' }]"
          @click="switchStatus('completed')">
          已处理
        </button>
      </div>
    </div>

    <div class="waitlist-content">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="displayedWaitlist.length === 0" class="empty-state">
        <p>暂无{{ activeStatus === 'waiting' ? '等待中' : '已处理' }}的候补</p>
      </div>

      <transition-group v-else name="list" tag="div" class="waitlist-list">
        <WaitlistRecordCard
          v-for="record in displayedWaitlist"
          :key="record.waitlistId"
          :record="formatRecordData(record)"
          :onRecordClick="handleRecordClick"
        />
      </transition-group>
    </div>

    <!-- 候补详情抽屉 -->
    <div v-if="detailDrawerVisible" class="detail-drawer-overlay" @click="closeDetailDrawer">
      <div class="detail-drawer" @click.stop>
        <div class="drawer-header">
          <h3>候补详情</h3>
          <button class="close-btn" @click="closeDetailDrawer">×</button>
        </div>

        <div class="drawer-content">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-row">
              <span class="label">医生：</span>
              <span class="value">{{ selectedRecord.doctorName }} - {{ selectedRecord.doctorTitle }}</span>
            </div>
            <div class="detail-row">
              <span class="label">患者：</span>
              <span class="value">{{ selectedRecord.patientName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">科室：</span>
              <span class="value">{{ selectedRecord.deptName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">地点：</span>
              <span class="value">{{ selectedRecord.building }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4>候补信息</h4>
            <div class="detail-row">
              <span class="label">预约类型：</span>
              <span class="value">{{ selectedRecord.typeName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">目标时间：</span>
              <span class="value">{{ selectedRecord.targetTime }}</span>
            </div>
            <div class="detail-row">
              <span class="label">目标日期：</span>
              <span class="value">{{ formatDate(selectedRecord.targetDate) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">候补状态：</span>
              <span :class="['value', 'status-' + selectedRecord.status]">
                {{ getStatusLabel(selectedRecord.status) }}
              </span>
            </div>
          </div>

          <div class="detail-section">
            <h4>时间信息</h4>
            <div class="detail-row">
              <span class="label">加入候补：</span>
              <span class="value">{{ formatDate(selectedRecord.createdAt) }}</span>
            </div>
            <div v-if="selectedRecord.processedAt" class="detail-row">
              <span class="label">处理时间：</span>
              <span class="value">{{ formatDate(selectedRecord.processedAt) }}</span>
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <button 
            v-if="activeStatus === 'waiting' && selectedRecord.status === 'waiting'"
            @click="handleCancelWaitlist(selectedRecord)"
            class="cancel-btn">
            取消候补
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import WaitlistRecordCard from './WaitListRecordCard.vue'

const activeStatus = ref('waiting')
const loading = ref(false)
const waitingWaitlist = ref([])
const completedWaitlist = ref([])
const detailDrawerVisible = ref(false)
const selectedRecord = ref({})
const token = ref(localStorage.getItem('token'))

// 显示对应状态列表
const displayedWaitlist = computed(() => {
  return activeStatus.value === 'waiting'
    ? waitingWaitlist.value
    : completedWaitlist.value
})

// 切换状态
function switchStatus(status) {
  activeStatus.value = status
  loadWaitlist()
}

// 获取候补列表
async function fetchWaitlist() {
  if (!token.value) return

  loading.value = true
  try {
    const response = await axios.get('/api/waitlist/my-detail', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const resData = response.data
    if (resData.code === 200) {
      const waitlists = resData.data || []
      // 区分等待中和已处理候补
      waitingWaitlist.value = waitlists.filter(item => 
        item.status === 'waiting'
      )
      completedWaitlist.value = waitlists.filter(item => 
        item.status === 'converted' || item.status === 'cancelled' || item.status === 'expired'
      )
    } else {
      console.warn('获取候补列表失败：', resData.message)
      waitingWaitlist.value = []
      completedWaitlist.value = []
    }
  } catch (err) {
    console.error('获取候补列表失败', err)
    waitingWaitlist.value = []
    completedWaitlist.value = []
  } finally {
    loading.value = false
  }
}

// 加载当前或历史候补
async function loadWaitlist() {
  if (activeStatus.value === 'waiting' && waitingWaitlist.value.length === 0) {
    await fetchWaitlist()
  } else if (activeStatus.value === 'completed' && completedWaitlist.value.length === 0) {
    if (completedWaitlist.value.length === 0 && waitingWaitlist.value.length > 0) {
      // 数据已加载，只需切换显示
    } else {
      await fetchWaitlist()
    }
  }
}

// 格式化候补数据为卡片格式
function formatRecordData(item) {
  return {
    waitlistId: item.waitId,               
    patientName: item.patientName || '本人',
    doctorName: item.doctorName,
    doctorTitle: item.doctorTitle,
    deptName: item.deptName,
    building: item.roomName,               
    typeName: item.appointmentTypeName,    
    targetTime: item.timeSlotName,         // 下午 / 上午
    targetDate: item.workDate,
    status: mapStatus(item.status),
    createdAt: item.requestedAt,           // 加入时间
    processedAt: item.notifiedAt,          // 通知或处理时间
    remarks: item.remarks || ''
  }
}


// 映射候补状态
function mapStatus(apiStatus) {
  const statusMap = {
    'waiting': 'waiting',
    'notified': 'notified',
    'converted': 'converted',
    'cancelled': 'cancelled',
    'expired': 'expired' //后端待加上
  }
  return statusMap[apiStatus] || 'waiting'
}

// 获取状态标签
function getStatusLabel(status) {
  const statusMap = {
    'waiting': '等待中',
    'notified': '可挂号',
    'converted': '已转预约',
    'cancelled': '已取消',
    'expired': '已过期'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '未指定日期'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

// 打开详情抽屉
function handleRecordClick(record) {
  selectedRecord.value = record
  detailDrawerVisible.value = true
}

// 关闭详情抽屉
function closeDetailDrawer() {
  detailDrawerVisible.value = false
  selectedRecord.value = {}
}

// 取消候补
async function handleCancelWaitlist(record) {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('未登录或登录已过期')
      return
    }

    const url = `/api/waitlist/cancel/${record.waitlistId || record.waitId}`

    const { data } = await axios.post(url, null, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (data.code === 200) {
      alert('候补已取消')
      closeDetailDrawer()
      //本地刷新
      waitingWaitlist.value = waitingWaitlist.value.filter(
    item => item.waitId !== record.waitlistId
  )
  completedWaitlist.value.push({
    ...record,
    status: 'cancelled'
  })
    } else {
      alert('取消失败')
    }
  } catch (error) {
    console.error('取消候补失败:', error)
    alert('取消候补失败，请稍后重试')
  }
}


onMounted(() => {
  fetchWaitlist()
})
</script>

<style scoped>
.waitlist-container {
  padding: 2.5rem;
  height: 100%;
  overflow-y: auto;
}

.waitlist-header {
  margin-bottom: 2rem;
}

h2 {
  margin: 0 0 1.5rem 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.status-tabs {
  display: flex;
  gap: 0.75rem;
  padding: 0.5rem;
  background: #f7fafc;
  border-radius: 12px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #4a5568;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.waitlist-content {
  min-height: 400px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #718096;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #a0aec0;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

.waitlist-list {
  display: grid;
  gap: 1.5rem;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 详情抽屉 */
.detail-drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.detail-drawer {
  background: white;
  border-radius: 16px 16px 0 0;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease;
}

/* 桌面端中居中显示 */
@media (min-width: 992px) {
  .detail-drawer-overlay {
    align-items: center;
  }
  .detail-drawer {
    border-radius: 16px;
    animation: scaleIn 0.25s ease;
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

@keyframes scaleIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
}

.drawer-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.75rem;
  color: #718096;
  cursor: pointer;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: #2d3748;
}

.drawer-content {
  padding: 2rem;
}

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h4 {
  margin: 0 0 1rem 0;
  color: #2d3748;
  font-size: 0.95rem;
  font-weight: 600;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  color: #4a5568;
  font-size: 0.95rem;
}

.label {
  color: #a0aec0;
  font-weight: 500;
  flex-shrink: 0;
}

.value {
  color: #2d3748;
  font-weight: 500;
  text-align: right;
  flex: 1;
  margin-left: 1rem;
}

.value.status-waiting {
  color: #ff9c6e;
  font-weight: 600;
}

.value.status-converted {
  color: #52c41a;
  font-weight: 600;
}

.value.status-cancelled {
  color: #8c8c8c;
}

.value.status-expired {
  color: #d9534f;
}

.remarks-text {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.6;
  padding: 0.75rem;
  background: #f7fafc;
  border-radius: 8px;
}

.drawer-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f7fafc;
}

.cancel-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
}

@media (max-width: 768px) {
  .waitlist-container {
    padding: 1.5rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  .status-tabs {
    flex-direction: column;
  }

  .detail-drawer {
    max-width: 100%;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .value {
    text-align: left;
    margin-left: 0;
    margin-top: 0.5rem;
  }
}
</style>