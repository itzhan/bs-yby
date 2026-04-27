import request from '@/utils/request'

/** 获取我的Offer列表（学生/企业通用，后端按角色返回） */
export function getMyOffers (params) {
  return request({ url: '/api/offers/my', method: 'get', params })
}

/** 获取企业Offer列表（同上接口，后端按角色区分） */
export function getCompanyOffers (params) {
  return request({ url: '/api/offers/my', method: 'get', params })
}

/** 发放Offer（企业） */
export function createOffer (data) {
  return request({ url: '/api/offers', method: 'post', data })
}

/** 学生回复Offer（接受/拒绝） */
export function respondOffer (id, data) {
  return request({ url: `/api/offers/${id}/respond`, method: 'put', data })
}
