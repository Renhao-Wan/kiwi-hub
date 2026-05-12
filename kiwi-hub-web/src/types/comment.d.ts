/**
 * 发表评论 DTO
 * 对应后端 CommentDTO
 */
export interface CommentDTO {
  articleId: number
  content: string
  parentId?: number
  rootId?: number
}

/**
 * 评论查询 DTO（游标分页）
 * 对应后端 CommentQueryDTO
 */
export interface CommentQueryDTO {
  articleId: number
  rootId: number
  parentId: number
  cursorId?: number
  pageSize?: number
}

/**
 * 评论 VO
 * 对应后端 CommentVO
 */
export interface CommentVO {
  id: number
  articleId: number
  authorId: number
  authorName: string
  authorAvatar: string
  content: string
  parentId: number | null
  rootId: number | null
  replyCount: number
  likeCount: number
  createdAt: string
  isLiked: boolean
}

/**
 * 评论回复列表响应（特殊格式）
 * GET /comments/replies 返回 Map<String, Object>
 */
export interface CommentReplyResponse {
  nextCursorId: number | null
  result: CommentVO[]
}
