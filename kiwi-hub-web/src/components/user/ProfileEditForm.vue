<script setup lang="ts">
import { ref, watch } from 'vue'
import type { UserProfileDTO, UserProfile } from '@/types/user'

const props = defineProps<{
  profile: UserProfile
  saving?: boolean
}>()

const emit = defineEmits<{
  submit: [data: UserProfileDTO]
  cancel: []
}>()

const form = ref<UserProfileDTO>({ ...props.profile })

watch(() => props.profile, (val) => {
  form.value = { ...val }
}, { deep: true })

function handleSubmit() {
  emit('submit', { ...form.value })
}
</script>

<template>
  <form class="profile-edit-form" @submit.prevent="handleSubmit">
    <div class="form-group">
      <label>昵称</label>
      <input v-model="form.nickname" type="text" placeholder="请输入昵称" />
    </div>
    <div class="form-group">
      <label>个人简介</label>
      <textarea v-model="form.bio" rows="3" placeholder="介绍一下自己"></textarea>
    </div>
    <div class="form-group">
      <label>头像 URL</label>
      <input v-model="form.avatar" type="text" placeholder="https://..." />
    </div>
    <div class="form-group">
      <label>个人网站</label>
      <input v-model="form.website" type="text" placeholder="https://..." />
    </div>
    <div class="form-group">
      <label>所在地</label>
      <input v-model="form.location" type="text" placeholder="城市" />
    </div>
    <div class="form-group">
      <label>生日</label>
      <input v-model="form.birthday" type="date" />
    </div>
    <div class="form-group">
      <label>性别</label>
      <select v-model="form.gender">
        <option :value="0">未设置</option>
        <option :value="1">男</option>
        <option :value="2">女</option>
      </select>
    </div>
    <div class="form-actions">
      <button type="button" class="btn-cancel" @click="emit('cancel')">取消</button>
      <button type="submit" class="btn-save" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.profile-edit-form {
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
.form-group textarea,
.form-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}
.form-group textarea {
  resize: vertical;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
.btn-cancel {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.btn-save {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  background: #42b883;
  color: #fff;
  cursor: pointer;
}
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-save:hover:not(:disabled) {
  background: #38a373;
}
</style>
