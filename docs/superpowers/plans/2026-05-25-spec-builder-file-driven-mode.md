# spec-builder File-Driven Mode 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 增强 spec-builder skill 支持 File-Driven 模式，并基于智慧机务需求文档完成 CHARTER 更新、ADR 生成、8 个模块的 spec/plan/tasks 全套 SDD 文档。

**Architecture:** 修改 SKILL.md 新增 Phase 2-F 段落；更新 CHARTER.md 至 v2.0.0；新建 `specs/mro/` 目录承载 8 个 spec；新建 ADR-004/005 记录技术决策。所有输出必须通过 `npm run specs:validate`。

**Tech Stack:** Markdown (YAML front-matter), Node.js validation scripts, existing SDD template system.

---

## File Structure

### 修改的文件

| 文件 | 职责 |
|------|------|
| `.qoder/skills/spec-builder/SKILL.md` | 新增 Phase 2-F 段落 |
| `specs/CHARTER.md` | 项目更名 + 技术栈扩展 + 里程碑扩展 (v2.0.0) |

### 新建的文件

| 文件 | 职责 |
|------|------|
| `specs/adr/004-mro-data-architecture.md` | MRO 数据架构决策 |
| `specs/adr/005-mro-tech-stack.md` | MRO 技术栈扩展决策 |
| `specs/mro/001-health-monitoring.spec.md` | 飞机健康管理与预测性维护 spec |
| `specs/mro/002-ar-collaboration.spec.md` | AR 智慧维修协作平台 spec |
| `specs/mro/003-troubleshooting-assistant.spec.md` | 智能排故助手 spec |
| `specs/mro/004-manual-management.spec.md` | 智慧维修手册管理 spec |
| `specs/mro/005-digital-twin-hangar.spec.md` | 数字孪生机库管理 spec |
| `specs/mro/006-tool-material-management.spec.md` | 智能工具间与航材管理 spec |
| `specs/mro/007-vr-ar-training.spec.md` | VR/AR 沉浸式培训 spec |
| `specs/mro/008-paperless-workcard.spec.md` | 无纸化电子工卡 spec |

Plan 和 Tasks 文件（在 spec approved 后生成）：
- `specs/mro/NNN-<slug>.plan.md` × 8
- `specs/mro/NNN-<slug>.tasks.md` × 8

---

## Task 1: 修改 SKILL.md — 插入 Phase 2-F

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: 读取当前 SKILL.md 确认插入锚点**

确认 `## Phase 2: Spec Generation` 段落结束位置和 `## Phase 2b: Spec Update` 开始位置。

- [ ] **Step 2: 插入 Phase 2-F 段落**

在 `## Phase 2: Spec Generation` 之后、`## Phase 2b: Spec Update` 之前插入以下完整内容：

```markdown
## Phase 2-F: File-Driven Spec Generation (批量文件输入模式)

### 触发条件

用户输入包含以下信号时进入此模式（而非交互式 Phase 2）：
- "基于 [目录/文件] 生成 spec"
- "从需求文档导入"
- "批量生成"
- 提供了包含结构化需求表格的文件路径

### 前置条件

- CHARTER 已更新到最新版本（包含目标模块的 In Scope 声明）
- 相关 ADR 已生成（如涉及新技术栈）

### 流程

1. **扫描源目录**
   - 读取用户指定目录下所有 .md 文件
   - 识别模块文件（含 `## 功能需求` 表格的文件）vs 横切文件（非功能需求/架构）
   - 呈现识别结果，确认模块顺序

2. **逐模块映射** — 对每个模块文件执行：

   a) **自动映射** — 直接提取：
      - `## 功能描述` → Spec 第 1 节（背景与目标）
      - `## 功能需求` 表格 → Spec 第 4 节（FR-N + GWT 验收标准）
      - `## 实施阶段` → Spec 第 2 节（范围标注）

   b) **筛选映射** — 从横切文件中筛选：
      - 非功能需求文件中与本模块相关的指标 → Spec 第 5 节

   c) **AI 推导** — 基于功能需求推导：
      - 用户故事（角色 × 功能 → "作为X，我希望Y，以便Z"）
      - 数据契约（CRUD 实体 → 字段表）
      - 接口契约（功能 → REST 端点）
      - 权限边界（模块 × 操作 → `<module>:<action>`）
      - 未决问题（技术要点中的不确定项）

3. **展示与确认** — 每模块一次性展示：

   ```
   ## MRO-NNN: [模块名]

   覆盖度：✓ 背景 | ✓ 范围 | ◐ 用户故事 | ✓ 功能需求 | ◐ 非功能 | ✗ 数据契约 | ✗ 接口契约 | ✗ 权限 | ◐ 验收 | ✗ 未决

   ### AI 推导内容（需确认）：
   [展示数据契约表格]
   [展示接口契约表格]
   [展示权限标识列表]
   [展示用户故事]
   [展示未决问题]

   确认 / 修改？
   ```

4. **写入与验证**
   - 组装完整 .spec.md
   - 写入 `specs/<domain>/<NNN>-<slug>.spec.md`
   - 执行 `npm run specs:validate`
   - 通过后提示："MRO-NNN spec 已生成。继续下一模块？"

### 推导规则（扩展）

在现有 Inference Rules 基础上新增：
- 实时数据采集类功能 → WebSocket/MQTT 推送端点 + 时序查询接口
- AI 分析类功能 → 异步任务提交 + 结果轮询/回调接口
- 硬件对接类功能 → 设备注册 + 状态上报 + 指令下发接口
- 文件/手册类功能 → 上传解析 + 全文搜索 + 版本对比接口
- 3D/可视化类功能 → 场景配置 + 数据订阅接口

### 审批流转

Phase 3/4 的 Gate Check 仍要求 `status: approved`。支持两种节奏：
- **逐模块审批**：每个模块 spec→approve→plan→approve→tasks，再进入下一模块
- **批量审批**：先生成全部 spec (draft)，用户统一 review 后批量 approve，再逐个生成 plan/tasks

用户在启动时选择节奏。
```

- [ ] **Step 3: 验证 SKILL.md 格式正确**

肉眼检查：Phase 2-F 段落位于 Phase 2 和 Phase 2b 之间，Markdown heading 层级正确（`##` 为顶层段落，`###` 为子节）。

- [ ] **Step 4: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(skill): add Phase 2-F file-driven spec generation to spec-builder"
```

---

## Task 2: 更新 CHARTER.md 至 v2.0.0

**Files:**
- Modify: `specs/CHARTER.md`

- [ ] **Step 1: 更新 YAML front-matter**

```yaml
---
id: CHARTER
title: 智慧机务系统 — 项目宪章
status: approved
version: 2.0.0
created: 2026-05-23
updated: 2026-05-25
owner: '@product'
---
```

- [ ] **Step 2: 更新愿景**

替换第 1 节内容为：

```markdown
## 1. 愿景

构建一套面向航空 MRO（维修、修理和大修）领域的 **智慧机务系统**，以完善的权限/数据权限管理为底座，以 AI、IoT、数字孪生、AR/VR 等前沿技术为引擎，支撑航空维修从"经验驱动"向"数据驱动"转型升级。
```

- [ ] **Step 3: 更新范围 In Scope**

替换 `### In Scope（首期）` 为：

```markdown
### In Scope

**基础底座（已有）：**
- 管理后台：登录、部门、用户、角色、菜单/权限、字典 6 大基础模块。
- 五种数据权限（全部 / 本部门 / 本部门及子部门 / 本人 / 自定义部门）。
- RBAC + JWT 认证。

**智慧机务业务模块（新增）：**
- 飞机健康管理与预测性维护系统
- AR 智慧维修协作平台
- 智能排故助手系统
- 智慧维修手册管理平台
- 数字孪生机库管理平台
- 智能工具间与航材管理系统
- VR/AR 沉浸式培训系统
- 无纸化电子工卡系统
```

- [ ] **Step 4: 更新技术栈约束**

替换关键约束表为：

```markdown
## 4. 关键约束

| 类别 | 约束 |
|------|------|
| 技术栈-前端 | Vue 3 + Element Plus + Tailwind；3D 可视化 Three.js/Unity WebGL |
| 技术栈-后端 | Spring Boot + Nacos + Dubbo + Redis + MySQL 8 |
| 技术栈-AI | RAG + 民航领域微调大模型；ES 分布式搜索引擎 |
| 技术栈-IoT | MQTT + Kafka + RFID(ISO 18000-6C)；时序数据库 InfluxDB/TDengine |
| 技术栈-XR | AR 眼镜 SDK；VR 头盔 + Unity 仿真引擎 |
| 技术栈-安全 | 区块链哈希上链（工卡签署）；国密 SM4/SM7；TLS 1.3 |
| 安全 | 密码 BCrypt；JWT；动态 SQL 数据权限；多因素认证；XSS/SQL注入/CSRF 防护 |
| 性能 | 数据查询 P95 < 2s；AI 图像识别 < 1s；排故方案 < 3min；视频推流延迟 < 500ms |
| 可用性 | 核心服务可用性 ≥ 99.95%；支持双活数据中心 |
| 并发 | ≥ 500 人同时在线 |
| 文档 | 所有需求/设计/决策必须落入 `specs/` |
```

- [ ] **Step 5: 更新关键里程碑**

替换里程碑表为：

```markdown
## 5. 关键里程碑

| 阶段 | 目标 |
|------|------|
| M1 | 前端骨架 + Mock + 6 大基础模块 UI 跑通 |
| M2 | 后端认证 + 用户/部门 + JWT |
| M3 | 角色/菜单/字典 + 数据权限拦截器 |
| M4 | 前后端联调 + 安全/性能验收 |
| M5 | MRO 一期：预测性维护 + 电子工卡 + 智能排故（核心机型） |
| M6 | MRO 二期：AR 协作 + 手册管理 + 智能工具间 |
| M7 | MRO 三期：数字孪生 + VR 培训 + 全机型覆盖 + 系统集成 |
```

- [ ] **Step 6: 运行验证**

Run: `npm run specs:validate`
Expected: PASS（无错误输出）

- [ ] **Step 7: Commit**

```bash
git add specs/CHARTER.md
git commit -m "feat(charter): upgrade to v2.0.0 - rename to 智慧机务, add MRO modules and extended tech stack"
```

---

## Task 3: 生成 ADR-004 (MRO 数据架构)

**Files:**
- Create: `specs/adr/004-mro-data-architecture.md`

- [ ] **Step 1: 创建 ADR-004 文件**

写入以下内容：

```markdown
---
id: ADR-004
title: MRO 数据架构 — 多源异构数据分治策略
status: accepted
date: 2026-05-25
deciders: ['@arch']
---

# ADR-004: MRO 数据架构 — 多源异构数据分治策略

## Context

智慧机务系统需接入多种异构数据源：飞行传感器时序数据、结构化维修记录、非结构化技术文档、实时影像流、IoT 设备事件（RFID 标签读取/位置/状态）。单一数据库无法满足所有场景的性能和存储需求。

## Decision

采用"分治存储 + 统一治理"策略：

| 数据类型 | 存储方案 | 理由 |
|---------|---------|------|
| 结构化业务数据 | MySQL 8 + ES 搜索引擎 | 事务一致性 + 全文检索 |
| 时序传感器数据 | InfluxDB / TDengine | 高并发写入、时间范围查询优化 |
| 非结构化文档 | 对象存储 (MinIO/OSS) | 大文件存储、CDN 分发 |
| 知识库向量数据 | Milvus / Elasticsearch dense_vector | RAG 语义检索 |
| IoT 事件流 | Kafka → InfluxDB + MySQL | 削峰缓冲、事件溯源 |
| 影像数据 | 对象存储 + 元数据索引 (MySQL) | 大容量 + 可检索 |

数据治理要求：
- 结构化数据完整率 ≥ 99%
- 非结构化数据解析成功率 ≥ 95%
- IoT 事件保留周期 ≥ 3 年
- 遵循《民航数据治理规范》

数据传输安全：
- 远程传输 TLS 1.3
- 敏感数据国密 SM4 加密
- RFID 空中接口 SM7 对称加密
- 数据分级分类存储，核心数据异地备份

## Consequences

### 正面
- 各类数据使用最适合的存储引擎，性能最优
- 时序数据库解决高并发传感器写入瓶颈
- 向量数据库支撑 RAG 知识检索

### 负面
- 运维复杂度增加（需维护多种数据库）
- 跨库查询需通过应用层或数据中台协调
- 数据一致性需依赖事件驱动最终一致性模型

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| 全部 MySQL | 时序数据写入性能不足，向量检索能力缺失 |
| 全部 PostgreSQL + TimescaleDB | 单引擎方案，IoT 高并发场景性能不如专用时序库 |
| 数据湖 (Hadoop/Spark) | 对本项目实时性要求而言过重，运维成本过高 |

## References

- 关联 Charter: `CHARTER.md` §4 技术栈
- 源文档: `智慧机务模块/10-数据架构要求.md`
```

- [ ] **Step 2: 运行验证**

Run: `npm run specs:validate`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add specs/adr/004-mro-data-architecture.md
git commit -m "feat(adr): ADR-004 MRO data architecture - multi-source heterogeneous data strategy"
```

---

## Task 4: 生成 ADR-005 (MRO 技术栈扩展)

**Files:**
- Create: `specs/adr/005-mro-tech-stack.md`

- [ ] **Step 1: 创建 ADR-005 文件**

写入以下内容：

```markdown
---
id: ADR-005
title: MRO 技术栈扩展 — 端边云协同架构
status: accepted
date: 2026-05-25
deciders: ['@arch']
---

# ADR-005: MRO 技术栈扩展 — 端边云协同架构

## Context

智慧机务 8 大业务模块引入了原有基础底座未覆盖的技术领域：IoT 设备接入、实时音视频协作、AI 大模型推理、3D 数字孪生渲染、RFID 硬件交互、VR/AR 沉浸式体验。需要在现有技术栈基础上进行扩展，采用"端—边—云"协同的四层架构。

## Decision

### 总体架构：四层体系

| 层级 | 职责 | 关键技术 |
|------|------|----------|
| 感知层 | 硬件设备数据采集 | AR 眼镜、智能工具柜、RFID 读写器、温湿度传感器、摄像头 |
| 网络层 | 泛在连接 | 5G 专网、WiFi 6、物联网网关、MQTT Broker |
| 平台层 | 数据融合与算法引擎 | 数据中台、AI 中台、物联网中台 |
| 应用层 | 业务模块 | 8 大 MRO 应用 + 基础管理底座 |

### 技术选型

| 技术领域 | 选型 | 备选 | 选择理由 |
|---------|------|------|----------|
| IoT 接入 | EMQX (MQTT Broker) | Mosquitto | 集群能力强，百万级连接 |
| 消息队列 | Kafka | RabbitMQ | 高吞吐事件流，IoT 场景适配 |
| 时序数据库 | InfluxDB 3.0 | TDengine | 生态成熟，InfluxQL 查询便捷 |
| AI 推理 | vLLM + RAG Pipeline | Ollama | 高性能批量推理，生产级部署 |
| 向量检索 | Elasticsearch dense_vector | Milvus | 复用现有 ES 集群，减少运维组件 |
| 3D 引擎(Web) | Three.js | Babylon.js | 社区大，与 Vue 3 集成案例多 |
| 3D 引擎(VR) | Unity | Unreal | 航空维修培训场景案例丰富 |
| AR SDK | 厂商 SDK (依 AR 眼镜品牌) | — | 需与硬件选型联动确定 |
| RFID 中间件 | 自研适配层 | 商用 RFID 中间件 | 需对接多品牌读写器，自研灵活性高 |
| 区块链 | Hyperledger Fabric (许可链) | 长安链 | 企业级许可链，工卡签署上链 |
| 边缘计算 | NVIDIA Jetson + K3s | 华为 Atlas | AI 推理边缘部署，机库端低延迟 |

### 通信协议

| 场景 | 协议 | 要求 |
|------|------|------|
| IoT 设备→平台 | MQTT 3.1.1 / 5.0 | QoS 1，TLS 加密 |
| RFID 读写器→中间件 | TCP + 自定义帧协议 / LLRP | 实时性 < 100ms |
| AR 视频协作 | WebRTC + SFU | 延迟 < 500ms |
| 浏览器→后端 | HTTPS REST + WebSocket | JWT 鉴权 |

## Consequences

### 正面
- 四层解耦，各层独立演进
- 边缘计算解决机库环境低延迟 AI 推理需求
- MQTT + Kafka 组合应对 IoT 高并发场景

### 负面
- 技术栈复杂度大幅增加，团队需补充 IoT/AI/3D 技能
- 多硬件品牌适配工作量大（AR 眼镜、RFID 读写器、智能工具柜）
- 边缘节点运维需额外投入

## Alternatives Considered

| 方案 | 拒绝理由 |
|------|----------|
| 纯云端架构 | 机库网络不稳定时 AI 推理中断，无法满足实时性要求 |
| 全私有化部署 | 初期投入过大，不利于快速迭代验证 |
| 采购商用 IoT 平台 | 定制化程度不足，与 RFID 工具柜等专用设备集成困难 |

## References

- 关联 Charter: `CHARTER.md` §4 技术栈
- 源文档: `智慧机务模块/11-技术架构与实施计划.md`
- 关联 ADR: ADR-004 (数据架构)
```

- [ ] **Step 2: 运行验证**

Run: `npm run specs:validate`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add specs/adr/005-mro-tech-stack.md
git commit -m "feat(adr): ADR-005 MRO tech stack extension - edge-cloud collaborative architecture"
```

---

## Task 5: 创建 specs/mro/ 目录

**Files:**
- Create: `specs/mro/` (directory)

- [ ] **Step 1: 创建目录**

```bash
mkdir -p specs/mro
```

- [ ] **Step 2: 验证目录存在**

```bash
ls specs/mro/
```

Expected: 空目录，无错误

---

## Task 6: 生成 MRO-001 Spec (飞机健康管理与预测性维护)

**Files:**
- Create: `specs/mro/001-health-monitoring.spec.md`
- Reference: `智慧机务模块/01-飞机健康管理与预测性维护系统.md`
- Reference: `智慧机务模块/09-非功能性需求.md`
- Reference: `智慧机务模块/00-项目概述.md` (用户角色表)

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：
- `## 功能描述` → 第 1 节（背景与目标）
- `## 功能需求` F01-F06 → 第 4 节 FR-1~FR-6 + GWT 验收标准
- `## 实施阶段` "一期" → 第 2 节 In Scope

筛选映射：
- 从 09 文件筛选：实时数据采集延迟 ≤30s、数据备份 RTO≤4h/RPO≤24h、系统可用性 ≥99.95%

AI 推导：
- 用户故事（一线机务、技术工程师、管理人员 × 健康管理功能）
- 数据契约（flight_data, fault_record, health_alert, prediction_report 等实体）
- 接口契约（数据采集、故障查询、趋势预测、预警配置等 REST + WebSocket 端点）
- 权限边界（health:list, health:view, health:config, health:export）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

展示数据契约表、接口契约表、权限标识、用户故事，等待用户确认或修改。

- [ ] **Step 3: 组装并写入 spec 文件**

写入 `specs/mro/001-health-monitoring.spec.md`，包含完整 YAML front-matter：

```yaml
---
id: MRO-001
title: 飞机健康管理与预测性维护
domain: mro
status: draft
owner: '@product'
version: 0.1.0
created: 2026-05-25
updated: 2026-05-25
charter: CHARTER.md
supersedes: []
depends-on: [ADR-004, ADR-005]
---
```

- [ ] **Step 4: 运行验证**

Run: `npm run specs:validate`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add specs/mro/001-health-monitoring.spec.md
git commit -m "feat(spec): MRO-001 health monitoring and predictive maintenance

Refs: MRO-001"
```

---

## Task 7: 生成 MRO-002 Spec (AR 智慧维修协作平台)

**Files:**
- Create: `specs/mro/002-ar-collaboration.spec.md`
- Reference: `智慧机务模块/02-AR智慧维修协作平台.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F07-F13 → FR-1~FR-7
筛选映射：视频推流延迟 ≤500ms、AI 图像识别 ≤1s
AI 推导：数据契约（inspection_task, ar_session, anomaly_record, video_archive）、接口契约（巡检任务、视频会话、异常识别、影像回放）、权限边界（ar:inspect, ar:call, ar:review, ar:archive）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装并写入 spec 文件**

```yaml
---
id: MRO-002
title: AR智慧维修协作平台
domain: mro
status: draft
owner: '@product'
version: 0.1.0
created: 2026-05-25
updated: 2026-05-25
charter: CHARTER.md
supersedes: []
depends-on: [ADR-005]
---
```

- [ ] **Step 4: 运行验证并 Commit**

```bash
git add specs/mro/002-ar-collaboration.spec.md
git commit -m "feat(spec): MRO-002 AR smart maintenance collaboration platform

Refs: MRO-002"
```

---

## Task 8: 生成 MRO-003 Spec (智能排故助手)

**Files:**
- Create: `specs/mro/003-troubleshooting-assistant.spec.md`
- Reference: `智慧机务模块/03-智能排故助手系统.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F14-F20 → FR-1~FR-7
筛选映射：排故方案生成 ≤3min
AI 推导：数据契约（knowledge_base, fault_query, troubleshooting_report, repair_history）、接口契约（知识库管理、故障查询、方案生成、历史统计）、权限边界（tshoot:query, tshoot:manage_kb, tshoot:export, tshoot:history）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/003-troubleshooting-assistant.spec.md
git commit -m "feat(spec): MRO-003 intelligent troubleshooting assistant

Refs: MRO-003"
```

---

## Task 9: 生成 MRO-004 Spec (智慧维修手册管理)

**Files:**
- Create: `specs/mro/004-manual-management.spec.md`
- Reference: `智慧机务模块/04-智慧维修手册管理平台.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F21-F26 → FR-1~FR-6
筛选映射：翻译准确率 ≥95%
AI 推导：数据契约（manual_document, manual_version, translation_task, search_index）、接口契约（手册上传解析、翻译任务、版本管理、全文搜索、多端同步）、权限边界（manual:upload, manual:translate, manual:edit, manual:search, manual:publish）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/004-manual-management.spec.md
git commit -m "feat(spec): MRO-004 smart maintenance manual management

Refs: MRO-004"
```

---

## Task 10: 生成 MRO-005 Spec (数字孪生机库管理)

**Files:**
- Create: `specs/mro/005-digital-twin-hangar.spec.md`
- Reference: `智慧机务模块/05-数字孪生机库管理平台.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F27-F31 → FR-1~FR-5
筛选映射：系统可用性 ≥99.95%
AI 推导：数据契约（hangar_model, workstation, production_plan, maintenance_order, resource_usage）、接口契约（3D 场景加载、工位状态、生产计划 CRUD、进度监控 WebSocket、数据分析报表）、权限边界（dtwin:view, dtwin:plan, dtwin:monitor, dtwin:analyze）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/005-digital-twin-hangar.spec.md
git commit -m "feat(spec): MRO-005 digital twin hangar management

Refs: MRO-005"
```

---

## Task 11: 生成 MRO-006 Spec (智能工具间与航材管理)

**Files:**
- Create: `specs/mro/006-tool-material-management.spec.md`
- Reference: `智慧机务模块/06-智能工具间与航材管理系统.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F32-F45 → FR-1~FR-14（该模块功能最多，14 个功能需求）
筛选映射：RFID 识别时间 ≤5s、身份验证 ≤3s
AI 推导：数据契约（tool, tool_cabinet, rfid_tag, borrow_record, material_item, material_stock, inspection_schedule）、接口契约（工具借还、RFID 盘点、柜体状态、航材库存 CRUD、补货预警、生命周期管理、送修流程）、权限边界（tool:borrow, tool:return, tool:inventory, tool:admin, material:list, material:restock, material:repair）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/006-tool-material-management.spec.md
git commit -m "feat(spec): MRO-006 intelligent tool room and material management

Refs: MRO-006"
```

---

## Task 12: 生成 MRO-007 Spec (VR/AR 沉浸式培训)

**Files:**
- Create: `specs/mro/007-vr-ar-training.spec.md`
- Reference: `智慧机务模块/07-VR-AR沉浸式培训系统.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F46-F52 → FR-1~FR-7
筛选映射：系统可用性 ≥99.95%
AI 推导：数据契约（training_scenario, trainee_profile, training_session, skill_assessment, digital_twin_model）、接口契约（场景管理、学员管理、培训任务、实时评估 WebSocket、考核报告生成）、权限边界（train:manage_scenario, train:assign, train:assess, train:report）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/007-vr-ar-training.spec.md
git commit -m "feat(spec): MRO-007 VR/AR immersive training system

Refs: MRO-007"
```

---

## Task 13: 生成 MRO-008 Spec (无纸化电子工卡)

**Files:**
- Create: `specs/mro/008-paperless-workcard.spec.md`
- Reference: `智慧机务模块/08-无纸化电子工卡系统.md`

- [ ] **Step 1: 按 Phase 2-F 映射规则生成 spec**

自动映射：F53-F59 → FR-1~FR-7
筛选映射：系统可用性 ≥99.95%
AI 推导：数据契约（workcard, workcard_step, workcard_signature, approval_flow, personnel_qualification）、接口契约（工卡 CRUD、审批流、执行签署、进度监控、区块链上链验证、手册链接跳转）、权限边界（workcard:create, workcard:approve, workcard:execute, workcard:sign, workcard:monitor）

- [ ] **Step 2: 展示 AI 推导内容给用户确认**

- [ ] **Step 3: 组装写入并验证 Commit**

```bash
git add specs/mro/008-paperless-workcard.spec.md
git commit -m "feat(spec): MRO-008 paperless electronic workcard system

Refs: MRO-008"
```

---

## Task 14: 全量验证 + 可追溯性报告

**Files:**
- All files in `specs/`

- [ ] **Step 1: 运行完整验证**

```bash
npm run specs:validate
```

Expected: PASS，所有 spec/adr 文件格式正确

- [ ] **Step 2: 生成可追溯性矩阵**

```bash
npm run specs:traceability
```

Expected: 生成 `specs/traceability.md`，包含 MRO-001~008 的完整追踪链

- [ ] **Step 3: 检查生成的 traceability 报告**

确认所有 MRO spec 的 depends-on 关系正确、ID 无冲突。

- [ ] **Step 4: Commit traceability 报告**

```bash
git add specs/traceability.md
git commit -m "docs(specs): regenerate traceability matrix with MRO-001~008"
```

---

## Task 15: 用户批量 Review 并 Approve Specs

**这不是自动化步骤 — 需要用户参与。**

- [ ] **Step 1: 通知用户 review**

告知用户："8 个 MRO spec 已全部生成（draft 状态）。请 review 后将需要进入 Plan 阶段的 spec 的 status 改为 approved。"

- [ ] **Step 2: 等待用户 approve**

用户可选择：
- 逐个 approve 并立即生成 plan/tasks
- 批量 approve 后统一生成

---

## Task 16-23: 生成 Plan 和 Tasks（Spec Approved 后执行）

每个 approved spec 执行：

- [ ] **Phase 3: Plan Generation** — 按 spec-builder Phase 3 流程生成 `specs/mro/NNN-<slug>.plan.md`
- [ ] **Phase 4: Tasks Generation** — Plan 确认后生成 `specs/mro/NNN-<slug>.tasks.md`

具体内容在 spec approved 后根据 spec 内容实时生成（技术选型、里程碑划分、任务拆解），此处不提前编写。

---

## Summary

| Task | 内容 | 预计耗时 |
|------|------|----------|
| 1 | SKILL.md 改造 | 5 min |
| 2 | CHARTER v2.0.0 | 5 min |
| 3 | ADR-004 | 3 min |
| 4 | ADR-005 | 3 min |
| 5 | 创建 mro 目录 | 1 min |
| 6-13 | 8 个 MRO spec (含用户确认) | 每个 5-10 min |
| 14 | 全量验证 | 2 min |
| 15 | 用户 review | 用户节奏 |
| 16-23 | Plan + Tasks × 8 | 每个 10-15 min |

总计：Spec 阶段约 60-90 min，Plan/Tasks 阶段约 80-120 min。
