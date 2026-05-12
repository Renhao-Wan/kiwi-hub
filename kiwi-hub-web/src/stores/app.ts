import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const globalLoading = ref(false)

  function setLoading(value: boolean) {
    globalLoading.value = value
  }

  return { globalLoading, setLoading }
})
