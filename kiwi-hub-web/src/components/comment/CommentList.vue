<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRootComments, postComment } from '@/api/comment'
import CommentItem from './CommentItem.vue'
import CommentReplyList from './CommentReplyList.vue'
import CommentInput from './CommentInput.vue'
import type { CommentVO } from '@/types/comment'

const props = defineProps<{
  articleId: number
}>()

const comments = ref<CommentVO[]>([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)

const replyingTo = ref<CommentVO | null>(null)

async function loadComments() {
  loading.value = true
  try {
    const { data } = await getRootComments({
      articleId: props.articleId,
      pageNum: pageNum.value,
      pageSize: 20,
    })
    comments.value = data.data.list
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

async function handlePostComment(content: string) {
  await postComment({
    articleId: props.articleId,
    content,
    parentId: replyingTo.value?.id,
    rootId: replyingTo.value?.rootId || replyingTo.value?.id,
  })
  replyingTo.value = null
  pageNum.value = 1
  await loadComments()
}

function handleReply(comment: CommentVO) {
  replyingTo.value = comment
}

function cancelReply() {
  replyingTo.value = null
}

onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="comment-section">
    <h3>评论 ({{ total }})</h3>
    <div class="comment-form">
      <div v-if="replyingTo" class="reply-hint">
        回复 {{ replyingTo.authorName }}：
        <button class="cancel-btn" @click="cancelReply">取消</button>
      </div>
      <CommentInput
        :placeholder="replyingTo ? `回复 ${replyingTo.authorName}...` : '写下你的评论...'"
        @submit="handlePostComment"
      />
    </div>
    <div v-if="loading && comments.length === 0" class="loading">加载中...</div>
    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-thread">
        <CommentItem :comment="comment" @reply="handleReply" />
        <CommentReplyList
          v-if="comment.replyCount > 0"
          :article-id="props.articleId"
          :root-id="comment.id"
          :parent-id="comment.id"
          @reply="handleReply"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}
.comment-section h3 {
  margin-bottom: 16px;
}
.comment-form {
  margin-bottom: 24px;
}
.reply-hint {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.cancel-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
}
.cancel-btn:hover {
  color: #ff4d4f;
}
.comment-list {
  display: flex;
  flex-direction: column;
}
.loading {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style>
