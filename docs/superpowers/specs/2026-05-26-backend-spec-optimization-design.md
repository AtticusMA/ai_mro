# 后端 Spec 优化设计文档

**日期**：2026-05-26  
**状态**：已审批  
**作者**：架构讨论产出

---

## 1. 背景

现有 `specs/` 目录的规格文档以前端视角为主，缺乏后端实现层面的规格内容。本次优化目标：

1. 补充完整的后端实现约束（JSON Schema、错误码、Dubbo 接口签名）
2. 反映架构决策变化（BFF + 微服务、Java 21）
3. 新增 8 个 MRO 业务模块的后端接口规格
4. 保持 `specs/` 作为唯一业务真理源

---

## 2. 架构设计

### 2.1 整体分层

```
浏览器 / 移动端 / IoT 设备
  ↓ HTTPS
gateway-service（Spring Cloud Gateway）
  · JWT 验证：HTTP 调 auth-service /internal/auth/verify
  · 路由规则：/manage/** → manage-web
  · 限流 / 日志 / 跨域
  ↓ HTTP（注入用户上下文 Header）
manage-web（厚 BFF，Spring Boot + Java 21）
  内部分层：
  ├── Controller 层：参数校验（@Valid），无权限注解，调 Service
  └── Service 层（编排层）：StructuredTaskScope 并发调多个 Dubbo Service，组装响应
  注：权限判断唯一入口是 Gateway，Controller 不做二次权限校验
  ↓ Dubbo RPC（Nacos 注册发现）+ Attachment 透传用户上下文
11 个微服务（各自独立 Spring Boot，无 Controller）
  基础服务：
  ├── auth-service        认证 / JWT / 权限验证
  ├── system-service      用户/部门/角色/菜单/字典/数据权限
  └── rag-service         RAG 编排（内部 Dubbo，外部 HTTP 调 RAGFlow）
  MRO 业务服务：
  ├── aircraft-health-service     飞机健康与预测性维护
  ├── ar-maintenance-service      AR 智慧维修协作
  ├── fault-diagnosis-service     智能排故助手
  ├── maintenance-manual-service  维修手册管理
  ├── digital-twin-service        数字孪生机库
  ├── tooling-material-service    工具间与航材管理
  ├── vr-ar-training-service      VR/AR 培训系统
  └── paperless-checkin-service   无纸化电子工卡
  ↓
MySQL 8（各服务独立 schema）/ Redis / Nacos / RAGFlow
```

### 2.2 调用规则（铁律）

| 规则 | 说明 |
|------|------|
| Gateway → auth-service | HTTP POST `/internal/auth/verify`，结果 Redis 缓存 |
| Gateway → manage-web | HTTP，注入 `X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions` Header |
| manage-web → service | Dubbo RPC，携带用户上下文 Attachment |
| service → service | **严禁**，无任何例外 |
| auth-service → 任何服务 | **严禁** |
| 数据库访问 | 每个 service 只访问自己的 schema，禁止跨 schema 查询 |

### 2.3 用户上下文传递链

```
JWT Token
  ↓ Gateway 解析
HTTP Header：X-User-Id / X-User-Dept-Id / X-User-Roles / X-User-Permissions
  ↓ manage-web 读取
Dubbo Attachment：userId / deptId / roles / permissions
  ↓ 各 service 从 RpcContext 读取，无需调任何服务
```

### 2.4 Gateway 鉴权流程

```
请求到达 Gateway
  → HTTP POST auth-service /internal/auth/verify（携带 Authorization Header）
  → auth-service 检查：JWT 签名 + Redis 黑名单 + 返回权限码列表
  → Gateway 比对当前路由所需权限码
  → 通过：注入用户上下文 Header，转发 manage-web
  → 失败：直接返回 401/403，不转发
```

---

## 3. Java 21 特性使用规范

### 3.1 Virtual Threads

所有服务统一开启：

```properties
spring.threads.virtual.enabled=true
```

manage-web 厚 BFF 聚合模式（核心用法）：

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var task1 = scope.fork(() -> serviceA.query(id));
    var task2 = scope.fork(() -> serviceB.query(id));
    scope.join().throwIfFailed();
    return new AggregatedVO(task1.get(), task2.get());
}
```

适用场景：manage-web 聚合多个 Dubbo 调用、rag-service 调 RAGFlow HTTP、所有 JDBC IO。

### 3.2 Record 类型

| 使用场景 | 规则 |
|----------|------|
| HTTP 层 DTO（请求/响应） | 全部用 Record |
| Dubbo 传输对象 | Record + `implements Serializable` |
| 领域实体（Entity） | 普通 Class（MyBatis 需要可变对象） |
| 配置类 | `@ConfigurationProperties` + Record |

示例：

```java
// HTTP 请求体
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}

// Dubbo 传输对象
public record UserContextDTO(
    Long userId,
    Long deptId,
    List<String> roles,
    List<String> permissions
) implements Serializable {}
```

### 3.3 Sealed Classes + Pattern Matching

业务异常统一建模：

```java
public sealed interface BizException permits
    AuthException, SystemException, MroException, RagException {}

public record AuthException(AuthErrorCode code, String message)
    implements BizException {}

public record MroException(MroErrorCode code, String message)
    implements BizException {}
```

全局异常处理：

```java
@ExceptionHandler(BizException.class)
public ResponseEntity<ApiResponse<?>> handle(BizException ex) {
    return switch (ex) {
        case AuthException e   -> ResponseEntity.status(401)
            .body(ApiResponse.fail(e.code().code, e.message()));
        case MroException e    -> ResponseEntity.status(400)
            .body(ApiResponse.fail(e.code().code, e.message()));
        case SystemException e -> ResponseEntity.status(400)
            .body(ApiResponse.fail(e.code().code, e.message()));
        case RagException e    -> ResponseEntity.status(502)
            .body(ApiResponse.fail(e.code().code, e.message()));
    };
}
```

---

## 4. 错误码分段规则

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

---

## 5. 统一响应结构

```json
{
  "code": 0,
  "msg": "ok",
  "data": {},
  "timestamp": 1716700800000
}
```

分页响应：

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

分页请求参数统一约定：`pageNum`（从 1 开始）、`pageSize`（默认 20，最大 100）。

---

## 6. 框架版本策略

跟随 Spring Cloud Alibaba 官方版本矩阵，不手动锁定版本号。

关键约束：
- Java 21（LTS）
- Spring Boot 3.x（支持 Virtual Threads）
- Spring Cloud Alibaba 官方推荐配套版本
- Dubbo 3.x（Nacos 注册）
- MyBatis-Plus（ORM）
- Spring AI（RAG 模块向量处理）

---

## 7. Spec 文件改动清单

### 新增 ADR

| 文件 | 内容 |
|------|------|
| `specs/adr/006-bff-microservice-architecture.md` | manage-web 厚 BFF + 11 微服务架构决策 |
| `specs/adr/007-java21-features.md` | Java 21 特性使用规范与边界 |
| `specs/adr/008-gateway-auth-http.md` | Gateway HTTP 调 auth-service 鉴权决策 |

### 重写

| 文件 | 变更内容 |
|------|----------|
| `specs/platform/001-architecture.spec.md` | 重写为 BFF + 11 微服务架构，补充服务依赖规则、用户上下文传递链、Java 21 技术栈 |
| `specs/platform/002-database-schema.spec.md` | 补充各服务独立 schema 规则，新增 MRO 业务表通用字段约定 |

### 追加（基础服务 Spec）

| 文件 | 追加内容 |
|------|----------|
| `specs/auth/001-login-jwt.spec.md` | JWT Payload Schema、Token 传递协议、Redis 黑名单 key 设计、完整 JSON Schema、错误码 4010-4039 |
| `specs/system/001-dept.spec.md` | 完整 Request/Response JSON Schema、分页约定、错误码 4100-4199 |
| `specs/system/002-user.spec.md` | 同上 |
| `specs/system/003-role.spec.md` | 同上 |
| `specs/system/004-menu.spec.md` | 同上 |
| `specs/system/005-dict.spec.md` | 同上 |
| `specs/system/006-data-permission.spec.md` | `@DataScope` 注解完整定义、MyBatis 拦截器注册方式、SQL 改写模板 |

### 追加（MRO 业务 Spec）

| 文件 | 追加内容 |
|------|----------|
| `specs/mro/001-health-monitoring.spec.md` | 完整 JSON Schema、错误码 4200-4299、Dubbo 接口签名 |
| `specs/mro/002-ar-collaboration.spec.md` | 完整 JSON Schema、错误码 4300-4399、Dubbo 接口签名 |
| `specs/mro/003-troubleshooting-assistant.spec.md` | 完整 JSON Schema、错误码 4400-4499、Dubbo 接口签名 |
| `specs/mro/004-manual-management.spec.md` | 完整 JSON Schema、错误码 4500-4599、Dubbo 接口签名 |
| `specs/mro/005-digital-twin-hangar.spec.md` | 完整 JSON Schema、错误码 4600-4699、Dubbo 接口签名 |
| `specs/mro/006-tool-material-management.spec.md` | 完整 JSON Schema、错误码 4700-4799、Dubbo 接口签名 |
| `specs/mro/007-vr-ar-training.spec.md` | 完整 JSON Schema、错误码 4800-4899、Dubbo 接口签名 |
| `specs/mro/008-paperless-workcard.spec.md` | 完整 JSON Schema、错误码 4900-4999、Dubbo 接口签名 |

### 新增平台 Spec

| 文件 | 内容 |
|------|------|
| `specs/platform/003-rag-integration.spec.md` | RAGFlow HTTP 集成规范、知识库管理、错误处理、错误码 5900-5999 |

---

## 8. 关键设计决策摘要

| 决策 | 选择 | 原因 |
|------|------|------|
| 架构模式 | BFF + 微服务 | 多端复用、独立扩容、故障隔离 |
| BFF 模式 | 厚 BFF | Dashboard 等复杂页面需多服务聚合 |
| 网关鉴权 | HTTP 调 auth-service | 支持黑名单、权限集中管理、Gateway 保持轻量 |
| 服务间通信 | Dubbo RPC only | 统一协议、性能优于 HTTP、服务发现集成 |
| 服务互调 | 严禁 | 避免循环依赖、保持服务边界清晰 |
| Java 版本 | Java 21 | Virtual Threads 提升并发、Record/Sealed 提升类型安全 |
| 框架版本 | 跟 SCA 官方矩阵 | 避免版本冲突、官方验证兼容性 |
| RAG 引擎 | RAGFlow（HTTP） | Java 调用外部 RAGFlow，rag-service 做 Dubbo 封装 |
