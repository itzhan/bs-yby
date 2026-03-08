import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const role = computed(() => userInfo.value?.role || '')
  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    // Fetch user info after login
    const infoRes = await getUserInfo()
    userInfo.value = infoRes.data
    localStorage.setItem('userInfo', JSON.stringify(infoRes.data))
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, role, isLoggedIn, login, logout }
})
