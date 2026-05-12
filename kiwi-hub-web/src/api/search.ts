import request from './request'
import type { Result, PageResult } from '@/types/api'
import type { ArticleSearchDTO, ArticleSearchResultVO } from '@/types/search'

/** 搜索文章 */
export function searchArticles(data: ArticleSearchDTO) {
  return request.post<Result<PageResult<ArticleSearchResultVO>>>('/search/articles', data)
}
