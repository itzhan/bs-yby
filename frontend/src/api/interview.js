import request from '@/utils/request'

/** 获取我的面试列表（学生/企业通用，后端按角色返回） */
export function getMyInterviews (params) {
  return request({ url: '/api/interviews/my', method: 'get', params })
}

/** 获取企业面试列表（企业，同上接口，后端按角色区分） */
export function getCompanyInterviews (params) {
  return request({ url: '/api/interviews/my', method: 'get', params })
}

/** 创建面试邀请（企业） */
export function createInterview (data) {
  return request({ url: '/api/interviews', method: 'post', data })
}

/** 更新面试状态 */
export function updateInterviewStatus (id, data) {
  return request({ url: `/api/interviews/${id}/status`, method: 'put', data })
}
