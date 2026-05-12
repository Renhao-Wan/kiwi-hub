import request from './request'
import type { Result, PageResult } from '@/types/api'
import type { CommentDTO, CommentQueryDTO, CommentVO, CommentReplyResponse } from '@/types/comment'

/** 发表评论 */
export function postComment(data: CommentDTO) {
  return request.post<Result<number>>('/comments', data)
}

/** 获取文章一级评论列表 */
export function getRootComments(params: { articleId: number; pageNum: number; pageSize: number }) {
  return request.get<Result<PageResult<CommentVO>>>('/comments', { params })
}

/** 获取评论回复列表（特殊响应格式） */
export function getReplies(params: CommentQueryDTO) {
  return request.get<Result<CommentReplyResponse>>('/comments/replies', { params })
}

/** 删除评论 */
export function deleteComment(commentId: number) {
  return request.delete<Result<null>>(`/comments/${commentId}`)
}
