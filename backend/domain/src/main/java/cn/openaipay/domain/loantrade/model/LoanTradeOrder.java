package cn.openaipay.domain.loantrade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 爱借交易单聚合。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class LoanTradeOrder {
    /** 数据库主键ID */
    private final Long id;
    /** XID */
    private final String xid;
    /** 分支ID */
    private final String branchId;
    /** 业务单号 */
    private final String businessNo;
    /** 业务单号 */
    private final String accountNo;
    /** 业务类型 */
    private final LoanTradeOperationType operationType;
    /** 当前状态编码 */
    private LoanTradeOrderStatus status;
    /** 请求金额 */
    private final Money requestAmount;
    /** 业务金额 */
    private final Money interestAmount;
    /** 业务金额 */
    private final Money principalAmount;
    /** 业务金额 */
    private final Money fineAmount;
    /** 分支ID */
    private final String interestBranchId;
    /** 分支ID */
    private final String principalBranchId;
    /** 分支ID */
    private final String fineBranchId;
    /** 费率信息 */
    private BigDecimal annualRatePercent;
    /** remainingtermmonths信息 */
    private Integer remainingTermMonths;
    /** monthlypayment信息 */
    private Money monthlyPayment;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public LoanTradeOrder(Long id,
                          String xid,
                          String branchId,
                          String businessNo,
                          String accountNo,
                          LoanTradeOperationType operationType,
                          LoanTradeOrderStatus status,
                          Money requestAmount,
                          Money interestAmount,
                          Money principalAmount,
                          Money fineAmount,
                          String interestBranchId,
                          String principalBranchId,
                          String fineBranchId,
                          BigDecimal annualRatePercent,
                          Integer remainingTermMonths,
                          Money monthlyPayment,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.xid = normalizeRequired(xid, "xid");
        this.branchId = normalizeRequired(branchId, "branchId");
        this.businessNo = normalizeOptional(businessNo);
        this.accountNo = normalizeRequired(accountNo, "accountNo");
        this.operationType = operationType == null ? LoanTradeOperationType.REPAY : operationType;
        this.status = status == null ? LoanTradeOrderStatus.TRIED : status;
        CurrencyUnit currency = resolveCurrencyUnit(requestAmount, interestAmount, principalAmount, fineAmount, monthlyPayment);
        this.requestAmount = normalizeNonNegativeAmount(requestAmount, currency, "requestAmount");
        this.interestAmount = normalizeNonNegativeAmount(interestAmount, currency, "interestAmount");
        this.principalAmount = normalizeNonNegativeAmount(principalAmount, currency, "principalAmount");
        this.fineAmount = normalizeNonNegativeAmount(fineAmount, currency, "fineAmount");
        this.interestBranchId = normalizeOptional(interestBranchId);
        this.principalBranchId = normalizeOptional(principalBranchId);
        this.fineBranchId = normalizeOptional(fineBranchId);
        this.annualRatePercent = normalizeRate(annualRatePercent);
        this.remainingTermMonths = normalizeRemainingTerm(remainingTermMonths);
        this.monthlyPayment = normalizeNonNegativeAmount(monthlyPayment, currency, "monthlyPayment");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 处理NEW信息。
     */
    public static LoanTradeOrder newTried(String xid,
                                          String branchId,
                                          String businessNo,
                                          String accountNo,
                                          LoanTradeOperationType operationType,
                                          Money requestAmount,
                                          Money interestAmount,
                                          Money principalAmount,
                                          Money fineAmount,
                                          String interestBranchId,
                                          String principalBranchId,
                                          String fineBranchId,
                                          BigDecimal annualRatePercent,
                                          Integer remainingTermMonths,
                                          Money monthlyPayment,
                                          LocalDateTime now) {
        LocalDateTime occurredAt = now == null ? LocalDateTime.now() : now;
        return new LoanTradeOrder(
                null,
                xid,
                branchId,
                businessNo,
                accountNo,
                operationType,
                LoanTradeOrderStatus.TRIED,
                requestAmount,
                interestAmount,
                principalAmount,
                fineAmount,
                interestBranchId,
                principalBranchId,
                fineBranchId,
                annualRatePercent,
                remainingTermMonths,
                monthlyPayment,
                occurredAt,
                occurredAt
        );
    }

    /**
     * 标记业务数据。
     */
    public void markConfirmed(BigDecimal annualRatePercent,
                              Integer remainingTermMonths,
                              Money monthlyPayment,
                              LocalDateTime now) {
        if (status == LoanTradeOrderStatus.CANCELED) {
            throw new IllegalStateException("loan trade has already been canceled");
        }
        this.status = LoanTradeOrderStatus.CONFIRMED;
        this.annualRatePercent = normalizeRate(annualRatePercent);
        this.remainingTermMonths = normalizeRemainingTerm(remainingTermMonths);
        CurrencyUnit currency = resolveCurrencyUnit(this.requestAmount, this.interestAmount, this.principalAmount, this.fineAmount, this.monthlyPayment);
        this.monthlyPayment = normalizeNonNegativeAmount(monthlyPayment, currency, "monthlyPayment");
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markCanceled(LocalDateTime now) {
        if (status == LoanTradeOrderStatus.CONFIRMED) {
            throw new IllegalStateException("loan trade has already been confirmed");
        }
        this.status = LoanTradeOrderStatus.CANCELED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取XID。
     */
    public String getXid() {
        return xid;
    }

    /**
     * 获取分支ID。
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * 获取NO信息。
     */
    public String getBusinessNo() {
        return businessNo;
    }

    /**
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取业务数据。
     */
    public LoanTradeOperationType getOperationType() {
        return operationType;
    }

    /**
     * 获取状态。
     */
    public LoanTradeOrderStatus getStatus() {
        return status;
    }

    /**
     * 获取请求金额。
     */
    public Money getRequestAmount() {
        return requestAmount;
    }

    /**
     * 获取金额。
     */
    public Money getInterestAmount() {
        return interestAmount;
    }

    /**
     * 获取金额。
     */
    public Money getPrincipalAmount() {
        return principalAmount;
    }

    /**
     * 获取金额。
     */
    public Money getFineAmount() {
        return fineAmount;
    }

    /**
     * 获取ID。
     */
    public String getInterestBranchId() {
        return interestBranchId;
    }

    /**
     * 获取ID。
     */
    public String getPrincipalBranchId() {
        return principalBranchId;
    }

    /**
     * 获取ID。
     */
    public String getFineBranchId() {
        return fineBranchId;
    }

    /**
     * 获取费率信息。
     */
    public BigDecimal getAnnualRatePercent() {
        return annualRatePercent;
    }

    /**
     * 获取业务数据。
     */
    public Integer getRemainingTermMonths() {
        return remainingTermMonths;
    }

    /**
     * 获取业务数据。
     */
    public Money getMonthlyPayment() {
        return monthlyPayment;
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

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CurrencyUnit resolveCurrencyUnit(Money requestAmount,
                                             Money interestAmount,
                                             Money principalAmount,
                                             Money fineAmount,
                                             Money monthlyPayment) {
        Money[] candidates = new Money[]{requestAmount, interestAmount, principalAmount, fineAmount, monthlyPayment};
        for (Money candidate : candidates) {
            if (candidate != null) {
                return candidate.getCurrencyUnit();
            }
        }
        return CurrencyUnit.of("CNY");
    }

    private Money normalizeNonNegativeAmount(Money amount, CurrencyUnit currencyUnit, String fieldName) {
        Money resolved = amount == null ? Money.zero(currencyUnit) : amount;
        if (!resolved.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(fieldName + " currency must be consistent");
        }
        if (resolved.isNegative()) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return resolved.rounded(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeRate(BigDecimal rawRate) {
        if (rawRate == null) {
            return new BigDecimal("3.2400");
        }
        if (rawRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("annualRatePercent must be greater than or equal to 0");
        }
        return rawRate.setScale(4, RoundingMode.HALF_UP);
    }

    private Integer normalizeRemainingTerm(Integer rawTerm) {
        if (rawTerm == null || rawTerm <= 0) {
            return 24;
        }
        return rawTerm;
    }
}
