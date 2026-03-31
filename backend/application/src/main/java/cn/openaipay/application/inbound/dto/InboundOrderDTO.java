package cn.openaipay.application.inbound.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 入金订单数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record InboundOrderDTO(
        /** 入金单号 */
        String inboundId,
        /** 请求业务单号 */
        String requestBizNo,
        /** 业务单号 */
        String bizOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 付款方账号 */
        String payerAccountNo,
        /** 入金金额 */
        Money inboundAmount,
        /** 业务金额 */
        Money accountAmount,
        /** 结算金额 */
        Money settleAmount,
        /** 入金状态 */
        String inboundStatus,
        /** 结果编码 */
        String resultCode,
        /** 结果信息 */
        String resultDescription,
        /** 机构ID */
        String instId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 入金订单单号 */
        String inboundOrderNo,
        /** 支付渠道编码 */
        String payChannelCode,
        /** GMTsubmit信息 */
        LocalDateTime gmtSubmit,
        /** GMTresp信息 */
        LocalDateTime gmtResp,
        /** GMT结算信息 */
        LocalDateTime gmtSettle,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
