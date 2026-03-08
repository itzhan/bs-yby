import request from '@/utils/request'

/** 获取当前学生个人信息 */
export function getCurrentProfile () {
  return request({ url: '/api/student-profile/current', method: 'get' })
}

/** 更新学生个人信息 */
export function updateProfile (data) {
  return request({ url: '/api/student-profile', method: 'put', data })
}
