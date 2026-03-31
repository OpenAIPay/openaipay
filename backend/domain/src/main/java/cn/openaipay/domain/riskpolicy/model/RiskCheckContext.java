package cn.openaipay.domain.riskpolicy.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 风控校验上下文。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record RiskCheckContext(
        /** 场景编码。 */
        RiskSceneCode sceneCode,
        /** 用户ID。 */
        Long userId,
        /** 账户号。 */
        String accountNo,
        /** 金额（正数）。 */
        BigDecimal amount,
        /** 扩展参数。 */
        Map<String, String> metadata
) {
}
