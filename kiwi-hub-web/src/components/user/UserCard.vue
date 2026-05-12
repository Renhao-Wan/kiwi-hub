<script setup lang="ts">
import UserAvatar from '@/components/common/UserAvatar.vue'
import type { UserCardVO } from '@/types/user'

defineProps<{
  user: UserCardVO
}>()

const emit = defineEmits<{
  follow: [userId: number]
  unfollow: [userId: number]
}>()
</script>

<template>
  <div class="user-card">
    <UserAvatar :src="user.avatar" :alt="user.nickname || user.username" size="medium" />
    <div class="user-info">
      <span class="username">{{ user.nickname || user.username }}</span>
      <p v-if="user.bio" class="bio">{{ user.bio }}</p>
      <span class="follower-count">粉丝: {{ user.followerCount }}</span>
    </div>
    <button
      class="follow-btn"
      :class="{ followed: user.isFollowed }"
      @click="user.isFollowed ? emit('unfollow', user.id) : emit('follow', user.id)"
    >
      {{ user.isFollowed ? '已关注' : '关注' }}
    </button>
  </div>
</template>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.username {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}
.bio {
  color: #666;
  font-size: 12px;
  margin: 4px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.follower-count {
  color: #999;
  font-size: 12px;
}
.follow-btn {
  padding: 4px 16px;
  border: 1px solid #42b883;
  border-radius: 16px;
  background: #fff;
  color: #42b883;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
.follow-btn:hover {
  background: #f0f9f4;
}
.follow-btn.followed {
  background: #f0f0f0;
  border-color: #ddd;
  color: #999;
}
</style>
