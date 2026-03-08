import request from '@/utils/request'

// 操作日志列表
export function getOperationLogList (params) {
  return request({ url: '/operation-logs', method: 'get', params })
}
