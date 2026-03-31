package cn.openaipay.domain.pay.model;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 银行卡资金明细模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PayBankCardFundDetail extends PayFundDetailSummary {

    /** 渠道 */
    private String channel;
    /** 机构ID */
    private String instId;
    /** 机构渠道编码 */
    private String instChannelCode;
    /** 支付渠道编码 */
    private String payChannelCode;
    /** 银行编码 */
    private String bankCode;
    /** 银行名称 */
    private String bankName;
    /** 卡类型 */
    private String cardType;
    /** 持卡人姓名 */
    private String cardHolderName;
    /** 卡尾号 */
    private String cardTailNo;
    /** 支付工具快照 */
    private String toolSnapshot;
    /** 银行订单号 */
    private String bankOrderNo;
    /** 银行卡号 */
    private String bankCardNo;
    /** 渠道收费金额 */
    private Money channelFeeAmount;
    /** 充值订单号 */
    private String depositOrderNo;

    public PayBankCardFundDetail(Long id,
                                 String payOrderNo,
                                 PayFundDetailOwner detailOwner,
                                 Money amount,
                                 Money cumulativeRefundAmount,
                                 String channel,
                                 String instId,
                                 String instChannelCode,
                                 String payChannelCode,
                                 String bankCode,
                                 String bankName,
                                 String cardType,
                                 String cardHolderName,
                                 String cardTailNo,
                                 String toolSnapshot,
                                 String bankOrderNo,
                                 String bankCardNo,
                                 Money channelFeeAmount,
                                 String depositOrderNo,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        super(
                id,
                payOrderNo,
                PayFundDetailTool.BANK_CARD,
                detailOwner,
                amount,
                cumulativeRefundAmount,
                createdAt,
                updatedAt
        );
        this.channel = normalizeOptional(channel);
        this.instId = normalizeOptional(instId);
        this.instChannelCode = normalizeOptional(instChannelCode);
        this.payChannelCode = normalizeOptional(payChannelCode);
        this.bankCode = normalizeOptional(bankCode);
        this.bankName = normalizeOptional(bankName);
        this.cardType = normalizeOptional(cardType);
        this.cardHolderName = normalizeOptional(cardHolderName);
        this.cardTailNo = normalizeOptional(cardTailNo);
        this.toolSnapshot = normalizeOptional(toolSnapshot);
        this.bankOrderNo = normalizeOptional(bankOrderNo);
        this.bankCardNo = normalizeOptional(bankCardNo);
        this.channelFeeAmount = normalizeNonNegative(
                channelFeeAmount,
                "channelFeeAmount",
                getAmount().getCurrencyUnit()
        );
        this.depositOrderNo = normalizeOptional(depositOrderNo);
    }

    /**
     * 创建业务数据。
     */
    public static PayBankCardFundDetail create(String payOrderNo,
                                               PayFundDetailOwner detailOwner,
                                               Money amount,
                                               String channel,
                                               String instId,
                                               String instChannelCode,
                                               String payChannelCode,
                                               String bankCode,
                                               String bankName,
                                               String cardType,
                                               String cardHolderName,
                                               String cardTailNo,
                                               String toolSnapshot,
                                               String bankOrderNo,
                                               String bankCardNo,
                                               Money channelFeeAmount,
                                               String depositOrderNo,
                                               LocalDateTime now) {
        return new PayBankCardFundDetail(
                null,
                payOrderNo,
                detailOwner,
                amount,
                null,
                channel,
                instId,
                instChannelCode,
                payChannelCode,
                bankCode,
                bankName,
                cardType,
                cardHolderName,
                cardTailNo,
                toolSnapshot,
                bankOrderNo,
                bankCardNo,
                channelFeeAmount,
                depositOrderNo,
                now,
                now
        );
    }

    /**
     * 刷新渠道信息。
     */
    public void refreshChannelInfo(String channel,
                                   String instId,
                                   String instChannelCode,
                                   String payChannelCode,
                                   String bankCode,
                                   String bankName,
                                   String cardType,
                                   String cardHolderName,
                                   String cardTailNo,
                                   String toolSnapshot,
                                   String bankOrderNo,
                                   String bankCardNo,
                                   Money channelFeeAmount,
                                   String depositOrderNo,
                                   LocalDateTime now) {
        this.channel = normalizeOptional(channel);
        this.instId = normalizeOptional(instId);
        this.instChannelCode = normalizeOptional(instChannelCode);
        this.payChannelCode = normalizeOptional(payChannelCode);
        this.bankCode = normalizeOptional(bankCode);
        this.bankName = normalizeOptional(bankName);
        this.cardType = normalizeOptional(cardType);
        this.cardHolderName = normalizeOptional(cardHolderName);
        this.cardTailNo = normalizeOptional(cardTailNo);
        this.toolSnapshot = normalizeOptional(toolSnapshot);
        this.bankOrderNo = normalizeOptional(bankOrderNo);
        this.bankCardNo = normalizeOptional(bankCardNo);
        this.channelFeeAmount = normalizeNonNegative(
                channelFeeAmount,
                "channelFeeAmount",
                getAmount().getCurrencyUnit()
        );
        this.depositOrderNo = normalizeOptional(depositOrderNo);
        touch(now);
    }

    /**
     * 获取渠道信息。
     */
    public String getChannel() {
        return channel;
    }

    /**
     * 获取机构ID。
     */
    public String getInstId() {
        return instId;
    }

    /**
     * 获取机构渠道编码。
     */
    public String getInstChannelCode() {
        return instChannelCode;
    }

    /**
     * 获取支付渠道编码。
     */
    public String getPayChannelCode() {
        return payChannelCode;
    }

    /**
     * 获取银行编码。
     */
    public String getBankCode() {
        return bankCode;
    }

    /**
     * 获取银行名称。
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * 获取卡类型。
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * 获取持卡人姓名。
     */
    public String getCardHolderName() {
        return cardHolderName;
    }

    /**
     * 获取卡尾号。
     */
    public String getCardTailNo() {
        return cardTailNo;
    }

    /**
     * 获取工具快照。
     */
    public String getToolSnapshot() {
        return toolSnapshot;
    }

    /**
     * 获取银行订单NO信息。
     */
    public String getBankOrderNo() {
        return bankOrderNo;
    }

    /**
     * 获取银行卡NO信息。
     */
    public String getBankCardNo() {
        return bankCardNo;
    }

    /**
     * 获取渠道FEE金额。
     */
    public Money getChannelFeeAmount() {
        return channelFeeAmount;
    }

    /**
     * 获取订单NO信息。
     */
    public String getDepositOrderNo() {
        return depositOrderNo;
    }
}
