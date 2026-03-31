package cn.openaipay.application.message.orchestrator.impl;

import cn.openaipay.application.contact.facade.ContactFacade;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.orchestrator.MessageMoneyOrchestrator;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.model.RedPacketOrderStatus;
import cn.openaipay.domain.message.repository.MessageRepository;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 消息资金类编排器实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class MessageMoneyOrchestratorImpl implements MessageMoneyOrchestrator {

    /** RED场景信息 */
    private static final String RED_PACKET_SEND_SCENE = "CHAT_RED_PACKET_SEND";
    /** RED场景信息 */
    private static final String RED_PACKET_CLAIM_SCENE = "CHAT_RED_PACKET_CLAIM";
    /** object映射器信息 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 消息ID业务类型编码 */
    private static final String MESSAGE_ID_BIZ_TYPE = "90";
    /** 红包ID业务类型编码 */
    private static final String RED_PACKET_ID_BIZ_TYPE = "91";
    /** 交易请求号业务类型编码 */
    private static final String TRADE_REQUEST_BIZ_TYPE = "92";

    /** 消息信息 */
    private final MessageRepository messageRepository;
    /** RED订单信息 */
    private final RedPacketOrderRepository redPacketOrderRepository;
    /** 会话信息 */
    private final ConversationFacade conversationFacade;
    /** 联系人信息 */
    private final ContactFacade contactFacade;
    /** 交易信息 */
    private final TradeFacade tradeFacade;
    /** RED用户ID */
    private final Long redPacketHoldingUserId;
    /** 全局ID生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;

    public MessageMoneyOrchestratorImpl(MessageRepository messageRepository,
                                        RedPacketOrderRepository redPacketOrderRepository,
                                        ConversationFacade conversationFacade,
                                        ContactFacade contactFacade,
                                        TradeFacade tradeFacade,
                                        @Value("${aipay.red-packet.holding-user-id:880231069206400031}") Long redPacketHoldingUserId,
                                        AiPayIdGenerator aiPayIdGenerator) {
        this.messageRepository = messageRepository;
        this.redPacketOrderRepository = redPacketOrderRepository;
        this.conversationFacade = conversationFacade;
        this.contactFacade = contactFacade;
        this.tradeFacade = tradeFacade;
        this.redPacketHoldingUserId = requirePositive(redPacketHoldingUserId, "redPacketHoldingUserId");
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 处理转账消息信息。
     */
    @Override
    public ChatMessage sendTransferMessage(SendTransferMessageCommand command) {
        Long senderUserId = requirePositive(command.senderUserId(), "senderUserId");
        Long receiverUserId = requirePositive(command.receiverUserId(), "receiverUserId");
        ensureCanChat(senderUserId, receiverUserId);
        Money amount = normalizeAmount(command.amount(), "amount");
        String requestNo = buildTradeRequestNo("TRF", senderUserId);
        String paymentMethod = defaultPaymentMethod(command.paymentMethod());
        String paymentToolCode = normalizeOptional(command.paymentToolCode());
        String remark = normalizeOptional(command.remark());

        TradeOrderDTO tradeOrder = tradeFacade.transfer(new CreateTransferTradeCommand(
                requestNo,
                "CHAT_TRANSFER",
                senderUserId,
                receiverUserId,
                paymentMethod,
                amount,
                null,
                null,
                null,
                null,
                paymentToolCode,
                remark
        ));

        return createAndPersistMessage(
                senderUserId,
                receiverUserId,
                MessageType.TRANSFER,
                "[转账]",
                null,
                amount,
                tradeOrder.tradeOrderNo(),
                command.extPayload(),
                "[转账]"
        );
    }

    /**
     * 处理RED红包消息信息。
     */
    @Override
    public ChatMessage sendRedPacketMessage(SendRedPacketMessageCommand command) {
        Long senderUserId = requirePositive(command.senderUserId(), "senderUserId");
        Long receiverUserId = requirePositive(command.receiverUserId(), "receiverUserId");
        ensureCanChat(senderUserId, receiverUserId);
        Money amount = normalizeAmount(command.amount(), "amount");

        ConversationDTO conversation = conversationFacade.openPrivateConversation(
                new OpenPrivateConversationCommand(senderUserId, receiverUserId)
        );
        LocalDateTime now = LocalDateTime.now();
        String messageId = buildMessageId(senderUserId);
        String redPacketNo = buildRedPacketNo(senderUserId);
        String coverId = readJsonText(command.extPayload(), "coverId");
        String coverTitle = readJsonText(command.extPayload(), "coverTitle");
        String blessingText = readJsonText(command.extPayload(), "blessingText");

        String fundingPayload = buildRedPacketPayload(
                command.extPayload(),
                redPacketNo,
                senderUserId,
                receiverUserId,
                redPacketHoldingUserId,
                defaultPaymentMethod(command.paymentMethod()),
                null,
                RedPacketOrderStatus.PENDING_CLAIM,
                null,
                null
        );

        TradeOrderDTO fundingTrade = tradeFacade.transfer(new CreateTransferTradeCommand(
                buildTradeRequestNo("RPS", senderUserId),
                RED_PACKET_SEND_SCENE,
                senderUserId,
                redPacketHoldingUserId,
                defaultPaymentMethod(command.paymentMethod()),
                amount,
                null,
                null,
                null,
                null,
                null,
                fundingPayload
        ));

        RedPacketOrder redPacketOrder = RedPacketOrder.createPending(
                redPacketNo,
                messageId,
                conversation.conversationNo(),
                senderUserId,
                receiverUserId,
                redPacketHoldingUserId,
                amount,
                fundingTrade.tradeOrderNo(),
                defaultPaymentMethod(command.paymentMethod()),
                coverId,
                coverTitle,
                blessingText,
                now
        );
        redPacketOrder = redPacketOrderRepository.save(redPacketOrder);

        String messagePayload = buildRedPacketPayload(
                command.extPayload(),
                redPacketOrder.getRedPacketNo(),
                redPacketOrder.getSenderUserId(),
                redPacketOrder.getReceiverUserId(),
                redPacketOrder.getHoldingUserId(),
                redPacketOrder.getPaymentMethod(),
                redPacketOrder.getFundingTradeNo(),
                redPacketOrder.getStatus(),
                redPacketOrder.getClaimTradeNo(),
                redPacketOrder.getClaimedAt()
        );

        ChatMessage message = ChatMessage.create(
                messageId,
                conversation.conversationNo(),
                senderUserId,
                receiverUserId,
                MessageType.RED_PACKET,
                "[红包]",
                null,
                amount,
                fundingTrade.tradeOrderNo(),
                messagePayload,
                now
        );
        message = messageRepository.save(message);

        conversationFacade.appendMessage(
                message.getConversationNo(),
                senderUserId,
                receiverUserId,
                message.getMessageId(),
                "[红包]",
                now
        );
        return message;
    }

    /**
     * 处理RED红包信息。
     */
    @Override
    public RedPacketOrder claimRedPacket(Long userId, String redPacketNo) {
        Long normalizedUserId = requirePositive(userId, "userId");
        RedPacketOrder redPacketOrder = redPacketOrderRepository.findByRedPacketNo(normalizeRequired(redPacketNo, "redPacketNo"))
                .orElseThrow(() -> new NoSuchElementException("red packet not found: " + redPacketNo));

        if (!redPacketOrder.getReceiverUserId().equals(normalizedUserId)) {
            throw new IllegalStateException("current user is not the red packet receiver");
        }
        if (redPacketOrder.getStatus() == RedPacketOrderStatus.CLAIMED) {
            return redPacketOrder;
        }
        if (redPacketOrder.getStatus() != RedPacketOrderStatus.PENDING_CLAIM) {
            throw new IllegalStateException("red packet status does not allow claim: " + redPacketOrder.getStatus().name());
        }

        String claimPayload = buildRedPacketPayload(
                loadOriginalExtPayload(redPacketOrder.getMessageId()),
                redPacketOrder.getRedPacketNo(),
                redPacketOrder.getSenderUserId(),
                redPacketOrder.getReceiverUserId(),
                redPacketOrder.getHoldingUserId(),
                "WALLET",
                redPacketOrder.getFundingTradeNo(),
                RedPacketOrderStatus.CLAIMED,
                null,
                null
        );

        TradeOrderDTO claimTrade = tradeFacade.transfer(new CreateTransferTradeCommand(
                buildTradeRequestNo("RPC", normalizedUserId),
                RED_PACKET_CLAIM_SCENE,
                redPacketOrder.getHoldingUserId(),
                redPacketOrder.getReceiverUserId(),
                "WALLET",
                redPacketOrder.getAmount(),
                null,
                null,
                null,
                null,
                null,
                claimPayload
        ));

        LocalDateTime now = LocalDateTime.now();
        redPacketOrder.markClaimed(normalizedUserId, claimTrade.tradeOrderNo(), now);
        redPacketOrder = redPacketOrderRepository.save(redPacketOrder);
        String messageId = redPacketOrder.getMessageId();

        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NoSuchElementException("message not found: " + messageId));
        message.updateExtPayload(buildRedPacketPayload(
                message.getExtPayload(),
                redPacketOrder.getRedPacketNo(),
                redPacketOrder.getSenderUserId(),
                redPacketOrder.getReceiverUserId(),
                redPacketOrder.getHoldingUserId(),
                redPacketOrder.getPaymentMethod(),
                redPacketOrder.getFundingTradeNo(),
                redPacketOrder.getStatus(),
                redPacketOrder.getClaimTradeNo(),
                redPacketOrder.getClaimedAt()
        ), now);
        messageRepository.save(message);
        return redPacketOrder;
    }

    private ChatMessage createAndPersistMessage(Long senderUserId,
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
        return message;
    }

    private void ensureCanChat(Long senderUserId, Long receiverUserId) {
        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("senderUserId and receiverUserId must be different");
        }
        if (!contactFacade.isFriend(senderUserId, receiverUserId)) {
            throw new IllegalStateException("sender and receiver must be friends");
        }
    }

    private String defaultPaymentMethod(String candidate) {
        String normalized = normalizeOptional(candidate);
        return normalized == null ? "WALLET" : normalized.toUpperCase(Locale.ROOT);
    }

    private Money normalizeAmount(Money amount, String fieldName) {
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount;
    }

    private String buildMessageId(Long senderUserId) {
        return "MSG" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_TRADE,
                MESSAGE_ID_BIZ_TYPE,
                String.valueOf(requirePositive(senderUserId, "senderUserId"))
        );
    }

    private String buildRedPacketNo(Long senderUserId) {
        return "RPK" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_COUPON,
                RED_PACKET_ID_BIZ_TYPE,
                String.valueOf(requirePositive(senderUserId, "senderUserId"))
        );
    }

    private String buildTradeRequestNo(String prefix, Long senderUserId) {
        return normalizeRequired(prefix, "prefix") + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_TRADE,
                TRADE_REQUEST_BIZ_TYPE,
                String.valueOf(requirePositive(senderUserId, "senderUserId"))
        );
    }

    private String buildRedPacketPayload(String rawPayload,
                                         String redPacketNo,
                                         Long senderUserId,
                                         Long receiverUserId,
                                         Long holdingUserId,
                                         String paymentMethod,
                                         String fundingTradeOrderNo,
                                         RedPacketOrderStatus status,
                                         String claimTradeOrderNo,
                                         LocalDateTime claimedAt) {
        ObjectNode payload = parsePayloadObject(rawPayload);
        payload.put("redPacketNo", normalizeRequired(redPacketNo, "redPacketNo"));
        payload.put("senderUserId", requirePositive(senderUserId, "senderUserId"));
        payload.put("receiverUserId", requirePositive(receiverUserId, "receiverUserId"));
        payload.put("holdingUserId", requirePositive(holdingUserId, "holdingUserId"));
        payload.put("paymentMethod", normalizeRequired(paymentMethod, "paymentMethod"));
        if (fundingTradeOrderNo != null) {
            payload.put("fundingTradeOrderNo", fundingTradeOrderNo);
        }
        payload.put("redPacketStatus", (status == null ? RedPacketOrderStatus.PENDING_CLAIM : status).name());
        if (claimTradeOrderNo != null) {
            payload.put("claimTradeNo", claimTradeOrderNo);
            payload.put("claimTradeOrderNo", claimTradeOrderNo);
        } else {
            payload.remove("claimTradeNo");
            payload.remove("claimTradeOrderNo");
        }
        if (claimedAt != null) {
            payload.put("claimedAt", claimedAt.toString());
        } else {
            payload.remove("claimedAt");
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to build red packet payload", ex);
        }
    }

    private ObjectNode parsePayloadObject(String rawPayload) {
        String normalized = normalizeOptional(rawPayload);
        if (normalized == null) {
            return OBJECT_MAPPER.createObjectNode();
        }
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(normalized);
            if (jsonNode instanceof ObjectNode objectNode) {
                return objectNode.deepCopy();
            }
        } catch (Exception ignored) {
            // ignore invalid client payload and keep a raw backup below.
        }
        ObjectNode fallback = OBJECT_MAPPER.createObjectNode();
        fallback.put("clientPayloadRaw", normalized);
        return fallback;
    }

    private String readJsonText(String rawPayload, String fieldName) {
        String normalized = normalizeOptional(rawPayload);
        if (normalized == null) {
            return null;
        }
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(normalized);
            if (jsonNode != null && jsonNode.hasNonNull(fieldName)) {
                String value = jsonNode.get(fieldName).asText();
                return normalizeOptional(value);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String loadOriginalExtPayload(String messageId) {
        return messageRepository.findByMessageId(messageId)
                .map(ChatMessage::getExtPayload)
                .orElse(null);
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
