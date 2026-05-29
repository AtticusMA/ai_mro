import request from '@/utils/request'

export const getWorkcardList = (params) => {
  return request.get('/api/workcards', { params })
}

export const getWorkcardDetail = (id) => {
  return request.get(`/api/workcards/${id}`)
}

export const createWorkcard = (data) => {
  return request.post('/api/workcards', data)
}

export const updateWorkcard = (id, data) => {
  return request.put(`/api/workcards/${id}`, data)
}

export const submitWorkcard = (id) => {
  return request.post(`/api/workcards/${id}/submit`)
}

export const approveWorkcard = (id, data) => {
  return request.post(`/api/workcards/${id}/approve`, data)
}

export const issueWorkcard = (id) => {
  return request.post(`/api/workcards/${id}/issue`)
}

export const completeStep = (id, stepId) => {
  return request.put(`/api/workcards/${id}/steps/${stepId}/complete`)
}

export const signWorkcard = (id, data) => {
  return request.post(`/api/workcards/${id}/sign`, data)
}

export const getSignatures = (id) => {
  return request.get(`/api/workcards/${id}/signatures`)
}

export const verifyBlockchain = (id) => {
  return request.get(`/api/workcards/${id}/blockchain-verify`)
}

export const getProgress = () => {
  return request.get('/api/workcards/progress')
}

export const getAlerts = () => {
  return request.get('/api/workcards/alerts')
}

export const getWorkcardSteps = (id) => {
  return request.get(`/api/workcards/${id}/steps`)
}

export const confirmStep = (workcardId, stepId, data) => {
  return request.post(`/api/workcards/${workcardId}/steps/${stepId}/confirm`, data)
}

export const checkin = (id, data) => {
  return request.post(`/api/workcards/${id}/checkin`, data)
}

export const checkout = (id, data) => {
  return request.post(`/api/workcards/${id}/checkout`, data)
}

export const uploadPhoto = (id, data) => {
  return request.post(`/api/workcards/${id}/photos`, data)
}

export const createReport = (id, data) => {
  return request.post(`/api/workcards/${id}/reports`, data)
}

export const getReports = (id) => {
  return request.get(`/api/workcards/${id}/reports`)
}

// ─── Quality Sign ─────────────────────────────────────────────────────────────

export const qualitySign = (data) => request.post('/api/workcards/quality-sign', data)
export const listSignRecords = (workcardId) => request.get(`/api/workcards/${workcardId}/sign-records`)
export const getSignRecord = (id) => request.get(`/api/workcards/sign-records/${id}`)

// ─── NCR ──────────────────────────────────────────────────────────────────────

export const createNcrWorkcard = (data) => request.post('/api/workcards/ncr', data)
export const closeNcrWorkcard = (data) => request.post('/api/workcards/ncr/close', data)
export const listNcrs = (params) => request.get('/api/workcards/ncr', { params })
export const getNcr = (id) => request.get(`/api/workcards/ncr/${id}`)

// ─── Checkin ──────────────────────────────────────────────────────────────────

export const checkIn = (data) => request.post('/api/workcards/checkin', data)
export const checkOut = (workcardId) => request.post('/api/workcards/checkout', null, { params: { workcardId } })
export const listCheckins = (workcardId) => request.get(`/api/workcards/${workcardId}/checkins`)
