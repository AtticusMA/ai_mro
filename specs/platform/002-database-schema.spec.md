---
id: PLAT-002
title: 数据库设计
domain: platform
status: approved
owner: '@arch'
version: 1.0.0
created: 2026-05-23
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: [AUTH-001, SYS-001, SYS-002, SYS-003, SYS-004, SYS-005, SYS-006]
---

# Spec: 数据库设计

## 1. 背景与目标

汇总核心表的 DDL 策略；各业务 Spec 引用本文以避免字段定义漂移。
每个微服务拥有独立 MySQL schema，禁止跨 schema 查询。

## 2. 范围

### In Scope
- 基础服务表：`sys_user / sys_dept / sys_role / sys_user_role / sys_role_menu / sys_role_dept / sys_menu / sys_dict / sys_user_contact`。
- 数据权限通用字段约定。
- MRO 业务表通用字段约定。
- 各服务 schema 命名规范。

### Out of Scope
- 具体 MRO 业务表 DDL（各业务 Spec 定义）。
- 时序数据库（InfluxDB）、向量库设计（见 ADR-004）。

## 3. Schema 命名规范

| 服务 | Schema 名 |
|------|-----------|
| auth-service | `mro_auth` |
| system-service | `mro_system` |
| rag-service | `mro_rag` |
| aircraft-health-service | `mro_aircraft_health` |
| ar-maintenance-service | `mro_ar_maintenance` |
| fault-diagnosis-service | `mro_fault_diagnosis` |
| maintenance-manual-service | `mro_maintenance_manual` |
| digital-twin-service | `mro_digital_twin` |
| tooling-material-service | `mro_tooling_material` |
| vr-ar-training-service | `mro_vr_ar_training` |
| paperless-checkin-service | `mro_paperless_checkin` |

**铁律：** 每个 service 的 DataSource 只配置自己的 schema，禁止配置其他 schema。

## 4. 基础服务表（mro_system schema）

### sys_user

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| username | varchar(64) | UNIQUE NOT NULL | 登录名 |
| password | varchar(100) | NOT NULL | BCrypt 加密（ADR-001） |
| employee_no | varchar(64) | UNIQUE | 工号 |
| real_name | varchar(64) | NOT NULL | 真实姓名 |
| dept_id | bigint | NOT NULL | 所属部门 |
| phone | varchar(20) | UNIQUE | |
| email | varchar(100) | | |
| avatar | varchar(255) | | |
| status | tinyint | NOT NULL DEFAULT 1 | 0 禁用 / 1 启用 |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | 软删除 |
| create_user_id | bigint | NOT NULL | |
| create_dept_id | bigint | NOT NULL | |
| create_time | datetime | NOT NULL | |
| update_user_id | bigint | | |
| update_time | datetime | | |

索引：`username` 唯一、`phone` 唯一、`employee_no` 唯一、`dept_id`、`status`。

### sys_dept

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| dept_name | varchar(64) | NOT NULL | |
| dept_code | varchar(64) | UNIQUE NOT NULL | |
| parent_id | bigint | NOT NULL DEFAULT 0 | 顶级为 0 |
| ancestors | varchar(512) | NOT NULL | 祖级 ID 逗号分隔 |
| order_num | int | NOT NULL DEFAULT 0 | |
| leader | varchar(64) | | |
| phone | varchar(20) | | |
| email | varchar(100) | | |
| status | tinyint | NOT NULL DEFAULT 1 | 0 禁用 / 1 启用 |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

索引：`parent_id`、`dept_code` 唯一。

### sys_role

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| role_name | varchar(64) | NOT NULL | |
| role_key | varchar(100) | UNIQUE NOT NULL | 权限字符串，超管为 `*:*:*` |
| data_scope | tinyint | NOT NULL DEFAULT 1 | 1全部/2本部门/3本部门及子部门/4本人/5自定义 |
| order_num | int | NOT NULL DEFAULT 0 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

### sys_user_role

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| user_id | bigint | NOT NULL |
| role_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合唯一：`(user_id, role_id)`。

### sys_role_menu

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| role_id | bigint | NOT NULL |
| menu_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合唯一：`(role_id, menu_id)`。

### sys_role_dept

| 字段 | 类型 | 约束 |
|------|------|------|
| id | bigint | PK |
| role_id | bigint | NOT NULL |
| dept_id | bigint | NOT NULL |
| create_time | datetime | NOT NULL |

联合索引：`(role_id, dept_id)`。

### sys_menu

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK | |
| menu_name | varchar(64) | NOT NULL | |
| parent_id | bigint | NOT NULL DEFAULT 0 | |
| menu_type | char(1) | NOT NULL | M目录/C菜单/F按钮 |
| path | varchar(255) | | 路由地址 |
| component | varchar(255) | | 组件路径 |
| perms | varchar(100) | | 权限码，如 `dept:list` |
| icon | varchar(100) | | |
| order_num | int | NOT NULL DEFAULT 0 | |
| visible | tinyint | NOT NULL DEFAULT 1 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

索引：`parent_id`、`perms`。

### sys_dict

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK | |
| dict_group | varchar(64) | NOT NULL | 字典分组，如 `gender` |
| dict_code | varchar(64) | NOT NULL | 字典码，如 `male` |
| dict_label | varchar(100) | NOT NULL | 显示名 |
| dict_value | varchar(100) | NOT NULL | 存储值 |
| order_num | int | NOT NULL DEFAULT 0 | |
| status | tinyint | NOT NULL DEFAULT 1 | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | |
| create_time | datetime | NOT NULL | |
| update_time | datetime | | |

联合唯一：`(dict_group, dict_code)`。见 ADR-002（单层平铺）。

## 5. 数据权限通用字段

所有需要数据权限过滤的业务表必须包含以下字段（见 SYS-006）：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| create_user_id | bigint | NOT NULL | 创建人 ID |
| create_dept_id | bigint | NOT NULL | 创建人所属部门 ID |
| use_for_dept_id | bigint | NULL | 业务归属部门 ID（可与创建部门不同） |
| create_time | datetime | NOT NULL | |
| update_user_id | bigint | NULL | |
| update_time | datetime | NULL | |

## 6. MRO 业务表通用字段

8 个 MRO 业务 schema 中的业务主表，除数据权限字段外还需包含：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | |
| is_deleted | tinyint | NOT NULL DEFAULT 0 | 软删除（0否/1是） |
| version | int | NOT NULL DEFAULT 0 | 乐观锁（MyBatis-Plus @Version） |
| remark | varchar(500) | NULL | 备注 |

## 7. 软删除与状态字段规范

| 字段 | 用途 | 规范 |
|------|------|------|
| `is_deleted` | 软删除 | 0=未删除，1=已删除；所有表必须有；MyBatis-Plus `@TableLogic` |
| `status` | 业务状态 | 0=禁用，1=启用；仅有状态概念的表才有 |

两者明确分离：`is_deleted` 控制可见性，`status` 控制可用性。

## 8. 验收标准

- [ ] 所有 service 的 DataSource 配置仅指向自己的 schema，无跨库配置。
- [ ] 所有业务主表包含数据权限通用字段（§5）。
- [ ] 软删除字段 `is_deleted` 与状态字段 `status` 命名一致，不混用。
- [ ] 联合唯一索引在并发插入下生效（测试验证）。

## 9. 未决问题

- [ ] 是否引入分库分表？首期单库单 schema。
- [ ] MRO 时序数据（传感器）的 InfluxDB schema 设计由各 MRO Spec 定义。

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-23 | 初稿 |
| 1.0.0 | 2026-05-26 | 重写：独立 schema 规范，MRO 通用字段，软删除规范，见 ADR-006 |
