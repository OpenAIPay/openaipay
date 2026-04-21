---

description: "短视频信息流功能实现任务清单"
---

# 任务清单：短视频信息流应用

**输入**: 来自 `/specs/001-short-video-feed/` 的设计文档  
**前置条件**: `plan.md`（必需）、`spec.md`（必需）、`research.md`、`data-model.md`、`contracts/`、`quickstart.md`

**测试要求**: 按宪章与实现计划补充与风险匹配的验证任务。后端接口行为优先使用自动化测试；iOS 侧优先补充 `swift test --package-path ios-app` 可覆盖的纯逻辑测试。由于当前仓库没有现成的 iOS UI 自动化目标，播放、上划和评论面板交互保留显式手工冒烟任务，并以 `quickstart.md` 为准。

**组织方式**: 任务按用户故事分组，确保每个故事都能独立实现、独立验证、独立演示。

## 格式：`[ID] [P?] [Story] 描述`

- **[P]**: 可并行执行（不同文件、无直接依赖）
- **[Story]**: 任务所属的用户故事（例如 `US1`、`US2`、`US3`）
- 每条任务描述都包含明确文件路径，便于直接落地

## 路径约定

- **后端**: `backend/domain`、`backend/application`、`backend/infrastructure`、`backend/adapter-web`
- **iOS**: `ios-app/OpenAiPay/Core`、`ios-app/OpenAiPay/Features`
- **迁移与本地联调**: `local/flyway-migration`、`specs/001-short-video-feed`

## Phase 1: 准备阶段（共享基础设施）

**目标**: 完成短视频功能的数据库与配置初始化，让后续开发具备可运行基础。

- [X] T001 在 `local/flyway-migration/V166__create_short_video_tables.sql` 中创建 `short_video_post`、`short_video_stats`、`short_video_engagement`、`short_video_comment` 表及必要索引、唯一约束
- [X] T002 [P] 在 `local/flyway-migration/V167__seed_short_video_demo_data.sql` 中写入至少 3 条 `PUBLISHED + PUBLIC` 演示视频、初始统计与评论种子数据
- [X] T003 [P] 在 `backend/adapter-web/src/main/resources/application.yml` 与 `backend/adapter-web/src/main/resources/application-local.yml` 中补充短视频对象存储、CDN 与本地演示配置项

---

## Phase 2: 基础能力阶段（阻塞前置）

**目标**: 搭好所有用户故事共享的领域、持久化、鉴权与客户端基础骨架。  
**⚠️ 关键要求**: 本阶段完成前，不进入任何用户故事的正式实现。

- [X] T004 [P] 在 `backend/domain/src/main/java/cn/openaipay/domain/shortvideo/model/ShortVideoPost.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/model/VideoStats.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/model/UserVideoEngagement.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/model/VideoComment.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/model/FeedCursor.java` 中落地短视频领域模型
- [X] T005 [P] 在 `backend/domain/src/main/java/cn/openaipay/domain/shortvideo/repository/ShortVideoPostRepository.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/repository/ShortVideoStatsRepository.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/repository/ShortVideoEngagementRepository.java`、`backend/domain/src/main/java/cn/openaipay/domain/shortvideo/repository/ShortVideoCommentRepository.java` 中定义短视频仓储接口
- [X] T006 [P] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/dataobject/ShortVideoPostDO.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/dataobject/ShortVideoStatsDO.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/dataobject/ShortVideoEngagementDO.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/dataobject/ShortVideoCommentDO.java` 中建立 MyBatis-Plus 持久化对象
- [X] T007 [P] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/mapper/ShortVideoPostMapper.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/mapper/ShortVideoStatsMapper.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/mapper/ShortVideoEngagementMapper.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/mapper/ShortVideoCommentMapper.java` 中建立 Mapper 接口
- [X] T008 [P] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoPostRepositoryImpl.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoStatsRepositoryImpl.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoEngagementRepositoryImpl.java`、`backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoCommentRepositoryImpl.java` 中建立仓储实现骨架
- [X] T009 [P] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoAuthorDTO.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoPlaybackDTO.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoStatsSnapshotDTO.java` 与 `backend/application/src/main/java/cn/openaipay/application/shortvideo/port/ShortVideoPlaybackPort.java` 中建立应用层共享 DTO 与播放端口
- [X] T010 在 `backend/adapter-web/src/main/java/cn/openaipay/adapter/security/UserWebMvcConfig.java` 中把 `/api/short-video/**` 纳入登录鉴权拦截范围
- [X] T011 [P] 在 `ios-app/OpenAiPay/Core/Models.swift`、`ios-app/OpenAiPay/Core/ShortVideoState.swift`、`ios-app/OpenAiPay/Core/APIClient.swift` 与 `ios-app/Package.swift` 中补充短视频共享模型、可测试纯逻辑骨架与测试入口
- [X] T012 [P] 在 `ios-app/OpenAiPay/Features/ShortVideo/Models/ShortVideoModels.swift`、`ios-app/OpenAiPay/Features/ShortVideo/ViewModels/ShortVideoFeedViewModel.swift`、`ios-app/OpenAiPay/Features/ShortVideo/Views/ShortVideoFeedView.swift`、`ios-app/OpenAiPay/Features/ShortVideo/Components/ShortVideoPlayerView.swift` 中建立客户端短视频模块骨架

**检查点**: 数据模型、仓储骨架、鉴权入口和 iOS 短视频模块骨架已就绪，可以开始按用户故事推进。

---

## Phase 3: 用户故事 1 - 全屏浏览短视频流 (优先级: P1) 🎯 MVP

**目标**: 已登录用户进入视频 Tab 后，能够立即看到首条全屏视频，并通过上划切换到下一条视频。  
**独立测试方式**: 运行 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoFeedControllerTest.java` 与 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoFeedStateTests.swift`，再按 `specs/001-short-video-feed/quickstart.md` 手工验证登录拦截、首条播放、上划切换和失败恢复。

### 用户故事 1 的验证任务

- [X] T013 [P] [US1] 在 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoFeedControllerTest.java` 编写信息流鉴权、首屏拉取、游标翻页和空结果集成测试
- [X] T014 [P] [US1] 在 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoFeedStateTests.swift` 编写信息流解码、游标推进、预取与失败恢复状态测试

### 用户故事 1 的实现任务

- [X] T015 [P] [US1] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/query/ListShortVideoFeedQuery.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoFeedItemDTO.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoFeedPageDTO.java` 中实现信息流查询模型
- [X] T016 [US1] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoPostRepositoryImpl.java` 与 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoStatsRepositoryImpl.java` 中实现公开视频过滤、排序与游标查询
- [X] T017 [US1] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/service/ShortVideoFeedService.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/service/impl/ShortVideoFeedServiceImpl.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/ShortVideoFacade.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/impl/ShortVideoFacadeImpl.java` 中实现已登录信息流装配
- [X] T018 [US1] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/storage/S3ShortVideoPlaybackGateway.java` 与 `backend/adapter-web/src/main/resources/application.yml` 中接通播放地址解析与对象存储配置读取
- [X] T019 [US1] 在 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoFeedController.java` 与 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/response/ShortVideoFeedResponse.java` 中实现 `/api/short-video/feed`
- [X] T020 [US1] 在 `ios-app/OpenAiPay/Core/APIClient.swift` 与 `ios-app/OpenAiPay/Core/Models.swift` 中实现信息流请求、响应解码与鉴权失败处理
- [X] T021 [P] [US1] 在 `ios-app/OpenAiPay/Core/ShortVideoState.swift`、`ios-app/OpenAiPay/Features/ShortVideo/Models/ShortVideoModels.swift`、`ios-app/OpenAiPay/Features/ShortVideo/ViewModels/ShortVideoFeedViewModel.swift` 中实现首屏加载、游标翻页与预取状态流
- [X] T022 [P] [US1] 在 `ios-app/OpenAiPay/Features/ShortVideo/Views/ShortVideoFeedView.swift` 与 `ios-app/OpenAiPay/Features/ShortVideo/Components/ShortVideoPlayerView.swift` 中实现全屏播放、上划切换、加载中与失败态
- [X] T023 [US1] 在 `ios-app/OpenAiPay/Features/Home/HomeView.swift` 与 `ios-app/OpenAiPay/ContentView.swift` 中把视频 Tab 从 `VideoReference` 占位切换到真实短视频入口并保持登录前置
- [ ] T024 [US1] 按 `specs/001-short-video-feed/quickstart.md` 执行登录拦截、首条播放、上划切换和失败恢复冒烟验证

**检查点**: 用户可以登录后进入真实短视频流，完成全屏浏览与上划切换，满足 MVP。

---

## Phase 4: 用户故事 2 - 点赞与收藏视频 (优先级: P2)

**目标**: 用户可以对当前视频点赞、取消点赞、收藏、取消收藏，并看到状态即时回显。  
**独立测试方式**: 运行 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoEngagementControllerTest.java` 与 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoEngagementStateTests.swift`，再按 `specs/001-short-video-feed/quickstart.md` 手工验证点赞/收藏闭环。

### 用户故事 2 的验证任务

- [X] T025 [P] [US2] 在 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoEngagementControllerTest.java` 编写点赞、取消点赞、收藏、取消收藏、幂等与 401/404 集成测试
- [X] T026 [P] [US2] 在 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoEngagementStateTests.swift` 编写点赞/收藏 optimistic update、重复点击与失败回滚测试

### 用户故事 2 的实现任务

- [X] T027 [P] [US2] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/command/LikeShortVideoCommand.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/command/FavoriteShortVideoCommand.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoEngagementDTO.java` 中实现互动命令与 DTO
- [X] T028 [US2] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoEngagementRepositoryImpl.java` 与 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoStatsRepositoryImpl.java` 中实现点赞/收藏幂等写入与统计更新
- [X] T029 [US2] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/service/ShortVideoEngagementService.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/service/impl/ShortVideoEngagementServiceImpl.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/ShortVideoFacade.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/impl/ShortVideoFacadeImpl.java` 中实现互动服务与门面编排
- [X] T030 [US2] 在 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoEngagementController.java` 与 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/response/ShortVideoEngagementResponse.java` 中实现点赞/收藏接口
- [X] T031 [US2] 在 `ios-app/OpenAiPay/Core/APIClient.swift` 与 `ios-app/OpenAiPay/Core/Models.swift` 中实现点赞/收藏接口、互动状态解码与错误透传
- [X] T032 [US2] 在 `ios-app/OpenAiPay/Core/ShortVideoState.swift`、`ios-app/OpenAiPay/Features/ShortVideo/Components/ShortVideoActionToolbar.swift`、`ios-app/OpenAiPay/Features/ShortVideo/ViewModels/ShortVideoFeedViewModel.swift` 中实现点赞/收藏即时回显与失败回滚
- [ ] T033 [US2] 按 `specs/001-short-video-feed/quickstart.md` 执行点赞、取消点赞、收藏、取消收藏与重复点击准确性验证

**检查点**: 用户在视频流中对当前视频的点赞和收藏状态可即时更新，并在失败时正确回滚。

---

## Phase 5: 用户故事 3 - 查看与发布评论 (优先级: P3)

**目标**: 用户可以打开评论区、查看评论、提交非空评论，并在返回视频流后保持当前浏览上下文。  
**独立测试方式**: 运行 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoCommentControllerTest.java` 与 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoCommentStateTests.swift`，再按 `specs/001-short-video-feed/quickstart.md` 手工验证评论区交互。

### 用户故事 3 的验证任务

- [X] T034 [P] [US3] 在 `backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoCommentControllerTest.java` 编写评论列表、发布评论、空评论拦截与 404 集成测试
- [X] T035 [P] [US3] 在 `ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoCommentStateTests.swift` 编写评论输入校验、分页合并、提交成功与失败提示测试

### 用户故事 3 的实现任务

- [X] T036 [P] [US3] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/command/CreateShortVideoCommentCommand.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/query/ListShortVideoCommentsQuery.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/dto/ShortVideoCommentDTO.java` 中实现评论命令、查询与 DTO
- [X] T037 [US3] 在 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoCommentRepositoryImpl.java` 与 `backend/infrastructure/src/main/java/cn/openaipay/infrastructure/shortvideo/repository/ShortVideoStatsRepositoryImpl.java` 中实现评论分页查询、评论写入与评论数更新
- [X] T038 [US3] 在 `backend/application/src/main/java/cn/openaipay/application/shortvideo/service/ShortVideoCommentService.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/service/impl/ShortVideoCommentServiceImpl.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/ShortVideoFacade.java`、`backend/application/src/main/java/cn/openaipay/application/shortvideo/facade/impl/ShortVideoFacadeImpl.java` 中实现评论服务与门面编排
- [X] T039 [US3] 在 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoCommentController.java`、`backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/request/CreateShortVideoCommentRequest.java`、`backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/response/ShortVideoCommentResponse.java` 中实现评论查询与发布接口
- [X] T040 [US3] 在 `ios-app/OpenAiPay/Core/APIClient.swift`、`ios-app/OpenAiPay/Core/Models.swift` 与 `ios-app/OpenAiPay/Features/ShortVideo/Models/ShortVideoCommentModels.swift` 中实现评论接口和模型映射
- [X] T041 [US3] 在 `ios-app/OpenAiPay/Core/ShortVideoState.swift`、`ios-app/OpenAiPay/Features/ShortVideo/ViewModels/ShortVideoCommentSheetViewModel.swift`、`ios-app/OpenAiPay/Features/ShortVideo/Views/ShortVideoCommentSheetView.swift` 中实现评论列表、非空校验与提交反馈
- [X] T042 [US3] 在 `ios-app/OpenAiPay/Features/ShortVideo/ViewModels/ShortVideoFeedViewModel.swift` 与 `ios-app/OpenAiPay/Features/ShortVideo/Views/ShortVideoFeedView.swift` 中接入评论面板并保证返回后保留当前视频位置
- [ ] T043 [US3] 按 `specs/001-short-video-feed/quickstart.md` 执行评论查看、发布、空评论拦截与返回上下文保持验证

**检查点**: 评论区可独立打开与关闭，评论写入后能正确回显，返回视频流后仍停留在原视频上下文。

---

## Phase 6: 收尾与横切优化

**目标**: 完成跨故事文档、可观测性和发布前验证收尾。

- [X] T044 [P] 在 `specs/001-short-video-feed/contracts/short-video.openapi.yaml` 与 `specs/001-short-video-feed/quickstart.md` 中回填实现后的真实字段、错误码、联调步骤与限制说明
- [X] T045 [P] 在 `backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoFeedController.java`、`backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoEngagementController.java`、`backend/adapter-web/src/main/java/cn/openaipay/adapter/shortvideo/web/ShortVideoCommentController.java` 与 `ios-app/OpenAiPay/Core/AppState.swift` 中补充页面访问、互动行为与故障诊断日志/埋点
- [X] T046 在 `backend/pom.xml`、`ios-app/Package.swift` 对应的测试入口上运行全量自动化验证并修复跨故事回归问题
- [ ] T047 按 `specs/001-short-video-feed/quickstart.md` 完整执行短视频功能端到端冒烟与发布前检查

---

## 依赖与执行顺序

### 阶段依赖

- **Phase 1: 准备阶段**: 无依赖，可立即开始
- **Phase 2: 基础能力阶段**: 依赖 Phase 1 完成，并阻塞所有用户故事
- **Phase 3: US1**: 依赖 Phase 2 完成，是 MVP 主路径
- **Phase 4: US2**: 依赖 Phase 2 完成；后端与状态逻辑可先行，但最终客户端集成依赖 US1 的真实视频流入口
- **Phase 5: US3**: 依赖 Phase 2 完成；后端与状态逻辑可先行，但评论面板接入依赖 US1 的真实视频流入口
- **Phase 6: 收尾阶段**: 依赖所有目标用户故事完成

### 用户故事依赖

- **US1（P1）**: 无用户故事前置依赖，是第一阶段 MVP
- **US2（P2）**: 互动业务本身不依赖评论功能，但需要一个可用的当前视频上下文；建议在 US1 的视频流骨架完成后合并客户端集成
- **US3（P3）**: 评论业务本身不依赖点赞/收藏，但需要一个可用的当前视频上下文；建议在 US1 的视频流骨架完成后合并客户端集成

### 每个用户故事内部顺序

- 自动化测试任务先落地，并先观察失败
- 先补充命令、查询、DTO 等契约，再实现仓储和服务
- 服务层完成后，再暴露 Web 接口
- iOS 侧先接 API/状态，再接页面与交互
- 手工冒烟任务放在对应故事最后，确保完成后能独立演示

---

## 并行机会

- Phase 1 中的 `T002`、`T003` 可并行
- Phase 2 中的 `T004`~`T009`、`T011`、`T012` 可在不冲突文件上并行
- US1 中的 `T013`、`T014`、`T015` 可并行；`T021` 与 `T022` 可在 API 契约稳定后并行
- US2 中的 `T025`、`T026`、`T027` 可并行
- US3 中的 `T034`、`T035`、`T036` 可并行
- Phase 6 中的 `T044`、`T045` 可并行

---

## 并行示例：用户故事 1

```bash
Task: "在 backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoFeedControllerTest.java 编写信息流集成测试"
Task: "在 ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoFeedStateTests.swift 编写信息流状态测试"
Task: "在 backend/application/src/main/java/cn/openaipay/application/shortvideo/query/ListShortVideoFeedQuery.java 等文件中实现信息流查询模型"
```

## 并行示例：用户故事 2

```bash
Task: "在 backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoEngagementControllerTest.java 编写互动接口测试"
Task: "在 ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoEngagementStateTests.swift 编写互动状态测试"
Task: "在 backend/application/src/main/java/cn/openaipay/application/shortvideo/command/LikeShortVideoCommand.java 等文件中实现互动命令模型"
```

## 并行示例：用户故事 3

```bash
Task: "在 backend/adapter-web/src/test/java/cn/openaipay/adapter/shortvideo/web/ShortVideoCommentControllerTest.java 编写评论接口测试"
Task: "在 ios-app/Tests/OpenAiPayCoreLogicTests/ShortVideoCommentStateTests.swift 编写评论状态测试"
Task: "在 backend/application/src/main/java/cn/openaipay/application/shortvideo/command/CreateShortVideoCommentCommand.java 等文件中实现评论命令模型"
```

---

## 实施策略

### 先做 MVP（仅用户故事 1）

1. 完成 Phase 1 与 Phase 2
2. 完成 Phase 3（US1）
3. 运行 `T013`、`T014`、`T024` 验证首屏播放、上划切换和登录前置
4. 在 MVP 验证通过前，不引入点赞、收藏和评论复杂度

### 增量交付

1. 先交付 US1，拿到真实短视频信息流入口
2. 在不破坏 US1 的前提下交付 US2，补齐点赞/收藏闭环
3. 最后交付 US3，补齐评论区与上下文保持
4. 每完成一个故事，都执行对应的自动化验证和手工冒烟

### 并行协作策略

1. 一名开发者优先打通 Phase 1 与 Phase 2
2. 基础能力稳定后：
   - 开发者 A：推进 US1 的后端信息流与 iOS 视频流入口
   - 开发者 B：并行推进 US2 的互动接口和状态逻辑
   - 开发者 C：并行推进 US3 的评论接口和评论状态逻辑
3. 待 US1 的客户端骨架完成后，再合并 US2、US3 的前端集成任务

---

## 备注

- 所有 `[P]` 任务都以“不修改同一文件”为前提
- `T024`、`T033`、`T043`、`T047` 是显式手工验证任务，不可省略
- 若实现中发现 OpenAPI、数据模型或 quickstart 与代码不一致，必须回到 `specs/001-short-video-feed/` 同步文档
- 建议每完成一个阶段就提交一次，降低跨层大改动的回归风险
