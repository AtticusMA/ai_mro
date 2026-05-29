import request from '@/utils/request'

// 维修计划 / 任务包 — 按 MRO-005 spec 归属 dtwin 域
// Refs: MRO-005
export const getTaskList = (params) => request.get('/api/dtwin/tasks', { params })
export const getTaskDetail = (id) => request.get(`/api/dtwin/tasks/${id}`)
export const createTask = (data) => request.post('/api/dtwin/tasks', data)
export const updateTaskStatus = (id, status) => request.put(`/api/dtwin/tasks/${id}/status`, { status })
// 删除任务 = 取消（设置 status=cancelled）
export const deleteTask = (id) => request.put(`/api/dtwin/tasks/${id}/status`, { status: 'cancelled' })

// 人员排班
export const getAssignments = (packageId) => request.get('/api/dtwin/assignments', { params: { packageId } })
export const saveAssignment = (data) => request.post('/api/dtwin/assignments', data)
// 兼容旧调用：assignPersonnel(taskId, data) → POST /api/dtwin/assignments，body 自动注入 packageId
export const assignPersonnel = (taskId, data) =>
  request.post('/api/dtwin/assignments', { ...data, packageId: taskId })

// 工作量分析
export const getWorkload = (params) => request.get('/api/dtwin/analytics/workload', { params })

// 可用人员（占位：暂从用户列表中拉取）
export const getAvailablePersonnel = (params) => request.get('/api/sys/users', { params })

// 运营看板
export const getOperationDashboard = (hangarId) =>
  request.get('/api/dtwin/dashboard/operation', { params: hangarId ? { hangarId } : {} })
