package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;

import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
/**
 * 创建Pricing规则请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreatePricingRuleRequest(
        /** 规则编码 */
        @NotBlank(message = "不能为空") String ruleCode,
        /** 规则名称 */
        @NotBlank(message = "不能为空") String ruleName,
        /** 业务场景编码 */
        @NotBlank(message = "不能为空") String businessSceneCode,
        /** 支付方式编码 */
        @NotBlank(message = "不能为空") String paymentMethod,
        /** 币种编码 */
        @NotBlank(message = "不能为空") String currencyCode,
        /** 手续费信息 */
        @NotBlank(message = "不能为空") String feeMode,
        /** 手续费费率 */
        RateValue feeRate,
        /** 手续费信息 */
        Money fixedFee,
        /** MIN手续费信息 */
        Money minFee,
        /** 最大手续费信息 */
        Money maxFee,
        /** 手续费信息 */
        @NotBlank(message = "不能为空") String feeBearer,
        /** 优先级 */
        Integer priority,
        /** 生效开始时间 */
        String validFrom,
        /** 生效结束时间 */
        String validTo,
        /** 规则载荷 */
        String rulePayload,
        /** 业务状态 */
        String initialStatus,
        /** 操作人 */
        String operator
) {
}
