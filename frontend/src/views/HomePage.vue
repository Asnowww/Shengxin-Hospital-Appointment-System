<template>
  <div class="home-container">
    <Navigation ref="navRef" />
    
    <!-- 提醒横幅 -->
    <div v-if="reminders.length > 0" class="reminders-container">
      <div 
        v-for="reminder in reminders" 
        :key="reminder.id"
        class="reminder-banner"
      >
        <span class="reminder-icon">📢</span>
        <span class="reminder-text">{{ reminder.message }}</span>
        <button 
          class="reminder-close"
          @click="dismissReminder(reminder.id)"
        >
          ×
        </button>
      </div>
    </div>

    <!-- Hero区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">欢迎使用圣心医院挂号预约系统</h1>
        <p class="hero-subtitle">专业医疗服务 · 守护您的健康</p>
        <div class="hero-actions">
          <button class="secondary-btn" @click="scrollToAbout">了解更多</button>
        </div>
      </div>
    </div>

    <!-- 医院介绍 -->
    <div class="about-section" ref="aboutSection">
      <div class="section-container">
        <div class="about-header">
          <h2 class="section-title">关于我们</h2>
          <div class="title-underline"></div>
        </div>
        
        <div class="about-content">
          <div class="about-text">
            <p class="intro-paragraph">
              我们是一家集医疗、教学、科研、预防、保健为一体的现代化综合性医院。
              医院始终坚持"以患者为中心"的服务理念，致力于为广大患者提供优质、高效、便捷的医疗服务。
            </p>
            <p class="intro-paragraph">
              医院拥有先进的医疗设备和经验丰富的医疗团队，涵盖内科、外科、妇产科、儿科等多个科室。
              我们不断引进国际先进的医疗技术和管理理念，为患者的健康保驾护航。
            </p>
          </div>

          <div class="features-grid">
            <div class="feature-card">
              <div class="feature-icon">🏥</div>
              <h3 class="feature-title">专业团队</h3>
              <p class="feature-desc">汇聚众多知名专家学者，提供专业的医疗服务</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">⚕️</div>
              <h3 class="feature-title">先进设备</h3>
              <p class="feature-desc">引进国际先进医疗设备，确保诊断准确性</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">❤️</div>
              <h3 class="feature-title">贴心服务</h3>
              <p class="feature-desc">以患者为中心，提供温馨舒适的就医环境</p>
            </div>
            <div class="feature-card">
              <div class="feature-icon">🔬</div>
              <h3 class="feature-title">科研创新</h3>
              <p class="feature-desc">持续开展医学研究，引领医疗技术发展</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速服务
    <div class="services-section">
      <div class="section-container">
        <div class="services-header">
          <h2 class="section-title">快速服务</h2>
          <div class="title-underline"></div>
        </div>
        
        <div class="services-grid">
          <div class="service-card" @click="handleService('/appointment')">
            <div class="service-icon">📅</div>
            <h3 class="service-title">在线预约</h3>
            <p class="service-desc">便捷的在线预约挂号服务</p>
          </div>
          <div class="service-card" @click="handleService('/departments')">
            <div class="service-icon">🏢</div>
            <h3 class="service-title">科室导航</h3>
            <p class="service-desc">查看医院科室与医生信息</p>
          </div>
          <div class="service-card" @click="handleService('/my-appointments')">
            <div class="service-icon">📋</div>
            <h3 class="service-title">我的预约</h3>
            <p class="service-desc">管理和查看预约记录</p>
          </div>
        </div>
      </div>
    </div> -->

    <!-- 联系方式 -->
    <div class="contact-section">
      <div class="section-container">
        <div class="contact-content">
          <div class="contact-item">
            <div class="contact-icon">📍</div>
            <div class="contact-info">
              <h4>医院地址</h4>
              <p>圣心医院1号</p>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">📞</div>
            <div class="contact-info">
              <h4>联系电话</h4>
              <p>00000000</p>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">⏰</div>
            <div class="contact-info">
              <h4>门诊时间</h4>
              <p>周一至周日 08:00-17:00</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navigation from '@/components/Navigation.vue'

const router = useRouter()
const isLoggedIn = ref(false)
const reminders = ref([])
const aboutSection = ref(null)

// 关闭提醒
const dismissReminder = (id) => {
  reminders.value = reminders.value.filter(r => r.id !== id)
}


// 滚动到介绍区域
const scrollToAbout = () => {
  if (aboutSection.value) {
    aboutSection.value.scrollIntoView({ behavior: 'smooth' })
  }
}



// onMounted(() => {
//   // 检查是否登录
//   const token = localStorage.getItem('token')
//   const userRole = localStorage.getItem('role')
//   isLoggedIn.value = !!token
  
//   // 如果是患者且已登录，检查是否已阅读挂号须知
//   if (isLoggedIn.value && userRole === 'PATIENT') {
//     const hasSeenNotice = sessionStorage.getItem('hasSeenNotice')
//     if (!hasSeenNotice) {
//       router.push('/patient/notice')
//     }
//   }
// })
</script>

<style scoped>
/* 核心颜色定义：医疗绿方案 */
:root {
  --primary-color: #00A78E; /* 主题绿 */
  --primary-hover: #008f7a;
  --secondary-color: #f0f9f8;
  --text-dark: #2c3e50;
  --text-light: #7f8c8d;
  --bg-light: #f4f7f6;
}

.home-container {
  min-height: 100vh;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  color: var(--text-dark);
}

/* 提醒横幅：使用柔和的警告色 */
.reminders-container {
  background: #fff9e6;
  border-bottom: 1px solid #ffeeba;
}

.reminder-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 10px 20px;
}

.reminder-text {
  font-size: 14px;
  color: #856404;
}

/* Hero区域：使用医疗绿渐变 */
.hero-section {
  height: 65vh;
  background: linear-gradient(
      rgba(0, 120, 167, 0.7), /* 顶部颜色：医疗绿，0.7透明度 */
      rgba(0, 0, 0, 0.5)      /* 底部颜色：深色，0.5透明度 */
    ),url('@/assets/images/hospital.jpeg'); /* 建议放一张模糊的背景图 */
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 167, 142, 0.9) 0%, rgba(46, 139, 87, 0.8) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 0 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 15px;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  font-weight: 300;
  letter-spacing: 1px;
}

.hero-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.primary-btn {
  background: white;
  color: #00A78E;
  padding: 14px 35px;
  border-radius: 50px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.primary-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.secondary-btn {
  background: transparent;
  color: white;
  padding: 14px 35px;
  border-radius: 50px;
  border: 2px solid rgba(255,255,255,0.8);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background: rgba(255,255,255,0.15);
}

/* 介绍部分 */
.about-section {
  padding: 80px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  font-size: 32px;
  text-align: center;
  margin-bottom: 10px;
}

.title-underline {
  width: 50px;
  height: 4px;
  background: #00A78E;
  margin: 0 auto 50px;
  border-radius: 10px;
}

.intro-paragraph {
  max-width: 800px;
  margin: 0 auto 60px;
  text-align: center;
  font-size: 18px;
  color: #555;
  line-height: 1.8;
}

/* 特色卡片优化 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 25px;
}

.feature-card {
  background: #fff;
  padding: 40px 25px;
  border-radius: 20px;
  text-align: center;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.feature-card:hover {
  border-color: #00A78E;
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0, 167, 142, 0.1);
}

.icon-wrapper {
  font-size: 45px;
  margin-bottom: 20px;
  background: #f0f9f8;
  width: 90px;
  height: 90px;
  line-height: 90px;
  border-radius: 50%;
  margin: 0 auto 20px;
  transition: transform 0.3s;
}

.feature-card:hover .icon-wrapper {
  transform: scale(1.1);
}

.feature-title {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.feature-desc {
  font-size: 14px;
  color: #777;
  line-height: 1.5;
}

/* 底部联系方式区 */
.contact-section {
  background: #f8fbfa;
  padding: 60px 0;
  border-top: 1px solid #edf2f1;
}

.contact-content {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 30px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.contact-circle {
  width: 50px;
  height: 50px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.contact-info h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.contact-info p {
  margin: 4px 0 0;
  color: #777;
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-title { font-size: 36px; }
}

@media (max-width: 640px) {
  .features-grid { grid-template-columns: 1fr; }
  .contact-content { flex-direction: column; align-items: flex-start; padding-left: 20px; }
  .hero-actions { flex-direction: column; }
}
</style>