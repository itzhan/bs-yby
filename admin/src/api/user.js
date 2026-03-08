import request from '@/utils/request'

// 用户列表
export function getUserList (params) { // params: { page, size, role, keyword }
  return request({ url: '/users', method: 'get', params })
}
// 用户详情
export function getUserById (id) {
  return request({ url: `/users/${id}`, method: 'get' })
}
// 更新用户
export function updateUser (id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}
// 更新用户状态
export function updateUserStatus (id, data) {
  return request({ url: `/users/${id}/status`, method: 'put', data })
}
// 删除用户
export function deleteUser (id) {
  return request({ url: `/users/${id}`, method: 'delete' })
}
