<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="chat-wrapper">
      <!-- 左侧：科室与医生选择区 -->
      <div class="selector-panel">
        <div class="panel-header">
          <h1>在线问诊</h1>
          <p class="sub-title">先选择科室，再选择医生发起实时在线交流</p>
        </div>

        <!-- 科室列表 -->
        <div class="section-block">
          <div class="section-title">
            <span>科室列表</span>
          </div>
          <div class="dept-list">
            <button
              v-for="dept in departments"
              :key="dept.id"
              class="dept-tag"
              :class="{ active: dept.id === activeDeptId }"
              @click="handleDeptClick(dept)"
            >
              {{ dept.name }}
            </button>
            <p v-if="!departments.length" class="hint-text">正在加载科室信息...</p>
          </div>
        </div>

        <!-- 医生列表 -->
        <div class="section-block doctor-section">
          <div class="section-title flex-between">
            <span v-if="activeDeptName">“{{ activeDeptName }}”科室医生</span>
            <span v-else class="muted">请先选择左侧科室</span>
            <button
              v-if="activeDeptId && doctors.length"
              class="random-btn"
              type="button"
              @click="handleRandomMatch"
            >
              系统随机分配在线医生
            </button>
          </div>

          <div v-if="activeDeptId" class="doctor-list">
            <template v-if="doctors.length">
              <div
                v-for="doc in doctors"
                :key="doc.id"
                class="doctor-card"
                :class="{ selected: doc.id === activeDoctorId }"
                @click="handleSelectDoctor(doc)"
              >
                <div class="avatar-circle">
                  {{ doc.name?.charAt(0) || '医' }}
                </div>
                <div class="doctor-main">
                  <div class="doctor-header">
                    <span class="doctor-name">{{ doc.name }}</span>
                    <span class="doctor-title">{{ doc.title || '主治医生' }}</span>
                  </div>
                  <p v-if="doc.description" class="doctor-desc">
                    {{ truncateText(doc.description, 50) }}
                  </p>
                  <div class="doctor-meta">
                    <span class="status-pill" :class="doc.status === 'online' ? 'online' : 'busy'">
                      <span class="status-dot"></span>
                      {{ doc.status === 'online' ? '在线' : '忙碌' }}
                    </span>
                  </div>
                </div>
                <button
                  class="chat-btn"
                  type="button"
                  :disabled="doc.status !== 'online'"
                  @click.stop="handleStartChat(doc)"
                >
                  {{ activeDoctorId === doc.id && activeRoomId ? '继续问诊' : '发起问诊' }}
                </button>
              </div>
            </template>
            <p v-else class="hint-text">该科室暂无医生信息。</p>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天面板 -->
      <div class="chat-panel">
        <div v-if="!activeRoomId" class="chat-placeholder">
          <h2>请选择医生发起在线问诊</h2>
          <p>左侧先选择科室，再点击医生右侧“发起问诊”按钮即可。</p>
        </div>
        <div v-else class="chat-room-wrapper">
          <div class="chat-room-header">
            <div>
              <h2>{{ activeDoctorName || '在线问诊' }}</h2>
              <p class="chat-subtitle">
                患者ID：{{ currentUserId }} · 医生ID：{{ activeDoctorId }}
              </p>
            </div>
            <button class="end-chat-btn" type="button" @click="handleEndChat">
              结束本次问诊
            </button>
          </div>

         <!-- 父组件片段：将 activeDoctorId 作为 receiverId 传入，并明确传入 sessionId -->
<ChatRoom
  :room-id="activeRoomId"
  :session-id="activeRoomId"
  :receiver-id="activeDoctorId"
  :end-signal="endSignal"
  :messages="chatMessages"
  :current-user-id="currentUserId"
  :session-status="activeSessionStatus"
  current-role="patient"
  @message-sent="loadMessages"
  @message-received="handleMessageReceived"
/>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import ChatRoom from '@/components/ChatRoom.vue'
import {
  fetchDepartments,
  fetchDoctorsByDept,
  createChatSession,
  fetchChatHistory,
  closeChatSession
} from '@/stores/api'

// 顶部导航高度处理，与其他页面保持一致
const navRef = ref(null)
const navHeight = ref(110)

const updateNavHeight = () => {
  if (navRef.value?.$el) {
    navHeight.value = navRef.value.$el.offsetHeight + 30
  }
}

const handleResize = () => {
  updateNavHeight()
}

// 患者当前登录用户（后续可替换为实际登录信息：如从 token 或全局 store 读取）
const currentUserId = ref(null)

// 科室 & 医生数据
const departments = ref([])
const activeDeptId = ref(null)
const activeDeptName = ref('')
const doctors = ref([])

// 当前选中的医生 & 聊天房间
const activeDoctorId = ref(null)
const activeDoctorName = ref('')
const activeRoomId = ref(null)
const chatMessages = ref([])

// Bug #1 修复：添加 endSignal 变量定义
const endSignal = ref(0)
// Bug #4 修复：添加会话状态，用于传递给 ChatRoom 组件
const activeSessionStatus = ref('active')

// 文本截断
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 加载科室列表（使用模拟 API，与 ChatPatient 保持一致）
const loadDepartments = async () => {
  try {
    const list = await fetchDepartments()
    departments.value = list || []
  } catch (e) {
    console.error('获取科室列表失败', e)
  }
}

// 加载某科室下的医生
const loadDoctors = async (deptId) => {
  try {
    doctors.value = await fetchDoctorsByDept(deptId)
  } catch (e) {
    console.error('获取医生列表失败', e)
    doctors.value = []
  }
}

// 进入/刷新当前房间的历史消息
const loadMessages = async () => {
  if (!activeRoomId.value) return
  try {
    chatMessages.value = await fetchChatHistory(activeRoomId.value)
  } catch (e) {
    console.error('获取聊天记录失败', e)
  }
}

// WebSocket 收到新消息时，直接 push 到本地列表，增强实时感
const handleMessageReceived = (message) => {
  chatMessages.value = [...chatMessages.value, message]
}

// 点击科室
const handleDeptClick = (dept) => {
  if (dept.id === activeDeptId.value) return
  activeDeptId.value = dept.id
  activeDeptName.value = dept.name
  doctors.value = []
  loadDoctors(dept.id)
}

// 选中某个医生卡片（不一定立即发起会话）
const handleSelectDoctor = (doctor) => {
  activeDoctorId.value = doctor.id
  activeDoctorName.value = doctor.name
}

// 发起或继续与某医生的问诊
const startChatWithDoctor = async (doctor) => {
  try {
    if (!currentUserId.value) {
      // 这里简化处理：从本地存储或其他全局登录信息中读取
      const uid = localStorage.getItem('patientId')
      if (!uid) {
        alert('请先登录后再使用在线问诊功能。')
        return
      }
      currentUserId.value = Number(uid)
    }

    activeDoctorId.value = doctor.id
    activeDoctorName.value = doctor.name
    // 使用真实后端 ChatSession 的 ID 作为房间 ID / sessionId
    const sessionId = await createChatSession(currentUserId.value, doctor.id)
    activeRoomId.value = sessionId
    await loadMessages()
  } catch (e) {
    console.error('创建会话失败', e)
    alert('创建问诊会话失败，请稍后重试。')
  }
}

// 点击“发起问诊”按钮
const handleStartChat = (doctor) => {
  if (doctor.status !== 'online') {
    alert('该医生当前忙碌，暂时无法发起问诊。')
    return
  }
  startChatWithDoctor(doctor)
}

// 随机匹配在线医生
const handleRandomMatch = () => {
  const onlineDoctors = doctors.value.filter((d) => d.status === 'online')
  if (!onlineDoctors.length) {
    alert('当前科室暂无在线医生可供分配。')
    return
  }
  const index = Math.floor(Math.random() * onlineDoctors.length)
  const doctor = onlineDoctors[index]
  startChatWithDoctor(doctor)
}

// 结束当前问诊 - Bug #2 修复：通知后端关闭会话
const handleEndChat = async () => {
  if (activeRoomId.value) {
    try {
      await closeChatSession(activeRoomId.value)
    } catch (e) {
      console.error('关闭会话失败', e)
    }
    endSignal.value += 1
  }
  activeRoomId.value = null
  chatMessages.value = []
  activeSessionStatus.value = 'active'
}

// 周期性轮询消息（在使用 WebSocket 的基础上，作为兜底）
let messageIntervalId
watch(
  activeRoomId,
  (newVal) => {
    if (messageIntervalId) {
      clearInterval(messageIntervalId)
      messageIntervalId = null
    }
    if (newVal) {
      messageIntervalId = setInterval(loadMessages, 3000)
    }
  },
  { immediate: true }
)

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
  await loadDepartments()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (messageIntervalId) {
    clearInterval(messageIntervalId)
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
}

.chat-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 1.5rem;
}

.selector-panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  padding: 1.5rem 1.75rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.panel-header h1 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: #1f2933;
}

.sub-title {
  margin-top: 0.35rem;
  font-size: 0.9rem;
  color: #6b7280;
}

.section-block {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
}

.section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 0.75rem;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dept-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.dept-tag {
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  font-size: 0.85rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dept-tag:hover {
  border-color: #4f46e5;
  background-color: #eef2ff;
  color: #3730a3;
}

.dept-tag.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-color: transparent;
}

.doctor-section {
  margin-top: 0.5rem;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.doctor-card {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 0.8rem 0.9rem;
  border-radius: 12px;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.doctor-card:hover {
  background-color: #eef2ff;
  border-color: #6366f1;
  transform: translateY(-1px);
}

.doctor-card.selected {
  border-color: #4f46e5;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.15);
}

.avatar-circle {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.doctor-main {
  flex: 1;
  min-width: 0;
}

.doctor-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.doctor-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
}

.doctor-title {
  font-size: 0.8rem;
  color: #4f46e5;
  background-color: #eef2ff;
  border-radius: 999px;
  padding: 0.1rem 0.5rem;
}

.doctor-desc {
  font-size: 0.8rem;
  color: #6b7280;
  margin: 0.1rem 0 0.3rem;
}

.doctor-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-pill .status-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  margin-right: 0.3rem;
}

.status-pill.online {
  background-color: #ecfdf5;
  color: #15803d;
}

.status-pill.online .status-dot {
  background-color: #22c55e;
}

.status-pill.busy {
  background-color: #fef3c7;
  color: #92400e;
}

.status-pill.busy .status-dot {
  background-color: #f97316;
}

.chat-btn {
  padding: 0.4rem 0.8rem;
  border-radius: 999px;
  border: none;
  font-size: 0.8rem;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.chat-btn:hover {
  filter: brightness(1.05);
}

.chat-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.random-btn {
  font-size: 0.8rem;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  border: 1px solid rgba(79, 70, 229, 0.2);
  background-color: #eef2ff;
  color: #4f46e5;
  cursor: pointer;
  transition: all 0.2s ease;
}

.random-btn:hover {
  background-color: #e0e7ff;
  border-color: #4f46e5;
}

.hint-text {
  font-size: 0.85rem;
  color: #9ca3af;
  margin-top: 0.25rem;
}

.muted {
  color: #9ca3af;
}

.chat-panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  min-height: 520px;
  display: flex;
  flex-direction: column;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #6b7280;
}

.chat-placeholder h2 {
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
  color: #111827;
}

.chat-room-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.chat-room-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #111827;
}

.chat-subtitle {
  margin: 0.2rem 0 0;
  font-size: 0.8rem;
  color: #9ca3af;
}

.end-chat-btn {
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  border: none;
  font-size: 0.8rem;
  color: #ffffff;
  background: #ef4444;
  cursor: pointer;
  transition: all 0.2s ease;
}

.end-chat-btn:hover {
  background: #dc2626;
}

.chat-room-body {
  flex: 1;
  margin-top: 0.5rem;
  min-height: 400px;
}

@media (max-width: 1024px) {
  .chat-wrapper {
    grid-template-columns: 1fr;
    padding: 1.5rem;
  }

  .chat-panel {
    min-height: 420px;
  }
}

@media (max-width: 768px) {
  .chat-wrapper {
    padding: 1rem;
  }

  .selector-panel {
    padding: 1.25rem 1.25rem;
  }
}
</style>


