<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import UserAvatar from '@/components/common/UserAvatar.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import UserStats from '@/components/user/UserStats.vue'
import ProfileEditForm from '@/components/user/ProfileEditForm.vue'
import type { UserProfileDTO } from '@/types/user'

const userStore = useUserStore()
const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  try {
    await userStore.fetchCurrentUser()
  } finally {
    loading.value = false
  }
})

async function handleSave(data: UserProfileDTO) {
  saving.value = true
  error.value = ''
  try {
    await updateProfile(data)
    await userStore.fetchCurrentUser()
    editing.value = false
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-view">
    <h1>个人中心</h1>
    <LoadingSpinner v-if="loading" text="加载中..." />
    <template v-else-if="userStore.user">
      <div v-if="error" class="error">{{ error }}</div>
      <template v-if="!editing">
        <div class="profile-card">
          <UserAvatar :src="userStore.user.profile.avatar" size="large" />
          <div class="info-section">
            <h2>{{ userStore.user.profile.nickname || userStore.user.username }}</h2>
            <p class="email">{{ userStore.user.email }}</p>
            <p v-if="userStore.user.profile.bio" class="bio">{{ userStore.user.profile.bio }}</p>
            <p v-if="userStore.user.profile.location" class="location">{{ userStore.user.profile.location }}</p>
            <p v-if="userStore.user.profile.website" class="website">
              <a :href="userStore.user.profile.website" target="_blank">{{ userStore.user.profile.website }}</a>
            </p>
            <UserStats :stats="userStore.user.socialStats" />
            <button class="edit-btn" @click="editing = true">编辑资料</button>
          </div>
        </div>
      </template>
      <template v-else>
        <ProfileEditForm
          :profile="userStore.user.profile"
          :saving="saving"
          @submit="handleSave"
          @cancel="editing = false"
        />
      </template>
    </template>
  </div>
</template>

<style scoped>
.profile-view {
  max-width: 800px;
  margin: 0 auto;
}
.profile-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  padding: 24px;
  display: flex;
  gap: 24px;
}
.info-section {
  flex: 1;
}
.info-section h2 {
  margin: 0 0 8px;
}
.email {
  color: #666;
  font-size: 14px;
  margin: 0 0 8px;
}
.bio {
  color: #333;
  margin: 0 0 8px;
}
.location {
  color: #666;
  font-size: 14px;
  margin: 0 0 8px;
}
.website {
  margin: 0 0 16px;
}
.website a {
  color: #42b883;
  font-size: 14px;
}
.edit-btn {
  margin-top: 16px;
  padding: 6px 20px;
  border: 1px solid #42b883;
  border-radius: 4px;
  background: #fff;
  color: #42b883;
  cursor: pointer;
}
.edit-btn:hover {
  background: #f0f9f4;
}
.error {
  color: #ff4d4f;
  font-size: 14px;
  margin-bottom: 16px;
}
</style>
