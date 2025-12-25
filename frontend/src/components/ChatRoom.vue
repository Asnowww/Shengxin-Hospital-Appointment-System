<template>
    <div class="chat-room-container">
        <div ref="messageContainer" class="message-list">
            <MessageItem 
                v-for="msg in messages" 
                :key="msg.id" 
                :message="msg"
                :is-mine="msg.senderId === currentUserId" 
            />
        </div>
        <div class="input-area">
            <input 
                v-model="inputMessage" 
                @keyup.enter="handleSend" 
                :placeholder="isChatEnded ? '本次问诊已结束，无法继续发送消息' : '输入您的消息...'"
                :disabled="isChatEnded"
            />
            <button @click="handleSend" :disabled="isChatEnded || !inputMessage.trim() || isSending">
                {{ isSending ? '发送中...' : '发送' }}
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import { sendMessageApi } from '../stores/api';
import MessageItem from './MessageItem.vue';

const props = defineProps({
    roomId: { type: [String, Number], required: true },
    sessionId: { type: [String, Number], default: null },
    receiverId: { type: [String, Number], default: null },
    sessionStatus: { type: String, default: 'open' },
    // 用于父组件触发“结束问诊”的信号，每次递增一次即可
    endSignal: { type: Number, default: 0 },
    messages: { type: Array, required: true },
    currentUserId: { type: [String, Number], required: true },
    currentRole: { type: String, required: true }, // doctor / patient
});


const emit = defineEmits(['message-sent', 'message-received']);

const inputMessage = ref('');
const messageContainer = ref(null);
const isSending = ref(false);
const socket = ref(null);
const isSocketOpen = ref(false);
const closingBySelf = ref(false);
const isChatEnded = ref(false);

const scrollToBottom = () => {
    nextTick(() => {
        const container = messageContainer.value;
        if (container) {
            container.scrollTop = container.scrollHeight;
        }
    });
};

// 监听父组件传入的消息列表变化时滚动
watch(() => props.messages, scrollToBottom, { deep: true });

// 监听会话状态（open/closed）
watch(
  () => props.sessionStatus,
  (status) => {
    isChatEnded.value = (status === 'closed')
  },
  { immediate: true }
)

// 监听切换会话时自动更新状态并重连 WebSocket - Bug #5 修复
watch(
  () => props.sessionId,
  (newVal, oldVal) => {
    if (newVal !== oldVal && newVal) {
      // 关闭旧连接
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        closingBySelf.value = true
        socket.value.close()
      }
      // 重置状态并建立新连接
      closingBySelf.value = false
      isChatEnded.value = (props.sessionStatus === 'closed')
      setupWebSocket()
    }
  }
)

// 监听 endSignal 变化，结束当前会话
watch(
    () => props.endSignal,
    (newVal, oldVal) => {
        if (newVal !== oldVal) {
            handleEndChatByParent();
        }
    }
);

const buildWebSocketUrl = () => {
    // 优先使用环境变量指定的地址，避免前后端端口不一致问题
    // 例如：VITE_WS_BASE_URL=ws://localhost:8080
    const base =
        import.meta.env.VITE_WS_BASE_URL ||
        `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.hostname}:8080`;
    const userId = encodeURIComponent(props.currentUserId);
    const role = encodeURIComponent(props.currentRole);
    return `${base}/ws/chat?userId=${userId}&role=${role}`;
};

const setupWebSocket = () => {
    try {
        const url = buildWebSocketUrl();
        socket.value = new WebSocket(url);

        socket.value.onopen = () => {
            isSocketOpen.value = true;
            console.log('WebSocket 连接已建立:', url);
        };

        socket.value.onclose = () => {
            isSocketOpen.value = false;
            console.log('WebSocket 连接已关闭');
            // 如果不是本端主动关闭，则提示“对方已关闭聊天”
            if (!closingBySelf.value) {
                isChatEnded.value = true;
                const systemMsg = {
                    id: Date.now(),
                    senderId: null,
                    role: 'system',
                    content: '对方已关闭聊天，此次问诊结束。',
                    timestamp: new Date().toLocaleTimeString('zh-CN', { hour12: false })
                };
                emit('message-received', systemMsg);
                scrollToBottom();
            }
        };

        socket.value.onerror = (err) => {
            console.error('WebSocket 发生错误:', err);
        };

        socket.value.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                // 处理会话关闭通知
                if (data.type === 'session_closed') {
                    // 检查是否是当前会话
                    if (String(data.sessionId) === String(props.sessionId)) {
                        isChatEnded.value = true;
                        const systemMsg = {
                            id: Date.now(),
                            senderId: null,
                            role: 'system',
                            content: '此会话已被关闭。',
                            timestamp: new Date().toLocaleTimeString('zh-CN', { hour12: false })
                        };
                        emit('message-received', systemMsg);
                        scrollToBottom();
                    }
                    return;
                }
                // 将后端 DTO 映射为前端 MessageItem 可消费的结构
                const mapped = {
                    id: data.id || Date.now(),
                    senderId: data.senderId,
                    role: data.senderType === 'doctor' ? 'doctor' : 'patient',
                    content: data.content,
                    timestamp: data.createdAt || new Date().toLocaleTimeString('zh-CN', { hour12: false })
                };
                emit('message-received', mapped);
                scrollToBottom();
            } catch (e) {
                console.error('解析 WebSocket 消息失败:', e, event.data);
            }
        };
    } catch (e) {
        console.error('初始化 WebSocket 失败:', e);
    }
};

const handleSend = async () => {
    const content = inputMessage.value.trim();
    if (!content || isSending.value) return;

    isSending.value = true;

    // 与后端 ChatMessageDTO 对齐的消息结构
    const dto = {
        // 注意：不再对 sessionId 做 Number() 转换，避免长整型精度丢失
        sessionId: props.sessionId || props.roomId || null,
        senderId: Number(props.currentUserId),
        senderType: props.currentRole, // 'doctor' / 'patient'
        receiverId: props.receiverId ? Number(props.receiverId) : null,
        content: content,
        contentType: 'text',
        appointmentId: null
    };


    try {
        if (socket.value && isSocketOpen.value) {
            socket.value.send(JSON.stringify(dto));
        } else {
            // 作为降级方案，仍然调用本地 mock API，保证页面在未连上后端时可用
            const fallbackMessage = {
                id: Date.now(),
                senderId: props.currentUserId,
                role: props.currentRole,
                content: content,
                timestamp: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
            };
            await sendMessageApi(props.roomId, fallbackMessage);
        }

        inputMessage.value = '';
        emit('message-sent'); 
        scrollToBottom();
    } catch (error) {
        console.error('发送消息失败:', error);
        alert('消息发送失败，请重试。');
    } finally {
        isSending.value = false;
    }
};

// 父组件触发的结束会话逻辑
const handleEndChatByParent = async () => {
    // 避免重复处理
    if (closingBySelf.value) return;
    closingBySelf.value = true;
    isChatEnded.value = true;

    // 在本端追加一条提示消息
    const localMsg = {
        id: Date.now(),
        senderId: null,
        role: 'system',
        content: '您已关闭聊天，此次问诊结束。',
        timestamp: new Date().toLocaleTimeString('zh-CN', { hour12: false })
    };
    emit('message-received', localMsg);
    scrollToBottom();

    // 主动关闭 WebSocket 连接（不强制发送额外协议消息，简单干净）
    if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.close();
    }
};

onMounted(() => {
    setupWebSocket();
    nextTick(scrollToBottom); // 初始加载时滚动
});

onBeforeUnmount(() => {
    if (socket.value) {
        socket.value.close();
    }
});
</script>

<style scoped>
/* 样式与原方案保持一致 */
.chat-room-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    border: 1px solid #ddd;
}
/* ... 消息列表和输入区域样式 ... */
.message-list {
    flex-grow: 1;
    overflow-y: auto;
    padding: 10px;
    background-color: #f7f7f7;
}
.input-area {
    padding: 10px;
    border-top: 1px solid #ccc;
    display: flex;
}
.input-area input {
    flex-grow: 1;
    padding: 10px;
    border: 1px solid #eee;
    border-radius: 5px;
    margin-right: 10px;
}
.input-area button {
    padding: 10px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}
</style>