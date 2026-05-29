---
id: SYS-010
title: 通知消息中心
domain: system
status: approved
owner: '@product'
version: 1.0.0
created: 2026-05-29
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 通知消息中心

## 1. 背景与目标

当前系统各模块（健康预警、工卡超时、证照到期、培训分配）的通知散落在各自页面中，用户缺少统一的消息入口。本模块提供全局通知中心，聚合各业务模块的预警和通知推送，提升用户对关键事件的感知效率。

对齐 Charter 目标：管理后台基础能力完善。

## 2. 范围

### In Scope
- Navbar 通知铃铛组件（未读计数 + 下拉最近通知）
- 通知列表页（分页、按类型/状态筛选、标记已读、全部已读）
- 通知数据聚合（health_alert / workcard_overdue / license_expiring / training_assignment）
- 前端 Pinia store 管理未读状态（轮询 60s）

### Out of Scope
- 实时 WebSocket 推送（后续增量）
- 邮件/短信外部渠道通知
- 通知模板配置管理后台

## 3. 用户故事 / 使用场景

- 作为系统用户，我希望在导航栏看到未读通知数量，以便快速感知是否有需要关注的事件。
- 作为系统用户，我希望点击铃铛查看最近通知摘要，以便快速了解最新动态。
- 作为系统用户，我希望进入通知中心按类型筛选和批量标记已读，以便高效管理通知。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | Navbar 显示通知铃铛图标和未读消息数量 | Given 用户已登录 When 存在未读通知 Then 铃铛旁显示红色未读数徽标 |
| FR-2 | 点击铃铛展开下拉面板，显示最近 5 条通知 | Given 点击铃铛 When 下拉展开 Then 显示最近 5 条通知的标题和时间 |
| FR-3 | 通知中心页面展示完整通知列表，支持分页 | Given 进入通知中心 When 通知数 > pageSize Then 显示分页控件 |
| FR-4 | 支持按通知类型筛选 | Given 通知列表页 When 选择类型筛选 Then 仅显示对应类型通知 |
| FR-5 | 支持单条标记已读和全部标记已读 | Given 存在未读通知 When 点击标记已读 Then 对应通知状态变为已读，未读计数减少 |

## 5. 非功能需求

- 性能：轮询间隔 60s，接口响应 < 500ms
- 安全：通知按用户隔离，仅查看本人通知

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| notification.id | bigint | Y | PK | 通知 ID |
| notification.type | enum | Y | health_alert/workcard_overdue/license_expiring/training_assignment | 通知类型 |
| notification.title | varchar(128) | Y | | 通知标题 |
| notification.content | varchar(512) | N | | 通知详细内容 |
| notification.is_read | boolean | Y | default false | 是否已读 |
| notification.created_at | timestamp | Y | | 创建时间 |
| notification.source_id | bigint | N | | 来源业务 ID（可跳转） |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/notifications | authenticated | 获取通知列表（分页+筛选） |
| GET | /api/notifications/unread-count | authenticated | 获取未读数量 |
| PUT | /api/notifications/{id}/read | authenticated | 标记单条已读 |
| PUT | /api/notifications/read-all | authenticated | 全部标记已读 |

## 8. 权限边界

- 无特殊菜单权限，所有已认证用户均可访问
- 数据权限：仅查看自己的通知

## 9. 验收标准

- [ ] 铃铛组件正常显示未读计数
- [ ] 下拉面板展示最近通知
- [ ] 通知中心页面列表、筛选、分页正常
- [ ] 标记已读后计数实时更新
- [ ] 全部已读功能正常

## 10. 未决问题

- [ ] 后续是否接入 WebSocket 实现实时推送
- [ ] 通知保留策略（超过 N 天自动清理）

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-05-29 | 初稿（含前端实现方案） |
