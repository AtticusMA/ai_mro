---
id: MRO-008
title: 无纸化电子工卡
domain: mro
status: approved
owner: '@product'
version: 1.2.1
created: 2026-05-25
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: [MRO-006]
---

# Spec: 无纸化电子工卡

## 1. 背景与目标

通过电子工卡替代传统纸质工卡，实现工卡编制、下发、执行、签署、反馈的全流程数字化。工卡按照具体流程和航耗材需求执行，完成后自动归档至维修管理系统。

对齐 Charter 目标：管理转型（全过程数字化管控，维修管理决策有据可依、有源可溯）、安全提升（区块链加密确保不可篡改，资质动态匹配防止越权操作）。

## 2. 范围

### In Scope
- 工卡电子化编制、审批和下发
- 航线、定检、排故等各类维修工作的电子化签署
- 按人员授权等级动态匹配维修任务
- 工卡执行进度实时监控和完成反馈
- 工卡到期提醒（关键节点预警）
- 电子签名和区块链加密（不可篡改、可追溯）
- 与手册管理平台对接（工卡内嵌手册链接跳转）
- 工卡质检签署（质检员对完成工卡进行质量确认，pass/fail）
- 不符合项（NCR）管理（创建、跟踪、关闭全流程）
- 工卡开工/完工签到（人员到场确认）

### Out of Scope
- 工卡模板标准制定（属于行业规范）
- 适航性审批签发（属于管理局职责）
- 薪酬绩效管理

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望在移动设备上接收电子工卡并逐步签署完成，以便替代纸质工卡实现无纸化作业。
- 作为机库管理人员，我希望实时监控所有工卡的执行进度，以便掌握维修整体进展并及时干预延误。
- 作为安全质量监管部门，我希望工卡签署采用区块链加密不可篡改，以便确保维修记录的真实性和可追溯性。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持工卡的电子化编制、审批和下发流程 | Given 编制人创建工卡 When 提交审批并审批通过 Then 工卡状态变为已下发，分配到执行人 |
| FR-2 | 支持航线、定检、排故等各类维修工作的电子化签署 | Given 工卡已下发 When 执行人完成步骤 Then 可进行电子签署，签署记录含时间戳和签署人 |
| FR-3 | 支持按人员授权等级动态匹配维修任务，确保资质与任务匹配 | Given 工卡需要分配 When 系统匹配执行人 Then 仅显示符合资质等级和机型授权的人员 |
| FR-4 | 具备工卡执行进度实时监控和完成反馈功能 | Given 工卡执行中 When 管理人员查看进度面板 Then 实时展示各工卡完成百分比和当前步骤 |
| FR-5 | 支持工卡到期提醒功能，关键节点自动预警 | Given 工卡设定到期时间 When 距到期不足24小时 Then 系统自动推送预警给执行人和管理人员 |
| FR-6 | 支持电子签名和区块链加密，确保工卡信息的真实性、不可篡改性和可追溯性 | Given 用户完成电子签署 When 签署数据提交 Then 系统生成数字签名并将哈希上链，可随时验证 |
| FR-7 | 与手册管理平台对接，支持工卡内嵌手册链接跳转 | Given 工卡步骤关联手册章节 When 执行人点击手册链接 Then 跳转到手册管理平台对应内容页 |
| FR-8 | 支持工卡完成后质检签署（pass/fail），失败时自动创建 NCR | Given 工卡所有步骤已完成 When 质检员进行质检签署 Then pass 时工卡归档；fail 时自动创建 NCR 并关联至工卡 |
| FR-9 | 支持 NCR 全流程管理：创建、责任人分配、整改、关闭，并记录严重等级 | Given NCR 已创建 When 责任人完成整改 When 质检员审核通过 Then NCR 状态变为 closed，关联工卡可继续归档 |
| FR-10 | 支持工卡开工/完工签到，记录人员到场时间，作为工时核算依据 | Given 工卡已下发 When 执行人开始/结束工作 Then 系统记录签到时间戳和人员信息 |

## 5. 非功能需求

- 性能：工卡列表查询 ≤ 2秒；签署操作响应 ≤ 1秒
- 安全：电子签名 + 区块链哈希上链；工卡数据加密存储
- 可用性：系统核心服务可用性 ≥ 99.95%；支持离线签署后同步
- 合规：电子签名符合《电子签名法》要求

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| workcard.id | bigint | Y | PK | 工卡 ID |
| workcard.card_no | varchar(64) | Y | UK | 工卡编号 |
| workcard.title | varchar(256) | Y | | 工卡标题 |
| workcard.card_type | enum | Y | line/heavy/troubleshoot | 工卡类型 |
| workcard.aircraft_id | varchar(32) | Y | | 飞机注册号 |
| workcard.priority | enum | Y | urgent/normal/low | 优先级 |
| workcard.status | enum | Y | draft/approved/issued/in_progress/completed/archived | 状态 |
| workcard.created_by | bigint | Y | FK | 编制人 |
| workcard.approved_by | bigint | N | FK | 审批人 |
| workcard.due_date | timestamp | N | | 到期时间 |
| workcard.created_at | timestamp | Y | | 创建时间 |
| workcard_step.id | bigint | Y | PK | 工卡步骤 ID |
| workcard_step.workcard_id | bigint | Y | FK | 关联工卡 |
| workcard_step.step_no | int | Y | | 步骤序号 |
| workcard_step.description | text | Y | | 步骤描述 |
| workcard_step.required_tools | jsonb | N | | 所需工具清单 |
| workcard_step.required_materials | jsonb | N | | 所需航材清单 |
| workcard_step.manual_ref | varchar(128) | N | | 关联手册章节 |
| workcard_step.status | enum | Y | pending/in_progress/completed/skipped | 状态 |
| workcard_step.completed_by | bigint | N | FK | 完成人 |
| workcard_step.completed_at | timestamp | N | | 完成时间 |
| workcard_signature.id | bigint | Y | PK | 签署记录 ID |
| workcard_signature.workcard_id | bigint | Y | FK | 关联工卡 |
| workcard_signature.step_id | bigint | N | FK | 关联步骤（可选） |
| workcard_signature.signer_id | bigint | Y | FK | 签署人 |
| workcard_signature.signature_type | enum | Y | execute/inspect/approve | 签署类型 |
| workcard_signature.digital_signature | text | Y | | 电子签名数据 |
| workcard_signature.blockchain_hash | varchar(128) | N | | 区块链哈希 |
| workcard_signature.signed_at | timestamp | Y | | 签署时间 |
| approval_flow.id | bigint | Y | PK | 审批流 ID |
| approval_flow.workcard_id | bigint | Y | FK | 关联工卡 |
| approval_flow.approver_id | bigint | Y | FK | 审批人 |
| approval_flow.action | enum | Y | approve/reject/return | 审批动作 |
| approval_flow.comment | text | N | | 审批意见 |
| approval_flow.acted_at | timestamp | Y | | 操作时间 |
| personnel_qualification.id | bigint | Y | PK | 资质记录 ID |
| personnel_qualification.user_id | bigint | Y | FK | 人员 |
| personnel_qualification.qualification_type | varchar(64) | Y | | 资质类型 |
| personnel_qualification.aircraft_type | varchar(32) | Y | | 适用机型 |
| personnel_qualification.level | enum | Y | A/B/C | 授权等级 |
| personnel_qualification.valid_from | date | Y | | 有效期起 |
| personnel_qualification.valid_to | date | Y | | 有效期止 |
| quality_sign_record.id | bigint | Y | PK | 质检签署记录 ID |
| quality_sign_record.workcard_id | bigint | Y | FK | 关联工卡 |
| quality_sign_record.inspector_id | bigint | Y | FK | 质检员 |
| quality_sign_record.result | enum | Y | pass/fail | 签署结果 |
| quality_sign_record.remark | text | N | | 备注 |
| quality_sign_record.digital_signature | text | Y | | 电子签名数据 |
| quality_sign_record.signed_at | timestamp | Y | | 签署时间 |
| ncr.id | bigint | Y | PK | NCR ID |
| ncr.workcard_id | bigint | Y | FK | 关联工卡 |
| ncr.title | varchar(256) | Y | | NCR 标题 |
| ncr.description | text | Y | | 问题描述 |
| ncr.severity | enum | Y | critical/major/minor | 严重等级 |
| ncr.status | enum | Y | open/in_progress/closed | 状态 |
| ncr.assignee_id | bigint | N | FK | 责任人 |
| ncr.corrective_action | text | N | | 整改措施 |
| ncr.closed_by | bigint | N | FK | 关闭人 |
| ncr.closed_at | timestamp | N | | 关闭时间 |
| ncr.created_at | timestamp | Y | | 创建时间 |
| workcard_checkin.id | bigint | Y | PK | 签到记录 ID |
| workcard_checkin.workcard_id | bigint | Y | FK | 关联工卡 |
| workcard_checkin.user_id | bigint | Y | FK | 签到人 |
| workcard_checkin.checkin_type | enum | Y | start/end | 签到类型 |
| workcard_checkin.checked_at | timestamp | Y | | 签到时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/workcards | workcard:monitor | 获取工卡列表（支持分页/筛选） |
| POST | /api/workcards | workcard:create | 创建工卡（编制） |
| GET | /api/workcards/{id} | workcard:execute | 获取工卡详情（含步骤） |
| PUT | /api/workcards/{id} | workcard:create | 修改工卡 |
| POST | /api/workcards/{id}/submit | workcard:create | 提交审批 |
| POST | /api/workcards/{id}/approve | workcard:approve | 审批通过/驳回 |
| POST | /api/workcards/{id}/issue | workcard:approve | 下发工卡 |
| PUT | /api/workcards/{id}/steps/{stepId}/complete | workcard:execute | 完成步骤 |
| POST | /api/workcards/{id}/sign | workcard:sign | 电子签署 |
| GET | /api/workcards/{id}/signatures | workcard:monitor | 获取签署记录 |
| GET | /api/workcards/{id}/blockchain-verify | workcard:monitor | 区块链验证签署真实性 |
| GET | /api/workcards/progress | workcard:monitor | 工卡执行进度概览 |
| GET | /api/workcards/alerts | workcard:monitor | 工卡到期预警列表 |
| GET | /api/qualifications | workcard:approve | 获取人员资质列表 |
| GET | /api/qualifications/match | workcard:approve | 资质与任务匹配检查 |
| GET | /api/workcards/pending-sign | quality:sign | 待质检签署工卡列表 |
| GET | /api/workcards/{id}/quality-sign | quality:sign | 获取工卡质检签署详情 |
| POST | /api/workcards/{id}/quality-sign | quality:sign | 提交质检签署（pass/fail） |
| GET | /api/ncr | quality:sign | 获取 NCR 列表（支持分页/筛选） |
| POST | /api/ncr | quality:sign | 创建 NCR |
| GET | /api/ncr/{id} | quality:sign | 获取 NCR 详情 |
| PUT | /api/ncr/{id} | quality:sign | 更新 NCR（整改措施/责任人） |
| POST | /api/ncr/{id}/close | quality:sign | 关闭 NCR |
| POST | /api/workcards/{id}/checkin | workcard:execute | 工卡开工/完工签到 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `workcard:create` — 编制/修改工卡、提交审批
  - `workcard:approve` — 审批/下发工卡、查看人员资质
  - `workcard:execute` — 执行工卡步骤
  - `workcard:sign` — 电子签署
  - `workcard:monitor` — 监控进度、查看签署记录、到期预警
  - `quality:sign` — 质检签署、NCR 管理（创建/更新/关闭）
- 数据权限：按维修基地/部门过滤

## 9. 验收标准

- [ ] 工卡编制→审批→下发全流程正常
- [ ] 电子签署操作响应 ≤ 1秒
- [ ] 区块链哈希上链并可验证
- [ ] 人员资质与任务动态匹配正常
- [ ] 工卡进度实时监控正常
- [ ] 到期预警自动触发
- [ ] 手册链接跳转正常
- [ ] 所有接口通过权限验证
- [ ] 质检签署 pass/fail 流程正常，fail 时自动创建 NCR
- [ ] NCR 创建→整改→关闭全流程正常
- [ ] 工卡开工/完工签到记录正确保存

## 10. 未决问题

- [ ] 区块链上链的具体实现（Hyperledger Fabric 节点部署方案和 gas 成本评估）
- [ ] 电子签名的法律效力认定（是否需要第三方 CA 证书）
- [ ] 工卡与工具系统联动时"禁止开工"的阻断力度需与业务方确认
- [ ] 离线场景下的工卡签署方案（机库网络中断时如何保证操作不中断）

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

#### GET /api/workcards/pending-sign — 待质检签署列表

Request query: `?pageNum=1&pageSize=20&aircraftId=B-1234`

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
        "completionRate": 100,
        "dueDate": "2026-06-10T18:00:00Z"
      }
    ],
    "total": 3, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/workcards/{id}/quality-sign — 提交质检签署

Request body:
```json
{
  "result": "fail",
  "remark": "步骤3液压压力测试未达标",
  "digitalSignature": "BASE64_ENCODED_SIGNATURE"
}
```

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "signRecordId": 9001,
    "result": "fail",
    "ncrId": 10001,
    "signedAt": "2026-05-28T10:00:00Z"
  },
  "timestamp": 1748304000000
}
```

#### GET /api/ncr — NCR 列表

Request query: `?pageNum=1&pageSize=20&status=open&workcardId=1001`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 10001,
        "workcardId": 1001,
        "workcardNo": "WC-2026-05-001",
        "title": "液压压力测试不合格",
        "severity": "major",
        "status": "open",
        "assigneeName": "李工程师",
        "createdAt": "2026-05-28T10:00:00Z"
      }
    ],
    "total": 4, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/ncr — 创建 NCR

Request body:
```json
{
  "workcardId": 1001,
  "title": "液压压力测试不合格",
  "description": "步骤3压力值为180bar，低于标准210bar",
  "severity": "major",
  "assigneeId": 102
}
```

Response: `{"code": 0, "msg": "ok", "data": {"id": 10001}, "timestamp": 1748304000000}`

#### PUT /api/ncr/{id} — 更新 NCR

Request body:
```json
{
  "correctiveAction": "更换液压泵密封圈后重新测试",
  "assigneeId": 102
}
```

Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### POST /api/ncr/{id}/close — 关闭 NCR

Request body:
```json
{
  "closeReason": "整改完成，复测压力值 215bar，符合标准",
  "digitalSignature": "BASE64_ENCODED_SIGNATURE"
}
```

Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### POST /api/workcards/{id}/checkin — 工卡签到

Request body:
```json
{
  "checkinType": "start"
}
```

Response: `{"code": 0, "msg": "ok", "data": {"checkinId": 11001, "checkedAt": "2026-05-28T08:00:00Z"}, "timestamp": 1748304000000}`

区块链集成：签署完成后，workcard-service 将签名哈希通过 Kafka 事件异步发送到区块链适配器。首期以 SM3 哈希存储在 DB 中模拟，待 Hyperledger Fabric 部署就绪后切换；`blockchainHash` 首期返回本地 SM3 哈希。

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
    PageResult<WorkcardDTO> listPendingSign(WorkcardQueryParam param, UserContextDTO ctx);
    QualitySignResultDTO qualitySign(QualitySignCommand cmd);
    PageResult<NcrDTO> listNcr(NcrQueryParam param);
    Long createNcr(CreateNcrCommand cmd);
    NcrDTO getNcr(Long ncrId);
    void updateNcr(UpdateNcrCommand cmd);
    void closeNcr(CloseNcrCommand cmd);
    Long checkin(WorkcardCheckinCommand cmd);
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

public record QualitySignCommand(
    Long workcardId, String result, String remark,
    String digitalSignature, Long inspectorId
) implements Serializable {}

public record QualitySignResultDTO(
    Long signRecordId, String result, Long ncrId, Instant signedAt
) implements Serializable {}

public record NcrDTO(
    Long id, Long workcardId, String workcardNo, String title,
    String description, String severity, String status,
    Long assigneeId, String assigneeName, String correctiveAction,
    Instant createdAt, Instant closedAt
) implements Serializable {}

public record CreateNcrCommand(
    Long workcardId, String title, String description,
    String severity, Long assigneeId, Long createdBy
) implements Serializable {}

public record UpdateNcrCommand(
    Long ncrId, String correctiveAction, Long assigneeId, Long operatorId
) implements Serializable {}

public record CloseNcrCommand(
    Long ncrId, String closeReason, String digitalSignature, Long closedBy
) implements Serializable {}

public record NcrQueryParam(
    Long workcardId, String status, String severity, int pageNum, int pageSize
) implements Serializable {}

public record WorkcardCheckinCommand(
    Long workcardId, String checkinType, Long userId
) implements Serializable {}
```

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
| 4909 | 工卡步骤未全部完成，不满足质检签署条件 |
| 4910 | 质检签署已存在（不可重复签署） |
| 4911 | 质检签署人无质检权限 |
| 4912 | NCR 不存在 |
| 4913 | NCR 状态不允许该操作（如已关闭的 NCR 不可再更新） |
| 4914 | NCR 关闭时数字签名验证失败 |
| 4915 | 工卡签到类型无效（必须为 start 或 end） |
| 4916 | 重复开工签到（已存在未完工的签到记录） |
| 4917 | 完工签到前未有开工签到记录 |
| 4918 | 存在开放 NCR，工卡不允许归档 |

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
| 1.1.0 | 2026-05-28 | 新增质检签署、NCR 管理、工卡签到模块（FR-8/9/10、quality_sign_record/ncr/workcard_checkin 数据契约、9个接口、quality:sign 权限标识、JSON Schema、Dubbo 记录、错误码 4909-4918） |
| 1.2.0 | 2026-05-29 | 目录与服务全面重组：质检签署/NCR管理/工卡签到统一归入「电子工卡」模块。前端 `pages/mro/workcard/` 含 WorkcardList/WorkcardExecute/WorkcardReport/QualityPending/QualitySign/QualityNcr，后端 `module/workcard/` 按业务功能拆分独立 Controller（WorkcardController/QualitySignController/NcrController/WorkcardCheckinController）；mro-common-dubbo 中 `WorkcardDubboService` 拆分为 4 个 Dubbo 接口（`WorkcardDubboService` 核心工卡 / `QualitySignDubboService` / `NcrDubboService` / `WorkcardCheckinDubboService`），paperless-checkin-service 微服务按相同粒度拆分 4 个 `@DubboService` 实现。API URL 不变 |
| 1.2.1 | 2026-05-29 | 前端目录扁平化：移除 `pages/mro/` 中间层，`pages/mro/workcard/` 调整为 `pages/workcard/`。仅前端目录调整，路由路径、API、后端结构均不变 |
