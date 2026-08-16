<template>
  <el-container class="merchant-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isCollapse }">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-mark" :class="{ small: isCollapse }">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="32" height="32" rx="8" fill="rgba(255,255,255,0.06)"/>
            <path d="M10 16L14 12L18 16L14 20L10 16Z" fill="#888"/>
            <path d="M14 16L18 12L22 16L18 20L14 16Z" fill="#666"/>
          </svg>
        </div>
        <transition name="fade">
          <div v-if="!isCollapse" class="logo-text">
            <span class="logo-name">EPay</span>
            <span class="logo-badge">商户</span>
          </div>
        </transition>
      </div>

      <!-- Navigation -->
      <nav class="sidebar-nav">
        <div class="sidebar-menu">
          <div
            v-for="item in menuItems"
            :key="item.path"
            class="menu-item"
            :class="{ active: $route.path === item.path }"
            @click="$router.push(item.path)"
          >
            <div class="menu-item-icon">
              <el-icon :size="20"><component :is="item.icon" /></el-icon>
            </div>
            <transition name="text-fade">
              <span v-if="!isCollapse" class="menu-item-text">{{ item.label }}</span>
            </transition>
            <div v-if="$route.path === item.path && !isCollapse" class="menu-item-indicator"></div>
          </div>
        </div>
      </nav>

      <!-- Collapse toggle -->
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="isCollapse = !isCollapse">
          <el-icon :size="16">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <transition name="fade">
            <span v-if="!isCollapse" class="collapse-text">收起菜单</span>
          </transition>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <el-container class="main-container">
      <!-- Header -->
      <header class="app-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/merchant/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-trigger">
              <el-avatar :size="32" class="user-avatar">
                {{ (userStore.nickname || 'M').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-meta">
                <span class="user-name">{{ userStore.nickname || '商户' }}</span>
                <span class="user-role">{{ userStore.merchantName || '' }}</span>
              </div>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="theme">
                  <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>{{ isDark ? '切换浅色模式' : '切换深色模式' }}
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isActingMerchantView" command="exitMerchantView">
                  <el-icon><Back /></el-icon>退出商户视角
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <div v-if="userStore.isActingMerchantView" class="acting-banner">
        <div class="acting-copy">
          <span class="acting-label">超级管理员商户视角</span>
          <span class="acting-text">正在查看商户：{{ userStore.actingMerchantName || userStore.merchantName || userStore.actingMerchantId }}</span>
        </div>
        <el-button size="small" type="warning" plain @click="exitMerchantView">退出商户视角</el-button>
      </div>

      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import { logout } from '../api/auth'
import { toggleTheme } from '../utils/theme'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const isDark = ref(document.documentElement.classList.contains('dark'))

onMounted(() => {
  if (!userStore.isActingMerchantView) {
    userStore.fetchMerchantInfo()
  }
})

const menuItems = [
  { path: '/merchant/dashboard', icon: 'DataLine', label: '工作台' },
  { path: '/merchant/orders', icon: 'Document', label: '订单管理' },
  { path: '/merchant/withdraw', icon: 'Wallet', label: '提现管理' },
  { path: '/merchant/settings', icon: 'Setting', label: '账户设置' },
]

function handleCommand(command) {
  if (command === 'theme') {
    toggleTheme()
    isDark.value = document.documentElement.classList.contains('dark')
  }
  if (command === 'exitMerchantView') {
    exitMerchantView()
  }
  if (command === 'logout') {
    try { logout() } catch { /* ignore */ }
    userStore.clearLogin()
    router.push('/login')
  }
}

function exitMerchantView() {
  userStore.exitMerchantView()
  ElMessage.success('已退出商户视角')
  router.push('/admin/merchants')
}
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
  overflow: hidden;
  background: var(--ep-bg-deep);
}

/* ── Sidebar ── */
.sidebar {
  width: var(--ep-sidebar-width);
  background: var(--ep-bg-inset);
  border-right: 1px solid var(--ep-border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: var(--ep-sidebar-collapsed-width);
}

/* ── Logo ── */
.sidebar-logo {
  height: var(--ep-header-height);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid var(--ep-border);
  flex-shrink: 0;
}

.sidebar.collapsed .sidebar-logo {
  justify-content: center;
  padding: 0;
}

.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-mark.small {
  width: 32px;
  height: 32px;
}

.logo-mark svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--ep-text-primary);
  letter-spacing: -0.5px;
}

.logo-badge {
  font-size: 10px;
  font-weight: 600;
  color: var(--ep-text-muted);
  background: rgba(255, 255, 255, 0.04);
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

/* ── Navigation ── */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu {
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: var(--ep-radius);
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  color: var(--ep-text-muted);
  margin: 0 4px;
}

.menu-item:hover {
  background: var(--ep-bg-hover);
  color: var(--ep-text-secondary);
}

.menu-item.active {
  background: rgba(255, 255, 255, 0.06);
  color: var(--ep-text-primary);
}

.menu-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.menu-item-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
}

.menu-item-indicator {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--ep-text-muted);
  border-radius: 3px 0 0 3px;
}

/* ── Footer ── */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--ep-border);
  flex-shrink: 0;
}

.collapse-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border-radius: var(--ep-radius);
  cursor: pointer;
  color: var(--ep-text-muted);
  transition: all 0.15s ease;
}

.collapse-btn:hover {
  background: var(--ep-bg-hover);
  color: var(--ep-text-secondary);
}

.sidebar.collapsed .collapse-btn {
  justify-content: center;
  padding: 8px;
}

.collapse-text {
  font-size: 12px;
}

/* ── Main Container ── */
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── Header ── */
.app-header {
  height: var(--ep-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--ep-bg-inset);
  border-bottom: 1px solid var(--ep-border);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* ── User trigger ── */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: var(--ep-radius);
  transition: background 0.15s ease;
}

.user-trigger:hover {
  background: var(--ep-bg-hover);
}

.user-avatar {
  background: var(--ep-chip-bg) !important;
  color: var(--ep-text-secondary) !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ep-text-primary);
}

.user-role {
  font-size: 11px;
  color: var(--ep-text-muted);
}

.user-arrow {
  color: var(--ep-text-muted);
  font-size: 12px;
}

/* ── Main Content ── */
.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.acting-banner {
  flex-shrink: 0;
  margin: 16px 24px 0;
  padding: 12px 16px;
  border: 1px solid rgba(230, 162, 60, 0.35);
  border-radius: var(--ep-radius);
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.16), rgba(230, 162, 60, 0.05));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.acting-copy {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.acting-label {
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(230, 162, 60, 0.18);
  color: #e6a23c;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.acting-text {
  color: var(--ep-text-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Transitions ── */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.text-fade-enter-active, .text-fade-leave-active { transition: opacity 0.2s ease; }
.text-fade-enter-from, .text-fade-leave-to { opacity: 0; }

.page-enter-active { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.page-leave-active { transition: all 0.12s cubic-bezier(0.4, 0, 0.2, 1); }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; }

/* ── Deep overrides ── */
.app-header :deep(.el-breadcrumb__inner) { color: var(--ep-text-muted) !important; font-weight: 400 !important; }
.app-header :deep(.el-breadcrumb__inner a) { color: var(--ep-text-muted) !important; }
.app-header :deep(.el-breadcrumb__inner.is-link) { color: var(--ep-text-muted) !important; }
.app-header :deep(.el-breadcrumb__separator) { color: rgba(255, 255, 255, 0.1) !important; }
</style>
