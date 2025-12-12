<template>
  <div class="consultation-container">
    <div class="chat-window">
      <div class="chat-header">
        <button class="back-btn" @click="goBack">
          ← 返回
        </button>
        <h2>智能问诊助手</h2>
        <p>请描述您的症状，我将为您提供初步分析和挂号建议。</p>
      </div>
      
      <div class="messages" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="avatar">
            <span v-if="msg.role === 'model'">🤖</span>
            <span v-else>👤</span>
          </div>
          <div class="content">
            <div v-if="msg.loading" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
            <div v-else v-html="formatMessage(msg.content)"></div>
            
            <!-- 显示报告卡片 -->
            <div v-if="msg.report" class="report-card">
              <h3>问诊报告</h3>
              <div class="report-content">{{ msg.report }}</div>
              <div class="recommendation">
                <strong>推荐科室：</strong> {{ msg.department }}
              </div>
              <button @click="goToRegistration(msg.department)" class="action-btn">
                去挂号
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <textarea 
          v-model="userInput" 
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入您的症状..."
          :disabled="isLoading"
        ></textarea>
        <button @click="sendMessage" :disabled="isLoading || !userInput.trim()">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { marked } from 'marked' // Assuming marked is available or we can use simple formatting

const router = useRouter()
const messages = ref([
  { 
    role: 'model', 
    content: '您好！我是智能问诊助手。请告诉我您哪里不舒服？' 
  }
])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

// Go back
const goBack = () => {
  router.back()
}

// Scroll to bottom
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Format message (simple markdown support)
const formatMessage = (content) => {
  // If marked is not installed, fallback to simple replacement
  // For now, let's assume simple text with newlines
  return content.replace(/\n/g, '<br>')
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  const text = userInput.value
  userInput.value = ''
  
  // Add user message
  messages.value.push({ role: 'user', content: text })
  scrollToBottom()

  // Add loading state
  isLoading.value = true
  messages.value.push({ role: 'model', content: '', loading: true })
  scrollToBottom()

  try {
    // Prepare history for API
    const history = messages.value
      .filter(m => !m.loading && m.content)
      .slice(0, -2) // Exclude the just added user message and the loading message
      .map(m => ({ role: m.role, content: m.content }))

    const response = await axios.post('http://localhost:8080/api/consultation/chat', {
      message: text,
      history: history
    })

    // Remove loading message
    messages.value.pop()

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
    messages.value.pop() // Remove loading
    messages.value.push({ role: 'model', content: '抱歉，我遇到了一些问题，请稍后再试。' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const goToRegistration = (departmentName) => {
  // Navigate to department list or specific department if possible
  // For now, go to department list
  router.push({ path: '/department', query: { recommend: departmentName } })
}
</script>

<style scoped>
.consultation-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  height: calc(100vh - 80px); /* Adjust based on nav height */
  background-color: #f5f7fa;
  margin-top: 80px;
}

.chat-window {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 20px;
  background-color: #EFE7DC;
  border-bottom: 1px solid #eee;
  text-align: center;
  position: relative; /* For absolute positioning of back button */
}

.back-btn {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
}

.back-btn:hover {
  background-color: rgba(0,0,0,0.05);
  color: #333;
}

.chat-header h2 {
  margin: 0 0 8px 0;
  color: #333;
}

.chat-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.model {
  align-self: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.user .avatar {
  background-color: #007bff;
  color: white;
}

.message.model .avatar {
  background-color: #F1D06F;
}

.content {
  padding: 12px 16px;
  border-radius: 12px;
  background-color: #f8f9fa;
  line-height: 1.5;
  position: relative;
}

.message.user .content {
  background-color: #007bff;
  color: white;
  border-top-right-radius: 2px;
}

.message.model .content {
  background-color: white;
  border: 1px solid #eee;
  border-top-left-radius: 2px;
}

.report-card {
  margin-top: 12px;
  padding: 12px;
  background-color: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.report-card h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #0369a1;
}

.report-content {
  font-size: 14px;
  color: #334155;
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.recommendation {
  font-size: 14px;
  color: #0c4a6e;
  margin-bottom: 12px;
}

.action-btn {
  background-color: #0ea5e9;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #0284c7;
}

.input-area {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  background-color: white;
}

textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  height: 50px;
  font-family: inherit;
}

textarea:focus {
  outline: none;
  border-color: #007bff;
}

button {
  padding: 0 24px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button:not(:disabled):hover {
  background-color: #0056b3;
}

.typing-indicator span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #ccc;
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
</style>
