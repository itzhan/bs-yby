import request from '@/utils/request'

/** 获取宣讲会列表 */
export function getJobFairList (params) {
  return request({ url: '/api/job-fairs', method: 'get', params })
}

/** 获取宣讲会详情 */
export function getJobFairDetail (id) {
  return request({ url: `/api/job-fairs/${id}`, method: 'get' })
}

/** 预约宣讲会（学生） */
export function bookJobFair (id) {
  return request({ url: `/api/job-fairs/${id}/book`, method: 'post' })
}

/** 取消预约（学生） */
export function cancelBooking (id) {
  return request({ url: `/api/job-fairs/${id}/book`, method: 'delete' })
}

/** 创建宣讲会（企业） */
export function createJobFair (data) {
  return request({ url: '/api/job-fairs', method: 'post', data })
}

/** 获取企业的宣讲会列表（企业，使用通用列表接口加companyId参数） */
export function getMyJobFairs (params) {
  return request({ url: '/api/job-fairs', method: 'get', params })
}

/** 查询当前学生已预约的宣讲会ID列表 */
export function getMyBookedFairIds () {
  return request({ url: '/api/job-fairs/my-bookings', method: 'get' })
}
