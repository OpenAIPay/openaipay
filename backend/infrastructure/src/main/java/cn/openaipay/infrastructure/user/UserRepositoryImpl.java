package cn.openaipay.infrastructure.user;

import cn.openaipay.domain.user.model.KycLevel;
import cn.openaipay.domain.user.model.UserAccount;
import cn.openaipay.domain.user.model.UserAccountSource;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserFeatureCode;
import cn.openaipay.domain.user.model.UserPrivacySetting;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.model.UserRecentContact;
import cn.openaipay.domain.user.model.UserSecuritySetting;
import cn.openaipay.domain.user.model.UserStatus;
import cn.openaipay.domain.user.model.UserAvatarCatalog;
import cn.openaipay.domain.user.repository.UserRepository;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserFeatureStatusDO;
import cn.openaipay.infrastructure.user.dataobject.UserPrivacySettingDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.dataobject.UserRecentContactDO;
import cn.openaipay.infrastructure.user.dataobject.UserSecuritySettingDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserFeatureStatusMapper;
import cn.openaipay.infrastructure.user.mapper.UserPrivacySettingMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.user.mapper.UserRecentContactMapper;
import cn.openaipay.infrastructure.user.mapper.UserSecuritySettingMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    /** User账户Persistence组件 */
    private final UserAccountMapper accountRepository;
    /** User资料Persistence组件 */
    private final UserProfileMapper profileRepository;
    /** User安全设置Persistence组件 */
    private final UserSecuritySettingMapper securityRepository;
    /** User隐私设置Persistence组件 */
    private final UserPrivacySettingMapper privacyRepository;
    /** User最近联系人Persistence组件 */
    private final UserRecentContactMapper recentContactRepository;
    /** 用户能力开通Persistence组件 */
    private final UserFeatureStatusMapper userFeatureStatusMapper;

    public UserRepositoryImpl(
            UserAccountMapper accountRepository,
            UserProfileMapper profileRepository,
            UserSecuritySettingMapper securityRepository,
            UserPrivacySettingMapper privacyRepository,
            UserRecentContactMapper recentContactRepository,
            UserFeatureStatusMapper userFeatureStatusMapper) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.securityRepository = securityRepository;
        this.privacyRepository = privacyRepository;
        this.recentContactRepository = recentContactRepository;
        this.userFeatureStatusMapper = userFeatureStatusMapper;
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public Optional<UserAggregate> findByUserId(Long userId) {
        Optional<UserAccountDO> accountEntityOpt = accountRepository.findByUserId(userId);
        if (accountEntityOpt.isEmpty()) {
            return Optional.empty();
        }

        UserAccountDO accountDO = accountEntityOpt.get();
        UserProfileDO profileDO = profileRepository.findByUserId(userId)
                .orElseGet(() -> defaultProfileDO(userId, accountDO.getCreatedAt()));
        UserSecuritySettingDO securityDO = securityRepository.findByUserId(userId)
                .orElseGet(() -> defaultSecurityDO(userId, accountDO.getCreatedAt()));
        UserPrivacySettingDO privacyDO = privacyRepository.findByUserId(userId)
                .orElseGet(() -> defaultPrivacyDO(userId, accountDO.getCreatedAt()));

        return Optional.of(new UserAggregate(
                toDomain(accountDO),
                toDomain(profileDO),
                toDomain(securityDO),
                toDomain(privacyDO)
        ));
    }

    /**
     * 按用户ID查找资料信息。
     */
    @Override
    public List<UserAggregate> findProfilesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<Long> normalizedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedUserIds.isEmpty()) {
            return List.of();
        }

        Map<Long, UserAccountDO> accountMap = accountRepository.findByUserIds(normalizedUserIds).stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (first, ignored) -> first, LinkedHashMap::new));
        Map<Long, UserProfileDO> profileMap = profileRepository.findByUserIds(normalizedUserIds).stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (first, ignored) -> first, LinkedHashMap::new));

        List<UserAggregate> aggregates = new ArrayList<>();
        for (Long userId : normalizedUserIds) {
            UserAccountDO accountDO = accountMap.get(userId);
            if (accountDO == null) {
                continue;
            }
            LocalDateTime createdAt = accountDO.getCreatedAt();
            UserProfileDO profileDO = profileMap.getOrDefault(userId, defaultProfileDO(userId, createdAt));
            aggregates.add(new UserAggregate(
                    toDomain(accountDO),
                    toDomain(profileDO),
                    toDomain(defaultSecurityDO(userId, createdAt)),
                    toDomain(defaultPrivacyDO(userId, createdAt))
            ));
        }
        return aggregates;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public UserAggregate save(UserAggregate aggregate) {
        UserAccountDO accountDO = accountRepository.findByUserId(aggregate.getAccount().getUserId())
                .orElse(new UserAccountDO());
        fillAccountDO(accountDO, aggregate.getAccount());
        accountRepository.save(accountDO);

        UserProfileDO profileDO = profileRepository.findByUserId(aggregate.getProfile().getUserId())
                .orElse(new UserProfileDO());
        fillProfileDO(profileDO, aggregate.getProfile());
        profileRepository.save(profileDO);

        UserSecuritySettingDO securityDO = securityRepository.findByUserId(aggregate.getSecuritySetting().getUserId())
                .orElse(new UserSecuritySettingDO());
        fillSecurityDO(securityDO, aggregate.getSecuritySetting());
        securityRepository.save(securityDO);

        UserPrivacySettingDO privacyDO = privacyRepository.findByUserId(aggregate.getPrivacySetting().getUserId())
                .orElse(new UserPrivacySettingDO());
        fillPrivacyDO(privacyDO, aggregate.getPrivacySetting());
        privacyRepository.save(privacyDO);

        return findByUserId(aggregate.getAccount().getUserId()).orElseThrow();
    }

    /**
     * 查询联系人信息列表。
     */
    @Override
    public List<UserRecentContact> listRecentContacts(Long ownerUserId, int limit) {
        if (ownerUserId == null || limit <= 0) {
            return List.of();
        }

        List<UserRecentContactDO> recentEntities =
                recentContactRepository.findByOwnerUserIdOrderByLastInteractionDesc(ownerUserId, limit);
        if (recentEntities.isEmpty()) {
            return List.of();
        }

        Set<Long> contactUserIds = recentEntities.stream()
                .map(UserRecentContactDO::getContactUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (contactUserIds.isEmpty()) {
            return List.of();
        }

        Map<Long, UserAccountDO> accountMap = accountRepository.findByUserIds(contactUserIds).stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserAccountDO::getUserId, Function.identity(), (first, ignored) -> first));
        Map<Long, UserProfileDO> profileMap = profileRepository.findByUserIds(contactUserIds).stream()
                .filter(entity -> entity.getUserId() != null)
                .collect(Collectors.toMap(UserProfileDO::getUserId, Function.identity(), (first, ignored) -> first));

        List<UserRecentContact> recentContacts = new ArrayList<>();
        for (UserRecentContactDO entity : recentEntities) {
            Long contactUserId = entity.getContactUserId();
            if (contactUserId == null) {
                continue;
            }
            recentContacts.add(toDomain(
                    ownerUserId,
                    entity,
                    accountMap.get(contactUserId),
                    profileMap.get(contactUserId)
            ));
        }
        return recentContacts;
    }

    /**
     * 按UID处理记录。
     */
    @Override
    public boolean existsByAipayUid(String aipayUid) {
        return accountRepository.existsByAipayUid(aipayUid);
    }

    /**
     * 标记功能信息。
     */
    @Override
    @Transactional
    public void markFeatureEnabled(Long userId, UserFeatureCode featureCode, LocalDateTime openedAt) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        if (featureCode == null) {
            throw new IllegalArgumentException("featureCode must not be null");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime normalizedOpenedAt = openedAt == null ? now : openedAt;
        UserFeatureStatusDO entity = userFeatureStatusMapper.findByUserIdAndFeatureCode(userId, featureCode.persistentCode())
                .orElse(null);
        if (entity != null) {
            entity.setEnabled(true);
            entity.setOpenedAt(normalizedOpenedAt);
            entity.setUpdatedAt(now);
            userFeatureStatusMapper.updateById(entity);
            return;
        }

        UserFeatureStatusDO created = new UserFeatureStatusDO();
        created.setUserId(userId);
        created.setFeatureCode(featureCode.name());
        created.setEnabled(true);
        created.setOpenedAt(normalizedOpenedAt);
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        try {
            userFeatureStatusMapper.insert(created);
        } catch (DuplicateKeyException duplicateKeyException) {
            UserFeatureStatusDO existing = userFeatureStatusMapper.findByUserIdAndFeatureCode(userId, featureCode.persistentCode())
                    .orElseThrow(() -> duplicateKeyException);
            existing.setEnabled(true);
            existing.setFeatureCode(featureCode.name());
            existing.setOpenedAt(normalizedOpenedAt);
            existing.setUpdatedAt(now);
            userFeatureStatusMapper.updateById(existing);
        }
    }

    /**
     * 判断是否功能信息。
     */
    @Override
    public boolean isFeatureEnabled(Long userId, UserFeatureCode featureCode) {
        if (userId == null || userId <= 0 || featureCode == null) {
            return false;
        }
        return userFeatureStatusMapper.findByUserIdAndFeatureCode(userId, featureCode.persistentCode())
                .map(UserFeatureStatusDO::getEnabled)
                .orElse(false);
    }

    private UserAccount toDomain(UserAccountDO entity) {
        return new UserAccount(
                entity.getUserId(),
                entity.getAipayUid(),
                entity.getLoginId(),
                UserStatus.valueOf(entity.getAccountStatus()),
                KycLevel.valueOf(entity.getKycLevel()),
                resolveAccountSource(entity.getAccountSource()),
                Boolean.TRUE.equals(entity.getLoginPasswordSet()),
                Boolean.TRUE.equals(entity.getPayPasswordSet()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserProfile toDomain(UserProfileDO entity) {
        return new UserProfile(
                entity.getUserId(),
                entity.getNickname(),
                normalizeAvatarUrl(entity.getUserId(), entity.getAvatarUrl()),
                entity.getCountryCode(),
                entity.getMobile(),
                entity.getMaskedRealName(),
                entity.getIdCardNo(),
                entity.getGender(),
                entity.getRegion(),
                entity.getBirthday(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserSecuritySetting toDomain(UserSecuritySettingDO entity) {
        return new UserSecuritySetting(
                entity.getUserId(),
                Boolean.TRUE.equals(entity.getBiometricEnabled()),
                entity.getTwoFactorMode(),
                entity.getRiskLevel(),
                Boolean.TRUE.equals(entity.getDeviceLockEnabled()),
                Boolean.TRUE.equals(entity.getPrivacyModeEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserPrivacySetting toDomain(UserPrivacySettingDO entity) {
        return new UserPrivacySetting(
                entity.getUserId(),
                Boolean.TRUE.equals(entity.getAllowSearchByMobile()),
                Boolean.TRUE.equals(entity.getAllowSearchByAipayUid()),
                Boolean.TRUE.equals(entity.getHideRealName()),
                Boolean.TRUE.equals(entity.getPersonalizedRecommendationEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserRecentContact toDomain(Long ownerUserId,
                                       UserRecentContactDO recentDO,
                                       UserAccountDO accountDO,
                                       UserProfileDO profileDO) {
        Long contactUserId = recentDO.getContactUserId();
        String contactAipayUid = firstNonBlank(
                accountDO == null ? null : accountDO.getAipayUid(),
                contactUserId == null ? null : String.valueOf(contactUserId)
        );
        String contactNickname = firstNonBlank(
                profileDO == null ? null : profileDO.getNickname()
        );
        String contactDisplayName = firstNonBlank(
                contactNickname,
                contactAipayUid,
                contactUserId == null ? null : "用户" + contactUserId
        );
        String contactMobileMasked = maskMobile(profileDO == null ? null : profileDO.getMobile());
        String contactMaskedRealName = profileDO == null ? null : profileDO.getMaskedRealName();

        return new UserRecentContact(
                ownerUserId,
                contactUserId,
                contactAipayUid,
                contactNickname,
                contactDisplayName,
                contactMaskedRealName,
                normalizeAvatarUrl(
                        contactUserId,
                        profileDO == null ? null : profileDO.getAvatarUrl()
                ),
                contactMobileMasked,
                recentDO.getInteractionSceneCode(),
                recentDO.getRemark(),
                recentDO.getInteractionCount() == null ? 0L : recentDO.getInteractionCount(),
                recentDO.getLastInteractionAt()
        );
    }

    private void fillAccountDO(UserAccountDO entity, UserAccount account) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() == null ? now : account.getCreatedAt());
        }
        entity.setUserId(account.getUserId());
        entity.setAipayUid(account.getAipayUid());
        entity.setLoginId(account.getLoginId());
        entity.setAccountStatus(account.getAccountStatus().name());
        entity.setKycLevel(account.getKycLevel().name());
        entity.setAccountSource(account.getAccountSource().name());
        entity.setLoginPasswordSet(account.isLoginPasswordSet());
        entity.setPayPasswordSet(account.isPayPasswordSet());
        entity.setUpdatedAt(account.getUpdatedAt() == null ? now : account.getUpdatedAt());
    }

    private void fillProfileDO(UserProfileDO entity, UserProfile profile) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(profile.getCreatedAt() == null ? now : profile.getCreatedAt());
        }
        entity.setUserId(profile.getUserId());
        entity.setNickname(profile.getNickname());
        entity.setAvatarUrl(normalizeAvatarUrl(profile.getUserId(), profile.getAvatarUrl()));
        entity.setCountryCode(profile.getCountryCode());
        entity.setMobile(profile.getMobile());
        entity.setMaskedRealName(profile.getMaskedRealName());
        entity.setIdCardNo(profile.getIdCardNo());
        entity.setGender(profile.getGender());
        entity.setRegion(profile.getRegion());
        entity.setBirthday(profile.getBirthday());
        entity.setUpdatedAt(profile.getUpdatedAt() == null ? now : profile.getUpdatedAt());
    }

    private void fillSecurityDO(UserSecuritySettingDO entity, UserSecuritySetting security) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(security.getCreatedAt() == null ? now : security.getCreatedAt());
        }
        entity.setUserId(security.getUserId());
        entity.setBiometricEnabled(security.isBiometricEnabled());
        entity.setTwoFactorMode(security.getTwoFactorMode());
        entity.setRiskLevel(security.getRiskLevel());
        entity.setDeviceLockEnabled(security.isDeviceLockEnabled());
        entity.setPrivacyModeEnabled(security.isPrivacyModeEnabled());
        entity.setUpdatedAt(security.getUpdatedAt() == null ? now : security.getUpdatedAt());
    }

    private void fillPrivacyDO(UserPrivacySettingDO entity, UserPrivacySetting privacy) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(privacy.getCreatedAt() == null ? now : privacy.getCreatedAt());
        }
        entity.setUserId(privacy.getUserId());
        entity.setAllowSearchByMobile(privacy.isAllowSearchByMobile());
        entity.setAllowSearchByAipayUid(privacy.isAllowSearchByAipayUid());
        entity.setHideRealName(privacy.isHideRealName());
        entity.setPersonalizedRecommendationEnabled(privacy.isPersonalizedRecommendationEnabled());
        entity.setUpdatedAt(privacy.getUpdatedAt() == null ? now : privacy.getUpdatedAt());
    }

    private UserProfileDO defaultProfileDO(Long userId, LocalDateTime createdAt) {
        UserProfileDO entity = new UserProfileDO();
        entity.setUserId(userId);
        entity.setNickname("用户" + userId);
        entity.setAvatarUrl(defaultAvatarUrl(userId));
        entity.setCountryCode("86");
        entity.setMobile(null);
        entity.setMaskedRealName(null);
        entity.setIdCardNo(null);
        entity.setGender("UNKNOWN");
        entity.setRegion("CN");
        entity.setBirthday(null);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }

    private UserSecuritySettingDO defaultSecurityDO(Long userId, LocalDateTime createdAt) {
        UserSecuritySettingDO entity = new UserSecuritySettingDO();
        entity.setUserId(userId);
        entity.setBiometricEnabled(false);
        entity.setTwoFactorMode("NONE");
        entity.setRiskLevel("LOW");
        entity.setDeviceLockEnabled(false);
        entity.setPrivacyModeEnabled(false);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }

    private UserPrivacySettingDO defaultPrivacyDO(Long userId, LocalDateTime createdAt) {
        UserPrivacySettingDO entity = new UserPrivacySettingDO();
        entity.setUserId(userId);
        entity.setAllowSearchByMobile(true);
        entity.setAllowSearchByAipayUid(true);
        entity.setHideRealName(false);
        entity.setPersonalizedRecommendationEnabled(true);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
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

    private String normalizeAvatarUrl(Long userId, String avatarUrl) {
        if (avatarUrl != null) {
            String trimmed = avatarUrl.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return defaultAvatarUrl(userId);
    }

    private String defaultAvatarUrl(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return UserAvatarCatalog.defaultWechatStyleAvatarUrl(userId);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private UserAccountSource resolveAccountSource(String rawAccountSource) {
        if (rawAccountSource == null || rawAccountSource.isBlank()) {
            return UserAccountSource.REGISTER;
        }
        try {
            return UserAccountSource.valueOf(rawAccountSource.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return UserAccountSource.REGISTER;
        }
    }
}
