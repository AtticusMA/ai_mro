# 后端服务开发计划总索引

> **For agentic workers:** 本文档是索引，不可直接执行。按下方顺序依次执行各子计划，每个子计划独立可测试、可部署。

**Goal:** 从零搭建智能机务 MRO 系统后端，覆盖 13 个服务单元（Gateway、auth、system、manage-web BFF、8 个 MRO 微服务、rag-service）。

**Architecture:** 多模块 Maven Mono-repo（`mro-backend/`），Spring Boot 3.x + Spring Cloud Alibaba + Dubbo 3 + Nacos，Java 21 Virtual Threads，manage-web 作厚 BFF，所有业务 HTTP 流量经 Gateway → manage-web → Dubbo 微服务，服务间严禁直调。

**Tech Stack:** Java 21 · Spring Boot 3.3 · Spring Cloud Alibaba 2023.0.x · Dubbo 3.3 · Nacos 2.x · MySQL 8 · Redis · MyBatis-Plus 3.5 · Maven 3.9

---

## 执行顺序（严格按序，存在依赖）

| 计划 | 文件 | 内容 | 前置 |
|------|------|------|------|
| BE-01 | `2026-05-26-be01-project-skeleton.md` | Maven 多模块骨架 + 公共模块 (`mro-common`) | 无 |
| BE-02 | `2026-05-26-be02-gateway-auth.md` | gateway-service + auth-service | BE-01 |
| BE-03 | `2026-05-26-be03-system-service.md` | system-service（部门/用户/角色/菜单/字典/数据权限） | BE-01 |
| BE-04 | `2026-05-26-be04-manage-web-bff.md` | manage-web BFF 骨架 + 系统管理 HTTP 层 | BE-02, BE-03 |
| BE-05 | `2026-05-26-be05-mro-health-ar-tshoot.md` | aircraft-health-service · ar-maintenance-service · fault-diagnosis-service | BE-01 |
| BE-06 | `2026-05-26-be06-mro-manual-twin-tool.md` | maintenance-manual-service · digital-twin-service · tooling-material-service | BE-01 |
| BE-07 | `2026-05-26-be07-mro-training-workcard-rag.md` | vr-ar-training-service · paperless-checkin-service · rag-service | BE-01 |
| BE-08 | `2026-05-26-be08-manage-web-mro-routes.md` | manage-web MRO HTTP 路由层（接入 BE-05/06/07 Dubbo 接口） | BE-04, BE-05, BE-06, BE-07 |

## 目录结构预览

```
mro-backend/                          ← Maven Aggregator（BE-01 创建）
├── pom.xml                           ← 父 POM（统一版本管理）
├── mro-common/                       ← 公共模块（BE-01）
│   ├── mro-common-core/              ← Result/Page/异常/UserContextDTO
│   ├── mro-common-dubbo/             ← 所有 Dubbo 接口定义 + Record DTO
│   └── mro-common-data/              ← MyBatis-Plus 配置/数据权限拦截器
├── gateway-service/                  ← BE-02
├── auth-service/                     ← BE-02
├── system-service/                   ← BE-03
├── manage-web/                       ← BE-04（骨架）/ BE-08（MRO 路由）
├── aircraft-health-service/          ← BE-05
├── ar-maintenance-service/           ← BE-05
├── fault-diagnosis-service/          ← BE-05
├── maintenance-manual-service/       ← BE-06
├── digital-twin-service/             ← BE-06
├── tooling-material-service/         ← BE-06
├── vr-ar-training-service/           ← BE-07
├── paperless-checkin-service/        ← BE-07
└── rag-service/                      ← BE-07
```

## 关键约定（所有计划共用）

- **错误码段**：见 `specs/platform/001-architecture.spec.md §10`
- **统一响应**：`Result<T> { code, msg, data, timestamp }`
- **分页**：`PageParam { pageNum≥1, pageSize default 20 max 100 }` / `PageResult<T> { list, total, pageNum, pageSize }`
- **Dubbo 分组/版本**：`group=mro, version=1.0.0`
- **用户上下文传递**：JWT → Gateway → HTTP Header → manage-web → Dubbo Attachment → service `RpcContext.getServerAttachment()`
- **Virtual Threads**：每个服务 `spring.threads.virtual.enabled=true`
- **Schema 隔离**：每服务独立 schema，见 `specs/platform/002-database-schema.spec.md §3`
- **Refs 规范**：commit message 必须含 `Refs: <SpecId>`

## Nacos 本地开发配置

```yaml
# 所有服务共用，本地开发环境
spring:
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        namespace: dev
      config:
        namespace: dev
        file-extension: yaml
```

Nacos 端口 8848，本地 `docker compose up nacos mysql redis` 启动基础设施。

## 测试策略（所有计划共用）

- **单元测试**：JUnit 5 + Mockito，覆盖 Service 层业务逻辑，不启动 Spring 上下文
- **集成测试**：`@SpringBootTest` + `@Sql` 初始化测试数据，覆盖 Mapper/Repository
- **契约测试**：`MockMvc` 测试 Controller，不依赖真实 Dubbo
- **命名**：测试类 `XxxTest`，方法 `test_方法名_场景_期望结果`
