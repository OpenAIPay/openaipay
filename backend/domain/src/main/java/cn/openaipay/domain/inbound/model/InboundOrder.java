package cn.openaipay.domain.inbound.model;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

/**
 * 入金订单模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class InboundOrder {
    /** 默认币种。 */
    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");
    /** 主键ID。 */
    private final Long id;
    /** 入金流水号。 */
    private final String inboundId;
    /** 机构号。 */
    private String instId;
    /** 金融交换码。 */
    private String instChannelCode;
    /** 入金订单号。 */
    private String inboundOrderNo;
    /** 付款账户号。 */
    private final String payerAccountNo;
    /** 入金金额。 */
    private final Money inboundAmount;
    /** 入账金额。 */
    private final Money accountAmount;
    /** 清算金额。 */
    private final Money settleAmount;
    /** 入金状态。 */
    private InboundStatus inboundStatus;
    /** 结果码。 */
    private String resultCode;
    /** 结果描述。 */
    private String resultDescription;
    /** 请求标识。 */
    private final String requestIdentify;
    /** 业务请求号。 */
    private final String requestBizNo;
    /** 业务单号。 */
    private final String bizOrderNo;
    /** 交易单号。 */
    private final String tradeOrderNo;
    /** 支付单号。 */
    private final String payOrderNo;
    /** 支付渠道编码。 */
    private final String payChannelCode;
    /** 业务身份标识。 */
    private final String bizIdentity;
    /** 提交时间。 */
    private LocalDateTime gmtSubmit;
    /** 响应时间。 */
    private LocalDateTime gmtResp;
    /** 清算完成时间。 */
    private LocalDateTime gmtSettle;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public InboundOrder(Long id,
                             String inboundId,
                             String instId,
                             String instChannelCode,
                             String inboundOrderNo,
                             String payerAccountNo,
                             Money inboundAmount,
                             Money accountAmount,
                             Money settleAmount,
                             InboundStatus inboundStatus,
                             String resultCode,
                             String resultDescription,
                             String requestIdentify,
                             String requestBizNo,
                             String bizOrderNo,
                             String tradeOrderNo,
                             String payOrderNo,
                             String payChannelCode,
                             String bizIdentity,
                             LocalDateTime gmtSubmit,
                             LocalDateTime gmtResp,
                             LocalDateTime gmtSettle,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.inboundId = normalizeRequired(inboundId, "inboundId");
        this.instId = normalizeOptional(instId);
        this.instChannelCode = normalizeOptional(instChannelCode);
        this.inboundOrderNo = normalizeOptional(inboundOrderNo);
        this.payerAccountNo = normalizeRequired(payerAccountNo, "payerAccountNo");
        this.inboundAmount = normalizePositiveMoney(inboundAmount, "inboundAmount");
        this.accountAmount = normalizeNonNegativeMoney(accountAmount, "accountAmount");
        this.settleAmount = normalizeNonNegativeMoney(settleAmount, "settleAmount");
        this.inboundStatus = inboundStatus == null ? InboundStatus.INIT : inboundStatus;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.requestIdentify = normalizeRequired(requestIdentify, "requestIdentify");
        this.requestBizNo = normalizeRequired(requestBizNo, "requestBizNo");
        this.bizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        this.tradeOrderNo = normalizeOptional(tradeOrderNo);
        this.payOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        this.payChannelCode = normalizeRequired(payChannelCode, "payChannelCode");
        this.bizIdentity = normalizeRequired(bizIdentity, "bizIdentity");
        this.gmtSubmit = gmtSubmit;
        this.gmtResp = gmtResp;
        this.gmtSettle = gmtSettle;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建用于信息。
     */
    public static InboundOrder createForDeposit(String inboundId,
                                                     String instId,
                                                     String instChannelCode,
                                                     String inboundOrderNo,
                                                     String payerAccountNo,
                                                     Money inboundAmount,
                                                     Money accountAmount,
                                                     Money settleAmount,
                                                     String requestIdentify,
                                                     String requestBizNo,
                                                     String bizOrderNo,
                                                     String tradeOrderNo,
                                                     String payOrderNo,
                                                     String payChannelCode,
                                                     String bizIdentity,
                                                     LocalDateTime now) {
        LocalDateTime created = now == null ? LocalDateTime.now() : now;
        return new InboundOrder(
                null,
                inboundId,
                instId,
                instChannelCode,
                inboundOrderNo,
                payerAccountNo,
                inboundAmount,
                accountAmount,
                settleAmount,
                InboundStatus.INIT,
                "INIT",
                "受理中",
                requestIdentify,
                requestBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                payChannelCode,
                bizIdentity,
                created,
                null,
                null,
                created,
                created
        );
    }

    /**
     * 标记业务数据。
     */
    public void markSubmitted(LocalDateTime now) {
        this.inboundStatus = InboundStatus.SUBMITTED;
        this.resultCode = "SUBMITTED";
        this.resultDescription = "已提交网关";
        LocalDateTime submitAt = now == null ? LocalDateTime.now() : now;
        if (this.gmtSubmit == null) {
            this.gmtSubmit = submitAt;
        }
        touch(submitAt);
    }

    /**
     * 标记业务数据。
     */
    public void markAccepted(String resultCode,
                             String resultDescription,
                             String instId,
                             String instChannelCode,
                             LocalDateTime responseAt,
                             LocalDateTime now) {
        this.inboundStatus = InboundStatus.ACCEPTED;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.instId = normalizeOptional(instId) == null ? this.instId : normalizeOptional(instId);
        this.instChannelCode = normalizeOptional(instChannelCode) == null
                ? this.instChannelCode
                : normalizeOptional(instChannelCode);
        this.gmtResp = responseAt == null ? LocalDateTime.now() : responseAt;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markSucceeded(String inboundOrderNo,
                              String resultCode,
                              String resultDescription,
                              LocalDateTime settleAt,
                              LocalDateTime now) {
        this.inboundStatus = InboundStatus.SUCCEEDED;
        this.inboundOrderNo = normalizeOptional(inboundOrderNo);
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        LocalDateTime settledAt = settleAt == null ? LocalDateTime.now() : settleAt;
        this.gmtSettle = settledAt;
        if (this.gmtResp == null) {
            this.gmtResp = settledAt;
        }
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markFailed(String resultCode,
                           String resultDescription,
                           LocalDateTime responseAt,
                           LocalDateTime now) {
        this.inboundStatus = InboundStatus.FAILED;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.gmtResp = responseAt == null ? LocalDateTime.now() : responseAt;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markReconPending(String resultCode,
                                 String resultDescription,
                                 LocalDateTime responseAt,
                                 LocalDateTime now) {
        this.inboundStatus = InboundStatus.RECON_PENDING;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.gmtResp = responseAt == null ? LocalDateTime.now() : responseAt;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markCanceled(String resultCode,
                             String resultDescription,
                             LocalDateTime responseAt,
                             LocalDateTime now) {
        this.inboundStatus = InboundStatus.CANCELED;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.gmtResp = responseAt == null ? LocalDateTime.now() : responseAt;
        touch(now);
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isTerminal() {
        return this.inboundStatus == InboundStatus.SUCCEEDED
                || this.inboundStatus == InboundStatus.FAILED
                || this.inboundStatus == InboundStatus.CANCELED;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取入金ID。
     */
    public String getInboundId() {
        return inboundId;
    }

    /**
     * 获取ID。
     */
    public String getInstId() {
        return instId;
    }

    /**
     * 获取渠道编码。
     */
    public String getInstChannelCode() {
        return instChannelCode;
    }

    /**
     * 获取入金订单NO信息。
     */
    public String getInboundOrderNo() {
        return inboundOrderNo;
    }

    /**
     * 获取付款方账户NO信息。
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * 获取入金金额。
     */
    public Money getInboundAmount() {
        return inboundAmount;
    }

    /**
     * 获取账户金额。
     */
    public Money getAccountAmount() {
        return accountAmount;
    }

    /**
     * 获取结算金额。
     */
    public Money getSettleAmount() {
        return settleAmount;
    }


    /**
     * 获取入金状态。
     */
    public InboundStatus getInboundStatus() {
        return inboundStatus;
    }

    /**
     * 获取结果编码。
     */
    public String getResultCode() {
        return resultCode;
    }

    /**
     * 获取结果。
     */
    public String getResultDescription() {
        return resultDescription;
    }

    /**
     * 获取请求。
     */
    public String getRequestIdentify() {
        return requestIdentify;
    }

    /**
     * 获取请求业务NO信息。
     */
    public String getRequestBizNo() {
        return requestBizNo;
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
     * 获取支付渠道编码。
     */
    public String getPayChannelCode() {
        return payChannelCode;
    }

    /**
     * 获取业务信息。
     */
    public String getBizIdentity() {
        return bizIdentity;
    }

    /**
     * 获取GMT信息。
     */
    public LocalDateTime getGmtSubmit() {
        return gmtSubmit;
    }

    /**
     * 获取GMT信息。
     */
    public LocalDateTime getGmtResp() {
        return gmtResp;
    }

    /**
     * 获取GMT结算信息。
     */
    public LocalDateTime getGmtSettle() {
        return gmtSettle;
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

    private Money normalizePositiveMoney(Money value, String fieldName) {
        if (value == null || value.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    private Money normalizeNonNegativeMoney(Money value, String fieldName) {
        if (value == null) {
            return Money.zero(DEFAULT_CURRENCY).rounded(2, RoundingMode.HALF_UP);
        }
        if (value.getAmount().signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }
}
