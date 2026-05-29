---
id: SYS-011
spec: system/011-analytics-dashboard.spec.md
status: approved
owner: '@arch'
created: 2026-05-29
updated: 2026-05-29
---

# Plan: 数据分析看板

## 1. 技术方案

- **可视化**: echarts 5 按需引入（LineChart, PieChart, RadarChart）
- **布局**: KPI 卡片行 + 双列图表行（趋势+雷达） + 单列饼图行
- **数据层**: Mock-first，4 个 GET 端点

## 2. 实施步骤

| 步骤 | 内容 | 交付物 |
|------|------|--------|
| 1 | Mock API + API 层 | src/mock/api/analytics.js + src/api/analytics.js |
| 2 | AnalyticsDashboard 页面 | src/pages/AnalyticsDashboard.vue |
| 3 | 路由 + 菜单注册 | routes.js + menu.js |
| 4 | 构建验证 | dev server 通过 |
