# AGENTS.md

This file provides guidance to the AI agent when working with code in this repository.

## Stack

- Vue 3 (Composition API, `<script setup>`) + Vite 8 + **plain JavaScript** (no TypeScript — do not add type annotations).
- Element Plus 2 UI, Pinia 3 store, Vue Router 4, Axios 1, Tailwind CSS 3.
- Package manager: **npm** (`package-lock.json` present).

## Business source of truth

**All business logic follows `../specs/` (SDD).** Repo root is `D:\ai_code\ui\`; specs live at `D:\ai_code\ui\specs\`.

- Charter: `../specs/CHARTER.md`.
- Per-module specs: `../specs/<domain>/<NNN>-<slug>.spec.md` (auth / system / platform).
- ADRs: `../specs/adr/`.
- Before implementing or modifying business behavior, **read the relevant `*.spec.md` first** — do not infer intent from code or from the `ai/` design docs.
- `../项目需求文档-完善版.md` is a **frozen v1.1 snapshot** kept for history; do not edit. New requirement changes must go into `../specs/` (bump `version` + Changelog).
- The `ai/` docs below are derivative. **If they conflict with `../specs/`, specs wins** — flag the conflict to the user.
- Code commits/PRs must include `Refs: <Spec-ID>` (e.g. `Refs: SYS-002`).

## Project design notes (derivative — read for context, not for truth)

Design/planning notes live in `ai/` at the project root (i.e. `D:\ai_code\ui\frontend\ai\`). They are **derivative** of `../specs/` and may lag. Read them for implementation context, but if they disagree with `../specs/`, specs wins.

- `ai/前端框架设计文档.md` — overall framework/architecture decisions (stack, layering, conventions).
- `ai/前端Demo方案设计.md` — Demo solution design (feature scope, UX flows, module breakdown).
- `ai/前端实施计划.md` — phased implementation plan and task ordering.
- `ai/前端第一阶段-代码开发指南.md` — phase-1 coding guide (concrete patterns to follow when adding code).
- `ai/前端项目启动指南.md` — project bootstrap / how to run locally.
- `ai/前端项目启动成功总结.md` — record of what was completed at startup; useful baseline for "what already exists".

If a doc and the code disagree, surface the conflict to the user instead of silently choosing one.

## Commands

- Dev: `npm run dev` (Vite on port 5173, auto-opens browser).
- Build: `npm run build`.
- Preview built bundle: `npm run preview`.
- Lint: `npm run lint` (or `npm run lint:fix`).
- Format: `npm run format` (or `npx prettier --write <file>` for a single file).
- No test runner is installed.

## Code style

- **No semicolons.** **Single quotes.** 2-space indent. Enforced via `.prettierrc.json`.
- Imports use the `@/` alias mapped to `src/` (configured in `vite.config.js`). Prefer `@/...` over relative paths that cross directories.
- Vue SFCs use `<script setup>` + Composition API; match the surrounding file's conventions (JSDoc-style comments on exported functions).

## Mock-first development

- `VITE_USE_MOCK=true` is the default (`.env.development`). All API calls are intercepted by `vite-plugin-mock` reading from `src/mock/api/*`.
- When `VITE_USE_MOCK=true`, `src/utils/request.js` sets axios `baseURL = ''` so Mock.js can intercept `/api/...` paths.
- When changing an API contract, update **both** `src/api/*.js` and the matching `src/mock/api/*.js` so the mock layer stays in sync.

## Permissions

- Custom permission system in `src/utils/permission.js`: `hasPermission`, `hasRole`, `v-permission`, `v-role` directives, plus `canAccessRoute` used by the router guard.
- Super-admin pattern is the literal string `*:*:*`.
- Route access is enforced in `src/router/guards.js` (`beforeEachGuard`).

## Localization

- UI is Chinese (app title: `AI工作辅助系统`). Keep user-facing strings in Chinese unless the user asks otherwise. Code identifiers and comments may be English.

## Commit messages

- Use Conventional Commits (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, etc.).

## Verification

- After non-trivial edits, run `/verify` (or `npm run build`) to confirm the project still builds. There is no test runner installed.

## Extending guidance

- For module-specific rules, add a `AGENTS.md` inside that subdirectory — it's loaded automatically when working there.
- For personal/local-only notes (not committed), use `AGENTS.local.md` at the project root.
- For focused rule files (e.g., `code-style.md`, `testing.md`), put them in `.qoder/rules/` and scope with a `paths` frontmatter.
