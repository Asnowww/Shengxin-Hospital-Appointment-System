<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏导航 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="avatar">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>
          <h3>医生中心</h3>
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
          
          <router-link
            to="/doctor/schedules"
            class="nav-item"
            :class="{ active: $route.path === '/doctor/schedules' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>我的排班</span>
          </router-link>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="main-content">
        <transition name="fade" mode="out-in">
          <div v-if="activeTab === 'info'" key="info" class="doctor-info">
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
                <!-- 基本信息 -->
                <div class="form-group">
                  <label class="form-label">姓名</label>
                  <input 
                    v-model="profile.name" 
                    type="text" 
                    disabled
                    class="form-control"
                    placeholder="姓名" />
                </div>

                <div class="form-group">
                  <label class="form-label">工号</label>
                  <input 
                    v-model="profile.employeeId" 
                    type="text" 
                    disabled
                    class="form-control"
                    placeholder="工号" />
                </div>

                <div class="form-group">
                  <label class="form-label">
                    科室 <span class="required">*</span>
                  </label>
                  <select 
                    v-model="profile.department" 
                    :disabled="!isEditing"
                    :class="['form-control', { 'error': errors.department }]">
                    <option value="">请选择</option>
                    <option value="CARDIO">心内科</option>
                    <option value="GASTRO">消化内科</option>
                    <option value="RESPIR">呼吸内科</option>
                    <option value="ORTHO">骨科</option>
                    <option value="NEURO">神经外科</option>
                    <option value="GYNEC">妇科</option>
                    <option value="PEDIM">小儿内科</option>
                  </select>
                  <span v-if="errors.department" class="error-text">{{ errors.department }}</span>
                </div>

                <div class="form-group">
                  <label class="form-label">
                    职称 <span class="required">*</span>
                  </label>
                  <select 
                    v-model="profile.title" 
                    :disabled="!isEditing"
                    :class="['form-control', { 'error': errors.title }]">
                    <option value="">请选择</option>
                    <option value="主治医师">主治医师</option>
                    <option value="副主任医师">副主任医师</option>
                    <option value="主任医师">主任医师</option>
                  </select>
                  <span v-if="errors.title" class="error-text">{{ errors.title }}</span>
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
                  <label class="form-label">邮箱</label>
                  <input 
                    v-model="profile.email" 
                    type="email" 
                    :disabled="!isEditing"
                    class="form-control"
                    placeholder="请输入邮箱" />
                </div>

                <!-- 专业信息 -->
                <div class="form-group full-width">
                  <label class="form-label">
                    擅长领域 <span class="required">*</span>
                  </label>
                  <textarea 
                    v-model="profile.specialization" 
                    :disabled="!isEditing"
                    :class="['form-control textarea', { 'error': errors.specialization }]"
                    rows="3"
                    placeholder="请描述您的擅长领域（如：冠心病、高血压、心律失常等）"></textarea>
                  <span v-if="errors.specialization" class="error-text">{{ errors.specialization }}</span>
                </div>

                <div class="form-group full-width">
                  <label class="form-label">个人简介</label>
                  <textarea 
                    v-model="profile.description" 
                    :disabled="!isEditing"
                    class="form-control textarea"
                    rows="4"
                    placeholder="请输入个人简介（学历、工作经历、研究方向等）"></textarea>
                </div>

                <!-- 出诊信息 -->
                <div class="form-section full-width">
                  <h3 class="section-title">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"></circle>
                      <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    出诊时间
                  </h3>
                  <div class="schedule-grid">
                    <label 
                      v-for="day in weekDays" 
                      :key="day.value"
                      class="schedule-checkbox">
                      <input 
                        type="checkbox" 
                        :value="day.value"
                        v-model="profile.workingDays"
                        :disabled="!isEditing"
                      />
                      <span class="checkbox-label">{{ day.label }}</span>
                    </label>
                  </div>
                </div>

                <div class="form-group">
                  <label class="form-label">上班时间</label>
                  <input 
                    v-model="profile.workStartTime" 
                    type="time" 
                    :disabled="!isEditing"
                    class="form-control" />
                </div>

                <div class="form-group">
                  <label class="form-label">下班时间</label>
                  <input 
                    v-model="profile.workEndTime" 
                    type="time" 
                    :disabled="!isEditing"
                    class="form-control" />
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
          </div>
        </transition>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const activeTab = ref('info')
const isEditing = ref(false)
const originalProfile = ref({})

const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const profile = reactive({
  name: '',
  employeeId: '',
  department: '',
  title: '',
  phone: '',
  email: '',
  specialization: '',
  description: '',
  workingDays: [],
  workStartTime: '',
  workEndTime: '',
  status: '' // 记录信息审核状态
})

const errors = reactive({
  department: '',
  title: '',
  phone: '',
  specialization: ''
})

function startEdit() {
  isEditing.value = true
  originalProfile.value = JSON.parse(JSON.stringify(profile))
  clearErrors()
}

function cancelEdit() {
  Object.assign(profile, originalProfile.value)
  isEditing.value = false
  clearErrors()
}

function clearErrors() {
  errors.department = ''
  errors.title = ''
  errors.phone = ''
  errors.specialization = ''
}

function validateForm() {
  clearErrors()
  let isValid = true

  if (!profile.department) {
    errors.department = '请选择科室'
    isValid = false
  }

  if (!profile.title) {
    errors.title = '请选择职称'
    isValid = false
  }

  if (!profile.phone) {
    errors.phone = '请输入手机号'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(profile.phone)) {
    errors.phone = '请输入有效的手机号'
    isValid = false
  }

  if (!profile.specialization || profile.specialization.trim().length < 10) {
    errors.specialization = '请输入至少10个字符的擅长领域描述'
    isValid = false
  }

  return isValid
}

async function fetchProfile() {
  try {
    // 实际使用时替换为：
    // const { data } = await axios.get('/api/doctor/profile')
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const mockData = {
      name: '张明',
      employeeId: 'D2020001',
      department: 'CARDIO',
      title: '主任医师',
      phone: '13800138000',
      email: 'zhangming@hospital.com',
      specialization: '从事心血管内科临床工作30余年，擅长冠心病、高血压、心力衰竭的诊治，对心律失常有深入研究',
      description: '医学博士，博士生导师，曾在美国哈佛医学院进修学习，发表SCI论文20余篇',
      workingDays: [1, 2, 3, 4, 5],
      workStartTime: '08:00',
      workEndTime: '17:00',
      status: 'ACTIVE'
    }
    
    Object.assign(profile, mockData)
    originalProfile.value = JSON.parse(JSON.stringify(mockData))
  } catch (err) {
    console.error('获取医生信息失败', err)
  }
}

async function handleSave() {
  if (!validateForm()) {
    return
  }

  try {
    // 实际使用时替换为：
    // await axios.post('/api/doctor/profile/update', profile)
    
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 800))
    
    alert('保存成功！')
    isEditing.value = false
    originalProfile.value = JSON.parse(JSON.stringify(profile))
  } catch (err) {
    alert('保存失败！')
    console.error('保存失败', err)
  }
}

// 导航栏高度管理
function updateNavHeight() {
  if (navRef.value?.$el) {
    const height = navRef.value.$el.offsetHeight
    navHeight.value = height + 30
  }
}

function handleResize() {
  updateNavHeight()
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  window.addEventListener('resize', handleResize)
  fetchProfile()
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

.nav-item svg {
  flex-shrink: 0;
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

.doctor-info {
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

.form-group.full-width,
.form-section.full-width {
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
  min-height: 80px;
  font-family: inherit;
}

/* 表单分区 */
.form-section {
  margin-top: 1rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #2d3748;
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.section-title svg {
  color: #667eea;
}

/* 出诊时间选择 */
.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 0.75rem;
}

.schedule-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.schedule-checkbox:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.schedule-checkbox input[type="checkbox"] {
  cursor: pointer;
}

.schedule-checkbox input[type="checkbox"]:checked + .checkbox-label {
  color: #667eea;
  font-weight: 600;
}

.schedule-checkbox input[type="checkbox"]:disabled {
  cursor: not-allowed;
}

.checkbox-label {
  color: #4a5568;
  font-size: 0.9rem;
}

/* 按钮组 */
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

/* 排班视图 */
.schedule-view {
  padding: 2.5rem;
}

.placeholder {
  text-align: center;
  color: #a0aec0;
  font-size: 1rem;
  padding: 3rem;
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

/* 响应式 */
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

  .doctor-info {
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

  .schedule-grid {
    grid-template-columns: repeat(2, 1fr);
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