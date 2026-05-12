import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 无限滚动组合函数
 * @param loadMore 加载更多回调
 * @param options 配置项
 */
export function useInfiniteScroll(
  loadMore: () => Promise<void>,
  options: { threshold?: number; container?: HTMLElement | null } = {},
) {
  const loading = ref(false)
  const finished = ref(false)
  const { threshold = 100, container = null } = options

  async function handleScroll() {
    if (loading.value || finished.value) return

    const el = container || document.documentElement
    const scrollTop = el.scrollTop || document.documentElement.scrollTop
    const scrollHeight = el.scrollHeight || document.documentElement.scrollHeight
    const clientHeight = el.clientHeight || document.documentElement.clientHeight

    if (scrollHeight - scrollTop - clientHeight < threshold) {
      loading.value = true
      try {
        await loadMore()
      } finally {
        loading.value = false
      }
    }
  }

  onMounted(() => {
    const target = container || window
    target.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    const target = container || window
    target.removeEventListener('scroll', handleScroll)
  })

  function finish() {
    finished.value = true
  }

  function reset() {
    finished.value = false
    loading.value = false
  }

  return { loading, finished, finish, reset }
}
