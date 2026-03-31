package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;

/**
 * App 行为埋点 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorEventDTO(
        /** 主键ID */
        Long id,
        /** 事件ID */
        String eventId,
        /** 会话ID */
        String sessionId,
        /** 应用编码 */
        String appCode,
        /** 事件名称 */
        String eventName,
        /** 事件类型 */
        String eventType,
        /** 事件编码 */
        String eventCode,
        /** 页面名称 */
        String pageName,
        /** 动作名称 */
        String actionName,
        /** 结果状态 */
        String resultStatus,
        /** 链路追踪ID */
        String traceId,
        /** 设备ID */
        String deviceId,
        /** 客户端ID */
        String clientId,
        /** 用户ID */
        Long userId,
        /** 爱付UID */
        String aipayUid,
        /** 登录账号 */
        String loginId,
        /** 账户状态 */
        String accountStatus,
        /** 实名等级 */
        String kycLevel,
        /** 昵称 */
        String nickname,
        /** 手机号 */
        String mobile,
        /** IP地址 */
        String ipAddress,
        /** 位置信息 */
        String locationInfo,
        /** 租户编码 */
        String tenantCode,
        /** 网络类型 */
        String networkType,
        /** 应用版本号 */
        String appVersionNo,
        /** 应用构建号 */
        String appBuildNo,
        /** 设备品牌 */
        String deviceBrand,
        /** 设备型号 */
        String deviceModel,
        /** 设备名称 */
        String deviceName,
        /** 设备类型 */
        String deviceType,
        /** 系统名称 */
        String osName,
        /** 系统版本 */
        String osVersion,
        /** 区域 */
        String locale,
        /** 时区 */
        String timezone,
        /** 语言 */
        String language,
        /** 国家编码 */
        String countryCode,
        /** 运营商 */
        String carrierName,
        /** 屏幕宽度 */
        Integer screenWidth,
        /** 屏幕高度 */
        Integer screenHeight,
        /** 可视宽度 */
        Integer viewportWidth,
        /** 可视高度 */
        Integer viewportHeight,
        /** 业务耗时 */
        Long durationMs,
        /** 登录时长 */
        Long loginDurationMs,
        /** 事件发生时间 */
        LocalDateTime eventOccurredAt,
        /** 扩展JSON */
        String payloadJson,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
