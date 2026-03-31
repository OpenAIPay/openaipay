package cn.openaipay.infrastructure.outbound.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.outbound.dataobject.OutboundOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 出金订单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrderDO> {

    /**
     * 按出金ID查找记录。
     */
    default Optional<OutboundOrderDO> findByOutboundId(String outboundId) {
        QueryWrapper<OutboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("outbound_id", outboundId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按请求业务单号查找记录。
     */
    default Optional<OutboundOrderDO> findByRequestBizNo(String requestBizNo) {
        QueryWrapper<OutboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("request_biz_no", requestBizNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按支付订单单号查找记录。
     */
    default Optional<OutboundOrderDO> findByPayOrderNo(String payOrderNo) {
        QueryWrapper<OutboundOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
