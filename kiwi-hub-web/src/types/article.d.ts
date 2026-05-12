/**
 * 发布文章 DTO
 * 对应后端 PublishArticleDTO
 */
export interface PublishArticleDTO {
  title: string
  content: string
  contentType: 'markdown' | 'html' | 'text'
  summary?: string
  tags?: string[]
}

/**
 * 文章列表 VO
 * 对应后端 ArticleListVO
 */
export interface ArticleListVO {
  id: number
  authorId: number
  authorName: string
  authorAvatar: string
  title: string
  summary: string
  contentType: string
  tags: string[]
  createdAt: string
  updatedAt: string
  viewCount: number
  likeCount: number
  commentCount: number
}

/**
 * 文章详情（含正文）
 * 由文章元数据 + MongoDB 正文合并而来
 */
export interface ArticleDetail {
  id: number
  authorId: number
  title: string
  content: string
  contentType: string
  createdAt: string
  updatedAt: string
  stats: ArticleStats
}

/**
 * 文章统计
 * 对应后端 ArticleStats
 */
export interface ArticleStats {
  viewCount: number
  likeCount: number
  commentCount: number
}

/**
 * 文章分页查询参数
 */
export interface ArticlePageParams {
  pageNum: number
  pageSize: number
}
