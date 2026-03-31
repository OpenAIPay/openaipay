package cn.openaipay.application.pay.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 支付资金明细摘要数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record PayFundDetailSummaryDTO(
        /** 支付单号 */
        String payOrderNo,
        /** 支付信息 */
        String payTool,
        /** 明细所属信息 */
        String detailOwner,
        /** 金额 */
        Money amount,
        /** 业务金额 */
        Money cumulativeRefundAmount,
        /** 渠道信息 */
        String channel,
        /** 银行订单单号 */
        String bankOrderNo,
        /** 银行卡单号 */
        String bankCardNo,
        /** 渠道手续费金额 */
        Money channelFeeAmount,
        /** 订单单号 */
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
        /** REDID */
        String redPacketId,
        /** 业务单号 */
        String accountNo,
        /** 资金编码 */
        String fundCode,
        /** 资金产品编码 */
        String fundProductCode,
        /** 资金身份信息 */
        String fundAccountIdentity,
        /** 信用单号 */
        String creditAccountNo,
        /** 信用类型 */
        String creditAccountType,
        /** 信用产品编码 */
        String creditProductCode,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
