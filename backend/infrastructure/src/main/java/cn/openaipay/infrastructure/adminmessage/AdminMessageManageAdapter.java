package cn.openaipay.infrastructure.adminmessage;

import cn.openaipay.application.adminmessage.dto.AdminContactBlacklistRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactFriendshipRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactRequestRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationMemberRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminRedPacketRowDTO;
import cn.openaipay.application.adminmessage.port.AdminMessageManagePort;
import cn.openaipay.infrastructure.contact.dataobject.ContactBlacklistDO;
import cn.openaipay.infrastructure.contact.dataobject.ContactFriendshipDO;
import cn.openaipay.infrastructure.contact.dataobject.ContactRequestDO;
import cn.openaipay.infrastructure.contact.mapper.ContactBlacklistMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactFriendshipMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactRequestMapper;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationMemberDO;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationSessionDO;
import cn.openaipay.infrastructure.conversation.mapper.ConversationMemberMapper;
import cn.openaipay.infrastructure.conversation.mapper.ConversationSessionMapper;
import cn.openaipay.infrastructure.message.dataobject.MessageRecordDO;
import cn.openaipay.infrastructure.message.dataobject.RedPacketOrderDO;
import cn.openaipay.infrastructure.message.mapper.MessageRecordMapper;
import cn.openaipay.infrastructure.message.mapper.RedPacketOrderMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 消息中心查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class AdminMessageManageAdapter implements AdminMessageManagePort {

    private final ConversationSessionMapper conversationSessionMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final MessageRecordMapper messageRecordMapper;
    private final RedPacketOrderMapper redPacketOrderMapper;
    private final ContactRequestMapper contactRequestMapper;
    private final ContactFriendshipMapper contactFriendshipMapper;
    private final ContactBlacklistMapper contactBlacklistMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;

    public AdminMessageManageAdapter(ConversationSessionMapper conversationSessionMapper,
                                     ConversationMemberMapper conversationMemberMapper,
                                     MessageRecordMapper messageRecordMapper,
                                     RedPacketOrderMapper redPacketOrderMapper,
                                     ContactRequestMapper contactRequestMapper,
                                     ContactFriendshipMapper contactFriendshipMapper,
                                     ContactBlacklistMapper contactBlacklistMapper,
                                     UserAccountMapper userAccountMapper,
                                     UserProfileMapper userProfileMapper) {
        this.conversationSessionMapper = conversationSessionMapper;
        this.conversationMemberMapper = conversationMemberMapper;
        this.messageRecordMapper = messageRecordMapper;
        this.redPacketOrderMapper = redPacketOrderMapper;
        this.contactRequestMapper = contactRequestMapper;
        this.contactFriendshipMapper = contactFriendshipMapper;
        this.contactBlacklistMapper = contactBlacklistMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public AdminMessageOverviewDTO overview() {
        long conversationCount = safeCount(conversationSessionMapper.selectCount(new QueryWrapper<>()));
        long messageCount = safeCount(messageRecordMapper.selectCount(new QueryWrapper<>()));
        long redPacketCount = safeCount(redPacketOrderMapper.selectCount(new QueryWrapper<>()));
        long pendingContactRequestCount = safeCount(contactRequestMapper.selectCount(
                new QueryWrapper<ContactRequestDO>().eq("status", "PENDING")
        ));
        long friendshipCount = safeCount(contactFriendshipMapper.selectCount(new QueryWrapper<>()));
        long blacklistCount = safeCount(contactBlacklistMapper.selectCount(new QueryWrapper<>()));
        return new AdminMessageOverviewDTO(
                conversationCount,
                messageCount,
                redPacketCount,
                pendingContactRequestCount,
                friendshipCount,
                blacklistCount
        );
    }

    @Override
    public List<AdminConversationRowDTO> listConversations(String keyword, Long userId, int pageNo, int pageSize) {
        String normalizedKeyword = normalizeKeyword(keyword);
        int offset = calculateOffset(pageNo, pageSize);
        int fetchSize = offset + Math.max(1, pageSize);

        List<ConversationSessionDO> sessions;
        Map<String, ConversationMemberDO> selectedMemberMap = new LinkedHashMap<>();
        if (userId != null && userId > 0) {
            List<ConversationMemberDO> selectedMembers = safeList(conversationMemberMapper.listByUserId(userId, fetchSize));
            selectedMembers.forEach(item -> selectedMemberMap.put(item.getConversationNo(), item));
            List<String> conversationNos = selectedMembers.stream()
                    .map(ConversationMemberDO::getConversationNo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            sessions = safeList(conversationSessionMapper.findByConversationNos(conversationNos)).stream()
                    .sorted(Comparator
                            .comparing(ConversationSessionDO::getLastMessageAt, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(ConversationSessionDO::getConversationNo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } else {
            QueryWrapper<ConversationSessionDO> wrapper = new QueryWrapper<>();
            if (normalizedKeyword != null) {
                wrapper.and(query -> query.like("conversation_no", normalizedKeyword)
                        .or()
                        .like("biz_key", normalizedKeyword)
                        .or()
                        .like("last_message_preview", normalizedKeyword));
            }
            wrapper.orderByDesc("last_message_at", "id");
            wrapper.last(buildPageClause(pageNo, pageSize));
            sessions = safeList(conversationSessionMapper.selectList(wrapper));
        }

        if (sessions.isEmpty()) {
            return List.of();
        }

        List<String> conversationNos = sessions.stream()
                .map(ConversationSessionDO::getConversationNo)
                .filter(Objects::nonNull)
                .toList();
        List<ConversationMemberDO> allMembers = safeList(conversationMemberMapper.selectList(
                new QueryWrapper<ConversationMemberDO>().in("conversation_no", conversationNos)
        ));
        Map<String, List<ConversationMemberDO>> membersByConversation = allMembers.stream()
                .collect(Collectors.groupingBy(ConversationMemberDO::getConversationNo));
        Map<String, Long> memberCountMap = membersByConversation.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.valueOf(entry.getValue().size())));

        Set<Long> userIds = new LinkedHashSet<>();
        allMembers.forEach(item -> {
            if (item.getUserId() != null && item.getUserId() > 0) {
                userIds.add(item.getUserId());
            }
            if (item.getPeerUserId() != null && item.getPeerUserId() > 0) {
                userIds.add(item.getPeerUserId());
            }
        });
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(userIds);

        return sessions.stream()
                .map(session -> {
                    ConversationMemberDO selectedMember = selectedMemberMap.get(session.getConversationNo());
                    Long peerUserId = selectedMember != null && selectedMember.getPeerUserId() != null
                            ? selectedMember.getPeerUserId()
                            : resolvePeerUserId(membersByConversation.get(session.getConversationNo()), selectedMember);
                    UserDigest peerDigest = peerUserId == null ? null : userDigestMap.get(peerUserId);
                    return new AdminConversationRowDTO(
                            session.getConversationNo(),
                            session.getConversationType(),
                            session.getBizKey(),
                            session.getLastMessagePreview(),
                            session.getLastMessageAt(),
                            memberCountMap.getOrDefault(session.getConversationNo(), 0L),
                            selectedMember == null ? null : selectedMember.getUnreadCount(),
                            peerUserId,
                            peerDigest == null ? null : peerDigest.displayName(),
                            peerDigest == null ? null : peerDigest.aipayUid()
                    );
                })
                .filter(item -> normalizedKeyword == null || matchConversationKeyword(item, normalizedKeyword))
                .skip(userId != null && userId > 0 ? offset : 0)
                .limit(pageSize)
                .toList();
    }

    @Override
    public AdminConversationDetailDTO getConversationDetail(String conversationNo) {
        ConversationSessionDO session = conversationSessionMapper.findByConversationNo(requireText(conversationNo, "conversationNo"))
                .orElseThrow(() -> new NoSuchElementException("conversation not found: " + conversationNo));

        List<ConversationMemberDO> members = safeList(conversationMemberMapper.selectList(
                new QueryWrapper<ConversationMemberDO>()
                        .eq("conversation_no", session.getConversationNo())
                        .orderByAsc("id")
        ));
        List<MessageRecordDO> recentMessages = safeList(messageRecordMapper.listByConversation(session.getConversationNo(), null, 20));

        Set<Long> userIds = new LinkedHashSet<>();
        members.forEach(item -> {
            if (item.getUserId() != null && item.getUserId() > 0) {
                userIds.add(item.getUserId());
            }
            if (item.getPeerUserId() != null && item.getPeerUserId() > 0) {
                userIds.add(item.getPeerUserId());
            }
        });
        recentMessages.forEach(item -> {
            if (item.getSenderUserId() != null && item.getSenderUserId() > 0) {
                userIds.add(item.getSenderUserId());
            }
            if (item.getReceiverUserId() != null && item.getReceiverUserId() > 0) {
                userIds.add(item.getReceiverUserId());
            }
        });
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(userIds);

        List<AdminConversationMemberRowDTO> memberRows = members.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    UserDigest peerDigest = userDigestMap.get(item.getPeerUserId());
                    return new AdminConversationMemberRowDTO(
                            item.getConversationNo(),
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            userDigest == null ? null : userDigest.aipayUid(),
                            item.getPeerUserId(),
                            peerDigest == null ? null : peerDigest.displayName(),
                            peerDigest == null ? null : peerDigest.aipayUid(),
                            item.getUnreadCount(),
                            item.getLastReadMessageId(),
                            item.getLastReadAt(),
                            item.getMuteFlag(),
                            item.getPinFlag(),
                            item.getUpdatedAt()
                    );
                })
                .toList();

        List<AdminMessageRowDTO> messageRows = recentMessages.stream()
                .map(item -> toMessageRow(item, userDigestMap))
                .toList();

        return new AdminConversationDetailDTO(
                new AdminConversationRowDTO(
                        session.getConversationNo(),
                        session.getConversationType(),
                        session.getBizKey(),
                        session.getLastMessagePreview(),
                        session.getLastMessageAt(),
                        Long.valueOf(members.size()),
                        null,
                        null,
                        null,
                        null
                ),
                memberRows,
                messageRows
        );
    }

    @Override
    public List<AdminMessageRowDTO> listMessages(String conversationNo,
                                                 String messageType,
                                                 Long senderUserId,
                                                 Long receiverUserId,
                                                 int pageNo,
                                                 int pageSize) {
        QueryWrapper<MessageRecordDO> wrapper = new QueryWrapper<>();
        if (conversationNo != null) {
            wrapper.eq("conversation_no", conversationNo);
        }
        if (messageType != null) {
            wrapper.eq("message_type", messageType);
        }
        if (senderUserId != null && senderUserId > 0) {
            wrapper.eq("sender_user_id", senderUserId);
        }
        if (receiverUserId != null && receiverUserId > 0) {
            wrapper.eq("receiver_user_id", receiverUserId);
        }
        wrapper.orderByDesc("id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<MessageRecordDO> records = safeList(messageRecordMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(collectUserIds(records, MessageRecordDO::getSenderUserId, MessageRecordDO::getReceiverUserId));
        return records.stream()
                .map(item -> toMessageRow(item, userDigestMap))
                .toList();
    }

    @Override
    public List<AdminRedPacketRowDTO> listRedPackets(String redPacketNo,
                                                     Long senderUserId,
                                                     Long receiverUserId,
                                                     String status,
                                                     int pageNo,
                                                     int pageSize) {
        QueryWrapper<RedPacketOrderDO> wrapper = new QueryWrapper<>();
        if (redPacketNo != null) {
            wrapper.like("red_packet_no", redPacketNo);
        }
        if (senderUserId != null && senderUserId > 0) {
            wrapper.eq("sender_user_id", senderUserId);
        }
        if (receiverUserId != null && receiverUserId > 0) {
            wrapper.eq("receiver_user_id", receiverUserId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<RedPacketOrderDO> orders = safeList(redPacketOrderMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(collectUserIds(orders, RedPacketOrderDO::getSenderUserId, RedPacketOrderDO::getReceiverUserId));
        return orders.stream()
                .map(item -> {
                    UserDigest sender = userDigestMap.get(item.getSenderUserId());
                    UserDigest receiver = userDigestMap.get(item.getReceiverUserId());
                    return new AdminRedPacketRowDTO(
                            item.getRedPacketNo(),
                            item.getConversationNo(),
                            item.getMessageId(),
                            item.getSenderUserId(),
                            sender == null ? null : sender.displayName(),
                            item.getReceiverUserId(),
                            receiver == null ? null : receiver.displayName(),
                            item.getAmount(),
                            item.getAmount() == null ? "CNY" : item.getAmount().getCurrencyUnit().getCode(),
                            item.getPaymentMethod(),
                            item.getStatus(),
                            item.getCoverTitle(),
                            item.getBlessingText(),
                            item.getFundingTradeNo(),
                            item.getClaimTradeNo(),
                            item.getCreatedAt(),
                            item.getClaimedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<AdminContactRequestRowDTO> listContactRequests(String requestNo,
                                                               Long requesterUserId,
                                                               Long targetUserId,
                                                               String status,
                                                               int pageNo,
                                                               int pageSize) {
        QueryWrapper<ContactRequestDO> wrapper = new QueryWrapper<>();
        if (requestNo != null) {
            wrapper.like("request_no", requestNo);
        }
        if (requesterUserId != null && requesterUserId > 0) {
            wrapper.eq("requester_user_id", requesterUserId);
        }
        if (targetUserId != null && targetUserId > 0) {
            wrapper.eq("target_user_id", targetUserId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<ContactRequestDO> requests = safeList(contactRequestMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(collectUserIds(requests, ContactRequestDO::getRequesterUserId, ContactRequestDO::getTargetUserId));
        return requests.stream()
                .map(item -> {
                    UserDigest requester = userDigestMap.get(item.getRequesterUserId());
                    UserDigest target = userDigestMap.get(item.getTargetUserId());
                    return new AdminContactRequestRowDTO(
                            item.getRequestNo(),
                            item.getRequesterUserId(),
                            requester == null ? null : requester.displayName(),
                            item.getTargetUserId(),
                            target == null ? null : target.displayName(),
                            item.getApplyMessage(),
                            item.getStatus(),
                            item.getHandledByUserId(),
                            item.getHandledAt(),
                            item.getCreatedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<AdminContactFriendshipRowDTO> listFriendships(Long ownerUserId, Long friendUserId, int pageNo, int pageSize) {
        QueryWrapper<ContactFriendshipDO> wrapper = new QueryWrapper<>();
        if (ownerUserId != null && ownerUserId > 0) {
            wrapper.eq("owner_user_id", ownerUserId);
        }
        if (friendUserId != null && friendUserId > 0) {
            wrapper.eq("friend_user_id", friendUserId);
        }
        wrapper.orderByDesc("id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<ContactFriendshipDO> rows = safeList(contactFriendshipMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(collectUserIds(rows, ContactFriendshipDO::getOwnerUserId, ContactFriendshipDO::getFriendUserId));
        return rows.stream()
                .map(item -> {
                    UserDigest owner = userDigestMap.get(item.getOwnerUserId());
                    UserDigest friend = userDigestMap.get(item.getFriendUserId());
                    return new AdminContactFriendshipRowDTO(
                            item.getOwnerUserId(),
                            owner == null ? null : owner.displayName(),
                            item.getFriendUserId(),
                            friend == null ? null : friend.displayName(),
                            item.getRemark(),
                            item.getSourceRequestNo(),
                            item.getCreatedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<AdminContactBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, int pageNo, int pageSize) {
        QueryWrapper<ContactBlacklistDO> wrapper = new QueryWrapper<>();
        if (ownerUserId != null && ownerUserId > 0) {
            wrapper.eq("owner_user_id", ownerUserId);
        }
        if (blockedUserId != null && blockedUserId > 0) {
            wrapper.eq("blocked_user_id", blockedUserId);
        }
        wrapper.orderByDesc("id");
        wrapper.last(buildPageClause(pageNo, pageSize));
        List<ContactBlacklistDO> rows = safeList(contactBlacklistMapper.selectList(wrapper));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(collectUserIds(rows, ContactBlacklistDO::getOwnerUserId, ContactBlacklistDO::getBlockedUserId));
        return rows.stream()
                .map(item -> {
                    UserDigest owner = userDigestMap.get(item.getOwnerUserId());
                    UserDigest blocked = userDigestMap.get(item.getBlockedUserId());
                    return new AdminContactBlacklistRowDTO(
                            item.getOwnerUserId(),
                            owner == null ? null : owner.displayName(),
                            item.getBlockedUserId(),
                            blocked == null ? null : blocked.displayName(),
                            item.getReason(),
                            item.getCreatedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    private AdminMessageRowDTO toMessageRow(MessageRecordDO item, Map<Long, UserDigest> userDigestMap) {
        UserDigest sender = userDigestMap.get(item.getSenderUserId());
        UserDigest receiver = userDigestMap.get(item.getReceiverUserId());
        return new AdminMessageRowDTO(
                item.getMessageId(),
                item.getConversationNo(),
                item.getSenderUserId(),
                sender == null ? null : sender.displayName(),
                item.getReceiverUserId(),
                receiver == null ? null : receiver.displayName(),
                item.getMessageType(),
                item.getContentText(),
                item.getMediaId(),
                item.getAmount(),
                item.getTradeOrderNo(),
                item.getMessageStatus(),
                item.getExtPayload(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private boolean matchConversationKeyword(AdminConversationRowDTO item, String keyword) {
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return containsKeyword(item.conversationNo(), normalized)
                || containsKeyword(item.bizKey(), normalized)
                || containsKeyword(item.lastMessagePreview(), normalized)
                || containsKeyword(item.peerDisplayName(), normalized)
                || containsKeyword(item.peerAipayUid(), normalized);
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Long resolvePeerUserId(List<ConversationMemberDO> members, ConversationMemberDO selectedMember) {
        if (selectedMember != null && selectedMember.getPeerUserId() != null && selectedMember.getPeerUserId() > 0) {
            return selectedMember.getPeerUserId();
        }
        if (members == null || members.isEmpty()) {
            return null;
        }
        return members.stream()
                .map(ConversationMemberDO::getPeerUserId)
                .filter(item -> item != null && item > 0)
                .findFirst()
                .orElseGet(() -> members.stream()
                        .map(ConversationMemberDO::getUserId)
                        .filter(item -> item != null && item > 0)
                        .findFirst()
                        .orElse(null));
    }

    private <T> Set<Long> collectUserIds(List<T> rows, Function<T, Long> firstExtractor, Function<T, Long> secondExtractor) {
        Set<Long> userIds = new LinkedHashSet<>();
        rows.forEach(item -> {
            Long first = firstExtractor.apply(item);
            Long second = secondExtractor.apply(item);
            if (first != null && first > 0) {
                userIds.add(first);
            }
            if (second != null && second > 0) {
                userIds.add(second);
            }
        });
        return userIds;
    }

    private Map<Long, UserDigest> loadUserDigestMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserAccountDO> accountMap = safeList(userAccountMapper.findByUserIds(userIds)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (left, right) -> left));
        Map<Long, UserProfileDO> profileMap = safeList(userProfileMapper.findByUserIds(userIds)).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (left, right) -> left));

        Map<Long, UserDigest> result = new LinkedHashMap<>();
        userIds.forEach(userId -> {
            UserAccountDO account = accountMap.get(userId);
            UserProfileDO profile = profileMap.get(userId);
            String displayName = profile != null && hasText(profile.getNickname())
                    ? profile.getNickname()
                    : account != null && hasText(account.getAipayUid())
                    ? account.getAipayUid()
                    : userId == null
                    ? null
                    : String.valueOf(userId);
            result.put(userId, new UserDigest(
                    userId,
                    displayName,
                    account == null ? null : account.getAipayUid(),
                    account == null ? null : account.getLoginId(),
                    profile == null ? null : profile.getMobile()
            ));
        });
        return result;
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String requireText(String value, String label) {
        String normalized = (value == null ? "" : value).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return normalized;
    }

    private String normalizeKeyword(String raw) {
        String normalized = (raw == null ? "" : raw).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int calculateOffset(int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(1, pageNo);
        int normalizedPageSize = Math.max(1, pageSize);
        return (normalizedPageNo - 1) * normalizedPageSize;
    }

    private String buildPageClause(int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(1, pageNo);
        int normalizedPageSize = Math.max(1, pageSize);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        return "LIMIT " + normalizedPageSize + " OFFSET " + offset;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private record UserDigest(
            Long userId,
            String displayName,
            String aipayUid,
            String loginId,
            String mobile
    ) {
    }
}
