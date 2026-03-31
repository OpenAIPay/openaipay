package cn.openaipay.domain.coupon.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;
/**
 * 优惠券发放模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class CouponIssue {

    /** 发放标识 */
    private final Long issueId;
    /** 券编号 */
    private final String couponNo;
    /** 模板标识 */
    private final Long templateId;
    /** 用户ID */
    private final Long userId;
    /** 券金额 */
    private final Money couponAmount;
    /** 业务状态值 */
    private CouponIssueStatus status;
    /** 领取渠道 */
    private final String claimChannel;
    /** 业务编号 */
    private final String businessNo;
    /** 业务订单号 */
    private String orderNo;
    /** 全局业务单号 */
    private String bizOrderNo;
    /** 交易单号 */
    private String tradeOrderNo;
    /** 支付单号 */
    private String payOrderNo;
    /** 领取时间 */
    private final LocalDateTime claimedAt;
    /** 过期时间 */
    private final LocalDateTime expireAt;
    /** 已用时间 */
    private LocalDateTime usedAt;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public CouponIssue(Long issueId,
                       String couponNo,
                       Long templateId,
                       Long userId,
                       Money couponAmount,
                       CouponIssueStatus status,
                       String claimChannel,
                       String businessNo,
                       String orderNo,
                       String bizOrderNo,
                       String tradeOrderNo,
                       String payOrderNo,
                       LocalDateTime claimedAt,
                       LocalDateTime expireAt,
                       LocalDateTime usedAt,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.issueId = issueId;
        this.couponNo = normalizeRequired(couponNo, "couponNo");
        this.templateId = requirePositive(templateId, "templateId");
        this.userId = requirePositive(userId, "userId");
        this.couponAmount = normalizeAmount(couponAmount, "couponAmount");
        this.status = requireNonNull(status, "status");
        this.claimChannel = normalizeRequired(claimChannel, "claimChannel");
        this.businessNo = normalizeOptional(businessNo);
        this.orderNo = normalizeOptional(orderNo);
        this.bizOrderNo = normalizeOptional(bizOrderNo);
        this.tradeOrderNo = normalizeOptional(tradeOrderNo);
        this.payOrderNo = normalizeOptional(payOrderNo);
        this.claimedAt = requireNonNull(claimedAt, "claimedAt");
        this.expireAt = requireNonNull(expireAt, "expireAt");
        this.usedAt = usedAt;
        this.createdAt = requireNonNull(createdAt, "createdAt");
        this.updatedAt = requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * 处理业务数据。
     */
    public static CouponIssue issue(String couponNo,
                                    Long templateId,
                                    Long userId,
                                    Money couponAmount,
                                    String claimChannel,
                                    String businessNo,
                                    LocalDateTime now,
                                    LocalDateTime expireAt) {
        return new CouponIssue(
                null,
                couponNo,
                templateId,
                userId,
                couponAmount,
                CouponIssueStatus.UNUSED,
                claimChannel,
                businessNo,
                null,
                null,
                null,
                null,
                now,
                expireAt,
                null,
                now,
                now
        );
    }

    /**
     * 处理用于信息。
     */
    public void reserveForPayment(LocalDateTime now) {
        if (status == CouponIssueStatus.USED) {
            throw new IllegalStateException("coupon already used");
        }
        if (status == CouponIssueStatus.EXPIRED) {
            throw new IllegalStateException("coupon already expired");
        }
        if (now.isAfter(expireAt)) {
            status = CouponIssueStatus.EXPIRED;
            updatedAt = now;
            throw new IllegalStateException("coupon expired");
        }
        status = CouponIssueStatus.FROZEN;
        updatedAt = now;
    }

    /**
     * 处理业务数据。
     */
    public void releaseReservation(LocalDateTime now) {
        if (status == CouponIssueStatus.FROZEN) {
            status = CouponIssueStatus.UNUSED;
            updatedAt = now;
        }
    }

    /**
     * 处理业务数据。
     */
    public void consumeAfterReservation(String orderNo, LocalDateTime now, LocalDateTime useStartTime, LocalDateTime useEndTime) {
        if (status != CouponIssueStatus.FROZEN && status != CouponIssueStatus.UNUSED) {
            throw new IllegalStateException("coupon is not in consumable status");
        }
        redeem(orderNo, now, useStartTime, useEndTime);
    }

    /**
     * 处理业务数据。
     */
    public void consumeAfterReservation(String orderNo,
                                        String bizOrderNo,
                                        String tradeOrderNo,
                                        String payOrderNo,
                                        LocalDateTime now,
                                        LocalDateTime useStartTime,
                                        LocalDateTime useEndTime) {
        if (status != CouponIssueStatus.FROZEN && status != CouponIssueStatus.UNUSED) {
            throw new IllegalStateException("coupon is not in consumable status");
        }
        redeem(orderNo, bizOrderNo, tradeOrderNo, payOrderNo, now, useStartTime, useEndTime);
    }

    /**
     * 处理业务数据。
     */
    public void redeem(String orderNo, LocalDateTime now, LocalDateTime useStartTime, LocalDateTime useEndTime) {
        redeem(orderNo, null, null, null, now, useStartTime, useEndTime);
    }

    /**
     * 处理业务数据。
     */
    public void redeem(String orderNo,
                       String bizOrderNo,
                       String tradeOrderNo,
                       String payOrderNo,
                       LocalDateTime now,
                       LocalDateTime useStartTime,
                       LocalDateTime useEndTime) {
        String normalizedOrderNo = normalizeRequired(orderNo, "orderNo");
        String normalizedBizOrderNo = normalizeOptional(bizOrderNo);
        String normalizedTradeOrderNo = normalizeOptional(tradeOrderNo);
        String normalizedPayOrderNo = normalizeOptional(payOrderNo);
        if (status == CouponIssueStatus.USED) {
            if (normalizedOrderNo.equals(this.orderNo)
                    && sameOptional(normalizedBizOrderNo, this.bizOrderNo)
                    && sameOptional(normalizedTradeOrderNo, this.tradeOrderNo)
                    && sameOptional(normalizedPayOrderNo, this.payOrderNo)) {
                return;
            }
            throw new IllegalStateException("coupon already used");
        }
        if (status == CouponIssueStatus.FROZEN
                && normalizedOrderNo.equals(this.orderNo)
                && sameOptional(normalizedBizOrderNo, this.bizOrderNo)
                && sameOptional(normalizedTradeOrderNo, this.tradeOrderNo)
                && sameOptional(normalizedPayOrderNo, this.payOrderNo)) {
            return;
        }
        if (status == CouponIssueStatus.EXPIRED) {
            throw new IllegalStateException("coupon already expired");
        }

        if (now.isAfter(expireAt) || (useEndTime != null && now.isAfter(useEndTime))) {
            status = CouponIssueStatus.EXPIRED;
            this.updatedAt = now;
            throw new IllegalStateException("coupon expired");
        }
        if (useStartTime != null && now.isBefore(useStartTime)) {
            throw new IllegalStateException("coupon not in valid use window");
        }

        this.status = CouponIssueStatus.USED;
        this.orderNo = normalizedOrderNo;
        this.bizOrderNo = normalizedBizOrderNo;
        this.tradeOrderNo = normalizedTradeOrderNo;
        this.payOrderNo = normalizedPayOrderNo;
        this.usedAt = now;
        this.updatedAt = now;
    }

    /**
     * 获取ID。
     */
    public Long getIssueId() {
        return issueId;
    }

    /**
     * 获取优惠券NO信息。
     */
    public String getCouponNo() {
        return couponNo;
    }

    /**
     * 获取模板ID。
     */
    public Long getTemplateId() {
        return templateId;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取优惠券金额。
     */
    public Money getCouponAmount() {
        return couponAmount;
    }

    /**
     * 获取状态。
     */
    public CouponIssueStatus getStatus() {
        return status;
    }

    /**
     * 获取渠道信息。
     */
    public String getClaimChannel() {
        return claimChannel;
    }

    /**
     * 获取NO信息。
     */
    public String getBusinessNo() {
        return businessNo;
    }

    /**
     * 获取订单NO信息。
     */
    public String getOrderNo() {
        return orderNo;
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
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUsedAt() {
        return usedAt;
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

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
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

    private boolean sameOptional(String left, String right) {
        return normalizeOptional(left) == null
                ? normalizeOptional(right) == null
                : normalizeOptional(left).equals(normalizeOptional(right));
    }

    private Money normalizeAmount(Money value, String field) {
        if (value == null || value.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroMoney() {
        return Money.zero(CurrencyUnit.of("CNY"));
    }
}
