import request from './request'
import type { Result } from '@/types/api'

/** 点赞/取消点赞（幂等切换） */
export function toggleLike(articleId: number) {
  return request.post<Result<boolean>>(`/interactions/like`, { articleId })
}

/** 检查是否已点赞 */
export function checkLikeStatus(articleId: number) {
  return request.get<Result<boolean>>(`/interactions/like/status/${articleId}`)
}
