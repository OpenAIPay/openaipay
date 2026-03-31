package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.FatigueControlRule;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 疲劳度规则仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface FatigueControlRuleRepository {

    /**
     * 按与编码查找频控规则。
     */
    List<FatigueControlRule> findFatigueRulesByEntityTypeAndCodes(DeliverEntityType entityType, Collection<String> entityCodes);

    /**
     * 按ID查找频控规则。
     */
    Optional<FatigueControlRule> findFatigueRuleById(Long id);

    /**
     * 查找ALL频控规则。
     */
    List<FatigueControlRule> findAllFatigueRules();

    /**
     * 保存频控规则。
     */
    FatigueControlRule saveFatigueRule(FatigueControlRule rule);
}
