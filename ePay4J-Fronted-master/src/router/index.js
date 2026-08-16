import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/modules/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: to => {
      const role = localStorage.getItem('role') || ''
      if (role === 'MERCHANT') return '/merchant/dashboard'
      return '/admin/dashboard'
    },
    children: [
      { path: 'dashboard', redirect: '/admin/dashboard' },
      { path: 'orders', redirect: '/admin/orders' },
      { path: 'merchants', redirect: '/admin/merchants' },
    ],
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { roles: ['SUPER_ADMIN', 'ADMIN', 'VIEWER'] },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/dashboard/DashboardView.vue'), meta: { title: '仪表盘' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/order/OrderListView.vue'), meta: { title: '订单管理' } },
      { path: 'merchants', name: 'Merchants', component: () => import('../views/merchant/MerchantListView.vue'), meta: { title: '商户管理' } },
      { path: 'merchants/:merchantId/account', name: 'MerchantAccount', component: () => import('../views/merchant/MerchantAccountView.vue'), meta: { title: '账户管理' } },
      { path: 'channels', name: 'Channels', component: () => import('../views/channel/ChannelListView.vue'), meta: { title: '支付通道' } },
      { path: 'system', name: 'System', component: () => import('../views/system/SystemConfigView.vue'), meta: { title: '系统设置' } },
      { path: 'users', name: 'Users', component: () => import('../views/user/UserListView.vue'), meta: { title: '管理员', roles: ['SUPER_ADMIN'] } },
      { path: 'withdraw', name: 'Withdraw', component: () => import('../views/withdraw/WithdrawListView.vue'), meta: { title: '提现管理' } },
    ],
  },
  {
    path: '/merchant',
    component: () => import('../layout/MerchantLayout.vue'),
    meta: { roles: ['MERCHANT'] },
    children: [
      { path: 'dashboard', name: 'MerchantDashboard', component: () => import('../views/merchant/MerchantDashboardView.vue'), meta: { title: '工作台' } },
      { path: 'orders', name: 'MerchantOrders', component: () => import('../views/order/MerchantOrderListView.vue'), meta: { title: '订单管理' } },
      { path: 'settings', name: 'MerchantSettings', component: () => import('../views/merchant/MerchantSettingsView.vue'), meta: { title: '账户设置' } },
      { path: 'withdraw', name: 'MerchantWithdraw', component: () => import('../views/merchant/MerchantWithdrawView.vue'), meta: { title: '提现管理' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    const role = localStorage.getItem('role') || ''
    const loginType = localStorage.getItem('loginType') || ''
    const merchantId = localStorage.getItem('merchantId') || ''
    const actingMerchantId = localStorage.getItem('actingMerchantId') || ''
    const toRole = to.path.startsWith('/merchant') ? 'MERCHANT' : 'ADMIN'
    if (toRole === 'MERCHANT' && role !== 'MERCHANT') {
      if (role === 'SUPER_ADMIN' && (actingMerchantId || loginType === 'MERCHANT' || merchantId)) {
        next()
      } else {
        next('/admin/merchants')
      }
    } else if (toRole === 'ADMIN' && role === 'MERCHANT') {
      next('/merchant/dashboard')
    } else {
      next()
    }
  }
})

export default router
