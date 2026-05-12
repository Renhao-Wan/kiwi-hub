<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [keyword: string]
}>()

const keyword = ref(props.modelValue || '')

function handleSearch() {
  const trimmed = keyword.value.trim()
  if (!trimmed) return
  emit('update:modelValue', trimmed)
  emit('search', trimmed)
}
</script>

<template>
  <form class="search-bar" @submit.prevent="handleSearch">
    <input
      v-model="keyword"
      type="text"
      :placeholder="placeholder || '搜索文章...'"
      @input="emit('update:modelValue', keyword)"
    />
    <button type="submit">搜索</button>
  </form>
</template>

<style scoped>
.search-bar {
  display: flex;
  gap: 8px;
}
.search-bar input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}
.search-bar input:focus {
  outline: none;
  border-color: #42b883;
}
.search-bar button {
  padding: 10px 24px;
  background: #42b883;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.search-bar button:hover {
  background: #38a373;
}
</style>
