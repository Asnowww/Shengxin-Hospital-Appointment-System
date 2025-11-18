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
          <h3>个人中心</h3>
        </div>

        <nav class="sidebar-nav">
          <button 
            :class="['nav-item', { active: activeTab === 'info' }]" 
            @click="activeTab = 'info'">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人信息</span>
          </button>
          
          <button 
            :class="['nav-item', { active: activeTab === 'appointment' }]" 
            @click="activeTab = 'appointment'">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>我的预约</span>
          </button>
           <button 
            :class="['nav-item', { active: activeTab === 'waitlist' }]" 
            @click="activeTab = 'waitlist'">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>我的候补</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
    <transition name="fade" mode="out-in">
      <ProfileInfo 
        v-if="activeTab === 'info'" 
        key="info"
      />
      <ProfileAppointments 
        v-else-if="activeTab === 'appointment'"
        key="appointment"
      />
      <ProfileWaitList
        v-else-if="activeTab === 'waitlist'"
        key="waitlist"
      />
    </transition>
</main>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import ProfileInfo from '@/components/PersonalInfo.vue'
import ProfileAppointments from '@/components/MyAppointments.vue'
import ProfileWaitList from '@/components/MyWaitList.vue'

// tab 管理
const activeTab = ref('info')

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
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
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

.nav-item svg {
  flex-shrink: 0;
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

/* 响应式设计 */
@media (max-width: 1024px) {
  .profile-layout {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
    padding: 1rem;
    gap: 1rem;
  }

  .sidebar {
    width: 100%;
    position: static;
    padding: 1.5rem;
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    text-align: left;
    padding-bottom: 1rem;
  }

  .avatar {
    width: 60px;
    height: 60px;
    margin: 0;
  }

  .avatar svg {
    width: 32px;
    height: 32px;
  }

  .sidebar-header h3 {
    font-size: 1.1rem;
  }

  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem;
    min-width: 100px;
    text-align: center;
  }

  .nav-item span {
    font-size: 0.875rem;
  }

  .main-content {
    min-height: 400px;
  }
}
</style>