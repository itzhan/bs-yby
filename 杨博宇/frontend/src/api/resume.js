import request from '@/utils/request'

/** 获取我的简历列表 */
export function getMyResumes (params) {
  return request({ url: '/api/resumes', method: 'get', params })
}

/** 获取简历详情 */
export function getResumeById (id) {
  return request({ url: `/api/resumes/${id}`, method: 'get' })
}

/** 创建简历 */
export function createResume (data) {
  return request({ url: '/api/resumes', method: 'post', data })
}

/** 更新简历 */
export function updateResume (id, data) {
  return request({ url: `/api/resumes/${id}`, method: 'put', data })
}

/** 删除简历 */
export function deleteResume (id) {
  return request({ url: `/api/resumes/${id}`, method: 'delete' })
}
