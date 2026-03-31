package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.Position;
import cn.openaipay.domain.deliver.model.TargetingRule;
import cn.openaipay.domain.deliver.repository.TargetingRuleRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 定向过滤处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class TargetingProcessor extends AbstractDeliverProcessor {

    /** 规则信息 */
    private final TargetingRuleRepository targetingRuleRepository;

    public TargetingProcessor(TargetingRuleRepository targetingRuleRepository) {
        this.targetingRuleRepository = targetingRuleRepository;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 2;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, DeliverContext context) {
        if (position.creativeCount() <= 0) {
            return;
        }
        if (!passesAllRules(targetingRuleRepository.findTargetingRulesByEntityTypeAndCodes(
                DeliverEntityType.POSITION, List.of(position.getPositionCode())), context)) {
            position.setDeliverCreativeList(List.of());
            return;
        }
        List<DeliverCreative> creativeList = position.getDeliverCreativeList();
        Map<String, List<TargetingRule>> unitRules = groupByEntityCode(targetingRuleRepository.findTargetingRulesByEntityTypeAndCodes(
                DeliverEntityType.UNIT,
                creativeList.stream().map(DeliverCreative::getUnitCode).filter(this::hasText).collect(Collectors.toSet())
        ));
        Map<String, List<TargetingRule>> creativeRules = groupByEntityCode(targetingRuleRepository.findTargetingRulesByEntityTypeAndCodes(
                DeliverEntityType.CREATIVE,
                creativeList.stream().map(DeliverCreative::getCreativeCode).filter(this::hasText).collect(Collectors.toSet())
        ));
        List<DeliverCreative> filtered = creativeList.stream()
                .filter(creative -> passesAllRules(unitRules.get(creative.getUnitCode()), context))
                .filter(creative -> passesAllRules(creativeRules.get(creative.getCreativeCode()), context))
                .toList();
        position.setDeliverCreativeList(filtered);
    }

    private Map<String, List<TargetingRule>> groupByEntityCode(Collection<TargetingRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return Map.of();
        }
        return rules.stream().collect(Collectors.groupingBy(TargetingRule::entityCode));
    }

    private boolean passesAllRules(List<TargetingRule> rules, DeliverContext context) {
        return rules == null || rules.isEmpty() || rules.stream().allMatch(rule -> rule.matches(context));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
