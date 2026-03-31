package cn.openaipay.domain.conversation.service.impl;

import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;
import cn.openaipay.domain.conversation.service.ConversationDomainService;
import cn.openaipay.domain.conversation.service.ConversationOpenPlan;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 会话领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class ConversationDomainServiceImpl implements ConversationDomainService {

    /**
     * 构建业务KEY信息。
     */
    @Override
    public String buildPrivateBizKey(Long userId, Long peerUserId) {
        long first = Math.min(requirePositive(userId, "userId"), requirePositive(peerUserId, "peerUserId"));
        long second = Math.max(userId, peerUserId);
        return "PRIVATE:" + first + ":" + second;
    }

    /**
     * 开通会话信息。
     */
    @Override
    public ConversationOpenPlan openPrivateConversation(ConversationSession existingSession,
                                                        ConversationMember existingInitiatorMember,
                                                        ConversationMember existingPeerMember,
                                                        Long userId,
                                                        Long peerUserId,
                                                        LocalDateTime now) {
        Long initiator = requirePositive(userId, "userId");
        Long peer = requirePositive(peerUserId, "peerUserId");
        if (initiator.equals(peer)) {
            throw new IllegalArgumentException("userId and peerUserId must be different");
        }
        LocalDateTime openedAt = now == null ? LocalDateTime.now() : now;
        String bizKey = buildPrivateBizKey(initiator, peer);
        ConversationSession session = existingSession == null
                ? ConversationSession.privateConversation(buildConversationNo(openedAt), bizKey, openedAt)
                : existingSession;
        ConversationMember initiatorMember = existingInitiatorMember == null
                ? ConversationMember.create(session.getConversationNo(), initiator, peer, openedAt)
                : existingInitiatorMember;
        ConversationMember peerMember = existingPeerMember == null
                ? ConversationMember.create(session.getConversationNo(), peer, initiator, openedAt)
                : existingPeerMember;
        return new ConversationOpenPlan(bizKey, session, initiatorMember, peerMember);
    }

    private String buildConversationNo(LocalDateTime now) {
        long timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return "CVS" + timestamp
                + String.format(Locale.ROOT, "%03d", ThreadLocalRandom.current().nextInt(0, 1000));
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
