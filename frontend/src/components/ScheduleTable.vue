<template>
  <div class="schedule-container">
    <!-- 控制条 -->
    <div class="control-bar">
      <button @click="prevWeek" class="btn-nav">← 上一周</button>
      <span class="week-info">
        {{ formatDate(weekDates[0]) }} - {{ formatDate(weekDates[6]) }}
      </span>
      <button @click="nextWeek" class="btn-nav">下一周 →</button>
      <button @click="resetToCurrentWeek" class="btn-reset">返回本周</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载排班数据中...</p>
    </div>

    <!-- 调试信息 -->
    <div class="debug-info">
      诊室数: {{ rooms.length }} | 医生数: {{ doctors.length }} | 排班数: {{ schedules.length }}
    </div>

    <!-- 表格 -->
    <div v-if="!loading && rooms.length > 0" class="table-wrapper">
      <table class="schedule-table">
        <thead>
          <tr>
            <th class="col-dept">诊室</th>
            <th v-for="(date, idx) in weekDates" :key="idx" class="col-date">
              {{ formatDisplayDate(date) }}
            </th>
          </tr>
          <tr>
            <th class="col-dept"></th>
            <th v-for="(date, idx) in weekDates" :key="'time-' + idx" class="col-time">
              <div class="time-slots">
                <span v-for="(slot, sidx) in timeSlots" :key="sidx" class="slot">{{ slot }}</span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(room, index) in rooms" :key="room.roomId + '-' + index">
            <td class="col-dept">{{ room.roomName }}</td>
            <td v-for="(date, didx) in weekDates" :key="'cell-' + room.roomId + '-' + didx" class="col-schedule">
              <div class="date-group">
                <div v-for="(slot, slotIdx) in timeSlots" :key="slotIdx" class="slot-cell">
                  <div class="slot-container" :class="{ disabled: !canOperate(date, slotIdx) }">
                     <div
    v-for="schedule in getSchedules(room.roomId, date, slotIdx)"
    :key="schedule.scheduleId"
    class="schedule-item"
    :class="{ disabled: !canOperate(date, slotIdx) }"
    @click="!canOperate(date, slotIdx) || editSchedule(schedule)"
  >
                      <div class="doctor-info">
  <div class="doctor-name">
    {{ schedule.doctorName }}
    <span class="type-tag" :class="'type-' + schedule.appointmentTypeId">
      {{ getTypeLabel(schedule.appointmentTypeId) }}
    </span>
  </div>
  <div class="appointments">
    预约: {{ schedule.bookedSlots }}/{{ schedule.maxSlots }}
  </div>
</div>

                      <div class="actions" @click.stop>
                        <!-- <button @click.stop="editSchedule(schedule)" class="btn-edit" title="编辑">编</button> -->
                        <button @click.stop="deleteSchedule(schedule)" class="btn-delete" title="删除">删</button>
                      </div>
                    </div>
                     <button
    class="btn-add"
    :disabled="!canOperate(date, slotIdx)"
    @click="canOperate(date, slotIdx) && addSchedule(room.roomId, date, slotIdx)"
  >+</button>
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else-if="!loading && rooms.length === 0" class="empty-state">
      <p>没有可用的诊室数据</p>
    </div>

    <!-- 弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ editingSchedule ? '编辑排班' : '添加排班' }}</h3>
        <form @submit.prevent="saveSchedule">
          <div class="form-group">
            <label>诊室</label>
            <select v-model="formData.roomId" :disabled="editingSchedule||creatingFromTable" class="form-input">
              <option value="">请选择诊室</option>
              <option v-for="room in rooms" :key="room.roomId" :value="room.roomId">{{ room.roomName }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>医生</label>
            <select v-model="formData.doctorId" class="form-input" required>
              <option value="">请选择医生</option>
              <option v-for="doctor in doctors" :key="doctor.doctorId" :value="doctor.doctorId">
                {{ doctor.doctorName }} - {{ doctor.deptName }} - {{ doctor.title }}
              </option>
            </select>
          </div>

            <div class="form-group">
            <label>号别</label>
            <select v-model="formData.appointmentTypeId" class="form-input" required>
              <option value="">请选择号别类型</option>
              <option value="1">普通</option>
              <option value="2">专家</option>
              <option value="3">特需</option>
            </select>
          </div>

          <div class="form-group">
            <label>日期</label>
            <input v-model="formData.workDate" type="date" class="form-input" :disabled="editingSchedule||creatingFromTable" required />
          </div>

         <div class="form-group">
  <label>时间段</label>
  <select 
    v-model="formData.timeSlot" 
    :disabled="editingSchedule||creatingFromTable " 
    class="form-input" 
    required
  >
    <option value="">请选择时间段</option>
    <option 
      v-for="(slot, idx) in timeSlots" 
      :key="idx" 
      :value="idx"
      :disabled="!canOperate(formData.workDate, idx)"
    >
      {{ slot }}
    </option>
  </select>
</div>


          <div class="form-group">
            <label>最大预约数</label>
            <input v-model.number="formData.maxSlots" type="number" min="1" class="form-input" required />
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-submit">保存</button>
            <button type="button" @click="closeModal" class="btn-cancel">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  deptId: { type: [String, Number], required: true },
  deptName: String
})

const currentDate = ref(new Date())
const rooms = ref([])
const doctors = ref([])
const schedules = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingSchedule = ref(null)
const creatingFromTable = ref(false)

const timeSlots = ['上午', '下午']

const getTypeLabel = (typeId) => {
  if (typeId === 1) return '普'
  if (typeId === 2) return '专'
  if (typeId === 3) return '特'
  return ''
}

const formData = ref({
  roomId: '',
  doctorId: '',
  workDate: '',
  timeSlot: '',
  maxSlots: ''
})

// 判断日期限制
const canOperate = (date, timeSlot) => {
  const now = new Date()
  const todayStr = formatDate(now)

  // date 可能是字符串 YYYY-MM-DD
  const dateStr = typeof date === 'string' ? date : formatDate(date)

  // 1) 如果日期 < 今天 → 禁止
  if (dateStr < todayStr) return false

  // 2) 如果是未来日期 → 永远可操作
  if (dateStr > todayStr) return true

  // 下面是 “今天” 的逻辑
  const hour = now.getHours()

  // 上午 slot = 0；下午 slot = 1

  if (hour < 7) return true       // 上午7点前，上午/下午都可操作
  if (hour < 12) return timeSlot === 1 // 上午过了，只能操作下午
  return false                    // 超过12点，今天不可操作
}



// 获取本周日期数组
const getMonday = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  return new Date(d.setDate(diff))
}

const weekDates = computed(() => {
  const monday = getMonday(currentDate.value)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday)
    d.setDate(d.getDate() + i)
    return d
  })
})

// 返回 YYYY-MM-DD 格式
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatDisplayDate = (date) => {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${date.getMonth() + 1}/${date.getDate()} 周${days[date.getDay()]}`
}

// 获取指定诊室、日期、时间段的排班数据
const getSchedules = (roomId, date, timeSlot) => {
  const dateStr = formatDate(date)
  return schedules.value.filter(s => {
    const sDate = s.workDate ? s.workDate.split(' ')[0] : ''

    return s.status !== 'cancelled' &&       
           String(s.roomId) === String(roomId) &&
           sDate === dateStr &&
           String(s.timeSlot) === String(timeSlot)
  })
}


// 请求接口
async function fetchRooms() {
  try {
    const { data } = await axios.get(`/api/rooms/dept/${props.deptId}`)
console.log('接口返回:', data)
const roomList = data?.data || data || []
rooms.value = [...roomList.sort((a, b) => a.roomId - b.roomId)]
console.log('✓ 诊室数据:', rooms.value)

  } catch (err) {
    console.error('✗ 获取诊室列表失败', err)
  }
}


async function fetchDoctors() {
  try {
    const { data } = await axios.get(`/api/doctor/dept/${props.deptId}`)
    const doctorList = Array.isArray(data) ? data : (data.data || [])
    doctors.value = doctorList
    console.log('✓ 医生数据加载:', doctors.value.length, '条')
  } catch (err) {
    console.error('✗ 获取医生列表失败', err)
  }
}

async function fetchSchedules() {
  loading.value = true
  try {
    const startDate = formatDate(weekDates.value[0])
    const endDate = formatDate(weekDates.value[6])
    console.log('📅 请求排班数据:', { deptId: props.deptId, startDate, endDate })
    
    const { data } = await axios.get('/api/admin/schedules/list', {
      params: { deptId: props.deptId, startDate, endDate }
    })
    
    const scheduleList = Array.isArray(data) ? data : (data.data || [])
    schedules.value = scheduleList
    console.log('✓ 排班数据加载:', schedules.value.length, '条')
  } catch (err) {
    console.error('✗ 获取排班数据失败', err)
  } finally {
    loading.value = false
  }
}

// 导航函数
const prevWeek = () => {
  const newDate = new Date(currentDate.value)
  newDate.setDate(newDate.getDate() - 7)
  currentDate.value = newDate
}

const nextWeek = () => {
  const newDate = new Date(currentDate.value)
  newDate.setDate(newDate.getDate() + 7)
  currentDate.value = newDate
}

const resetToCurrentWeek = () => {
  currentDate.value = new Date()
}

// 弹窗函数
const addSchedule = (roomId, date, timeSlot) => {
  creatingFromTable.value = true
  editingSchedule.value = null
  formData.value = {
    roomId,
    doctorId: '',
    workDate: formatDate(date),
    timeSlot: timeSlot.toString(),
    maxSlots: 10
  }
  showModal.value = true
}

const editSchedule = (schedule) => {
  editingSchedule.value = schedule
  formData.value = {
    roomId: schedule.roomId,
    doctorId: schedule.doctorId,
    workDate: schedule.workDate.split(' ')[0],
    timeSlot: schedule.timeSlot.toString(),
    maxSlots: schedule.maxSlots
  }
  showModal.value = true
}

const deleteSchedule = async (schedule) => {
  if (!confirm('确定删除该排班吗？')) return
  const operatorId = localStorage.getItem('userId') 
  try {
   await axios.delete(`/api/admin/schedules/${schedule.scheduleId}`, {
  params: {
    reason: '管理员删除',
    operatorId: operatorId
  }
})
    alert('删除成功')
    fetchSchedules()
  } catch (err) {
    alert('删除失败：' + (err.response?.data?.message || err.message))
  }
}

const saveSchedule = async () => {
  if (!formData.value.doctorId) { alert('请选择医生'); return }
  try {
    if (editingSchedule.value) {
      await axios.put(`/api/admin/schedules/${editingSchedule.value.scheduleId}`, {
        doctorId: formData.value.doctorId,
        maxSlots: formData.value.maxSlots
      })
    } else {
    await axios.post('/api/admin/schedules/create', {
  roomId: formData.value.roomId,
  deptId: props.deptId,
  appointmentTypeId: 1,
  doctorId: formData.value.doctorId,
  startDate: formData.value.workDate,  
  timeSlots: [parseInt(formData.value.timeSlot)],  
  maxSlots: formData.value.maxSlots
})

    }
    alert('保存成功')
    closeModal()
    fetchSchedules()
  } catch (err) {
    alert('保存失败：' + (err.response?.data?.message || err.message))
  }
}

const closeModal = () => { showModal.value = false; editingSchedule.value = null; creatingFromTable.value = false}

// 初始化和监听
onMounted(async () => {
  console.log('🚀 组件挂载，开始加载数据...')
  await fetchRooms()
  await fetchDoctors()
  await fetchSchedules()
})

watch(currentDate, () => {
  console.log('📅 日期变化，重新加载排班')
  fetchSchedules()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.slot-container.disabled,
.schedule-item.disabled {
  opacity: 0.75;
  pointer-events: none; /* 避免误点 */
}

.btn-add:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.type-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 0 4px;
  font-size: 12px;
  border-radius: 4px;
  color: #fff;
}

.type-1 { background: #4caf50; } /* 普通：绿 */
.type-2 { background: #2196f3; } /* 专家：蓝 */
.type-3 { background: #f44336; } /* 特需：红 */

.schedule-container {
  padding: 0;
  background: transparent;
  min-height: auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.debug-info {
  padding: 12px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

/* 控制条 */
.control-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
  flex-wrap: wrap;
}

.btn-nav, .btn-reset {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-nav {
  background: #f093fb;
  color: white;
}

.btn-nav:hover {
  background: #2563eb;
}

.btn-reset {
  background: #f5e057;
  color: rgb(3, 3, 3);
}

.btn-reset:hover {
  background: #059669;
}

.week-info {
  font-weight: 600;
  color: #333;
  min-width: 200px;
  text-align: center;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #718096;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 表格 */
.table-wrapper {
  background: #f7fafc;
  border-radius: 8px;
  overflow-x: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

.schedule-table th {
  background: #f3f4f6;
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  color: #374151;
  border: 1px solid #e5e7eb;
  font-size: 13px;
}

.col-dept {
  width: 100px;
  background: #f9fafb;
}

.col-date {
  min-width: 150px;
  padding: 12px 8px;
}

.col-time {
  min-width: 150px;
}

.time-slots {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.slot {
  flex: 1;
  padding: 6px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: white;
  font-size: 12px;
}

.schedule-table td {
  padding: 8px;
  border: 1px solid #e5e7eb;
}

.col-schedule {
  padding: 0;
}

.date-group {
  display: flex;
  gap: 8px;
  height: 100%;
}

.slot-cell {
  flex: 1;
  min-height: 120px;
  display: flex;
  align-items: stretch;
}

.slot-container {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px;
  background: #fafbfc;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.schedule-item {
  background: white;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  padding: 8px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.schedule-item:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.doctor-info {
  flex: 1;
  cursor: pointer;
  user-select: none;
}

.doctor-name {
  font-weight: 600;
  color: #1f2937;
}

.appointments {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

.actions {
  display: flex;
  gap: 4px;
}

.btn-edit, .btn-delete {
  padding: 4px 6px;
  border: none;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit {
  background: #dbeafe;
  color: #1e40af;
}

.btn-edit:hover {
  background: #bfdbfe;
}

.btn-delete {
  background: #fee2e2;
  color: #991b1b;
}

.btn-delete:hover {
  background: #fecaca;
}

.btn-add {
  width: 100%;
  padding: 6px;
  border: 2px dashed #9ca3af;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-add:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

/* 弹窗 */
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
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.15);
}

.modal-content h3 {
  margin-bottom: 20px;
  font-size: 18px;
  color: #1f2937;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
  color: #6b7280;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-submit, .btn-cancel {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit {
  background: #3b82f6;
  color: white;
}

.btn-submit:hover {
  background: #2563eb;
}

.btn-cancel {
  background: #e5e7eb;
  color: #374151;
}

.btn-cancel:hover {
  background: #d1d5db;
}

@media (max-width: 768px) {
  .control-bar {
    gap: 10px;
  }

  .schedule-table {
    font-size: 0.85rem;
  }

  .modal-content {
    width: 95%;
    padding: 1.5rem;
  }
}
</style>