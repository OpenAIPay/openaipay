package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 信用业务交易扩展单模型。
 *
 * 业务场景：爱花、爱借等信用类业务需要独立记录账单号、还款计划号、本金利息拆分，方便账单和后台运营查询。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class TradeCreditOrder {
    /** 扩展单主键ID。 */
    private final Long id;
    /** 信用业务交易单号。 */
    private final String bizOrderNo;
    /** 关联统一交易主单号。 */
    private final String tradeOrderNo;
    /** 信用产品类型。 */
    private final TradeCreditProductType creditProductType;
    /** 信用账户号。 */
    private final String creditAccountNo;
    /** 账单号。 */
    private final String billNo;
    /** 账单月份。 */
    private final String billMonth;
    /** 还款计划号。 */
    private final String repaymentPlanNo;
    /** 信用业务交易类型。 */
    private final TradeCreditTradeType creditTradeType;
    /** 主体金额。 */
    private final Money subjectAmount;
    /** 本金金额。 */
    private final Money principalAmount;
    /** 利息金额。 */
    private final Money interestAmount;
    /** 费用金额。 */
    private final Money feeAmount;
    /** 交易对手名称。 */
    private final String counterpartyName;
    /** 业务发生时间。 */
    private final LocalDateTime occurredAt;
    /** 记录创建时间。 */
    private final LocalDateTime createdAt;
    /** 记录更新时间。 */
    private final LocalDateTime updatedAt;

    public TradeCreditOrder(Long id,
                            String bizOrderNo,
                            String tradeOrderNo,
                            TradeCreditProductType creditProductType,
                            String creditAccountNo,
                            String billNo,
                            String billMonth,
                            String repaymentPlanNo,
                            TradeCreditTradeType creditTradeType,
                            Money subjectAmount,
                            Money principalAmount,
                            Money interestAmount,
                            Money feeAmount,
                            String counterpartyName,
                            LocalDateTime occurredAt,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.bizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        this.tradeOrderNo = normalizeRequired(tradeOrderNo, "tradeOrderNo");
        this.creditProductType = creditProductType == null ? TradeCreditProductType.AICREDIT : creditProductType;
        this.creditAccountNo = normalizeRequired(creditAccountNo, "creditAccountNo");
        this.billNo = normalizeOptional(billNo);
        this.billMonth = normalizeOptional(billMonth);
        this.repaymentPlanNo = normalizeOptional(repaymentPlanNo);
        this.creditTradeType = creditTradeType == null ? TradeCreditTradeType.REPAY : creditTradeType;
        CurrencyUnit currency = resolveCurrencyUnit(subjectAmount, principalAmount, interestAmount, feeAmount);
        this.subjectAmount = normalizeAmount(subjectAmount, currency, "subjectAmount");
        this.principalAmount = normalizeAmount(principalAmount, currency, "principalAmount");
        this.interestAmount = normalizeAmount(interestAmount, currency, "interestAmount");
        this.feeAmount = normalizeAmount(feeAmount, currency, "feeAmount");
        this.counterpartyName = normalizeOptional(counterpartyName);
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.createdAt = createdAt == null ? this.occurredAt : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    private String normalizeRequired(String raw, String field) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
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

    private CurrencyUnit resolveCurrencyUnit(Money... amounts) {
        for (Money amount : amounts) {
            if (amount != null) {
                return amount.getCurrencyUnit();
            }
        }
        return CurrencyUnit.of("CNY");
    }

    private Money normalizeAmount(Money value, CurrencyUnit currencyUnit, String field) {
        Money resolved = value == null ? Money.zero(currencyUnit) : value;
        if (!resolved.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(field + " currency must be consistent");
        }
        if (resolved.isNegative()) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
        return resolved.rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
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
     * 获取信用信息。
     */
    public TradeCreditProductType getCreditProductType() {
        return creditProductType;
    }

    /**
     * 获取信用账户NO信息。
     */
    public String getCreditAccountNo() {
        return creditAccountNo;
    }

    /**
     * 获取账单NO信息。
     */
    public String getBillNo() {
        return billNo;
    }

    /**
     * 获取业务数据。
     */
    public String getBillMonth() {
        return billMonth;
    }

    /**
     * 获取计划NO信息。
     */
    public String getRepaymentPlanNo() {
        return repaymentPlanNo;
    }

    /**
     * 获取信用交易类型信息。
     */
    public TradeCreditTradeType getCreditTradeType() {
        return creditTradeType;
    }

    /**
     * 获取科目金额。
     */
    public Money getSubjectAmount() {
        return subjectAmount;
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
    public Money getInterestAmount() {
        return interestAmount;
    }

    /**
     * 获取FEE金额。
     */
    public Money getFeeAmount() {
        return feeAmount;
    }

    /**
     * 获取业务数据。
     */
    public String getCounterpartyName() {
        return counterpartyName;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
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
