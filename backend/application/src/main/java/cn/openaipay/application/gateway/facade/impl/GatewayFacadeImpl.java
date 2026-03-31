package cn.openaipay.application.gateway.facade.impl;

import cn.openaipay.application.gateway.command.GatewayDepositCancelCommand;
import cn.openaipay.application.gateway.command.GatewayDepositConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayDepositInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayDepositQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayDepositResultDTO;
import cn.openaipay.application.gateway.facade.GatewayFacade;
import cn.openaipay.application.gateway.command.GatewayWithdrawCancelCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawConfirmCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawInitiateCommand;
import cn.openaipay.application.gateway.command.GatewayWithdrawQueryCommand;
import cn.openaipay.application.gateway.dto.GatewayWithdrawResultDTO;
import cn.openaipay.application.gateway.service.GatewayService;
import org.springframework.stereotype.Service;

/**
 * 银行网关门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class GatewayFacadeImpl implements GatewayFacade {
    /** 网关应用服务。 */
    private final GatewayService gatewayService;

    public GatewayFacadeImpl(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayDepositResultDTO initiateDeposit(GatewayDepositInitiateCommand command) {
        return gatewayService.initiateDeposit(command);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public GatewayDepositResultDTO queryDeposit(GatewayDepositQueryCommand command) {
        return gatewayService.queryDeposit(command);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayDepositResultDTO confirmDeposit(GatewayDepositConfirmCommand command) {
        return gatewayService.confirmDeposit(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayDepositResultDTO cancelDeposit(GatewayDepositCancelCommand command) {
        return gatewayService.cancelDeposit(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO initiateWithdraw(GatewayWithdrawInitiateCommand command) {
        return gatewayService.initiateWithdraw(command);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO queryWithdraw(GatewayWithdrawQueryCommand command) {
        return gatewayService.queryWithdraw(command);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO confirmWithdraw(GatewayWithdrawConfirmCommand command) {
        return gatewayService.confirmWithdraw(command);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public GatewayWithdrawResultDTO cancelWithdraw(GatewayWithdrawCancelCommand command) {
        return gatewayService.cancelWithdraw(command);
    }
}
