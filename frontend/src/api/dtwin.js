import request from '@/utils/request'

export const getHangarList = () =>
  request.get('/api/dtwin/hangars')

export const getHangarModel = (id) =>
  request.get(`/api/dtwin/hangars/${id}/model`)

export const getWorkstations = (hangarId, params) =>
  request.get(`/api/dtwin/hangars/${hangarId}/workstations`, { params })

export const getPlanList = (params) =>
  request.get('/api/dtwin/plans', { params })

export const createPlan = (data) =>
  request.post('/api/dtwin/plans', data)

export const updatePlan = (data) =>
  request.put(`/api/dtwin/plans/${data.id}`, data)

export const getOrderList = (params) =>
  request.get('/api/dtwin/orders', { params })

export const createOrder = (data) =>
  request.post('/api/dtwin/orders', data)

export const updateOrderProgress = (id, data) =>
  request.put(`/api/dtwin/orders/${id}/progress`, data)

export const getWorkloadAnalytics = (params) =>
  request.get('/api/dtwin/analytics/workload', { params })

export const getEfficiencyAnalytics = (params) =>
  request.get('/api/dtwin/analytics/efficiency', { params })

export const listTaskPackages = (params) => request.get('/api/dtwin/task-packages', { params })
export const createTaskPackage = (data) => request.post('/api/dtwin/task-packages', data)
export const updateTaskPackageStatus = (id, data) => request.put(`/api/dtwin/task-packages/${id}/status`, data)
export const getOperationDashboard = (params) => request.get('/api/dtwin/operation-dashboard', { params })
export const saveAssignment = (packageId, data) => request.post(`/api/dtwin/task-packages/${packageId}/assignments`, data)
export const listAssignments = (packageId) => request.get(`/api/dtwin/task-packages/${packageId}/assignments`)
