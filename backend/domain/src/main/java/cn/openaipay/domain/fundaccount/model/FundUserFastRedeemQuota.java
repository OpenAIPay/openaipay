package cn.openaipay.domain.fundaccount.model;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基金用户Fast赎回额度模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class FundUserFastRedeemQuota {

    /** 基金产品编码 */
    private final String fundCode;
    /** 用户ID */
    private final Long userId;
    /** 额度日期 */
    private final LocalDate quotaDate;
    /** 额度上限 */
    private FundAmount quotaLimit;
    /** 额度已用 */
    private FundAmount quotaUsed;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public FundUserFastRedeemQuota(String fundCode,
                                   Long userId,
                                   LocalDate quotaDate,
                                   FundAmount quotaLimit,
                                   FundAmount quotaUsed,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        this.fundCode = fundCode;
        this.userId = userId;
        this.quotaDate = quotaDate;
        this.quotaLimit = normalize(quotaLimit);
        this.quotaUsed = normalize(quotaUsed);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 初始化业务数据。
     */
    public static FundUserFastRedeemQuota init(String fundCode,
                                               Long userId,
                                               LocalDate quotaDate,
                                               FundAmount quotaLimit,
                                               LocalDateTime now) {
        return new FundUserFastRedeemQuota(
                fundCode,
                userId,
                quotaDate,
                quotaLimit,
                FundAmount.ZERO,
                now,
                now
        );
    }

    /**
     * 获取基金编码。
     */
    public String getFundCode() {
        return fundCode;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取额度日期信息。
     */
    public LocalDate getQuotaDate() {
        return quotaDate;
    }

    /**
     * 获取额度限额信息。
     */
    public FundAmount getQuotaLimit() {
        return quotaLimit;
    }

    /**
     * 获取额度信息。
     */
    public FundAmount getQuotaUsed() {
        return quotaUsed;
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
     * 重置限额信息。
     */
    public void resetLimit(FundAmount limit, LocalDateTime now) {
        FundAmount normalizedLimit = normalize(limit);
        if (normalizedLimit.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException("quota limit must be greater than 0");
        }
        this.quotaLimit = normalizedLimit;
        this.updatedAt = now;
    }

    /**
     * 处理业务数据。
     */
    public void occupy(FundAmount amount, LocalDateTime now) {
        FundAmount value = normalize(amount);
        if (value.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        if (quotaUsed.add(value).compareTo(quotaLimit) > 0) {
            throw new IllegalArgumentException("user fast redeem quota exceeded");
        }
        quotaUsed = normalize(quotaUsed.add(value));
        updatedAt = now;
    }

    /**
     * 处理业务数据。
     */
    public void release(FundAmount amount, LocalDateTime now) {
        FundAmount value = normalize(amount);
        if (value.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        if (quotaUsed.compareTo(value) < 0) {
            throw new IllegalStateException("quota used is not enough to release");
        }
        quotaUsed = normalize(quotaUsed.subtract(value));
        updatedAt = now;
    }

    private FundAmount normalize(FundAmount source) {
        if (source == null) {
            return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return source.setScale(4, RoundingMode.HALF_UP);
    }
}
