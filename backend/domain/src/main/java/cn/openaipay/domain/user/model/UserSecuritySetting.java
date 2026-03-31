package cn.openaipay.domain.user.model;

import java.time.LocalDateTime;

/**
 * 用户安全Setting模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserSecuritySetting {

    /** 用户ID */
    private final Long userId;
    /** 生物识别启用开关 */
    private boolean biometricEnabled;
    /** 双因子模式 */
    private String twoFactorMode;
    /** 风险等级 */
    private String riskLevel;
    /** 设备锁启用开关 */
    private boolean deviceLockEnabled;
    /** 隐私模式启用开关 */
    private boolean privacyModeEnabled;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public UserSecuritySetting(
            Long userId,
            boolean biometricEnabled,
            String twoFactorMode,
            String riskLevel,
            boolean deviceLockEnabled,
            boolean privacyModeEnabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.userId = userId;
        this.biometricEnabled = biometricEnabled;
        this.twoFactorMode = twoFactorMode;
        this.riskLevel = riskLevel;
        this.deviceLockEnabled = deviceLockEnabled;
        this.privacyModeEnabled = privacyModeEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理OF信息。
     */
    public static UserSecuritySetting defaultOf(Long userId, LocalDateTime now) {
        return new UserSecuritySetting(userId, false, "NONE", "LOW", false, false, now, now);
    }

    /**
     * 更新业务数据。
     */
    public void update(Boolean biometricEnabled, String twoFactorMode, String riskLevel,
                       Boolean deviceLockEnabled, Boolean privacyModeEnabled) {
        if (biometricEnabled != null) {
            this.biometricEnabled = biometricEnabled;
        }
        if (twoFactorMode != null) {
            this.twoFactorMode = twoFactorMode;
        }
        if (riskLevel != null) {
            this.riskLevel = riskLevel;
        }
        if (deviceLockEnabled != null) {
            this.deviceLockEnabled = deviceLockEnabled;
        }
        if (privacyModeEnabled != null) {
            this.privacyModeEnabled = privacyModeEnabled;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isBiometricEnabled() {
        return biometricEnabled;
    }

    /**
     * 获取TWO信息。
     */
    public String getTwoFactorMode() {
        return twoFactorMode;
    }

    /**
     * 获取风控信息。
     */
    public String getRiskLevel() {
        return riskLevel;
    }

    /**
     * 判断是否设备信息。
     */
    public boolean isDeviceLockEnabled() {
        return deviceLockEnabled;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isPrivacyModeEnabled() {
        return privacyModeEnabled;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
