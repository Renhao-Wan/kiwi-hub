<script setup lang="ts">
import { formatRelativeTime } from '@/utils/format'

defineProps<{
  authorName: string
  createdAt: string
  viewCount: number
  likeCount: number
  commentCount: number
}>()
</script>

<template>
  <div class="article-meta">
    <span class="author">{{ authorName }}</span>
    <span class="time">{{ formatRelativeTime(createdAt) }}</span>
    <span class="stat">{{ viewCount }} 阅读</span>
    <span class="stat">{{ likeCount }} 点赞</span>
    <span class="stat">{{ commentCount }} 评论</span>
  </div>
</template>

<style scoped>
.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #999;
  font-size: 13px;
}
.author {
  font-weight: bold;
  color: #333;
}
</style>
