import request from '@/utils/request'

export const getAnalyticsOverview = () => {
  return request.get('/api/analytics/overview')
}

export const getAnalyticsTrends = () => {
  return request.get('/api/analytics/trends')
}

export const getModuleKpis = () => {
  return request.get('/api/analytics/module-kpis')
}

export const getMaintenanceDistribution = () => {
  return request.get('/api/analytics/maintenance-distribution')
}
