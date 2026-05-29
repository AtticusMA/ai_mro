import request from '@/utils/request'

// ==================== 字典类型 ====================

/**
 * 获取字典类型分页列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getDictTypePage = (params) => {
  return request.get('/api/system/dict-type/page', { params })
}

/**
 * 获取字典类型详情
 * @param {number} id - 字典类型 ID
 * @returns {Promise}
 */
export const getDictTypeDetail = (id) => {
  return request.get('/api/system/dict-type/detail', { params: { id } })
}

/**
 * 创建字典类型
 * @param {Object} data - 字典类型数据
 * @returns {Promise}
 */
export const createDictType = (data) => {
  return request.post('/api/system/dict-type/create', data)
}

/**
 * 更新字典类型
 * @param {Object} data - 字典类型数据
 * @returns {Promise}
 */
export const updateDictType = (data) => {
  return request.put('/api/system/dict-type/update', data)
}

/**
 * 删除字典类型
 * @param {number} id - 字典类型 ID
 * @returns {Promise}
 */
export const deleteDictType = (id) => {
  return request.delete('/api/system/dict-type/remove', { params: { id } })
}

// ==================== 字典项 ====================

/**
 * 获取字典项列表
 * @param {Object} params - 查询参数 { dictTypeId }
 * @returns {Promise}
 */
export const getDictItemList = (params) => {
  return request.get('/api/system/dict-item/list', { params })
}

/**
 * 创建字典项
 * @param {Object} data - 字典项数据
 * @returns {Promise}
 */
export const createDictItem = (data) => {
  return request.post('/api/system/dict-item/create', data)
}

/**
 * 更新字典项
 * @param {Object} data - 字典项数据
 * @returns {Promise}
 */
export const updateDictItem = (data) => {
  return request.put('/api/system/dict-item/update', data)
}

/**
 * 删除字典项
 * @param {number} id - 字典项 ID
 * @returns {Promise}
 */
export const deleteDictItem = (id) => {
  return request.delete('/api/system/dict-item/remove', { params: { id } })
}
