<script setup lang="ts">
defineProps<{
  text?: string
  icon?: string
}>()
</script>

<template>
  <div class="empty-state">
    <div class="icon">{{ icon || '📭' }}</div>
    <p class="text">{{ text || '暂无数据' }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}
.icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.text {
  color: #999;
  font-size: 14px;
}
</style>
