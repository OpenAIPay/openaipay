package cn.openaipay.infrastructure.fundaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundAccountDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金账户持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FundAccountMapper extends BaseMapper<FundAccountDO> {

    /**
     * 按用户ID与基金编码查找记录。
     */
    default Optional<FundAccountDO> findByUserIdAndFundCode(Long userId, String fundCode) {
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("fund_code", fundCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default List<FundAccountDO> findByUserId(Long userId) {
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return selectList(wrapper);
    }

    /**
     * 按基金编码查找记录。
     */
    default List<FundAccountDO> findByFundCode(String fundCode) {
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fund_code", fundCode);
        return selectList(wrapper);
    }

    /**
     * 按用户ID与基金编码查找记录并加锁。
     */
    default Optional<FundAccountDO> findByUserIdAndFundCodeForUpdate(Long userId, String fundCode) {
        QueryWrapper<FundAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("fund_code", fundCode);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
