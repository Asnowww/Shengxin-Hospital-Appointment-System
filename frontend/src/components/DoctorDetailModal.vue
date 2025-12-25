<template>
  <transition name="modal-fade">
    <div v-if="modelValue" class="modal-overlay" @click.self="close">
      <div class="modal-content doctor-detail-card">
        <!-- 头部渐变背景 -->
        <div class="modal-header-hero">
          <button class="close-btn-ghost" @click="close">
            <svg xmlns="https://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
          
          <div class="hero-content">
            <div class="avatar-wrapper">
              <div class="avatar-placeholder">
                {{ doctor?.name?.charAt(0) || 'D' }}
              </div>
            </div>
            <div class="hero-text">
              <h2>{{ doctor?.name }}</h2>
              <div class="hero-badges">
                <span class="title-tag">{{ doctor?.title }}</span>
                <span class="gender-tag" :class="doctor?.gender">
                  {{ doctor?.gender === 'M' ? '男' : '女' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-body-content">
          <div class="info-grid">
            <!-- 职业信息卡片 -->
            <div class="info-card full-width">
              <div class="card-title">
                <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><polyline points="17 11 19 13 23 9"></polyline></svg>
                执业信息
              </div>
              <div class="card-row">
                <span class="label">工号</span>
                <span class="value">{{ doctor?.id }}</span>
              </div>
              <div class="card-row">
                <span class="label">科室</span>
                <span class="value">{{ doctor?.departmentName }}</span>
              </div>
              <div class="card-row">
                <span class="label">职称</span>
                <span class="value">{{ doctor?.title }}</span>
              </div>
              <div class="card-row">
                <span class="label">当前状态</span>
                <span class="status-dot" :class="doctor?.status">
                  {{ getStatusLabel(doctor?.status) }}
                </span>
              </div>
            </div>

            <!-- 联系方式卡片 -->
            <div class="info-card full-width">
              <div class="card-title">
                <svg xmlns="https://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l2.31-2.31a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                联系方式
              </div>
              <div class="card-row">
                <span class="label">手机号码</span>
                <span class="value">{{ doctor?.phone || '未填写' }}</span>
              </div>
              <div class="card-row">
                <span class="label">电子邮箱</span>
                <span class="value">{{ doctor?.email || '未填写' }}</span>
              </div>
            </div>
          </div>

          <!-- 擅长领域 -->
          <div class="info-section">
            <h4 class="section-title">擅长领域</h4>
            <div class="bio-content">
              {{ doctor?.bio || '该医生非常神秘，暂不愿透露。' }}
            </div>
          </div>
        </div>

        <div class="modal-footer-minimal">
          <button class="btn-close-main" @click="close">关闭</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  doctor: Object
})

const emit = defineEmits(['update:modelValue'])

const close = () => {
  emit('update:modelValue', false)
}

const specialtyList = computed(() => {
  if (!props.doctor?.specialty) return []
  return props.doctor.specialty.split(/[，,]/).filter(s => s.trim())
})

const getStatusLabel = (status) => {
  const labels = {
    'active': '在职',
    'on-leave': '休假',
    'retired': '退休'
  }
  return labels[status] || '未知'
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.doctor-detail-card {
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 580px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header-hero {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 3rem 2rem 2rem;
  position: relative;
  color: white;
}

.close-btn-ghost {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn-ghost:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.25);
  border: 4px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.hero-text h2 {
  margin: 0 0 0.5rem;
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.hero-badges {
  display: flex;
  gap: 0.75rem;
}

.title-tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.gender-tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
}

.modal-body-content {
  padding: 2rem;
  max-height: 50vh;
  overflow-y: auto;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-card.full-width {
  grid-column: span 2;
}

.info-card {
  background: #f8fafc;
  padding: 1.25rem;
  border-radius: 16px;
  border: 1px solid #f1f5f9;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 1rem;
}

.card-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.card-row:last-child { margin-bottom: 0; }

.label { color: #94a3b8; font-size: 0.85rem; flex-shrink: 0; }
.value { color: #1e293b; font-weight: 600; word-break: break-all; text-align: right; }
.value.highlight { color: #f5576c; }

.status-dot {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
}

.status-dot::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active { color: #10b981; }
.status-dot.active::before { background: #10b981; box-shadow: 0 0 8px rgba(16, 185, 129, 0.4); }
.status-dot.on-leave { color: #f59e0b; }
.status-dot.on-leave::before { background: #f59e0b; }
.status-dot.retired { color: #ef4444; }
.status-dot.retired::before { background: #ef4444; }

.info-section {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
}

.specialty-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  background: white;
  border: 2px solid #f1f5f9;
  padding: 0.4rem 0.8rem;
  border-radius: 10px;
  font-size: 0.85rem;
  color: #475569;
  font-weight: 500;
}

.bio-content {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 12px;
  color: #64748b;
  font-size: 0.9rem;
  line-height: 1.6;
}

.modal-footer-minimal {
  padding: 1.5rem 2rem;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: center;
}

.btn-close-main {
  width: 100%;
  padding: 0.75rem;
  background: #f1f5f9;
  border: none;
  border-radius: 12px;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close-main:hover {
  background: #e2e8f0;
  color: #1e293b;
}

/* 动画 */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-content {
  transform: scale(0.95) translateY(20px);
}

.modal-fade-leave-to .modal-content {
  transform: scale(0.95) translateY(20px);
}

@media (max-width: 640px) {
  .info-grid { grid-template-columns: 1fr; }
  .modal-header-hero { padding: 2rem 1.5rem 1.5rem; }
  .hero-content { flex-direction: column; text-align: center; }
}
</style>
