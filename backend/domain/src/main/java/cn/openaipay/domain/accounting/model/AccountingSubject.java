package cn.openaipay.domain.accounting.model;

import java.time.LocalDateTime;

/**
 * 会计科目。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingSubject {

    /** 数据库主键ID */
    private final Long id;
    /** 科目编码 */
    private final String subjectCode;
    /** 科目名称 */
    private String subjectName;
    /** 科目类型 */
    private final SubjectType subjectType;
    /** 余额方向 */
    private final DebitCreditFlag balanceDirection;
    /** 科目编码 */
    private final String parentSubjectCode;
    /** 业务单号 */
    private final Integer levelNo;
    /** 启用标记 */
    private boolean enabled;
    /** 业务备注 */
    private String remark;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public AccountingSubject(Long id,
                             String subjectCode,
                             String subjectName,
                             SubjectType subjectType,
                             DebitCreditFlag balanceDirection,
                             String parentSubjectCode,
                             Integer levelNo,
                             boolean enabled,
                             String remark,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.subjectCode = required(subjectCode, "subjectCode");
        this.subjectName = required(subjectName, "subjectName");
        this.subjectType = subjectType == null ? SubjectType.ASSET : subjectType;
        this.balanceDirection = balanceDirection == null ? DebitCreditFlag.DEBIT : balanceDirection;
        this.parentSubjectCode = optional(parentSubjectCode);
        this.levelNo = levelNo == null || levelNo <= 0 ? 1 : levelNo;
        this.enabled = enabled;
        this.remark = optional(remark);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取科目编码。
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * 获取科目信息。
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * 获取科目类型信息。
     */
    public SubjectType getSubjectType() {
        return subjectType;
    }

    /**
     * 获取业务数据。
     */
    public DebitCreditFlag getBalanceDirection() {
        return balanceDirection;
    }

    /**
     * 获取科目编码。
     */
    public String getParentSubjectCode() {
        return parentSubjectCode;
    }

    /**
     * 获取NO信息。
     */
    public Integer getLevelNo() {
        return levelNo;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取业务数据。
     */
    public String getRemark() {
        return remark;
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

    /**
     * 更新业务数据。
     */
    public void update(String subjectName, boolean enabled, String remark, LocalDateTime now) {
        this.subjectName = required(subjectName, "subjectName");
        this.enabled = enabled;
        this.remark = optional(remark);
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
