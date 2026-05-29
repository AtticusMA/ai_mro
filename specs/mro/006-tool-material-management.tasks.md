---
id: MRO-006
plan: mro/006-tool-material-management.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 智能工具间与航材管理

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | RFID 读写器抽象接口设计 + 工厂模式 | @dev | - | 接口定义完成 + 单测通过 + Code Review | todo |
| T-002 | RFID 协议解析器实现（Impinj/Zebra 两品牌适配） | @dev | T-001 | 解析单测覆盖率 ≥ 90% + Code Review | todo |
| T-003 | RFID 事件上报（MQTT 发布 + 消息格式定义） | @dev | T-002 | MQTT 发布集成测试通过 + Code Review | todo |
| T-004 | RFID 心跳检测 + 断线重连 + 离线缓存 | @dev | T-003 | 异常场景单测通过 + Code Review | todo |
| T-005 | 人脸识别服务集成（InsightFace 推理 + 特征库管理） | @dev | - | 识别准确率 ≥ 99% + API 单测通过 + Code Review | todo |
| T-006 | 柜控身份验证模块（人脸 + 二维码双模） | @dev | T-005 | 双模认证集成测试通过 + Code Review | todo |
| T-007 | 格口电磁锁控制 + 状态监测 | @dev | - | 硬件接口协议文档 + 模拟测试通过 + Code Review | todo |
| T-008 | 盘点逻辑实现（关门触发 + 差异比对 + 事件上报） | @dev | T-003, T-007 | 盘点差异计算单测通过 + 集成测试通过 + Code Review | todo |
| T-009 | 柜控离线模式（本地 SQLite 缓存 + 恢复同步） | @dev | T-008 | 断网模拟测试通过 + Code Review | todo |
| T-010 | 工具台账 CRUD + 数据模型（MySQL） | @dev | - | API 单测通过 + Code Review | todo |
| T-011 | 工具借还状态机实现 + RFID 事件消费 | @dev | T-003, T-010 | 状态机单测覆盖全路径 + Code Review | todo |
| T-012 | 超时未归检测定时任务 + 预警通知 | @dev | T-011 | 定时任务单测通过 + 通知集成测试通过 + Code Review | todo |
| T-013 | 工具生命周期管理（校验到期提醒 + 报废流程） | @dev | T-010 | 生命周期单测通过 + Code Review | todo |
| T-014 | 工具使用统计 + 热力图数据聚合（InfluxDB） | @dev | T-011 | 聚合查询单测通过 + Code Review | todo |
| T-015 | 航材库存 CRUD + 数据模型 | @dev | - | API 单测通过 + Code Review | todo |
| T-016 | 航材批次追溯 + 适用机型关联 | @dev | T-015 | 追溯查询单测通过 + Code Review | todo |
| T-017 | 安全库存预警 + 智能补货建议（消耗速率算法） | @dev | T-015 | 预警逻辑单测通过 + Code Review | todo |
| T-018 | 航材送修流程（申请→审批→出库→跟踪→回库） | @dev | T-015 | 流程状态机单测通过 + Code Review | todo |
| T-019 | 工具管理后台前端（台账 + 借还记录 + 统计看板） | @dev | T-010, T-011, T-014 | 页面功能验收 + Code Review | todo |
| T-020 | 航材管理后台前端（库存 + 预警 + 送修） | @dev | T-015, T-017, T-018 | 页面功能验收 + Code Review | todo |
| T-021 | 移动端监控页面（实时状态 + 告警推送） | @dev | T-012, T-017 | 移动端功能验收 + Code Review | todo |
| T-022 | 工卡系统联动接口（工具清单校验 + 阻断/提醒） | @dev | T-011 | 联动集成测试通过 + Code Review | todo |
| T-023 | 硬件联调（真实工具柜 + RFID 批量识别 ≤5s/200件） | @dev | T-008, T-019 | 硬件联调报告 + 性能达标 | todo |
| T-024 | E2E 测试（完整借还流程 + 航材出入库流程） | @dev | T-019, T-020, T-022 | E2E 测试全部通过 + 验收报告 | todo |

> 状态枚举：todo / doing / review / done / blocked

---

## v1.1.0 增量任务 — 航材领料申请（FR-15）

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-025 | DB 迁移：新建 material_request 表（V006_01） | @dev | - | Flyway migrate 成功 + Code Review | todo |
| T-026 | MaterialRequest 实体 + Mapper + 6 个 DTO Records | @dev | T-025 | 实体映射单测通过 + Code Review | todo |
| T-027 | MaterialRequestService：createRequest / approveRequest / rejectRequest / receiveRequest（含库存扣减事务） | @dev | T-026 | 7 个 Service 单测通过（含事务回滚场景）+ Code Review | todo |
| T-028 | MaterialDubboService 扩展：新增 7 个航材领料方法 | @dev | T-027 | Dubbo 接口调用集成测试通过 + Code Review | todo |
| T-029 | manage-web MaterialRequestController：7 个 REST 端点 + MockMvc 测试 | @dev | T-028 | MockMvc 全部通过 + 权限验证正确 + Code Review | todo |
| T-030 | 前端 Mock 同步：material/requests 7 个端点 | @dev | T-029 | VITE_USE_MOCK=true 页面可正常调用 + Code Review | todo |
