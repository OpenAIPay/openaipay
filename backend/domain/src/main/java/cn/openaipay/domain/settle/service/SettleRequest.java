package cn.openaipay.domain.settle.service;

import org.joda.money.Money;

/**
 * 结算计划解析输入。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettleRequest(
        /** 交易类型 */
        String tradeType,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 结算业务单号 */
        String settleBizNo,
        /** 结算金额 */
        Money settleAmount,
        /** 手续费金额 */
        Money feeAmount,
        /** 原始金额 */
        Money originalAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 信用收款方信息 */
        boolean shouldCreditPayee,
        /** 手续费信息 */
        String feeBearer,
        /** 手续费用户ID */
        Long platformFeeUserId
) {
}
