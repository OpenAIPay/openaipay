package cn.openaipay.infrastructure.message;

import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageStatus;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.repository.MessageRepository;
import cn.openaipay.infrastructure.message.dataobject.MessageRecordDO;
import cn.openaipay.infrastructure.message.mapper.MessageRecordMapper;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.joda.money.Money;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

    /** 消息记录信息 */
    private final MessageRecordMapper messageRecordMapper;

    public MessageRepositoryImpl(MessageRecordMapper messageRecordMapper) {
        this.messageRecordMapper = messageRecordMapper;
    }

    /**
     * 按消息ID查找记录。
     */
    @Override
    public Optional<ChatMessage> findByMessageId(String messageId) {
        return messageRecordMapper.findByMessageId(messageId).map(this::toDomain);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public ChatMessage save(ChatMessage message) {
        MessageRecordDO entity = messageRecordMapper.findByMessageId(message.getMessageId())
                .orElse(new MessageRecordDO());
        fillDO(entity, message);
        return toDomain(messageRecordMapper.save(entity));
    }

    /**
     * 查询RED红包历史信息。
     */
    @Override
    public List<ChatMessage> listRedPacketHistory(Long userId, String direction, Integer year, int limit) {
        return messageRecordMapper.listRedPacketHistory(userId, direction, year, limit).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 处理数量RED红包历史信息。
     */
    @Override
    public long countRedPacketHistory(Long userId, String direction, Integer year) {
        return messageRecordMapper.countRedPacketHistory(userId, direction, year);
    }

    /**
     * 汇总RED红包历史金额。
     */
    @Override
    public Money sumRedPacketHistoryAmount(Long userId, String direction, Integer year) {
        return messageRecordMapper.sumRedPacketHistoryAmount(userId, direction, year);
    }

    /**
     * 按会话查询记录列表。
     */
    @Override
    public List<ChatMessage> listByConversation(String conversationNo, String beforeMessageId, int limit) {
        Long beforeId = null;
        if (beforeMessageId != null && !beforeMessageId.isBlank()) {
            beforeId = messageRecordMapper.findByMessageId(beforeMessageId).map(MessageRecordDO::getId).orElse(null);
        }
        List<ChatMessage> messages = messageRecordMapper.listByConversation(conversationNo, beforeId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
        return messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private ChatMessage toDomain(MessageRecordDO entity) {
        return new ChatMessage(
                entity.getId(),
                entity.getMessageId(),
                entity.getConversationNo(),
                entity.getSenderUserId(),
                entity.getReceiverUserId(),
                MessageType.from(entity.getMessageType()),
                entity.getContentText(),
                entity.getMediaId(),
                entity.getAmount(),
                entity.getTradeOrderNo(),
                entity.getExtPayload(),
                MessageStatus.from(entity.getMessageStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(MessageRecordDO entity, ChatMessage message) {
        LocalDateTime now = LocalDateTime.now();
        entity.setMessageId(message.getMessageId());
        entity.setConversationNo(message.getConversationNo());
        entity.setSenderUserId(message.getSenderUserId());
        entity.setReceiverUserId(message.getReceiverUserId());
        entity.setMessageType(message.getMessageType().name());
        entity.setContentText(message.getContentText());
        entity.setMediaId(message.getMediaId());
        entity.setAmount(message.getAmount());
        entity.setTradeOrderNo(message.getTradeOrderNo());
        entity.setExtPayload(message.getExtPayload());
        entity.setMessageStatus(message.getMessageStatus().name());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(message.getCreatedAt() == null ? now : message.getCreatedAt());
        }
        entity.setUpdatedAt(message.getUpdatedAt() == null ? now : message.getUpdatedAt());
    }
}
