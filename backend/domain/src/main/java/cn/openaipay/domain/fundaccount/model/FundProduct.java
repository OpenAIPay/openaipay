package cn.openaipay.domain.fundaccount.model;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 基金产品模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class FundProduct {

    /** 基金产品编码 */
    private final String fundCode;
    /** 产品名称 */
    private String productName;
    /** 币种编码 */
    private String currencyCode;
    /** 产品状态 */
    private FundProductStatus productStatus;
    /** 单笔申购最小金额 */
    private FundAmount singleSubscribeMinAmount;
    /** 单笔申购最大金额 */
    private FundAmount singleSubscribeMaxAmount;
    /** 单日申购最大金额 */
    private FundAmount dailySubscribeMaxAmount;
    /** 单笔赎回最小份额 */
    private FundAmount singleRedeemMinShare;
    /** 单笔赎回最大份额 */
    private FundAmount singleRedeemMaxShare;
    /** 单日赎回最大份额 */
    private FundAmount dailyRedeemMaxShare;
    /** 快速赎回单日额度 */
    private FundAmount fastRedeemDailyQuota;
    /** 快速赎回单用户单日额度 */
    private FundAmount fastRedeemPerUserDailyQuota;
    /** 切换启用开关 */
    private boolean switchEnabled;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public FundProduct(String fundCode,
                       String productName,
                       String currencyCode,
                       FundProductStatus productStatus,
                       FundAmount singleSubscribeMinAmount,
                       FundAmount singleSubscribeMaxAmount,
                       FundAmount dailySubscribeMaxAmount,
                       FundAmount singleRedeemMinShare,
                       FundAmount singleRedeemMaxShare,
                       FundAmount dailyRedeemMaxShare,
                       FundAmount fastRedeemDailyQuota,
                       FundAmount fastRedeemPerUserDailyQuota,
                       boolean switchEnabled,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.fundCode = fundCode;
        this.productName = productName;
        this.currencyCode = currencyCode;
        this.productStatus = productStatus;
        this.singleSubscribeMinAmount = normalize(singleSubscribeMinAmount);
        this.singleSubscribeMaxAmount = normalize(singleSubscribeMaxAmount);
        this.dailySubscribeMaxAmount = normalize(dailySubscribeMaxAmount);
        this.singleRedeemMinShare = normalize(singleRedeemMinShare);
        this.singleRedeemMaxShare = normalize(singleRedeemMaxShare);
        this.dailyRedeemMaxShare = normalize(dailyRedeemMaxShare);
        this.fastRedeemDailyQuota = normalize(fastRedeemDailyQuota);
        this.fastRedeemPerUserDailyQuota = normalize(fastRedeemPerUserDailyQuota);
        this.switchEnabled = switchEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理OF信息。
     */
    public static FundProduct defaultOf(String fundCode, String currencyCode, LocalDateTime now) {
        return new FundProduct(
                fundCode,
                fundCode + " Product",
                currencyCode,
                FundProductStatus.ACTIVE,
                new FundAmount("0.0100"),
                new FundAmount("1000000.0000"),
                new FundAmount("5000000.0000"),
                new FundAmount("0.0100"),
                new FundAmount("1000000.0000"),
                new FundAmount("5000000.0000"),
                new FundAmount("10000000.0000"),
                new FundAmount("100000.0000"),
                true,
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
     * 获取业务数据。
     */
    public String getProductName() {
        return productName;
    }

    /**
     * 获取编码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取状态。
     */
    public FundProductStatus getProductStatus() {
        return productStatus;
    }

    /**
     * 获取MIN金额。
     */
    public FundAmount getSingleSubscribeMinAmount() {
        return singleSubscribeMinAmount;
    }

    /**
     * 获取MAX金额。
     */
    public FundAmount getSingleSubscribeMaxAmount() {
        return singleSubscribeMaxAmount;
    }

    /**
     * 获取MAX金额。
     */
    public FundAmount getDailySubscribeMaxAmount() {
        return dailySubscribeMaxAmount;
    }

    /**
     * 获取MIN份额信息。
     */
    public FundAmount getSingleRedeemMinShare() {
        return singleRedeemMinShare;
    }

    /**
     * 获取MAX份额信息。
     */
    public FundAmount getSingleRedeemMaxShare() {
        return singleRedeemMaxShare;
    }

    /**
     * 获取MAX份额信息。
     */
    public FundAmount getDailyRedeemMaxShare() {
        return dailyRedeemMaxShare;
    }

    /**
     * 获取额度信息。
     */
    public FundAmount getFastRedeemDailyQuota() {
        return fastRedeemDailyQuota;
    }

    /**
     * 获取PER用户额度信息。
     */
    public FundAmount getFastRedeemPerUserDailyQuota() {
        return fastRedeemPerUserDailyQuota;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isSwitchEnabled() {
        return switchEnabled;
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
     * 更新业务数据。
     */
    public void updatePolicy(String productName,
                             String currencyCode,
                             FundProductStatus productStatus,
                             FundAmount singleSubscribeMinAmount,
                             FundAmount singleSubscribeMaxAmount,
                             FundAmount dailySubscribeMaxAmount,
                             FundAmount singleRedeemMinShare,
                             FundAmount singleRedeemMaxShare,
                             FundAmount dailyRedeemMaxShare,
                             FundAmount fastRedeemDailyQuota,
                             FundAmount fastRedeemPerUserDailyQuota,
                             Boolean switchEnabled,
                             LocalDateTime now) {
        if (productName != null && !productName.isBlank()) {
            this.productName = productName;
        }
        if (currencyCode != null && !currencyCode.isBlank()) {
            this.currencyCode = currencyCode;
        }
        if (productStatus != null) {
            this.productStatus = productStatus;
        }
        if (singleSubscribeMinAmount != null) {
            this.singleSubscribeMinAmount = normalize(singleSubscribeMinAmount);
        }
        if (singleSubscribeMaxAmount != null) {
            this.singleSubscribeMaxAmount = normalize(singleSubscribeMaxAmount);
        }
        if (dailySubscribeMaxAmount != null) {
            this.dailySubscribeMaxAmount = normalize(dailySubscribeMaxAmount);
        }
        if (singleRedeemMinShare != null) {
            this.singleRedeemMinShare = normalize(singleRedeemMinShare);
        }
        if (singleRedeemMaxShare != null) {
            this.singleRedeemMaxShare = normalize(singleRedeemMaxShare);
        }
        if (dailyRedeemMaxShare != null) {
            this.dailyRedeemMaxShare = normalize(dailyRedeemMaxShare);
        }
        if (fastRedeemDailyQuota != null) {
            this.fastRedeemDailyQuota = normalize(fastRedeemDailyQuota);
        }
        if (fastRedeemPerUserDailyQuota != null) {
            this.fastRedeemPerUserDailyQuota = normalize(fastRedeemPerUserDailyQuota);
        }
        if (switchEnabled != null) {
            this.switchEnabled = switchEnabled;
        }
        validateRanges();
        this.updatedAt = now;
    }

    /**
     * 校验业务数据。
     */
    public void validateSubscribe(FundAmount amount) {
        ensureActive();
        FundAmount value = normalize(amount);
        if (value.compareTo(singleSubscribeMinAmount) < 0) {
            throw new IllegalArgumentException("subscribe amount is lower than minimum");
        }
        if (value.compareTo(singleSubscribeMaxAmount) > 0) {
            throw new IllegalArgumentException("subscribe amount is greater than maximum");
        }
    }

    /**
     * 校验赎回信息。
     */
    public void validateRedeem(FundAmount share) {
        ensureActive();
        FundAmount value = normalize(share);
        if (value.compareTo(singleRedeemMinShare) < 0) {
            throw new IllegalArgumentException("redeem share is lower than minimum");
        }
        if (value.compareTo(singleRedeemMaxShare) > 0) {
            throw new IllegalArgumentException("redeem share is greater than maximum");
        }
    }

    /**
     * 确保业务数据。
     */
    public void ensureSwitchEnabled() {
        ensureActive();
        if (!switchEnabled) {
            throw new IllegalArgumentException("product switch is disabled");
        }
    }

    private void ensureActive() {
        if (productStatus != FundProductStatus.ACTIVE) {
            throw new IllegalStateException("fund product status is not ACTIVE");
        }
    }

    private void validateRanges() {
        if (singleSubscribeMinAmount.compareTo(FundAmount.ZERO) <= 0
                || singleSubscribeMaxAmount.compareTo(singleSubscribeMinAmount) < 0) {
            throw new IllegalArgumentException("invalid subscribe amount range");
        }
        if (singleRedeemMinShare.compareTo(FundAmount.ZERO) <= 0
                || singleRedeemMaxShare.compareTo(singleRedeemMinShare) < 0) {
            throw new IllegalArgumentException("invalid redeem share range");
        }
        if (dailySubscribeMaxAmount.compareTo(singleSubscribeMaxAmount) < 0) {
            throw new IllegalArgumentException("daily subscribe max must be >= single max");
        }
        if (dailyRedeemMaxShare.compareTo(singleRedeemMaxShare) < 0) {
            throw new IllegalArgumentException("daily redeem max must be >= single max");
        }
        if (fastRedeemDailyQuota.compareTo(FundAmount.ZERO) <= 0
                || fastRedeemPerUserDailyQuota.compareTo(FundAmount.ZERO) <= 0
                || fastRedeemDailyQuota.compareTo(fastRedeemPerUserDailyQuota) < 0) {
            throw new IllegalArgumentException("invalid fast redeem quota range");
        }
    }

    private FundAmount normalize(FundAmount value) {
        if (value == null) {
            return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
