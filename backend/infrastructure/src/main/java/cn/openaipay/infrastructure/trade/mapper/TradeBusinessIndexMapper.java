package cn.openaipay.infrastructure.trade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.trade.dataobject.TradeBusinessIndexDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 业务交易查询索引持久化接口。
 *
 * 业务场景：按业务域和业务单号快速查询交易，避免从统一交易主单反向组装业务数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface TradeBusinessIndexMapper extends BaseMapper<TradeBusinessIndexDO> {
    /** 红包业务域编码。 */
    String RED_PACKET_DOMAIN_CODE = "RED_PACKET";
    /** 红包领取展示标题关键字。 */
    String RED_PACKET_CLAIM_TITLE_KEYWORD = "RED_PACKET_CLAIM";

    @Insert("""
            INSERT INTO trade_bill_index (
                trade_order_no,
                business_domain_code,
                biz_order_no,
                product_type,
                business_type,
                user_id,
                counterparty_user_id,
                account_no,
                bill_no,
                bill_month,
                display_title,
                display_subtitle,
                amount,
                status,
                trade_time,
                created_at,
                updated_at
            ) VALUES (
                #{tradeOrderNo},
                #{businessDomainCode},
                #{bizOrderNo},
                #{productType},
                #{businessType},
                #{userId},
                #{counterpartyUserId},
                #{accountNo},
                #{billNo},
                #{billMonth},
                #{displayTitle},
                #{displaySubtitle},
                #{amount},
                #{status},
                #{tradeTime},
                #{createdAt},
                #{updatedAt}
            )
            ON DUPLICATE KEY UPDATE
                id = LAST_INSERT_ID(id),
                trade_order_no = VALUES(trade_order_no),
                business_domain_code = VALUES(business_domain_code),
                biz_order_no = VALUES(biz_order_no),
                product_type = COALESCE(VALUES(product_type), product_type),
                business_type = COALESCE(VALUES(business_type), business_type),
                user_id = VALUES(user_id),
                counterparty_user_id = COALESCE(VALUES(counterparty_user_id), counterparty_user_id),
                account_no = COALESCE(VALUES(account_no), account_no),
                bill_no = COALESCE(VALUES(bill_no), bill_no),
                bill_month = COALESCE(VALUES(bill_month), bill_month),
                display_title = COALESCE(VALUES(display_title), display_title),
                display_subtitle = COALESCE(VALUES(display_subtitle), display_subtitle),
                amount = VALUES(amount),
                status = VALUES(status),
                trade_time = VALUES(trade_time),
                updated_at = VALUES(updated_at)
            """)
    /**
     * 保存或更新业务数据。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(TradeBusinessIndexDO entity);

    /**
     * 按交易订单单号查找记录。
     */
    default Optional<TradeBusinessIndexDO> findByTradeOrderNo(String tradeOrderNo) {
        QueryWrapper<TradeBusinessIndexDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按交易订单单号和用户ID查找记录。
     */
    default Optional<TradeBusinessIndexDO> findByTradeOrderNoAndUserId(String tradeOrderNo, Long userId) {
        QueryWrapper<TradeBusinessIndexDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo)
                .eq("user_id", userId)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按订单查找记录。
     */
    default Optional<TradeBusinessIndexDO> findByBusinessOrder(String businessDomainCode, String bizOrderNo) {
        QueryWrapper<TradeBusinessIndexDO> wrapper = new QueryWrapper<>();
        wrapper.eq("business_domain_code", businessDomainCode)
                .eq("biz_order_no", bizOrderNo)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default List<TradeBusinessIndexDO> findRecentByUserId(Long userId,
                                                           String billMonth,
                                                           String businessDomainCode,
                                                           int limit) {
        return findByUserIdWithOffset(userId, billMonth, businessDomainCode, 0, limit);
    }

    /**
     * 按用户ID分页查找记录。
     */
    default List<TradeBusinessIndexDO> findByUserIdWithOffset(Long userId,
                                                               String billMonth,
                                                               String businessDomainCode,
                                                               int offset,
                                                               int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        int normalizedOffset = Math.max(0, offset);
        QueryWrapper<TradeBusinessIndexDO> wrapper = new QueryWrapper<>();
        applyBillQueryUserScope(wrapper, userId);
        wrapper.eq("status", "SUCCEEDED");
        if (billMonth != null && !billMonth.isBlank()) {
            wrapper.eq("bill_month", billMonth);
        }
        if (businessDomainCode != null && !businessDomainCode.isBlank()) {
            wrapper.eq("business_domain_code", businessDomainCode);
        }
        wrapper.orderByDesc("trade_time")
                .orderByDesc("id")
                .last("LIMIT " + normalizedOffset + "," + normalizedLimit);
        return selectList(wrapper);
    }

    /**
     * 按用户ID和游标分页查找记录。
     */
    default List<TradeBusinessIndexDO> findByUserIdAfterCursor(Long userId,
                                                                String billMonth,
                                                                String businessDomainCode,
                                                                LocalDateTime cursorTradeTime,
                                                                Long cursorId,
                                                                int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        QueryWrapper<TradeBusinessIndexDO> wrapper = new QueryWrapper<>();
        applyBillQueryUserScope(wrapper, userId);
        wrapper.eq("status", "SUCCEEDED");
        if (billMonth != null && !billMonth.isBlank()) {
            wrapper.eq("bill_month", billMonth);
        }
        if (businessDomainCode != null && !businessDomainCode.isBlank()) {
            wrapper.eq("business_domain_code", businessDomainCode);
        }
        wrapper.apply("(trade_time < {0} OR (trade_time = {0} AND id < {1}))", cursorTradeTime, cursorId);
        wrapper.orderByDesc("trade_time")
                .orderByDesc("id")
                .last("LIMIT " + normalizedLimit);
        return selectList(wrapper);
    }

    private static void applyBillQueryUserScope(QueryWrapper<TradeBusinessIndexDO> wrapper, Long userId) {
        wrapper.and(scope -> scope
                .eq("user_id", userId)
                .or(claimScope -> claimScope
                        .eq("counterparty_user_id", userId)
                        .eq("business_domain_code", RED_PACKET_DOMAIN_CODE)
                        .like("display_title", RED_PACKET_CLAIM_TITLE_KEYWORD))
        );
    }
}
