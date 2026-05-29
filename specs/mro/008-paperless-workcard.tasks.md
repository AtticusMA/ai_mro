---
id: MRO-008
plan: mro/008-paperless-workcard.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 无纸化电子工卡

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 工卡数据模型设计 + 数据库表创建 | @dev | - | DDL 执行通过 + Code Review | todo |
| T-002 | 工卡 CRUD API（创建/查询/修改/列表） | @dev | T-001 | API 单测通过 + Code Review | todo |
| T-003 | 工卡步骤管理 API（步骤增删改+排序） | @dev | T-001 | API 单测通过 + Code Review | todo |
| T-004 | 审批状态机实现（draft→submitted→approved→issued→in_progress→completed） | @dev | T-002 | 状态机单测覆盖全路径 + Code Review | todo |
| T-005 | 审批流程 API（提交/审批通过/驳回/下发） | @dev | T-004 | 审批流程集成测试通过 + Code Review | todo |
| T-006 | 数字签名服务（RSA-2048 + SM2 双轨） | @dev | - | 签名生成/验证单测通过 + Code Review | todo |
| T-007 | 证书管理（X.509 证书签发/吊销/续期） | @dev | T-006 | 证书生命周期单测通过 + Code Review | todo |
| T-008 | 电子签署 API（步骤签署 + 工卡签署） | @dev | T-003, T-006 | 签署集成测试通过 + Code Review | todo |
| T-009 | Hyperledger Fabric 网络部署（Peer + Orderer + CA） | @dev | - | 网络启动并可执行交易 + 部署文档 | todo |
| T-010 | 链码开发（存证上链 + 哈希验证） | @dev | T-009 | 链码单测通过 + Code Review | todo |
| T-011 | 区块链网关 API（异步上链 + 验证接口） | @dev | T-010 | 上链/验证集成测试通过 + Code Review | todo |
| T-012 | 签署-上链集成（签署完成→异步上链→回写 Hash） | @dev | T-008, T-011 | 端到端链路测试通过 + Code Review | todo |
| T-013 | 人员资质数据模型 + CRUD API | @dev | - | API 单测通过 + Code Review | todo |
| T-014 | 资质动态匹配服务（机型+等级+有效期校验） | @dev | T-013 | 匹配算法单测覆盖率 ≥ 90% + Code Review | todo |
| T-015 | 工卡到期预警定时任务 + 通知推送 | @dev | T-002 | 预警触发单测通过 + 通知集成测试通过 + Code Review | todo |
| T-016 | 工卡编制前端（步骤编辑器 + 工具/航材关联） | @dev | T-002, T-003 | 页面功能验收 + Code Review | todo |
| T-017 | 工卡审批前端（审批列表 + 审批操作） | @dev | T-005 | 页面功能验收 + Code Review | todo |
| T-018 | 工卡执行前端/移动端（步骤签署 + 进度展示） | @dev | T-008 | 页面功能验收 + 离线签署测试通过 + Code Review | todo |
| T-019 | 进度监控看板前端（全局进度 + 到期预警列表） | @dev | T-015 | 页面功能验收 + Code Review | todo |
| T-020 | 区块链验证前端（签署记录查看 + 验真入口） | @dev | T-011 | 页面功能验收 + Code Review | todo |
| T-021 | 工具系统联动集成（步骤执行前校验工具清单，MRO-006） | @dev | T-003 | 联动集成测试通过 + Code Review | todo |
| T-022 | 手册平台对接（步骤内嵌手册链接跳转，MRO-004） | @dev | T-003 | 跳转功能验收 + Code Review | todo |
| T-023 | E2E 测试（完整工卡生命周期 + 区块链验证 + 联动） | @dev | T-012, T-018, T-021, T-022 | E2E 测试全部通过 + 验收报告 | todo |

> 状态枚举：todo / doing / review / done / blocked

---

## v1.1.0 增量任务 — 质检签署 / NCR 管理 / 工卡签到（FR-8/9/10）

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-024 | DB 迁移：新建 quality_sign_record / ncr / workcard_checkin 表（V008_01） | @dev | - | Flyway migrate 成功 + Code Review | todo |
| T-025 | QualitySignRecord / Ncr / WorkcardCheckin 实体 + Mapper + 8 个 DTO Records | @dev | T-024 | 实体映射单测通过 + Code Review | todo |
| T-026 | QualitySignService：qualitySign（pass→工卡归档；fail→自动创建 NCR）+ 3 个 Service 单测 | @dev | T-025 | 单测全部通过（含 4909/4910 错误码场景）+ Code Review | todo |
| T-027 | NcrService：createNcr / updateNcr / closeNcr（关闭必须电子签名）/ listNcr + 3 个 Service 单测 | @dev | T-025 | 单测全部通过（含 4911/4914/4915 错误码场景）+ Code Review | todo |
| T-028 | WorkcardCheckinService：checkin（end 前必须有 start 记录）+ 2 个 Service 单测 | @dev | T-025 | 单测全部通过（含 4916 错误码场景）+ Code Review | todo |
| T-029 | WorkcardDubboService 扩展：8 个新方法（listPendingSign / qualitySign / listNcr / createNcr / getNcr / updateNcr / closeNcr / checkin） | @dev | T-026, T-027, T-028 | Dubbo 接口集成测试通过 + Code Review | todo |
| T-030 | manage-web WorkcardController：9 个新 REST 端点 + MockMvc 测试（4 个核心场景） | @dev | T-029 | MockMvc 全部通过 + quality:sign / workcard:execute 权限验证正确 + Code Review | todo |
| T-031 | 前端 Mock 同步：workcard（pending-sign/quality-sign/checkin）+ ncr（5 个端点）共 8 个端点 | @dev | T-030 | VITE_USE_MOCK=true 页面可正常调用 + Code Review | todo |
