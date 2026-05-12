import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser, login as loginApi, logout as logoutApi, register as registerApi } from '@/api/user'
import type { UserDetailVO, UserLoginDTO, UserRegisterDTO } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserDetailVO | null>(null)
  const isLoggedIn = computed(() => user.value !== null)

  async function fetchCurrentUser() {
    try {
      const { data } = await getCurrentUser()
      user.value = data.data
      return true
    } catch {
      user.value = null
      return false
    }
  }

  async function login(dto: UserLoginDTO) {
    await loginApi(dto)
    await fetchCurrentUser()
  }

  async function register(dto: UserRegisterDTO) {
    await registerApi(dto)
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      user.value = null
    }
  }

  function clearUser() {
    user.value = null
  }

  return { user, isLoggedIn, fetchCurrentUser, login, register, logout, clearUser }
})
