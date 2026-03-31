package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayWalletFundDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 钱包资金明细持久化接口
 *
 * 业务场景：按summary_id维护钱包出资明细，与支付资金摘要表形成1:1关系。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface PayWalletFundDetailMapper extends BaseMapper<PayWalletFundDetailDO> {

    /**
     * 按汇总ID查找记录。
     */
    default Optional<PayWalletFundDetailDO> findBySummaryId(Long summaryId) {
        QueryWrapper<PayWalletFundDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("summary_id", summaryId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
