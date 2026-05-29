import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(getUserInfo() || null)
  const token = ref(getToken() || '')
  const refreshToken = ref('')
  const isLoggedIn = computed(() => !!token.value)
  const loading = ref(false)
  const error = ref(null)

  // 获取器
  const userInfo = computed(() => user.value)
  const isAuthenticated = computed(() => !!token.value && !!user.value)

  /**
   * 检查用户是否拥有某个角色
   * @param {string} role - 角色标识
   * @returns {boolean}
   */
  const hasRole = (role) => {
    if (!user.value || !user.value.roles) return false
    return user.value.roles.includes(role)
  }

  /**
   * 检查用户是否拥有任意一个角色
   * @param {Array} roles - 角色数组
   * @returns {boolean}
   */
  const hasAnyRole = (roles) => {
    if (!user.value || !user.value.roles) return false
    return roles.some(role => user.value.roles.includes(role))
  }

  /**
   * 检查用户是否拥有所有角色
   * @param {Array} roles - 角色数组
   * @returns {boolean}
   */
  const hasAllRoles = (roles) => {
    if (!user.value || !user.value.roles) return false
    return roles.every(role => user.value.roles.includes(role))
  }

  /**
   * 用户登录
   * @param {Object} credentials - 登录凭证
   * @param {string} credentials.username - 用户名
   * @param {string} credentials.password - 密码
   * @returns {Promise}
   */
  const login = async (credentials) => {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.login(credentials)
      const { token: newToken, refreshToken: newRefreshToken, user: userData } = response.data

      // 保存 Token
      token.value = newToken
      refreshToken.value = newRefreshToken
      setToken(newToken)

      // 保存用户信息
      user.value = userData
      setUserInfo(userData)

      return response
    } catch (err) {
      error.value = err.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登出
   * @returns {Promise}
   */
  const logout = async () => {
    loading.value = true
    error.value = null
    try {
      await authApi.logout()
    } catch (err) {
      console.error('登出失败:', err)
    } finally {
      // 清除本地数据
      token.value = ''
      refreshToken.value = ''
      user.value = null
      removeToken()
      removeUserInfo()
      loading.value = false
    }
  }

  /**
   * 获取当前用户信息
   * @returns {Promise}
   */
  const getCurrentUser = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.getUserInfo()
      const userData = response.data

      user.value = userData
      setUserInfo(userData)

      return response
    } catch (err) {
      error.value = err.message || '获取用户信息失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置 Token
   * @param {string} newToken - 新 Token
   */
  const setAuthToken = (newToken) => {
    token.value = newToken
    setToken(newToken)
  }

  /**
   * 清除认证信息
   */
  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    removeToken()
    removeUserInfo()
  }

  /**
   * 刷新 Token
   * @returns {Promise}
   */
  const refreshAccessToken = async () => {
    try {
      const response = await authApi.refreshToken()
      const { token: newToken } = response.data

      token.value = newToken
      setToken(newToken)

      return response
    } catch (err) {
      // Token 刷新失败，清除认证信息
      clearAuth()
      throw err
    }
  }

  /**
   * 修改密码
   * @param {Object} data - 密码修改数据
   * @returns {Promise}
   */
  const changePassword = async (data) => {
    loading.value = true
    error.value = null
    try {
      const response = await authApi.changePassword(data)
      return response
    } catch (err) {
      error.value = err.message || '修改密码失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    user,
    token,
    refreshToken,
    isLoggedIn,
    loading,
    error,

    // 获取器
    userInfo,
    isAuthenticated,

    // 方法
    hasRole,
    hasAnyRole,
    hasAllRoles,
    login,
    logout,
    getCurrentUser,
    setAuthToken,
    clearAuth,
    refreshAccessToken,
    changePassword
  }
})
