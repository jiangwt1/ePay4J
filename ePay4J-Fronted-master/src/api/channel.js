import request from './request'

export function getChannels() {
  return request.get('/admin/channels')
}

export function getChannel(id) {
  return request.get(`/admin/channels/${id}`)
}

export function updateChannel(id, data) {
  return request.put(`/admin/channels/${id}`, data)
}

export function updateChannelStatus(id, status) {
  return request.put(`/admin/channels/${id}/status`, { status })
}

export function testChannel(id) {
  return request.post(`/admin/channels/${id}/test`)
}

export function createChannel(data) {
  return request.post('/admin/channels', data)
}

export function deleteChannel(id) {
  return request.delete(`/admin/channels/${id}`)
}
