<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="consultation-wrapper">
      <div class="chat-window">
        <!-- Header -->
        <div class="chat-header">
          <button class="back-btn" @click="goBack" title="返回">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
          </button>
          <div class="header-content">
            <h2>智能问诊助手</h2>
            <p>描述您的症状，AI 智能助手为您提供专业分诊建议</p>
          </div>
        </div>
        
        <!-- Messages Area -->
        <div class="messages" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
            <div class="avatar">
              <span v-if="msg.role === 'model'">🤖</span>
              <span v-else>👤</span>
            </div>
            <div class="message-content">
              <!-- Loading Indicator -->
              <div v-if="msg.loading" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
              <!-- Text Content -->
              <div v-else class="bubble" v-html="formatMessage(msg.content)"></div>
              
              <!-- Report Card (Only for model) -->
              <div v-if="msg.report" class="report-card">
                <div class="report-header">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                  <span>分析报告</span>
                </div>
                <div class="report-body">{{ msg.report }}</div>
                <div class="recommendation-section">
                  <div class="rec-label">推荐科室</div>
                  <div class="rec-value">{{ msg.department }}</div>
                </div>
                <button @click="goToRegistration(msg.department)" class="action-btn">
                  立即挂号
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea 
              v-model="userInput" 
              @keydown.enter.prevent="sendMessage"
              placeholder="请详细描述您的症状，例如：头痛持续三天，伴有恶心..."
              :disabled="isLoading"
              rows="1"
              ref="textareaRef"
              @input="autoResize"
            ></textarea>
            <button class="send-btn" @click="sendMessage" :disabled="isLoading || !userInput.trim()">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const router = useRouter()
const messages = ref([
  { 
    role: 'model', 
    content: '您好！我是您的智能健康助手。请告诉我您哪里不舒服？我会协助您进行初步分诊。' 
  }
])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const textareaRef = ref(null)

// Navigation handling
const navRef = ref(null)
const navHeight = ref(80)

function updateNavHeight() {
  if (navRef.value && navRef.value.$el) {
    navHeight.value = navRef.value.$el.offsetHeight
  }
}

function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const goBack = () => {
  router.back()
}

// Auto resize textarea
const autoResize = () => {
  const el = textareaRef.value
  if (el) {
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

const sendMessage = async () => {
  const token = localStorage.getItem('token')
  if (!userInput.value.trim() || isLoading.value) return

  const text = userInput.value
  userInput.value = ''
  if (textareaRef.value) textareaRef.value.style.height = 'auto' // Reset height
  
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  isLoading.value = true
  messages.value.push({ role: 'model', content: '', loading: true })
  scrollToBottom()

  try {
    const history = messages.value
      .filter(m => !m.loading && m.content)
      .slice(0, -2)
      .map(m => ({ role: m.role, content: m.content }))

    const response = await axios.post('http://localhost:8443/api/consultation/chat', {
      message: text,
      history: history
    },
    {
      headers: token
        ? { Authorization: `Bearer ${token}` }
        : {}
    })

    messages.value.pop() // Remove loading

    const data = response.data
    const aiMsg = {
      role: 'model',
      content: data.reply,
      report: data.report,
      department: data.recommendedDepartment
    }
    messages.value.push(aiMsg)
    
  } catch (error) {
    console.error('Chat error:', error)
    messages.value.pop()
    messages.value.push({ role: 'model', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const goToRegistration = (departmentName) => {
  router.push({ path: '/department', query: { recommend: departmentName } })
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: #f3f4f6;
  display: flex;
  justify-content: center;
  box-sizing: border-box;
}

.consultation-wrapper {
  width: 100%;
  max-width: 900px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--nav-height, 80px) - 40px); /* Slightly shorter */
}

.chat-window {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(229, 231, 235, 1);
}

.chat-header {
  padding: 16px 24px;
  background: white; /* Clean white header */
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.back-btn {
  background: #f3f4f6;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #4b5563;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #e5e7eb;
  color: #111827;
}

.header-content h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.header-content p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-row {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background-color: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.message-row.model .avatar {
  background-color: #fff;
  border: 1px solid #e5e7eb;
}

.message-row.user .avatar {
  background-color: #3b82f6;
  color: white;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.message-row.user .message-content {
  align-items: flex-end;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
  word-wrap: break-word;
  max-width: 100%;
}

.message-row.model .bubble {
  background-color: white;
  border: 1px solid #e5e7eb;
  color: #374151;
  border-top-left-radius: 4px;
}

.message-row.user .bubble {
  background-color: #3b82f6;
  color: white;
  border: 1px solid #2563eb;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
}

/* Report Card Styling */
.report-card {
  margin-top: 4px;
  background: white;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  width: 280px;
}

.report-header {
  background: #eff6ff;
  padding: 10px 16px;
  color: #1e40af;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid #dbeafe;
}

.report-body {
  padding: 12px 16px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  border-bottom: 1px solid #f3f4f6;
}

.recommendation-section {
  padding: 12px 16px;
  background: #f8fafc;
}

.rec-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.rec-value {
  font-weight: 600;
  color: #0f172a;
  font-size: 15px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background: #fff;
  border: none;
  border-top: 1px solid #e2e8f0;
  color: #2563eb;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #f1f5f9;
}

/* Input Area */
.input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #f3f4f6;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  background: #f9fafb;
  padding: 8px 8px 8px 16px;
  border-radius: 24px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
  align-items: flex-end;
}

.input-wrapper:focus-within {
  background: white;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

textarea {
  flex: 1;
  background: transparent;
  border: none;
  padding: 8px 0;
  resize: none;
  max-height: 120px;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  color: #1f2937;
}

textarea:focus {
  outline: none;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

/* Animations */
.typing-indicator span {
  display: inline-block;
  width: 5px;
  height: 5px;
  background-color: #9ca3af;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* Responsive */
@media (max-width: 640px) {
  .consultation-wrapper {
    padding: 0;
    height: calc(100vh - var(--nav-height, 60px));
  }
  
  .chat-window {
    border-radius: 0;
    border: none;
    box-shadow: none;
  }
}
</style>
