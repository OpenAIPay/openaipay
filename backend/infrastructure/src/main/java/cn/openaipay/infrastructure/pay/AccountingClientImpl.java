package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.accounting.facade.AccountingFacade;
import cn.openaipay.domain.pay.client.AccountingClient;
import cn.openaipay.domain.pay.client.PayAccountingEventRequest;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * AccountingClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class AccountingClientImpl implements AccountingClient {

    /** 核算门面信息 */
    private final AccountingFacade accountingFacade;

    public AccountingClientImpl(AccountingFacade accountingFacade) {
        this.accountingFacade = accountingFacade;
    }

    /**
     * 处理事件信息。
     */
    @Override
    public void acceptEvent(PayAccountingEventRequest request) {
        accountingFacade.acceptEvent(new AcceptAccountingEventCommand(
                request.eventId(),
                request.eventType(),
                request.eventVersion(),
                request.bookId(),
                request.sourceSystem(),
                request.sourceBizType(),
                request.sourceBizNo(),
                request.bizOrderNo(),
                request.requestNo(),
                request.tradeOrderNo(),
                request.payOrderNo(),
                request.businessSceneCode(),
                request.businessDomainCode(),
                request.payerUserId(),
                request.payeeUserId(),
                request.currencyCode(),
                request.occurredAt(),
                request.idempotencyKey(),
                request.globalTxId(),
                request.traceId(),
                request.payload(),
                toLegCommands(request.legs())
        ));
    }

    private List<AcceptAccountingEventCommand.AccountingLegCommand> toLegCommands(
            List<PayAccountingEventRequest.PayAccountingLegRequest> legs) {
        if (legs == null || legs.isEmpty()) {
            return List.of();
        }
        return legs.stream()
                .map(leg -> new AcceptAccountingEventCommand.AccountingLegCommand(
                        leg.legNo(),
                        leg.accountDomain(),
                        leg.accountType(),
                        leg.accountNo(),
                        leg.ownerType(),
                        leg.ownerId(),
                        leg.amount(),
                        leg.direction(),
                        leg.bizRole(),
                        leg.subjectHint(),
                        leg.referenceNo(),
                        leg.metadata()
                ))
                .toList();
    }
}
