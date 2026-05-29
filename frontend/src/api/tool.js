import request from '@/utils/request'

// ---- Tool Cabinets ----
export const listCabinets = (params) => request.get('/api/tool/cabinets', { params })
export const getCabinetSlots = (id) => request.get(`/api/tool/cabinets/${id}/slots`)
export const triggerInventory = (id) => request.post(`/api/tool/cabinets/${id}/inventory`)

// ---- Tools ----
export const listTools = (params) => request.get('/api/tool/tools', { params })
export const getToolLifecycle = (id) => request.get(`/api/tool/tools/${id}/lifecycle`)

// ---- Borrow / Return ----
export const borrowTools = (data) => request.post('/api/tool/borrow', data)
export const returnTools = (data) => request.post('/api/tool/return', data)
export const listBorrowRecords = (params) => request.get('/api/tool/borrow-records', { params })

// ---- Alerts ----
export const listAlerts = (params) => request.get('/api/tool/alerts', { params })

// ---- Materials ----
export const listMaterials = (params) => request.get('/api/material/items', { params })
export const createMaterial = (data) => request.post('/api/material/items', data)
export const updateMaterial = (id, data) => request.put(`/api/material/items/${id}`, data)
export const listMaterialAlerts = (params) => request.get('/api/material/alerts', { params })

// ---- Repair Orders ----
export const listRepairOrders = (params) => request.get('/api/material/repair-orders', { params })
export const createRepairOrder = (data) => request.post('/api/material/repair-orders', data)
