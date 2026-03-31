package cn.openaipay.infrastructure.walletaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletAccountDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包账户持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccountDO> {

    /**
     * 按用户ID查找记录。
     */
    default Optional<WalletAccountDO> findByUserId(Long userId) {
        return findByUserIdAndCurrency(userId, "CNY");
    }

    /**
     * 按用户ID与币种查找记录。
     */
    default Optional<WalletAccountDO> findByUserIdAndCurrency(Long userId, String currencyCode) {
        QueryWrapper<WalletAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("currency_code", currencyCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录并加锁。
     */
    default Optional<WalletAccountDO> findByUserIdForUpdate(Long userId) {
        return findByUserIdAndCurrencyForUpdate(userId, "CNY");
    }

    /**
     * 按用户ID与币种查找记录并加锁。
     */
    default Optional<WalletAccountDO> findByUserIdAndCurrencyForUpdate(Long userId, String currencyCode) {
        QueryWrapper<WalletAccountDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("currency_code", currencyCode);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
