<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  placeholder?: string
}>()

const emit = defineEmits<{
  submit: [content: string]
}>()

const content = ref('')
const submitting = ref(false)

async function handleSubmit() {
  if (!content.value.trim() || submitting.value) return
  submitting.value = true
  try {
    emit('submit', content.value.trim())
    content.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="comment-input" @submit.prevent="handleSubmit">
    <textarea
      v-model="content"
      :placeholder="placeholder || '写下你的评论...'"
      rows="3"
    ></textarea>
    <button type="submit" :disabled="!content.trim() || submitting">
      {{ submitting ? '发送中...' : '发表评论' }}
    </button>
  </form>
</template>

<style scoped>
.comment-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.comment-input textarea {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}
.comment-input button {
  align-self: flex-end;
  padding: 6px 16px;
  background: #42b883;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.comment-input button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
