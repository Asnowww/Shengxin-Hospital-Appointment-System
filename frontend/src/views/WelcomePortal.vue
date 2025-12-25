<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToLogin = (role) => {
  router.push(`/login/${role}`)
}
</script>

<template>
  <div class="welcome-container">
    <div class="glass-overlay">
      <!-- 欢迎区域 -->
      <header class="welcome-header">
        <h1 class="main-title">欢迎使用圣心医院智慧医疗平台</h1>
        <p class="sub-title">Welcome to Shengxin Hospital Appointment System</p>
      </header>

      <!-- 卡片区域 -->
      <main class="cards-container">
        <!-- 患者入口 -->
        <div class="role-card patient-card" @click="navigateToLogin('patient')">
          <div class="icon-wrapper">
            <!-- Heart/User Icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="role-icon">
              <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
            </svg>
          </div>
          <h2 class="role-title">患者端</h2>
          <p class="role-desc">便捷挂号 · 预约查看 · 个人健康档案</p>
          <button class="enter-btn">进入登录</button>
        </div>

        <!-- 医生入口 -->
        <div class="role-card doctor-card" @click="navigateToLogin('doctor')">
          <div class="icon-wrapper">
            <!-- Stethoscope-like / Cross Icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="role-icon">
              <path fill-rule="evenodd" d="M11.484 2.17a.75.75 0 011.032 0 11.209 11.209 0 007.877 3.08.75.75 0 01.722.515 12.74 12.74 0 01.635 3.985c0 5.942-4.064 10.933-9.563 12.348a.749.749 0 01-.374 0C6.314 20.683 2.25 15.692 2.25 9.75c0-1.39.223-2.73.635-3.985a.75.75 0 01.722-.516 11.209 11.209 0 007.877-3.08zM12 6.972a.75.75 0 01.75.75v2.06h2.06a.75.75 0 010 1.5h-2.06v2.06a.75.75 0 01-1.5 0v-2.06H9.19a.75.75 0 010-1.5h2.06V7.722a.75.75 0 01.75-.75z" clip-rule="evenodd" />
            </svg>
          </div>
          <h2 class="role-title">医生端</h2>
          <p class="role-desc">排班管理 · 接诊处理 · 患者病历查看</p>
          <button class="enter-btn">进入登录</button>
        </div>

        <!-- 管理员入口 -->
        <div class="role-card admin-card" @click="navigateToLogin('admin')">
          <div class="icon-wrapper">
            <!-- Cog/Gear Icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="role-icon">
              <path fill-rule="evenodd" d="M11.078 2.25c-.917 0-1.699.663-1.85 1.567L9.05 4.889c-.02.12-.115.26-.297.348a7.493 7.493 0 00-.986.57c-.166.115-.334.126-.45.083L6.3 5.508a1.875 1.875 0 00-2.282.819l-.922 1.597a1.875 1.875 0 00.432 2.385l.84.692c.095.078.17.229.154.43a7.598 7.598 0 000 1.139c.015.2-.059.352-.153.43l-.841.692a1.875 1.875 0 00-.432 2.385l.922 1.597a1.875 1.875 0 002.282.818l1.019-.382c.115-.043.283-.031.45.082.312.214.641.405.985.57.182.088.277.228.297.35l.178 1.071c.151.904.933 1.567 1.85 1.567h1.844c.916 0 1.699-.663 1.85-1.567l.178-1.072c.02-.12.114-.26.297-.349.344-.165.673-.356.985-.57.167-.114.335-.125.45-.082l1.02.382a1.875 1.875 0 002.28-.819l.923-1.597a1.875 1.875 0 00-.432-2.385l-.84-.692c-.095-.078-.17-.229-.154-.43a7.614 7.614 0 000-1.139c-.016-.2.059-.352.153-.43l.84-.692c.708-.582.891-1.59.433-2.385l-.922-1.597a1.875 1.875 0 00-2.282-.818l-1.02.382c-.114.043-.282.031-.449-.083a7.49 7.49 0 00-.985-.57c-.183-.087-.277-.227-.297-.348l-.179-1.072a1.875 1.875 0 00-1.85-1.567h-1.843zM12 15.75a3.75 3.75 0 100-7.5 3.75 3.75 0 000 7.5z" clip-rule="evenodd" />
            </svg>
          </div>
          <h2 class="role-title">管理员端</h2>
          <p class="role-desc">系统维护 · 审计监控 · 全局配置管理</p>
          <button class="enter-btn">进入登录</button>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 全局容器：使用固定背景，确保看起来干净 */
.welcome-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* System White Background fallback if global bg fails, 
     but App.vue has a bg-image with opacity. 
     We'll make this transparent to let the App.vue bg show through, 
     or give it a semi-transparent white for readability. */
  background: transparent; 
  padding: 20px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* 玻璃拟态叠加层（可选），增加层次感 */
.glass-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 1200px;
}

/* 标题区域 */
.welcome-header {
  text-align: center;
  margin-bottom: 60px;
  animation: fadeInDown 0.8s ease-out;
}

.main-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.sub-title {
  font-size: 1.2rem;
  color: #666;
  font-weight: 300;
}

/* 卡片容器 */
.cards-container {
  display: flex;
  justify-content: center;
  gap: 30px;
  width: 100%;
  flex-wrap: wrap; 
  animation: fadeInUp 1s ease-out;
}

/* 单个卡片样式 */
.role-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px; /* 大圆角 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08); /* shadow-sm / soft shadow */
  width: 300px;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}

/* 卡片悬停效果 */
.role-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(255, 117, 140, 0.25); /* 悬停时带有主题色光晕 */
  border-color: #ff758c;
}

/* 图标包装 */
.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 25px;
  transition: background 0.3s ease;
}

.role-card:hover .icon-wrapper {
  background: linear-gradient(135deg, #ff758c 0%, #ff7eb3 100%);
}

.role-icon {
  width: 40px;
  height: 40px;
  color: #555;
  transition: color 0.3s ease;
}

.role-card:hover .role-icon {
  color: white;
}

/* 角色标题 */
.role-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

/* 描述文本 */
.role-desc {
  font-size: 0.95rem;
  color: #888;
  margin-bottom: 30px;
  line-height: 1.5;
  flex-grow: 1; /* 保证按钮位置对齐 */
}

/* 按钮样式 */
.enter-btn {
  background: linear-gradient(90deg, #ff758c 0%, #ff7eb3 100%);
  border: none;
  border-radius: 30px; /* 胶囊按钮 */
  color: white;
  padding: 12px 35px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 117, 140, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}

.enter-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(255, 117, 140, 0.6);
}

.enter-btn:active {
  transform: scale(0.98);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .welcome-header {
    margin-bottom: 40px;
  }
  
  .main-title {
    font-size: 1.8rem;
  }
  
  .cards-container {
    flex-direction: column;
    align-items: center;
  }
  
  .role-card {
    width: 90%;
    max-width: 340px;
  }
}

/* 动画定义 */
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
