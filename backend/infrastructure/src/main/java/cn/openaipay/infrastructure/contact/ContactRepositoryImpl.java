package cn.openaipay.infrastructure.contact;

import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import cn.openaipay.domain.contact.model.ContactRequestStatus;
import cn.openaipay.domain.contact.model.ContactSearchProfile;
import cn.openaipay.domain.contact.repository.ContactRepository;
import cn.openaipay.infrastructure.contact.dataobject.ContactBlacklistDO;
import cn.openaipay.infrastructure.contact.dataobject.ContactFriendshipDO;
import cn.openaipay.infrastructure.contact.dataobject.ContactRequestDO;
import cn.openaipay.infrastructure.contact.mapper.ContactBlacklistMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactFriendshipMapper;
import cn.openaipay.infrastructure.contact.mapper.ContactRequestMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.dataobject.UserPrivacySettingDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.user.mapper.UserPrivacySettingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 联系人仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class ContactRepositoryImpl implements ContactRepository {

    /** 联系人请求信息 */
    private final ContactRequestMapper contactRequestMapper;
    /** 联系人信息 */
    private final ContactFriendshipMapper contactFriendshipMapper;
    /** 联系人信息 */
    private final ContactBlacklistMapper contactBlacklistMapper;
    /** 用户信息 */
    private final UserAccountMapper userAccountMapper;
    /** 用户资料信息 */
    private final UserProfileMapper userProfileMapper;
    /** 用户配置信息 */
    private final UserPrivacySettingMapper userPrivacySettingMapper;

    public ContactRepositoryImpl(ContactRequestMapper contactRequestMapper,
                                 ContactFriendshipMapper contactFriendshipMapper,
                                 ContactBlacklistMapper contactBlacklistMapper,
                                 UserAccountMapper userAccountMapper,
                                 UserProfileMapper userProfileMapper,
                                 UserPrivacySettingMapper userPrivacySettingMapper) {
        this.contactRequestMapper = contactRequestMapper;
        this.contactFriendshipMapper = contactFriendshipMapper;
        this.contactBlacklistMapper = contactBlacklistMapper;
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.userPrivacySettingMapper = userPrivacySettingMapper;
    }

    /**
     * 按请求单号查找请求。
     */
    @Override
    public Optional<ContactRequest> findRequestByRequestNo(String requestNo) {
        return contactRequestMapper.findByRequestNo(requestNo).map(this::toDomainRequest);
    }

    /**
     * 查找请求。
     */
    @Override
    public Optional<ContactRequest> findPendingRequest(Long requesterUserId, Long targetUserId) {
        return contactRequestMapper.findPendingRequest(requesterUserId, targetUserId).map(this::toDomainRequest);
    }

    /**
     * 保存请求。
     */
    @Override
    @Transactional
    public ContactRequest saveRequest(ContactRequest request) {
        ContactRequestDO entity = contactRequestMapper.findByRequestNo(request.getRequestNo())
                .orElse(new ContactRequestDO());
        fillRequestDO(entity, request);
        return toDomainRequest(contactRequestMapper.save(entity));
    }

    /**
     * 查询请求列表。
     */
    @Override
    public List<ContactRequest> listReceivedRequests(Long targetUserId, int limit) {
        return contactRequestMapper.listReceived(targetUserId, limit)
                .stream()
                .map(this::toDomainRequest)
                .toList();
    }

    /**
     * 查询请求列表。
     */
    @Override
    public List<ContactRequest> listSentRequests(Long requesterUserId, int limit) {
        return contactRequestMapper.listSent(requesterUserId, limit)
                .stream()
                .map(this::toDomainRequest)
                .toList();
    }

    /**
     * 查找业务数据。
     */
    @Override
    public Optional<ContactFriendship> findFriendship(Long ownerUserId, Long friendUserId) {
        return contactFriendshipMapper.findByOwnerAndFriend(ownerUserId, friendUserId).map(this::toDomainFriendship);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public ContactFriendship saveFriendship(ContactFriendship friendship) {
        ContactFriendshipDO entity = contactFriendshipMapper
                .findByOwnerAndFriend(friendship.getOwnerUserId(), friendship.getFriendUserId())
                .orElse(new ContactFriendshipDO());
        fillFriendshipDO(entity, friendship);
        return toDomainFriendship(contactFriendshipMapper.save(entity));
    }

    /**
     * 删除业务数据。
     */
    @Override
    @Transactional
    public void deleteFriendship(Long ownerUserId, Long friendUserId) {
        contactFriendshipMapper.deleteByOwnerAndFriend(ownerUserId, friendUserId);
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    public List<ContactFriendship> listFriendships(Long ownerUserId, int limit) {
        return contactFriendshipMapper.findByOwnerUserId(ownerUserId, limit)
                .stream()
                .map(this::toDomainFriendship)
                .toList();
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    public boolean isBlocked(Long ownerUserId, Long blockedUserId) {
        return contactBlacklistMapper.findByOwnerAndBlocked(ownerUserId, blockedUserId).isPresent();
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public void block(Long ownerUserId, Long blockedUserId, String reason) {
        ContactBlacklistDO entity = contactBlacklistMapper.findByOwnerAndBlocked(ownerUserId, blockedUserId)
                .orElse(new ContactBlacklistDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setOwnerUserId(ownerUserId);
        entity.setBlockedUserId(blockedUserId);
        entity.setReason(reason);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        contactBlacklistMapper.save(entity);
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public void unblock(Long ownerUserId, Long blockedUserId) {
        contactBlacklistMapper.deleteByOwnerAndBlocked(ownerUserId, blockedUserId);
    }

    /**
     * 按用户ID查找资料信息。
     */
    @Override
    public List<ContactSearchProfile> findProfilesByUserIds(Long ownerUserId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return buildProfiles(ownerUserId, userIds);
    }

    /**
     * 处理搜索资料信息。
     */
    @Override
    public List<ContactSearchProfile> searchProfiles(Long ownerUserId, String keyword, int limit) {
        String rawKeyword = keyword == null ? "" : keyword.trim();
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null || rawKeyword.isEmpty()) {
            return List.of();
        }

        int safeLimit = Math.max(1, limit);
        int candidateLimit = Math.max(60, safeLimit * 8);
        boolean digitsOnlyKeyword = rawKeyword.matches("[\\d\\s\\-+()]+");

        List<ContactFriendshipDO> friendships = contactFriendshipMapper.findAllByOwnerUserId(ownerUserId);
        Map<Long, ContactFriendshipDO> friendshipMap = friendships.stream()
                .filter(friendship -> friendship.getFriendUserId() != null)
                .collect(Collectors.toMap(
                        ContactFriendshipDO::getFriendUserId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        LinkedHashSet<Long> candidateUserIds = new LinkedHashSet<>();
        candidateUserIds.addAll(friendshipMap.keySet());
        candidateUserIds.addAll(searchUserIdsFromProfile(rawKeyword, digitsOnlyKeyword, candidateLimit));
        candidateUserIds.addAll(searchUserIdsFromAccount(rawKeyword, candidateLimit));
        candidateUserIds.remove(ownerUserId);
        if (candidateUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> orderedCandidateUserIds = new ArrayList<>(candidateUserIds);
        if (orderedCandidateUserIds.size() > candidateLimit) {
            orderedCandidateUserIds = orderedCandidateUserIds.subList(0, candidateLimit);
        }

        Map<Long, UserPrivacySettingDO> privacySettingMap = loadPrivacySettingMap(orderedCandidateUserIds);

        return buildProfiles(ownerUserId, orderedCandidateUserIds, friendshipMap).stream()
                .filter(profile -> {
                    if (!digitsOnlyKeyword || profile.friend()) {
                        return true;
                    }
                    UserPrivacySettingDO privacySetting = privacySettingMap.get(profile.userId());
                    return privacySetting == null || !Boolean.FALSE.equals(privacySetting.getAllowSearchByMobile());
                })
                .map(profile -> rankSearchProfile(profile, normalizedKeyword))
                .flatMap(Optional::stream)
                .sorted(Comparator
                        .comparingInt(SearchRankedProfile::score).reversed()
                        .thenComparing(candidate -> safeSortText(candidate.profile().remark()))
                        .thenComparing(candidate -> safeSortText(candidate.profile().nickname()))
                        .thenComparing(candidate -> candidate.profile().userId(), Comparator.nullsLast(Long::compareTo)))
                .limit(safeLimit)
                .map(SearchRankedProfile::profile)
                .toList();
    }

    private List<ContactSearchProfile> buildProfiles(Long ownerUserId, List<Long> userIds) {
        return buildProfiles(ownerUserId, userIds, Map.of());
    }

    private List<ContactSearchProfile> buildProfiles(Long ownerUserId,
                                                     List<Long> userIds,
                                                     Map<Long, ContactFriendshipDO> friendshipMap) {
        Map<Long, ContactFriendshipDO> resolvedFriendshipMap = friendshipMap == null
                ? Map.of()
                : friendshipMap;
        if (resolvedFriendshipMap.isEmpty()) {
            resolvedFriendshipMap = contactFriendshipMapper.findByOwnerAndFriendUserIds(ownerUserId, userIds)
                    .stream()
                    .filter(entity -> entity.getFriendUserId() != null)
                    .collect(Collectors.toMap(
                            ContactFriendshipDO::getFriendUserId,
                            Function.identity(),
                            (first, ignored) -> first
                    ));
        }

        Set<Long> blockedUserIds = contactBlacklistMapper.findByOwnerAndBlockedUserIds(ownerUserId, userIds)
                .stream()
                .map(ContactBlacklistDO::getBlockedUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserAccountDO> accountMap = userAccountMapper.findByUserIds(userIds)
                .stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (first, ignored) -> first));
        Map<Long, UserProfileDO> profileMap = userProfileMapper.findByUserIds(userIds)
                .stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (first, ignored) -> first));

        List<ContactSearchProfile> results = new ArrayList<>();
        for (Long userId : userIds) {
            UserAccountDO account = accountMap.get(userId);
            if (account == null || account.getUserId() == null) {
                continue;
            }
            UserProfileDO profile = profileMap.get(userId);
            ContactFriendshipDO friendship = resolvedFriendshipMap.get(userId);
            boolean blocked = blockedUserIds.contains(userId);
            String resolvedMobile = resolveProfileMobile(profile, account);

            results.add(new ContactSearchProfile(
                    userId,
                    account.getAipayUid(),
                    profile == null ? null : profile.getNickname(),
                    profile == null ? null : profile.getAvatarUrl(),
                    resolvedMobile,
                    profile == null ? null : profile.getMaskedRealName(),
                    friendship != null,
                    blocked,
                    friendship == null ? null : friendship.getRemark()
            ));
        }
        return results;
    }

    private String resolveProfileMobile(UserProfileDO profile, UserAccountDO account) {
        String profileMobile = normalizeProfileText(profile == null ? null : profile.getMobile());
        if (profileMobile != null) {
            return profileMobile;
        }
        String loginId = normalizeProfileText(account == null ? null : account.getLoginId());
        if (loginId == null || !loginId.chars().allMatch(Character::isDigit) || loginId.length() < 7) {
            return null;
        }
        return loginId;
    }

    private String normalizeProfileText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Optional<SearchRankedProfile> rankSearchProfile(ContactSearchProfile profile, String normalizedKeyword) {
        int score = -1;

        score = Math.max(score, scoreMatch(profile.mobile(), normalizedKeyword, 1000, 940, 860, true));
        score = Math.max(score, scoreMatch(profile.aipayUid(), normalizedKeyword, 990, 930, 840, true));
        score = Math.max(score, scoreMatch(profile.remark(), normalizedKeyword, 980, 920, 820, false));
        score = Math.max(score, scoreMatch(profile.nickname(), normalizedKeyword, 960, 900, 800, false));
        score = Math.max(score, scoreMatch(profile.maskedRealName(), normalizedKeyword, 940, 880, 780, false));

        if (score < 0) {
            return Optional.empty();
        }
        return Optional.of(new SearchRankedProfile(profile, score));
    }

    private int scoreMatch(String candidate,
                           String normalizedKeyword,
                           int exactScore,
                           int prefixScore,
                           int containsScore,
                           boolean digitsOnly) {
        String normalizedCandidate = normalizeSearchSource(candidate, digitsOnly);
        if (normalizedCandidate == null || normalizedCandidate.isEmpty()) {
            return -1;
        }
        if (Objects.equals(normalizedCandidate, normalizedKeyword)) {
            return exactScore;
        }
        if (normalizedCandidate.startsWith(normalizedKeyword)) {
            return prefixScore;
        }
        if (normalizedCandidate.contains(normalizedKeyword)) {
            return containsScore;
        }
        return -1;
    }

    private String normalizeKeyword(String raw) {
        boolean digitsOnly = raw != null && raw.trim().matches("[\\d\\s\\-+()]+");
        String normalized = normalizeSearchSource(raw, digitsOnly);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSearchSource(String raw, boolean digitsOnly) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = digitsOnly
                ? trimmed.replaceAll("\\D+", "")
                : trimmed.replaceAll("[\\s\\-_]+", "").toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String safeSortText(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private List<Long> searchUserIdsFromProfile(String rawKeyword, boolean digitsOnly, int limit) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return List.of();
        }
        QueryWrapper<UserProfileDO> wrapper = new QueryWrapper<>();
        wrapper.select("user_id");
        if (digitsOnly) {
            String normalizedDigits = rawKeyword.replaceAll("\\D+", "");
            if (normalizedDigits.isEmpty()) {
                return List.of();
            }
            wrapper.like("mobile", normalizedDigits);
        } else {
            wrapper.and(q -> q.like("nickname", rawKeyword).or().like("masked_real_name", rawKeyword));
        }
        wrapper.orderByDesc("updated_at");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return userProfileMapper.selectList(wrapper).stream()
                .map(UserProfileDO::getUserId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> searchUserIdsFromAccount(String rawKeyword, int limit) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return List.of();
        }
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        wrapper.select("user_id");
        wrapper.and(q -> q.like("login_id", rawKeyword).or().like("aipay_uid", rawKeyword));
        wrapper.orderByDesc("updated_at");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return userAccountMapper.selectList(wrapper).stream()
                .map(UserAccountDO::getUserId)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, UserPrivacySettingDO> loadPrivacySettingMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<UserPrivacySettingDO> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        return userPrivacySettingMapper.selectList(wrapper).stream()
                .filter(privacy -> privacy.getUserId() != null)
                .collect(Collectors.toMap(UserPrivacySettingDO::getUserId, Function.identity(), (first, ignored) -> first));
    }

    private record SearchRankedProfile(
        /** 资料信息 */
        ContactSearchProfile profile,
        /** score信息 */
        int score
    ) {
    }

    private ContactRequest toDomainRequest(ContactRequestDO entity) {
        return new ContactRequest(
                entity.getId(),
                entity.getRequestNo(),
                entity.getRequesterUserId(),
                entity.getTargetUserId(),
                entity.getApplyMessage(),
                ContactRequestStatus.from(entity.getStatus()),
                entity.getHandledByUserId(),
                entity.getHandledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ContactFriendship toDomainFriendship(ContactFriendshipDO entity) {
        return new ContactFriendship(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getFriendUserId(),
                entity.getRemark(),
                entity.getSourceRequestNo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillRequestDO(ContactRequestDO entity, ContactRequest request) {
        LocalDateTime now = LocalDateTime.now();
        entity.setRequestNo(request.getRequestNo());
        entity.setRequesterUserId(request.getRequesterUserId());
        entity.setTargetUserId(request.getTargetUserId());
        entity.setApplyMessage(request.getApplyMessage());
        entity.setStatus(request.getStatus().name());
        entity.setHandledByUserId(request.getHandledByUserId());
        entity.setHandledAt(request.getHandledAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(request.getCreatedAt() == null ? now : request.getCreatedAt());
        }
        entity.setUpdatedAt(request.getUpdatedAt() == null ? now : request.getUpdatedAt());
    }

    private void fillFriendshipDO(ContactFriendshipDO entity, ContactFriendship friendship) {
        LocalDateTime now = LocalDateTime.now();
        entity.setOwnerUserId(friendship.getOwnerUserId());
        entity.setFriendUserId(friendship.getFriendUserId());
        entity.setRemark(friendship.getRemark());
        entity.setSourceRequestNo(friendship.getSourceRequestNo());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(friendship.getCreatedAt() == null ? now : friendship.getCreatedAt());
        }
        entity.setUpdatedAt(friendship.getUpdatedAt() == null ? now : friendship.getUpdatedAt());
    }
}
