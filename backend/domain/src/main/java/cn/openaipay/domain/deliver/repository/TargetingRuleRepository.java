package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.TargetingRule;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 定向规则仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface TargetingRuleRepository {

    /**
     * 按与编码查找定向规则。
     */
    List<TargetingRule> findTargetingRulesByEntityTypeAndCodes(DeliverEntityType entityType, Collection<String> entityCodes);

    /**
     * 按ID查找定向规则。
     */
    Optional<TargetingRule> findTargetingRuleById(Long id);

    /**
     * 查找ALL定向规则。
     */
    List<TargetingRule> findAllTargetingRules();

    /**
     * 保存定向规则。
     */
    TargetingRule saveTargetingRule(TargetingRule rule);
}
