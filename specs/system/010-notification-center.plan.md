---
id: SYS-010
spec: system/010-notification-center.spec.md
status: approved
owner: '@arch'
created: 2026-05-29
updated: 2026-05-29
---

# Plan: 通知消息中心

## 1. 技术方案

- **前端组件**: NotificationBell.vue（Navbar 内嵌） + NotificationCenter.vue（独立页面）
- **状态管理**: Pinia store 管理 unreadCount，60s 轮询更新
- **数据层**: Mock-first（vite-plugin-mock），axios 封装
- **UI**: Element Plus 的 el-badge, el-popover, el-table, el-tag

## 2. 实施步骤

| 步骤 | 内容 | 交付物 |
|------|------|--------|
| 1 | Mock API + API 层 | src/mock/api/notification.js + src/api/notification.js |
| 2 | Pinia store | src/store/modules/notification.js |
| 3 | NotificationBell 组件 | src/components/NotificationBell.vue |
| 4 | Navbar 集成 | 修改 src/components/Navbar.vue |
| 5 | NotificationCenter 页面 | src/pages/NotificationCenter.vue |
| 6 | 路由注册 | src/router/routes.js |
| 7 | 构建验证 | npm run dev 通过 |

## 3. 架构图

```
Navbar
├── NotificationBell.vue
│   ├── el-badge (unread count from store)
│   ├── el-popover (recent 5 notifications)
│   └── "查看全部" → /notifications
└── UserDropdown

NotificationCenter.vue
├── Search card (type filter, status filter)
├── Action bar ("全部已读" button)
└── Table card (list + pagination)

Pinia Store (notification)
├── state: unreadCount, recentList
├── actions: fetchUnreadCount(), markRead(), markAllRead()
└── polling: setInterval 60s
```
