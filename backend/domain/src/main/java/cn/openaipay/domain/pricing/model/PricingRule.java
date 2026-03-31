package cn.openaipay.domain.pricing.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import cn.openaipay.domain.shared.number.RateValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
/**
 * Pricing规则模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PricingRule {

    /** 全匹配常量 */
    public static final String MATCH_ALL = "ALL";

    /** 规则标识 */
    private final Long ruleId;
    /** 计费规则编码 */
    private final String ruleCode;
    /** 规则名称 */
    private String ruleName;
    /** 业务场景编码 */
    private String businessSceneCode;
    /** 支付方式 */
    private String paymentMethod;
    /** 币种编码 */
    private String currencyCode;
    /** 计费模式 */
    private PricingFeeMode feeMode;
    /** 手续费费率 */
    private RateValue feeRate;
    /** 固定费用 */
    private Money fixedFee;
    /** 最低费用 */
    private Money minFee;
    /** 最高费用 */
    private Money maxFee;
    /** 手续费承担方 */
    private PricingFeeBearer feeBearer;
    /** 优先级 */
    private Integer priority;
    /** 业务状态值 */
    private PricingRuleStatus status;
    /** 生效开始 */
    private LocalDateTime validFrom;
    /** 生效结束 */
    private LocalDateTime validTo;
    /** 规则载荷 */
    private String rulePayload;
    /** 创建人 */
    private final String createdBy;
    /** 更新人 */
    private String updatedBy;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public PricingRule(Long ruleId,
                       String ruleCode,
                       String ruleName,
                       String businessSceneCode,
                       String paymentMethod,
                       String currencyCode,
                       PricingFeeMode feeMode,
                       RateValue feeRate,
                       Money fixedFee,
                       Money minFee,
                       Money maxFee,
                       PricingFeeBearer feeBearer,
                       Integer priority,
                       PricingRuleStatus status,
                       LocalDateTime validFrom,
                       LocalDateTime validTo,
                       String rulePayload,
                       String createdBy,
                       String updatedBy,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.ruleId = ruleId;
        this.ruleCode = normalizeRequired(ruleCode, "ruleCode").toUpperCase(Locale.ROOT);
        this.ruleName = normalizeRequired(ruleName, "ruleName");
        this.businessSceneCode = normalizeDimension(businessSceneCode, "businessSceneCode");
        this.paymentMethod = normalizeDimension(paymentMethod, "paymentMethod");
        this.currencyCode = normalizeDimension(currencyCode, "currencyCode");
        this.feeMode = feeMode == null ? PricingFeeMode.RATE : feeMode;
        this.feeRate = normalizeRate(feeRate);
        this.fixedFee = normalizeFee(fixedFee, "fixedFee");
        this.minFee = normalizeNonNegative(minFee, "minFee");
        this.maxFee = normalizeNonNegative(maxFee, "maxFee");
        this.feeBearer = feeBearer == null ? PricingFeeBearer.PAYER : feeBearer;
        this.priority = normalizePriority(priority);
        this.status = status == null ? PricingRuleStatus.DRAFT : status;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.rulePayload = normalizeOptional(rulePayload);
        this.createdBy = normalizeRequired(createdBy, "createdBy");
        this.updatedBy = normalizeRequired(updatedBy, "updatedBy");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;

        validateTimeWindow();
        validateFeeExpression();
    }

    /**
     * 创建NEW信息。
     */
    public static PricingRule createNew(String ruleCode,
                                        String ruleName,
                                        String businessSceneCode,
                                        String paymentMethod,
                                        String currencyCode,
                                        PricingFeeMode feeMode,
                                        RateValue feeRate,
                                        Money fixedFee,
                                        Money minFee,
                                        Money maxFee,
                                        PricingFeeBearer feeBearer,
                                        Integer priority,
                                        PricingRuleStatus status,
                                        LocalDateTime validFrom,
                                        LocalDateTime validTo,
                                        String rulePayload,
                                        String operator,
                                        LocalDateTime now) {
        PricingRuleStatus initialStatus = status == null ? PricingRuleStatus.DRAFT : status;
        return new PricingRule(
                null,
                ruleCode,
                ruleName,
                businessSceneCode,
                paymentMethod,
                currencyCode,
                feeMode,
                feeRate,
                fixedFee,
                minFee,
                maxFee,
                feeBearer,
                priority,
                initialStatus,
                validFrom,
                validTo,
                rulePayload,
                operator,
                operator,
                now,
                now
        );
    }

    /**
     * 更新业务数据。
     */
    public void update(String ruleName,
                       String businessSceneCode,
                       String paymentMethod,
                       String currencyCode,
                       PricingFeeMode feeMode,
                       RateValue feeRate,
                       Money fixedFee,
                       Money minFee,
                       Money maxFee,
                       PricingFeeBearer feeBearer,
                       Integer priority,
                       LocalDateTime validFrom,
                       LocalDateTime validTo,
                       String rulePayload,
                       String operator,
                       LocalDateTime now) {
        this.ruleName = normalizeRequired(ruleName, "ruleName");
        this.businessSceneCode = normalizeDimension(businessSceneCode, "businessSceneCode");
        this.paymentMethod = normalizeDimension(paymentMethod, "paymentMethod");
        this.currencyCode = normalizeDimension(currencyCode, "currencyCode");
        this.feeMode = feeMode == null ? PricingFeeMode.RATE : feeMode;
        this.feeRate = normalizeRate(feeRate);
        this.fixedFee = normalizeFee(fixedFee, "fixedFee");
        this.minFee = normalizeNonNegative(minFee, "minFee");
        this.maxFee = normalizeNonNegative(maxFee, "maxFee");
        this.feeBearer = feeBearer == null ? PricingFeeBearer.PAYER : feeBearer;
        this.priority = normalizePriority(priority);
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.rulePayload = normalizeOptional(rulePayload);

        validateTimeWindow();
        validateFeeExpression();
        touch(operator, now);
    }

    /**
     * 处理状态。
     */
    public void changeStatus(PricingRuleStatus targetStatus, String operator, LocalDateTime now) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.status = targetStatus;
        touch(operator, now);
    }

    /**
     * 判断是否AT信息。
     */
    public boolean isActiveAt(LocalDateTime now) {
        if (status != PricingRuleStatus.ACTIVE) {
            return false;
        }
        if (now == null) {
            return true;
        }
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }
        return true;
    }

    /**
     * 处理业务数据。
     */
    public boolean matches(String businessSceneCode,
                           String paymentMethod,
                           String currencyCode,
                           LocalDateTime now) {
        String scene = normalizeDimension(businessSceneCode, "businessSceneCode");
        String method = normalizeDimension(paymentMethod, "paymentMethod");
        String currency = normalizeDimension(currencyCode, "currencyCode");
        return isActiveAt(now)
                && (MATCH_ALL.equals(this.businessSceneCode) || this.businessSceneCode.equals(scene))
                && (MATCH_ALL.equals(this.paymentMethod) || this.paymentMethod.equals(method))
                && (MATCH_ALL.equals(this.currencyCode) || this.currencyCode.equals(currency));
    }

    /**
     * 处理FEE信息。
     */
    public Money calculateFee(Money originalAmount) {
        Money normalizedOriginal = normalizeAmount(originalAmount, "originalAmount");
        Money fee;
        switch (feeMode) {
            case RATE -> fee = multiplyByRate(normalizedOriginal, feeRate);
            case FIXED -> fee = fixedFee;
            case RATE_PLUS_FIXED -> fee = multiplyByRate(normalizedOriginal, feeRate).plus(fixedFee);
            default -> throw new IllegalStateException("unsupported feeMode");
        }
        fee = fee.rounded(2, RoundingMode.HALF_UP);

        if (minFee.compareTo(zeroMoney()) > 0 && fee.compareTo(minFee) < 0) {
            fee = minFee;
        }
        if (maxFee.compareTo(zeroMoney()) > 0 && fee.compareTo(maxFee) > 0) {
            fee = maxFee;
        }
        if (fee.compareTo(zeroMoney()) < 0) {
            throw new IllegalStateException("calculated fee can not be negative");
        }
        return fee;
    }

    /**
     * 处理金额。
     */
    public Money calculatePayableAmount(Money originalAmount, Money feeAmount) {
        Money original = normalizeAmount(originalAmount, "originalAmount");
        Money fee = normalizeNonNegative(feeAmount, "feeAmount");
        if (feeBearer == PricingFeeBearer.PAYER) {
            return original.plus(fee).rounded(2, RoundingMode.HALF_UP);
        }
        return original;
    }

    /**
     * 处理结算金额。
     */
    public Money calculateSettleAmount(Money originalAmount, Money feeAmount) {
        Money original = normalizeAmount(originalAmount, "originalAmount");
        Money fee = normalizeNonNegative(feeAmount, "feeAmount");
        if (feeBearer == PricingFeeBearer.PAYEE) {
            Money settle = original.minus(fee).rounded(2, RoundingMode.HALF_UP);
            if (settle.compareTo(zeroMoney()) < 0) {
                throw new IllegalArgumentException("fee exceeds originalAmount when feeBearer is PAYEE");
            }
            return settle;
        }
        return original;
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
     * 获取编码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取FEE信息。
     */
    public PricingFeeMode getFeeMode() {
        return feeMode;
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
     * 获取MINFEE信息。
     */
    public Money getMinFee() {
        return minFee;
    }

    /**
     * 获取MAXFEE信息。
     */
    public Money getMaxFee() {
        return maxFee;
    }

    /**
     * 获取FEE信息。
     */
    public PricingFeeBearer getFeeBearer() {
        return feeBearer;
    }

    /**
     * 获取业务数据。
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 获取状态。
     */
    public PricingRuleStatus getStatus() {
        return status;
    }

    /**
     * 获取业务数据。
     */
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * 获取TO信息。
     */
    public LocalDateTime getValidTo() {
        return validTo;
    }

    /**
     * 获取规则。
     */
    public String getRulePayload() {
        return rulePayload;
    }

    /**
     * 按条件获取记录。
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 按条件获取记录。
     */
    public String getUpdatedBy() {
        return updatedBy;
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

    private void touch(String operator, LocalDateTime now) {
        this.updatedBy = normalizeRequired(operator, "operator");
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private void validateTimeWindow() {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("validFrom must be before validTo");
        }
    }

    private void validateFeeExpression() {
        if (maxFee.compareTo(zeroMoney()) > 0 && minFee.compareTo(maxFee) > 0) {
            throw new IllegalArgumentException("minFee can not exceed maxFee");
        }

        boolean ratePositive = feeRate.compareTo(RateValue.ZERO) > 0;
        boolean fixedPositive = fixedFee.compareTo(zeroMoney()) > 0;

        switch (feeMode) {
            case RATE -> {
                if (feeRate.compareTo(RateValue.ZERO) < 0) {
                    throw new IllegalArgumentException("feeRate must not be less than 0 for RATE mode");
                }
            }
            case FIXED -> {
                if (fixedFee.compareTo(zeroMoney()) < 0) {
                    throw new IllegalArgumentException("fixedFee must not be less than 0 for FIXED mode");
                }
            }
            case RATE_PLUS_FIXED -> {
                if (feeRate.compareTo(RateValue.ZERO) < 0 || fixedFee.compareTo(zeroMoney()) < 0) {
                    throw new IllegalArgumentException("feeRate and fixedFee must not be less than 0 for RATE_PLUS_FIXED mode");
                }
            }
            default -> throw new IllegalStateException("unsupported feeMode");
        }
    }

    private Integer normalizePriority(Integer priority) {
        if (priority == null) {
            return 100;
        }
        if (priority < 0) {
            throw new IllegalArgumentException("priority must not be less than 0");
        }
        return priority;
    }

    private String normalizeDimension(String raw, String fieldName) {
        String normalized = normalizeRequired(raw, fieldName).toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new IllegalArgumentException(fieldName + " length must be <= 64");
        }
        return normalized;
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

    private Money normalizeAmount(Money amount, String fieldName) {
        if (amount == null || amount.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeFee(Money amount, String fieldName) {
        if (amount == null) {
            return zeroMoney().rounded(2, RoundingMode.HALF_UP);
        }
        if (amount.compareTo(zeroMoney()) < 0) {
            throw new IllegalArgumentException(fieldName + " must not be less than 0");
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
        if (rate.compareTo(RateValue.ONE) > 0) {
            throw new IllegalArgumentException("feeRate must be <= 1");
        }
        return rate.setScale(6, RoundingMode.HALF_UP);
    }

    private Money multiplyByRate(Money amount, RateValue rate) {
        BigDecimal multiplied = amount.getAmount().multiply(rate.toBigDecimal());
        return Money.of(amount.getCurrencyUnit(), multiplied, RoundingMode.HALF_UP);
    }

    private Money zeroMoney() {
        if (currencyCode == null || currencyCode.isBlank() || MATCH_ALL.equals(currencyCode)) {
            return Money.zero(CurrencyUnit.of("CNY"));
        }
        return Money.zero(CurrencyUnit.of(currencyCode.trim().toUpperCase(Locale.ROOT)));
    }
}
