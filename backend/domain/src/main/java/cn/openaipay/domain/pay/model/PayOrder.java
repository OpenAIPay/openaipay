package cn.openaipay.domain.pay.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 支付订单模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PayOrder {

    /** 数据库主键ID */
    private final Long id;
    /** 支付单号 */
    private final String payOrderNo;
    /** 交易单号 */
    private final String tradeOrderNo;
    /** 业务订单编号 */
    private final String bizOrderNo;
    /** 来源业务类型 */
    private final String sourceBizType;
    /** 来源业务单号 */
    private final String sourceBizNo;
    /** 来源业务内支付尝试序号（从1开始递增） */
    private final int attemptNo;
    /** 来源业务执行快照 */
    private final String sourceBizSnapshot;
    /** 业务场景编码 */
    private final String businessSceneCode;
    /** 付款方用户ID */
    private final Long payerUserId;
    /** 收款方用户ID */
    private final Long payeeUserId;
    /** 原始金额 */
    private final Money originalAmount;
    /** 优惠金额 */
    private final Money discountAmount;
    /** 应付金额 */
    private final Money payableAmount;
    /** 实付金额 */
    private Money actualPaidAmount;
    /** 参与方扣款拆分计划 */
    private final PaySplitPlan splitPlan;
    /** 券编号 */
    private final String couponNo;
    /** 结算计划快照载荷 */
    private final String settlementPlanSnapshot;
    /** 全局事务号 */
    private final String globalTxId;
    /** 业务状态值 */
    private PayOrderStatus status;
    /** 支付状态版本号 */
    private int statusVersion;
    /** 支付结果码 */
    private String resultCode;
    /** 支付结果描述 */
    private String resultMessage;
    /** 失败原因描述 */
    private String failureReason;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public PayOrder(Long id,
                    String payOrderNo,
                    String tradeOrderNo,
                    String bizOrderNo,
                    String sourceBizType,
                    String sourceBizNo,
                    int attemptNo,
                    String sourceBizSnapshot,
                    String businessSceneCode,
                    Long payerUserId,
                    Long payeeUserId,
                    Money originalAmount,
                    Money discountAmount,
                    Money payableAmount,
                    Money actualPaidAmount,
                    PaySplitPlan splitPlan,
                    String couponNo,
                    String settlementPlanSnapshot,
                    String globalTxId,
                    PayOrderStatus status,
                    int statusVersion,
                    String resultCode,
                    String resultMessage,
                    String failureReason,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.payOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        this.sourceBizType = normalizeRequired(sourceBizType, "sourceBizType");
        this.sourceBizNo = normalizeRequired(sourceBizNo, "sourceBizNo");
        this.tradeOrderNo = resolveTradeOrderNo(tradeOrderNo, this.sourceBizType, this.sourceBizNo);
        this.bizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        if (attemptNo <= 0) {
            throw new IllegalArgumentException("attemptNo must be greater than 0");
        }
        this.attemptNo = attemptNo;
        this.sourceBizSnapshot = normalizeOptional(sourceBizSnapshot);
        this.businessSceneCode = normalizeRequired(businessSceneCode, "businessSceneCode");
        this.payerUserId = requirePositive(payerUserId, "payerUserId");
        this.payeeUserId = payeeUserId == null ? null : requirePositive(payeeUserId, "payeeUserId");
        this.originalAmount = normalizeAmount(originalAmount, "originalAmount");
        CurrencyUnit currencyUnit = this.originalAmount.getCurrencyUnit();
        this.discountAmount = normalizeNonNegative(discountAmount, "discountAmount", currencyUnit);
        this.payableAmount = normalizeNonNegative(payableAmount, "payableAmount", currencyUnit);
        this.actualPaidAmount = normalizeNonNegative(actualPaidAmount, "actualPaidAmount", currencyUnit);
        this.splitPlan = normalizeSplitPlan(splitPlan, currencyUnit);
        this.couponNo = normalizeOptional(couponNo);
        this.settlementPlanSnapshot = normalizeOptional(settlementPlanSnapshot);
        this.globalTxId = normalizeRequired(globalTxId, "globalTxId");
        this.status = status == null ? PayOrderStatus.CREATED : status;
        if (statusVersion < 0) {
            throw new IllegalArgumentException("statusVersion must be greater than or equal to 0");
        }
        this.statusVersion = statusVersion;
        this.resultCode = normalizeOptional(resultCode);
        this.resultMessage = normalizeOptional(resultMessage);
        this.failureReason = normalizeOptional(failureReason);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;

        if (this.discountAmount.compareTo(this.originalAmount) > 0) {
            throw new IllegalArgumentException("discountAmount can not exceed originalAmount");
        }
        Money participantTotal = this.splitPlan.totalDebitAmount();
        if (participantTotal.compareTo(this.payableAmount) != 0) {
            throw new IllegalArgumentException("sum of participant debit amounts must equal payableAmount");
        }
    }

    /**
     * 创建业务数据。
     */
    public static PayOrder create(String payOrderNo,
                                  String tradeOrderNo,
                                  String bizOrderNo,
                                  String businessSceneCode,
                                  Long payerUserId,
                                  Long payeeUserId,
                                  Money originalAmount,
                                  Money discountAmount,
                                  Money payableAmount,
                                  PaySplitPlan splitPlan,
                                  String couponNo,
                                  String globalTxId,
                                  LocalDateTime now) {
        CurrencyUnit currencyUnit = resolveCurrencyUnit(originalAmount);
        return new PayOrder(
                null,
                payOrderNo,
                tradeOrderNo,
                bizOrderNo,
                "MERCHANT_ORDER",
                bizOrderNo,
                1,
                null,
                businessSceneCode,
                payerUserId,
                payeeUserId,
                originalAmount,
                discountAmount,
                payableAmount,
                Money.zero(currencyUnit),
                splitPlan,
                couponNo,
                null,
                globalTxId,
                PayOrderStatus.CREATED,
                0,
                null,
                null,
                null,
                now,
                now
        );
    }

    /**
     * 创建业务数据。
     */
    public static PayOrder createSubmitted(String payOrderNo,
                                           String tradeOrderNo,
                                           String bizOrderNo,
                                           String sourceBizType,
                                           String sourceBizNo,
                                           int attemptNo,
                                           String sourceBizSnapshot,
                                           String businessSceneCode,
                                           Long payerUserId,
                                           Long payeeUserId,
                                           Money originalAmount,
                                           Money discountAmount,
                                           Money payableAmount,
                                           PaySplitPlan splitPlan,
                                           String couponNo,
                                           String settlementPlanSnapshot,
                                           String globalTxId,
                                           LocalDateTime now) {
        CurrencyUnit currencyUnit = resolveCurrencyUnit(originalAmount);
        return new PayOrder(
                null,
                payOrderNo,
                tradeOrderNo,
                bizOrderNo,
                sourceBizType,
                sourceBizNo,
                attemptNo,
                sourceBizSnapshot,
                businessSceneCode,
                payerUserId,
                payeeUserId,
                originalAmount,
                discountAmount,
                payableAmount,
                Money.zero(currencyUnit),
                splitPlan,
                couponNo,
                settlementPlanSnapshot,
                globalTxId,
                PayOrderStatus.SUBMITTED,
                1,
                "SUBMITTED",
                "支付请求已提交",
                null,
                now,
                now
        );
    }

    /**
     * 标记业务数据。
     */
    public void markTrying(LocalDateTime now) {
        this.status = PayOrderStatus.TRYING;
        this.failureReason = null;
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markPrepared(LocalDateTime now) {
        this.status = PayOrderStatus.PREPARED;
        this.failureReason = null;
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markCommitting(LocalDateTime now) {
        this.status = PayOrderStatus.COMMITTING;
        this.failureReason = null;
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markCommitted(LocalDateTime now) {
        this.status = PayOrderStatus.COMMITTED;
        this.actualPaidAmount = payableAmount;
        this.failureReason = null;
        this.resultCode = "SUCCESS";
        this.resultMessage = "支付成功";
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markReconPending(String reason, LocalDateTime now) {
        this.status = PayOrderStatus.RECON_PENDING;
        this.failureReason = normalizeOptional(reason);
        this.resultCode = "RECON_PENDING";
        this.resultMessage = normalizeOptional(reason);
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markReconPendingAfterActualPaid(String reason, LocalDateTime now) {
        this.actualPaidAmount = payableAmount;
        markReconPending(reason, now);
    }

    /**
     * 标记业务数据。
     */
    public void markRollingBack(LocalDateTime now) {
        this.status = PayOrderStatus.ROLLING_BACK;
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markRolledBack(String reason, LocalDateTime now) {
        this.status = PayOrderStatus.ROLLED_BACK;
        this.failureReason = normalizeOptional(reason);
        this.resultCode = "ROLLED_BACK";
        this.resultMessage = normalizeOptional(reason);
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markFailed(String reason, LocalDateTime now) {
        this.status = PayOrderStatus.FAILED;
        this.failureReason = normalizeOptional(reason);
        this.resultCode = "FAILED";
        this.resultMessage = normalizeOptional(reason);
        advanceStatusVersion();
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markFailedAfterActualPaid(String reason, LocalDateTime now) {
        this.actualPaidAmount = payableAmount;
        markFailed(reason, now);
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isTerminal() {
        return status == PayOrderStatus.COMMITTED
                || status == PayOrderStatus.ROLLED_BACK
                || status == PayOrderStatus.FAILED;
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
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取业务订单NO信息。
     */
    public String getBizOrderNo() {
        return bizOrderNo;
    }

    /**
     * 获取业务信息。
     */
    public String getSourceBizType() {
        return sourceBizType;
    }

    /**
     * 获取业务NO信息。
     */
    public String getSourceBizNo() {
        return sourceBizNo;
    }

    /**
     * 获取NO信息。
     */
    public int getAttemptNo() {
        return attemptNo;
    }

    /**
     * 获取业务快照信息。
     */
    public String getSourceBizSnapshot() {
        return sourceBizSnapshot;
    }

    /**
     * 获取场景编码。
     */
    public String getBusinessSceneCode() {
        return businessSceneCode;
    }

    /**
     * 获取付款方用户ID。
     */
    public Long getPayerUserId() {
        return payerUserId;
    }

    /**
     * 获取收款方用户ID。
     */
    public Long getPayeeUserId() {
        return payeeUserId;
    }

    /**
     * 获取金额。
     */
    public Money getOriginalAmount() {
        return originalAmount;
    }

    /**
     * 获取金额。
     */
    public Money getDiscountAmount() {
        return discountAmount;
    }

    /**
     * 获取金额。
     */
    public Money getPayableAmount() {
        return payableAmount;
    }

    /**
     * 获取金额。
     */
    public Money getActualPaidAmount() {
        return actualPaidAmount;
    }

    /**
     * 获取计划信息。
     */
    public PaySplitPlan getSplitPlan() {
        return splitPlan;
    }

    /**
     * 获取钱包金额。
     */
    public Money getWalletDebitAmount() {
        return splitPlan.getWalletDebitAmount();
    }

    /**
     * 获取基金金额。
     */
    public Money getFundDebitAmount() {
        return splitPlan.getFundDebitAmount();
    }

    /**
     * 获取信用金额。
     */
    public Money getCreditDebitAmount() {
        return splitPlan.getCreditDebitAmount();
    }

    /**
     * 获取入金金额。
     */
    public Money getInboundDebitAmount() {
        return splitPlan.getInboundDebitAmount();
    }

    /**
     * 获取优惠券NO信息。
     */
    public String getCouponNo() {
        return couponNo;
    }

    /**
     * 获取计划快照信息。
     */
    public String getSettlementPlanSnapshot() {
        return settlementPlanSnapshot;
    }

    /**
     * 获取TXID。
     */
    public String getGlobalTxId() {
        return globalTxId;
    }

    /**
     * 获取状态。
     */
    public PayOrderStatus getStatus() {
        return status;
    }

    /**
     * 获取状态版本信息。
     */
    public int getStatusVersion() {
        return statusVersion;
    }

    /**
     * 获取结果编码。
     */
    public String getResultCode() {
        return resultCode;
    }

    /**
     * 获取结果消息信息。
     */
    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * 获取业务数据。
     */
    public String getFailureReason() {
        return failureReason;
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

    private void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private void advanceStatusVersion() {
        this.statusVersion = this.statusVersion + 1;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
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

    private String resolveTradeOrderNo(String tradeOrderNo, String sourceBizType, String sourceBizNo) {
        String normalizedTradeOrderNo = normalizeOptional(tradeOrderNo);
        if (normalizedTradeOrderNo != null) {
            return normalizedTradeOrderNo;
        }
        if ("TRADE".equalsIgnoreCase(normalizeOptional(sourceBizType))) {
            return normalizeOptional(sourceBizNo);
        }
        return null;
    }

    private PaySplitPlan normalizeSplitPlan(PaySplitPlan plan, CurrencyUnit currencyUnit) {
        if (plan == null) {
            return PaySplitPlan.empty(currencyUnit);
        }
        return PaySplitPlan.of(
                currencyUnit,
                plan.getWalletDebitAmount(),
                plan.getFundDebitAmount(),
                plan.getCreditDebitAmount(),
                plan.getInboundDebitAmount()
        );
    }

    private Money normalizeAmount(Money amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        Money normalized = amount.rounded(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(zeroOf(normalized)) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return normalized;
    }

    private Money normalizeNonNegative(Money amount, String fieldName, CurrencyUnit currencyUnit) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        if (!amount.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(fieldName + " currency must equal originalAmount currency");
        }
        if (amount.compareTo(zeroOf(amount)) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroOf(Money amount) {
        return Money.zero(amount.getCurrencyUnit());
    }

    private static CurrencyUnit resolveCurrencyUnit(Money amount) {
        if (amount == null) {
            return CurrencyUnit.of("CNY");
        }
        return amount.getCurrencyUnit();
    }
}
