# AGENTS.md (project root: D:\ai_code\ui)

本仓库采用 **SDD（Spec-Driven Development）**。AI Agent 在本仓库工作时必须遵守以下规则。

## 唯一业务真理源

**所有需求、设计、决策以 `specs/` 目录为准。**

- `specs/CHARTER.md` —— 项目宪章。
- `specs/<domain>/<NNN>-<slug>.spec.md` —— 模块规格。
- `specs/<domain>/<NNN>-<slug>.plan.md` —— 实施计划（Spec approved 后才写）。
- `specs/<domain>/<NNN>-<slug>.tasks.md` —— 任务拆解（Plan 评审后才写）。
- `specs/adr/<NNN>-*.md` —— 架构决策记录。

旧文档 `项目需求文档-完善版.md` / `项目需求文档.md` / `plan.md` / `后端实施计划.md` / `frontend/ai/*` 是 **历史快照或派生文档**，与 `specs/` 冲突时以 `specs/` 为准。

## Agent 工作守则

1. **任何业务功能改动前，先读对应 `*.spec.md`**；找不到对口 Spec 时停下来询问用户。
2. **不要直接修改 `项目需求文档-完善版.md`**；新增/变更内容写到 `specs/`，并 bump Spec `version` + 追加 Changelog。
3. **代码 PR/commit message 必须包含 `Refs: <ID>`**（如 `Refs: SYS-006` 或 `Refs: AUTH-001 / T-003`）。
4. **重大决策**（替换库、变更架构、改安全/数据契约）必须新增 ADR；旧 Spec 标记 `deprecated` + `supersedes`。
5. **Mock 与真实接口契约必须同步**（见 ADR-003 / `frontend/AGENTS.md`）。

## 校验

```bash
npm run specs:validate       # front-matter + 链接 + 状态门禁
npm run specs:traceability   # 生成 specs/traceability.md
```

新建 spec/plan/tasks 请基于 `specs/_templates/` 模板，禁止徒手起。

## 子目录指引

- `frontend/AGENTS.md` —— 前端开发约定（栈、命令、Mock-first、权限指令等）。
- 后续后端目录加入时，请同时新增 `<服务>/AGENTS.md`。
