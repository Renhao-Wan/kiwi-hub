import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ResultCode } from '@/types/api'
import type { Result } from '@/types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => config,
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const { data } = response
    if (data.code === ResultCode.SUCCESS) {
      return response
    }
    if (data.code === ResultCode.UNAUTHORIZED) {
      window.location.href = '/login'
      return Promise.reject(new Error(data.message || '请先登录'))
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
