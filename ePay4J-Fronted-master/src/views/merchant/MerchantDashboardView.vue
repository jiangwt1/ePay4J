<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="dash-header">
      <div class="header-left">
        <div class="header-greeting">
          <h1 class="greeting-title">
            <span class="greeting-label">Welcome back,</span>
            <span class="greeting-name">{{ userStore.merchantName || '商户' }}</span>
          </h1>
          <p class="greeting-sub" v-if="userStore.nickname">
            {{ userStore.nickname }} · 商户管理中心
          </p>
          <p class="greeting-sub" v-else>商户管理中心</p>
        </div>
      </div>
      <div class="header-right">
        <button class="icon-btn" @click="loadData" :class="{ spinning: loading }">
          <el-icon :size="18"><Refresh /></el-icon>
        </button>
      </div>
    </div>

    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card" v-for="(stat, i) in stats" :key="stat.label" :style="{ animationDelay: i * 0.08 + 's' }">
        <div class="stat-top">
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-icon">
            <component :is="stat.icon" />
          </span>
        </div>
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-bar">
          <div class="stat-bar-fill" :style="{ width: stat.barWidth }"></div>
        </div>
      </div>
    </div>

    <!-- Main Grid: Credentials + Quick Info -->
    <div class="main-grid">
      <!-- Credentials Card -->
      <div class="card cred-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon">
              <el-icon :size="18"><Key /></el-icon>
            </span>
            <span class="card-title">对接凭证</span>
          </div>
          <el-tooltip
            :disabled="!userStore.isActingMerchantView"
            content="管理员商户视角不能重置密钥，请回后台商户管理操作"
            placement="top"
          >
            <button class="text-btn" :disabled="userStore.isActingMerchantView" @click="handleResetKey">
              <el-icon :size="14"><RefreshRight /></el-icon> 重置密钥
            </button>
          </el-tooltip>
        </div>

        <div class="cred-rows">
          <!-- PID -->
          <div class="cred-row">
            <span class="cred-label">商户编号 (PID)</span>
            <div class="cred-value-row">
              <code class="cred-code pid">{{ userStore.merchantId || credentials.pid || '-' }}</code>
              <button class="copy-btn" @click="copyPid" title="复制">
                <el-icon :size="14"><CopyDocument /></el-icon>
              </button>
            </div>
          </div>

          <!-- Key -->
          <div class="cred-row">
            <span class="cred-label">密钥 (KEY)</span>
            <div class="cred-value-row">
              <code class="cred-code key" :class="{ masked: !showKey }">
                {{ showKey ? credentials.key : '••••••••••••••••••••••••••••••••' }}
              </code>
              <button class="toggle-btn" @click="showKey = !showKey">
                {{ showKey ? '隐藏' : '显示' }}
              </button>
              <button class="copy-btn" @click="copyKey" title="复制">
                <el-icon :size="14"><CopyDocument /></el-icon>
              </button>
            </div>
          </div>

          <div class="cred-divider"></div>

          <!-- Site Name -->
          <div class="cred-row">
            <span class="cred-label">站点名称</span>
            <span class="cred-text">{{ siteSettings.site_name || '-' }}</span>
          </div>

          <!-- API URL -->
          <div class="cred-row">
            <span class="cred-label">支付接口</span>
            <div class="cred-value-row">
              <code class="cred-code url">{{ siteSettings.pay_api_url || '-' }}</code>
              <button class="copy-btn" @click="copyApiUrl" title="复制">
                <el-icon :size="14"><CopyDocument /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Start Card -->
      <div class="card quickstart-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-icon">
              <el-icon :size="18"><Lightning /></el-icon>
            </span>
            <span class="card-title">快速接入</span>
          </div>
        </div>

        <div class="qs-steps">
          <div class="qs-step">
            <div class="qs-step-num">01</div>
            <div class="qs-step-content">
              <div class="qs-step-title">获取凭证</div>
              <div class="qs-step-desc">使用左侧的 PID 和 KEY 进行接口签名</div>
            </div>
          </div>
          <div class="qs-step">
            <div class="qs-step-num">02</div>
            <div class="qs-step-content">
              <div class="qs-step-title">发起支付</div>
              <div class="qs-step-desc">向支付接口地址发送 POST 请求创建订单</div>
            </div>
          </div>
          <div class="qs-step">
            <div class="qs-step-num">03</div>
            <div class="qs-step-content">
              <div class="qs-step-title">异步通知</div>
              <div class="qs-step-desc">支付完成后系统向您的 notify_url 发送回调</div>
            </div>
          </div>
        </div>

        <!-- Mini terminal -->
        <div class="qs-terminal">
          <div class="qs-term-bar">
            <span class="qs-dot"></span>
            <span class="qs-dot"></span>
            <span class="qs-dot"></span>
            <span class="qs-term-title">示例请求</span>
          </div>
          <div class="qs-term-body">
            <div class="qs-line"><span class="t-k">POST</span> <span class="t-v">{{ siteSettings.pay_api_url || '/api/pay' }}</span></div>
            <div class="qs-line"><span class="t-k">Content-Type:</span> <span class="t-v">application/json</span></div>
            <div class="qs-line q"></div>
            <div class="qs-line"><span class="t-s">{{ '{' }}</span></div>
            <div class="qs-line">  <span class="t-k">"pid"</span>: <span class="t-s">"{{ userStore.merchantId || credentials.pid || '1001' }}"</span>,</div>
            <div class="qs-line">  <span class="t-k">"type"</span>: <span class="t-s">"alipay"</span>,</div>
            <div class="qs-line">  <span class="t-k">"out_trade_no"</span>: <span class="t-s">"ORDER_{{ '{' }}timestamp{{ '}' }}"</span>,</div>
            <div class="qs-line">  <span class="t-k">"notify_url"</span>: <span class="t-s">"https://your-site.com/notify"</span></div>
            <div class="qs-line"><span class="t-s">{{ '}' }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useUserStore } from '../../store/modules/user'
import { getMerchantAccountInfo, getMerchantCredentials, getSiteSettings, resetMerchantKey } from '../../api/auth'
import { getMerchantOrders } from '../../api/order'
import { formatMoney } from '../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)

const summary = reactive({ totalCount: 0, successRate: '0.0' })
const accountBalance = ref(0)
const freezeBalance = ref(0)
const totalIncome = ref(0)
const showKey = ref(false)
const credentials = reactive({ pid: '-', key: '', name: '' })
const siteSettings = reactive({ site_name: '', pay_api_url: '' })

// SVG icon components
const IconTickets = () => h('svg', { width: 16, height: 16, viewBox: '0 0 16 16', fill: 'none', innerHTML: '<path d="M2 4H14M2 4V12C2 12.5523 2.44772 13 3 13H13C13.5523 13 14 12.5523 14 12V4M2 4V3C2 2.44772 2.44772 2 3 2H13C13.5523 2 14 2.44772 14 3V4M6 7H10M6 10H8" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>' })
const IconCheck = () => h('svg', { width: 16, height: 16, viewBox: '0 0 16 16', fill: 'none', innerHTML: '<circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/><path d="M5.5 8L7.2 9.7L10.5 6.3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>' })
const IconWallet = () => h('svg', { width: 16, height: 16, viewBox: '0 0 16 16', fill: 'none', innerHTML: '<rect x="1.5" y="4" width="13" height="9" rx="2" stroke="currentColor" stroke-width="1.2"/><path d="M1.5 7H14.5" stroke="currentColor" stroke-width="1.2"/><path d="M11 9.5H12" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>' })
const IconLock = () => h('svg', { width: 16, height: 16, viewBox: '0 0 16 16', fill: 'none', innerHTML: '<rect x="3" y="7" width="10" height="7" rx="2" stroke="currentColor" stroke-width="1.2"/><path d="M5.5 7V5C5.5 3.61929 6.61929 2.5 8 2.5C9.38071 2.5 10.5 3.61929 10.5 5V7" stroke="currentColor" stroke-width="1.2"/>' })
const IconTrend = () => h('svg', { width: 16, height: 16, viewBox: '0 0 16 16', fill: 'none', innerHTML: '<path d="M2 12L5.5 8.5L8.5 10.5L14 4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/><path d="M10 4H14V8" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>' })

const stats = computed(() => [
  {
    label: '订单总数',
    value: summary.totalCount,
    icon: IconTickets,
    barWidth: summary.totalCount > 0 ? '60%' : '5%',
  },
  {
    label: '成功率',
    value: summary.successRate + '%',
    icon: IconCheck,
    barWidth: summary.successRate + '%',
  },
  {
    label: '账户余额',
    value: '¥' + formatMoney(accountBalance.value),
    icon: IconWallet,
    barWidth: accountBalance.value > 0 ? '70%' : '5%',
  },
  {
    label: '冻结余额',
    value: '¥' + formatMoney(freezeBalance.value),
    icon: IconLock,
    barWidth: freezeBalance.value > 0 ? '40%' : '5%',
  },
  {
    label: '总收入',
    value: '¥' + formatMoney(totalIncome.value),
    icon: IconTrend,
    barWidth: totalIncome.value > 0 ? '85%' : '5%',
  },
])

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const [accountRes, orderRes, credRes, settingsRes] = await Promise.all([
      getMerchantAccountInfo(),
      getMerchantOrders({ page: 1, size: 100 }),
      getMerchantCredentials(),
      getSiteSettings(),
    ])
    const data = accountRes.data
    accountBalance.value = (data.availableBalance || 0) + (data.frozenBalance || 0)
    freezeBalance.value = data.frozenBalance || 0
    totalIncome.value = data.totalIncome || 0

    const total = orderRes.data.total || 0
    const records = orderRes.data.records || []
    summary.totalCount = total

    if (total > 0 && records.length > 0) {
      const successCount = records.filter(o => o.status === 1).length
      summary.successRate = ((successCount / records.length) * 100).toFixed(1)
    } else {
      summary.successRate = '0.0'
    }

    Object.assign(credentials, credRes.data)
    Object.assign(siteSettings, settingsRes.data || {})
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function copyPid() {
  const pid = userStore.merchantId || credentials.pid
  if (pid) navigator.clipboard.writeText(String(pid)).then(() => ElMessage.success('PID 已复制'))
}

function copyKey() {
  if (credentials.key) navigator.clipboard.writeText(credentials.key).then(() => ElMessage.success('密钥已复制'))
}

function copyApiUrl() {
  if (siteSettings.pay_api_url) navigator.clipboard.writeText(siteSettings.pay_api_url).then(() => ElMessage.success('接口地址已复制'))
}

async function handleResetKey() {
  if (userStore.isActingMerchantView) {
    ElMessage.warning('管理员商户视角不能重置密钥，请回后台商户管理操作')
    return
  }
  try {
    await ElMessageBox.confirm('重置后原密钥将立即失效，确定继续？', '重置密钥', { type: 'warning' })
    await resetMerchantKey()
    ElMessage.success('密钥已重置')
    loadData()
  } catch { /* cancel */ }
}
</script>

<style scoped>
.dashboard {
  position: relative;
}

/* ===== Header ===== */
.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
}

.greeting-title {
  margin: 0;
  line-height: 1.2;
}

.greeting-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--ep-text-muted);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.greeting-name {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: var(--ep-text-primary);
  letter-spacing: -0.5px;
}

.greeting-sub {
  font-size: 13px;
  color: var(--ep-text-secondary);
  margin: 8px 0 0;
}

.icon-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  background: var(--ep-bg-surface);
  color: var(--ep-text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.icon-btn:hover {
  border-color: var(--ep-border-active);
  color: var(--ep-text-secondary);
}

.icon-btn.spinning svg {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Stats Row ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: border-color 0.15s ease;
  animation: fadeSlideUp 0.5s ease both;
}

.stat-card:hover {
  border-color: var(--ep-border-active);
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--ep-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-icon {
  width: 28px;
  height: 28px;
  border-radius: var(--ep-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.04);
  color: var(--ep-text-muted);
}

.stat-value {
  font-family: 'Geist Mono', monospace;
  font-size: 22px;
  font-weight: 700;
  color: var(--ep-text-primary);
  letter-spacing: -0.5px;
}

.stat-bar {
  height: 3px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 2px;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  border-radius: 2px;
  background: var(--ep-text-muted);
  opacity: 0.4;
  transition: width 1s cubic-bezier(0.22, 1, 0.36, 1);
}

/* ===== Main Grid ===== */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* ===== Cards ===== */
.card {
  background: var(--ep-bg-surface);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 24px;
  animation: fadeSlideUp 0.6s ease 0.3s both;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.card-title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  width: 30px;
  height: 30px;
  border-radius: var(--ep-radius-sm);
  background: rgba(255, 255, 255, 0.04);
  color: var(--ep-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ep-text-primary);
}

.text-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  color: var(--ep-text-muted);
  font-size: 12px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--ep-radius-sm);
  transition: all 0.15s ease;
}

.text-btn:hover:not(:disabled) {
  color: var(--ep-text-secondary);
  background: rgba(255, 255, 255, 0.04);
}

.text-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

/* ===== Credentials ===== */
.cred-rows {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cred-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cred-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--ep-text-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.cred-value-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cred-code {
  font-family: 'Geist Mono', monospace;
  font-size: 13px;
  color: var(--ep-text-primary);
  background: var(--ep-input-bg);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  padding: 8px 12px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cred-code.pid {
  color: var(--ep-text-secondary);
}

.cred-code.masked {
  color: var(--ep-text-muted);
  letter-spacing: 3px;
}

.cred-code.url {
  font-size: 12px;
  color: var(--ep-text-muted);
}

.cred-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--ep-text-primary);
}

.copy-btn,
.toggle-btn {
  flex-shrink: 0;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius-sm);
  background: transparent;
  color: var(--ep-text-muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.copy-btn {
  width: 32px;
}

.toggle-btn {
  width: auto;
  padding: 0 10px;
  font-size: 11px;
  font-weight: 500;
}

.copy-btn:hover,
.toggle-btn:hover {
  border-color: var(--ep-border-active);
  color: var(--ep-text-secondary);
  background: rgba(255, 255, 255, 0.04);
}

.cred-divider {
  height: 1px;
  background: var(--ep-border);
  margin: 4px 0;
}

/* ===== Quick Start ===== */
.qs-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.qs-step {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.qs-step-num {
  font-family: 'Geist Mono', monospace;
  font-size: 11px;
  font-weight: 700;
  color: var(--ep-text-muted);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius-sm);
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.qs-step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ep-text-primary);
  margin-bottom: 3px;
}

.qs-step-desc {
  font-size: 12px;
  color: var(--ep-text-secondary);
  line-height: 1.6;
}

/* Mini terminal */
.qs-terminal {
  background: var(--ep-bg-deep);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  overflow: hidden;
}

.qs-term-bar {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid var(--ep-border);
}

.qs-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--ep-text-muted);
  opacity: 0.4;
}

.qs-term-title {
  font-family: 'Geist Mono', monospace;
  font-size: 10px;
  color: var(--ep-text-muted);
  margin-left: 6px;
  letter-spacing: 0.3px;
}

.qs-term-body {
  padding: 12px 14px;
}

.qs-line {
  font-family: 'Geist Mono', monospace;
  font-size: 11px;
  line-height: 1.9;
  white-space: nowrap;
  color: var(--ep-text-secondary);
}

.qs-line.q {
  height: 4px;
}

.t-k { color: var(--ep-text-secondary); }
.t-v { color: var(--ep-text-muted); }
.t-s { color: var(--ep-text-primary); }

/* ===== Animations ===== */
@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .greeting-name {
    font-size: 24px;
  }

  .stat-value {
    font-size: 20px;
  }

  .qs-terminal {
    display: none;
  }
}
</style>
