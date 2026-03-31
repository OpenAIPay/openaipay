package cn.openaipay.domain.fundaccount.model;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 基金账户模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class FundAccount {

    /** 用户ID */
    private final Long userId;
    /** 基金产品编码 */
    private final String fundCode;
    /** 币种编码 */
    private final String currencyCode;
    /** 可用份额 */
    private FundAmount availableShare;
    /** 冻结份额 */
    private FundAmount frozenShare;
    /** 待处理申购金额 */
    private FundAmount pendingSubscribeAmount;
    /** 待处理赎回份额 */
    private FundAmount pendingRedeemShare;
    /** 累计收益 */
    private FundAmount accumulatedIncome;
    /** 昨日收益 */
    private FundAmount yesterdayIncome;
    /** 最新净值 */
    private FundAmount latestNav;
    /** 账户状态 */
    private FundAccountStatus accountStatus;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public FundAccount(Long userId,
                       String fundCode,
                       String currencyCode,
                       FundAmount availableShare,
                       FundAmount frozenShare,
                       FundAmount pendingSubscribeAmount,
                       FundAmount pendingRedeemShare,
                       FundAmount accumulatedIncome,
                       FundAmount yesterdayIncome,
                       FundAmount latestNav,
                       FundAccountStatus accountStatus,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.userId = userId;
        this.fundCode = fundCode;
        this.currencyCode = currencyCode;
        this.availableShare = normalize(availableShare);
        this.frozenShare = normalize(frozenShare);
        this.pendingSubscribeAmount = normalize(pendingSubscribeAmount);
        this.pendingRedeemShare = normalize(pendingRedeemShare);
        this.accumulatedIncome = normalize(accumulatedIncome);
        this.yesterdayIncome = normalize(yesterdayIncome);
        this.latestNav = normalize(latestNav);
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        assertNonNegativeState();
    }

    /**
     * 开通业务数据。
     */
    public static FundAccount open(Long userId, String fundCode, String currencyCode, LocalDateTime now) {
        return new FundAccount(
                userId,
                fundCode,
                currencyCode,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ONE,
                FundAccountStatus.ACTIVE,
                now,
                now
        );
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取基金编码。
     */
    public String getFundCode() {
        return fundCode;
    }

    /**
     * 获取编码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取份额信息。
     */
    public FundAmount getAvailableShare() {
        return availableShare;
    }

    /**
     * 获取份额信息。
     */
    public FundAmount getFrozenShare() {
        return frozenShare;
    }

    /**
     * 获取金额。
     */
    public FundAmount getPendingSubscribeAmount() {
        return pendingSubscribeAmount;
    }

    /**
     * 获取份额信息。
     */
    public FundAmount getPendingRedeemShare() {
        return pendingRedeemShare;
    }

    /**
     * 获取收益信息。
     */
    public FundAmount getAccumulatedIncome() {
        return accumulatedIncome;
    }

    /**
     * 获取收益信息。
     */
    public FundAmount getYesterdayIncome() {
        return yesterdayIncome;
    }

    /**
     * 获取NAV信息。
     */
    public FundAmount getLatestNav() {
        return latestNav;
    }

    /**
     * 获取账户状态。
     */
    public FundAccountStatus getAccountStatus() {
        return accountStatus;
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
     * 获取份额信息。
     */
    public FundAmount getHoldingShare() {
        return normalize(availableShare.add(frozenShare).add(pendingRedeemShare));
    }

    /**
     * 获取金额。
     */
    public FundAmount getHoldingAmount() {
        return normalize(getHoldingShare().multiply(latestNav));
    }

    /**
     * 处理业务数据。
     */
    public void placeSubscribe(FundAmount amount, LocalDateTime now) {
        ensureActive();
        FundAmount subscribeAmount = requirePositive(amount, "subscribe amount");
        pendingSubscribeAmount = normalize(pendingSubscribeAmount.add(subscribeAmount));
        touch(now);
    }

    /**
     * 确认业务数据。
     */
    public void confirmSubscribe(FundAmount amount, FundAmount confirmedShare, FundAmount nav, LocalDateTime now) {
        ensureActive();
        FundAmount subscribeAmount = requirePositive(amount, "subscribe amount");
        FundAmount share = requirePositive(confirmedShare, "confirmed share");
        if (pendingSubscribeAmount.compareTo(subscribeAmount) < 0) {
            throw new IllegalStateException("pending subscribe amount is not enough");
        }
        pendingSubscribeAmount = normalize(pendingSubscribeAmount.subtract(subscribeAmount));
        availableShare = normalize(availableShare.add(share));
        if (nav != null) {
            latestNav = normalize(nav);
        }
        touch(now);
    }

    /**
     * 取消业务数据。
     */
    public void cancelSubscribe(FundAmount amount, LocalDateTime now) {
        ensureActive();
        FundAmount subscribeAmount = requirePositive(amount, "subscribe amount");
        if (pendingSubscribeAmount.compareTo(subscribeAmount) < 0) {
            throw new IllegalStateException("pending subscribe amount is not enough");
        }
        pendingSubscribeAmount = normalize(pendingSubscribeAmount.subtract(subscribeAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void placeRedeem(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount redeemShare = requirePositive(share, "redeem share");
        if (availableShare.compareTo(redeemShare) < 0) {
            throw new IllegalArgumentException("insufficient available share");
        }
        availableShare = normalize(availableShare.subtract(redeemShare));
        pendingRedeemShare = normalize(pendingRedeemShare.add(redeemShare));
        touch(now);
    }

    /**
     * 确认赎回信息。
     */
    public void confirmRedeem(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount redeemShare = requirePositive(share, "redeem share");
        if (pendingRedeemShare.compareTo(redeemShare) < 0) {
            throw new IllegalStateException("pending redeem share is not enough");
        }
        pendingRedeemShare = normalize(pendingRedeemShare.subtract(redeemShare));
        touch(now);
    }

    /**
     * 取消赎回信息。
     */
    public void cancelRedeem(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount redeemShare = requirePositive(share, "redeem share");
        if (pendingRedeemShare.compareTo(redeemShare) < 0) {
            throw new IllegalStateException("pending redeem share is not enough");
        }
        pendingRedeemShare = normalize(pendingRedeemShare.subtract(redeemShare));
        availableShare = normalize(availableShare.add(redeemShare));
        touch(now);
    }

    /**
     * 处理结算收益信息。
     */
    public void settleIncome(FundAmount incomeAmount, FundAmount nav, LocalDateTime now) {
        ensureActive();
        FundAmount income = requirePositive(incomeAmount, "income amount");
        accumulatedIncome = normalize(accumulatedIncome.add(income));
        yesterdayIncome = income;
        if (nav != null) {
            latestNav = normalize(nav);
        }
        touch(now);
    }

    /**
     * 处理份额信息。
     */
    public void freezeShare(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount freeze = requirePositive(share, "freeze share");
        if (availableShare.compareTo(freeze) < 0) {
            throw new IllegalArgumentException("insufficient available share");
        }
        availableShare = normalize(availableShare.subtract(freeze));
        frozenShare = normalize(frozenShare.add(freeze));
        touch(now);
    }

    /**
     * 处理份额信息。
     */
    public void unfreezeShare(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount unfreeze = requirePositive(share, "unfreeze share");
        if (frozenShare.compareTo(unfreeze) < 0) {
            throw new IllegalStateException("frozen share is not enough");
        }
        frozenShare = normalize(frozenShare.subtract(unfreeze));
        availableShare = normalize(availableShare.add(unfreeze));
        touch(now);
    }

    /**
     * 处理结算份额信息。
     */
    public void settleFrozenShare(FundAmount share, LocalDateTime now) {
        ensureActive();
        FundAmount settle = requirePositive(share, "settle share");
        if (frozenShare.compareTo(settle) < 0) {
            throw new IllegalStateException("frozen share is not enough");
        }
        frozenShare = normalize(frozenShare.subtract(settle));
        touch(now);
    }

    private void ensureActive() {
        if (accountStatus != FundAccountStatus.ACTIVE) {
            throw new IllegalStateException("fund account status is not ACTIVE");
        }
    }

    private FundAmount requirePositive(FundAmount value, String fieldName) {
        FundAmount normalized = normalize(value);
        if (normalized.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return normalized;
    }

    private FundAmount normalize(FundAmount value) {
        if (value == null) {
            return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private void touch(LocalDateTime now) {
        assertNonNegativeState();
        this.updatedAt = now;
    }

    private void assertNonNegativeState() {
        ensureNonNegative(availableShare, "availableShare");
        ensureNonNegative(frozenShare, "frozenShare");
        ensureNonNegative(pendingSubscribeAmount, "pendingSubscribeAmount");
        ensureNonNegative(pendingRedeemShare, "pendingRedeemShare");
        ensureNonNegative(accumulatedIncome, "accumulatedIncome");
        ensureNonNegative(yesterdayIncome, "yesterdayIncome");
        ensureNonNegative(latestNav, "latestNav");
    }

    private void ensureNonNegative(FundAmount amount, String fieldName) {
        if (amount == null) {
            return;
        }
        if (amount.compareTo(FundAmount.ZERO) < 0) {
            throw new IllegalStateException(fieldName + " must not be less than 0");
        }
    }
}
