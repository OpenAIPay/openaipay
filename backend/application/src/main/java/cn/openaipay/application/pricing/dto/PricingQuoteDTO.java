package cn.openaipay.application.pricing.dto;

import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
import java.time.LocalDateTime;
/**
 * Pricing报价数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PricingQuoteDTO(
        /** 业务单号 */
        String quoteNo,
        /** 请求幂等号 */
        String requestNo,
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
        /** 原始金额 */
        Money originalAmount,
        /** 手续费金额 */
        Money feeAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 结算金额 */
        Money settleAmount,
        /** 手续费信息 */
        String feeMode,
        /** 手续费信息 */
        String feeBearer,
        /** 手续费费率 */
        RateValue feeRate,
        /** 手续费信息 */
        Money fixedFee,
        /** 规则载荷 */
        String rulePayload,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
