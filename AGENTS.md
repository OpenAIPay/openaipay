# openaipay 开发指南

根据所有功能计划自动生成。最后更新： 2026-03-31

## 当前使用技术

- Swift 5 + SwiftUI（iOS 客户端）、Java 21（Spring Boot 后端） + SwiftUI、AVKit/AVFoundation、现有 `APIClient`/`AppState`/`AuthStore`；Spring Boot 3.4、MyBatis-Plus 3.5、Flyway、现有认证与媒资模块 (001-short-video-feed)
- MySQL 8 保存视频元数据、互动状态和评论；S3 兼容对象存储保存视频与封面；CDN 对外分发播放资源 (001-short-video-feed)

## 项目结构

```text
backend/
ios-app/
app-bff/
local/
specs/
```

## 常用命令

`mvn -f backend/pom.xml test`
`mvn -f backend/adapter-web/pom.xml spring-boot:run`
`xcodebuild -project ios-app/OpenAiPay.xcodeproj -scheme OpenAiPay -configuration Debug build`
`open ios-app/OpenAiPay.xcodeproj`

## 代码风格

Swift：遵循 SwiftUI 的视图、状态与功能目录约定。
Java：遵循现有 Spring Boot 分层、MyBatis-Plus 持久化和 Flyway 迁移约定。

## 最近变更

- 001-short-video-feed: 新增 Swift 5 + SwiftUI（iOS 客户端）、Java 21（Spring Boot 后端） + SwiftUI、AVKit/AVFoundation、现有 `APIClient`/`AppState`/`AuthStore`；Spring Boot 3.4、MyBatis-Plus 3.5、Flyway、现有认证与媒资模块

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
