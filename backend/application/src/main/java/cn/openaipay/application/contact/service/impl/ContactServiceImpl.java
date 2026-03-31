package cn.openaipay.application.contact.service.impl;

import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.BlockContactCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.contact.command.UpdateContactRemarkCommand;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.contact.dto.ContactFriendDTO;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.dto.ContactSearchDTO;
import cn.openaipay.application.contact.service.ContactService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import cn.openaipay.domain.contact.model.ContactRequestStatus;
import cn.openaipay.domain.contact.model.ContactSearchProfile;
import cn.openaipay.domain.contact.repository.ContactRepository;
import cn.openaipay.domain.contact.service.ContactDomainService;
import cn.openaipay.domain.contact.service.ContactRequestHandlePlan;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.MessageType;
import cn.openaipay.domain.message.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 联系人应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class ContactServiceImpl implements ContactService {

    /** 默认信息 */
    private static final int DEFAULT_LIMIT = 20;
    /** 最大信息 */
    private static final int MAX_LIMIT = 100;
    /** 好友拒绝通知消息 ID 业务类型编码。 */
    private static final String FRIEND_REJECTED_MESSAGE_BIZ_TYPE = "90";
    /** 好友拒绝通知文案。 */
    private static final String FRIEND_REJECTED_NOTICE_TEXT = "对方拒绝了你的好友申请";

    /** 联系人信息 */
    private final ContactRepository contactRepository;
    /** 联系人域信息 */
    private final ContactDomainService contactDomainService;
    /** 会话门面。 */
    private final ConversationFacade conversationFacade;
    /** 消息仓储。 */
    private final MessageRepository messageRepository;
    /** 分布式 ID 生成器。 */
    private final AiPayIdGenerator aiPayIdGenerator;

    public ContactServiceImpl(ContactRepository contactRepository,
                              ContactDomainService contactDomainService,
                              ConversationFacade conversationFacade,
                              MessageRepository messageRepository,
                              AiPayIdGenerator aiPayIdGenerator) {
        this.contactRepository = contactRepository;
        this.contactDomainService = contactDomainService;
        this.conversationFacade = conversationFacade;
        this.messageRepository = messageRepository;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 应用请求。
     */
    @Override
    @Transactional
    public ContactRequestDTO applyFriendRequest(ApplyFriendRequestCommand command) {
        Long requesterUserId = requirePositive(command.requesterUserId(), "requesterUserId");
        Long targetUserId = requirePositive(command.targetUserId(), "targetUserId");
        if (requesterUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("requesterUserId and targetUserId must be different");
        }
        boolean blockedByEither = contactRepository.isBlocked(requesterUserId, targetUserId)
                || contactRepository.isBlocked(targetUserId, requesterUserId);
        boolean alreadyFriend = contactRepository.findFriendship(requesterUserId, targetUserId).isPresent();
        ContactRequest pending = contactRepository.findPendingRequest(requesterUserId, targetUserId).orElse(null);
        ContactRequest request = contactDomainService.prepareFriendRequest(
                requesterUserId,
                targetUserId,
                blockedByEither,
                alreadyFriend,
                pending,
                command.applyMessage(),
                LocalDateTime.now()
        );
        if (pending != null) {
            return toRequestDTO(refreshPendingRequestApplyMessageIfNeeded(request, command.applyMessage()));
        }
        return toRequestDTO(contactRepository.saveRequest(request));
    }

    /**
     * 处理请求。
     */
    @Override
    @Transactional
    public ContactRequestDTO handleFriendRequest(HandleFriendRequestCommand command) {
        Long operatorUserId = requirePositive(command.operatorUserId(), "operatorUserId");
        String requestNo = normalizeRequired(command.requestNo(), "requestNo");
        String action = normalizeRequired(command.action(), "action");

        ContactRequest request = contactRepository.findRequestByRequestNo(requestNo)
                .orElseThrow(() -> new NoSuchElementException("contact request not found: " + requestNo));
        LocalDateTime now = LocalDateTime.now();
        ContactRequestHandlePlan handlePlan = contactDomainService.handleRequest(request, action, operatorUserId, now);
        contactRepository.saveRequest(handlePlan.request());
        for (ContactFriendship friendship : handlePlan.friendshipsToCreate()) {
            if (contactRepository.findFriendship(friendship.getOwnerUserId(), friendship.getFriendUserId()).isPresent()) {
                continue;
            }
            contactRepository.saveFriendship(friendship);
        }
        if (handlePlan.request().getStatus() == ContactRequestStatus.REJECTED) {
            sendFriendRequestRejectedNotice(handlePlan.request());
        }
        return toRequestDTO(handlePlan.request());
    }

    /**
     * 查询请求列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContactRequestDTO> listReceivedRequests(Long targetUserId, Integer limit) {
        Long target = requirePositive(targetUserId, "targetUserId");
        List<ContactRequest> requests = contactRepository.listReceivedRequests(target, normalizeLimit(limit));
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requesterUserIds = requests.stream()
                .map(ContactRequest::getRequesterUserId)
                .distinct()
                .toList();
        Map<Long, ContactSearchProfile> requesterProfileMap = contactRepository.findProfilesByUserIds(target, requesterUserIds)
                .stream()
                .collect(Collectors.toMap(ContactSearchProfile::userId, profile -> profile, (first, ignored) -> first));
        return requests.stream()
                .map(request -> toRequestDTO(request, requesterProfileMap.get(request.getRequesterUserId())))
                .toList();
    }

    /**
     * 查询请求列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContactRequestDTO> listSentRequests(Long requesterUserId, Integer limit) {
        return contactRepository.listSentRequests(requirePositive(requesterUserId, "requesterUserId"), normalizeLimit(limit))
                .stream()
                .map(request -> toRequestDTO(request, null))
                .toList();
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContactFriendDTO> listFriends(Long ownerUserId, Integer limit) {
        Long owner = requirePositive(ownerUserId, "ownerUserId");
        List<ContactFriendship> friendships = contactRepository.listFriendships(owner, normalizeLimit(limit));
        if (friendships.isEmpty()) {
            return List.of();
        }
        List<Long> friendUserIds = friendships.stream().map(ContactFriendship::getFriendUserId).toList();
        Map<Long, ContactSearchProfile> profileMap = contactRepository.findProfilesByUserIds(owner, friendUserIds)
                .stream()
                .collect(Collectors.toMap(ContactSearchProfile::userId, profile -> profile, (first, ignored) -> first));

        return friendships.stream().map(friendship -> {
            ContactSearchProfile profile = profileMap.get(friendship.getFriendUserId());
            return new ContactFriendDTO(
                    friendship.getFriendUserId(),
                    profile == null ? null : profile.aipayUid(),
                    profile == null ? null : profile.nickname(),
                    profile == null ? null : profile.maskedRealName(),
                    profile == null ? null : maskMobile(profile.mobile()),
                    profile == null ? null : profile.avatarUrl(),
                    friendship.getRemark(),
                    friendship.getCreatedAt()
            );
        }).toList();
    }

    /**
     * 处理搜索联系人信息。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContactSearchDTO> searchContacts(Long ownerUserId, String keyword, Integer limit) {
        return contactRepository.searchProfiles(
                        requirePositive(ownerUserId, "ownerUserId"),
                        normalizeRequired(keyword, "keyword"),
                        normalizeLimit(limit)
                )
                .stream()
                .map(profile -> new ContactSearchDTO(
                        profile.userId(),
                        profile.aipayUid(),
                        profile.nickname(),
                        profile.avatarUrl(),
                        profile.mobile(),
                        profile.maskedRealName(),
                        profile.friend(),
                        profile.blocked(),
                        profile.remark()
                ))
                .toList();
    }

    /**
     * 更新业务数据。
     */
    @Override
    @Transactional
    public void updateRemark(UpdateContactRemarkCommand command) {
        Long ownerUserId = requirePositive(command.ownerUserId(), "ownerUserId");
        Long friendUserId = requirePositive(command.friendUserId(), "friendUserId");
        ContactFriendship friendship = contactRepository.findFriendship(ownerUserId, friendUserId)
                .orElseThrow(() -> new NoSuchElementException("friendship not found"));
        friendship.updateRemark(command.remark(), LocalDateTime.now());
        contactRepository.saveFriendship(friendship);
    }

    /**
     * 删除业务数据。
     */
    @Override
    @Transactional
    public void deleteFriend(Long ownerUserId, Long friendUserId) {
        Long owner = requirePositive(ownerUserId, "ownerUserId");
        Long friend = requirePositive(friendUserId, "friendUserId");
        contactRepository.deleteFriendship(owner, friend);
        contactRepository.deleteFriendship(friend, owner);
    }

    /**
     * 处理联系人信息。
     */
    @Override
    @Transactional
    public void blockContact(BlockContactCommand command) {
        Long owner = requirePositive(command.ownerUserId(), "ownerUserId");
        Long blocked = requirePositive(command.blockedUserId(), "blockedUserId");
        if (owner.equals(blocked)) {
            throw new IllegalArgumentException("ownerUserId and blockedUserId must be different");
        }
        contactRepository.block(owner, blocked, command.reason());
        contactRepository.deleteFriendship(owner, blocked);
        contactRepository.deleteFriendship(blocked, owner);

        LocalDateTime now = LocalDateTime.now();
        contactRepository.findPendingRequest(owner, blocked).ifPresent(request -> {
            contactDomainService.cancelPendingRequestForBlock(request, owner, now);
            contactRepository.saveRequest(request);
        });
        contactRepository.findPendingRequest(blocked, owner).ifPresent(request -> {
            contactDomainService.cancelPendingRequestForBlock(request, owner, now);
            contactRepository.saveRequest(request);
        });
    }

    /**
     * 处理联系人信息。
     */
    @Override
    @Transactional
    public void unblockContact(Long ownerUserId, Long blockedUserId) {
        contactRepository.unblock(
                requirePositive(ownerUserId, "ownerUserId"),
                requirePositive(blockedUserId, "blockedUserId")
        );
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFriend(Long ownerUserId, Long friendUserId) {
        return contactRepository.findFriendship(
                requirePositive(ownerUserId, "ownerUserId"),
                requirePositive(friendUserId, "friendUserId")
        ).isPresent();
    }

    private ContactRequestDTO toRequestDTO(ContactRequest request) {
        return toRequestDTO(request, null);
    }

    private ContactRequestDTO toRequestDTO(ContactRequest request, ContactSearchProfile requesterProfile) {
        return new ContactRequestDTO(
                request.getRequestNo(),
                request.getRequesterUserId(),
                request.getTargetUserId(),
                requesterProfile == null ? null : requesterProfile.nickname(),
                requesterProfile == null ? null : requesterProfile.maskedRealName(),
                requesterProfile == null ? null : maskMobile(requesterProfile.mobile()),
                requesterProfile == null ? null : requesterProfile.avatarUrl(),
                request.getApplyMessage(),
                request.getStatus().name(),
                request.getHandledByUserId(),
                request.getHandledAt(),
                request.getCreatedAt()
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

    private String maskMobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        String normalized = mobile.trim();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.contains("*")) {
            return normalized;
        }
        if (normalized.length() <= 7) {
            return normalized;
        }
        return normalized.substring(0, 3) + "****" + normalized.substring(normalized.length() - 4);
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private ContactRequest refreshPendingRequestApplyMessageIfNeeded(ContactRequest pendingRequest, String latestApplyMessage) {
        String normalizedLatestApplyMessage = normalizeOptional(latestApplyMessage);
        if (normalizedLatestApplyMessage == null
                || Objects.equals(normalizedLatestApplyMessage, pendingRequest.getApplyMessage())) {
            return pendingRequest;
        }
        ContactRequest refreshedPendingRequest = new ContactRequest(
                pendingRequest.getId(),
                pendingRequest.getRequestNo(),
                pendingRequest.getRequesterUserId(),
                pendingRequest.getTargetUserId(),
                normalizedLatestApplyMessage,
                pendingRequest.getStatus(),
                pendingRequest.getHandledByUserId(),
                pendingRequest.getHandledAt(),
                pendingRequest.getCreatedAt(),
                LocalDateTime.now()
        );
        return contactRepository.saveRequest(refreshedPendingRequest);
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void sendFriendRequestRejectedNotice(ContactRequest rejectedRequest) {
        Long senderUserId = requirePositive(rejectedRequest.getTargetUserId(), "targetUserId");
        Long receiverUserId = requirePositive(rejectedRequest.getRequesterUserId(), "requesterUserId");
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
                MessageType.SYSTEM,
                FRIEND_REJECTED_NOTICE_TEXT,
                null,
                null,
                null,
                null,
                now
        );
        message = messageRepository.save(message);
        conversationFacade.appendMessage(
                message.getConversationNo(),
                senderUserId,
                receiverUserId,
                message.getMessageId(),
                FRIEND_REJECTED_NOTICE_TEXT,
                now
        );
    }

    private String buildMessageId(Long senderUserId) {
        return "MSG" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_TRADE,
                FRIEND_REJECTED_MESSAGE_BIZ_TYPE,
                String.valueOf(requirePositive(senderUserId, "senderUserId"))
        );
    }
}
