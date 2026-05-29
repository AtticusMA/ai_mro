# 设计: spec-builder File-Driven Mode 增强

## 概述

增强 spec-builder skill，新增 Phase 2-F（File-Driven Spec Generation），支持从结构化需求文件批量生成 SDD spec/plan/tasks 文档。解决现有 skill 逐问逐答模式无法高效处理大型项目（55+ function）的问题。

## 背景

- 项目更名：AI工作辅助系统 → 智慧机务系统
- 新增 8 大业务模块（MRO-001~008），共 59 个功能点（F01-F59）
- 需求文档已按模块拆分为独立 .md 文件（`智慧机务模块/` 目录）
- 现有 spec-builder 的交互式流程需 64-96 轮对话，效率过低

## 整体工作流程

```
Phase 0: CHARTER 更新 (v1.0.0 → v2.0.0)
    │
    ▼
Phase 1: Context Sensing (不变)
    │
    ▼
Phase 2-F: File-Driven Spec Generation (新增)
    │  对每个模块文件:
    │  1. 读取源文件 → 自动映射到 10 节模板
    │  2. 展示覆盖度 (✓/◐/✗)
    │  3. 对 ✗ 部分 AI 推导 → 展示给用户确认
    │  4. 写入 .spec.md → validate
    │  5. 用户确认 → 下一模块
    │
    ▼
Phase 3: Plan Generation (每模块, 不变)
    │
    ▼
Phase 4: Tasks Generation (每模块, 不变)
```

## Domain 划分与 ID 规划

### Domain 前缀

| Domain | 含义 | 涵盖模块 |
|--------|------|----------|
| `SYS` | 系统基础（已有） | 登录、部门、用户、角色、菜单、字典 |
| `MRO` | 智慧机务业务（新增） | 8 大业务模块 |

### ID 分配

| ID | 对应模块文件 | Spec 标题 |
|----|-------------|-----------|
| MRO-001 | 01-飞机健康管理与预测性维护系统 | 飞机健康管理与预测性维护 |
| MRO-002 | 02-AR智慧维修协作平台 | AR智慧维修协作平台 |
| MRO-003 | 03-智能排故助手系统 | 智能排故助手 |
| MRO-004 | 04-智慧维修手册管理平台 | 智慧维修手册管理 |
| MRO-005 | 05-数字孪生机库管理平台 | 数字孪生机库管理 |
| MRO-006 | 06-智能工具间与航材管理系统 | 智能工具间与航材管理 |
| MRO-007 | 07-VR-AR沉浸式培训系统 | VR/AR沉浸式培训 |
| MRO-008 | 08-无纸化电子工卡系统 | 无纸化电子工卡 |

### 横切关注点

- `09-非功能性需求.md` → 分摊到各模块 spec 第 5 节
- `10-数据架构要求.md` → ADR-004
- `11-技术架构与实施计划.md` → ADR-005

### 目录结构

```
specs/
├── CHARTER.md              (v2.0.0)
├── system/                 (已有, SYS-001~006)
├── mro/                    (新建)
│   ├── 001-health-monitoring.spec.md
│   ├── 002-ar-collaboration.spec.md
│   ├── 003-troubleshooting-assistant.spec.md
│   ├── 004-manual-management.spec.md
│   ├── 005-digital-twin-hangar.spec.md
│   ├── 006-tool-material-management.spec.md
│   ├── 007-vr-ar-training.spec.md
│   └── 008-paperless-workcard.spec.md
└── adr/
    ├── (已有 ADRs)
    ├── 004-mro-data-architecture.md
    └── 005-mro-tech-stack.md
```

### depends-on 关系

| Spec | depends-on |
|------|-----------|
| MRO-001 | ADR-004, ADR-005 |
| MRO-002 | ADR-005 |
| MRO-003 | ADR-004, ADR-005 |
| MRO-004 | ADR-005 |
| MRO-005 | ADR-004, ADR-005 |
| MRO-006 | ADR-005 |
| MRO-007 | ADR-005 |
| MRO-008 | MRO-006 |

## File-Driven 映射规则

### 自动映射表

| Spec 章节 | 源文件对应内容 | 映射方式 |
|-----------|---------------|----------|
| 1. 背景与目标 | `## 功能描述` | 直接映射 |
| 2. 范围 | `## 功能需求` 表 + `## 实施阶段` | 推导 |
| 3. 用户故事 | 00-项目概述 `目标用户` 表 × 模块功能 | AI 推导 |
| 4. 功能需求 | `## 功能需求` 表 (F01-F59) | 直接映射 + 补 GWT |
| 5. 非功能需求 | `09-非功能性需求.md` 相关指标 | 筛选映射 |
| 6. 数据契约 | 无直接内容 | AI 推导 |
| 7. 接口契约 | 无直接内容 | AI 推导 |
| 8. 权限边界 | 无直接内容 | AI 推导 |
| 9. 验收标准 | 功能需求优先级 + 指标要求 | 推导 |
| 10. 未决问题 | `## 技术要点` 中不确定项 | AI 识别 |

### 覆盖度预判

```
✓ 直接覆盖 (无需确认): 背景与目标、范围、功能需求      → 3/10
◐ 部分覆盖 (展示确认): 用户故事、非功能需求、验收标准   → 3/10
✗ 需 AI 推导 (展示确认): 数据契约、接口契约、权限边界、未决问题 → 4/10
```

### 推导规则（扩展）

在现有 Inference Rules 基础上新增：
- 实时数据采集类功能 → WebSocket/MQTT 推送端点 + 时序查询接口
- AI 分析类功能 → 异步任务提交 + 结果轮询/回调接口
- 硬件对接类功能 → 设备注册 + 状态上报 + 指令下发接口
- 文件/手册类功能 → 上传解析 + 全文搜索 + 版本对比接口
- 3D/可视化类功能 → 场景配置 + 数据订阅接口

### 每模块交互节奏

```
AI: "模块 MRO-NNN 映射完成，覆盖度如下：[✓/◐/✗ 列表]"
AI: "以下是 AI 推导的数据契约和接口契约：[展示表格]"
用户: 确认 / 修改
AI: "写入 specs/mro/NNN-slug.spec.md，验证通过。进入下一模块？"
```

## CHARTER 更新方案

### 变更项

| 章节 | 变更 |
|------|------|
| title | AI工作辅助系统 → 智慧机务系统 |
| 愿景 | 扩展：增加航空维修智能化描述 |
| In Scope | 新增 8 大 MRO 业务模块 |
| 关键约束-技术栈 | 新增：IoT(MQTT/RFID)、时序数据库(InfluxDB/TDengine)、3D引擎(Three.js/Unity)、ES搜索引擎、区块链(哈希上链)、5G专网、AI大模型(RAG) |
| 关键里程碑 | 新增 M5-M7 对应一期/二期/三期 MRO 模块 |
| version | 1.0.0 → 2.0.0 |

### 新增 ADR

- **ADR-004: MRO 数据架构** — 时序数据 vs 关系型数据分治、数据湖架构、多源接入标准
- **ADR-005: MRO 技术栈扩展** — IoT 接入层、3D 引擎、AI 大模型部署、RFID 硬件接口

### 执行顺序

1. 更新 CHARTER.md → v2.0.0
2. 生成 ADR-004, ADR-005
3. 开始 MRO-001~008 spec 生成

## Skill 改造方案

### 改动文件

`.qoder/skills/spec-builder/SKILL.md` — 新增 Phase 2-F 段落。

### 插入位置

在 `## Phase 2: Spec Generation` 和 `## Phase 2b: Spec Update` 之间。

### 不改动的部分

- Phase 1（Context Sensing）
- Phase 2（交互式，保留用于无源文件场景）
- Phase 2b（更新）
- Phase 3（Plan）
- Phase 4（Tasks）
- Hard Rules
- Error Handling

## 预期效果

| 对比项 | 原 Phase 2 | 新 Phase 2-F |
|--------|-----------|-------------|
| 每模块交互轮次 | 8-12 轮 | 2-3 轮 |
| 8 模块总交互 | 64-96 轮 | 16-24 轮 |
| 输入来源 | 用户口述 | 结构化文件 |
| 推导精度 | 逐问逐答高精度 | 批量推导+确认 |

## 审批流转

Phase 3/4 的 Gate Check 仍要求 `status: approved`。逐模块工作流中的审批节奏：

```
Phase 2-F 生成 spec (status: draft)
    → 用户 review 后手动改 status: approved
    → Phase 3 生成 plan (status: draft)
    → 用户 review 后确认
    → Phase 4 生成 tasks
    → 进入下一模块
```

为减少摩擦，支持两种节奏：
- **逐模块审批**：每个模块 spec→approve→plan→approve→tasks，再进入下一模块
- **批量审批**：先生成全部 8 个 spec (draft)，用户统一 review 后批量 approve，再逐个生成 plan/tasks

用户在启动时选择节奏。

## 实施步骤

1. 修改 SKILL.md — 插入 Phase 2-F 内容
2. 更新 CHARTER.md — v2.0.0
3. 生成 ADR-004, ADR-005
4. 逐模块执行 Phase 2-F → Phase 3 → Phase 4
