import request from '@/utils/request'

/**
 * 获取角色分页列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getRolePage = (params) => {
  return request.get('/api/system/role/page', { params })
}

/**
 * 获取角色详情
 * @param {number} id - 角色 ID
 * @returns {Promise}
 */
export const getRoleDetail = (id) => {
  return request.get('/api/system/role/detail', { params: { id } })
}

/**
 * 获取角色列表（不分页，用于下拉选择）
 * @returns {Promise}
 */
export const getRoleList = () => {
  return request.get('/api/system/role/list')
}

/**
 * 创建角色
 * @param {Object} data - 角色数据
 * @returns {Promise}
 */
export const createRole = (data) => {
  return request.post('/api/system/role/create', data)
}

/**
 * 更新角色
 * @param {Object} data - 角色数据
 * @returns {Promise}
 */
export const updateRole = (data) => {
  return request.put('/api/system/role/update', data)
}

/**
 * 删除角色
 * @param {number} id - 角色 ID
 * @returns {Promise}
 */
export const deleteRole = (id) => {
  return request.delete('/api/system/role/remove', { params: { id } })
}
