package cn.openaipay.adapter.gateway.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.gateway.web.request.GatewayDepositCancelRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayDepositConfirmRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayDepositInitiateRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayDepositQueryRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayWithdrawCancelRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayWithdrawConfirmRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayWithdrawInitiateRequest;
import cn.openaipay.adapter.gateway.web.request.GatewayWithdrawQueryRequest;
import cn.openaipay.application.gateway.command.GatewayDepositCancelCommand;
import cn.openaipay.application.gateway.command.GatewayDepositConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayDepositInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayDepositQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayDepositResultDTO;
import cn.openaipay.application.gateway.command.GatewayWithdrawCancelCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayWithdrawResultDTO;
import cn.openaipay.application.gateway.facade.GatewayFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 银行网关控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {
    /** 网关门面。 */
    private final GatewayFacade gatewayFacade;

    /** 创建网关控制器并注入网关门面。 */
    public GatewayController(GatewayFacade gatewayFacade) {
        this.gatewayFacade = gatewayFacade;
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/deposit/initiate")
    public ApiResponse<GatewayDepositResultDTO> initiateDeposit(@Valid @RequestBody GatewayDepositInitiateRequest request) {
        GatewayDepositResultDTO result = gatewayFacade.initiateDeposit(new GatewayDepositInitiateCommand(
                request.inboundId(),
                request.instChannelCode(),
                request.payerUserId(),
                request.payerAccountNo(),
                request.amount(),
                request.payChannelCode(),
                request.requestIdentify(),
                request.bizIdentity()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 查询业务数据。
     */
    @PostMapping("/deposit/query")
    public ApiResponse<GatewayDepositResultDTO> queryDeposit(@Valid @RequestBody GatewayDepositQueryRequest request) {
        return ApiResponse.success(gatewayFacade.queryDeposit(new GatewayDepositQueryCommand(
                request.inboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 确认业务数据。
     */
    @PostMapping("/deposit/confirm")
    public ApiResponse<GatewayDepositResultDTO> confirmDeposit(@Valid @RequestBody GatewayDepositConfirmRequest request) {
        return ApiResponse.success(gatewayFacade.confirmDeposit(new GatewayDepositConfirmCommand(
                request.inboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/deposit/cancel")
    public ApiResponse<GatewayDepositResultDTO> cancelDeposit(@Valid @RequestBody GatewayDepositCancelRequest request) {
        return ApiResponse.success(gatewayFacade.cancelDeposit(new GatewayDepositCancelCommand(
                request.inboundId(),
                request.instChannelCode(),
                request.reason()
        )));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/withdraw/initiate")
    public ApiResponse<GatewayWithdrawResultDTO> initiateWithdraw(@Valid @RequestBody GatewayWithdrawInitiateRequest request) {
        GatewayWithdrawResultDTO result = gatewayFacade.initiateWithdraw(new GatewayWithdrawInitiateCommand(
                request.outboundId(),
                request.instChannelCode(),
                request.payerUserId(),
                request.payeeAccountNo(),
                request.amount(),
                request.payChannelCode(),
                request.requestIdentify(),
                request.bizIdentity()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 查询业务数据。
     */
    @PostMapping("/withdraw/query")
    public ApiResponse<GatewayWithdrawResultDTO> queryWithdraw(@Valid @RequestBody GatewayWithdrawQueryRequest request) {
        return ApiResponse.success(gatewayFacade.queryWithdraw(new GatewayWithdrawQueryCommand(
                request.outboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 确认业务数据。
     */
    @PostMapping("/withdraw/confirm")
    public ApiResponse<GatewayWithdrawResultDTO> confirmWithdraw(@Valid @RequestBody GatewayWithdrawConfirmRequest request) {
        return ApiResponse.success(gatewayFacade.confirmWithdraw(new GatewayWithdrawConfirmCommand(
                request.outboundId(),
                request.instChannelCode()
        )));
    }

    /**
     * 取消业务数据。
     */
    @PostMapping("/withdraw/cancel")
    public ApiResponse<GatewayWithdrawResultDTO> cancelWithdraw(@Valid @RequestBody GatewayWithdrawCancelRequest request) {
        return ApiResponse.success(gatewayFacade.cancelWithdraw(new GatewayWithdrawCancelCommand(
                request.outboundId(),
                request.instChannelCode(),
                request.reason()
        )));
    }
}
