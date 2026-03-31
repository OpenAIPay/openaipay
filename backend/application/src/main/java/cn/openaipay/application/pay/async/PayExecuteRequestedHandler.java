package cn.openaipay.application.pay.async;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.pay.service.impl.PayServiceImpl;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import org.springframework.stereotype.Component;

/**
 * 支付执行请求异步消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Component
public class PayExecuteRequestedHandler implements OutboxMessageHandler {
    /** 支付信息 */
    private final PayServiceImpl payService;

    public PayExecuteRequestedHandler(PayServiceImpl payService) {
        this.payService = payService;
    }

    /**
     * 处理主题信息。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.PAY_EXECUTE_REQUESTED;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        PayExecuteRequestedPayload payload = PayExecuteRequestedPayload.fromPayload(outboxMessage.getPayload());
        payService.executeSubmittedPayment(payload.payOrderNo());
    }
}
