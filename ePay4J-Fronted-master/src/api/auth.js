import request from './request'

export function login(data) {
  return request.post('/admin/auth/login', data)
}

export function merchantLogin(data) {
  return request.post('/merchant/auth/login', data)
}

export function getMerchantInfo() {
  return request.get('/merchant/info')
}

export function getMerchantAccountInfo() {
  return request.get('/merchant/account')
}

export function updateMerchantInfo(data) {
  return request.put('/merchant/info', data)
}

export function updateMerchantPassword(data) {
  return request.put('/merchant/password', data)
}

export function getMerchantCredentials() {
  return request.get('/merchant/credentials')
}

export function getSiteSettings() {
  return request.get('/settings')
}

export function resetMerchantKey() {
  return request.post('/merchant/reset-key')
}

export function getWithdrawRecords(params) {
  return request.get('/merchant/withdraw/records', { params })
}

export function submitWithdraw(data) {
  return request.post('/merchant/withdraw', data)
}

export function getInfo() {
  return request.get('/admin/auth/info')
}

export function logout() {
  return request.post('/admin/auth/logout')
}

export function changePassword(data) {
  return request.put('/admin/auth/password', data)
}

export function resetPassword(data) {
  return request.post('/admin/auth/reset-password', data)
}
