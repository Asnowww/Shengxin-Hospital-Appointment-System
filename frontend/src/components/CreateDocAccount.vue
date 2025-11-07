<template>
  <div class="create-doc-account">
    <h2>添加医生账号</h2>

    <div class="form-container">
      <div class="form-item">
        <label>用户名 / 医生姓名：</label>
        <input v-model="doctor.name" type="text" placeholder="请输入用户名（姓名）" />
      </div>

      <div class="form-item">
        <label>手机号：</label>
        <input v-model="doctor.phone" type="tel" placeholder="请输入手机号（必填）" />
      </div>

      <div class="form-item">
        <label>邮箱：</label>
        <input v-model="doctor.email" type="email" placeholder="请输入邮箱（可选）" />
      </div>

      <div class="form-item">
        <label>性别：</label>
        <select v-model="doctor.gender">
          <option value="">未选择</option>
          <option value="male">男</option>
          <option value="female">女</option>
        </select>
      </div>

      <div class="form-item">
        <label>所属科室：</label>
        <select v-model.number="doctor.deptId">
          <option value="">请选择科室（必选）</option>
          <option v-for="item in deptOptions" :key="item.deptId" :value="item.deptId">{{ item.deptName }}</option>
        </select>
      </div>

      <div class="form-item">
        <label>职称：</label>
        <select v-model="doctor.title">
          <option value="">请选择（默认：住院医师）</option>
          <option value="住院医师">住院医师</option>
          <option value="主治医师">主治医师</option>
          <option value="副主任医师">副主任医师</option>
          <option value="主任医师">主任医师</option>
        </select>
      </div>

      <div class="form-item">
        <label>简介：</label>
        <textarea v-model="doctor.bio" rows="3" placeholder="医生简介（可选）"></textarea>
      </div>

      <button class="submit-btn" @click="createAccount">创建账号</button>

      <p v-if="message" class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
const emit = defineEmits(['created'])

// 表单数据
const doctor = ref({
  name: '',
  phone: '',
  email: '',
  gender: '',
  deptId: '',
  title: '',
  bio: '',
  jobNumber: ''
})

// 提示信息
const message = ref('')

// 科室下拉
const deptOptions = ref([])
async function fetchDepartments() {
  try {
    const { data } = await axios.get('/api/departments/all')
    if (data && data.code === 200) {
      const flat = []
      const walk = (nodes) => {
        if (!Array.isArray(nodes)) return
        nodes.forEach(n => {
          if (n?.deptId && n?.deptName) flat.push({ deptId: n.deptId, deptName: n.deptName })
          if (n.children && n.children.length) walk(n.children)
        })
      }
      walk(data.data)
      const seen = new Set()
      deptOptions.value = flat.filter(it => {
        if (seen.has(it.deptId)) return false
        seen.add(it.deptId)
        return true
      })
    }
  } catch (e) {
    console.warn('获取科室列表失败', e)
  }
}
onMounted(fetchDepartments)

// 接入后端“创建医生账号”接口
const createAccount = async () => {
  if (!doctor.value.name) {
    message.value = '请填写用户名（姓名）'
    return
  }
  if (!doctor.value.phone) {
    message.value = '请填写手机号'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(doctor.value.phone)) {
    message.value = '请输入有效的中国大陆手机号'
    return
  }
  if (!doctor.value.deptId) {
    message.value = '请选择科室'
    return
  }
  try {
    message.value = '正在创建账号...'
    const token = localStorage.getItem('token')
    const payloadGender = doctor.value.gender === 'male' ? 'M' : (doctor.value.gender === 'female' ? 'F' : undefined)
    const payload = {
      username: doctor.value.name,
      phone: doctor.value.phone,
      email: doctor.value.email || undefined,
      gender: payloadGender,
      deptId: doctor.value.deptId,
      title: (doctor.value.title && doctor.value.title.trim()) ? doctor.value.title.trim() : '住院医师',
      bio: (doctor.value.bio ?? '')
    }
    const { data } = await axios.post('/api/admin/doctors/add', payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data && data.code === 200) {
      message.value = `✅ ${data.message || '账号创建成功'}`
      emit('created')
      doctor.value = { name: '', phone: '', email: '', gender: '', deptId: '', title: '', bio: '', jobNumber: '' }
    } else {
      message.value = `❌ 创建失败：${data?.message || ''}`
    }
  } catch (e) {
    const backendMsg = e?.response?.data?.message || e?.response?.data?.msg
    message.value = `❌ 创建失败：${backendMsg || '请稍后重试'}`
    console.error('创建医生失败:', e)
  }
}

// 未来替换真实接口时：
// const createAccount = async () => {
//   try {
//     const res = await axios.post('/api/doctors/create', doctor.value)
//     message.value = res.data.msg || '创建成功'
//   } catch (e) {
//     message.value = '创建失败，请检查网络'
//   }
// }
</script>

<style scoped>
.create-doc-account {
  max-width: 600px;
  margin: 40px auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 32px 40px;
}

h2 {
  text-align: center;
  margin-bottom: 28px;
  color: #333;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #444;
}

input {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
}

select {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  background: #fff;
}

textarea {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  resize: vertical;
}

input:focus {
  border-color: #4f9cf9;
  box-shadow: 0 0 4px rgba(79, 156, 249, 0.4);
}

.submit-btn {
  margin-top: 10px;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background-color: #4f9cf9;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.submit-btn:hover {
  background-color: #388ef5;
}

.message {
  margin-top: 12px;
  text-align: center;
  color: #333;
  font-size: 15px;
}
</style>
