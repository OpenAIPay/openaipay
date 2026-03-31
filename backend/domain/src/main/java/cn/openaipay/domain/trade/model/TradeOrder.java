package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 统一交易主单模型。
 *
 * 业务场景：交易系统负责交易信息流处理与支付编排，因此主单聚合只保存编排所需公共字段；
 * 爱花、爱借、爱存等业务特有字段下沉到独立扩展单表。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class TradeOrder {
    /** 主键ID。 */
    private final Long id;
    /** 统一交易主单号。 */
    private final String tradeOrderNo;
    /** 请求幂等号。 */
    private final String requestNo;
    /** 交易类型。 */
    private final TradeType tradeType;
    /** 业务场景编码：面向交易编排，例如 TRADE_TRANSFER、TRADE_PAY。 */
    private final String businessSceneCode;
    /** 业务域编码：面向业务查询，例如 AICREDIT、AILOAN、AICASH。 */
    private final String businessDomainCode;
    /** 业务交易单号：在业务域内唯一，用于业务单查询。 */
    private final String bizOrderNo;
    /** 原交易单号：退款等逆向交易场景使用。 */
    private final String originalTradeOrderNo;
    /** 付款方用户ID。 */
    private final Long payerUserId;
    /** 收款方用户ID。 */
    private final Long payeeUserId;
    /** 支付方式编码。 */
    private final String paymentMethod;
    /** 原始交易金额。 */
    private final Money originalAmount;
    /** 手续费金额。 */
    private Money feeAmount;
    /** 应付金额。 */
    private Money payableAmount;
    /** 结算金额。 */
    private Money settleAmount;
    /** 交易查询侧保留的支付拆分快照。 */
    private TradeSplitPlan splitPlan;
    /** 计费报价单号。 */
    private String pricingQuoteNo;
    /** 当前生效的支付模块支付单号。 */
    private String payOrderNo;
    /** 最近一次已应用的支付状态版本号。 */
    private int lastPayStatusVersion;
    /** 支付结果码。 */
    private String payResultCode;
    /** 支付结果描述。 */
    private String payResultMessage;
    /** 交易状态。 */
    private TradeStatus status;
    /** 失败原因。 */
    private String failureReason;
    /** 扩展信息快照。 */
    private final String metadata;
    /** 支付工具列表快照。 */
    private String paymentToolSnapshot;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public TradeOrder(Long id,
                      String tradeOrderNo,
                      String requestNo,
                      TradeType tradeType,
                      String businessSceneCode,
                      String businessDomainCode,
                      String bizOrderNo,
                      String originalTradeOrderNo,
                      Long payerUserId,
                      Long payeeUserId,
                      String paymentMethod,
                      Money originalAmount,
                      Money feeAmount,
                      Money payableAmount,
                      Money settleAmount,
                      TradeSplitPlan splitPlan,
                      String pricingQuoteNo,
                      String payOrderNo,
                      int lastPayStatusVersion,
                      String payResultCode,
                      String payResultMessage,
                      TradeStatus status,
                      String failureReason,
                      String metadata,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this(
                id,
                tradeOrderNo,
                requestNo,
                tradeType,
                businessSceneCode,
                businessDomainCode,
                bizOrderNo,
                originalTradeOrderNo,
                payerUserId,
                payeeUserId,
                paymentMethod,
                originalAmount,
                feeAmount,
                payableAmount,
                settleAmount,
                splitPlan,
                pricingQuoteNo,
                payOrderNo,
                lastPayStatusVersion,
                payResultCode,
                payResultMessage,
                status,
                failureReason,
                metadata,
                null,
                createdAt,
                updatedAt
        );
    }

    public TradeOrder(Long id,
                      String tradeOrderNo,
                      String requestNo,
                      TradeType tradeType,
                      String businessSceneCode,
                      String businessDomainCode,
                      String bizOrderNo,
                      String originalTradeOrderNo,
                      Long payerUserId,
                      Long payeeUserId,
                      String paymentMethod,
                      Money originalAmount,
                      Money feeAmount,
                      Money payableAmount,
                      Money settleAmount,
                      TradeSplitPlan splitPlan,
                      String pricingQuoteNo,
                      String payOrderNo,
                      int lastPayStatusVersion,
                      String payResultCode,
                      String payResultMessage,
                      TradeStatus status,
                      String failureReason,
                      String metadata,
                      String paymentToolSnapshot,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.tradeOrderNo = normalizeRequired(tradeOrderNo, "tradeOrderNo");
        this.requestNo = normalizeRequired(requestNo, "requestNo");
        this.tradeType = tradeType == null ? TradeType.PAY : tradeType;
        this.businessSceneCode = normalizeRequired(businessSceneCode, "businessSceneCode");
        this.paymentMethod = normalizeRequired(paymentMethod, "paymentMethod");
        this.businessDomainCode = resolveBusinessDomainCode(businessDomainCode, this.businessSceneCode, this.paymentMethod);
        this.bizOrderNo = resolveBizOrderNo(bizOrderNo, this.tradeOrderNo);
        this.originalTradeOrderNo = normalizeOptional(originalTradeOrderNo);
        this.payerUserId = requirePositive(payerUserId, "payerUserId");
        this.payeeUserId = payeeUserId == null ? null : requirePositive(payeeUserId, "payeeUserId");
        this.originalAmount = normalizePositive(originalAmount, "originalAmount");
        CurrencyUnit currencyUnit = this.originalAmount.getCurrencyUnit();
        this.feeAmount = normalizeNonNegative(feeAmount, "feeAmount", currencyUnit);
        this.payableAmount = normalizeNonNegative(payableAmount, "payableAmount", currencyUnit);
        this.settleAmount = normalizeNonNegative(settleAmount, "settleAmount", currencyUnit);
        this.splitPlan = normalizeSplitPlan(splitPlan, currencyUnit);
        this.pricingQuoteNo = normalizeOptional(pricingQuoteNo);
        this.payOrderNo = normalizeOptional(payOrderNo);
        if (lastPayStatusVersion < 0) {
            throw new IllegalArgumentException("lastPayStatusVersion must be greater than or equal to 0");
        }
        this.lastPayStatusVersion = lastPayStatusVersion;
        this.payResultCode = normalizeOptional(payResultCode);
        this.payResultMessage = normalizeOptional(payResultMessage);
        this.status = status == null ? TradeStatus.CREATED : status;
        this.failureReason = normalizeOptional(failureReason);
        this.metadata = normalizeOptional(metadata);
        this.paymentToolSnapshot = normalizeOptional(paymentToolSnapshot);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;

        if (requiresSubmittedSplitPlanValidation()
                && this.splitPlan.totalDebitAmount().compareTo(this.payableAmount) > 0) {
            throw new IllegalArgumentException("participant split amount must equal payableAmount");
        }
    }

    /**
     * 使用默认业务域策略创建交易主单。
     *
     * 业务场景：统一交易入口未显式指定业务域时按场景和支付方式自动推断。
     */
    public static TradeOrder create(String tradeOrderNo,
                                    String requestNo,
                                    TradeType tradeType,
                                    String businessSceneCode,
                                    String originalTradeOrderNo,
                                    Long payerUserId,
                                    Long payeeUserId,
                                    String paymentMethod,
                                    Money originalAmount,
                                    String metadata,
                                    LocalDateTime now) {
        return create(
                tradeOrderNo,
                requestNo,
                tradeType,
                businessSceneCode,
                null,
                null,
                originalTradeOrderNo,
                payerUserId,
                payeeUserId,
                paymentMethod,
                originalAmount,
                metadata,
                now
        );
    }

    /**
     * 使用显式业务域和业务单号创建交易主单。
     *
     * 业务场景：爱花还款、爱借借款、爱存申赎等业务可在创建交易时直接绑定业务查询主键。
     */
    public static TradeOrder create(String tradeOrderNo,
                                    String requestNo,
                                    TradeType tradeType,
                                    String businessSceneCode,
                                    String businessDomainCode,
                                    String bizOrderNo,
                                    String originalTradeOrderNo,
                                    Long payerUserId,
                                    Long payeeUserId,
                                    String paymentMethod,
                                    Money originalAmount,
                                    String metadata,
                                    LocalDateTime now) {
        LocalDateTime created = now == null ? LocalDateTime.now() : now;
        CurrencyUnit currency = resolveCurrencyUnit(originalAmount);
        return new TradeOrder(
                null,
                tradeOrderNo,
                requestNo,
                tradeType,
                businessSceneCode,
                businessDomainCode,
                bizOrderNo,
                originalTradeOrderNo,
                payerUserId,
                payeeUserId,
                paymentMethod,
                originalAmount,
                Money.zero(currency),
                Money.zero(currency),
                Money.zero(currency),
                TradeSplitPlan.empty(currency),
                null,
                null,
                0,
                null,
                null,
                TradeStatus.CREATED,
                null,
                metadata,
                created,
                created
        );
    }

    /**
     * 标记计费信息。
     */
    public void markPricingQuoteApplied(String pricingQuoteNo,
                                        Money feeAmount,
                                        Money payableAmount,
                                        Money settleAmount,
                                        LocalDateTime now) {
        this.pricingQuoteNo = normalizeRequired(pricingQuoteNo, "pricingQuoteNo");
        this.feeAmount = normalizeNonNegative(feeAmount, "feeAmount");
        this.payableAmount = normalizePositive(payableAmount, "payableAmount");
        this.settleAmount = normalizeNonNegative(settleAmount, "settleAmount");
        this.failureReason = null;
        this.status = TradeStatus.QUOTED;
        touch(now);
    }

    /**
     * 标记支付信息。
     */
    public void markPaySubmitted(String payOrderNo,
                                 TradeSplitPlan splitPlan,
                                 LocalDateTime now) {
        this.payOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        this.splitPlan = normalizeSplitPlan(splitPlan, this.originalAmount.getCurrencyUnit());
        // Trade order payableAmount is pre-coupon quote amount.
        // When coupon applies, submitted participant debit can be lower than trade payable,
        // but it must never exceed trade payable.
        if (this.splitPlan.totalDebitAmount().compareTo(this.payableAmount) > 0) {
            throw new IllegalArgumentException("participant split amount must equal payableAmount");
        }
        this.lastPayStatusVersion = 0;
        this.payResultCode = null;
        this.payResultMessage = null;
        this.failureReason = null;
        this.status = TradeStatus.PAY_SUBMITTED;
        touch(now);
    }

    /**
     * 标记支付信息。
     */
    public void markPayProcessing(Integer payStatusVersion,
                                  String payResultCode,
                                  String payResultMessage,
                                  LocalDateTime now) {
        applyPayResultMetadata(payStatusVersion, payResultCode, payResultMessage);
        this.failureReason = null;
        this.status = TradeStatus.PAY_PROCESSING;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markSucceeded(LocalDateTime now) {
        this.status = TradeStatus.SUCCEEDED;
        this.failureReason = null;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markReconPending(String reason,
                                 Integer payStatusVersion,
                                 String payResultCode,
                                 String payResultMessage,
                                 LocalDateTime now) {
        applyPayResultMetadata(payStatusVersion, payResultCode, payResultMessage);
        this.status = TradeStatus.RECON_PENDING;
        this.failureReason = normalizeOptional(reason);
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markRolledBack(String reason, LocalDateTime now) {
        this.status = TradeStatus.ROLLED_BACK;
        this.failureReason = normalizeOptional(reason);
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markFailed(String reason, LocalDateTime now) {
        this.status = TradeStatus.FAILED;
        this.failureReason = normalizeOptional(reason);
        touch(now);
    }

    /**
     * 判断是否应支付结果。
     */
    public boolean shouldApplyPayResult(Integer payStatusVersion) {
        if (payStatusVersion == null) {
            return true;
        }
        return payStatusVersion > this.lastPayStatusVersion;
    }

    /**
     * 记录支付结果。
     */
    public void recordPayResult(Integer payStatusVersion,
                                String payResultCode,
                                String payResultMessage,
                                LocalDateTime now) {
        applyPayResultMetadata(payStatusVersion, payResultCode, payResultMessage);
        touch(now);
    }

    /**
     * 刷新支付工具列表快照。
     */
    public void refreshPaymentToolSnapshot(String snapshot, LocalDateTime now) {
        this.paymentToolSnapshot = normalizeOptional(snapshot);
        touch(now);
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isTerminal() {
        return this.status == TradeStatus.SUCCEEDED
                || this.status == TradeStatus.ROLLED_BACK
                || this.status == TradeStatus.FAILED;
    }

    private void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private void applyPayResultMetadata(Integer payStatusVersion,
                                        String payResultCode,
                                        String payResultMessage) {
        if (payStatusVersion != null) {
            if (payStatusVersion < this.lastPayStatusVersion) {
                throw new IllegalArgumentException("payStatusVersion must not move backwards");
            }
            this.lastPayStatusVersion = payStatusVersion;
        }
        this.payResultCode = normalizeOptional(payResultCode);
        this.payResultMessage = normalizeOptional(payResultMessage);
    }

    private boolean requiresSubmittedSplitPlanValidation() {
        if (this.payOrderNo != null) {
            return true;
        }
        // 爱存申赎等快照型交易不会走支付提交流程，允许保留空拆分计划。
        return this.splitPlan.totalDebitAmount().compareTo(zeroOf(this.splitPlan.totalDebitAmount())) > 0;
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

    private TradeSplitPlan normalizeSplitPlan(TradeSplitPlan plan, CurrencyUnit currencyUnit) {
        if (plan == null) {
            return TradeSplitPlan.empty(currencyUnit);
        }
        return TradeSplitPlan.of(
                currencyUnit,
                plan.getWalletDebitAmount(),
                plan.getFundDebitAmount(),
                plan.getCreditDebitAmount(),
                plan.getInboundDebitAmount()
        );
    }

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private Money normalizePositive(Money value, String field) {
        if (value == null || value.compareTo(zeroOf(value)) <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String field) {
        if (value == null) {
            return Money.zero(this.originalAmount.getCurrencyUnit()).rounded(2, RoundingMode.HALF_UP);
        }
        if (value.compareTo(zeroOf(value)) < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegative(Money value, String field, CurrencyUnit currencyUnit) {
        if (value == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!value.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException(field + " currency must equal originalAmount currency");
        }
        if (value.compareTo(zeroOf(value)) < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private String resolveBusinessDomainCode(String rawBusinessDomainCode,
                                             String resolvedBusinessSceneCode,
                                             String resolvedPaymentMethod) {
        String normalized = normalizeOptional(rawBusinessDomainCode);
        if (normalized != null) {
            return TradeBusinessDomainCode.from(normalized).name();
        }
        return TradeBusinessDomainCode.detect(resolvedBusinessSceneCode, resolvedPaymentMethod).name();
    }

    private String resolveBizOrderNo(String rawBizOrderNo, String resolvedTradeOrderNo) {
        String normalized = normalizeOptional(rawBizOrderNo);
        return normalized == null ? resolvedTradeOrderNo : normalized;
    }

    private Money zeroOf(Money value) {
        return Money.zero(value.getCurrencyUnit());
    }

    private static CurrencyUnit resolveCurrencyUnit(Money value) {
        if (value == null) {
            return CurrencyUnit.of("CNY");
        }
        return value.getCurrencyUnit();
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取请求NO信息。
     */
    public String getRequestNo() {
        return requestNo;
    }

    /**
     * 获取交易类型信息。
     */
    public TradeType getTradeType() {
        return tradeType;
    }

    /**
     * 获取场景编码。
     */
    public String getBusinessSceneCode() {
        return businessSceneCode;
    }

    /**
     * 获取领域编码。
     */
    public String getBusinessDomainCode() {
        return businessDomainCode;
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
    public String getOriginalTradeOrderNo() {
        return originalTradeOrderNo;
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
     * 获取计划信息。
     */
    public TradeSplitPlan getSplitPlan() {
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
     * 获取计费NO信息。
     */
    public String getPricingQuoteNo() {
        return pricingQuoteNo;
    }

    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取当前支付订单NO信息。
     */
    public String getCurrentPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取支付状态版本信息。
     */
    public int getLastPayStatusVersion() {
        return lastPayStatusVersion;
    }

    /**
     * 获取支付结果编码。
     */
    public String getPayResultCode() {
        return payResultCode;
    }

    /**
     * 获取支付结果消息信息。
     */
    public String getPayResultMessage() {
        return payResultMessage;
    }

    /**
     * 获取状态。
     */
    public TradeStatus getStatus() {
        return status;
    }

    /**
     * 获取业务数据。
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * 获取业务数据。
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * 获取支付工具列表快照。
     */
    public String getPaymentToolSnapshot() {
        return paymentToolSnapshot;
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
}
