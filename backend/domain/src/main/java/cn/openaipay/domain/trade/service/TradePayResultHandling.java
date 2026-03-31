package cn.openaipay.domain.trade.service;

/**
 * 交易域对支付结果的处理类型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public enum TradePayResultHandling {
    COMMITTED,
    PROCESSING,
    ROLLED_BACK,
    RECON_PENDING,
    FAILED
}
