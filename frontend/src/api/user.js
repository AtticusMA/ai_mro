import request from '@/utils/request'

/**
 * 获取用户分页列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getUserPage = (params) => {
  return request.get('/api/system/user/page', { params })
}

/**
 * 获取用户详情
 * @param {number} id - 用户 ID
 * @returns {Promise}
 */
export const getUserDetail = (id) => {
  return request.get('/api/system/user/detail', { params: { id } })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export const createUser = (data) => {
  return request.post('/api/system/user/create', data)
}

/**
 * 更新用户
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export const updateUser = (data) => {
  return request.put('/api/system/user/update', data)
}

/**
 * 删除用户
 * @param {number} id - 用户 ID
 * @returns {Promise}
 */
export const deleteUser = (id) => {
  return request.delete('/api/system/user/remove', { params: { id } })
}

/**
 * 重置用户密码
 * @param {number} id - 用户 ID
 * @returns {Promise}
 */
export const resetUserPassword = (id) => {
  return request.put('/api/system/user/reset-password', null, { params: { id } })
}
