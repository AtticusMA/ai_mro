import request from '@/utils/request'

export const getScenarioList = (params) => {
  return request.get('/api/training/scenarios', { params })
}

export const createScenario = (data) => {
  return request.post('/api/training/scenarios', data)
}

export const updateScenario = (id, data) => {
  return request.put(`/api/training/scenarios/${id}`, data)
}

export const deleteScenario = (id) => {
  return request.delete(`/api/training/scenarios/${id}`)
}

export const publishScenario = (id) => {
  return request.put(`/api/training/scenarios/${id}/publish`)
}

export const getTraineeList = (params) => {
  return request.get('/api/training/trainees', { params })
}

export const getTraineeProfile = (id) => {
  return request.get(`/api/training/trainees/${id}/profile`)
}

export const getSessionList = (params) => {
  return request.get('/api/training/sessions', { params })
}

export const createTrainingSession = (data) => {
  return request.post('/api/training/sessions', data)
}

export const getAssessment = (sessionId) => {
  return request.get(`/api/training/assessments/${sessionId}`)
}

export const getIndividualReport = (traineeId) => {
  return request.get(`/api/training/reports/individual/${traineeId}`)
}

export const getOverviewReport = () => {
  return request.get('/api/training/reports/overview')
}
