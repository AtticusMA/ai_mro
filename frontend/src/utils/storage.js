/**
 * 本地存储工具函数
 */

const TOKEN_KEY = 'auth_token'
const REFRESH_TOKEN_KEY = 'auth_refresh_token'
const USER_INFO_KEY = 'user_info'

/**
 * 获取 Token
 * @returns {string|null}
 */
export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 * @param {string} token
 */
export const setToken = (token) => {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  }
}

/**
 * 移除 Token
 */
export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取刷新 Token
 * @returns {string|null}
 */
export const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新 Token
 * @param {string} token
 */
export const setRefreshToken = (token) => {
  if (token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
  }
}

/**
 * 移除刷新 Token
 */
export const removeRefreshToken = () => {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

/**
 * 获取用户信息
 * @returns {Object|null}
 */
export const getUserInfo = () => {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  if (userInfo) {
    try {
      return JSON.parse(userInfo)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }
  return null
}

/**
 * 设置用户信息
 * @param {Object} user
 */
export const setUserInfo = (user) => {
  if (user) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
  }
}

/**
 * 移除用户信息
 */
export const removeUserInfo = () => {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 清除所有认证相关数据
 */
export const clearAuth = () => {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
}

/**
 * 设置本地存储项
 * @param {string} key
 * @param {any} value
 */
export const setItem = (key, value) => {
  try {
    if (typeof value === 'object') {
      localStorage.setItem(key, JSON.stringify(value))
    } else {
      localStorage.setItem(key, value)
    }
  } catch (error) {
    console.error('设置本地存储失败:', error)
  }
}

/**
 * 获取本地存储项
 * @param {string} key
 * @returns {any}
 */
export const getItem = (key) => {
  try {
    const value = localStorage.getItem(key)
    if (value) {
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    }
    return null
  } catch (error) {
    console.error('获取本地存储失败:', error)
    return null
  }
}

/**
 * 移除本地存储项
 * @param {string} key
 */
export const removeItem = (key) => {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    console.error('移除本地存储失败:', error)
  }
}

/**
 * 清除所有本地存储
 */
export const clearAll = () => {
  try {
    localStorage.clear()
  } catch (error) {
    console.error('清除本地存储失败:', error)
  }
}
