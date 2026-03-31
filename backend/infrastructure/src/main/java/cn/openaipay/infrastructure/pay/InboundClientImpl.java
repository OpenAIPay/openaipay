package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.inbound.command.CancelInboundDepositCommand;
import cn.openaipay.application.inbound.command.SubmitInboundDepositCommand;
import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.facade.InboundFacade;
import cn.openaipay.domain.pay.client.InboundClient;
import cn.openaipay.domain.pay.client.PayInboundOrderSnapshot;
import cn.openaipay.domain.pay.client.PayInboundSubmitRequest;
import org.springframework.stereotype.Component;

/**
 * InboundClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class InboundClientImpl implements InboundClient {

    /** 入金信息 */
    private final InboundFacade inboundFacade;

    public InboundClientImpl(InboundFacade inboundFacade) {
        this.inboundFacade = inboundFacade;
    }

    /**
     * 提交业务数据。
     */
    @Override
    public PayInboundOrderSnapshot submitDeposit(PayInboundSubmitRequest request) {
        return toSnapshot(inboundFacade.submitDeposit(new SubmitInboundDepositCommand(
                request.requestBizNo(),
                request.bizOrderNo(),
                request.tradeOrderNo(),
                request.payOrderNo(),
                request.payerUserId(),
                request.payerAccountNo(),
                request.amount(),
                request.payChannelCode(),
                request.instChannelCode(),
                request.requestIdentify(),
                request.bizIdentity()
        )));
    }

    /**
     * 取消业务数据。
     */
    @Override
    public PayInboundOrderSnapshot cancelDeposit(String inboundId, String reason) {
        return toSnapshot(inboundFacade.cancelDeposit(new CancelInboundDepositCommand(inboundId, reason)));
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    public PayInboundOrderSnapshot queryByRequestBizNo(String requestBizNo) {
        return toSnapshot(inboundFacade.queryByRequestBizNo(requestBizNo));
    }

    private PayInboundOrderSnapshot toSnapshot(InboundOrderDTO order) {
        return new PayInboundOrderSnapshot(
                order.inboundId(),
                order.requestBizNo(),
                order.bizOrderNo(),
                order.tradeOrderNo(),
                order.payOrderNo(),
                order.payerAccountNo(),
                order.inboundAmount(),
                order.accountAmount(),
                order.settleAmount(),
                order.inboundStatus(),
                order.resultCode(),
                order.resultDescription(),
                order.instId(),
                order.instChannelCode(),
                order.inboundOrderNo(),
                order.payChannelCode(),
                order.gmtSubmit(),
                order.gmtResp(),
                order.gmtSettle(),
                order.createdAt(),
                order.updatedAt()
        );
    }
}
