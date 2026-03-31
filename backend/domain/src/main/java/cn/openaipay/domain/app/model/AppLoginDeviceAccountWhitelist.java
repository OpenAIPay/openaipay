package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * 登录设备白名单账号模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
public class AppLoginDeviceAccountWhitelist {

    /** 主键ID。 */
    private final Long id;
    /** 应用编码。 */
    private final String appCode;
    /** 设备ID。 */
    private final String deviceId;
    /** 登录账号。 */
    private final String loginId;
    /** 昵称。 */
    private final String nickname;
    /** 是否启用。 */
    private final boolean enabled;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public AppLoginDeviceAccountWhitelist(Long id,
                                          String appCode,
                                          String deviceId,
                                          String loginId,
                                          String nickname,
                                          boolean enabled,
                                          LocalDateTime createdAt,
                                          LocalDateTime updatedAt) {
        this.id = id;
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.deviceId = AppDomainSupport.normalizeRequired(deviceId, "deviceId");
        this.loginId = AppDomainSupport.normalizeRequired(loginId, "loginId");
        this.nickname = AppDomainSupport.normalizeOptional(nickname);
        this.enabled = enabled;
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 获取主键ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取设备ID。
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取登录账号。
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * 获取昵称。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 判断是否启用。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取创建时间。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取更新时间。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
