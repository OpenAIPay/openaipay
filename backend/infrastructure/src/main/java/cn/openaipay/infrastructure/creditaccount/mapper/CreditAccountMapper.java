package cn.openaipay.infrastructure.creditaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditAccountDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 信用账户持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface CreditAccountMapper extends BaseMapper<CreditAccountDO> {

    /**
     * 按单号查找记录。
     */
    default Optional<CreditAccountDO> findByAccountNo(String accountNo) {
        QueryWrapper<CreditAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("account_no", accountNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default Optional<CreditAccountDO> findByUserId(Long userId) {
        return findByUserIdAndAccountNoPrefix(userId, "CA");
    }

    /**
     * 按用户ID与单号查找记录。
     */
    default Optional<CreditAccountDO> findByUserIdAndAccountNoPrefix(Long userId, String accountNoPrefix) {
        QueryWrapper<CreditAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.likeRight("account_no", accountNoPrefix);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按单号查找记录并加锁。
     */
    default Optional<CreditAccountDO> findByAccountNoForUpdate(String accountNo) {
        QueryWrapper<CreditAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("account_no", accountNo);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
