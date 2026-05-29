---
id: MRO-004
title: 智慧维修手册管理
domain: mro
status: approved
owner: '@product'
version: 1.0.0
created: 2026-05-25
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 智慧维修手册管理

## 1. 背景与目标

实现从手册深度解析、智能翻译、客户化修订、发布，直到多终端访问浏览的完整闭环管理。基于超过50万句高质量航空维修领域双语语料，通过大模型微调技术打造专为飞机维修领域定制的智能翻译引擎。

对齐 Charter 目标：效率优化（快速检索手册内容，告别人工翻页查找）、人员赋能（智能翻译降低英文手册理解门槛）。

## 2. 范围

### In Scope
- 多格式手册深度解析（PDF/XML/SGML）
- 航空维修领域中英文智能翻译
- 客户化手册修订与版本管理
- PC 端和移动端多终端访问
- 与工卡系统和健康管理平台接口对接
- 手册全文搜索与语义检索

### Out of Scope
- 原厂手册编写（仅做接收、解析、翻译、客户化修订）
- 适航性审批流程
- 手册印刷与纸质分发

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望在手机/平板上快速搜索维修手册内容，以便在现场即时查阅操作步骤。
- 作为技术支援工程师，我希望获取中英文对照的手册翻译，以便准确理解原厂英文手册内容。
- 作为机库管理人员，我希望管理手册版本修订历史，以便确保一线人员使用的始终是最新有效版本。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持多格式维修手册（PDF、XML、SGML等）的深度解析与结构化处理 | Given 用户上传PDF/XML/SGML手册 When 触发解析 Then 系统完成结构化处理，可按章节/段落检索 |
| FR-2 | 具备航空维修领域专用的中英文智能翻译能力，翻译准确率不低于95% | Given 英文手册已解析 When 用户提交翻译任务 Then 系统返回中文翻译，专业术语准确率 ≥ 95% |
| FR-3 | 支持客户化手册修订和版本管理，修订历史可追溯 | Given 手册已导入 When 管理员进行客户化修订 Then 系统保存新版本并记录修订摘要和修订人 |
| FR-4 | 支持PC端和移动端（iOS/Android）多终端灵活访问 | Given 手册已发布 When 用户从PC/手机/平板访问 Then 均可正常浏览，移动端适配良好 |
| FR-5 | 实现与工卡管理系统和健康管理平台的接口对接，支持手册跳转关联 | Given 工卡引用手册章节 When 用户点击手册链接 Then 直接跳转到对应手册内容页 |
| FR-6 | 支持手册内容的快速检索和全文搜索 | Given 手册已解析入库 When 用户输入关键词 Then 2秒内返回相关章节列表（高亮匹配） |

## 5. 非功能需求

- 性能：搜索响应时间 ≤ 2秒；翻译任务处理 ≤ 5分钟/篇
- 安全：手册数据加密存储；按机型权限访问控制
- 可用性：多端自适应；移动端支持离线缓存
- 翻译质量：航空术语翻译准确率 ≥ 95%

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| manual_document.id | bigint | Y | PK | 手册文档 ID |
| manual_document.title | varchar(256) | Y | | 手册标题 |
| manual_document.manual_no | varchar(64) | Y | UK | 手册编号 |
| manual_document.aircraft_type | varchar(32) | Y | | 适用机型 |
| manual_document.format | enum | Y | PDF/XML/SGML | 原始格式 |
| manual_document.file_url | varchar(512) | Y | | 原始文件存储地址 |
| manual_document.parsed_status | enum | Y | pending/parsed/failed | 解析状态 |
| manual_document.uploaded_at | timestamp | Y | | 上传时间 |
| manual_version.id | bigint | Y | PK | 版本记录 ID |
| manual_version.document_id | bigint | Y | FK | 关联手册 |
| manual_version.version_no | varchar(32) | Y | | 版本号 |
| manual_version.change_summary | text | Y | | 修订摘要 |
| manual_version.effective_date | date | Y | | 生效日期 |
| manual_version.revised_by | bigint | Y | FK | 修订人 |
| manual_version.created_at | timestamp | Y | | 创建时间 |
| translation_task.id | bigint | Y | PK | 翻译任务 ID |
| translation_task.document_id | bigint | Y | FK | 关联手册 |
| translation_task.source_lang | varchar(8) | Y | | 源语言 (en) |
| translation_task.target_lang | varchar(8) | Y | | 目标语言 (zh) |
| translation_task.status | enum | Y | pending/processing/completed/failed | 状态 |
| translation_task.accuracy_score | decimal(5,4) | N | | 翻译质量评分 |
| translation_task.result_url | varchar(512) | N | | 翻译结果地址 |
| translation_task.created_at | timestamp | Y | | 创建时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/manuals | manual:search | 获取手册列表（支持分页/筛选） |
| POST | /api/manuals | manual:upload | 上传新手册文档 |
| GET | /api/manuals/{id} | manual:search | 获取手册详情 |
| POST | /api/manuals/{id}/parse | manual:upload | 触发手册深度解析 |
| GET | /api/manuals/{id}/versions | manual:search | 获取手册版本历史 |
| POST | /api/manuals/{id}/versions | manual:edit | 创建新版本（客户化修订） |
| POST | /api/manuals/{id}/translate | manual:translate | 提交翻译任务 |
| GET | /api/manuals/translations/{taskId} | manual:translate | 获取翻译结果 |
| GET | /api/manuals/search | manual:search | 全文搜索（关键词+语义） |
| POST | /api/manuals/{id}/publish | manual:publish | 发布手册版本 |
| DELETE | /api/manuals/{id} | manual:upload | 删除手册 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `manual:search` — 搜索和查阅手册
  - `manual:upload` — 上传和删除手册文档
  - `manual:edit` — 客户化修订
  - `manual:translate` — 提交翻译任务
  - `manual:publish` — 发布正式版本
- 数据权限：按机型授权访问

## 9. 验收标准

- [ ] PDF/XML/SGML 手册上传后可正常解析为结构化内容
- [ ] 翻译准确率 ≥ 95%（航空术语）
- [ ] 版本管理可追溯修订历史
- [ ] PC 端和移动端均可正常访问和搜索
- [ ] 全文搜索响应时间 ≤ 2秒
- [ ] 与工卡系统的手册跳转链接正常
- [ ] 所有接口通过权限验证

## 10. 未决问题

- [ ] SGML 格式手册解析器的兼容性范围需确认（不同厂商 SGML DTD 差异大）
- [ ] 翻译引擎是调用统一大模型还是独立部署专用翻译模型
- [ ] 移动端离线缓存策略（手册体积大，需确认离线范围）

## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/manuals — 手册列表

Request query: `?pageNum=1&pageSize=20&aircraftType=B737-800&parsedStatus=parsed`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "title": "B737NG AMM 修订版本 47",
        "manualNo": "AMM-B737-47",
        "aircraftType": "B737-800",
        "format": "PDF",
        "parsedStatus": "parsed",
        "uploadedAt": "2026-05-01T00:00:00Z"
      }
    ],
    "total": 23, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/manuals — 上传手册文档

Request body (multipart/form-data): `file`、`title`、`manualNo`（唯一）、`aircraftType`、`format`（PDF/XML/SGML）

Response: `{"code": 0, "msg": "ok", "data": {"id": 1002}, "timestamp": 1748304000000}`

#### POST /api/manuals/{id}/versions — 创建新版本（客户化修订）

Request body:
```json
{
  "versionNo": "Rev.48",
  "changeSummary": "更新液压系统维修程序第3章",
  "effectiveDate": "2026-06-01"
}
```

Response: `{"code": 0, "msg": "ok", "data": {"versionId": 2001}, "timestamp": 1748304000000}`

#### GET /api/manuals/{id}/versions — 版本历史

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 2001,
        "documentId": 1001,
        "versionNo": "Rev.48",
        "changeSummary": "更新液压系统维修程序第3章",
        "effectiveDate": "2026-06-01",
        "revisedByName": "李工程师",
        "createdAt": "2026-05-26T00:00:00Z"
      }
    ],
    "total": 48, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/manuals/{id}/translate — 提交翻译任务

Request body:
```json
{
  "sourceLang": "en",
  "targetLang": "zh"
}
```

Response: `{"code": 0, "msg": "ok", "data": {"taskId": 3001}, "timestamp": 1748304000000}`

#### GET /api/manuals/translations/{taskId} — 获取翻译结果

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "taskId": 3001,
    "status": "completed",
    "accuracyScore": 0.9621,
    "resultUrl": "https://oss.example.com/translations/3001.pdf"
  },
  "timestamp": 1748304000000
}
```

#### GET /api/manuals/search — 全文搜索

Request query: `?q=液压泵拆装&aircraftType=B737-800&pageNum=1&pageSize=20`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "documentId": 1001,
        "manualNo": "AMM-B737-47",
        "chapterRef": "29-10-01",
        "highlight": "...拆卸<em>液压泵</em>时需断开电源...",
        "score": 0.95
      }
    ],
    "total": 8, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

翻译任务通过 Dubbo 调用 `RagDubboService.translate(...)` 执行，异步处理，状态轮询通过 `getTranslationResult` 接口返回。

```java
public interface ManualDubboService {
    PageResult<ManualDocDTO> listManuals(ManualQueryParam param);
    ManualDocDTO getManualById(Long id);
    Long createManual(CreateManualCommand cmd);
    void triggerParse(Long id);
    PageResult<ManualVersionDTO> listVersions(Long documentId, PageParam param);
    Long createVersion(Long documentId, CreateVersionCommand cmd);
    Long submitTranslation(Long documentId, String sourceLang, String targetLang);
    TranslationTaskDTO getTranslationResult(Long taskId);
    PageResult<ManualSearchResultDTO> searchManuals(ManualSearchParam param);
    void publishManual(Long id, Long operatorId);
    void deleteManual(Long id, Long operatorId);
}

public record ManualDocDTO(
    Long id, String title, String manualNo, String aircraftType,
    String format, String parsedStatus, Instant uploadedAt
) implements Serializable {}

public record CreateManualCommand(
    String title, String manualNo, String aircraftType,
    String format, String fileUrl, Long uploaderId
) implements Serializable {}

public record ManualVersionDTO(
    Long id, Long documentId, String versionNo, String changeSummary,
    LocalDate effectiveDate, String revisedByName, Instant createdAt
) implements Serializable {}

public record CreateVersionCommand(
    String versionNo, String changeSummary, LocalDate effectiveDate, Long revisedBy
) implements Serializable {}

public record TranslationTaskDTO(
    Long taskId, String status, BigDecimal accuracyScore, String resultUrl
) implements Serializable {}

public record ManualSearchResultDTO(
    Long documentId, String manualNo, String chapterRef,
    String highlight, double score
) implements Serializable {}

public record ManualSearchParam(
    String query, String aircraftType, int pageNum, int pageSize
) implements Serializable {}
```

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4500 | 手册文档不存在 |
| 4501 | 手册编号已存在（唯一约束冲突） |
| 4502 | 手册尚未解析，无法创建版本 |
| 4503 | 翻译任务不存在 |
| 4504 | 手册已发布，不允许删除 |
| 4505 | 指定机型无访问权限 |
| 4506 | 文件格式不支持（仅支持 PDF/XML/SGML） |

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
