<template>
  <div class="payment-container">
    <!-- 按钮触发弹窗 -->
    <button @click="showPopup = true" class="pay-btn" :disabled="loading">
      {{ loading ? '处理中...' : '去支付' }}
    </button>

    <!-- 遮罩层 -->
    <div v-if="showPopup" class="overlay" @click.self="closePopup">
      <div class="popup">
        <h2>确认支付</h2>
        <p>请确认是否支付 <strong>￥{{ amount?.toFixed(2) || '0.00' }}</strong></p>

        <!-- 支付方式选择 -->
        <div class="payment-methods">
          <label v-for="method in paymentMethods" :key="method.value" class="method-item">
            <input 
              type="radio" 
              v-model="selectedMethod" 
              :value="method.value"
              :disabled="loading"
            />
            <span class="method-name">{{ method.label }}</span>
          </label>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="error-message">
          {{ errorMsg }}
        </div>

        <div class="button-group">
          <button class="cancel" @click="closePopup" :disabled="loading">取消</button>
          <button class="confirm" @click="handlePay" :disabled="loading || !selectedMethod">
            {{ loading ? '支付中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// Props 接收
const props = defineProps({
  appointmentId: {
    type: Number,
    required: true
  },
  amount: {
    type: Number,
    required: true
  }
})

// Emits
const emit = defineEmits(['payment-success', 'payment-error'])

// 状态
const showPopup = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const selectedMethod = ref('alipay')

// 支付方式列表
const paymentMethods = ref([
  { label: '支付宝', value: 'alipay' },
  { label: '微信支付', value: 'wechat' },
  { label: '银行卡', value: 'card' }
])

// 获取 token
function getToken() {
  // 从 localStorage 获取 token
  let token = localStorage.getItem('token')
  
  // 如果 localStorage 没有，尝试从 sessionStorage 获取
  if (!token) {
    token = sessionStorage.getItem('token')
  }
  
  // 如果都没有，尝试从 cookie 获取（可选）
  if (!token) {
    token = getCookieValue('Authorization')
  }
  
  return token
}

// 从 cookie 获取值
function getCookieValue(name) {
  const cookies = document.cookie.split(';')
  for (let cookie of cookies) {
    const [key, value] = cookie.trim().split('=')
    if (key === name) {
      return decodeURIComponent(value)
    }
  }
  return null
}

function closePopup() {
  showPopup.value = false
  errorMsg.value = ''
}

async function handlePay() {
  if (!selectedMethod.value) {
    errorMsg.value = '请选择支付方式'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const token = getToken()
    
    if (!token) {
      errorMsg.value = '未找到认证信息，请重新登录'
      loading.value = false
      return
    }

    // 调用后端支付接口
    const response = await axios.put(
      '/pay',
      null,
      {
        params: {
          appointmentId: props.appointmentId,
          paymentMethod: selectedMethod.value
        },
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )

    // 处理响应
    if (response.data.code === 200 || response.data.success) {
      showPopup.value = false
      emit('payment-success', response.data.data)
      // 可选：显示成功提示
      console.log('支付成功:', response.data.data)
    } else {
      errorMsg.value = response.data.msg || response.data.message || '支付失败，请重试'
      emit('payment-error', errorMsg.value)
    }
  } catch (error) {
    console.error('支付请求错误:', error)
    
    if (error.response) {
      errorMsg.value = error.response.data?.msg || error.response.data?.message || '支付失败'
    } else if (error.request) {
      errorMsg.value = '网络连接失败，请检查网络'
    } else {
      errorMsg.value = error.message || '发生未知错误'
    }
    
    emit('payment-error', errorMsg.value)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.payment-container {
  display: inline-block;
}

.pay-btn {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: 0.3s;
  font-weight: 500;
}

.pay-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.3);
}

.pay-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

.popup {
  background: white;
  border-radius: 16px;
  padding: 2rem 2.5rem;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 320px;
  animation: slideUp 0.25s ease;
  max-height: 80vh;
  overflow-y: auto;
}

.popup h2 {
  margin-bottom: 1rem;
  font-size: 1.4rem;
  color: #2d3748;
}

.popup p {
  margin-bottom: 1.5rem;
  color: #4a5568;
  font-size: 1.1rem;
}

.payment-methods {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 12px;
  text-align: left;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 0.6rem 0;
  cursor: pointer;
  transition: 0.2s;
}

.method-item:hover {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  padding-left: 0.5rem;
}

.method-item input[type="radio"] {
  cursor: pointer;
  margin-right: 0.8rem;
}

.method-name {
  cursor: pointer;
  color: #2d3748;
  font-weight: 500;
}

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  border-left: 3px solid #c53030;
}

.button-group {
  display: flex;
  gap: 1rem;
  justify-content: space-around;
}

button.cancel,
button.confirm {
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.4rem;
  font-size: 0.95rem;
  cursor: pointer;
  transition: 0.3s;
  font-weight: 500;
  flex: 1;
}

button.cancel {
  background: #e2e8f0;
  color: #4a5568;
}

button.cancel:hover:not(:disabled) {
  background: #cbd5e0;
}

button.cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

button.confirm {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

button.confirm:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

button.confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>