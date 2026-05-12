<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', email: '', password: '' })
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await userStore.register(form.value)
    router.push('/login')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-view">
    <h1>注册</h1>
    <form class="register-form" @submit.prevent="handleRegister">
      <div v-if="error" class="error">{{ error }}</div>
      <div class="form-group">
        <label>用户名</label>
        <input v-model="form.username" type="text" required />
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="form.email" type="email" required />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input v-model="form.password" type="password" required />
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <p class="switch-link">
        已有账号？<router-link to="/login">去登录</router-link>
      </p>
    </form>
  </div>
</template>

<style scoped>
.register-view {
  max-width: 400px;
  margin: 40px auto;
}
.register-form {
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
  color: #333;
}
.form-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
button[type="submit"] {
  padding: 10px;
  background: #42b883;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
button[type="submit"]:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error {
  color: #ff4d4f;
  font-size: 14px;
}
.switch-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}
.switch-link a {
  color: #42b883;
}
</style>
