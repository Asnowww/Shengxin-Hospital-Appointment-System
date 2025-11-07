<template>
  <div class="audit-container">
    <h2>身份认证审核</h2>
    <table class="audit-table">
      <thead>
        <tr>
          <th>用户名</th>
          <th>用户账号</th>
          <th>身份证照片</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in verifications" :key="item.id">
          <td>{{ item.username }}</td>
          <td>{{ item.userAccount }}</td>
          <td>
            <img :src="item.docUrl" alt="ID photo" class="id-photo" />
          </td>
          <td>
            <button @click="review(item.verificationId, 'approved')" class="approve-btn">通过</button>
            <button @click="review(item.verificationId, 'rejected')" class="reject-btn">拒绝</button>
          </td>
        </tr>
        <tr v-if="verifications.length === 0">
          <td colspan="4" class="empty">暂无待审核记录</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const verifications = ref([])

// 拉取所有待审核记录
async function fetchPending() {
  try {
    const res = await axios.get('/api/verifications/pending') // 后端新增接口
    verifications.value = res.data.data.map(item => ({
      verificationId: item.verificationId,
      username: item.userName || item.username, // 后端字段可能是 username
      userAccount: item.userAccount || item.userId,
      docUrl: item.docUrl
    }))
  } catch (err) {
    console.error('获取待审核列表失败', err)
  }
}

// 审核操作
async function review(verificationId, status) {
  const reason = status === 'rejected' ? prompt('请输入拒绝理由') : null
  try {
    const token = localStorage.getItem('token')
    if (!verificationId) {
      alert('缺少审核记录ID')
      return
    }
    // await axios.post('/api/verifications/review', null, {
    //   params: {
    //     verificationId,
    //     result: status,  // ✅ 拼写正确
    //     reason
    //   },
    //   headers: {         // ✅ 小写
    //     'Authorization': `Bearer ${token}`
    //   }
    // })
    const formData = new URLSearchParams()
    formData.append('verificationId', verificationId)
    formData.append('result', status)
    if (reason) formData.append('reason', reason)

    await axios.post('/api/verifications/review', formData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    alert('操作成功')
    fetchPending()
  } catch (err) {
    console.error('审核失败', err)
    alert('审核失败')
  }
}


onMounted(() => {
  fetchPending()
})
</script>

<style scoped>
.audit-container {
  padding: 2rem;
  background: #f9f9f9;
  border-radius: 12px;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.audit-table th,
.audit-table td {
  padding: 1rem;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.audit-table th {
  background: #667eea;
  color: white;
}

.id-photo {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

button {
  padding: 0.5rem 1rem;
  margin: 0 0.25rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: white;
}

.approve-btn {
  background-color: #48bb78;
}

.reject-btn {
  background-color: #f56565;
}

.empty {
  text-align: center;
  color: #999;
  padding: 1rem;
}
</style>
