import { ref, type Ref } from 'vue'
import type { PageResult } from '@/types/api'

/**
 * 通用分页组合函数
 * @param fetchFn 数据获取函数，接收分页参数，返回 PageResult
 * @param defaultPageSize 默认每页数量
 */
export function usePagination<T>(
  fetchFn: (params: { pageNum: number; pageSize: number }) => Promise<{ data: { data: PageResult<T> } }>,
  defaultPageSize = 20,
) {
  const list: Ref<T[]> = ref([]) as Ref<T[]>
  const loading = ref(false)
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)
  const totalPages = ref(0)

  async function loadData() {
    loading.value = true
    try {
      const { data } = await fetchFn({ pageNum: pageNum.value, pageSize: pageSize.value })
      list.value = data.data.list
      total.value = data.data.total
      totalPages.value = data.data.totalPages
    } finally {
      loading.value = false
    }
  }

  function goToPage(page: number) {
    if (page < 1 || page > totalPages.value) return
    pageNum.value = page
    loadData()
  }

  function nextPage() {
    goToPage(pageNum.value + 1)
  }

  function prevPage() {
    goToPage(pageNum.value - 1)
  }

  function reset() {
    pageNum.value = 1
    list.value = []
    total.value = 0
    totalPages.value = 0
  }

  return {
    list,
    loading,
    pageNum,
    pageSize,
    total,
    totalPages,
    loadData,
    goToPage,
    nextPage,
    prevPage,
    reset,
  }
}
