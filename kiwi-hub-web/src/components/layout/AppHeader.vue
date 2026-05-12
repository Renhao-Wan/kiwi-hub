<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">KiwiHub</router-link>
      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/search">搜索</router-link>
        <template v-if="userStore.isLoggedIn">
          <router-link to="/articles/publish">发布文章</router-link>
          <router-link to="/users/me">个人中心</router-link>
          <span class="username">{{ userStore.user?.username }}</span>
          <button class="btn-logout" @click="handleLogout">登出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  color: #42b883;
  text-decoration: none;
}
.nav {
  display: flex;
  align-items: center;
  gap: 16px;
}
.nav a {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}
.nav a:hover {
  color: #42b883;
}
.username {
  color: #666;
  font-size: 14px;
}
.btn-logout {
  background: none;
  border: 1px solid #ddd;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.btn-logout:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}
</style>
