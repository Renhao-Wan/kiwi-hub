<script setup lang="ts">
import { onMounted } from 'vue'
import { getArticleList } from '@/api/article'
import { usePagination } from '@/composables/usePagination'
import ArticleCard from '@/components/article/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const {
  list: articles,
  loading,
  pageNum,
  totalPages,
  loadData,
  goToPage,
} = usePagination(getArticleList)

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="home-view">
    <h1>文章列表</h1>
    <LoadingSpinner v-if="loading" text="加载中..." />
    <template v-else>
      <EmptyState v-if="articles.length === 0" text="暂无文章" />
      <div v-else class="article-list">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <Pagination
        :page-num="pageNum"
        :total-pages="totalPages"
        @update:page-num="goToPage"
      />
    </template>
  </div>
</template>

<style scoped>
.home-view h1 {
  margin-bottom: 20px;
}
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
