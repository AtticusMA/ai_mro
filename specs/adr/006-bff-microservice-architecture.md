---
id: ADR-006
title: BFF + 微服务架构 — manage-web 厚 BFF + 11 独立微服务
status: accepted
date: 2026-05-26
deciders: ['@arch']
---

# ADR-006: BFF + 微服务架构

## Context

项目为智能机务系统（MRO），Phase 1 目标是 manage-web 管理后台。
系统需支持多端（manage-web / user-web / iot-web 等），8 个 MRO 业务域
业务逻辑复杂，各端需要不同的数据裁剪和聚合方式。

早期考虑过模块化单体（Modular Monolith），但以下因素驱动选择微服务：
- 8 个 MRO 业务域技术栈差异大（IoT、AR、VR、RAG），独立部署需求明确
- 各业务域负载特征差异大（AI 推理 vs 结构化 CRUD），需独立扩容
- 团队按业务域分工，服务边界即团队边界

## Decision

采用 **厚 BFF + 微服务** 架构：

### 服务清单

| 服务 | 类型 | 职责 |
|------|------|------|
| gateway-service | 网关 | 路由、JWT 鉴权（HTTP 调 auth-service）、限流 |
| manage-web | 厚 BFF | REST Controller + Service 编排层，Dubbo 调下游 |
| auth-service | 基础微服务 | 认证、JWT 签发、权限验证 |
| system-service | 基础微服务 | 用户/部门/角色/菜单/字典/数据权限 |
| rag-service | 基础微服务 | RAG 编排，HTTP 调 RAGFlow |
| aircraft-health-service | MRO 微服务 | 飞机健康与预测性维护 |
| ar-maintenance-service | MRO 微服务 | AR 智慧维修协作 |
| fault-diagnosis-service | MRO 微服务 | 智能排故助手 |
| maintenance-manual-service | MRO 微服务 | 维修手册管理 |
| digital-twin-service | MRO 微服务 | 数字孪生机库 |
| tooling-material-service | MRO 微服务 | 工具间与航材管理 |
| vr-ar-training-service | MRO 微服务 | VR/AR 培训系统 |
| paperless-checkin-service | MRO 微服务 | 无纸化电子工卡 |

### 调用规则（铁律）

| 规则 | 说明 |
|------|------|
| Gateway → auth-service | HTTP POST `/internal/auth/verify`，结果 Redis 缓存 TTL = Token 剩余有效期 |
| Gateway → manage-web | HTTP，注入 `X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions` |
| manage-web → service | Dubbo RPC + Attachment 透传用户上下文 |
| service → service | **严禁**，无任何例外 |
| 数据库访问 | 每个 service 独立 schema，禁止跨 schema 查询 |

### manage-web 内部分层

```
Controller 层
  · 参数校验（@Valid / @Validated）
  · 无权限注解（权限判断在 Gateway）
  · 只调 Service，不写业务逻辑

Service 层（编排层）
  · StructuredTaskScope 并发调多个 Dubbo Service
  · 组装/裁剪响应数据
  · 不直接操作数据库，不直接调 RAGFlow
```

### 用户上下文传递链

```
JWT Token
  → Gateway 解析 + auth-service 验证
  → HTTP Header：X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions
  → manage-web 读取 Header
  → Dubbo Attachment：userId / deptId / roles / permissions
  → 各 service 从 RpcContext.getServerAttachment() 读取
```

## Consequences

### 正面
- 各业务域独立部署、独立扩容
- 多端（manage/user/iot）复用同一套下游 service
- 故障隔离：单个 MRO 服务故障不影响其他服务
- 团队按服务边界并行开发

### 负面
- 运维复杂度高（13 个独立部署单元）
- 需要 Nacos、Redis、分布式链路追踪（SkyWalking）支撑
- Dubbo 接口升级需维护版本兼容性

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| 模块化单体 | 无法满足各业务域独立扩容需求，AI 推理负载需独立隔离 |
| 薄 BFF | 聚合逻辑散落多处，Dashboard 等复杂页面难以维护 |
| 服务间 HTTP 互调 | 耦合度高，循环依赖风险，统一改为 Dubbo RPC 集中管控 |

## References

- 关联 Charter: `CHARTER.md`
- 关联 ADR: ADR-005（MRO 技术栈扩展）
- 设计文档: `docs/superpowers/specs/2026-05-26-backend-spec-optimization-design.md`