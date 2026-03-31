package cn.openaipay.infrastructure.bankcard.mapper;

import cn.openaipay.infrastructure.bankcard.dataobject.BankCardDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 银行卡持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface BankCardMapper extends BaseMapper<BankCardDO> {

    /**
     * 按单号查找记录。
     */
    default Optional<BankCardDO> findByCardNo(String cardNo) {
        QueryWrapper<BankCardDO> wrapper = new QueryWrapper<>();
        wrapper.eq("card_no", cardNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID订单ISIDASC查找记录。
     */
    default List<BankCardDO> findByUserIdOrderByIsDefaultDescIdAsc(Long userId) {
        QueryWrapper<BankCardDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("is_default");
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 按用户ID与状态订单ISIDASC查找记录。
     */
    default List<BankCardDO> findByUserIdAndCardStatusOrderByIsDefaultDescIdAsc(Long userId, String cardStatus) {
        QueryWrapper<BankCardDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("card_status", cardStatus);
        wrapper.orderByDesc("is_default");
        wrapper.orderByAsc("id");
        return selectList(wrapper);
    }
}
