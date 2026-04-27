import request from '@/utils/request'

/** 获取消息列表 */
export function getMessages(params) {
  return request.get('/api/messages', { params })
}

/** 标记单条消息已读 */
export function markAsRead(id) {
  return request.put(`/api/messages/${id}/read`)
}

/** 标记所有消息已读 */
export function markAllAsRead() {
  return request.put('/api/messages/read-all')
}

/** 获取未读消息数量 */
export function getUnreadCount() {
  return request.get('/api/messages/unread-count')
}
