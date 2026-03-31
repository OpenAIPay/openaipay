package cn.openaipay.domain.message.repository;

import cn.openaipay.domain.message.model.RedPacketOrder;
import java.util.List;
import java.util.Optional;

/**
 * 红包订单仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface RedPacketOrderRepository {

    /**
     * 按红包单号查询红包订单。
     */
    Optional<RedPacketOrder> findByRedPacketNo(String redPacketNo);

    /**
     * 按消息ID查询红包订单。
     */
    Optional<RedPacketOrder> findByMessageId(String messageId);

    /**
     * 按发红包资金交易号查询红包订单。
     */
    Optional<RedPacketOrder> findByFundingTradeNo(String fundingTradeNo);

    /**
     * 按发红包资金交易号批量查询红包订单。
     */
    List<RedPacketOrder> findByFundingTradeNos(List<String> fundingTradeNos);

    /**
     * 按领取交易号查询红包订单。
     */
    Optional<RedPacketOrder> findByClaimTradeNo(String claimTradeNo);

    /**
     * 按领取交易号批量查询红包订单。
     */
    List<RedPacketOrder> findByClaimTradeNos(List<String> claimTradeNos);

    /**
     * 保存红包订单。
     */
    RedPacketOrder save(RedPacketOrder redPacketOrder);
}
