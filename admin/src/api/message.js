import request from '@/utils/request'

// 消息列表
export function getMessageList (params) {
  return request({ url: '/messages', method: 'get', params })
}
// 标记已读
export function markAsRead (id) {
  return request({ url: `/messages/${id}/read`, method: 'put' })
}
// 全部标记已读
export function markAllAsRead () {
  return request({ url: '/messages/read-all', method: 'put' })
}
// 未读数量
export function getUnreadCount () {
  return request({ url: '/messages/unread-count', method: 'get' })
}
