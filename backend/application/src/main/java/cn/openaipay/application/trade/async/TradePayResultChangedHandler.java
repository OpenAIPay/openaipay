package cn.openaipay.application.trade.async;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.pay.async.PayResultChangedPayload;
import cn.openaipay.application.trade.service.impl.TradeServiceImpl;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import org.springframework.stereotype.Component;

/**
 * 支付结果回写交易异步消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Component
public class TradePayResultChangedHandler implements OutboxMessageHandler {
    /** 交易信息 */
    private final TradeServiceImpl tradeService;

    public TradePayResultChangedHandler(TradeServiceImpl tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * 处理主题信息。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.PAY_RESULT_CHANGED;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        tradeService.handlePayResultChanged(PayResultChangedPayload.fromPayload(outboxMessage.getPayload()));
    }
}
