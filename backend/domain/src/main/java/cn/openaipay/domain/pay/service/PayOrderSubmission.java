package cn.openaipay.domain.pay.service;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 支付提交领域输入。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayOrderSubmission(
        /** 支付单号 */
        String payOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 订单单号 */
        String bizOrderNo,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 尝试单号 */
        int attemptNo,
        /** 来源业务快照 */
        String sourceBizSnapshot,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 原始金额 */
        Money originalAmount,
        /** 钱包金额 */
        Money walletDebitAmount,
        /** 资金金额 */
        Money fundDebitAmount,
        /** 信用金额 */
        Money creditDebitAmount,
        /** 入金金额 */
        Money inboundDebitAmount,
        /** 业务金额 */
        Money discountAmount,
        /** 优惠券单号 */
        String couponNo,
        /** 计划快照 */
        String settlementPlanSnapshot,
        /** TXID */
        String globalTxId,
        /** 业务时间 */
        LocalDateTime occurredAt
) {
}
