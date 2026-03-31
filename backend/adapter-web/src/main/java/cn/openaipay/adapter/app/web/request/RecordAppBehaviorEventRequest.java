package cn.openaipay.adapter.app.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 上报 App 行为埋点请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record RecordAppBehaviorEventRequest(
        /** 事件ID */
        @Size(max = 64, message = "长度不能超过64") String eventId,
        /** 会话ID */
        @Size(max = 64, message = "长度不能超过64") String sessionId,
        /** 应用编码 */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String appCode,
        /** 事件名称 */
        @NotBlank(message = "不能为空") @Size(max = 128, message = "长度不能超过128") String eventName,
        /** 事件类型 */
        @Size(max = 32, message = "长度不能超过32") String eventType,
        /** 事件编码 */
        @Size(max = 128, message = "长度不能超过128") String eventCode,
        /** 页面名称 */
        @Size(max = 128, message = "长度不能超过128") String pageName,
        /** 动作名称 */
        @Size(max = 128, message = "长度不能超过128") String actionName,
        /** 结果状态 */
        @Size(max = 64, message = "长度不能超过64") String resultStatus,
        /** 链路追踪ID */
        @Size(max = 64, message = "长度不能超过64") String traceId,
        /** 设备ID */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String deviceId,
        /** 客户端ID */
        @Size(max = 64, message = "长度不能超过64") String clientId,
        /** 用户ID */
        @Min(value = 1, message = "必须大于0") Long userId,
        /** 爱付UID */
        @Size(max = 64, message = "长度不能超过64") String aipayUid,
        /** 登录账号 */
        @Size(max = 64, message = "长度不能超过64") String loginId,
        /** 账户状态 */
        @Size(max = 32, message = "长度不能超过32") String accountStatus,
        /** 实名等级 */
        @Size(max = 32, message = "长度不能超过32") String kycLevel,
        /** 昵称 */
        @Size(max = 128, message = "长度不能超过128") String nickname,
        /** 手机号 */
        @Size(max = 32, message = "长度不能超过32") String mobile,
        /** IP地址 */
        @Size(max = 64, message = "长度不能超过64") String ipAddress,
        /** 位置信息 */
        @Size(max = 255, message = "长度不能超过255") String locationInfo,
        /** 租户编码 */
        @Size(max = 64, message = "长度不能超过64") String tenantCode,
        /** 网络类型 */
        @Size(max = 32, message = "长度不能超过32") String networkType,
        /** 应用版本号 */
        @Size(max = 64, message = "长度不能超过64") String appVersionNo,
        /** 应用构建号 */
        @Size(max = 32, message = "长度不能超过32") String appBuildNo,
        /** 设备品牌 */
        @Size(max = 64, message = "长度不能超过64") String deviceBrand,
        /** 设备型号 */
        @Size(max = 64, message = "长度不能超过64") String deviceModel,
        /** 设备名称 */
        @Size(max = 128, message = "长度不能超过128") String deviceName,
        /** 设备类型 */
        @Size(max = 32, message = "长度不能超过32") String deviceType,
        /** 系统名称 */
        @Size(max = 64, message = "长度不能超过64") String osName,
        /** 系统版本 */
        @Size(max = 64, message = "长度不能超过64") String osVersion,
        /** 区域 */
        @Size(max = 64, message = "长度不能超过64") String locale,
        /** 时区 */
        @Size(max = 64, message = "长度不能超过64") String timezone,
        /** 语言 */
        @Size(max = 64, message = "长度不能超过64") String language,
        /** 国家编码 */
        @Size(max = 16, message = "长度不能超过16") String countryCode,
        /** 运营商 */
        @Size(max = 64, message = "长度不能超过64") String carrierName,
        /** 屏幕宽度 */
        @Min(value = 0, message = "必须大于等于0") Integer screenWidth,
        /** 屏幕高度 */
        @Min(value = 0, message = "必须大于等于0") Integer screenHeight,
        /** 可视宽度 */
        @Min(value = 0, message = "必须大于等于0") Integer viewportWidth,
        /** 可视高度 */
        @Min(value = 0, message = "必须大于等于0") Integer viewportHeight,
        /** 业务耗时 */
        @Min(value = 0, message = "必须大于等于0") Long durationMs,
        /** 登录时长 */
        @Min(value = 0, message = "必须大于等于0") Long loginDurationMs,
        /** 事件时间戳(毫秒) */
        @Min(value = 0, message = "必须大于等于0") Long eventAtEpochMs,
        /** 扩展JSON */
        @Size(max = 20000, message = "长度不能超过20000") String payloadJson
) {
}
