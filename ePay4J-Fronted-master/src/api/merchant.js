import request from './request'

export function getMerchantPage(params) {
  return request.get('/admin/merchants/page', { params })
}

export function getMerchantDetail(id) {
  return request.get(`/admin/merchants/${id}`)
}

export function createMerchant(data) {
  return request.post('/admin/merchants', data)
}

export function updateMerchant(id, data) {
  return request.put(`/admin/merchants/${id}`, data)
}

export function updateMerchantStatus(id, status) {
  return request.put(`/admin/merchants/${id}/status`, { status })
}

export function resetMerchantKey(id) {
  return request.post(`/admin/merchants/${id}/reset-key`)
}

export function bindCurrentAdminMerchant(id) {
  return request.post(`/admin/merchants/${id}/bind-current-admin`)
}

export function deleteMerchant(id) {
  return request.delete(`/admin/merchants/${id}`)
}

export function getMerchantAccount(merchantId) {
  return request.get(`/admin/merchant-accounts/${merchantId}`)
}
