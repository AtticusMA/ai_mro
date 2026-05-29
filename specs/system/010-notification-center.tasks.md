---
id: SYS-010
plan: system/010-notification-center.plan.md
created: 2026-05-29
updated: 2026-05-29
---

# Tasks: 通知消息中心

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | Mock API（通知列表/未读数/标记已读） | @dev | - | Mock 端点返回正确数据 | doing |
| T-002 | API 层封装（notification.js） | @dev | T-001 | API 函数可正常调用 | doing |
| T-003 | Pinia store（notification module） | @dev | T-002 | unreadCount 响应式更新 | doing |
| T-004 | NotificationBell 组件 + Navbar 集成 | @dev | T-003 | 铃铛显示未读数，下拉显示通知 | doing |
| T-005 | NotificationCenter 页面 | @dev | T-002 | 列表/筛选/标记已读/分页正常 | doing |
| T-006 | 路由注册 | @dev | T-005 | 页面可通过 URL 访问 | doing |
| T-007 | 构建验证 | @dev | T-001~T-006 | Vite dev server 编译通过 | doing |

> 状态枚举：todo / doing / review / done / blocked
