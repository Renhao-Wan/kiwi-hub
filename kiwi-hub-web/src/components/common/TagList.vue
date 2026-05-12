<script setup lang="ts">
defineProps<{
  tags: string[]
  removable?: boolean
}>()

const emit = defineEmits<{
  remove: [index: number]
}>()
</script>

<template>
  <div class="tag-list">
    <span v-for="(tag, i) in tags" :key="i" class="tag">
      {{ tag }}
      <button v-if="removable" class="remove-btn" @click="emit('remove', i)">&times;</button>
    </span>
  </div>
</template>

<style scoped>
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  background: #f0f9f4;
  color: #42b883;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.remove-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  line-height: 1;
}
.remove-btn:hover {
  color: #ff4d4f;
}
</style>
