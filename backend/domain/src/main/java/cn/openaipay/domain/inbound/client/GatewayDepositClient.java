package cn.openaipay.domain.inbound.client;

/**
 * GatewayDepositClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface GatewayDepositClient {
    /**
     * 处理业务数据。
     */
    GatewayDepositResultSnapshot initiateDeposit(GatewayDepositInitiateRequest request);

    /**
     * 查询业务数据。
     */
    GatewayDepositResultSnapshot queryDeposit(GatewayDepositQueryRequest request);

    /**
     * 确认业务数据。
     */
    GatewayDepositResultSnapshot confirmDeposit(GatewayDepositConfirmRequest request);

    /**
     * 取消业务数据。
     */
    GatewayDepositResultSnapshot cancelDeposit(GatewayDepositCancelRequest request);
}
