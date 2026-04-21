# 实现计划：短视频信息流应用

**分支**: `[001-short-video-feed]` | **日期**: 2026-03-31 | **规格**: [/Users/tenggk/code/openaipay/specs/001-short-video-feed/spec.md](/Users/tenggk/code/openaipay/specs/001-short-video-feed/spec.md)  
**输入**: 来自 `/specs/001-short-video-feed/spec.md` 的功能规格说明，以及用户补充的技术约束

**说明**: 本计划以现有 [openaipay](/Users/tenggk/code/openaipay) 单仓架构为基础，要求与现有体系保持一致、模块边界清晰、便于后续合并回主线。

## 摘要

本功能将在现有 `openaipay` 单仓库内新增一套相对独立的短视频领域能力。客户端先只做
iOS，采用 SwiftUI 和系统视频播放能力实现全屏信息流、上划切换、点赞、收藏与评论。
后端继续沿用当前 `backend/domain -> application -> infrastructure -> adapter-web`
的分层架构，新增 `shortvideo` 领域模块，并复用现有登录认证、用户体系、请求鉴权、
媒资基础设施和 MySQL 迁移机制。视频文件采用 S3 兼容对象存储，并通过 CDN 分发；
结构化数据继续保存在 MySQL。第一阶段不新增短视频专用 BFF，而是由 iOS 端直接调用
`adapter-web` 暴露的短视频 REST API，以减少跨层重复编排并降低与现有支付/BFF 路径
的耦合。

## 技术背景

**语言/版本**: Swift 5 + SwiftUI（iOS 客户端）、Java 21（Spring Boot 后端）  
**核心依赖**: SwiftUI、AVKit/AVFoundation、现有 `APIClient`/`AppState`/`AuthStore`；Spring Boot 3.4、MyBatis-Plus 3.5、Flyway、现有认证与媒资模块  
**存储方式**: MySQL 8 保存视频元数据、互动状态和评论；S3 兼容对象存储保存视频与封面；CDN 对外分发播放资源  
**测试方案**: iOS 使用 XCTest / Swift Package Tests；后端使用 JUnit5、Spring Boot Web 集成测试；关键 API 增加契约校验  
**目标平台**: iOS 原生 App + `openaipay` 单仓 Java 后端  
**项目类型**: 单仓内的 mobile-app + web-service 组合  
**性能目标**: 已登录用户进入短视频页后 2 秒内看到首条可播放视频；上划切换目标保持 60fps 交互流畅度；点赞/收藏/评论成功结果 2 秒内回显  
**约束条件**: 必须复用现有 `openaipay` 登录与用户体系；必须保持现有后端分层结构；短视频模块需尽量独立，避免侵入支付、资金、会计等核心热路径；必须支持后续推送到 `git@git.pingpongx.org:tenggk/openaipay.git` 的独立分支并方便合并  
**规模/范围**: 第一阶段只支持 iOS 登录用户浏览短视频、点赞、收藏、评论；不包含视频上传、创作者工具、推荐算法、直播、关注关系和商业化功能

## 宪章检查

*门禁：在 Phase 0 研究前必须通过，并在 Phase 1 设计后重新检查。*

- 规格产物已完整更新：`spec.md` 已覆盖登录前置、用户故事、验收标准、边界情况与假设。  
  结论：通过。
- 设计保持用户故事可独立交付：P1 先交付全屏视频流，P2 叠加点赞/收藏，P3 再叠加评论。  
  结论：通过。
- 每个行为变化都定义了验证策略：iOS 核心交互、后端接口、集成回归均已纳入计划。  
  结论：通过。
- 复杂度有明确理由：不新增新的顶层服务，不复制已有认证体系，不将短视频逻辑散落到现有支付域。  
  结论：通过。
- 文档、诊断和迁移影响已识别：需要新增短视频迁移脚本、运行配置、接口契约与本地联调说明。  
  结论：通过。

**设计后复核**: Phase 1 输出的 `research.md`、`data-model.md`、`contracts/` 和 `quickstart.md`
已保持与宪章一致，无需额外豁免。

## 项目结构

### 文档结构（当前功能）

```text
specs/001-short-video-feed/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── short-video.openapi.yaml
└── tasks.md
```

### 源码结构（仓库根目录）

```text
backend/
├── domain/src/main/java/cn/openaipay/domain/shortvideo/
│   ├── model/
│   ├── repository/
│   └── service/
├── application/src/main/java/cn/openaipay/application/shortvideo/
│   ├── command/
│   ├── dto/
│   ├── facade/
│   └── query/
├── infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/
│   ├── dataobject/
│   ├── mapper/
│   ├── repository/
│   └── storage/
├── adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/
│   ├── request/
│   └── response/
└── adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/

ios-app/OpenAiPay/
├── Core/
│   ├── APIClient.swift
│   ├── AppState.swift
│   ├── AuthStore.swift
│   └── Models.swift
└── Features/
    ├── Auth/
    └── ShortVideo/
        ├── Components/
        ├── Models/
        ├── ViewModels/
        └── Views/

local/flyway-migration/
└── Vxxx__create_short_video_tables.sql

app-bff/
└── 保持现状，Phase 1 不新增短视频专用聚合逻辑
```

**结构决策**: 后端保持现有 Maven 多模块与领域分层，只在每一层新增 `shortvideo`
命名空间，不创建新的顶层 `backend-shortvideo` 工程；iOS 端在 `Features/` 下新增
`ShortVideo` 功能目录，继续复用现有 `Core` 层中的网络、登录态和全局状态能力；
`app-bff` 在第一阶段保持不变，避免为只服务 iOS 的短视频流重复增加聚合层。

## 复杂度跟踪

当前无需要额外豁免的复杂度例外。以下设计选择已在本计划内完成合理化：

| 设计点 | 采用原因 | 放弃的更简单或更重方案 |
|--------|----------|------------------------|
| 直接使用 `adapter-web` 暴露短视频 API | 保持模块独立、减少 BFF 重复编排、便于后续合并 | 放弃新增短视频专用 BFF；当前阶段收益不足 |
| 使用 S3 兼容对象存储 + CDN | 更适合视频分发、与本地/生产环境兼容、易于替换具体厂商 | 放弃继续沿用本地文件系统；不适合视频规模与分发需求 |
| 复用现有登录与媒资基础设施 | 减少重复建设，保持用户体系一致 | 放弃新建独立账号系统或全新媒资子系统 |
