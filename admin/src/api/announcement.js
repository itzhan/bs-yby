import request from '@/utils/request'

// 公告列表
export function getAnnouncementList (params) { // params: { page, size, type, status }
  return request({ url: '/announcements', method: 'get', params })
}
// 公告详情
export function getAnnouncementById (id) {
  return request({ url: `/announcements/${id}`, method: 'get' })
}
// 创建公告
export function createAnnouncement (data) {
  return request({ url: '/announcements', method: 'post', data })
}
// 更新公告
export function updateAnnouncement (id, data) {
  return request({ url: `/announcements/${id}`, method: 'put', data })
}
// 删除公告
export function deleteAnnouncement (id) {
  return request({ url: `/announcements/${id}`, method: 'delete' })
}
