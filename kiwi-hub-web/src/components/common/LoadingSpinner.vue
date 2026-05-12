<script setup lang="ts">
defineProps<{
  size?: 'small' | 'medium' | 'large'
  text?: string
}>()
</script>

<template>
  <div class="loading-spinner" :class="size || 'medium'">
    <div class="spinner"></div>
    <p v-if="text" class="text">{{ text }}</p>
  </div>
</template>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.spinner {
  border: 3px solid #e8e8e8;
  border-top-color: #42b883;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.small .spinner { width: 20px; height: 20px; }
.medium .spinner { width: 32px; height: 32px; }
.large .spinner { width: 48px; height: 48px; }
.text {
  margin-top: 12px;
  color: #999;
  font-size: 14px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
