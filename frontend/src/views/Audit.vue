<template>
  <Navigation ref="navRef" />
  <div class="page-container">
    <div class="profile-layout" :style="{ paddingTop: navHeight + 'px' }">
      <!-- 左侧边栏导航 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="avatar">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>
          <h3>管理员审核</h3>
        </div>

        <nav class="sidebar-nav">
          <button 
            :class="['nav-item', { active: activeTab === 'identity' }]" 
            @click="activeTab = 'identity'">
            <span>身份认证审核</span>
          </button>
          
          <!-- <button 
            :class="['nav-item', { active: activeTab === 'leave' }]" 
            @click="activeTab = 'leave'">
            <span>请假审核</span>
          </button> -->
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <transition name="fade" mode="out-in">
          <IdentityAudit v-if="activeTab === 'identity'" key="identity" />
          <!-- <LeaveAudit v-else-if="activeTab === 'leave'" key="leave" /> -->
        </transition>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import IdentityAudit from '@/components/IdentityAudit.vue'
// import LeaveAudit from '@/components/LeaveAudit.vue'

const activeTab = ref('identity')

// 导航栏高度管理
const navRef = ref(null)
const navHeight = ref(110)

function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30
    document.documentElement.style.setProperty('--nav-height', height + 'px')
  }
}

function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
}

.profile-layout {
  display: flex;
  min-height: calc(100vh - 80px);
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 2rem;
  height: fit-content;
  position: sticky;
  top: calc(var(--nav-height, 110px) + 2rem);
  flex-shrink: 0;
}

.sidebar-header {
  text-align: center;
  padding-bottom: 2rem;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 1.5rem;
}

.avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.avatar svg {
  color: white;
}

.sidebar-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
  font-size: 1rem;
  font-weight: 500;
  text-align: left;
  width: 100%;
}

.nav-item:hover {
  background: #f7fafc;
  color: #667eea;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.nav-item span {
  flex: 1;
}

/* 右侧主内容区 */
.main-content {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  min-height: 600px;
  overflow: hidden;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
