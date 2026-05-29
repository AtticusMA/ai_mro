import { useAuthStore } from '@/store/modules/auth'
import { canAccessRoute } from '@/utils/permission'

/**
 * 路由守卫（Vue Router 4 推荐使用 return 代替 next）
 */

/**
 * 前置守卫
 */
export const beforeEachGuard = async (to, from) => {
  const authStore = useAuthStore()

  // 获取 Token
  const hasToken = authStore.token

  // 如果访问登录页
  if (to.path === '/login') {
    // 如果已登录，重定向到首页
    if (hasToken) {
      return { path: '/dashboard' }
    }
    return true
  }

  // 如果访问 404 页面
  if (to.path === '/404') {
    return true
  }

  // 检查是否需要认证
  if (to.meta?.requiresAuth) {
    // 如果没有 Token，重定向到登录页
    if (!hasToken) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    // 检查用户信息是否已加载
    if (!authStore.user) {
      try {
        // 获取用户信息
        await authStore.getCurrentUser()
      } catch (error) {
        // 获取用户信息失败，清除 Token 并重定向到登录页
        authStore.clearAuth()
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    }

    // 检查路由权限
    if (!canAccessRoute(to)) {
      // 无权限访问，重定向到 404 页面
      return { path: '/404' }
    }

    return true
  }

  return true
}

/**
 * 后置守卫
 */
export const afterEachGuard = (to, from) => {
  // 更新页面标题
  const title = to.meta?.title || '首页'
  document.title = `${title} - 智慧机务`

  // 滚动到顶部
  window.scrollTo(0, 0)
}

/**
 * 错误处理
 */
export const handleRouterError = (error) => {
  console.error('路由错误:', error)

  // 处理特定的路由错误
  if (error.type === 'aborted') {
    console.log('路由导航被中止')
  } else if (error.type === 'duplicated') {
    console.log('重复的路由导航')
  }
}
