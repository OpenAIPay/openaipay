package cn.openaipay.domain.pricing.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import cn.openaipay.domain.shared.number.RateValue;
import java.math.RoundingMode;
import java.time.LocalDateTime;
/**
 * Pricing报价模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PricingQuote {

    /** 报价标识 */
    private final Long quoteId;
    /** 报价单号 */
    private final String quoteNo;
    /** 请求流水号 */
    private final String requestNo;
    /** 规则标识 */
    private final Long ruleId;
    /** 计费规则编码 */
    private final String ruleCode;
    /** 规则名称 */
    private final String ruleName;
    /** 业务场景编码 */
    private final String businessSceneCode;
    /** 支付方式 */
    private final String paymentMethod;
    /** 原始金额 */
    private final Money originalAmount;
    /** 手续费金额 */
    private final Money feeAmount;
    /** 应付金额 */
    private final Money payableAmount;
    /** 结算金额 */
    private final Money settleAmount;
    /** 计费模式 */
    private final PricingFeeMode feeMode;
    /** 手续费承担方 */
    private final PricingFeeBearer feeBearer;
    /** 手续费费率 */
    private final RateValue feeRate;
    /** 固定费用 */
    private final Money fixedFee;
    /** 规则载荷 */
    private final String rulePayload;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public PricingQuote(Long quoteId,
                        String quoteNo,
                        String requestNo,
                        Long ruleId,
                        String ruleCode,
                        String ruleName,
                        String businessSceneCode,
                        String paymentMethod,
                        Money originalAmount,
                        Money feeAmount,
                        Money payableAmount,
                        Money settleAmount,
                        PricingFeeMode feeMode,
                        PricingFeeBearer feeBearer,
                        RateValue feeRate,
                        Money fixedFee,
                        String rulePayload,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.quoteId = quoteId;
        this.quoteNo = normalizeRequired(quoteNo, "quoteNo");
        this.requestNo = normalizeRequired(requestNo, "requestNo");
        this.ruleId = requirePositive(ruleId, "ruleId");
        this.ruleCode = normalizeRequired(ruleCode, "ruleCode");
        this.ruleName = normalizeRequired(ruleName, "ruleName");
        this.businessSceneCode = normalizeRequired(businessSceneCode, "businessSceneCode");
        this.paymentMethod = normalizeRequired(paymentMethod, "paymentMethod");
        this.originalAmount = normalizeAmount(originalAmount, "originalAmount");
        this.feeAmount = normalizeNonNegative(feeAmount, "feeAmount");
        this.payableAmount = normalizeNonNegative(payableAmount, "payableAmount");
        this.settleAmount = normalizeNonNegative(settleAmount, "settleAmount");
        this.feeMode = feeMode == null ? PricingFeeMode.RATE : feeMode;
        this.feeBearer = feeBearer == null ? PricingFeeBearer.PAYER : feeBearer;
        this.feeRate = normalizeRate(feeRate);
        this.fixedFee = normalizeNonNegative(fixedFee, "fixedFee");
        this.rulePayload = normalizeOptional(rulePayload);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static PricingQuote create(String quoteNo,
                                      String requestNo,
                                      PricingRule rule,
                                      Money originalAmount,
                                      LocalDateTime now) {
        Money normalizedOriginal = normalizeAmountStatic(originalAmount, "originalAmount");
        Money fee = rule.calculateFee(normalizedOriginal);
        Money payable = rule.calculatePayableAmount(normalizedOriginal, fee);
        Money settle = rule.calculateSettleAmount(normalizedOriginal, fee);
        return new PricingQuote(
                null,
                quoteNo,
                requestNo,
                rule.getRuleId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getBusinessSceneCode(),
                rule.getPaymentMethod(),
                normalizedOriginal,
                fee,
                payable,
                settle,
                rule.getFeeMode(),
                rule.getFeeBearer(),
                rule.getFeeRate(),
                rule.getFixedFee(),
                rule.getRulePayload(),
                now,
                now
        );
    }

    /**
     * 获取ID。
     */
    public Long getQuoteId() {
        return quoteId;
    }

    /**
     * 获取NO信息。
     */
    public String getQuoteNo() {
        return quoteNo;
    }

    /**
     * 获取请求NO信息。
     */
    public String getRequestNo() {
        return requestNo;
    }

    /**
     * 获取规则ID。
     */
    public Long getRuleId() {
        return ruleId;
    }

    /**
     * 获取规则编码。
     */
    public String getRuleCode() {
        return ruleCode;
    }

    /**
     * 获取规则。
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * 获取场景编码。
     */
    public String getBusinessSceneCode() {
        return businessSceneCode;
    }

    /**
     * 获取业务数据。
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * 获取金额。
     */
    public Money getOriginalAmount() {
        return originalAmount;
    }

    /**
     * 获取FEE金额。
     */
    public Money getFeeAmount() {
        return feeAmount;
    }

    /**
     * 获取金额。
     */
    public Money getPayableAmount() {
        return payableAmount;
    }

    /**
     * 获取结算金额。
     */
    public Money getSettleAmount() {
        return settleAmount;
    }

    /**
     * 获取FEE信息。
     */
    public PricingFeeMode getFeeMode() {
        return feeMode;
    }

    /**
     * 获取FEE信息。
     */
    public PricingFeeBearer getFeeBearer() {
        return feeBearer;
    }

    /**
     * 获取FEE费率信息。
     */
    public RateValue getFeeRate() {
        return feeRate;
    }

    /**
     * 获取FEE信息。
     */
    public Money getFixedFee() {
        return fixedFee;
    }

    /**
     * 获取规则。
     */
    public String getRulePayload() {
        return rulePayload;
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

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private Money normalizeAmount(Money amount, String fieldName) {
        if (amount == null || amount.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private static Money normalizeAmountStatic(Money amount, String fieldName) {
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money amount, String fieldName) {
        if (amount == null) {
            return zeroMoney().rounded(2, RoundingMode.HALF_UP);
        }
        if (amount.compareTo(zeroMoney()) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be less than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private RateValue normalizeRate(RateValue rate) {
        if (rate == null) {
            return RateValue.ZERO;
        }
        if (rate.compareTo(RateValue.ZERO) < 0) {
            throw new IllegalArgumentException("feeRate must not be less than 0");
        }
        return rate.setScale(6, RoundingMode.HALF_UP);
    }

    private Money zeroMoney() {
        if (originalAmount == null) {
            return Money.zero(CurrencyUnit.of("CNY"));
        }
        return Money.zero(originalAmount.getCurrencyUnit());
    }
}
