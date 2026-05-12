<script setup lang="ts">
defineProps<{
  visible: boolean
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="dialog-overlay" @click.self="emit('cancel')">
      <div class="dialog">
        <h3 class="dialog-title">{{ title || '确认操作' }}</h3>
        <p class="dialog-message">{{ message || '确定要执行此操作吗？' }}</p>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="emit('cancel')">{{ cancelText || '取消' }}</button>
          <button class="btn-confirm" @click="emit('confirm')">{{ confirmText || '确定' }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-width: 320px;
  max-width: 480px;
}
.dialog-title {
  margin: 0 0 12px;
  font-size: 18px;
}
.dialog-message {
  color: #666;
  font-size: 14px;
  margin: 0 0 24px;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-cancel {
  padding: 6px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.btn-confirm {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  background: #42b883;
  color: #fff;
  cursor: pointer;
}
.btn-confirm:hover {
  background: #38a373;
}
</style>
