<script setup lang="ts">
import { ref } from 'vue'
import { searchArticles } from '@/api/search'
import SearchBar from '@/components/search/SearchBar.vue'
import SearchResultCard from '@/components/search/SearchResultCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { ArticleSearchResultVO } from '@/types/search'

const keyword = ref('')
const results = ref<ArticleSearchResultVO[]>([])
const loading = ref(false)
const searched = ref(false)

async function handleSearch(kw: string) {
  loading.value = true
  searched.value = true
  try {
    const { data } = await searchArticles({ keyword: kw, pageNum: 1, pageSize: 20 })
    results.value = data.data.list
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="search-view">
    <h1>搜索文章</h1>
    <SearchBar v-model="keyword" @search="handleSearch" />
    <LoadingSpinner v-if="loading" text="搜索中..." />
    <EmptyState v-else-if="searched && results.length === 0" text="未找到相关文章" />
    <div v-else class="results">
      <SearchResultCard v-for="item in results" :key="item.id" :item="item" />
    </div>
  </div>
</template>

<style scoped>
.search-view {
  max-width: 800px;
  margin: 0 auto;
}
.search-view h1 {
  margin-bottom: 20px;
}
.results {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}
</style>
