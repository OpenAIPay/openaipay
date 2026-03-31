package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayFundDetailSummaryDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付资金明细摘要持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface PayFundDetailSummaryMapper extends BaseMapper<PayFundDetailSummaryDO> {

    /**
     * 按支付订单单号订单IDASC查找记录。
     */
    default List<PayFundDetailSummaryDO> findByPayOrderNoOrderByIdAsc(String payOrderNo) {
        QueryWrapper<PayFundDetailSummaryDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 按支付订单单号与支付与明细查找记录。
     */
    default Optional<PayFundDetailSummaryDO> findByPayOrderNoAndPayToolAndDetailOwner(String payOrderNo, String payTool, String detailOwner) {
        QueryWrapper<PayFundDetailSummaryDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.eq("pay_tool", payTool);
        wrapper.eq("detail_owner", detailOwner);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
