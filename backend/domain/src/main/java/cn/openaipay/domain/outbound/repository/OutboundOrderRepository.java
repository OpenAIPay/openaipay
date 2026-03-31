package cn.openaipay.domain.outbound.repository;

import cn.openaipay.domain.outbound.model.OutboundOrder;

import java.util.Optional;

/**
 * 出金订单仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface OutboundOrderRepository {

    /**
     * 保存业务数据。
     */
    OutboundOrder save(OutboundOrder order);

    /**
     * 按出金ID查找记录。
     */
    Optional<OutboundOrder> findByOutboundId(String outboundId);

    /**
     * 按请求业务单号查找记录。
     */
    Optional<OutboundOrder> findByRequestBizNo(String requestBizNo);

    /**
     * 按支付订单单号查找记录。
     */
    Optional<OutboundOrder> findByPayOrderNo(String payOrderNo);
}
