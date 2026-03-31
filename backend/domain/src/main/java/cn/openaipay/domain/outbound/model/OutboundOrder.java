package cn.openaipay.domain.outbound.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 出金订单模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class OutboundOrder {
    /** 主键ID。 */
    private final Long id;
    /** 出金流水号。 */
    private final String outboundId;
    /** 机构号。 */
    private String instId;
    /** 金融交换码。 */
    private String instChannelCode;
    /** 出金订单号。 */
    private String outboundOrderNo;
    /** 收款账户号。 */
    private final String payeeAccountNo;
    /** 出金金额。 */
    private final Money outboundAmount;
    /** 出金出金状态。 */
    private OutboundStatus outboundStatus;
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

    public OutboundOrder(Long id,
                        String outboundId,
                        String instId,
                        String instChannelCode,
                        String outboundOrderNo,
                        String payeeAccountNo,
                        Money outboundAmount,
                        OutboundStatus outboundStatus,
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
        this.outboundId = normalizeRequired(outboundId, "outboundId");
        this.instId = normalizeOptional(instId);
        this.instChannelCode = normalizeOptional(instChannelCode);
        this.outboundOrderNo = normalizeOptional(outboundOrderNo);
        this.payeeAccountNo = normalizeRequired(payeeAccountNo, "payeeAccountNo");
        this.outboundAmount = normalizeAmount(outboundAmount, "outboundAmount");
        this.outboundStatus = outboundStatus == null ? OutboundStatus.CREATED : outboundStatus;
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
    public static OutboundOrder createForWithdraw(String outboundId,
                                                 String instId,
                                                 String instChannelCode,
                                                 String outboundOrderNo,
                                                 String payeeAccountNo,
                                                 Money outboundAmount,
                                                 String requestIdentify,
                                                 String requestBizNo,
                                                 String bizOrderNo,
                                                 String tradeOrderNo,
                                                 String payOrderNo,
                                                 String payChannelCode,
                                                 String bizIdentity,
                                                 LocalDateTime createdAt) {
        return new OutboundOrder(
                null,
                outboundId,
                instId,
                instChannelCode,
                outboundOrderNo,
                payeeAccountNo,
                outboundAmount,
                OutboundStatus.CREATED,
                null,
                null,
                requestIdentify,
                requestBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                payChannelCode,
                bizIdentity,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 标记业务数据。
     */
    public void markSubmitted(LocalDateTime now) {
        this.outboundStatus = OutboundStatus.SUBMITTED;
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
        this.outboundStatus = OutboundStatus.ACCEPTED;
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
    public void markSucceeded(String outboundOrderNo,
                              String resultCode,
                              String resultDescription,
                              LocalDateTime settleAt,
                              LocalDateTime now) {
        this.outboundStatus = OutboundStatus.SUCCEEDED;
        this.outboundOrderNo = normalizeOptional(outboundOrderNo);
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
        this.outboundStatus = OutboundStatus.FAILED;
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
        this.outboundStatus = OutboundStatus.RECON_PENDING;
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
        this.outboundStatus = OutboundStatus.CANCELED;
        this.resultCode = normalizeOptional(resultCode);
        this.resultDescription = normalizeOptional(resultDescription);
        this.gmtResp = responseAt == null ? LocalDateTime.now() : responseAt;
        touch(now);
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isTerminal() {
        return this.outboundStatus == OutboundStatus.SUCCEEDED
                || this.outboundStatus == OutboundStatus.FAILED
                || this.outboundStatus == OutboundStatus.CANCELED;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取出金ID。
     */
    public String getOutboundId() {
        return outboundId;
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
     * 获取出金订单NO信息。
     */
    public String getOutboundOrderNo() {
        return outboundOrderNo;
    }

    /**
     * 获取收款方账户NO信息。
     */
    public String getPayeeAccountNo() {
        return payeeAccountNo;
    }

    /**
     * 获取出金金额。
     */
    public Money getOutboundAmount() {
        return outboundAmount;
    }

    /**
     * 获取出金状态。
     */
    public OutboundStatus getOutboundStatus() {
        return outboundStatus;
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

    private static Money normalizeAmount(Money amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private static String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
