package cn.openaipay.infrastructure.trade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.trade.dataobject.TradeFundOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 基金业务交易扩展单持久化接口。
 *
 * 业务场景：按统一交易号或业务单号查询爱存等基金业务交易扩展数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface TradeFundOrderMapper extends BaseMapper<TradeFundOrderDO> {

    /**
     * 按交易订单单号查找记录。
     */
    default Optional<TradeFundOrderDO> findByTradeOrderNo(String tradeOrderNo) {
        QueryWrapper<TradeFundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务订单单号查找记录。
     */
    default Optional<TradeFundOrderDO> findByBizOrderNo(String bizOrderNo) {
        QueryWrapper<TradeFundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_order_no", bizOrderNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
