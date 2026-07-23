import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor - attach JWT token
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
http.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        return handleTokenRefresh(response.config)
      }
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return res.data as any
  },
  async (error) => {
    if (error.response?.status === 401) {
      try {
        await doRefreshToken()
        const token = localStorage.getItem('accessToken')
        error.config.headers.Authorization = `Bearer ${token}`
        return http(error.config)
      } catch {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

async function doRefreshToken(): Promise<void> {
  const refreshTokenStr = localStorage.getItem('refreshToken')
  if (!refreshTokenStr) throw new Error('No refresh token')
  const res = await axios.post<ApiResponse>('/api/auth/refresh', { refreshToken: refreshTokenStr })
  const data = res.data.data as any
  localStorage.setItem('accessToken', data.accessToken)
  localStorage.setItem('refreshToken', data.refreshToken)
}

// Handle refresh in interceptor (avoids circular axios instance call)
let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

function handleTokenRefresh(config: InternalAxiosRequestConfig) {
  if (!isRefreshing) {
    isRefreshing = true
    doRefreshToken()
      .then(() => {
        isRefreshing = false
        const token = localStorage.getItem('accessToken')!
        refreshSubscribers.forEach((cb) => cb(token))
        refreshSubscribers = []
      })
      .catch(() => {
        isRefreshing = false
        refreshSubscribers = []
        localStorage.clear()
        window.location.href = '/login'
      })
  }
  return new Promise((resolve) => {
    refreshSubscribers.push((token: string) => {
      config.headers.Authorization = `Bearer ${token}`
      resolve(http(config))
    })
  })
}

export default http
