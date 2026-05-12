<script setup lang="ts">
import { renderMarkdown } from '@/utils/markdown'
import DOMPurify from 'dompurify'

const props = defineProps<{
  content: string
  contentType: string
}>()

function renderContent(): string {
  if (props.contentType === 'markdown') {
    return renderMarkdown(props.content)
  }
  return DOMPurify.sanitize(props.content)
}
</script>

<template>
  <div class="article-content" v-html="renderContent()"></div>
</template>

<style scoped>
.article-content {
  line-height: 1.8;
  font-size: 16px;
}
.article-content :deep(pre) {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}
.article-content :deep(code) {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 14px;
}
.article-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.article-content :deep(blockquote) {
  border-left: 4px solid #42b883;
  padding-left: 16px;
  color: #666;
  margin: 16px 0;
}
</style>
