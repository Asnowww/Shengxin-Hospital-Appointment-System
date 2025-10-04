<template>
  <div v-if="visible" class="modal-backdrop">
    <div class="modal">
      <h3>就诊提醒</h3>
      <p>{{ message }}</p>
      <button @click="close">我知道了</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const message = ref('')

function open(msg) {
  message.value = msg
  visible.value = true
}

function close() {
  visible.value = false
}

defineExpose({ open })
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
}
</style>
