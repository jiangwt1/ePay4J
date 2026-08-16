<template>
  <div class="sidebar-menu">
    <div class="menu-section-label" v-if="!collapse">主菜单</div>
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
        <span v-if="!collapse" class="menu-item-text">{{ item.label }}</span>
      </transition>
      <div v-if="$route.path === item.path && !collapse" class="menu-item-indicator"></div>
    </div>

    <div class="menu-divider" v-if="!collapse && isSuperAdmin"></div>
    <div class="menu-section-label" v-if="!collapse && isSuperAdmin">系统</div>

    <div
      v-if="isSuperAdmin"
      class="menu-item"
      :class="{ active: $route.path === '/admin/users' }"
      @click="$router.push('/admin/users')"
    >
      <div class="menu-item-icon">
        <el-icon :size="20"><User /></el-icon>
      </div>
      <transition name="text-fade">
        <span v-if="!collapse" class="menu-item-text">用户管理</span>
      </transition>
      <div v-if="$route.path === '/admin/users' && !collapse" class="menu-item-indicator"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../store/modules/user'

defineProps({
  collapse: Boolean,
})

const userStore = useUserStore()
const isSuperAdmin = computed(() => userStore.role === 'SUPER_ADMIN')

const menuItems = [
  { path: '/admin/dashboard', icon: 'DataLine', label: '仪表盘' },
  { path: '/admin/orders', icon: 'Document', label: '订单管理' },
  { path: '/admin/merchants', icon: 'Shop', label: '商户管理' },
  { path: '/admin/channels', icon: 'Connection', label: '支付通道' },
  { path: '/admin/withdraw', icon: 'Wallet', label: '提现管理' },
  { path: '/admin/system', icon: 'Setting', label: '系统设置' },
]
</script>

<style scoped>
.sidebar-menu {
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-section-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--ep-text-muted);
  text-transform: uppercase;
  letter-spacing: 1.5px;
  padding: 16px 14px 8px;
}

.menu-divider {
  height: 1px;
  background: var(--ep-border);
  margin: 8px 14px;
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

/* Text fade transition */
.text-fade-enter-active, .text-fade-leave-active { transition: opacity 0.2s ease; }
.text-fade-enter-from, .text-fade-leave-to { opacity: 0; }
</style>
