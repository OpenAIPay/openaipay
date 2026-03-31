package cn.openaipay.infrastructure.fundaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundIncomeCalendarDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金收益日历持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FundIncomeCalendarMapper extends BaseMapper<FundIncomeCalendarDO> {

    /**
     * 按基金编码与业务日期查找记录。
     */
    default Optional<FundIncomeCalendarDO> findByFundCodeAndBizDate(String fundCode, LocalDate bizDate) {
        QueryWrapper<FundIncomeCalendarDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("biz_date", bizDate);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按基金编码与业务日期查找记录并加锁。
     */
    default Optional<FundIncomeCalendarDO> findByFundCodeAndBizDateForUpdate(String fundCode, LocalDate bizDate) {
        QueryWrapper<FundIncomeCalendarDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("biz_date", bizDate);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
