package cn.openaipay.domain.conversation.repository;

import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;
import cn.openaipay.domain.conversation.model.ConversationSummary;
import java.util.List;
import java.util.Optional;

/**
 * 会话仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface ConversationRepository {

    /**
     * 按会话单号查找记录。
     */
    Optional<ConversationSession> findSessionByConversationNo(String conversationNo);

    /**
     * 按业务KEY查找记录。
     */
    Optional<ConversationSession> findSessionByBizKey(String bizKey);

    /**
     * 保存业务数据。
     */
    ConversationSession saveSession(ConversationSession session);

    /**
     * 查找成员信息。
     */
    Optional<ConversationMember> findMember(String conversationNo, Long userId);

    /**
     * 保存成员信息。
     */
    ConversationMember saveMember(ConversationMember member);

    /**
     * 查询业务数据列表。
     */
    List<ConversationSummary> listSummaries(Long userId, int limit);
}
