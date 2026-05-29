import request from '@/utils/request'

export const getOverview        = () => request.get('/api/dashboard/overview')
export const getKpis            = () => request.get('/api/dashboard/kpis')
export const getActiveWorkcards = () => request.get('/api/dashboard/workcards/active')
export const getHangarBays      = () => request.get('/api/dashboard/hangar/bays')
export const getArLive          = () => request.get('/api/dashboard/ar/live')
export const getFaultsByAta     = () => request.get('/api/dashboard/faults/by-ata')
export const getEvents          = () => request.get('/api/dashboard/events')
export const getSystemInfo      = () => request.get('/api/dashboard/system-info')
export const getOperationDashboard = () => request.get('/api/dashboard/operation')
