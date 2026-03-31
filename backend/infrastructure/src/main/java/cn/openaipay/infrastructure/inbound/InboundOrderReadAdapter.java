package cn.openaipay.infrastructure.inbound;

import cn.openaipay.application.inbound.dto.InboundOrderDTO;
import cn.openaipay.application.inbound.dto.InboundOrderOverviewDTO;
import cn.openaipay.application.inbound.port.InboundOrderReadPort;
import cn.openaipay.infrastructure.inbound.dataobject.InboundOrderDO;
import cn.openaipay.infrastructure.inbound.mapper.InboundOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 入金只读查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class InboundOrderReadAdapter implements InboundOrderReadPort {

    private final InboundOrderMapper inboundOrderMapper;

    public InboundOrderReadAdapter(InboundOrderMapper inboundOrderMapper) {
        this.inboundOrderMapper = inboundOrderMapper;
    }

    @Override
    public InboundOrderOverviewDTO getOverview() {
        long totalCount = safeCount(inboundOrderMapper.selectCount(new QueryWrapper<>()));
        long successCount = safeCount(inboundOrderMapper.selectCount(new QueryWrapper<InboundOrderDO>().eq("inbound_status", "SUCCEEDED")));
        long processingCount = safeCount(inboundOrderMapper.selectCount(
                new QueryWrapper<InboundOrderDO>().in("inbound_status", List.of("SUBMITTED", "ACCEPTED"))
        ));
        long failedCount = safeCount(inboundOrderMapper.selectCount(new QueryWrapper<InboundOrderDO>().eq("inbound_status", "FAILED")));
        return new InboundOrderOverviewDTO(totalCount, successCount, processingCount, failedCount);
    }

    @Override
    public List<InboundOrderDTO> listOrders(String inboundId,
                                            String requestBizNo,
                                            String payOrderNo,
                                            String inboundStatus,
                                            int pageNo,
                                            int pageSize) {
        QueryWrapper<InboundOrderDO> wrapper = new QueryWrapper<>();
        String normalizedInboundId = normalizeKeyword(inboundId);
        if (normalizedInboundId != null) {
            wrapper.like("inbound_id", normalizedInboundId);
        }
        String normalizedRequestBizNo = normalizeKeyword(requestBizNo);
        if (normalizedRequestBizNo != null) {
            wrapper.like("request_biz_no", normalizedRequestBizNo);
        }
        String normalizedPayOrderNo = normalizeKeyword(payOrderNo);
        if (normalizedPayOrderNo != null) {
            wrapper.like("pay_order_no", normalizedPayOrderNo);
        }
        String normalizedInboundStatus = normalizeCode(inboundStatus);
        if (normalizedInboundStatus != null) {
            wrapper.eq("inbound_status", normalizedInboundStatus);
        }
        wrapper.orderByDesc("gmt_submit", "id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        return safeList(inboundOrderMapper.selectList(wrapper)).stream()
                .map(this::toDTO)
                .toList();
    }

    private InboundOrderDTO toDTO(InboundOrderDO entity) {
        return new InboundOrderDTO(
                entity.getInboundId(),
                entity.getRequestBizNo(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getPayerAccountNo(),
                entity.getInboundAmount(),
                entity.getAccountAmount(),
                entity.getSettleAmount(),
                entity.getInboundStatus(),
                entity.getResultCode(),
                entity.getResultDescription(),
                entity.getInstId(),
                entity.getInstChannelCode(),
                entity.getInboundOrderNo(),
                entity.getPayChannelCode(),
                entity.getGmtSubmit(),
                entity.getGmtResp(),
                entity.getGmtSettle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
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
