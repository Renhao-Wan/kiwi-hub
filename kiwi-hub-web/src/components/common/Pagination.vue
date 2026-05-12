<script setup lang="ts">
const props = defineProps<{
  pageNum: number
  totalPages: number
}>()

const emit = defineEmits<{
  'update:pageNum': [page: number]
}>()

function goTo(page: number) {
  if (page < 1 || page > props.totalPages) return
  emit('update:pageNum', page)
}
</script>

<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="pageNum <= 1" @click="goTo(pageNum - 1)">上一页</button>
    <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
    <button :disabled="pageNum >= totalPages" @click="goTo(pageNum + 1)">下一页</button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}
.pagination button {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.pagination button:hover:not(:disabled) {
  border-color: #42b883;
  color: #42b883;
}
.page-info {
  color: #666;
  font-size: 14px;
}
</style>
