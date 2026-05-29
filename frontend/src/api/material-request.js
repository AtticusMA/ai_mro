import request from '@/utils/request'

export const getMaterialRequestList = (params) => request.get('/api/material-requests', { params })
export const getMaterialRequestDetail = (id) => request.get(`/api/material-requests/${id}`)
export const createMaterialRequest = (data) => request.post('/api/material-requests', data)
export const approveMaterialRequest = (id, data) => request.post(`/api/material-requests/${id}/approve`, data)
export const rejectMaterialRequest = (id, data) => request.post(`/api/material-requests/${id}/reject`, data)
export const confirmReceive = (id, data) => request.post(`/api/material-requests/${id}/receive`, data)
export const getWorkcardBom = (workcardId) => request.get(`/api/workcards/${workcardId}/bom`)
