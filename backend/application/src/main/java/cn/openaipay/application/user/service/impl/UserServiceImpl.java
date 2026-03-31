package cn.openaipay.application.user.service.impl;

import cn.openaipay.application.auth.port.UserAuthQueryPort;
import cn.openaipay.application.user.command.CreateUserCommand;
import cn.openaipay.application.user.command.UpdateUserPrivacyCommand;
import cn.openaipay.application.user.command.UpdateUserProfileCommand;
import cn.openaipay.application.user.command.UpdateUserSecurityCommand;
import cn.openaipay.application.user.dto.UserInitDTO;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.dto.UserRecentContactDTO;
import cn.openaipay.application.user.dto.UserSecurityDTO;
import cn.openaipay.application.user.dto.UserSettingsDTO;
import cn.openaipay.application.user.id.StructuredUserIdGenerator;
import cn.openaipay.application.user.service.UserService;
import cn.openaipay.domain.user.model.UserAccount;
import cn.openaipay.domain.user.model.UserAccountSource;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserAvatarCatalog;
import cn.openaipay.domain.user.model.UserPrivacySetting;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.model.UserRecentContact;
import cn.openaipay.domain.user.model.UserSecuritySetting;
import cn.openaipay.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 用户应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class UserServiceImpl implements UserService {

    /** 最近联系人查询默认条数 */
    private static final int DEFAULT_RECENT_CONTACT_LIMIT = 20;
    /** 最近联系人查询最大条数 */
    private static final int MAX_RECENT_CONTACT_LIMIT = 50;

    /** UserRepository组件 */
    private final UserRepository userRepository;
    /** 认证查询组件 */
    private final UserAuthQueryPort userAuthQueryPort;
    /** StructuredUserIdGenerator组件 */
    private final StructuredUserIdGenerator userIdGenerator;

    public UserServiceImpl(UserRepository userRepository,
                           UserAuthQueryPort userAuthQueryPort) {
        this.userRepository = userRepository;
        this.userAuthQueryPort = userAuthQueryPort;
        this.userIdGenerator = new StructuredUserIdGenerator();
    }

    /**
     * 创建用户主数据。
     */
    @Override
    @Transactional
    public Long createCoreUser(CreateUserCommand command) {
        if (command.aipayUid() != null && !command.aipayUid().isBlank() && userRepository.existsByAipayUid(command.aipayUid())) {
            throw new IllegalArgumentException("aipayUid already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        Long userId = userIdGenerator.generate(command.userTypeCode());
        String resolvedAipayUid = command.aipayUid() == null || command.aipayUid().isBlank()
                ? String.valueOf(userId)
                : command.aipayUid().trim();
        UserAccountSource accountSource = normalizeAccountSource(command.accountSource());

        UserAccount account = UserAccount.newUser(userId, resolvedAipayUid, command.loginId(), accountSource, now);
        UserProfile profile = UserProfile.defaultOf(
                userId,
                command.nickname(),
                resolveAvatarUrl(command.avatarUrl(), userId),
                command.countryCode() == null ? "86" : command.countryCode(),
                command.mobile(),
                null,
                null,
                now
        );
        UserSecuritySetting securitySetting = UserSecuritySetting.defaultOf(userId, now);
        UserPrivacySetting privacySetting = UserPrivacySetting.defaultOf(userId, now);

        userRepository.save(new UserAggregate(account, profile, securitySetting, privacySetting));
        return userId;
    }

    /**
     * 获取资料信息。
     */
    @Override
    public UserProfileDTO getProfile(Long userId) {
        UserAggregate aggregate = mustGetUser(userId);
        return toProfileDTO(aggregate);
    }

    /**
     * 按登录账号ID获取资料信息。
     */
    @Override
    public UserProfileDTO getProfileByLoginId(String loginId) {
        String normalizedLoginId = normalizeRequired(loginId, "loginId");
        Long userId = userAuthQueryPort.findByLoginId(normalizedLoginId)
                .map(authView -> authView.userId())
                .orElseThrow(() -> new NoSuchElementException("user not found"));
        return getProfile(userId);
    }

    /**
     * 查询资料信息列表。
     */
    @Override
    public List<UserProfileDTO> listProfiles(List<Long> userIds) {
        List<Long> normalizedUserIds = normalizeProfileLookupIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findProfilesByUserIds(normalizedUserIds).stream()
                .map(this::toProfileDTO)
                .toList();
    }

    /**
     * 获取安全信息。
     */
    @Override
    public UserSecurityDTO getSecurity(Long userId) {
        UserAggregate aggregate = mustGetUser(userId);
        return toSecurityDTO(aggregate);
    }

    /**
     * 获取设置信息。
     */
    @Override
    public UserSettingsDTO getSettings(Long userId) {
        UserAggregate aggregate = mustGetUser(userId);
        return toSettingsDTO(aggregate);
    }

    /**
     * 获取初始化信息。
     */
    @Override
    public UserInitDTO getInit(Long userId) {
        UserAggregate aggregate = mustGetUser(userId);
        return new UserInitDTO(toProfileDTO(aggregate), toSecurityDTO(aggregate), toSettingsDTO(aggregate));
    }

    /**
     * 查询联系人信息列表。
     */
    @Override
    public List<UserRecentContactDTO> listRecentContacts(Long userId, Integer limit) {
        mustGetUser(userId);
        int normalizedLimit = normalizeRecentContactLimit(limit);
        return userRepository.listRecentContacts(userId, normalizedLimit).stream()
                .map(this::toRecentContactDTO)
                .toList();
    }

    /**
     * 更新资料信息。
     */
    @Override
    @Transactional
    public void updateProfile(UpdateUserProfileCommand command) {
        UserAggregate aggregate = mustGetUser(command.userId());
        aggregate.getProfile().updateBasicProfile(
                command.nickname(),
                command.avatarUrl(),
                command.mobile(),
                command.gender(),
                command.region(),
                parseDate(command.birthday())
        );
        userRepository.save(aggregate);
    }

    /**
     * 更新安全信息。
     */
    @Override
    @Transactional
    public void updateSecurity(UpdateUserSecurityCommand command) {
        UserAggregate aggregate = mustGetUser(command.userId());
        aggregate.getAccount().updateSecurityFlags(command.loginPasswordSet(), command.payPasswordSet());
        aggregate.getSecuritySetting().update(
                command.biometricEnabled(),
                command.twoFactorMode(),
                command.riskLevel(),
                command.deviceLockEnabled(),
                command.privacyModeEnabled()
        );
        userRepository.save(aggregate);
    }

    /**
     * 更新业务数据。
     */
    @Override
    @Transactional
    public void updatePrivacy(UpdateUserPrivacyCommand command) {
        UserAggregate aggregate = mustGetUser(command.userId());
        aggregate.getPrivacySetting().update(
                command.allowSearchByMobile(),
                command.allowSearchByAipayUid(),
                command.hideRealName(),
                command.personalizedRecommendationEnabled()
        );
        userRepository.save(aggregate);
    }

    private String resolveAvatarUrl(String requestedAvatarUrl, Long userId) {
        if (requestedAvatarUrl != null) {
            String trimmed = requestedAvatarUrl.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return UserAvatarCatalog.defaultWechatStyleAvatarUrl(userId);
    }

    private UserAccountSource normalizeAccountSource(String rawAccountSource) {
        if (rawAccountSource == null || rawAccountSource.isBlank()) {
            return UserAccountSource.REGISTER;
        }
        try {
            return UserAccountSource.valueOf(rawAccountSource.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return UserAccountSource.REGISTER;
        }
    }

    private UserAggregate mustGetUser(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + userId));
    }

    private UserProfileDTO toProfileDTO(UserAggregate aggregate) {
        UserAccount account = aggregate.getAccount();
        UserProfile profile = aggregate.getProfile();
        return new UserProfileDTO(
                account.getUserId(),
                account.getAipayUid(),
                account.getLoginId(),
                account.getAccountStatus().name(),
                account.getKycLevel().name(),
                account.getAccountSource().name(),
                profile.getNickname(),
                profile.getAvatarUrl(),
                profile.getCountryCode(),
                profile.getMobile(),
                profile.getMaskedRealName(),
                profile.getIdCardNo(),
                profile.getGender(),
                profile.getRegion(),
                profile.getBirthday() == null ? null : profile.getBirthday().toString()
        );
    }

    private UserSecurityDTO toSecurityDTO(UserAggregate aggregate) {
        UserAccount account = aggregate.getAccount();
        UserSecuritySetting security = aggregate.getSecuritySetting();
        return new UserSecurityDTO(
                account.getUserId(),
                account.isLoginPasswordSet(),
                account.isPayPasswordSet(),
                security.isBiometricEnabled(),
                security.getTwoFactorMode(),
                security.getRiskLevel(),
                security.isDeviceLockEnabled(),
                security.isPrivacyModeEnabled()
        );
    }

    private UserSettingsDTO toSettingsDTO(UserAggregate aggregate) {
        UserPrivacySetting privacy = aggregate.getPrivacySetting();
        UserSecuritySetting security = aggregate.getSecuritySetting();
        return new UserSettingsDTO(
                privacy.getUserId(),
                privacy.isAllowSearchByMobile(),
                privacy.isAllowSearchByAipayUid(),
                privacy.isHideRealName(),
                privacy.isPersonalizedRecommendationEnabled(),
                security.isBiometricEnabled(),
                security.getTwoFactorMode(),
                security.getRiskLevel(),
                security.isDeviceLockEnabled(),
                security.isPrivacyModeEnabled()
        );
    }

    private UserRecentContactDTO toRecentContactDTO(UserRecentContact recentContact) {
        return new UserRecentContactDTO(
                recentContact.getOwnerUserId(),
                recentContact.getContactUserId(),
                recentContact.getContactAipayUid(),
                recentContact.getContactNickname(),
                recentContact.getContactDisplayName(),
                recentContact.getContactMaskedRealName(),
                recentContact.getContactAvatarUrl(),
                recentContact.getContactMobileMasked(),
                recentContact.getInteractionSceneCode(),
                recentContact.getInteractionRemark(),
                recentContact.getInteractionCount(),
                recentContact.getLastInteractionAt() == null ? null : recentContact.getLastInteractionAt().toString()
        );
    }

    private int normalizeRecentContactLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_RECENT_CONTACT_LIMIT;
        }
        if (limit <= 0) {
            return DEFAULT_RECENT_CONTACT_LIMIT;
        }
        return Math.min(limit, MAX_RECENT_CONTACT_LIMIT);
    }

    private List<Long> normalizeProfileLookupIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> deduplicated = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId != null && userId > 0) {
                deduplicated.add(userId);
            }
            if (deduplicated.size() >= MAX_RECENT_CONTACT_LIMIT) {
                break;
            }
        }
        return List.copyOf(deduplicated);
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("birthday format must be yyyy-MM-dd");
        }
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }
}
