package cn.openaipay.application.settle.async;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.accounting.facade.AccountingFacade;
import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxMessageHandler;
import cn.openaipay.application.settle.support.SettleAccountingEventAssembler;
import cn.openaipay.domain.outbox.model.OutboxMessage;
import org.springframework.stereotype.Component;

/**
 * 结算成功后触发会计事件过账的异步消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class SettleAccountingEventRequestedHandler implements OutboxMessageHandler {

    /** 结算事件信息 */
    private final SettleAccountingEventAssembler settleAccountingEventAssembler;
    /** 核算门面信息 */
    private final AccountingFacade accountingFacade;

    public SettleAccountingEventRequestedHandler(SettleAccountingEventAssembler settleAccountingEventAssembler,
                                                 AccountingFacade accountingFacade) {
        this.settleAccountingEventAssembler = settleAccountingEventAssembler;
        this.accountingFacade = accountingFacade;
    }

    /**
     * 处理主题信息。
     */
    @Override
    public String topic() {
        return AsyncMessageTopics.SETTLE_ACCOUNTING_EVENT_REQUESTED;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public void handle(OutboxMessage outboxMessage) {
        SettleAccountingEventRequestedPayload payload =
                SettleAccountingEventRequestedPayload.fromPayload(outboxMessage.getPayload());
        AcceptAccountingEventCommand command = settleAccountingEventAssembler.assemble(payload);
        if (command == null) {
            return;
        }
        accountingFacade.acceptEvent(command);
    }
}
