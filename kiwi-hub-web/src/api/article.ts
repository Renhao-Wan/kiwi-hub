import request from './request'
import type { Result, PageResult } from '@/types/api'
import type { PublishArticleDTO, ArticleListVO, ArticleDetail } from '@/types/article'

/** 发布文章 */
export function publishArticle(data: PublishArticleDTO) {
  return request.post<Result<number>>('/articles/publish', data)
}

/** 获取文章列表（分页） */
export function getArticleList(params: { pageNum: number; pageSize: number }) {
  return request.get<Result<PageResult<ArticleListVO>>>('/articles/list', { params })
}

/** 获取文章详情 */
export function getArticleDetail(articleId: number) {
  return request.get<Result<ArticleDetail>>(`/articles/${articleId}`)
}

/** 删除文章 */
export function deleteArticle(articleId: number) {
  return request.delete<Result<null>>(`/articles/${articleId}`)
}
