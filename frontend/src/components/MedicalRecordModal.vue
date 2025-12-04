<template>
  <div v-if="visible" class="modal-overlay" @click.self="handleClose">
    <div class="modal-container">
      <!-- 头部 -->
      <div class="modal-header">
        <div class="header-info">
          <h2>患者病历</h2>
          <div class="patient-info">
            <span class="patient-name">{{ patientInfo.patientName || '未知患者' }}</span>
            <span class="separator">·</span>
            <span>{{ patientInfo.gender === 'M' ? '男' : patientInfo.gender === 'F' ? '女' : '未知' }}</span>
            <span class="separator">·</span>
            <span>{{ patientInfo.age }}岁</span>
          </div>
        </div>
        <button class="close-btn" @click="handleClose" :disabled="saving">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <!-- 内容 -->
      <div class="modal-body">
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载病历信息中...</p>
        </div>

        <div v-else-if="error" class="error-state">
          <div class="alert error">{{ error }}</div>
          <button class="btn secondary" @click="initForm">重新加载</button>
        </div>

        <div v-else class="record-form">
          <!-- 基本信息（只读） -->
          <div class="section">
            <h3 class="section-title">基本信息</h3>
            <div class="form-grid readonly">
              <div class="form-item">
                <label>就诊时段</label>
                <div class="readonly-value">{{ formatDateTime(workDate) }}</div>
              </div>
              <div class="form-item">
                <label>就诊科室</label>
                <div class="readonly-value">{{ patientInfo.deptName || '—' }}</div>
              </div>
            </div>
          </div>

          <div class="section">
            <h3 class="section-title">病史信息</h3>
            <div class="form-item full-width">
              <label>既往病史</label>
              <div class="readonly-value">
                <span v-if="loadingHistory">加载中...</span>
                <span v-else-if="errorHistory" class="error">{{ errorHistory }}</span>
                <span v-else>{{ pastMedicalHistory }}</span>
              </div>
            </div>
          </div>

          <!-- 病史信息 -->
          <div class="section">
            <h3 class="section-title">病史信息</h3>
            <div class="form-item full-width">
              <label>既往病史</label>
              <div class="readonly-value">
                <span v-if="loadingHistory">加载中...</span>
                <span v-else-if="errorHistory" class="error">{{ errorHistory }}</span>
                <span v-else>{{ pastMedicalHistory }}</span>
              </div>
            </div>
          </div>

          <div class="section">
            <h3 class="section-title">历史病历</h3>
            <div class="form-item full-width">
              <div v-if="loadingRecords">加载中...</div>
              <div v-else-if="errorRecords" class="error">{{ errorRecords }}</div>
              <div v-else>
                <ul class="history-list">
                  <li v-for="record in pastRecords" :key="record.recordId" class="history-item">
                    <div class="history-field">
                      <span class="history-field-label">主诉：</span>
                      <span class="history-field-value">{{ record.chiefComplaint }}</span>
                    </div>
                    <div class="history-field">
                      <span class="history-field-label">诊断：</span>
                      <span class="history-field-value">{{ record.diagnosis }}</span>
                    </div>
                    <div class="history-field">
                      <span class="history-field-label">治疗方案：</span>
                      <span class="history-field-value">{{ record.treatment }}</span>
                    </div>
                  </li>
                  <li v-if="pastRecords.length === 0" class="history-empty">暂无历史病历</li>
                </ul>
              </div>
            </div>
          </div>



          <!-- 就诊信息（可编辑） -->
          <div class="section editable">
            <h3 class="section-title">
              <span>就诊记录</span>
              <span v-if="isEditing" class="edit-badge">填写中</span>
            </h3>

            <div class="form-item full-width">
              <label>主诉 <span class="required">*</span></label>
              <textarea
                  v-model="formData.chiefComplaint"
                  :disabled="!isEditing"
                  placeholder="请输入患者主诉..."
                  rows="3"
              ></textarea>
            </div>

            <div class="form-item full-width">
              <label>现病史</label>
              <textarea
                  v-model="formData.presentIllness"
                  :disabled="!isEditing"
                  placeholder="请输入现病史..."
                  rows="4"
              ></textarea>
            </div>

            <div class="form-item full-width">
              <label>体格检查</label>
              <textarea
                  v-model="formData.physicalExam"
                  :disabled="!isEditing"
                  placeholder="请输入体格检查结果..."
                  rows="4"
              ></textarea>
            </div>

            <div class="form-item full-width">
              <label>初步诊断 <span class="required">*</span></label>
              <textarea
                  v-model="formData.diagnosis"
                  :disabled="!isEditing"
                  placeholder="请输入诊断结果..."
                  rows="3"
              ></textarea>
            </div>

            <div class="form-item full-width">
              <label>治疗方案</label>
              <textarea
                  v-model="formData.treatment"
                  :disabled="!isEditing"
                  placeholder="请输入治疗方案..."
                  rows="4"
              ></textarea>
            </div>

            <div class="form-item full-width">
              <label>医嘱</label>
              <textarea
                  v-model="formData.doctorAdvice"
                  :disabled="!isEditing"
                  placeholder="请输入医嘱..."
                  rows="3"
              ></textarea>
            </div>

            <div class="form-grid">
              <div class="form-item">
                <label>费用（元）</label>
                <input
                    v-model.number="formData.fee"
                    type="number"
                    step="0.01"
                    :disabled="!isEditing"
                    placeholder="0.00"
                />
              </div>
              <div class="form-item">
                <label>下次复诊日期</label>
                <input
                    v-model="formData.nextVisitDate"
                    type="date"
                    :disabled="!isEditing"
                />
              </div>
            </div>

            <div class="form-item full-width">
              <label>备注</label>
              <textarea
                  v-model="formData.notes"
                  :disabled="!isEditing"
                  placeholder="其他备注信息..."
                  rows="2"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="modal-footer">
        <button class="btn secondary" @click="handleClose" :disabled="saving">取消</button>
        <button
            v-if="!isEditing"
            class="btn primary"
            @click="startEdit"
        >
          填写病历
        </button>
        <template v-else>
          <button class="btn ghost" @click="cancelEdit" :disabled="saving">取消编辑</button>
          <button class="btn success" @click="saveRecord" :disabled="saving || !isFormValid">
            {{ saving ? '保存中...' : '保存病历' }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  visible: Boolean,
  patientInfo: Object,
  workDate: String
})

const emit = defineEmits(['close', 'saved'])

const token = ref(localStorage.getItem('token'))
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const isEditing = ref(false)

const pastMedicalHistory = ref('无既往病史记录')
const loadingHistory = ref(false)
const errorHistory = ref('')

const pastRecords = ref([])          // 历史病历列表
const loadingRecords = ref(false)    // 历史病历加载状态
const errorRecords = ref('')         // 历史病历错误信息


const formData = ref({
  chiefComplaint: '',
  presentIllness: '',
  physicalExam: '',
  diagnosis: '',
  treatment: '',
  doctorAdvice: '',
  fee: null,
  nextVisitDate: '',
  notes: ''
})

const isFormValid = computed(() => {
  return formData.value.chiefComplaint?.trim() && formData.value.diagnosis?.trim()
})

// 打开弹窗初始化表单，并加载既往病史和历史病历
watch(() => props.visible, async (val) => {
  if (val && props.patientInfo.patientId) {
    initForm()
    await Promise.all([
      loadPastHistory(props.patientInfo.patientId),
      loadPastRecords(props.patientInfo.patientId)
    ])
  }
})

function initForm() {
  loading.value = false
  error.value = ''
  isEditing.value = true

  // 初始化空表单
  formData.value = {
    chiefComplaint: '',
    presentIllness: '',
    physicalExam: '',
    diagnosis: '',
    treatment: '',
    doctorAdvice: '',
    fee: null,
    nextVisitDate: '',
    notes: ''
  }
}

// 开始编辑
function startEdit() {
  isEditing.value = true
}

// 取消编辑
function cancelEdit() {
  if (hasChanges() && !confirm('有未保存更改，确定取消吗？')) return
  initForm()
  isEditing.value = false
}

// 表单是否有修改
function hasChanges() {
  return Object.values(formData.value).some(v => v !== '' && v !== null)
}

// 保存病历（POST 创建 + 自动标记已就诊）
async function saveRecord() {
  if (!isFormValid.value) {
    alert('请填写必填项：主诉和诊断')
    return
  }
  saving.value = true
  try {
    // 从 localStorage 或 ref 获取 token
    const myToken = token.value
    if (!myToken) {
      alert('未登录或 token 缺失')
      saving.value = false
      return
    }

    const headers = {
      Authorization: `Bearer ${myToken}`
    }

    const payload = {
      patientId: props.patientInfo.patientId,
      appointmentId: props.patientInfo.appointmentId || null,
      chiefComplaint: formData.value.chiefComplaint,
      presentIllness: formData.value.presentIllness,
      physicalExam: formData.value.physicalExam,
      diagnosis: formData.value.diagnosis,
      treatment: formData.value.treatment,
      doctorAdvice: formData.value.doctorAdvice,
      fee: formData.value.fee,
      nextVisitDate: formData.value.nextVisitDate,
      notes: formData.value.notes
    }

    const { data } = await axios.post('/api/doctor/medical-record/new', payload, { headers })
    if (data?.code !== 200) throw new Error(data?.message || '创建病历失败')

    alert('病历创建成功')

    if (props.patientInfo.appointmentId) {
      await axios.put(
          `/api/doctor/patient/${props.patientInfo.appointmentId}/completed`,
          null,
          { headers }
      )
      alert('患者已标记为已就诊')
    }

    initForm()
    isEditing.value = false
    emit('saved', props.workDate)
    emit('close')

  } catch (e) {
    alert(e?.response?.data?.message || e?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function loadPastHistory(patientId) {
  loadingHistory.value = true
  errorHistory.value = ''
  try {
    const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
    const { data } = await axios.get(`/api/doctor/medical-record/past/${patientId}`, { headers })
    if (data?.code !== 200) throw new Error(data?.message || '获取病史失败')
    pastMedicalHistory.value = data.data
  } catch (e) {
    errorHistory.value = e?.response?.data?.message || e?.message || '获取病史失败'
    pastMedicalHistory.value = '获取失败'
  } finally {
    loadingHistory.value = false
  }
}

// 加载历史病历
async function loadPastRecords(patientId) {
  loadingRecords.value = true
  errorRecords.value = ''
  try {
    const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
    const { data } = await axios.get(`/api/doctor/medical-record/history/${patientId}`, { headers })
    if (data?.code !== 200) throw new Error(data?.message || '获取历史病历失败')
    // 取最近5条病历，筛选主要字段
    pastRecords.value = (data.data || []).slice(-5).map(r => ({
      recordId: r.recordId,
      chiefComplaint: r.chiefComplaint,
      diagnosis: r.diagnosis,
      treatment: r.treatment
    }))
  } catch (e) {
    errorRecords.value = e?.response?.data?.message || e?.message || '获取历史病历失败'
    pastRecords.value = []
  } finally {
    loadingRecords.value = false
  }
}

// 关闭弹窗
function handleClose() {
  if (isEditing.value && hasChanges() && !confirm('有未保存更改，确定关闭吗？')) return
  isEditing.value = false
  emit('close')
}

// 时间格式化
function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
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
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 24px 16px;
  border-bottom: 2px solid #f3f4f6;
}

.header-info h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #111827;
}

.patient-info {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #6b7280;
  font-size: 14px;
}

.patient-name {
  font-weight: 600;
  color: #374151;
}

.separator {
  color: #d1d5db;
}

.close-btn {
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.close-btn:hover:not(:disabled) {
  background: #e5e7eb;
  color: #374151;
}

.close-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
}

.alert {
  padding: 12px 16px;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.alert.error {
  background: #fdecea;
  color: #b91c1c;
  border: 1px solid #fca5a5;
}

.section {
  margin-bottom: 32px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.form-grid.readonly {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item.full-width {
  grid-column: 1 / -1;
}

.form-item label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.required {
  color: #ef4444;
}

.readonly-value {
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  color: #374151;
  font-size: 14px;
  min-height: 20px;
}

input, textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
  background: #f9fafb;
  transition: all 0.2s;
}

input:disabled, textarea:disabled {
  background: #f9fafb;
  cursor: not-allowed;
}

input.editing, textarea.editing {
  background: #fff;
  border-color: #667eea;
  cursor: text;
}

input.editing:focus, textarea.editing:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea {
  resize: vertical;
  min-height: 60px;
  font-family: inherit;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
}

.history-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.history-date {
  font-weight: 600;
  color: #111827;
}

.history-dept, .history-doctor {
  color: #6b7280;
  font-size: 14px;
}

.history-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-field {
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
}

.history-field strong {
  color: #111827;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 2px solid #f3f4f6;
  background: #f9fafb;
  border-radius: 0 0 16px 16px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
}

.btn.success:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.btn.secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn.secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn.ghost {
  background: transparent;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.btn.ghost:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
/* 历史病历列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.history-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
}

.history-field {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 14px;
  line-height: 1.6;
}

.history-field:last-child {
  margin-bottom: 0;
}

.history-field-label {
  font-weight: 600;
  color: #111827;
  min-width: 60px;
}

.history-field-value {
  color: #374151;
  word-break: break-word;
}

.history-empty {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  padding: 12px 0;
}


@media (max-width: 768px) {
  .modal-container {
    max-height: 95vh;
    border-radius: 12px;
  }

  .modal-header, .modal-body, .modal-footer {
    padding: 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>