---
id: MRO-005
title: 数字孪生机库管理
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

# Spec: 数字孪生机库管理

## 1. 背景与目标

通过数字孪生技术构建机库运行的虚拟映射，实现对维修基地一线现场的全方位数字化管理，涵盖从飞机入场到维修交付的主要维修流程，包括工序、质量、安环、设备、工具等环节的数据采集、组织、分析、集成与管理。

对齐 Charter 目标：管理转型（推动维修保障模式从"经验依赖"转向"数据驱动"）、效率优化（生产过程可视化，资源统筹调度）。

## 2. 范围

### In Scope
- 机库全场景三维数字孪生模型
- 维修过程实时数据映射
- 生产计划数字化编排（计划→指令→执行→反馈→归档）
- 生产过程监控（工位进度/资源使用）
- 维修数据可视化分析
- 任务包管理（工序分组、优先级排序、多人协同）
- 人员工位排班（班次计划、实时在岗状态）
- 运营看板（机位/工卡/预警/NCR 实时汇总，面向机库管理层）

### Out of Scope
- 机库建筑物理施工与改造
- 飞机结构强度仿真分析
- 财务成本核算系统

## 3. 用户故事 / 使用场景

- 作为机库管理人员，我希望在3D数字孪生界面上查看机库全场景布局和各工位实时状态，以便统筹生产调度。
- 作为一线机务维修人员，我希望在数字孪生中查看我负责工位的任务进度和资源情况，以便高效执行维修。
- 作为安全质量监管部门，我希望通过可视化数据分析追踪维修质量趋势，以便做出数据驱动的管理决策。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 建立机库全场景的三维数字孪生模型，还原飞机、设备、工装等空间布局 | Given 3D模型已制作并上传 When 用户打开数字孪生界面 Then 完整展示机库三维场景，可旋转/缩放/漫游 |
| FR-2 | 实现维修过程的实时数据映射，将物理世界中的工序进度同步更新至数字孪生空间 | Given IoT传感器/人工反馈已接入 When 工位状态变更 Then 3D场景中对应工位在5秒内同步更新状态标识 |
| FR-3 | 支持生产计划的数字化编排，实现从计划到指令、执行、反馈、归档的全流程闭环管理 | Given 管理员创建生产计划 When 计划审批通过 Then 自动拆解为维修指令并分配到工位 |
| FR-4 | 具备生产过程监控功能，实时展示各工位的维修进度和资源使用情况 | Given 维修指令执行中 When 查看监控面板 Then 展示各工位进度百分比和当前资源占用 |
| FR-5 | 支持维修数据的可视化分析，为管理决策提供数据支撑 | Given 历史维修数据已积累 When 查看分析报表 Then 展示工位负载、维修效率、资源利用率等指标 |
| FR-6 | 支持任务包管理：将多条维修指令打包为任务包，支持优先级排序和多人协同 | Given 计划审批通过 When 管理员创建任务包并关联维修指令 Then 任务包可分配给多名人员，各人员可独立更新进度 |
| FR-7 | 支持人员工位排班：按班次制定工位人员分配计划，实时显示在岗状态 | Given 排班计划已创建 When 查看排班面板 Then 展示每个工位当前班次的在岗人员列表和出勤状态 |
| FR-8 | 提供运营看板：实时汇总机位占用、工卡进度、到期预警、开放 NCR 数等核心指标，供机库管理层决策 | Given 各子系统数据已接入 When 管理层打开运营看板 Then 展示当日机位/工卡/预警/NCR 汇总数据，每30秒自动刷新 |

## 5. 非功能需求

- 性能：3D 场景首屏加载 ≤ 5秒；实时状态更新延迟 ≤ 5秒
- 安全：数据传输 TLS 加密；操作审计日志
- 可用性：系统核心服务可用性 ≥ 99.95%
- 兼容性：支持主流浏览器 WebGL 渲染（Chrome/Edge/Firefox）

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| hangar_model.id | bigint | Y | PK | 机库模型 ID |
| hangar_model.name | varchar(128) | Y | | 机库名称 |
| hangar_model.model_url | varchar(512) | Y | | 3D 模型文件地址 |
| hangar_model.version | varchar(32) | Y | | 模型版本 |
| hangar_model.updated_at | timestamp | Y | | 最后更新时间 |
| workstation.id | bigint | Y | PK | 工位 ID |
| workstation.hangar_id | bigint | Y | FK | 所属机库 |
| workstation.name | varchar(64) | Y | | 工位名称 |
| workstation.position_x | decimal(10,2) | Y | | 3D 坐标 X |
| workstation.position_y | decimal(10,2) | Y | | 3D 坐标 Y |
| workstation.position_z | decimal(10,2) | Y | | 3D 坐标 Z |
| workstation.status | enum | Y | idle/occupied/maintenance | 工位状态 |
| workstation.current_aircraft_id | varchar(32) | N | | 当前停靠飞机 |
| production_plan.id | bigint | Y | PK | 生产计划 ID |
| production_plan.hangar_id | bigint | Y | FK | 所属机库 |
| production_plan.aircraft_id | varchar(32) | Y | | 飞机注册号 |
| production_plan.plan_type | enum | Y | line/heavy/component | 计划类型 |
| production_plan.scheduled_start | timestamp | Y | | 计划开始时间 |
| production_plan.scheduled_end | timestamp | Y | | 计划结束时间 |
| production_plan.status | enum | Y | draft/approved/in_progress/completed | 计划状态 |
| maintenance_order.id | bigint | Y | PK | 维修指令 ID |
| maintenance_order.plan_id | bigint | Y | FK | 关联计划 |
| maintenance_order.workstation_id | bigint | Y | FK | 分配工位 |
| maintenance_order.assignee_id | bigint | Y | FK | 负责人 |
| maintenance_order.description | text | Y | | 指令描述 |
| maintenance_order.progress | int | Y | 0~100 | 完成进度% |
| maintenance_order.status | enum | Y | pending/executing/completed/blocked | 状态 |
| resource_usage.id | bigint | Y | PK | 资源使用记录 ID |
| resource_usage.workstation_id | bigint | Y | FK | 关联工位 |
| resource_usage.resource_type | enum | Y | personnel/equipment/material | 资源类型 |
| resource_usage.resource_id | bigint | Y | | 资源 ID |
| resource_usage.allocated_at | timestamp | Y | | 分配时间 |
| resource_usage.released_at | timestamp | N | | 释放时间 |
| task_package.id | bigint | Y | PK | 任务包 ID |
| task_package.plan_id | bigint | Y | FK | 关联生产计划 |
| task_package.name | varchar(128) | Y | | 任务包名称 |
| task_package.priority | enum | Y | high/normal/low | 优先级 |
| task_package.status | enum | Y | pending/executing/completed | 状态 |
| task_package.created_at | timestamp | Y | | 创建时间 |
| personnel_assignment.id | bigint | Y | PK | 排班记录 ID |
| personnel_assignment.workstation_id | bigint | Y | FK | 关联工位 |
| personnel_assignment.user_id | bigint | Y | FK | 人员 |
| personnel_assignment.shift | enum | Y | day/evening/night | 班次 |
| personnel_assignment.shift_date | date | Y | | 排班日期 |
| personnel_assignment.status | enum | Y | scheduled/on_duty/off_duty | 状态 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/dtwin/hangars | dtwin:view | 获取机库列表 |
| GET | /api/dtwin/hangars/{id}/model | dtwin:view | 获取3D模型数据 |
| GET | /api/dtwin/hangars/{id}/workstations | dtwin:view | 获取工位列表及状态 |
| GET | /api/dtwin/plans | dtwin:plan | 获取生产计划列表 |
| POST | /api/dtwin/plans | dtwin:plan | 创建生产计划 |
| PUT | /api/dtwin/plans/{id} | dtwin:plan | 修改生产计划 |
| GET | /api/dtwin/orders | dtwin:monitor | 获取维修指令列表 |
| POST | /api/dtwin/orders | dtwin:plan | 下发维修指令 |
| PUT | /api/dtwin/orders/{id}/progress | dtwin:monitor | 更新指令进度 |
| GET | /api/dtwin/analytics/workload | dtwin:analyze | 工位负载分析 |
| GET | /api/dtwin/analytics/efficiency | dtwin:analyze | 维修效率分析 |
| WS | /ws/dtwin/realtime | dtwin:monitor | 实时状态推送（工位/进度变更） |
| GET | /api/dtwin/tasks | dtwin:plan | 获取任务包列表 |
| POST | /api/dtwin/tasks | dtwin:plan | 创建任务包 |
| PUT | /api/dtwin/tasks/{id}/status | dtwin:monitor | 更新任务包状态 |
| GET | /api/dtwin/assignments | dtwin:plan | 获取人员排班列表 |
| POST | /api/dtwin/assignments | dtwin:plan | 创建/修改排班 |
| GET | /api/dtwin/dashboard/operation | dtwin:monitor | 运营看板汇总数据 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `dtwin:view` — 查看机库3D场景和工位状态
  - `dtwin:plan` — 创建/修改生产计划、下发维修指令
  - `dtwin:monitor` — 监控维修进度、更新进度
  - `dtwin:analyze` — 查看数据分析报表
- 数据权限：按机库/维修基地过滤

## 9. 验收标准

- [ ] 3D 数字孪生场景正常加载（首屏 ≤ 5秒）
- [ ] 工位状态实时同步（延迟 ≤ 5秒）
- [ ] 生产计划 CRUD 及审批流程正常
- [ ] 维修指令从计划自动拆解并分配
- [ ] 监控面板展示进度和资源使用
- [ ] 分析报表数据准确
- [ ] 所有接口通过权限验证
- [ ] 任务包创建/状态更新正常
- [ ] 人员排班列表正确显示在岗状态
- [ ] 运营看板汇总数据准确且自动刷新

## 10. 未决问题

- [ ] 3D 模型的制作精度标准和更新频率需确认
- [ ] WebGL 渲染大场景时的性能优化策略（LOD 分级/模型轻量化）
- [ ] IoT 传感器（工位占用检测）的硬件选型和部署方案待定

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

#### GET /api/dtwin/tasks — 任务包列表

Request query: `?pageNum=1&pageSize=20&planId=2001&status=executing`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 4001,
        "planId": 2001,
        "name": "发动机分解检查-A",
        "priority": "high",
        "status": "executing",
        "orderCount": 3,
        "completedOrders": 1,
        "createdAt": "2026-05-20T08:00:00Z"
      }
    ],
    "total": 8, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### POST /api/dtwin/tasks — 创建任务包

Request body:
```json
{
  "planId": 2001,
  "name": "发动机分解检查-A",
  "priority": "high",
  "orderIds": [3001, 3002]
}
```

Response: `{"code": 0, "msg": "ok", "data": {"id": 4001}, "timestamp": 1748304000000}`

#### GET /api/dtwin/assignments — 人员排班列表

Request query: `?workstationId=101&shiftDate=2026-05-28`

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "list": [
      {
        "id": 5001,
        "workstationId": 101,
        "workstationName": "C检工位A",
        "userId": 101,
        "userName": "王工程师",
        "shift": "day",
        "shiftDate": "2026-05-28",
        "status": "on_duty"
      }
    ],
    "total": 12, "pageNum": 1, "pageSize": 20
  },
  "timestamp": 1748304000000
}
```

#### GET /api/dtwin/dashboard/operation — 运营看板

Response:
```json
{
  "code": 0, "msg": "ok",
  "data": {
    "totalWorkcards": 28,
    "inProgress": 12,
    "pendingSign": 3,
    "overdue": 2,
    "completedToday": 5,
    "openNcr": 4,
    "bayStatus": [
      {"bayId": 101, "bayName": "C检工位A", "status": "occupied", "aircraftId": "B-1234", "completionRate": 45},
      {"bayId": 102, "bayName": "C检工位B", "status": "idle", "aircraftId": null, "completionRate": 0}
    ],
    "alerts": [
      {"workcardId": 1001, "cardNo": "WC-2026-05-001", "title": "B-1234 液压系统检查", "hoursUntilDue": 20.5, "level": "warning"}
    ]
  },
  "timestamp": 1748304000000
}
```

WebSocket 实时推送：工位/进度状态变更时，dtwin-service 写 Redis Pub/Sub，manage-web 的 WebSocket Handler 订阅后推送给前端。

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
    PageResult<TaskPackageDTO> listTaskPackages(TaskPackageQueryParam param);
    Long createTaskPackage(CreateTaskPackageCommand cmd);
    void updateTaskPackageStatus(Long taskId, String status, Long operatorId);
    PageResult<PersonnelAssignmentDTO> listAssignments(AssignmentQueryParam param);
    void saveAssignment(SaveAssignmentCommand cmd);
    OperationDashboardDTO getOperationDashboard(UserContextDTO ctx);
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

public record TaskPackageDTO(
    Long id, Long planId, String name, String priority, String status,
    int orderCount, int completedOrders, Instant createdAt
) implements Serializable {}

public record CreateTaskPackageCommand(
    Long planId, String name, String priority, List<Long> orderIds, Long createdBy
) implements Serializable {}

public record TaskPackageQueryParam(
    Long planId, String status, int pageNum, int pageSize
) implements Serializable {}

public record PersonnelAssignmentDTO(
    Long id, Long workstationId, String workstationName,
    Long userId, String userName, String shift, LocalDate shiftDate, String status
) implements Serializable {}

public record SaveAssignmentCommand(
    Long workstationId, Long userId, String shift, LocalDate shiftDate, Long operatorId
) implements Serializable {}

public record AssignmentQueryParam(
    Long workstationId, LocalDate shiftDate, int pageNum, int pageSize
) implements Serializable {}

public record OperationDashboardDTO(
    int totalWorkcards, int inProgress, int pendingSign,
    int overdue, int completedToday, int openNcr,
    List<BayStatusDTO> bayStatus, List<DashboardAlertDTO> alerts
) implements Serializable {}

public record BayStatusDTO(
    Long bayId, String bayName, String status, String aircraftId, int completionRate
) implements Serializable {}

public record DashboardAlertDTO(
    Long workcardId, String cardNo, String title, double hoursUntilDue, String level
) implements Serializable {}
```

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
| 4607 | 任务包不存在 |
| 4608 | 任务包状态不允许该操作 |
| 4609 | 任务包关联的维修指令不属于同一计划 |
| 4610 | 排班记录不存在 |
| 4611 | 该工位该班次该日期已有排班记录（唯一约束冲突） |
| 4612 | 排班日期不得早于今日 |

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
| 1.1.0 | 2026-05-28 | 新增任务包管理、人员排班、运营看板模块（FR-6/7/8、task_package/personnel_assignment 数据契约、6个接口、JSON Schema、Dubbo 记录、错误码 4607-4612） |
| 1.2.0 | 2026-05-29 | 目录与服务全面重组：机库概览/数字孪生/运维看板/维修计划统一归入「机库管理」模块。前端 `pages/mro/dtwin/`、后端 `module/dtwin/` 按业务功能拆分独立 Controller（DtwinController/TaskPackageController/PersonnelAssignmentController/OperationDashboardController）；mro-common-dubbo 中 `DtwinDubboService` 拆分为 4 个 Dubbo 接口（`HangarDubboService` / `ProductionPlanDubboService` / `TaskPackageDubboService` / `PersonnelAssignmentDubboService`），digital-twin-service 微服务按相同粒度拆分 4 个 `@DubboService` 实现。API URL 不变 |
| 1.2.1 | 2026-05-29 | 前端目录扁平化：移除 `pages/mro/` 中间层，`pages/mro/dtwin/` 调整为 `pages/dtwin/`。仅前端目录调整，路由路径、API、后端结构均不变 |
