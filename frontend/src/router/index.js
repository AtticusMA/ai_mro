import { createRouter, createWebHistory } from 'vue-router'
import { allRoutes } from './routes'
import { beforeEachGuard, afterEachGuard, handleRouterError } from './guards'

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: allRoutes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置，返回保存的位置
    if (savedPosition) {
      return savedPosition
    }
    // 否则滚动到顶部
    return { top: 0 }
  }
})

/**
 * 注册路由守卫
 */
router.beforeEach(beforeEachGuard)
router.afterEach(afterEachGuard)

/**
 * 处理路由错误
 */
router.onError(handleRouterError)

export default router
