---
id: SYS-009
plan: system/009-log4j2-config.plan.md
created: 2026-05-28
updated: 2026-05-28
---

# Tasks: Log4j2 应用运行日志配置规范

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 在 `mro-common` 的 `pom.xml` 中排除 `spring-boot-starter-logging`，引入 `spring-boot-starter-log4j2`（≥2.24.0）和 `disruptor`（Log4j2 官方推荐版本） | '@dev' | - | 依赖冲突检查通过（`mvn dependency:tree` 无 Logback 残留）；Code Review | todo |
| T-002 | 在 `mro-common` 中编写 `log4j2-spring.xml` 配置模板：`<springProperty>` 读取 `spring.application.name` 为 `APP_NAME`（默认值 `unknown-service`）；定义 `LOG_HOME=/opt/logs/mro-${APP_NAME}` | '@dev' | T-001 | 模板语法正确，Log4j2 可解析；`APP_NAME` 占位正确；Code Review | todo |
| T-003 | 在模板中配置 Console Appender：格式 `%d{yyyy-MM-dd HH:mm:ss:SSS} \| %p \| [%X{requestId}] \| - %l - %m%n` | '@dev' | T-002 | 控制台输出行包含 requestId 占位符；MDC 无值时输出空字符串而非报错；Code Review | todo |
| T-004 | 在模板中配置 `RollingFileDebug` Appender：`ThresholdFilter level="debug"`；单文件 200MB；按天滚动；归档目录 `$${date:yyyy-MM}`；`DefaultRolloverStrategy max="30"` | '@dev' | T-002 | debug.log 创建正常；按 DEBUG+ 过滤；归档命名符合规范；Code Review | todo |
| T-005 | 在模板中配置 `RollingFileInfo` Appender：`ThresholdFilter level="info"`；单文件 200MB；按天滚动；默认保留策略（7个） | '@dev' | T-002 | info.log 创建正常；INFO+ 过滤；DEBUG 日志不出现；Code Review | todo |
| T-006 | 在模板中配置 `RollingFileWarn` Appender：`ThresholdFilter level="warn"`；单文件 200MB；`DefaultRolloverStrategy max="20"` | '@dev' | T-002 | warn.log 创建正常；WARN+ 过滤正确；Code Review | todo |
| T-007 | 在模板中配置 `RollingFileError` Appender：`ThresholdFilter level="error"`；单文件 200MB；默认保留策略（7个） | '@dev' | T-002 | error.log 创建正常；ERROR+ 过滤正确；Code Review | todo |
| T-008 | 在模板中配置 Root Logger（level=INFO）引用全部五个 Appender；配置 `<Logger name="org.springframework" level="INFO"/>` 过滤框架噪声 | '@dev' | T-003 T-004 T-005 T-006 T-007 | INFO 日志出现在 info.log；org.springframework DEBUG 日志不出现；Code Review | todo |
| T-009 | 在模板中配置 `AsyncLogger` for Mapper：`name="com.mro.${APP_NAME}.mapper"`；`level="TRACE"`；`additivity="false"`；引用 Console + RollingFileDebug | '@dev' | T-008 | SQL 日志仅出现在 debug.log 和 Console，不出现在 info.log；Code Review | todo |
| T-010 | 各服务（gateway-service、manage-web、auth-service、system-service 及 9 个 MRO 微服务）`pom.xml` 排除 Logback，引入 log4j2 依赖；复制模板为本服务 `src/main/resources/log4j2-spring.xml`；确认 `spring.application.name` 已在 Nacos 或本地配置中设置 | '@dev' | T-009 | 13 个服务均启动正常，`/opt/logs/mro-{服务名}/` 下四个日志文件创建；无 Logback 相关 ClassNotFoundException；Code Review | todo |
| T-011 | 集成验证：向任意服务写入 DEBUG / INFO / WARN / ERROR 级别日志，确认各文件过滤正确；触发 Mapper SQL 查询，确认 `additivity=false` 生效（SQL 不出现在 info.log） | '@dev' | T-010 | 四级过滤验证通过；Mapper SQL 仅在 debug.log；Code Review | todo |
| T-012 | 热重载验证：运行中修改 `log4j2-spring.xml` Root level 为 DEBUG，等待 ≤30 秒，确认无需重启即生效 | '@dev' | T-011 | 30 秒内 DEBUG 日志出现在控制台和 debug.log；Code Review | todo |
| T-013 | Virtual Threads MDC 验证：在 `spring.threads.virtual.enabled=true` 环境下，发起携带 `X-Request-Id` 的请求（依赖 SYS-007 T-004 RequestIdFilter），验证所有日志行（含 AsyncLogger Mapper SQL）中 `[requestId]` 与 Header 值一致 | '@dev' | T-010 | 虚拟线程下 requestId 不丢失；Mapper SQL 日志行含正确 requestId；Code Review | todo |
| T-014 | 归档策略验证：模拟 debug.log 超过 30 个归档文件，确认最旧文件自动删除；warn.log 超过 20 个同样验证 | '@dev' | T-011 | 归档数量不超过设定最大值；旧文件自动清理；Code Review | todo |

> 状态枚举：todo / doing / review / done / blocked

## 执行顺序说明

```
T-001 → T-002 → T-003 ┐
                T-004 ├→ T-008 → T-009 → T-010 → T-011 → T-012 ┐
                T-005 ┤                                           ├→ T-013
                T-006 ┤                              T-014 ───────┘
                T-007 ┘
```
