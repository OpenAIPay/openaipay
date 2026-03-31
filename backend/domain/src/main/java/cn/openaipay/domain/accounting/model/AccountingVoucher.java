package cn.openaipay.domain.accounting.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 会计凭证。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingVoucher {

    /** 数据库主键ID */
    private final Long id;
    /** 业务单号 */
    private final String voucherNo;
    /** 业务ID */
    private final String bookId;
    /** 业务类型 */
    private final VoucherType voucherType;
    /** 事件ID */
    private final String eventId;
    /** 来源业务类型 */
    private final String sourceBizType;
    /** 来源业务单号 */
    private final String sourceBizNo;
    /** 业务单号 */
    private final String bizOrderNo;
    /** 交易订单单号 */
    private final String tradeOrderNo;
    /** 支付订单单号 */
    private final String payOrderNo;
    /** 业务场景编码 */
    private final String businessSceneCode;
    /** 业务域编码 */
    private final String businessDomainCode;
    /** 当前状态编码 */
    private AccountingVoucherStatus status;
    /** 总金额 */
    private final Money totalDebitAmount;
    /** 总信用金额 */
    private final Money totalCreditAmount;
    /** 分录列表 */
    private final List<AccountingEntry> entries;
    /** 业务时间 */
    private final LocalDateTime occurredAt;
    /** 业务日期 */
    private final LocalDate postingDate;
    /** 业务单号 */
    private final String reversedVoucherNo;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public AccountingVoucher(Long id,
                             String voucherNo,
                             String bookId,
                             VoucherType voucherType,
                             String eventId,
                             String sourceBizType,
                             String sourceBizNo,
                             String bizOrderNo,
                             String tradeOrderNo,
                             String payOrderNo,
                             String businessSceneCode,
                             String businessDomainCode,
                             AccountingVoucherStatus status,
                             CurrencyUnit currencyUnit,
                             Money totalDebitAmount,
                             Money totalCreditAmount,
                             List<AccountingEntry> entries,
                             LocalDateTime occurredAt,
                             LocalDate postingDate,
                             String reversedVoucherNo,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.voucherNo = required(voucherNo, "voucherNo");
        this.bookId = required(bookId, "bookId");
        this.voucherType = voucherType == null ? VoucherType.NORMAL : voucherType;
        this.eventId = required(eventId, "eventId");
        this.sourceBizType = required(sourceBizType, "sourceBizType");
        this.sourceBizNo = required(sourceBizNo, "sourceBizNo");
        this.bizOrderNo = optional(bizOrderNo);
        this.tradeOrderNo = optional(tradeOrderNo);
        this.payOrderNo = optional(payOrderNo);
        this.businessSceneCode = optional(businessSceneCode);
        this.businessDomainCode = optional(businessDomainCode);
        this.status = status == null ? AccountingVoucherStatus.CREATED : status;
        this.entries = Collections.unmodifiableList(normalizeEntries(entries));
        CurrencyUnit resolvedCurrency = currencyUnit == null
                ? resolveCurrency(this.entries, totalDebitAmount, totalCreditAmount)
                : currencyUnit;
        this.totalDebitAmount = normalizeNonNegative(totalDebitAmount, "totalDebitAmount", resolvedCurrency);
        this.totalCreditAmount = normalizeNonNegative(totalCreditAmount, "totalCreditAmount", resolvedCurrency);
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.postingDate = postingDate == null ? this.occurredAt.toLocalDate() : postingDate;
        this.reversedVoucherNo = optional(reversedVoucherNo);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;

        if (this.totalDebitAmount.compareTo(this.totalCreditAmount) != 0) {
            throw new IllegalArgumentException("voucher debit and credit must be balanced");
        }
    }

    /**
     * 创建业务数据。
     */
    public static AccountingVoucher create(String voucherNo,
                                           String bookId,
                                           VoucherType voucherType,
                                           String eventId,
                                           String sourceBizType,
                                           String sourceBizNo,
                                           String bizOrderNo,
                                           String tradeOrderNo,
                                           String payOrderNo,
                                           String businessSceneCode,
                                           String businessDomainCode,
                                           CurrencyUnit currencyUnit,
                                           List<AccountingEntry> entries,
                                           LocalDateTime occurredAt) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("voucher entries must not be empty");
        }
        Money totalDebit = sum(entries, DebitCreditFlag.DEBIT, currencyUnit);
        Money totalCredit = sum(entries, DebitCreditFlag.CREDIT, currencyUnit);
        return new AccountingVoucher(
                null,
                voucherNo,
                bookId,
                voucherType,
                eventId,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                businessSceneCode,
                businessDomainCode,
                AccountingVoucherStatus.CREATED,
                currencyUnit,
                totalDebit,
                totalCredit,
                entries,
                occurredAt,
                occurredAt == null ? null : occurredAt.toLocalDate(),
                null,
                occurredAt,
                occurredAt
        );
    }

    /**
     * 标记业务数据。
     */
    public void markPosted(LocalDateTime now) {
        this.status = AccountingVoucherStatus.POSTED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markReversed(LocalDateTime now) {
        this.status = AccountingVoucherStatus.REVERSED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() { return id; }
    /**
     * 获取凭证NO信息。
     */
    public String getVoucherNo() { return voucherNo; }
    /**
     * 获取ID。
     */
    public String getBookId() { return bookId; }
    /**
     * 获取凭证类型信息。
     */
    public VoucherType getVoucherType() { return voucherType; }
    /**
     * 获取事件ID。
     */
    public String getEventId() { return eventId; }
    /**
     * 获取业务信息。
     */
    public String getSourceBizType() { return sourceBizType; }
    /**
     * 获取业务NO信息。
     */
    public String getSourceBizNo() { return sourceBizNo; }
    /**
     * 获取业务NO信息。
     */
    public String getBizOrderNo() { return bizOrderNo; }
    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() { return tradeOrderNo; }
    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() { return payOrderNo; }
    /**
     * 获取场景编码。
     */
    public String getBusinessSceneCode() { return businessSceneCode; }
    /**
     * 获取领域编码。
     */
    public String getBusinessDomainCode() { return businessDomainCode; }
    /**
     * 获取状态。
     */
    public AccountingVoucherStatus getStatus() { return status; }
    /**
     * 获取单元信息。
     */
    public CurrencyUnit getCurrencyUnit() { return totalDebitAmount.getCurrencyUnit(); }
    /**
     * 获取金额。
     */
    public Money getTotalDebitAmount() { return totalDebitAmount; }
    /**
     * 获取信用金额。
     */
    public Money getTotalCreditAmount() { return totalCreditAmount; }
    /**
     * 获取业务数据。
     */
    public List<AccountingEntry> getEntries() { return entries; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getOccurredAt() { return occurredAt; }
    /**
     * 获取日期信息。
     */
    public LocalDate getPostingDate() { return postingDate; }
    /**
     * 获取凭证NO信息。
     */
    public String getReversedVoucherNo() { return reversedVoucherNo; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    private static CurrencyUnit resolveCurrency(List<AccountingEntry> entries, Money totalDebitAmount, Money totalCreditAmount) {
        if (totalDebitAmount != null) {
            return totalDebitAmount.getCurrencyUnit();
        }
        if (totalCreditAmount != null) {
            return totalCreditAmount.getCurrencyUnit();
        }
        if (entries != null && !entries.isEmpty()) {
            return entries.get(0).getAmount().getCurrencyUnit();
        }
        return CurrencyUnit.of("CNY");
    }

    private static List<AccountingEntry> normalizeEntries(List<AccountingEntry> entries) {
        if (entries == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entries);
    }

    private static Money sum(List<AccountingEntry> entries, DebitCreditFlag target, CurrencyUnit currencyUnit) {
        CurrencyUnit resolved = currencyUnit;
        if (resolved == null) {
            resolved = resolveCurrency(entries, null, null);
        }
        Money total = Money.zero(resolved);
        if (entries == null) {
            return total;
        }
        for (AccountingEntry entry : entries) {
            if (entry.getDcFlag() == target) {
                total = total.plus(entry.getAmount());
            }
        }
        return total.rounded(2, RoundingMode.HALF_UP);
    }

    private static Money normalizeNonNegative(Money amount, String fieldName, CurrencyUnit currencyUnit) {
        if (amount == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!currencyUnit.equals(amount.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must equal voucher currency");
        }
        if (amount.isLessThan(Money.zero(currencyUnit))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
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
