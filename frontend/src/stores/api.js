// api.js - 使用真实后端接口，替换原有模拟 API

import axios from 'axios'

/**
 * 获取科室列表（用于在线问诊左侧科室选择）
 * 复用 Department.vue 的 /api/departments/all 接口，
 * 但这里展开为「二级科室」列表（因为医生的 deptId 归属于二级科室）。
 */
export const fetchDepartments = async () => {
    const res = await axios.get('/api/departments/all')
    const list = res.data?.data || []

    const subDepartments = []

    list.forEach((dept) => {
        const children = Array.isArray(dept.children) ? dept.children : []
        children.forEach((sub) => {
            subDepartments.push({
                id: sub.deptId,
                name: sub.deptName
            })
        })
    })

    return subDepartments
}

/**
 * 获取某个科室下的医生列表
 * 后端接口（已确认）：GET /api/doctor/dept/{deptId}
 * 返回：Result<List<DoctorVO>>
 */
export const fetchDoctorsByDept = async (deptId) => {
    const res = await axios.get(`/api/doctor/dept/${deptId}`)

    const list = res.data?.data || []
    return list.map((d) => ({
        id: d.doctorId,
        name: d.doctorName,
        deptId: d.deptId,
        title: d.title,
        description: d.bio,
        // 若 DoctorVO 中已有在线状态字段（如 onlineStatus），可在此直接使用
        status: d.onlineStatus || d.status || 'busy'
    }))
}

/**
 * 创建（或获取）医生-患者的聊天会话
 * 对应后端：GET /api/chat/session?doctorId=&patientId=&appointmentId=
 * 返回 ChatSession，前端使用 session.id 作为房间 ID
 */
export const createChatSession = async (patientId, doctorId, appointmentId = null) => {
    const res = await axios.get('/api/chat/session', {
        params: {
            doctorId,
            patientId,
            appointmentId
        }
    })
    const session = res.data
    return session.id
}

/**
 * 获取某个会话的聊天记录（分页简单按第一页 50 条）
 * 对应后端：GET /api/chat/messages?sessionId=&userId=&page=&size=
 * 返回 ChatMessage 分页；这里展开 records 并映射为 ChatRoom 可用的结构
 */
export const fetchChatHistory = async (sessionId, userId = null, page = 1, size = 50) => {
    // 自动获取 userId（从 localStorage 获取当前登录用户）
    const currentUserId = userId || localStorage.getItem('doctorId') || localStorage.getItem('patientId')

    const res = await axios.get('/api/chat/messages', {
        params: {
            sessionId,
            userId: currentUserId,
            page,
            size
        }
    })

    const pageData = res.data
    const records = pageData?.records || pageData?.data?.records || []

    return records.map((m) => ({
        id: m.id,
        senderId: m.senderId,
        role: m.senderType === 'doctor' ? 'doctor' : 'patient',
        content: m.content,
        timestamp: m.createdAt
    }))
}

/**
 * 发送消息：当前实时发送通过 WebSocket 完成，这里仅保留一个兜底接口（可选）
 * 若需要 REST 发送，可以在后端新增 POST /api/chat/message 接口，并在此处调用。
 */
export const sendMessageApi = async (/* sessionId, message */) => {
    console.warn('sendMessageApi: 当前项目消息发送走 WebSocket，如需 HTTP 发送请在此实现。')
    return { success: true }
}

/**
 * 医生端：获取该医生的会话列表
 */
export const fetchDoctorSessions = async (doctorId) => {
    if (!doctorId) {
        throw new Error('doctorId is required')
    }
    const res = await axios.get(`/api/chat/sessions/doctor/${doctorId}`)
    return res.data || []
}

/**
 * 将会话消息标记为已读
 */
export const markMessagesRead = async (sessionId, userId) => {
    if (!sessionId || !userId) return
    await axios.post('/api/chat/messages/read', null, {
        params: { sessionId, userId }
    })
}

/**
 * 关闭会话（将 chat_session.status 设为 closed）
 * 需要传递 userId 以验证用户身份
 */
export const closeChatSession = async (sessionId) => {
    if (!sessionId) return
    const userId = localStorage.getItem('patientId') || localStorage.getItem('doctorId')
    await axios.post('/api/chat/session/close', null, {
        params: { sessionId, userId }
    })
}