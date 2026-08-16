import request from './request'

export function getConfig() {
  return request.get('/admin/system/config')
}

export function updateConfig(data) {
  return request.put('/admin/system/config', data)
}
