import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

const request = axios.create({
  baseURL: '',
  timeout: 10000
})

// Request interceptor — attach Bearer token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — unwrap data & handle errors
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // If the backend wraps responses in { code, data, message }
    if (res.code !== undefined) {
      if (res.code === 200 || res.code === 0) {
        return res
      }
      // 401 — unauthorized
      if (res.code === 401) {
        message.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
        return Promise.reject(new Error(res.message || '未授权'))
      }
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // Plain response (no code wrapper)
    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        message.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
      } else if (status === 403) {
        message.error('没有权限访问')
      } else if (status === 404) {
        message.error('请求的资源不存在')
      } else if (status === 500) {
        message.error('服务器内部错误')
      } else {
        message.error(error.message || '请求失败')
      }
    } else {
      message.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
