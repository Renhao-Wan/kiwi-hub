<script setup lang="ts">
import type { CommentVO } from '@/types/comment'

defineProps<{
  comment: CommentVO
}>()

const emit = defineEmits<{
  reply: [comment: CommentVO]
}>()
</script>

<template>
  <div class="comment-item">
    <div class="comment-header">
      <span class="author">{{ comment.authorName }}</span>
      <span class="time">{{ comment.createdAt }}</span>
    </div>
    <div class="comment-body">{{ comment.content }}</div>
    <div class="comment-actions">
      <button class="action-btn" @click="emit('reply', comment)">回复</button>
      <span class="like-count">{{ comment.likeCount }} 赞</span>
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.author {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}
.time {
  color: #999;
  font-size: 12px;
}
.comment-body {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 8px;
}
.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.action-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
.action-btn:hover {
  color: #42b883;
}
.like-count {
  color: #999;
  font-size: 12px;
}
</style>
