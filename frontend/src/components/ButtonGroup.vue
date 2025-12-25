
<template>
  <div class="button-group">
    <button
      type="button"
      @click="$emit('cancel')"
      :disabled="loading"
      class="btn btn-cancel"
    >
      取消
    </button>
    <button
      type="button"
      @click="$emit('submit')"
      :disabled="loading || !canSubmit"
      class="btn btn-submit"
    >
      <span v-if="loading" class="loading-spinner">
        <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="2" x2="12" y2="6"></line>
          <line x1="12" y1="18" x2="12" y2="22"></line>
          <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
          <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
          <line x1="2" y1="12" x2="6" y2="12"></line>
          <line x1="18" y1="12" x2="22" y2="12"></line>
          <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
          <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
        </svg>
      </span>
      <span>{{ loading ? '提交中...' : '提交审核' }}</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  loading: Boolean,
  canSubmit: Boolean
})

defineEmits(['cancel', 'submit'])
</script>

<style scoped>
.button-group {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
}

.btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-cancel {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-cancel:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-spinner svg {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .button-group {
    gap: 0.75rem;
  }

  .btn {
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
  }
}
</style>