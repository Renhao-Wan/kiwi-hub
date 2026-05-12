<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import { toggleLike } from '@/api/interaction'
import ArticleContent from '@/components/article/ArticleContent.vue'
import ArticleStats from '@/components/article/ArticleStats.vue'
import ShortLinkShare from '@/components/article/ShortLinkShare.vue'
import CommentList from '@/components/comment/CommentList.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { ArticleDetail } from '@/types/article'

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const loading = ref(false)
const isLiked = ref(false)
const liking = ref(false)

onMounted(async () => {
  const articleId = Number(route.params.articleId)
  if (!articleId) return
  loading.value = true
  try {
    const { data } = await getArticleDetail(articleId)
    article.value = data.data
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  if (!article.value || liking.value) return
  liking.value = true
  try {
    const wasLiked = isLiked.value
    isLiked.value = !wasLiked
    article.value.stats.likeCount += wasLiked ? -1 : 1
    await toggleLike(article.value.id)
  } catch {
    if (article.value) {
      isLiked.value = !isLiked.value
      article.value.stats.likeCount += isLiked.value ? 1 : -1
    }
  } finally {
    liking.value = false
  }
}
</script>

<template>
  <div class="article-detail">
    <LoadingSpinner v-if="loading" text="加载中..." />
    <template v-else-if="article">
      <h1>{{ article.title }}</h1>
      <ArticleStats
        :view-count="article.stats.viewCount"
        :like-count="article.stats.likeCount"
        :comment-count="article.stats.commentCount"
      />
      <ArticleContent :content="article.content" :content-type="article.contentType" />
      <div class="actions">
        <button
          class="like-btn"
          :class="{ liked: isLiked }"
          :disabled="liking"
          @click="handleLike"
        >
          {{ isLiked ? '已赞' : '点赞' }} {{ article.stats.likeCount }}
        </button>
        <ShortLinkShare :article-id="article.id" />
      </div>
      <CommentList :article-id="article.id" />
    </template>
    <EmptyState v-else text="文章不存在" />
  </div>
</template>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
}
.article-detail h1 {
  margin-bottom: 12px;
}
.actions {
  margin: 24px 0;
  padding: 16px 0;
  border-top: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 16px;
}
.like-btn {
  padding: 8px 24px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.like-btn:hover {
  border-color: #42b883;
  color: #42b883;
}
.like-btn.liked {
  background: #42b883;
  border-color: #42b883;
  color: #fff;
}
.like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
