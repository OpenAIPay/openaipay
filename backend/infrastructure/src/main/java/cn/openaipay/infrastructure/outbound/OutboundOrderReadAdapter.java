package cn.openaipay.infrastructure.outbound;

import cn.openaipay.application.outbound.dto.OutboundOrderDTO;
import cn.openaipay.application.outbound.dto.OutboundOrderOverviewDTO;
import cn.openaipay.application.outbound.port.OutboundOrderReadPort;
import cn.openaipay.infrastructure.outbound.dataobject.OutboundOrderDO;
import cn.openaipay.infrastructure.outbound.mapper.OutboundOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * 出金只读查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class OutboundOrderReadAdapter implements OutboundOrderReadPort {

    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");

    private final OutboundOrderMapper outboundOrderMapper;

    public OutboundOrderReadAdapter(OutboundOrderMapper outboundOrderMapper) {
        this.outboundOrderMapper = outboundOrderMapper;
    }

    @Override
    public OutboundOrderOverviewDTO getOverview() {
        long totalCount = safeCount(outboundOrderMapper.selectCount(new QueryWrapper<>()));
        long successCount = safeCount(outboundOrderMapper.selectCount(new QueryWrapper<OutboundOrderDO>().eq("outbound_status", "SUCCEEDED")));
        long processingCount = safeCount(outboundOrderMapper.selectCount(
                new QueryWrapper<OutboundOrderDO>().in("outbound_status", List.of("SUBMITTED", "ACCEPTED"))
        ));
        long failedCount = safeCount(outboundOrderMapper.selectCount(new QueryWrapper<OutboundOrderDO>().eq("outbound_status", "FAILED")));
        return new OutboundOrderOverviewDTO(totalCount, successCount, processingCount, failedCount);
    }

    @Override
    public List<OutboundOrderDTO> listOrders(String outboundId,
                                             String requestBizNo,
                                             String payOrderNo,
                                             String outboundStatus,
                                             int pageNo,
                                             int pageSize) {
        QueryWrapper<OutboundOrderDO> wrapper = new QueryWrapper<>();
        String normalizedOutboundId = normalizeKeyword(outboundId);
        if (normalizedOutboundId != null) {
            wrapper.like("outbound_id", normalizedOutboundId);
        }
        String normalizedRequestBizNo = normalizeKeyword(requestBizNo);
        if (normalizedRequestBizNo != null) {
            wrapper.like("request_biz_no", normalizedRequestBizNo);
        }
        String normalizedPayOrderNo = normalizeKeyword(payOrderNo);
        if (normalizedPayOrderNo != null) {
            wrapper.like("pay_order_no", normalizedPayOrderNo);
        }
        String normalizedOutboundStatus = normalizeCode(outboundStatus);
        if (normalizedOutboundStatus != null) {
            wrapper.eq("outbound_status", normalizedOutboundStatus);
        }
        wrapper.orderByDesc("gmt_submit", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        return safeList(outboundOrderMapper.selectList(wrapper)).stream()
                .map(this::toDTO)
                .toList();
    }

    private OutboundOrderDTO toDTO(OutboundOrderDO entity) {
        return new OutboundOrderDTO(
                entity.getOutboundId(),
                entity.getRequestBizNo(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getPayeeAccountNo(),
                toMoney(entity.getOutboundAmount()),
                entity.getOutboundStatus(),
                entity.getResultCode(),
                entity.getResultDescription(),
                entity.getInstId(),
                entity.getInstChannelCode(),
                entity.getOutboundOrderNo(),
                entity.getPayChannelCode(),
                entity.getGmtSubmit(),
                entity.getGmtResp(),
                entity.getGmtSettle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Money toMoney(BigDecimal amount) {
        return Money.of(DEFAULT_CURRENCY, amount == null ? BigDecimal.ZERO : amount);
    }

    private String normalizeKeyword(String raw) {
        String normalized = (raw == null ? "" : raw).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCode(String raw) {
        String normalized = normalizeKeyword(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String buildPageClause(int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(1, pageNo);
        int normalizedPageSize = Math.max(1, pageSize);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        return "LIMIT " + normalizedPageSize + " OFFSET " + offset;
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }
}
