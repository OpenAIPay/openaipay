package cn.openaipay.application.message.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.contact.facade.ContactFacade;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.media.facade.MediaFacade;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.orchestrator.impl.MessageMoneyOrchestratorImpl;
import cn.openaipay.application.message.service.MessageService;
import cn.openaipay.application.message.service.impl.MessageServiceImpl;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.application.trade.command.CreateTransferTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.facade.UserFacade;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.model.RedPacketOrderStatus;
import cn.openaipay.domain.message.repository.MessageRepository;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MessageFacadeImplTest 门面行为测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class MessageFacadeImplTest {

    /** object mapper。 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** 发送方用户 ID。 */
    private static final Long SENDER_USER_ID = 880109000000000001L;
    /** 接收方用户 ID。 */
    private static final Long RECEIVER_USER_ID = 880109000000000002L;
    /** 红包托管户用户 ID。 */
    private static final Long HOLDING_USER_ID = 880909000000000031L;
    /** 会话号。 */
    private static final String CONVERSATION_NO = "CONV202603210001";

    /** 消息仓储。 */
    @Mock
    private MessageRepository messageRepository;
    /** 红包仓储。 */
    @Mock
    private RedPacketOrderRepository redPacketOrderRepository;
    /** 会话门面。 */
    @Mock
    private ConversationFacade conversationFacade;
    /** 联系人门面。 */
    @Mock
    private ContactFacade contactFacade;
    /** 媒体门面。 */
    @Mock
    private MediaFacade mediaFacade;
    /** 用户门面。 */
    @Mock
    private UserFacade userFacade;
    /** 交易门面。 */
    @Mock
    private TradeFacade tradeFacade;
    /** ID 生成器。 */
    @Mock
    private AiPayIdGenerator aiPayIdGenerator;

    /** 消息门面。 */
    private MessageFacadeImpl facade;

    @BeforeEach
    void setUp() {
        MessageMoneyOrchestratorImpl orchestrator = new MessageMoneyOrchestratorImpl(
                messageRepository,
                redPacketOrderRepository,
                conversationFacade,
                contactFacade,
                tradeFacade,
                HOLDING_USER_ID,
                aiPayIdGenerator
        );
        MessageService messageService = new MessageServiceImpl(
                messageRepository,
                redPacketOrderRepository,
                conversationFacade,
                contactFacade,
                mediaFacade,
                userFacade,
                orchestrator,
                aiPayIdGenerator
        );
        facade = new MessageFacadeImpl(messageService);

        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendTransferMessageShouldCreateTransferTradeAndPersistMessageViaFacade() {
        when(contactFacade.isFriend(SENDER_USER_ID, RECEIVER_USER_ID)).thenReturn(true);
        when(aiPayIdGenerator.generate(anyString(), anyString(), anyString()))
                .thenReturn("202603210001000000000000000001", "202603210001000000000000000002");
        when(conversationFacade.openPrivateConversation(any(OpenPrivateConversationCommand.class)))
                .thenReturn(conversation());
        when(tradeFacade.transfer(any(CreateTransferTradeCommand.class)))
                .thenReturn(trade("TRD202603210001", "BANK_CARD"));

        MessageDTO message = facade.sendTransferMessage(new SendTransferMessageCommand(
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                money("88.88"),
                "BANK_CARD",
                "CARD_TOOL_ICBC_9005",
                "房租",
                "{\"memo\":\"chat\"}"
        ));

        ArgumentCaptor<CreateTransferTradeCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTransferTradeCommand.class);
        verify(tradeFacade).transfer(commandCaptor.capture());
        CreateTransferTradeCommand command = commandCaptor.getValue();

        assertTrue(command.requestNo().startsWith("TRF"));
        assertEquals("CHAT_TRANSFER", command.businessSceneCode());
        assertEquals(SENDER_USER_ID, command.payerUserId());
        assertEquals(RECEIVER_USER_ID, command.payeeUserId());
        assertEquals("BANK_CARD", command.paymentMethod());
        assertEquals(money("88.88"), command.amount());
        assertEquals("CARD_TOOL_ICBC_9005", command.paymentToolCode());
        assertEquals("房租", command.metadata());
        assertEquals("TRANSFER", message.messageType());
        assertEquals("[转账]", message.contentText());
        assertEquals("TRD202603210001", message.tradeOrderNo());
        assertEquals(money("88.88"), message.amount());
    }

    @Test
    void sendRedPacketMessageShouldPersistOrderAndReturnRedPacketPayloadViaFacade() throws Exception {
        when(contactFacade.isFriend(SENDER_USER_ID, RECEIVER_USER_ID)).thenReturn(true);
        when(redPacketOrderRepository.save(any(RedPacketOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiPayIdGenerator.generate(anyString(), anyString(), anyString())).thenReturn(
                "202603210001000000000000000011",
                "202603210001000000000000000012",
                "202603210001000000000000000013"
        );
        when(conversationFacade.openPrivateConversation(any(OpenPrivateConversationCommand.class)))
                .thenReturn(conversation());
        when(tradeFacade.transfer(any(CreateTransferTradeCommand.class)))
                .thenReturn(trade("TRD202603210002", "wallet"));

        MessageDTO message = facade.sendRedPacketMessage(new SendRedPacketMessageCommand(
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                money("18.88"),
                "wallet",
                "{\"coverId\":\"cover-1\",\"coverTitle\":\"恭喜发财\",\"blessingText\":\"周末快乐\"}"
        ));

        ArgumentCaptor<CreateTransferTradeCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTransferTradeCommand.class);
        ArgumentCaptor<RedPacketOrder> orderCaptor = ArgumentCaptor.forClass(RedPacketOrder.class);
        verify(tradeFacade).transfer(commandCaptor.capture());
        verify(redPacketOrderRepository).save(orderCaptor.capture());

        CreateTransferTradeCommand command = commandCaptor.getValue();
        RedPacketOrder savedOrder = orderCaptor.getValue();
        JsonNode payload = OBJECT_MAPPER.readTree(message.extPayload());

        assertTrue(command.requestNo().startsWith("RPS"));
        assertEquals("CHAT_RED_PACKET_SEND", command.businessSceneCode());
        assertEquals(SENDER_USER_ID, command.payerUserId());
        assertEquals(HOLDING_USER_ID, command.payeeUserId());
        assertEquals("WALLET", command.paymentMethod());
        assertEquals(CONVERSATION_NO, savedOrder.getConversationNo());
        assertEquals(SENDER_USER_ID, savedOrder.getSenderUserId());
        assertEquals(RECEIVER_USER_ID, savedOrder.getReceiverUserId());
        assertEquals(HOLDING_USER_ID, savedOrder.getHoldingUserId());
        assertEquals("WALLET", savedOrder.getPaymentMethod());
        assertEquals(RedPacketOrderStatus.PENDING_CLAIM, savedOrder.getStatus());
        assertEquals("cover-1", savedOrder.getCoverId());
        assertEquals("恭喜发财", savedOrder.getCoverTitle());
        assertEquals("周末快乐", savedOrder.getBlessingText());
        assertEquals("RED_PACKET", message.messageType());
        assertEquals(savedOrder.getRedPacketNo(), payload.get("redPacketNo").asText());
        assertEquals("PENDING_CLAIM", payload.get("redPacketStatus").asText());
        assertEquals("WALLET", payload.get("paymentMethod").asText());
        assertFalse(payload.has("claimTradeNo"));
    }

    @Test
    void claimRedPacketShouldUpdatePayloadAndReturnDetailViaFacade() throws Exception {
        when(redPacketOrderRepository.save(any(RedPacketOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiPayIdGenerator.generate(anyString(), anyString(), anyString()))
                .thenReturn("202603210001000000000000000021");
        RedPacketOrder pendingOrder = RedPacketOrder.createPending(
                "RPK202603210001",
                "MSG202603210001",
                CONVERSATION_NO,
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                HOLDING_USER_ID,
                money("6.66"),
                "TRD_FUND_001",
                "WALLET",
                "cover-1",
                "好运红包",
                "恭喜发财",
                LocalDateTime.of(2026, 3, 21, 10, 0)
        );
        ChatMessage originalMessage = ChatMessage.create(
                pendingOrder.getMessageId(),
                CONVERSATION_NO,
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                MessageType.RED_PACKET,
                "[红包]",
                null,
                money("6.66"),
                "TRD_FUND_001",
                "{\"coverId\":\"cover-1\",\"coverTitle\":\"好运红包\",\"blessingText\":\"恭喜发财\"}",
                LocalDateTime.of(2026, 3, 21, 10, 0)
        );
        when(redPacketOrderRepository.findByRedPacketNo("RPK202603210001"))
                .thenReturn(Optional.of(pendingOrder));
        when(messageRepository.findByMessageId("MSG202603210001"))
                .thenReturn(Optional.of(originalMessage));
        when(tradeFacade.transfer(any(CreateTransferTradeCommand.class)))
                .thenReturn(trade("TRD_CLAIM_001", "WALLET"));
        when(userFacade.getProfile(SENDER_USER_ID)).thenReturn(userProfile(SENDER_USER_ID, "顾郡"));
        when(userFacade.getProfile(RECEIVER_USER_ID)).thenReturn(userProfile(RECEIVER_USER_ID, "祁欣"));

        RedPacketDetailDTO detail = facade.claimRedPacket(RECEIVER_USER_ID, "RPK202603210001");

        ArgumentCaptor<CreateTransferTradeCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTransferTradeCommand.class);
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(tradeFacade).transfer(commandCaptor.capture());
        verify(messageRepository).save(messageCaptor.capture());

        CreateTransferTradeCommand command = commandCaptor.getValue();
        ChatMessage savedMessage = messageCaptor.getValue();
        JsonNode payload = OBJECT_MAPPER.readTree(savedMessage.getExtPayload());

        assertTrue(command.requestNo().startsWith("RPC"));
        assertEquals("CHAT_RED_PACKET_CLAIM", command.businessSceneCode());
        assertEquals(HOLDING_USER_ID, command.payerUserId());
        assertEquals(RECEIVER_USER_ID, command.payeeUserId());
        assertEquals("WALLET", command.paymentMethod());
        assertEquals("CLAIMED", payload.get("redPacketStatus").asText());
        assertEquals("TRD_CLAIM_001", payload.get("claimTradeNo").asText());
        assertNotNull(payload.get("claimedAt").asText());
        assertEquals("CLAIMED", detail.status());
        assertEquals("TRD_CLAIM_001", detail.claimTradeNo());
        assertEquals("顾郡", detail.senderNickname());
        assertEquals("祁欣", detail.receiverNickname());
        assertFalse(detail.claimableByViewer());
        assertTrue(detail.claimedByViewer());
    }

    private ConversationDTO conversation() {
        return new ConversationDTO(
                CONVERSATION_NO,
                "PRIVATE",
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                String.valueOf(RECEIVER_USER_ID),
                "祁欣",
                "/api/media/qixin.png",
                0L,
                null,
                null,
                null,
                LocalDateTime.of(2026, 3, 21, 9, 0)
        );
    }

    private TradeOrderDTO trade(String tradeOrderNo, String paymentMethod) {
        return new TradeOrderDTO(
                tradeOrderNo,
                "REQ202603210001",
                "TRANSFER",
                "CHAT_TRANSFER",
                "WALLET",
                null,
                null,
                SENDER_USER_ID,
                RECEIVER_USER_ID,
                paymentMethod,
                money("1.00"),
                null,
                money("1.00"),
                money("1.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                "SUCCEEDED",
                null,
                null,
                null,
                LocalDateTime.of(2026, 3, 21, 9, 30),
                LocalDateTime.of(2026, 3, 21, 9, 31),
                List.of(),
                List.of()
        );
    }

    private UserProfileDTO userProfile(Long userId, String nickname) {
        return new UserProfileDTO(
                userId,
                String.valueOf(userId),
                "13920000002",
                "ACTIVE",
                "L2",
                "REGISTER",
                nickname,
                "/api/media/" + nickname + ".png",
                "86",
                "13920000002",
                nickname,
                "4401**********0001",
                "F",
                "广州",
                "1990-01-01"
        );
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
