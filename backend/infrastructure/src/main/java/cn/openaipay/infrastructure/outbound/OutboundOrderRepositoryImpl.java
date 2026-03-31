package cn.openaipay.infrastructure.outbound;

import cn.openaipay.domain.outbound.model.OutboundStatus;
import cn.openaipay.domain.outbound.model.OutboundOrder;
import cn.openaipay.domain.outbound.repository.OutboundOrderRepository;
import cn.openaipay.infrastructure.outbound.dataobject.OutboundOrderDO;
import cn.openaipay.infrastructure.outbound.mapper.OutboundOrderMapper;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 出金订单仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class OutboundOrderRepositoryImpl implements OutboundOrderRepository {
    private static final CurrencyUnit DEFAULT_OUTBOUND_CURRENCY = CurrencyUnit.of("CNY");

    /** 出金订单信息 */
    private final OutboundOrderMapper outboundOrderMapper;

    public OutboundOrderRepositoryImpl(OutboundOrderMapper outboundOrderMapper) {
        this.outboundOrderMapper = outboundOrderMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public OutboundOrder save(OutboundOrder order) {
        OutboundOrderDO entity = order.getId() == null
                ? outboundOrderMapper.findByOutboundId(order.getOutboundId()).orElse(new OutboundOrderDO())
                : outboundOrderMapper.findById(order.getId()).orElse(new OutboundOrderDO());
        fillDO(entity, order);
        return toDomain(outboundOrderMapper.save(entity));
    }

    /**
     * 按出金ID查找记录。
     */
    @Override
    public Optional<OutboundOrder> findByOutboundId(String outboundId) {
        return outboundOrderMapper.findByOutboundId(outboundId).map(this::toDomain);
    }

    /**
     * 按请求业务单号查找记录。
     */
    @Override
    public Optional<OutboundOrder> findByRequestBizNo(String requestBizNo) {
        return outboundOrderMapper.findByRequestBizNo(requestBizNo).map(this::toDomain);
    }

    /**
     * 按支付订单单号查找记录。
     */
    @Override
    public Optional<OutboundOrder> findByPayOrderNo(String payOrderNo) {
        return outboundOrderMapper.findByPayOrderNo(payOrderNo).map(this::toDomain);
    }

    private OutboundOrder toDomain(OutboundOrderDO entity) {
        return new OutboundOrder(
                entity.getId(),
                entity.getOutboundId(),
                entity.getInstId(),
                entity.getInstChannelCode(),
                entity.getOutboundOrderNo(),
                entity.getPayeeAccountNo(),
                toMoney(entity.getOutboundAmount()),
                OutboundStatus.from(entity.getOutboundStatus()),
                entity.getResultCode(),
                entity.getResultDescription(),
                entity.getRequestIdentify(),
                entity.getRequestBizNo(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getPayChannelCode(),
                entity.getBizIdentity(),
                entity.getGmtSubmit(),
                entity.getGmtResp(),
                entity.getGmtSettle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(OutboundOrderDO entity, OutboundOrder order) {
        LocalDateTime now = LocalDateTime.now();
        entity.setOutboundId(order.getOutboundId());
        entity.setInstId(order.getInstId());
        entity.setInstChannelCode(order.getInstChannelCode());
        entity.setOutboundOrderNo(order.getOutboundOrderNo());
        entity.setPayeeAccountNo(order.getPayeeAccountNo());
        entity.setOutboundAmount(order.getOutboundAmount().getAmount());
        entity.setOutboundStatus(order.getOutboundStatus().name());
        entity.setResultCode(order.getResultCode());
        entity.setResultDescription(order.getResultDescription());
        entity.setRequestIdentify(order.getRequestIdentify());
        entity.setRequestBizNo(order.getRequestBizNo());
        entity.setBizOrderNo(order.getBizOrderNo());
        entity.setTradeOrderNo(order.getTradeOrderNo());
        entity.setPayOrderNo(order.getPayOrderNo());
        entity.setPayChannelCode(order.getPayChannelCode());
        entity.setBizIdentity(order.getBizIdentity());
        entity.setGmtSubmit(order.getGmtSubmit());
        entity.setGmtResp(order.getGmtResp());
        entity.setGmtSettle(order.getGmtSettle());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(order.getCreatedAt() == null ? now : order.getCreatedAt());
        }
        entity.setUpdatedAt(order.getUpdatedAt() == null ? now : order.getUpdatedAt());
    }

    private Money toMoney(BigDecimal amountValue) {
        BigDecimal normalizedAmount = amountValue == null ? BigDecimal.ZERO : amountValue;
        return Money.of(DEFAULT_OUTBOUND_CURRENCY, normalizedAmount);
    }
}
