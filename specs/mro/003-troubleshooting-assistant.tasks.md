---
id: MRO-003
plan: mro/003-troubleshooting-assistant.plan.md
created: 2026-05-25
updated: 2026-05-25
---

# Tasks: 智能排故助手

| ID | 任务 | 负责人 | 依赖 | DoD | 状态 |
|----|------|--------|------|-----|------|
| T-001 | 配置 ES dense_vector 索引模板 + BM25 分析器 | @dev | - | 索引创建成功、可写入/检索 + Code Review | todo |
| T-002 | 实现文档解析服务（PDF/XML/纯文本→分段→结构化） | @dev | - | 多格式解析正确、分段合理 + 单测通过 + Code Review | todo |
| T-003 | 部署 Embedding 模型 (BGE-large-zh) + 向量生成服务 | @dev | - | API 可调用、返回正确维度向量 + Code Review | todo |
| T-004 | 实现知识库管理模块（CRUD + 文档上传 + 向量化流水线） | @dev | T-001, T-002, T-003 | 创建知识库→上传文档→自动向量化→状态 ready + Code Review | todo |
| T-005 | 部署 vLLM 推理服务 + 模型加载 | @dev | - | LLM 推理 API 可用、响应正常 + Code Review | todo |
| T-006 | 实现 RAG Pipeline（混合检索 + Reranker + Prompt 模板） | @dev | T-001, T-003, T-005 | 输入查询→检索→重排→生成方案 + Code Review | todo |
| T-007 | 实现引用标注提取器（从 LLM 输出中提取手册编号/章节） | @dev | T-006 | 引用来源准确提取并结构化 + 单测通过 + Code Review | todo |
| T-008 | 实现排故流程图生成（基于方案步骤自动生成 Mermaid 图） | @dev | T-006 | 流程图可渲染、步骤对应正确 + Code Review | todo |
| T-009 | 实现异步查询机制（提交→队列→处理→通知完成） | @dev | T-006 | 异步提交→轮询/回调获取结果 + Code Review | todo |
| T-010 | 部署 Whisper 语音识别服务 | @dev | - | 中文语音→文本转换准确 + Code Review | todo |
| T-011 | 实现输入适配层（代码解析 + 语音转文本 + NLU 标准化） | @dev | T-010 | 三种输入方式均可正确转为标准化查询 + 单测通过 + Code Review | todo |
| T-012 | 实现历史维修记录导入与查询服务 | @dev | T-001 | 历史数据可导入 ES + 按条件查询 + Code Review | todo |
| T-013 | 实现后端 REST API（知识库/查询/报告/历史/导出） | @dev | T-004, T-009, T-012 | 全量接口可调用、权限验证通过 + Code Review | todo |
| T-014 | 前端：排故查询界面（多输入方式 + 提交 + 等待） | @dev | T-013 | 代码/语音/文本输入→提交→Loading 状态 + Code Review | todo |
| T-015 | 前端：排故报告展示页（方案 + 引用 + 流程图 + 导出） | @dev | T-013 | 报告完整展示、引用可点击、流程图渲染 + Code Review | todo |
| T-016 | 前端：知识库管理页（知识库列表 + 文档上传 + 状态） | @dev | T-013 | CRUD 正常、上传进度显示、状态实时更新 + Code Review | todo |
| T-017 | 前端：历史统计页（故障频次 + 部件更换统计） | @dev | T-013 | 图表展示正确、筛选联动 + Code Review | todo |
| T-018 | 集成测试：全链路验证（多输入→RAG→方案→引用） | @dev | T-001~T-017 | 端到端流程通过 | todo |
| T-019 | 准确率评估：构建测试集 + Top-1/Top-3 命中率测试 | @dev | T-018 | 测试报告 + 命中率达标 | todo |
| T-020 | 性能测试：方案生成 ≤ 3min + 并发 ≥ 10 验证 | @dev | T-018 | 性能指标达标 + 测试报告 | todo |

> 状态枚举：todo / doing / review / done / blocked
