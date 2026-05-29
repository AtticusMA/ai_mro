---
id: PLT-004
title: 前后端传输国密加密（SM2+SM4+HMAC-SM3）
domain: platform
status: approved
owner: '@arch'
version: 0.2.0
created: 2026-05-27
updated: 2026-05-27
charter: CHARTER.md
supersedes: []
depends-on:
  - platform/001-architecture.spec.md
---

# Spec: 前后端传输国密加密（SM2+SM4+HMAC-SM3）

## 1. 背景与目标

当前前后端通信不满足国家商用密码合规要求（等保 2.0 / 商密认证）。

**目标：**
- 全部 API 接口请求实施国密加密传输
- 敏感字段字段级加密（保密性）：SM4-CBC
- 密钥安全传输：SM2 公钥加密 SM4 密钥
- 防篡改 + 防重放：HMAC-SM3 + timestamp + requestId
- `@Encrypted` 注解由业务方自行标注，框架层透明处理
- 响应体暂不加密（Out of Scope）
- 对齐 Charter 安全约束：传输加密合规

## 2. 范围

### In Scope

- 公钥分发接口：`GET /api/security/public-key`
- 后端工具类：`SM2Util`、`SM4Util`、`SM3Util`
- 后端解密拦截器：`SmDecryptInterceptor`（Spring MVC HandlerInterceptor，拦截全部 `/api/**`）
- `FieldEncryptionUtil` 内部调用改为 SM4
- 后端 Maven 依赖：`bcprov-jdk18on` + `hutool-crypto`
- `application.yml` 配置：`security.sm2.*`
- 前端国密服务：`SmEncryptionService`（替换原加密服务）
- 前端依赖：`sm-crypto`
- Axios 请求拦截器接入 `SmEncryptionService`（覆盖全部请求）
- SM2 密钥对生成工具脚本（一次性运行，输出存入 Nacos）

### Out of Scope

- 响应体加密
- HTTPS/TLS 层改造（TLCP 国密 TLS）
- 数据库静态数据加密
- `keyId` 多版本密钥共存（停机轮换，无需多版本）
- 前端 SM2 双向签名（前端无私钥，签名使用 HMAC-SM3）
- 原 RSA+AES 方案兼容（直接下线，不保留兼容模式）

## 3. 用户故事 / 使用场景

- 作为**安全合规负责人**，我希望全部 API 请求使用国密算法加密传输，以便满足等保 2.0 及商密认证要求。
- 作为**后端业务开发者**，我希望只需在 DTO 字段上标注 `@Encrypted`，框架自动完成加解密，以便不侵入业务逻辑。
- 作为**前端开发者**，我希望 Axios 拦截器自动处理加密，业务代码无感知，以便无缝接入。
- 作为**运维人员**，我希望 SM2 密钥存储在 Nacos 配置中心，停机时可安全轮换，以便管理密钥生命周期。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 公钥分发接口 | Given `GET /api/security/public-key`，When 请求（无需认证），Then 返回 Hex 格式 SM2 公钥（64字节，不含 04 前缀） |
| FR-2 | 后端 SM2 工具类 | Given SM2 公钥 Hex，When 加密任意 ≤190 字节数据，Then 用对应私钥可正确解密；私钥签名后可用公钥验签通过 |
| FR-3 | 后端 SM4 工具类 | Given 16字节密钥和 IV，When 加密字符串，Then 用同一密钥+IV 可正确解密还原原文 |
| FR-4 | 后端 SM3/HMAC-SM3 工具类 | Given SM4 密钥和消息，When 计算 HMAC-SM3，Then 相同输入输出一致；篡改消息后验证失败 |
| FR-5 | `FieldEncryptionUtil` 改为 SM4 | Given 含 `@Encrypted` 字段的业务对象，When 调用 encryptFields/decryptFields，Then 字段正确加解密，其余字段不变 |
| FR-6 | `SmDecryptInterceptor` 拦截全部 `/api/**` | Given 携带加密 Header 的请求，When 经过拦截器，Then：timestamp 超期→4001；requestId 重复→4003；HMAC 验签失败→4002；正常→`@Encrypted` 字段解密后交给 Controller |
| FR-7 | 前端 `SmEncryptionService` | Given 后端 SM2 公钥，When 调用 `buildEncryptedRequest(data)`，Then 返回含加密 Header 元数据 + 敏感字段已加密的 Body |
| FR-8 | Axios 拦截器全量接入 | Given 任意 API 请求，When 发起，Then 自动添加 5 个加密 Header，Body 中 `SENSITIVE_FIELDS` 配置的字段自动 SM4 加密 |
| FR-9 | SM2 密钥生成工具 | Given 运行密钥生成脚本，Then 输出私钥 Hex（32字节）和公钥 Hex（64字节，无 04 前缀），可直接写入 Nacos |
| FR-10 | 前端 SM2 公钥内存缓存 | Given 页面初始化，When 首次请求，Then 调用 `/api/security/public-key` 获取公钥并缓存至内存；刷新页面重新获取 |

## 5. 非功能需求

- **性能**：SM4 加解密单次 < 1ms；SM2 加密单次 < 10ms；拦截器总增加延迟 < 50ms；符合 Charter P95 < 1s
- **安全**：
  - SM4-CBC + PKCS7Padding，密钥 16字节（GM/T 0002 合规）
  - SM2 使用 C1C3C2 模式（新标准）
  - HMAC-SM3 覆盖 Body JSON + timestamp + requestId，防篡改
  - 防重放：timestamp ±5 分钟窗口 + requestId Redis SETNX（TTL 5分钟）
  - SM2 私钥仅存 Nacos/环境变量，禁止硬编码，禁止通过接口暴露
  - SM2 密钥 90 天停机轮换
- **可用性**：国密库初始化失败时服务启动失败并告警（Fail Fast）
- **可维护性**：SM2/SM4/SM3 工具类各有单元测试覆盖往返一致性

## 6. 数据契约

### 6.1 请求 Header（加密元数据）

| Header | 类型 | 必填 | 说明 |
|--------|------|------|------|
| X-Encrypted-Key | string（Base64） | 是 | SM2 加密后的 SM4 密钥（16字节原文） |
| X-Encrypted-Iv | string（Base64） | 是 | SM2 加密后的 SM4 IV（16字节原文） |
| X-Signature | string（Base64） | 是 | HMAC-SM3(SM4Key, Body+timestamp+requestId) |
| X-Timestamp | string（毫秒） | 是 | 服务端验证 ±5 分钟有效 |
| X-Request-Id | string（UUID v4） | 是 | 防重放唯一标识 |

### 6.2 请求 Body（字段级加密，结构不变）

```json
{
  "username": "张三",
  "age": 25,
  "mobile": "<SM4-CBC密文 Base64>",
  "idCard": "<SM4-CBC密文 Base64>",
  "address": [
    { "city": "北京", "street": "朝阳区" }
  ]
}
```

`@Encrypted` 字段由业务方在 DTO 上标注，`FieldEncryptionUtil` 递归处理，非敏感字段明文传输，JSON 结构不变。

### 6.3 后端配置项

| 配置键 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| security.sm2.private-key | string（Hex） | 是 | SM2 私钥，32字节，Nacos 注入 |
| security.sm2.public-key | string（Hex） | 是 | SM2 公钥，64字节，无 04 前缀 |
| security.encryption.enabled | boolean | 是 | 加密总开关，false 时拦截器放行（仅开发环境） |
| security.encryption.timestamp-tolerance | long（ms） | 是 | 防重放时间窗口，默认 300000（5分钟） |

### 6.4 Redis 防重放键

| 键模式 | TTL | 说明 |
|--------|-----|------|
| `sm:req:id:{requestId}` | 5分钟 | requestId 唯一性校验，SETNX |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/security/public-key | 公开（Gateway 白名单） | 返回 SM2 公钥 Hex 字符串，前端初始化调用 |

## 8. 权限边界

- `/api/security/public-key`：无需认证，加入 Gateway 白名单
- `SmDecryptInterceptor`：拦截全部 `/api/**`，`security.encryption.enabled=false` 时跳过（仅开发/测试环境）
- SM2 私钥：仅 Nacos 配置可读，任何接口禁止返回私钥内容
- `@Encrypted` 标注权：业务方 DTO 自主标注，框架不预设敏感字段列表

## 9. 验收标准

- [ ] `SM2Util`：加密→解密往返一致性单测通过
- [ ] `SM4Util`：加密→解密往返一致性单测通过（16字节密钥）
- [ ] `SM3Util`：HMAC-SM3 相同输入一致、篡改后验证失败单测通过
- [ ] `SmDecryptInterceptor`：timestamp 超期返回 4001
- [ ] `SmDecryptInterceptor`：requestId 重复返回 4003
- [ ] `SmDecryptInterceptor`：HMAC 验签失败返回 4002
- [ ] 集成测试：前端 `buildEncryptedRequest` → 后端拦截器解密 `@Encrypted` 字段一致性验证通过
- [ ] `GET /api/security/public-key` 返回 64字节 Hex SM2 公钥（不含 04 前缀）
- [ ] Axios 拦截器为全部请求自动添加 5 个加密 Header
- [ ] `security.encryption.enabled=false` 时拦截器放行，开发环境可用
- [ ] 密钥生成脚本输出私钥 Hex（32字节）和公钥 Hex（64字节）
- [ ] `npm run specs:validate` 通过

## 10. 未决问题

- [ ] `@Encrypted` 字段前后端不同步风险：前端 `SENSITIVE_FIELDS` 配置与后端 `@Encrypted` 注解由不同人维护，需约定同步机制（文档 or 自动生成？）
- [ ] `security.encryption.enabled=false` 仅允许非生产环境，是否需要环境校验防止误开？

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-27 | 初稿：基于 docs/sm-encryption-scheme.md |
| 0.2.0 | 2026-05-27 | 签名改为 HMAC-SM3；覆盖全部接口；去除兼容模式；停机轮换；内存缓存公钥 |
