---
id: MRO-004
plan: mro/004-manual-management.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 智慧维修手册管理

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 部署 MinIO 对象存储，配置手册存储 bucket | @dev | - | 文件上传/下载/删除正常 + Code Review | todo |
| T-002 | 配置 ES 索引模板（手册结构化内容 + 向量字段） | @dev | - | 索引创建、写入、全文检索正常 + Code Review | todo |
| T-003 | 实现 PDF 解析器（分章节/段落结构化） | @dev | - | PDF 解析正确、章节层级保留 + 单测通过 + Code Review | todo |
| T-004 | 实现 XML 解析器（ATA 标准 XML 手册） | @dev | - | XML 解析正确、元数据提取 + 单测通过 + Code Review | todo |
| T-005 | 实现 SGML 解析器（航空手册 DTD 适配） | @dev | - | 主要 DTD 格式解析正确 + 单测通过 + Code Review | todo |
| T-006 | 实现文档解析调度服务（上传→解析→ES 索引） | @dev | T-001, T-002, T-003, T-004, T-005 | 上传后自动解析入库、状态流转正常 + Code Review | todo |
| T-007 | 实现全文搜索服务（BM25 + 语义检索 + 高亮） | @dev | T-002 | 搜索结果相关、高亮准确、分页正常 + Code Review | todo |
| T-008 | 训练航空领域翻译模型（50万句双语语料微调） | @dev | - | 模型训练完成、BLEU ≥ 0.85 + 训练报告 | todo |
| T-009 | 部署翻译推理服务 + 翻译任务异步处理 | @dev | T-008 | 翻译 API 可调用、异步任务流转正常 + Code Review | todo |
| T-010 | 实现版本管理模块（修订 + 历史 + diff 对比） | @dev | T-002 | 新版本创建、历史列表、diff 展示 + Code Review | todo |
| T-011 | 实现手册发布流程（审核→发布→生效） | @dev | T-010 | 发布状态流转正常、生效日期生效 + Code Review | todo |
| T-012 | 实现后端 REST API（手册 CRUD/搜索/翻译/版本/发布） | @dev | T-006, T-007, T-009, T-010, T-011 | 全量接口可调用、权限验证通过 + Code Review | todo |
| T-013 | 前端：手册上传与管理页 | @dev | T-012 | 上传进度、解析状态、列表管理正常 + Code Review | todo |
| T-014 | 前端：手册浏览与搜索页（全文搜索 + 章节导航） | @dev | T-012 | 搜索→高亮→章节跳转流畅 + Code Review | todo |
| T-015 | 前端：翻译管理页（提交翻译 + 进度 + 结果查看） | @dev | T-012 | 翻译任务提交/进度/中英对照查看 + Code Review | todo |
| T-016 | 前端：版本历史与 diff 页 | @dev | T-012 | 版本列表、diff 对比高亮 + Code Review | todo |
| T-017 | 前端：移动端 PWA 适配（离线缓存 + 响应式） | @dev | T-014 | 移动端浏览正常、可离线查看已缓存章节 + Code Review | todo |
| T-018 | 实现与工卡系统的手册跳转接口 | @dev | T-012 | 工卡端点击手册链接→跳转到对应章节 + Code Review | todo |
| T-019 | 集成测试：上传→解析→搜索→翻译→发布 全链路 | @dev | T-001~T-018 | 端到端流程通过 | todo |
| T-020 | 翻译准确率评估：100 组测试集 + 术语抽检 | @dev | T-019 | 准确率 ≥ 95% + 评估报告 | todo |
| T-021 | 性能测试：搜索 ≤ 2s + PDF 解析 ≤ 60s/100页 | @dev | T-019 | 性能指标达标 + 测试报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
