<template>
  <div class="register-container">
    <form @submit.prevent="handleRegister">
      <h2>用户注册</h2>

      <div class="form-row">
        <label>身份 <span class="required">*</span></label>
        <select v-model="form.role" required>
          <option value="">请选择</option>
          <option value="teacher">教师</option>
          <option value="student">学生</option>
        </select>
      </div>

      <div class="form-row">
        <label>真实姓名 <span class="required">*</span></label>
        <input v-model="form.name" type="text" required />
      </div>

      <div class="form-row">
        <label>密码 <span class="required">*</span></label>
        <input v-model="form.password" type="password" required />
      </div>

      <div class="form-row">
        <label>确认密码 <span class="required">*</span></label>
        <input v-model="form.confirmPassword" type="password" required />
      </div>

      <div class="form-row">
        <label>手机号码 <span class="required">*</span></label>
        <input v-model="form.phone" type="tel" required />
      </div>

      <!-- 可选信息 -->
      <div class="form-row">
        <label>性别</label>
        <select v-model="form.gender">
          <option value="">请选择</option>
          <option value="male">男</option>
          <option value="female">女</option>
        </select>
      </div>

      <div class="form-row">
        <label>出生年月</label>
        <input v-model="form.birth" type="date" />
      </div>

      <button type="submit" class="submit-btn">注册</button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  role: '', name: '', password: '', confirmPassword: '', phone: '', gender: '', birth: ''
})

function handleRegister() {
  if (form.password !== form.confirmPassword) return alert('两次密码不一致')
  console.log('注册信息', form)
  alert('注册成功！')
}
</script>

<style scoped>
.register-container {
  width: 100%;
  max-width: 480px;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-top: 82px; /* 避开导航栏 */
  box-sizing: border-box;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

label {
  font-weight: 500;
  margin-right: 8px;
}

input, select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}
</style>
