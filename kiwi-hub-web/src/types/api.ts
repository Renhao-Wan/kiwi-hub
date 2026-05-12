/**
 * 统一响应包装类
 * 对应后端 Result<T>
 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应包装类
 * 对应后端 PageResult<T>
 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/**
 * 后端响应状态码
 */
export const ResultCode = {
  SUCCESS: 20000,
  FAIL: 40001,
  PARAM_ERROR: 40002,
  UNAUTHORIZED: 40101,
  FORBIDDEN: 40301,
  NOT_FOUND: 40401,
  CONFLICT: 40901,
  SYSTEM_ERROR: 50001,
  DEPENDENCY_UNAVAILABLE: 50301,
} as const

export type ResultCode = (typeof ResultCode)[keyof typeof ResultCode]
