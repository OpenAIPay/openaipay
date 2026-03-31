package cn.openaipay.application.deliver.facade.impl;

import cn.openaipay.application.deliver.command.DeliverCommand;
import cn.openaipay.application.deliver.command.DeliverEventCommand;
import cn.openaipay.application.deliver.dto.DeliverPositionDTO;
import cn.openaipay.application.deliver.facade.DeliverFacade;
import cn.openaipay.application.deliver.service.DeliverService;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 投放门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class DeliverFacadeImpl implements DeliverFacade {

    /** 投放信息 */
    private final DeliverService deliverService;

    public DeliverFacadeImpl(DeliverService deliverService) {
        this.deliverService = deliverService;
    }

    /**
     * 处理投放信息。
     */
    @Override
    public Map<String, DeliverPositionDTO> deliver(DeliverCommand command) {
        return deliverService.deliver(command);
    }

    /**
     * 记录事件信息。
     */
    @Override
    public void recordEvent(DeliverEventCommand command) {
        deliverService.recordEvent(command);
    }
}
