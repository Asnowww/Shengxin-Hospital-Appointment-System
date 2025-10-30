<template>
  <div class="waitlist-record-card" @click="onCardClick">
    <div class="record-header">
      <div class="header-left">
        <h3 class="doctor-name">{{ record.doctorName }}</h3>
        <span class="doctor-title">{{ record.doctorTitle }}</span>
      </div>
      <div class="header-right">
        <span :class="['status-badge', record.status]">{{ getStatusLabel(record.status) }}</span>
        <svg class="arrow-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </div>
    </div>

    <div class="record-content">
      <div class="content-grid">
        <div class="content-item">
          <span class="content-label">科室</span>
          <span class="content-value">{{ record.deptName }}</span>
        </div>
        <div class="content-item">
          <span class="content-label">预约类型</span>
          <span class="content-value">{{ record.typeName }}</span>
        </div>
        <div class="content-item">
          <span class="content-label">地点</span>
          <span class="content-value">{{ record.building }}</span>
        </div>
        <div class="content-item">
          <span class="content-label">队位</span>
          <span class="content-value queue-position">第 {{ record.queuePosition }} 位</span>
        </div>
      </div>

      <div class="target-time-row">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
        <span class="time-label">目标时间：</span>
        <span class="time-value">{{ record.targetDate }} {{ record.targetTime }}</span>
      </div>

      <div class="patient-row">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <span class="patient-label">患者：</span>
        <span class="patient-value">{{ record.patientName }}</span>
      </div>
    </div>

    <div class="record-footer">
      <div class="join-time">
        <span class="join-icon">📅</span>
        <span class="join-text">加入于 {{ formatTime(record.createdAt) }}</span>
      </div>
      <div v-if="record.status === 'converted'" class="status-tip converted">
        <span class="tip-icon">✓</span>
        <span class="tip-text">已转预约</span>
      </div>
      <div v-else-if="record.status === 'expired'" class="status-tip expired">
        <span class="tip-icon">⏱</span>
        <span class="tip-text">已过期</span>
      </div>
      <div v-else-if="record.status === 'cancelled'" class="status-tip cancelled">
        <span class="tip-icon">✕</span>
        <span class="tip-text">已取消</span>
      </div>
      <div v-else class="status-tip waiting">
        <span class="tip-icon">⏳</span>
        <span class="tip-text">等待中</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  record: {
    type: Object,
    required: true,
    validator(value) {
      return value.doctorName && value.targetTime && value.status
    }
  },
  onRecordClick: {
    type: Function,
    default: null
  }
})

const statusMap = {
  'waiting': '等待中',
  'converted': '已转预约',
  'cancelled': '已取消',
  'expired': '已过期'
}

function getStatusLabel(status) {
  return statusMap[status] || status
}

function formatTime(dateStr) {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

function onCardClick() {
  if (props.onRecordClick) {
    props.onRecordClick(props.record)
  }
}
</script>

<style scoped>
.waitlist-record-card {
  background: #fff;
  border-radius: 14px;
  padding: 1.25rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.waitlist-record-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.waitlist-record-card:hover::before {
  left: 100%;
}

.waitlist-record-card:hover {
  border-color: #cbd5e0;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  transform: translateY(-3px);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex: 1;
  min-width: 0;
}

.doctor-name {
  margin: 0;
  color: #2d3748;
  font-size: 1.1rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doctor-title {
  padding: 0.2rem 0.6rem;
  background: #edf2f7;
  color: #4a5568;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.status-badge {
  padding: 0.35rem 0.8rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.waiting {
  background: #ffe7ba;
  color: #d46b08;
}

.status-badge.converted {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.cancelled {
  background: #f5f5f5;
  color: #8c8c8c;
}

.status-badge.expired {
  background: #fff1f0;
  color: #f5222d;
}

.arrow-icon {
  color: #cbd5e0;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.waitlist-record-card:hover .arrow-icon {
  color: #667eea;
  transform: translateX(4px);
}

.record-content {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.6rem 1rem;
}

.content-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.content-label {
  color: #a0aec0;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.content-value {
  color: #2d3748;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content-value.queue-position {
  color: #667eea;
  font-weight: 700;
}

.target-time-row,
.patient-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-top: 0.6rem;
  border-top: 1px solid #f0f4f8;
  color: #4a5568;
  font-size: 0.9rem;
}

.target-time-row svg,
.patient-row svg {
  color: #667eea;
  flex-shrink: 0;
}

.time-label,
.patient-label {
  color: #a0aec0;
  font-weight: 500;
}

.time-value,
.patient-value {
  color: #2d3748;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.8rem;
  padding-top: 0.8rem;
  border-top: 1px solid #f0f4f8;
}

.join-time {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #718096;
  font-size: 0.8rem;
}

.join-icon {
  font-size: 0.9rem;
}

.join-text {
  white-space: nowrap;
}

.status-tip {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.tip-icon {
  font-size: 0.9rem;
}

.status-tip.converted {
  color: #52c41a;
}

.status-tip.expired {
  color: #f5222d;
}

.status-tip.cancelled {
  color: #8c8c8c;
}

.status-tip.waiting {
  color: #ff9c6e;
}

@media (max-width: 640px) {
  .waitlist-record-card {
    padding: 1rem;
  }

  .record-header {
    margin-bottom: 0.8rem;
    gap: 0.5rem;
  }

  .content-grid {
    grid-template-columns: 1fr 1fr;
    gap: 0.5rem 0.8rem;
  }

  .doctor-name {
    font-size: 1rem;
  }

  .content-label {
    font-size: 0.7rem;
  }

  .content-value {
    font-size: 0.85rem;
  }

  .target-time-row,
  .patient-row {
    font-size: 0.85rem;
    flex-wrap: wrap;
  }

  .record-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .status-tip {
    font-size: 0.7rem;
  }
}
</style>