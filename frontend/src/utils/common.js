/**
 * 通用工具函数
 */

/**
 * 格式化日期
 * @param {Date|string|number} date - 日期
 * @param {string} format - 格式字符串，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string}
 */
export const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化时间（相对时间）
 * @param {Date|string|number} date - 日期
 * @returns {string}
 */
export const formatTime = (date) => {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return formatDate(date, 'YYYY-MM-DD')
  }
}

/**
 * 防抖函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function}
 */
export const debounce = (func, wait = 300) => {
  let timeout

  return function (...args) {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function}
 */
export const throttle = (func, wait = 300) => {
  let timeout
  let previous = 0

  return function (...args) {
    const now = Date.now()
    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(this, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(this, args)
      }, remaining)
    }
  }
}

/**
 * 深拷贝
 * @param {any} obj - 要拷贝的对象
 * @returns {any}
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  if (obj instanceof Date) {
    return new Date(obj.getTime())
  }

  if (obj instanceof Array) {
    return obj.map(item => deepClone(item))
  }

  if (obj instanceof Object) {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 检查值是否为空
 * @param {any} value - 要检查的值
 * @returns {boolean}
 */
export const isEmpty = (value) => {
  return (
    value === null ||
    value === undefined ||
    value === '' ||
    (Array.isArray(value) && value.length === 0) ||
    (typeof value === 'object' && Object.keys(value).length === 0)
  )
}

/**
 * 检查值是否不为空
 * @param {any} value - 要检查的值
 * @returns {boolean}
 */
export const isNotEmpty = (value) => {
  return !isEmpty(value)
}

/**
 * 检查是否为邮箱
 * @param {string} email - 邮箱地址
 * @returns {boolean}
 */
export const isEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 检查是否为手机号
 * @param {string} phone - 手机号
 * @returns {boolean}
 */
export const isPhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 检查是否为URL
 * @param {string} url - URL
 * @returns {boolean}
 */
export const isUrl = (url) => {
  const urlRegex = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
  return urlRegex.test(url)
}

/**
 * 检查是否为身份证号
 * @param {string} idCard - 身份证号
 * @returns {boolean}
 */
export const isIdCard = (idCard) => {
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return idCardRegex.test(idCard)
}

/**
 * 生成随机字符串
 * @param {number} length - 长度
 * @returns {string}
 */
export const generateRandomString = (length = 16) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 生成 UUID
 * @returns {string}
 */
export const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 对象转 URL 查询字符串
 * @param {Object} obj - 对象
 * @returns {string}
 */
export const objectToQueryString = (obj) => {
  const params = new URLSearchParams()
  for (const key in obj) {
    if (obj.hasOwnProperty(key) && obj[key] !== null && obj[key] !== undefined) {
      params.append(key, obj[key])
    }
  }
  return params.toString()
}

/**
 * URL 查询字符串转对象
 * @param {string} queryString - 查询字符串
 * @returns {Object}
 */
export const queryStringToObject = (queryString) => {
  const params = new URLSearchParams(queryString)
  const obj = {}
  for (const [key, value] of params) {
    obj[key] = value
  }
  return obj
}

/**
 * 复制到剪贴板
 * @param {string} text - 要复制的文本
 * @returns {Promise}
 */
export const copyToClipboard = (text) => {
  return navigator.clipboard.writeText(text)
}

/**
 * 下载文件
 * @param {string} url - 文件 URL
 * @param {string} filename - 文件名
 */
export const downloadFile = (url, filename) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename || 'download'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 延迟执行
 * @param {number} ms - 延迟时间（毫秒）
 * @returns {Promise}
 */
export const delay = (ms = 1000) => {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 获取浏览器信息
 * @returns {Object}
 */
export const getBrowserInfo = () => {
  const ua = navigator.userAgent
  return {
    isChrome: /Chrome/.test(ua),
    isFirefox: /Firefox/.test(ua),
    isSafari: /Safari/.test(ua),
    isEdge: /Edge/.test(ua),
    isIE: /MSIE|Trident/.test(ua),
    isMobile: /Mobile|Android|iPhone/.test(ua)
  }
}

/**
 * 获取设备信息
 * @returns {Object}
 */
export const getDeviceInfo = () => {
  return {
    width: window.innerWidth,
    height: window.innerHeight,
    pixelRatio: window.devicePixelRatio,
    isMobile: window.innerWidth < 768
  }
}
