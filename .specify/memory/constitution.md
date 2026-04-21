<!--
同步影响报告
版本变更: 1.0.0 -> 1.1.0
已修改原则:
- V. Observable And Documented Change -> V. 可观测且有据可依的变更
新增内容:
- 工程标准中新增“项目 Markdown 文档默认使用简体中文撰写”的约束
- 全量中文化现有宪章与模板文案
移除内容:
- 无
需要同步的模板:
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/spec-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/plan-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/tasks-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/checklist-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/agent-file-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/.specify/templates/constitution-template.md
- ✅ 已更新 /Users/tenggk/code/openaipay/specs/001-short-video-feed/spec.md
- ✅ 已更新 /Users/tenggk/code/openaipay/specs/001-short-video-feed/checklists/requirements.md
- ✅ /Users/tenggk/code/openaipay/.specify/templates/commands 不存在，无需同步
后续待办:
- 无
-->
# spec-kit 项目宪章

## 核心原则

### I. 先规格后交付
所有非琐碎变更都 MUST 先更新 spec-kit 产物，包括 `spec.md`、`plan.md`、
`tasks.md`，以及在治理规则变化时同步更新宪章。实现内容 MUST 能追溯到
明确的用户需求和已确认的计划。若是不会改变行为的小修复，可以走更轻量的
流程，但仍 MUST 在对应任务、评审或提交说明中留下记录。

### II. 用户价值独立可交付
功能 MUST 按照可独立交付的用户故事拆分，并保留清晰的 MVP 路径。每个用户
故事 MUST 都有明确的验收场景和独立验证方式，确保它可以单独实现、单独验证、
单独演示。跨故事耦合 MUST 尽量减少；如果确实无法避免，MUST 在实现计划中
显式说明依赖关系。

### III. 合并前必须验证
每一项行为变化在合并前 MUST 具备与风险相匹配的验证。`plan.md` MUST 说明
验证策略，`tasks.md` MUST 列出证明该变化成立所需的具体测试或验证工作。
对于可长期回归的行为变化，优先使用自动化测试；如果未使用自动化，计划或
任务中 MUST 记录原因，以及替代验证方式的具体步骤。

### IV. 简单优先，复杂度需有理由
设计 MUST 优先选择满足当前规格所需的最简单方案。引入新的抽象、服务、
框架或依赖时，必须附带书面理由，说明为何简单方案不足。大型重构 MUST 保持
现有行为不变，除非规格与计划中已经明确记录了预期行为变化。

### V. 可观测且有据可依的变更
所有面向用户、运维层面或会影响数据的改动 MUST 同步更新文档与必要的运行期
信号，使系统在变更后仍可被理解、验证与排障。错误处理 MUST 明确；当日志、
提示信息或诊断信息能显著提升问题定位效率时，MUST 一并补齐。任何改变使用
流程的功能，也 MUST 更新 quickstart、使用说明或相关开发文档。

## 工程标准

- 代码 MUST 保持可读、格式统一，并符合仓库既有约定。
- 密钥、令牌和敏感配置 MUST NOT 被提交到仓库。示例配置必须使用占位值，并
  说明所需环境变量。
- 依赖 SHOULD 尽量精简。新增依赖 MUST 对交付速度、可靠性或可维护性有明确
  收益。
- 接口、数据契约或开发流程发生破坏性变化时，MUST 在规格、计划和迁移说明中
  明确指出。
- 项目内默认生成和维护的 Markdown 文档 MUST 使用简体中文撰写；代码标识符、
  协议字段、命令名、库名等必须保留原文的内容，可以在中文语境中原样引用。

## 工作流与质量门禁

- `spec.md` MUST 在计划开始前覆盖用户场景、验收标准、边界情况与关键假设。
- `plan.md` MUST 在实现前通过 Constitution Check，并记录结构选择、约束条件、
  复杂度权衡和验证策略。
- `tasks.md` MUST 按用户故事组织工作内容，以便每个故事都能独立完成和独立
  验证。
- 代码评审 MUST 检查实现是否可追溯回规格、计划和任务，并确认验证与文档
  义务已满足。
- 在变更被视为完成前，计划中的验证步骤 MUST 已执行，且所有受影响的快速开始、
  运维说明或开发文档 MUST 已同步更新。

## 治理

本宪章高于仓库中的非正式习惯做法。任何修订 MUST 与受影响的模板或指导文档
在同一变更中完成，评审者 MUST 同时确认同步影响报告仍然准确。宪章版本遵循
语义化版本：MAJOR 用于不兼容的原则调整或移除，MINOR 用于新增原则或实质性
扩展义务，PATCH 用于澄清或纯文字层面的优化。合规检查发生在计划评审、任务
生成、代码评审和合并前验证四个阶段。若某项变更无法满足某条原则，计划中
MUST 记录该例外、必要性原因，以及允许的最小范围。

**Version**: 1.1.0 | **Ratified**: 2026-03-30 | **Last Amended**: 2026-03-30
