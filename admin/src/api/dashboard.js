import request from '@/utils/request'

// 统计数据
export function getDashboardData () {
  return request({ url: '/dashboard', method: 'get' })
}
