package cn.openaipay.domain.outbound.client;

/**
 * GatewayWithdrawClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface GatewayWithdrawClient {
    /**
     * 处理业务数据。
     */
    GatewayWithdrawResultSnapshot initiateWithdraw(GatewayWithdrawInitiateRequest request);

    /**
     * 查询业务数据。
     */
    GatewayWithdrawResultSnapshot queryWithdraw(GatewayWithdrawQueryRequest request);

    /**
     * 确认业务数据。
     */
    GatewayWithdrawResultSnapshot confirmWithdraw(GatewayWithdrawConfirmRequest request);

    /**
     * 取消业务数据。
     */
    GatewayWithdrawResultSnapshot cancelWithdraw(GatewayWithdrawCancelRequest request);
}
