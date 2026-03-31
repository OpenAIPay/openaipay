package cn.openaipay.domain.pay.client;

/**
 * OutboundClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface OutboundClient {
    /**
     * 提交业务数据。
     */
    PayOutboundOrderSnapshot submitWithdraw(PayOutboundSubmitRequest request);

    /**
     * 取消业务数据。
     */
    PayOutboundOrderSnapshot cancelWithdraw(String outboundId, String reason);

    /**
     * 按请求业务单号查询记录。
     */
    PayOutboundOrderSnapshot queryByRequestBizNo(String requestBizNo);
}
