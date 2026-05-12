import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getArticleDetail } from '@/api/article'
import type { ArticleDetail } from '@/types/article'

export const useArticleStore = defineStore('article', () => {
  const currentArticle = ref<ArticleDetail | null>(null)

  async function fetchArticle(articleId: number) {
    const { data } = await getArticleDetail(articleId)
    currentArticle.value = data.data
    return data.data
  }

  function clearArticle() {
    currentArticle.value = null
  }

  return { currentArticle, fetchArticle, clearArticle }
})
