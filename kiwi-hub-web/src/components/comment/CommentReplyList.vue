<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getReplies } from '@/api/comment'
import CommentItem from './CommentItem.vue'
import type { CommentVO, CommentQueryDTO } from '@/types/comment'

const props = defineProps<{
  articleId: number
  rootId: number
  parentId: number
}>()

const emit = defineEmits<{
  reply: [comment: CommentVO]
}>()

const replies = ref<CommentVO[]>([])
const nextCursorId = ref<number | null>(null)
const loading = ref(false)

async function loadReplies() {
  loading.value = true
  try {
    const params: CommentQueryDTO = {
      articleId: props.articleId,
      rootId: props.rootId,
      parentId: props.parentId,
      pageSize: 10,
    }
    if (nextCursorId.value !== null) {
      params.cursorId = nextCursorId.value
    }
    const { data } = await getReplies(params)
    const responseData = data.data
    replies.value = [...replies.value, ...responseData.result]
    nextCursorId.value = responseData.nextCursorId
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReplies()
})
</script>

<template>
  <div class="reply-list">
    <CommentItem
      v-for="reply in replies"
      :key="reply.id"
      :comment="reply"
      @reply="emit('reply', $event)"
    />
    <button
      v-if="nextCursorId !== null"
      class="load-more"
      :disabled="loading"
      @click="loadReplies"
    >
      {{ loading ? '加载中...' : '加载更多回复' }}
    </button>
  </div>
</template>

<style scoped>
.reply-list {
  margin-left: 24px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}
.load-more {
  background: none;
  border: none;
  color: #42b883;
  font-size: 12px;
  cursor: pointer;
  padding: 8px 0;
}
.load-more:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
