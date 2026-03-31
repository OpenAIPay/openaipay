package cn.openaipay.application.gateway.facade;

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

/**
 * 银行网关门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface GatewayFacade {

    /**
     * 处理业务数据。
     */
    GatewayDepositResultDTO initiateDeposit(GatewayDepositInitiateCommand command);

    /**
     * 查询业务数据。
     */
    GatewayDepositResultDTO queryDeposit(GatewayDepositQueryCommand command);

    /**
     * 确认业务数据。
     */
    GatewayDepositResultDTO confirmDeposit(GatewayDepositConfirmCommand command);

    /**
     * 取消业务数据。
     */
    GatewayDepositResultDTO cancelDeposit(GatewayDepositCancelCommand command);

    /**
     * 处理业务数据。
     */
    GatewayWithdrawResultDTO initiateWithdraw(GatewayWithdrawInitiateCommand command);

    /**
     * 查询业务数据。
     */
    GatewayWithdrawResultDTO queryWithdraw(GatewayWithdrawQueryCommand command);

    /**
     * 确认业务数据。
     */
    GatewayWithdrawResultDTO confirmWithdraw(GatewayWithdrawConfirmCommand command);

    /**
     * 取消业务数据。
     */
    GatewayWithdrawResultDTO cancelWithdraw(GatewayWithdrawCancelCommand command);
}
