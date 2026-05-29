---
id: SYS-009
title: Log4j2 应用运行日志配置规范
domain: system
status: approved
owner: '@arch'
version: 0.1.0
created: 2026-05-28
updated: 2026-05-28
charter: CHARTER.md
supersedes: []
depends-on: [PLAT-001]
---

# Spec: Log4j2 应用运行日志配置规范

## 1. 背景与目标

各微服务在开发和运维过程中需要统一、可维护的日志输出规范。当前各服务缺乏统一的日志框架配置，
导致日志格式不一致、无法按级别分文件归档、SQL 调试日志与业务日志混杂、运维无法快速定位问题。

本 Spec 定义所有服务（`gateway-service`、`manage-web`、`auth-service`、`system-service` 及所有 MRO 微服务）
的 Log4j2 配置规范，包括：框架选型、日志级别策略、分级滚动文件规则、MDC 格式、Mapper 层异步 Logger，
作为 SYS-007 操作日志全链路追踪（requestId / MDC）的平台基础。

对齐 Charter 目标：系统可运维性、安全审计能力、P95 < 1s 性能约束下的非侵入式日志采集。

## 2. 范围

### In Scope

- 所有服务统一使用 Log4j2 作为日志实现，SLF4J 作为门面。
- 日志输出格式规范（含 MDC requestId 占位符）。
- 四级分文件滚动输出规范（debug / info / warn / error）。
- 控制台输出规范。
- Mapper 层（MyBatis-Plus）SQL 日志异步 Logger 配置。
- 各服务日志目录路径约定。
- Spring / MyBatis 框架噪声日志过滤规则。
- Log4j2 配置热重载（`monitorInterval`）。
- Java 21 Virtual Threads 环境下 MDC 兼容性要求。

### Out of Scope

- 日志采集到 ELK / 集中式日志平台（后续单独 Spec）。
- 日志脱敏（后续按需 ADR）。
- 登录/登出日志、操作审计日志（属于 SYS-007）。
- 告警规则与监控大盘配置。
- 容器化环境下的日志卷挂载（属于部署 Spec）。

## 3. 用户故事 / 使用场景

- 作为**运维人员**，我希望各服务的日志按级别分文件存储到统一路径，以便通过脚本快速采集 error.log 进行告警。
- 作为**开发人员**，我希望在 debug.log 中能看到完整的 SQL 语句和参数，以便排查数据层问题，同时不污染 info.log。
- 作为**开发人员**，我希望每行日志都包含 requestId（来自 MDC），以便跨日志文件关联同一次请求的完整链路（配合 SYS-007）。
- 作为**架构师**，我希望日志配置支持运行时热重载，以便在不重启服务的情况下调整日志级别排查线上问题。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 统一日志框架 | Given 任意服务启动，When 输出日志，Then 使用 Log4j2 实现 + SLF4J 门面；`pom.xml` 中排除 spring-boot-starter-logging，引入 `spring-boot-starter-log4j2` |
| FR-2 | 配置热重载 | Given 服务运行中，When 修改 `log4j2.xml` 配置文件，Then 30 秒内自动生效，无需重启（`monitorInterval="30"`） |
| FR-3 | 统一日志格式 | Given 任意日志输出，When 写入控制台或文件，Then 格式为 `%d{yyyy-MM-dd HH:mm:ss:SSS} \| %p \| [%X{requestId}] \| - %l - %m%n`，时间精确到毫秒，requestId 来自 MDC |
| FR-4 | 四级分文件滚动 | Given 服务运行产生日志，When 按级别过滤，Then 输出到四个独立 RollingFile：debug.log（DEBUG+）、info.log（INFO+）、warn.log（WARN+）、error.log（ERROR+），每文件最大 200MB，按天滚动归档到 `$${date:yyyy-MM}/` 子目录 |
| FR-5 | 滚动文件保留策略 | Given 日志按天滚动归档，When 达到最大保留数，Then debug.log 最多保留 30 个归档文件，warn.log 最多保留 20 个，info.log 和 error.log 使用 DefaultRolloverStrategy 默认值（7 个） |
| FR-6 | Mapper 层异步 Logger | Given 服务包含 MyBatis-Plus Mapper，When 执行 SQL，Then 通过 `AsyncLogger` 以 TRACE 级别采集 SQL 日志，`additivity="false"` 避免向 Root Logger 传播，仅输出到 Console 和 RollingFileDebug |
| FR-7 | 框架噪声过滤 | Given 服务依赖 Spring / MyBatis 框架，When 框架输出 DEBUG 日志，Then `org.springframework` 包日志级别强制设为 INFO，避免框架噪声写入 debug.log |
| FR-8 | 日志目录约定 | Given 服务部署到目标机器，When 写入日志文件，Then 日志目录为 `/opt/logs/mro-{服务名}/`，归档子目录为 `/opt/logs/mro-{服务名}/$${date:yyyy-MM}/`，归档文件命名为 `mro-{服务名}-{级别}-%d{yyyy-MM-dd}-%i.log` |
| FR-9 | MDC requestId 集成 | Given SYS-007 在 MDC 中写入 requestId，When Log4j2 输出任意日志行，Then 通过 `%X{requestId}` 自动携带，MDC 中无 requestId 时输出空字符串而非报错 |
| FR-10 | Virtual Threads MDC 兼容 | Given 服务启用 Java 21 Virtual Threads（`spring.threads.virtual.enabled=true`），When 在虚拟线程中输出日志，Then MDC 中的 requestId 正确传递，不因线程切换丢失（要求 Log4j2 版本 ≥ 2.24.0） |

## 5. 非功能需求

- **性能**：AsyncLogger 使用 LMAX Disruptor 异步队列，Mapper SQL 日志不阻塞业务主线程；同步日志输出（Root Logger）对请求耗时影响不超过 2ms。
- **安全**：日志文件路径不得包含用户可控输入，防止路径穿越；日志内容不得打印密码、Token 等敏感字段（由业务层代码保证，本 Spec 不做自动脱敏）。
- **可维护性**：`log4j2.xml` 使用 `<property>` 定义服务名变量（`LOG_HOME`、`APP_NAME`），各服务只需修改这两个变量，不重复维护 appender 配置。
- **兼容性**：Log4j2 版本 ≥ 2.24.0（支持 Virtual Threads MDC 继承）；与 Spring Boot 3.x 官方版本矩阵对齐。

## 6. 数据契约

本 Spec 不涉及数据库表。以下为日志格式与文件路径约定：

### 日志输出格式

```
%d{yyyy-MM-dd HH:mm:ss:SSS} | %p | [%X{requestId}] | - %l - %m%n
```

| 占位符 | 含义 |
|--------|------|
| `%d{yyyy-MM-dd HH:mm:ss:SSS}` | 时间戳，精确到毫秒 |
| `%p` | 日志级别（INFO / WARN / ERROR / DEBUG / TRACE） |
| `%X{requestId}` | MDC 中的 requestId，由 SYS-007 写入；不存在时输出空字符串 |
| `%l` | 输出位置（全限定类名.方法名:行号） |
| `%m` | 日志消息体 |
| `%n` | 换行 |

### 分级文件规范

| Appender 名称 | 文件名 | 过滤级别 | 单文件上限 | 最大归档数 |
|--------------|--------|---------|-----------|-----------|
| RollingFileDebug | `debug.log` | DEBUG+ | 200 MB | 30 |
| RollingFileInfo | `info.log` | INFO+ | 200 MB | 7（默认） |
| RollingFileWarn | `warn.log` | WARN+ | 200 MB | 20 |
| RollingFileError | `error.log` | ERROR+ | 200 MB | 7（默认） |

### 服务日志路径

| 服务 | 日志目录 |
|------|---------|
| gateway-service | `/opt/logs/mro-gateway-service/` |
| manage-web | `/opt/logs/mro-manage-web/` |
| auth-service | `/opt/logs/mro-auth-service/` |
| system-service | `/opt/logs/mro-system-service/` |
| 其他 MRO 微服务 | `/opt/logs/mro-{service-name}/` |

### log4j2.xml 参考配置骨架

```xml
<configuration status="WARN" monitorInterval="30">
  <properties>
    <property name="LOG_HOME">/opt/logs/mro-${APP_NAME}</property>
    <property name="APP_NAME">replace-me</property>
  </properties>

  <appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss:SSS} | %p | [%X{requestId}] | - %l - %m%n"/>
    </Console>

    <RollingFile name="RollingFileDebug"
                 fileName="${LOG_HOME}/debug.log"
                 filePattern="${LOG_HOME}/$${date:yyyy-MM}/mro-${APP_NAME}-debug-%d{yyyy-MM-dd}-%i.log">
      <ThresholdFilter level="debug" onMatch="ACCEPT" onMismatch="DENY"/>
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss:SSS} | %p | [%X{requestId}] | - %l - %m%n"/>
      <Policies>
        <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
        <SizeBasedTriggeringPolicy size="200MB"/>
      </Policies>
      <DefaultRolloverStrategy max="30"/>
    </RollingFile>

    <!-- RollingFileInfo / RollingFileWarn / RollingFileError 结构相同，ThresholdFilter level 与 max 按上表调整 -->
  </appenders>

  <loggers>
    <Logger name="org.springframework" level="INFO"/>

    <Root level="INFO">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="RollingFileInfo"/>
      <AppenderRef ref="RollingFileDebug"/>
      <AppenderRef ref="RollingFileWarn"/>
      <AppenderRef ref="RollingFileError"/>
    </Root>

    <AsyncLogger name="com.mro.${APP_NAME}.mapper" level="TRACE" additivity="false">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="RollingFileDebug"/>
    </AsyncLogger>
  </loggers>
</configuration>
```

## 7. 接口契约

本 Spec 不涉及 HTTP 接口。配置通过各服务 `src/main/resources/log4j2.xml` 落地。

## 8. 权限边界

- 本 Spec 为平台基础配置规范，不涉及业务权限控制。
- 生产环境日志目录 `/opt/logs/` 的文件系统权限由运维管理，服务进程仅需写权限。

## 9. 验收标准

- [ ] 所有服务 `pom.xml` 排除 `spring-boot-starter-logging`，引入 `spring-boot-starter-log4j2`，版本 ≥ 2.24.0。
- [ ] 服务启动后 `/opt/logs/mro-{服务名}/` 目录下生成 debug.log、info.log、warn.log、error.log 四个文件。
- [ ] 每行日志格式符合 `时间戳 | 级别 | [requestId] | - 位置 - 消息` 规范。
- [ ] 携带 `X-Request-Id` 的 HTTP 请求，其触发的所有日志行中 `[requestId]` 字段与 Header 值一致。
- [ ] `org.springframework` 包的 DEBUG 日志不出现在任何日志文件中。
- [ ] Mapper 层 SQL 仅出现在 debug.log 和 Console，不出现在 info.log。
- [ ] 修改 `log4j2.xml` 中的日志级别后，30 秒内无需重启即可生效。
- [ ] Java 21 Virtual Threads 环境下，异步线程中的日志行 requestId 与发起线程一致。
- [ ] debug.log 归档文件最多 30 个，warn.log 最多 20 个，超出后旧文件自动删除。

## 10. 未决问题

- [ ] `APP_NAME` 变量的注入方式：使用 `log4j2.xml` 内 `<property>` 硬编码，还是通过 Spring Boot 的 `logging.config` + `logback-spring.xml` 风格的 `<springProperty>` 动态注入？Log4j2 原生不支持 `<springProperty>`，需确认方案（可选：`log4j2-spring.xml` + `spring.application.name` 系统属性）。
- [ ] Virtual Threads MDC 传递：Log4j2 2.24.0 通过 `ThreadContextMap` 的 `InheritableThreadLocal` 模式支持，需验证与 Dubbo 3.x 线程模型的兼容性。
- [ ] 是否需要为 `RollingFileInfo` 的 DefaultRolloverStrategy 显式设置 `max`？默认 7 个是否满足运维归档需求？

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-28 | 初稿 |
