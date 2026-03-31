package cn.openaipay.domain.user.model;

/**
 * 用户聚合模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserAggregate {

    /** 账户 */
    private final UserAccount account;
    /** 资料 */
    private final UserProfile profile;
    /** 安全设置 */
    private final UserSecuritySetting securitySetting;
    /** 隐私设置 */
    private final UserPrivacySetting privacySetting;

    public UserAggregate(UserAccount account, UserProfile profile, UserSecuritySetting securitySetting,
                         UserPrivacySetting privacySetting) {
        this.account = account;
        this.profile = profile;
        this.securitySetting = securitySetting;
        this.privacySetting = privacySetting;
    }

    /**
     * 获取账户信息。
     */
    public UserAccount getAccount() {
        return account;
    }

    /**
     * 获取资料信息。
     */
    public UserProfile getProfile() {
        return profile;
    }

    /**
     * 获取安全设置信息。
     */
    public UserSecuritySetting getSecuritySetting() {
        return securitySetting;
    }

    /**
     * 获取设置信息。
     */
    public UserPrivacySetting getPrivacySetting() {
        return privacySetting;
    }
}
