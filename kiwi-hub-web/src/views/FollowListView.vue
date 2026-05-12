<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getFollowingList, getFollowerList, followUser, unfollowUser } from '@/api/user'
import UserCard from '@/components/user/UserCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import type { UserCardVO } from '@/types/user'

const route = useRoute()
const users = ref<UserCardVO[]>([])
const loading = ref(false)

const isFollowing = computed(() => route.name === 'FollowingList')
const title = computed(() => isFollowing.value ? '我的关注' : '我的粉丝')

onMounted(async () => {
  loading.value = true
  try {
    const api = isFollowing.value ? getFollowingList : getFollowerList
    const { data } = await api({ pageNum: 1, pageSize: 50 })
    users.value = data.data.list
  } finally {
    loading.value = false
  }
})

async function handleFollow(userId: number) {
  await followUser(userId)
  const user = users.value.find(u => u.id === userId)
  if (user) user.isFollowed = true
}

async function handleUnfollow(userId: number) {
  await unfollowUser(userId)
  const user = users.value.find(u => u.id === userId)
  if (user) user.isFollowed = false
}
</script>

<template>
  <div class="follow-list-view">
    <h1>{{ title }}</h1>
    <LoadingSpinner v-if="loading" text="加载中..." />
    <EmptyState v-else-if="users.length === 0" text="暂无数据" />
    <div v-else class="user-list">
      <UserCard
        v-for="user in users"
        :key="user.id"
        :user="user"
        @follow="handleFollow"
        @unfollow="handleUnfollow"
      />
    </div>
  </div>
</template>

<style scoped>
.follow-list-view {
  max-width: 600px;
  margin: 0 auto;
}
.follow-list-view h1 {
  margin-bottom: 20px;
}
.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
