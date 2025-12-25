<template>
  <div class="appointment-record-card" @click="onCardClick">
    <div class="record-header">
      <div class="header-left">
        <h3 class="doctor-name">{{ record.doctorName }}</h3>
        <span class="doctor-title">{{ record.doctorTitle }}</span>
      </div>
      <div class="header-right">
        <span :class="['status-badge', record.status]">{{ getStatusLabel(record.status) }}</span>
        <svg class="arrow-icon" xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
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
          <span class="content-label">类型</span>
          <span class="content-value">{{ record.typeName }}</span>
        </div>
        <div class="content-item location-item">
          <span class="content-label">地点</span>
          <div class="location-row">
            <span class="content-value">{{ record.building }}</span>
            <button 
              class="nav-chip" 
              @click.stop="handleNavigate"
              type="button"
            >
              导航
            </button>
          </div>
        </div>
        <div class="content-item">
          <span class="content-label">费用</span>
          <span class="content-value fee">¥{{ record.feeFinal }}</span>
        </div>
      </div>
      

      <div class="time-row">
        <svg xmlns="https://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
        <span class="time-value">{{ record.appointmentTime }}</span>
      </div>
    </div>

    <div v-if="record.remarks" class="record-remark">
      <span class="remark-icon">💬</span>
      <span class="remark-text">{{ record.remarks }}</span>
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
      return value.doctorName && value.appointmentTime && value.status
    }
  },
  onRecordClick: {
    type: Function,
    default: null
  },
  onNavigate: {
    type: Function,
    default: null
  }
})

const statusMap = {
  'pending': '待支付',
  'booked': '待就诊',
  'confirmed': '已确认',
  'completed': '已完成',
  'cancelled': '已取消',
  'no-show': '未到诊',
  'pending_confirm': '待处理',
  'waiting_action': '待确认--预约取消！！'
}

function getStatusLabel(status) {
  return statusMap[status] || status
}

function onCardClick() {
  if (props.onRecordClick) {
    props.onRecordClick(props.record)
  }
}

function handleNavigate() {
  if (props.onNavigate) {
    props.onNavigate(props.record)
  }
}
</script>

<style scoped>
.appointment-record-card {
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

.appointment-record-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.appointment-record-card:hover::before {
  left: 100%;
}

.appointment-record-card:hover {
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

.status-badge.pending_confirm {
  background: #ffede6;
  color: #ff1818;
}
.status-badge.waiting_action {
  background: #f7d1ef;
  color: #540648;
}

.status-badge.pending {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.booked {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.confirmed {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.cancelled {
  background: #f5f5f5;
  color: #8c8c8c;
}

.status-badge.no-show {
  background: #fff1f0;
  color: #f5222d;
}

.arrow-icon {
  color: #cbd5e0;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.appointment-record-card:hover .arrow-icon {
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

.content-value.fee {
  color: #667eea;
  font-weight: 700;
  font-size: 0.95rem;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-chip {
  padding: 0.25rem 0.65rem;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #2563eb;
  font-weight: 700;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.nav-chip:hover {
  background: #e0ecff;
  border-color: #bfdbfe;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-top: 0.6rem;
  border-top: 1px solid #f0f4f8;
  color: #4a5568;
}

.time-row svg {
  color: #667eea;
  flex-shrink: 0;
}

.time-value {
  font-size: 0.9rem;
  font-weight: 500;
  color: #2d3748;
}

.record-remark {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 0.8rem;
  padding: 0.6rem 0.8rem;
  background: #f7fafc;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.remark-icon {
  flex-shrink: 0;
  font-size: 0.9rem;
}

.remark-text {
  color: #718096;
  font-size: 0.8rem;
  line-height: 1.4;
  word-break: break-word;
}

@media (max-width: 640px) {
  .appointment-record-card {
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
}
</style>
