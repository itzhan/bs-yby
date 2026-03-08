import request from '@/utils/request'

// 宣讲会列表
export function getJobFairList (params) { // params: { page, size, status, companyId }
  return request({ url: '/job-fairs', method: 'get', params })
}
// 宣讲会详情
export function getJobFairById (id) {
  return request({ url: `/job-fairs/${id}`, method: 'get' })
}
// 审核宣讲会
export function auditJobFair (id, data) {
  return request({ url: `/job-fairs/${id}/audit`, method: 'put', data })
}
// 预约列表
export function getJobFairBookings (id, params) {
  return request({ url: `/job-fairs/${id}/bookings`, method: 'get', params })
}
