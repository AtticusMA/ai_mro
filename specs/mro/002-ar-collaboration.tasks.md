---
id: MRO-002
plan: mro/002-ar-collaboration.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: AR智慧维修协作平台

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 部署 WebRTC SFU (mediasoup) + 信令服务 (WebSocket) | @dev | - | SFU 可建立视频会话、信令握手成功 + Code Review | todo |
| T-002 | 部署 MinIO 对象存储，配置 bucket 策略 | @dev | - | 文件上传/下载正常、过期清理策略生效 + Code Review | todo |
| T-003 | 实现 AR 巡检路径模板管理（CRUD） | @dev | - | 路径模板增删改查、数据持久化 + Code Review | todo |
| T-004 | AI 视觉识别：数据标注 + YOLOv8 模型训练 | @dev | - | 模型 mAP ≥ 80%（盖板/安全销检测）+ 训练报告 | todo |
| T-005 | AI 推理服务部署（边缘节点 Jetson + API 封装） | @dev | T-004 | 推理延迟 ≤ 1s、REST API 可调用 + Code Review | todo |
| T-006 | 实现巡检任务模块（创建/开始/完成/异常记录） | @dev | T-003, T-005 | 巡检全流程 CRUD + AI 联动 + Code Review | todo |
| T-007 | 实现异常检测联动（AI→弹窗→自动截图存档） | @dev | T-005, T-006, T-002 | 异常触发→截图存 MinIO→记录入库 + Code Review | todo |
| T-008 | 实现远程协作：一键呼叫 + 专家加入 + 结束会话 | @dev | T-001 | 呼叫→通知→接入→视频通话建立 + Code Review | todo |
| T-009 | 实现画面标注实时同步（Canvas + WebSocket） | @dev | T-008 | 标注实时到达对端、多种标注工具可用 + Code Review | todo |
| T-010 | 实现手册推送功能（协作中推送手册链接到 AR 端） | @dev | T-008 | 专家推送→AR 端显示手册内容 + Code Review | todo |
| T-011 | 实现影像录制与归档（SFU 录制→MinIO 存储） | @dev | T-001, T-002, T-008 | 会话结束后录像可回放 + Code Review | todo |
| T-012 | 实现后端 REST API（巡检/会话/异常/影像全量接口） | @dev | T-006, T-007, T-008, T-011 | 所有接口可调用、权限验证通过 + Code Review | todo |
| T-013 | 实现 WebSocket 信令接口（WebRTC 信令 + 标注同步） | @dev | T-001, T-009 | 信令协商正常、标注实时同步 + Code Review | todo |
| T-014 | 前端：巡检任务管理页 | @dev | T-012 | 任务列表/详情/异常记录展示正常 + Code Review | todo |
| T-015 | 前端：远程协作页（视频窗口 + 标注工具 + 手册面板） | @dev | T-012, T-013 | 专家端视频/标注/手册推送功能正常 + Code Review | todo |
| T-016 | 前端：影像档案回放页 | @dev | T-012 | 影像列表/播放/下载正常 + Code Review | todo |
| T-017 | AR 眼镜端联调（巡检引导 + AI 叠加 + 远程协作） | @dev | T-005, T-006, T-008 | AR 端全流程跑通 + Code Review | todo |
| T-018 | 集成测试：全链路端到端验证 | @dev | T-001~T-017 | 巡检→识别→告警 + 呼叫→视频→标注 全流程通过 | todo |
| T-019 | 性能测试：视频延迟 ≤ 500ms + AI ≤ 1s 验证 | @dev | T-018 | 性能指标达标 + 测试报告 | todo |
| T-020 | 安全审计：TLS 验证 + 权限隔离 + 影像访问控制 | @dev | T-018 | 安全测试通过 + 报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
