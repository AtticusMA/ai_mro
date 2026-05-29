/**
 * 表单验证工具函数
 */

/**
 * 验证用户名
 * @param {string} username
 * @returns {boolean}
 */
export const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度在 3 到 20 个字符'))
  } else if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字、下划线和连字符'))
  } else {
    callback()
  }
}

/**
 * 验证密码
 * @param {string} password
 * @returns {boolean}
 */
export const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度在 6 到 20 个字符'))
  } else {
    callback()
  }
}

/**
 * 验证邮箱
 * @param {string} email
 * @returns {boolean}
 */
export const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

/**
 * 验证手机号
 * @param {string} phone
 * @returns {boolean}
 */
export const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

/**
 * 验证必填项
 * @param {any} value
 * @returns {boolean}
 */
export const validateRequired = (rule, value, callback) => {
  if (!value || (typeof value === 'string' && value.trim() === '')) {
    callback(new Error('此项为必填项'))
  } else {
    callback()
  }
}

/**
 * 验证URL
 * @param {string} url
 * @returns {boolean}
 */
export const validateUrl = (rule, value, callback) => {
  const urlRegex = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
  if (!value) {
    callback(new Error('请输入URL'))
  } else if (!urlRegex.test(value)) {
    callback(new Error('请输入正确的URL'))
  } else {
    callback()
  }
}

/**
 * 验证身份证号
 * @param {string} idCard
 * @returns {boolean}
 */
export const validateIdCard = (rule, value, callback) => {
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!idCardRegex.test(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

/**
 * 验证数字
 * @param {any} value
 * @returns {boolean}
 */
export const validateNumber = (rule, value, callback) => {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入数字'))
  } else if (isNaN(value)) {
    callback(new Error('请输入正确的数字'))
  } else {
    callback()
  }
}

/**
 * 验证整数
 * @param {any} value
 * @returns {boolean}
 */
export const validateInteger = (rule, value, callback) => {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入整数'))
  } else if (!Number.isInteger(Number(value))) {
    callback(new Error('请输入正确的整数'))
  } else {
    callback()
  }
}

/**
 * 验证中文
 * @param {string} value
 * @returns {boolean}
 */
export const validateChinese = (rule, value, callback) => {
  const chineseRegex = /^[一-龥]+$/
  if (!value) {
    callback(new Error('请输入中文'))
  } else if (!chineseRegex.test(value)) {
    callback(new Error('请输入正确的中文'))
  } else {
    callback()
  }
}

/**
 * 验证英文
 * @param {string} value
 * @returns {boolean}
 */
export const validateEnglish = (rule, value, callback) => {
  const englishRegex = /^[a-zA-Z]+$/
  if (!value) {
    callback(new Error('请输入英文'))
  } else if (!englishRegex.test(value)) {
    callback(new Error('请输入正确的英文'))
  } else {
    callback()
  }
}

/**
 * 检查值是否为空
 * @param {any} value
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
 * @param {any} value
 * @returns {boolean}
 */
export const isNotEmpty = (value) => {
  return !isEmpty(value)
}
