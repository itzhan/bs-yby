import request from '@/utils/request'

/** 获取企业列表 */
export function getCompanyList (params) {
  return request({ url: '/api/companies', method: 'get', params })
}

/** 获取企业详情 */
export function getCompanyDetail (id) {
  return request({ url: `/api/companies/${id}`, method: 'get' })
}

/** 获取当前登录企业信息 */
export function getCurrentCompany () {
  return request({ url: '/api/companies/current', method: 'get' })
}

/** 更新企业信息（当前企业） */
export function updateCompany (data) {
  return request({ url: '/api/companies', method: 'put', data })
}
