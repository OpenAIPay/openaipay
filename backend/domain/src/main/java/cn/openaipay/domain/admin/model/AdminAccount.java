package cn.openaipay.domain.admin.model;

import java.time.LocalDateTime;

/**
 * 后台管理账户模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class AdminAccount {

    /** 管理标识 */
    private final Long adminId;
    /** 用户名 */
    private final String username;
    /** 显示名称 */
    private final String displayName;
    /** 密码哈希 */
    private final String passwordSha256;
    /** 账户状态 */
    private final String accountStatus;
    /** 最近登录时间 */
    private LocalDateTime lastLoginAt;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public AdminAccount(Long adminId,
                        String username,
                        String displayName,
                        String passwordSha256,
                        String accountStatus,
                        LocalDateTime lastLoginAt,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.adminId = adminId;
        this.username = username;
        this.displayName = displayName;
        this.passwordSha256 = passwordSha256;
        this.accountStatus = accountStatus;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(accountStatus);
    }

    /**
     * 记录登录信息。
     */
    public void recordLogin(LocalDateTime now) {
        this.lastLoginAt = now;
        this.updatedAt = now;
    }

    /**
     * 获取后台ID。
     */
    public Long getAdminId() {
        return adminId;
    }

    /**
     * 获取用户名信息。
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取业务数据。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取SHA256信息。
     */
    public String getPasswordSha256() {
        return passwordSha256;
    }

    /**
     * 获取账户状态。
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * 获取登录AT信息。
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
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
