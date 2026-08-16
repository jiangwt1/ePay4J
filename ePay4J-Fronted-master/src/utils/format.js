export function formatMoney(amount) {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

export function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

export const ORDER_STATUS = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已关闭', type: 'info' },
  3: { label: '已退款', type: 'danger' },
}

export const PAY_TYPE = {
  PAGE: '电脑网站',
  WAP: '手机网站',
  EPAY_PAGE: '易支付页面',
  EPAY_API: '易支付API',
}
