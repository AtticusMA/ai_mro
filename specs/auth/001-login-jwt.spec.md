---
id: AUTH-001
title: 登录与 JWT
domain: auth
status: draft
owner: '@arch'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 登录与 JWT

## 1. 背景与目标

提供企业内部用户的认证入口：用户名/密码登录 → BCrypt 校验 → 颁发 JWT。
首期不集成 SSO，密码加密见 ADR-001。

## 2. 范围

### In Scope
- 登录、登出、Token 刷新、获取当前用户信息、修改密码。
- 记住密码（前端 LocalStorage 持久化 Token）。
- 登出后 Token 加入 Redis 黑名单（可选）。

### Out of Scope
- SSO / 第三方登录。
- 多端互踢、设备管理。
- 验证码（仅作扩展点保留）。

## 3. 用户故事

- 作为内部员工，我希望用工号或用户名 + 密码登录系统。
- 作为管理员，我希望强制下线某用户。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 登录 | 输入正确用户名/密码后返回 Token + 用户基本信息 |
| FR-2 | 失败提示 | 密码错误统一返回 "用户名或密码错误"，避免账户枚举 |
| FR-3 | Token 校验 | 网关/服务端拦截器解析 JWT，无效或过期则 401 |
| FR-4 | 刷新 | refreshToken 未过期可换新 accessToken |
| FR-5 | 登出 | 清前端 Token，可选加入 Redis 黑名单直至到期 |

## 5. 非功能需求

- 登录接口 P95 < 500ms。
- accessToken 有效期默认 2h；refreshToken 7d。
- 密码使用 BCrypt（ADR-001）。

## 6. 数据契约

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string(3-20) | Y | |
| password | string(6-20) | Y | 明文传输（HTTPS），后端 BCrypt 校验 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| POST | /api/auth/login | 公开 | 登录 |
| POST | /api/auth/logout | 已登录 | 登出 |
| POST | /api/auth/refresh-token | 已登录 | 刷新 |
| GET | /api/auth/user-info | 已登录 | 当前用户信息 + 菜单 + 权限码 |
| POST | /api/auth/change-password | 已登录 | 修改密码 |

## 8. 权限边界

- 登录接口公开。
- 其余接口需 JWT；用户只能修改自己的密码。

## 9. 验收标准

- [ ] 登录后 `/api/auth/user-info` 返回的 `permissions` 与该用户角色权限一致。
- [ ] 错误密码 5 次内不锁定（首期），但写入登录日志。
- [ ] Token 过期返回 401 而非 403。

## 10. 未决问题

- [ ] 是否引入登录失败次数锁定？
- [ ] refreshToken 是否一次性使用（rotation）？

---

## B. 后端实现约束

### B.1 JWT Payload Schema

```json
{
  "sub": "1",
  "username": "admin",
  "deptId": 10,
  "roles": ["admin", "mro_manager"],
  "permissions": ["dept:list", "user:add"],
  "jti": "uuid-v4",
  "iat": 1716700800,
  "exp": 1716708000
}
```

- `sub`：userId（string）
- `jti`：JWT ID，用于 Redis 黑名单 key
- `iat` / `exp`：Unix 时间戳（秒）

### B.2 Token 传输协议

- 客户端：`Authorization: Bearer <accessToken>`
- Gateway 提取 Header，HTTP POST auth-service `/internal/auth/verify`
- 验证通过后注入 Header 转发 manage-web：
  - `X-User-Id: {userId}`
  - `X-User-Dept-Id: {deptId}`
  - `X-User-Roles: admin,mro_manager`（逗号分隔）
  - `X-User-Permissions: dept:list,user:add`（逗号分隔）
- manage-web 从 Header 读取，构建 `UserContextDTO`，通过 Dubbo Attachment 透传

### B.3 Redis 黑名单策略

| 场景 | Key 格式 | TTL |
|------|----------|-----|
| 验证缓存 | `auth:verify:{jti}` | Token 剩余有效期（动态） |
| 登出黑名单 | `auth:blacklist:{jti}` | Token 剩余有效期（动态） |

auth-service 验证逻辑：先查 `auth:blacklist:{jti}`，存在则拒绝；再查 `auth:verify:{jti}` 缓存命中直接返回。

### B.4 接口 JSON Schema

#### POST /api/auth/login

请求：
```json
{
  "username": "admin",
  "password": "password123"
}
```

响应（成功）：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatar": "https://..."
  },
  "timestamp": 1716700800000
}
```

响应（失败）：
```json
{
  "code": 4011,
  "msg": "用户名或密码错误",
  "data": null,
  "timestamp": 1716700800000
}
```

#### POST /api/auth/refresh-token

请求：
```json
{
  "refreshToken": "eyJhbGci..."
}
```

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGci...",
    "expiresIn": 7200
  },
  "timestamp": 1716700800000
}
```

#### GET /api/auth/user-info

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatar": "https://...",
    "deptId": 10,
    "deptName": "技术部",
    "roles": ["admin"],
    "permissions": ["dept:list", "user:add", "user:edit"]
  },
  "timestamp": 1716700800000
}
```

#### POST /api/auth/change-password

请求：
```json
{
  "oldPassword": "old123",
  "newPassword": "new456",
  "confirmPassword": "new456"
}
```

### B.5 Dubbo 接口签名（auth-service）

```java
public interface AuthDubboService {

    // Gateway 调用（HTTP，非 Dubbo）：POST /internal/auth/verify

    /** 获取用户完整信息（含权限码），用于 /user-info 接口 */
    UserInfoDTO getUserInfo(Long userId);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 刷新 Token */
    TokenDTO refreshToken(String refreshToken);

    /** 登出（将 jti 加入黑名单） */
    void logout(String jti, long remainingTtlSeconds);
}

public record UserInfoDTO(
    Long userId,
    String username,
    String realName,
    String avatar,
    Long deptId,
    String deptName,
    List<String> roles,
    List<String> permissions
) implements Serializable {}

public record TokenDTO(
    String accessToken,
    long expiresIn
) implements Serializable {}
```

### B.6 错误码（auth-service，4010–4039）

| 错误码 | 含义 |
|--------|------|
| 4010 | 用户名或密码错误 |
| 4011 | Token 无效或已过期 |
| 4012 | Token 已加入黑名单（已登出） |
| 4013 | refreshToken 无效或已过期 |
| 4014 | 新旧密码不能相同 |
| 4015 | 旧密码校验失败 |
| 4020 | 无此接口权限（403） |
| 4021 | 账号已禁用 |
| 4022 | 所属部门已禁用 |

### B.7 路由说明

`/api/auth/**` 由 Gateway 直接路由到 auth-service（HTTP），manage-web 不参与 auth 流程。
`GET /api/auth/user-info` 由 auth-service 内部 Dubbo 调 system-service 获取部门名称和菜单权限。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，从 项目需求文档-完善版.md §3.1 §6.1 拆出 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：JWT Payload Schema、Token 传输协议、Redis 黑名单、JSON Schema、Dubbo 接口签名、错误码 |
