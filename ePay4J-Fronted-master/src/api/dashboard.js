import request from './request'

export function getStats() {
  return request.get('/admin/dashboard/stats')
}

export function getRevenueChart(days = 7) {
  return request.get('/admin/dashboard/revenue-chart', { params: { days } })
}

export function getOrderStatusChart() {
  return request.get('/admin/dashboard/order-status-chart')
}

export function getRecentOrders(limit = 10) {
  return request.get('/admin/dashboard/recent-orders', { params: { limit } })
}
