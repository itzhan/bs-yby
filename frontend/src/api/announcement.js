import request from '@/utils/request'

/** 获取公告列表 */
export function getAnnouncementList(params) {
  return request.get('/api/announcements', { params })
}

/** 获取公告详情 */
export function getAnnouncementDetail(id) {
  return request.get(`/api/announcements/${id}`)
}
