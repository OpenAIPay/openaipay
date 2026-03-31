package cn.openaipay.domain.riskpolicy.service;

import cn.openaipay.domain.riskpolicy.model.RiskCheckContext;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;

/**
 * 风控策略领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface RiskPolicyDomainService {

    /**
     * 评估风控策略。
     */
    RiskDecision evaluate(RiskCheckContext context);
}
