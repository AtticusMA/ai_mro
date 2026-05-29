---
id: MRO-007
plan: mro/007-vr-ar-training.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: VR/AR沉浸式培训

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 培训场景 CRUD + 数据模型（MySQL） | @dev | - | API 单测通过 + Code Review | todo |
| T-002 | 学员档案 CRUD + 技能等级管理 | @dev | - | API 单测通过 + Code Review | todo |
| T-003 | 培训会话管理（创建/结束/状态机） | @dev | T-001, T-002 | 状态机单测覆盖全路径 + Code Review | todo |
| T-004 | WebSocket 网关搭建（行为事件实时推送通道） | @dev | - | 连接建立/断线重连集成测试通过 + Code Review | todo |
| T-005 | Unity XR 项目骨架 + XR Interaction Toolkit 配置 | @dev | - | Quest/Pico 双平台编译通过 + Code Review | todo |
| T-006 | 通用交互系统（抓取/旋转/拆装/按钮） | @dev | T-005 | 基础交互 Demo 可运行 + Code Review | todo |
| T-007 | 场景加载器（远程资源包下载 + 加载 + 缓存） | @dev | T-005 | 场景加载单测通过 + Code Review | todo |
| T-008 | 行为事件采集 SDK（Unity 端埋点 + Protobuf 序列化） | @dev | T-004, T-006 | 事件上报集成测试通过 + Code Review | todo |
| T-009 | AI 规则引擎（步骤正确性/工具规范度/时间指标） | @dev | - | 评分算法单测覆盖率 ≥ 90% + Code Review | todo |
| T-010 | AI 评分服务（消费行为流 + 规则匹配 + 评分计算） | @dev | T-004, T-009 | 评分集成测试通过 + Code Review | todo |
| T-011 | 评估结果存储 + 档案更新（技能评分趋势） | @dev | T-002, T-010 | 档案更新单测通过 + Code Review | todo |
| T-012 | VR 场景制作 — 航线绕机检查 | @dev | T-006, T-007 | 场景可完整执行 + 帧率 ≥ 72fps | todo |
| T-013 | VR 场景制作 — 发动机拆装 | @dev | T-006, T-007 | 场景可完整执行 + 物理反馈正常 | todo |
| T-014 | VR 场景制作 — 高危突发故障演练 | @dev | T-006, T-007 | 场景可反复进入 + 初始条件一致 | todo |
| T-015 | 场景管理前端（列表/创建/编辑/发布 + Three.js 预览） | @dev | T-001 | 页面功能验收 + Code Review | todo |
| T-016 | 学员档案前端（能力雷达图 + 技能趋势图） | @dev | T-002, T-011 | 页面功能验收 + Code Review | todo |
| T-017 | 考核报告前端（个人报告 + 整体统计概览） | @dev | T-011 | 页面功能验收 + Code Review | todo |
| T-018 | 培训任务分配前端（学员+场景匹配 + 会话列表） | @dev | T-003 | 页面功能验收 + Code Review | todo |
| T-019 | Photon 多人协同集成（房间管理 + 状态同步） | @dev | T-005, T-006 | 10 人同时在线测试通过 + Code Review | todo |
| T-020 | AR 模式适配（HoloLens SLAM + 虚实注册） | @dev | T-005, T-006 | AR 注册精度 ≤ 5mm 测试通过 | todo |
| T-021 | E2E 测试（完整培训流程 + 评估报告生成） | @dev | T-012, T-010, T-015 | E2E 测试全部通过 + 验收报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
