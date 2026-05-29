---
id: PLAT-003
title: RAG 集成规格
domain: platform
status: approved
owner: '@arch'
version: 1.0.0
created: 2026-05-26
updated: 2026-05-26
charter: CHARTER.md
supersedes: []
depends-on: [PLAT-001, MRO-003, MRO-004]
---

# Spec: RAG 集成规格

## 1. 背景与目标

rag-service 是平台唯一的 AI 编排层，封装对 RAGFlow 的所有 HTTP 调用，并通过 Dubbo 对业务服务暴露统一的 `RagDubboService` 接口。业务服务（fault-diagnosis-service、maintenance-manual-service 等）**严禁**直接调用 RAGFlow HTTP API，必须经由 rag-service。

对齐 Charter 目标：效率优化（语义检索替代关键字翻页）、人员赋能（智能翻译降低英文手册门槛）、安全提升（AI 推理链路集中审计）。

## 2. 范围

### In Scope
- rag-service 对外暴露的 Dubbo 接口定义（`RagDubboService`）
- RAGFlow HTTP API 调用封装（知识库管理、文档上传、检索、翻译）
- rag-service 内部数据库 schema（`mro_rag`）存储 KB 元数据、任务状态
- 错误码段 5900-5999
- 异步任务（文档解析、翻译）的状态机与回调机制
- 向量化与检索参数规范

### Out of Scope
- RAGFlow 本身的部署运维（由 Ops 负责）
- 业务层的 Prompt 工程（由各业务服务自行定义）
- 向量数据库选型（内置 RAGFlow，见 ADR-004）
- Spring AI 向量处理（Phase 2）

## 3. 用户故事 / 使用场景

- 作为 fault-diagnosis-service，我需要调用 `RagDubboService.retrieve()` 获取语义相关的故障历史和维修知识，以便生成 AI 诊断结果。
- 作为 maintenance-manual-service，我需要调用 `RagDubboService.translate()` 将英文手册段落翻译为中文，以便满足一线人员查阅需求。
- 作为运维人员，我需要通过 rag-service 统一管理所有知识库（KB）的创建、文档上传和索引状态，以便 AI 检索质量可控可追踪。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | rag-service 通过 Dubbo 暴露 `RagDubboService`，所有 AI 调用经此接口 | Given 业务服务调用 `RagDubboService.retrieve()` When RAGFlow 正常 Then 返回检索结果；RAGFlow 不可用 Then 抛出 `RagUnavailableException`（5900） |
| FR-2 | 支持知识库（KB）的创建、查询和删除 | Given 管理员调用 `createKnowledgeBase()` When RAGFlow KB 创建成功 Then 返回 ragflowKbId，元数据持久化到 `mro_rag.kb` |
| FR-3 | 支持文档异步上传与索引，状态可查询 | Given 调用 `uploadDocument()` When 文档提交到 RAGFlow When 索引完成 Then `doc.status` 更新为 `indexed`，可通过 `getDocumentStatus()` 查询 |
| FR-4 | 支持语义检索，返回 Top-K 相关段落及来源引用 | Given 调用 `retrieve()` When RAGFlow 返回结果 Then 每条结果含 `content / score / docId / docTitle / chunkId` |
| FR-5 | 支持航空维修领域中英文翻译，调用 RAGFlow 翻译能力 | Given 调用 `translate()` When RAGFlow 翻译完成 Then 返回目标语言文本；翻译时长超过 5 分钟触发超时错误（5904） |
| FR-6 | 文档上传支持 PDF / XML / SGML 格式，RAGFlow 解析后建立向量索引 | Given 上传 PDF 文档 When 解析完成 Then 文档可被语义检索命中 |
| FR-7 | rag-service 异步任务（文档索引/翻译）通过数据库状态机管理，不丢失 | Given 异步任务提交后 rag-service 重启 When 服务恢复 Then 任务从 `pending` 状态继续执行 |

## 5. 非功能需求

- 性能：`retrieve()` 调用 P95 < 3s；`translate()` 异步提交 P95 < 500ms（状态轮询独立）
- 安全：RAGFlow API Key 存 Nacos 配置中心，禁止硬编码；Dubbo 接口不对 Gateway 直接暴露
- 可用性：RAGFlow 不可用时快速失败（超时 5s），不阻塞业务服务主流程
- 隔离：各业务的知识库在 RAGFlow 中按 `kb_type` 物理隔离，不共享向量索引

## 6. 数据契约

rag-service 使用 schema `mro_rag`，存储 KB 元数据与任务状态。

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| rag_knowledge_base.id | bigint | Y | PK | 知识库 ID |
| rag_knowledge_base.name | varchar(128) | Y | | 知识库名称 |
| rag_knowledge_base.kb_type | enum | Y | tshoot/manual/training | 归属业务类型 |
| rag_knowledge_base.ragflow_kb_id | varchar(64) | Y | UK | RAGFlow 侧 KB ID |
| rag_knowledge_base.description | text | N | | 描述 |
| rag_knowledge_base.created_by | bigint | Y | | 创建人 |
| rag_knowledge_base.created_at | timestamp | Y | | 创建时间 |
| rag_document.id | bigint | Y | PK | 文档记录 ID |
| rag_document.kb_id | bigint | Y | FK | 关联知识库 |
| rag_document.file_name | varchar(256) | Y | | 文件名 |
| rag_document.file_format | enum | Y | pdf/xml/sgml | 文件格式 |
| rag_document.ragflow_doc_id | varchar(64) | N | | RAGFlow 侧文档 ID |
| rag_document.status | enum | Y | pending/parsing/indexed/failed | 索引状态 |
| rag_document.chunk_count | int | N | | 切片数量（索引后填充） |
| rag_document.uploaded_by | bigint | Y | | 上传人 |
| rag_document.uploaded_at | timestamp | Y | | 上传时间 |
| rag_document.indexed_at | timestamp | N | | 索引完成时间 |
| rag_translation_task.id | bigint | Y | PK | 翻译任务 ID |
| rag_translation_task.source_text_hash | varchar(64) | Y | | 源文本 SHA-256（用于缓存命中） |
| rag_translation_task.source_lang | varchar(8) | Y | | 源语言（en） |
| rag_translation_task.target_lang | varchar(8) | Y | | 目标语言（zh） |
| rag_translation_task.status | enum | Y | pending/processing/completed/failed | 状态 |
| rag_translation_task.result_text | text | N | | 翻译结果 |
| rag_translation_task.created_at | timestamp | Y | | 创建时间 |
| rag_translation_task.completed_at | timestamp | N | | 完成时间 |

## 7. 接口契约

### 7.1 Dubbo 接口（`RagDubboService`）

rag-service 在端口 **20882** 注册 Dubbo 服务，manage-web 及各业务服务通过 Dubbo 调用。

```java
/**
 * RAG 统一服务接口，所有 AI 调用必须经此接口，严禁业务服务直接调 RAGFlow。
 */
public interface RagDubboService {

    // ── 知识库管理 ──────────────────────────────────────────────

    /** 创建知识库，在 RAGFlow 建库并持久化元数据。 */
    Long createKnowledgeBase(CreateKbCommand cmd);

    /** 查询知识库列表（按类型过滤）。 */
    PageResult<KbDTO> listKnowledgeBases(KbQueryParam param);

    /** 删除知识库（同步删除 RAGFlow 侧）。 */
    void deleteKnowledgeBase(Long kbId);

    // ── 文档管理 ────────────────────────────────────────────────

    /**
     * 异步上传文档到 RAGFlow 并触发索引。
     * @return rag_document.id，调用方通过 getDocumentStatus() 轮询状态
     */
    Long uploadDocument(Long kbId, UploadDocCommand cmd);

    /** 查询文档索引状态。 */
    RagDocStatusDTO getDocumentStatus(Long docId);

    /** 删除文档（同步删除 RAGFlow 侧向量数据）。 */
    void deleteDocument(Long kbId, Long docId);

    // ── 检索 ────────────────────────────────────────────────────

    /**
     * 语义检索，返回 Top-K 相关段落。
     * 调用方传入已构建好的 query 文本，rag-service 透传至 RAGFlow。
     */
    List<RetrievalChunkDTO> retrieve(RetrieveParam param);

    // ── 翻译 ────────────────────────────────────────────────────

    /**
     * 提交翻译任务（异步）。
     * @return rag_translation_task.id
     */
    Long submitTranslation(TranslateCommand cmd);

    /** 查询翻译结果（轮询）。 */
    TranslationResultDTO getTranslationResult(Long taskId);
}
```

### 7.2 关键 Java 21 Record 定义

```java
// ── 知识库 ──────────────────────────────────────────────────────

public record CreateKbCommand(
    String name,
    String kbType,        // tshoot / manual / training
    String description,
    Long createdBy
) implements Serializable {}

public record KbDTO(
    Long id,
    String name,
    String kbType,
    String ragflowKbId,
    String description,
    int documentCount,
    LocalDateTime createdAt
) implements Serializable {}

public record KbQueryParam(
    String kbType,        // 可选，按类型过滤
    int pageNum,
    int pageSize
) implements Serializable {}

// ── 文档 ────────────────────────────────────────────────────────

public record UploadDocCommand(
    String fileName,
    String fileFormat,    // pdf / xml / sgml
    byte[] fileContent,   // 文件字节数组（Dubbo 传输，≤ 50MB）
    Long uploadedBy
) implements Serializable {}

public record RagDocStatusDTO(
    Long docId,
    String ragflowDocId,
    String status,        // pending / parsing / indexed / failed
    Integer chunkCount,
    LocalDateTime indexedAt
) implements Serializable {}

// ── 检索 ────────────────────────────────────────────────────────

public record RetrieveParam(
    Long kbId,
    String query,
    int topK,             // 默认 5，最大 20
    double scoreThreshold // 相似度阈值，默认 0.5
) implements Serializable {}

public record RetrievalChunkDTO(
    String chunkId,
    String content,
    double score,
    Long docId,
    String docTitle,
    String pageRange       // 来源页码范围（PDF），可为 null
) implements Serializable {}

// ── 翻译 ────────────────────────────────────────────────────────

public record TranslateCommand(
    String sourceText,
    String sourceLang,    // en
    String targetLang     // zh
) implements Serializable {}

public record TranslationResultDTO(
    Long taskId,
    String status,        // pending / processing / completed / failed
    String resultText,    // 翻译结果（status=completed 时有值）
    LocalDateTime completedAt
) implements Serializable {}
```

### 7.3 RAGFlow HTTP API（rag-service 内部调用，业务服务不可见）

| Method | RAGFlow Path | 说明 |
|--------|-------------|------|
| POST | `/api/v1/knowledgebases` | 创建知识库 |
| DELETE | `/api/v1/knowledgebases/{kb_id}` | 删除知识库 |
| POST | `/api/v1/knowledgebases/{kb_id}/documents` | 上传文档 |
| GET | `/api/v1/documents/{doc_id}` | 查询文档状态 |
| DELETE | `/api/v1/documents/{doc_id}` | 删除文档 |
| POST | `/api/v1/retrieval` | 语义检索 |
| POST | `/api/v1/chat/completions` | 翻译（Prompt 模式） |

RAGFlow API Key 通过 Nacos 配置项 `rag.ragflow.api-key` 注入，禁止写入代码或 git。

## 8. 权限边界

- `RagDubboService` 为 Dubbo 内部接口，仅注册于 Nacos，不经 Gateway 暴露给前端。
- rag-service 无需从 RpcContext 读取用户上下文（AI 编排无数据权限隔离需求）。
- 知识库管理操作（创建/删除）由业务服务在自身权限校验后调用，rag-service 不重复校验。

## 9. 异步任务状态机

### 文档索引状态机

```
pending → parsing → indexed
              ↓
           failed（可重试）
```

- rag-service 启动定时任务（每 10s）扫描 `status=pending/parsing` 的文档，轮询 RAGFlow 状态并更新。
- 单文档最长解析时间 10 分钟，超时置为 `failed`。

### 翻译任务状态机

```
pending → processing → completed
               ↓
            failed（不自动重试，由调用方决策）
```

- 同源文本（SHA-256 相同）且目标语言相同时，直接复用已 `completed` 的任务结果（缓存复用）。

## 10. 验收标准

- [ ] `RagDubboService` 通过 Nacos 注册，fault-diagnosis-service / maintenance-manual-service 调用成功
- [ ] 知识库 CRUD 操作与 RAGFlow 状态同步
- [ ] 文档上传后异步索引，`getDocumentStatus()` 正确返回 `indexed`
- [ ] `retrieve()` 返回结果含 `content / score / docId / docTitle`
- [ ] 翻译任务同源文本缓存复用正常
- [ ] RAGFlow 不可用时 `retrieve()` 5s 超时快速失败，抛出 5900 错误
- [ ] RAGFlow API Key 从 Nacos 读取，不硬编码
- [ ] 所有异步任务在 rag-service 重启后从 DB 恢复执行

## 11. 未决问题

- [ ] RAGFlow 版本锁定（当前按 v0.x API，需在部署前确认稳定版本）
- [ ] 文档超过 50MB 时的分片上传方案（当前 Dubbo 单次传输限制）
- [ ] `retrieve()` 的 Prompt 模板是否由 rag-service 统一管理还是由各业务服务自行传入
- [ ] 翻译接口是否需要对接专用航空领域微调模型（当前用 RAGFlow Chat Completions 通用接口）

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0.0 | 2026-05-26 | 初稿，定义 RagDubboService 接口、知识库/文档/检索/翻译契约、错误码 5900-5999 |

## B. 后端实现约束

### B.1 JSON Schema

rag-service 为纯 Dubbo 服务，**不直接暴露 HTTP 接口给前端**。以下为 manage-web 内部可能提供的管理接口（供运维/管理员操作 KB）：

**GET /api/rag/knowledge-bases — 获取知识库列表**

```json
// Request Query
{
  "kbType": "tshoot",   // 可选：tshoot / manual / training
  "pageNum": 1,
  "pageSize": 20
}

// Response
{
  "code": 0,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "MRO排故知识库",
        "kbType": "tshoot",
        "ragflowKbId": "kb-abc123",
        "description": "涵盖A320/B737系列故障历史",
        "documentCount": 128,
        "createdAt": "2026-05-01T08:00:00Z"
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1716700800000
}
```

**POST /api/rag/knowledge-bases — 创建知识库**

```json
// Request Body
{
  "name": "MRO排故知识库",
  "kbType": "tshoot",
  "description": "涵盖A320/B737系列故障历史"
}

// Response
{
  "code": 0,
  "msg": "ok",
  "data": { "id": 1 },
  "timestamp": 1716700800000
}
```

**POST /api/rag/knowledge-bases/{kbId}/documents — 上传文档**

```json
// Request: multipart/form-data
// 字段: file (binary), uploadedBy (Long)

// Response（异步，立即返回 docId）
{
  "code": 0,
  "msg": "ok",
  "data": { "docId": 42 },
  "timestamp": 1716700800000
}
```

**GET /api/rag/documents/{docId}/status — 查询文档索引状态**

```json
// Response
{
  "code": 0,
  "msg": "ok",
  "data": {
    "docId": 42,
    "ragflowDocId": "doc-xyz789",
    "status": "indexed",
    "chunkCount": 87,
    "indexedAt": "2026-05-26T10:30:00Z"
  },
  "timestamp": 1716700800000
}
```

### B.2 Dubbo 服务接口

```java
/**
 * rag-service 对内暴露，端口 20882。
 * 业务服务（fault-diagnosis / maintenance-manual）通过 Dubbo 调用，
 * 严禁绕过直接访问 RAGFlow HTTP API。
 */
@DubboService(version = "1.0.0", group = "mro")
public interface RagDubboService {

    Long createKnowledgeBase(CreateKbCommand cmd);
    PageResult<KbDTO> listKnowledgeBases(KbQueryParam param);
    void deleteKnowledgeBase(Long kbId);

    Long uploadDocument(Long kbId, UploadDocCommand cmd);
    RagDocStatusDTO getDocumentStatus(Long docId);
    void deleteDocument(Long kbId, Long docId);

    List<RetrievalChunkDTO> retrieve(RetrieveParam param);

    Long submitTranslation(TranslateCommand cmd);
    TranslationResultDTO getTranslationResult(Long taskId);
}

// ── Records ────────────────────────────────────────────────

public record CreateKbCommand(String name, String kbType, String description, Long createdBy) implements Serializable {}
public record KbDTO(Long id, String name, String kbType, String ragflowKbId, String description, int documentCount, LocalDateTime createdAt) implements Serializable {}
public record KbQueryParam(String kbType, int pageNum, int pageSize) implements Serializable {}

public record UploadDocCommand(String fileName, String fileFormat, byte[] fileContent, Long uploadedBy) implements Serializable {}
public record RagDocStatusDTO(Long docId, String ragflowDocId, String status, Integer chunkCount, LocalDateTime indexedAt) implements Serializable {}

public record RetrieveParam(Long kbId, String query, int topK, double scoreThreshold) implements Serializable {}
public record RetrievalChunkDTO(String chunkId, String content, double score, Long docId, String docTitle, String pageRange) implements Serializable {}

public record TranslateCommand(String sourceText, String sourceLang, String targetLang) implements Serializable {}
public record TranslationResultDTO(Long taskId, String status, String resultText, LocalDateTime completedAt) implements Serializable {}
```

### B.3 错误码（5900-5999，rag-service）

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 5900 | RAG_UNAVAILABLE | RAGFlow 服务不可用（超时或连接拒绝） |
| 5901 | KB_NOT_FOUND | 知识库不存在 |
| 5902 | DOC_NOT_FOUND | 文档不存在 |
| 5903 | DOC_PARSE_FAILED | 文档解析失败（格式不支持或内容损坏） |
| 5904 | TRANSLATION_TIMEOUT | 翻译任务超时（超过 5 分钟） |
| 5905 | TRANSLATION_FAILED | 翻译失败（RAGFlow 返回错误） |
| 5906 | RETRIEVAL_EMPTY | 检索结果为空（低于阈值，无匹配段落） |
| 5907 | FILE_TOO_LARGE | 文件超过 50MB 限制 |
| 5908 | UNSUPPORTED_FORMAT | 文件格式不支持（非 pdf/xml/sgml） |
| 5909 | KB_NAME_DUPLICATE | 知识库名称已存在（同 kbType 下） |
