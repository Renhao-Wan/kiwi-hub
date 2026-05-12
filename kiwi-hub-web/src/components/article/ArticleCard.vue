<script setup lang="ts">
import ArticleMeta from './ArticleMeta.vue'
import TagList from '@/components/common/TagList.vue'
import type { ArticleListVO } from '@/types/article'

defineProps<{
  article: ArticleListVO
}>()
</script>

<template>
  <div class="article-card">
    <router-link :to="`/articles/${article.id}`" class="title">
      {{ article.title }}
    </router-link>
    <p v-if="article.summary" class="summary">{{ article.summary }}</p>
    <TagList v-if="article.tags?.length" :tags="article.tags" />
    <ArticleMeta
      :author-name="article.authorName"
      :created-at="article.createdAt"
      :view-count="article.viewCount"
      :like-count="article.likeCount"
      :comment-count="article.commentCount"
    />
  </div>
</template>

<style scoped>
.article-card {
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
