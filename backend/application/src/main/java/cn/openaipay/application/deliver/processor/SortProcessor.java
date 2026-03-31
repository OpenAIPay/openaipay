package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverSortType;
import cn.openaipay.domain.deliver.model.Position;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 排序处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class SortProcessor extends AbstractDeliverProcessor {

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 4;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, cn.openaipay.domain.deliver.model.DeliverContext context) {
        if (position.creativeCount() <= 0) {
            return;
        }
        Comparator<DeliverCreative> comparator = switch (position.getSortType() == null ? DeliverSortType.PRIORITY : position.getSortType()) {
            case WEIGHT -> Comparator.comparingInt((DeliverCreative creative) -> defaultInt(creative.getWeight())).reversed()
                    .thenComparingInt(creative -> defaultInt(creative.getPriority()))
                    .thenComparingInt(creative -> defaultInt(creative.getDisplayOrder()));
            case MANUAL -> Comparator.comparingInt((DeliverCreative creative) -> defaultInt(creative.getDisplayOrder()))
                    .thenComparingInt(creative -> defaultInt(creative.getPriority()))
                    .thenComparingInt(creative -> defaultInt(creative.getWeight()) * -1);
            case PRIORITY -> Comparator.comparingInt((DeliverCreative creative) -> defaultInt(creative.getPriority()))
                    .thenComparingInt(creative -> defaultInt(creative.getDisplayOrder()))
                    .thenComparingInt(creative -> defaultInt(creative.getWeight()) * -1);
        };
        List<DeliverCreative> sorted = position.getDeliverCreativeList().stream().sorted(comparator).toList();
        position.setDeliverCreativeList(sorted);
    }

    private int defaultInt(Integer value) {
        return value == null ? Integer.MAX_VALUE / 4 : value;
    }
}
