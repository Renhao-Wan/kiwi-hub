<script setup lang="ts">
import { formatNumber } from '@/utils/format'

defineProps<{
  viewCount: number
  likeCount: number
  commentCount: number
}>()
</script>

<template>
  <div class="article-stats">
    <span class="stat">{{ formatNumber(viewCount) }} 阅读</span>
    <span class="stat">{{ formatNumber(likeCount) }} 点赞</span>
    <span class="stat">{{ formatNumber(commentCount) }} 评论</span>
  </div>
</template>

<style scoped>
.article-stats {
  display: flex;
  gap: 16px;
}
.stat {
  color: #999;
  font-size: 13px;
}
</style>
