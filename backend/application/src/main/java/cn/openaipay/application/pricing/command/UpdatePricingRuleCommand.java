package cn.openaipay.application.pricing.command;

import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
/**
 * 更新Pricing规则命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdatePricingRuleCommand(
        /** 规则ID */
        Long ruleId,
        /** 规则名称 */
        String ruleName,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 支付方式编码 */
        String paymentMethod,
        /** 币种编码 */
        String currencyCode,
        /** 手续费信息 */
        String feeMode,
        /** 手续费费率 */
        RateValue feeRate,
        /** 手续费信息 */
        Money fixedFee,
        /** MIN手续费信息 */
        Money minFee,
        /** 最大手续费信息 */
        Money maxFee,
        /** 手续费信息 */
        String feeBearer,
        /** 优先级 */
        Integer priority,
        /** 生效开始时间 */
        String validFrom,
        /** 生效结束时间 */
        String validTo,
        /** 规则载荷 */
        String rulePayload,
        /** 操作人 */
        String operator
) {
}
