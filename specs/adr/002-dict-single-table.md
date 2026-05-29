---
id: ADR-002
title: 字典管理改为单层平铺
status: accepted
date: 2026-05-23
deciders: ['@product', '@arch']
---

# ADR-002: 字典管理改为单层平铺

## Context

最初设计沿用主流后台框架的"字典类型 + 字典数据"两级结构（`sys_dict_type` / `sys_dict_data`），但实际产品需求中：
- 90% 的字典是简单枚举（性别、状态等），二级结构带来不必要的 CRUD 与缓存复杂度；
- 前端使用时永远只关心"字典组 + 字典码 → 字典值"三元组；
- 后台运营人员反馈两级结构难维护。

## Decision

字典表合并为单表 `sys_dict`，字段：
- `dict_group`（字典组，如 `sex`、`status`、`user_status`）
- `dict_code`（字典组内编码，如 `0`、`1`）
- `dict_label`（展示文本）
- `status`（启用/停用）
- 公共审计字段

唯一索引：`(dict_group, dict_code)`。

前端通过 `dict_group` 聚合查询，缓存到 Pinia store。

## Consequences

### 正面
- 表数量减半，CRUD 直观。
- 缓存策略简单：以 `dict_group` 为 key 一次性拉取整组。
- 前端字典组件 API 更简洁。

### 负面
- 失去对"字典组本身"的元数据管理（如 group 描述、归属业务）；如有需要可后续以独立表恢复，但首期不做。
- 与某些主流后台框架的脚手架不一致，迁移这些代码需要少量改动。

## Alternatives Considered

| 方案 | 描述 | 拒绝理由 |
|------|------|----------|
| 保留两级结构 | type + data | 复杂度溢价无法被首期需求消化 |
| JSON 列存储 | 一行一个 group，data 存 JSON | 不利于按 status 单条启停、不利于审计 |

## References

- 关联 Spec：`system/005-dict.spec.md`
- 触发文档：`项目需求文档-完善版.md` v1.1 修订记录
