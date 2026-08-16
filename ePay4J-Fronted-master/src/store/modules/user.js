import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getInfo, getMerchantInfo } from '../../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref('')
  const nickname = ref('')
  const role = ref(localStorage.getItem('role') || '')
  const loginType = ref(localStorage.getItem('loginType') || '')
  const merchantId = ref(localStorage.getItem('merchantId') || null)
  const merchantName = ref(localStorage.getItem('merchantName') || '')
  const actingMerchantId = ref(localStorage.getItem('actingMerchantId') || '')
  const actingMerchantName = ref(localStorage.getItem('actingMerchantName') || '')
  const isActingMerchantView = computed(() => role.value === 'SUPER_ADMIN' && !!actingMerchantId.value)

  function setLogin(data) {
    token.value = data.token
    username.value = data.username
    nickname.value = data.nickname
    role.value = data.role
    loginType.value = data.loginType || ''
    merchantId.value = data.merchantId || null
    merchantName.value = data.merchantName || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    if (loginType.value) localStorage.setItem('loginType', loginType.value)
    else localStorage.removeItem('loginType')
    if (merchantId.value) localStorage.setItem('merchantId', String(merchantId.value))
    else localStorage.removeItem('merchantId')
    if (merchantName.value) localStorage.setItem('merchantName', merchantName.value)
    else localStorage.removeItem('merchantName')
    exitMerchantView()
  }

  function clearLogin() {
    token.value = ''
    username.value = ''
    nickname.value = ''
    role.value = ''
    loginType.value = ''
    merchantId.value = null
    merchantName.value = ''
    exitMerchantView()
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('loginType')
    localStorage.removeItem('merchantId')
    localStorage.removeItem('merchantName')
  }

  function enterMerchantView(merchant) {
    actingMerchantId.value = String(merchant.id)
    actingMerchantName.value = merchant.name || ''
    localStorage.setItem('actingMerchantId', actingMerchantId.value)
    localStorage.setItem('actingMerchantName', actingMerchantName.value)
  }

  function exitMerchantView() {
    actingMerchantId.value = ''
    actingMerchantName.value = ''
    localStorage.removeItem('actingMerchantId')
    localStorage.removeItem('actingMerchantName')
  }

  async function fetchInfo() {
    const res = await getInfo()
    username.value = res.data.username
    nickname.value = res.data.nickname
    role.value = res.data.role
  }

  async function fetchMerchantInfo() {
    const res = await getMerchantInfo()
    merchantId.value = res.data.id
    nickname.value = res.data.nickName
    merchantName.value = res.data.name
  }

  return {
    token,
    username,
    nickname,
    role,
    loginType,
    merchantId,
    merchantName,
    actingMerchantId,
    actingMerchantName,
    isActingMerchantView,
    setLogin,
    clearLogin,
    enterMerchantView,
    exitMerchantView,
    fetchInfo,
    fetchMerchantInfo,
  }
})
