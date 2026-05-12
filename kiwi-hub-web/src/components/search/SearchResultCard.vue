<script setup lang="ts">
import DOMPurify from 'dompurify'
import ArticleMeta from '@/components/article/ArticleMeta.vue'
import TagList from '@/components/common/TagList.vue'
import type { ArticleSearchResultVO } from '@/types/search'

defineProps<{
  item: ArticleSearchResultVO
}>()

function safeHtml(html: string): string {
  return DOMPurify.sanitize(html)
}
</script>

<template>
  <div class="search-result-card">
    <router-link :to="`/articles/${item.id}`" class="title">
      <span v-html="safeHtml(item.title)"></span>
    </router-link>
    <p class="summary" v-html="safeHtml(item.summary)"></p>
    <TagList v-if="item.tags?.length" :tags="item.tags" />
    <ArticleMeta
      :author-name="item.authorName"
      :created-at="item.createdAt"
      :view-count="item.viewCount"
      :like-count="item.likeCount"
      :comment-count="item.commentCount"
    />
  </div>
</template>

<style scoped>
.search-result-card {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}
.title:hover {
  color: #42b883;
}
.summary {
  color: #666;
  font-size: 14px;
  margin: 0;
}
</style>
