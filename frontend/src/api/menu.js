import request from '@/utils/request'

/**
 * 获取当前用户的菜单树及权限码列表（侧边栏专用）
 * @returns {Promise<{menus: Array, permissions: Array}>}
 */
export const getUserMenus = () => {
  return request.get('/api/system/menu/user')
}

/**
 * 获取菜单树
 * @returns {Promise}
 */
export const getMenuTree = () => {
  return request.get('/api/system/menu/tree')
}

/**
 * 获取菜单详情
 * @param {number} id - 菜单 ID
 * @returns {Promise}
 */
export const getMenuDetail = (id) => {
  return request.get('/api/system/menu/detail', { params: { id } })
}

/**
 * 创建菜单
 * @param {Object} data - 菜单数据
 * @returns {Promise}
 */
export const createMenu = (data) => {
  return request.post('/api/system/menu/create', data)
}

/**
 * 更新菜单
 * @param {Object} data - 菜单数据
 * @returns {Promise}
 */
export const updateMenu = (data) => {
  return request.put('/api/system/menu/update', data)
}

/**
 * 删除菜单
 * @param {number} id - 菜单 ID
 * @returns {Promise}
 */
export const deleteMenu = (id) => {
  return request.delete('/api/system/menu/remove', { params: { id } })
}
