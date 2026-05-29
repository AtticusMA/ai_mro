---
id: PLAT-001
title: 系统架构
domain: platform
status: approved
owner: '@arch'
version: 1.3.0
created: 2026-05-23
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 系统架构

## 1. 背景与目标

定义智能机务系统（MRO）的完整服务划分、通信模式、鉴权链路、Java 21 技术底座，
作为所有业务 Spec 的技术基础。见 ADR-006、ADR-007、ADR-008。

## 2. 范围

### In Scope
- 服务清单与职责划分（13 个部署单元）。
- Gateway 鉴权流程（HTTP 调 auth-service）。
- manage-web 厚 BFF 内部分层规范。
- 用户上下文传递链（JWT → Header → Dubbo Attachment）。
- Java 21 技术底座（Virtual Threads / Record / Sealed Classes）。
- 统一响应格式与错误码分段。
- 前端栈：Vue 3 + Vite + Element Plus + Tailwind + Mock-first（ADR-003）。

### Out of Scope
- 容器化与发布流水线（独立 Spec）。
- 多端 BFF（user-web / iot-web）——Phase 2。
- IoT 接入层（EMQX / Kafka）——见 ADR-005。

## 3. 服务清单

| 服务 | 类型 | 端口（默认） | 职责 |
|------|------|-------------|------|
| gateway-service | 网关 | 8080 | 路由、JWT 鉴权、限流、跨域 |
| manage-web | 厚 BFF | 8081 | 管理后台 REST 接口 + Dubbo 编排 |
| auth-service | 基础微服务 | 20880 | 认证、JWT、权限验证 |
| system-service | 基础微服务 | 20881 | 用户/部门/角色/菜单/字典/数据权限 |
| rag-service | 基础微服务 | 20882 | RAG 编排（HTTP 调 RAGFlow） |
| aircraft-health-service | MRO 微服务 | 20883 | 飞机健康与预测性维护 |
| ar-maintenance-service | MRO 微服务 | 20884 | AR 智慧维修协作 |
| fault-diagnosis-service | MRO 微服务 | 20885 | 智能排故助手 |
| maintenance-manual-service | MRO 微服务 | 20886 | 维修手册管理 |
| digital-twin-service | MRO 微服务 | 20887 | 数字孪生机库 |
| tooling-material-service | MRO 微服务 | 20888 | 工具间与航材管理 |
| vr-ar-training-service | MRO 微服务 | 20889 | VR/AR 培训系统 |
| paperless-checkin-service | MRO 微服务 | 20890 | 无纸化电子工卡 |

## 4. 调用规则（铁律）

| 规则 | 说明 |
|------|------|
| Gateway → auth-service | HTTP POST `/internal/auth/verify`，结果 Redis 缓存 |
| Gateway → manage-web | HTTP，注入用户上下文 Header |
| manage-web → service | Dubbo RPC + Attachment 透传用户上下文 |
| service → service | **严禁**，无任何例外 |
| 数据库访问 | 每个 service 独立 schema，禁止跨 schema 查询 |

## 5. Gateway 鉴权流程

```
请求到达 Gateway
  → HTTP POST auth-service /internal/auth/verify
  → 验证：JWT 签名 + 过期时间 + Redis 黑名单 + 权限码
  → 通过：注入 Header 转发 manage-web
      X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions
  → 失败：返回 401/403，不转发
```

详见 ADR-008。

## 6. manage-web 内部分层

### 6.1 分层规范

```
Controller 层  (com.mro.web.module.{模块}.controller)
  · 参数校验（@Valid / @Validated）
  · 无权限注解（权限判断在 Gateway）
  · 只调 AppService，不写业务逻辑，不持有 @DubboReference
  · 方法体内只允许一次 AppService 调用

AppService 层  (com.mro.web.module.{模块}.app)
  · 命名规范：{业务名}AppService，如 UserAppService、WorkOrderAppService
  · 持有全部 @DubboReference，是唯一调用 Dubbo 的位置
  · @DataScope 注解打在 AppService 方法上（数据权限在编排层生效）
  · StructuredTaskScope 并发调多个 Dubbo Service
  · 组装/裁剪响应数据
  · 不直接操作数据库，不直接调 RAGFlow
```

### 6.2 包结构规范

```
com.mro.web
├── ManageWebApplication.java
├── annotation/          # 平台级注解（@DataScope 等）
├── aspect/              # 平台级切面（DataScopeAspect 等）
├── config/              # 平台级配置（WebConfig、EncryptionProperties 等）
├── context/             # 用户上下文（UserContext、UserContextFilter）
├── interceptor/         # 平台级拦截器（SmDecryptInterceptor、CachedBodyFilter 等）
└── module/
    ├── security/
    │   └── controller/  # SecurityController（公钥接口，无需 AppService）
    ├── sys/
    │   ├── controller/  # User/Dept/Dict/Menu/RoleController
    │   └── app/         # User/Dept/Dict/Menu/Role/SystemAppService
    └── {业务模块}/      # 新增业务模块按此结构扩展
        ├── controller/
        └── app/
```

**规则：**
- 平台级（跨模块复用、框架集成）代码放 `com.mro.web` 根下对应子包，**不按模块划分**。
- 业务功能代码统一放 `com.mro.web.module.{模块名}` 下，**禁止**直接放 `com.mro.web.controller` 或 `com.mro.web.app`。
- 新增业务模块在 `module/` 下新建目录，无需改动平台层。

### 6.3 命名区分

| 层 | 位置 | 示例 |
|----|------|------|
| BFF 编排层 | manage-web `com.mro.web.module.sys.app` | `UserAppService` |
| 微服务内部 | system-service `com.mro.system.service` | `UserService` |

同名业务概念（如 User）通过包路径和后缀明确区分，避免歧义。

## 7. common-dubbo 模块包结构规范

### 7.1 职责

`common-dubbo` 是 Dubbo 接口契约模块，定义所有微服务对外暴露的 Service 接口、入参与出参。manage-web 通过 `@DubboReference` 引用此模块中的接口。

### 7.2 包结构

```
com.mro.dubbo
├── common/                          # 跨域共享基础类
│   ├── service/                     # 公共 Service 接口
│   ├── request/                     # 公共入参（PageParam 等）
│   └── response/                    # 公共出参（PageResult、UserContextDTO 等）
├── auth/                            # 认证鉴权（AUTH-001）
│   ├── service/
│   ├── request/
│   └── response/
├── system/                          # 系统管理（SYS-001~008）
│   ├── service/
│   ├── request/
│   └── response/
├── health/                          # 飞机健康监控（MRO-001）
│   ├── service/
│   ├── request/
│   └── response/
├── ar/                              # AR 智慧维修协作（MRO-002）
│   ├── service/
│   ├── request/
│   └── response/
├── tshoot/                          # 智能排故助手（MRO-003）
│   ├── service/
│   ├── request/
│   └── response/
├── manual/                          # 维修手册管理（MRO-004）
│   ├── service/
│   ├── request/
│   └── response/
├── dtwin/                           # 数字孪生机库（MRO-005）
│   ├── service/
│   ├── request/
│   └── response/
├── tool/                            # 工具管理（MRO-006）
│   ├── service/
│   ├── request/
│   └── response/
├── material/                        # 航材管理（MRO-006）
│   ├── service/
│   ├── request/
│   └── response/
├── training/                        # VR/AR 培训（MRO-007）
│   ├── service/
│   ├── request/
│   └── response/
└── workcard/                        # 无纸化电子工卡（MRO-008）
    ├── service/
    ├── request/
    └── response/
```

### 7.3 命名规范

| 子包 | 类后缀 | 说明 |
|------|--------|------|
| `service/` | `*DubboService` | Dubbo 接口定义 |
| `request/` | `*Command` / `*Param` / `*QueryParam` | 写操作入参 / 查询入参 |
| `response/` | `*DTO` | 服务返回数据 |

### 7.4 业务包名映射

| 微服务 | 包名 | Spec |
|--------|------|------|
| auth-service | `auth` | AUTH-001 |
| system-service | `system` | SYS-001~008 |
| aircraft-health-service | `health` | MRO-001 |
| ar-maintenance-service | `ar` | MRO-002 |
| fault-diagnosis-service | `tshoot` | MRO-003 |
| maintenance-manual-service | `manual` | MRO-004 |
| digital-twin-service | `dtwin` | MRO-005 |
| tooling-material-service | `tool` / `material` | MRO-006 |
| vr-ar-training-service | `training` | MRO-007 |
| paperless-checkin-service | `workcard` | MRO-008 |

### 7.5 规则

- 包名与业务域一致，禁止使用有歧义的缩写。
- 每个业务域包含且仅包含 `service/`、`request/`、`response/` 三个子包。
- 接口和类均为 `public record ... implements Serializable`（Java 21 Record）。
- 跨域复用的基础类放 `common/` 包，业务类严禁跨包引用其他业务域的 request/response。

## 8. 用户上下文传递链

```
JWT Token
  → Gateway 解析 + auth-service 验证
  → HTTP Header：X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions
  → manage-web Service 读取 Header，构建 UserContextDTO
  → Dubbo Attachment（manage-web → service）
  → service 从 RpcContext.getServerAttachment() 读取，无需调任何服务
```

UserContextDTO（Dubbo 传输对象）：

```java
public record UserContextDTO(
    Long userId,
    Long deptId,
    List<String> roles,
    List<String> permissions
) implements Serializable {}
```

## 9. Java 21 技术底座

详见 ADR-007。核心要点：

- `spring.threads.virtual.enabled=true`（所有服务）
- HTTP DTO 用 Record，Entity 用普通 Class
- 业务异常用 Sealed Interface + Pattern Matching switch
- manage-web 聚合用 `StructuredTaskScope.ShutdownOnFailure`

## 10. 统一响应格式

```json
{
  "code": 0,
  "msg": "ok",
  "data": {},
  "timestamp": 1716700800000
}
```

分页响应 data 结构：

```json
{
  "list": [],
  "total": 100,
  "pageNum": 1,
  "pageSize": 20
}
```

分页请求参数：`pageNum`（从 1 开始）、`pageSize`（默认 20，最大 100）。

## 11. 错误码分段

| 段 | 范围 | 归属 |
|----|------|------|
| 0 | 成功 | 全局 |
| 4010-4039 | 认证 + 权限错误 | auth-service |
| 4100-4199 | system 业务错误 | system-service |
| 4200-4299 | 飞机健康与预测性维护 | aircraft-health-service |
| 4300-4399 | AR 智慧维修协作 | ar-maintenance-service |
| 4400-4499 | 智能排故助手 | fault-diagnosis-service |
| 4500-4599 | 维修手册管理 | maintenance-manual-service |
| 4600-4699 | 数字孪生机库 | digital-twin-service |
| 4700-4799 | 工具间与航材管理 | tooling-material-service |
| 4800-4899 | VR/AR 培训系统 | vr-ar-training-service |
| 4900-4999 | 无纸化电子工卡 | paperless-checkin-service |
| 5000-5899 | 系统内部错误 | 全局 |
| 5900-5999 | RAG 异常 | rag-service |

## 12. 非功能需求

- Gateway P95 < 200ms（含 auth-service 缓存命中验证）。
- 单 service P95 < 1s。
- auth-service 高可用：≥ 2 实例，Gateway 鉴权链路的关键路径。
- 任一 MRO 服务故障不影响其他服务。

## 13. 技术栈

| 组件 | 选型 |
|------|------|
| JDK | Java 21（LTS） |
| Web 框架 | Spring Boot 3.x（跟 SCA 官方版本矩阵） |
| 微服务 | Spring Cloud Alibaba（官方版本矩阵） |
| RPC | Dubbo 3.x |
| 注册/配置 | Nacos 2.x |
| 缓存 | Redis |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8 |
| RAG 引擎 | RAGFlow（HTTP 调用） |
| AI | Spring AI（向量处理） |
| 网关 | Spring Cloud Gateway |
| 前端 | Vue 3 + Vite + Element Plus + Tailwind |

## 14. 验收标准

- [ ] 所有 service 通过 Nacos 注册，manage-web Dubbo 调用成功。
- [ ] Gateway 鉴权：无 Token 返回 401，Token 过期返回 401，无权限返回 403。
- [ ] 登出后 Token 加入黑名单，再次请求返回 401。
- [ ] manage-web Controller 无任何权限注解（权限统一在 Gateway）。
- [ ] service 之间无任何直接调用（代码审查 + 架构门禁）。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿 |
| 1.0.0 | 2026-05-26 | 重写：BFF+微服务架构，Java 21，Gateway 鉴权，11 微服务，见 ADR-006/007/008 |
| 1.1.0 | 2026-05-27 | 细化 manage-web 分层：新增 AppService 层规范，Controller 禁止持有 DubboReference，@DataScope 下沉到 AppService |
| 1.2.0 | 2026-05-27 | manage-web 包结构重组：平台级代码留根包子目录，业务代码统一迁移至 com.mro.web.module.{模块} |
| 1.3.0 | 2026-05-29 | 新增 common-dubbo 模块包结构规范（Section 7）：按业务域分包，每域下 service/request/response 三目录 |
