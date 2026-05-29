---
id: SYS-006
title: 数据权限过滤
domain: system
status: draft
owner: '@arch'
version: 1.2.0
created: 2026-05-23
updated: 2026-05-27
charter: CHARTER.md
supersedes: []
depends-on: [SYS-001, SYS-002, SYS-003]
---

# Spec: 数据权限过滤

## 1. 背景与目标

为业务表提供透明的行级数据权限过滤，避免每个 Service 重复写 WHERE。
基于 6 种权限类型，通过 MyBatis 拦截器在执行前动态改写 SQL。

**核心设计原则（v1.2.0）：**
- 数据范围绑定在**用户**上（`sys_user.data_scope` + `sys_user_dept`），而非角色。
- `sys_user_dept` 存储**预计算展开后的扁平 dept_id 列表**；拦截器直接 `IN` 查询，运行时不做树遍历。
- 部门变动时，后台任务重新计算并更新受影响用户的 `sys_user_dept`。
- 权限分为两个维度：**部门维度**（`create_dept_id IN`）和**创建人维度**（`create_user_id =`），可单独或组合使用。
- 角色的 `data_scope` 字段仅作为**新建用户时的默认值建议**，不直接参与运行时过滤决策。

## 2. 范围

### In Scope
- 6 种权限类型的拦截逻辑：全部 / 本部门 / 本部门及子部门 / 本人 / 自定义部门 / 本人及下属部门。
- `sys_user_dept` 预计算展开存储，拦截器直接读取，不做运行时树遍历。
- 部门变动时重新计算受影响用户的 `sys_user_dept`。

### Out of Scope
- 字段级（列）权限。
- 行级写入权限（首期仅过滤读）。

## 3. 用户故事

- 作为部门经理，我登录后看到的列表自动过滤为本部门及子部门数据。
- 作为管理员，我希望为两个拥有相同角色的主管分别配置不同的可见部门范围，互不干扰。
- 作为项目经理，我希望看到自己创建的数据以及我所管辖子部门的数据。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 拦截 SELECT | 命中带 `@DataScope` 注解的方法才追加 WHERE 条件 |
| FR-2 | 类型 1 全部 | 不追加任何条件 |
| FR-3 | 类型 2 本部门 | `sys_user_dept` 存本人所在 dept_id（1条）；追加 `create_dept_id IN (...)` |
| FR-4 | 类型 3 本部门及子部门 | `sys_user_dept` 存本部门 + 所有子孙 dept_id；追加 `create_dept_id IN (...)` |
| FR-5 | 类型 4 本人 | `sys_user_dept` 不写；追加 `create_user_id = ?` |
| FR-6 | 类型 5 自定义部门 | `sys_user_dept` 存手动选择部门 + 其子孙 dept_id（预计算展开）；追加 `create_dept_id IN (...)` |
| FR-7 | 类型 6 本人及下属部门 | `sys_user_dept` 存所有直接子部门及其子孙 dept_id；追加 `create_user_id = ? OR create_dept_id IN (...)` |
| FR-8 | 数据范围来源 | 运行时读取 `sys_user.data_scope`，不聚合角色 data_scope |
| FR-9 | 预计算触发 | 配置数据范围时或部门结构变更时，重新计算写入 `sys_user_dept` |

## 5. 非功能需求

- 拦截器单次开销 < 5ms（直接读 `sys_user_dept` 扁平列表，无树遍历）。
- 部门结构变更后，受影响用户的 `sys_user_dept` 重新计算完成时间 ≤ 30s（异步任务）。

## 6. 数据契约

业务表强制字段：
- `create_user_id` BIGINT NOT NULL
- `create_dept_id` BIGINT NOT NULL
- `create_time` / `update_user_id` / `update_time`

用户数据范围字段（`sys_user`）：
- `data_scope` TINYINT NOT NULL DEFAULT 3（1全部/2本部门/3本部门及子部门/4本人/5自定义/6本人及下属部门）

用户数据范围部门关联表（预计算展开存储）：
```
sys_user_dept(
  user_id  BIGINT NOT NULL,
  dept_id  BIGINT NOT NULL,
  PRIMARY KEY (user_id, dept_id)
)
```

各类型写入规则：

| data_scope | sys_user_dept 写入内容 |
|------------|----------------------|
| 1（全部） | 不写 |
| 2（本部门） | 用户所在 dept_id，共 1 条 |
| 3（本部门及子部门） | 用户所在 dept_id + 所有子孙 dept_id |
| 4（本人） | 不写 |
| 5（自定义部门） | 手动选择的 dept_id + 各自子孙 dept_id（全量展开） |
| 6（本人及下属部门） | 用户直接子部门 + 所有子孙 dept_id（不含本人所在部门） |

角色模板表（保留，语义为默认模板）：
- `sys_role_dept(role_id, dept_id)`：创建用户时的默认部门建议，不参与运行时过滤

## 7. 接口契约

不直接暴露接口；通过 `@DataScope(deptAlias="t", userAlias="t")` 注解在 Mapper 方法上启用。

## 8. 权限边界

- 超管角色 `role_key='*:*:*'` 跳过过滤。
- `@IgnoreDataScope` 注解可临时跳过（如统计后台报表，需走另一套审计）。

## 9. 验收标准

- [ ] 类型 2：`sys_user_dept` 仅含 1 条用户所在 dept_id，SQL 追加 `create_dept_id IN (...)`。
- [ ] 类型 3：`sys_user_dept` 包含本部门 + 所有子孙 dept_id，SQL 追加 `create_dept_id IN (...)`。
- [ ] 类型 4：`sys_user_dept` 不写，SQL 追加 `create_user_id = ?`。
- [ ] 类型 5：`sys_user_dept` 包含手动选择部门及其全部子孙，SQL 追加 `create_dept_id IN (...)`。
- [ ] 类型 6：`sys_user_dept` 包含所有子孙部门（不含本人部门），SQL 追加 `create_user_id = ? OR create_dept_id IN (...)`。
- [ ] 部门结构变更后，受影响用户的 `sys_user_dept` 在 30s 内重新计算完成。
- [ ] 同一角色的两个用户配置不同 data_scope，各自列表互不可见。

## 10. 未决问题

- [ ] 大集合（>1000 部门）时改为子查询而非 IN 列表？

---

## B. 后端实现约束

### B.1 @DataScope 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /** 数据表别名，用于 SQL 拼接 */
    String deptAlias() default "t";
    /** 用户别名，用于 SQL 拼接（类型4使用） */
    String userAlias() default "t";
}
```

使用示例（在 Mapper 接口方法上）：

```java
public interface UserMapper extends BaseMapper<SysUser> {

    @DataScope(deptAlias = "u", userAlias = "u")
    List<SysUser> selectUserList(UserQueryParam param);
}
```

### B.2 MyBatis 拦截器注册

```java
@Configuration
public class MyBatisConfig {

    @Bean
    public DataScopeInterceptor dataScopeInterceptor(
            DeptDubboService deptDubboService) {
        return new DataScopeInterceptor(deptDubboService);
    }
}
```

### B.3 SQL 改写模板

拦截器检测到方法上有 `@DataScope` 注解时，读取当前用户的 `data_scope` 值，在原 SQL 的 WHERE 子句末尾追加：

| 类型 | 追加 SQL 片段 | sys_user_dept 是否参与 |
|------|--------------|----------------------|
| 1（全部） | 不追加 | 否 |
| 2（本部门） | `AND {deptAlias}.create_dept_id IN ({sys_user_dept})` | 是（1条） |
| 3（本部门及子部门） | `AND {deptAlias}.create_dept_id IN ({sys_user_dept})` | 是（N条） |
| 4（本人） | `AND {userAlias}.create_user_id = {userId}` | 否 |
| 5（自定义部门） | `AND {deptAlias}.create_dept_id IN ({sys_user_dept})` | 是（N条） |
| 6（本人及下属部门） | `AND ({userAlias}.create_user_id = {userId} OR {deptAlias}.create_dept_id IN ({sys_user_dept}))` | 是（N条） |

> **预计算说明**：`sys_user_dept` 中已存储展开后的扁平列表，拦截器直接 `SELECT dept_id FROM sys_user_dept WHERE user_id = ?` 取出后拼入 IN 条件，无需运行时遍历部门树。

> 超管跳过：`roles` 包含 `*:*:*` 时拦截器直接跳过，不追加任何 SQL。

### B.4 数据权限上下文获取

在 system-service 中，从 Dubbo 服务端 Attachment 读取用户上下文：

```java
public class DataScopeContext {

    public static UserContextDTO current() {
        String userId    = RpcContext.getServerAttachment().getAttachment("userId");
        String deptId    = RpcContext.getServerAttachment().getAttachment("deptId");
        String roles     = RpcContext.getServerAttachment().getAttachment("roles");
        String permissions = RpcContext.getServerAttachment().getAttachment("permissions");
        // v1.1.0: data_scope 由 manage-web 登录时从 sys_user 读取后随 Attachment 透传
        String dataScope = RpcContext.getServerAttachment().getAttachment("dataScope");
        return new UserContextDTO(
            Long.parseLong(userId),
            Long.parseLong(deptId),
            Arrays.asList(roles.split(",")),
            Arrays.asList(permissions.split(",")),
            dataScope != null ? Integer.parseInt(dataScope) : 3  // 默认本部门及子部门
        );
    }
}
```

> `dataScope` 在用户登录时从 `sys_user.data_scope` 读取，与 `userId`/`deptId` 一起写入 JWT 或随 Dubbo Attachment 传递，运行时无需再查库。

### B.5 超管跳过与临时关闭

```java
// 超管跳过：roles 包含 *:*:* 时拦截器直接跳过，不追加任何 SQL

// 临时关闭（如统计报表）：
@IgnoreDataScope
public List<StatDTO> getStatReport(StatParam param) { ... }
```

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDataScope {}
```

### B.6 错误码

数据权限拦截器不直接抛业务错误码；用户上下文缺失时抛 `5001`（系统内部错误：用户上下文丢失）。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 §4 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：@DataScope 注解定义、MyBatis 拦截器注册、SQL 改写模板、用户上下文获取、超管跳过机制 |
| 1.1.0 | 2026-05-27 | 数据范围从角色绑定改为用户绑定：新增 sys_user.data_scope 字段、sys_user_dept 表；FR-6/FR-7 逻辑更新；B.3 类型5来源改为 sys_user_dept；B.4 上下文新增 dataScope 字段透传 |
| 1.2.0 | 2026-05-27 | 新增类型6（本人及下属部门）；sys_user_dept 改为预计算展开存储，移除运行时树遍历；各类型 SQL 模板统一基于 sys_user_dept IN 查询；更新数据契约写入规则表；非功能需求增加重新计算 SLA |
