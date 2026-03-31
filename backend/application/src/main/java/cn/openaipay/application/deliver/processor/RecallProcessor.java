package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.Position;
import cn.openaipay.domain.deliver.service.RecallDomainService;
import org.springframework.stereotype.Component;

/**
 * 召回处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Component
public class RecallProcessor extends AbstractDeliverProcessor {

    /** 域信息 */
    private final RecallDomainService recallDomainService;

    public RecallProcessor(RecallDomainService recallDomainService) {
        this.recallDomainService = recallDomainService;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public int sort() {
        return 1;
    }

    /**
     * 处理DO信息。
     */
    @Override
    protected void doProcess(Position position, DeliverContext context) {
        position.setDeliverCreativeList(recallDomainService.recall(position.getId(), context.fallbackMode(), context.requestTime()));
    }
}
