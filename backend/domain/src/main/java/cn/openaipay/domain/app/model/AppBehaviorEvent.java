package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * App 行为埋点模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public class AppBehaviorEvent {

    /** 埋点自增主键。 */
    private final Long id;
    /** 事件唯一ID。 */
    private final String eventId;
    /** 会话ID。 */
    private final String sessionId;
    /** 应用编码。 */
    private final String appCode;
    /** 事件名称。 */
    private final String eventName;
    /** 事件类型。 */
    private final String eventType;
    /** 事件编码。 */
    private final String eventCode;
    /** 页面名称。 */
    private final String pageName;
    /** 动作名称。 */
    private final String actionName;
    /** 结果状态。 */
    private final String resultStatus;
    /** 链路追踪ID。 */
    private final String traceId;
    /** 设备ID。 */
    private final String deviceId;
    /** 客户端ID。 */
    private final String clientId;
    /** 用户ID。 */
    private final Long userId;
    /** 爱付UID。 */
    private final String aipayUid;
    /** 登录账号。 */
    private final String loginId;
    /** 账户状态。 */
    private final String accountStatus;
    /** 实名等级。 */
    private final String kycLevel;
    /** 昵称。 */
    private final String nickname;
    /** 手机号。 */
    private final String mobile;
    /** IP 地址。 */
    private final String ipAddress;
    /** 位置信息。 */
    private final String locationInfo;
    /** 租户编码。 */
    private final String tenantCode;
    /** 网络类型。 */
    private final String networkType;
    /** 应用版本号。 */
    private final String appVersionNo;
    /** 应用构建号。 */
    private final String appBuildNo;
    /** 设备品牌。 */
    private final String deviceBrand;
    /** 设备型号。 */
    private final String deviceModel;
    /** 设备名称。 */
    private final String deviceName;
    /** 设备类型。 */
    private final String deviceType;
    /** 操作系统名称。 */
    private final String osName;
    /** 操作系统版本。 */
    private final String osVersion;
    /** 本地化区域。 */
    private final String locale;
    /** 时区。 */
    private final String timezone;
    /** 语言。 */
    private final String language;
    /** 国家编码。 */
    private final String countryCode;
    /** 运营商。 */
    private final String carrierName;
    /** 屏幕宽度。 */
    private final Integer screenWidth;
    /** 屏幕高度。 */
    private final Integer screenHeight;
    /** 可视区域宽度。 */
    private final Integer viewportWidth;
    /** 可视区域高度。 */
    private final Integer viewportHeight;
    /** 业务耗时（毫秒）。 */
    private final Long durationMs;
    /** 登录耗时（毫秒）。 */
    private final Long loginDurationMs;
    /** 事件发生时间。 */
    private final LocalDateTime eventOccurredAt;
    /** 扩展上下文 JSON。 */
    private final String payloadJson;
    /** 记录创建时间。 */
    private final LocalDateTime createdAt;

    public AppBehaviorEvent(Long id,
                            String eventId,
                            String sessionId,
                            String appCode,
                            String eventName,
                            String eventType,
                            String eventCode,
                            String pageName,
                            String actionName,
                            String resultStatus,
                            String traceId,
                            String deviceId,
                            String clientId,
                            Long userId,
                            String aipayUid,
                            String loginId,
                            String accountStatus,
                            String kycLevel,
                            String nickname,
                            String mobile,
                            String ipAddress,
                            String locationInfo,
                            String tenantCode,
                            String networkType,
                            String appVersionNo,
                            String appBuildNo,
                            String deviceBrand,
                            String deviceModel,
                            String deviceName,
                            String deviceType,
                            String osName,
                            String osVersion,
                            String locale,
                            String timezone,
                            String language,
                            String countryCode,
                            String carrierName,
                            Integer screenWidth,
                            Integer screenHeight,
                            Integer viewportWidth,
                            Integer viewportHeight,
                            Long durationMs,
                            Long loginDurationMs,
                            LocalDateTime eventOccurredAt,
                            String payloadJson,
                            LocalDateTime createdAt) {
        this.id = id;
        this.eventId = AppDomainSupport.normalizeRequired(eventId, "eventId");
        this.sessionId = AppDomainSupport.normalizeOptional(sessionId);
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.eventName = AppDomainSupport.normalizeRequired(eventName, "eventName");
        this.eventType = AppDomainSupport.normalizeOptional(eventType);
        this.eventCode = AppDomainSupport.normalizeOptional(eventCode);
        this.pageName = AppDomainSupport.normalizeOptional(pageName);
        this.actionName = AppDomainSupport.normalizeOptional(actionName);
        this.resultStatus = AppDomainSupport.normalizeOptional(resultStatus);
        this.traceId = AppDomainSupport.normalizeOptional(traceId);
        this.deviceId = AppDomainSupport.normalizeRequired(deviceId, "deviceId");
        this.clientId = AppDomainSupport.normalizeOptional(clientId);
        this.userId = normalizeUserId(userId);
        this.aipayUid = AppDomainSupport.normalizeOptional(aipayUid);
        this.loginId = AppDomainSupport.normalizeOptional(loginId);
        this.accountStatus = AppDomainSupport.normalizeOptional(accountStatus);
        this.kycLevel = AppDomainSupport.normalizeOptional(kycLevel);
        this.nickname = AppDomainSupport.normalizeOptional(nickname);
        this.mobile = AppDomainSupport.normalizeOptional(mobile);
        this.ipAddress = AppDomainSupport.normalizeOptional(ipAddress);
        this.locationInfo = AppDomainSupport.normalizeOptional(locationInfo);
        this.tenantCode = AppDomainSupport.normalizeOptional(tenantCode);
        this.networkType = AppDomainSupport.normalizeOptional(networkType);
        this.appVersionNo = AppDomainSupport.normalizeOptional(appVersionNo);
        this.appBuildNo = AppDomainSupport.normalizeOptional(appBuildNo);
        this.deviceBrand = AppDomainSupport.defaultText(deviceBrand, "APPLE");
        this.deviceModel = AppDomainSupport.normalizeOptional(deviceModel);
        this.deviceName = AppDomainSupport.normalizeOptional(deviceName);
        this.deviceType = AppDomainSupport.normalizeOptional(deviceType);
        this.osName = AppDomainSupport.normalizeOptional(osName);
        this.osVersion = AppDomainSupport.normalizeOptional(osVersion);
        this.locale = AppDomainSupport.normalizeOptional(locale);
        this.timezone = AppDomainSupport.normalizeOptional(timezone);
        this.language = AppDomainSupport.normalizeOptional(language);
        this.countryCode = AppDomainSupport.normalizeOptional(countryCode);
        this.carrierName = AppDomainSupport.normalizeOptional(carrierName);
        this.screenWidth = normalizeNonNegative(screenWidth, "screenWidth");
        this.screenHeight = normalizeNonNegative(screenHeight, "screenHeight");
        this.viewportWidth = normalizeNonNegative(viewportWidth, "viewportWidth");
        this.viewportHeight = normalizeNonNegative(viewportHeight, "viewportHeight");
        this.durationMs = AppDomainSupport.normalizeNonNegative(durationMs, "durationMs");
        this.loginDurationMs = AppDomainSupport.normalizeNonNegative(loginDurationMs, "loginDurationMs");
        this.eventOccurredAt = AppDomainSupport.defaultNow(eventOccurredAt);
        this.payloadJson = AppDomainSupport.normalizeOptional(payloadJson);
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
    }

    public static AppBehaviorEvent record(String eventId,
                                          String sessionId,
                                          String appCode,
                                          String eventName,
                                          String eventType,
                                          String eventCode,
                                          String pageName,
                                          String actionName,
                                          String resultStatus,
                                          String traceId,
                                          String deviceId,
                                          String clientId,
                                          Long userId,
                                          String aipayUid,
                                          String loginId,
                                          String accountStatus,
                                          String kycLevel,
                                          String nickname,
                                          String mobile,
                                          String ipAddress,
                                          String locationInfo,
                                          String tenantCode,
                                          String networkType,
                                          String appVersionNo,
                                          String appBuildNo,
                                          String deviceBrand,
                                          String deviceModel,
                                          String deviceName,
                                          String deviceType,
                                          String osName,
                                          String osVersion,
                                          String locale,
                                          String timezone,
                                          String language,
                                          String countryCode,
                                          String carrierName,
                                          Integer screenWidth,
                                          Integer screenHeight,
                                          Integer viewportWidth,
                                          Integer viewportHeight,
                                          Long durationMs,
                                          Long loginDurationMs,
                                          LocalDateTime eventOccurredAt,
                                          String payloadJson) {
        return new AppBehaviorEvent(
                null,
                eventId,
                sessionId,
                appCode,
                eventName,
                eventType,
                eventCode,
                pageName,
                actionName,
                resultStatus,
                traceId,
                deviceId,
                clientId,
                userId,
                aipayUid,
                loginId,
                accountStatus,
                kycLevel,
                nickname,
                mobile,
                ipAddress,
                locationInfo,
                tenantCode,
                networkType,
                appVersionNo,
                appBuildNo,
                deviceBrand,
                deviceModel,
                deviceName,
                deviceType,
                osName,
                osVersion,
                locale,
                timezone,
                language,
                countryCode,
                carrierName,
                screenWidth,
                screenHeight,
                viewportWidth,
                viewportHeight,
                durationMs,
                loginDurationMs,
                eventOccurredAt,
                payloadJson,
                eventOccurredAt
        );
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventCode() {
        return eventCode;
    }

    public String getPageName() {
        return pageName;
    }

    public String getActionName() {
        return actionName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAipayUid() {
        return aipayUid;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public String getKycLevel() {
        return kycLevel;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMobile() {
        return mobile;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getAppVersionNo() {
        return appVersionNo;
    }

    public String getAppBuildNo() {
        return appBuildNo;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public Integer getViewportWidth() {
        return viewportWidth;
    }

    public Integer getViewportHeight() {
        return viewportHeight;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public Long getLoginDurationMs() {
        return loginDurationMs;
    }

    public LocalDateTime getEventOccurredAt() {
        return eventOccurredAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private static Long normalizeUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        return userId;
    }

    private static Integer normalizeNonNegative(Integer value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }
}
