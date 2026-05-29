---
id: SYS-011
plan: system/011-analytics-dashboard.plan.md
created: 2026-05-29
updated: 2026-05-29
---

# Tasks: 数据分析看板

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | Mock API（4 个分析端点） | @dev | - | Mock 返回正确数据 | doing |
| T-002 | API 层封装 | @dev | T-001 | API 函数可调用 | doing |
| T-003 | AnalyticsDashboard 页面（4 个 echarts 图表） | @dev | T-002 | 页面渲染正常 | doing |
| T-004 | 路由 + 菜单注册 | @dev | T-003 | 页面可通过菜单访问 | doing |
| T-005 | 构建验证 | @dev | T-001~T-004 | dev server 编译通过 | doing |

> 状态枚举：todo / doing / review / done / blocked
