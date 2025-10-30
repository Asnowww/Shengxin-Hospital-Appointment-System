<template>
  <div class="reminder-container">
    <transition-group name="reminder" tag="div" class="reminder-list">
      <div
        v-for="reminder in reminders"
        :key="reminder.id"
        :class="['reminder-item', `reminder-${reminder.type}`]"
        @click="handleReminder(reminder)"
      >
        <div class="reminder-icon">
          <svg v-if="reminder.type === 'appointment'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <svg v-else-if="reminder.type === 'review'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <svg v-else-if="reminder.type === 'payment'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
            <line x1="1" y1="10" x2="23" y2="10"></line>
          </svg>
          <svg v-else-if="reminder.type === 'urgent'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3.05h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
        </div>

        <div class="reminder-content">
          <div class="reminder-header">
            <h4 class="reminder-title">{{ reminder.title }}</h4>
            <span :class="['reminder-badge', `badge-${reminder.status}`]">{{ getStatusText(reminder.status) }}</span>
          </div>
          <p class="reminder-message">{{ reminder.message }}</p>
          <div class="reminder-meta">
            <span class="reminder-time">{{ formatTime(reminder.createTime) }}</span>
            <span v-if="reminder.actionText" class="reminder-action">{{ reminder.actionText }}</span>
          </div>
        </div>

        <button
          class="reminder-close"
          @click.stop="dismissReminder(reminder.id)"
          aria-label="关闭提醒"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const reminders = ref([])
const token = ref(localStorage.getItem('token'))
let pollInterval = null

// 提醒类型映射
const typeMap = {
  'appointment': '就诊提醒',
  'review': '审核结果',
  'payment': '支付提醒',
  'urgent': '紧急提醒',
  'info': '信息提醒'
}

// 状态映射
const statusMap = {
  'unread': '未读',
  'read': '已读',
  'pending': '待处理',
  'completed': '已完成'
}

function getStatusText(status) {
  return statusMap[status] || '未知'
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return '刚刚'
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }
  // 1小时内
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  // 24小时内
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  // 7天内
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  
  return date.toLocaleDateString('zh-CN')
}

// 模拟数据
const mockReminders = [
  {
    id: 1,
    type: 'appointment',
    title: '就诊提醒',
    message: '您有一个预约即将开始，请在下午2:00前到达医院就诊。医生：张医生，科室：心内科',
    status: 'unread',
    createTime: new Date(Date.now() - 5 * 60000).toISOString(),
    actionText: '查看详情'
  },
  {
    id: 2,
    type: 'review',
    title: '身份认证通过',
    message: '恭喜！您的身份认证已通过审核，现在可以使用完整功能。',
    status: 'unread',
    createTime: new Date(Date.now() - 1 * 3600000).toISOString(),
    actionText: '查看详情'
  },
  {
    id: 3,
    type: 'payment',
    title: '支付提醒',
    message: '您有一笔待支付的预约费用，金额：¥100.00，请及时完成支付。',
    status: 'pending',
    createTime: new Date(Date.now() - 2 * 3600000).toISOString(),
    actionText: '立即支付'
  },
  {
    id: 4,
    type: 'urgent',
    title: '紧急提醒',
    message: '系统检测到您的预约时间冲突，请及时处理。',
    status: 'unread',
    createTime: new Date(Date.now() - 10 * 60000).toISOString(),
    actionText: '查看冲突'
  },
  {
    id: 5,
    type: 'info',
    title: '信息提醒',
    message: '医院已发布新的挂号规则，请查看最新规则。',
    status: 'read',
    createTime: new Date(Date.now() - 1 * 86400000).toISOString(),
    actionText: '了解更多'
  }
]

// 获取提醒列表
async function fetchReminders() {
  try {
    // 模拟 API 延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 使用模拟数据
    reminders.value = mockReminders
  } catch (err) {
    console.error('获取提醒失败', err)
  }
}

// 处理提醒点击
async function handleReminder(reminder) {
  // 标记为已读
  if (reminder.status === 'unread') {
    try {
      await axios.put(`/api/patient/reminders/${reminder.id}/read`, {}, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      reminder.status = 'read'
    } catch (err) {
      console.error('标记为已读失败', err)
    }
  }

  // 发射事件给父组件处理具体逻辑
  emit('reminder-click', reminder)
}

// 关闭提醒
async function dismissReminder(reminderId) {
  try {
    await axios.delete(`/api/patient/reminders/${reminderId}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    reminders.value = reminders.value.filter(r => r.id !== reminderId)
  } catch (err) {
    console.error('关闭提醒失败', err)
  }
}

// 标记所有为已读
async function markAllAsRead() {
  try {
    await axios.put('/api/patient/reminders/mark-all-read', {}, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    reminders.value.forEach(r => r.status = 'read')
  } catch (err) {
    console.error('标记所有为已读失败', err)
  }
}

// 清空所有提醒
async function clearAllReminders() {
  if (!confirm('确定要清空所有提醒吗？')) return

  try {
    await axios.delete('/api/patient/reminders/all', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    reminders.value = []
  } catch (err) {
    console.error('清空提醒失败', err)
  }
}

// 计算未读数
const unreadCount = computed(() => {
  return reminders.value.filter(r => r.status === 'unread').length
})

const emit = defineEmits(['reminder-click'])

onMounted(() => {
  // 初始加载
  fetchReminders()

  // 定时轮询（每30秒）
  pollInterval = setInterval(() => {
    fetchReminders()
  }, 30000)
})

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval)
  }
})

// 导出方法供父组件调用
defineExpose({
  fetchReminders,
  markAllAsRead,
  clearAllReminders,
  unreadCount
})
</script>

<style scoped>
.reminder-container {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 380px;
  max-height: 600px;
  overflow-y: auto;
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.reminder-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 12px;
  border-left: 4px solid #667eea;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.reminder-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 不同类型的提醒样式 */
.reminder-appointment {
  border-left-color: #1890ff;
}

.reminder-appointment .reminder-icon {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.reminder-review {
  border-left-color: #52c41a;
}

.reminder-review .reminder-icon {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.reminder-payment {
  border-left-color: #faad14;
}

.reminder-payment .reminder-icon {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}

.reminder-urgent {
  border-left-color: #f5222d;
}

.reminder-urgent .reminder-icon {
  background: rgba(245, 34, 45, 0.1);
  color: #f5222d;
}

.reminder-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.reminder-icon svg {
  width: 100%;
  height: 100%;
  padding: 8px;
}

.reminder-content {
  flex: 1;
  min-width: 0;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reminder-title {
  margin: 0;
  color: #2d3748;
  font-size: 0.95rem;
  font-weight: 600;
}

.reminder-badge {
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-unread {
  background: #e6f7ff;
  color: #1890ff;
}

.badge-read {
  background: #f5f5f5;
  color: #8c8c8c;
}

.badge-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.badge-completed {
  background: #f6ffed;
  color: #52c41a;
}

.reminder-message {
  margin: 0 0 0.5rem 0;
  color: #4a5568;
  font-size: 0.9rem;
  line-height: 1.5;
  word-break: break-word;
}

.reminder-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.reminder-time {
  font-size: 0.8rem;
  color: #a0aec0;
}

.reminder-action {
  font-size: 0.8rem;
  color: #667eea;
  font-weight: 600;
}

.reminder-close {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 24px;
  height: 24px;
  background: none;
  border: none;
  color: #cbd5e0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.reminder-close:hover {
  color: #2d3748;
  transform: rotate(90deg);
}

/* 动画 */
.reminder-enter-active,
.reminder-leave-active {
  transition: all 0.3s ease;
}

.reminder-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.reminder-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .reminder-container {
    width: 90vw;
    max-width: 360px;
    right: 10px;
    left: 10px;
    top: auto;
    bottom: 20px;
    max-height: 50vh;
    border-radius: 12px;
    box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  }

  .reminder-item {
    padding: 0.75rem;
    gap: 0.75rem;
  }

  .reminder-title {
    font-size: 0.9rem;
  }

  .reminder-message {
    font-size: 0.85rem;
  }
}
</style>