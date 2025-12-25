<template>
  <div class="appointments-container">
    <div class="appointments-header">
      <h2>我的预约</h2>
      <div class="status-tabs">
        <button 
          :class="['tab-btn', { active: activeStatus === 'current' }]"
          @click="switchStatus('current')">
          当前预约
        </button>
        <button 
          :class="['tab-btn', { active: activeStatus === 'history' }]"
          @click="switchStatus('history')">
          历史预约
        </button>
      </div>
    </div>

    <div class="appointments-content">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="displayedAppointments.length === 0" class="empty-state">
        <p>暂无{{ activeStatus === 'current' ? '当前' : '历史' }}预约</p>
      </div>

      <transition-group v-else name="list" tag="div" class="appointments-list">
        <AppointmentRecordCard
          v-for="appt in displayedAppointments"
          :key="appt.appointmentId"
          :record="formatRecordData(appt)"
          :onRecordClick="handleRecordClick"
          :onNavigate="openNavigator"
        />
      </transition-group>
    </div>

    <div v-if="detailDrawerVisible" class="detail-drawer-overlay" @click="closeDetailDrawer">
      <div class="detail-drawer" @click.stop>
        <div class="drawer-header">
          <h3>预约详情</h3>
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
              <span class="value location-value">
                <span class="location-text">{{ selectedRecord.building }}</span>
                <button class="nav-link" @click="openNavigator(selectedRecord)">
                  导航
                </button>
              </span>
            </div>
          </div>

          <div class="detail-section">
            <h4>预约信息</h4>
            <div class="detail-row">
              <span class="label">预约类型：</span>
              <span class="value">{{ selectedRecord.typeName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">预约时间：</span>
              <span class="value">{{ selectedRecord.appointmentTime }}</span>
            </div>
            <div class="detail-row">
              <span class="label">预约日期：</span>
              <span class="value">{{ formatDate(selectedRecord.bookingTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">预约状态：</span>
              <span :class="['value', 'status-' + selectedRecord.status]">
                {{ getStatusLabel(selectedRecord.status) }}
              </span>
            </div>
            <div v-if="selectedRecord.status === 'waiting_action'">
  <div class="detail-row source-info-row">
    <p class="cancel-message">很抱歉，您预约的排班已被系统取消，请重新挂号</p>
  </div>
</div>

            <div v-if="selectedRecord.status === 'pending_confirm'">
                <div class="detail-row source-info-row">
                    <span class="label status-pending-confirm">原预约状态：</span>
                    <span class="value status-pending-confirm">
                        {{ getStatusLabel(selectedRecord.sourceStatus) }}
                    </span>
                </div>
                  <div class="detail-row source-info-row">
                    <span class="label status-pending-confirm">原预约医生：</span>
                    <span class="value status-pending-confirm">{{ selectedRecord.sourceDoctorName }}</span>
                </div>
                  <div class="detail-row source-info-row">
                    <span class="label status-pending-confirm">原预约医生职称：</span>
                    <span class="value status-pending-confirm">{{ selectedRecord.sourceDoctorTitle }}</span>
                </div>
                <div class="detail-row source-info-row">
                    <span class="label status-pending-confirm">原就诊时间：</span>
                    <span class="value status-pending-confirm">{{ selectedRecord.sourceAppointmentTime }}</span>
                </div>
                
                <div class="detail-row">
                    <span class="label">改约说明：</span>
                    <span class="value highlight-text">请确认新的预约信息是否符合您的需求。</span>
                </div>
            </div>

          </div>

          <div class="detail-section">
            <h4>费用信息</h4>
            <div class="detail-row">
              <span class="label">原始费用：</span>
              <span class="value fee">¥{{ selectedRecord.feeOriginal ?? 0 }}</span>
            </div>
            <div class="detail-row">
              <span class="label">待支付费用（报销后）：</span>
              <span class="value fee">¥{{ selectedRecord.feeFinal ?? 0 }}</span>
            </div>
          </div>


          <div v-if="selectedRecord.remarks" class="detail-section">
            <h4>备注</h4>
            <p class="remarks-text">{{ selectedRecord.remarks }}</p>
          </div>
        </div>

        <div class="drawer-footer">
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status === 'pending_confirm'"
            @click="handleConfirmReassign(selectedRecord)"
            class="action-btn third">
            接受改期
          </button>
          
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status === 'pending_confirm'"
            @click="handleRefuseReassign(selectedRecord)"
            class="action-btn secondary">
            拒绝改期
          </button>
          
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status === 'waiting_action'"
            @click="handleAcknowledgeCancel(selectedRecord)"
            class="action-btn">
            我知道了
          </button>
          
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status !== 'cancelled' && selectedRecord.status !== 'waiting_action' && selectedRecord.status !== 'pending_confirm'"
            @click="handleCancelAppointment(selectedRecord)"
            :disabled="!canCancelAppointment(selectedRecord)"
            :class="['action-btn', { 'disabled': !canCancelAppointment(selectedRecord) }]"
            :title="!canCancelAppointment(selectedRecord) ? '已过可取消预约时间' : ''">
            取消预约
          </button>
          
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status !== 'cancelled' && selectedRecord.status !== 'waiting_action' && selectedRecord.status !== 'pending_confirm'"
            @click="handleChangeAppointment(selectedRecord)"
            class="action-btn secondary">
            改约
          </button>
          
          <button 
            v-if="activeStatus === 'current' && selectedRecord.status === 'pending'"
            @click="handlePay(selectedRecord)"
            class="action-btn third">
            去支付
          </button>
        </div>
      </div>
    </div>

    <Payment
      :visible="payDialogVisible"
      :appointment-id="payInfo.appointmentId"
      @close="closePayDialog"
      @payment-success="handlePaymentSuccess"
      @payment-error="handlePaymentError"
    />
    <HospitalNavigator
      v-if="navigatorVisible"
      :visible="navigatorVisible"
      :default-destination="navigatorDestination"
      :appointment-info="navigatorInfo"
      @close="navigatorVisible = false"
    />
    <!-- 改约弹窗 -->
    <RescheduleModal
      :visible="rescheduleModalVisible"
      :appointment-id="rescheduleAppointmentId"
      :appointment-info="rescheduleInfo"
      @close="rescheduleModalVisible = false"
      @success="handleRescheduleSuccess"
    />
  </div>
  

</template>

<script setup>
import { ref, computed, onMounted,nextTick} from 'vue'
import axios from 'axios'
import AppointmentRecordCard from './AppointmentRecordCard.vue'
import Payment from './Payment.vue'
import HospitalNavigator from './HospitalNavigator.vue'
import RescheduleModal from './RescheduleModal.vue'

const payDialogVisible = ref(false)
const payInfo = ref({ appointmentId: null })
const navigatorVisible = ref(false)
const navigatorDestination = ref('')
const navigatorInfo = ref({})

// 改约弹窗状态
const rescheduleModalVisible = ref(false)
const rescheduleAppointmentId = ref(null)
const rescheduleInfo = ref({})

async function handlePay(record) {
  if (!record || !record.appointmentId) {
    console.error('handlePay: appointmentId 不存在', record)
    return
  }
  payInfo.value = {
    appointmentId: record.appointmentId,
  }
  await nextTick()
  payDialogVisible.value = true
}

function closePayDialog() {
  payDialogVisible.value = false
  payInfo.value = { appointmentId: null}
}

function handlePaymentSuccess(data) {
  alert('支付成功')
  closePayDialog()
  closeDetailDrawer() 
  fetchAppointments() 
}

function handlePaymentError(err) {
  console.warn('支付错误：', err)
}


const activeStatus = ref('current')
const loading = ref(false)
const currentAppointments = ref([])
const historyAppointments = ref([])
const detailDrawerVisible = ref(false)
const selectedRecord = ref({})
const token = ref(localStorage.getItem('token'))

const displayedAppointments = computed(() => {
  return activeStatus.value === 'current'
    ? currentAppointments.value
    : historyAppointments.value
})

function switchStatus(status) {
  activeStatus.value = status
  loadAppointments()
}

// 获取预约列表
async function fetchAppointments() {
  if (!token.value) return

  loading.value = true
  try {
    const response = await axios.get('/api/patient/appointment/list', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const resData = response.data
    if (resData.code === 200) {
      const appointments = resData.data || []
      // 区分当前和历史预约
      currentAppointments.value = appointments.filter(appt =>
        appt.status === 'booked'
        || appt.status === 'pending'
        || appt.status === 'pending_patient_confirm'
        || appt.status === 'waiting_patient_action'
      )
      historyAppointments.value = appointments.filter(appt =>
        appt.status === 'completed'
        || appt.status === 'cancelled'
        || appt.status === 'no_show'
        || appt.status === 'refunded'
        || appt.status === 'converted'

      )
    } else {
      console.warn('获取预约失败：', resData.message)
      currentAppointments.value = []
      historyAppointments.value = []
    }
  } catch (err) {
    console.error('获取预约失败', err)
    currentAppointments.value = []
    historyAppointments.value = []
  } finally {
    loading.value = false
  }
}

// 加载当前或历史预约
async function loadAppointments() {
  if (activeStatus.value === 'current' && currentAppointments.value.length === 0) {
    await fetchAppointments()
  } else if (activeStatus.value === 'history' && historyAppointments.value.length === 0) {
    if (historyAppointments.value.length === 0 && currentAppointments.value.length > 0) {
      // 数据已加载，只需切换显示
    } else {
      await fetchAppointments()
    }
  }
}

// 格式化预约数据为卡片格式
function formatRecordData(appt) {
  const locationParts = [
    appt.building,
    appt.roomName || appt.room_name
  ].filter(Boolean)
  const location = locationParts.join(' ')
  const roomId =
    appt.roomId ||
    appt.room_id ||
    (appt.roomName || appt.room_name || '').match(/\d{3}/)?.[0] ||
    ''

  return {
    appointmentId: appt.appointmentId,
    patientName: appt.patientName || '患者',
    doctorName: appt.doctorName || '医生',
    doctorTitle: appt.doctorTitle || '医师',
    deptName: appt.deptName || '科室',
    building: location || appt.building || '医院',
    roomName: appt.roomName || appt.room_name || '',
    roomId,
    typeName: appt.typeName || '普通',
    appointmentTime: appt.appointmentTime || '未指定时间',
    bookingTime: appt.bookingTime,
    status: mapStatus(appt.status),
    feeOriginal: appt.feeOriginal || '未知',
    feeFinal: appt.feeFinal || '未知',
    remarks: appt.remarks,
    // 根据新的DTO字段进行映射
    sourceAppointmentId: appt.sourceAppointmentId || null,
    sourceAppointmentTime: appt.sourceAppointmentTime || '未知',
    sourceStatus: mapStatus(appt.sourceStatus) || '未知',
    sourceDoctorName: appt.sourceDoctorName || '未知',
    sourceDoctorTitle: appt.sourceDoctorTitle || '未知',
  }
}

// 映射预约状态
function mapStatus(apiStatus) {
  const statusMap = {
    'pending': 'pending',
    'booked': 'booked',
    'completed': 'completed',
    'cancelled': 'cancelled',
    'refunded': 'cancelled',
    'no_show': 'no-show',
    'converted':'converted',
    'pending_patient_confirm': 'pending_confirm',
    'waiting_patient_action': 'waiting_action',
    // 映射原预约状态
    'source_booked': 'booked',
    'source_pending': 'pending',
    'source_cancelled': 'cancelled',
    // DTO 中的 sourceStatus 字段可能会直接返回原始状态
  }
  // 考虑到 sourceStatus 也可能返回原始的 status 字符串，直接返回
  return statusMap[apiStatus] || apiStatus || 'pending' 
}

// 获取状态标签
function getStatusLabel(status) {
  const statusMap = {
    'pending': '待支付',
    'booked': '待就诊',
    'completed': '已完成',
    'cancelled': '已取消',
    'no-show': '未到诊',
    'pending_confirm': '待处理',
    'waiting_action': '待确认',
    // 增加对原预约状态的标签
    'source_booked': '原待就诊',
    'source_cancelled': '原已取消',
    'converted':'已改约'
  }
  return statusMap[status] || status || '未知'
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

// 接受改期
async function handleConfirmReassign(record) {
  if (!token.value) return alert('未登录或登录已过期，请重新登录')
  loading.value = true
  try {
    const { data } = await axios.put('/api/patient/appointment/confirm-reassign', null, {
      headers: { Authorization: `Bearer ${token.value}` },
      params: { appointmentId: record.appointmentId }
    })
    if (data.code === 200) {
      alert('已确认新的预约，请及时支付')
      closeDetailDrawer()
      await fetchAppointments()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (err) {
    console.error('确认失败', err)
    alert(err?.response?.data?.message || '操作失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * ② 拒绝改期
 * @param {object} record - 待确认改期的预约记录
 */
async function handleRefuseReassign(record) {
    const isConfirmed = confirm('拒绝后只能自行挂号，确认拒绝本次改期吗？')
    if (!isConfirmed) return

    if (!token.value) return alert('未登录或登录已过期，请重新登录')
    loading.value = true
    
    try {
        // 调用拒绝改期接口
        const { data } = await axios.put('/api/patient/appointment/reject-reassign', null, {
            headers: { Authorization: `Bearer ${token.value}` },
            params: { appointmentId: record.appointmentId }
        })

        if (data.code === 200) {
            alert('已拒绝本次改期。原预约将被取消，请自行挂号。')
            closeDetailDrawer()
            await fetchAppointments()
        } else {
            alert(data.message || '操作失败')
        }
    } catch (err) {
        console.error('拒绝改期失败', err)
        alert(err?.response?.data?.message || '操作失败，请重试')
    } finally {
        loading.value = false
    }
}


async function handleAcknowledgeCancel(record) {
  if (!token.value) return alert('未登录或登录已过期，请重新登录')
  loading.value = true
  try {
    const { data } = await axios.put('/api/patient/appointment/acknowledge-cancel', null, {
      headers: { Authorization: `Bearer ${token.value}` },
      params: { appointmentId: record.appointmentId }
    })
    if (data.code === 200) {
      alert('已知晓该预约取消')
      closeDetailDrawer()
      await fetchAppointments()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (err) {
    console.error('确认失败', err)
    alert(err?.response?.data?.message || '操作失败，请重试')
  } finally {
    loading.value = false
  }
}
// 改约
function handleChangeAppointment(record) {
  rescheduleAppointmentId.value = record.appointmentId
  rescheduleInfo.value = {
    currentDeptId: record.deptId,
    currentDoctorName: record.doctorName,
    currentTime: record.appointmentTime
  }
  rescheduleModalVisible.value = true
}

// 改约成功回调
async function handleRescheduleSuccess() {
  rescheduleModalVisible.value = false
  closeDetailDrawer()
  await fetchAppointments()
  alert('改约成功！')
}

function openNavigator(record) {
  navigatorDestination.value = record.roomId || ''
  navigatorInfo.value = {
    deptName: record.deptName,
    roomName: record.roomName,
    building: record.building
  }
  navigatorVisible.value = true
}

// 解析中文日期格式 "2025年12月29日 上午" 或 "2025年12月29日 下午"
function parseChineseDateTime(timeStr) {
  if (!timeStr) return null
  
  try {
    // 匹配格式：YYYY年MM月DD日 上午/下午
    const match = timeStr.match(/(\d{4})年(\d{1,2})月(\d{1,2})日\s*(上午|下午)?/)
    if (!match) return null
    
    const year = parseInt(match[1])
    const month = parseInt(match[2]) - 1 // JavaScript月份从0开始
    const day = parseInt(match[3])
    const period = match[4] // 上午 或 下午
    
    // 创建日期对象
    const date = new Date(year, month, day)
    
    // 如果是上午，设置为9:00；如果是下午，设置为14:00
    if (period === '上午') {
      date.setHours(9, 0, 0, 0)
    } else if (period === '下午') {
      date.setHours(14, 0, 0, 0)
    } else {
      // 如果没有指定上午/下午，默认设置为上午9:00
      date.setHours(9, 0, 0, 0)
    }
    
    return date
  } catch (err) {
    console.error('解析中文日期失败:', timeStr, err)
    return null
  }
}

// 判断是否可以取消预约（预约前一日17:00之前）
function canCancelAppointment(record) {
  if (!record.appointmentTime) return false
  
  try {
    // 解析预约时间（中文格式）
    const appointmentDate = parseChineseDateTime(record.appointmentTime)
    if (!appointmentDate) return false
    
    // 获取预约前一日的17:00
    const deadlineDate = new Date(appointmentDate)
    deadlineDate.setDate(deadlineDate.getDate() - 1)
    deadlineDate.setHours(17, 0, 0, 0)
    
    // 获取当前时间
    const now = new Date()
    
    // 当前时间必须在截止时间之前
    return now < deadlineDate
  } catch (err) {
    console.error('计算取消预约时间失败', err)
    return false
  }
}

// 取消预约
async function handleCancelAppointment(record) {
  if (!canCancelAppointment(record)) {
    alert('已超过取消预约的时间限制（预约前一日17:00前）')
    return
  }
  
  if (!confirm('确定要取消这个预约吗？')) return
  if (!token.value) return alert('未登录或登录已过期，请重新登录')

  loading.value = true
  try {
    const { data } = await axios.put('/api/patient/appointment/cancel', null, {
      headers: { Authorization: `Bearer ${token.value}` },
      params: { appointmentId: record.appointmentId }
    })

    if (data.code === 200) {
      alert('预约已取消')
      closeDetailDrawer()
      await fetchAppointments()
    } else {
      alert('取消失败：' + data.message)
    }
  } catch (err) {
    console.error('取消预约失败', err)
    alert('取消失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.cancel-message {
  color: #ff4d4f;
  font-weight: 600;
  font-size: 0.9rem;
  margin: 0.5rem 0;
}

.appointments-container {
  padding: 2.5rem;
  height: 100%;
  overflow-y: auto;
}

.appointments-header {
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

.appointments-content {
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

.appointments-list {
  display: grid;
  gap: 1.5rem;
}

.list-enter-active,
.list-leave-active,
.list-enter-from,
.list-enter-to,
.list-leave-from,
.list-leave-to {
  transition: none;
  opacity: 1;
  transform: none;
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

.value.fee {
  color: #667eea;
  font-weight: 600;
}

.location-value {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.location-text {
  flex: 1;
}

.nav-link {
  padding: 0.35rem 0.75rem;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #2563eb;
  font-weight: 700;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: #e0ecff;
  border-color: #bfdbfe;
}

.value.status-pending {
  color: #1890ff;
}

.value.status-confirmed {
  color: #52c41a;
}

.value.status-completed {
  color: #1890ff;
}

.value.status-cancelled {
  color: #8c8c8c;
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

/* 按钮基础样式 */
.action-btn,
.close-drawer-btn {
  position: relative;
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.action-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
}

.action-btn.secondary {
  background: #edf2f7;
  color: #2d3748;
  border: 1px solid #cbd5e0;
}

.action-btn.secondary:hover {
  background: #e2e8f0;
}

.action-btn.third {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.action-btn.third:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

/* 禁用按钮样式 */
.action-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #e2e8f0;
  color: #a0aec0;
  border: 1px solid #cbd5e0;
}

.action-btn.disabled:hover {
  background-color: #e2e8f0;
  color: #a0aec0;
  border: 1px solid #cbd5e0;
  transform: none;
  box-shadow: none;
}

/* Tooltip 提示效果 */
.action-btn.disabled[title]:hover::after {
  content: attr(title);
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 12px;
  background-color: rgba(0, 0, 0, 0.85);
  color: white;
  font-size: 12px;
  white-space: nowrap;
  border-radius: 6px;
  z-index: 1001;
  pointer-events: none;
  animation: tooltipFadeIn 0.2s ease;
}

.action-btn.disabled[title]:hover::before {
  content: '';
  position: absolute;
  bottom: calc(100% + 2px);
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: rgba(0, 0, 0, 0.85);
  z-index: 1001;
  pointer-events: none;
  animation: tooltipFadeIn 0.2s ease;
}

@keyframes tooltipFadeIn {
  from { 
    opacity: 0;
    transform: translateX(-50%) translateY(4px);
  }
  to { 
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* 针对待确认改期状态下的信息样式 */
.status-pending-confirm {
  color: #e59900 !important;
  font-weight: 600 !important;
}

.source-info-row {
  background-color: #fffbe6;
  border-radius: 4px;
  padding: 0.5rem 0.25rem;
  margin-bottom: 0.5rem;
}

.highlight-text {
  color: #0b7a0b !important;
  font-weight: 600 !important;
}

@media (max-width: 768px) {
  .appointments-container {
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
