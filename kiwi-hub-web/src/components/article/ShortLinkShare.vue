<script setup lang="ts">
import { ref } from 'vue'
import { createLink } from '@/api/link'

const props = defineProps<{
  articleId: number
}>()

const shortUrl = ref('')
const loading = ref(false)
const copied = ref(false)
const error = ref('')

async function generateLink() {
  loading.value = true
  error.value = ''
  try {
    const originalUrl = `${window.location.origin}/articles/${props.articleId}`
    const { data } = await createLink(originalUrl)
    shortUrl.value = data.data.shortUrl
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '生成短链失败'
  } finally {
    loading.value = false
  }
}

async function copyLink() {
  if (!shortUrl.value) return
  try {
    await navigator.clipboard.writeText(shortUrl.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // fallback: select input
    const input = document.createElement('input')
    input.value = shortUrl.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
}
</script>

<template>
  <div class="short-link-share">
    <button v-if="!shortUrl" class="share-btn" :disabled="loading" @click="generateLink">
      {{ loading ? '生成中...' : '生成短链分享' }}
    </button>
    <template v-else>
      <div class="link-row">
        <input :value="shortUrl" readonly class="link-input" />
        <button class="copy-btn" @click="copyLink">{{ copied ? '已复制' : '复制' }}</button>
      </div>
    </template>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.short-link-share {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.share-btn {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  color: #666;
}
.share-btn:hover {
  border-color: #42b883;
  color: #42b883;
}
.share-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.link-row {
  display: flex;
  gap: 8px;
}
.link-input {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  background: #f9f9f9;
}
.copy-btn {
  padding: 6px 12px;
  border: 1px solid #42b883;
  border-radius: 4px;
  background: #fff;
  color: #42b883;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
.copy-btn:hover {
  background: #f0f9f4;
}
.error {
  color: #ff4d4f;
  font-size: 12px;
  margin: 0;
}
</style>
