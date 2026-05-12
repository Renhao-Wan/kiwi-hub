import request from './request'
import type { Result } from '@/types/api'
import type {
  UserRegisterDTO,
  UserLoginDTO,
  UserProfileDTO,
  UserDetailVO,
  UserCardVO,
  UserStats,
} from '@/types/user'

/** 用户注册（multipart/form-data） */
export function register(data: UserRegisterDTO) {
  const formData = new FormData()
  formData.append('username', data.username)
  formData.append('email', data.email)
  formData.append('password', data.password)
  return request.post<Result<null>>('/users/register', formData)
}

/** 用户登录 */
export function login(data: UserLoginDTO) {
  return request.post<Result<null>>('/users/login', data)
}

/** 用户登出 */
export function logout() {
  return request.post<Result<null>>('/users/logout')
}

/** 获取当前用户信息 */
export function getCurrentUser() {
  return request.get<Result<UserDetailVO>>('/users/me')
}

/** 更新用户资料 */
export function updateProfile(data: UserProfileDTO) {
  return request.put<Result<null>>('/users/profile', data)
}

/** 获取用户统计 */
export function getUserStats() {
  return request.get<Result<UserStats>>('/users/stats')
}

/** 关注用户 */
export function followUser(userId: number) {
  return request.post<Result<null>>(`/users/follow/${userId}`)
}

/** 取消关注 */
export function unfollowUser(userId: number) {
  return request.delete<Result<null>>(`/users/follow/${userId}`)
}

/** 获取关注列表 */
export function getFollowingList(params: { pageNum: number; pageSize: number }) {
  return request.get<Result<{ list: UserCardVO[]; total: number }>>('/users/following', { params })
}

/** 获取粉丝列表 */
export function getFollowerList(params: { pageNum: number; pageSize: number }) {
  return request.get<Result<{ list: UserCardVO[]; total: number }>>('/users/followers', { params })
}

/** 检查是否已关注 */
export function checkFollowStatus(userId: number) {
  return request.get<Result<boolean>>(`/users/follow/status/${userId}`)
}
