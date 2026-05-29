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