package cn.openaipay.application.pay.dto;

import org.joda.money.Money;

/**
 * 支付参与方扣款拆分计划 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record PaySplitPlanDTO(
        /** 钱包金额 */
        Money walletDebitAmount,
        /** 资金金额 */
        Money fundDebitAmount,
        /** 信用金额 */
        Money creditDebitAmount,
        /** 入金金额 */
        Money inboundDebitAmount
) {
}
