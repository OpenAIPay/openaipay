package cn.openaipay.application.pricing.command;
/**
 * 变更Pricing规则状态命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record ChangePricingRuleStatusCommand(
        /** 规则ID */
        Long ruleId,
        /** 状态编码 */
        String status,
        /** 操作人 */
        String operator
) {
}
