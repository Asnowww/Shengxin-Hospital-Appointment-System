<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="profile-layout">
      <!-- 左侧边栏 -->
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

      <!-- 右侧 -->
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
                修改
              </button>
            </div>

            <form @submit.prevent="handleSave">
              <div class="form-grid">

                <!-- 基本信息（后端不可改 → disabled） -->
                <div class="form-group">
                  <label>姓名</label>
                  <input v-model="profile.doctorName" type="text" class="form-control" disabled />
                </div>

                <div class="form-group">
                  <label>工号</label>
                  <input v-model="profile.doctorId" type="text" class="form-control" disabled />
                </div>

                <div class="form-group">
                  <label>科室</label>
                  <input v-model="profile.deptName" type="text" disabled class="form-control" />
                </div>

                <div class="form-group">
                  <label>职称</label>
                  <input v-model="profile.title" type="text" disabled class="form-control" />
                </div>

                <!-- 电话（可编辑字段） -->
                <div class="form-group">
                  <label>手机号 *</label>
                  <input 
                    v-model="profile.phone" 
                    type="tel"
                    :disabled="!isEditing"
                    class="form-control" 
                  />
                </div>

                <!-- 邮箱（可编辑） -->
                <div class="form-group">
                  <label>邮箱</label>
                  <input 
                    v-model="profile.email" 
                    type="email"
                    :disabled="!isEditing"
                    class="form-control" 
                  />
                </div>

                <!-- <div class="form-group full-width">
                  <label>擅长领域</label>
                  <textarea 
                    v-model="profile.specialization" 
                    disabled
                    class="form-control textarea"
                    rows="3"
                  ></textarea>
                </div> -->

                <div class="form-group full-width">
                  <label>擅长领域</label>
                  <textarea 
                    v-model="profile.bio"
                    disabled
                    class="form-control textarea"
                    rows="4"
                  ></textarea>
                </div>

              </div>

              <div v-if="isEditing" class="button-group">
                <button type="button" @click="cancelEdit" class="cancel-btn">取消</button>
                <button type="submit" class="submit-btn">保存修改</button>
              </div>
            </form>
          </div>
        </transition>
      </main>
    </div>
  </div>
</template>


<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import Navigation from '@/components/Navigation.vue'
import axios from 'axios'

const navRef = ref(null)
const navHeight = ref(110)
const activeTab = ref('info')
const isEditing = ref(false)
const originalProfile = ref({})

const profile = reactive({
  doctorName: '',
  doctorId: '',
  deptName: '',
  title: '',
  phone: '',
  email: '',
  bio:'',
  status: ''
})

function startEdit() {
  isEditing.value = true
  originalProfile.value = JSON.parse(JSON.stringify(profile))
}

function cancelEdit() {
  Object.assign(profile, originalProfile.value)
  isEditing.value = false
}

async function fetchProfile() {
  const id = localStorage.getItem('userId')
  try {
    const resp = await axios.get(`/api/doctor/${id}`)
    Object.assign(profile, resp.data)
  } catch (err) {
    console.error('获取医生信息失败', err)
  }
}

async function handleSave() {
  try {
    const id = localStorage.getItem('userId')
    await axios.put(`/api/doctor/${id}/update-contact`, {
      phone: profile.phone,
      email: profile.email
    })

    alert('保存成功')
    isEditing.value = false
  } catch (err) {
    alert('保存失败')
    console.error(err)
  }
}

function updateNavHeight() {
  if (navRef.value?.$el) {
    navHeight.value = navRef.value.$el.offsetHeight + 30
  }
}

onMounted(async () => {
  await nextTick()
  updateNavHeight()
  fetchProfile()
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