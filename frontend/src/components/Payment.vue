<template>
  <div v-if="visible" class="overlay" @click.self="handleOverlayClick">
    <div class="popup" @click.stop>
      <h2>确认支付</h2>
      <p>请确认是否支付 <strong>￥{{ displayAmount }}</strong></p>

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

      <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

      <div class="button-group">
        <button class="cancel" @click="onCancel" :disabled="loading">取消</button>
        <button class="confirm" @click="handlePay" :disabled="loading || !selectedMethod">
          {{ loading ? '支付中...' : '确认支付' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  appointmentId: { type: [Number, String, null], default: null },
  amount: { type: [Number, String, null], default: 0 },
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'payment-success', 'payment-error'])

const loading = ref(false)
const errorMsg = ref('')
const selectedMethod = ref('alipay')

const paymentMethods = [
  { label: '支付宝', value: 'alipay' },
  { label: '微信支付', value: 'wechat' },
  { label: '银行卡', value: 'card' }
]

// 安全格式化金额：当不是合法数字时显示 0.00
const displayAmount = computed(() => {
  const n = Number(props.amount)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
})

// 当父传入 visible=false 时，清除本地错误/loading 状态（防止残留）
watch(() => props.visible, (v) => {
  if (!v) {
    loading.value = false
    errorMsg.value = ''
    selectedMethod.value = 'alipay'
  }
})

function handleOverlayClick() {
  // 点击遮罩视为取消
  onCancel()
}

function onCancel() {
  // 发出 close 事件，由父去彻底清理 payInfo
  emit('close')
}

async function handlePay() {
  if (!selectedMethod.value) {
    errorMsg.value = '请选择支付方式'
    return
  }

  // 额外校验 appointmentId
  if (props.appointmentId === null || props.appointmentId === undefined) {
    errorMsg.value = '预约信息不完整，无法支付'
    emit('payment-error', errorMsg.value)
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const token = localStorage.getItem('token') || ''

    const response = await axios.put(
      '/api/patient/appointment/pay',
      null,
      {
        params: {
          appointmentId: props.appointmentId,
          paymentMethod: selectedMethod.value
        },
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    )

    if (response?.data?.code === 200 || response?.data?.success) {
      emit('payment-success', response.data.data)
      // 不直接 emit close：让父决定是否关闭（父通常会在 success 回调中 close）
    } else {
      errorMsg.value = response.data?.msg || response.data?.message || '支付失败，请重试'
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