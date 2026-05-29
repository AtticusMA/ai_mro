import request from '@/utils/request'

/**
 * 获取机队健康状态列表
 * @param {Object} params - 查询参数
 */
export const getAircraftList = (params) => {
  return request.get('/api/health/aircraft', { params })
}

/**
 * 获取单机健康详情
 * @param {string} id - 飞机注册号
 */
export const getAircraftDetail = (id) => {
  return request.get(`/api/health/aircraft/${id}`)
}

/**
 * 获取单机故障记录
 * @param {string} id - 飞机注册号
 * @param {Object} params - 分页参数
 */
export const getAircraftFaults = (id, params) => {
  return request.get(`/api/health/aircraft/${id}/faults`, { params })
}

/**
 * 获取趋势预测报告
 * @param {string} id - 飞机注册号
 */
export const getAircraftPredictions = (id) => {
  return request.get(`/api/health/aircraft/${id}/predictions`)
}

/**
 * 获取预警列表
 * @param {Object} params - 查询参数
 */
export const getAlertList = (params) => {
  return request.get('/api/health/alerts', { params })
}

/**
 * 确认/处理预警
 * @param {number} id - 预警 ID
 */
export const acknowledgeAlert = (id) => {
  return request.put(`/api/health/alerts/${id}/acknowledge`)
}

/**
 * 获取预警规则列表
 * @param {Object} params - 查询参数
 */
export const getAlertRules = (params) => {
  return request.get('/api/health/alert-rules', { params })
}

/**
 * 创建预警规则
 * @param {Object} data - 规则数据
 */
export const createAlertRule = (data) => {
  return request.post('/api/health/alert-rules', data)
}

/**
 * 修改预警规则
 * @param {number} id - 规则 ID
 * @param {Object} data - 规则数据
 */
export const updateAlertRule = (id, data) => {
  return request.put(`/api/health/alert-rules/${id}`, data)
}

/**
 * 删除预警规则
 * @param {number} id - 规则 ID
 */
export const deleteAlertRule = (id) => {
  return request.delete(`/api/health/alert-rules/${id}`)
}

/**
 * 故障统计查询
 * @param {Object} params - 查询参数
 */
export const getStatistics = (params) => {
  return request.get('/api/health/statistics', { params })
}

/**
 * 导出健康报告
 * @param {Object} params - 导出参数
 */
export const exportReport = (params) => {
  return request.get('/api/health/export', { params, responseType: 'blob' })
}
