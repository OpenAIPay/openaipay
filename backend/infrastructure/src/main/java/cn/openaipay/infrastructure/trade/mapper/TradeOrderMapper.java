package cn.openaipay.infrastructure.trade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.trade.dataobject.TradeOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 统一交易主单持久化接口。
 *
 * 业务场景：统一管理 trade_order 表的读写，支撑交易编排、退款校验以及业务索引回查。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderDO> {

    /**
     * 显式插入统一交易主单。
     *
     * 业务场景：trade_order 经过多次扩展后包含较多 nullable/非 nullable 字段，显式 SQL 可避免动态列策略遗漏业务域字段。
     */
    @Insert("""
            INSERT INTO trade_order (
                trade_order_no,
                request_no,
                trade_type,
                business_scene_code,
                business_domain_code,
                biz_order_no,
                original_trade_order_no,
                payer_user_id,
                payee_user_id,
                payment_method,
                original_amount,
                fee_amount,
                payable_amount,
                settle_amount,
                split_plan_snapshot,
                pricing_quote_no,
                pay_order_no,
                last_pay_status_version,
                pay_result_code,
                pay_result_message,
                status,
                failure_reason,
                metadata,
                payment_tool_snapshot,
                created_at,
                updated_at
            ) VALUES (
                #{tradeOrderNo},
                #{requestNo},
                #{tradeType},
                #{businessSceneCode},
                #{businessDomainCode},
                #{bizOrderNo},
                #{originalTradeOrderNo},
                #{payerUserId},
                #{payeeUserId},
                #{paymentMethod},
                #{originalAmount},
                #{feeAmount},
                #{payableAmount},
                #{settleAmount},
                #{splitPlanSnapshot},
                #{pricingQuoteNo},
                #{payOrderNo},
                #{lastPayStatusVersion},
                #{payResultCode},
                #{payResultMessage},
                #{status},
                #{failureReason},
                #{metadata},
                #{paymentToolSnapshot},
                #{createdAt},
                #{updatedAt}
            )
            """)
    /**
     * 处理交易订单信息。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTradeOrder(TradeOrderDO entity);

    /**
     * 显式更新统一交易主单。
     *
     * 业务场景：交易主单在报价、支付预处理、成功/失败回写时会多次更新，需要稳定覆盖业务域与支付流水号字段。
     */
    @Update("""
            UPDATE trade_order
            SET trade_order_no = #{tradeOrderNo},
                request_no = #{requestNo},
                trade_type = #{tradeType},
                business_scene_code = #{businessSceneCode},
                business_domain_code = #{businessDomainCode},
                biz_order_no = #{bizOrderNo},
                original_trade_order_no = #{originalTradeOrderNo},
                payer_user_id = #{payerUserId},
                payee_user_id = #{payeeUserId},
                payment_method = #{paymentMethod},
                original_amount = #{originalAmount},
                fee_amount = #{feeAmount},
                payable_amount = #{payableAmount},
                settle_amount = #{settleAmount},
                split_plan_snapshot = #{splitPlanSnapshot},
                pricing_quote_no = #{pricingQuoteNo},
                pay_order_no = #{payOrderNo},
                last_pay_status_version = #{lastPayStatusVersion},
                pay_result_code = #{payResultCode},
                pay_result_message = #{payResultMessage},
                status = #{status},
                failure_reason = #{failureReason},
                metadata = #{metadata},
                payment_tool_snapshot = #{paymentToolSnapshot},
                created_at = #{createdAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    /**
     * 更新交易订单信息。
     */
    int updateTradeOrder(TradeOrderDO entity);

    /**
     * 保存业务数据。
     */
    @Override
    default TradeOrderDO save(TradeOrderDO entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getId() == null) {
          /**
           * 处理交易订单信息。
           */
            insertTradeOrder(entity);
            return entity;
        }
        int updatedRows = updateTradeOrder(entity);
        if (updatedRows <= 0) {
          /**
           * 处理交易订单信息。
           */
            insertTradeOrder(entity);
        }
        return entity;
    }

    /**
     * 按交易订单单号查找记录。
     */
    default Optional<TradeOrderDO> findByTradeOrderNo(String tradeOrderNo) {
        QueryWrapper<TradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按请求单号查找记录。
     */
    default Optional<TradeOrderDO> findByRequestNo(String requestNo) {
        QueryWrapper<TradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("request_no", requestNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按订单查找记录。
     */
    default Optional<TradeOrderDO> findByBusinessOrder(String businessDomainCode, String bizOrderNo) {
        QueryWrapper<TradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("business_domain_code", businessDomainCode)
                .eq("biz_order_no", bizOrderNo)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default List<TradeOrderDO> findRecentSucceededByUserId(Long userId, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        int branchLimit = Math.min(normalizedLimit * 2, 200);
        Map<String, TradeOrderDO> mergedTrades = new LinkedHashMap<>();
        findRecentSucceededByPayerUserId(userId, branchLimit)
                .forEach(tradeOrder -> mergedTrades.put(tradeOrder.getTradeOrderNo(), tradeOrder));
        findRecentSucceededByPayeeUserId(userId, branchLimit)
                .forEach(tradeOrder -> mergedTrades.putIfAbsent(tradeOrder.getTradeOrderNo(), tradeOrder));
        return mergedTrades.values().stream()
                .sorted(RECENT_SUCCEEDED_TRADE_COMPARATOR)
                .limit(normalizedLimit)
                .toList();
    }

    /**
     * 按付款方用户ID查找记录。
     */
    default List<TradeOrderDO> findRecentSucceededByPayerUserId(Long userId, int limit) {
        return findRecentSucceededByParticipant("payer_user_id", userId, limit);
    }

    /**
     * 按收款方用户ID查找记录。
     */
    default List<TradeOrderDO> findRecentSucceededByPayeeUserId(Long userId, int limit) {
        return findRecentSucceededByParticipant("payee_user_id", userId, limit);
    }

    /**
     * 汇总成功退款金额。
     */
    default BigDecimal sumSucceededRefundAmount(String originalTradeOrderNo) {
        QueryWrapper<TradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.select("coalesce(sum(original_amount), 0)");
        wrapper.eq("original_trade_order_no", originalTradeOrderNo)
                .eq("trade_type", "REFUND")
                .eq("status", "SUCCEEDED");
        Object value = selectObjs(wrapper).stream().findFirst().orElse(BigDecimal.ZERO);
        if (value instanceof BigDecimal amount) {
            return amount;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private List<TradeOrderDO> findRecentSucceededByParticipant(String columnName, Long userId, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        QueryWrapper<TradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "SUCCEEDED")
                .eq(columnName, userId)
                .orderByDesc("updated_at")
                .orderByDesc("id")
                .last("LIMIT " + normalizedLimit);
        return selectList(wrapper);
    }

    private static LocalDateTime resolveRecentTradeTime(TradeOrderDO tradeOrder) {
        return tradeOrder.getUpdatedAt() == null ? tradeOrder.getCreatedAt() : tradeOrder.getUpdatedAt();
    }

    Comparator<TradeOrderDO> RECENT_SUCCEEDED_TRADE_COMPARATOR = Comparator
            .comparing(TradeOrderMapper::resolveRecentTradeTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(TradeOrderDO::getId, Comparator.nullsLast(Comparator.reverseOrder()));
}
