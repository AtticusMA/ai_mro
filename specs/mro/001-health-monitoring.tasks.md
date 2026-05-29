---
id: MRO-001
plan: mro/001-health-monitoring.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 飞机健康管理与预测性维护

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 部署 EMQX MQTT Broker 集群，配置 TLS + 认证 | @dev | - | Broker 启动、TLS 握手成功、客户端可连接 + Code Review | todo |
| T-002 | 部署 InfluxDB 实例，创建 database + retention policy | @dev | - | 数据库可写入/查询、保留策略生效 + Code Review | todo |
| T-003 | 部署 Kafka 集群，创建 topic（raw-flight-data, fault-events） | @dev | - | 生产者/消费者通信成功 + Code Review | todo |
| T-004 | 实现 ACMS/QAR 数据报文解析器（支持多格式） | @dev | T-001 | 单测覆盖主要报文格式、解析正确率 100% + Code Review | todo |
| T-005 | 实现 MQTT→Kafka→InfluxDB 写入链路 | @dev | T-001, T-002, T-003, T-004 | 端到端数据流通、延迟 ≤ 30s + Code Review | todo |
| T-006 | 实现故障自动识别引擎（规则引擎 + 异常检测） | @dev | T-005 | 可识别预定义故障模式、生成 fault_record + Code Review | todo |
| T-007 | 实现告警服务（分级预警生成 + 推送通知） | @dev | T-006 | 预警按规则触发、通知到达 + Code Review | todo |
| T-008 | 实现趋势预测模型训练 Pipeline | @dev | T-005 | 模型可训练、输出预测结果 + Code Review | todo |
| T-009 | 实现预测推理服务（定时任务 + 报告生成） | @dev | T-008 | 定时生成 prediction_report、结果可查询 + Code Review | todo |
| T-010 | 实现后端 REST API（机队状态、故障记录、预测报告、统计） | @dev | T-005, T-006, T-009 | API 全量接口可调用、权限验证通过 + Code Review | todo |
| T-011 | 实现预警规则 CRUD API（创建/修改/删除规则） | @dev | T-007, T-010 | 规则 CRUD 正常、规则变更立即生效 + Code Review | todo |
| T-012 | 实现 WebSocket 实时推送服务 | @dev | T-007, T-010 | 预警实时推送到前端、连接稳定 + Code Review | todo |
| T-013 | 实现数据导出接口（健康报告导出） | @dev | T-010 | 导出 Excel/PDF 成功 + Code Review | todo |
| T-014 | 前端：机队健康状态列表页 | @dev | T-010 | 列表展示、分页筛选正常 + Code Review | todo |
| T-015 | 前端：单机健康详情页（故障记录 + 预测报告） | @dev | T-010 | 详情页数据完整、图表正常 + Code Review | todo |
| T-016 | 前端：24小时监控大屏 | @dev | T-012 | 大屏实时更新、WebSocket 连接稳定 + Code Review | todo |
| T-017 | 前端：预警管理页（预警列表 + 规则配置） | @dev | T-011, T-012 | 预警列表实时刷新、规则 CRUD 正常 + Code Review | todo |
| T-018 | 前端：故障统计分析页 | @dev | T-010 | 图表数据准确、筛选联动正常 + Code Review | todo |
| T-019 | 集成测试：全链路端到端验证 | @dev | T-001~T-018 | MQTT→解析→存储→识别→预警→前端 全流程通过 + Code Review | todo |
| T-020 | 性能测试：InfluxDB 写入压测 + API P95 验证 | @dev | T-019 | 写入 ≥ 1000 pts/s、API P95 < 2s + 报告 | todo |
| T-021 | 安全审计：TLS 验证 + 权限隔离测试 | @dev | T-019 | TLS 证书有效、越权访问被拒 + 报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
