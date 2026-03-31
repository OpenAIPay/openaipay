package cn.openaipay.domain.settle.service;

/**
 * 结算入账领域服务。
 *
 * 负责根据交易类型、金额与对手信息，决定支付成功后的入账执行计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface SettleDomainService {

    /**
     * 解析交易支付成功后的结算计划。
     */
    SettlePlan resolveCommittedTradePlan(SettleRequest request);
}
