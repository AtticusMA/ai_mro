# Design: spec-builder Skill

## Overview

为本项目 `specs/` 目录定制的 SDD 文档生成 skill。作为 brainstorming 的下游，将设计探索结果转化为标准的 spec / plan / tasks 文档，严格遵守项目 SDD 生命周期门禁。

## Metadata

| 属性 | 值 |
|------|-----|
| Skill name | `spec-builder` |
| 适用范围 | 本项目专用（`D:\ai_code\ui\specs/`） |
| 上游依赖 | brainstorming skill（可选，用户也可直接调用） |
| 输出产物 | `.spec.md` / `.plan.md` / `.tasks.md` |
| 验证方式 | `npm run specs:validate` |

## Trigger & Positioning

```yaml
name: spec-builder
description: >
  为本项目 specs/ 目录生成标准 SDD 文档（spec -> plan -> tasks）。
  自动感知项目上下文，通过提问补全缺失信息，输出符合模板和验证规则的文档。
trigger: >
  当用户要为本项目新增业务模块/功能需求并需要编写 spec 文档时使用。
  通常作为 brainstorming 的下游：brainstorming 完成设计探索后，
  调用 spec-builder 将设计转化为标准 SDD 文档。
  用户也可在需求已明确时直接调用。
```

**与 brainstorming 的分工：**
- brainstorming：探索需求、讨论方案、做设计决策
- spec-builder：将决策落地为标准 SDD 文档，确保格式合规、信息完整

**接收 brainstorming 输出的识别规则：**
- 若用户输入中包含设计决策（"我们决定..."、"方案是..."）、用户故事、scope 列表、接口定义等结构化内容，skill 将其直接映射到对应 section
- 若当前会话上下文中存在 brainstorming 的设计讨论记录，skill 从对话历史中提取已确认的决策
- 无论输入来源如何，skill 都会展示"已识别信息 → 待补全信息"的映射结果，让用户确认后再继续提问

## Phase 1: Context Sensing（自动上下文感知）

skill 启动时自动执行，无需用户干预：

### 扫描动作

1. **扫描 `specs/` 目录** — 读取所有 `.spec.md` 的 YAML front-matter，构建：
   - 已有 domain 列表（auth, system, platform...）
   - 每个 domain 下已用的 ID 序列（如 SYS 已用 001-006）
   - 全局 depends-on 关系图

2. **确定下一个可用 ID** — 根据目标 domain 自动分配（如 domain=system 则分配 SYS-007）。若用户需要创建新 domain，skill 引导用户确定 domain 缩写（2-8 位大写字母），并创建对应子目录

3. **读取模板** — 加载 `specs/_templates/spec.template.md`（及 plan/tasks 模板）

4. **读取 CHARTER.md** — 了解项目目标，确保 spec 的"背景与目标"对齐到 Charter

5. **检查相关 ADR** — 扫描 `specs/adr/` 确定是否有已决策的约束影响当前模块

### 感知摘要输出

向用户展示扫描结果，例如：

> 已扫描项目上下文：
> - 可用 domain: auth, system, platform
> - system domain 下一个 ID: SYS-007
> - 已有 6 个 system spec，3 个 ADR
> - 检测到你的需求可能依赖: SYS-001(部门), SYS-002(用户)

## Phase 2: Spec Generation（Spec 生成）

### 输入分析

skill 接收用户输入（自由文本或 brainstorming 输出），对照模板 10 个 section 逐一检查：

- 将输入映射到模板各 section
- 标记：已覆盖 / 部分覆盖 / 缺失
- 跳过已明确的内容，只问缺失部分

### 提问策略

| 原则 | 说明 |
|------|------|
| 按依赖顺序 | 先范围 → 功能需求 → 接口契约 → 权限 |
| 优先选择题 | 基于项目上下文生成选项 |
| 每次一个问题 | 不堆叠多个问题 |
| 智能推导 | 能推导的先推导，让用户确认而非从零填写 |

### 提问顺序

| 顺序 | Section | 触发条件 |
|------|---------|----------|
| 1 | Domain + 标题 | 总是先确认 |
| 2 | 背景与目标 | 输入未明确 why |
| 3 | 范围（In/Out Scope） | 总是确认边界 |
| 4 | 用户故事 | 输入未包含角色-动作-价值 |
| 5 | 功能需求 + 验收标准 | 核心，必问细节 |
| 6 | 非功能需求 | 提供合理默认值让用户确认 |
| 7 | 数据契约 | 根据功能需求推导，让用户确认 |
| 8 | 接口契约 | 根据功能需求推导，让用户确认 |
| 9 | 权限边界 | 参考已有 spec 的权限模式推导 |
| 10 | 未决问题 | skill 主动识别模糊点列出 |

### 智能推导能力

- 从功能需求自动推导接口（CRUD 模式识别）
- 从已有 spec 的数据契约风格推导字段命名规范（snake_case）
- 从 CHARTER.md 的安全/性能约束自动填充非功能需求默认值
- 从 depends-on 图推导关联关系

### 输出

- 按模板结构组装完整 `.spec.md`
- 自动填充 front-matter：id、domain、status:draft、version:0.1.0、日期、charter 引用
- 自动计算 `depends-on`
- 写入 `specs/<domain>/<NNN>-<slug>.spec.md`
- 运行 `npm run specs:validate` 验证通过后提交
- 请用户审批

## Phase 3: Plan Generation（Plan 生成）

### 门禁检查

- 读取对应 spec 的 `status` 字段
- 必须为 `approved` 才可进入
- 否则提示用户："请先将 spec status 改为 approved 后再生成 plan"

### 生成逻辑

- 读取 spec 功能需求 → 推导技术选型对比表
- 读取 spec 接口契约 → 生成时序图（Mermaid 格式）
- 根据项目技术栈（前端 Vue 3 + Element Plus + Tailwind / 后端 Spring Boot + Nacos + Dubbo）填充架构方案
- 生成阶段划分、风险与回滚预案、测试策略
- 引用相关 ADR

### 提问（Plan 阶段）

- 技术选型偏好（当存在多种可选方案时）
- 里程碑划分确认
- 风险评估确认

### 输出

- 写入 `specs/<domain>/<NNN>-<slug>.plan.md`
- 运行 `npm run specs:validate`
- 请用户审批

## Phase 4: Tasks Generation（Tasks 生成）

### 门禁检查

- 读取对应 plan 的 `status` 字段
- 必须为 `approved` 才可进入

### 生成逻辑

- 读取 plan 的里程碑和阶段划分 → 拆解为可执行任务
- 每个 task 包含：ID（T-001 格式）、描述、负责人、依赖、DoD、状态(todo)
- 自动按依赖关系排序
- 任务粒度：每个 task 应可在 1-2 天内完成

### 输出

- 写入 `specs/<domain>/<NNN>-<slug>.tasks.md`
- 运行 `npm run specs:validate`
- 输出完成

## Error Handling

| 场景 | 处理方式 |
|------|----------|
| 验证失败 | skill 自动修复（调整 front-matter 格式、补全必填字段），再次验证 |
| 用户中途退出 | 已完成阶段文件保留，下次检测到已有文件时询问是否继续 |
| ID 冲突 | 重新扫描分配下一个可用 ID |
| 门禁未通过 | 明确提示用户需要先完成上一阶段审批 |
| brainstorming 输出不完整 | 正常进入提问流程补全缺失信息 |

## Validation Checklist（输出前自检）

每次生成文档前，skill 内部执行：

1. front-matter 所有必填字段已填写且格式正确
2. ID 符合 `^[A-Z]{2,8}-\d{3}$` 模式
3. version 为合法 semver
4. 日期为 `YYYY-MM-DD` 格式
5. depends-on 引用的 ID 实际存在
6. 无 TBD/TODO 占位符（未决问题除外，它们放在"未决问题"section）
7. 接口路径与已有 spec 无冲突
8. 权限标识命名一致（`<module>:<action>` 格式）

## Skill File Structure

单文件 skill：

```
spec-builder.md
├── Frontmatter (name, description, trigger)
├── Hard Rules (项目约束、命名规范、验证要求)
├── Phase 1: Context Sensing
├── Phase 2: Spec Generation (提问策略 + 输出格式)
├── Phase 3: Plan Generation (门禁 + 生成逻辑)
├── Phase 4: Tasks Generation (门禁 + 拆解逻辑)
├── Templates Reference (引用 specs/_templates/ 路径)
└── Validation Checklist
```

## Constraints（硬性约束）

- 所有输出必须通过 `npm run specs:validate`
- 文档语言：中文（与现有 spec 一致）
- 字段命名：snake_case（与现有数据契约一致）
- 接口路径前缀：`/api/`（与现有契约一致）
- 权限标识：`<module>:<action>` 格式
- 不修改已有 spec 文件（除非用户明确要求更新版本）
- Spec status 初始为 draft，由用户手动推进到 approved
