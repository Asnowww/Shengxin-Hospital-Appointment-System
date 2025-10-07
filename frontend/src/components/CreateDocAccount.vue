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
// import axios from 'axios' // 后端完成后可替换使用

// 表单数据
const doctor = ref({
  name: '',
  jobNumber: '',
//   department: '',
})

// 提示信息
const message = ref('')

// 模拟“创建医生账号”接口
const createAccount = async () => {
  if (!doctor.value.name || !doctor.value.jobNumber || !doctor.value.department) {
    message.value = '请完整填写医生信息'
    return
  }

  message.value = '正在创建账号...'

  // 模拟接口延迟
  await new Promise((r) => setTimeout(r, 800))

  // 模拟接口响应
  const res = {
    code: 200,
    data: {
      id: Math.floor(Math.random() * 10000),
      ...doctor.value,
    },
    msg: '账号创建成功',
  }

  if (res.code === 200) {
    message.value = `✅ ${res.msg}（账号ID：${res.data.id}）`
    // 创建成功后清空输入
    doctor.value = {
      name: '',
      jobNumber: '',
    //   department: '',
    }
  } else {
    message.value = '❌ 创建失败，请稍后重试'
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
