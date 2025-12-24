<template>
  <Navigation ref="navRef" />

  <div class="page-container">
    <div class="content-wrapper" :style="{ paddingTop: navHeight + 'px' }">
      <form @submit.prevent="handleRegister">
        <h2>用户注册</h2>

        <div class="form-row">
          <label>身份 <span class="required">*</span></label>
          <select v-model="form.identityType" required>
            <option value="">请选择</option>
            <option value="teacher">教师</option>
            <option value="student">学生</option>
          </select>
          <span class="error" v-if="errors.identityType">{{ errors.identityType }}</span>
        </div>

        <div class="form-row">
          <label>姓名 <span class="required">*</span></label>
          <input v-model="form.username" type="text" required />
          <span class="error" v-if="errors.username">{{ errors.username }}</span>
        </div>

        <div class="form-row">
          <label>{{ form.identityType === 'teacher' ? '教工号' : '学号' }} <span class="required">*</span></label>
          <input v-model="form.patientAccount" type="text" required />
          <span class="error" v-if="errors.patientAccount">{{ errors.patientAccount }}</span>
        </div>

        <div class="form-row">
          <label>手机号码 <span class="required">*</span></label>
          <input v-model="form.phone" type="tel" required />
          <span class="error" v-if="errors.phone">{{ errors.phone }}</span>
        </div>

        <div class="form-row">
          <label>密码 <span class="required">*</span></label>
          <input v-model="form.password" type="password" required />
          <span class="error" v-if="errors.password">{{ errors.password }}</span>
        </div>

        <div class="form-row">
          <label>确认密码 <span class="required">*</span></label>
          <input v-model="form.confirmPassword" type="password" required />
          <span class="error" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
        </div>

        <div class="form-row">
          <label>电子邮箱 <span class="required">*</span></label>
          <div class="email-input-group">
            <input v-model="form.email" type="email" required />
            <button 
              type="button" 
              class="get-code-btn"
              @click="handleSendCode"
              :disabled="!isEmailValid || isSendingCode || codeSent"
            >
              {{ codeSent ? `已发送(${countdown}s)` : '获取验证码' }}
            </button>
          </div>
          <span class="error" v-if="errors.email">{{ errors.email }}</span>
        </div>

        <div class="form-row">
          <label>验证码 <span class="required">*</span></label>
          <input v-model="form.emailCode" type="text" placeholder="请输入邮箱中的验证码" required />
          <span class="error" v-if="errors.emailCode">{{ errors.emailCode }}</span>
        </div>

        <div class="form-row">
          <label>出生年月</label>
          <input v-model="form.birthDate" type="date" :max="today" />
          <span class="error" v-if="errors.birthDate">{{ errors.birthDate }}</span>
        </div>

        <div class="form-row">
          <label>性别</label>
          <select v-model="form.gender">
            <option value="">请选择</option>
            <option value="M">男</option>
            <option value="F">女</option>
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
          <input v-model="form.emergencyPhone" type="tel" />
          <span class="error" v-if="errors.emergencyPhone">{{ errors.emergencyPhone }}</span>
        </div>

        <div class="form-row">
          <label>身高 (cm)</label>
          <input v-model="form.height" type="number" min="0" />
          <span class="error" v-if="errors.height">{{ errors.height }}</span>
        </div>

        <div class="form-row">
          <label>体重 (kg)</label>
          <input v-model="form.weight" type="number" min="0" />
          <span class="error" v-if="errors.weight">{{ errors.weight }}</span>
        </div>

        <div class="form-row">
          <label>既往病史</label>
          <textarea v-model="form.medicalHistory"></textarea>
        </div>

        <button type="submit" class="submit-btn" :disabled="hasErrors">注册</button>

        <p class="switch">
          已有账号？
          <router-link to="/login/patient">去登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

/* ========= 正则表达式 & 初始数据 ========= */

const nameRegex = /^[\u4e00-\u9fa5A-Za-z·\s]{2,20}$/
const phoneRegex = /^(?:\+?86)?1[3-9]\d{9}$/
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const today = new Date().toISOString().split('T')[0] // 用于限制出生日期

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

const errors = reactive({}) // 存储校验错误信息

// 验证码相关状态
const isSendingCode = ref(false)
const codeSent = ref(false)
const countdown = ref(60)

// ... (导航栏高度管理部分保持不变) ...
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

/* ========= 实时校验 (Watchers) ========= */

// 身份校验
watch(() => form.identityType, v => {
  errors.identityType = v ? '' : '请选择身份'
}, { immediate: true })

// 姓名校验
watch(() => form.username, v => {
  errors.username = nameRegex.test(v) ? '' : '姓名需为 2–20 位中英文'
})

// 学号/教工号校验
watch(() => form.patientAccount, v => {
  errors.patientAccount =
    v.length >= 4 && v.length <= 20 ? '' : '长度需 4–20 位'
})

// 手机号校验
watch(() => form.phone, v => {
  errors.phone = phoneRegex.test(v) ? '' : '手机号格式错误'
})

// 密码校验
watch(() => form.password, v => {
  errors.password = passwordRegex.test(v)
    ? ''
    : '密码需 8–20 位，包含字母和数字'
  // 同时触发确认密码校验
  if (form.confirmPassword) {
    errors.confirmPassword = v === form.confirmPassword ? '' : '两次密码不一致'
  }
})

// 确认密码校验
watch(() => form.confirmPassword, v => {
  errors.confirmPassword =
    v === form.password ? '' : '两次密码不一致'
})

// 邮箱校验
watch(() => form.email, v => {
  errors.email = emailRegex.test(v) ? '' : '邮箱格式错误'
})

// 验证码校验
watch(() => form.emailCode, v => {
  errors.emailCode = v.length === 6 ? '' : '验证码长度为 6 位'
})

// 出生年月校验
watch(() => form.birthDate, v => {
  errors.birthDate =
    !v || new Date(v) <= new Date() ? '' : '出生日期不能晚于今天'
})

// 身高校验（非负整数）
watch(() => form.height, v => {
  if (v === '') {
    errors.height = ''
  } else if (!/^\d+$/.test(v)) {
    errors.height = '身高必须为整数'
  } else if (parseInt(v) <= 0) {
    errors.height = '身高不能小于等于0'
  } else {
    errors.height = ''
  }
})

// 体重校验（非负整数）
watch(() => form.weight, v => {
  if (v === '') {
    errors.weight = ''
  } else if (!/^\d+$/.test(v)) {
    errors.weight = '体重必须为整数'
  } else if (parseInt(v) <= 0) {
    errors.weight = '体重不能小于等于0'
  } else {
    errors.weight = ''
  }
})

// 紧急联系人电话校验
watch(() => form.emergencyPhone, v => {
  errors.emergencyPhone =
    !v || phoneRegex.test(v) ? '' : '手机号格式错误'
})

/* ========= 计算属性 ========= */

// 检查是否存在任何校验错误
const hasErrors = computed(() =>
  Object.values(errors).some(msg => msg)
)

// 判断邮箱是否有效，用于控制发送验证码按钮
const isEmailValid = computed(() =>
  emailRegex.test(form.email) && !errors.email
)

/* ========= 业务逻辑 ========= */

// 发送验证码
async function handleSendCode() {
  if (!isEmailValid.value) {
    alert('请先输入有效的邮箱地址')
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
      alert(response.data.message || '验证码已发送，请查收邮箱')
      
      // 倒计时
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          codeSent.value = false
          countdown.value = 60
        }
      }, 1000)
    } else {
      alert(response.data.message || '发送验证码失败')
    }
  } catch (error) {
    console.error(error)
    alert(error.response?.data?.message || '发送验证码请求失败')
  } finally {
    isSendingCode.value = false
  }
}

// 注册
async function handleRegister() {
  // 确保所有必填项都被触发校验
  // 简单地检查一次 form.字段 即可触发 watch 中的 immediate: true
  // 复杂的校验可以在提交前手动触发
  if (!form.identityType) errors.identityType = '请选择身份'
  if (!form.username) errors.username = '姓名不能为空'
  if (!form.phone) errors.phone = '手机号不能为空'
  if (!form.email) errors.email = '邮箱不能为空'
  if (!form.emailCode) errors.emailCode = '验证码不能为空'

  // 在提交前再次检查是否存在错误
  if (hasErrors.value) {
    alert('请先修正表单错误后重试')
    return
  }

  try {
    const response = await axios.post('/api/auth/register/patient', form)

    if (response.data.code === 200) {
      alert('注册成功！')
      window.location.href = '/login/patient'
    } else {
      alert(response.data.message || `注册失败，: ${response.data.code}`)
    }
  } catch (error) {
    if (error.response && error.response.data) {
      alert(error.response.data.message || `注册失败，状态码: ${error.response.status}`)
    } else if (error.request) {
      alert('请求未发送或网络错误，请检查网络')
    } else {
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

.error {
  /* 关键修改：占据整行空间，并排在最后 */
  flex-basis: 100%;
  order: 99; 
  margin-top: 4px;
  /* 调整 margin-left 使其与输入框左对齐 (label min-width 80px + margin-right 8px) */
  margin-left: 88px; 
  font-size: 12px;
  color: #f56c6c;
  line-height: 1.4;
}

@media (max-width: 600px) {
  .error {
    margin-left: 0;
  }
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
  flex-wrap: wrap;
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