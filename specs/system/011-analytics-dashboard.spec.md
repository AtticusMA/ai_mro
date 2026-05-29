---
id: SYS-011
title: 数据分析看板
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

# Spec: 数据分析看板

## 1. 背景与目标

当前 Dashboard 聚焦实时运营状态（班次进度、活跃工卡、AR 会话），缺少历史数据分析和跨模块 KPI 对比能力。本模块提供独立的数据分析页面，帮助管理层基于历史数据做决策。

对齐 Charter 目标：管理后台基础能力完善，支撑 AI 能力的数据基础。

## 2. 范围

### In Scope
- 综合 KPI 卡片（维修任务总数、平均完成时长、机队可用率、培训通过率）
- 月度趋势折线图（6 个月维修量/故障量/培训次数）
- 模块能力雷达图（健康/AR/培训/工卡/工具 5 维度评分）
- 维修分布饼图（按 ATA 章节分布）

### Out of Scope
- 自定义看板配置
- 数据导出/报表下载
- 实时流数据（属于 Dashboard）

## 3. 用户故事 / 使用场景

- 作为机库管理层，我希望查看跨模块的历史数据趋势，以便做出资源调配决策。
- 作为质量管理人员，我希望了解维修任务按 ATA 章节的分布，以便识别高频故障领域。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 展示 4 个核心 KPI 指标卡片 | Given 页面加载 When 数据返回 Then 4 个 KPI 卡片正确展示数值 |
| FR-2 | 月度趋势折线图展示 6 个月历史数据 | Given 页面加载 When 数据返回 Then 折线图展示 3 条线（维修/故障/培训） |
| FR-3 | 模块能力雷达图展示 5 个维度评分 | Given 页面加载 When 数据返回 Then 雷达图正确展示各模块得分 |
| FR-4 | 维修分布饼图按 ATA 章节展示 | Given 页面加载 When 数据返回 Then 饼图正确展示各章节占比 |

## 5. 非功能需求

- 性能：页面加载（含图表渲染）< 2s
- 可用性：图表支持 tooltip 交互

## 6. 数据契约

无独立数据表，聚合查询现有业务数据。

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/analytics/overview | dashboard:view | 综合 KPI 概览 |
| GET | /api/analytics/trends | dashboard:view | 月度趋势数据 |
| GET | /api/analytics/module-kpis | dashboard:view | 模块 KPI 雷达数据 |
| GET | /api/analytics/maintenance-distribution | dashboard:view | 维修 ATA 分布 |

## 8. 权限边界

- 权限标识：`dashboard:view`
- 菜单名称：数据分析

## 9. 验收标准

- [ ] 4 个 KPI 卡片正确展示
- [ ] 折线图正常渲染，tooltip 交互正常
- [ ] 雷达图正常渲染
- [ ] 饼图正常渲染，显示百分比

## 10. 未决问题

- [ ] 后续是否支持自定义时间范围查询
- [ ] 是否需要支持数据导出

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-05-29 | 初稿 |
