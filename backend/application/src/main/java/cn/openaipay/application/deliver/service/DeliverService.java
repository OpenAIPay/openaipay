package cn.openaipay.application.deliver.service;

import cn.openaipay.application.deliver.command.DeliverCommand;
import cn.openaipay.application.deliver.command.DeliverEventCommand;
import cn.openaipay.application.deliver.dto.DeliverPositionDTO;
import java.util.Map;

/**
 * 投放应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverService {

    /**
     * 处理投放信息。
     */
    Map<String, DeliverPositionDTO> deliver(DeliverCommand command);

    /**
     * 记录事件信息。
     */
    void recordEvent(DeliverEventCommand command);
}
