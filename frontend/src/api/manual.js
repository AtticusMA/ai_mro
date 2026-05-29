import request from '@/utils/request'

export const getManualList = (params) =>
  request.get('/api/manuals', { params })

export const getManualDetail = (id) =>
  request.get(`/api/manuals/${id}`)

export const uploadManual = (data) =>
  request.post('/api/manuals', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const deleteManual = (id) =>
  request.delete(`/api/manuals/${id}`)

export const triggerParse = (id) =>
  request.post(`/api/manuals/${id}/parse`)

export const publishManual = (id) =>
  request.post(`/api/manuals/${id}/publish`)

export const getManualVersions = (id, params) =>
  request.get(`/api/manuals/${id}/versions`, { params })

export const createManualVersion = (id, data) =>
  request.post(`/api/manuals/${id}/versions`, data)

export const submitTranslation = (id, data) =>
  request.post(`/api/manuals/${id}/translate`, data)

export const getTranslationResult = (taskId) =>
  request.get(`/api/manuals/translations/${taskId}`)

export const searchManuals = (params) =>
  request.get('/api/manuals/search', { params })
