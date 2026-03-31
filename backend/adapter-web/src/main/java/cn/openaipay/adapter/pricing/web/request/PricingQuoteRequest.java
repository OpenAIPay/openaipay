package cn.openaipay.adapter.pricing.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.joda.money.Money;
/**
 * Pricing报价请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PricingQuoteRequest(
        /** 请求幂等号 */
        @NotBlank(message = "不能为空") String requestNo,
        /** 业务场景编码 */
        @NotBlank(message = "不能为空") String businessSceneCode,
        /** 支付方式编码 */
        @NotBlank(message = "不能为空") String paymentMethod,
        /** 原始金额 */
        @NotNull(message = "不能为空")
                                        @DecimalMin(value = "0.01", message = "必须大于0") Money originalAmount
) {
}
