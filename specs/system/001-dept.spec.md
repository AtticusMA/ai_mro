---
id: SYS-001
title: 部门管理
domain: system
status: draft
owner: '@product'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 部门管理

## 1. 背景与目标

部门是组织树与数据权限的基础。所有用户必须归属到唯一部门，业务数据必须记录创建部门。

## 2. 范围

### In Scope
- 树形展示 / 新增 / 编辑 / 删除 / 启停 / 排序 / Excel 导入。

### Out of Scope
- 跨集团、矩阵式组织（首期单一树）。

## 3. 用户故事

- 作为管理员，我希望以树状结构维护公司多级部门。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 树形展示 | 支持折叠/展开、按名称搜索高亮 |
| FR-2 | 新增 | 必填校验通过后落库，刷新树 |
| FR-3 | 删除 | 有子部门或在岗用户时禁止删除并提示 |
| FR-4 | 导入 | Excel 模板含 dept_code/parent_code，按 parent_code 解析层级 |
| FR-5 | 启停 | 禁用部门后该部门用户登录提示"部门已禁用" |

## 5. 非功能需求

- 部门树查询走 Redis 缓存；变更后失效。
- 单次最多 10000 节点。

## 6. 数据契约

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dept_name | varchar(64) | Y | |
| dept_code | varchar(64) | Y | 唯一 |
| parent_id | bigint | N | 顶级为 0 |
| ancestors | varchar(255) | Y | 祖级 ID 列表，逗号分隔 |
| order_num | int | N | |
| leader | varchar(64) | N | |
| phone | varchar(20) | N | |
| email | varchar(100) | N | |
| status | tinyint | Y | 0 禁用 / 1 启用 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/system/dept/tree | dept:list | 部门树 |
| POST | /api/system/dept | dept:add | 新增 |
| PUT | /api/system/dept/{id} | dept:edit | 编辑 |
| DELETE | /api/system/dept/{id} | dept:delete | 删除 |
| POST | /api/system/dept/import | dept:import | Excel 导入 |

## 8. 权限边界

- 菜单权限：`dept:list/add/edit/delete/import`。
- 数据权限：部门表本身不参与数据权限过滤（作为权限维度本体）。

## 9. 验收标准

- [ ] 修改父部门后 `ancestors` 自动重算（含所有子部门级联更新）。
- [ ] 部门 code 全局唯一约束生效。

## 10. 未决问题

- [ ] 删除是软删除还是物理删除？

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 项目需求文档-完善版.md §3.2.1 |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：JSON Schema、Dubbo 接口签名、错误码 4100–4119、Redis 缓存策略 |
