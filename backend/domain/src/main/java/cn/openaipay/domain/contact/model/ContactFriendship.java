package cn.openaipay.domain.contact.model;

import java.time.LocalDateTime;

/**
 * 好友关系模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ContactFriendship {

    /** 主键ID。 */
    private final Long id;
    /** 所属用户ID。 */
    private final Long ownerUserId;
    /** 好友用户ID。 */
    private final Long friendUserId;
    /** 备注。 */
    private String remark;
    /** 来源请求号。 */
    private final String sourceRequestNo;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public ContactFriendship(Long id,
                             Long ownerUserId,
                             Long friendUserId,
                             String remark,
                             String sourceRequestNo,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.ownerUserId = requirePositive(ownerUserId, "ownerUserId");
        this.friendUserId = requirePositive(friendUserId, "friendUserId");
        this.remark = normalizeOptional(remark);
        this.sourceRequestNo = normalizeOptional(sourceRequestNo);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static ContactFriendship create(Long ownerUserId,
                                           Long friendUserId,
                                           String sourceRequestNo,
                                           LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new ContactFriendship(
                null,
                ownerUserId,
                friendUserId,
                null,
                sourceRequestNo,
                createdAt,
                createdAt
        );
    }

    /**
     * 更新业务数据。
     */
    public void updateRemark(String remark, LocalDateTime now) {
        this.remark = normalizeOptional(remark);
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取所属方用户ID。
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * 获取用户ID。
     */
    public Long getFriendUserId() {
        return friendUserId;
    }

    /**
     * 获取业务数据。
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 获取请求NO信息。
     */
    public String getSourceRequestNo() {
        return sourceRequestNo;
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

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
