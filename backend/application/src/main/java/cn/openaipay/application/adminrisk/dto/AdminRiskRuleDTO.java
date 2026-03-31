package cn.openaipay.application.adminrisk.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控规则配置DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AdminRiskRuleDTO(
        /** 规则编码。 */
        String ruleCode,
        /** 场景编码。 */
        String sceneCode,
        /** 规则类型：SINGLE_LIMIT/DAILY_LIMIT/USER_BLOCK。 */
        String ruleType,
        /** 生效范围：GLOBAL/USER。 */
        String scopeType,
        /** 范围值（scopeType=USER时为userId）。 */
        String scopeValue,
        /** 阈值金额。 */
        BigDecimal thresholdAmount,
        /** 币种编码。 */
        String currencyCode,
        /** 优先级。 */
        Integer priority,
        /** 状态：ACTIVE/INACTIVE。 */
        String status,
        /** 规则描述。 */
        String ruleDesc,
        /** 更新人。 */
        String updatedBy,
        /** 创建时间。 */
        LocalDateTime createdAt,
        /** 更新时间。 */
        LocalDateTime updatedAt
) {
}

