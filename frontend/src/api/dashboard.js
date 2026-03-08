import request from '@/utils/request'

/** 获取企业工作台统计数据 */
export function getDashboardData() {
  return request.get('/api/dashboard')
}
