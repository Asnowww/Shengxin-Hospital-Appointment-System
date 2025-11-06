<template>
  <div class="audit-container">
    <div class="card">
      <div class="card-header">
        <div class="title-group">
          <h2>医生信息更改审核</h2>
          <p class="subtitle">审核并更新医生公开信息</p>
        </div>
        <div class="filters">
          <input v-model="keyword" class="input" placeholder="搜索姓名" @keyup.enter="fetchDoctors" />
          <button class="btn ghost" @click="fetchDoctors">搜索</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else>
        <div v-if="doctors.length === 0" class="empty">暂无数据</div>
        <div v-else class="table">
          <div class="thead">
            <div>姓名</div>
            <div>科室</div>
            <div>职称</div>
            <div>电话</div>
            <div>邮箱</div>
            <div>操作</div>
          </div>
          <div class="row" v-for="d in doctors" :key="d.doctorId">
            <div class="cell-strong">{{ d.username }}</div>
            <div>{{ d.deptName || '-' }}</div>
            <div>{{ d.title || '-' }}</div>
            <div>{{ d.phone || '-' }}</div>
            <div>{{ d.email || '-' }}</div>
            <div class="actions">
              <button class="btn primary" @click="openEdit(d)">编辑</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="modal">
      <div v-if="editing" class="modal" @click.self="closeEdit">
        <div class="dialog">
          <div class="dialog-header">
            <h3>编辑医生信息</h3>
            <button class="close" @click="closeEdit">✕</button>
          </div>
          <div class="form">
            <label>职称</label>
            <select v-model="form.title">
              <option value="住院医师">住院医师</option>
              <option value="主治医师">主治医师</option>
              <option value="副主任医师">副主任医师</option>
              <option value="主任医师">主任医师</option>
            </select>

            <label>简介</label>
            <textarea v-model="form.bio" rows="4"></textarea>
          </div>
          <div class="dialog-actions">
            <button class="btn ghost" @click="closeEdit">取消</button>
            <button class="btn success" @click="saveEdit">保存并通过</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
 </template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const doctors = ref([])
const loading = ref(false)
const keyword = ref('')

const editing = ref(false)
const currentId = ref(null)
const form = ref({ title: '住院医师', bio: '' })

async function fetchDoctors() {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const { data } = await axios.get('/api/admin/doctors/list', {
      params: { username: keyword.value || undefined },
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data?.code === 200) {
      doctors.value = data.data || []
    } else {
      doctors.value = []
    }
  } catch (e) {
    console.error('获取医生列表失败', e)
    alert('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

function openEdit(d) {
  currentId.value = d.doctorId
  form.value = { title: d.title || '住院医师', bio: d.bio || '' }
  editing.value = true
}

function closeEdit() {
  editing.value = false
  currentId.value = null
}

async function saveEdit() {
  if (!currentId.value) return
  try {
    const token = localStorage.getItem('token')
    await axios.put(`/api/admin/doctors/update/${currentId.value}`, form.value, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    alert('已保存并通过')
    closeEdit()
    fetchDoctors()
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败，请稍后再试')
  }
}

fetchDoctors()
</script>

<style scoped>
.audit-container { padding: 2rem; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); overflow: hidden; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #f0f0f0; }
.title-group h2 { margin: 0; color: #2d3748; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: .25rem 0 0 0; color: #718096; font-size: .9rem; }
.filters { display: flex; gap: .5rem; align-items: center; }
.input { padding: 8px 10px; border: 2px solid #e2e8f0; border-radius: 8px; }
.btn { padding: 8px 12px; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.btn.ghost { background: #fff; border: 2px solid #e2e8f0; color: #4a5568; }
.btn.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
.btn.success { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); color: #fff; }

.loading, .empty { padding: 2rem; color: #888; text-align: center; }
.table { display: grid; gap: 8px; padding: 1rem 1.5rem 1.5rem; }
.thead, .row { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr 1.2fr 1fr; gap: 8px; align-items: center; }
.thead { background: #667eea; padding: 12px; border-radius: 10px; font-weight: 700; color: #fff; }
.row { background: #fff; border: 2px solid #e2e8f0; border-radius: 10px; padding: 12px; transition: .2s; }
.row:hover { border-color: #cbd5e0; box-shadow: 0 4px 12px rgba(0,0,0,.05); }
.cell-strong { font-weight: 600; color: #2d3748; }
.actions { display: flex; gap: 8px; justify-content: center; }

/* modal */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: #fff; width: 520px; border-radius: 12px; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #eee; }
.close { border: none; background: transparent; font-size: 18px; cursor: pointer; }
.form { display: grid; gap: 10px; padding: 16px; }
select, textarea { border: 1px solid #ddd; border-radius: 6px; padding: 8px 10px; }
.dialog-actions { display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid #eee; justify-content: flex-end; }

.modal-enter-active, .modal-leave-active { transition: opacity .25s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>


