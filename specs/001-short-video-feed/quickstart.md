# Phase 1 快速开始：短视频信息流应用

本说明描述短视频功能完成后的本地联调与基础验证路径，目标是让开发者可以在
现有 `openaipay` 单仓中快速启动短视频能力并完成冒烟验证。

## 1. 准备代码与分支

```bash
cd /Users/tenggk/code/openaipay
git checkout 001-short-video-feed
```

## 2. 启动 MySQL

继续沿用现有本地数据库方式：

```bash
docker compose -f docker-compose.local.yml up -d
```

## 3. 准备视频对象存储

短视频播放资源采用 S3 兼容对象存储。开发环境推荐使用 MinIO 或其他兼容实现，
并至少创建一个用于短视频资源的 bucket，例如 `short-video`.

建议准备以下环境变量：

```bash
export OPENAIPAY_VIDEO_STORAGE_MODE=s3
export OPENAIPAY_VIDEO_S3_ENDPOINT=http://127.0.0.1:9000
export OPENAIPAY_VIDEO_S3_REGION=local
export OPENAIPAY_VIDEO_S3_BUCKET=short-video
export OPENAIPAY_VIDEO_S3_ACCESS_KEY=minioadmin
export OPENAIPAY_VIDEO_S3_SECRET_KEY=minioadmin
export OPENAIPAY_VIDEO_CDN_BASE_URL=http://127.0.0.1:9000/short-video
```

说明：

- 生产环境应接入真实 S3 兼容对象存储与 CDN。
- 本地环境不强制要求单独 CDN，可直接通过对象存储回源地址验证播放。

## 4. 启动后端

沿用现有后端启动方式，并启用本地配置：

```bash
SPRING_PROFILES_ACTIVE=local \
SPRING_FLYWAY_ENABLED=true \
OPENAIPAY_DB_HOST=127.0.0.1 \
OPENAIPAY_DB_PORT=13306 \
OPENAIPAY_DB_NAME=openaipay \
OPENAIPAY_DB_USERNAME=root \
OPENAIPAY_DB_PASSWORD=openaipay \
mvn -f backend/adapter-web/pom.xml spring-boot:run
```

## 5. 启动 iOS 客户端

```bash
open /Users/tenggk/code/openaipay/ios-app/OpenAiPay.xcodeproj
```

建议验证时确认以下运行配置：

- `OpenAiPayBffBaseURL` 指向当前网关根地址；短视频第一阶段可以直接指向后端根地址，例如 `http://127.0.0.1:8080`
- `OpenAiPayBackendBaseURL` 建议与上面的根地址保持一致，避免其他接口走到不同环境
- iOS 侧直接调用 `/api/short-video/**`，不依赖新的短视频专用 BFF 聚合层
- 使用现有登录流程完成认证，再进入短视频入口

## 6. 导入或准备演示视频

在第一阶段，视频内容可以通过以下任一方式准备：

- 使用迁移脚本或初始化脚本写入演示视频元数据
- 通过后续管理工具导入测试视频
- 使用开发专用脚本向对象存储上传视频文件与封面，并写入 `short_video_*` 表

最低准备内容：

- 至少 3 条 `PUBLISHED + PUBLIC` 的短视频数据
- 每条视频包含封面、播放资源、作者信息和基础统计

## 7. 自动化验证

建议先跑完自动化验证，再做手工冒烟：

```bash
cd /Users/tenggk/code/openaipay

mvn -f backend/pom.xml -pl adapter-web -am test \
  -Dtest=ShortVideoFeedControllerTest,ShortVideoEngagementControllerTest,ShortVideoCommentControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false

swift test --package-path ios-app

xcodebuild -project ios-app/OpenAiPay.xcodeproj \
  -scheme OpenAiPay \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  build CODE_SIGNING_ALLOWED=NO
```

当前自动化重点覆盖：

- 后端：信息流、点赞/收藏、评论接口的鉴权、成功响应、分页、校验失败与不存在视频场景
- iOS 纯逻辑：信息流分页、点赞/收藏 optimistic update、评论输入校验、评论列表合并与提交反馈
- iOS 工程：`OpenAiPay` 主工程可成功编译，确保 SwiftUI 页面集成没有回归

## 8. 冒烟验证

完成登录后，在 iOS 端验证以下最小闭环：

1. 进入短视频页后，首条视频能正常展示并开始播放
2. 上划后可以切换到下一条视频
3. 点赞状态可以立即回显，再次点击可以取消
4. 收藏状态可以立即回显，再次点击可以取消
5. 打开评论区能看到评论列表
6. 提交非空评论后，评论能出现在当前视频下
7. 关闭评论区后，仍停留在刚才浏览的那条视频
8. 弱网或资源异常时，界面能展示可恢复的失败状态

## 9. 联调时需要关注的字段与错误码

当前接口契约已经与代码对齐，联调时重点确认以下字段：

- `/api/short-video/feed` 返回 `items`、`nextCursor`、`hasMore`；每个条目包含 `videoId`、`caption`、`author`、`coverUrl`、`playback`、`engagement`
- `playback` 当前实际字段为 `playbackUrl`、`protocol`、`mimeType`、`durationMs`、`width`、`height`
- `engagement` 当前实际字段为 `liked`、`favorited`、`likeCount`、`favoriteCount`、`commentCount`
- `/api/short-video/videos/{videoId}/comments` 返回 `items`、`nextCursor`、`hasMore`；评论项包含 `commentId`、`videoId`、`user`、`content`、`createdAt`

当前已验证的错误码/提示语包括：

- 未登录或 token 无效：HTTP `401`，错误码 `UNAUTHORIZED`
- 视频不存在：HTTP `404`，错误码 `RESOURCE_NOT_FOUND`，消息为“未找到视频”
- 评论内容为空：HTTP `400`，错误码 `VALIDATION_ERROR`，消息为“内容不能为空”

## 10. 建议的最小回归集合

- 后端：短视频信息流接口、点赞/收藏接口、评论接口集成测试
- 后端：鉴权失败、视频不存在、评论为空、互动幂等性等异常场景
- iOS：视频流分页、上划切换、互动状态回显、评论提交流程
- 配置：对象存储配置错误时的错误反馈与日志

## 11. 当前实现限制

- 评论内容服务端和客户端都按去除首尾空白后校验，最大长度 `500` 字
- 信息流单次 `limit` 当前限制为 `1 ~ 10`，iOS 默认请求 `3`
- 评论列表单次 `limit` 当前限制为 `1 ~ 50`，iOS 默认请求 `20`
- 当前只支持 iOS 已登录用户浏览，不包含 Android、匿名访问和视频上传能力
- 本地对象存储默认走 S3 兼容接口，`aipay.short-video.cdn-base-url` 为空时需要显式配置可访问地址

## 12. 本阶段不包含的内容

以下内容不属于当前快速开始范围：

- 创作者上传与审核后台
- 推荐算法与个性化召回
- 直播、关注、转发、分享链路
- Android 客户端
