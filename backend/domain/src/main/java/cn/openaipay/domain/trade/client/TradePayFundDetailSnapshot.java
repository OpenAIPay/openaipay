package cn.openaipay.domain.trade.client;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * TradePayFundDetailSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record TradePayFundDetailSnapshot(
        /** 支付单号 */
        String payOrderNo,
        /** 支付工具 */
        String payTool,
        /** 明细归属方 */
        String detailOwner,
        /** 金额 */
        Money amount,
        /** 累计退款金额 */
        Money cumulativeRefundAmount,
        /** 渠道 */
        String channel,
        /** 银行订单号 */
        String bankOrderNo,
        /** 银行卡号 */
        String bankCardNo,
        /** 渠道手续费 */
        Money channelFeeAmount,
        /** 充值/提现订单号 */
        String depositOrderNo,
        /** 机构ID */
        String instId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 支付渠道编码 */
        String payChannelCode,
        /** 银行编码 */
        String bankCode,
        /** 银行名称 */
        String bankName,
        /** 卡类型 */
        String cardType,
        /** 持卡人姓名 */
        String cardHolderName,
        /** 卡尾号 */
        String cardTailNo,
        /** 支付工具快照 */
        String toolSnapshot,
        /** 红包ID */
        String redPacketId,
        /** 钱包账户号 */
        String accountNo,
        /** 基金编码 */
        String fundCode,
        /** 基金产品编码 */
        String fundProductCode,
        /** 基金账户标识 */
        String fundAccountIdentity,
        /** 信用账户号 */
        String creditAccountNo,
        /** 信用账户类型 */
        String creditAccountType,
        /** 信用产品编码 */
        String creditProductCode,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
