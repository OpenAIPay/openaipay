package cn.openaipay.application.deliver.processor;

import cn.openaipay.domain.deliver.model.DeliverContext;
import cn.openaipay.domain.deliver.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 投放处理器抽象基类。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public abstract class AbstractDeliverProcessor implements DeliverProcessor {

    /** logger信息 */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 处理业务数据。
     */
    @Override
    public void process(Position position, DeliverContext context) {
        long start = System.currentTimeMillis();
        doProcess(position, context);
        logger.debug(
                "deliver processor={} positionCode={} creativeCount={} cost={}ms fallback={}",
                getClass().getSimpleName(),
                position.getPositionCode(),
                position.creativeCount(),
                System.currentTimeMillis() - start,
                context.fallbackMode()
        );
    }

    /**
     * 处理DO信息。
     */
    protected abstract void doProcess(Position position, DeliverContext context);
}
