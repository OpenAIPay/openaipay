package cn.openaipay.application.pay.async;

import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.pay.support.PayAccountingEventAssembler;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import cn.openaipay.domain.pay.client.AccountingClient;
import cn.openaipay.domain.pay.client.PayAccountingEventRequest;
import org.springframework.stereotype.Component;

/**
 * 支付成功后触发会计事件过账的异步消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class PayAccountingEventRequestedHandler implements OutboxMessageHandler {

    /** 支付事件信息 */
    private final PayAccountingEventAssembler payAccountingEventAssembler;
    /** 核算客户端信息 */
    private final AccountingClient accountingClient;

    public PayAccountingEventRequestedHandler(PayAccountingEventAssembler payAccountingEventAssembler,
                                              AccountingClient accountingClient) {
        this.payAccountingEventAssembler = payAccountingEventAssembler;
        this.accountingClient = accountingClient;
    }

    /**
     * 处理主题信息。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.PAY_ACCOUNTING_EVENT_REQUESTED;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        PayAccountingEventRequestedPayload payload =
                PayAccountingEventRequestedPayload.fromPayload(outboxMessage.getPayload());
        PayAccountingEventRequest command =
                payAccountingEventAssembler.assembleCommittedPayment(payload.payOrderNo());
        if (command == null) {
            return;
        }
        accountingClient.acceptEvent(command);
    }
}
