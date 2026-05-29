# Plan 2：Auth + System Spec 补充后端内容

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 auth + system 7 个 spec 追加后端实现内容：完整 JSON Schema、Dubbo 接口签名、错误码分配。

**Architecture:** Gateway → manage-web（厚 BFF）→ system-service / auth-service（Dubbo RPC）。各 spec 在现有 Changelog 前追加 `## B. 后端实现约束` 章节，不修改已有内容。

**Tech Stack:** Java 21、Spring Boot 3.x、Dubbo 3.x、MyBatis-Plus、Redis、MySQL 8。

---

## 文件清单

| 操作 | 文件路径 |
|------|----------|
| 追加 | `specs/auth/001-login-jwt.spec.md` |
| 追加 | `specs/system/001-dept.spec.md` |
| 追加 | `specs/system/002-user.spec.md` |
| 追加 | `specs/system/003-role.spec.md` |
| 追加 | `specs/system/004-menu.spec.md` |
| 追加 | `specs/system/005-dict.spec.md` |
| 追加 | `specs/system/006-data-permission.spec.md` |

---

## Task 1：AUTH-001（登录与 JWT）补充后端内容

**Files:**
- Modify: `specs/auth/001-login-jwt.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/auth/001-login-jwt.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
---
## B. 后端实现约束

### B.1 JWT Payload Schema

```json
{
  "sub": "1",
  "username": "admin",
  "deptId": 10,
  "roles": ["admin", "mro_manager"],
  "permissions": ["dept:list", "user:add"],
  "jti": "uuid-v4",
  "iat": 1716700800,
  "exp": 1716708000
}
```

- `sub`：userId（string）
- `jti`：JWT ID，用于 Redis 黑名单 key
- `iat` / `exp`：Unix 时间戳（秒）

### B.2 Token 传输协议

- 客户端：`Authorization: Bearer <accessToken>`
- Gateway 提取 Header，HTTP POST auth-service `/internal/auth/verify`
- 验证通过后注入 Header 转发 manage-web：
  - `X-User-Id: {userId}`
  - `X-User-Dept-Id: {deptId}`
  - `X-User-Roles: admin,mro_manager`（逗号分隔）
  - `X-User-Permissions: dept:list,user:add`（逗号分隔）
- manage-web 从 Header 读取，构建 `UserContextDTO`，通过 Dubbo Attachment 透传

### B.3 Redis 黑名单策略

| 场景 | Key 格式 | TTL |
|------|----------|-----|
| 验证缓存 | `auth:verify:{jti}` | Token 剩余有效期（动态） |
| 登出黑名单 | `auth:blacklist:{jti}` | Token 剩余有效期（动态） |

auth-service 验证逻辑：先查 `auth:blacklist:{jti}`，存在则拒绝；再查 `auth:verify:{jti}` 缓存命中直接返回。

### B.4 接口 JSON Schema

#### POST /api/auth/login

请求：
```json
{
  "username": "admin",
  "password": "password123"
}
```

响应（成功）：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatar": "https://..."
  },
  "timestamp": 1716700800000
}
```

响应（失败）：
```json
{
  "code": 4011,
  "msg": "用户名或密码错误",
  "data": null,
  "timestamp": 1716700800000
}
```

#### POST /api/auth/refresh-token

请求：
```json
{
  "refreshToken": "eyJhbGci..."
}
```

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGci...",
    "expiresIn": 7200
  },
  "timestamp": 1716700800000
}
```

#### GET /api/auth/user-info

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatar": "https://...",
    "deptId": 10,
    "deptName": "技术部",
    "roles": ["admin"],
    "permissions": ["dept:list", "user:add", "user:edit"]
  },
  "timestamp": 1716700800000
}
```

#### POST /api/auth/change-password

请求：
```json
{
  "oldPassword": "old123",
  "newPassword": "new456",
  "confirmPassword": "new456"
}
```

### B.5 Dubbo 接口签名（auth-service）

```java
public interface AuthDubboService {

    // Gateway 调用（HTTP，非 Dubbo）：POST /internal/auth/verify
    // Dubbo 供 manage-web 调用：

    /** 获取用户完整信息（含权限码），用于 /user-info 接口 */
    UserInfoDTO getUserInfo(Long userId);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 刷新 Token */
    TokenDTO refreshToken(String refreshToken);

    /** 登出（将 jti 加入黑名单） */
    void logout(String jti, long remainingTtlSeconds);
}

public record UserInfoDTO(
    Long userId,
    String username,
    String realName,
    String avatar,
    Long deptId,
    String deptName,
    List<String> roles,
    List<String> permissions
) implements Serializable {}

public record TokenDTO(
    String accessToken,
    long expiresIn
) implements Serializable {}
```

### B.6 错误码（auth-service，4010–4039）

| 错误码 | 含义 |
|--------|------|
| 4010 | 用户名或密码错误 |
| 4011 | Token 无效或已过期 |
| 4012 | Token 已加入黑名单（已登出） |
| 4013 | refreshToken 无效或已过期 |
| 4014 | 新旧密码不能相同 |
| 4015 | 旧密码校验失败 |
| 4020 | 无此接口权限（403） |
| 4021 | 账号已禁用 |
| 4022 | 所属部门已禁用 |

### B.7 manage-web 登录接口实现要点

```
Controller：POST /api/auth/login
  · @Valid LoginRequest（username NotBlank, password Size(6,20)）
  · 无任何权限注解
  · 调 AuthService.login()

Service：AuthService.login()
  · Dubbo 调 auth-service 不适用（登录是 auth-service 自身的 HTTP 接口）
  · manage-web 直接转发登录请求到 auth-service REST 接口（内部 HTTP）
  · 或 manage-web 本地不处理，由 Gateway 路由 /api/auth/** 直接到 auth-service

注意：/api/auth/** 路由由 Gateway 直接路由到 auth-service，
      manage-web 不参与 auth 流程（除 user-info 需要 system-service 的部门名称）
```

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/auth/001-login-jwt.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 2：SYS-001（部门管理）补充后端内容

**Files:**
- Modify: `specs/system/001-dept.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/001-dept.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
---
## B. 后端实现约束

### B.1 接口 JSON Schema

#### GET /api/system/dept/tree

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "deptName": "总公司",
      "deptCode": "HQ",
      "parentId": 0,
      "ancestors": "0",
      "orderNum": 1,
      "leader": "张三",
      "phone": "13800000000",
      "email": "hq@example.com",
      "status": 1,
      "children": [
        {
          "id": 2,
          "deptName": "技术部",
          "deptCode": "TECH",
          "parentId": 1,
          "ancestors": "0,1",
          "orderNum": 1,
          "status": 1,
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1716700800000
}
```

#### POST /api/system/dept（新增）

请求：
```json
{
  "deptName": "技术部",
  "deptCode": "TECH",
  "parentId": 1,
  "orderNum": 1,
  "leader": "李四",
  "phone": "13900000000",
  "email": "tech@example.com",
  "status": 1
}
```

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": { "id": 2 },
  "timestamp": 1716700800000
}
```

#### PUT /api/system/dept/{id}（编辑）

请求体同新增，响应 `data: null`。

#### DELETE /api/system/dept/{id}

响应（有子部门或在岗用户时）：
```json
{
  "code": 4101,
  "msg": "该部门下存在子部门，无法删除",
  "data": null,
  "timestamp": 1716700800000
}
```

#### POST /api/system/dept/import（Excel 导入）

请求：`multipart/form-data`，字段 `file`（Excel 文件）。

响应（含错误行）：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "successCount": 98,
    "failCount": 2,
    "failDetails": [
      { "rowNum": 5, "reason": "dept_code 重复：TECH" },
      { "rowNum": 12, "reason": "parent_code 不存在：UNKNOWN" }
    ]
  },
  "timestamp": 1716700800000
}
```

### B.2 Dubbo 接口签名（system-service）

```java
public interface DeptDubboService {

    /** 查询部门树（带 Redis 缓存） */
    List<DeptTreeDTO> getDeptTree();

    /** 根据 ID 查询部门（带部门名称，用于用户上下文组装） */
    DeptDTO getDeptById(Long deptId);

    /** 新增部门 */
    Long createDept(CreateDeptCommand cmd);

    /** 编辑部门（自动级联更新子部门 ancestors） */
    void updateDept(UpdateDeptCommand cmd);

    /** 删除部门（有子部门或在岗用户时抛 SystemException） */
    void deleteDept(Long deptId);

    /** 批量查询部门（用于数据权限展开） */
    List<DeptDTO> getDeptsByIds(List<Long> deptIds);

    /** 查询某部门的所有子孙部门 ID */
    List<Long> getDescendantDeptIds(Long deptId);
}

public record DeptDTO(Long id, String deptName, String deptCode, Long parentId,
                      String ancestors, Integer status) implements Serializable {}

public record DeptTreeDTO(Long id, String deptName, String deptCode, Long parentId,
                          Integer orderNum, Integer status,
                          List<DeptTreeDTO> children) implements Serializable {}

public record CreateDeptCommand(String deptName, String deptCode, Long parentId,
                                Integer orderNum, String leader, String phone,
                                String email, Integer status) implements Serializable {}

public record UpdateDeptCommand(Long id, String deptName, Long parentId,
                                Integer orderNum, String leader, String phone,
                                String email, Integer status) implements Serializable {}
```

### B.3 错误码（system-service 部门，4100–4119）

| 错误码 | 含义 |
|--------|------|
| 4100 | 部门名称不能为空 |
| 4101 | 该部门下存在子部门，无法删除 |
| 4102 | 该部门下存在在岗用户，无法删除 |
| 4103 | dept_code 已存在 |
| 4104 | 父部门不存在 |
| 4105 | 部门已禁用 |

### B.4 Redis 缓存策略

- Key：`sys:dept:tree`，TTL：30min
- 变更（新增/编辑/删除/导入）后主动 `del sys:dept:tree`

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/001-dept.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 3：SYS-002（用户管理）补充后端内容

**Files:**
- Modify: `specs/system/002-user.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/002-user.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
---
## B. 后端实现约束

### B.1 分页约定

所有列表接口请求参数：

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "keyword": "张三",
  "deptId": 10,
  "status": 1
}
```

响应 data 结构：

```json
{
  "list": [],
  "total": 100,
  "pageNum": 1,
  "pageSize": 20
}
```

`pageNum` 从 1 开始，`pageSize` 默认 20，最大 100。

### B.2 接口 JSON Schema

#### GET /api/system/user（列表）

请求参数（Query String）：`pageNum=1&pageSize=20&keyword=张三&deptId=10&status=1`

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "username": "zhangsan",
        "realName": "张三",
        "employeeNo": "EMP001",
        "gender": 1,
        "phone": "13800000001",
        "email": "zhangsan@example.com",
        "avatar": "https://...",
        "deptId": 10,
        "deptName": "技术部",
        "status": 1,
        "createTime": "2026-05-01 10:00:00",
        "roles": ["admin"]
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1716700800000
}
```

#### POST /api/system/user（新增）

请求：
```json
{
  "username": "lisi",
  "password": "pass123",
  "realName": "李四",
  "employeeNo": "EMP002",
  "gender": 1,
  "phone": "13800000002",
  "email": "lisi@example.com",
  "deptId": 10,
  "status": 1,
  "roleIds": [2, 3]
}
```

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": { "id": 2 },
  "timestamp": 1716700800000
}
```

#### PUT /api/system/user/{id}/reset-password

请求：
```json
{
  "newPassword": "temp123456"
}
```

响应：`data: null`，用户下次登录强制修改密码（`need_change_pwd = 1`）。

#### PUT /api/system/user/{id}/roles

请求：
```json
{
  "roleIds": [1, 2, 3]
}
```

#### POST /api/system/user/import

请求：`multipart/form-data`，字段 `file`。

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "successCount": 48,
    "failCount": 2,
    "failDetails": [
      { "rowNum": 3, "reason": "手机号已存在：13800000001" },
      { "rowNum": 7, "reason": "工号已存在：EMP001" }
    ]
  },
  "timestamp": 1716700800000
}
```

### B.3 Dubbo 接口签名（system-service）

```java
public interface UserDubboService {

    /** 分页查询用户（受数据权限过滤） */
    PageResult<UserDTO> listUsers(UserQueryParam param, UserContextDTO ctx);

    /** 根据 ID 查询用户 */
    UserDTO getUserById(Long userId);

    /** 根据用户名查询（登录鉴权用） */
    UserDTO getUserByUsername(String username);

    /** 新增用户 */
    Long createUser(CreateUserCommand cmd);

    /** 编辑用户 */
    void updateUser(UpdateUserCommand cmd);

    /** 软删除用户 */
    void deleteUser(Long userId);

    /** 重置密码 */
    void resetPassword(Long userId, String newPassword);

    /** 分配角色 */
    void assignRoles(Long userId, List<Long> roleIds);

    /** 获取用户权限码集合（auth-service 验证用） */
    List<String> getUserPermissions(Long userId);
}

public record UserDTO(
    Long id, String username, String realName, String employeeNo,
    Integer gender, String phone, String email, String avatar,
    Long deptId, String deptName, Integer status, String createTime,
    List<String> roles
) implements Serializable {}

public record UserQueryParam(
    Integer pageNum, Integer pageSize, String keyword,
    Long deptId, Integer status
) implements Serializable {}

public record CreateUserCommand(
    String username, String password, String realName, String employeeNo,
    Integer gender, String phone, String email, Long deptId,
    Integer status, List<Long> roleIds
) implements Serializable {}

public record UpdateUserCommand(
    Long id, String realName, Integer gender, String phone,
    String email, String avatar, Long deptId, Integer status
) implements Serializable {}
```

### B.4 错误码（system-service 用户，4120–4139）

| 错误码 | 含义 |
|--------|------|
| 4120 | 用户名已存在 |
| 4121 | 手机号已存在 |
| 4122 | 工号已存在 |
| 4123 | 邮箱已存在 |
| 4124 | 用户不存在 |
| 4125 | 用户已禁用 |
| 4126 | 超管账号不可删除 |
| 4127 | 旧密码校验失败 |

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/002-user.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 4：SYS-003（角色管理）补充后端内容

**Files:**
- Modify: `specs/system/003-role.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/003-role.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
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

仅当 `dataScope = 5`（自定义）时有效。

### B.2 数据权限类型枚举

| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 本部门数据 |
| 3 | 本部门及子部门数据 |
| 4 | 本人数据 |
| 5 | 自定义部门 |

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

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/003-role.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 5：SYS-004（菜单/权限管理）补充后端内容

**Files:**
- Modify: `specs/system/004-menu.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/004-menu.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
---
## B. 后端实现约束

### B.1 接口 JSON Schema

#### GET /api/system/menu/tree（完整菜单树，管理页用）

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "parentId": 0,
      "orderNum": 100,
      "path": "/system",
      "component": null,
      "menuType": "M",
      "perms": null,
      "icon": "setting",
      "visible": 1,
      "status": 1,
      "children": [
        {
          "id": 10,
          "menuName": "用户管理",
          "parentId": 1,
          "orderNum": 1,
          "path": "/system/user",
          "component": "system/user/index",
          "menuType": "C",
          "perms": null,
          "icon": "user",
          "visible": 1,
          "status": 1,
          "children": [
            {
              "id": 100,
              "menuName": "新增用户",
              "parentId": 10,
              "orderNum": 1,
              "path": null,
              "component": null,
              "menuType": "F",
              "perms": "user:add",
              "icon": null,
              "visible": 1,
              "status": 1,
              "children": []
            }
          ]
        }
      ]
    }
  ],
  "timestamp": 1716700800000
}
```

#### GET /api/system/menu/user（当前用户菜单，侧边栏用）

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "menus": [
      {
        "id": 1,
        "menuName": "系统管理",
        "path": "/system",
        "icon": "setting",
        "children": [
          {
            "id": 10,
            "menuName": "用户管理",
            "path": "/system/user",
            "component": "system/user/index",
            "icon": "user",
            "children": []
          }
        ]
      }
    ],
    "permissions": ["user:list", "user:add", "dept:list"]
  },
  "timestamp": 1716700800000
}
```

注意：`menus` 仅含 `menuType = M/C` 且 `visible = 1`、`status = 1` 的节点；`permissions` 为该用户拥有的所有按钮权限码（`menuType = F`）。

#### POST /api/system/menu（新增）

请求：
```json
{
  "menuName": "新增用户",
  "parentId": 10,
  "orderNum": 1,
  "path": null,
  "component": null,
  "menuType": "F",
  "perms": "user:add",
  "icon": null,
  "visible": 1,
  "status": 1
}
```

### B.2 Dubbo 接口签名（system-service）

```java
public interface MenuDubboService {

    /** 查询完整菜单树（管理页） */
    List<MenuTreeDTO> getMenuTree();

    /** 查询当前用户可见菜单树 + 权限码（带缓存） */
    UserMenuResult getUserMenuAndPermissions(Long userId);

    /** 新增菜单 */
    Long createMenu(CreateMenuCommand cmd);

    /** 编辑菜单 */
    void updateMenu(UpdateMenuCommand cmd);

    /** 删除菜单（有子菜单时拒绝） */
    void deleteMenu(Long menuId);

    /** 查询角色已分配的菜单 ID 列表（角色编辑回显用） */
    List<Long> getMenuIdsByRoleId(Long roleId);
}

public record MenuTreeDTO(
    Long id, String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType, String perms,
    String icon, Integer visible, Integer status,
    List<MenuTreeDTO> children
) implements Serializable {}

public record UserMenuResult(
    List<MenuTreeDTO> menus,
    List<String> permissions
) implements Serializable {}

public record CreateMenuCommand(
    String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType,
    String perms, String icon, Integer visible, Integer status
) implements Serializable {}

public record UpdateMenuCommand(
    Long id, String menuName, Long parentId, Integer orderNum,
    String path, String component, String menuType,
    String perms, String icon, Integer visible, Integer status
) implements Serializable {}
```

### B.3 错误码（system-service 菜单，4160–4179）

| 错误码 | 含义 |
|--------|------|
| 4160 | perms 权限码已存在 |
| 4161 | 菜单不存在 |
| 4162 | F 类型菜单必须挂在 C 类型下 |
| 4163 | 该菜单下存在子菜单，无法删除 |
| 4164 | C 类型菜单 path 和 component 不能为空 |

### B.4 Redis 缓存策略

- Key：`sys:user:menu:{userId}`，TTL：30min
- 菜单变更（增/删/改）、角色菜单变更后，批量失效受影响用户缓存

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/004-menu.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 6：SYS-005（字典管理）补充后端内容

**Files:**
- Modify: `specs/system/005-dict.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/005-dict.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
---
## B. 后端实现约束

### B.1 接口 JSON Schema

#### GET /api/system/dict（列表）

请求参数：`pageNum=1&pageSize=20&dictGroup=gender&status=1`

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "dictGroup": "gender",
        "dictCode": "1",
        "dictLabel": "男",
        "status": 1,
        "remark": "",
        "createTime": "2026-05-01 10:00:00"
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1716700800000
}
```

#### GET /api/system/dict/group/{group}（按 group 聚合，供前端缓存）

响应：
```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    { "dictCode": "0", "dictLabel": "女" },
    { "dictCode": "1", "dictLabel": "男" },
    { "dictCode": "2", "dictLabel": "未知" }
  ],
  "timestamp": 1716700800000
}
```

注意：只返回 `status = 1`（启用）的条目，按 `dict_code` 升序排列。

#### POST /api/system/dict（新增）

请求：
```json
{
  "dictGroup": "approval_status",
  "dictCode": "pending",
  "dictLabel": "待审批",
  "status": 1,
  "remark": "工单审批状态"
}
```

### B.2 Dubbo 接口签名（system-service）

```java
public interface DictDubboService {

    /** 分页查询字典 */
    PageResult<DictDTO> listDicts(DictQueryParam param);

    /** 按 group 查询启用字典（带缓存，供前端预热） */
    List<DictItemDTO> getDictByGroup(String dictGroup);

    /** 新增字典 */
    Long createDict(CreateDictCommand cmd);

    /** 编辑字典 */
    void updateDict(UpdateDictCommand cmd);

    /** 删除字典 */
    void deleteDict(Long id);
}

public record DictDTO(
    Long id, String dictGroup, String dictCode, String dictLabel,
    Integer status, String remark, String createTime
) implements Serializable {}

public record DictItemDTO(
    String dictCode, String dictLabel
) implements Serializable {}

public record DictQueryParam(
    Integer pageNum, Integer pageSize, String dictGroup, Integer status
) implements Serializable {}

public record CreateDictCommand(
    String dictGroup, String dictCode, String dictLabel,
    Integer status, String remark
) implements Serializable {}

public record UpdateDictCommand(
    Long id, String dictLabel, Integer status, String remark
) implements Serializable {}
```

### B.3 错误码（system-service 字典，4180–4199）

| 错误码 | 含义 |
|--------|------|
| 4180 | (dict_group, dict_code) 组合已存在 |
| 4181 | 字典不存在 |

### B.4 Redis 缓存策略

- Key：`sys:dict:{dictGroup}`，TTL：1h
- 字典变更后：`del sys:dict:{dictGroup}`

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/005-dict.spec.md
```

期望输出：`version: 1.0.0`

---

## Task 7：SYS-006（数据权限过滤）补充后端内容

**Files:**
- Modify: `specs/system/006-data-permission.spec.md`

- [ ] **Step 1：追加后端实现约束章节并 bump 版本**

在 `specs/system/006-data-permission.spec.md` 的 `## Changelog` 前追加以下内容，同时将 front-matter `version` 改为 `1.0.0`、`updated` 改为 `2026-05-26`：

```markdown
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

拦截器检测到方法上有 `@DataScope` 注解时，根据当前用户（从 `RpcContext.getServerAttachment()` 读取）的最宽松数据权限类型，在原 SQL 的 WHERE 子句末尾追加：

| 类型 | 追加 SQL 片段 |
|------|---------------|
| 1（全部） | 不追加 |
| 2（本部门） | `AND {deptAlias}.create_dept_id = {deptId}` |
| 3（本部门及子部门） | `AND {deptAlias}.create_dept_id IN ({deptId及所有子孙Id})` |
| 4（本人） | `AND {userAlias}.create_user_id = {userId}` |
| 5（自定义部门） | `AND {deptAlias}.create_dept_id IN ({sys_role_dept中所有部门Id及子孙})` |

多角色时取最宽松原则（优先级：1 > 3 > 2 > 5 > 4）。

### B.4 数据权限上下文获取

在 system-service 中，从 Dubbo 服务端 Attachment 读取用户上下文：

```java
public class DataScopeContext {

    public static UserContextDTO current() {
        String userId = RpcContext.getServerAttachment().getAttachment("userId");
        String deptId = RpcContext.getServerAttachment().getAttachment("deptId");
        String roles = RpcContext.getServerAttachment().getAttachment("roles");
        String permissions = RpcContext.getServerAttachment().getAttachment("permissions");
        return new UserContextDTO(
            Long.parseLong(userId),
            Long.parseLong(deptId),
            Arrays.asList(roles.split(",")),
            Arrays.asList(permissions.split(","))
        );
    }
}
```

### B.5 超管跳过与临时关闭

```java
// 超管跳过：roles 包含 *:*:* 时拦截器直接跳过
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

---
```

- [ ] **Step 2：确认版本已更新**

```bash
grep "version:" specs/system/006-data-permission.spec.md
```

期望输出：`version: 1.0.0`
