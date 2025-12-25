<template>
    <div :class="['message-item', isMine ? 'mine' : 'other']">
        <div class="message-content">
            <div class="text">{{ message.content }}</div>
            <div class="time">{{ message.timestamp }}</div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    message: {
        type: Object,
        required: true
    },
    isMine: {
        type: Boolean,
        required: true
    }
});
</script>

<style scoped>
.message-item {
    display: flex;
    margin-bottom: 15px;
}
.mine {
    justify-content: flex-end; /* 自己的消息靠右 */
}
.other {
    justify-content: flex-start; /* 对方的消息靠左 */
}
.message-content {
    max-width: 60%;
    padding: 8px 12px;
    border-radius: 10px;
    position: relative;
    line-height: 1.5;
}
.mine .message-content {
    background-color: #cceeff; /* 浅蓝色 */
}
.other .message-content {
    background-color: #f0f0f0; /* 浅灰色 */
}
.time {
    font-size: 0.7em;
    color: #999;
    text-align: right;
    margin-top: 4px;
}
</style>