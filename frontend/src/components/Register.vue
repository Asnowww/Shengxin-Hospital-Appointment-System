<!-- 格式校验：姓名为中文字符/英文字符，位数限制；手机号位数限制；密码复杂度要求；电子邮箱格式校验；出生年月不超过当前日期；身高体重不为负数；紧急联系人要求手机号码； -->
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
          <select v-model="form.identityType" required>
            <option value="">请选择</option>
            <option value="teacher">教师</option>
            <option value="student">学生</option>
          </select>
        </div>

        <!-- 必填：姓名 -->
        <div class="form-row">
          <label>姓名 <span class="required">*</span></label>
          <input v-model="form.username" type="text" required />
        </div>

        <!-- 必填：学号/教工号 -->
        <div class="form-row">
          <label>{{ form.identityType === 'teacher' ? '教工号' : '学号' }} <span class="required">*</span></label>
          <input v-model="form.patientAccount" type="text" required />
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

        <!-- 必填：邮箱 -->
        <div class="form-row">
          <label>电子邮箱 <span class="required">*</span></label>
          <div class="email-input-group">
            <input v-model="form.email" type="email" required />
            <button 
              type="button" 
              class="get-code-btn"
              @click="handleSendCode"
              :disabled="!form.email || isSendingCode || codeSent"
            >
              {{ codeSent ? `已发送(${countdown}s)` : '获取验证码' }}
            </button>
          </div>
        </div>

        <!-- 必填：验证码 -->
        <div class="form-row">
          <label>验证码 <span class="required">*</span></label>
          <input v-model="form.emailCode" type="text" placeholder="请输入邮箱中的验证码" required />
        </div>

        <!-- 可选信息 -->
        <div class="form-row">
          <label>出生年月</label>
          <input v-model="form.birthDate" type="date" />
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
          <label>紧急联系人电话</label>
          <input v-model="form.emergencyPhone" type="number" />
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
import axios from 'axios'

const form = reactive({
  identityType: '', 
  username: '',
  patientAccount: '', 
  phone: '', 
  password: '',
  confirmPassword: '',
  email: '',
  emailCode: '',
  birthDate: '', 
  gender: '', 
  address: '', 
  emergencyContact: '',
  emergencyPhone: '',
  height: '', 
  weight: '', 
  medicalHistory: ''
})

// 验证码相关状态
const isSendingCode = ref(false)
const codeSent = ref(false)
const countdown = ref(60)

// 导航栏高度管理
const navRef = ref(null)
const navHeight = ref(110)

// 更新导航栏高度
function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30
    document.documentElement.style.setProperty('--nav-height', height + 'px')
  }
}

// 监听窗口大小变化
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

// 发送验证码
async function handleSendCode() {
  if (!form.email) {
    alert('请先输入邮箱地址')
    return
  }

  isSendingCode.value = true
  try {
    const response = await axios.post('/api/auth/sendEmailCode', {
      email: form.email
    })

    if (response.data.code === 200) {
      codeSent.value = true
      countdown.value = 60
      alert(response.data.msg || '验证码已发送，请查收邮箱')
      
      // 倒计时
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          codeSent.value = false
          countdown.value = 60
        }
      }, 1000)
    } else if (response.data.code === 409) {
      alert(response.data.msg)
    } else if (response.data.code === 429) {
      alert(response.data.msg || '发送过于频繁，请稍后再试')
    } else {
      alert(response.data.msg || '发送验证码失败')
    }
  } catch (error) {
    console.error(error)
    alert(error.response?.data?.msg || '发送验证码请求失败')
  } finally {
    isSendingCode.value = false
  }
}

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    alert('两次密码不一致')
    return
  }

  if (!form.emailCode) {
    alert('请输入验证码')
    return
  }

 try {
  const response = await axios.post('/api/auth/register/patient', form)

  // 后端返回 200 code 表示成功
  if (response.data.code === 200) {
    alert('注册成功！')
    window.location.href = '/login/patient'
  } else {
    // 后端返回非 200 code
    alert(response.data.msg || `注册失败，错误码: ${response.data.code}`)
  }
} catch (error) {
  // 1. 请求已发出，但服务器返回错误状态码（4xx, 5xx）
  if (error.response && error.response.data) {
    alert(error.response.data.msg || `注册失败，状态码: ${error.response.status}`)
  } 
  // 2. 请求未发出或网络错误
  else if (error.request) {
    alert('请求未发送或网络错误，请检查网络')
  } 
  // 3. 其他异常
  else {
    alert(`注册异常: ${error.message}`)
  }

  console.error('注册失败详细信息:', error)
}

}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
  box-sizing: border-box;
}

.content-wrapper {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

form {
  width: 100%;
  max-width: 480px;
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

/* 邮箱输入组 */
.email-input-group {
  display: flex;
  gap: 8px;
  flex: 1;
  width: 100%;

  @media (max-width: 600px) {
    flex-direction: column;
  }
}

.email-input-group input {
  flex: 1;
  min-width: 0;
}

/* 获取验证码按钮 */
.get-code-btn {
  padding: 8px 16px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
  transition: background-color 0.3s ease;

  @media (max-width: 600px) {
    width: 100%;
  }
}

.get-code-btn:hover:not(:disabled) {
  background-color: #337ecc;
}

.get-code-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
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