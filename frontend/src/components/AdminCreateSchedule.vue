<template>
  <!-- 创建/编辑排班弹窗 -->
  <transition name="modal">
    <div v-if="showScheduleModal" class="modal-overlay" @click.self="closeScheduleModal">
      <div class="modal-container large">
        <button @click="closeScheduleModal" class="close-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <div class="modal-header">
          <h2>{{ isEditing ? '编辑排班' : '创建排班' }}</h2>
          <p class="subtitle">{{ isEditing ? '修改排班信息' : '为医生创建新的排班' }}</p>
        </div>

        <form @submit.prevent="handleSubmit" class="modal-body">
          <!-- 批量创建选项 -->
          <div v-if="!isEditing" class="batch-section">
            <label class="checkbox-label">
              <input type="checkbox" v-model="scheduleForm.isBatch" />
              <span>批量创建排班</span>
            </label>
          </div>

          <div class="form-grid">
            <!-- 医生 -->
            <div class="form-group">
              <label class="form-label">
                医生 <span class="required">*</span>
              </label>
              <select
                v-model="scheduleForm.doctorId"
                :disabled="isEditing"
                @change="checkConflict"
                :class="['form-control', { error: errors.doctorId }]"
                required
              >
                <option value="">请选择医生</option>
                <option v-for="doctor in doctors" :key="doctor.doctorId" :value="doctor.doctorId">
                  {{ doctor.doctorName }} - {{ doctor.deptName }} - {{ doctor.title }}
                </option>
              </select>
              <span v-if="errors.doctorId" class="error-text">{{ errors.doctorId }}</span>
            </div>

            <!-- 日期 / 日期范围 -->
            <div v-if="!scheduleForm.isBatch" class="form-group">
              <label class="form-label">日期 <span class="required">*</span></label>
              <input
                v-model="scheduleForm.date"
                type="date"
                @change="checkConflict"
                :min="minDate"
                :class="['form-control', { error: errors.date }]"
                required
              />
              <span v-if="errors.date" class="error-text">{{ errors.date }}</span>
            </div>

            <div v-else class="form-group full-width">
              <label class="form-label">日期范围 <span class="required">*</span></label>
              <div class="date-range-input">
                <input v-model="scheduleForm.startDate" type="date" :min="minDate" class="form-control" required />
                <span class="date-separator">至</span>
                <input v-model="scheduleForm.endDate" type="date" :min="scheduleForm.startDate || minDate" class="form-control" required />
              </div>
            </div>

            <!-- 时间段选择 -->
            <div class="form-group full-width">
              <label class="form-label">时段 <span class="required">*</span></label>
              <div class="time-period-selector">
                <label class="time-period-option">
                  <input type="checkbox" :value="0" v-model="scheduleForm.timeSlots" />
                  <span>上午（08:00 - 12:00）</span>
                </label>
                <label class="time-period-option">
                  <input type="checkbox" :value="1" v-model="scheduleForm.timeSlots" />
                  <span>下午（13:00 - 17:00）</span>
                </label>
                <label class="time-period-option">
                  <input type="checkbox" :value="2" v-model="scheduleForm.timeSlots" />
                  <span>夜班（18:00 - 22:00）</span>
                </label>
              </div>
            </div>

            <!-- 诊室选择 -->
            <div class="form-group">
              <label class="form-label">
                诊室 <span class="required">*</span>
              </label>
              <select
                v-model="scheduleForm.roomId"
                :disabled="isEditing"
                :class="['form-control', { error: errors.roomId }]"
                required
              >
                <option value="">请选择诊室</option>
                <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">
                  {{ room.roomName }}（{{ room.building }}）
                </option>
              </select>
              <span v-if="errors.roomId" class="error-text">{{ errors.roomId }}</span>
            </div>

            <!-- 号别类型 -->
            <div class="form-group">
              <label class="form-label">
                号别类型 <span class="required">*</span>
              </label>
              <select
                v-model="scheduleForm.appointmentTypeId"
                class="form-control"
                required
              >
                <option value="">请选择号别类型</option>
                <option value="1">普通号</option>
                <option value="2">特需号</option>
                <option value="3">专家号</option>
              </select>
            </div>

            <!-- 最大接诊人数 -->
            <div class="form-group">
              <label class="form-label">最大接诊人数 <span class="required">*</span></label>
              <input
                v-model.number="scheduleForm.maxPatients"
                type="number"
                min="1"
                max="100"
                :class="['form-control', { error: errors.maxPatients }]"
                required
              />
              <span v-if="errors.maxPatients" class="error-text">{{ errors.maxPatients }}</span>
            </div>

            <!-- 批量：工作日 -->
            <div v-if="scheduleForm.isBatch" class="form-group full-width">
              <label class="form-label">工作日 <span class="required">*</span></label>
              <div class="weekday-selector">
                <label
                  v-for="day in weekDays"
                  :key="day.value"
                  class="weekday-checkbox"
                >
                  <input type="checkbox" :value="day.value" v-model="scheduleForm.weekdays" />
                  <span class="checkbox-label">{{ day.label }}</span>
                </label>
              </div>
            </div>

            <!-- 备注 -->
            <div class="form-group full-width">
              <label class="form-label">备注</label>
              <textarea
                v-model="scheduleForm.notes"
                class="form-control textarea"
                rows="3"
                placeholder="备注信息（选填）"
              ></textarea>
            </div>
          </div>

          <!-- 冲突警告 -->
          <div v-if="hasConflict" class="warning-box">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <div>
              <strong>排班冲突：</strong>该医生在此时间段已有排班，请调整时间。
            </div>
          </div>

          <!-- 按钮 -->
          <div class="button-group">
            <button type="button" @click="closeScheduleModal" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn" :disabled="hasConflict || scheduleForm.timeSlots.length === 0">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {{ isEditing ? '保存修改' : (scheduleForm.isBatch ? '批量创建' : '创建排班') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import axios from 'axios'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  isEditing: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: null
  },
  doctors: {
    type: Array,
    default: () => []
  },
  rooms: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['close', 'submit'])

// Reactive state
const showScheduleModal = ref(props.show)
const hasConflict = ref(false)

const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const scheduleForm = reactive({
  id: null,
  doctorId: '',
  deptId: null,
  date: '',
  startDate: '',
  endDate: '',
  roomId: null,
  appointmentTypeId: '',
  timeSlots: [],
  maxPatients: 30,
  notes: '',
  isBatch: false,
  weekdays: []
})

const errors = reactive({
  doctorId: '',
  date: '',
  roomId: '',
  maxPatients: ''
})

// Computed
const minDate = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// Watchers
watch(() => props.show, (newVal) => {
  showScheduleModal.value = newVal
})

watch(() => scheduleForm.doctorId, (newDoctorId) => {
  const selectedDoctor = props.doctors.find(d => d.doctorId === newDoctorId)
  if (selectedDoctor) {
    scheduleForm.deptId = selectedDoctor.deptId
  } else {
    scheduleForm.deptId = ''
  }
})

watch(() => props.initialData, (newVal) => {
  if (newVal && props.isEditing) {
    scheduleForm.id = newVal.scheduleId
    scheduleForm.doctorId = newVal.doctorId
    scheduleForm.date = newVal.date
    scheduleForm.maxPatients = newVal.maxPatients
    scheduleForm.notes = newVal.notes
    scheduleForm.isBatch = false
    scheduleForm.weekdays = []
  }
}, { deep: true })

// Methods
function closeScheduleModal() {
  showScheduleModal.value = false
  emit('close')
  resetForm()
}

function resetForm() {
  scheduleForm.id = null
  scheduleForm.doctorId = ''
  scheduleForm.date = ''
  scheduleForm.startDate = ''
  scheduleForm.endDate = ''
  scheduleForm.roomId = null
  scheduleForm.appointmentTypeId = ''
  scheduleForm.maxPatients = 30
  scheduleForm.notes = ''
  scheduleForm.isBatch = false
  scheduleForm.timeSlots = []
  scheduleForm.weekdays = []
  clearErrors()
  hasConflict.value = false
}

function clearErrors() {
  errors.doctorId = ''
  errors.date = ''
  errors.roomId = ''
  errors.maxPatients = ''
}

function validateForm() {
  clearErrors()
  let isValid = true

  if (!scheduleForm.doctorId) {
    console.log('doctorId missing')
    errors.doctorId = '请选择医生'
    isValid = false
  }

  if (!scheduleForm.isBatch && !scheduleForm.date) {
    errors.date = '请选择日期'
    isValid = false
  }

  if (!scheduleForm.roomId) {
    errors.roomId = '请选择诊室'
    isValid = false
  }

  if (!scheduleForm.maxPatients || scheduleForm.maxPatients < 1) {
    errors.maxPatients = '请输入有效的接诊人数'
    isValid = false
  }

  if (scheduleForm.isBatch && scheduleForm.weekdays.length === 0) {
    alert('请至少选择一个工作日')
    isValid = false
  }

  return isValid
}

async function checkConflict() {
  if (!scheduleForm.doctorId || !scheduleForm.date) {
    hasConflict.value = false
    return
  }

  try {
    await new Promise(resolve => setTimeout(resolve, 200))
    hasConflict.value = false
  } catch (err) {
    console.error('检查冲突失败', err)
  }
}

async function handleSubmit() {
  if (!validateForm()) {
    console.log('表单验证失败')
    return
  }

  if (hasConflict.value) {
    alert('存在排班冲突，请调整时间')
    return
  }

  try {
    let response

    if (props.isEditing) {
      response = await axios.put(`/api/admin/schedules/${scheduleForm.id}`, scheduleForm)
    } else {
      if (scheduleForm.isBatch) {
        // 批量创建
        response = await axios.post('/api/admin/schedules/batchCreate', scheduleForm)
      } else {
        // 单条创建
        response = await axios.post('/api/admin/schedules/create', scheduleForm)
      }
    }

    const { code, message } = response.data

    if (code === 200) {
      if (scheduleForm.isBatch) {
        alert(props.isEditing ? '批量修改成功' : '批量创建成功')
      } else {
        alert(props.isEditing ? '修改成功' : '创建成功')
      }

      emit('submit', scheduleForm)
      closeScheduleModal()
    } else {
      alert(`操作失败：${message || '未知错误'}`)
      console.error('后端返回错误：', response.data)
    }
  } catch (err) {
    console.error('请求出错：', err)
    alert('网络或服务器错误')
  }
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-container.large {
  max-width: 800px;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 32px;
  height: 32px;
  background: #f7fafc;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #718096;
  z-index: 1;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #2d3748;
}

.modal-header {
  padding: 2rem 2rem 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 0.9rem;
}

.modal-body {
  padding: 2rem;
}

.batch-section {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f7fafc;
  border-radius: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 500;
  color: #4a5568;
}

.checkbox-label input[type="checkbox"] {
  cursor: pointer;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 600;
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

.form-control:focus {
  outline: none;
  border-color: #f5576c;
  box-shadow: 0 0 0 3px rgba(245, 87, 108, 0.1);
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

.date-range-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-separator {
  color: #718096;
  font-size: 0.9rem;
}

.weekday-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 0.75rem;
}

.weekday-checkbox {
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

.weekday-checkbox:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.weekday-checkbox input[type="checkbox"]:checked ~ .checkbox-label {
  color: #f5576c;
  font-weight: 600;
}

.warning-box {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 10px;
  color: #856404;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.warning-box svg {
  flex-shrink: 0;
  color: #ffc107;
}

.warning-box strong {
  font-weight: 700;
}

.button-group {
  display: flex;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.time-period-selector {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  padding: 0.75rem;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
}

.time-period-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  color: #4a5568;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.time-period-option:hover {
  background: white;
}

.time-period-option input[type="checkbox"] {
  accent-color: #f5576c;
  cursor: pointer;
  width: 18px;
  height: 18px;
}

.time-period-option input[type="checkbox"]:checked {
  accent-color: #f093fb;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column-reverse;
  }

  .modal-container {
    margin: 1rem;
  }

  .modal-header,
  .modal-body {
    padding: 1.5rem;
  }
}
</style>