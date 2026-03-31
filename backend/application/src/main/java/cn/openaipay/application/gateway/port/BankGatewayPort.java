package cn.openaipay.application.gateway.port;

/**
 * 银行网关端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public interface BankGatewayPort {

    /**
     * 处理业务数据。
     */
    BankDepositResult initiateDeposit(BankDepositInitiateRequest request);

    /**
     * 查询业务数据。
     */
    BankDepositResult queryDeposit(BankDepositQueryRequest request);

    /**
     * 确认业务数据。
     */
    BankDepositResult confirmDeposit(BankDepositConfirmRequest request);

    /**
     * 取消业务数据。
     */
    BankDepositResult cancelDeposit(BankDepositCancelRequest request);

    /**
     * 处理业务数据。
     */
    BankWithdrawResult initiateWithdraw(BankWithdrawInitiateRequest request);

    /**
     * 查询业务数据。
     */
    BankWithdrawResult queryWithdraw(BankWithdrawQueryRequest request);

    /**
     * 确认业务数据。
     */
    BankWithdrawResult confirmWithdraw(BankWithdrawConfirmRequest request);

    /**
     * 取消业务数据。
     */
    BankWithdrawResult cancelWithdraw(BankWithdrawCancelRequest request);
}
