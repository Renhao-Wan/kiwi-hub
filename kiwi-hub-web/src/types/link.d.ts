/**
 * 短链接 VO
 * 对应后端 LinkVO
 */
export interface LinkVO {
  shortCode: string
  shortUrl: string
  originalUrl: string
  createdAt: string
}

/**
 * 短链接创建参数
 */
export interface CreateLinkParams {
  originalUrl: string
}
