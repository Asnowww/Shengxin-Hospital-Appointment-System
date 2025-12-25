<template>
  <div class="medical-records">
    <div class="header">
      <h2>电子病历</h2>
      <button @click="fetchRecords" class="refresh-btn">
        <svg xmlns="https://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="23 4 23 10 17 10"></polyline>
          <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
        </svg>
        刷新
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在拉取您的病历档案...</p>
    </div>

    <div v-else-if="records.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg xmlns="https://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#e2e8f0" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
          <polyline points="14.5 2 14.5 7.5 20 7.5"></polyline>
          <line x1="12" y1="18" x2="12" y2="12"></line>
          <line x1="9" y1="15" x2="15" y2="15"></line>
        </svg>
      </div>
      <p>暂无电子病历记录</p>
      <span>您的病历将在就诊结束后由医生录入系统</span>
    </div>

    <div v-else class="records-list">
      <div v-for="record in records" :key="record.recordId" class="record-card">
        <div class="card-header">
          <div class="date-tag">
            <span class="day">{{ formatDate(record.createdAt, 'day') }}</span>
            <span class="month">{{ formatDate(record.createdAt, 'month') }}</span>
          </div>
          <div class="doctor-info">
            <span class="dept">{{ record.deptName }}</span>
            <span class="doc-name">接诊医生: {{ record.doctorName }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="info-item">
            <label>主诉</label>
            <p>{{ record.chiefComplaint }}</p>
          </div>
          <div class="info-item">
            <label>诊断结果</label>
            <p class="diagnosis">{{ record.diagnosis }}</p>
          </div>
          <div class="info-item" v-if="record.treatment">
            <label>处理意见/处方</label>
            <p>{{ record.treatment }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const records = ref([])
const loading = ref(true)

async function fetchRecords() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/api/patient/medical-record/my-history', {
      headers: { Authorization: `Bearer ${token}` }
    })

    console.log('病历API完整响应:', res.data) // 查看完整响应

    if (res.data.code === 200) {
      const originalRecords = res.data.data
      console.log('原始病历记录:', originalRecords) // 查看病历数据

      // 提取所有不重复的医生ID
      const doctorIds = [...new Set(
          originalRecords
              .map(record => record.doctorId)
              .filter(id => id)
      )]

      console.log('提取的医生IDs:', doctorIds) // 确认医生ID是否提取成功

      if (doctorIds.length > 0) {
        const doctorPromises = doctorIds.map(id =>
            axios.get(`/api/doctor/doctorId/${id}`)
                .then(res => {
                  console.log(`医生${id}的完整响应:`, res.data) // 查看每个医生API响应
                  return res.data
                })
                .catch(err => {
                  console.error(`获取医生 ${id} 信息失败:`, err)
                  return null
                })
        )

        const doctorsData = await Promise.all(doctorPromises)
        console.log('所有医生数据:', doctorsData) // 查看获取到的医生数据

        const doctorMap = {}
        doctorsData.forEach(doctor => {
          if (doctor && doctor.doctorId) {
            doctorMap[doctor.doctorId] = doctor
          }
        })

        console.log('医生映射表:', doctorMap)

        // 合并医生信息到病历记录
        records.value = originalRecords.map(record => {
          console.log(`处理病历 ${record.recordId}, doctorId: ${record.doctorId}`) // 查看每条病历的处理
          const doctor = doctorMap[record.doctorId]
          console.log(`找到的医生信息:`, doctor)

          return {
            ...record,
            doctorName: doctor?.doctorName || '未知医生',
            deptName: doctor?.deptName || '未知科室',
            title: doctor?.title || ''
          }
        })
      } else {
        records.value = originalRecords
      }

      console.log('最终病历数据:', records.value)
    }
  } catch (err) {
    console.error('获取病历失败:', err)
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr, part) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (part === 'day') return date.getDate().toString().padStart(2, '0')
  if (part === 'month') return (date.getMonth() + 1) + '月'
  return date.toLocaleString()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.medical-records {
  padding: 2.5rem;
  height: 100%;
}

.header {
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #4a5568;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5rem 0;
  color: #a0aec0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  margin-bottom: 1.5rem;
}

.empty-state p {
  font-size: 1.25rem;
  color: #4a5568;
  margin-bottom: 0.5rem;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.record-card {
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  overflow: hidden;
  transition: transform 0.2s;
}

.record-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  background: white;
  border-bottom: 1px solid #edf2f7;
  gap: 1.5rem;
}

.date-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.date-tag .day {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1;
}

.date-tag .month {
  font-size: 0.75rem;
  opacity: 0.9;
}

.doctor-info {
  display: flex;
  flex-direction: column;
}

.dept {
  font-weight: 700;
  color: #2d3748;
  font-size: 1.1rem;
}

.doc-name {
  font-size: 0.9rem;
  color: #718096;
}

.card-body {
  padding: 1.5rem;
}

.info-item {
  margin-bottom: 1rem;
}

.info-item:last-child { margin-bottom: 0; }

.info-item label {
  display: block;
  font-size: 0.85rem;
  color: #a0aec0;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 0.25rem;
}

.info-item p {
  color: #4a5568;
  line-height: 1.6;
}

.diagnosis {
  color: #2d3748 !important;
  font-weight: 600;
}

@media (max-width: 640px) {
  .medical-records { padding: 1.5rem; }
  .card-header { flex-direction: row; gap: 1rem; }
  .date-tag { min-width: 50px; padding: 0.4rem; }
  .date-tag .day { font-size: 1.25rem; }
}
</style>
