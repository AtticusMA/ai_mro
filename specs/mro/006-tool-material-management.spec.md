---
id: MRO-006
title: 智能工具间与航材管理
domain: mro
status: approved
owner: '@product'
version: 1.2.1
created: 2026-05-25
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: 智能工具间与航材管理

## 1. 背景与目标

通过物联网技术（特别是RFID射频识别）实现工具的自动化借还与全程追溯，以及航材库存的智能化管理。系统能够自动识别并精准记录工具借还信息，有效减少工具遗失或错放导致的维修延误。

对齐 Charter 目标：安全提升（工具离位报警防止遗留在飞机上）、效率优化（刷脸借还替代人工登记）、经济价值（智慧航材管理节省资金）。

## 2. 范围

### In Scope
- 多种身份验证（刷脸/扫码）快速借还
- RFID 一物一码数字化标识
- 自动识别与计数（借出/归还实时更新）
- 工具离位报警（超时未归预警）
- 后台远程监控（手机实时查看）
- 航材信息库（件号/库存/位置/有效期）及智能补货预警
- 航材送修全流程无纸化及 NFF 处置
- 智能工具柜多格口独立控制 + RFID 在位检测
- 温湿度传感与除湿
- RFID 读写器 ISO 18000-6C 协议支持
- 批量借还（关柜盘点，≤5秒识别200件）
- 小型工具定制 RFID 方案
- 工具柜与工卡系统联动
- 工具生命周期管理（使用次数/检定/维修/送检提醒）
- 航材领料申请（按工卡 BOM 申请、审批、领取全流程）

### Out of Scope
- 工具制造与采购流程
- 航材价格谈判与供应商管理
- 非维修类办公用品管理

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望刷脸即可快速借还工具，以便减少繁琐的人工登记流程。
- 作为航材与工具管理员，我希望系统自动监控工具库存和离位状态，以便及时发现工具遗失并预防安全隐患。
- 作为机库管理人员，我希望航材库存低于安全水位时自动预警并触发补货，以便避免因缺料导致维修延误。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持刷脸、扫码等多种身份验证方式，验证时长不超过3秒 | Given 用户站在工具柜前 When 进行刷脸/扫码认证 Then 3秒内完成验证并解锁柜门 |
| FR-2 | 配备统一编码（RFID/二维码），实现"一物一码"的数字化标识 | Given 工具已注册入系统 When 查询工具信息 Then 显示唯一RFID标签号和对应工具详情 |
| FR-3 | 具备自动识别与计数能力，借出/归还实时更新库存状态 | Given 用户关闭柜门 When RFID盘点完成 Then 系统在5秒内识别工具变化并更新借还记录 |
| FR-4 | 具备工具离位报警功能：工具超时未归系统自动预警 | Given 工具已借出且设定归还期限 When 超过期限未归还 Then 系统自动发送预警通知给借用人和管理员 |
| FR-5 | 支持后台远程监控，管理者可通过手机实时查看工具库存和遗失信息 | Given 管理员登录移动端 When 查看工具监控面板 Then 实时显示各工具柜库存状态和异常列表 |
| FR-6 | 建立航材信息库（含件号、库存量、存放位置、有效期等），支持智能补货预警 | Given 航材已录入系统 When 库存低于最低安全水位 Then 系统自动生成补货预警通知 |
| FR-7 | 支持航材送修全流程的无纸化管理和NFF数字化处置 | Given 航材需要送修 When 创建送修单 Then 全流程电子化流转（送修→检测→NFF判定→返库/报废） |
| FR-8 | 智能工具柜应支持多格口独立控制，每个格口内置RFID天线，实现工具在位实时检测 | Given 工具柜已部署 When 查看格口状态 Then 精确显示每个格口内工具的在位/缺失状态 |
| FR-9 | 工具柜需具备温湿度传感器和除湿模块，满足精密电子工具存放环境要求 | Given 精密工具存放格口 When 环境温湿度超标 Then 自动启动除湿并发送环境预警 |
| FR-10 | RFID读写器应支持ISO 18000-6C协议，读取距离可调节（0~50cm） | Given RFID系统已配置 When 工具进入读取范围 Then 按设定距离准确读取标签（0~50cm可调） |
| FR-11 | 支持批量借还：关闭柜门后自动触发盘点，一次识别最多200件RFID标签工具，识别时间≤5秒 | Given 用户归还多件工具并关门 When 系统触发盘点 Then 5秒内完成最多200件工具的识别和记录更新 |
| FR-12 | 对无法粘贴RFID标签的小型工具，采用定制蚀刻RFID挂签或嵌入树脂标签 | Given 小型工具需入库管理 When 采用蚀刻挂签/树脂标签 Then RFID系统可正常识别该工具 |
| FR-13 | 工具柜与维修工卡系统联动：工卡所需工具未全部借出时，系统禁止开工并提醒 | Given 工卡已关联所需工具清单 When 维修人员尝试开工 Then 系统检查工具借出状态，未齐全时提醒/阻断 |
| FR-14 | 提供工具生命周期管理：使用次数、检定有效期、维修历史，到期自动推送送检提醒 | Given 工具已记录检定有效期 When 距到期30天 Then 系统自动推送送检提醒给管理员 |
| FR-15 | 支持按工卡 BOM 发起航材领料申请，经审批后执行领取，库存同步扣减 | Given 工卡已下发且含航材需求 When 申请人提交领料单 Then 创建待审批申请；审批通过后可确认领取，库存相应扣减 |

## 5. 非功能需求

- 性能：身份验证 ≤ 3秒；RFID 批量盘点 ≤ 5秒（200件）
- 安全：RFID 空中接口 SM7 加密；操作日志加密存储+审计同步
- 可靠性：断电续航 ≥ 30分钟（UPS）；网络冗余（5G/WiFi 6/有线）
- 硬件：柜体防尘防水 IP54；RFID 工作频率 920~925MHz

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| tool.id | bigint | Y | PK | 工具 ID |
| tool.name | varchar(128) | Y | | 工具名称 |
| tool.tool_code | varchar(64) | Y | UK | 工具统一编码 |
| tool.rfid_tag | varchar(64) | Y | UK | RFID 标签号 |
| tool.category | varchar(64) | Y | | 工具分类 |
| tool.cabinet_id | bigint | Y | FK | 所在工具柜 |
| tool.slot_no | int | Y | | 格口号 |
| tool.status | enum | Y | in_cabinet/borrowed/lost/maintenance | 状态 |
| tool.calibration_due | date | N | | 检定到期日 |
| tool.use_count | int | Y | default 0 | 使用次数 |
| tool_cabinet.id | bigint | Y | PK | 工具柜 ID |
| tool_cabinet.name | varchar(64) | Y | | 柜体名称 |
| tool_cabinet.location | varchar(128) | Y | | 安装位置 |
| tool_cabinet.slot_count | int | Y | | 格口数量 |
| tool_cabinet.temperature | decimal(5,2) | N | | 当前温度 |
| tool_cabinet.humidity | decimal(5,2) | N | | 当前湿度 |
| tool_cabinet.online_status | enum | Y | online/offline | 在线状态 |
| borrow_record.id | bigint | Y | PK | 借还记录 ID |
| borrow_record.tool_id | bigint | Y | FK | 工具 ID |
| borrow_record.user_id | bigint | Y | FK | 借用人 |
| borrow_record.borrow_time | timestamp | Y | | 借出时间 |
| borrow_record.expected_return | timestamp | Y | | 预计归还时间 |
| borrow_record.actual_return | timestamp | N | | 实际归还时间 |
| borrow_record.status | enum | Y | borrowed/returned/overdue | 状态 |
| borrow_record.workcard_id | bigint | N | FK | 关联工卡 |
| material_item.id | bigint | Y | PK | 航材 ID |
| material_item.part_no | varchar(64) | Y | UK | 件号 |
| material_item.name | varchar(128) | Y | | 航材名称 |
| material_item.category | varchar(64) | Y | | 分类 |
| material_item.stock_qty | int | Y | | 库存数量 |
| material_item.min_stock | int | Y | | 最低库存 |
| material_item.location | varchar(128) | Y | | 存放位置 |
| material_item.expiry_date | date | N | | 有效期 |
| material_item.unit_price | decimal(12,2) | N | | 单价 |
| material_request.id | bigint | Y | PK | 领料申请 ID |
| material_request.workcard_id | bigint | Y | FK | 关联工卡 |
| material_request.requester_id | bigint | Y | FK | 申请人 |
| material_request.status | enum | Y | pending/approved/rejected/received | 申请状态 |
| material_request.items | jsonb | Y | | 领料明细（partNo/name/qty 数组） |
| material_request.approved_by | bigint | N | FK | 审批人 |
| material_request.received_at | timestamp | N | | 实际领取时间 |
| material_request.created_at | timestamp | Y | | 创建时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| POST | /api/tool/borrow | tool:borrow | 借出工具（身份验证+RFID 盘点） |
| POST | /api/tool/return | tool:return | 归还工具 |
| GET | /api/tool/cabinets | tool:inventory | 获取工具柜列表及状态 |
| GET | /api/tool/cabinets/{id}/slots | tool:inventory | 获取柜体格口工具在位情况 |
| POST | /api/tool/cabinets/{id}/inventory | tool:inventory | 触发盘点 |
| GET | /api/tool/tools | tool:inventory | 获取工具列表（支持筛选） |
| GET | /api/tool/tools/{id}/lifecycle | tool:admin | 获取工具生命周期 |
| GET | /api/tool/borrow-records | tool:inventory | 获取借还记录 |
| GET | /api/tool/alerts | tool:inventory | 获取工具预警列表（超时/遗失） |
| GET | /api/material/items | material:list | 获取航材列表 |
| POST | /api/material/items | material:restock | 新增航材入库 |
| PUT | /api/material/items/{id} | material:restock | 修改航材信息 |
| GET | /api/material/alerts | material:list | 获取补货预警列表 |
| POST | /api/material/repair-orders | material:repair | 提交航材送修单 |
| GET | /api/material/repair-orders | material:repair | 获取送修单列表 |
| MQTT | tool/cabinet/{id}/event | — | 工具柜事件上报（开柜/关柜/盘点结果） |
| GET | /api/workcards/{id}/bom | material:request | 获取工卡航材 BOM 清单 |
| GET | /api/material/requests | material:request | 获取领料申请列表（支持分页/筛选） |
| POST | /api/material/requests | material:request | 创建领料申请 |
| GET | /api/material/requests/{id} | material:request | 获取领料申请详情 |
| POST | /api/material/requests/{id}/approve | material:approve | 审批通过领料申请 |
| POST | /api/material/requests/{id}/reject | material:approve | 驳回领料申请 |
| POST | /api/material/requests/{id}/receive | material:receive | 确认领取（库存扣减） |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `tool:borrow` — 借出工具
  - `tool:return` — 归还工具
  - `tool:inventory` — 查看工具库存/预警/借还记录
  - `tool:admin` — 工具生命周期管理（检定/维修/报废）
  - `material:list` — 查看航材库存
  - `material:restock` — 航材入库/补货
  - `material:repair` — 航材送修管理
  - `material:request` — 发起/查看航材领料申请
  - `material:approve` — 审批航材领料申请
  - `material:receive` — 确认领取（库存扣减）
- 数据权限：按工具间/库房归属过滤

## 9. 验收标准

- [ ] 刷脸/扫码身份验证 ≤ 3秒
- [ ] 关柜后 RFID 批量盘点 ≤ 5秒（200件）
- [ ] 工具离位超时自动预警
- [ ] 移动端远程监控实时显示
- [ ] 航材补货预警自动触发
- [ ] 送修单全流程电子化流转
- [ ] 工具柜与工卡系统联动正常
- [ ] 检定到期自动送检提醒
- [ ] 所有接口通过权限验证
- [ ] MQTT 事件上报稳定
- [ ] 航材领料申请创建→审批→领取全流程正常
- [ ] 领取确认后库存正确扣减
- [ ] 工卡 BOM 接口返回正确航材清单

## 10. 未决问题

- [ ] 智能工具柜硬件供应商和具体型号待选型确认
- [ ] 对无法粘贴 RFID 标签的微小工具，蚀刻挂签方案的可行性需与供应商验证
- [ ] 工具柜与工卡系统联动的业务规则（未借齐禁止开工）是否为强制阻断还是仅提醒
- [ ] RFID 多通道读写器的抗干扰性能在密集金属环境下需实测验证

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

#### GET /api/workcards/{id}/bom — 工卡航材 BOM

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "workcardId": 1001,
    "items": [
      {
        "partNo": "B737-SEAL-029",
        "name": "液压系统密封圈",
        "requiredQty": 2,
        "stockQty": 8,
        "location": "B区-架位12"
      }
    ]
  },
  "timestamp": 1748304000000
}
```

#### POST /api/material/requests — 创建领料申请

Request body:
```json
{
  "workcardId": 1001,
  "items": [
    {"partNo": "B737-SEAL-029", "name": "液压系统密封圈", "qty": 2}
  ]
}
```

Response: `{"code": 0, "msg": "ok", "data": {"requestId": 8001}, "timestamp": 1748304000000}`

#### GET /api/material/requests — 领料申请列表

Request query: `?pageNum=1&pageSize=20&status=pending&workcardId=1001`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 8001,
        "workcardId": 1001,
        "workcardNo": "WC-2026-05-001",
        "requesterName": "王工程师",
        "status": "pending",
        "itemCount": 2,
        "createdAt": "2026-05-28T09:00:00Z"
      }
    ],
    "total": 5, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/material/requests/{id}/approve — 审批通过

Request body: `{"comment": "同意领用"}`

Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### POST /api/material/requests/{id}/reject — 驳回

Request body: `{"comment": "库存不足，请先等待补货"}`

Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

#### POST /api/material/requests/{id}/receive — 确认领取

Request body: `{}`

Response: `{"code": 0, "msg": "ok", "data": null, "timestamp": 1748304000000}`

MQTT 集成：工具柜事件（开柜/关柜/盘点结果）通过 MQTT 上报到 `tool/cabinet/{id}/event` topic，由 tool-service 的 MQTT Consumer 处理后写入数据库并触发预警逻辑。

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
    WorkcardBomDTO getWorkcardBom(Long workcardId);
    Long createMaterialRequest(CreateMaterialRequestCommand cmd);
    PageResult<MaterialRequestDTO> listMaterialRequests(MaterialRequestQueryParam param);
    MaterialRequestDTO getMaterialRequest(Long requestId);
    void approveMaterialRequest(Long requestId, String comment, Long approverId);
    void rejectMaterialRequest(Long requestId, String comment, Long approverId);
    void receiveMaterialRequest(Long requestId, Long operatorId);
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

public record WorkcardBomDTO(
    Long workcardId,
    List<BomItemDTO> items
) implements Serializable {}

public record BomItemDTO(
    String partNo, String name, int requiredQty, int stockQty, String location
) implements Serializable {}

public record CreateMaterialRequestCommand(
    Long workcardId, List<RequestItemCommand> items, Long requesterId
) implements Serializable {}

public record RequestItemCommand(
    String partNo, String name, int qty
) implements Serializable {}

public record MaterialRequestDTO(
    Long id, Long workcardId, String workcardNo, String requesterName,
    String status, int itemCount, Instant createdAt
) implements Serializable {}

public record MaterialRequestQueryParam(
    Long workcardId, String status, int pageNum, int pageSize
) implements Serializable {}
```

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
| 4710 | 领料申请不存在 |
| 4711 | 领料申请状态不允许该操作（如已审批的申请不可重复审批） |
| 4712 | 领料申请中的航材库存不足 |
| 4713 | 申请人无领料申请权限 |
| 4714 | 审批人无领料审批权限 |
| 4715 | 工卡状态不允许发起领料（工卡未下发） |

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
| 1.1.0 | 2026-05-28 | 新增航材领料申请模块（FR-15、material_request 数据契约、7个接口、3个权限标识、JSON Schema、Dubbo 记录、错误码 4710-4715） |
| 1.2.0 | 2026-05-29 | 目录与服务重组：领料申请统一归入「工具航材」模块。前端 `pages/mro/tool/` 含 ToolManagement/MaterialManagement/MaterialRequestList/MaterialRequestDetail，后端 `module/tool/` 含 ToolController/MaterialController/MaterialRequestController；mro-common-dubbo 中 `MaterialDubboService` 拆分为 `MaterialDubboService`（库存/补货预警/送修工单）+ `MaterialRequestDubboService`（领料申请），tooling-material-service 按相同粒度拆分两个 `@DubboService` 实现。API URL 不变 |
| 1.2.1 | 2026-05-29 | 前端目录扁平化：移除 `pages/mro/` 中间层，`pages/mro/tool/` 调整为 `pages/tool/`。仅前端目录调整，路由路径、API、后端结构均不变 |
