---
id: SYS-002
title: 用户管理
domain: system
status: draft
owner: '@product'
version: 1.2.0
created: 2026-05-23
updated: 2026-05-27
charter: CHARTER.md
supersedes: []
depends-on: [SYS-001, SYS-003]
---

# Spec: 用户管理

## 1. 背景与目标

维护系统用户与其角色、部门、紧急联系人。是认证与数据权限的主体。

## 2. 范围

### In Scope
- 列表（分页/搜索/筛选）、增删改、Excel 导入、重置密码、启停、分配角色、配置紧急联系人。

### Out of Scope
- 用户自助资料修改入口（首期仅管理员维护）。

## 3. 用户故事

- 作为管理员，我希望批量导入新员工并按部门、工号管理。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 列表 | 支持按部门/状态/关键词筛选，分页 |
| FR-2 | 新增 | 表单验证，密码 BCrypt 入库 |
| FR-3 | 删除 | 软删除（is_deleted=1） |
| FR-4 | Excel 导入 | 错误行收集后整体回报，不部分提交 |
| FR-5 | 重置密码 | 生成临时密码并强制下次登录修改 |
| FR-6 | 分配角色 | 可多选；保存到 `sys_user_role` |
| FR-7 | 紧急联系人 | 可增删多个 |
| FR-8 | 配置数据范围 | 为用户单独设置 data_scope（1-6）及自定义部门列表；预计算展开后保存到 `sys_user.data_scope` + `sys_user_dept` |

## 5. 非功能需求

- 单次列表查询 ≤ 1s。
- 导入单批次 ≤ 5000 行。

## 6. 数据契约

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | varchar(64) | Y | 唯一 |
| password | varchar(255) | Y | BCrypt |
| real_name | varchar(64) | Y | |
| employee_no | varchar(64) | Y | 唯一 |
| gender | tinyint | N | 0女/1男/2未知 |
| phone | varchar(20) | Y | 唯一 |
| email | varchar(100) | N | 唯一 |
| avatar | varchar(255) | N | |
| address | varchar(255) | N | |
| dept_id | bigint | Y | |
| status | tinyint | Y | |
| data_scope | tinyint | Y | 数据范围类型 1-6，默认3（本部门及子部门） |

紧急联系人 `sys_user_contact`：`user_id, contact_name, contact_phone, relationship, address`。
自定义数据范围部门 `sys_user_dept`：`user_id, dept_id`，存储预计算展开后的扁平 dept_id 列表；类型 1（全部）和类型 4（本人）不写，其余类型均写入对应展开结果（见 SYS-006 数据契约）。

## 7. 接口契约

| Method | Path | 权限 |
|--------|------|------|
| GET | /api/system/user | user:list |
| POST | /api/system/user | user:add |
| PUT | /api/system/user/{id} | user:edit |
| DELETE | /api/system/user/{id} | user:delete |
| POST | /api/system/user/import | user:import |
| PUT | /api/system/user/{id}/reset-password | user:reset |
| PUT | /api/system/user/{id}/roles | user:assign-role |
| PUT | /api/system/user/{id}/data-scope | user:assign-data-scope |

## 8. 权限边界

- 菜单权限：`user:*`。
- 数据权限：列表受数据权限拦截器过滤（按 `create_dept_id` / `use_for_dept_id`）。

## 9. 验收标准

- [ ] 软删除后用户无法登录但历史数据保留。
- [ ] 唯一索引在并发插入下生效。

## 10. 未决问题

- [ ] 是否支持用户头像 OSS 直传？

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

#### PUT /api/system/user/{id}/data-scope

请求：
```json
{
  "dataScope": 6,
  "deptIds": [10, 11, 12]
}
```

`deptIds` 含义随 `dataScope` 类型而不同：
- 类型 1（全部）、类型 4（本人）：忽略，传空数组即可
- 类型 2（本部门）、类型 3（本部门及子部门）：传空数组（系统自动从用户所在部门计算）
- 类型 5（自定义部门）：传手动选择的 dept_id 列表，后端负责展开子孙
- 类型 6（本人及下属部门）：传空数组（系统自动从用户所在部门计算子部门）

响应：`data: null`。

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

    /** 配置用户数据范围（预计算展开后全量替换 sys_user_dept） */
    void assignDataScope(Long userId, Integer dataScope, List<Long> deptIds);

    /** 获取用户权限码集合（auth-service 验证用） */
    List<String> getUserPermissions(Long userId);
}

public record UserDTO(
    Long id, String username, String realName, String employeeNo,
    Integer gender, String phone, String email, String avatar,
    Long deptId, String deptName, Integer status, Integer dataScope,
    String createTime, List<String> roles
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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 §3.2.2 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：分页约定、JSON Schema、Dubbo 接口签名、错误码 4120–4139 |
| 1.1.0 | 2026-05-27 | 新增 FR-8 用户数据范围配置；数据契约新增 data_scope 字段及 sys_user_dept 表；接口契约新增 PUT /data-scope；B.2 补充 JSON Schema；B.3 新增 assignDataScope Dubbo 方法；UserDTO 新增 dataScope 字段 |
| 1.2.0 | 2026-05-27 | data_scope 类型扩展至 1-6（新增类型6本人及下属部门）；sys_user_dept 语义改为预计算展开存储；B.2 /data-scope 接口补充各类型 deptIds 含义说明 |
