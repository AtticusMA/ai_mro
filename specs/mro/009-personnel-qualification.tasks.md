---
id: MRO-009
plan: mro/009-personnel-qualification.plan.md
created: 2026-05-28
updated: 2026-05-28
---

# Tasks: 人员证照与资质管理

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | DB 迁移：新建 personnel_license 表（V009_01） | @dev | - | Flyway migrate 成功 + Code Review | todo |
| T-002 | PersonnelLicense 实体 + PersonnelLicenseMapper（含 countTotalPersonnel / statsByAircraftType 查询） | @dev | T-001 | 实体映射单测通过 + Code Review | todo |
| T-003 | 11 个 DTO Records（PersonnelLicenseDTO / CreateLicenseCommand / UpdateLicenseCommand / LicenseQueryParam / LicenseAlertDTO / LicenseAlertQueryParam / LicenseStatsDTO / AircraftTypeLicenseStatDTO / ImportLicenseCommand / ImportResultDTO / ImportErrorRow） | @dev | T-002 | DTO 字段与 Spec 数据契约完全对齐 + Code Review | todo |
| T-004 | PersonnelLicenseService：createLicense / getLicense / updateLicense / deleteLicense / uploadAttachment / listLicenses / listAlerts / getStats / checkQualification / listValidLicensesByUser | @dev | T-003 | 7 个 Service 单测通过（含状态计算边界、4001/4002 错误码、checkQualification 正反场景）+ Code Review | todo |
| T-005 | LicenseAlertScheduler：@Scheduled 每日 08:00 扫描有效期 ≤ 30 天证照并推送 warning/urgent 分级预警 | @dev | T-004 | 2 个调度单测通过（30天warning / 7天urgent 边界）+ Code Review | todo |
| T-006 | LicenseImportService：EasyExcel 解析 + 逐行 createLicense + 错误行收集（ImportResultDTO） | @dev | T-004 | 混合行（正常+错误）导入单测通过 + Code Review | todo |
| T-007 | PersonnelLicenseDubboService 接口定义 + PersonnelLicenseDubboServiceImpl 实现（11 个方法） | @dev | T-004, T-006 | Dubbo 接口集成测试通过 + Code Review | todo |
| T-008 | manage-web PersonnelLicenseController：9 个 REST 端点（CRUD + 附件上传 + 预警列表 + 统计 + 批量导入）+ MockMvc 4 场景测试 | @dev | T-007 | MockMvc 全部通过 + personnel:view / personnel:manage 权限验证正确 + 附件格式/大小校验通过 + Code Review | todo |
| T-009 | 前端 Mock 同步：/api/personnel/licenses/* 9 个端点（含 stats / alerts / import） | @dev | T-008 | VITE_USE_MOCK=true 页面可正常调用 + Code Review | todo |
| T-010 | 与 MRO-008 集成验证：workcard-service 通过 Dubbo 调用 checkQualification / listValidLicensesByUser，工卡资质匹配结果正确 | @dev | T-007 | 联动集成测试通过 + Code Review | todo |
| T-011 | 性能测试：证照列表查询 ≤ 2s；资质匹配校验 ≤ 500ms | @dev | T-008 | 性能指标达标 + 测试报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
