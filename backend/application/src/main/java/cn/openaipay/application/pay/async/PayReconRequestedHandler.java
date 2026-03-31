package cn.openaipay.application.pay.async;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.pay.service.impl.PayServiceImpl;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import org.springframework.stereotype.Component;

/**
 * 支付对账/补偿请求异步消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class PayReconRequestedHandler implements OutboxMessageHandler {

    /** 支付信息 */
    private final PayServiceImpl payService;

    public PayReconRequestedHandler(PayServiceImpl payService) {
        this.payService = payService;
    }

    /**
     * 处理主题信息。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.PAY_RECON_REQUESTED;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        PayReconRequestedPayload payload = PayReconRequestedPayload.fromPayload(outboxMessage.getPayload());
        payService.reconcilePendingPayment(payload.payOrderNo());
    }
}
