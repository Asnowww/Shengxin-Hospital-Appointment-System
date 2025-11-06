<template>
  <div class="profile-info">
    <div class="info-header">
      <h2>个人信息</h2>
      <button 
        v-if="!isEditing" 
        @click="startEdit" 
        class="edit-btn"
        type="button">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
        </svg>
        修改
      </button>
    </div>

    <form @submit.prevent="handleSave">
      <div class="form-grid">
        
        <div class="form-group">
          <label class="form-label">身份</label>
          <input
            :value="identityText"
            type="text"
            disabled
            class="form-control"
          />
        </div>

        <!-- 身份认证状态 -->
        <div class="form-group">
          <label class="form-label">身份认证状态</label>
          <div class="status-wrapper">
            <input
              :value="statusText"
              type="text"
              disabled
              class="form-control"
              :class="statusClass"
            />

            <button 
              v-if="profile.status === 'unverified' || profile.status === 'rejected'" 
              @click="showVerifyModal = true" 
              class="verify-btn"
              type="button">
              去认证
            </button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">姓名</label>
          <input 
            v-model="profile.username" 
            type="text" 
            disabled
            class="form-control" />
        </div>

<div class="form-group">
          <label class="form-label">学号/教工号</label>
          <input 
            v-model="profile.patientAccount" 
            type="text" 
            disabled
            class="form-control" />
        </div>
        <div class="form-group">
          <label class="form-label">
            手机号 <span class="required">*</span>
          </label>
          <input 
            v-model="profile.phone" 
            type="tel" 
            :disabled="!isEditing"
            :class="['form-control', { 'error': errors.phone }]"
            placeholder="请输入手机号" />
          <span v-if="errors.phone" class="error-text">{{ errors.phone }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">出生年月</label>
          <input 
            v-model="profile.birthDate" 
            type="date" 
            :disabled="!isEditing"
            class="form-control" />
        </div>

        <div class="form-group">
          <label class="form-label">性别</label>
          <select 
            v-model="profile.gender" 
            :disabled="!isEditing"
            class="form-control">
            <option value="">请选择</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>

        <div class="form-group full-width">
          <label class="form-label">家庭地址</label>
          <input 
            v-model="profile.address" 
            type="text" 
            :disabled="!isEditing"
            class="form-control"
            placeholder="请输入家庭地址" />
        </div>

        <div class="form-group">
          <label class="form-label">紧急联系人</label>
          <input 
            v-model="profile.emergencyContact" 
            type="text" 
            :disabled="!isEditing"
            class="form-control"
            placeholder="请输入紧急联系人姓名" />
        </div>

        <div class="form-group">
          <label class="form-label">紧急联系电话</label>
          <input 
            v-model="profile.emergencyPhone" 
            type="tel" 
            :disabled="!isEditing"
            class="form-control"
            placeholder="请输入紧急联系电话" />
        </div>

        <div class="form-group">
          <label class="form-label">身高 (cm)</label>
          <input 
            v-model="profile.height" 
            type="number" 
            :disabled="!isEditing"
            class="form-control"
            placeholder="请输入身高" />
        </div>

        <div class="form-group">
          <label class="form-label">体重 (kg)</label>
          <input 
            v-model="profile.weight" 
            type="number" 
            :disabled="!isEditing"
            class="form-control"
            placeholder="请输入体重" />
        </div>

        <div class="form-group full-width">
          <label class="form-label">既往病史</label>
          <textarea 
            v-model="profile.medicalHistory" 
            :disabled="!isEditing"
            class="form-control textarea"
            rows="4"
            placeholder="请输入既往病史（选填）"></textarea>
        </div>
      </div>

      <div v-if="isEditing" class="button-group">
        <button type="button" @click="cancelEdit" class="cancel-btn">
          取消
        </button>
        <button type="submit" class="submit-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          保存修改
        </button>
      </div>
    </form>

    <!-- 身份认证弹窗 -->
    <Verify 
      :visible="showVerifyModal"
      @update:visible="showVerifyModal = $event"
      @success="handleVerifySuccess"
      @cancel="showVerifyModal = false"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Verify from '@/views/Verify.vue'

const router = useRouter()
const isEditing = ref(false)
const originalProfile = ref({})
const showVerifyModal = ref(false)

const profile = reactive({
  identityType: '',
  username: '',
  patientAccount: '',
  status: '',
  birthDate: '',
  gender: '',
  address: '',
  phone: '',
  emergencyContact: '',
  emergencyPhone: '',
  height: '',
  weight: '',
  medicalHistory: ''
})

//身份映射
const identityMap = {
  teacher: '教师',
  student: '学生',
}
const identityText = computed(() => identityMap[profile.identityType] || '')

//状态映射
const statusMap = {
  unverified: '未认证',
  pending: '认证中',
  verified: '已认证',
  rejected: '审核失败'
}
const statusText = computed(() => statusMap[profile.status] || '未知')

const statusClass = computed(() => {
  switch (profile.status) {
    case 'inactive': return 'status-inactive'
    case 'active': return 'status-active'
    default: return ''
  }
})

const errors = reactive({
  identityType: '',
  phone: ''
})

function startEdit() {
  isEditing.value = true
  originalProfile.value = { ...profile }
  clearErrors()
}

function cancelEdit() {
  Object.assign(profile, originalProfile.value)
  isEditing.value = false
  clearErrors()
}

function clearErrors() {
  errors.identityType = ''
  errors.phone = ''
}

function validateForm() {
  clearErrors()
  let isValid = true

  if (!profile.phone) {
    errors.phone = '请输入手机号'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(profile.phone)) {
    errors.phone = '请输入有效的手机号'
    isValid = false
  }

  return isValid
}

// 自动带 token 获取个人信息
async function fetchProfile() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('未登录或登录已过期')
      router.push('/login/patient')
      return
    }

    const res = await axios.get('/api/patient/profile', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (res.data.code === 200) {
      const data = res.data.data
      Object.assign(profile, data)
      originalProfile.value = { ...data }
      localStorage.setItem('patientAccount', data.patientAccount)
    } else {
      alert(res.data.msg || '获取个人信息失败')
    }

  } catch (err) {
    console.error('获取个人信息失败', err)
    if (err.response?.status === 401) {
      alert('登录已过期，请重新登录')
      router.push('/login/patient')
    } else {
      alert('获取个人信息失败，请稍后再试')
    }
  }
}

// 保存修改
async function handleSave() {
  if (!validateForm()) return

  try {
    const token = localStorage.getItem('token')
    await axios.put('/api/patient/profile/update', profile, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    alert('保存成功！')
    isEditing.value = false
  } catch (err) {
    console.error(err)
    alert('保存失败，请稍后再试')
  }
}

// 认证成功回调
function handleVerifySuccess() {
  showVerifyModal.value = false
  // 重新获取个人信息以更新认证状态
  fetchProfile()
}

// 页面加载时自动获取个人信息
onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-info {
  padding: 2.5rem;
  height: 100%;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

h2 {
  margin: 0;
  color: #2d3748;
  font-size: 1.75rem;
  font-weight: 600;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 500;
  color: #4a5568;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-control {
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
}

.form-control:not(:disabled):hover {
  border-color: #cbd5e0;
}

.form-control:not(:disabled):focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-control:disabled {
  background: #f7fafc;
  color: #4a5568;
  cursor: not-allowed;
  border-color: #e2e8f0;
}

.form-control.error {
  border-color: #e53e3e;
}

.error-text {
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
}

.cancel-btn:hover {
  background: #cbd5e0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.status-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.form-control.status-inactive {
  border-color: #e53e3e;
  color: #e53e3e;
}

.form-control.status-active {
  border-color: #38a169;
  color: #38a169;
}

.verify-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  background-color: #e53e3e;
  color: white;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.verify-btn:hover {
  background-color: #c53030;
}

@media (max-width: 768px) {
  .profile-info {
    padding: 1.5rem;
  }

  .info-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .edit-btn {
    width: 100%;
    justify-content: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .button-group {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>