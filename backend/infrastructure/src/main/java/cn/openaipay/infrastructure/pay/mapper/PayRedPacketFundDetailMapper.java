package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayRedPacketFundDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 红包资金明细持久化接口
 *
 * 业务场景：按summary_id维护红包出资明细，保障支付明细可回溯红包来源。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface PayRedPacketFundDetailMapper extends BaseMapper<PayRedPacketFundDetailDO> {

    /**
     * 按汇总ID查找记录。
     */
    default Optional<PayRedPacketFundDetailDO> findBySummaryId(Long summaryId) {
        QueryWrapper<PayRedPacketFundDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("summary_id", summaryId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
