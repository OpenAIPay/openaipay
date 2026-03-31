package cn.openaipay.domain.conversation.service;

import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;
import java.time.LocalDateTime;

/**
 * 会话领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface ConversationDomainService {

    /**
     * 构建业务KEY信息。
     */
    String buildPrivateBizKey(Long userId, Long peerUserId);

    ConversationOpenPlan openPrivateConversation(ConversationSession existingSession,
                                                 ConversationMember existingInitiatorMember,
                                                 ConversationMember existingPeerMember,
                                                 Long userId,
                                                 Long peerUserId,
                                                 LocalDateTime now);
}
