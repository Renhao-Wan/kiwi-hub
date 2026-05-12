/**
 * 用户注册 DTO
 * 对应后端 UserRegisterDTO，multipart/form-data 提交
 */
export interface UserRegisterDTO {
  username: string
  email: string
  password: string
}

/**
 * 用户登录 DTO
 * 对应后端 UserLoginDTO，JSON 提交
 */
export interface UserLoginDTO {
  username: string
  email: string
  password: string
}

/**
 * 用户资料更新 DTO
 * 对应后端 UserProfileDTO
 */
export interface UserProfileDTO {
  nickname?: string
  bio?: string
  avatar?: string
  website?: string
  location?: string
  birthday?: string
  gender?: number
}

/**
 * 用户详情 VO（当前登录用户）
 * 对应后端 UserDetailVO
 */
export interface UserDetailVO {
  username: string
  email: string
  createdAt: string
  profile: UserProfile
  socialStats: UserSocialStats
}

/**
 * 用户资料
 */
export interface UserProfile {
  nickname: string
  bio: string
  avatar: string
  website: string
  location: string
  birthday: string
  gender: number
}

/**
 * 用户社交统计
 */
export interface UserSocialStats {
  articleCount: number
  followingCount: number
  followerCount: number
}

/**
 * 用户卡片 VO（列表展示）
 * 对应后端 UserCardVO
 */
export interface UserCardVO {
  id: number
  username: string
  nickname: string
  avatar: string
  bio: string
  followerCount: number
  isFollowed: boolean
}

/**
 * 用户统计
 * 对应后端 UserStats
 */
export interface UserStats {
  articleCount: number
  followingCount: number
  followerCount: number
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  userId: number
  username: string
  token: string
}
