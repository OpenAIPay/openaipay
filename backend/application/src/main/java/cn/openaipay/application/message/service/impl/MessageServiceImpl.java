package cn.openaipay.application.message.service.impl;

import cn.openaipay.application.contact.facade.ContactFacade;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.media.dto.MediaAssetDTO;
import cn.openaipay.application.media.facade.MediaFacade;
import cn.openaipay.application.message.command.SendImageMessageCommand;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTextMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.dto.RedPacketHistoryDTO;
import cn.openaipay.application.message.dto.RedPacketHistoryItemDTO;
import cn.openaipay.application.message.orchestrator.MessageMoneyOrchestrator;
import cn.openaipay.application.message.service.MessageService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.facade.UserFacade;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.model.RedPacketOrderStatus;
import cn.openaipay.domain.message.repository.MessageRepository;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class MessageServiceImpl implements MessageService {

    /** 默认信息 */
    private static final int DEFAULT_LIMIT = 30;
    /** 最大信息 */
    private static final int MAX_LIMIT = 200;
    /** 消息ID业务类型编码 */
    private static final String MESSAGE_ID_BIZ_TYPE = "90";

    /** 消息信息 */
    private final MessageRepository messageRepository;
    /** RED订单信息 */
    private final RedPacketOrderRepository redPacketOrderRepository;
    /** 会话信息 */
    private final ConversationFacade conversationFacade;
    /** 联系人信息 */
    private final ContactFacade contactFacade;
    /** 媒体信息 */
    private final MediaFacade mediaFacade;
    /** 用户信息 */
    private final UserFacade userFacade;
    /** 消息信息 */
    private final MessageMoneyOrchestrator messageMoneyOrchestrator;
    /** 全局ID生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;

    public MessageServiceImpl(MessageRepository messageRepository,
                                         RedPacketOrderRepository redPacketOrderRepository,
                                         ConversationFacade conversationFacade,
                                         ContactFacade contactFacade,
                                         MediaFacade mediaFacade,
                                         UserFacade userFacade,
                                         MessageMoneyOrchestrator messageMoneyOrchestrator,
                                         AiPayIdGenerator aiPayIdGenerator) {
        this.messageRepository = messageRepository;
        this.redPacketOrderRepository = redPacketOrderRepository;
        this.conversationFacade = conversationFacade;
        this.contactFacade = contactFacade;
        this.mediaFacade = mediaFacade;
        this.userFacade = userFacade;
        this.messageMoneyOrchestrator = messageMoneyOrchestrator;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 处理消息信息。
     */
    @Override
    @Transactional
    public MessageDTO sendTextMessage(SendTextMessageCommand command) {
        Long senderUserId = requirePositive(command.senderUserId(), "senderUserId");
        Long receiverUserId = requirePositive(command.receiverUserId(), "receiverUserId");
        ensureCanChat(senderUserId, receiverUserId);
        String contentText = normalizeRequired(command.contentText(), "contentText");

        return createAndPersistMessage(
                senderUserId,
                receiverUserId,
                MessageType.TEXT,
                contentText,
                null,
                null,
                null,
                command.extPayload(),
                shortPreview(contentText)
        );
    }

    /**
     * 处理消息信息。
     */
    @Override
    @Transactional
    public MessageDTO sendImageMessage(SendImageMessageCommand command) {
        Long senderUserId = requirePositive(command.senderUserId(), "senderUserId");
        Long receiverUserId = requirePositive(command.receiverUserId(), "receiverUserId");
        ensureCanChat(senderUserId, receiverUserId);

        String mediaId = normalizeRequired(command.mediaId(), "mediaId");
        MediaAssetDTO mediaAsset = mediaFacade.getMedia(mediaId);
        if (!senderUserId.equals(mediaAsset.ownerUserId())) {
            throw new IllegalArgumentException("media owner must be senderUserId");
        }

        return createAndPersistMessage(
                senderUserId,
                receiverUserId,
                MessageType.IMAGE,
                "[图片]",
                mediaId,
                null,
                null,
                command.extPayload(),
                "[图片]"
        );
    }

    /**
     * 处理转账消息信息。
     */
    @Override
    @Transactional
    public MessageDTO sendTransferMessage(SendTransferMessageCommand command) {
        return toDTO(messageMoneyOrchestrator.sendTransferMessage(command));
    }

    /**
     * 处理RED红包消息信息。
     */
    @Override
    @Transactional
    public MessageDTO sendRedPacketMessage(SendRedPacketMessageCommand command) {
        return toDTO(messageMoneyOrchestrator.sendRedPacketMessage(command));
    }

    /**
     * 获取RED红包明细信息。
     */
    @Override
    @Transactional(readOnly = true)
    public RedPacketDetailDTO getRedPacketDetail(Long userId, String redPacketNo) {
        Long normalizedUserId = requirePositive(userId, "userId");
        RedPacketOrder redPacketOrder = redPacketOrderRepository.findByRedPacketNo(normalizeRequired(redPacketNo, "redPacketNo"))
                .orElseThrow(() -> new NoSuchElementException("red packet not found: " + redPacketNo));
        ensureCanViewRedPacket(redPacketOrder, normalizedUserId);
        return toRedPacketDetailDTO(redPacketOrder, normalizedUserId);
    }

    /**
     * 处理RED红包信息。
     */
    @Override
    @Transactional
    public RedPacketDetailDTO claimRedPacket(Long userId, String redPacketNo) {
        Long normalizedUserId = requirePositive(userId, "userId");
        RedPacketOrder redPacketOrder = messageMoneyOrchestrator.claimRedPacket(normalizedUserId, redPacketNo);
        return toRedPacketDetailDTO(redPacketOrder, normalizedUserId);
    }

    /**
     * 获取RED红包历史信息。
     */
    @Override
    @Transactional(readOnly = true)
    public RedPacketHistoryDTO getRedPacketHistory(Long userId, String direction, Integer year, Integer limit) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String normalizedDirection = normalizeHistoryDirection(direction);
        Integer normalizedYear = normalizeHistoryYear(year);
        int normalizedLimit = normalizeHistoryLimit(limit);
        List<ChatMessage> messages = messageRepository.listRedPacketHistory(
                normalizedUserId,
                normalizedDirection,
                normalizedYear,
                normalizedLimit
        );
        long totalCount = messageRepository.countRedPacketHistory(normalizedUserId, normalizedDirection, normalizedYear);
        Money totalAmount = messageRepository.sumRedPacketHistoryAmount(normalizedUserId, normalizedDirection, normalizedYear);
        Map<Long, UserProfileDTO> profileCache = new HashMap<>();
        Map<String, RedPacketOrder> redPacketOrderByFundingTradeNo = new HashMap<>();
        List<String> fundingTradeNos = messages.stream()
                .map(ChatMessage::getTradeOrderNo)
                .map(this::normalizeOptional)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        if (!fundingTradeNos.isEmpty()) {
            List<RedPacketOrder> redPacketOrders = redPacketOrderRepository.findByFundingTradeNos(fundingTradeNos);
            for (RedPacketOrder redPacketOrder : redPacketOrders) {
                String fundingTradeNo = normalizeOptional(redPacketOrder.getFundingTradeNo());
                if (fundingTradeNo != null) {
                    redPacketOrderByFundingTradeNo.put(fundingTradeNo, redPacketOrder);
                }
            }
        }

        List<RedPacketHistoryItemDTO> items = messages.stream()
                .map(message -> toRedPacketHistoryItemDTO(message, normalizedDirection, profileCache, redPacketOrderByFundingTradeNo))
                .toList();
        return new RedPacketHistoryDTO(
                normalizedUserId,
                normalizedDirection,
                normalizedYear,
                totalCount,
                totalAmount,
                items
        );
    }

    /**
     * 查询会话消息信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> listConversationMessages(Long userId,
                                                     String conversationNo,
                                                     String beforeMessageId,
                                                     Integer limit) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String normalizedConversationNo = normalizeRequired(conversationNo, "conversationNo");
        if (!conversationFacade.hasMember(normalizedConversationNo, normalizedUserId)) {
            throw new IllegalArgumentException("user is not member of conversation");
        }
        return messageRepository.listByConversation(normalizedConversationNo, normalizeOptional(beforeMessageId), normalizeLimit(limit))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private MessageDTO createAndPersistMessage(Long senderUserId,
                                               Long receiverUserId,
                                               MessageType messageType,
                                               String contentText,
                                               String mediaId,
                                               Money amount,
                                               String tradeOrderNo,
                                               String extPayload,
                                               String preview) {
        ConversationDTO conversation = conversationFacade.openPrivateConversation(
                new OpenPrivateConversationCommand(senderUserId, receiverUserId)
        );
        LocalDateTime now = LocalDateTime.now();
        String messageId = buildMessageId(senderUserId);

        ChatMessage message = ChatMessage.create(
                messageId,
                conversation.conversationNo(),
                senderUserId,
                receiverUserId,
                messageType,
                contentText,
                mediaId,
                amount,
                tradeOrderNo,
                extPayload,
                now
        );
        message = messageRepository.save(message);

        conversationFacade.appendMessage(
                message.getConversationNo(),
                senderUserId,
                receiverUserId,
                message.getMessageId(),
                preview,
                now
        );
        return toDTO(message);
    }

    private void ensureCanChat(Long senderUserId, Long receiverUserId) {
        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("senderUserId and receiverUserId must be different");
        }
        if (!contactFacade.isFriend(senderUserId, receiverUserId)) {
            throw new IllegalStateException("sender and receiver must be friends");
        }
    }

    private void ensureCanViewRedPacket(RedPacketOrder redPacketOrder, Long currentUserId) {
        if (!redPacketOrder.getSenderUserId().equals(currentUserId)
                && !redPacketOrder.getReceiverUserId().equals(currentUserId)) {
            throw new IllegalStateException("current user is not member of the red packet conversation");
        }
    }

    private String normalizeHistoryDirection(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return "SENT";
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (!"SENT".equals(upper) && !"RECEIVED".equals(upper)) {
            throw new IllegalArgumentException("direction must be SENT or RECEIVED");
        }
        return upper;
    }

    private Integer normalizeHistoryYear(Integer year) {
        if (year == null || year <= 0) {
            return LocalDateTime.now().getYear();
        }
        return year;
    }

    private int normalizeHistoryLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private MessageDTO toDTO(ChatMessage message) {
        return new MessageDTO(
                message.getMessageId(),
                message.getConversationNo(),
                message.getSenderUserId(),
                message.getReceiverUserId(),
                message.getMessageType().name(),
                message.getContentText(),
                message.getMediaId(),
                message.getAmount(),
                message.getTradeOrderNo(),
                message.getExtPayload(),
                message.getMessageStatus().name(),
                message.getCreatedAt()
        );
    }

    private RedPacketDetailDTO toRedPacketDetailDTO(RedPacketOrder redPacketOrder, Long viewerUserId) {
        UserProfileDTO senderProfile = resolveUserProfile(redPacketOrder.getSenderUserId(), new HashMap<>());
        UserProfileDTO receiverProfile = resolveUserProfile(redPacketOrder.getReceiverUserId(), new HashMap<>());
        boolean claimableByViewer = redPacketOrder.getReceiverUserId().equals(viewerUserId)
                && redPacketOrder.getStatus() == RedPacketOrderStatus.PENDING_CLAIM;
        boolean claimedByViewer = redPacketOrder.getReceiverUserId().equals(viewerUserId)
                && redPacketOrder.getStatus() == RedPacketOrderStatus.CLAIMED;
        return new RedPacketDetailDTO(
                redPacketOrder.getRedPacketNo(),
                redPacketOrder.getMessageId(),
                redPacketOrder.getConversationNo(),
                redPacketOrder.getSenderUserId(),
                profileNickname(senderProfile, redPacketOrder.getSenderUserId()),
                senderProfile == null ? null : senderProfile.avatarUrl(),
                redPacketOrder.getReceiverUserId(),
                profileNickname(receiverProfile, redPacketOrder.getReceiverUserId()),
                receiverProfile == null ? null : receiverProfile.avatarUrl(),
                redPacketOrder.getHoldingUserId(),
                redPacketOrder.getAmount(),
                redPacketOrder.getPaymentMethod(),
                redPacketOrder.getCoverId(),
                redPacketOrder.getCoverTitle(),
                redPacketOrder.getBlessingText(),
                redPacketOrder.getStatus().name(),
                redPacketOrder.getFundingTradeNo(),
                redPacketOrder.getClaimTradeNo(),
                claimableByViewer,
                claimedByViewer,
                redPacketOrder.getClaimedAt(),
                redPacketOrder.getCreatedAt()
        );
    }

    private RedPacketHistoryItemDTO toRedPacketHistoryItemDTO(ChatMessage message,
                                                              String direction,
                                                              Map<Long, UserProfileDTO> profileCache,
                                                              Map<String, RedPacketOrder> redPacketOrderByFundingTradeNo) {
        Long counterpartyUserId = "RECEIVED".equals(direction) ? message.getSenderUserId() : message.getReceiverUserId();
        UserProfileDTO profile = resolveUserProfile(counterpartyUserId, profileCache);
        String nickname = profileNickname(profile, counterpartyUserId);
        String tradeOrderNo = message.getTradeOrderNo();
        String normalizedTradeOrderNo = normalizeOptional(tradeOrderNo);
        RedPacketOrder redPacketOrder = normalizedTradeOrderNo == null
                ? null
                : redPacketOrderByFundingTradeNo.get(normalizedTradeOrderNo);
        String redPacketNo = redPacketOrder == null ? null : redPacketOrder.getRedPacketNo();
        String redPacketStatus = redPacketOrder == null ? null : redPacketOrder.getStatus().name();
        return new RedPacketHistoryItemDTO(
                message.getMessageId(),
                message.getConversationNo(),
                direction,
                counterpartyUserId,
                nickname,
                profile == null ? null : profile.avatarUrl(),
                message.getAmount(),
                tradeOrderNo,
                message.getMessageStatus().name(),
                redPacketNo,
                redPacketStatus,
                message.getCreatedAt()
        );
    }

    private UserProfileDTO resolveUserProfile(Long userId, Map<Long, UserProfileDTO> profileCache) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (profileCache.containsKey(userId)) {
            return profileCache.get(userId);
        }
        UserProfileDTO profile;
        try {
            profile = userFacade.getProfile(userId);
        } catch (RuntimeException ignored) {
            profile = null;
        }
        profileCache.put(userId, profile);
        return profile;
    }

    private String profileNickname(UserProfileDTO profile, Long fallbackUserId) {
        if (profile == null || profile.nickname() == null || profile.nickname().isBlank()) {
            return "用户" + fallbackUserId;
        }
        return profile.nickname().trim();
    }

    private String shortPreview(String contentText) {
        if (contentText == null || contentText.length() <= 60) {
            return contentText;
        }
        return contentText.substring(0, 60);
    }

    private String buildMessageId(Long senderUserId) {
        return "MSG" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_TRADE,
                MESSAGE_ID_BIZ_TYPE,
                String.valueOf(requirePositive(senderUserId, "senderUserId"))
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
