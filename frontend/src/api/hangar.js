import request from '@/utils/request'

export const getHangarList = (params) => {
  return request.get('/api/hangar/list', { params })
}

export const getHangarDetail = (id) => {
  return request.get(`/api/hangar/detail/${id}`)
}

export const getBayList = (hangarId) => {
  return request.get(`/api/hangar/${hangarId}/bays`)
}

export const getEquipmentList = (params) => {
  return request.get('/api/hangar/equipment', { params })
}

export const getEnvironmentData = (hangarId) => {
  return request.get(`/api/hangar/${hangarId}/environment`)
}

export const getScheduleList = (params) => {
  return request.get('/api/hangar/schedule', { params })
}

export const createSchedule = (data) => {
  return request.post('/api/hangar/schedule', data)
}
