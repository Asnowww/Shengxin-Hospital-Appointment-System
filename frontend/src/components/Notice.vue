<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="notice-mask">
        <div class="notice-container">
          <header class="notice-header">
            <h3>圣心医院预约挂号系统挂号须知</h3>
          </header>

          <main class="notice-content">
            <section>
              <h4>一、挂号规则</h4>
              <p><strong>1. 预约时间：</strong>圣心医院挂号预约系统提供24小时预约服务，提前一周（7天）开放号源。每日11:00后禁止预约当日上午时间段的号源，16:00后禁止预约当日全部时间段的号源。（北京时间）</p>
              <p><strong>2. 取消预约：</strong>如因个人原因不能在预约时间段前来就诊，可在预约前一日17：00前在【个人中心】-【我的预约】-【当前预约】自行取消预约。</p>
              <p><strong>3. 临时停诊：</strong>因我院医师承担医、教、研等工作的特殊性，可能会出现医师临时停诊的情况。如遇此情况，我们会尽量将您的预约调整到同时段可用的医生排班，并通过邮件通知；您可以在收到邮件的24小时内前往【个人中心】-【我的预约】-【当前预约】中进行确认，需重新支付挂号费用。</p>
            </section>

            <section>
              <h4>二、预约限制</h4>
              <p><strong>1. 预约限制：</strong>为了保证预约的公平性，同一患者、同一时间段只能预约一次。</p>
              <p><strong>2. 爽约规则：</strong>预约成功后就诊当日未至或预约成功后就诊当日退号视为爽约。预约后若无法就诊，请及时取消，否则将计入爽约。三个月内累计爽约2次，限制预约12周，但不影响到院后现场挂号。</p>
              <p><strong>3. 多次取消：</strong>预约成功后又取消预约，若一周内（7天）主动取消达3次，限制预约4周，但不影响到院后现场挂号。</p>
            </section>

            <section>
              <h4>三、温馨提示</h4>
              <p>1. 就诊挂号当日有效，隔日自动作废。</p>
              <p>2. 欢迎使用【在线问诊】【智能问诊】功能进行线上咨询。</p>
            </section>
          </main>

          <footer class="notice-footer">
            <button class="confirm-btn" @click="handleClose">我已知晓</button>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

// 接收父组件控制显示的逻辑
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
/* 遮罩层 */
.notice-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 3000;
  backdrop-filter: blur(4px);
}

/* 容器 */
.notice-container {
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

/* 标题栏 */
.notice-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
}

.notice-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.25rem;
}

/* 内容区 */
.notice-content {
  padding: 20px;
  overflow-y: auto;
  line-height: 1.6;
  color: #444;
  font-size: 14px;
  text-align: justify;
}

.notice-content h4 {
  color: #1a73e8;
  margin: 15px 0 8px 0;
}

.notice-content p {
  margin-bottom: 10px;
}

/* 底部按钮 */
.notice-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: center;
}

.confirm-btn {
  background-color: #1a73e8;
  color: white;
  border: none;
  padding: 10px 40px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.confirm-btn:hover {
  background-color: #1557b0;
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>