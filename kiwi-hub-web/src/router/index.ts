import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guest: true },
  },
  {
    path: '/articles/publish',
    name: 'ArticlePublish',
    component: () => import('@/views/ArticlePublishView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/articles/:articleId',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetailView.vue'),
  },
  {
    path: '/users/me',
    name: 'UserProfile',
    component: () => import('@/views/UserProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/SearchView.vue'),
  },
  {
    path: '/users/me/following',
    name: 'FollowingList',
    component: () => import('@/views/FollowListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/users/me/followers',
    name: 'FollowerList',
    component: () => import('@/views/FollowListView.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 首次访问时尝试恢复 Session
  if (!userStore.isLoggedIn) {
    await userStore.fetchCurrentUser()
  }

  // 需要认证的页面：未登录则跳转登录页
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // 仅限游客的页面（登录/注册）：已登录则跳转首页
  if (to.meta.guest && userStore.isLoggedIn) {
    return { name: 'Home' }
  }
})

export default router
