<template>
  <div class="create-doc-account">
    <h2>添加医生账号</h2>

    <div class="form-container">
      <div class="form-item">
        <label>医生姓名：</label>
        <input v-model="doctor.name" type="text" placeholder="请输入医生姓名" />
      </div>

      <div class="form-item">
        <label>工号：</label>
        <input v-model="doctor.jobNumber" type="text" placeholder="请输入工号" />
      </div>

      
      <!-- <div class="form-item">
        <label>所属科室：</label>
        <input v-model="doctor.department" type="text" placeholder="请输入科室" />
      </div> -->

      <button class="submit-btn" @click="createAccount">创建账号</button>

      <p v-if="message" class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
const emit = defineEmits(['created'])

// 表单数据
const doctor = ref({
  name: '',
  jobNumber: '',
//   department: '',
})

// 提示信息
const message = ref('')

// 接入后端“创建医生账号”接口
const createAccount = async () => {
  if (!doctor.value.name) {
    message.value = '请填写医生姓名（用户名）'
    return
  }
  try {
    message.value = '正在创建账号...'
    const token = localStorage.getItem('token')
    // 后端 DoctorAccountDTO 主要字段：username、deptId、title 等
    const payload = {
      username: doctor.value.name
      // TODO: 可扩展 deptId、title、email、phone 等字段
    }
    const { data } = await axios.post('/api/admin/doctors/add', payload, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (data && data.code === 200) {
      message.value = `✅ ${data.message || '账号创建成功'}`
      emit('created')
      doctor.value = { name: '', jobNumber: '' }
    } else {
      message.value = `❌ 创建失败：${data?.message || ''}`
    }
  } catch (e) {
    message.value = '❌ 创建失败，请稍后重试'
    console.error(e)
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
