import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export const login = (data) => {
  return request.post('/api/auth/login', data)
}

/**
 * 用户登出
 * @returns {Promise}
 */
export const logout = () => {
  return request.post('/api/auth/logout')
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export const getUserInfo = () => {
  return request.get('/api/auth/user-info')
}

/**
 * 刷新 Token
 * @returns {Promise}
 */
export const refreshToken = () => {
  return request.post('/api/auth/refresh-token')
}

/**
 * 修改密码
 * @param {Object} data - 密码修改数据
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export const changePassword = (data) => {
  return request.post('/api/auth/change-password', data)
}
