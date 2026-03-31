package cn.openaipay.infrastructure.trade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.trade.dataobject.TradeFlowStepDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易流程步骤持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface TradeFlowStepMapper extends BaseMapper<TradeFlowStepDO> {

    /**
     * 按交易订单单号订单IDASC查找记录。
     */
    default List<TradeFlowStepDO> findByTradeOrderNoOrderByIdAsc(String tradeOrderNo) {
        QueryWrapper<TradeFlowStepDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo);
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }
}
