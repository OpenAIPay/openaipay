package cn.openaipay.application.trade.dto;

import org.joda.money.Money;

/**
 * 交易查询返回的扣款拆分计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record TradeSplitPlanDTO(
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
