package cn.openaipay.domain.cashier.service;

/**
 * 收银台最近支付偏好线索。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CashierRecentPaymentHint(
        /** 支付方式编码 */
        String paymentMethod,
        /** 扩展信息 */
        String metadata
) {
}
