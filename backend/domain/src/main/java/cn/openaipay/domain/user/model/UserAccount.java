package cn.openaipay.domain.user.model;

import java.time.LocalDateTime;

/**
 * 用户账户模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserAccount {

    /** 用户ID */
    private final Long userId;
    /** 平台用户号 */
    private String aipayUid;
    /** 登录标识 */
    private String loginId;
    /** 账户状态 */
    private UserStatus accountStatus;
    /** KYC等级 */
    private KycLevel kycLevel;
    /** 账号来源 */
    private UserAccountSource accountSource;
    /** 登录密码已设置 */
    private boolean loginPasswordSet;
    /** 支付密码已设置 */
    private boolean payPasswordSet;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public UserAccount(
            Long userId,
            String aipayUid,
            String loginId,
            UserStatus accountStatus,
            KycLevel kycLevel,
            UserAccountSource accountSource,
            boolean loginPasswordSet,
            boolean payPasswordSet,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.userId = userId;
        this.aipayUid = aipayUid;
        this.loginId = loginId;
        this.accountStatus = accountStatus;
        this.kycLevel = kycLevel;
        this.accountSource = accountSource == null ? UserAccountSource.REGISTER : accountSource;
        this.loginPasswordSet = loginPasswordSet;
        this.payPasswordSet = payPasswordSet;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理NEW用户信息。
     */
    public static UserAccount newUser(Long userId,
                                      String aipayUid,
                                      String loginId,
                                      UserAccountSource accountSource,
                                      LocalDateTime now) {
        return new UserAccount(userId, aipayUid, loginId, UserStatus.ACTIVE, KycLevel.L0, accountSource, false, false, now, now);
    }

    /**
     * 更新安全信息。
     */
    public void updateSecurityFlags(Boolean loginPasswordSet, Boolean payPasswordSet) {
        if (loginPasswordSet != null) {
            this.loginPasswordSet = loginPasswordSet;
        }
        if (payPasswordSet != null) {
            this.payPasswordSet = payPasswordSet;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新KYC信息。
     */
    public void updateKycLevel(KycLevel kycLevel) {
        this.kycLevel = kycLevel;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取UID。
     */
    public String getAipayUid() {
        return aipayUid;
    }

    /**
     * 获取登录ID。
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * 获取账户状态。
     */
    public UserStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * 获取KYC信息。
     */
    public KycLevel getKycLevel() {
        return kycLevel;
    }

    /**
     * 获取账号来源。
     */
    public UserAccountSource getAccountSource() {
        return accountSource;
    }

    /**
     * 判断是否登录SET信息。
     */
    public boolean isLoginPasswordSet() {
        return loginPasswordSet;
    }

    /**
     * 判断是否支付SET信息。
     */
    public boolean isPayPasswordSet() {
        return payPasswordSet;
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
