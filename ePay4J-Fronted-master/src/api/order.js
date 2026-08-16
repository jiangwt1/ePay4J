import request from './request'

export function getOrderPage(params) {
  return request.get('/admin/orders/page', { params })
}

export function getOrderTimeout() {
  return request.get('/admin/orders/timeout')
}

export function getOrderDetail(id) {
  return request.get(`/admin/orders/${id}`)
}

export function refundOrder(id, data) {
  return request.post(`/admin/orders/${id}/refund`, data)
}

export function closeOrder(id) {
  return request.post(`/admin/orders/${id}/close`)
}

export function deleteOrder(id) {
  return request.delete(`/admin/orders/${id}`)
}

export function batchDeleteOrders(ids) {
  return request.delete('/admin/orders/batch', { data: ids })
}

export function getMerchantOrders(params) {
  return request.get('/merchant/orders', { params })
}

export function getMerchantOrderDetail(id) {
  return request.get(`/merchant/orders/${id}`)
}

export function getWithdrawPage(params) {
  return request.get('/admin/withdraw/page', { params })
}

export function getWithdrawDetail(id) {
  return request.get(`/admin/withdraw/${id}`)
}

export function approveWithdraw(id) {
  return request.post(`/admin/withdraw/${id}/approve`)
}

export function approveWithdrawManual(id) {
  return request.post(`/admin/withdraw/${id}/approve-manual`)
}

export function rejectWithdraw(id, data) {
  return request.post(`/admin/withdraw/${id}/reject`, data)
}
