import { ref, watch, type Ref } from 'vue'

/**
 * 防抖组合函数
 * @param value 响应式值
 * @param delay 延迟毫秒数
 */
export function useDebounce<T>(value: Ref<T>, delay = 300): Ref<T> {
  const debouncedValue = ref(value.value) as Ref<T>
  let timer: ReturnType<typeof setTimeout> | null = null

  watch(value, (newVal) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      debouncedValue.value = newVal
    }, delay)
  })

  return debouncedValue
}

/**
 * 防抖函数组合
 * @param fn 需要防抖的函数
 * @param delay 延迟毫秒数
 */
export function useDebounceFn<T extends (...args: unknown[]) => unknown>(fn: T, delay = 300) {
  let timer: ReturnType<typeof setTimeout> | null = null

  function debouncedFn(...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
    }, delay)
  }

  function cancel() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return { debouncedFn, cancel }
}
