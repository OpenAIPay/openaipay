package cn.openaipay.domain.fundaccount.model;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 基金交易模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class FundTransaction {

    /** 业务订单号 */
    private final String orderNo;
    /** 用户ID */
    private final Long userId;
    /** 基金产品编码 */
    private final String fundCode;
    /** 交易类型 */
    private final FundTransactionType transactionType;
    /** 交易状态 */
    private FundTransactionStatus transactionStatus;
    /** 请求金额 */
    private final FundAmount requestAmount;
    /** 请求份额 */
    private final FundAmount requestShare;
    /** 确认金额 */
    private FundAmount confirmedAmount;
    /** 确认份额 */
    private FundAmount confirmedShare;
    /** 业务编号 */
    private final String businessNo;
    /** 扩展信息 */
    private final String extInfo;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public FundTransaction(String orderNo,
                           Long userId,
                           String fundCode,
                           FundTransactionType transactionType,
                           FundTransactionStatus transactionStatus,
                           FundAmount requestAmount,
                           FundAmount requestShare,
                           FundAmount confirmedAmount,
                           FundAmount confirmedShare,
                           String businessNo,
                           String extInfo,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.fundCode = fundCode;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.requestAmount = normalize(requestAmount);
        this.requestShare = normalize(requestShare);
        this.confirmedAmount = normalize(confirmedAmount);
        this.confirmedShare = normalize(confirmedShare);
        this.businessNo = businessNo;
        this.extInfo = extInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static FundTransaction pendingSubscribe(String orderNo,
                                                   Long userId,
                                                   String fundCode,
                                                   FundAmount requestAmount,
                                                   String businessNo,
                                                   LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                fundCode,
                FundTransactionType.SUBSCRIBE,
                FundTransactionStatus.PENDING,
                requestAmount,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                businessNo,
                null,
                now,
                now
        );
    }

    /**
     * 处理业务数据。
     */
    public static FundTransaction pendingRedeem(String orderNo,
                                                Long userId,
                                                String fundCode,
                                                FundAmount requestShare,
                                                String businessNo,
                                                String extInfo,
                                                LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                fundCode,
                FundTransactionType.REDEEM,
                FundTransactionStatus.PENDING,
                FundAmount.ZERO,
                requestShare,
                FundAmount.ZERO,
                FundAmount.ZERO,
                businessNo,
                extInfo,
                now,
                now
        );
    }

    /**
     * 处理业务数据。
     */
    public static FundTransaction pendingFastRedeem(String orderNo,
                                                    Long userId,
                                                    String fundCode,
                                                    FundAmount requestShare,
                                                    String businessNo,
                                                    LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                fundCode,
                FundTransactionType.FAST_REDEEM,
                FundTransactionStatus.PENDING,
                FundAmount.ZERO,
                requestShare,
                FundAmount.ZERO,
                FundAmount.ZERO,
                businessNo,
                "FAST",
                now,
                now
        );
    }

    /**
     * 处理业务数据。
     */
    public static FundTransaction pendingProductSwitch(String orderNo,
                                                       Long userId,
                                                       String sourceFundCode,
                                                       FundAmount sourceShare,
                                                       String targetFundCode,
                                                       String businessNo,
                                                       LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                sourceFundCode,
                FundTransactionType.PRODUCT_SWITCH,
                FundTransactionStatus.PENDING,
                FundAmount.ZERO,
                sourceShare,
                FundAmount.ZERO,
                FundAmount.ZERO,
                businessNo,
                targetFundCode,
                now,
                now
        );
    }

    /**
     * 处理业务数据。
     */
    public static FundTransaction pendingFreeze(String orderNo,
                                                Long userId,
                                                String fundCode,
                                                FundAmount requestAmount,
                                                FundAmount requestShare,
                                                String businessNo,
                                                String extInfo,
                                                LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                fundCode,
                FundTransactionType.FREEZE,
                FundTransactionStatus.PENDING,
                requestAmount,
                requestShare,
                FundAmount.ZERO,
                FundAmount.ZERO,
                businessNo,
                extInfo,
                now,
                now
        );
    }

    /**
     * 处理收益结算信息。
     */
    public static FundTransaction confirmedIncomeSettle(String orderNo,
                                                        Long userId,
                                                        String fundCode,
                                                        FundAmount incomeAmount,
                                                        String businessNo,
                                                        LocalDateTime now) {
        return new FundTransaction(
                orderNo,
                userId,
                fundCode,
                FundTransactionType.INCOME_SETTLE,
                FundTransactionStatus.CONFIRMED,
                incomeAmount,
                FundAmount.ZERO,
                incomeAmount,
                FundAmount.ZERO,
                businessNo,
                null,
                now,
                now
        );
    }

    /**
     * 获取订单NO信息。
     */
    public String getOrderNo() {
        return orderNo;
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
     * 获取业务数据。
     */
    public FundTransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * 获取状态。
     */
    public FundTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * 获取请求金额。
     */
    public FundAmount getRequestAmount() {
        return requestAmount;
    }

    /**
     * 获取请求份额信息。
     */
    public FundAmount getRequestShare() {
        return requestShare;
    }

    /**
     * 获取金额。
     */
    public FundAmount getConfirmedAmount() {
        return confirmedAmount;
    }

    /**
     * 获取份额信息。
     */
    public FundAmount getConfirmedShare() {
        return confirmedShare;
    }

    /**
     * 获取NO信息。
     */
    public String getBusinessNo() {
        return businessNo;
    }

    /**
     * 获取EXT信息。
     */
    public String getExtInfo() {
        return extInfo;
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
     * 标记业务数据。
     */
    public void markConfirmed(FundAmount confirmedAmount, FundAmount confirmedShare, LocalDateTime now) {
        if (transactionStatus == FundTransactionStatus.CANCELED
                || transactionStatus == FundTransactionStatus.COMPENSATED
                || transactionStatus == FundTransactionStatus.REJECTED) {
            throw new IllegalStateException("transaction has been canceled or rejected");
        }
        transactionStatus = FundTransactionStatus.CONFIRMED;
        this.confirmedAmount = normalize(confirmedAmount);
        this.confirmedShare = normalize(confirmedShare);
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markCanceled(LocalDateTime now) {
        if (transactionStatus == FundTransactionStatus.CONFIRMED) {
            throw new IllegalStateException("transaction has been confirmed");
        }
        transactionStatus = FundTransactionStatus.CANCELED;
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markCompensated(LocalDateTime now) {
        if (transactionStatus == FundTransactionStatus.CONFIRMED) {
            throw new IllegalStateException("transaction has been confirmed");
        }
        transactionStatus = FundTransactionStatus.COMPENSATED;
        this.updatedAt = now;
    }

    private FundAmount normalize(FundAmount value) {
        if (value == null) {
            return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
