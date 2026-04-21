# Phase 1 数据模型：短视频信息流应用

## 设计目标

- 只覆盖第一阶段所需的“浏览、点赞、收藏、评论”闭环。
- 尽量复用现有 `user`、`auth`、`media` 基础设施，不重复建设账号和媒资体系。
- 保持短视频领域模型相对独立，便于未来从单体模块演进为可拆分边界。

## 实体 1：短视频内容（VideoPost）

**用途**: 表示一条可出现在视频流中的短视频内容，是信息流、互动和评论的主实体。

| 字段 | 类型 | 说明 |
|------|------|------|
| `videoId` | String | 业务唯一标识，对外暴露给 API 与客户端 |
| `creatorUserId` | Long | 视频作者，复用现有用户体系 |
| `caption` | String | 视频文案，允许为空但长度受限 |
| `coverMediaId` | String | 封面媒资标识，复用现有 `media` 体系 |
| `playbackMediaId` | String | 播放媒资标识，指向视频播放资源 |
| `durationMs` | Long | 时长，毫秒 |
| `aspectRatio` | String | 画面比例，例如 `9:16` |
| `publishStatus` | Enum | 发布状态，例如 `DRAFT / PUBLISHED / HIDDEN / REMOVED` |
| `visibilityStatus` | Enum | 可见性状态，例如 `PUBLIC / PRIVATE / BLOCKED` |
| `feedPriority` | Integer | 信息流排序权重，用于首阶段的人工/规则排序 |
| `publishedAt` | DateTime | 发布时间 |
| `createdAt` | DateTime | 创建时间 |
| `updatedAt` | DateTime | 更新时间 |

**校验规则**

- `videoId` 必须存在且全局唯一。
- `creatorUserId` 必须指向现有有效用户。
- `playbackMediaId`、`coverMediaId` 必须能解析到有效媒资。
- `durationMs` 必须大于 0。
- `caption` 建议限制在 500 字以内。
- 只有 `publishStatus=PUBLISHED` 且 `visibilityStatus=PUBLIC` 的内容才能进入默认视频流。

**状态流转**

```text
DRAFT -> PUBLISHED -> HIDDEN -> REMOVED
```

第一阶段不建设上传工作流，因此 `DRAFT -> PUBLISHED` 更可能由后台导入、脚本灌数或后续管理能力触发。

## 实体 2：短视频统计快照（VideoStats）

**用途**: 承载列表页和详情页需要直接展示的互动统计，避免每次请求都做重聚合。

| 字段 | 类型 | 说明 |
|------|------|------|
| `videoId` | String | 对应视频标识 |
| `likeCount` | Long | 点赞数 |
| `favoriteCount` | Long | 收藏数 |
| `commentCount` | Long | 评论数 |
| `playCount` | Long | 播放计数，可在第一阶段作为可选观测字段 |
| `updatedAt` | DateTime | 最近一次统计更新时间 |

**校验规则**

- 所有统计字段必须非负。
- 统计更新与用户互动写入必须保证最终一致。

## 实体 3：用户视频互动（UserVideoEngagement）

**用途**: 记录“某个用户对某条视频”的互动状态，是点赞/收藏能力的主实体。

| 字段 | 类型 | 说明 |
|------|------|------|
| `engagementId` | String | 互动记录标识 |
| `videoId` | String | 关联视频 |
| `userId` | Long | 关联用户 |
| `liked` | Boolean | 是否已点赞 |
| `favorited` | Boolean | 是否已收藏 |
| `likedAt` | DateTime? | 最近点赞时间 |
| `favoritedAt` | DateTime? | 最近收藏时间 |
| `lastViewedAt` | DateTime? | 最近浏览时间，可作为后续扩展字段 |
| `createdAt` | DateTime | 创建时间 |
| `updatedAt` | DateTime | 更新时间 |

**校验规则**

- `videoId + userId` 必须唯一，避免同一用户为同一视频产生多条状态记录。
- 点赞和收藏可独立存在，任意一个为 `true` 都应保留记录。
- 当 `liked=false` 且 `favorited=false` 时，可保留空壳状态记录或物理删除，具体实现由应用层统一约束。

## 实体 4：视频评论（VideoComment）

**用途**: 表示某个用户在某条视频下发布的评论。

| 字段 | 类型 | 说明 |
|------|------|------|
| `commentId` | String | 评论标识 |
| `videoId` | String | 关联视频 |
| `userId` | Long | 评论作者 |
| `content` | String | 评论内容 |
| `status` | Enum | 状态，例如 `ACTIVE / HIDDEN / DELETED` |
| `createdAt` | DateTime | 创建时间 |
| `updatedAt` | DateTime | 更新时间 |

**校验规则**

- `content` 必须去除首尾空白后仍非空。
- `content` 建议限制在 500 字以内。
- 只有 `status=ACTIVE` 的评论默认展示给客户端。

**状态流转**

```text
ACTIVE -> HIDDEN
ACTIVE -> DELETED
```

## 实体 5：信息流游标（FeedCursor）

**用途**: 表示客户端向下浏览视频流时的翻页上下文。它更偏运行时对象，而不是必须落库的实体。

| 字段 | 类型 | 说明 |
|------|------|------|
| `cursorToken` | String | 对外返回给客户端的游标 |
| `lastSortKey` | String/Long | 上一条视频的排序位置 |
| `lastVideoId` | String | 上一条视频标识 |
| `generatedAt` | DateTime | 游标生成时间 |

**校验规则**

- 游标必须可验证且不可被客户端任意篡改。
- 游标过期后应允许客户端重新拉取首屏。

## 与现有系统的关系

### 复用现有实体或基础设施

- **用户身份**: 复用现有 `auth / user` 体系，不新增短视频账号系统。
- **请求鉴权**: 复用 `UserSecurityInterceptor` 和现有 Bearer Token。
- **媒资抽象**: 复用当前 `media` 领域的媒资模型和查询能力，但需要扩展到视频播放场景。

### 第一阶段新增的短视频领域边界

- 短视频主体与视频流排序
- 点赞/收藏互动状态
- 评论模型与评论列表
- 短视频统计快照

## 建议的数据库落地拆分

为第一阶段，建议至少新增以下表：

- `short_video_post`
- `short_video_stats`
- `short_video_engagement`
- `short_video_comment`

若后续需要更强的内容运营能力，再引入视频编排、推荐、标签、审核等扩展表。
