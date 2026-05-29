import request from '@/utils/request'

// Quality sign — per MRO-008 spec, all under /api/workcards/* and /api/ncr
export const getPendingList = (params) => request.get('/api/workcards/pending-sign', { params })
export const getSignDetail = (id) => request.get(`/api/workcards/${id}/quality-sign`)
export const submitSign = (id, data) => request.post(`/api/workcards/${id}/quality-sign`, data)

// NCR — per MRO-008 spec, under /api/ncr
export const getNcrList = (params) => request.get('/api/ncr', { params })
export const createNcr = (data) => request.post('/api/ncr', data)
export const getNcrDetail = (id) => request.get(`/api/ncr/${id}`)
export const closeNcr = (id, data) => request.post(`/api/ncr/${id}/close`, data)
