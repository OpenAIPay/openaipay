package cn.openaipay.domain.user.model;

import java.time.LocalDateTime;

/**
 * 用户最近联系人模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserRecentContact {

    /** 联系人所有者用户ID */
    private final Long ownerUserId;
    /** 最近互动联系人用户ID */
    private final Long contactUserId;
    /** 联系人平台用户号 */
    private final String contactAipayUid;
    /** 联系人昵称 */
    private final String contactNickname;
    /** 联系人展示名称 */
    private final String contactDisplayName;
    /** 联系人脱敏真实姓名 */
    private final String contactMaskedRealName;
    /** 联系人头像地址 */
    private final String contactAvatarUrl;
    /** 联系人展示手机号（脱敏） */
    private final String contactMobileMasked;
    /** 最近互动场景编码 */
    private final String interactionSceneCode;
    /** 最近互动备注 */
    private final String interactionRemark;
    /** 历史互动次数 */
    private final long interactionCount;
    /** 最近互动时间 */
    private final LocalDateTime lastInteractionAt;

    public UserRecentContact(Long ownerUserId,
                             Long contactUserId,
                             String contactAipayUid,
                             String contactNickname,
                             String contactDisplayName,
                             String contactMaskedRealName,
                             String contactAvatarUrl,
                             String contactMobileMasked,
                             String interactionSceneCode,
                             String interactionRemark,
                             long interactionCount,
                             LocalDateTime lastInteractionAt) {
        this.ownerUserId = ownerUserId;
        this.contactUserId = contactUserId;
        this.contactAipayUid = contactAipayUid;
        this.contactNickname = contactNickname;
        this.contactDisplayName = contactDisplayName;
        this.contactMaskedRealName = contactMaskedRealName;
        this.contactAvatarUrl = contactAvatarUrl;
        this.contactMobileMasked = contactMobileMasked;
        this.interactionSceneCode = interactionSceneCode;
        this.interactionRemark = interactionRemark;
        this.interactionCount = interactionCount;
        this.lastInteractionAt = lastInteractionAt;
    }

    /**
     * 获取所属方用户ID。
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * 获取联系人用户ID。
     */
    public Long getContactUserId() {
        return contactUserId;
    }

    /**
     * 获取联系人UID。
     */
    public String getContactAipayUid() {
        return contactAipayUid;
    }

    /**
     * 获取联系人信息。
     */
    public String getContactNickname() {
        return contactNickname;
    }

    /**
     * 获取联系人信息。
     */
    public String getContactDisplayName() {
        return contactDisplayName;
    }

    /**
     * 获取联系人信息。
     */
    public String getContactMaskedRealName() {
        return contactMaskedRealName;
    }

    /**
     * 获取联系人URL信息。
     */
    public String getContactAvatarUrl() {
        return contactAvatarUrl;
    }

    /**
     * 获取联系人手机号信息。
     */
    public String getContactMobileMasked() {
        return contactMobileMasked;
    }

    /**
     * 获取场景编码。
     */
    public String getInteractionSceneCode() {
        return interactionSceneCode;
    }

    /**
     * 获取业务数据。
     */
    public String getInteractionRemark() {
        return interactionRemark;
    }

    /**
     * 获取数量信息。
     */
    public long getInteractionCount() {
        return interactionCount;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getLastInteractionAt() {
        return lastInteractionAt;
    }
}
