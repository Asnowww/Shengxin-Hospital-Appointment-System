<template>
  <div v-if="visible" class="overlay" @click.self="handleOverlayClick">
    <div class="popup" @click.stop>
      <h2>确认支付</h2>
      <p>请确认是否支付 <strong>￥{{ displayAmount }}</strong></p>
      <div class="fee-box">

  <div class="fee-row">
    <span>原始费用：</span>
    <strong>￥{{ feeOriginal.toFixed(2) }}</strong>
  </div>

  <div class="fee-row">
    <span>报销后费用：</span>
    <strong>￥{{ feeFinal.toFixed(2) }}</strong>
  </div>
</div>


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
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'payment-success', 'payment-error'])

const loading = ref(false)
const errorMsg = ref('')
const selectedMethod = ref('alipay')

// 新增：从后端获取的费用
const feeOriginal = ref(0)
const feeFinal = ref(0)

const paymentMethods = [
  { label: '支付宝', value: 'alipay' },
  { label: '微信支付', value: 'wechat' },
  { label: '银行卡', value: 'card' }
]

// 显示支付金额 = feeFinal
const displayAmount = computed(() => {
  const n = Number(feeFinal.value)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
})

// 监听 visible 和 appointmentId，只要弹窗打开就去获取费用
watch(
  () => props.visible,
  async (v) => {
    if (v && props.appointmentId) {
      // 使用 nextTick 确保 props 已更新
      await new Promise(resolve => setTimeout(resolve, 0))
      console.log('fetching fee for appointmentId:', props.appointmentId)
      await fetchFeeInfo()
    }
    if (!v) {
      loading.value = false
      errorMsg.value = ''
      selectedMethod.value = 'alipay'
    }
  },
  { flush: 'post' } // 关键：在组件更新后执行
)

watch(
  () => props.appointmentId,
  async (newId) => {
    console.log('appointmentId changed:', newId)
    if (props.visible && newId) {
      await nextTick()
      await fetchFeeInfo()
    }
  }
)

async function fetchFeeInfo() {
  if (!props.appointmentId) {
    console.warn('fetchFeeInfo: appointmentId 为空')
    errorMsg.value = '预约信息不完整'
    return
  }

  try {
    console.log('开始获取费用，appointmentId:', props.appointmentId)
    const res = await axios.get(`/api/fee/${props.appointmentId}`)
    
    if (res.data.code === 200) {
      feeOriginal.value = res.data.data.feeOriginal || 0
      feeFinal.value = res.data.data.feeFinal || 0
      console.log('费用获取成功:', feeOriginal.value, feeFinal.value)
    } else {
      throw new Error(res.data.message || '费用查询失败')
    }
  } catch (err) {
    console.error('fetchFeeInfo 出错:', err)
    feeOriginal.value = 0
    feeFinal.value = 0
    errorMsg.value = '无法获取费用，请稍后再试'
  }
}

function handleOverlayClick() {
  onCancel()
}

function onCancel() {
  emit('close')
}

async function handlePay() {
  if (!selectedMethod.value) {
    errorMsg.value = '请选择支付方式'
    return
  }

  if (!props.appointmentId) {
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

    if (response.data.code === 200) {
      emit('payment-success', response.data.data)
    } else {
      errorMsg.value = response.data.message || '支付失败'
      emit('payment-error', errorMsg.value)
    }

  } catch (error) {
    errorMsg.value = error.response?.data?.message || '支付失败'
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
.fee-box {
  margin: 1rem 0 1.5rem;
  text-align: left;
  background: #f7fafc;
  padding: 1rem;
  border-radius: 10px;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.6rem;
  font-size: 0.95rem;
  color: #4a5568;
}

.fee-row strong {
  color: #2d3748;
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