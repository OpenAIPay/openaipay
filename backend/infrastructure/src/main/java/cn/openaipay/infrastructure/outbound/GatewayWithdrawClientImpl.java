package cn.openaipay.infrastructure.outbound;

import cn.openaipay.application.gateway.command.GatewayWithdrawCancelCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayWithdrawResultDTO;
import cn.openaipay.application.gateway.facade.GatewayFacade;
import cn.openaipay.domain.outbound.client.GatewayWithdrawCancelRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawClient;
import cn.openaipay.domain.outbound.client.GatewayWithdrawConfirmRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawInitiateRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawQueryRequest;
import cn.openaipay.domain.outbound.client.GatewayWithdrawResultSnapshot;
import org.springframework.stereotype.Component;

/**
 * GatewayWithdrawClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class GatewayWithdrawClientImpl implements GatewayWithdrawClient {

    /** 网关信息 */
    private final GatewayFacade gatewayFacade;

    public GatewayWithdrawClientImpl(GatewayFacade gatewayFacade) {
        this.gatewayFacade = gatewayFacade;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayWithdrawResultSnapshot initiateWithdraw(GatewayWithdrawInitiateRequest request) {
        return toSnapshot(gatewayFacade.initiateWithdraw(new GatewayWithdrawInitiateCommand(
                request.outboundId(),
                request.instChannelCode(),
                request.payerUserId(),
                request.payeeAccountNo(),
                request.amount(),
                request.payChannelCode(),
                request.requestIdentify(),
                request.bizIdentity()
        )));
    }

    /**
     * 查询业务数据。
     */
    @Override
    public GatewayWithdrawResultSnapshot queryWithdraw(GatewayWithdrawQueryRequest request) {
        return toSnapshot(gatewayFacade.queryWithdraw(new GatewayWithdrawQueryCommand(
                request.outboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayWithdrawResultSnapshot confirmWithdraw(GatewayWithdrawConfirmRequest request) {
        return toSnapshot(gatewayFacade.confirmWithdraw(new GatewayWithdrawConfirmCommand(
                request.outboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayWithdrawResultSnapshot cancelWithdraw(GatewayWithdrawCancelRequest request) {
        return toSnapshot(gatewayFacade.cancelWithdraw(new GatewayWithdrawCancelCommand(
                request.outboundId(),
                request.instChannelCode(),
                request.reason()
        )));
    }

    private GatewayWithdrawResultSnapshot toSnapshot(GatewayWithdrawResultDTO result) {
        return new GatewayWithdrawResultSnapshot(
                result.success(),
                result.resultCode(),
                result.resultDescription(),
                result.instId(),
                result.instSerialNo(),
                result.instRefNo(),
                result.instChannelCode(),
                result.outboundOrderNo(),
                result.gmtResp(),
                result.gmtSettle()
        );
    }
}
