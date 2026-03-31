package cn.openaipay.domain.trade.client;

/**
 * TradeSettleResult 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record TradeSettleResult(
        /** 状态编码 */
        String status,
        /** 消息内容 */
        String message
) {
}
