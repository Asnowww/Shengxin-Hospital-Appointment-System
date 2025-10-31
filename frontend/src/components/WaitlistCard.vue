<template>
  <div :class="['waitlist-card', `status-${record.status}`]" @click="handleClick">
    <div class="card-header">
      <div class="doctor-info">
        <h4>{{ record.doctorName }}</h4>
        <span class="doctor-title">{{ record.doctorTitle }}</span>
      </div>
      <span :class="['status-badge', record.status]">
        {{ getStatusText(record.status) }}
      </span>
    </div>

    <div class="card-body">
      <div class="info-row">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
        </svg>
        <span>{{ record.deptName }}</span>
      </div>

      <div class="info-row">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
          <line x1="16" y1="2" x2="16" y2="6"></line>
          <line x1="8" y1="2" x2="8" y2="6"></line>
          <line x1="3" y1="10" x2="21" y2="10"></line>
        </svg>
        <span>{{ record.workDate }} {{ record.timeSlotName }}</span>
      </div>

      <div class="info-row">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        <span v-if="record.status === 'waiting'">
          排队位置: 第 {{ record.queuePosition }} 位 / 共 {{ record.totalWaiting }} 人
        </span>
        <span v-else>申请时间: {{ formatDateTime(record.requestedAt) }}</span>
      </div>

      <div v-if="record.priority > 0" class="priority-tag">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
        </svg>
        {{ getPriorityText(record.priority) }}
      </div>
    </div>

    <div v-if="record.status === 'converted'" class="conversion-notice">
      已自动为您创建预约，请前往"我的预约"查看
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  record: {
    type: Object,
    required: true
  },
  onRecordClick: {
    type: Function,
    default: null
  }
})

function handleClick() {
  if (props.onRecordClick) {
    props.onRecordClick(props.record)
  }
}

function getStatusText(status) {
  const map = {
    waiting: '候补中',
    converted: '已转正',
    cancelled: '已取消',
    expired: '已过期'
  }
  return map[status] || '未知'
}

function getPriorityText(priority) {
  const map = {
    0: '普通',
    1: '紧急',
    2: '非常紧急'
  }
  return map[priority] || '普通'
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.waitlist-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.waitlist-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.waitlist-card.status-converted {
  border-color: #48bb78;
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
}

.waitlist-card.status-cancelled {
  opacity: 0.7;
  border-color: #cbd5e0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.doctor-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.doctor-info h4 {
  margin: 0;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
}

.doctor-title {
  color: #718096;
  font-size: 0.875rem;
}

.status-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.waiting {
  background: #fff3cd;
  color: #856404;
}

.status-badge.converted {
  background: #d4edda;
  color: #28a745;
}

.status-badge.cancelled {
  background: #e2e8f0;
  color: #718096;
}

.status-badge.expired {
  background: #f8d7da;
  color: #721c24;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #4a5568;
  font-size: 0.9rem;
}

.info-row svg {
  color: #667eea;
  flex-shrink: 0;
}

.priority-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
  color: #d68910;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  align-self: flex-start;
  margin-top: 0.5rem;
}

.priority-tag svg {
  flex-shrink: 0;
  fill: #d68910;
}

.conversion-notice {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #d4edda;
  color: #155724;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  text-align: center;
}

@media (max-width: 768px) {
  .waitlist-card {
    padding: 1.25rem;
  }

  .card-header {
    flex-direction: column;
    gap: 0.75rem;
  }

  .status-badge {
    align-self: flex-start;
  }

  .info-row {
    font-size: 0.85rem;
  }
}
</style>
