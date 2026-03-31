package cn.openaipay.domain.riskpolicy.service.impl;

import cn.openaipay.domain.riskpolicy.model.RiskCheckContext;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.model.RiskSceneCode;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * 风控策略领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public class RiskPolicyDomainServiceImpl implements RiskPolicyDomainService {

    /** 场景单笔限额。 */
    private final Map<RiskSceneCode, BigDecimal> singleLimits;

    public RiskPolicyDomainServiceImpl(Map<RiskSceneCode, BigDecimal> singleLimits) {
        this.singleLimits = sanitizeLimits(singleLimits);
    }

    /**
     * 评估风控策略。
     */
    @Override
    public RiskDecision evaluate(RiskCheckContext context) {
        if (context == null) {
            return RiskDecision.reject("RISK_INVALID_CONTEXT", "risk context must not be null");
        }
        if (context.sceneCode() == null) {
            return RiskDecision.reject("RISK_INVALID_SCENE", "risk scene must not be null");
        }
        if (context.userId() == null || context.userId() <= 0) {
            return RiskDecision.reject("RISK_INVALID_USER", "userId must be greater than 0");
        }
        BigDecimal amount = normalizeAmount(context.amount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RiskDecision.reject("RISK_INVALID_AMOUNT", "amount must be greater than 0");
        }
        BigDecimal limit = singleLimits.getOrDefault(context.sceneCode(), BigDecimal.ZERO);
        if (limit.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(limit) > 0) {
            return RiskDecision.reject(
                    "RISK_EXCEED_SINGLE_LIMIT",
                    "single amount exceeds risk limit for scene " + context.sceneCode().name()
            );
        }
        return RiskDecision.pass();
    }

    private Map<RiskSceneCode, BigDecimal> sanitizeLimits(Map<RiskSceneCode, BigDecimal> source) {
        Map<RiskSceneCode, BigDecimal> normalized = new EnumMap<>(RiskSceneCode.class);
        if (source == null) {
            return normalized;
        }
        for (Map.Entry<RiskSceneCode, BigDecimal> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            BigDecimal limit = entry.getValue();
            if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            normalized.put(entry.getKey(), limit.setScale(2, RoundingMode.HALF_UP));
        }
        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.abs().setScale(2, RoundingMode.HALF_UP);
    }
}
