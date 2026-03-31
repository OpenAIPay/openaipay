package cn.openaipay.domain.accounting.model;

import org.joda.money.Money;

import java.math.RoundingMode;

/**
 * 会计事件中的标准化资金腿。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingLeg {

    /** LEG单号 */
    private final Integer legNo;
    /** 域信息 */
    private final String accountDomain;
    /** 业务类型 */
    private final String accountType;
    /** 业务单号 */
    private final String accountNo;
    /** 所属类型 */
    private final String ownerType;
    /** 所属ID */
    private final Long ownerId;
    /** 交易金额 */
    private final Money amount;
    /** 方向 */
    private final AccountingAmountDirection direction;
    /** 业务角色信息 */
    private final String bizRole;
    /** 科目信息 */
    private final String subjectHint;
    /** 业务单号 */
    private final String referenceNo;
    /** 业务扩展信息 */
    private final String metadata;

    public AccountingLeg(Integer legNo,
                         String accountDomain,
                         String accountType,
                         String accountNo,
                         String ownerType,
                         Long ownerId,
                         Money amount,
                         AccountingAmountDirection direction,
                         String bizRole,
                         String subjectHint,
                         String referenceNo,
                         String metadata) {
        if (legNo == null || legNo <= 0) {
            throw new IllegalArgumentException("legNo must be greater than 0");
        }
        this.legNo = legNo;
        this.accountDomain = required(accountDomain, "accountDomain");
        this.accountType = required(accountType, "accountType");
        this.accountNo = optional(accountNo);
        this.ownerType = optional(ownerType);
        this.ownerId = ownerId;
        this.amount = normalizePositive(amount, "amount");
        this.direction = direction == null ? AccountingAmountDirection.OUT : direction;
        this.bizRole = optional(bizRole);
        this.subjectHint = optional(subjectHint);
        this.referenceNo = optional(referenceNo);
        this.metadata = optional(metadata);
    }

    /**
     * 处理OF信息。
     */
    public static AccountingLeg of(Integer legNo,
                                   String accountDomain,
                                   String accountType,
                                   String accountNo,
                                   String ownerType,
                                   Long ownerId,
                                   Money amount,
                                   AccountingAmountDirection direction,
                                   String bizRole,
                                   String subjectHint,
                                   String referenceNo) {
        return new AccountingLeg(
                legNo,
                accountDomain,
                accountType,
                accountNo,
                ownerType,
                ownerId,
                amount,
                direction,
                bizRole,
                subjectHint,
                referenceNo,
                null
        );
    }

    /**
     * 获取LEGNO信息。
     */
    public Integer getLegNo() {
        return legNo;
    }

    /**
     * 获取账户领域信息。
     */
    public String getAccountDomain() {
        return accountDomain;
    }

    /**
     * 获取账户类型信息。
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取所属方类型信息。
     */
    public String getOwnerType() {
        return ownerType;
    }

    /**
     * 获取所属方ID。
     */
    public Long getOwnerId() {
        return ownerId;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取业务数据。
     */
    public AccountingAmountDirection getDirection() {
        return direction;
    }

    /**
     * 获取业务角色信息。
     */
    public String getBizRole() {
        return bizRole;
    }

    /**
     * 获取科目信息。
     */
    public String getSubjectHint() {
        return subjectHint;
    }

    /**
     * 获取NO信息。
     */
    public String getReferenceNo() {
        return referenceNo;
    }

    /**
     * 获取业务数据。
     */
    public String getMetadata() {
        return metadata;
    }

    private static Money normalizePositive(Money amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        if (amount.compareTo(Money.zero(amount.getCurrencyUnit())) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
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
