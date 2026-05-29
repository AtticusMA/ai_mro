import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { getToken, removeToken } from '@/utils/storage'
import { encryptRequest } from '@/utils/smEncryptionService'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// 创建 axios 实例
// Mock 模式下不设置 baseURL，让请求 URL 保持为纯路径（如 /api/system/user/page），
// 确保 Mock.js 能稳定拦截；非 Mock 模式下使用后端地址
const request = axios.create({
  baseURL: USE_MOCK ? '' : (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  async (config) => {
    // 添加 Token
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 国密传输加密（非 Mock 模式，POST/PUT/PATCH，调用方传入 encryptFields）
    const encryptFields = config.encryptFields
    if (
      !USE_MOCK &&
      encryptFields?.length > 0 &&
      ['post', 'put', 'patch'].includes(config.method?.toLowerCase()) &&
      config.data
    ) {
      try {
        const { headers, body } = await encryptRequest(config.data, encryptFields)
        Object.assign(config.headers, headers)
        config.data = body
      } catch (err) {
        console.error('[SM Encrypt] 加密失败:', err)
        return Promise.reject(err)
      }
    }

    // 请求日志
    if (import.meta.env.DEV) {
      console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, config.data)
    }

    return config
  },
  (error) => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response) => {
    const { data } = response

    // 响应日志
    if (import.meta.env.DEV) {
      console.log(`[Response] ${response.config.url}`, data)
    }

    // 检查业务状态码
    if (data.code === 200) {
      return data
    }

    // 处理特定错误码
    if (data.code === 401) {
      // Token 过期或无效
      handleTokenExpired()
      return Promise.reject(new Error(data.message || '未授权，请重新登录'))
    }

    if (data.code === 403) {
      ElMessage.error('禁止访问')
      return Promise.reject(new Error(data.message || '禁止访问'))
    }

    if (data.code === 404) {
      ElMessage.error('请求的资源不存在')
      return Promise.reject(new Error(data.message || '请求的资源不存在'))
    }

    if (data.code === 500) {
      ElMessage.error('服务器内部错误')
      return Promise.reject(new Error(data.message || '服务器内部错误'))
    }

    // 其他错误
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    // 处理网络错误
    if (error.response) {
      const { status, data } = error.response

      // 响应日志
      if (import.meta.env.DEV) {
        console.error(`[Error] ${status}`, data)
      }

      if (status === 401) {
        handleTokenExpired()
        return Promise.reject(new Error('未授权，请重新登录'))
      }

      if (status === 403) {
        ElMessage.error('禁止访问')
        return Promise.reject(new Error('禁止访问'))
      }

      if (status === 404) {
        ElMessage.error('请求的资源不存在')
        return Promise.reject(new Error('请求的资源不存在'))
      }

      if (status === 500) {
        ElMessage.error('服务器内部错误')
        return Promise.reject(new Error('服务器内部错误'))
      }

      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }

    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时')
      return Promise.reject(new Error('请求超时'))
    }

    if (!window.navigator.onLine) {
      ElMessage.error('网络连接失败')
      return Promise.reject(new Error('网络连接失败'))
    }

    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

/**
 * 处理 Token 过期
 */
const handleTokenExpired = () => {
  const authStore = useAuthStore()

  // 清除认证信息
  removeToken()
  authStore.clearAuth()

  // 重定向到登录页
  window.location.href = '/login'
}

/**
 * GET 请求
 * @param {string} url
 * @param {Object} config
 * @returns {Promise}
 */
export const get = (url, config = {}) => {
  return request.get(url, config)
}

/**
 * POST 请求
 * @param {string} url
 * @param {Object} data
 * @param {Object} config
 * @returns {Promise}
 */
export const post = (url, data = {}, config = {}) => {
  return request.post(url, data, config)
}

/**
 * PUT 请求
 * @param {string} url
 * @param {Object} data
 * @param {Object} config
 * @returns {Promise}
 */
export const put = (url, data = {}, config = {}) => {
  return request.put(url, data, config)
}

/**
 * PATCH 请求
 * @param {string} url
 * @param {Object} data
 * @param {Object} config
 * @returns {Promise}
 */
export const patch = (url, data = {}, config = {}) => {
  return request.patch(url, data, config)
}

/**
 * DELETE 请求
 * @param {string} url
 * @param {Object} config
 * @returns {Promise}
 */
export const del = (url, config = {}) => {
  return request.delete(url, config)
}

export default request
