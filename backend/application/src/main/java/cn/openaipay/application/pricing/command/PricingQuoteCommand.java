package cn.openaipay.application.pricing.command;

import org.joda.money.Money;
/**
 * Pricing报价命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PricingQuoteCommand(
        /** 请求幂等号 */
        String requestNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 支付方式编码 */
        String paymentMethod,
        /** 原始金额 */
        Money originalAmount
) {
}
