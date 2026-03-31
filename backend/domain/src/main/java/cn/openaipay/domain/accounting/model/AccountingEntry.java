package cn.openaipay.domain.accounting.model;

import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 会计分录行。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingEntry {

    /** 数据库主键ID */
    private final Long id;
    /** 业务单号 */
    private final String voucherNo;
    /** 业务单号 */
    private final Integer lineNo;
    /** 科目编码 */
    private final String subjectCode;
    /** DC标记 */
    private final DebitCreditFlag dcFlag;
    /** 交易金额 */
    private final Money amount;
    /** 所属类型 */
    private final String ownerType;
    /** 所属ID */
    private final Long ownerId;
    /** 域信息 */
    private final String accountDomain;
    /** 业务类型 */
    private final String accountType;
    /** 业务单号 */
    private final String accountNo;
    /** 业务角色信息 */
    private final String bizRole;
    /** 业务单号 */
    private final String bizOrderNo;
    /** 交易订单单号 */
    private final String tradeOrderNo;
    /** 支付订单单号 */
    private final String payOrderNo;
    /** 来源业务类型 */
    private final String sourceBizType;
    /** 来源业务单号 */
    private final String sourceBizNo;
    /** 业务单号 */
    private final String referenceNo;
    /** 分录备注 */
    private final String entryMemo;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public AccountingEntry(Long id,
                           String voucherNo,
                           Integer lineNo,
                           String subjectCode,
                           DebitCreditFlag dcFlag,
                           Money amount,
                           String ownerType,
                           Long ownerId,
                           String accountDomain,
                           String accountType,
                           String accountNo,
                           String bizRole,
                           String bizOrderNo,
                           String tradeOrderNo,
                           String payOrderNo,
                           String sourceBizType,
                           String sourceBizNo,
                           String referenceNo,
                           String entryMemo,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.voucherNo = required(voucherNo, "voucherNo");
        if (lineNo == null || lineNo <= 0) {
            throw new IllegalArgumentException("lineNo must be greater than 0");
        }
        this.lineNo = lineNo;
        this.subjectCode = required(subjectCode, "subjectCode");
        this.dcFlag = dcFlag == null ? DebitCreditFlag.DEBIT : dcFlag;
        this.amount = normalizePositive(amount, "amount");
        this.ownerType = optional(ownerType);
        this.ownerId = ownerId;
        this.accountDomain = optional(accountDomain);
        this.accountType = optional(accountType);
        this.accountNo = optional(accountNo);
        this.bizRole = optional(bizRole);
        this.bizOrderNo = optional(bizOrderNo);
        this.tradeOrderNo = optional(tradeOrderNo);
        this.payOrderNo = optional(payOrderNo);
        this.sourceBizType = required(sourceBizType, "sourceBizType");
        this.sourceBizNo = required(sourceBizNo, "sourceBizNo");
        this.referenceNo = optional(referenceNo);
        this.entryMemo = optional(entryMemo);
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
     * 获取凭证NO信息。
     */
    public String getVoucherNo() {
        return voucherNo;
    }

    /**
     * 获取NO信息。
     */
    public Integer getLineNo() {
        return lineNo;
    }

    /**
     * 获取科目编码。
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * 获取DC信息。
     */
    public DebitCreditFlag getDcFlag() {
        return dcFlag;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
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
     * 获取业务角色信息。
     */
    public String getBizRole() {
        return bizRole;
    }

    /**
     * 获取业务订单NO信息。
     */
    public String getBizOrderNo() {
        return bizOrderNo;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取业务信息。
     */
    public String getSourceBizType() {
        return sourceBizType;
    }

    /**
     * 获取业务NO信息。
     */
    public String getSourceBizNo() {
        return sourceBizNo;
    }

    /**
     * 获取NO信息。
     */
    public String getReferenceNo() {
        return referenceNo;
    }

    /**
     * 获取分录信息。
     */
    public String getEntryMemo() {
        return entryMemo;
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
     * 刷新业务数据。
     */
    public void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
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
