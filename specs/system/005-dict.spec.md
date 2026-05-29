---
id: SYS-005
title: 字典管理
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

# Spec: 字典管理

## 1. 背景与目标

枚举值（性别、状态、用户状态等）的统一来源。结构按 ADR-002 改为单层平铺。

## 2. 范围

### In Scope
- 增删改查、按 group/code/label/status 搜索、启停、按 group 聚合查询。

### Out of Scope
- group 元数据管理（保留扩展点）。

## 3. 用户故事

- 作为运营，我希望新增"用户离职原因"字典而无需开发介入。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 唯一约束 | (dict_group, dict_code) 唯一 |
| FR-2 | 聚合查询 | 一次接口拉取整组用于前端缓存 |
| FR-3 | 启停 | 停用后前端聚合接口默认过滤 |

## 5. 非功能需求

- 聚合接口走缓存，变更后失效。
- 前端启动时预热常用 group。

## 6. 数据契约

`sys_dict`：`id, dict_group, dict_code, dict_label, status, remark, audit...`
唯一索引 `(dict_group, dict_code)`。

## 7. 接口契约

| Method | Path | 权限 |
|--------|------|------|
| GET | /api/system/dict | dict:list |
| GET | /api/system/dict/group/{group} | 已登录 | 按 group 聚合 |
| POST | /api/system/dict | dict:add |
| PUT | /api/system/dict/{id} | dict:edit |
| DELETE | /api/system/dict/{id} | dict:delete |

## 8. 权限边界

- 菜单权限：`dict:*`。

## 9. 验收标准

- [ ] 同 group 下重复 code 入库报 4xx。
- [ ] 缓存失效在 ≤ 5s 生效。

## 10. 未决问题

- [ ] 是否引入 i18n 多语言 label？

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿，源自 §3.2.5（v1.1 修订） |
| 1.0.0 | 2026-05-26 | 新增 B 节后端实现约束：JSON Schema、Dubbo 接口签名、错误码 4180–4199、Redis 缓存策略 |
