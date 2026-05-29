# specs/ — Spec-Driven Development

本目录是项目的**唯一业务真理源**。所有需求、设计、决策按 SDD 四层模型组织。

> 想知道**怎么用 / 怎么落地** → 直接看 [`USAGE.md`](USAGE.md)。本文件只讲结构与命名。

## 文档分层

| 层级 | 位置 | 用途 |
|------|------|------|
| Charter | `CHARTER.md` | 项目愿景、范围、非目标、关键约束 |
| Spec | `<domain>/<NNN>-<slug>.spec.md` | 模块要做什么、契约、验收标准 |
| Plan | `<domain>/<NNN>-<slug>.plan.md` | 怎么实现、技术选型、阶段、风险 |
| Tasks | `<domain>/<NNN>-<slug>.tasks.md` | 拆任务、负责人、DoD |
| ADR | `adr/<NNN>-<slug>.md` | 重大决策记录（MADR 简化版） |

## 命名规则

- `<domain>` ∈ {`auth`, `system`, `platform`}（按需扩展）。
- `NNN` 为该 domain 内三位递增编号（001 起）。
- `<slug>` 为 kebab-case 英文短名。
- Spec ID 形如 `SYS-006`、`AUTH-001`、`PLAT-001`。

## 工作流

```
Charter → Spec(draft) → review → approved → Plan → Tasks → Code → PR(Refs: SYS-NNN)
                                              ↑
                                              └── ADR
```

门禁：
1. Spec `status` 不到 `approved` 不允许写 Plan。
2. Plan 评审通过前不允许拆 Tasks。
3. PR 描述必须包含 `Refs: <ID>`。
4. Spec 变更必须 bump `version` 并追加 Changelog。
5. 旧 Spec 被替代时标记 `deprecated` + `supersedes`，并写 ADR。

## 模板与脚本

- 模板：`_templates/{spec,plan,tasks,adr}.template.md`
- 校验脚本：
  - `npm run specs:validate` —— 检查 front-matter + 链接
  - `npm run specs:traceability` —— 生成 `traceability.md`

## 索引

| Domain | ID | 标题 | 状态 |
|--------|----|------|------|
| auth | AUTH-001 | 登录与 JWT | draft |
| system | SYS-001 | 部门管理 | draft |
| system | SYS-002 | 用户管理 | draft |
| system | SYS-003 | 角色管理 | draft |
| system | SYS-004 | 菜单/权限管理 | draft |
| system | SYS-005 | 字典管理 | draft |
| system | SYS-006 | 数据权限过滤 | draft |
| platform | PLAT-001 | 系统架构 | draft |
| platform | PLAT-002 | 数据库设计 | draft |

## ADR 索引

| ID | 标题 | 状态 |
|----|------|------|
| ADR-001 | 密码加密改用 BCrypt | accepted |
| ADR-002 | 字典管理改为单层平铺 | accepted |
| ADR-003 | 前端先行 + Mock-first 开发 | accepted |

## 历史快照

`../项目需求文档-完善版.md` 是 v1.1 历史快照（冻结），不再更新；以本目录为准。
`../项目需求文档.md` 是原始草稿，仅作存档。
`../frontend/ai/*.md` 是派生设计文档，文件头标注 `derived-from`，与本目录冲突时以本目录为准。
