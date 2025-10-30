<template>
  <div>
    <!-- 提醒列表 -->
    <div v-if="isLoggedIn && reminders.length > 0" class="reminders-container">
      <div v-for="(reminder, index) in reminders" :key="index" class="reminder-banner">
        <span class="reminder-icon">ℹ️</span>
        <span class="reminder-text">{{ reminder }}</span>
        <button class="reminder-close" @click="removeReminder(index)">×</button>
      </div>
    </div>
    
    <Navigation ref="navRef" />
    <Reminder ref="reminderRef" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navigation from '@/components/Navigation.vue'
import Reminder from '@/components/Reminder.vue'

const navRef = ref(null)
const reminderRef = ref(null)
const isLoggedIn = ref(false)
const reminders = ref([])

onMounted(() => {
  // 检查是否登录
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  
  // 如果已登录，获取提醒列表
  if (isLoggedIn.value) {
    fetchReminders()
  }
})

async function fetchReminders() {
  try {
    const response = await fetch('/api/reminders', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      reminders.value = data.reminders || []
    }
  } catch (error) {
    console.error('获取提醒失败:', error)
  }
}

function removeReminder(index) {
  reminders.value.splice(index, 1)
}
</script>

<style scoped>
.reminders-container {
  width: 100%;
  background: #f5f7fa;
}

.reminder-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #e0e6ed;
  background: #fff;
  transition: background 0.3s;
}

.reminder-banner:hover {
  background: #f9f9f9;
}

.reminder-banner:last-child {
  border-bottom: none;
}

.reminder-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.reminder-text {
  font-size: 14px;
  color: #333;
  word-break: break-word;
  flex: 1;
}

.reminder-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  margin-left: 12px;
  transition: color 0.2s;
  flex-shrink: 0;
}

.reminder-close:hover {
  color: #666;
}
</style>