import request from '@/utils/request'

/** 岗位列表 */
export function getJobList (params) {
  return request({ url: '/api/jobs', method: 'get', params })
}

/** 岗位详情 */
export function getJobDetail (id) {
  return request({ url: `/api/jobs/${id}`, method: 'get' })
}

/** 创建岗位（企业） */
export function createJob (data) {
  return request({ url: '/api/jobs', method: 'post', data })
}

/** 更新岗位（企业） */
export function updateJob (id, data) {
  return request({ url: `/api/jobs/${id}`, method: 'put', data })
}

/** 关闭岗位（企业） */
export function closeJob (id) {
  return request({ url: `/api/jobs/${id}/close`, method: 'put' })
}

/** 删除岗位（企业） */
export function deleteJob (id) {
  return request({ url: `/api/jobs/${id}`, method: 'delete' })
}

/** 人岗匹配（学生）：返回带 matchScore 的岗位列表 */
export function matchJobs () {
  return request({ url: '/api/jobs/match', method: 'get' })
}

/** 个性化推荐岗位 */
export function getRecommendedJobs (limit = 8) {
  return request({ url: '/api/jobs/recommended', method: 'get', params: { limit } })
}

/** 上报浏览行为 */
export function recordBehavior (id, type = 1) {
  return request({ url: `/api/jobs/${id}/behavior`, method: 'post', params: { type } })
}
