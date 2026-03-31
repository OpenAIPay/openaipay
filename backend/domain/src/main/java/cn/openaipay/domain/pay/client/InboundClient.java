package cn.openaipay.domain.pay.client;

/**
 * InboundClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface InboundClient {
    /**
     * 提交业务数据。
     */
    PayInboundOrderSnapshot submitDeposit(PayInboundSubmitRequest request);

    /**
     * 取消业务数据。
     */
    PayInboundOrderSnapshot cancelDeposit(String inboundId, String reason);

    /**
     * 按请求业务单号查询记录。
     */
    PayInboundOrderSnapshot queryByRequestBizNo(String requestBizNo);
}
