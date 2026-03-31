package cn.openaipay.application.contact.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.service.impl.ContactServiceImpl;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import cn.openaipay.domain.contact.model.ContactRequestStatus;
import cn.openaipay.domain.contact.repository.ContactRepository;
import cn.openaipay.domain.contact.service.ContactDomainService;
import cn.openaipay.domain.contact.service.ContactRequestHandlePlan;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ContactServiceImplTest 验证好友申请在“重复待处理”场景下的附言更新行为。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    /** 申请发起方用户ID。 */
    private static final long REQUESTER_USER_ID = 880100068483692100L;
    /** 申请目标用户ID。 */
    private static final long TARGET_USER_ID = 880102069981881102L;

    /** 联系人仓储。 */
    @Mock
    private ContactRepository contactRepository;
    /** 联系人领域服务。 */
    @Mock
    private ContactDomainService contactDomainService;
    /** 会话门面。 */
    @Mock
    private ConversationFacade conversationFacade;
    /** 消息仓储。 */
    @Mock
    private MessageRepository messageRepository;
    /** 分布式 ID 生成器。 */
    @Mock
    private AiPayIdGenerator aiPayIdGenerator;

    /** 联系人应用服务。 */
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactServiceImpl(
                contactRepository,
                contactDomainService,
                conversationFacade,
                messageRepository,
                aiPayIdGenerator
        );
    }

    @Test
    void applyFriendRequestShouldRefreshPendingApplyMessageWhenClientSendsNewMessage() {
        ContactRequest pendingRequest = buildPendingRequest("hello");
        ContactRequest savedRequest = buildSavedRequestFromPending(pendingRequest, "我是顾郡，麻烦通过");

        when(contactRepository.isBlocked(anyLong(), anyLong())).thenReturn(false);
        when(contactRepository.findFriendship(REQUESTER_USER_ID, TARGET_USER_ID)).thenReturn(Optional.empty());
        when(contactRepository.findPendingRequest(REQUESTER_USER_ID, TARGET_USER_ID)).thenReturn(Optional.of(pendingRequest));
        when(contactDomainService.prepareFriendRequest(
                eq(REQUESTER_USER_ID),
                eq(TARGET_USER_ID),
                anyBoolean(),
                anyBoolean(),
                eq(pendingRequest),
                eq("我是顾郡，麻烦通过"),
                any(LocalDateTime.class)
        )).thenReturn(pendingRequest);
        when(contactRepository.saveRequest(any(ContactRequest.class))).thenReturn(savedRequest);

        ContactRequestDTO dto = contactService.applyFriendRequest(
                new ApplyFriendRequestCommand(REQUESTER_USER_ID, TARGET_USER_ID, "我是顾郡，麻烦通过")
        );

        ArgumentCaptor<ContactRequest> requestCaptor = ArgumentCaptor.forClass(ContactRequest.class);
        verify(contactRepository).saveRequest(requestCaptor.capture());
        ContactRequest capturedRequest = requestCaptor.getValue();

        assertEquals("我是顾郡，麻烦通过", capturedRequest.getApplyMessage());
        assertEquals("我是顾郡，麻烦通过", dto.applyMessage());
        assertEquals(pendingRequest.getRequestNo(), capturedRequest.getRequestNo());
        assertEquals(pendingRequest.getCreatedAt(), capturedRequest.getCreatedAt());
    }

    @Test
    void applyFriendRequestShouldKeepPendingApplyMessageWhenLatestMessageBlank() {
        ContactRequest pendingRequest = buildPendingRequest("hello");

        when(contactRepository.isBlocked(anyLong(), anyLong())).thenReturn(false);
        when(contactRepository.findFriendship(REQUESTER_USER_ID, TARGET_USER_ID)).thenReturn(Optional.empty());
        when(contactRepository.findPendingRequest(REQUESTER_USER_ID, TARGET_USER_ID)).thenReturn(Optional.of(pendingRequest));
        when(contactDomainService.prepareFriendRequest(
                eq(REQUESTER_USER_ID),
                eq(TARGET_USER_ID),
                anyBoolean(),
                anyBoolean(),
                eq(pendingRequest),
                eq("   "),
                any(LocalDateTime.class)
        )).thenReturn(pendingRequest);

        ContactRequestDTO dto = contactService.applyFriendRequest(
                new ApplyFriendRequestCommand(REQUESTER_USER_ID, TARGET_USER_ID, "   ")
        );

        verify(contactRepository, never()).saveRequest(any(ContactRequest.class));
        assertEquals("hello", dto.applyMessage());
    }

    @Test
    void handleFriendRequestShouldSendSystemNoticeToRequesterWhenRejected() {
        ContactRequest pendingRequest = buildPendingRequest("hello");
        ContactRequest rejectedRequest = new ContactRequest(
                pendingRequest.getId(),
                pendingRequest.getRequestNo(),
                pendingRequest.getRequesterUserId(),
                pendingRequest.getTargetUserId(),
                pendingRequest.getApplyMessage(),
                ContactRequestStatus.REJECTED,
                TARGET_USER_ID,
                LocalDateTime.of(2026, 3, 23, 19, 0, 0),
                pendingRequest.getCreatedAt(),
                LocalDateTime.of(2026, 3, 23, 19, 0, 0)
        );

        when(contactRepository.findRequestByRequestNo(pendingRequest.getRequestNo())).thenReturn(Optional.of(pendingRequest));
        when(contactDomainService.handleRequest(
                eq(pendingRequest),
                eq("REJECT"),
                eq(TARGET_USER_ID),
                any(LocalDateTime.class)
        )).thenReturn(new ContactRequestHandlePlan(rejectedRequest, List.of()));
        when(conversationFacade.openPrivateConversation(any())).thenReturn(new ConversationDTO(
                "CONV202603230001",
                "PRIVATE",
                TARGET_USER_ID,
                REQUESTER_USER_ID,
                null,
                null,
                null,
                0L,
                null,
                null,
                null,
                LocalDateTime.of(2026, 3, 23, 19, 0, 0)
        ));
        when(aiPayIdGenerator.generate(
                eq(AiPayIdGenerator.DOMAIN_TRADE),
                eq("90"),
                eq(String.valueOf(TARGET_USER_ID))
        )).thenReturn("10012026032319000009000000000001");
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContactRequestDTO dto = contactService.handleFriendRequest(
                new HandleFriendRequestCommand(TARGET_USER_ID, pendingRequest.getRequestNo(), "REJECT")
        );

        assertEquals("REJECTED", dto.status());

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(messageCaptor.capture());
        ChatMessage savedMessage = messageCaptor.getValue();
        assertEquals(MessageType.SYSTEM, savedMessage.getMessageType());
        assertEquals(TARGET_USER_ID, savedMessage.getSenderUserId());
        assertEquals(REQUESTER_USER_ID, savedMessage.getReceiverUserId());
        assertEquals("对方拒绝了你的好友申请", savedMessage.getContentText());
        verify(conversationFacade).appendMessage(
                eq("CONV202603230001"),
                eq(TARGET_USER_ID),
                eq(REQUESTER_USER_ID),
                eq(savedMessage.getMessageId()),
                eq("对方拒绝了你的好友申请"),
                any(LocalDateTime.class)
        );
    }

    @Test
    void handleFriendRequestShouldNotSendSystemNoticeWhenAccepted() {
        ContactRequest pendingRequest = buildPendingRequest("hello");
        ContactRequest acceptedRequest = new ContactRequest(
                pendingRequest.getId(),
                pendingRequest.getRequestNo(),
                pendingRequest.getRequesterUserId(),
                pendingRequest.getTargetUserId(),
                pendingRequest.getApplyMessage(),
                ContactRequestStatus.ACCEPTED,
                TARGET_USER_ID,
                LocalDateTime.of(2026, 3, 23, 19, 5, 0),
                pendingRequest.getCreatedAt(),
                LocalDateTime.of(2026, 3, 23, 19, 5, 0)
        );
        ContactFriendship forward = ContactFriendship.create(
                REQUESTER_USER_ID,
                TARGET_USER_ID,
                pendingRequest.getRequestNo(),
                LocalDateTime.of(2026, 3, 23, 19, 5, 0)
        );
        ContactFriendship reverse = ContactFriendship.create(
                TARGET_USER_ID,
                REQUESTER_USER_ID,
                pendingRequest.getRequestNo(),
                LocalDateTime.of(2026, 3, 23, 19, 5, 0)
        );

        when(contactRepository.findRequestByRequestNo(pendingRequest.getRequestNo())).thenReturn(Optional.of(pendingRequest));
        when(contactDomainService.handleRequest(
                eq(pendingRequest),
                eq("ACCEPT"),
                eq(TARGET_USER_ID),
                any(LocalDateTime.class)
        )).thenReturn(new ContactRequestHandlePlan(acceptedRequest, List.of(forward, reverse)));
        when(contactRepository.findFriendship(anyLong(), anyLong())).thenReturn(Optional.empty());

        ContactRequestDTO dto = contactService.handleFriendRequest(
                new HandleFriendRequestCommand(TARGET_USER_ID, pendingRequest.getRequestNo(), "ACCEPT")
        );

        assertEquals("ACCEPTED", dto.status());
        verify(messageRepository, never()).save(any(ChatMessage.class));
        verify(conversationFacade, never()).appendMessage(any(), anyLong(), anyLong(), any(), any(), any(LocalDateTime.class));
    }

    private ContactRequest buildPendingRequest(String applyMessage) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 22, 23, 40, 36);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 22, 23, 40, 36);
        return new ContactRequest(
                3L,
                "CTRQ1774194036135621",
                REQUESTER_USER_ID,
                TARGET_USER_ID,
                applyMessage,
                ContactRequestStatus.PENDING,
                null,
                null,
                createdAt,
                updatedAt
        );
    }

    private ContactRequest buildSavedRequestFromPending(ContactRequest pendingRequest, String latestApplyMessage) {
        return new ContactRequest(
                pendingRequest.getId(),
                pendingRequest.getRequestNo(),
                pendingRequest.getRequesterUserId(),
                pendingRequest.getTargetUserId(),
                latestApplyMessage,
                pendingRequest.getStatus(),
                pendingRequest.getHandledByUserId(),
                pendingRequest.getHandledAt(),
                pendingRequest.getCreatedAt(),
                LocalDateTime.of(2026, 3, 23, 18, 10, 0)
        );
    }
}
