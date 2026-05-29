---
id: MRO-009
title: 人员证照与资质管理
domain: mro
status: approved
owner: '@product'
version: 1.1.1
created: 2026-05-28
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: [MRO-008]
---

# Spec: 人员证照与资质管理

## 1. 背景与目标

维修人员必须持有有效证照才能执行对应机型和等级的维修任务。通过数字化管理人员证照档案，实现证照到期自动预警、与工卡动态资质校验集成，防止无证或过期证照人员上岗操作。

对齐 Charter 目标：安全提升（资质动态匹配防止越权操作）、合规（证照数字档案符合 CCAR-66 规定，支持审计追溯）。

## 2. 范围

### In Scope
- 人员证照档案管理（CCAR-66 维修执照、型别授权、特种作业证等）
- 证照有效期追踪与到期自动预警（提前30天/7天）
- 证照与工卡任务的动态资质校验（已在 MRO-008 中定义接口，本 Spec 提供数据源）
- 证照附件上传（扫描件存档）
- 人员资质概览报表

### Out of Scope
- 证照申请/考试流程（属于培训管理系统）
- 薪酬与绩效管理
- 跨公司人员调配

## 3. 用户故事 / 使用场景

- 作为 HR/资质管理员，我希望录入并维护每名维修人员的证照信息，以便有据可查。
- 作为机库管理人员，我希望在分配工卡时系统自动筛选符合资质的人员，以便防止越权操作。
- 作为质量安全部门，我希望证照即将到期时系统自动提醒相关人员，以便及时续期避免违规上岗。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持人员证照档案的增删改查，含证照类型、机型授权、授权等级、有效期 | Given 管理员进入证照管理 When 新增证照信息 Then 证照记录保存成功，关联到对应人员 |
| FR-2 | 支持证照附件（扫描件）上传与在线预览 | Given 证照记录已创建 When 上传 PDF/图片附件 Then 附件成功关联到证照记录，可在线预览 |
| FR-3 | 证照到期前30天和7天自动推送预警通知给证照持有人和资质管理员 | Given 证照已记录有效期 When 距到期 ≤ 30天 Then 系统推送预警；距到期 ≤ 7天再次加急推送 |
| FR-4 | 支持按人员/机型/证照类型/有效状态筛选查询 | Given 查询页面 When 输入筛选条件 Then 返回符合条件的证照列表 |
| FR-5 | 提供资质概览报表：统计各机型持证人数、即将到期人数、已过期人数 | Given 证照数据已录入 When 查看资质报表 Then 展示各维度统计数据 |
| FR-6 | 支持证照批量导入（Excel 模板） | Given 管理员准备好 Excel 文件 When 执行批量导入 Then 成功录入，错误行给出提示 |

## 5. 非功能需求

- 性能：证照列表查询 ≤ 2秒；资质匹配校验 ≤ 500ms
- 安全：证照附件存储于对象存储，访问需鉴权；操作审计日志
- 可用性：系统核心服务可用性 ≥ 99.95%
- 合规：证照记录符合 CCAR-66 规定，支持按人员/时间段导出审计报告

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| personnel_license.id | bigint | Y | PK | 证照 ID |
| personnel_license.user_id | bigint | Y | FK | 人员 ID |
| personnel_license.license_type | varchar(64) | Y | | 证照类型（CCAR-66/型别授权/特种等） |
| personnel_license.license_no | varchar(64) | Y | UK(user_id+license_no) | 证照编号 |
| personnel_license.aircraft_type | varchar(32) | N | | 适用机型（如 B737-800） |
| personnel_license.category | enum | N | A/B1/B2/C | CCAR-66 执照类别 |
| personnel_license.valid_from | date | Y | | 有效期起 |
| personnel_license.valid_to | date | Y | | 有效期止 |
| personnel_license.issued_by | varchar(128) | N | | 颁发机构 |
| personnel_license.attachment_url | varchar(512) | N | | 附件地址 |
| personnel_license.status | enum | Y | valid/expiring_soon/expired | 有效状态（系统自动计算） |
| personnel_license.created_by | bigint | Y | FK | 录入人 |
| personnel_license.created_at | timestamp | Y | | 创建时间 |
| personnel_license.updated_at | timestamp | Y | | 更新时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/personnel/licenses | personnel:view | 获取证照列表（支持分页/筛选） |
| POST | /api/personnel/licenses | personnel:manage | 新增证照 |
| GET | /api/personnel/licenses/{id} | personnel:view | 获取证照详情 |
| PUT | /api/personnel/licenses/{id} | personnel:manage | 修改证照 |
| DELETE | /api/personnel/licenses/{id} | personnel:manage | 删除证照 |
| POST | /api/personnel/licenses/{id}/attachment | personnel:manage | 上传证照附件 |
| GET | /api/personnel/licenses/alerts | personnel:view | 获取即将到期/已过期预警列表 |
| GET | /api/personnel/licenses/stats | personnel:view | 资质概览统计 |
| POST | /api/personnel/licenses/import | personnel:manage | 批量导入（Excel） |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `personnel:view` — 查看证照列表、详情、预警、统计
  - `personnel:manage` — 新增/修改/删除证照、上传附件、批量导入
- 数据权限：按维修基地/部门过滤；人员本人可查看自己的证照

## 9. 验收标准

- [ ] 证照 CRUD 及附件上传正常
- [ ] 证照到期前30天/7天自动预警推送
- [ ] 资质列表筛选查询响应 ≤ 2秒
- [ ] 资质概览统计数据准确
- [ ] Excel 批量导入成功，错误行有明确提示
- [ ] 所有接口通过权限验证
- [ ] 资质校验与工卡系统集成正常（MRO-008 qualifications/match 接口使用本 Spec 数据）

## 10. 未决问题

- [ ] CCAR-66 执照类别与机型授权的关联规则需与适航部门确认
- [ ] 证照附件存储方案（MinIO 还是 OSS）待基础设施确认
- [ ] 批量导入 Excel 模板格式需与 HR 部门对齐

## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/personnel/licenses — 证照列表

Request query: `?pageNum=1&pageSize=20&userId=101&status=expiring_soon&aircraftType=B737-800`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "userId": 101,
        "userName": "王工程师",
        "licenseType": "CCAR-66",
        "licenseNo": "CCAR66-2020-00101",
        "aircraftType": "B737-800",
        "category": "B1",
        "validFrom": "2020-01-01",
        "validTo": "2026-06-15",
        "issuedBy": "中国民用航空局",
        "status": "expiring_soon",
        "attachmentUrl": "https://oss.example.com/licenses/1001.pdf"
      }
    ],
    "total": 45, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/personnel/licenses — 新增证照

Request body:
```json
{
  "userId": 101,
  "licenseType": "CCAR-66",
  "licenseNo": "CCAR66-2020-00101",
  "aircraftType": "B737-800",
  "category": "B1",
  "validFrom": "2020-01-01",
  "validTo": "2026-06-15",
  "issuedBy": "中国民用航空局"
}
```

Response: `{"code": 0, "msg": "ok", "data": {"id": 1001}, "timestamp": 1748304000000}`

#### GET /api/personnel/licenses/alerts — 预警列表

Request query: `?pageNum=1&pageSize=20&alertLevel=urgent`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "licenseId": 1001,
        "userId": 101,
        "userName": "王工程师",
        "licenseType": "CCAR-66",
        "licenseNo": "CCAR66-2020-00101",
        "aircraftType": "B737-800",
        "validTo": "2026-06-15",
        "daysUntilExpiry": 18,
        "alertLevel": "warning"
      }
    ],
    "total": 6, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/personnel/licenses/stats — 资质概览统计

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "totalPersonnel": 120,
    "totalLicenses": 345,
    "expiringSoon": 12,
    "expired": 3,
    "byAircraftType": [
      {"aircraftType": "B737-800", "total": 85, "valid": 80, "expiringSoon": 4, "expired": 1}
    ]
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

人员证照归属 system-service，通过 Dubbo RPC 供 workcard-service（资质校验）和 manage-web（管理界面）调用。

```java
public interface PersonnelLicenseDubboService {
    PageResult<PersonnelLicenseDTO> listLicenses(LicenseQueryParam param);
    Long createLicense(CreateLicenseCommand cmd);
    PersonnelLicenseDTO getLicense(Long licenseId);
    void updateLicense(UpdateLicenseCommand cmd);
    void deleteLicense(Long licenseId, Long operatorId);
    void uploadAttachment(Long licenseId, String attachmentUrl, Long operatorId);
    PageResult<LicenseAlertDTO> listAlerts(LicenseAlertQueryParam param);
    LicenseStatsDTO getStats(UserContextDTO ctx);
    int importLicenses(List<ImportLicenseCommand> items, Long operatorId);
    List<PersonnelLicenseDTO> listValidLicensesByUser(Long userId);
    boolean checkQualification(Long userId, String aircraftType, String category);
}

public record PersonnelLicenseDTO(
    Long id, Long userId, String userName, String licenseType,
    String licenseNo, String aircraftType, String category,
    LocalDate validFrom, LocalDate validTo, String issuedBy,
    String attachmentUrl, String status
) implements Serializable {}

public record CreateLicenseCommand(
    Long userId, String licenseType, String licenseNo, String aircraftType,
    String category, LocalDate validFrom, LocalDate validTo,
    String issuedBy, Long createdBy
) implements Serializable {}

public record UpdateLicenseCommand(
    Long licenseId, String aircraftType, String category,
    LocalDate validFrom, LocalDate validTo, String issuedBy, Long operatorId
) implements Serializable {}

public record LicenseQueryParam(
    Long userId, String status, String aircraftType, String licenseType,
    int pageNum, int pageSize
) implements Serializable {}

public record LicenseAlertDTO(
    Long licenseId, Long userId, String userName, String licenseType,
    String licenseNo, String aircraftType, LocalDate validTo,
    int daysUntilExpiry, String alertLevel
) implements Serializable {}

public record LicenseAlertQueryParam(
    String alertLevel, int pageNum, int pageSize
) implements Serializable {}

public record LicenseStatsDTO(
    int totalPersonnel, int totalLicenses, int expiringSoon, int expired,
    List<AircraftTypeLicenseStatDTO> byAircraftType
) implements Serializable {}

public record AircraftTypeLicenseStatDTO(
    String aircraftType, int total, int valid, int expiringSoon, int expired
) implements Serializable {}

public record ImportLicenseCommand(
    Long userId, String licenseType, String licenseNo, String aircraftType,
    String category, LocalDate validFrom, LocalDate validTo, String issuedBy
) implements Serializable {}
```

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4000 | 证照不存在 |
| 4001 | 证照编号已存在（同一人员唯一约束冲突） |
| 4002 | 有效期起止时间无效（结束早于开始） |
| 4003 | 附件格式不支持（仅允许 PDF/JPG/PNG） |
| 4004 | 附件大小超限（最大 10MB） |
| 4005 | 人员不存在 |
| 4006 | 操作人无证照管理权限 |
| 4007 | 批量导入数据格式错误（返回错误行明细） |
| 4008 | 证照已过期，不允许关联到工卡任务 |
| 4009 | 该人员该机型无有效证照 |

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-05-28 | 初稿，新建人员证照与资质管理 Spec |
| 1.1.0 | 2026-05-29 | 目录重组：人员资质提升为顶级 MRO 模块（第 9 个）。前端 `pages/mro/personnel/` 含 PersonnelList/LicenseManagement/PersonnelAlerts/PersonnelDetail；后端 `module/personnel/` 含 PersonnelLicenseController（从 module/system/ 迁移），API URL `/api/personnel/licenses` 不变 |
| 1.1.1 | 2026-05-29 | 前端目录扁平化：移除 `pages/mro/` 中间层，`pages/mro/personnel/` 调整为 `pages/personnel/`。仅前端目录调整，路由路径、API、后端结构均不变 |
