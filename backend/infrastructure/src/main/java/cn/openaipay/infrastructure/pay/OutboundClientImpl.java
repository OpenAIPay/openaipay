package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.outbound.command.CancelOutboundWithdrawCommand;
import cn.openaipay.application.outbound.command.SubmitOutboundWithdrawCommand;
import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.facade.OutboundFacade;
import cn.openaipay.domain.pay.client.OutboundClient;
import cn.openaipay.domain.pay.client.PayOutboundOrderSnapshot;
import cn.openaipay.domain.pay.client.PayOutboundSubmitRequest;
import org.springframework.stereotype.Component;

/**
 * OutboundClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class OutboundClientImpl implements OutboundClient {

    /** 出金信息 */
    private final OutboundFacade outboundFacade;

    public OutboundClientImpl(OutboundFacade outboundFacade) {
        this.outboundFacade = outboundFacade;
    }

    /**
     * 提交业务数据。
     */
    @Override
    public PayOutboundOrderSnapshot submitWithdraw(PayOutboundSubmitRequest request) {
        return toSnapshot(outboundFacade.submitWithdraw(new SubmitOutboundWithdrawCommand(
                request.requestBizNo(),
                request.bizOrderNo(),
                request.tradeOrderNo(),
                request.payOrderNo(),
                request.payerUserId(),
                request.payeeAccountNo(),
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
    public PayOutboundOrderSnapshot cancelWithdraw(String outboundId, String reason) {
        return toSnapshot(outboundFacade.cancelWithdraw(new CancelOutboundWithdrawCommand(outboundId, reason)));
    }

    /**
     * 按请求业务单号查询记录。
     */
    @Override
    public PayOutboundOrderSnapshot queryByRequestBizNo(String requestBizNo) {
        return toSnapshot(outboundFacade.queryByRequestBizNo(requestBizNo));
    }

    private PayOutboundOrderSnapshot toSnapshot(OutboundOrderDTO order) {
        return new PayOutboundOrderSnapshot(
                order.outboundId(),
                order.requestBizNo(),
                order.bizOrderNo(),
                order.tradeOrderNo(),
                order.payOrderNo(),
                order.payeeAccountNo(),
                order.outboundAmount(),
                order.outboundStatus(),
                order.resultCode(),
                order.resultDescription(),
                order.instId(),
                order.instChannelCode(),
                order.outboundOrderNo(),
                order.payChannelCode(),
                order.gmtSubmit(),
                order.gmtResp(),
                order.gmtSettle(),
                order.createdAt(),
                order.updatedAt()
        );
    }
}
