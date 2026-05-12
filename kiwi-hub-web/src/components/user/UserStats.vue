<script setup lang="ts">
import { formatNumber } from '@/utils/format'
import type { UserSocialStats } from '@/types/user'

defineProps<{
  stats: UserSocialStats
}>()
</script>

<template>
  <div class="user-stats">
    <div class="stat-item">
      <span class="stat-value">{{ formatNumber(stats.articleCount) }}</span>
      <span class="stat-label">文章</span>
    </div>
    <router-link to="/users/me/following" class="stat-item stat-link">
      <span class="stat-value">{{ formatNumber(stats.followingCount) }}</span>
      <span class="stat-label">关注</span>
    </router-link>
    <router-link to="/users/me/followers" class="stat-item stat-link">
      <span class="stat-value">{{ formatNumber(stats.followerCount) }}</span>
      <span class="stat-label">粉丝</span>
    </router-link>
  </div>
</template>

<style scoped>
.user-stats {
  display: flex;
  gap: 24px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.stat-link {
  text-decoration: none;
  cursor: pointer;
}
.stat-link:hover .stat-value {
  color: #42b883;
}
</style>
