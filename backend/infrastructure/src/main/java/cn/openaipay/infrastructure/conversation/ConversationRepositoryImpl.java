package cn.openaipay.infrastructure.conversation;

import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;
import cn.openaipay.domain.conversation.model.ConversationSummary;
import cn.openaipay.domain.conversation.model.ConversationType;
import cn.openaipay.domain.conversation.repository.ConversationRepository;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationMemberDO;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationSessionDO;
import cn.openaipay.infrastructure.conversation.mapper.ConversationMemberMapper;
import cn.openaipay.infrastructure.conversation.mapper.ConversationSessionMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class ConversationRepositoryImpl implements ConversationRepository {

    /** 会话会话信息 */
    private final ConversationSessionMapper conversationSessionMapper;
    /** 会话成员信息 */
    private final ConversationMemberMapper conversationMemberMapper;
    /** 用户信息 */
    private final UserAccountMapper userAccountMapper;
    /** 用户资料信息 */
    private final UserProfileMapper userProfileMapper;

    public ConversationRepositoryImpl(ConversationSessionMapper conversationSessionMapper,
                                      ConversationMemberMapper conversationMemberMapper,
                                      UserAccountMapper userAccountMapper,
                                      UserProfileMapper userProfileMapper) {
        this.conversationSessionMapper = conversationSessionMapper;
        this.conversationMemberMapper = conversationMemberMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * 按会话单号查找记录。
     */
    @Override
    public Optional<ConversationSession> findSessionByConversationNo(String conversationNo) {
        return conversationSessionMapper.findByConversationNo(conversationNo).map(this::toDomainSession);
    }

    /**
     * 按业务KEY查找记录。
     */
    @Override
    public Optional<ConversationSession> findSessionByBizKey(String bizKey) {
        return conversationSessionMapper.findByBizKey(bizKey).map(this::toDomainSession);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public ConversationSession saveSession(ConversationSession session) {
        ConversationSessionDO entity = conversationSessionMapper.findByConversationNo(session.getConversationNo())
                .orElse(new ConversationSessionDO());
        fillSessionDO(entity, session);
        return toDomainSession(conversationSessionMapper.save(entity));
    }

    /**
     * 查找成员信息。
     */
    @Override
    public Optional<ConversationMember> findMember(String conversationNo, Long userId) {
        return conversationMemberMapper.findByConversationAndUser(conversationNo, userId).map(this::toDomainMember);
    }

    /**
     * 保存成员信息。
     */
    @Override
    @Transactional
    public ConversationMember saveMember(ConversationMember member) {
        ConversationMemberDO entity = conversationMemberMapper
                .findByConversationAndUser(member.getConversationNo(), member.getUserId())
                .orElse(new ConversationMemberDO());
        fillMemberDO(entity, member);
        return toDomainMember(conversationMemberMapper.save(entity));
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    public List<ConversationSummary> listSummaries(Long userId, int limit) {
        List<ConversationMemberDO> members = conversationMemberMapper.listByUserId(userId, limit);
        if (members.isEmpty()) {
            return List.of();
        }

        List<String> conversationNos = members.stream().map(ConversationMemberDO::getConversationNo).toList();
        Map<String, ConversationSessionDO> sessionMap = conversationSessionMapper.findByConversationNos(conversationNos)
                .stream()
                .collect(Collectors.toMap(ConversationSessionDO::getConversationNo, Function.identity(), (first, ignored) -> first));

        List<Long> peerUserIds = members.stream().map(ConversationMemberDO::getPeerUserId).toList();
        Map<Long, UserAccountDO> accountMap = userAccountMapper.findByUserIds(peerUserIds)
                .stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (first, ignored) -> first));
        Map<Long, UserProfileDO> profileMap = userProfileMapper.findByUserIds(peerUserIds)
                .stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (first, ignored) -> first));

        List<ConversationSummary> summaries = new ArrayList<>();
        for (ConversationMemberDO member : members) {
            ConversationSessionDO session = sessionMap.get(member.getConversationNo());
            if (session == null) {
                continue;
            }
            Long peerUserId = member.getPeerUserId();
            UserAccountDO account = peerUserId == null ? null : accountMap.get(peerUserId);
            UserProfileDO profile = peerUserId == null ? null : profileMap.get(peerUserId);

            summaries.add(new ConversationSummary(
                    session.getConversationNo(),
                    session.getConversationType(),
                    member.getUserId(),
                    peerUserId,
                    account == null ? null : account.getAipayUid(),
                    profile == null ? null : profile.getNickname(),
                    profile == null ? null : profile.getAvatarUrl(),
                    member.getUnreadCount() == null ? 0 : member.getUnreadCount(),
                    session.getLastMessageId(),
                    session.getLastMessagePreview(),
                    session.getLastMessageAt(),
                    member.getUpdatedAt() == null ? session.getUpdatedAt() : member.getUpdatedAt()
            ));
        }

        summaries.sort(Comparator.comparing(ConversationSummary::updatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return summaries;
    }

    private ConversationSession toDomainSession(ConversationSessionDO entity) {
        return new ConversationSession(
                entity.getId(),
                entity.getConversationNo(),
                ConversationType.from(entity.getConversationType()),
                entity.getBizKey(),
                entity.getLastMessageId(),
                entity.getLastMessagePreview(),
                entity.getLastMessageAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ConversationMember toDomainMember(ConversationMemberDO entity) {
        return new ConversationMember(
                entity.getId(),
                entity.getConversationNo(),
                entity.getUserId(),
                entity.getPeerUserId(),
                entity.getUnreadCount() == null ? 0 : entity.getUnreadCount(),
                entity.getLastReadMessageId(),
                entity.getLastReadAt(),
                Boolean.TRUE.equals(entity.getMuteFlag()),
                Boolean.TRUE.equals(entity.getPinFlag()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillSessionDO(ConversationSessionDO entity, ConversationSession session) {
        LocalDateTime now = LocalDateTime.now();
        entity.setConversationNo(session.getConversationNo());
        entity.setConversationType(session.getConversationType().name());
        entity.setBizKey(session.getBizKey());
        entity.setLastMessageId(session.getLastMessageId());
        entity.setLastMessagePreview(session.getLastMessagePreview());
        entity.setLastMessageAt(session.getLastMessageAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(session.getCreatedAt() == null ? now : session.getCreatedAt());
        }
        entity.setUpdatedAt(session.getUpdatedAt() == null ? now : session.getUpdatedAt());
    }

    private void fillMemberDO(ConversationMemberDO entity, ConversationMember member) {
        LocalDateTime now = LocalDateTime.now();
        entity.setConversationNo(member.getConversationNo());
        entity.setUserId(member.getUserId());
        entity.setPeerUserId(member.getPeerUserId());
        entity.setUnreadCount(member.getUnreadCount());
        entity.setLastReadMessageId(member.getLastReadMessageId());
        entity.setLastReadAt(member.getLastReadAt());
        entity.setMuteFlag(member.isMute());
        entity.setPinFlag(member.isPin());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(member.getCreatedAt() == null ? now : member.getCreatedAt());
        }
        entity.setUpdatedAt(member.getUpdatedAt() == null ? now : member.getUpdatedAt());
    }
}
