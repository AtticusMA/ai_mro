# SDD 使用与工程化落地手册

> 配套：`specs/README.md`（结构总览）、`AGENTS.md`（Agent 守则）。本手册回答**怎么用、怎么落地、怎么治理**。

---

## 0. TL;DR

```
想法 → Charter（一次） → Spec(draft → review → approved)
                          ↓
                         Plan(draft → review → approved)
                          ↓
                         Tasks → Code → PR(Refs: ID) → Merge
                          ↑
                          └── 重大决策 → ADR
```

每次新需求做的事就 5 步：
1. `cp specs/_templates/spec.template.md specs/<domain>/<NNN>-<slug>.spec.md` 写 spec。
2. `npm run specs:validate` 必须 0 报错。
3. 评审通过 → 把 spec `status` 改为 `approved`。
4. 写 plan / tasks，开始编码，PR/commit 带 `Refs: <ID>`。
5. 合并后跑 `npm run specs:traceability` 更新追溯矩阵。

---

## 1. 角色与职责

| 角色 | 主要产出 | 主要审阅 |
|------|----------|----------|
| 产品 PM | Charter、Spec | Plan |
| 架构师 | Plan、ADR | Spec |
| 前端/后端开发 | Tasks、Code | Plan、Tasks |
| QA | 验收用例 | Spec §9 |
| Tech Lead / Owner | 审批 status 流转 | 全部 |

> 小团队可一人多角，但**评审环节必须至少两个人**：作者 + 一位评审。

---

## 2. 完整生命周期

### 2.1 立项阶段（一次性）

1. 起草 `specs/CHARTER.md`，写清愿景、范围、非目标、关键约束。
2. 全员对齐后将 `status: approved`，version 标记 1.0.0。
3. 新建 `specs/adr/` 下记录关键技术选型（栈、安全、Mock 策略等）。

> 本仓库已完成此阶段：`CHARTER.md` + ADR-001/002/003。

### 2.2 新需求/新模块进来

```
┌─────────────────────────────────────────────────────────┐
│  Step 1  开 Spec                                         │
│  - 复制 spec.template.md，填 front-matter（id 用下一个）  │
│  - 用 Given/When/Then 写验收标准                          │
│  - 把"未决问题"列出来，别藏                                │
│  - status: draft                                          │
├─────────────────────────────────────────────────────────┤
│  Step 2  Spec 评审                                        │
│  - PR：only specs/ 改动；标题 [Spec] SYS-007: xxx         │
│  - 评审重点：范围是否清晰、契约是否完整、与既有 Spec 冲突 │
│  - 改 status: review；评审通过后 approved + bump version  │
├─────────────────────────────────────────────────────────┤
│  Step 3  开 Plan                                          │
│  - Spec approved 才允许创建 plan（脚本会校验）             │
│  - 写技术选型/里程碑/风险/测试策略                          │
│  - 重大选型同时新增 ADR                                    │
├─────────────────────────────────────────────────────────┤
│  Step 4  拆 Tasks                                         │
│  - 每条任务必须能在 1~2 天内完成                           │
│  - DoD 写明"测什么"、"谁验"                                │
├─────────────────────────────────────────────────────────┤
│  Step 5  写代码                                            │
│  - 每个 PR 关联一条 Task ID，commit/PR 带 Refs            │
│  - 实现偏离 Spec → 先改 Spec 再改代码                      │
├─────────────────────────────────────────────────────────┤
│  Step 6  收尾                                              │
│  - 把 Tasks 状态改为 done                                  │
│  - npm run specs:traceability 重生成矩阵                   │
│  - 出现重大变化时 bump Spec version + Changelog           │
└─────────────────────────────────────────────────────────┘
```

### 2.3 需求变更（最常见）

1. **小改**（措辞/字段说明）：直接改 spec，bump patch（0.1.0 → 0.1.1），追加 Changelog。
2. **加字段/加接口**：bump minor（0.1.0 → 0.2.0），追加 Changelog，**通知所有依赖该 spec 的 Plan 作者**（看 `depends-on`）。
3. **破坏性变更**（删字段/改语义）：bump major（0.x → 1.0），并同步：
   - 旧 Plan/Tasks 中受影响项标 `blocked` 或重写。
   - 必要时写一份 ADR 说明决策。
   - 标记被替代的旧 Spec 为 `deprecated` + `supersedes`。

### 2.4 废弃模块

1. 旧 spec `status: deprecated`，写明替代方案。
2. 新 spec front-matter 加 `supersedes: [OLD-ID]`。
3. ADR 记录"为什么废弃"。

---

## 3. 命名 / ID / 版本规则

| 项 | 规则 | 示例 |
|---|---|---|
| Spec ID | `<DOMAIN>-<NNN>` | `SYS-006`、`AUTH-001`、`PLAT-002` |
| ADR ID | `ADR-<NNN>` | `ADR-001` |
| Task ID | `T-<NNN>`（在 tasks 内自洽） | `T-003` |
| 文件名 | `<NNN>-<slug>.<type>.md` | `006-data-permission.spec.md` |
| slug | kebab-case 英文短名 | `login-jwt`、`data-permission` |
| 版本 | semver，spec 与 charter 都用 | `0.1.0` → `0.2.0`（加字段）→ `1.0.0`（破坏性） |

> NNN 在同一 domain 内递增，不复用、不跳号。

---

## 4. 每日/每周节奏

### 每日（开发者）
- 拉新代码后先看 `specs/traceability.md`，了解哪些 Spec 状态变了。
- 写代码前打开自己 Task 对应的 spec/plan，确认契约没变。
- 提交时 commit message 带 `Refs: <ID>`。

### 每周（Tech Lead）
- 跑一次 `npm run specs:check`，确保门禁通过。
- 检查 `traceability.md`：是否有"Plan: -"或"Tasks: -"长时间空着的 approved Spec？
- 把 `status: review` 卡住超过 3 天的 spec 推进。

### 每迭代结束
- 关闭已完成 spec 的所有 Task。
- 把当迭代的 ADR 列入回顾会。
- bump Charter（如有范围调整）。

---

## 5. 具体操作 Cookbook

### 5.1 新建一个 Spec
```bash
# 假设新建 system 域第 7 个 Spec：操作日志
cp specs/_templates/spec.template.md specs/system/007-operation-log.spec.md
# 编辑 front-matter：id=SYS-007、title、owner、created/updated 日期
# 编辑正文 10 个章节
npm run specs:validate
```

### 5.2 Spec 评审通过
```diff
- status: draft
+ status: approved
- version: 0.1.0
+ version: 1.0.0
```
末尾追加：
```markdown
## Changelog
| 1.0.0 | 2026-05-25 | 评审通过 |
```

### 5.3 创建配套 Plan
```bash
cp specs/_templates/plan.template.md specs/system/007-operation-log.plan.md
# front-matter：id=SYS-007（同 spec）、spec 路径、owner
npm run specs:validate
```

### 5.4 创建 Tasks
```bash
cp specs/_templates/tasks.template.md specs/system/007-operation-log.tasks.md
# 填表格，状态都先标 todo
```

### 5.5 PR 模板（建议放 `.github/pull_request_template.md`）
```markdown
## Refs
- Spec: SYS-007 v1.0.0
- Tasks: T-001, T-002

## Summary
<改动总结>

## Spec Conformance
- [ ] 已对照 spec §4 功能需求
- [ ] 已对照 spec §6 数据契约
- [ ] 已对照 spec §7 接口契约
- [ ] Mock 与真实接口契约一致（如适用）

## Test plan
- [ ] ...
```

### 5.6 写一条 ADR
```bash
# 找下一个空闲编号
ls specs/adr | tail -3
cp specs/_templates/adr.template.md specs/adr/004-<title-slug>.md
# 填 Context / Decision / Consequences / Alternatives
npm run specs:validate
```

### 5.7 标记 Spec 废弃
```diff
- status: approved
+ status: deprecated
+ # 在新 spec 中：
+ supersedes: [SYS-007]
```

---

## 6. 与 CI / Hooks 集成

### 6.1 PreCommit（推荐）
在 `frontend/.qoder/hooks/` 或仓库级 git hook 加：
```bash
#!/usr/bin/env bash
set -e
npm run specs:validate
```
front-matter 错或链接坏 → 直接 reject 提交。

### 6.2 CommitMsg（推荐）
```bash
#!/usr/bin/env bash
msg=$(cat "$1")
if ! grep -qE 'Refs: (SYS|AUTH|PLAT|ADR)-[0-9]{3}' <<<"$msg"; then
  echo "[hint] commit message 缺 Refs: <ID>，仅警告不拦截。"
fi
```

### 6.3 CI（GitHub Actions / 任意）
```yaml
- name: Spec gates
  run: |
    npm run specs:validate
    npm run specs:traceability
    git diff --exit-code specs/traceability.md  # 矩阵漂移则 fail
```

### 6.4 Qoder Skills（可选）
- 创建 `/spec-new` 技能：交互式选 domain → 自动算下一个 NNN → 复制模板 → 打开编辑。
- 创建 `/spec-validate` 技能：包装 `npm run specs:validate`，失败时高亮文件名。

---

## 7. 反模式（红线）

- 在代码 PR 里**顺手改 spec**而不走 Spec 评审。**正确做法**：先发 Spec PR，合并后再发 Code PR。
- spec 写"已实现"细节（如 SQL、组件名）。**spec 只描述要什么，不描述怎么做**——那是 plan 的事。
- 把 `项目需求文档-完善版.md` 当真理源更新。**它是冻结快照**，不再修改。
- 一份 spec 覆盖多个不相关模块。**一个 NNN 一个聚焦主题**，方便追溯。
- 长期挂着 `status: draft` 的 spec。draft 超过 3 天就该推到 review 或砍掉。
- 重大决策只在群里讨论不写 ADR。**没写 ADR 等于没决策过**。
- Plan 在 Spec 还是 draft 时就写。脚本会拦，绕过的只会让自己返工。
- Mock 接口和真实接口契约不一致。**前端先行的代价是契约严格同步**（见 ADR-003）。

---

## 8. 上手 Checklist（新人 30 分钟）

- [ ] 读 `specs/README.md`（5min）
- [ ] 读 `specs/CHARTER.md`（5min）
- [ ] 读 自己将参与模块的 `*.spec.md`（10min）
- [ ] 浏览 `specs/traceability.md` 看全局状态（2min）
- [ ] 浏览 `specs/adr/` 三份 ADR（5min）
- [ ] 跑一次 `npm run specs:check`（1min）
- [ ] 复制一份模板试写一个假 spec，再删掉（练手 2min）

---

## 9. FAQ

**Q：specs 是前端的还是后端的？**
A：项目级，前后端共用。前端按 spec 写页面/Mock，后端按 spec 写接口/表，QA 按 spec §9 出验收用例。

**Q：spec 一定要写得很详细吗？**
A：覆盖 10 个章节即可，能让"另一个团队照着实现"就够。冗长但模糊比简短但精确更糟。

**Q：临时实验/PoC 也要走 SDD 吗？**
A：可以放 `specs/experiments/` 下，front-matter 加 `status: draft` + `experimental: true`。结论稳定再升级为正式 spec。

**Q：Spec 与代码不一致时怎么办？**
A：**永远以 spec 为准**。代码偏离 → 要么改代码贴齐 spec，要么先发 Spec PR 改 spec、再发 Code PR 跟进。

**Q：能跳过 Plan 直接写 Tasks 吗？**
A：脚本不强制，但强烈不建议。哪怕 Plan 只有 5 行（选型一句、阶段一句、风险一句），也比没有强。

**Q：模板能改吗？**
A：能，改 `_templates/` + 同步更新 `_scripts/validate-frontmatter.mjs` 的必填字段校验。改完跑 `npm run specs:validate` 验所有存量 spec。

---

## 10. 参考

- 本仓库现有产物：`specs/CHARTER.md`、`specs/adr/001~003`、`specs/{auth,system,platform}/*.spec.md`。
- 工具：`specs/_scripts/{validate-frontmatter,check-links,gen-traceability}.mjs`。
- 模板：`specs/_templates/{spec,plan,tasks,adr}.template.md`。
- Agent 守则：根 `AGENTS.md`、`frontend/AGENTS.md`。
