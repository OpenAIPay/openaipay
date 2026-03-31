package cn.openaipay.domain.pay.client;

/**
 * AccountingClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingClient {
    /**
     * 处理事件信息。
     */
    void acceptEvent(PayAccountingEventRequest request);
}
