import request from '@/utils/request'

/**
 * 获取部门列表（树形）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getDeptList = (params) => {
  return request.get('/api/system/dept/list', { params })
}

/**
 * 获取部门详情
 * @param {number} id - 部门 ID
 * @returns {Promise}
 */
export const getDeptDetail = (id) => {
  return request.get('/api/system/dept/detail', { params: { id } })
}

/**
 * 创建部门
 * @param {Object} data - 部门数据
 * @returns {Promise}
 */
export const createDept = (data) => {
  return request.post('/api/system/dept/create', data)
}

/**
 * 更新部门
 * @param {Object} data - 部门数据
 * @returns {Promise}
 */
export const updateDept = (data) => {
  return request.put('/api/system/dept/update', data)
}

/**
 * 删除部门
 * @param {number} id - 部门 ID
 * @returns {Promise}
 */
export const deleteDept = (id) => {
  return request.delete('/api/system/dept/remove', { params: { id } })
}
