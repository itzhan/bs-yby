import request from '@/utils/request'

/** 获取管理员全局统计数据 */
export function getDashboardData() {
  return request.get('/api/dashboard')
}

/** 获取企业专属工作台数据 */
export function getCompanyDashboardData() {
  return request.get('/api/dashboard/company')
}
