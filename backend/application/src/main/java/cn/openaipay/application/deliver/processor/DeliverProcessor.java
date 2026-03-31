package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.Position;

/**
 * 投放处理器接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverProcessor {

    /**
     * 处理业务数据。
     */
    int sort();

    /**
     * 处理业务数据。
     */
    void process(Position position, DeliverContext context);
}
