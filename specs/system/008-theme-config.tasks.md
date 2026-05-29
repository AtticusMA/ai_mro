---
id: SYS-008
plan: system/008-theme-config.plan.md
created: 2026-05-23
updated: 2026-05-23
---

# Tasks: 前端主题配置

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 创建 `src/utils/theme.js`：实现 `applyTheme(themeData)` 函数，注入 `--el-color-primary`、`--el-color-primary-light-{1~9}`、`--el-color-primary-dark-2`、`--sidebar-bg`、`--sidebar-text-color` 到 `:root` | '@dev' | - | applyTheme 传入任意主题色后页面 EP 按钮颜色正确变化 + Code Review | todo |
| T-002 | 创建 `src/stores/theme.js`（Pinia）：持有 `themeCode` / `primaryColor` / `sidebarBg` / `sidebarTextColor`；提供 `loadTheme()` 调用 API 并调用 `applyTheme`；提供 `setTheme(themeCode)` 调用 PUT 接口并刷新状态 | '@dev' | T-001 | store 状态正确响应 + Code Review | todo |
| T-003 | 创建 `src/mock/api/theme.js`：GET 返回当前主题（默认 internet）；PUT 更新内存中激活主题；7 种预设枚举颜色值与 spec 第 6 节一致 | '@dev' | - | Mock GET/PUT 均返回正确数据结构 + Code Review | todo |
| T-004 | 创建 `src/api/theme.js`：`getTheme()` → GET `/api/system/theme`；`updateTheme(themeCode)` → PUT `/api/system/theme` | '@dev' | T-003 | API 模块与 Mock 契约一致 + Code Review | todo |
| T-005 | 更新 `src/pages/Login.vue`：`onMounted` 调用 `themeStore.loadTheme()`，登录页应用主题色（按钮主色等随主题变化） | '@dev' | T-002, T-004 | 登录页打开后 EP 按钮颜色与当前主题一致 + Code Review | todo |
| T-006 | 更新 `src/components/Sidebar.vue`：将硬编码的侧边栏背景色和文字色改为读取 CSS 变量 `var(--sidebar-bg)` 和 `var(--sidebar-text-color)` | '@dev' | T-001 | 切换主题后侧边栏背景色和文字色随之变化 + Code Review | todo |
| T-007 | 更新主界面初始化（`src/components/Layout.vue` 或 `onMounted`）：登录后调用 `themeStore.loadTheme()` 加载主题 | '@dev' | T-002, T-004 | 登录后主界面主题与系统配置一致 + Code Review | todo |
| T-008 | 创建主题配置页面 `src/pages/system/ThemeConfig.vue`：展示 7 种行业主题卡片（含名称、色块预览）；当前激活主题高亮；点击保存调用 `themeStore.setTheme()`；保存后即时生效；使用 `v-permission="'system:theme:edit'"` 控制保存按钮可见性 | '@dev' | T-002, T-004 | 7 种卡片渲染正确，切换即时生效，无权限时按钮隐藏 + Code Review | todo |
| T-009 | 注册路由：在 `src/router/routes.js` system 子路由中添加 `{ path: 'theme', name: 'ThemeConfig', component: () => import('@/pages/system/ThemeConfig.vue'), meta: { title: '主题配置', requiresAuth: true, permissions: ['system:theme:edit'] } }` | '@dev' | T-008 | 路由可访问 + Code Review | todo |
| T-010 | 在 `src/mock/api/menu.js` 的 `_menus` 中添加主题配置菜单项：`{ id: 22, parentId: 2, name: '主题配置', type: 'C', icon: 'Brush', path: '/system/theme', component: 'system/ThemeConfig', permission: 'system:theme:edit', sort: 7, status: 1 }` | '@dev' | T-009 | admin 账号侧边栏出现"主题配置"菜单项 + Code Review | todo |
| T-011 | 构建验证：运行 `npm run build`，确认无编译错误；手动测试切换 7 种主题、登录页加载、降级兜底 | '@dev' | T-001~T-010 | build 成功，7 种主题切换正常，接口失败时不白屏 | todo |

> 状态枚举：todo / doing / review / done / blocked
