package cn.openaipay.infrastructure.fundaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.fundaccount.dataobject.FundTransactionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基金交易持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FundTransactionMapper extends BaseMapper<FundTransactionDO> {

    /**
     * 按订单单号查找记录。
     */
    default Optional<FundTransactionDO> findByOrderNo(String orderNo) {
        QueryWrapper<FundTransactionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按订单单号查找记录并加锁。
     */
    default Optional<FundTransactionDO> findByOrderNoForUpdate(String orderNo) {
        QueryWrapper<FundTransactionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按单号查找记录。
     */
    default Optional<FundTransactionDO> findByBusinessNoAndType(Long userId,
                                                                String fundCode,
                                                                String transactionType,
                                                                String businessNo) {
        QueryWrapper<FundTransactionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("transaction_type", transactionType);
        wrapper.eq("business_no", businessNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按条件查找记录。
     */
    default List<FundTransactionDO> findRecentByTypes(String fundCode, List<String> transactionTypes, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 2000));
        QueryWrapper<FundTransactionDO> wrapper = new QueryWrapper<>();
        if (fundCode != null && !fundCode.isBlank()) {
            wrapper.eq("fund_code", fundCode.trim().toUpperCase(Locale.ROOT));
        }
        if (transactionTypes != null && !transactionTypes.isEmpty()) {
            wrapper.in("transaction_type", transactionTypes);
        }
        wrapper.orderByDesc("updated_at")
                .orderByDesc("id")
                .last("LIMIT " + normalizedLimit);
        return selectList(wrapper);
    }

    /**
     * 按用户、基金、状态、更新时间范围查找交易。
     */
    default List<FundTransactionDO> findByStatusAndUpdatedRange(Long userId,
                                                                String fundCode,
                                                                String transactionStatus,
                                                                LocalDateTime updatedFromInclusive,
                                                                LocalDateTime updatedToInclusive,
                                                                List<String> transactionTypes) {
        QueryWrapper<FundTransactionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("fund_code", fundCode);
        wrapper.eq("transaction_status", transactionStatus);
        if (updatedFromInclusive != null) {
            wrapper.ge("updated_at", updatedFromInclusive);
        }
        if (updatedToInclusive != null) {
            wrapper.le("updated_at", updatedToInclusive);
        }
        if (transactionTypes != null && !transactionTypes.isEmpty()) {
            wrapper.in("transaction_type", transactionTypes);
        }
        wrapper.orderByAsc("updated_at")
                .orderByAsc("id")
                .last("LIMIT 2000");
        return selectList(wrapper);
    }
}
