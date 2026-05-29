import { useAuthStore } from '@/store/modules/auth'

/**
 * 检查用户是否拥有某个权限
 * @param {string} permission - 权限标识
 * @returns {boolean}
 */
export const hasPermission = (permission) => {
  const authStore = useAuthStore()
  const { user } = authStore

  if (!user || !user.permissions) {
    return false
  }

  // 超级权限
  if (user.permissions.includes('*:*:*')) {
    return true
  }

  return user.permissions.includes(permission)
}

/**
 * 检查用户是否拥有某个角色
 * @param {string} role - 角色标识
 * @returns {boolean}
 */
export const hasRole = (role) => {
  const authStore = useAuthStore()
  return authStore.hasRole(role)
}

/**
 * 检查用户是否拥有任意一个角色
 * @param {Array} roles - 角色数组
 * @returns {boolean}
 */
export const hasAnyRole = (roles) => {
  const authStore = useAuthStore()
  return authStore.hasAnyRole(roles)
}

/**
 * 检查用户是否拥有所有角色
 * @param {Array} roles - 角色数组
 * @returns {boolean}
 */
export const hasAllRoles = (roles) => {
  const authStore = useAuthStore()
  return authStore.hasAllRoles(roles)
}

/**
 * 检查用户是否已认证
 * @returns {boolean}
 */
export const isAuthenticated = () => {
  const authStore = useAuthStore()
  return authStore.isAuthenticated
}

/**
 * 检查用户是否是超级管理员
 * @returns {boolean}
 */
export const isAdmin = () => {
  return hasRole('admin')
}

/**
 * 权限指令
 * 用法：v-permission="['user:add', 'user:edit']"
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding

    if (value && value instanceof Array && value.length > 0) {
      const hasPermissions = value.some(permission => hasPermission(permission))

      if (!hasPermissions) {
        el.style.display = 'none'
      }
    } else {
      throw new Error('权限指令需要传入权限数组')
    }
  }
}

/**
 * 角色指令
 * 用法：v-role="['admin', 'manager']"
 */
export const roleDirective = {
  mounted(el, binding) {
    const { value } = binding

    if (value && value instanceof Array && value.length > 0) {
      const hasRoles = hasAnyRole(value)

      if (!hasRoles) {
        el.style.display = 'none'
      }
    } else {
      throw new Error('角色指令需要传入角色数组')
    }
  }
}

/**
 * 获取用户信息
 * @returns {Object|null}
 */
export const getUserInfo = () => {
  const authStore = useAuthStore()
  return authStore.user
}

/**
 * 获取用户权限列表
 * @returns {Array}
 */
export const getPermissions = () => {
  const authStore = useAuthStore()
  return authStore.user?.permissions || []
}

/**
 * 获取用户角色列表
 * @returns {Array}
 */
export const getRoles = () => {
  const authStore = useAuthStore()
  return authStore.user?.roles || []
}

/**
 * 检查是否有权限访问某个路由
 * @param {Object} route - 路由对象
 * @returns {boolean}
 */
export const canAccessRoute = (route) => {
  if (!route.meta) {
    return true
  }

  const { requiresAuth, permissions, roles } = route.meta

  // 检查是否需要认证
  if (requiresAuth && !isAuthenticated()) {
    return false
  }

  // 检查权限
  if (permissions && permissions.length > 0) {
    return permissions.some(permission => hasPermission(permission))
  }

  // 检查角色
  if (roles && roles.length > 0) {
    return hasAnyRole(roles)
  }

  return true
}
