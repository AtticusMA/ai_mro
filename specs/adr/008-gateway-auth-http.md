---
id: ADR-008
title: Gateway 鉴权策略 — HTTP 调 auth-service 验证 Token + 权限
status: accepted
date: 2026-05-26
deciders: ['@arch']
---

# ADR-008: Gateway 鉴权策略

## Context

Gateway（Spring Cloud Gateway，响应式 Netty）需要对每个请求进行：
1. JWT Token 有效性验证（签名 + 过期时间）
2. Redis 黑名单检查（登出后 Token 是否已失效）
3. 接口权限判断（当前用户是否有权访问该路由）

有三种方案：
- A：Gateway 本地解析 JWT
- B：Gateway HTTP 调 auth-service
- C：Gateway Dubbo 调 auth-service

## Decision

选择**方案 B：Gateway HTTP 调 auth-service**。

### 鉴权流程

```
请求到达 Gateway
  ↓
GlobalFilter：提取 Authorization Header
  ↓
HTTP POST auth-service /internal/auth/verify
  Body: { "token": "Bearer xxx" }
  ↓
auth-service 返回：
  成功: { "valid": true, "userId": 1, "deptId": 2,
          "roles": ["admin"], "permissions": ["dept:list"] }
  失败: { "valid": false, "code": 4011, "msg": "Token已过期" }
  ↓
Gateway 比对当前路由所需权限码（路由元数据中配置）
  ↓
通过：注入 Header 转发 manage-web
  X-User-Id: 1
  X-User-Dept-Id: 2
  X-User-Roles: admin
  X-User-Permissions: dept:list,user:list
失败：直接返回 401/403，不转发
```

### Redis 缓存策略

auth-service 将验证结果缓存到 Redis：
- Key：`auth:verify:{token_jti}`
- Value：用户上下文 JSON
- TTL：与 Token 剩余有效期一致（动态设置）
- 登出时：将 jti 加入黑名单 `auth:blacklist:{jti}`，TTL = Token 剩余有效期

### auth-service 内部验证接口

```
POST /internal/auth/verify
Header: X-Internal-Secret: {配置的内部调用密钥}
Body: { "token": "eyJhbGci..." }

Response 200（验证成功）:
{
  "valid": true,
  "userId": 1,
  "deptId": 10,
  "roles": ["admin", "mro_manager"],
  "permissions": ["dept:list", "user:add", "aircraft:view"]
}

Response 200（验证失败）:
{
  "valid": false,
  "code": 4011,
  "msg": "Token已过期"
}
```

注意：该接口不经过 Gateway，仅供 Gateway 内部调用，需配置 `X-Internal-Secret` 防止外部直接访问。

## Consequences

### 正面
- 支持 Redis 黑名单（登出后 Token 立即失效）
- 权限判断集中在 auth-service，manage-web 无需关心鉴权
- Gateway 保持轻量（无 Dubbo 依赖）
- 缓存命中后性能损耗 < 5ms

### 负面
- 每次请求多一次 HTTP 调用（缓存未命中时）
- auth-service 成为关键路径，需高可用部署（≥ 2 实例）

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| A：本地解析 JWT | 无法检查 Redis 黑名单，登出后 Token 在有效期内仍可用 |
| C：Dubbo 调 auth-service | Gateway 是响应式 Netty，引入 Dubbo 同步 RPC 需要线程模型适配，复杂度高收益低 |

## References

- 关联 ADR: ADR-006（BFF 微服务架构）
- 关联 Spec: AUTH-001（登录与 JWT）