package cn.openaipay.application.pricing.dto;

import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
import java.time.LocalDateTime;
/**
 * Pricing规则数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PricingRuleDTO(
        /** 规则ID */
        Long ruleId,
        /** 规则编码 */
        String ruleCode,
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
        /** 状态编码 */
        String status,
        /** 生效开始时间 */
        LocalDateTime validFrom,
        /** 生效结束时间 */
        LocalDateTime validTo,
        /** 规则载荷 */
        String rulePayload,
        /** 创建BY信息 */
        String createdBy,
        /** 更新BY信息 */
        String updatedBy,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
