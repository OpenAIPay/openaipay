package cn.openaipay.infrastructure.inbound;

import cn.openaipay.domain.inbound.model.InboundStatus;
import cn.openaipay.domain.inbound.model.InboundOrder;
import cn.openaipay.domain.inbound.repository.InboundOrderRepository;
import cn.openaipay.infrastructure.inbound.dataobject.InboundOrderDO;
import cn.openaipay.infrastructure.inbound.mapper.InboundOrderMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入金订单仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class InboundOrderRepositoryImpl implements InboundOrderRepository {
    /** 入金主单持久化 */
    private final InboundOrderMapper inboundOrderMapper;

    public InboundOrderRepositoryImpl(InboundOrderMapper inboundOrderMapper) {
        this.inboundOrderMapper = inboundOrderMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public InboundOrder save(InboundOrder order) {
        InboundOrderDO entity = new InboundOrderDO();
        entity.setId(order.getId());
        fillDO(entity, order);
        inboundOrderMapper.upsertByInboundId(entity);
        return toDomain(entity);
    }

    /**
     * 按入金ID查找记录。
     */
    @Override
    public Optional<InboundOrder> findByInboundId(String inboundId) {
        return inboundOrderMapper.findByInboundId(inboundId).map(this::toDomain);
    }

    /**
     * 按请求业务单号查找记录。
     */
    @Override
    public Optional<InboundOrder> findByRequestBizNo(String requestBizNo) {
        return inboundOrderMapper.findByRequestBizNo(requestBizNo).map(this::toDomain);
    }

    /**
     * 按支付订单单号查找记录。
     */
    @Override
    public Optional<InboundOrder> findByPayOrderNo(String payOrderNo) {
        return inboundOrderMapper.findByPayOrderNo(payOrderNo).map(this::toDomain);
    }

    private InboundOrder toDomain(InboundOrderDO entity) {
        return new InboundOrder(
                entity.getId(),
                entity.getInboundId(),
                entity.getInstId(),
                entity.getInstChannelCode(),
                entity.getInboundOrderNo(),
                entity.getPayerAccountNo(),
                entity.getInboundAmount(),
                entity.getAccountAmount(),
                entity.getSettleAmount(),
                InboundStatus.from(entity.getInboundStatus()),
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

    private void fillDO(InboundOrderDO entity, InboundOrder order) {
        LocalDateTime now = LocalDateTime.now();
        entity.setInboundId(order.getInboundId());
        entity.setInstId(order.getInstId());
        entity.setInstChannelCode(order.getInstChannelCode());
        entity.setInboundOrderNo(order.getInboundOrderNo());
        entity.setPayerAccountNo(order.getPayerAccountNo());
        entity.setInboundAmount(order.getInboundAmount());
        entity.setAccountAmount(order.getAccountAmount());
        entity.setSettleAmount(order.getSettleAmount());
        entity.setInboundStatus(order.getInboundStatus().name());
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
}
