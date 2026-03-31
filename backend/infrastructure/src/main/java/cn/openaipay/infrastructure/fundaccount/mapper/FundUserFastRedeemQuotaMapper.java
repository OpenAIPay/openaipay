package cn.openaipay.infrastructure.fundaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundUserFastRedeemQuotaDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金用户Fast赎回额度持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FundUserFastRedeemQuotaMapper extends BaseMapper<FundUserFastRedeemQuotaDO> {

    /**
     * 按基金编码与用户ID与额度日期查找记录。
     */
    default Optional<FundUserFastRedeemQuotaDO> findByFundCodeAndUserIdAndQuotaDate(String fundCode, Long userId, LocalDate quotaDate) {
        QueryWrapper<FundUserFastRedeemQuotaDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("user_id", userId);
        wrapper.eq("quota_date", quotaDate);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按基金编码与用户ID与额度日期查找记录并加锁。
     */
    default Optional<FundUserFastRedeemQuotaDO> findByFundCodeAndUserIdAndQuotaDateForUpdate(String fundCode, Long userId, LocalDate quotaDate) {
        QueryWrapper<FundUserFastRedeemQuotaDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("user_id", userId);
        wrapper.eq("quota_date", quotaDate);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
