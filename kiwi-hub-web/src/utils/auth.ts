import { useUserStore } from '@/stores/user'

/**
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  const userStore = useUserStore()
  return userStore.isLoggedIn
}

/**
 * 获取当前用户 ID（从 store 中获取）
 * 注意：UserDetailVO 不含 ID，此函数需后端补充 ID 后才能使用
 */
export function getCurrentUserId(): number | null {
  // UserDetailVO 当前不含 ID 字段，待后端补充
  return null
}
