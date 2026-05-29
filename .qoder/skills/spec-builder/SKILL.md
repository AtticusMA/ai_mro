---
name: spec-builder
description: Generate or update standard SDD documents (spec, plan, tasks) for this project's specs/ directory. Auto-senses project context (existing specs, ID sequences, ADRs, depends-on graph), asks targeted questions to fill gaps, and outputs validated documents following specs/_templates/. Use when creating new feature specs, updating existing specs with new requirements, generating plans from approved specs, or breaking plans into tasks. Typically invoked after brainstorming, or directly when requirements are already clear.
---

# spec-builder

## Hard Rules

- All output documents MUST pass `npm run specs:validate` before presenting to user.
- Document language: Chinese (matching existing specs).
- Field naming in data contracts: snake_case.
- API path prefix: `/api/`.
- Permission identifiers: `<module>:<action>` format.
- Never modify existing spec files unless user explicitly requests a version update.
- Spec status starts as `draft`; only the user promotes to `approved`.
- ID format: `^[A-Z]{2,8}-\d{3}$` (e.g., SYS-007, AUTH-002).
- Version format: semver `x.y.z`.
- Date format: `YYYY-MM-DD`.
- Templates location: `specs/_templates/`.
- One question per message. Prefer multiple-choice when possible.

## Brainstorming Integration

This skill is the downstream of the `brainstorming` skill. When invoked after brainstorming:

### Recognition Rules

Identify brainstorming output by these signals in user input or conversation context:
- Design decisions: "我们决定...", "方案是...", "选择了..."
- User stories in "作为X，我希望Y，以便Z" format
- Scope lists (In Scope / Out of Scope)
- Interface definitions or API paths
- Architecture choices or technology selections

### Mapping Strategy

Map recognized content directly to spec sections:
- Design decisions -> 背景与目标 + 非功能需求
- User stories -> 用户故事 (section 3)
- Scope lists -> 范围 (section 2)
- API definitions -> 接口契约 (section 7)
- Data models -> 数据契约 (section 6)
- Architecture choices -> feed into Plan generation later

### Reduced Questioning

When brainstorming output covers a section, do NOT re-ask. Only ask for:
- Sections with no coverage at all
- Sections where brainstorming was ambiguous (partial coverage)
- Validation of inferred content (present inference, ask "确认？")

## Phase 1: Context Sensing

On activation, BEFORE any user interaction, perform these scans silently and present a summary:

### Actions

1. **Glob `specs/**/*.spec.md`** -- Read each file's YAML front-matter. Extract:
   - `id` -> build list of used IDs per domain prefix (e.g., SYS: [001,002,...006])
   - `domain` -> build available domain list
   - `depends-on` -> build dependency graph
   - `status` -> note which specs are approved vs draft

2. **Determine next available ID** -- For the target domain, find max NNN and increment. If user needs a new domain, ask them for a 2-8 uppercase letter prefix and create the subdirectory.

3. **Read `specs/_templates/spec.template.md`** -- This is the output format. Also read `plan.template.md` and `tasks.template.md` for later phases.

4. **Read `specs/CHARTER.md`** -- Extract project goals and constraints to align the spec's "背景与目标" section.

5. **Glob `specs/adr/*.md`** -- Read front-matter of all ADRs. Note any `accepted` decisions that may constrain the new module.

### Output to User

Present a concise summary:

```
已扫描项目上下文：
- 可用 domain: [list domains]
- [domain] 下一个可用 ID: [DOMAIN-NNN]
- 已有 [N] 个 spec，[N] 个 ADR
- 相关约束 ADR: [list if applicable]
- 可能的依赖: [list if detectable from user input]
```

Then proceed to Phase 2.

## Phase 2: Spec Generation

### Input Analysis

Receive user input (free text, brainstorming output, or structured requirements). Map it against the 10 template sections:

1. 背景与目标
2. 范围 (In Scope / Out of Scope)
3. 用户故事
4. 功能需求 + 验收标准
5. 非功能需求
6. 数据契约
7. 接口契约
8. 权限边界
9. 验收标准
10. 未决问题

For each section, classify: **covered** / **partial** / **missing**.

Present the mapping to the user:

```
已从你的输入中识别到：
✓ 背景与目标 -- [summary]
◐ 功能需求 -- 部分覆盖，需补充验收标准
✗ 数据契约 -- 缺失，将根据功能需求推导
...
```

Skip sections marked "covered". Ask about "partial" and "missing" sections.

### Questioning Strategy

Ask in this order (skip what's already known):

| Order | Section | Approach |
|-------|---------|----------|
| 1 | Domain + Title | Multiple choice from available domains + confirm title |
| 2 | 背景与目标 | Ask "why" if not clear; help align to CHARTER.md goals |
| 3 | 范围 | Always confirm boundaries; propose Out of Scope based on similar specs |
| 4 | 用户故事 | If missing, propose stories in "作为X，我希望Y，以便Z" format for confirmation |
| 5 | 功能需求 | Core section -- ask for each feature, propose GWT acceptance criteria |
| 6 | 非功能需求 | Propose defaults from CHARTER.md constraints, ask user to confirm/adjust |
| 7 | 数据契约 | Infer fields from functional requirements (CRUD pattern recognition), present table for confirmation |
| 8 | 接口契约 | Infer REST endpoints from features (GET/POST/PUT/DELETE patterns), present for confirmation |
| 9 | 权限边界 | Reference existing specs' permission patterns, propose `<module>:<action>` identifiers |
| 10 | 未决问题 | Proactively identify ambiguities and list them |

### Inference Rules

- CRUD feature -> infer GET (list), POST (create), PUT (update), DELETE (delete) endpoints
- List feature -> infer pagination, search/filter query params
- Tree structure -> infer tree endpoint + flat list endpoint
- Import/Export -> infer multipart upload + file download endpoints
- Field naming: follow snake_case, match existing specs' style (e.g., `created_by`, `dept_id`)
- Permission naming: `<module>:<action>` where action in {list, add, edit, delete, import, export}

### Output Assembly

After all sections are confirmed, assemble the complete `.spec.md`:

1. Generate YAML front-matter:
   ```yaml
   ---
   id: [AUTO from Phase 1]
   title: [confirmed title]
   domain: [confirmed domain]
   status: draft
   owner: '@product'
   version: 0.1.0
   created: [today YYYY-MM-DD]
   updated: [today YYYY-MM-DD]
   charter: CHARTER.md
   supersedes: []
   depends-on: [AUTO from dependency analysis]
   ---
   ```

2. Fill all 10 sections + Changelog using the template structure from `specs/_templates/spec.template.md`.

3. Write to `specs/<domain>/<NNN>-<slug>.spec.md` where slug is derived from title (lowercase, hyphens, ASCII).

4. Run `npm run specs:validate` in project root.

5. If validation fails: read error output, fix automatically (common fixes: date format, missing field, ID pattern), re-validate.

6. If validation passes: present the file to user and ask for review.

7. Tell user: "Spec 已生成并验证通过。请 review 后将 status 改为 approved，然后我可以继续生成 Plan。"

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

## Phase 2b: Spec Update (更新已有 Spec)

当用户明确要求更新现有 Spec 时执行此阶段。

### 触发条件

用户输入包含以下信号时进入此阶段（而非新建）：
- "更新 / 修改 / 变更 / 补充 XXX 规格"
- "在 [SYS-NNN] 中增加 / 删除 / 调整 ..."
- "现有需求有变化"
- Resume from Interruption 检测到已有 `.spec.md` 并用户选择"继续完善"

### 流程

1. **读取目标 Spec 文件**
   - `Read specs/<domain>/<NNN>-<slug>.spec.md`
   - 提取当前 `version`、`status`、所有功能需求、数据契约、接口契约

2. **分析变更范围**
   - 将用户描述的变更映射到受影响的 Spec 章节（同 Phase 2 的 10 节分类）
   - 呈现变更摘要：
     ```
     检测到以下变更：
     ✎ 功能需求 FR-N -- 新增 / 修改 / 删除
     ✎ 数据契约 -- 新增字段 xxx
     ✎ 接口契约 -- 新增接口 POST /api/...
     ```
   - 逐项与用户确认（每次一个问题）

3. **版本策略**

   | 变更类型 | 版本 bump |
   |---------|-----------|
   | 新增功能需求 / 接口 / 字段 | minor (x.**Y**.0) |
   | 修改现有需求描述或验收标准 | patch (x.y.**Z**) |
   | 删除功能 / 破坏性变更 | major (**X**.0.0) |
   | 仅修正文字错误 / 格式 | patch |

4. **执行修改**
   - 编辑对应章节内容
   - 更新 `version` 字段（按上表 bump）
   - 更新 `updated` 字段为今日 `YYYY-MM-DD`
   - 在文件末尾 `## Changelog` 表格追加一行：
     ```
     | x.y.z | YYYY-MM-DD | [变更摘要] |
     ```
   - **不修改** `status`（保持原值，由用户决定是否 re-approve）
   - **不修改** `created` 字段

5. **运行验证**
   ```bash
   npm run specs:validate
   ```
   自动修复常见错误，最多重试 2 次。

6. **呈现结果**
   - 展示修改后的文件（或关键变更 diff）
   - 告知用户："Spec 已更新至 vX.Y.Z 并验证通过。如需重新 approve，请将 status 改为 approved。如关联 Plan/Tasks 已存在，建议同步评估是否需要更新。"

### 注意事项

- 若当前 `status: approved`，更新后自动降级为 `draft`，并提示用户："已将 status 降级为 draft，变更需重新 review 并 approve 后方可驱动 Plan/Tasks。"
- 若变更涉及删除已被 Plan/Tasks 引用的 FR-N，需提示："FR-N 可能已在 Plan/Tasks 中被引用，请手动检查。"
- 重大变更（major bump）时，询问用户是否需要新增 ADR 记录决策。

## Phase 3: Plan Generation

### Gate Check

Before starting:
1. Read the target spec file's front-matter `status` field.
2. If `status` is NOT `approved`, stop and tell user:
   "该 Spec 当前 status 为 [status]。请先将 status 改为 approved 后再生成 Plan。"
3. Only proceed if `status: approved`.

### Generation Logic

Read the approved spec and generate a plan following `specs/_templates/plan.template.md`:

1. **技术选型与对比** -- Based on the spec's requirements:
   - For frontend features: Vue 3 + Element Plus + Tailwind CSS options
   - For backend features: Spring Boot + MyBatis Plus options
   - Present 2-3 alternatives with pros/cons, ask user to confirm selection

2. **阶段划分** -- Break into milestones based on feature count and complexity:
   - Each milestone should be independently deployable/testable
   - Propose timeline estimates, ask user to confirm

3. **架构图/时序图** -- Generate Mermaid diagrams:
   - Sequence diagram for main user flows
   - Component diagram if multiple services involved

4. **风险与回滚预案** -- Identify risks from:
   - depends-on relationships (upstream changes)
   - New technology or patterns not yet used in project
   - Data migration needs

5. **测试策略** -- Based on spec's acceptance criteria:
   - Map each FR-N to test type (unit/integration/e2e)

6. **关联 ADR** -- Link relevant ADRs from Phase 1 scan

### Questions (Plan Phase)

- Technical preference when multiple valid options exist
- Milestone breakdown confirmation
- Risk assessment confirmation

### Output

1. Generate YAML front-matter:
   ```yaml
   ---
   id: [same as spec ID]
   spec: <domain>/<NNN>-<slug>.spec.md
   status: draft
   owner: '@arch'
   created: [today]
   updated: [today]
   ---
   ```

2. Write to `specs/<domain>/<NNN>-<slug>.plan.md`.

3. Run `npm run specs:validate`.

4. Fix any validation errors automatically, re-validate.

5. Tell user: "Plan 已生成并验证通过。请 review 后确认，然后我可以继续生成 Tasks。"

## Phase 4: Tasks Generation

### Gate Check

Before starting:
1. Read the target plan file's front-matter `status` field.
2. If `status` is NOT `approved`, stop and tell user:
   "该 Plan 当前 status 为 [status]。请先确认 Plan 后再生成 Tasks。"
3. Only proceed if `status: approved`.

### Generation Logic

Read the approved plan and generate tasks following `specs/_templates/tasks.template.md`:

1. For each milestone in the plan's "阶段划分":
   - Break into atomic tasks (1-2 days each)
   - Assign T-NNN IDs sequentially (T-001, T-002, ...)

2. For each task determine:
   - **描述**: Clear, actionable description
   - **负责人**: Default to '@dev' (user can reassign)
   - **依赖**: Reference other T-NNN IDs if sequential dependency exists
   - **DoD (Definition of Done)**: Derived from spec's acceptance criteria + "单测通过 + Code Review"
   - **状态**: All start as `todo`

3. Order tasks by dependency (tasks with no dependencies first).

4. Typical task patterns:
   - Database/model tasks before API tasks
   - API tasks before frontend tasks
   - Shared components before page-specific components
   - Core logic before edge cases

### Output

1. Generate YAML front-matter:
   ```yaml
   ---
   id: [same as spec/plan ID]
   plan: <domain>/<NNN>-<slug>.plan.md
   created: [today]
   updated: [today]
   ---
   ```

2. Write to `specs/<domain>/<NNN>-<slug>.tasks.md`.

3. Run `npm run specs:validate`.

4. Fix any validation errors automatically, re-validate.

5. Tell user: "Tasks 已生成并验证通过。SDD 文档全套完成：spec -> plan -> tasks。"

## Error Handling

### Validation Failures

When `npm run specs:validate` fails:
1. Read the error output.
2. Common auto-fixable issues:
   - Date not in YYYY-MM-DD -> reformat
   - Missing required front-matter field -> add with sensible default
   - ID pattern mismatch -> regenerate ID from scan
   - Version not semver -> set to `0.1.0`
3. Apply fix, re-run validation.
4. If still failing after 2 attempts, show error to user and ask for guidance.

### Resume from Interruption

On activation, check if target files already exist:
- If `.spec.md` exists for the identified module -> ask: "检测到已有 spec 文件 [path]。是要继续完善这个 spec，还是新建一个？"
- If `.spec.md` exists with `status: approved` and no `.plan.md` -> ask: "检测到 [ID] spec 已 approved 但无 plan。是否生成 Plan？"
- If `.plan.md` exists with `status: approved` and no `.tasks.md` -> ask: "检测到 [ID] plan 已确认但无 tasks。是否生成 Tasks？"

### ID Conflicts

If the auto-assigned ID already exists (race condition with manual creation):
1. Re-scan `specs/` directory.
2. Assign next available ID.
3. Inform user of the change.
