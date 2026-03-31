package cn.openaipay.domain.user.model;

import java.time.LocalDateTime;

/**
 * 用户隐私Setting模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserPrivacySetting {

    /** 用户ID */
    private final Long userId;
    /** 手机号可搜索 */
    private boolean allowSearchByMobile;
    /** 用户号可搜索 */
    private boolean allowSearchByAipayUid;
    /** 隐藏实名名称 */
    private boolean hideRealName;
    /** 个性化推荐启用开关 */
    private boolean personalizedRecommendationEnabled;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public UserPrivacySetting(
            Long userId,
            boolean allowSearchByMobile,
            boolean allowSearchByAipayUid,
            boolean hideRealName,
            boolean personalizedRecommendationEnabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.userId = userId;
        this.allowSearchByMobile = allowSearchByMobile;
        this.allowSearchByAipayUid = allowSearchByAipayUid;
        this.hideRealName = hideRealName;
        this.personalizedRecommendationEnabled = personalizedRecommendationEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理OF信息。
     */
    public static UserPrivacySetting defaultOf(Long userId, LocalDateTime now) {
        return new UserPrivacySetting(userId, true, true, false, true, now, now);
    }

    /**
     * 更新业务数据。
     */
    public void update(Boolean allowSearchByMobile, Boolean allowSearchByAipayUid,
                       Boolean hideRealName, Boolean personalizedRecommendationEnabled) {
        if (allowSearchByMobile != null) {
            this.allowSearchByMobile = allowSearchByMobile;
        }
        if (allowSearchByAipayUid != null) {
            this.allowSearchByAipayUid = allowSearchByAipayUid;
        }
        if (hideRealName != null) {
            this.hideRealName = hideRealName;
        }
        if (personalizedRecommendationEnabled != null) {
            this.personalizedRecommendationEnabled = personalizedRecommendationEnabled;
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
     * 按手机号判断是否搜索信息。
     */
    public boolean isAllowSearchByMobile() {
        return allowSearchByMobile;
    }

    /**
     * 按UID判断是否搜索信息。
     */
    public boolean isAllowSearchByAipayUid() {
        return allowSearchByAipayUid;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isHideRealName() {
        return hideRealName;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isPersonalizedRecommendationEnabled() {
        return personalizedRecommendationEnabled;
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
