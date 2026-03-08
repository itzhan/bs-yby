import request from '@/utils/request'

// 企业列表
export function getCompanyList (params) { // params: { page, size, auditStatus, keyword }
  return request({ url: '/companies', method: 'get', params })
}
// 企业详情
export function getCompanyById (id) {
  return request({ url: `/companies/${id}`, method: 'get' })
}
// 审核企业
export function auditCompany (id, data) { // data: { auditStatus, auditRemark }
  return request({ url: `/companies/${id}/audit`, method: 'put', data })
}
