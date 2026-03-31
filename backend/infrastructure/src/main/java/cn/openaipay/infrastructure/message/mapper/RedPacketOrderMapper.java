package cn.openaipay.infrastructure.message.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.message.dataobject.RedPacketOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 红包订单持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Mapper
public interface RedPacketOrderMapper extends BaseMapper<RedPacketOrderDO> {

    /**
     * 按红包单号查询。
     */
    default Optional<RedPacketOrderDO> findByRedPacketNo(String redPacketNo) {
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("red_packet_no", redPacketNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按消息ID查询。
     */
    default Optional<RedPacketOrderDO> findByMessageId(String messageId) {
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("message_id", messageId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按发红包资金交易号查询。
     */
    default Optional<RedPacketOrderDO> findByFundingTradeNo(String fundingTradeNo) {
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("funding_trade_no", fundingTradeNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按发红包资金交易号批量查询。
     */
    default List<RedPacketOrderDO> findByFundingTradeNos(List<String> fundingTradeNos) {
        if (fundingTradeNos == null || fundingTradeNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.in("funding_trade_no", fundingTradeNos);
        return selectList(wrapper);
    }

    /**
     * 按领取交易号查询。
     */
    default Optional<RedPacketOrderDO> findByClaimTradeNo(String claimTradeNo) {
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("claim_trade_no", claimTradeNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按领取交易号批量查询。
     */
    default List<RedPacketOrderDO> findByClaimTradeNos(List<String> claimTradeNos) {
        if (claimTradeNos == null || claimTradeNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        wrapper.in("claim_trade_no", claimTradeNos);
        return selectList(wrapper);
    }
}
