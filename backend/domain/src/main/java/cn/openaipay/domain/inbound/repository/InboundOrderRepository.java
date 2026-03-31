package cn.openaipay.domain.inbound.repository;

import cn.openaipay.domain.inbound.model.InboundOrder;

import java.util.Optional;

/**
 * 入金订单仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface InboundOrderRepository {

    /**
     * 保存入金订单。
     */
    InboundOrder save(InboundOrder order);

    /**
     * 按入金流水号查询订单。
     */
    Optional<InboundOrder> findByInboundId(String inboundId);

    /**
     * 按业务请求号查询订单。
     */
    Optional<InboundOrder> findByRequestBizNo(String requestBizNo);

    /**
     * 按支付单号查询订单。
     */
    Optional<InboundOrder> findByPayOrderNo(String payOrderNo);
}
