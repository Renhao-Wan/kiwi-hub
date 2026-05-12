import request from './request'
import type { Result } from '@/types/api'
import type { LinkVO } from '@/types/link'

/** 创建短链接 */
export function createLink(originalUrl: string) {
  return request.post<Result<LinkVO>>('/links/create', { originalUrl })
}

/** 获取短链接信息 */
export function getLinkInfo(shortCode: string) {
  return request.get<Result<LinkVO>>(`/links/${shortCode}`)
}
