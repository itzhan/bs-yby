import request from '@/utils/request'

/**
 * 登录
 */
export function login (parameter) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: parameter
  })
}

/**
 * 获取当前用户信息
 */
export function getInfo () {
  return request({
    url: '/users/current',
    method: 'get'
  })
}

/**
 * 登出（前端清除 token 即可）
 */
export function logout () {
  return Promise.resolve()
}
