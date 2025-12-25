<template>
  <div class="notice-page">
    <div class="glass-container">
      <div class="header-section">
        <h1 class="title">圣心医院预约挂号须知</h1>
        <p class="subtitle">Shengxin Hospital Appointment Registration Notice</p>
      </div>

      <div class="content-scroll">
        <!-- 挂号规则 -->
        <section class="notice-card">
          <div class="card-header">
            <span class="icon-wrapper blue">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
            </span>
            <h3>挂号规则</h3>
          </div>
          <div class="card-content">
            <div class="rule-item">
              <span class="rule-title">预约时间</span>
              <p>我院提供24小时预约服务，提前一周（7天）开放号源。</p>
              <ul class="sub-list">
                <li>每日 <strong>11:00</strong> 后禁止预约当日上午时间段的号源</li>
                <li>每日 <strong>16:00</strong> 后禁止预约当日全部时间段的号源</li>
              </ul>
            </div>
            
            <div class="rule-item">
              <span class="rule-title">取消预约</span>
              <p>如因个人原因不能就诊，请在<strong>预约前一日 17:00</strong> 前取消。</p>
              <p class="path-hint">路径：【个人中心】-【我的预约】-【当前预约】</p>
            </div>

            <div class="rule-item">
              <span class="rule-title">医师停诊</span>
              <p>如遇医师临时停诊，我们将尽量为您调整至同时间段其他医生，并通过邮件通知。</p>
              <p>请在收到通知后24小时内确认，需重新支付挂号费用。</p>
            </div>
          </div>
        </section>

        <!-- 预约限制 -->
        <section class="notice-card">
          <div class="card-header">
            <span class="icon-wrapper orange">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86 7.86 2"></polygon><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
            </span>
            <h3>预约限制与违约</h3>
          </div>
          <div class="card-content">
            <div class="limit-grid">
              <div class="limit-box">
                <h4>公平性原则</h4>
                <p>同一患者、同一时间段只能预约一次。</p>
              </div>
              <div class="limit-box warning">
                <h4>爽约规则</h4>
                <p>未按时就诊或当日退号视为爽约。</p>
                <p class="small">3个月累计爽约2次 → 限制预约12周</p>
              </div>
              <div class="limit-box warning">
                <h4>多次取消</h4>
                <p>一周内主动取消达3次 → 限制预约4周</p>
                <p class="small">（但不影响到院后现场挂号）</p>
              </div>
              <div class="limit-box danger">
                <h4>恶意抢号</h4>
                <p>预约频率 < 1分钟 → 警告1次</p>
                <p class="small">24小时警告≥3次 → 限制预约48周</p>
              </div>
            </div>
            
            <div class="misc-limits">
              <h4>详细限制条款：</h4>
              <ul class="limit-list">
                <li>预约频率不可小于1分钟，否则触发警告；</li>
                <li>不可预约同时段排班、同一排班、同一时段的排班；</li>
                <li>已在该排班候补队列时，不可重复预约；</li>
                <li>禁用时间到期后会自动解禁。</li>
              </ul>
            </div>
          </div>
        </section>

        <!-- 温馨提示 -->
        <section class="notice-card simple">
          <div class="card-header">
            <span class="icon-wrapper green">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path></svg>
            </span>
            <h3>温馨提示</h3>
          </div>
          <div class="card-content">
            <p>1. 就诊挂号当日有效，隔日自动作废。</p>
            <p>2. 欢迎使用【在线问诊】【智能问诊】功能进行线上咨询。</p>
          </div>
        </section>
      </div>

      <div class="footer-action">
        <button class="agree-link" @click="handleAgree">
          我已阅读并知晓上述规则 
          <span class="arrow">→</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleAgree = () => {
  // 记录已阅读状态到本次会话
  sessionStorage.setItem('hasSeenNotice', 'true')
  router.push('/home')
}
</script>

<style scoped>
.notice-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.glass-container {
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-section {
  padding: 2rem 2.5rem 1.5rem;
  text-align: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.subtitle {
  margin-top: 0.5rem;
  color: #7f8c8d;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 2rem 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 滚动条美化 */
.content-scroll::-webkit-scrollbar {
  width: 6px;
}
.content-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.notice-card {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.8);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.icon-wrapper.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.icon-wrapper.orange { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
.icon-wrapper.green { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }

.card-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2d3436;
}

.card-content {
  padding-left: 3.25rem; /* 对齐图标右侧 */
  color: #57606f;
  line-height: 1.6;
}

/* 规则项样式 */
.rule-item {
  margin-bottom: 1.25rem;
}

.rule-title {
  display: block;
  font-weight: 600;
  color: #34495e;
  margin-bottom: 0.25rem;
  position: relative;
}

.rule-title::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 8px;
  width: 6px;
  height: 6px;
  background: #cbd5e0;
  border-radius: 50%;
}

.sub-list {
  margin: 0.5rem 0 0 0;
  padding-left: 1.25rem;
  font-size: 0.9em;
  color: #747d8c;
}

.path-hint {
  font-size: 0.85em;
  color: #a4b0be;
  margin-top: 0.25rem;
}

/* 限制网格 */
.limit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.limit-box {
  background: #fff;
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid #f1f2f6;
}

.limit-box h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.95rem;
  color: #2f3542;
}

.limit-box p {
  margin: 0;
  font-size: 0.85rem;
  color: #747d8c;
}

.limit-box.warning { border-left: 4px solid #ffa502; }
.limit-box.danger { border-left: 4px solid #ff4757; }

.limit-box .small {
  font-size: 0.75rem;
  color: #a4b0be;
  margin-top: 0.5rem;
}

.misc-limits {
  font-size: 0.85rem;
  color: #747d8c;
  border-top: 1px dashed #e0e6ed;
  padding-top: 1rem;
  margin-top: 1rem;
}

.misc-limits h4 {
  margin: 0 0 0.5rem 0;
  color: #2f3542;
  font-size: 0.9rem;
  font-weight: 600;
}

.limit-list {
  margin: 0;
  padding-left: 1.25rem;
  list-style-type: disc;
}

.limit-list li {
  margin-bottom: 0.25rem;
  line-height: 1.5;
}

/* 底部按钮 */
.footer-action {
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.4);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
}

.agree-link {
  background: none;
  border: none;
  padding: 0.5rem 1rem;
  color: #718096;
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  position: relative;
}

.agree-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 1px;
  background: #718096;
  transition: width 0.3s ease;
}

.agree-link:hover {
  color: #2d3436;
}

.agree-link:hover::after {
  width: 100%;
}

.arrow {
  transition: transform 0.3s ease;
}

.agree-link:hover .arrow {
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .notice-page { padding: 1rem; }
  .content-scroll { padding: 1.5rem; }
  .card-content { padding-left: 0; margin-top: 1rem; }
  .limit-grid { grid-template-columns: 1fr; }
}
</style>
