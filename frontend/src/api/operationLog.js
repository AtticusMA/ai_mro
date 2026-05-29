import request from '@/utils/request'

export const getOperationLogList = (params) => {
  return request.get('/api/system/operation-log', { params })
}

export const getOperationLogDetail = (id) => {
  return request.get(`/api/system/operation-log/${id}`)
}
