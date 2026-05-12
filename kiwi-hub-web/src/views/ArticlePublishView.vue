<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishArticle } from '@/api/article'
import type { PublishArticleDTO } from '@/types/article'

const router = useRouter()
const form = ref<PublishArticleDTO>({
  title: '',
  content: '',
  contentType: 'markdown',
  summary: '',
  tags: [],
})
const tagInput = ref('')
const loading = ref(false)
const error = ref('')

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !form.value.tags?.includes(tag)) {
    form.value.tags = [...(form.value.tags || []), tag]
  }
  tagInput.value = ''
}

function removeTag(index: number) {
  form.value.tags = form.value.tags?.filter((_, i) => i !== index)
}

async function handlePublish() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await publishArticle(form.value)
    router.push(`/articles/${data.data}`)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '发布失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="publish-view">
    <h1>发布文章</h1>
    <form class="publish-form" @submit.prevent="handlePublish">
      <div v-if="error" class="error">{{ error }}</div>
      <div class="form-group">
        <label>标题</label>
        <input v-model="form.title" type="text" required placeholder="请输入文章标题" />
      </div>
      <div class="form-group">
        <label>摘要</label>
        <input v-model="form.summary" type="text" placeholder="可选，不填则自动截取" />
      </div>
      <div class="form-group">
        <label>标签</label>
        <div class="tags-input">
          <span v-for="(tag, i) in form.tags" :key="i" class="tag">
            {{ tag }}
            <button type="button" @click="removeTag(i)">&times;</button>
          </span>
          <input v-model="tagInput" type="text" placeholder="输入标签后回车" @keydown.enter.prevent="addTag" />
        </div>
      </div>
      <div class="form-group">
        <label>正文</label>
        <textarea v-model="form.content" rows="15" required placeholder="支持 Markdown 格式"></textarea>
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? '发布中...' : '发布文章' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.publish-view {
  max-width: 800px;
  margin: 0 auto;
}
.publish-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-group label {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
.form-group input,
.form-group textarea {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}
.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.tag {
  background: #42b883;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.tag button {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}
.tags-input input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 100px;
}
button[type="submit"] {
  padding: 10px;
  background: #42b883;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error {
  color: #ff4d4f;
  font-size: 14px;
}
</style>
