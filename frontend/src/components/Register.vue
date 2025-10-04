<template>
     <Navigation ref="navRef" />
  <div class="page-container">
  
 

    <!-- 内容区域 -->
    <div class="content-wrapper" :style="{ paddingTop: navHeight + 'px' }">
      <form @submit.prevent="handleRegister">
        <h2>用户注册</h2>

        <!-- 必填：身份 -->
        <div class="form-row">
          <label>身份 <span class="required">*</span></label>
          <select v-model="form.role" required>
            <option value="">请选择</option>
            <option value="teacher">教师</option>
            <option value="student">学生</option>
          </select>
        </div>

        <!-- 必填：学号/教工号 -->
        <div class="form-row">
          <label>{{ form.role === 'teacher' ? '教工号' : '学号' }} <span class="required">*</span></label>
          <input v-model="form.userId" type="text" required />
        </div>

        <!-- 必填：手机号 -->
        <div class="form-row">
          <label>手机号码 <span class="required">*</span></label>
          <input v-model="form.phone" type="tel" required />
        </div>

        <!-- 必填：密码 -->
        <div class="form-row">
          <label>密码 <span class="required">*</span></label>
          <input v-model="form.password" type="password" required />
        </div>

        <!-- 必填：确认密码 -->
        <div class="form-row">
          <label>确认密码 <span class="required">*</span></label>
          <input v-model="form.confirmPassword" type="password" required />
        </div>

        <!-- 可选信息 -->
        <div class="form-row">
          <label>出生年月</label>
          <input v-model="form.birth" type="date" />
        </div>

        <div class="form-row">
          <label>性别</label>
          <select v-model="form.gender">
            <option value="">请选择</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>

        <div class="form-row">
          <label>家庭地址</label>
          <input v-model="form.address" type="text" />
        </div>

        <div class="form-row">
          <label>紧急联系人</label>
          <input v-model="form.emergencyContact" type="text" />
        </div>

        <div class="form-row">
          <label>身高 (cm)</label>
          <input v-model="form.height" type="number" />
        </div>

        <div class="form-row">
          <label>体重 (kg)</label>
          <input v-model="form.weight" type="number" />
        </div>

        <div class="form-row">
          <label>既往病史</label>
          <textarea v-model="form.medicalHistory"></textarea>
        </div>

        <button type="submit" class="submit-btn">注册</button>

        <p class="switch">
          已有账号？
          <router-link to="/login/patient">去登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'

const form = reactive({
  role: '', userId: '', phone: '', password: '', confirmPassword: '',
  birth: '', gender: '', address: '', emergencyContact: '',
  height: '', weight: '', medicalHistory: ''
})

// 导航栏高度管理
const navRef = ref(null)
const navHeight = ref(110) // 默认值，稍微大一点作为安全值

// 更新导航栏高度
function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30 // 添加30px缓冲空间
    // 同时更新CSS变量，供样式使用
    document.documentElement.style.setProperty('--nav-height', height + 'px')
  }
}

// 监听窗口大小变化
function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  // 确保组件完全渲染后获取高度
  await nextTick()
  updateNavHeight()
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

function handleRegister() {
  if (form.password !== form.confirmPassword) return alert('两次密码不一致')
  // 此处将注册信息通过接口发送给后端
  console.log('注册信息', form)
  alert('注册成功！')
}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;   /* 水平居中 */
  align-items: center;       /* 垂直居中 */
  min-height: calc(100vh - 80px); /* 留出导航栏高度 (80px和导航栏保持一致) */
  box-sizing: border-box;
}

.content-wrapper {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}



form {
  width: 100%;
  max-width: 480px;
  /* 移除margin-top，因为现在使用居中布局 */
}

h2 { 
  text-align: center; 
  margin-bottom: 24px; 
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  
  @media (max-width: 600px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

label { 
  font-weight: 500; 
  margin-right: 8px;
  min-width: 80px;
  
  @media (max-width: 600px) {
    min-width: auto;
    margin-right: 0;
  }
}

.required {
  color: #f56c6c;
}

input, select, textarea {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  
  @media (max-width: 600px) {
    width: 100%;
    box-sizing: border-box;
  }
}

textarea { 
  min-height: 60px; 
  resize: vertical; 
  font-family: inherit;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  font-size: 16px;
  margin-top: 20px;
  transition: background-color 0.3s ease;
}

.submit-btn:hover {
  background-color: #337ecc;
}

.switch {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

.switch a { 
  color: #409eff; 
  text-decoration: none; 
}

.switch a:hover {
  text-decoration: underline;
}

/* 小屏幕适配 */
@media (max-width: 480px) {
  .content-wrapper {
    padding-left: 15px;
    padding-right: 15px;
    padding-bottom: 30px;
  }
  
  form {
    max-width: 100%;
  }
  
  h2 {
    font-size: 20px;
  }
  
  .form-row {
    margin-bottom: 16px;
  }
  
  input, select, textarea {
    padding: 10px 12px;
  }
}
</style>