package cn.openaipay.infrastructure.message;

import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.model.RedPacketOrderStatus;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import cn.openaipay.infrastructure.message.dataobject.RedPacketOrderDO;
import cn.openaipay.infrastructure.message.mapper.RedPacketOrderMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 红包订单仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Repository
public class RedPacketOrderRepositoryImpl implements RedPacketOrderRepository {

    /** RED订单信息 */
    private final RedPacketOrderMapper redPacketOrderMapper;

    public RedPacketOrderRepositoryImpl(RedPacketOrderMapper redPacketOrderMapper) {
        this.redPacketOrderMapper = redPacketOrderMapper;
    }

    /**
     * 按RED红包单号查找记录。
     */
    @Override
    public Optional<RedPacketOrder> findByRedPacketNo(String redPacketNo) {
        return redPacketOrderMapper.findByRedPacketNo(redPacketNo).map(this::toDomain);
    }

    /**
     * 按消息ID查找记录。
     */
    @Override
    public Optional<RedPacketOrder> findByMessageId(String messageId) {
        return redPacketOrderMapper.findByMessageId(messageId).map(this::toDomain);
    }

    /**
     * 按交易单号查找记录。
     */
    @Override
    public Optional<RedPacketOrder> findByFundingTradeNo(String fundingTradeNo) {
        return redPacketOrderMapper.findByFundingTradeNo(fundingTradeNo).map(this::toDomain);
    }

    /**
     * 按交易单号查找记录。
     */
    @Override
    public List<RedPacketOrder> findByFundingTradeNos(List<String> fundingTradeNos) {
        return redPacketOrderMapper.findByFundingTradeNos(fundingTradeNos).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 按交易单号查找记录。
     */
    @Override
    public Optional<RedPacketOrder> findByClaimTradeNo(String claimTradeNo) {
        return redPacketOrderMapper.findByClaimTradeNo(claimTradeNo).map(this::toDomain);
    }

    /**
     * 按交易单号查找记录。
     */
    @Override
    public List<RedPacketOrder> findByClaimTradeNos(List<String> claimTradeNos) {
        return redPacketOrderMapper.findByClaimTradeNos(claimTradeNos).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public RedPacketOrder save(RedPacketOrder redPacketOrder) {
        RedPacketOrderDO entity = redPacketOrderMapper.findByRedPacketNo(redPacketOrder.getRedPacketNo())
                .orElse(new RedPacketOrderDO());
        fillDO(entity, redPacketOrder);
        return toDomain(redPacketOrderMapper.save(entity));
    }

    private RedPacketOrder toDomain(RedPacketOrderDO entity) {
        return new RedPacketOrder(
                entity.getId(),
                entity.getRedPacketNo(),
                entity.getMessageId(),
                entity.getConversationNo(),
                entity.getSenderUserId(),
                entity.getReceiverUserId(),
                entity.getHoldingUserId(),
                entity.getAmount(),
                entity.getFundingTradeNo(),
                entity.getClaimTradeNo(),
                entity.getPaymentMethod(),
                entity.getCoverId(),
                entity.getCoverTitle(),
                entity.getBlessingText(),
                RedPacketOrderStatus.from(entity.getStatus()),
                entity.getClaimedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(RedPacketOrderDO entity, RedPacketOrder redPacketOrder) {
        LocalDateTime now = LocalDateTime.now();
        entity.setRedPacketNo(redPacketOrder.getRedPacketNo());
        entity.setMessageId(redPacketOrder.getMessageId());
        entity.setConversationNo(redPacketOrder.getConversationNo());
        entity.setSenderUserId(redPacketOrder.getSenderUserId());
        entity.setReceiverUserId(redPacketOrder.getReceiverUserId());
        entity.setHoldingUserId(redPacketOrder.getHoldingUserId());
        entity.setAmount(redPacketOrder.getAmount());
        entity.setFundingTradeNo(redPacketOrder.getFundingTradeNo());
        entity.setClaimTradeNo(redPacketOrder.getClaimTradeNo());
        entity.setPaymentMethod(redPacketOrder.getPaymentMethod());
        entity.setCoverId(redPacketOrder.getCoverId());
        entity.setCoverTitle(redPacketOrder.getCoverTitle());
        entity.setBlessingText(redPacketOrder.getBlessingText());
        entity.setStatus(redPacketOrder.getStatus().name());
        entity.setClaimedAt(redPacketOrder.getClaimedAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(redPacketOrder.getCreatedAt() == null ? now : redPacketOrder.getCreatedAt());
        }
        entity.setUpdatedAt(redPacketOrder.getUpdatedAt() == null ? now : redPacketOrder.getUpdatedAt());
    }
}
