package cn.openaipay.domain.pay.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 支付资金明细摘要模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public abstract class PayFundDetailSummary {

    /** 主键ID */
    private final Long id;
    /** 支付单号 */
    private final String payOrderNo;
    /** 支付工具 */
    private final PayFundDetailTool payTool;
    /** 归属方 */
    private final PayFundDetailOwner detailOwner;
    /** 金额 */
    private final Money amount;
    /** 累计退款金额 */
    private Money cumulativeRefundAmount;
    /** 创建时间 */
    private final LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    protected PayFundDetailSummary(Long id,
                                   String payOrderNo,
                                   PayFundDetailTool payTool,
                                   PayFundDetailOwner detailOwner,
                                   Money amount,
                                   Money cumulativeRefundAmount,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        this.id = id;
        this.payOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        this.payTool = payTool == null ? PayFundDetailTool.WALLET : payTool;
        this.detailOwner = detailOwner == null ? PayFundDetailOwner.PAYER : detailOwner;
        this.amount = normalizeRequiredAmount(amount, "amount");
        this.cumulativeRefundAmount = normalizeNonNegative(
                cumulativeRefundAmount,
                "cumulativeRefundAmount",
                this.amount.getCurrencyUnit()
        );
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if (this.cumulativeRefundAmount.isGreaterThan(this.amount)) {
            throw new IllegalArgumentException("cumulativeRefundAmount can not exceed amount");
        }
    }

    /**
     * 处理金额。
     */
    public void increaseRefundAmount(Money refundAmount, LocalDateTime now) {
        Money normalized = normalizeNonNegative(refundAmount, "refundAmount", this.amount.getCurrencyUnit());
        this.cumulativeRefundAmount = this.cumulativeRefundAmount.plus(normalized).rounded(2, RoundingMode.HALF_UP);
        if (this.cumulativeRefundAmount.isGreaterThan(this.amount)) {
            throw new IllegalArgumentException("cumulativeRefundAmount can not exceed amount");
        }
        touch(now);
    }

    /**
     * 刷新业务数据。
     */
    protected void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取支付信息。
     */
    public PayFundDetailTool getPayTool() {
        return payTool;
    }

    /**
     * 获取明细所属方信息。
     */
    public PayFundDetailOwner getDetailOwner() {
        return detailOwner;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取金额。
     */
    public Money getCumulativeRefundAmount() {
        return cumulativeRefundAmount;
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
     * 处理金额。
     */
    protected Money zeroMoney() {
        return Money.zero(this.amount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 规范化NON信息。
     */
    protected Money normalizeNonNegative(Money value, String fieldName, CurrencyUnit currencyUnit) {
        if (value == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!currencyUnit.equals(value.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must equal amount currency");
        }
        if (value.isLessThan(Money.zero(currencyUnit))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 规范化必需金额。
     */
    protected Money normalizeRequiredAmount(Money value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        if (value.isLessThan(Money.zero(value.getCurrencyUnit()))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 规范化必需信息。
     */
    protected String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    /**
     * 规范化业务数据。
     */
    protected String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
