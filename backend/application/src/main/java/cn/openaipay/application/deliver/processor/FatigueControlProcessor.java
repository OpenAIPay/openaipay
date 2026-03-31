package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.FatigueControlRule;
import cn.openaipay.domain.deliver.model.Position;
import cn.openaipay.domain.deliver.repository.DeliverEventRecordRepository;
import cn.openaipay.domain.deliver.repository.FatigueControlRuleRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 疲劳度控制处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class FatigueControlProcessor extends AbstractDeliverProcessor {

    /** 规则信息 */
    private final FatigueControlRuleRepository fatigueControlRuleRepository;
    /** 投放事件记录信息 */
    private final DeliverEventRecordRepository deliverEventRecordRepository;

    public FatigueControlProcessor(FatigueControlRuleRepository fatigueControlRuleRepository,
                                   DeliverEventRecordRepository deliverEventRecordRepository) {
        this.fatigueControlRuleRepository = fatigueControlRuleRepository;
        this.deliverEventRecordRepository = deliverEventRecordRepository;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 3;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, DeliverContext context) {
        if (position.creativeCount() <= 0) {
            return;
        }
        List<FatigueControlRule> positionRules = fatigueControlRuleRepository.findFatigueRulesByEntityTypeAndCodes(
                DeliverEntityType.POSITION,
                List.of(position.getPositionCode())
        );
        if (isBlocked(positionRules, context, position.getPositionCode(), DeliverEntityType.POSITION)) {
            position.setDeliverCreativeList(List.of());
            return;
        }

        List<DeliverCreative> creativeList = position.getDeliverCreativeList();
        Map<String, List<FatigueControlRule>> unitRules = fatigueControlRuleRepository.findFatigueRulesByEntityTypeAndCodes(
                        DeliverEntityType.UNIT,
                        creativeList.stream().map(DeliverCreative::getUnitCode).filter(this::hasText).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.groupingBy(FatigueControlRule::entityCode));
        Map<String, List<FatigueControlRule>> creativeRules = fatigueControlRuleRepository.findFatigueRulesByEntityTypeAndCodes(
                        DeliverEntityType.CREATIVE,
                        creativeList.stream().map(DeliverCreative::getCreativeCode).filter(this::hasText).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.groupingBy(FatigueControlRule::entityCode));

        List<DeliverCreative> filtered = creativeList.stream()
                .filter(creative -> !isBlocked(unitRules.get(creative.getUnitCode()), context, creative.getUnitCode(), DeliverEntityType.UNIT))
                .filter(creative -> !isBlocked(creativeRules.get(creative.getCreativeCode()), context, creative.getCreativeCode(), DeliverEntityType.CREATIVE))
                .toList();
        position.setDeliverCreativeList(filtered);
    }

    private boolean isBlocked(List<FatigueControlRule> rules,
                              DeliverContext context,
                              String entityCode,
                              DeliverEntityType entityType) {
        if (rules == null || rules.isEmpty() || !hasText(entityCode)) {
            return false;
        }
        for (FatigueControlRule rule : rules) {
            long recentCount = deliverEventRecordRepository.countRecent(
                    context.clientId(),
                    context.userId(),
                    entityType,
                    entityCode,
                    rule.eventType(),
                    context.requestTime().minusMinutes(rule.resolveWindowMinutes()),
                    context.requestTime().plusNanos(1)
            );
            if (rule.isBlocked(recentCount)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
