import request from '@/utils/request'

/** 投递岗位（学生） */
export function apply (data) {
  return request({ url: '/api/applications', method: 'post', data })
}

/** 获取我的投递记录（学生） */
export function getMyApplications (params) {
  return request({ url: '/api/applications/my', method: 'get', params })
}

/** 获取某岗位的投递记录（企业） - jobId 是路径参数 */
export function getJobApplications (jobId, params) {
  return request({ url: `/api/applications/job/${jobId}`, method: 'get', params })
}

/** 所有投递（管理员） */
export function getApplicationList (params) {
  return request({ url: '/api/applications', method: 'get', params })
}

/** 更新投递状态（企业/管理员） */
export function updateApplicationStatus (id, data) {
  return request({ url: `/api/applications/${id}/status`, method: 'put', data })
}

/** 撤回投递（学生） */
export function withdraw (id) {
  return request({ url: `/api/applications/${id}/withdraw`, method: 'put' })
}
