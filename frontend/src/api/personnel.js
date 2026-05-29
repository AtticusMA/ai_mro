import request from '@/utils/request'

export const getPersonnelList = (params) => request.get('/api/personnel/list', { params })
export const getPersonnelDetail = (id) => request.get(`/api/personnel/${id}`)
export const createPersonnel = (data) => request.post('/api/personnel', data)
export const updatePersonnel = (id, data) => request.put(`/api/personnel/${id}`, data)
export const deletePersonnel = (id) => request.delete(`/api/personnel/${id}`)
export const getLicenseAlerts = (params) => request.get('/api/personnel/alerts', { params })
export const getLicenseTypes = () => request.get('/api/personnel/license-types')
export const checkPersonnelQualification = (personnelId, workcardType) =>
  request.get('/api/personnel/check-qualification', { params: { personnelId, workcardType } })