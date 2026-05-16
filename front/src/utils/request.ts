import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/modules/user'
import type { ApiResponse } from '@/types/auth'

// 注意：前端请求路径里已经写了 /api/v1/...，
// 如果这里再设置 baseURL = '/api'，最终就会拼成 /api/api/v1/...。
// 因此这里必须保持 baseURL 为空，让 Vite 代理接管 /api 前缀的转发。
const request: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<unknown>
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) return res.data
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.clearUser()
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response.data
  },
  (error: AxiosError<any>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'

    if (status === 401) {
      const userStore = useUserStore()
      userStore.clearUser()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      ElMessage.error('登录已失效，请重新登录')
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('当前账号无权限执行该操作')
      return Promise.reject(error)
    }

    if (status === 404) {
      ElMessage.error('请求资源不存在')
      return Promise.reject(error)
    }

    if (status === 409) {
      ElMessage.error('数据已存在或发生冲突')
      return Promise.reject(error)
    }

    if (status === 429) {
      ElMessage.error('请求过于频繁，请稍后再试')
      return Promise.reject(error)
    }

    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
