# spec-builder Skill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a project-local Qoder CLI skill that generates standard SDD documents (spec/plan/tasks) for this project's `specs/` directory, with automatic context sensing, intelligent questioning, and validation.

**Architecture:** Single-file skill (`SKILL.md`) with embedded references to project templates. The skill instructs the AI agent to scan existing specs, ask targeted questions, generate documents following project templates, and validate output via `npm run specs:validate`.

**Tech Stack:** Qoder CLI skill system (Markdown + YAML frontmatter), project's existing `specs/_templates/` and `specs/_scripts/` infrastructure.

---

### Task 1: Create Skill Directory

**Files:**
- Create: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Create the skill directory**

```bash
mkdir -p D:/ai_code/ui/.qoder/skills/spec-builder
```

- [ ] **Step 2: Create initial SKILL.md with frontmatter**

Create `.qoder/skills/spec-builder/SKILL.md` with:

```markdown
---
name: spec-builder
description: Generate standard SDD documents (spec, plan, tasks) for this project's specs/ directory. Auto-senses project context (existing specs, ID sequences, ADRs, depends-on graph), asks targeted questions to fill gaps, and outputs validated documents following specs/_templates/. Use when creating new feature specs, generating plans from approved specs, or breaking plans into tasks. Typically invoked after brainstorming, or directly when requirements are already clear.
---

# spec-builder
```

- [ ] **Step 3: Verify directory structure**

```bash
ls D:/ai_code/ui/.qoder/skills/spec-builder/
```

Expected: `SKILL.md`

- [ ] **Step 4: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat: scaffold spec-builder skill directory"
```

---

### Task 2: Write Phase 1 — Hard Rules & Context Sensing

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Hard Rules section**

Append to SKILL.md after the frontmatter title:

```markdown
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
```

- [ ] **Step 2: Add Phase 1 Context Sensing section**

Append:

```markdown
## Phase 1: Context Sensing

On activation, BEFORE any user interaction, perform these scans silently and present a summary:

### Actions

1. **Glob `specs/**/*.spec.md`** — Read each file's YAML front-matter. Extract:
   - `id` → build list of used IDs per domain prefix (e.g., SYS: [001,002,...006])
   - `domain` → build available domain list
   - `depends-on` → build dependency graph
   - `status` → note which specs are approved vs draft

2. **Determine next available ID** — For the target domain, find max NNN and increment. If user needs a new domain, ask them for a 2-8 uppercase letter prefix and create the subdirectory.

3. **Read `specs/_templates/spec.template.md`** — This is the output format. Also read `plan.template.md` and `tasks.template.md` for later phases.

4. **Read `specs/CHARTER.md`** — Extract project goals and constraints to align the spec's "背景与目标" section.

5. **Glob `specs/adr/*.md`** — Read front-matter of all ADRs. Note any `accepted` decisions that may constrain the new module.

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
```

- [ ] **Step 3: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add hard rules and context sensing phase"
```

---

### Task 3: Write Phase 2 — Spec Generation

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Input Analysis section**

Append:

```markdown
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
✓ 背景与目标 — [summary]
◐ 功能需求 — 部分覆盖，需补充验收标准
✗ 数据契约 — 缺失，将根据功能需求推导
...
```

Skip sections marked "covered". Ask about "partial" and "missing" sections.
```

- [ ] **Step 2: Add Questioning Strategy section**

Append:

```markdown
### Questioning Strategy

Ask in this order (skip what's already known):

| Order | Section | Approach |
|-------|---------|----------|
| 1 | Domain + Title | Multiple choice from available domains + confirm title |
| 2 | 背景与目标 | Ask "why" if not clear; help align to CHARTER.md goals |
| 3 | 范围 | Always confirm boundaries; propose Out of Scope based on similar specs |
| 4 | 用户故事 | If missing, propose stories in "作为X，我希望Y，以便Z" format for confirmation |
| 5 | 功能需求 | Core section — ask for each feature, propose GWT acceptance criteria |
| 6 | 非功能需求 | Propose defaults from CHARTER.md constraints, ask user to confirm/adjust |
| 7 | 数据契约 | Infer fields from functional requirements (CRUD pattern recognition), present table for confirmation |
| 8 | 接口契约 | Infer REST endpoints from features (GET/POST/PUT/DELETE patterns), present for confirmation |
| 9 | 权限边界 | Reference existing specs' permission patterns, propose `<module>:<action>` identifiers |
| 10 | 未决问题 | Proactively identify ambiguities and list them |

### Inference Rules

- CRUD feature → infer GET (list), POST (create), PUT (update), DELETE (delete) endpoints
- List feature → infer pagination, search/filter query params
- Tree structure → infer tree endpoint + flat list endpoint
- Import/Export → infer multipart upload + file download endpoints
- Field naming: follow snake_case, match existing specs' style (e.g., `created_by`, `dept_id`)
- Permission naming: `<module>:<action>` where action ∈ {list, add, edit, delete, import, export}
```

- [ ] **Step 3: Add Output Assembly section**

Append:

```markdown
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
```

- [ ] **Step 4: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add spec generation phase with questioning and inference"
```

---

### Task 4: Write Phase 3 — Plan Generation

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Plan Generation section**

Append:

```markdown
## Phase 3: Plan Generation

### Gate Check

Before starting:
1. Read the target spec file's front-matter `status` field.
2. If `status` is NOT `approved`, stop and tell user:
   "该 Spec 当前 status 为 [status]。请先将 status 改为 approved 后再生成 Plan。"
3. Only proceed if `status: approved`.

### Generation Logic

Read the approved spec and generate a plan following `specs/_templates/plan.template.md`:

1. **技术选型与对比** — Based on the spec's requirements:
   - For frontend features: Vue 3 + Element Plus + Tailwind CSS options
   - For backend features: Spring Boot + MyBatis Plus options
   - Present 2-3 alternatives with pros/cons, ask user to confirm selection

2. **阶段划分** — Break into milestones based on feature count and complexity:
   - Each milestone should be independently deployable/testable
   - Propose timeline estimates, ask user to confirm

3. **架构图/时序图** — Generate Mermaid diagrams:
   - Sequence diagram for main user flows
   - Component diagram if multiple services involved

4. **风险与回滚预案** — Identify risks from:
   - depends-on relationships (upstream changes)
   - New technology or patterns not yet used in project
   - Data migration needs

5. **测试策略** — Based on spec's acceptance criteria:
   - Map each FR-N to test type (unit/integration/e2e)

6. **关联 ADR** — Link relevant ADRs from Phase 1 scan

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
```

- [ ] **Step 2: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add plan generation phase with gate check"
```

---

### Task 5: Write Phase 4 — Tasks Generation

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Tasks Generation section**

Append:

```markdown
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

5. Tell user: "Tasks 已生成并验证通过。SDD 文档全套完成：spec → plan → tasks。"
```

- [ ] **Step 2: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add tasks generation phase with gate check"
```

---

### Task 6: Write Error Handling & Resume Logic

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Error Handling section**

Append:

```markdown
## Error Handling

### Validation Failures

When `npm run specs:validate` fails:
1. Read the error output.
2. Common auto-fixable issues:
   - Date not in YYYY-MM-DD → reformat
   - Missing required front-matter field → add with sensible default
   - ID pattern mismatch → regenerate ID from scan
   - Version not semver → set to `0.1.0`
3. Apply fix, re-run validation.
4. If still failing after 2 attempts, show error to user and ask for guidance.

### Resume from Interruption

On activation, check if target files already exist:
- If `.spec.md` exists for the identified module → ask: "检测到已有 spec 文件 [path]。是要继续完善这个 spec，还是新建一个？"
- If `.spec.md` exists with `status: approved` and no `.plan.md` → ask: "检测到 [ID] spec 已 approved 但无 plan。是否生成 Plan？"
- If `.plan.md` exists with `status: approved` and no `.tasks.md` → ask: "检测到 [ID] plan 已确认但无 tasks。是否生成 Tasks？"

### ID Conflicts

If the auto-assigned ID already exists (race condition with manual creation):
1. Re-scan `specs/` directory.
2. Assign next available ID.
3. Inform user of the change.
```

- [ ] **Step 2: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add error handling and resume logic"
```

---

### Task 7: Write Brainstorming Integration Section

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md`

- [ ] **Step 1: Add Brainstorming Integration section**

Append after the Hard Rules, before Phase 1:

```markdown
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
- Design decisions → 背景与目标 + 非功能需求
- User stories → 用户故事 (section 3)
- Scope lists → 范围 (section 2)
- API definitions → 接口契约 (section 7)
- Data models → 数据契约 (section 6)
- Architecture choices → feed into Plan generation later

### Reduced Questioning

When brainstorming output covers a section, do NOT re-ask. Only ask for:
- Sections with no coverage at all
- Sections where brainstorming was ambiguous (partial coverage)
- Validation of inferred content (present inference, ask "确认？")
```

- [ ] **Step 2: Commit**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): add brainstorming integration section"
```

---

### Task 8: Final Assembly, Validation & Test

**Files:**
- Modify: `.qoder/skills/spec-builder/SKILL.md` (reorder sections into final structure)

- [ ] **Step 1: Reorder SKILL.md into final structure**

The final file should have sections in this order:
1. YAML frontmatter
2. `# spec-builder` title
3. `## Hard Rules`
4. `## Brainstorming Integration`
5. `## Phase 1: Context Sensing`
6. `## Phase 2: Spec Generation`
7. `## Phase 3: Plan Generation`
8. `## Phase 4: Tasks Generation`
9. `## Error Handling`

Read the file and reorder if needed (sections were appended in Tasks 2-7, Task 7 adds Brainstorming Integration after Hard Rules).

- [ ] **Step 2: Validate skill structure**

Run checklist:
- [ ] `SKILL.md` exists in `.qoder/skills/spec-builder/`
- [ ] YAML frontmatter has `name: spec-builder`
- [ ] YAML frontmatter has `description` field (single-line, ≤1024 chars)
- [ ] `name` field matches directory name
- [ ] No `TODO:` strings remain
- [ ] No extraneous files (no README, CHANGELOG, etc.)
- [ ] File is under 500 lines

```bash
wc -l D:/ai_code/ui/.qoder/skills/spec-builder/SKILL.md
```

Expected: under 500 lines.

- [ ] **Step 3: Verify description is single-line and ≤1024 characters**

```bash
head -5 D:/ai_code/ui/.qoder/skills/spec-builder/SKILL.md
```

Check that `description:` value contains no newlines within the YAML string and is ≤1024 chars.

- [ ] **Step 4: Test skill recognition**

Restart session or run `/skills reload`, then verify with `/skills list` that `spec-builder` appears.

- [ ] **Step 5: Commit final version**

```bash
git add .qoder/skills/spec-builder/SKILL.md
git commit -m "feat(spec-builder): finalize skill structure and validate"
```

---

## Summary

| Task | Description | Output |
|------|-------------|--------|
| 1 | Create directory + scaffold | `.qoder/skills/spec-builder/SKILL.md` (skeleton) |
| 2 | Hard Rules + Phase 1 | Context sensing logic |
| 3 | Phase 2 | Spec generation with questioning + inference |
| 4 | Phase 3 | Plan generation with gate check |
| 5 | Phase 4 | Tasks generation with gate check |
| 6 | Error handling | Validation fixes + resume logic |
| 7 | Brainstorming integration | Input recognition + reduced questioning |
| 8 | Final assembly + validation | Reorder, validate, test |
