import request from '@/utils/request'

// 岗位列表
export function getJobList (params) { // params: { page, size, keyword, city, category, jobType, status, companyId }
  return request({ url: '/jobs', method: 'get', params })
}
// 岗位详情
export function getJobById (id) {
  return request({ url: `/jobs/${id}`, method: 'get' })
}
// 审核岗位
export function auditJob (id, data) { // data: { auditStatus: 2(pass)/4(reject), auditRemark }
  return request({ url: `/jobs/${id}/audit`, method: 'put', data })
}
// 删除岗位
export function deleteJob (id) {
  return request({ url: `/jobs/${id}`, method: 'delete' })
}
