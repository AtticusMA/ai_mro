---
id: SYS-007
title: 用户操作日志
domain: system
status: approved
owner: '@product'
version: 0.3.0
created: 2026-05-23
updated: 2026-05-28
charter: CHARTER.md
supersedes: []
depends-on: [SYS-002, SYS-006, SYS-009]
---

# Spec: 用户操作日志

## 1. 背景与目标

系统需要对用户的操作行为进行审计追踪，满足内部安全合规要求，并为问题排查提供依据。
通过服务端拦截器（Spring AOP / Filter）自动采集每次 HTTP 请求的关键信息，异步持久化到数据库，
前端提供查询与展示页面，权限控制沿用 SYS-006 数据权限体系。

对齐 Charter 目标：完善的权限/数据权限管理底座 + 安全审计能力。

## 2. 范围

### In Scope

- 服务端拦截器自动记录操作日志（请求路径、请求时间、请求耗时、操作人 ID/姓名、请求参数、成功/失败）。
- 操作日志列表页：支持按操作人姓名/用户名、时间范围、请求路径、操作结果查询。
- 分页展示，支持查看单条日志详情（含完整请求参数和请求耗时）。
- 日志数据遵循 SYS-006 数据权限过滤。
- 日志只读，不提供删除功能。
- 从前端请求 Header `X-Request-Id` 读取 requestId，写入 MDC，透传至 Dubbo Attachment，实现全链路日志关联。
- HTTP 拦截器在请求执行前/后分别向控制台打印结构化日志（REQ-IN / REQ-OUT），含 requestId、路径、时间、操作人、请求体、响应结果、耗时。
- Dubbo Provider 端过滤器向控制台打印调用日志（DUBBO-IN / DUBBO-OUT / DUBBO-ERR），含 requestId、方法名、请求参数、耗时。

### Out of Scope

- 登录/登出日志（属于认证模块，后续单独 Spec）。
- 字段级变更 diff（只记录原始请求参数，不做新旧值对比）。
- 日志导出（后续按需增量加入）。
- 敏感字段自动脱敏（首期完整保存，后续如需脱敏单独 ADR）。
- 日志删除/清理（不支持）。

## 3. 用户故事 / 使用场景

- 作为**系统管理员**，我希望查看所有用户的操作日志，以便追溯异常操作来源。
- 作为**普通管理员**，我希望按数据权限范围查看本部门用户的操作日志，以便进行部门内审计。
- 作为**运维人员**，我希望按时间范围和请求路径筛选日志，以便快速定位接口异常。
- 作为**开发人员**，我希望查看完整的请求参数和请求耗时，以便复现和排查性能问题。
- 作为**开发人员**，我希望通过 requestId 在控制台日志中串联 HTTP 入口到 Dubbo 调用的完整链路，以便快速定位跨服务问题。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 服务端拦截器采集操作日志 | Given 用户发起任意 HTTP 请求，When 请求处理完成（成功或异常），Then 拦截器异步写入一条日志记录到数据库，包含：请求路径、请求时间、**请求耗时（毫秒）**、操作人 user_id、操作人 user_name、请求参数（JSON）、操作结果（success/fail） |
| FR-2 | 日志列表分页查询 | Given 管理员进入操作日志页面，When 使用任意查询条件组合，Then 返回符合条件的日志列表，分页显示（默认 20 条/页），响应时间 P95 < 1s |
| FR-3 | 按操作人查询 | Given 操作日志列表页，When 输入操作人姓名或用户名（模糊匹配），Then 仅展示匹配用户的日志 |
| FR-4 | 按时间范围查询 | Given 操作日志列表页，When 选择开始时间和结束时间，Then 仅展示该时间区间内的日志 |
| FR-5 | 按请求路径查询 | Given 操作日志列表页，When 输入请求路径关键词（模糊匹配），Then 仅展示路径匹配的日志 |
| FR-6 | 按操作结果查询 | Given 操作日志列表页，When 选择成功或失败，Then 仅展示对应结果的日志 |
| FR-7 | 查看日志详情 | Given 日志列表中任意一条记录，When 点击查看详情，Then 弹出详情面板，展示该条日志的完整请求参数（格式化 JSON）及请求耗时 |
| FR-8 | 数据权限过滤 | Given 当前登录用户的数据权限类型，When 查询操作日志，Then 仅返回其数据权限范围内的日志（依赖 SYS-006） |
| FR-9 | requestId 读取与全链路传递 | Given 前端发起请求并在 Header 中携带 `X-Request-Id`，When manage-web 入口 Filter 处理请求，Then 从 Header 读取 requestId 写入 MDC，并在调用 Dubbo 时注入 RpcContext Attachment（key: `requestId`）；下游微服务从 Attachment 读取后写入本地 MDC；请求结束时执行 MDC.clear()；若 Header 不携带则 MDC 记为 `-` |
| FR-10 | HTTP 拦截器控制台打印 | Given 用户发起任意 `/api/**` 请求，When preHandle 阶段，Then 以 INFO 级别打印 `[REQ-IN ] requestId=\|path=\|time=\|userId=\|body=（超 512 字符截断）`；When afterCompletion 阶段，Then 以 INFO 级别打印 `[REQ-OUT] requestId=\|result=（超 1024 字符截断）\|costTime=ms\|status=success\|fail` |
| FR-11 | Dubbo Provider 过滤器控制台打印 | Given manage-web 通过 Dubbo 调用下游微服务，When Provider 端 DubboLogFilter 拦截，Then 调用前以 INFO 级别打印 `[DUBBO-IN ] requestId=\|method=接口类#方法名\|params=（超 512 字符截断）`；正常返回以 INFO 打印 `[DUBBO-OUT] requestId=\|method=\|costTime=ms\|result=（超 512 字符截断）`；异常以 ERROR 打印 `[DUBBO-ERR] requestId=\|method=\|costTime=ms\|error=异常类型:消息` |

## 5. 非功能需求

- **性能**：日志写入采用异步方式（线程池/消息队列），不阻塞主业务请求；列表查询 P95 < 1s；`request_time` 字段建立索引；控制台打印为同步操作但仅字符串拼接，对主链路耗时增加不超过 2ms。
- **安全**：日志接口须携带有效 JWT Token；仅具备 `log:list` 权限的用户可访问日志页面；日志数据只读，无修改/删除接口。
- **可用性**：日志写入失败不影响主业务响应（catch 异常静默处理）；Dubbo Filter 异常通过 try-finally 保护，不影响业务正常返回。
- **存储**：日志表数据量可能较大，`request_time` 加索引；后续按需引入分区或归档方案。
- **线程安全**：MDC 在请求结束时必须执行 clear()，避免线程池复用导致 requestId 污染。

## 6. 数据契约

### 表：sys_operation_log

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| id | BIGINT | 是 | PK, AUTO_INCREMENT | 主键 |
| request_id | VARCHAR(64) | 否 | | 前端传入的请求唯一标识（来自 Header X-Request-Id），用于全链路日志关联；未传时为 NULL |
| request_path | VARCHAR(512) | 是 | NOT NULL | 请求路径，如 /api/user/list |
| request_time | DATETIME(3) | 是 | NOT NULL, INDEX | 请求发生时间（毫秒精度） |
| cost_time | BIGINT | 是 | NOT NULL | 请求耗时（毫秒） |
| user_id | BIGINT | 是 | NOT NULL | 操作人用户 ID |
| user_name | VARCHAR(64) | 是 | NOT NULL | 操作人姓名 |
| request_params | TEXT | 否 | | 请求参数（JSON 字符串，body + query params） |
| result_status | TINYINT | 是 | NOT NULL | 操作结果：1=成功，0=失败 |
| dept_id | BIGINT | 否 | | 操作人所属部门 ID（用于数据权限过滤） |
| created_at | DATETIME | 是 | NOT NULL | 记录创建时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/system/operation-log | log:list | 分页查询操作日志列表 |
| GET | /api/system/operation-log/{id} | log:list | 查询单条操作日志详情 |

### GET /api/system/operation-log 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| page_size | Integer | 否 | 每页条数，默认 20，最大 100 |
| user_name | String | 否 | 操作人姓名/用户名（模糊） |
| begin_time | String | 否 | 开始时间，格式 YYYY-MM-DD HH:mm:ss |
| end_time | String | 否 | 结束时间，格式 YYYY-MM-DD HH:mm:ss |
| request_path | String | 否 | 请求路径关键词（模糊） |
| result_status | Integer | 否 | 操作结果：1=成功，0=失败 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `log:list` —— 查看操作日志列表及详情
- 数据权限：
  - 依赖 SYS-006 数据权限过滤机制，`sys_operation_log.dept_id` 作为过滤字段。
  - 权限类型遵循 SYS-006 定义的 5 种类型（全部 / 本部门 / 本部门及子部门 / 本人 / 自定义部门）。

## 9. 验收标准

- [ ] 服务端拦截器覆盖所有 `/api/` 请求，成功和失败的请求均有日志记录。
- [ ] 每条日志记录包含正确的 `cost_time`（单位毫秒，值 ≥ 0）。
- [ ] 异步写入不阻塞主业务，主业务接口响应时间不因日志采集增加超过 10ms。
- [ ] 日志列表分页查询正确，各查询条件单独及组合均生效。
- [ ] 数据权限过滤生效：不同权限类型的用户只能查到自己权限范围内的日志。
- [ ] 日志详情可正确展示完整请求参数（格式化 JSON）及请求耗时。
- [ ] 无删除/修改接口，日志只读。
- [ ] 前端 Mock 数据与接口契约第 7 节保持一致（含 cost_time 字段）。
- [ ] 前端携带 `X-Request-Id` Header 时，同一请求的所有日志行（REQ-IN、REQ-OUT、DUBBO-IN、DUBBO-OUT）均含相同 requestId。
- [ ] 前端未携带 `X-Request-Id` 时，MDC 中 requestId 为 `-`，不抛出异常，主业务正常响应。
- [ ] REQ-IN 日志包含 requestId、path、time、userId、body（超长截断），REQ-OUT 包含 requestId、result（超长截断）、costTime、status。
- [ ] DUBBO-IN/OUT/ERR 日志中 requestId 与同次 HTTP 请求的 REQ-IN requestId 一致。
- [ ] 请求结束后 MDC 已清空，下一次请求不残留前次 requestId。

## 10. 未决问题

- [ ] 拦截器路径白名单：健康检查（`/actuator/**`）、静态资源等路径是否需排除控制台打印和数据库写入？建议实现阶段通过可配置白名单解决。
- [ ] 超大请求体截断策略：控制台打印截断 512 字符、数据库保存截断 4096 字符，文件上传接口（`multipart/form-data`）是否直接跳过 body 记录？
- [ ] Virtual Threads 下 MDC 传递：Java 21 Virtual Threads 的 ThreadLocal（MDC 底层）在线程切换时是否正确继承？需验证 Log4j2 版本对 Virtual Threads MDC 的支持情况（依赖 SYS-009 配置规范）。
- [ ] Dubbo Filter 注册范围：`DubboLogFilter` 是注册为全局 Filter（所有 Provider）还是按服务配置？建议全局注册，由白名单控制豁免。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿 |
| 0.2.0 | 2026-05-23 | 新增 cost_time 字段（请求耗时，毫秒）；FR-1/FR-7 验收标准同步更新；移除已决策的未决问题 |
| 0.3.0 | 2026-05-28 | 新增 FR-9（requestId 全链路传递）、FR-10（HTTP 拦截器控制台打印）、FR-11（Dubbo 过滤器打印）；数据契约补充 request_id 字段；非功能需求补充控制台性能与 MDC 线程安全要求；验收标准扩充 5 项；未决问题更新为 4 项；depends-on 新增 SYS-009 |
