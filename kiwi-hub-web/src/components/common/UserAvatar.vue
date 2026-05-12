<script setup lang="ts">
defineProps<{
  src?: string
  alt?: string
  size?: 'small' | 'medium' | 'large'
}>()
</script>

<template>
  <img
    class="user-avatar"
    :class="size || 'medium'"
    :src="src || '/default-avatar.png'"
    :alt="alt || '头像'"
    @error="($event.target as HTMLImageElement).src = '/default-avatar.png'"
  />
</template>

<style scoped>
.user-avatar {
  border-radius: 50%;
  object-fit: cover;
  background: #f0f0f0;
}
.small { width: 24px; height: 24px; }
.medium { width: 40px; height: 40px; }
.large { width: 64px; height: 64px; }
</style>
