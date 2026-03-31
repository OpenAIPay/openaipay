package cn.openaipay.domain.coupon.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
/**
 * 优惠券模板模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class CouponTemplate {

    /** 模板标识 */
    private final Long templateId;
    /** 模板编码 */
    private final String templateCode;
    /** 模板名称 */
    private String templateName;
    /** 场景类型 */
    private CouponSceneType sceneType;
    /** 值类型 */
    private CouponValueType valueType;
    /** 金额 */
    private Money amount;
    /** 最小金额 */
    private Money minAmount;
    /** 最大金额 */
    private Money maxAmount;
    /** 门槛金额 */
    private Money thresholdAmount;
    /** 总预算 */
    private Money totalBudget;
    /** 总库存 */
    private Integer totalStock;
    /** 已领数量 */
    private Integer claimedCount;
    /** 单用户上限 */
    private Integer perUserLimit;
    /** 领取开始时间 */
    private LocalDateTime claimStartTime;
    /** 领取结束时间 */
    private LocalDateTime claimEndTime;
    /** 使用开始时间 */
    private LocalDateTime useStartTime;
    /** 使用结束时间 */
    private LocalDateTime useEndTime;
    /** 资金来源 */
    private String fundingSource;
    /** 规则载荷 */
    private String rulePayload;
    /** 业务状态值 */
    private CouponTemplateStatus status;
    /** 创建人 */
    private String createdBy;
    /** 更新人 */
    private String updatedBy;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public CouponTemplate(Long templateId,
                          String templateCode,
                          String templateName,
                          CouponSceneType sceneType,
                          CouponValueType valueType,
                          Money amount,
                          Money minAmount,
                          Money maxAmount,
                          Money thresholdAmount,
                          Money totalBudget,
                          Integer totalStock,
                          Integer claimedCount,
                          Integer perUserLimit,
                          LocalDateTime claimStartTime,
                          LocalDateTime claimEndTime,
                          LocalDateTime useStartTime,
                          LocalDateTime useEndTime,
                          String fundingSource,
                          String rulePayload,
                          CouponTemplateStatus status,
                          String createdBy,
                          String updatedBy,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.templateId = templateId;
        this.templateCode = normalizeRequired(templateCode, "templateCode");
        this.templateName = normalizeRequired(templateName, "templateName");
        this.sceneType = requireNonNull(sceneType, "sceneType");
        this.valueType = requireNonNull(valueType, "valueType");
        this.amount = normalizeAmountNullable(amount);
        this.minAmount = normalizeAmountNullable(minAmount);
        this.maxAmount = normalizeAmountNullable(maxAmount);
        this.thresholdAmount = normalizeAmount(thresholdAmount, "thresholdAmount");
        this.totalBudget = normalizeAmount(totalBudget, "totalBudget");
        this.totalStock = requirePositive(totalStock, "totalStock");
        this.claimedCount = claimedCount == null ? 0 : Math.max(claimedCount, 0);
        this.perUserLimit = requirePositive(perUserLimit, "perUserLimit");
        this.claimStartTime = requireNonNull(claimStartTime, "claimStartTime");
        this.claimEndTime = requireNonNull(claimEndTime, "claimEndTime");
        this.useStartTime = requireNonNull(useStartTime, "useStartTime");
        this.useEndTime = requireNonNull(useEndTime, "useEndTime");
        this.fundingSource = normalizeRequired(fundingSource, "fundingSource");
        this.rulePayload = normalizeOptional(rulePayload);
        this.status = requireNonNull(status, "status");
        this.createdBy = normalizeRequired(createdBy, "createdBy");
        this.updatedBy = normalizeRequired(updatedBy, "updatedBy");
        this.createdAt = requireNonNull(createdAt, "createdAt");
        this.updatedAt = requireNonNull(updatedAt, "updatedAt");

        validateTimeWindow();
        validateValueTypeRules();
    }

    /**
     * 创建NEW信息。
     */
    public static CouponTemplate createNew(String templateCode,
                                           String templateName,
                                           CouponSceneType sceneType,
                                           CouponValueType valueType,
                                           Money amount,
                                           Money minAmount,
                                           Money maxAmount,
                                           Money thresholdAmount,
                                           Money totalBudget,
                                           Integer totalStock,
                                           Integer perUserLimit,
                                           LocalDateTime claimStartTime,
                                           LocalDateTime claimEndTime,
                                           LocalDateTime useStartTime,
                                           LocalDateTime useEndTime,
                                           String fundingSource,
                                           String rulePayload,
                                           CouponTemplateStatus status,
                                           String operator,
                                           LocalDateTime now) {
        CouponTemplateStatus templateStatus = status == null ? CouponTemplateStatus.DRAFT : status;
        return new CouponTemplate(
                null,
                templateCode,
                templateName,
                sceneType,
                valueType,
                amount,
                minAmount,
                maxAmount,
                thresholdAmount,
                totalBudget,
                totalStock,
                0,
                perUserLimit,
                claimStartTime,
                claimEndTime,
                useStartTime,
                useEndTime,
                fundingSource,
                rulePayload,
                templateStatus,
                operator,
                operator,
                now,
                now
        );
    }

    /**
     * 更新业务数据。
     */
    public void update(String templateName,
                       CouponSceneType sceneType,
                       CouponValueType valueType,
                       Money amount,
                       Money minAmount,
                       Money maxAmount,
                       Money thresholdAmount,
                       Money totalBudget,
                       Integer totalStock,
                       Integer perUserLimit,
                       LocalDateTime claimStartTime,
                       LocalDateTime claimEndTime,
                       LocalDateTime useStartTime,
                       LocalDateTime useEndTime,
                       String fundingSource,
                       String rulePayload,
                       String operator,
                       LocalDateTime now) {
        this.templateName = normalizeRequired(templateName, "templateName");
        this.sceneType = requireNonNull(sceneType, "sceneType");
        this.valueType = requireNonNull(valueType, "valueType");
        this.amount = normalizeAmountNullable(amount);
        this.minAmount = normalizeAmountNullable(minAmount);
        this.maxAmount = normalizeAmountNullable(maxAmount);
        this.thresholdAmount = normalizeAmount(thresholdAmount, "thresholdAmount");
        this.totalBudget = normalizeAmount(totalBudget, "totalBudget");
        this.totalStock = requirePositive(totalStock, "totalStock");
        this.perUserLimit = requirePositive(perUserLimit, "perUserLimit");
        this.claimStartTime = requireNonNull(claimStartTime, "claimStartTime");
        this.claimEndTime = requireNonNull(claimEndTime, "claimEndTime");
        this.useStartTime = requireNonNull(useStartTime, "useStartTime");
        this.useEndTime = requireNonNull(useEndTime, "useEndTime");
        this.fundingSource = normalizeRequired(fundingSource, "fundingSource");
        this.rulePayload = normalizeOptional(rulePayload);
        validateTimeWindow();
        validateValueTypeRules();
        if (claimedCount > totalStock) {
            throw new IllegalArgumentException("totalStock can not be smaller than current claimedCount");
        }
        touch(operator, now);
    }

    /**
     * 处理状态。
     */
    public void changeStatus(CouponTemplateStatus targetStatus, String operator, LocalDateTime now) {
        this.status = requireNonNull(targetStatus, "status");
        touch(operator, now);
    }

    /**
     * 判断是否可业务数据。
     */
    public boolean canClaim(LocalDateTime now) {
        if (status != CouponTemplateStatus.ACTIVE) {
            return false;
        }
        if (claimedCount >= totalStock) {
            return false;
        }
        return !now.isBefore(claimStartTime) && !now.isAfter(claimEndTime);
    }

    /**
     * 处理优惠券金额。
     */
    public Money nextCouponAmount() {
        if (valueType == CouponValueType.FIXED) {
            return amount;
        }
        long minCents = minAmount.getAmount().movePointRight(2).longValueExact();
        long maxCents = maxAmount.getAmount().movePointRight(2).longValueExact();
        long randomCents = ThreadLocalRandom.current().nextLong(minCents, maxCents + 1);
        return Money.ofMinor(defaultCurrencyUnit(), randomCents);
    }

    /**
     * 处理数量信息。
     */
    public void increaseClaimedCount(String operator, LocalDateTime now) {
        if (claimedCount >= totalStock) {
            throw new IllegalStateException("coupon stock exhausted");
        }
        claimedCount = claimedCount + 1;
        touch(operator, now);
    }

    /**
     * 判断是否AT信息。
     */
    public boolean isUsableAt(LocalDateTime now) {
        return !now.isBefore(useStartTime) && !now.isAfter(useEndTime);
    }

    /**
     * 获取模板ID。
     */
    public Long getTemplateId() {
        return templateId;
    }

    /**
     * 获取模板编码。
     */
    public String getTemplateCode() {
        return templateCode;
    }

    /**
     * 获取模板信息。
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * 获取场景类型信息。
     */
    public CouponSceneType getSceneType() {
        return sceneType;
    }

    /**
     * 获取值类型信息。
     */
    public CouponValueType getValueType() {
        return valueType;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取MIN金额。
     */
    public Money getMinAmount() {
        return minAmount;
    }

    /**
     * 获取MAX金额。
     */
    public Money getMaxAmount() {
        return maxAmount;
    }

    /**
     * 获取金额。
     */
    public Money getThresholdAmount() {
        return thresholdAmount;
    }

    /**
     * 获取业务数据。
     */
    public Money getTotalBudget() {
        return totalBudget;
    }

    /**
     * 获取业务数据。
     */
    public Integer getTotalStock() {
        return totalStock;
    }

    /**
     * 获取数量信息。
     */
    public Integer getClaimedCount() {
        return claimedCount;
    }

    /**
     * 获取PER用户限额信息。
     */
    public Integer getPerUserLimit() {
        return perUserLimit;
    }

    /**
     * 获取时间。
     */
    public LocalDateTime getClaimStartTime() {
        return claimStartTime;
    }

    /**
     * 获取END时间。
     */
    public LocalDateTime getClaimEndTime() {
        return claimEndTime;
    }

    /**
     * 获取USE时间。
     */
    public LocalDateTime getUseStartTime() {
        return useStartTime;
    }

    /**
     * 获取USEEND时间。
     */
    public LocalDateTime getUseEndTime() {
        return useEndTime;
    }

    /**
     * 获取业务数据。
     */
    public String getFundingSource() {
        return fundingSource;
    }

    /**
     * 获取规则。
     */
    public String getRulePayload() {
        return rulePayload;
    }

    /**
     * 获取状态。
     */
    public CouponTemplateStatus getStatus() {
        return status;
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

    private void validateTimeWindow() {
        if (claimEndTime.isBefore(claimStartTime)) {
            throw new IllegalArgumentException("claimEndTime must be after claimStartTime");
        }
        if (useEndTime.isBefore(useStartTime)) {
            throw new IllegalArgumentException("useEndTime must be after useStartTime");
        }
        if (useEndTime.isBefore(claimStartTime)) {
            throw new IllegalArgumentException("use window must overlap claim window");
        }
    }

    private void validateValueTypeRules() {
        if (valueType == CouponValueType.FIXED) {
            if (amount == null || amount.compareTo(zeroMoney()) <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0 for FIXED coupon");
            }
            return;
        }

        if (minAmount == null || minAmount.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException("minAmount must be greater than 0 for RANDOM coupon");
        }
        if (maxAmount == null || maxAmount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("maxAmount must be >= minAmount for RANDOM coupon");
        }
    }

    private void touch(String operator, LocalDateTime now) {
        this.updatedBy = normalizeRequired(operator, "operator");
        this.updatedAt = requireNonNull(now, "updatedAt");
    }

    private <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }

    private Integer requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
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

    private Money normalizeAmount(Money amount, String field) {
        if (amount == null || amount.compareTo(zeroMoney()) < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeAmountNullable(Money amount) {
        if (amount == null) {
            return null;
        }
        if (amount.compareTo(zeroMoney()) < 0) {
            throw new IllegalArgumentException("amount must be greater than or equal to 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroMoney() {
        return Money.zero(defaultCurrencyUnit());
    }

    private CurrencyUnit defaultCurrencyUnit() {
        return CurrencyUnit.of("CNY");
    }
}
