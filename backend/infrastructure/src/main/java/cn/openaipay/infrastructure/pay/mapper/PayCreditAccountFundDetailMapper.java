package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayCreditAccountFundDetailDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * PayCreditAccountFundDetailMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Mapper
public interface PayCreditAccountFundDetailMapper extends BaseMapper<PayCreditAccountFundDetailDO> {

    /**
     * 按汇总ID查找记录。
     */
    default Optional<PayCreditAccountFundDetailDO> findBySummaryId(Long summaryId) {
        QueryWrapper<PayCreditAccountFundDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("summary_id", summaryId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
