<template>
  <transition name="modal-fade">
    <div v-if="modelValue" class="modal-overlay" @click="handleClose">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑医生信息</h3>
          <button class="close-btn" @click="handleClose">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <!-- 不可编辑的信息 -->
          <div class="readonly-section">
            <div class="form-group">
              <label class="form-label">工号</label>
              <input type="text" class="form-input readonly" :value="formData.id" readonly />
            </div>
            <div class="form-group">
              <label class="form-label">姓名</label>
              <input type="text" class="form-input readonly" :value="formData.name" readonly />
            </div>
          </div>

          <!-- 可编辑的信息 -->
          <div class="form-group required">
            <label class="form-label">科室</label>
            <div class="custom-select-container" @click.stop="dropdownVisible = !dropdownVisible">
              <div class="select-trigger" :class="{ 'is-open': dropdownVisible }">
                <span :class="{ 'placeholder-text': !formData.departmentName }">
                  {{ formData.departmentName || '请选择科室' }}
                </span>
                <svg class="arrow-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </div>

              <transition name="fade">
                <div v-show="dropdownVisible" class="select-options">
                  <div v-for="dept in departments" :key="dept.deptId" class="dept-group">
                    <div class="dept-group-title">{{ dept.deptName }}</div>
                    <div 
                      v-for="child in dept.children || []" 
                      :key="child.deptId" 
                      class="dept-option"
                      :class="{ selected: formData.department === child.deptId }"
                      @click.stop="selectDept(child)"
                    >
                      {{ child.deptName }}
                    </div>
                  </div>
                  <div v-if="departments.length === 0" class="no-data">暂无科室数据</div>
                </div>
              </transition>
            </div>
          </div>

  <div class="form-group">
  <label class="form-label">职称</label>
  <select
    class="form-input"
    v-model="formData.title"
  >
    <option value="" disabled>请选择职称</option>
    <option value="住院医师">住院医师</option>
    <option value="主治医师">主治医师</option>
    <option value="副主任医师">副主任医师</option>
    <option value="主任医师">主任医师</option>
  </select>
</div>


          <div class="form-group">
            <label class="form-label">性别</label>
            <select class="form-input" v-model="formData.gender">
              <option value="">请选择</option>
              <option value="M">男</option>
              <option value="F">女</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">联系电话</label>
            <input
              type="tel"
              class="form-input"
              v-model="formData.phone"
              placeholder="请输入手机号"
            />
          </div>

          <div class="form-group">
            <label class="form-label">电子邮箱</label>
            <input
              type="email"
              class="form-input"
              v-model="formData.email"
              placeholder="请输入邮箱"
            />
          </div>

          

          <div class="form-group">
            <label class="form-label">擅长领域</label>
            <textarea
              class="form-textarea"
              v-model="formData.bio"
              placeholder="请输入擅长领域"
              rows="4"
            ></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-cancel" @click="handleClose">
              取消
            </button>
            <button type="button" class="btn-submit" @click="handleSubmit" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  doctor: {
    type: Object,
    default: null
  },
  departments: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// State
const dropdownVisible = ref(false)
const saving = ref(false)
const formData = reactive({
  id: '',
  name: '',
  department: '',
  departmentName: '',
  title: '',
  gender: '',
  phone: '',
  email: '',
  bio: ''
})

// 监听医生数据变化，填充表单
watch(() => props.doctor, async (newDoctor) => {
  if (newDoctor && props.modelValue) {
    // 获取完整的医生信息
    await loadDoctorDetail(newDoctor.id)
  }
}, { immediate: true })

// 关闭下拉菜单
const closeDropdown = () => {
  dropdownVisible.value = false
}

// 加载医生详细信息
const loadDoctorDetail = async (doctorId) => {
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get(`/api/admin/doctors/${doctorId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    
    if (data && data.code === 200) {
      const detail = data.data
      formData.id = detail.doctorId || ''
      formData.name = detail.username || ''
      formData.department = detail.deptId || ''
      formData.departmentName = detail.deptName || ''
      formData.title = detail.title || ''
      formData.gender = detail.gender || ''
      formData.phone = detail.phone || ''
      formData.email = detail.email || ''
      formData.bio = detail.bio || ''
    }
  } catch (e) {
    console.error('加载医生详情失败', e)
    alert('获取医生详情失败')
  }
}

// 选择科室
const selectDept = (child) => {
  formData.department = child.deptId
  formData.departmentName = child.deptName
  dropdownVisible.value = false
}

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
  dropdownVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formData.department) {
    alert('请选择科室')
    return
  }

  saving.value = true
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.put(
      `/api/admin/doctors/update/${formData.id}`,
      {
        department: formData.department,
        title: formData.title,
        gender: formData.gender,
        phone: formData.phone,
        email: formData.email,
        specialty: formData.specialty,
        bio: formData.bio
      },
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      }
    )

    if (data.code === 200) {
      alert('修改成功')
      emit('success')
      handleClose()
    } else {
      alert(data.message || '修改失败')
    }
  } catch (e) {
    console.error('修改医生信息失败', e)
    alert('请求失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 点击页面其他地方关闭下拉
onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
/* 模态框过渡动画 */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f7fafc;
  color: #2d3748;
}

.modal-body {
  padding: 2rem;
}

.readonly-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group.required .form-label::after {
  content: '*';
  color: #f44336;
  margin-left: 4px;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 600;
  font-size: 0.9rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.form-input.readonly {
  background: #f7fafc;
  color: #718096;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

/* 自定义下拉框 */
.custom-select-container {
  position: relative;
  cursor: pointer;
}

.select-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #fff;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s;
  color: #333;
}

.select-trigger:hover {
  border-color: #f5576c;
}

.select-trigger.is-open {
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
}

.placeholder-text {
  color: #94a3b8;
}

.arrow-icon {
  color: #94a3b8;
  transition: transform 0.2s;
  flex-shrink: 0;
}

.select-trigger.is-open .arrow-icon {
  transform: rotate(180deg);
}

.select-options {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: #fff;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.dept-group {
  border-bottom: 1px solid #f1f5f9;
}

.dept-group:last-child {
  border-bottom: none;
}

.dept-group-title {
  padding: 8px 12px;
  font-size: 0.75rem;
  color: #94a3b8;
  background-color: #f8fafc;
  font-weight: 600;
}

.dept-option {
  padding: 10px 12px 10px 24px;
  color: #475569;
  transition: background 0.2s;
  cursor: pointer;
}

.dept-option:hover {
  background-color: #ffe6f0;
  color: #f5576c;
}

.dept-option.selected {
  background-color: #ffe6f0;
  color: #f5576c;
  font-weight: 500;
}

.no-data {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
}

.select-options::-webkit-scrollbar {
  width: 6px;
}

.select-options::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

/* 下拉动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.btn-cancel {
  background: #f7fafc;
  color: #4a5568;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-submit {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 640px) {
  .readonly-section {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }
}
</style>