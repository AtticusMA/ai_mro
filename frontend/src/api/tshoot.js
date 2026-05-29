import request from '@/utils/request'

/**
 * 获取知识库列表
 */
export const getKnowledgeBases = (params) => {
  return request.get('/api/tshoot/knowledge-bases', { params })
}

/**
 * 创建知识库
 */
export const createKnowledgeBase = (data) => {
  return request.post('/api/tshoot/knowledge-bases', data)
}

/**
 * 上传文档到知识库
 */
export const uploadDocument = (kbId, data) => {
  return request.post(`/api/tshoot/knowledge-bases/${kbId}/documents`, data)
}

/**
 * 删除知识库文档
 */
export const deleteDocument = (kbId, docId) => {
  return request.delete(`/api/tshoot/knowledge-bases/${kbId}/documents/${docId}`)
}

/**
 * 提交排故查询
 */
export const submitQuery = (data) => {
  return request.post('/api/tshoot/query', data)
}

/**
 * 获取排故结果
 */
export const getQueryResult = (id) => {
  return request.get(`/api/tshoot/query/${id}/result`)
}

/**
 * 查询历史维修记录
 */
export const getRepairHistory = (params) => {
  return request.get('/api/tshoot/history', { params })
}

/**
 * 故障统计分析
 */
export const getHistoryStatistics = (params) => {
  return request.get('/api/tshoot/history/statistics', { params })
}

/**
 * 获取排故报告列表
 */
export const getReportList = (params) => {
  return request.get('/api/tshoot/reports', { params })
}

/**
 * 获取排故报告详情
 */
export const getReportDetail = (id) => {
  return request.get(`/api/tshoot/reports/${id}`)
}

/**
 * 导出排故报告
 */
export const exportReport = (id) => {
  return request.get(`/api/tshoot/export/${id}`, { responseType: 'blob' })
}
