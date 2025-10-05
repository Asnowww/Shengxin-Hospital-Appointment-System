<template>
  <div class="doctor-card" @click="onCardClick">
    <div class="doctor-avatar">
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
    </div>

    <div class="doctor-info">
      <div class="doctor-header">
        <div class="name-title">
          <h3>{{ doctor.name }}</h3>
          <span :class="['title-badge', doctor.category]">{{ doctor.title }}</span>
        </div>
        <span :class="['category-badge', doctor.category]">{{ categoryLabel }}</span>
      </div>

      <p class="description">{{ doctor.description }}</p>

      <div class="doctor-footer">
        <div class="stats">
          <div class="stat-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <line x1="19" y1="8" x2="19" y2="14"></line>
              <line x1="22" y1="11" x2="16" y2="11"></line>
            </svg>
            <span>已服务 {{ doctor.patientCount || 0 }}+ 人</span>
          </div>

          <div v-if="doctor.goodRate" class="stat-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>好评率 {{ doctor.goodRate }}%</span>
          </div>
        </div>

        <button 
          @click.stop="handleAppointment"
          class="appointment-btn">
          预约挂号
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props：接收父组件传入的数据
const props = defineProps({
  doctor: { type: Object, required: true },
  getCategoryLabel: { type: Function, required: true },
  onAppointment: { type: Function, required: true }
})

// 计算分类标签文本
const categoryLabel = computed(() => props.getCategoryLabel(props.doctor.category))

// 点击预约按钮
function handleAppointment() {
  props.onAppointment(props.doctor)
}

// 整个卡片点击（可选，比如查看详情）
function onCardClick() {
  // 可添加逻辑：跳转医生详情页
}
</script>

<style scoped>
.doctor-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  gap: 1.25rem;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}
.doctor-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #e2e8f0;
  transform: translateY(-2px);
}
.doctor-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.doctor-avatar svg {
  color: #667eea;
}
.doctor-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.doctor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 1rem;
}
.name-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.name-title h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}
.title-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  background: #edf2f7;
  color: #4a5568;
}
.category-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}
.category-badge.normal {
  background: #e6f7ff;
  color: #1890ff;
}
.category-badge.expert {
  background: #fff7e6;
  color: #fa8c16;
}
.category-badge.special {
  background: #fff1f0;
  color: #f5222d;
}
.description {
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 1rem 0;
}
.doctor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #718096;
  font-size: 0.85rem;
}
.stat-item svg {
  color: #667eea;
}
.appointment-btn {
  padding: 0.625rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}
.appointment-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}
</style>
