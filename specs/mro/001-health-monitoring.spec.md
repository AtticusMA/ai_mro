---
id: MRO-001
title: 飞机健康管理与预测性维护
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

# Spec: 飞机健康管理与预测性维护

## 1. 背景与目标

通过空地数据链采集飞机ACMS（飞机状态监控系统）、QAR（快速访问记录器）等数据，利用人工智能技术对数据进行实时分析，实现飞机故障的实时识别、精准定位和趋势预测，推动维修模式从"预防性维修"向"预测性维修"转型升级。

对齐 Charter 目标：安全提升（故障识别效率提升40%，人工误判概率降低30%）、效率优化（故障响应时间从"小时级"压缩至"分钟级"）。

## 2. 范围

### In Scope
- 实时数据采集与解析（ACMS/QAR）
- 故障自动识别与精准定位
- 故障趋势分析与预测
- 24小时全天候安全运行态势监控
- 分级预警与可视化
- 故障信息统计数据库

### Out of Scope
- 飞行控制系统在线修改
- 航空器零部件制造
- 航班调度与签派系统

## 3. 用户故事 / 使用场景

- 作为一线机务维修人员，我希望实时查看飞机健康状态和预警信息，以便及时发现潜在故障。
- 作为技术支援工程师，我希望查看故障趋势预测报告，以便制定预防性维修方案。
- 作为机库管理人员，我希望查看机队整体健康状态概览，以便合理安排维修资源。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 支持对ACMS和QAR数据的实时采集与解析，数据采集延迟不超过30秒 | Given ACMS/QAR数据源已接入 When 飞机产生新数据 Then 系统在30秒内完成采集并解析入库 |
| FR-2 | 具备故障自动识别能力，能够识别异常数据并精准定位故障部件 | Given 传感器数据持续输入 When 数据出现异常模式 Then 系统自动识别并定位故障部件，生成故障记录 |
| FR-3 | 构建故障趋势分析模型，预测故障可能发生的时间、位置和严重程度 | Given 历史故障数据和实时数据已积累 When 用户查看趋势报告 Then 系统展示故障预测（时间/位置/严重度）及置信度 |
| FR-4 | 实现24小时全天候安全运行态势监控，提前识别安全隐患 | Given 系统运行中 When 任意时刻 Then 监控大屏实时展示所有在线飞机的健康态势 |
| FR-5 | 支持自定义推送分级警告，实现机队和单机健康状态的可视化预警 | Given 管理员已配置预警规则 When 飞机健康指标触发阈值 Then 系统按规则推送对应级别预警（红/橙/黄） |
| FR-6 | 建立故障信息统计数据库，为维修人员提供有价值的信息参考 | Given 故障记录已积累 When 维修人员查询故障统计 Then 系统返回按机型/部件/时间段的统计分析结果 |

## 5. 非功能需求

- 性能：实时数据采集与呈现延迟 ≤ 30秒；数据查询响应时间 ≤ 2秒
- 安全：数据传输 TLS 1.3 加密；敏感数据国密 SM4 加密存储
- 可用性：系统核心服务可用性 ≥ 99.95%
- 可靠性：故障检测模块全年误报率 ≤ 5%
- 数据备份：每日自动备份，RTO ≤ 4小时，RPO ≤ 24小时

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| flight_data.id | bigint | Y | PK, auto_increment | 飞行数据记录 ID |
| flight_data.aircraft_id | varchar(32) | Y | FK | 飞机注册号 |
| flight_data.data_source | enum | Y | ACMS/QAR/FDR | 数据来源类型 |
| flight_data.collected_at | timestamp | Y | | 采集时间 |
| flight_data.payload | jsonb | Y | | 原始报文数据 |
| fault_record.id | bigint | Y | PK | 故障记录 ID |
| fault_record.aircraft_id | varchar(32) | Y | FK | 关联飞机 |
| fault_record.fault_code | varchar(64) | Y | | 故障代码 |
| fault_record.severity | enum | Y | critical/major/minor | 严重程度 |
| fault_record.component | varchar(128) | Y | | 故障部件 |
| fault_record.detected_at | timestamp | Y | | 检测时间 |
| fault_record.status | enum | Y | open/confirmed/resolved | 处理状态 |
| health_alert.id | bigint | Y | PK | 预警记录 ID |
| health_alert.aircraft_id | varchar(32) | Y | FK | 关联飞机 |
| health_alert.alert_level | enum | Y | red/orange/yellow | 预警等级 |
| health_alert.predicted_fault_time | timestamp | N | | 预测故障时间 |
| health_alert.message | text | Y | | 预警内容 |
| prediction_report.id | bigint | Y | PK | 趋势报告 ID |
| prediction_report.aircraft_id | varchar(32) | Y | FK | 关联飞机 |
| prediction_report.model_version | varchar(32) | Y | | 预测模型版本 |
| prediction_report.predicted_at | timestamp | Y | | 预测生成时间 |
| prediction_report.result | jsonb | Y | | 预测结果（故障概率/时间/位置） |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/health/aircraft | health:list | 获取机队健康状态列表 |
| GET | /api/health/aircraft/{id} | health:view | 获取单机健康详情 |
| GET | /api/health/aircraft/{id}/faults | health:view | 获取单机故障记录 |
| GET | /api/health/aircraft/{id}/predictions | health:view | 获取趋势预测报告 |
| GET | /api/health/alerts | health:list | 获取预警列表（支持分页/筛选） |
| PUT | /api/health/alerts/{id}/acknowledge | health:config | 确认/处理预警 |
| POST | /api/health/alert-rules | health:config | 创建自定义预警规则 |
| PUT | /api/health/alert-rules/{id} | health:config | 修改预警规则 |
| DELETE | /api/health/alert-rules/{id} | health:config | 删除预警规则 |
| GET | /api/health/statistics | health:view | 故障统计数据库查询 |
| WS | /ws/health/realtime | health:view | 实时健康态势推送（WebSocket） |
| GET | /api/health/export | health:export | 导出健康报告 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `health:list` — 查看机队健康列表
  - `health:view` — 查看单机详情/故障记录/预测报告
  - `health:config` — 配置预警规则、确认预警
  - `health:export` — 导出健康报告
- 数据权限：按维修基地/机队归属过滤

## 9. 验收标准

- [ ] ACMS/QAR 数据采集延迟 ≤ 30秒
- [ ] 故障自动识别准确率符合预期（误报率 ≤ 5%）
- [ ] 趋势预测报告可正常生成并展示
- [ ] 预警规则配置与推送功能正常
- [ ] 24小时监控大屏无中断运行
- [ ] 故障统计查询响应时间 ≤ 2秒
- [ ] 所有接口通过权限验证
- [ ] WebSocket 实时推送稳定

## 10. 未决问题

- [ ] ACMS/QAR 数据接入的具体协议版本与报文格式需与航空公司确认
- [ ] AI 故障预测模型的训练数据来源和初始准确率目标待定
- [ ] 是否需要对接空管系统获取航班计划来辅助预测

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
