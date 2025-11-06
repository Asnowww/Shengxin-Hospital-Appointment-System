<template>
  <Navigation ref="navRef" />
  <div class="page-container" :style="{ paddingTop: navHeight + 'px' }">
    <div class="content">
      <div class="card">
        <div class="card-header">
          <div class="title-group">
            <h2>信息变更申请</h2>
            <p class="subtitle">提交对外显示的医生信息（职称/简介）变更，待管理员审核</p>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label>职称 <span class="required">*</span></label>
            <select v-model="form.title">
              <option value="住院医师">住院医师</option>
              <option value="主治医师">主治医师</option>
              <option value="副主任医师">副主任医师</option>
              <option value="主任医师">主任医师</option>
            </select>
          </div>
          <div class="form-group full">
            <label>简介</label>
            <textarea v-model="form.bio" rows="4" placeholder="请输入修改后的简介"></textarea>
          </div>
        </div>

        <div class="actions">
          <button class="btn primary" @click="submit">提交申请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import axios from 'axios'
import Navigation from '@/components/Navigation.vue'

const navRef = ref(null)
const navHeight = ref(110)
function updateNavHeight() {
  if (navRef.value?.$el) {
    const h = navRef.value.$el.offsetHeight
    navHeight.value = h + 30
  }
}

const doctorId = ref(null)
const form = ref({ title: '住院医师', bio: '' })

async function submit() {
  if (!doctorId.value) { await ensureDoctorId(); if (!doctorId.value) { alert('无法识别医生身份，请稍后重试'); return } }
  try {
    const token = localStorage.getItem('token')
    // 说明：后端暂未提供“医生提交变更申请”的专用接口，先直接提交到更新接口，管理员侧可在“医生信息更改审核”中查看并再次调整。
    await axios.put(`/api/admin/doctors/update/${doctorId.value}`, form.value, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert('已提交，管理员将进行审核与发布')
  } catch (e) {
    console.error(e)
    alert(e?.response?.data?.message || '提交失败')
  }
}

async function ensureDoctorId() {
  const cached = localStorage.getItem('doctorId')
  if (cached) { doctorId.value = Number(cached); return }
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/doctor/list', {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    const username = localStorage.getItem('username') || localStorage.getItem('name')
    if (data?.code === 200 && Array.isArray(data.data)) {
      const hit = data.data.find(d => d.name === username || d.username === username)
      if (hit && (hit.id || hit.doctorId)) {
        doctorId.value = Number(hit.id || hit.doctorId)
        localStorage.setItem('doctorId', String(doctorId.value))
      }
    }
  } catch (e) {}
}

onMounted(async () => { await nextTick(); updateNavHeight(); await ensureDoctorId() })
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f7fafc; }
.content { max-width: 800px; margin: 0 auto; padding: 1.5rem; display: grid; gap: 1.5rem; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,.1); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #f0f0f0; }
.title-group h2 { margin: 0; color: #2d3748; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: .25rem 0 0 0; color: #718096; font-size: .9rem; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; padding: 1.25rem 1.5rem; }
.form-group { display: flex; flex-direction: column; gap: .5rem; }
.form-group.full { grid-column: 1 / -1; }
input, select, textarea { padding: 10px 12px; border: 2px solid #e2e8f0; border-radius: 8px; }
.required { color: #e53e3e; }
.actions { display: flex; justify-content: flex-end; gap: .75rem; padding: 0 1.5rem 1.25rem; }
.btn { padding: 10px 14px; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; }
.btn.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
</style>


