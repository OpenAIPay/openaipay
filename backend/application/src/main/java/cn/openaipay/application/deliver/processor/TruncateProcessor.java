package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.Position;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 截断处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class TruncateProcessor extends AbstractDeliverProcessor {

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 8;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, cn.openaipay.domain.deliver.model.DeliverContext context) {
        if (position.creativeCount() <= 0) {
            return;
        }
        int maxDisplayCount = position.getMaxDisplayCount() == null || position.getMaxDisplayCount() <= 0
                ? 1
                : position.getMaxDisplayCount();
        if (position.creativeCount() <= maxDisplayCount) {
            return;
        }
        List<cn.openaipay.domain.deliver.model.DeliverCreative> creatives = position.getDeliverCreativeList();
        position.setDeliverCreativeList(creatives.subList(0, maxDisplayCount));
    }
}
