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
        />
      </transition-group>
    </div>

    <!-- 预约详情抽屉 -->
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
              <span class="value">{{ selectedRecord.building }}</span>
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
          </div>

        <div class="detail-section">
  <h4>费用信息</h4>
  <div class="detail-row">
    <span class="label">原始费用：</span>
    <span class="value fee">¥{{ selectedRecord.feeOriginal ?? selectedRecord.feeFinal ?? 0 }}</span>
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
            v-if="activeStatus === 'current' && selectedRecord.status !== 'cancelled'"
            @click="handleCancelAppointment(selectedRecord)"
            class="cancel-btn">
            取消预约
          </button>
           <button 
            v-if="activeStatus === 'current' && selectedRecord.status !== 'cancelled'"
            @click="handleChangeAppointment(selectedRecord)"
            class="cancel-btn secondary">
            改约
          </button>
                     <button 
    v-if="activeStatus === 'current' && selectedRecord.status === 'pending'"
    @click="handlePay(selectedRecord)"
    class="cancel-btn third">
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
  </div>
  

</template>

<script setup>
import { ref, computed, onMounted,nextTick} from 'vue'
import axios from 'axios'
import AppointmentRecordCard from './AppointmentRecordCard.vue'
import Payment from './Payment.vue'

const payDialogVisible = ref(false)
const payInfo = ref({ appointmentId: null })

async function handlePay(record) {
  if (!record || !record.appointmentId) {
    console.error('handlePay: appointmentId 不存在', record)
    return
  }

  // 先设置数据
  payInfo.value = {
    appointmentId: record.appointmentId,
  }

  console.log('设置 payInfo：', payInfo.value)

  // 等待 Vue 同步 props 更新
  await nextTick()

  // 然后再打开弹窗
  payDialogVisible.value = true

  console.log('打开支付窗口，payInfo：', payInfo.value)
}

function closePayDialog() {
  // 关闭对话框并清理信息（防止下一次残留）
  payDialogVisible.value = false
  payInfo.value = { appointmentId: null}
}

function handlePaymentSuccess(data) {
  // data: 可由后端返回的支付结果
  alert('支付成功')
  closePayDialog()
  closeDetailDrawer() // 如果需要同时关闭详情抽屉
  fetchAppointments() // 刷新预约列表
}

function handlePaymentError(err) {
  console.warn('支付错误：', err)
  // 这里一般不自动关闭支付框，让用户重试或取消
}



const activeStatus = ref('current')
const loading = ref(false)
const currentAppointments = ref([])
const historyAppointments = ref([])
const detailDrawerVisible = ref(false)
const selectedRecord = ref({})
const token = ref(localStorage.getItem('token'))

// 显示对应状态列表
const displayedAppointments = computed(() => {
  return activeStatus.value === 'current'
    ? currentAppointments.value
    : historyAppointments.value
})

// 切换状态
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
        appt.status === 'booked' || appt.status === 'pending' 
      )
      historyAppointments.value = appointments.filter(appt => 
        appt.status === 'completed' || appt.status === 'cancelled' || appt.status === 'no_show'
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
  console.log('feeFinal from backend:', appt.feeFinal)

  return {
    appointmentId: appt.appointmentId,
    patientName: appt.patientName || '患者',
    doctorName: appt.doctorName || '医生',
    doctorTitle: appt.doctorTitle || '医师',
    deptName: appt.deptName || '科室',
    building: appt.building || '医院',
    typeName: appt.typeName || '普通',
    appointmentTime: appt.appointmentTime || '未指定时间',
    bookingTime: appt.bookingTime,
    status: mapStatus(appt.status),
    feeFinal: appt.feeFinal || '未知',
    remarks: appt.remarks
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
    'no_show': 'no-show'
  }
  return statusMap[apiStatus] || 'pending'
}

// 获取状态标签
function getStatusLabel(status) {
  const statusMap = {
    'pending': '待支付',
    'booked': '待就诊',
    'completed': '已完成',
    'cancelled': '已取消',
    'no-show': '未到诊'
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
//改约
function handleChangeAppointment(record) {
  // 可添加改约逻辑
  alert('改约功能暂未实现')
}
// 取消预约
async function handleCancelAppointment(record) {
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

.value.fee {
  color: #667eea;
  font-weight: 600;
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



.cancel-btn,
.close-drawer-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.cancel-btn:hover {
  background: #fed7d7;
  border-color: #fc8181;
}
.cancel-btn.secondary {
  background: #edf2f7;
  color: #2d3748;
  border: 1px solid #cbd5e0;
}
.cancel-btn.secondary:hover {
  background: #e2e8f0;
}
.cancel-btn.third {
  background: #fcecd1;
  color: #b9562b;
  border: 1px solid #d3b81e;
}
.cancel-btn.third:hover {
  background: #ecc896;
  border-color: #d1724f;
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