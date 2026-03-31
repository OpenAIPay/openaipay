package cn.openaipay.infrastructure.trade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.trade.dataobject.TradeCreditOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 信用业务交易扩展单持久化接口。
 *
 * 业务场景：按统一交易号或业务单号查询爱花、爱借等信用交易扩展数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface TradeCreditOrderMapper extends BaseMapper<TradeCreditOrderDO> {

    /**
     * 按交易订单单号查找记录。
     */
    default Optional<TradeCreditOrderDO> findByTradeOrderNo(String tradeOrderNo) {
        QueryWrapper<TradeCreditOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务订单单号查找记录。
     */
    default Optional<TradeCreditOrderDO> findByBizOrderNo(String bizOrderNo) {
        QueryWrapper<TradeCreditOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_order_no", bizOrderNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按与交易查找记录。
     */
    default Optional<TradeCreditOrderDO> findLatestByAccountAndTradeType(String creditAccountNo, String creditTradeType) {
        QueryWrapper<TradeCreditOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("credit_account_no", creditAccountNo);
        wrapper.eq("credit_trade_type", creditTradeType);
        wrapper.orderByDesc("occurred_at");
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
