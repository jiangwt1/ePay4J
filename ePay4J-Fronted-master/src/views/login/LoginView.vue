<template>
  <div class="login-page">
    <!-- Main content -->
    <div class="main-container">
      <!-- Left: Brand Panel -->
      <div class="brand-panel">
        <div class="brand-content">
          <!-- Logo mark -->
          <div class="logo-mark">
            <svg viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="1" y="1" width="54" height="54" rx="14" stroke="currentColor" stroke-width="1.5" opacity="0.2"/>
              <rect x="8" y="8" width="40" height="40" rx="10" fill="currentColor" opacity="0.04"/>
              <path d="M20 28L26 22L32 28L26 34L20 28Z" fill="currentColor" opacity="0.6"/>
              <path d="M26 28L32 22L38 28L32 34L26 28Z" fill="currentColor" opacity="0.3"/>
              <circle cx="26" cy="28" r="2" fill="currentColor"/>
            </svg>
          </div>

          <!-- Brand text -->
          <div class="brand-text">
            <h1 class="brand-title">
              <span class="title-line title-line-1">Easy</span>
              <span class="title-line title-line-2">Payment</span>
            </h1>
            <div class="brand-divider">
              <span class="divider-line"></span>
              <span class="divider-dot"></span>
              <span class="divider-line"></span>
            </div>
            <p class="brand-desc">
              为个人站点提供<br/>安全、快捷的收款能力
            </p>
          </div>

          <!-- Feature tags -->
          <div class="feature-tags">
            <span class="tag">易支付协议</span>
            <span class="tag">支付宝通道</span>
            <span class="tag">即时到账</span>
          </div>

          <!-- Terminal decoration -->
          <div class="terminal-block">
            <div class="terminal-header">
              <span class="terminal-dot"></span>
              <span class="terminal-dot"></span>
              <span class="terminal-dot"></span>
              <span class="terminal-title">api-request.sh</span>
            </div>
            <div class="terminal-body">
              <div class="terminal-line">
                <span class="t-prompt">$</span>
                <span class="t-cmd"> curl</span>
                <span class="t-flag"> -X</span>
                <span class="t-str"> POST</span>
              </div>
              <div class="terminal-line">
                <span class="t-arg">&nbsp;&nbsp;https://api.epay.com/pay</span>
              </div>
              <div class="terminal-line">
                <span class="t-flag">&nbsp;&nbsp;-d</span>
                <span class="t-str"> '{"pid":"1001","type":"alipay"}'</span>
              </div>
              <div class="terminal-line response">
                <span class="t-ok">✓</span>
                <span class="t-res"> 200 — 支付链接已生成</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Form Panel -->
      <div class="form-panel">
        <div class="form-wrapper fade-in-up">
          <!-- Mobile logo -->
          <div class="mobile-logo">
            <span class="mobile-logo-text">EPay</span>
          </div>

          <!-- Role switcher -->
          <div class="role-switcher">
            <button
              class="role-btn"
              :class="{ active: loginRole === 'MERCHANT' }"
              @click="loginRole = 'MERCHANT'"
            >
              商户
            </button>
            <button
              class="role-btn"
              :class="{ active: loginRole === 'ADMIN' }"
              @click="loginRole = 'ADMIN'"
            >
              管理员
            </button>
          </div>

          <!-- Form area -->
          <transition name="slide" mode="out-in">
            <div v-if="mode === 'login'" key="login" class="form-area">
              <div class="form-header">
                <h2 class="form-title">欢迎回来</h2>
                <p class="form-subtitle">
                  {{ loginRole === 'MERCHANT' ? '登录商户中心管理您的收款' : '登录管理后台管理平台' }}
                </p>
              </div>

              <el-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                @keyup.enter="handleLogin"
                class="login-form"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="用户名"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    size="large"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item class="submit-item">
                  <el-button
                    :loading="loading"
                    class="submit-btn"
                    @click="handleLogin"
                  >
                    {{ loading ? '正在验证...' : '进入系统' }}
                  </el-button>
                </el-form-item>
              </el-form>

              <div class="form-footer">
                <button class="text-btn" @click="mode = 'forgot'">
                  忘记密码？
                </button>
              </div>
            </div>

            <!-- Reset Password -->
            <div v-else key="forgot" class="form-area">
              <div class="form-header">
                <button class="back-btn" @click="mode = 'login'">
                  <el-icon><ArrowLeft /></el-icon> 返回
                </button>
                <h2 class="form-title">重置密码</h2>
                <p class="form-subtitle">通过安全码验证后设置新密码</p>
              </div>

              <el-form
                ref="forgotFormRef"
                :model="forgotForm"
                :rules="forgotRules"
                @keyup.enter="handleReset"
                class="login-form"
              >
                <el-form-item prop="username">
                  <el-input v-model="forgotForm.username" placeholder="用户名" size="large">
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item prop="securityCode">
                  <el-input v-model="forgotForm.securityCode" placeholder="安全码" size="large">
                    <template #prefix>
                      <el-icon><Key /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item prop="newPassword">
                  <el-input v-model="forgotForm.newPassword" type="password" placeholder="新密码" size="large" show-password>
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="确认新密码" size="large" show-password>
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item class="submit-item">
                  <el-button :loading="loading" class="submit-btn" @click="handleReset">
                    {{ loading ? '提交中...' : '重置密码' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </transition>

          <!-- Footer -->
          <div class="form-bottom">
            <span class="copyright">EPay &copy; {{ new Date().getFullYear() }}</span>
            <span class="version">v1.0</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/modules/user'
import { login, merchantLogin, resetPassword } from '../../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const mode = ref('login')
const loginRole = ref('MERCHANT')

const loginFormRef = ref()
const forgotFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const forgotForm = reactive({ username: '', securityCode: '', newPassword: '', confirmPassword: '' })
const forgotRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  securityCode: [{ required: true, message: '请输入安全码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const res = loginRole.value === 'ADMIN'
      ? await login(loginForm)
      : await merchantLogin(loginForm)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push(loginRole.value === 'MERCHANT' ? '/merchant/dashboard' : '/admin/dashboard')
  } finally {
    loading.value = false
  }
}

async function handleReset() {
  await forgotFormRef.value.validate()
  loading.value = true
  try {
    await resetPassword({
      username: forgotForm.username,
      securityCode: forgotForm.securityCode,
      newPassword: forgotForm.newPassword,
    })
    ElMessage.success('密码重置成功，请登录')
    mode.value = 'login'
    loginForm.username = forgotForm.username
    loginForm.password = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== Page ===== */
.login-page {
  height: 100vh;
  width: 100vw;
  background: var(--ep-bg-deep);
  overflow: hidden;
}

/* ===== Main Container ===== */
.main-container {
  display: flex;
  height: 100%;
}

/* ===== Brand Panel (Left) ===== */
.brand-panel {
  flex: 0 0 45%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  border-right: 1px solid var(--ep-border);
  background: var(--ep-bg-surface);
}

.brand-content {
  max-width: 440px;
  width: 100%;
}

.logo-mark {
  width: 56px;
  height: 56px;
  color: var(--ep-text-muted);
  margin-bottom: 48px;
  animation: fadeIn 0.8s ease both;
}

.brand-title {
  margin: 0;
  line-height: 1;
}

.title-line {
  display: block;
  font-size: 56px;
  font-weight: 800;
  color: var(--ep-text-primary);
  letter-spacing: -2px;
  animation: slideUp 0.8s ease both;
}

.title-line-1 {
  animation-delay: 0.1s;
}

.title-line-2 {
  color: var(--ep-text-secondary);
  animation-delay: 0.2s;
}

.brand-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 32px 0;
  animation: fadeIn 0.8s ease 0.3s both;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: var(--ep-border);
}

.divider-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ep-text-muted);
}

.brand-desc {
  font-size: 16px;
  line-height: 1.8;
  color: var(--ep-text-secondary);
  margin: 0;
  animation: slideUp 0.8s ease 0.4s both;
}

/* Feature tags */
.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 40px;
  animation: slideUp 0.8s ease 0.5s both;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  background: var(--ep-accent-bg);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  font-size: 12px;
  color: var(--ep-text-muted);
  letter-spacing: 0.5px;
}

/* Terminal decoration */
.terminal-block {
  margin-top: 48px;
  background: var(--ep-bg-deep);
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  overflow: hidden;
  animation: slideUp 0.8s ease 0.6s both;
}

.terminal-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: var(--ep-hover-fill);
  border-bottom: 1px solid var(--ep-border);
}

.terminal-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ep-text-muted);
  opacity: 0.4;
}

.terminal-title {
  font-size: 11px;
  color: var(--ep-text-muted);
  margin-left: 8px;
}

.terminal-body {
  padding: 14px 16px;
}

.terminal-line {
  font-family: 'Geist Mono', 'JetBrains Mono', monospace;
  font-size: 12px;
  line-height: 2;
  animation: typeIn 0.4s ease both;
}

.t-prompt { color: var(--ep-text-secondary); }
.t-cmd { color: var(--ep-text-primary); }
.t-flag { color: var(--ep-text-muted); }
.t-str { color: var(--ep-text-secondary); }
.t-arg { color: var(--ep-text-muted); }
.t-ok { color: var(--ep-success); }
.t-res { color: var(--ep-success); opacity: 0.7; }

.terminal-line.response {
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed var(--ep-border);
}

/* ===== Form Panel (Right) ===== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
}

.mobile-logo {
  display: none;
  margin-bottom: 32px;
}

.mobile-logo-text {
  font-size: 28px;
  font-weight: 800;
  color: var(--ep-text-primary);
}

/* Role switcher */
.role-switcher {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
}

.role-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius);
  background: transparent;
  color: var(--ep-text-muted);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.role-btn:hover {
  border-color: var(--ep-border-active);
  color: var(--ep-text-secondary);
}

.role-btn.active {
  border-color: var(--ep-border-active);
  background: var(--ep-hover-fill);
  color: var(--ep-text-primary);
}

/* Form area */
.form-area {
  min-height: 300px;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--ep-text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--ep-text-secondary);
  margin: 0;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  color: var(--ep-text-muted);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 20px;
  transition: color 0.15s ease;
}

.back-btn:hover {
  color: var(--ep-text-secondary);
}

/* Form */
.login-form {
  margin-top: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--ep-input-bg) !important;
  border: 1px solid var(--ep-border) !important;
  box-shadow: none !important;
  border-radius: var(--ep-radius) !important;
  height: 46px !important;
  padding: 0 16px !important;
  transition: border-color 0.15s ease !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--ep-border-active) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--ep-border-active) !important;
  box-shadow: none !important;
}

.login-form :deep(.el-input__inner) {
  color: var(--ep-text-primary) !important;
  font-size: 14px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: var(--ep-text-muted) !important;
}

.login-form :deep(.el-input__prefix) {
  color: var(--ep-text-muted) !important;
  margin-right: 10px;
}

.login-form :deep(.el-input__suffix .el-icon) {
  color: var(--ep-text-muted) !important;
}

.login-form :deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
}

/* Submit button */
.submit-item {
  margin-top: 28px !important;
  margin-bottom: 0 !important;
}

.submit-btn {
  width: 100%;
  height: 46px !important;
  border-radius: var(--ep-radius) !important;
  font-size: 14px !important;
  font-weight: 600 !important;
}

/* Footer */
.form-footer {
  text-align: center;
  margin-top: 20px;
}

.text-btn {
  border: none;
  background: none;
  color: var(--ep-text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.15s ease;
}

.text-btn:hover {
  color: var(--ep-text-secondary);
}

.form-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--ep-border);
}

.copyright {
  font-size: 11px;
  color: var(--ep-text-muted);
  letter-spacing: 0.5px;
}

.version {
  font-size: 10px;
  color: var(--ep-text-muted);
  padding: 3px 8px;
  border: 1px solid var(--ep-border);
  border-radius: var(--ep-radius-sm);
}

/* ===== Animations ===== */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes typeIn {
  from { opacity: 0; transform: translateX(-8px); }
  to { opacity: 1; transform: translateX(0); }
}

/* Form transition */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-in-up {
  animation: slideUp 0.6s ease both;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    padding: 24px;
  }

  .mobile-logo {
    display: block;
  }
}

@media (max-width: 480px) {
  .form-wrapper {
    max-width: 100%;
  }

  .role-btn {
    height: 40px;
    font-size: 13px;
  }

  .form-title {
    font-size: 22px;
  }
}
</style>
