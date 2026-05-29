---
id: MRO-003
title: 智能排故助手
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

# Spec: 智能排故助手

## 1. 背景与目标

围绕民航维修场景构建全链路智能排故体系，以"准、快、可追溯"为硬指标，通过数据底座、检索能力、工作流编排、结果输出四大核心环节，实现从故障查询到排故建议的一站式输出。

对齐 Charter 目标：效率优化（将故障响应时间从"小时级"压缩至"分钟级"）、人员赋能（降低新手学习门槛，几秒钟获取精准排故方案）。

## 2. 范围

### In Scope
- 结构化维修记录与非结构化知识整合
- 7个以上专属知识库（覆盖主要机型）
- RAG（检索增强生成）关键词+语义双重检索
- 故障精准匹配与排故方案生成
- 排故方案可追溯性（引用来源标注）
- 机队历史故障数据与部件更换统计
- 多种输入方式（代码/语音/自然语言）

### Out of Scope
- 维修方案的自动执行（仍需人工确认）
- 适航性签署（属于工卡系统职责）
- 航材采购决策

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望输入故障代码或描述后快速获取排故方案，以便缩短排故时间。
- 作为技术支援工程师，我希望查看排故方案的引用来源和历史案例，以便验证方案的可靠性。
- 作为培训部门，我希望通过排故助手积累的知识库辅助新员工学习，以便缩短培训周期。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 整合结构化维修记录和非结构化知识（排故手册、工程文件、通告） | Given 多源数据已导入 When 知识库构建完成 Then 系统可检索结构化记录和非结构化文档 |
| FR-2 | 构建7个以上专属知识库，覆盖主要机型的技术文档 | Given 技术文档已准备 When 管理员创建知识库并上传文档 Then 知识库状态变为ready且文档已向量化 |
| FR-3 | 采用RAG技术，支持关键词和语义双重检索 | Given 知识库已就绪 When 用户输入查询 Then 系统同时执行关键词匹配和语义相似度检索，返回综合排序结果 |
| FR-4 | 在几分钟内完成故障精准匹配，输出带手册编号、流程图和中文翻译的排故方案 | Given 用户提交故障查询 When RAG pipeline处理完成 Then 3分钟内返回包含引用手册编号和排故流程图的方案 |
| FR-5 | 支持排故方案的可追溯性，每个结论应标注引用来源 | Given 排故报告已生成 When 用户查看报告 Then 每条建议旁显示引用来源（手册编号/章节/页码） |
| FR-6 | 整合机队历史故障数据和部件更换统计，为决策提供参考 | Given 历史数据已积累 When 用户查看统计 Then 展示按机型/部件/时间段的故障频次和更换统计 |
| FR-7 | 支持多种输入方式（代码录入、语音输入、自然语言描述） | Given 用户在排故界面 When 选择任一输入方式并提交 Then 系统均能正确解析并执行查询 |

## 5. 非功能需求

- 性能：智能排故方案生成时间 ≤ 3分钟；数据查询响应时间 ≤ 2秒
- 安全：知识库数据加密存储；按机型授权访问控制
- 可靠性：RAG 检索召回率 ≥ 90%
- 可扩展：新机型知识库可快速接入

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| knowledge_base.id | bigint | Y | PK | 知识库 ID |
| knowledge_base.name | varchar(128) | Y | | 知识库名称 |
| knowledge_base.aircraft_type | varchar(32) | Y | | 适用机型 |
| knowledge_base.doc_count | int | Y | | 文档数量 |
| knowledge_base.status | enum | Y | building/ready/updating | 状态 |
| knowledge_base.created_at | timestamp | Y | | 创建时间 |
| knowledge_doc.id | bigint | Y | PK | 文档 ID |
| knowledge_doc.kb_id | bigint | Y | FK | 所属知识库 |
| knowledge_doc.title | varchar(256) | Y | | 文档标题 |
| knowledge_doc.doc_type | enum | Y | manual/bulletin/history/experience | 文档类型 |
| knowledge_doc.content_hash | varchar(64) | Y | | 内容哈希 |
| knowledge_doc.vector_status | enum | Y | pending/indexed | 向量化状态 |
| fault_query.id | bigint | Y | PK | 查询记录 ID |
| fault_query.user_id | bigint | Y | FK | 查询人 |
| fault_query.input_type | enum | Y | code/voice/text | 输入方式 |
| fault_query.input_content | text | Y | | 输入内容 |
| fault_query.aircraft_type | varchar(32) | N | | 机型（用于缩小范围） |
| fault_query.queried_at | timestamp | Y | | 查询时间 |
| troubleshooting_report.id | bigint | Y | PK | 排故报告 ID |
| troubleshooting_report.query_id | bigint | Y | FK | 关联查询 |
| troubleshooting_report.solution | text | Y | | 排故方案正文 |
| troubleshooting_report.references | jsonb | Y | | 引用来源列表（手册编号/章节/页码） |
| troubleshooting_report.flowchart_url | varchar(512) | N | | 排故流程图地址 |
| troubleshooting_report.confidence | decimal(5,4) | Y | 0~1 | 方案置信度 |
| troubleshooting_report.generated_at | timestamp | Y | | 生成时间 |
| repair_history.id | bigint | Y | PK | 历史维修记录 ID |
| repair_history.aircraft_id | varchar(32) | Y | | 飞机注册号 |
| repair_history.fault_code | varchar(64) | Y | | 故障代码 |
| repair_history.repair_action | text | Y | | 处理措施 |
| repair_history.component_replaced | varchar(128) | N | | 更换部件 |
| repair_history.repaired_at | timestamp | Y | | 维修时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/tshoot/knowledge-bases | tshoot:manage_kb | 获取知识库列表 |
| POST | /api/tshoot/knowledge-bases | tshoot:manage_kb | 创建知识库 |
| POST | /api/tshoot/knowledge-bases/{id}/documents | tshoot:manage_kb | 上传文档到知识库 |
| DELETE | /api/tshoot/knowledge-bases/{id}/documents/{docId} | tshoot:manage_kb | 删除知识库文档 |
| POST | /api/tshoot/query | tshoot:query | 提交排故查询（异步） |
| GET | /api/tshoot/query/{id}/result | tshoot:query | 获取排故结果 |
| GET | /api/tshoot/history | tshoot:history | 查询历史维修记录 |
| GET | /api/tshoot/history/statistics | tshoot:history | 故障统计分析 |
| GET | /api/tshoot/reports | tshoot:query | 获取我的排故报告列表 |
| GET | /api/tshoot/reports/{id} | tshoot:query | 获取排故报告详情 |
| GET | /api/tshoot/export/{id} | tshoot:export | 导出排故报告 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `tshoot:query` — 提交排故查询、查看报告
  - `tshoot:manage_kb` — 管理知识库（创建/上传/删除文档）
  - `tshoot:history` — 查看历史维修记录与统计
  - `tshoot:export` — 导出排故报告
- 数据权限：知识库按机型授权访问

## 9. 验收标准

- [ ] 知识库创建与文档上传、向量化流程正常
- [ ] 排故方案生成时间 ≤ 3分钟
- [ ] 方案中包含引用来源标注（手册编号/章节/页码）
- [ ] 关键词+语义双重检索均返回相关结果
- [ ] 多输入方式（代码/语音/文本）均可正常提交查询
- [ ] 历史故障统计查询功能正常
- [ ] 所有接口通过权限验证

## 10. 未决问题

- [ ] 民航领域大模型的部署方式（云端 vs 边缘）需根据数据安全策略确定
- [ ] 知识库初始语料的采集范围和数量目标待定
- [ ] 排故方案生成时间 ≤ 3分钟的目标在复杂故障场景下是否可达需验证

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

Request body (multipart/form-data): `file`（文档文件）、`title`（标题）、`docType`（manual/bulletin/history/experience）

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

排故助手通过 Dubbo 调用 `RagDubboService.retrieve(...)` 获取相关文档片段，再调用大模型生成方案，结果异步写入 `troubleshooting_report`。

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
