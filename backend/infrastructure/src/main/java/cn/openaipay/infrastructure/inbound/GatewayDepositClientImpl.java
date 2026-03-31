package cn.openaipay.infrastructure.inbound;

import cn.openaipay.application.gateway.command.GatewayDepositCancelCommand;
import cn.openaipay.application.gateway.command.GatewayDepositConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayDepositInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayDepositQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayDepositResultDTO;
import cn.openaipay.application.gateway.facade.GatewayFacade;
import cn.openaipay.domain.inbound.client.GatewayDepositCancelRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositClient;
import cn.openaipay.domain.inbound.client.GatewayDepositConfirmRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositInitiateRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositQueryRequest;
import cn.openaipay.domain.inbound.client.GatewayDepositResultSnapshot;
import org.springframework.stereotype.Component;

/**
 * GatewayDepositClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class GatewayDepositClientImpl implements GatewayDepositClient {

    /** 网关信息 */
    private final GatewayFacade gatewayFacade;

    public GatewayDepositClientImpl(GatewayFacade gatewayFacade) {
        this.gatewayFacade = gatewayFacade;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayDepositResultSnapshot initiateDeposit(GatewayDepositInitiateRequest request) {
        return toSnapshot(gatewayFacade.initiateDeposit(new GatewayDepositInitiateCommand(
                request.inboundId(),
                request.instChannelCode(),
                request.payerUserId(),
                request.payerAccountNo(),
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
    public GatewayDepositResultSnapshot queryDeposit(GatewayDepositQueryRequest request) {
        return toSnapshot(gatewayFacade.queryDeposit(new GatewayDepositQueryCommand(
                request.inboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayDepositResultSnapshot confirmDeposit(GatewayDepositConfirmRequest request) {
        return toSnapshot(gatewayFacade.confirmDeposit(new GatewayDepositConfirmCommand(
                request.inboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayDepositResultSnapshot cancelDeposit(GatewayDepositCancelRequest request) {
        return toSnapshot(gatewayFacade.cancelDeposit(new GatewayDepositCancelCommand(
                request.inboundId(),
                request.instChannelCode(),
                request.reason()
        )));
    }

    private GatewayDepositResultSnapshot toSnapshot(GatewayDepositResultDTO result) {
        return new GatewayDepositResultSnapshot(
                result.success(),
                result.resultCode(),
                result.resultDescription(),
                result.instId(),
                result.instSerialNo(),
                result.instRefNo(),
                result.instChannelCode(),
                result.inboundOrderNo(),
                result.gmtResp(),
                result.gmtSettle()
        );
    }
}
