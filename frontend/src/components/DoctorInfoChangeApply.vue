<template>
  <div class="modal-mask" v-if="visible">
    <div class="modal-container">
      <h2 class="modal-title">擅长领域修改申请</h2>
      <p class="modal-sub">提交后将进入管理员审核流程</p>

      <textarea
        v-model="form.bio"
        rows="5"
        class="input-area"
        placeholder="请输入修改后的擅长领域"
      ></textarea>

      <div class="actions">
        <button class="btn cancel" @click="close">取消</button>
        <button class="btn primary" @click="submit">提交申请</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'


const props = defineProps({
  visible: Boolean,
  userId: String,
  currentBio: String
})

const emit = defineEmits(['close', 'submitted'])

const form = ref({
  bio: ''
})

// 弹窗开启时载入当前擅长领域（如果是 pending 则为空）
watch(
  () => props.visible,
  (v) => {
    if (v) {
      form.value.bio = props.currentBio || ''
    }
  }
)

function close() {
  emit('close')
}

async function submit() {
  if (!props.userId) {
    alert('无法识别用户身份')
    return
  }

  try {
    const token = localStorage.getItem('token')

    // form-urlencoded 构建
    const payload = new URLSearchParams()
    payload.append('userId', props.userId)
    payload.append('newBio', form.value.bio)

    await axios.post('/api/doctor/bio/request', payload, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      }
    })

    alert('已提交修改申请，等待管理员审核')
    emit('submitted')
    close()
  } catch (e) {
    alert('提交失败，请稍后再试')
    console.error(e)
  }
}
</script>


<style scoped>
.modal-mask {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 99999;
}

.modal-container {
  width: 520px;
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,.15);
}

.modal-title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 700;
}

.modal-sub {
  color: #666;
  font-size: .9rem;
  margin-bottom: 1rem;
}

.input-area {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  resize: vertical;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.cancel {
  background: #e2e8f0;
}

.primary {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
