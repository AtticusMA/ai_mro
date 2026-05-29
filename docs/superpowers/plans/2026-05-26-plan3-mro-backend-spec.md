# Plan 3: MRO 业务 Spec 后端实现约束补充

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 8 个 MRO 业务 Spec 文件追加 `## B. 后端实现约束` 章节，包含完整 JSON Schema、Dubbo 接口签名、错误码。

**Architecture:** 每个 MRO 业务模块对应一个独立微服务（Dubbo 服务，独立 MySQL Schema），manage-web 作为 BFF 通过 StructuredTaskScope 调用 Dubbo 服务，用户上下文通过 RpcContext Attachments 传递。

**Tech Stack:** Java 21 Records/Sealed Classes/Virtual Threads, Dubbo 3, MyBatis-Plus, Spring Boot 3

**错误码分配（与 Plan 2 延续）：**
- MRO-001 健康监控：4200–4299
- MRO-002 AR 协作：4300–4399
- MRO-003 排故助手：4400–4499
- MRO-004 手册管理：4500–4599
- MRO-005 数字孪生机库：4600–4699
- MRO-006 工具/航材管理：4700–4799
- MRO-007 VR/AR 培训：4800–4899
- MRO-008 电子工卡：4900–4999

---

### Task 1: MRO-001 飞机健康管理与预测性维护

**Files:**
- Modify: `specs/mro/001-health-monitoring.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/health/aircraft — 机队健康状态列表

Request query: `?pageNum=1&pageSize=20&status=open&severity=critical`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "aircraftId": "B-1234",
        "registrationNo": "B-1234",
        "aircraftType": "B737-800",
        "overallHealth": "warning",
        "activeAlerts": 2,
        "activeFaults": 1,
        "lastDataAt": "2026-05-26T10:00:00Z"
      }
    ],
    "total": 50, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/health/aircraft/{id} — 单机健康详情

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "aircraftId": "B-1234",
    "aircraftType": "B737-800",
    "overallHealth": "warning",
    "latestDataAt": "2026-05-26T10:00:00Z",
    "activeFaults": [
      {
        "id": 1001,
        "faultCode": "ATA27-001",
        "severity": "major",
        "component": "副翼作动器",
        "detectedAt": "2026-05-26T08:30:00Z",
        "status": "open"
      }
    ],
    "activeAlerts": [
      {
        "id": 2001,
        "alertLevel": "orange",
        "message": "液压压力偏低趋势",
        "predictedFaultTime": "2026-05-28T00:00:00Z"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### GET /api/health/aircraft/{id}/predictions — 趋势预测报告

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 3001,
        "aircraftId": "B-1234",
        "modelVersion": "v2.1.0",
        "predictedAt": "2026-05-26T09:00:00Z",
        "result": {
          "faultProbability": 0.72,
          "predictedFaultTime": "2026-05-28T12:00:00Z",
          "faultLocation": "液压系统A",
          "confidence": 0.85
        }
      }
    ],
    "total": 10, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/health/alerts — 预警列表

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 2001,
        "aircraftId": "B-1234",
        "alertLevel": "orange",
        "message": "液压压力偏低趋势",
        "predictedFaultTime": "2026-05-28T00:00:00Z",
        "acknowledged": false,
        "createdAt": "2026-05-26T08:00:00Z"
      }
    ],
    "total": 5, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### PUT /api/health/alerts/{id}/acknowledge — 确认预警

Request body: `{}`
Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### POST /api/health/alert-rules — 创建预警规则

Request body:
```json
{
  "ruleName": "液压压力低预警",
  "aircraftType": "B737-800",
  "metricName": "hydraulic_pressure_a",
  "operator": "lt",
  "threshold": 2800.0,
  "alertLevel": "orange",
  "notifyUserIds": [101, 102]
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 5001}, "timestamp": 1748304000000}`

### B.2 Dubbo 服务接口

```java
public interface HealthDubboService {
    PageResult<AircraftHealthDTO> listAircraftHealth(HealthQueryParam param, UserContextDTO ctx);
    AircraftDetailDTO getAircraftDetail(String aircraftId);
    PageResult<FaultRecordDTO> listFaults(String aircraftId, FaultQueryParam param);
    PageResult<PredictionReportDTO> listPredictions(String aircraftId, PageParam param);
    PageResult<HealthAlertDTO> listAlerts(AlertQueryParam param, UserContextDTO ctx);
    void acknowledgeAlert(Long alertId, Long operatorId);
    Long createAlertRule(CreateAlertRuleCommand cmd);
    void updateAlertRule(UpdateAlertRuleCommand cmd);
    void deleteAlertRule(Long ruleId);
    HealthStatisticsDTO getStatistics(StatQueryParam param);
}

public record AircraftHealthDTO(
    String aircraftId, String aircraftType, String overallHealth,
    int activeAlerts, int activeFaults, Instant lastDataAt
) implements Serializable {}

public record AircraftDetailDTO(
    String aircraftId, String aircraftType, String overallHealth,
    Instant latestDataAt, List<FaultRecordDTO> activeFaults,
    List<HealthAlertDTO> activeAlerts
) implements Serializable {}

public record FaultRecordDTO(
    Long id, String faultCode, String severity, String component,
    Instant detectedAt, String status
) implements Serializable {}

public record HealthAlertDTO(
    Long id, String aircraftId, String alertLevel, String message,
    Instant predictedFaultTime, boolean acknowledged, Instant createdAt
) implements Serializable {}

public record PredictionReportDTO(
    Long id, String aircraftId, String modelVersion,
    Instant predictedAt, Map<String, Object> result
) implements Serializable {}

public record CreateAlertRuleCommand(
    String ruleName, String aircraftType, String metricName,
    String operator, Double threshold, String alertLevel,
    List<Long> notifyUserIds
) implements Serializable {}

public record HealthQueryParam(
    int pageNum, int pageSize, String status, String severity, String aircraftType
) implements Serializable {}

public record AlertQueryParam(
    int pageNum, int pageSize, String alertLevel, Boolean acknowledged, String aircraftId
) implements Serializable {}
```

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4200 | 飞机不存在 |
| 4201 | 预警规则不存在 |
| 4202 | 预警已确认，不可重复操作 |
| 4203 | 预警规则阈值配置无效 |
| 4204 | 统计查询时间范围超出限制（最大90天） |
```

- [ ] **Step 2: 更新 front-matter `version` 和 `updated`，追加 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`

Changelog 追加:
```
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
```

- [ ] **Step 3: 验证文件已正确修改**

```bash
grep -n "version:" specs/mro/001-health-monitoring.spec.md | head -3
grep -n "## B." specs/mro/001-health-monitoring.spec.md
```

---

### Task 2: MRO-002 AR智慧维修协作平台

**Files:**
- Modify: `specs/mro/002-ar-collaboration.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/ar/inspections — 巡检任务列表

Request query: `?pageNum=1&pageSize=20&status=in_progress`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "aircraftId": "B-1234",
        "inspectorId": 101,
        "inspectorName": "张三",
        "routeTemplate": "A320_LINE_CHECK_V2",
        "status": "in_progress",
        "startedAt": "2026-05-26T08:00:00Z",
        "completedAt": null
      }
    ],
    "total": 10, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/ar/inspections — 创建巡检任务

Request body:
```json
{
  "aircraftId": "B-1234",
  "routeTemplate": "A320_LINE_CHECK_V2"
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 1001}, "timestamp": 1748304000000}`

#### GET /api/ar/inspections/{id}/anomalies — 巡检异常记录

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 2001,
        "taskId": 1001,
        "anomalyType": "盖板未闭合",
        "confidence": 0.95,
        "snapshotUrl": "https://oss.example.com/snapshots/2001.jpg",
        "detectedAt": "2026-05-26T08:15:00Z"
      }
    ],
    "total": 3, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/ar/sessions — 发起远程协作会话

Request body:
```json
{
  "callerId": 101,
  "taskId": 1001,
  "expertId": 201
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 3001, "signalingToken": "xxx"}, "timestamp": 1748304000000}`

#### GET /api/ar/archives — 影像档案列表

Request query: `?pageNum=1&pageSize=20&taskId=1001`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 4001,
        "taskId": 1001,
        "sessionId": null,
        "fileUrl": "https://oss.example.com/videos/4001.mp4",
        "durationSeconds": 1800,
        "createdAt": "2026-05-26T10:00:00Z"
      }
    ],
    "total": 5, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

```java
public interface ArDubboService {
    PageResult<InspectionTaskDTO> listInspections(InspectionQueryParam param, UserContextDTO ctx);
    Long createInspection(CreateInspectionCommand cmd);
    void startInspection(Long taskId, Long operatorId);
    void completeInspection(Long taskId, Long operatorId);
    PageResult<AnomalyRecordDTO> listAnomalies(Long taskId, PageParam param);
    ArSessionDTO createSession(CreateSessionCommand cmd);
    void joinSession(Long sessionId, Long expertId);
    void endSession(Long sessionId, Long operatorId);
    void sendAnnotation(Long sessionId, AnnotationDTO annotation);
    PageResult<VideoArchiveDTO> listArchives(ArchiveQueryParam param, UserContextDTO ctx);
}

public record InspectionTaskDTO(
    Long id, String aircraftId, Long inspectorId, String inspectorName,
    String routeTemplate, String status, Instant startedAt, Instant completedAt
) implements Serializable {}

public record AnomalyRecordDTO(
    Long id, Long taskId, String anomalyType, BigDecimal confidence,
    String snapshotUrl, Instant detectedAt
) implements Serializable {}

public record ArSessionDTO(
    Long id, Long callerId, Long expertId, String status,
    String signalingToken, Instant createdAt
) implements Serializable {}

public record CreateInspectionCommand(
    String aircraftId, String routeTemplate, Long inspectorId
) implements Serializable {}

public record CreateSessionCommand(
    Long callerId, Long taskId, Long expertId
) implements Serializable {}

public record AnnotationDTO(
    Long sessionId, String annotationType, Map<String, Object> coordinates, String content
) implements Serializable {}

public record VideoArchiveDTO(
    Long id, Long taskId, Long sessionId, String fileUrl,
    int durationSeconds, Instant createdAt
) implements Serializable {}
```

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4300 | 巡检任务不存在 |
| 4301 | 巡检任务状态不允许该操作（如已完成任务无法再次开始） |
| 4302 | 远程协作会话不存在 |
| 4303 | 会话已结束，不可操作 |
| 4304 | 巡检路线模板不存在 |
| 4305 | 影像档案不存在 |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/002-ar-collaboration.spec.md
```

---

### Task 3: MRO-003 智能排故助手

**Files:**
- Modify: `specs/mro/003-troubleshooting-assistant.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/tshoot/knowledge-bases — 知识库列表

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "name": "B737NG 维修手册知识库",
        "aircraftType": "B737-800",
        "docCount": 156,
        "status": "ready",
        "createdAt": "2026-05-01T00:00:00Z"
      }
    ],
    "total": 7, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/tshoot/knowledge-bases — 创建知识库

Request body:
```json
{
  "name": "A320 排故手册知识库",
  "aircraftType": "A320"
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 1002}, "timestamp": 1748304000000}`

#### POST /api/tshoot/knowledge-bases/{id}/documents — 上传文档

Request body (multipart/form-data):
- `file`: 文档文件（PDF/Word/TXT）
- `title`: 文档标题
- `docType`: manual | bulletin | history | experience

Response: `{"code": 0, "msg": "ok", "data": {"docId": 5001, "vectorStatus": "pending"}, "timestamp": 1748304000000}`

#### POST /api/tshoot/query — 提交排故查询（异步）

Request body:
```json
{
  "inputType": "code",
  "inputContent": "27-001",
  "aircraftType": "B737-800",
  "kbIds": [1001]
}
```
Response: `{"code": 0, "msg": "ok", "data": {"queryId": 9001}, "timestamp": 1748304000000}`

#### GET /api/tshoot/query/{id}/result — 获取排故结果

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "queryId": 9001,
    "status": "completed",
    "report": {
      "id": 8001,
      "solution": "检查副翼位置传感器...",
      "references": [
        {"manualNo": "AMM-B737-27", "chapter": "27-10-00", "page": "201"}
      ],
      "flowchartUrl": "https://oss.example.com/flowcharts/8001.png",
      "confidence": 0.92,
      "generatedAt": "2026-05-26T09:05:00Z"
    }
  },
  "timestamp": 1748304000000
}
```

#### GET /api/tshoot/history/statistics — 故障统计分析

Request query: `?aircraftType=B737-800&startDate=2026-01-01&endDate=2026-05-26`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "totalFaults": 328,
    "byComponent": [
      {"component": "液压系统", "count": 45},
      {"component": "起落架", "count": 38}
    ],
    "byMonth": [
      {"month": "2026-01", "count": 52}
    ],
    "topReplacedParts": [
      {"partNo": "B737-HYD-001", "replaceCount": 12}
    ]
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

排故助手需调用 RAG 服务（rag-service:20882），通过 Dubbo 调用而非 HTTP 直连：

```java
public interface TshootDubboService {
    PageResult<KnowledgeBaseDTO> listKnowledgeBases(PageParam param);
    Long createKnowledgeBase(CreateKbCommand cmd);
    Long uploadDocument(Long kbId, UploadDocCommand cmd);
    void deleteDocument(Long kbId, Long docId);
    Long submitQuery(FaultQueryCommand cmd);
    TshootResultDTO getQueryResult(Long queryId);
    PageResult<RepairHistoryDTO> listHistory(HistoryQueryParam param);
    FaultStatisticsDTO getStatistics(StatQueryParam param);
    PageResult<TshootReportDTO> listMyReports(Long userId, PageParam param);
    TshootReportDTO getReport(Long reportId);
}

public record KnowledgeBaseDTO(
    Long id, String name, String aircraftType, int docCount,
    String status, Instant createdAt
) implements Serializable {}

public record CreateKbCommand(String name, String aircraftType) implements Serializable {}

public record UploadDocCommand(
    String title, String docType, String fileUrl, String contentHash
) implements Serializable {}

public record FaultQueryCommand(
    Long userId, String inputType, String inputContent,
    String aircraftType, List<Long> kbIds
) implements Serializable {}

public record TshootResultDTO(
    Long queryId, String status, TshootReportDTO report
) implements Serializable {}

public record TshootReportDTO(
    Long id, String solution, List<Map<String, String>> references,
    String flowchartUrl, BigDecimal confidence, Instant generatedAt
) implements Serializable {}

public record RepairHistoryDTO(
    Long id, String aircraftId, String faultCode, String repairAction,
    String componentReplaced, Instant repairedAt
) implements Serializable {}
```

**RAG 调用说明：** TshootService 在处理 `submitQuery` 时，通过 Dubbo 调用 `RagDubboService.retrieve(...)` 获取相关文档片段，再调用大模型生成方案，结果异步写入 `troubleshooting_report`。

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4400 | 知识库不存在 |
| 4401 | 知识库正在构建中，不可上传文档 |
| 4402 | 文档不存在 |
| 4403 | 查询记录不存在 |
| 4404 | 排故查询仍在处理中，请稍后获取结果 |
| 4405 | 知识库无可用文档，无法执行查询 |
| 4406 | 指定机型无授权知识库访问权限 |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/003-troubleshooting-assistant.spec.md
```

---

### Task 4: MRO-004 智慧维修手册管理

**Files:**
- Modify: `specs/mro/004-manual-management.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
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

Request body (multipart/form-data):
- `file`: 手册文件
- `title`: 手册标题
- `manualNo`: 手册编号（唯一）
- `aircraftType`: 适用机型
- `format`: PDF | XML | SGML

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

**翻译引擎调用：** ManualService 通过 Dubbo 调用 `RagDubboService.translate(...)` 执行翻译，任务异步处理，状态轮询通过 `getTranslationResult` 接口返回。

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
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/004-manual-management.spec.md
```

---

### Task 5: MRO-005 数字孪生机库管理

**Files:**
- Modify: `specs/mro/005-digital-twin-hangar.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/dtwin/hangars/{id}/workstations — 工位列表及状态

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 101,
        "hangarId": 1,
        "name": "C检工位A",
        "positionX": 15.50,
        "positionY": 0.00,
        "positionZ": 30.20,
        "status": "occupied",
        "currentAircraftId": "B-1234"
      }
    ],
    "total": 8, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/dtwin/plans — 生产计划列表

Request query: `?pageNum=1&pageSize=20&status=in_progress&hangarId=1`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 2001,
        "hangarId": 1,
        "aircraftId": "B-1234",
        "planType": "heavy",
        "scheduledStart": "2026-05-20T08:00:00Z",
        "scheduledEnd": "2026-06-10T18:00:00Z",
        "status": "in_progress"
      }
    ],
    "total": 3, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/dtwin/plans — 创建生产计划

Request body:
```json
{
  "hangarId": 1,
  "aircraftId": "B-1234",
  "planType": "heavy",
  "scheduledStart": "2026-05-20T08:00:00Z",
  "scheduledEnd": "2026-06-10T18:00:00Z"
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 2001}, "timestamp": 1748304000000}`

#### GET /api/dtwin/orders — 维修指令列表

Request query: `?pageNum=1&pageSize=20&planId=2001&status=executing`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 3001,
        "planId": 2001,
        "workstationId": 101,
        "workstationName": "C检工位A",
        "assigneeId": 101,
        "assigneeName": "王工程师",
        "description": "发动机拆卸检查",
        "progress": 45,
        "status": "executing"
      }
    ],
    "total": 12, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### PUT /api/dtwin/orders/{id}/progress — 更新指令进度

Request body:
```json
{
  "progress": 60,
  "status": "executing"
}
```
Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### GET /api/dtwin/analytics/workload — 工位负载分析

Request query: `?hangarId=1&startDate=2026-05-01&endDate=2026-05-26`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "workstationLoads": [
      {
        "workstationId": 101,
        "workstationName": "C检工位A",
        "utilizationRate": 0.87,
        "totalOrders": 15,
        "completedOrders": 13
      }
    ],
    "avgUtilization": 0.72
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

```java
public interface DtwinDubboService {
    PageResult<HangarDTO> listHangars(UserContextDTO ctx);
    HangarModelDTO getHangarModel(Long hangarId);
    PageResult<WorkstationDTO> listWorkstations(Long hangarId, PageParam param);
    PageResult<ProductionPlanDTO> listPlans(PlanQueryParam param, UserContextDTO ctx);
    Long createPlan(CreatePlanCommand cmd);
    void updatePlan(UpdatePlanCommand cmd);
    PageResult<MaintenanceOrderDTO> listOrders(OrderQueryParam param, UserContextDTO ctx);
    Long createOrder(CreateOrderCommand cmd);
    void updateOrderProgress(Long orderId, int progress, String status, Long operatorId);
    WorkloadAnalyticsDTO analyzeWorkload(AnalyticsParam param);
    EfficiencyAnalyticsDTO analyzeEfficiency(AnalyticsParam param);
}

public record HangarDTO(Long id, String name) implements Serializable {}

public record HangarModelDTO(Long id, String name, String modelUrl, String version) implements Serializable {}

public record WorkstationDTO(
    Long id, Long hangarId, String name, BigDecimal positionX,
    BigDecimal positionY, BigDecimal positionZ, String status,
    String currentAircraftId
) implements Serializable {}

public record ProductionPlanDTO(
    Long id, Long hangarId, String aircraftId, String planType,
    Instant scheduledStart, Instant scheduledEnd, String status
) implements Serializable {}

public record MaintenanceOrderDTO(
    Long id, Long planId, Long workstationId, String workstationName,
    Long assigneeId, String assigneeName, String description,
    int progress, String status
) implements Serializable {}

public record CreatePlanCommand(
    Long hangarId, String aircraftId, String planType,
    Instant scheduledStart, Instant scheduledEnd, Long createdBy
) implements Serializable {}

public record CreateOrderCommand(
    Long planId, Long workstationId, Long assigneeId,
    String description, Long createdBy
) implements Serializable {}

public record WorkloadAnalyticsDTO(
    List<WorkstationLoadDTO> workstationLoads, double avgUtilization
) implements Serializable {}

public record WorkstationLoadDTO(
    Long workstationId, String workstationName, double utilizationRate,
    int totalOrders, int completedOrders
) implements Serializable {}
```

**WebSocket 实时推送：** 工位/进度状态变更时，manage-web 通过 Dubbo 消费 dtwin-service 推送的事件（或由 dtwin-service 直接写 Redis Pub/Sub），manage-web 的 WebSocket Handler 订阅后推送给前端。

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4600 | 机库不存在 |
| 4601 | 工位不存在 |
| 4602 | 生产计划不存在 |
| 4603 | 维修指令不存在 |
| 4604 | 进度值无效（必须为 0-100 整数） |
| 4605 | 工位已被占用，无法分配该计划 |
| 4606 | 计划状态不允许修改（已完成计划不可编辑） |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/005-digital-twin-hangar.spec.md
```

---

### Task 6: MRO-006 智能工具间与航材管理

**Files:**
- Modify: `specs/mro/006-tool-material-management.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### POST /api/tool/borrow — 借出工具

Request body:
```json
{
  "userId": 101,
  "cabinetId": 1,
  "toolIds": [2001, 2002],
  "workcardId": 3001,
  "expectedReturnHours": 8
}
```
Response: `{"code": 0, "msg": "ok", "data": {"borrowRecordIds": [4001, 4002]}, "timestamp": 1748304000000}`

#### POST /api/tool/return — 归还工具

Request body:
```json
{
  "userId": 101,
  "cabinetId": 1,
  "rfidScanResult": ["RFID-001", "RFID-002"]
}
```
Response: `{"code": 0, "msg": "ok", "data": {"returnedCount": 2, "missingRfids": []}, "timestamp": 1748304000000}`

#### GET /api/tool/cabinets — 工具柜列表

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "C检工具柜-01",
        "location": "机库A区",
        "slotCount": 50,
        "availableSlots": 32,
        "temperature": 22.5,
        "humidity": 45.2,
        "onlineStatus": "online"
      }
    ],
    "total": 5, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/tool/cabinets/{id}/slots — 格口工具在位

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "cabinetId": 1,
    "slots": [
      {
        "slotNo": 1,
        "toolId": 2001,
        "toolName": "扭力扳手 1/2\"",
        "toolCode": "TW-001",
        "rfidTag": "RFID-001",
        "status": "in_cabinet"
      },
      {
        "slotNo": 2,
        "toolId": null,
        "status": "empty"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### GET /api/tool/alerts — 工具预警列表

Request query: `?pageNum=1&pageSize=20&alertType=overdue`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 5001,
        "toolId": 2001,
        "toolName": "扭力扳手 1/2\"",
        "alertType": "overdue",
        "borrowerId": 101,
        "borrowerName": "张三",
        "borrowTime": "2026-05-26T08:00:00Z",
        "expectedReturn": "2026-05-26T16:00:00Z",
        "overdueHours": 2.5
      }
    ],
    "total": 3, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/material/items — 航材列表

Request query: `?pageNum=1&pageSize=20&lowStock=true`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 6001,
        "partNo": "B737-SEAL-029",
        "name": "液压系统密封圈",
        "category": "密封件",
        "stockQty": 3,
        "minStock": 10,
        "location": "B区-架位12",
        "expiryDate": "2027-12-31",
        "belowMinStock": true
      }
    ],
    "total": 120, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/material/repair-orders — 提交航材送修单

Request body:
```json
{
  "materialId": 6001,
  "quantity": 2,
  "faultDescription": "密封性能下降",
  "vendorId": 301
}
```
Response: `{"code": 0, "msg": "ok", "data": {"orderId": 7001}, "timestamp": 1748304000000}`

### B.2 Dubbo 服务接口

```java
public interface ToolDubboService {
    BorrowResultDTO borrowTools(BorrowCommand cmd);
    ReturnResultDTO returnTools(ReturnCommand cmd);
    PageResult<ToolCabinetDTO> listCabinets(UserContextDTO ctx);
    List<SlotStatusDTO> getCabinetSlots(Long cabinetId);
    void triggerInventory(Long cabinetId);
    PageResult<ToolDTO> listTools(ToolQueryParam param);
    ToolLifecycleDTO getToolLifecycle(Long toolId);
    PageResult<BorrowRecordDTO> listBorrowRecords(BorrowRecordQueryParam param);
    PageResult<ToolAlertDTO> listAlerts(AlertQueryParam param);
}

public interface MaterialDubboService {
    PageResult<MaterialItemDTO> listMaterials(MaterialQueryParam param);
    Long createMaterial(CreateMaterialCommand cmd);
    void updateMaterial(UpdateMaterialCommand cmd);
    PageResult<MaterialAlertDTO> listRestockAlerts(UserContextDTO ctx);
    Long createRepairOrder(CreateRepairOrderCommand cmd);
    PageResult<RepairOrderDTO> listRepairOrders(PageParam param);
}

public record BorrowCommand(
    Long userId, Long cabinetId, List<Long> toolIds,
    Long workcardId, int expectedReturnHours
) implements Serializable {}

public record BorrowResultDTO(List<Long> borrowRecordIds) implements Serializable {}

public record ReturnCommand(
    Long userId, Long cabinetId, List<String> rfidScanResult
) implements Serializable {}

public record ReturnResultDTO(int returnedCount, List<String> missingRfids) implements Serializable {}

public record ToolCabinetDTO(
    Long id, String name, String location, int slotCount, int availableSlots,
    BigDecimal temperature, BigDecimal humidity, String onlineStatus
) implements Serializable {}

public record SlotStatusDTO(
    int slotNo, Long toolId, String toolName, String toolCode,
    String rfidTag, String status
) implements Serializable {}

public record ToolAlertDTO(
    Long id, Long toolId, String toolName, String alertType,
    Long borrowerId, String borrowerName, Instant borrowTime,
    Instant expectedReturn, double overdueHours
) implements Serializable {}

public record MaterialItemDTO(
    Long id, String partNo, String name, String category,
    int stockQty, int minStock, String location, LocalDate expiryDate,
    boolean belowMinStock
) implements Serializable {}

public record CreateRepairOrderCommand(
    Long materialId, int quantity, String faultDescription, Long vendorId, Long createdBy
) implements Serializable {}
```

**MQTT 集成：** 工具柜事件（开柜/关柜/盘点结果）通过 MQTT 上报到 `tool/cabinet/{id}/event` topic，由 tool-service 的 MQTT Consumer 处理后写入数据库并触发预警逻辑。

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4700 | 工具不存在 |
| 4701 | 工具已被借出，不可重复借出 |
| 4702 | 工具不在柜内，无法借出 |
| 4703 | 工具柜不存在 |
| 4704 | 工具柜离线，操作失败 |
| 4705 | 工具检定已过期，不允许借出 |
| 4706 | 航材不存在 |
| 4707 | 件号已存在（唯一约束冲突） |
| 4708 | 送修单不存在 |
| 4709 | 关联工卡所需工具未全部借出，不满足开工条件 |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/006-tool-material-management.spec.md
```

---

### Task 7: MRO-007 VR/AR沉浸式培训

**Files:**
- Modify: `specs/mro/007-vr-ar-training.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/training/scenarios — 培训场景列表

Request query: `?pageNum=1&pageSize=20&status=published&category=engine`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "name": "B737NG CFM56-7B 发动机拆装",
        "category": "engine",
        "difficulty": "intermediate",
        "modelUrl": "https://oss.example.com/models/1001.glb",
        "durationMinutes": 90,
        "status": "published"
      }
    ],
    "total": 15, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/training/scenarios — 创建培训场景

Request body:
```json
{
  "name": "B737NG CFM56-7B 发动机拆装",
  "category": "engine",
  "difficulty": "intermediate",
  "modelUrl": "https://oss.example.com/models/1001.glb",
  "durationMinutes": 90
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 1001}, "timestamp": 1748304000000}`

#### GET /api/training/trainees/{id}/profile — 学员能力档案

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "id": 2001,
    "userId": 101,
    "userName": "张三",
    "skillLevel": "mid",
    "totalTrainingHours": 128.5,
    "lastAssessmentDate": "2026-05-20",
    "skillRadar": {
      "lineCheck": 82.5,
      "engineRepair": 67.3,
      "emergencyHandling": 75.0,
      "toolUsage": 90.2
    },
    "recentSessions": [
      {
        "sessionId": 3001,
        "scenarioName": "发动机拆装",
        "score": 78.5,
        "completedAt": "2026-05-20T14:00:00Z"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### POST /api/training/sessions — 创建培训任务

Request body:
```json
{
  "scenarioId": 1001,
  "traineeIds": [101, 102],
  "mode": "vr",
  "scheduledAt": "2026-05-27T09:00:00Z"
}
```
Response: `{"code": 0, "msg": "ok", "data": {"sessionIds": [3001, 3002]}, "timestamp": 1748304000000}`

#### GET /api/training/assessments/{sessionId} — 评估结果

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "sessionId": 3001,
    "traineeId": 101,
    "overallScore": 78.5,
    "assessments": [
      {
        "id": 4001,
        "metricName": "拆装步骤正确性",
        "score": 82.0,
        "detail": {
          "correctSteps": 18,
          "totalSteps": 22,
          "errorSteps": ["步骤7：螺栓力矩未达标"]
        },
        "assessedAt": "2026-05-20T14:30:00Z"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### GET /api/training/reports/individual/{traineeId} — 个人技能考核报告

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "traineeId": 101,
    "traineeName": "张三",
    "reportPeriod": {"start": "2026-01-01", "end": "2026-05-26"},
    "totalSessions": 24,
    "totalHours": 128.5,
    "skillTrend": [
      {"month": "2026-01", "avgScore": 68.2},
      {"month": "2026-05", "avgScore": 78.5}
    ],
    "weakPoints": ["紧急故障处理", "发动机检查"],
    "recommendations": "建议加强发动机拆装场景训练频次"
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

```java
public interface TrainingDubboService {
    PageResult<TrainingScenarioDTO> listScenarios(ScenarioQueryParam param);
    Long createScenario(CreateScenarioCommand cmd);
    void updateScenario(UpdateScenarioCommand cmd);
    void publishScenario(Long scenarioId, Long operatorId);
    PageResult<TraineeDTO> listTrainees(UserContextDTO ctx);
    TraineeProfileDTO getTraineeProfile(Long traineeId);
    List<Long> createSessions(CreateSessionCommand cmd);
    PageResult<TrainingSessionDTO> listSessions(SessionQueryParam param);
    TrainingSessionDTO getSession(Long sessionId);
    List<SkillAssessmentDTO> getAssessments(Long sessionId);
    IndividualReportDTO generateIndividualReport(Long traineeId, LocalDate start, LocalDate end);
    OverviewReportDTO generateOverviewReport(UserContextDTO ctx, LocalDate start, LocalDate end);
}

public record TrainingScenarioDTO(
    Long id, String name, String category, String difficulty,
    String modelUrl, int durationMinutes, String status
) implements Serializable {}

public record TraineeProfileDTO(
    Long id, Long userId, String userName, String skillLevel,
    BigDecimal totalTrainingHours, LocalDate lastAssessmentDate,
    Map<String, Double> skillRadar, List<RecentSessionDTO> recentSessions
) implements Serializable {}

public record CreateScenarioCommand(
    String name, String category, String difficulty,
    String modelUrl, int durationMinutes, Long createdBy
) implements Serializable {}

public record CreateSessionCommand(
    Long scenarioId, List<Long> traineeIds, String mode,
    Instant scheduledAt, Long assignedBy
) implements Serializable {}

public record SkillAssessmentDTO(
    Long id, Long sessionId, String metricName, BigDecimal score,
    Map<String, Object> detail, Instant assessedAt
) implements Serializable {}

public record IndividualReportDTO(
    Long traineeId, String traineeName, LocalDate start, LocalDate end,
    int totalSessions, BigDecimal totalHours,
    List<Map<String, Object>> skillTrend, List<String> weakPoints,
    String recommendations
) implements Serializable {}
```

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4800 | 培训场景不存在 |
| 4801 | 场景状态不允许发布（需为 draft 状态） |
| 4802 | 学员档案不存在 |
| 4803 | 培训会话不存在 |
| 4804 | 培训会话仍在进行中，无法查看最终评估 |
| 4805 | 协同培训人数超出上限（最大 10 人） |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/007-vr-ar-training.spec.md
```

---

### Task 8: MRO-008 无纸化电子工卡

**Files:**
- Modify: `specs/mro/008-paperless-workcard.spec.md`

- [ ] **Step 1: 在 Changelog 前插入 `## B. 后端实现约束` 内容**

```
## B. 后端实现约束

### B.1 JSON Schema

#### GET /api/workcards — 工卡列表

Request query: `?pageNum=1&pageSize=20&status=in_progress&aircraftId=B-1234`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "cardNo": "WC-2026-05-001",
        "title": "B-1234 C检 27章液压系统检查",
        "cardType": "heavy",
        "aircraftId": "B-1234",
        "priority": "normal",
        "status": "in_progress",
        "createdByName": "李编制员",
        "dueDate": "2026-06-10T18:00:00Z",
        "completionRate": 45
      }
    ],
    "total": 28, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/workcards — 创建工卡

Request body:
```json
{
  "title": "B-1234 C检 27章液压系统检查",
  "cardType": "heavy",
  "aircraftId": "B-1234",
  "priority": "normal",
  "dueDate": "2026-06-10T18:00:00Z",
  "steps": [
    {
      "stepNo": 1,
      "description": "断开液压系统A电源",
      "requiredTools": [{"toolCode": "TW-001", "toolName": "扭力扳手"}],
      "requiredMaterials": [],
      "manualRef": "AMM-B737-47:29-10-01"
    }
  ]
}
```
Response: `{"code": 0, "msg": "ok", "data": {"id": 1001}, "timestamp": 1748304000000}`

#### GET /api/workcards/{id} — 工卡详情（含步骤）

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "id": 1001,
    "cardNo": "WC-2026-05-001",
    "title": "B-1234 C检 27章液压系统检查",
    "cardType": "heavy",
    "aircraftId": "B-1234",
    "priority": "normal",
    "status": "in_progress",
    "steps": [
      {
        "id": 2001,
        "stepNo": 1,
        "description": "断开液压系统A电源",
        "requiredTools": [{"toolCode": "TW-001", "toolName": "扭力扳手"}],
        "requiredMaterials": [],
        "manualRef": "AMM-B737-47:29-10-01",
        "status": "completed",
        "completedByName": "王工程师",
        "completedAt": "2026-05-26T09:30:00Z"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### POST /api/workcards/{id}/sign — 电子签署

Request body:
```json
{
  "stepId": 2001,
  "signatureType": "execute",
  "digitalSignature": "BASE64_ENCODED_SIGNATURE"
}
```
Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "signatureId": 3001,
    "blockchainHash": "0xabc123...def456",
    "signedAt": "2026-05-26T09:31:00Z"
  },
  "timestamp": 1748304000000
}
```

#### GET /api/workcards/{id}/blockchain-verify — 区块链验证

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "workcardId": 1001,
    "verified": true,
    "signatures": [
      {
        "signatureId": 3001,
        "signerName": "王工程师",
        "signatureType": "execute",
        "blockchainHash": "0xabc123...def456",
        "onChainAt": "2026-05-26T09:31:05Z",
        "tampered": false
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### GET /api/qualifications/match — 资质与任务匹配检查

Request query: `?workcardId=1001&aircraftType=B737-800&cardType=heavy`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "qualifiedPersonnel": [
      {
        "userId": 101,
        "userName": "王工程师",
        "qualificationType": "维修许可证",
        "level": "B",
        "validTo": "2027-12-31"
      }
    ],
    "total": 5
  },
  "timestamp": 1748304000000
}
```

#### GET /api/workcards/alerts — 工卡到期预警

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "workcardId": 1001,
        "cardNo": "WC-2026-05-001",
        "title": "B-1234 C检 27章液压系统检查",
        "dueDate": "2026-05-27T18:00:00Z",
        "hoursUntilDue": 20.5,
        "completionRate": 45,
        "assignees": ["王工程师", "李工程师"]
      }
    ],
    "total": 2, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

### B.2 Dubbo 服务接口

```java
public interface WorkcardDubboService {
    PageResult<WorkcardDTO> listWorkcards(WorkcardQueryParam param, UserContextDTO ctx);
    Long createWorkcard(CreateWorkcardCommand cmd);
    WorkcardDetailDTO getWorkcard(Long workcardId);
    void updateWorkcard(UpdateWorkcardCommand cmd);
    void submitForApproval(Long workcardId, Long operatorId);
    void approveWorkcard(Long workcardId, String action, String comment, Long operatorId);
    void issueWorkcard(Long workcardId, Long operatorId);
    void completeStep(Long workcardId, Long stepId, Long operatorId);
    SignatureResultDTO signWorkcard(SignWorkcardCommand cmd);
    List<SignatureDTO> getSignatures(Long workcardId);
    BlockchainVerifyDTO verifyBlockchain(Long workcardId);
    WorkcardProgressDTO getProgress(UserContextDTO ctx);
    PageResult<WorkcardAlertDTO> getAlerts(PageParam param, UserContextDTO ctx);
    PageResult<QualificationDTO> matchQualifications(QualificationMatchParam param);
}

public record WorkcardDTO(
    Long id, String cardNo, String title, String cardType, String aircraftId,
    String priority, String status, String createdByName,
    Instant dueDate, int completionRate
) implements Serializable {}

public record WorkcardDetailDTO(
    Long id, String cardNo, String title, String cardType, String aircraftId,
    String priority, String status, List<WorkcardStepDTO> steps
) implements Serializable {}

public record WorkcardStepDTO(
    Long id, int stepNo, String description,
    List<Map<String, String>> requiredTools, List<Map<String, String>> requiredMaterials,
    String manualRef, String status, String completedByName, Instant completedAt
) implements Serializable {}

public record SignWorkcardCommand(
    Long workcardId, Long stepId, String signatureType,
    String digitalSignature, Long signerId
) implements Serializable {}

public record SignatureResultDTO(
    Long signatureId, String blockchainHash, Instant signedAt
) implements Serializable {}

public record BlockchainVerifyDTO(
    Long workcardId, boolean verified, List<SignatureVerifyDTO> signatures
) implements Serializable {}

public record SignatureVerifyDTO(
    Long signatureId, String signerName, String signatureType,
    String blockchainHash, Instant onChainAt, boolean tampered
) implements Serializable {}

public record CreateWorkcardCommand(
    String title, String cardType, String aircraftId, String priority,
    Instant dueDate, List<CreateStepCommand> steps, Long createdBy
) implements Serializable {}

public record CreateStepCommand(
    int stepNo, String description, List<Map<String, String>> requiredTools,
    List<Map<String, String>> requiredMaterials, String manualRef
) implements Serializable {}

public record QualificationMatchParam(
    Long workcardId, String aircraftType, String cardType
) implements Serializable {}

public record QualificationDTO(
    Long userId, String userName, String qualificationType,
    String level, LocalDate validTo
) implements Serializable {}
```

**区块链集成说明：** 电子签署完成后，workcard-service 将签名哈希通过 Kafka 事件异步发送到区块链适配器服务（首期可用 SM3 哈希存储在 Redis/DB 中模拟，待 Hyperledger Fabric 部署就绪后切换）。`blockchainHash` 字段在正式上链前返回本地计算的 SM3 哈希。

### B.3 错误码

| 错误码 | 含义 |
|--------|------|
| 4900 | 工卡不存在 |
| 4901 | 工卡编号已存在（唯一约束冲突） |
| 4902 | 工卡状态不允许该操作（如已完成工卡不可修改） |
| 4903 | 步骤不存在 |
| 4904 | 步骤状态不允许签署（必须先完成步骤） |
| 4905 | 签署人无该工卡/机型资质 |
| 4906 | 数字签名验证失败（格式或密钥错误） |
| 4907 | 工卡关联工具未全部借出，不满足开工条件 |
| 4908 | 审批人无工卡审批权限 |
```

- [ ] **Step 2: 更新 front-matter 和 Changelog**

front-matter: `version: 1.0.0`, `updated: 2026-05-26`
Changelog: `| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |`

- [ ] **Step 3: 验证**

```bash
grep -n "version:\|## B." specs/mro/008-paperless-workcard.spec.md
```
