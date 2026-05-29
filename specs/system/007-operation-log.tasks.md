---
id: SYS-007
plan: system/007-operation-log.plan.md
created: 2026-05-23
updated: 2026-05-28
---

# Tasks: 用户操作日志

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 创建 `sys_operation_log` 表 DDL 迁移脚本（含 `request_id VARCHAR(64)`、`request_time` 索引、`dept_id` 字段） | '@dev' | - | SQL 可幂等执行；字段与 Spec 6 节数据契约完全一致；Code Review | todo |
| T-002 | 创建 `SysOperationLog` Entity + MyBatis Mapper（含分页查询 XML，`request_id` 字段映射） | '@dev' | T-001 | 单测：Mapper CRUD 通过；Code Review | todo |
| T-003 | 配置 `@Async` 线程池 Bean（核心线程 4、最大 16、有界队列 1000、CallerRunsPolicy） | '@dev' | - | 单测：异步任务在独立线程执行；Code Review | todo |
| T-004 | 实现 `RequestIdFilter`：从 `X-Request-Id` Header 读取 requestId → `MDC.put("requestId", id)`；无 Header 时写 `-`；finally 执行 `MDC.clear()`；注册 Filter 顺序在所有业务 Filter 之前 | '@dev' | - | 单测：有 Header 时 MDC 值正确；无 Header 时 MDC 为 `-`；请求结束后 MDC 已清空；不抛异常；Code Review | todo |
| T-005 | 实现 `OperationLogInterceptor`（HandlerInterceptor）：preHandle 以 INFO 打印 `[REQ-IN] requestId\|path\|time\|userId\|body(截断512)`；afterCompletion 以 INFO 打印 `[REQ-OUT] requestId\|result(截断1024)\|costTime\|status`；使用 `ContentCachingResponseWrapper` 读取响应体；全程 try-catch 不上抛 | '@dev' | T-004 | 单测：REQ-IN/REQ-OUT 日志格式正确；超长 body/result 截断正确；异常被静默处理；Code Review | todo |
| T-006 | 实现 `AsyncLogService`：异步将拦截器采集的数据写入 `sys_operation_log` 表（含 `request_id` 字段）；与 T-005 共用同一次参数解析 | '@dev' | T-002 T-003 T-005 | 单测：写入失败不影响主业务；`request_id` 字段正确持久化；Code Review | todo |
| T-007 | 实现 `DubboLogFilter`（抽至 `mro-common`）：调用前以 INFO 打印 `[DUBBO-IN] requestId\|method\|params(截断512)`；正常返回以 INFO 打印 `[DUBBO-OUT] requestId\|method\|costTime\|result(截断512)`；异常以 ERROR 打印 `[DUBBO-ERR] requestId\|method\|costTime\|error`；try-finally 保护；通过 `RpcContext.getServerAttachment("requestId")` 读取 requestId | '@dev' | T-004 | 单测：正常/异常场景日志格式正确；requestId 从 Attachment 读取；业务正常返回不受影响；Code Review | todo |
| T-008 | 在各微服务（system-service、auth-service 等）注册 `DubboLogFilter`：添加 SPI 文件 `META-INF/dubbo/org.apache.dubbo.rpc.Filter`；全局配置 `dubbo.provider.filter=dubboLogFilter` | '@dev' | T-007 | 各服务启动后 Dubbo 调用日志出现 DUBBO-IN/OUT；Code Review | todo |
| T-009 | 实现 `OperationLogService.listPage()`：整合 SYS-006 `@DataScope` 数据权限、动态查询条件（user_name 模糊、时间范围、request_path 模糊、result_status）、分页 | '@dev' | T-002 | 单测：各查询条件单独及组合正确；数据权限 SQL 改写验证；Code Review | todo |
| T-010 | 实现 `OperationLogController`：GET `/api/system/operation-log`（列表）+ GET `/api/system/operation-log/{id}`（详情） | '@dev' | T-006 T-009 | 接口返回格式与 Spec 7 节一致；`log:list` 权限校验生效；Code Review | todo |
| T-011 | 创建前端 Mock 文件 `src/mock/api/operationLog.js`：列表（含 `request_id` 字段、分页、多条件过滤）+ 详情，字段与 Spec 6 节完全一致 | '@dev' | - | Mock 数据可在 `VITE_USE_MOCK=true` 下正常返回；字段命名与接口契约一致；Code Review | todo |
| T-012 | 创建前端 API 模块 `src/api/operationLog.js`：封装 `getOperationLogList(params)` 和 `getOperationLogDetail(id)` | '@dev' | T-011 | 函数签名与 Mock 及真实接口一致；Code Review | todo |
| T-013 | 实现操作日志列表页 `src/views/system/operationLog/index.vue`：搜索栏（操作人、时间范围、路径、结果）、el-table 表格（含 request_id 列）、分页；接入 `log:list` 权限指令 | '@dev' | T-012 | Mock 模式各搜索条件生效，分页正常；Code Review | todo |
| T-014 | 实现日志详情抽屉组件：点击表格行弹出 el-drawer，格式化展示 `request_params` JSON 及 `cost_time`，只读 | '@dev' | T-013 | JSON 格式化显示（非单行字符串）；`cost_time` 单位毫秒展示；Code Review | todo |
| T-015 | 在菜单管理中新增「操作日志」菜单项，绑定权限标识 `log:list`，配置路由 | '@dev' | T-013 | 菜单可按权限显示/隐藏；路由跳转正常；Code Review | todo |
| T-016 | 前后端联调：切换 `VITE_USE_MOCK=false`；验证 Spec 9 节全部验收标准（重点：同一请求 REQ-IN / DUBBO-IN / REQ-OUT requestId 三者一致；MDC 清空无泄漏） | '@dev' | T-010 T-014 T-015 | Spec 9 节全部 12 条验收标准通过；P95 < 1s 压测通过；Code Review | todo |

> 状态枚举：todo / doing / review / done / blocked

## 执行顺序说明

```
T-001 → T-002 ──────────────────┐
T-003 ──────────────────────────┤
T-004 → T-005 → T-006 ──────────┤→ T-010 ──────────────────────┐
         T-004 → T-007 → T-008 ─┘                               ├→ T-016
T-009 ──────────────────────────→ T-010                         |
T-011 → T-012 → T-013 → T-014 ─────────────────────────────────┤
                         T-015 ─────────────────────────────────┘
```
