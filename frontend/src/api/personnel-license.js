import request from '@/utils/request'

// Per MRO-009 spec — all under /api/personnel/licenses
export const listLicenses = (params) => request.get('/api/personnel/licenses', { params })

export const getLicense = (id) => request.get(`/api/personnel/licenses/${id}`)

export const createLicense = (data) => request.post('/api/personnel/licenses', data)

export const updateLicense = (id, data) => request.put(`/api/personnel/licenses/${id}`, data)

export const deleteLicense = (id) => request.delete(`/api/personnel/licenses/${id}`)

export const renewLicense = (id, data) => request.post(`/api/personnel/licenses/${id}/renew`, data)

export const getLicenseAlerts = () => request.get('/api/personnel/licenses/alerts')

export const getLicenseStats = () => request.get('/api/personnel/licenses/stats')

export const checkQualification = (data) => request.post('/api/personnel/licenses/check-qualification', data)

export const uploadAttachment = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/api/personnel/licenses/${id}/attachment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const importLicenses = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/personnel/licenses/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
