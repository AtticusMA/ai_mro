---
id: MRO-002
title: AR智慧维修协作平台
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

# Spec: AR智慧维修协作平台

## 1. 背景与目标

以AR眼镜为载体，结合AI识别模型，构建可视化的飞机航线绕机检机制、高检专项维修机制、远程排故技术支援机制，实现维修全过程的可视化记录与监控。

对齐 Charter 目标：安全提升（AI 智能巡检关键区域自动识别与异常实时提醒）、人员赋能（远程专家实时指导，降低一线人员对经验的依赖）。

## 2. 范围

### In Scope
- AR 辅助航线绕机检查（路径引导 + AI 识别）
- AI 视觉识别（盖板闭合、安全销在位等）
- 异常即时弹窗提醒与影像存档
- 一键呼叫远程专家协作
- 远程画面标注、手册推送、语音指导
- 红外测温等外设模块接入
- 维修全流程影像录制与电子档案

### Out of Scope
- AR 眼镜硬件生产制造
- 航空器结构修理方案制定（属人工决策）
- 非维修场景的视频监控

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望通过 AR 眼镜获得巡检路径引导和异常识别提醒，以便高效完成航线绕机检查。
- 作为技术支援工程师，我希望远程接入维修现场 AR 视频，实时标注画面指导排故，以便快速支援一线人员。
- 作为安全质量监管部门，我希望维修过程全流程影像自动存档，以便事后追溯和质量审查。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持航线绕机检查的AR辅助引导，按路线自动规划巡检路径 | Given AR眼镜已连接 When 机务人员启动巡检任务 Then 眼镜上显示巡检路径引导和检查点提示 |
| FR-2 | 具备AI视觉识别能力，实时识别盖板闭合状态、安全销在位等关键区域状态 | Given AR眼镜摄像头采集画面 When 画面中出现关键检查区域 Then AI模型在1秒内返回识别结果 |
| FR-3 | 支持异常情况即时弹窗提醒，并将异常影像自动存档 | Given AI识别到异常 When 置信度超过阈值 Then 眼镜端弹窗提醒并自动截图存档 |
| FR-4 | 支持一键呼叫技术专家，实现多方视频联动远程指导 | Given 维修人员遇到问题 When 点击呼叫按钮 Then 系统发起远程协作请求，专家可在PC/移动端接入 |
| FR-5 | 支持远程画面标注、技术手册推送和语音指导功能 | Given 远程协作会话已建立 When 专家在画面上标注或推送手册 Then 一线人员AR视野中实时显示标注内容 |
| FR-6 | 支持红外测温等外设模块接入，满足发动机修理等专项工作需求 | Given 红外模块已连接AR眼镜 When 对准发动机部件 Then 实时叠加温度数据到AR视野 |
| FR-7 | 支持维修过程全流程影像录制，自动生成可追溯的电子维修档案 | Given 巡检/维修任务进行中 When 全程 Then 系统自动录制影像，任务结束后归档并关联工单 |

## 5. 非功能需求

- 性能：AI 图像识别响应时间 ≤ 1秒；视频远程协作推流延迟 ≤ 500毫秒
- 安全：影像数据加密存储；远程协作通道 TLS 加密
- 可用性：AR 眼镜重量 ≤ 500g，连续佩戴2小时无不适
- 网络：5G 专网覆盖，保障大带宽低延迟

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| inspection_task.id | bigint | Y | PK | 巡检任务 ID |
| inspection_task.aircraft_id | varchar(32) | Y | FK | 关联飞机 |
| inspection_task.inspector_id | bigint | Y | FK | 执行人 |
| inspection_task.route_template | varchar(64) | Y | | 巡检路线模板 |
| inspection_task.status | enum | Y | pending/in_progress/completed | 任务状态 |
| inspection_task.started_at | timestamp | N | | 开始时间 |
| inspection_task.completed_at | timestamp | N | | 完成时间 |
| ar_session.id | bigint | Y | PK | 远程协作会话 ID |
| ar_session.caller_id | bigint | Y | FK | 发起人 |
| ar_session.expert_id | bigint | N | FK | 专家 |
| ar_session.status | enum | Y | waiting/active/ended | 会话状态 |
| ar_session.recording_url | varchar(512) | N | | 录像存储地址 |
| ar_session.duration_seconds | int | N | | 通话时长 |
| anomaly_record.id | bigint | Y | PK | 异常记录 ID |
| anomaly_record.task_id | bigint | Y | FK | 关联巡检任务 |
| anomaly_record.anomaly_type | varchar(64) | Y | | 异常类型（盖板未闭合/安全销缺失等） |
| anomaly_record.confidence | decimal(5,4) | Y | 0~1 | AI 识别置信度 |
| anomaly_record.snapshot_url | varchar(512) | Y | | 异常截图地址 |
| anomaly_record.detected_at | timestamp | Y | | 检测时间 |
| video_archive.id | bigint | Y | PK | 影像档案 ID |
| video_archive.task_id | bigint | N | FK | 关联任务 |
| video_archive.session_id | bigint | N | FK | 关联会话 |
| video_archive.file_url | varchar(512) | Y | | 文件存储地址 |
| video_archive.duration_seconds | int | Y | | 时长 |
| video_archive.created_at | timestamp | Y | | 创建时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/ar/inspections | ar:list | 获取巡检任务列表 |
| POST | /api/ar/inspections | ar:inspect | 创建巡检任务 |
| PUT | /api/ar/inspections/{id}/start | ar:inspect | 开始巡检 |
| PUT | /api/ar/inspections/{id}/complete | ar:inspect | 完成巡检 |
| GET | /api/ar/inspections/{id}/anomalies | ar:review | 获取巡检异常记录 |
| POST | /api/ar/sessions | ar:call | 发起远程协作会话 |
| PUT | /api/ar/sessions/{id}/join | ar:call | 专家加入会话 |
| PUT | /api/ar/sessions/{id}/end | ar:call | 结束会话 |
| POST | /api/ar/sessions/{id}/annotations | ar:call | 发送画面标注 |
| GET | /api/ar/archives | ar:archive | 获取影像档案列表 |
| GET | /api/ar/archives/{id}/playback | ar:archive | 回放影像 |
| WS | /ws/ar/stream | ar:call | AR 实时视频流（WebRTC 信令） |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `ar:list` — 查看巡检任务列表
  - `ar:inspect` — 执行巡检（AR 眼镜端）
  - `ar:call` — 发起/加入远程协作
  - `ar:review` — 查看异常记录
  - `ar:archive` — 查看/回放影像档案
- 数据权限：按维修基地过滤

## 9. 验收标准

- [ ] AR 巡检路径引导功能正常
- [ ] AI 视觉识别响应时间 ≤ 1秒
- [ ] 异常弹窗提醒与自动截图存档正常
- [ ] 远程协作视频延迟 ≤ 500毫秒
- [ ] 画面标注实时同步到对端
- [ ] 全流程影像录制与归档正常
- [ ] 所有接口通过权限验证

## 10. 未决问题

- [ ] AR 眼镜品牌和 SDK 尚未最终确定，接口可能需适配调整
- [ ] AI 视觉识别模型（盖板/安全销检测）的训练数据集需定制采集
- [ ] 5G 专网覆盖范围是否能覆盖所有外场停机位

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
