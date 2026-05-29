---
id: ADR-003
title: 前端先行 + Mock-first 开发
status: accepted
date: 2026-05-23
deciders: ['@product', '@arch']
---

# ADR-003: 前端先行 + Mock-first 开发

## Context

项目首期目标是验证产品形态与权限模型，后端微服务（Nacos/Dubbo/MyBatis 拦截器/数据权限）从零搭建工期较长。如果按"后端 → 联调 → 前端"的传统瀑布顺序，会出现以下问题：
- 业务方看不到任何可交互的 UI，需求确认周期长；
- 前端等待接口阻塞，资源闲置；
- 等到联调阶段才暴露字段/契约不一致，返工成本高。

## Decision

1. **前端先行**：基于 Vue 3 + Vite + Element Plus + Tailwind 搭起完整骨架，按 Spec 先把所有页面/路由/组件/权限指令打通。
2. **Mock-first**：开发期 `VITE_USE_MOCK=true` 默认开启，所有 `/api/...` 由 `vite-plugin-mock` 拦截到 `src/mock/api/*`。
3. **契约同源**：每条 API 在 Spec 中以表格形式定义；前端 `src/api/*.js` 与 `src/mock/api/*.js` 必须同步更新（写在 `frontend/AGENTS.md` 强制约束）。
4. **后端跟进**：后端按相同 Spec 实现接口，前端通过切换 `VITE_USE_MOCK=false` 平滑接入真实接口。

## Consequences

### 正面
- 业务方在 M1 即可看到完整可交互原型，需求迭代快。
- 前端开发不阻塞，可与后端并行。
- 字段/契约的不一致在 Spec 阶段而非联调阶段暴露。

### 负面
- 必须严格维护 Mock 与真实接口的契约一致性，否则切换时会爆炸（用 Spec + AGENTS.md 强约束化解）。
- Mock 层有一定维护成本（与并行收益相比可接受）。

## Alternatives Considered

| 方案 | 描述 | 拒绝理由 |
|------|------|----------|
| 后端先行 | 先做接口再做 UI | 业务方无可视化反馈周期长 |
| MSW (Service Worker) | 浏览器层 Mock | `vite-plugin-mock` 与现有栈集成更轻；项目已有先例 |
| OpenAPI codegen | 由 OpenAPI 文件生成 client | 首期 Spec 表格已足够，引入 codegen 收益不抵成本 |

## References

- 关联 Spec：所有 `system/*.spec.md` 与 `auth/*.spec.md`
- 关联文档：`frontend/AGENTS.md` Mock-first 章节
