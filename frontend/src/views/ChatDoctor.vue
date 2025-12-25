<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="chat-dashboard">
      <!-- 左侧：患者会话列表 -->
      <section class="session-panel">
        <div class="panel-header">
          <div>
            <h1>患者消息盒子</h1>
            <p>实时查看并回复等待中的患者</p>
          </div>
          <button class="refresh-btn" type="button" @click="loadSessions" :disabled="isLoadingSessions">
            <span v-if="!isLoadingSessions">刷新</span>
            <span v-else>刷新中...</span>
          </button>
        </div>

        <div class="session-filters">
          <div class="search-box">
            <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.35-4.35" />
            </svg>
            <input
              v-model.trim="sessionSearch"
              type="text"
              placeholder="搜索患者姓名 / 会话"
            />
          </div>
          <label class="unread-toggle">
            <input type="checkbox" v-model="showUnreadOnly" />
            <span>仅显示未读</span>
          </label>
        </div>

        <div class="session-list" :class="{ empty: !filteredSessions.length }">
          <template v-if="filteredSessions.length">
            <article
              v-for="session in filteredSessions"
              :key="session.id"
              :class="['session-item', { active: session.id === activeSessionId }]"
              @click="selectSession(session)"
            >
              <div class="avatar-circle">
                {{ session.patientName?.charAt(0) || '患' }}
              </div>
              <div class="session-content">
                <div class="session-row">
                  <span class="patient-name">{{ session.patientName }}</span>
                  <span class="session-time">{{ session.lastTime || '--:--' }}</span>
                </div>
                <p class="session-preview">
                  {{ session.lastMessage || '暂无消息' }}
                </p>
                <div class="session-tags">
                  <span class="tag">{{ session.statusLabel }}</span>
                  <span v-if="session.appointmentId" class="tag subtle">预约 #{{ session.appointmentId }}</span>
                </div>
              </div>
              <span v-if="session.unreadCount > 0" class="unread-badge">
                {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
              </span>
            </article>
          </template>
          <div v-else class="empty-state">
            <p>暂无会话或没有匹配结果</p>
          </div>
        </div>
      </section>

      <!-- 右侧：聊天窗口 -->
      <section class="conversation-panel">
        <div v-if="!activeSession" class="placeholder">
          <h2>请选择左侧患者开始问诊</h2>
          <p>点击患者条目即可进入实时聊天。</p>
        </div>
        <div v-else class="conversation-wrapper">
          <header class="conversation-header">
            <div>
              <h2>{{ activeSession.patientName }}</h2>
              <p>患者 ID：{{ activeSession.patientId }} · 会话 ID：{{ activeSession.sessionId }}</p>
            </div>
            <button type="button" class="end-session-btn" @click="endCurrentSession">
              结束当前会话
            </button>
          </header>
          <div class="conversation-body">
            <ChatRoom
              :room-id="activeSession.sessionId"
              :session-id="activeSession.sessionId"
              :receiver-id="activeSession.patientId"
              :end-signal="endSignal"
              :messages="chatMessages"
              :current-user-id="currentDoctorId"
              :session-status="activeSession.status"     
              current-role="doctor"
              @message-sent="handleMessageSent"
              @message-received="handleMessageReceived"
            />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import ChatRoom from '@/components/ChatRoom.vue'
import { fetchDoctorSessions, fetchChatHistory, markMessagesRead, closeChatSession } from '@/stores/api'

const navRef = ref(null)
const navHeight = ref(110)
const updateNavHeight = () => {
  if (navRef.value?.$el) {
    navHeight.value = navRef.value.$el.offsetHeight + 30
  }
}
const handleResize = () => updateNavHeight()

const currentDoctorId = ref(null)
const sessions = ref([])
const sessionSearch = ref('')
const showUnreadOnly = ref(false)
const activeSessionId = ref(null)
const chatMessages = ref([])
const isLoadingSessions = ref(false)
const isLoadingMessages = ref(false)
let messagePollingTimer = null

const filteredSessions = computed(() => {
  const keyword = sessionSearch.value.trim().toLowerCase()
  return sessions.value.filter((session) => {
    const matchesKeyword =
      !keyword ||
      session.patientName?.toLowerCase().includes(keyword) ||
      String(session.sessionId).includes(keyword)
    const matchesUnread = !showUnreadOnly.value || session.unreadCount > 0
    return matchesKeyword && matchesUnread
  })
})

const activeSession = computed(() => sessions.value.find((s) => s.id === activeSessionId.value) || null)

const ensureDoctorId = () => {
  if (currentDoctorId.value) return currentDoctorId.value
  const stored = localStorage.getItem('doctorId') 
  if (!stored) {
    throw new Error('未获取到医生登录信息，请先登录。')
  }
  currentDoctorId.value = Number(stored)
  return currentDoctorId.value
}

const formatTimestamp = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const now = new Date()
  const sameDay = date.toDateString() === now.toDateString()
  if (sameDay) {
    return date.toLocaleTimeString('zh-CN', { hour12: false, hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const mapSessionRecord = (record) => ({
  id: record.id || record.sessionId || `${record.patientId}_${record.sessionId}`,
  sessionId: record.sessionId || record.id,
  patientId: record.patientId,
  patientName: record.patientName || `患者 ${record.patientId}`,
  lastMessage: record.lastMessage || '',
  lastTime: formatTimestamp(record.lastTime || record.updatedAt),
  unreadCount: record.unreadCount ?? 0,
  statusLabel: record.statusLabel || (record.unreadCount > 0 ? '未读' : '已读'),
  status: record.status, 
  appointmentId: record.appointmentId || null
})

const loadSessions = async () => {
  isLoadingSessions.value = true
  try {
    const doctorId = ensureDoctorId()
    const res = await fetchDoctorSessions(doctorId)
    sessions.value = (res || []).map(mapSessionRecord)
    if (!activeSessionId.value && sessions.value.length) {
      activeSessionId.value = sessions.value[0].id
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
    sessions.value = []
  } finally {
    isLoadingSessions.value = false
  }
}

const loadMessages = async () => {
  if (!activeSession.value) {
    chatMessages.value = []
    return
  }
  isLoadingMessages.value = true
  try {
    const history = await fetchChatHistory(activeSession.value.sessionId)
    chatMessages.value = history
    await markMessagesRead(activeSession.value.sessionId, ensureDoctorId())
    markSessionRead(activeSession.value.id)
  } catch (error) {
    console.error('加载聊天记录失败', error)
  } finally {
    isLoadingMessages.value = false
  }
}

const selectSession = (session) => {
  if (session.id === activeSessionId.value) return
  activeSessionId.value = session.id
}

const markSessionRead = (sessionId) => {
  if (!sessionId) return
  sessions.value = sessions.value.map((session) =>
    session.id === sessionId ? { ...session, unreadCount: 0, statusLabel: '已读' } : session
  )
}

const incrementUnread = (sessionId) => {
  sessions.value = sessions.value.map((session) => {
    if (session.id === sessionId) {
      return {
        ...session,
        unreadCount: (session.unreadCount || 0) + 1,
        statusLabel: '未读'
      }
    }
    return session
  })
}

const handleMessageSent = () => {
  loadMessages()
  loadSessions()
}

const handleMessageReceived = (message) => {
  // 去重检查：通过消息ID或内容+发送者+时间戳进行判断
  const isDuplicate = chatMessages.value.some(
    (m) => m.id === message.id || 
    (m.content === message.content && 
     m.senderId === message.senderId && 
     Math.abs(new Date(m.timestamp) - new Date(message.timestamp)) < 5000)
  )
  if (!isDuplicate) {
    chatMessages.value = [...chatMessages.value, message]
  }
  if (message.role === 'patient') {
    const targetSession = sessions.value.find((session) => session.patientId === message.senderId)
    if (targetSession) {
      if (targetSession.id === activeSessionId.value) {
        markSessionRead(targetSession.id)
      } else {
        incrementUnread(targetSession.id)
      }
      sessions.value = sessions.value.map((session) =>
        session.id === targetSession.id
          ? { ...session, lastMessage: message.content, lastTime: message.timestamp || '刚刚' }
          : session
      )
    } else {
      loadSessions()
    }
  }
}

// 结束会话：不销毁当前会话，仅通过 ChatRoom 通知并关闭连接
const endSignal = ref(0)

const endCurrentSession = async () => {
  if (!activeSession.value) return
  // 标记为已读
  markSessionRead(activeSession.value.id)
  // 通知子组件关闭连接并显示“已结束”提示
  endSignal.value += 1
  // 通知后端关闭会话（置 chat_session.status = closed）
  try {
    await closeChatSession(activeSession.value.sessionId)
  } catch (e) {
    console.error('关闭会话失败', e)
  }
}

watch(
  () => activeSessionId.value,
  async () => {
    await loadMessages()
    if (messagePollingTimer) {
      clearInterval(messagePollingTimer)
      messagePollingTimer = null
    }
    if (activeSessionId.value) {
      messagePollingTimer = setInterval(loadMessages, 5000)
    }
  }
)

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
  await loadSessions()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (messagePollingTimer) {
    clearInterval(messagePollingTimer)
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.chat-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 1.5rem;
}

.session-panel,
.conversation-panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.08);
  padding: 1.5rem;
  height: 600px;
  max-height: 600px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.panel-header h1 {
  margin: 0;
  font-size: 1.4rem;
  color: #111827;
}

.panel-header p {
  margin: 0.35rem 0 0;
  font-size: 0.9rem;
  color: #6b7280;
}

.refresh-btn {
  border: none;
  border-radius: 999px;
  padding: 0.4rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.session-filters {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-top: 1.25rem;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.9rem;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.9rem;
  color: #111827;
}

.search-box input:focus {
  outline: none;
}

.unread-toggle {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: #4b5563;
  cursor: pointer;
}

.unread-toggle input {
  accent-color: #6366f1;
}

.session-list {
  margin-top: 1.25rem;
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  min-height: 0;
}

.session-list.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 0.9rem 1rem;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.session-item + .session-item {
  margin-top: 0.75rem;
}

.session-item:hover {
  border-color: #c7d2fe;
  background: #eef2ff;
}

.session-item.active {
  border-color: #4f46e5;
  background: #eef2ff;
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.25);
}

.avatar-circle {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: #6b7280;
}

.patient-name {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
}

.session-preview {
  margin: 0.35rem 0;
  font-size: 0.85rem;
  color: #4b5563;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.session-tags {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.tag {
  font-size: 0.75rem;
  color: #4c1d95;
  background: #ede9fe;
  padding: 0.1rem 0.6rem;
  border-radius: 999px;
  font-weight: 500;
}

.tag.subtle {
  color: #475569;
  background: #e2e8f0;
}

.unread-badge {
  background: #ef4444;
  color: #ffffff;
  border-radius: 999px;
  font-size: 0.75rem;
  padding: 0.15rem 0.45rem;
  font-weight: 600;
}

.empty-state {
  font-size: 0.9rem;
  color: #94a3b8;
}

.conversation-panel {
  height: 600px;
  max-height: 600px;
}

.placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #6b7280;
}

.placeholder h2 {
  margin-bottom: 0.5rem;
  color: #111827;
}

.conversation-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.conversation-header h2 {
  margin: 0;
  color: #111827;
  font-size: 1.3rem;
}

.conversation-header p {
  margin: 0.25rem 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.end-session-btn {
  border: none;
  border-radius: 999px;
  padding: 0.4rem 1rem;
  background: #dc2626;
  color: #ffffff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.end-session-btn:hover {
  background: #b91c1c;
}

.conversation-body {
  margin-top: 1rem;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

@media (max-width: 1024px) {
  .chat-dashboard {
    grid-template-columns: 1fr;
    padding: 1.5rem;
  }

  .session-panel {
    min-height: auto;
  }
}

@media (max-width: 768px) {
  .chat-dashboard {
    padding: 1rem;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .session-filters {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

