package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayBankCardFundDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 银行卡资金明细持久化接口
 *
 * 业务场景：按summary_id维护银行卡出资明细，承载渠道流水与渠道费用字段。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface PayBankCardFundDetailMapper extends BaseMapper<PayBankCardFundDetailDO> {

    /**
     * 按汇总ID查找记录。
     */
    default Optional<PayBankCardFundDetailDO> findBySummaryId(Long summaryId) {
        QueryWrapper<PayBankCardFundDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("summary_id", summaryId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
