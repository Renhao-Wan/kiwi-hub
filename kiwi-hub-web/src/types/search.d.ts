/**
 * 文章搜索 DTO
 * 对应后端 ArticleSearchDTO
 */
export interface ArticleSearchDTO {
  keyword: string
  mode?: 'fulltext' | 'regex'
  pageNum?: number
  pageSize?: number
}

/**
 * 文章搜索结果 VO
 * 对应后端 ArticleSearchResultVO
 */
export interface ArticleSearchResultVO {
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
  relevanceScore: number
  highlights: string[]
}
