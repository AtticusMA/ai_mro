# Plan 1：架构决策记录 + 平台 Spec 重写

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增 3 个 ADR 记录架构决策，重写 PLAT-001/002 反映 BFF + 11 微服务真实架构。

**Architecture:** Gateway（HTTP 调 auth-service 鉴权）→ manage-web（厚 BFF，Controller + Service 编排层）→ 11 个独立微服务（Dubbo RPC，无 Controller）。服务间严禁互调，权限判断唯一入口为 Gateway。

**Tech Stack:** Java 21、Spring Boot 3.x、Spring Cloud Alibaba（跟官方版本矩阵）、Dubbo 3.x、Nacos、Redis、MySQL 8、RAGFlow（HTTP）。

---

## 文件清单

| 操作 | 文件路径 |
|------|----------|
| 新增 | `specs/adr/006-bff-microservice-architecture.md` |
| 新增 | `specs/adr/007-java21-features.md` |
| 新增 | `specs/adr/008-gateway-auth-http.md` |
| 重写 | `specs/platform/001-architecture.spec.md` |
| 重写 | `specs/platform/002-database-schema.spec.md` |

---

## Task 1：新增 ADR-006（BFF + 微服务架构决策）

**Files:**
- Create: `specs/adr/006-bff-microservice-architecture.md`

- [ ] **Step 1：创建 ADR-006 文件**

写入以下完整内容：

```markdown
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
```

- [ ] **Step 2：确认文件已创建**

```bash
cat specs/adr/006-bff-microservice-architecture.md | head -5
```

期望输出：`id: ADR-006` 开头的 front-matter。

---

## Task 2：新增 ADR-007（Java 21 特性使用规范）

**Files:**
- Create: `specs/adr/007-java21-features.md`

- [ ] **Step 1：创建 ADR-007 文件**

写入以下完整内容：

```markdown
---
id: ADR-007
title: Java 21 特性使用规范 — Virtual Threads / Record / Sealed Classes
status: accepted
date: 2026-05-26
deciders: ['@arch']
---

# ADR-007: Java 21 特性使用规范

## Context

项目采用 Java 21（LTS），Spring Boot 3.2+ 原生支持 Virtual Threads。
需要统一规范 Java 21 新特性的使用边界，避免滥用或不一致。

## Decision

### 1. Virtual Threads

所有服务统一开启：

```properties
spring.threads.virtual.enabled=true
```

**适用场景：**

| 场景 | 说明 |
|------|------|
| manage-web 聚合多 Dubbo 调用 | 使用 StructuredTaskScope 并发调用，等全部返回再组装 |
| rag-service 调 RAGFlow | HTTP 阻塞调用不占用平台线程 |
| 所有 JDBC IO | 自动受益，无需改代码 |

**manage-web 聚合标准写法：**

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var taskA = scope.fork(() -> serviceA.query(param));
    var taskB = scope.fork(() -> serviceB.query(param));
    scope.join().throwIfFailed();
    return new AggregatedVO(taskA.get(), taskB.get());
}
```

**禁止：** 在 Virtual Thread 中使用 `synchronized` 块（改用 `ReentrantLock`）。

### 2. Record 类型

| 使用场景 | 规则 |
|----------|------|
| HTTP 请求体 / 响应体 DTO | 全部用 Record |
| Dubbo RPC 传输对象 | Record + `implements Serializable` |
| 领域实体（Entity） | 普通 Class（MyBatis 需要无参构造 + setter） |
| `@ConfigurationProperties` | Record（Spring Boot 3.x 支持） |

**标准写法：**

```java
// HTTP DTO
public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank @Size(min = 6, max = 20) String password,
    @NotNull Long deptId
) {}

// Dubbo 传输对象
public record UserContextDTO(
    Long userId,
    Long deptId,
    List<String> roles,
    List<String> permissions
) implements Serializable {}
```

### 3. Sealed Classes + Pattern Matching

用于业务异常的类型安全建模，禁止使用 `instanceof` 链。

**异常体系：**

```java
// 顶层密封接口
public sealed interface BizException permits
    AuthException, SystemException, MroException, RagException {}

// 各模块异常（Record 实现）
public record AuthException(AuthErrorCode code, String message)
    implements BizException {}
public record SystemException(SystemErrorCode code, String message)
    implements BizException {}
public record MroException(MroErrorCode code, String message)
    implements BizException {}
public record RagException(int code, String message)
    implements BizException {}
```

**全局异常处理（manage-web GlobalExceptionHandler）：**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<?>> handle(BizException ex) {
        return switch (ex) {
            case AuthException e -> ResponseEntity.status(401)
                .body(ApiResponse.fail(e.code().code, e.message()));
            case SystemException e -> ResponseEntity.status(400)
                .body(ApiResponse.fail(e.code().code, e.message()));
            case MroException e -> ResponseEntity.status(400)
                .body(ApiResponse.fail(e.code().code, e.message()));
            case RagException e -> ResponseEntity.status(502)
                .body(ApiResponse.fail(e.code(), e.message()));
        };
    }
}
```

### 错误码分段

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

## Consequences

### 正面
- Virtual Threads 提升 manage-web BFF 并发聚合吞吐
- Record 消除 Lombok 依赖，类型更安全
- Sealed Classes 使异常处理穷举，编译期发现遗漏

### 负面
- Virtual Threads 与部分旧版连接池（如 HikariCP < 5.1）有兼容性问题，需确认版本
- Record 不能被继承，领域对象层级设计需调整
- Sealed Classes 新增业务域时需同步修改顶层接口

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| 继续用 Lombok | 编译期注解处理复杂，Record 更简洁且 IDE 原生支持 |
| 传统线程池 | Virtual Threads 在 IO 密集型 BFF 场景吞吐量提升显著 |
| 普通 Exception 继承树 | 无法穷举检查，switch 表达式无法利用 |

## References

- 关联 ADR: ADR-006（BFF 微服务架构）
- Spring Boot Virtual Threads: https://docs.spring.io/spring-boot/reference/features/spring-application.html
```

- [ ] **Step 2：确认文件已创建**

```bash
cat specs/adr/007-java21-features.md | head -5
```

期望输出：`id: ADR-007` 开头的 front-matter。

---

## Task 3：新增 ADR-008（Gateway HTTP 调 auth-service 鉴权）

**Files:**
- Create: `specs/adr/008-gateway-auth-http.md`

- [ ] **Step 1：创建 ADR-008 文件**

写入以下完整内容：

```markdown
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

Response 200:
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
```

- [ ] **Step 2：确认文件已创建**

```bash
cat specs/adr/008-gateway-auth-http.md | head -5
```

期望输出：`id: ADR-008` 开头的 front-matter。

---

## Task 4：重写 PLAT-001（系统架构 Spec）

**Files:**
- Modify: `specs/platform/001-architecture.spec.md`

- [ ] **Step 1：完整替换 PLAT-001 内容**

用以下内容替换 `specs/platform/001-architecture.spec.md` 全文：

```markdown
---
id: PLAT-001
title: 系统架构
domain: platform
status: approved
owner: '@arch'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
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

```
Controller 层
  · 参数校验（@Valid / @Validated）
  · 无权限注解（权限判断在 Gateway）
  · 只调 Service，不写业务逻辑，不直接调 Dubbo

Service 层（编排层）
  · StructuredTaskScope 并发调多个 Dubbo Service
  · 组装/裁剪响应数据
  · 不直接操作数据库，不直接调 RAGFlow
```

## 7. 用户上下文传递链

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

## 8. Java 21 技术底座

详见 ADR-007。核心要点：

- `spring.threads.virtual.enabled=true`（所有服务）
- HTTP DTO 用 Record，Entity 用普通 Class
- 业务异常用 Sealed Interface + Pattern Matching switch
- manage-web 聚合用 `StructuredTaskScope.ShutdownOnFailure`

## 9. 统一响应格式

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

## 10. 错误码分段

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

## 11. 非功能需求

- Gateway P95 < 200ms（含 auth-service 缓存命中验证）。
- 单 service P95 < 1s。
- auth-service 高可用：≥ 2 实例，Gateway 鉴权链路的关键路径。
- 任一 MRO 服务故障不影响其他服务。

## 12. 技术栈

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

## 13. 验收标准

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
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/platform/001-architecture.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 5：重写 PLAT-002（数据库设计 Spec）

**Files:**
- Modify: `specs/platform/002-database-schema.spec.md`

- [ ] **Step 1：完整替换 PLAT-002 内容**

用以下内容替换 `specs/platform/002-database-schema.spec.md` 全文：

```markdown
---
id: PLAT-002
title: 数据库设计
domain: platform
status: approved
owner: '@arch'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: [AUTH-001, SYS-001, SYS-002, SYS-003, SYS-004, SYS-005, SYS-006]
---

# Spec: 数据库设计

## 1. 背景与目标

汇总核心表的 DDL 策略；各业务 Spec 引用本文以避免字段定义漂移。
每个微服务拥有独立 MySQL schema，禁止跨 schema 查询。

## 2. 范围

### In Scope
- 基础服务表：`sys_user / sys_dept / sys_role / sys_user_role / sys_role_menu / sys_role_dept / sys_menu / sys_dict / sys_user_contact`。
- 数据权限通用字段约定。
- MRO 业务表通用字段约定。
- 各服务 schema 命名规范。

### Out of Scope
- 具体 MRO 业务表 DDL（各业务 Spec 定义）。
- 时序数据库（InfluxDB）、向量库设计（见 ADR-004）。

## 3. Schema 命名规范

| 服务 | Schema 名 |
|------|-----------|
| auth-service | `mro_auth` |
| system-service | `mro_system` |
| rag-service | `mro_rag` |
| aircraft-health-service | `mro_aircraft_health` |
| ar-maintenance-service | `mro_ar_maintenance` |
| fault-diagnosis-service | `mro_fault_diagnosis` |
| maintenance-manual-service | `mro_maintenance_manual` |
| digital-twin-service | `mro_digital_twin` |
| tooling-material-service | `mro_tooling_material` |
| vr-ar-training-service | `mro_vr_ar_training` |
| paperless-checkin-service | `mro_paperless_checkin` |

**铁律：** 每个 service 的 DataSource 只配置自己的 schema，禁止配置其他 schema。

## 4. 基础服务表（mro_system schema）

### sys_user

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| username | varchar(64) | UNIQUE NOT NULL | 登录名 |
| password | varchar(100) | NOT NULL | BCrypt 加密（ADR-001） |
| employee_no | varchar(64) | UNIQUE | 工号 |
| real_name | varchar(64) | NOT NULL | 真实姓名 |
| dept_id | bigint | NOT NULL | 所属部门 |
| phone | varchar(20) | UNIQUE | |
| email | varchar(100) | | |
| avatar | varchar(255) | | |
| status | tinyint | NOT NULL DEFAULT 1 | 0 禁用 / 1 启用 |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | 软删除 |
| create_user_id | bigint | NOT NULL | |
| create_dept_id | bigint | NOT NULL | |
| create_time | datetime | NOT NULL | |
| update_user_id | bigint | | |
| update_time | datetime | | |

索引：`username` 唯一、`phone` 唯一、`employee_no` 唯一、`dept_id`、`status`。

### sys_dept

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| dept_name | varchar(64) | NOT NULL | |
| dept_code | varchar(64) | UNIQUE NOT NULL | |
| parent_id | bigint | NOT NULL DEFAULT 0 | 顶级为 0 |
| ancestors | varchar(512) | NOT NULL | 祖级 ID 逗号分隔 |
| order_num | int | NOT NULL DEFAULT 0 | |
| leader | varchar(64) | | |
| phone | varchar(20) | | |
| email | varchar(100) | | |
| status | tinyint | NOT NULL DEFAULT 1 | 0 禁用 / 1 启用 |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

索引：`parent_id`、`dept_code` 唯一。

### sys_role

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| role_name | varchar(64) | NOT NULL | |
| role_key | varchar(100) | UNIQUE NOT NULL | 权限字符串，超管为 `*:*:*` |
| data_scope | tinyint | NOT NULL DEFAULT 1 | 1全部/2本部门/3本部门及子部门/4本人/5自定义 |
| order_num | int | NOT NULL DEFAULT 0 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

### sys_user_role

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| user_id | bigint | NOT NULL |
| role_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合唯一：`(user_id, role_id)`。

### sys_role_menu

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| role_id | bigint | NOT NULL |
| menu_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合唯一：`(role_id, menu_id)`。

### sys_role_dept

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| role_id | bigint | NOT NULL |
| dept_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合索引：`(role_id, dept_id)`。

### sys_menu

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK | |
| menu_name | varchar(64) | NOT NULL | |
| parent_id | bigint | NOT NULL DEFAULT 0 | |
| menu_type | char(1) | NOT NULL | M目录/C菜单/F按钮 |
| path | varchar(255) | | 路由地址 |
| component | varchar(255) | | 组件路径 |
| perms | varchar(100) | | 权限码，如 `dept:list` |
| icon | varchar(100) | | |
| order_num | int | NOT NULL DEFAULT 0 | |
| visible | tinyint | NOT NULL DEFAULT 1 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

索引：`parent_id`、`perms`。

### sys_dict

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK | |
| dict_group | varchar(64) | NOT NULL | 字典分组，如 `gender` |
| dict_code | varchar(64) | NOT NULL | 字典码，如 `male` |
| dict_label | varchar(100) | NOT NULL | 显示名 |
| dict_value | varchar(100) | NOT NULL | 存储值 |
| order_num | int | NOT NULL DEFAULT 0 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

联合唯一：`(dict_group, dict_code)`。见 ADR-002（单层平铺）。

## 5. 数据权限通用字段

所有需要数据权限过滤的业务表必须包含以下字段（见 SYS-006）：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| create_user_id | bigint | NOT NULL | 创建人 ID |
| create_dept_id | bigint | NOT NULL | 创建人所属部门 ID |
| use_for_dept_id | bigint | NULL | 业务归属部门 ID（可与创建部门不同） |
| create_time | datetime | NOT NULL | |
| update_user_id | bigint | NULL | |
| update_time | datetime | NULL | |

## 6. MRO 业务表通用字段

8 个 MRO 业务 schema 中的业务主表，除数据权限字段外还需包含：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | 软删除（0否/1是） |
| version | int | NOT NULL DEFAULT 0 | 乐观锁（MyBatis-Plus @Version） |
| remark | varchar(500) | NULL | 备注 |

## 7. 软删除与状态字段规范

| 字段 | 用途 | 规范 |
|------|------|------|
| `is_deleted` | 软删除 | 0=未删除，1=已删除；所有表必须有；MyBatis-Plus `@TableLogic` |
| `status` | 业务状态 | 0=禁用，1=启用；仅有状态概念的表才有 |

两者明确分离：`is_deleted` 控制可见性，`status` 控制可用性。

## 8. 验收标准

- [ ] 所有 service 的 DataSource 配置仅指向自己的 schema，无跨库配置。
- [ ] 所有业务主表包含数据权限通用字段（§5）。
- [ ] 软删除字段 `is_deleted` 与状态字段 `status` 命名一致，不混用。
- [ ] 联合唯一索引在并发插入下生效（测试验证）。

## 9. 未决问题

- [ ] 是否引入分库分表？首期单库单 schema。
- [ ] MRO 时序数据（传感器）的 InfluxDB schema 设计由各 MRO Spec 定义。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿 |
| 1.0.0 | 2026-05-26 | 重写：独立 schema 规范，MRO 通用字段，软删除规范，见 ADR-006 |
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/platform/002-database-schema.spec.md
```

期望输出：`version: 1.0.0`

---

## 自检结果

**Spec 覆盖：**
- ADR-006：BFF 微服务架构决策 ✅ Task 1
- ADR-007：Java 21 特性规范 ✅ Task 2
- ADR-008：Gateway 鉴权策略 ✅ Task 3
- PLAT-001 重写 ✅ Task 4
- PLAT-002 重写 ✅ Task 5

**Placeholder 扫描：** 无 TBD / TODO。

**一致性检查：**
- 错误码分段：ADR-007 与 PLAT-001 §10 一致。
- 调用规则：ADR-006、ADR-008、PLAT-001 §4 三处描述一致。
- UserContextDTO 字段：ADR-006、PLAT-001 §7 一致。
- schema 命名：PLAT-002 §3 与服务清单 PLAT-001 §3 一一对应。
