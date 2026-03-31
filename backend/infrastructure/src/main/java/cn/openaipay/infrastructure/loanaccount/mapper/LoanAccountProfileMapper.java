package cn.openaipay.infrastructure.loanaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.loanaccount.dataobject.LoanAccountProfileDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爱借账户档案持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Mapper
public interface LoanAccountProfileMapper extends BaseMapper<LoanAccountProfileDO> {

    /**
     * 按账户号查询档案。
     */
    default Optional<LoanAccountProfileDO> findByAccountNo(String accountNo) {
        QueryWrapper<LoanAccountProfileDO> wrapper = new QueryWrapper<>();
        wrapper.eq("account_no", accountNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按账户号查询档案并加锁。
     */
    default Optional<LoanAccountProfileDO> findByAccountNoForUpdate(String accountNo) {
        QueryWrapper<LoanAccountProfileDO> wrapper = new QueryWrapper<>();
        wrapper.eq("account_no", accountNo);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
