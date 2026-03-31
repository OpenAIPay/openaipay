package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayFundAccountFundDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * PayFundAccountFundDetailMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Mapper
public interface PayFundAccountFundDetailMapper extends BaseMapper<PayFundAccountFundDetailDO> {

    /**
     * 按汇总ID查找记录。
     */
    default Optional<PayFundAccountFundDetailDO> findBySummaryId(Long summaryId) {
        QueryWrapper<PayFundAccountFundDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("summary_id", summaryId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
