---
id: MRO-007
title: VR/AR沉浸式培训
domain: mro
status: approved
owner: '@product'
version: 1.1.0
created: 2026-05-25
updated: 2026-05-29
charter: CHARTER.md
supersedes: []
depends-on: []
---

# Spec: VR/AR沉浸式培训

## 1. 背景与目标

基于高精度数字孪生体和VR/AR技术构建虚拟仿真培训环境，实现不接触真机即可进行的实操训练。培训中AI系统实时记录学员拆装步骤、工具使用规范度、故障判断速度，建立个人"能力档案"。

对齐 Charter 目标：人员赋能（降低新手学习门槛，VR模拟替代真机训练）、安全提升（高危场景可反复演练，无安全风险）。

## 2. 范围

### In Scope
- 高精度飞机数字孪生模型（含物理特性和故障逻辑）
- AR 头盔虚实融合训练
- VR 沉浸式模拟训练（航线绕机、发动机拆装等）
- 高危/突发故障场景模拟
- AI 行为采集与量化评估
- 个人培训档案与技能考核报告
- 多学员协同演练

### Out of Scope
- VR/AR 硬件设备制造
- 非维修类的通用企业培训
- 考试认证与适航执照发放（属于管理局职责）

## 3. 用户故事 / 使用场景

- 作为培训部门，我希望创建各种维修场景的 VR 模拟课程，以便新员工无需接触真机即可完成实操训练。
- 作为一线机务维修人员（学员），我希望在 VR 环境中反复练习高危场景操作，以便在不冒风险的情况下掌握应急处置能力。
- 作为培训部门主管，我希望通过 AI 量化评估学员表现并生成能力报告，以便精准识别技能短板并制定个性化培养计划。

## 4. 功能需求

| ID | 需求 | 验收标准 |
|----|------|----------|
| FR-1 | 构建高精度飞机数字孪生模型，复刻物理细节（螺栓螺纹、管路角度、叶片轨迹），包含物理特性和故障逻辑 | Given 3D模型已制作 When 学员在VR中操作部件 Then 部件按真实物理特性响应（重力/阻力/卡扣反馈） |
| FR-2 | 支持AR头盔佩戴下的虚实融合训练，虚拟部件与实景环境精确匹配 | Given 学员佩戴AR头盔在实际机库 When 触发AR培训模式 Then 虚拟部件准确叠加在对应物理位置 |
| FR-3 | 支持VR沉浸式模拟训练，涵盖航线绕机检查、发动机拆装等典型维修场景 | Given 场景已发布 When 学员进入VR培训 Then 可完整执行场景内所有操作步骤 |
| FR-4 | 具备高危/突发故障场景模拟能力（如发动机突发停转、恶劣天气抢修等），支持反复演练 | Given 高危场景已创建 When 学员选择该场景 Then 可反复进入演练，每次初始条件一致 |
| FR-5 | 实现培训全过程的AI行为采集与分析，包括拆装步骤正确性、工具使用规范度等指标量化评估 | Given 培训会话进行中 When 学员执行操作 Then AI实时采集行为数据并在会话结束后生成量化评分 |
| FR-6 | 建立个人培训档案，输出技能考核报告，追踪学员能力提升曲线 | Given 学员完成多次培训 When 查看个人档案 Then 展示各项技能评分趋势和能力雷达图 |
| FR-7 | 支持多学员同时在线培训和协同演练 | Given 协同场景已创建 When 多名学员同时加入 Then 各学员可在同一虚拟空间协作操作 |

## 5. 非功能需求

- 性能：VR 渲染帧率 ≥ 72fps；AR 注册精度 ≤ 5mm
- 安全：培训数据按学员权限隔离
- 可用性：VR 头盔重量 ≤ 500g；连续使用无眩晕感
- 并发：支持 ≥ 10人同时在线协同演练

## 6. 数据契约

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| training_scenario.id | bigint | Y | PK | 培训场景 ID |
| training_scenario.name | varchar(128) | Y | | 场景名称 |
| training_scenario.category | enum | Y | line_check/engine/emergency/component | 场景分类 |
| training_scenario.difficulty | enum | Y | beginner/intermediate/advanced | 难度等级 |
| training_scenario.model_url | varchar(512) | Y | | 3D 场景模型地址 |
| training_scenario.duration_minutes | int | Y | | 预计时长（分钟） |
| training_scenario.status | enum | Y | draft/published/archived | 状态 |
| trainee_profile.id | bigint | Y | PK | 学员档案 ID |
| trainee_profile.user_id | bigint | Y | FK, UK | 关联系统用户 |
| trainee_profile.skill_level | enum | Y | junior/mid/senior | 当前技能等级 |
| trainee_profile.total_training_hours | decimal(8,2) | Y | default 0 | 累计培训时长 |
| trainee_profile.last_assessment_date | date | N | | 最近考核日期 |
| training_session.id | bigint | Y | PK | 培训会话 ID |
| training_session.scenario_id | bigint | Y | FK | 关联场景 |
| training_session.trainee_id | bigint | Y | FK | 学员 |
| training_session.mode | enum | Y | vr/ar/collaborative | 培训模式 |
| training_session.started_at | timestamp | Y | | 开始时间 |
| training_session.ended_at | timestamp | N | | 结束时间 |
| training_session.status | enum | Y | in_progress/completed/aborted | 状态 |
| skill_assessment.id | bigint | Y | PK | 评估记录 ID |
| skill_assessment.session_id | bigint | Y | FK | 关联培训会话 |
| skill_assessment.metric_name | varchar(64) | Y | | 评估指标名称 |
| skill_assessment.score | decimal(5,2) | Y | 0~100 | 得分 |
| skill_assessment.detail | jsonb | N | | 评估详情（步骤正确性/工具规范度等） |
| skill_assessment.assessed_at | timestamp | Y | | 评估时间 |

## 7. 接口契约

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/training/scenarios | train:manage_scenario | 获取培训场景列表 |
| POST | /api/training/scenarios | train:manage_scenario | 创建培训场景 |
| PUT | /api/training/scenarios/{id} | train:manage_scenario | 修改培训场景 |
| POST | /api/training/scenarios/{id}/publish | train:manage_scenario | 发布场景 |
| GET | /api/training/trainees | train:assign | 获取学员列表 |
| GET | /api/training/trainees/{id}/profile | train:assess | 获取学员能力档案 |
| POST | /api/training/sessions | train:assign | 创建培训任务（分配学员+场景） |
| GET | /api/training/sessions | train:assign | 获取培训会话列表 |
| GET | /api/training/sessions/{id} | train:assess | 获取培训会话详情 |
| GET | /api/training/assessments/{sessionId} | train:assess | 获取评估结果 |
| GET | /api/training/reports/individual/{traineeId} | train:report | 生成个人技能考核报告 |
| GET | /api/training/reports/overview | train:report | 培训整体统计概览 |
| WS | /ws/training/realtime/{sessionId} | train:assess | 实时行为采集推送 |

## 8. 权限边界

- 菜单/按钮权限标识：
  - `train:manage_scenario` — 管理培训场景（创建/编辑/发布）
  - `train:assign` — 分配培训任务、查看学员列表
  - `train:assess` — 查看评估结果和学员档案
  - `train:report` — 生成/查看考核报告
- 数据权限：按培训部门/基地过滤

## 9. 验收标准

- [ ] VR 场景可正常加载并交互（帧率 ≥ 72fps）
- [ ] AR 虚实融合精度 ≤ 5mm
- [ ] 高危场景可反复演练
- [ ] AI 行为采集与量化评分正常
- [ ] 个人能力档案和考核报告可正常生成
- [ ] 多学员协同演练功能正常（≥ 10人）
- [ ] 所有接口通过权限验证

## 10. 未决问题

- [ ] VR 头盔品牌选型（影响 SDK 和内容制作工具链）
- [ ] 3D 数字孪生模型的精度要求（螺栓级 vs 部件级），制作成本与效果的平衡
- [ ] 多学员协同演练的网络架构（P2P vs 服务器中转）
- [ ] AI 行为采集的指标定义和评分算法需与培训专家共同制定

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

## Changelog

| 版本 | 日期 | 变更 |
|------|------|------|
| 0.1.0 | 2026-05-25 | 初稿 |
| 1.0.0 | 2026-05-26 | 补充后端实现约束（JSON Schema / Dubbo 接口 / 错误码） |
| 1.1.0 | 2026-05-29 | 补充前端管理后台页面需求（P5里程碑）：场景管理、学员列表/档案、培训任务、AI评估详情 5 个页面，含 echarts 可视化（雷达图/折线图/柱状图） |
