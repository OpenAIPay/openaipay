package cn.openaipay.application.pay.service;

/**
 * 支付待对账主单兜底续跑服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface PayReconSweepService {

    /**
     * 扫描并续跑待对账支付单。
     *
     * @param limit 本次最多处理数量
     * @return 实际触发处理的支付单数量
     */
    int sweepReconPendingPayments(int limit);
}

