package cn.openaipay.domain.trade.service;

/**
 * 支付结果在交易域内的语义决策。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record TradePayResultDecision(
        /** 支付状态 */
        String payStatus,
        /** 处理中标记 */
        TradePayResultHandling handling,
        /** 失败原因 */
        String failureReason
) {
}
