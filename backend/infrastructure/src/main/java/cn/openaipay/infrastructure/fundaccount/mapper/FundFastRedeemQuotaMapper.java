package cn.openaipay.infrastructure.fundaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundFastRedeemQuotaDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金Fast赎回额度持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FundFastRedeemQuotaMapper extends BaseMapper<FundFastRedeemQuotaDO> {

    /**
     * 按基金编码与额度日期查找记录。
     */
    default Optional<FundFastRedeemQuotaDO> findByFundCodeAndQuotaDate(String fundCode, LocalDate quotaDate) {
        QueryWrapper<FundFastRedeemQuotaDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("quota_date", quotaDate);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按基金编码与额度日期查找记录并加锁。
     */
    default Optional<FundFastRedeemQuotaDO> findByFundCodeAndQuotaDateForUpdate(String fundCode, LocalDate quotaDate) {
        QueryWrapper<FundFastRedeemQuotaDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("quota_date", quotaDate);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
