---
id: SYS-003
title: 角色管理
domain: system
status: draft
owner: '@product'
version: 1.0.2
created: 2026-05-23
updated: 2026-05-27
charter: CHARTER.md
supersedes: []
depends-on: [SYS-001, SYS-004]
---

# Spec: 角色管理

## 1. 背景与目标

角色是 RBAC 的中介：用户绑角色，角色绑菜单权限。角色的 `data_scope` 字段作为**新建用户时的默认数据范围模板**；运行时实际数据范围由用户自身的 `sys_user.data_scope` + `sys_user_dept` 决定（见 SYS-002、SYS-006）。

## 2. 范围

### In Scope
- 列表、增删改、启停、分配菜单权限、设置数据范围默认模板。

### Out of Scope
- 角色继承、动态角色（按规则自动赋予）。
- 运行时数据权限决策（由用户级 data_scope 承担，见 SYS-002）。

## 3. 用户故事

- 作为管理员，我希望创建"部门经理"角色，给定一组菜单权限和"本部门及子部门"数据权限。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 创建角色 | role_key 全局唯一 |
| FR-2 | 分配菜单 | 树形多选；保存到 `sys_role_menu` |
| FR-3 | 数据范围默认模板 | 6 选 1：全部/本部门/本部门及子部门/本人/自定义/本人及下属部门；保存到 `sys_role.data_scope`，仅作为新建用户时的默认值 |
| FR-4 | 自定义部门模板 | 当类型=5 时弹部门树多选；保存到 `sys_role_dept`（仅模板用途，运行时从 `sys_user_dept` 读取） |
| FR-5 | 删除 | 已绑用户的角色禁止删除 |

## 5. 非功能需求

- 角色变更后用户的权限缓存（Redis）应在 ≤ 5s 内一致。

## 6. 数据契约

`sys_role`：`id, role_name, role_key, role_sort, data_scope(1-6), status, is_deleted, audit...`
`sys_role_menu(role_id, menu_id)` 联合唯一。
`sys_role_dept(role_id, dept_id)` 仅作为数据范围默认模板存储，不参与运行时权限决策（运行时使用 `sys_user_dept`）。

## 7. 接口契约

| Method | Path | 权限 |
|--------|------|------|
| GET | /api/system/role | role:list |
| POST | /api/system/role | role:add |
| PUT | /api/system/role/{id} | role:edit |
| DELETE | /api/system/role/{id} | role:delete |
| PUT | /api/system/role/{id}/menus | role:assign-menu |
| PUT | /api/system/role/{id}/depts | role:assign-dept |

## 8. 权限边界

- 菜单权限：`role:*`。
- 超管角色 `role_key='*:*:*'` 不可删除、不可降权。

## 9. 验收标准

- [ ] 角色 data_scope 保存后，新建该角色用户时默认继承此 data_scope 值。
- [ ] sys_role_dept 仅在 dataScope=5 时写入，且不影响运行时数据权限（见 SYS-006）。

## 10. 未决问题

- [ ] 是否允许"角色 → 角色"的层级？（首期不做）

---

## B. 后端实现约束

### B.1 接口 JSON Schema

#### GET /api/system/role（列表）

请求参数：`pageNum=1&pageSize=20&keyword=经理&status=1`

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleKey": "*:*:*",
        "roleSort": 1,
        "dataScope": 1,
        "status": 1,
        "createTime": "2026-05-01 10:00:00"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1716700800000
}
```

#### POST /api/system/role（新增）

请求：
```json
{
  "roleName": "部门经理",
  "roleKey": "dept_manager",
  "roleSort": 2,
  "dataScope": 3,
  "status": 1,
  "menuIds": [1, 2, 3, 10, 11]
}
```

#### PUT /api/system/role/{id}/menus（分配菜单权限）

请求：
```json
{
  "menuIds": [1, 2, 3, 10, 11, 12]
}
```

#### PUT /api/system/role/{id}/depts（配置自定义数据权限部门）

请求：
```json
{
  "deptIds": [10, 11, 12]
}
```

仅当 `dataScope = 5`（自定义）时有效。保存至 `sys_role_dept` 作为模板，**不直接参与运行时数据权限计算**（运行时由 `sys_user_dept` 决定，见 SYS-006）。

### B.2 数据权限类型枚举

| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 本部门数据 |
| 3 | 本部门及子部门数据 |
| 4 | 本人数据 |
| 5 | 自定义部门 |
| 6 | 本人及下属部门 |

### B.3 Dubbo 接口签名（system-service）

```java
public interface RoleDubboService {

    /** 分页查询角色 */
    PageResult<RoleDTO> listRoles(RoleQueryParam param);

    /** 查询角色详情（含已分配菜单 ID 列表） */
    RoleDetailDTO getRoleById(Long roleId);

    /** 新增角色 */
    Long createRole(CreateRoleCommand cmd);

    /** 编辑角色 */
    void updateRole(UpdateRoleCommand cmd);

    /** 删除角色（有绑定用户时抛 SystemException） */
    void deleteRole(Long roleId);

    /** 分配菜单权限（全量替换 sys_role_menu） */
    void assignMenus(Long roleId, List<Long> menuIds);

    /** 配置自定义数据权限部门（全量替换 sys_role_dept） */
    void assignDepts(Long roleId, List<Long> deptIds);

    /** 查询用户的角色列表（用于 user-info 接口） */
    List<RoleDTO> getRolesByUserId(Long userId);
}

public record RoleDTO(
    Long id, String roleName, String roleKey, Integer roleSort,
    Integer dataScope, Integer status, String createTime
) implements Serializable {}

public record RoleDetailDTO(
    Long id, String roleName, String roleKey, Integer roleSort,
    Integer dataScope, Integer status, List<Long> menuIds
) implements Serializable {}

public record CreateRoleCommand(
    String roleName, String roleKey, Integer roleSort,
    Integer dataScope, Integer status, List<Long> menuIds
) implements Serializable {}

public record UpdateRoleCommand(
    Long id, String roleName, Integer roleSort,
    Integer dataScope, Integer status
) implements Serializable {}

public record RoleQueryParam(
    Integer pageNum, Integer pageSize, String keyword, Integer status
) implements Serializable {}
```

### B.4 错误码（system-service 角色，4140–4159）

| 错误码 | 含义 |
|--------|------|
| 4140 | role_key 已存在 |
| 4141 | 角色不存在 |
| 4142 | 该角色已绑定用户，无法删除 |
| 4143 | 超管角色不可删除或降权 |

### B.5 权限缓存刷新

角色菜单变更后，触发 Redis 中受影响用户权限缓存失效：
- Key 格式：`sys:user:permissions:{userId}`，TTL：30min
- 变更角色菜单时，查出所有绑定该角色的用户 ID，批量 `del sys:user:permissions:{userId}`

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 §3.2.3 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：JSON Schema、数据权限枚举、Dubbo 接口签名、错误码 4140–4159、权限缓存刷新策略 |
| 1.0.1 | 2026-05-27 | 数据范围语义调整：角色 data_scope 降级为新建用户时的默认模板值，不再参与运行时权限决策；sys_role_dept 标注为模板用途；更新 FR-3/FR-4/范围/验收标准/数据契约 |
| 1.0.2 | 2026-05-27 | data_scope 枚举扩展至 1-6（新增类型6本人及下属部门）；FR-3 更新为6选1；数据契约 data_scope(1-6) |
