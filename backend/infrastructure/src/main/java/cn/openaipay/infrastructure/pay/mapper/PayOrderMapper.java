package cn.openaipay.infrastructure.pay.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pay.dataobject.PayOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrderDO> {

    /**
     * 按支付订单单号查找记录。
     */
    default Optional<PayOrderDO> findByPayOrderNo(String payOrderNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("pay_order_no", payOrderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按交易订单号查找最新记录。
     */
    default Optional<PayOrderDO> findLatestByTradeOrderNo(String tradeOrderNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("trade_order_no", tradeOrderNo)
                .orderByDesc("attempt_no")
                .orderByDesc("id")
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务订单单号查找记录。
     */
    default Optional<PayOrderDO> findByBizOrderNo(String bizOrderNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_order_no", bizOrderNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务查找记录。
     */
    default Optional<PayOrderDO> findBySourceBiz(String sourceBizType, String sourceBizNo) {
        return findLatestBySourceBiz(sourceBizType, sourceBizNo);
    }

    /**
     * 批量按交易订单号查找记录（按trade_order_no分组后 attempt_no/id 倒序）。
     */
    default List<PayOrderDO> findByTradeOrderNos(List<String> tradeOrderNos) {
        List<String> normalizedTradeOrderNos = normalizeLookupKeys(tradeOrderNos);
        if (normalizedTradeOrderNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.in("trade_order_no", normalizedTradeOrderNos)
                .orderByAsc("trade_order_no")
                .orderByDesc("attempt_no")
                .orderByDesc("id");
        return selectList(wrapper);
    }

    /**
     * 批量按业务订单号查找记录。
     */
    default List<PayOrderDO> findByBizOrderNos(List<String> bizOrderNos) {
        List<String> normalizedBizOrderNos = normalizeLookupKeys(bizOrderNos);
        if (normalizedBizOrderNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.in("biz_order_no", normalizedBizOrderNos)
                .orderByAsc("biz_order_no")
                .orderByDesc("id");
        return selectList(wrapper);
    }

    /**
     * 批量按支付单号查找记录。
     */
    default List<PayOrderDO> findByPayOrderNos(List<String> payOrderNos) {
        List<String> normalizedPayOrderNos = normalizeLookupKeys(payOrderNos);
        if (normalizedPayOrderNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.in("pay_order_no", normalizedPayOrderNos)
                .orderByAsc("pay_order_no");
        return selectList(wrapper);
    }

    /**
     * 按业务查找记录。
     */
    default Optional<PayOrderDO> findLatestBySourceBiz(String sourceBizType, String sourceBizNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("source_biz_type", sourceBizType)
                .eq("source_biz_no", sourceBizNo)
                .orderByDesc("attempt_no")
                .orderByDesc("id")
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按来源业务类型与来源业务单号查找记录（按source_biz_no分组后 attempt_no/id 倒序）。
     */
    default List<PayOrderDO> findBySourceBizNos(String sourceBizType, List<String> sourceBizNos) {
        List<String> normalizedSourceBizNos = normalizeLookupKeys(sourceBizNos);
        if (sourceBizType == null || sourceBizType.isBlank() || normalizedSourceBizNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("source_biz_type", sourceBizType)
                .in("source_biz_no", normalizedSourceBizNos)
                .orderByAsc("source_biz_no")
                .orderByDesc("attempt_no")
                .orderByDesc("id");
        return selectList(wrapper);
    }

    /**
     * 按业务与单号查找记录。
     */
    default Optional<PayOrderDO> findBySourceBizAndAttemptNo(String sourceBizType, String sourceBizNo, int attemptNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("source_biz_type", sourceBizType)
                .eq("source_biz_no", sourceBizNo)
                .eq("attempt_no", attemptNo)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务查找全部信息。
     */
    default List<PayOrderDO> findAllBySourceBiz(String sourceBizType, String sourceBizNo) {
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("source_biz_type", sourceBizType)
                .eq("source_biz_no", sourceBizNo)
                .orderByDesc("attempt_no")
                .orderByDesc("id");
        return selectList(wrapper);
    }

    /**
     * 查找订单信息。
     */
    default List<PayOrderDO> findReconPendingOrders(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "RECON_PENDING")
                .orderByAsc("updated_at")
                .orderByAsc("id")
                .last("LIMIT " + normalizedLimit);
        return selectList(wrapper);
    }

    private static List<String> normalizeLookupKeys(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }
        Set<String> deduplicated = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            String normalized = rawValue.trim();
            if (!normalized.isEmpty()) {
                deduplicated.add(normalized);
            }
        }
        if (deduplicated.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(deduplicated);
    }
}
