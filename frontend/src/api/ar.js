import request from '@/utils/request'

/**
 * 获取巡检任务列表
 * @param {Object} params - 查询参数
 */
export const getInspectionList = (params) => {
  return request.get('/api/ar/inspections', { params })
}

/**
 * 创建巡检任务
 * @param {Object} data - 任务数据
 */
export const createInspection = (data) => {
  return request.post('/api/ar/inspections', data)
}

/**
 * 开始巡检
 * @param {number} id - 任务 ID
 */
export const startInspection = (id) => {
  return request.put(`/api/ar/inspections/${id}/start`)
}

/**
 * 完成巡检
 * @param {number} id - 任务 ID
 */
export const completeInspection = (id) => {
  return request.put(`/api/ar/inspections/${id}/complete`)
}

/**
 * 获取巡检异常记录
 * @param {number} id - 任务 ID
 */
export const getInspectionAnomalies = (id) => {
  return request.get(`/api/ar/inspections/${id}/anomalies`)
}

/**
 * 发起远程协作会话
 * @param {Object} data - 会话数据
 */
export const createSession = (data) => {
  return request.post('/api/ar/sessions', data)
}

/**
 * 专家加入会话
 * @param {number} id - 会话 ID
 */
export const joinSession = (id) => {
  return request.put(`/api/ar/sessions/${id}/join`)
}

/**
 * 结束会话
 * @param {number} id - 会话 ID
 */
export const endSession = (id) => {
  return request.put(`/api/ar/sessions/${id}/end`)
}

/**
 * 发送画面标注
 * @param {number} id - 会话 ID
 * @param {Object} data - 标注数据
 */
export const sendAnnotation = (id, data) => {
  return request.post(`/api/ar/sessions/${id}/annotations`, data)
}

/**
 * 获取远程协作会话列表
 * @param {Object} params - 查询参数
 */
export const getSessionList = (params) => {
  return request.get('/api/ar/sessions', { params })
}

/**
 * 获取影像档案列表
 * @param {Object} params - 查询参数
 */
export const getArchiveList = (params) => {
  return request.get('/api/ar/archives', { params })
}

/**
 * 回放影像
 * @param {number} id - 档案 ID
 */
export const getArchivePlayback = (id) => {
  return request.get(`/api/ar/archives/${id}/playback`)
}
