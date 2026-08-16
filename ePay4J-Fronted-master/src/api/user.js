import request from './request'

export function getUserPage(params) {
  return request.get('/admin/users/page', { params })
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, { status })
}

export function resetPassword(id, password) {
  return request.put(`/admin/users/${id}/reset-password`, { password })
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}
