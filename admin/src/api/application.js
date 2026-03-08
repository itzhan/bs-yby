import request from '@/utils/request'

// 所有投递列表
export function getApplicationList (params) { // params: { page, size, status, keyword }
  return request({ url: '/applications', method: 'get', params })
}
// 更新投递状态
export function updateApplicationStatus (id, data) { // data: { status, remark }
  return request({ url: `/applications/${id}/status`, method: 'put', data })
}
