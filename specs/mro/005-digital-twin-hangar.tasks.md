---
id: MRO-005
plan: mro/005-digital-twin-hangar.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 数字孪生机库管理

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 搭建 Three.js 场景框架（场景/相机/渲染器/控制器） | @dev | - | 3D 场景可加载并交互（旋转/缩放/漫游）+ Code Review | todo |
| T-002 | 实现 glTF 模型加载器 + LOD 分级 + Draco 解压 | @dev | T-001 | 机库模型正常加载、远近 LOD 切换平滑 + Code Review | todo |
| T-003 | 设计并实现数据模型（hangar_model/workstation/production_plan/maintenance_order/resource_usage） | @dev | - | DDL 执行成功、MyBatis 映射正确 + Code Review | todo |
| T-004 | 实现工位管理 CRUD + 状态流转逻辑 | @dev | T-003 | 工位增删改查 + 状态机流转正确 + 单测通过 + Code Review | todo |
| T-005 | 实现 MQTT 工位状态事件消费者 | @dev | T-004 | MQTT 消息→解析→更新工位状态 + Code Review | todo |
| T-006 | 实现 WebSocket 实时推送服务（状态变更/进度变更） | @dev | T-004 | 状态变更实时推送到前端 + Code Review | todo |
| T-007 | 前端：3D 工位状态标识联动（颜色/图标随状态变化） | @dev | T-001, T-002, T-006 | WebSocket 推送→3D 标识实时更新 + Code Review | todo |
| T-008 | 实现生产计划 CRUD + 审批状态流转 | @dev | T-003 | 计划增删改查 + draft→approved→in_progress→completed + Code Review | todo |
| T-009 | 实现维修指令下发（计划拆解→指令→分配工位/人员） | @dev | T-008, T-004 | 计划审批后自动拆解为指令 + Code Review | todo |
| T-010 | 实现指令进度更新与反馈 | @dev | T-009 | 进度 0~100% 更新、状态流转 + Code Review | todo |
| T-011 | 实现资源使用记录（人员/设备/航材分配与释放） | @dev | T-009 | 资源分配/释放正常记录 + Code Review | todo |
| T-012 | 实现后端 REST API（机库/工位/计划/指令/分析） | @dev | T-004, T-008, T-009, T-010, T-011 | 全量接口可调用、权限验证通过 + Code Review | todo |
| T-013 | 实现数据分析服务（工位负载/维修效率/资源利用率） | @dev | T-012 | 分析数据准确、支持时间范围查询 + Code Review | todo |
| T-014 | 前端：生产计划管理页（列表/创建/编辑/审批） | @dev | T-012 | 计划全流程操作正常 + Code Review | todo |
| T-015 | 前端：维修指令与进度监控页 | @dev | T-012, T-006 | 指令列表/进度条/实时更新 + Code Review | todo |
| T-016 | 前端：数据分析报表页（ECharts 图表） | @dev | T-013 | 负载/效率/利用率图表正确 + Code Review | todo |
| T-017 | 前端：3D 场景与 2D 面板融合布局 | @dev | T-007, T-014, T-015, T-016 | 3D 场景与管理面板协调展示 + Code Review | todo |
| T-018 | 集成测试：IoT→状态→3D + 计划→指令→进度 全链路 | @dev | T-001~T-017 | 端到端流程通过 | todo |
| T-019 | 性能测试：首屏 ≤ 5s + 推送延迟 ≤ 5s + 50工位并发 | @dev | T-018 | 性能指标达标 + 测试报告 | todo |

> 状态枚举：todo / doing / review / done / blocked

---

## v1.1.0 增量任务 — 任务包管理 / 人员排班 / 运营看板（FR-6/7/8）

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-020 | DB 迁移：新建 task_package / task_package_order / personnel_assignment 表（V005_01） | @dev | - | Flyway migrate 成功 + Code Review | todo |
| T-021 | TaskPackage / PersonnelAssignment 实体 + Mapper + 9 个 DTO Records | @dev | T-020 | 实体映射单测通过 + Code Review | todo |
| T-022 | TaskPackageService：create / updateStatus（校验计划存在 + 状态流转）+ 4 个 Service 单测 | @dev | T-021 | 单测全部通过（含 4607/4608/4609 错误码场景）+ Code Review | todo |
| T-023 | PersonnelAssignmentService：saveAssignment（日期/重复班次校验）+ 3 个 Service 单测 | @dev | T-021 | 单测全部通过（含 4611/4612 错误码场景）+ Code Review | todo |
| T-024 | DtwinDubboService 扩展：6 个新方法含 getOperationDashboard 跨表聚合 | @dev | T-022, T-023 | Dubbo 接口集成测试通过 + Code Review | todo |
| T-025 | manage-web DtwinController：6 个 REST 端点 + MockMvc 测试 | @dev | T-024 | MockMvc 全部通过 + 权限验证正确 + Code Review | todo |
| T-026 | 前端 Mock 同步：dtwin/tasks / dtwin/assignments / dtwin/dashboard/operation 6 个端点 | @dev | T-025 | VITE_USE_MOCK=true 页面可正常调用 + Code Review | todo |
