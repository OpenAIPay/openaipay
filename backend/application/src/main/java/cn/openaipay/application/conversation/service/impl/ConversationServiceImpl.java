package cn.openaipay.application.conversation.service.impl;

import cn.openaipay.application.conversation.command.MarkConversationReadCommand;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.service.ConversationService;
import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;
import cn.openaipay.domain.conversation.model.ConversationSummary;
import cn.openaipay.domain.conversation.repository.ConversationRepository;
import cn.openaipay.domain.conversation.service.ConversationDomainService;
import cn.openaipay.domain.conversation.service.ConversationOpenPlan;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    /** 默认信息 */
    private static final int DEFAULT_LIMIT = 20;
    /** 最大信息 */
    private static final int MAX_LIMIT = 100;

    /** 会话信息 */
    private final ConversationRepository conversationRepository;
    /** 会话域信息 */
    private final ConversationDomainService conversationDomainService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                              ConversationDomainService conversationDomainService) {
        this.conversationRepository = conversationRepository;
        this.conversationDomainService = conversationDomainService;
    }

    /**
     * 开通会话信息。
     */
    @Override
    @Transactional
    public ConversationDTO openPrivateConversation(OpenPrivateConversationCommand command) {
        Long userId = requirePositive(command.userId(), "userId");
        Long peerUserId = requirePositive(command.peerUserId(), "peerUserId");
        if (userId.equals(peerUserId)) {
            throw new IllegalArgumentException("userId and peerUserId must be different");
        }

        String bizKey = conversationDomainService.buildPrivateBizKey(userId, peerUserId);
        LocalDateTime now = LocalDateTime.now();
        ConversationSession session = conversationRepository.findSessionByBizKey(bizKey).orElse(null);
        ConversationMember initiatorMember = session == null
                ? null
                : conversationRepository.findMember(session.getConversationNo(), userId).orElse(null);
        ConversationMember peerMember = session == null
                ? null
                : conversationRepository.findMember(session.getConversationNo(), peerUserId).orElse(null);
        ConversationOpenPlan openPlan = conversationDomainService.openPrivateConversation(
                session,
                initiatorMember,
                peerMember,
                userId,
                peerUserId,
                now
        );
        ConversationSession savedSession = conversationRepository.saveSession(openPlan.session());
        conversationRepository.saveMember(openPlan.initiatorMember());
        conversationRepository.saveMember(openPlan.peerMember());
        String conversationNo = savedSession.getConversationNo();

        return conversationRepository.listSummaries(userId, MAX_LIMIT).stream()
                .filter(summary -> conversationNo.equals(summary.conversationNo()))
                .findFirst()
                .map(this::toDTO)
                .orElse(new ConversationDTO(
                        savedSession.getConversationNo(),
                        savedSession.getConversationType().name(),
                        userId,
                        peerUserId,
                        null,
                        null,
                        null,
                        0,
                        savedSession.getLastMessageId(),
                        savedSession.getLastMessagePreview(),
                        savedSession.getLastMessageAt(),
                        savedSession.getUpdatedAt()
                ));
    }

    /**
     * 查询用户会话信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> listUserConversations(Long userId, Integer limit) {
        return conversationRepository.listSummaries(
                        requirePositive(userId, "userId"),
                        normalizeLimit(limit)
                ).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 标记会话信息。
     */
    @Override
    @Transactional
    public void markConversationRead(MarkConversationReadCommand command) {
        Long userId = requirePositive(command.userId(), "userId");
        String conversationNo = normalizeRequired(command.conversationNo(), "conversationNo");
        ConversationMember member = conversationRepository.findMember(conversationNo, userId)
                .orElseThrow(() -> new NoSuchElementException("conversation member not found"));
        member.markRead(command.lastReadMessageId(), LocalDateTime.now());
        conversationRepository.saveMember(member);
    }

    /**
     * 处理消息信息。
     */
    @Override
    @Transactional
    public void appendMessage(String conversationNo,
                              Long senderUserId,
                              Long receiverUserId,
                              String messageId,
                              String preview,
                              LocalDateTime messageAt) {
        String normalizedConversationNo = normalizeRequired(conversationNo, "conversationNo");
        Long sender = requirePositive(senderUserId, "senderUserId");
        Long receiver = requirePositive(receiverUserId, "receiverUserId");
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("senderUserId and receiverUserId must be different");
        }

        ConversationSession session = conversationRepository.findSessionByConversationNo(normalizedConversationNo)
                .orElseThrow(() -> new NoSuchElementException("conversation not found: " + normalizedConversationNo));

        LocalDateTime now = messageAt == null ? LocalDateTime.now() : messageAt;
        session.updateLastMessage(normalizeRequired(messageId, "messageId"), preview, now, now);
        conversationRepository.saveSession(session);

        ConversationMember senderMember = conversationRepository.findMember(normalizedConversationNo, sender)
                .orElseGet(() -> ConversationMember.create(normalizedConversationNo, sender, receiver, now));
        ConversationMember receiverMember = conversationRepository.findMember(normalizedConversationNo, receiver)
                .orElseGet(() -> ConversationMember.create(normalizedConversationNo, receiver, sender, now));

        receiverMember.increaseUnread(now);
        conversationRepository.saveMember(senderMember);
        conversationRepository.saveMember(receiverMember);
    }

    /**
     * 判断是否存在成员信息。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasMember(String conversationNo, Long userId) {
        return conversationRepository.findMember(
                normalizeRequired(conversationNo, "conversationNo"),
                requirePositive(userId, "userId")
        ).isPresent();
    }

    private ConversationDTO toDTO(ConversationSummary summary) {
        return new ConversationDTO(
                summary.conversationNo(),
                summary.conversationType(),
                summary.userId(),
                summary.peerUserId(),
                summary.peerAipayUid(),
                summary.peerNickname(),
                summary.peerAvatarUrl(),
                summary.unreadCount(),
                summary.lastMessageId(),
                summary.lastMessagePreview(),
                summary.lastMessageAt(),
                summary.updatedAt()
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
}
