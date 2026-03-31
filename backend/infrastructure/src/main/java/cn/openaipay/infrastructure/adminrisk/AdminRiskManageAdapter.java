package cn.openaipay.infrastructure.adminrisk;

import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.application.adminrisk.port.AdminRiskManagePort;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.infrastructure.contact.dataobject.ContactBlacklistDO;
import cn.openaipay.infrastructure.contact.mapper.ContactBlacklistMapper;
import cn.openaipay.infrastructure.risk.dataobject.RiskRuleDO;
import cn.openaipay.infrastructure.risk.mapper.RiskRuleMapper;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserPrivacySettingDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.dataobject.UserSecuritySettingDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserPrivacySettingMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import cn.openaipay.infrastructure.user.mapper.UserSecuritySettingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 风控管理适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class AdminRiskManageAdapter implements AdminRiskManagePort {

    private final UserAccountMapper userAccountMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserSecuritySettingMapper userSecuritySettingMapper;
    private final UserPrivacySettingMapper userPrivacySettingMapper;
    private final ContactBlacklistMapper contactBlacklistMapper;
    private final RiskRuleMapper riskRuleMapper;
    private final JdbcTemplate jdbcTemplate;

    public AdminRiskManageAdapter(UserAccountMapper userAccountMapper,
                                  UserProfileMapper userProfileMapper,
                                  UserSecuritySettingMapper userSecuritySettingMapper,
                                  UserPrivacySettingMapper userPrivacySettingMapper,
                                  ContactBlacklistMapper contactBlacklistMapper,
                                  RiskRuleMapper riskRuleMapper,
                                  JdbcTemplate jdbcTemplate) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
        this.userSecuritySettingMapper = userSecuritySettingMapper;
        this.userPrivacySettingMapper = userPrivacySettingMapper;
        this.contactBlacklistMapper = contactBlacklistMapper;
        this.riskRuleMapper = riskRuleMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AdminRiskOverviewDTO overview() {
        long totalUserCount = safeCount(userAccountMapper.selectCount(new QueryWrapper<>()));
        long l0Count = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("kyc_level", "L0")));
        long l1Count = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("kyc_level", "L1")));
        long l2Count = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("kyc_level", "L2")));
        long l3Count = safeCount(userAccountMapper.selectCount(new QueryWrapper<UserAccountDO>().eq("kyc_level", "L3")));
        long lowRiskCount = safeCount(userSecuritySettingMapper.selectCount(new QueryWrapper<UserSecuritySettingDO>().eq("risk_level", "LOW")));
        long mediumRiskCount = safeCount(userSecuritySettingMapper.selectCount(new QueryWrapper<UserSecuritySettingDO>().eq("risk_level", "MEDIUM")));
        long highRiskCount = safeCount(userSecuritySettingMapper.selectCount(new QueryWrapper<UserSecuritySettingDO>().eq("risk_level", "HIGH")));
        long blacklistCount = safeCount(contactBlacklistMapper.selectCount(new QueryWrapper<>()));
        return new AdminRiskOverviewDTO(
                totalUserCount,
                l0Count,
                l1Count,
                l2Count,
                l3Count,
                lowRiskCount,
                mediumRiskCount,
                highRiskCount,
                blacklistCount
        );
    }

    @Override
    public List<AdminRiskUserRowDTO> listUsers(String keyword, String kycLevel, String riskLevel, int pageNo, int pageSize) {
        QueryWrapper<UserAccountDO> wrapper = new QueryWrapper<>();
        if (kycLevel != null) {
            wrapper.eq("kyc_level", kycLevel);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword != null) {
            Set<Long> profileMatchedUserIds = safeList(userProfileMapper.selectList(
                    new QueryWrapper<UserProfileDO>()
                            .select("user_id")
                            .and(query -> query.like("nickname", normalizedKeyword)
                                    .or()
                                    .like("mobile", normalizedKeyword)
                                    .or()
                                    .like("masked_real_name", normalizedKeyword))
            )).stream()
                    .map(UserProfileDO::getUserId)
                    .filter(item -> item != null && item > 0)
                    .collect(Collectors.toSet());

            wrapper.and(query -> {
                query.like("aipay_uid", normalizedKeyword)
                        .or()
                        .like("login_id", normalizedKeyword);
                Long userId = parsePositiveLongOrNull(normalizedKeyword);
                if (userId != null) {
                    query.or().eq("user_id", userId);
                }
                if (!profileMatchedUserIds.isEmpty()) {
                    query.or().in("user_id", profileMatchedUserIds);
                }
            });
        }

        if (riskLevel != null) {
            Set<Long> matchedUserIds = safeList(userSecuritySettingMapper.selectList(
                    new QueryWrapper<UserSecuritySettingDO>()
                            .select("user_id")
                            .eq("risk_level", riskLevel)
            )).stream()
                    .map(UserSecuritySettingDO::getUserId)
                    .filter(item -> item != null && item > 0)
                    .collect(Collectors.toSet());
            if (matchedUserIds.isEmpty()) {
                return List.of();
            }
            wrapper.in("user_id", matchedUserIds);
        }

        wrapper.orderByDesc("updated_at");
        wrapper.last(buildPageClause(pageNo, pageSize));
        return buildRiskUserRows(safeList(userAccountMapper.selectList(wrapper)));
    }

    @Override
    public List<AdminRiskBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, int pageNo, int pageSize) {
        QueryWrapper<ContactBlacklistDO> wrapper = new QueryWrapper<>();
        if (ownerUserId != null) {
            wrapper.eq("owner_user_id", ownerUserId);
        }
        if (blockedUserId != null) {
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
                    return new AdminRiskBlacklistRowDTO(
                            item.getOwnerUserId(),
                            owner == null ? null : owner.displayName(),
                            owner == null ? null : owner.aipayUid(),
                            item.getBlockedUserId(),
                            blocked == null ? null : blocked.displayName(),
                            blocked == null ? null : blocked.aipayUid(),
                            item.getReason(),
                            item.getCreatedAt(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Override
    public AdminRiskUserRowDTO updateRiskProfile(Long userId,
                                                 String kycLevel,
                                                 String riskLevel,
                                                 String twoFactorMode,
                                                 Boolean deviceLockEnabled,
                                                 Boolean privacyModeEnabled) {
        Long normalizedUserId = requirePositive(userId, "userId");
        UserAccountDO account = userAccountMapper.findByUserId(normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + normalizedUserId));

        if (kycLevel != null) {
            UserAccountDO updatedAccount = new UserAccountDO();
            updatedAccount.setKycLevel(kycLevel);
            userAccountMapper.update(updatedAccount, new QueryWrapper<UserAccountDO>().eq("user_id", normalizedUserId));
        }

        if (riskLevel != null || twoFactorMode != null || deviceLockEnabled != null || privacyModeEnabled != null) {
            UserSecuritySettingDO security = userSecuritySettingMapper.findByUserId(normalizedUserId).orElse(null);
            if (security == null) {
                security = new UserSecuritySettingDO();
                security.setUserId(normalizedUserId);
                security.setBiometricEnabled(false);
                security.setTwoFactorMode(twoFactorMode == null ? "NONE" : twoFactorMode);
                security.setRiskLevel(riskLevel == null ? "LOW" : riskLevel);
                security.setDeviceLockEnabled(Boolean.TRUE.equals(deviceLockEnabled));
                security.setPrivacyModeEnabled(Boolean.TRUE.equals(privacyModeEnabled));
                security.setCreatedAt(LocalDateTime.now());
                security.setUpdatedAt(LocalDateTime.now());
                userSecuritySettingMapper.insert(security);
            } else {
                UserSecuritySettingDO updated = new UserSecuritySettingDO();
                if (riskLevel != null) {
                    updated.setRiskLevel(riskLevel);
                }
                if (twoFactorMode != null) {
                    updated.setTwoFactorMode(twoFactorMode);
                }
                if (deviceLockEnabled != null) {
                    updated.setDeviceLockEnabled(deviceLockEnabled);
                }
                if (privacyModeEnabled != null) {
                    updated.setPrivacyModeEnabled(privacyModeEnabled);
                }
                userSecuritySettingMapper.update(updated, new QueryWrapper<UserSecuritySettingDO>().eq("user_id", normalizedUserId));
            }
        }

        List<AdminRiskUserRowDTO> rows = buildRiskUserRows(List.of(userAccountMapper.findByUserId(normalizedUserId).orElse(account)));
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<AdminRiskRuleDTO> listRules(String sceneCode, String status, int pageNo, int pageSize) {
        QueryWrapper<RiskRuleDO> wrapper = new QueryWrapper<>();
        if (sceneCode != null) {
            wrapper.eq("scene_code", sceneCode);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("priority").orderByDesc("updated_at").last(buildPageClause(pageNo, pageSize));
        return safeList(riskRuleMapper.selectList(wrapper)).stream().map(this::toRiskRule).toList();
    }

    @Override
    public Optional<AdminRiskRuleDTO> findRule(String ruleCode) {
        if (ruleCode == null || ruleCode.isBlank()) {
            return Optional.empty();
        }
        return riskRuleMapper.findByRuleCode(ruleCode.trim().toUpperCase(Locale.ROOT)).map(this::toRiskRule);
    }

    @Override
    public AdminRiskRuleDTO saveRule(AdminRiskRuleDTO rule) {
        RiskRuleDO entity = riskRuleMapper.findByRuleCode(rule.ruleCode())
                .orElseGet(RiskRuleDO::new);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setRuleCode(rule.ruleCode());
            entity.setCreatedAt(now);
        }
        entity.setSceneCode(rule.sceneCode());
        entity.setRuleType(rule.ruleType());
        entity.setScopeType(rule.scopeType());
        entity.setScopeValue(rule.scopeValue());
        entity.setThresholdAmount(rule.thresholdAmount());
        entity.setCurrencyCode(rule.currencyCode());
        entity.setPriority(rule.priority());
        entity.setStatus(rule.status());
        entity.setRuleDesc(rule.ruleDesc());
        entity.setUpdatedBy(rule.updatedBy());
        entity.setUpdatedAt(now);
        riskRuleMapper.save(entity);
        return toRiskRule(entity);
    }

    @Override
    public AdminRiskRuleDTO changeRuleStatus(String ruleCode, String status, String operator) {
        RiskRuleDO entity = riskRuleMapper.findByRuleCode(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("risk rule not found: " + ruleCode));
        entity.setStatus(status);
        entity.setUpdatedBy(operator);
        entity.setUpdatedAt(LocalDateTime.now());
        riskRuleMapper.save(entity);
        return toRiskRule(entity);
    }

    @Override
    public RiskDecision evaluateTradeRisk(String sceneCode, Long userId, BigDecimal amount, String currencyCode) {
        List<RiskRuleDO> activeRules = safeList(riskRuleMapper.selectList(
                new QueryWrapper<RiskRuleDO>()
                        .eq("scene_code", sceneCode)
                        .eq("status", "ACTIVE")
                        .orderByAsc("priority")
                        .orderByDesc("updated_at")
        ));
        if (activeRules.isEmpty()) {
            return RiskDecision.pass();
        }
        BigDecimal normalizedAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal todayAccumulated = queryTodayAccumulatedAmount(sceneCode, userId, currencyCode);
        for (RiskRuleDO rule : activeRules) {
            if (!matchesScope(rule, userId)) {
                continue;
            }
            String ruleType = normalizeUpper(rule.getRuleType());
            if ("USER_BLOCK".equals(ruleType)) {
                return RiskDecision.reject("RISK_RULE_USER_BLOCK", "用户触发风控阻断规则：" + rule.getRuleCode());
            }
            BigDecimal threshold = rule.getThresholdAmount() == null ? BigDecimal.ZERO : rule.getThresholdAmount();
            if ("SINGLE_LIMIT".equals(ruleType) && threshold.compareTo(BigDecimal.ZERO) > 0
                    && normalizedAmount.compareTo(threshold) > 0) {
                return RiskDecision.reject("RISK_RULE_SINGLE_LIMIT", "超过单笔限额规则：" + rule.getRuleCode());
            }
            if ("DAILY_LIMIT".equals(ruleType) && threshold.compareTo(BigDecimal.ZERO) > 0
                    && todayAccumulated.add(normalizedAmount).compareTo(threshold) > 0) {
                return RiskDecision.reject("RISK_RULE_DAILY_LIMIT", "超过单日累计限额规则：" + rule.getRuleCode());
            }
        }
        return RiskDecision.pass();
    }

    private List<AdminRiskUserRowDTO> buildRiskUserRows(List<UserAccountDO> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = accounts.stream()
                .map(UserAccountDO::getUserId)
                .filter(item -> item != null && item > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, UserDigest> userDigestMap = loadUserDigestMap(userIds);
        Map<Long, UserSecuritySettingDO> securityMap = safeList(userSecuritySettingMapper.selectList(
                new QueryWrapper<UserSecuritySettingDO>().in("user_id", userIds)
        )).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserSecuritySettingDO::getUserId, Function.identity(), (left, right) -> left));
        Map<Long, UserPrivacySettingDO> privacyMap = safeList(userPrivacySettingMapper.selectList(
                new QueryWrapper<UserPrivacySettingDO>().in("user_id", userIds)
        )).stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(UserPrivacySettingDO::getUserId, Function.identity(), (left, right) -> left));

        return accounts.stream()
                .map(item -> {
                    UserDigest userDigest = userDigestMap.get(item.getUserId());
                    UserSecuritySettingDO security = securityMap.get(item.getUserId());
                    UserPrivacySettingDO privacy = privacyMap.get(item.getUserId());
                    return new AdminRiskUserRowDTO(
                            item.getUserId(),
                            userDigest == null ? null : userDigest.displayName(),
                            item.getAipayUid(),
                            item.getLoginId(),
                            userDigest == null ? null : userDigest.mobile(),
                            item.getAccountStatus(),
                            item.getKycLevel(),
                            security == null ? null : security.getRiskLevel(),
                            security == null ? null : security.getTwoFactorMode(),
                            security == null ? null : security.getDeviceLockEnabled(),
                            security == null ? null : security.getPrivacyModeEnabled(),
                            privacy == null ? null : privacy.getAllowSearchByMobile(),
                            privacy == null ? null : privacy.getAllowSearchByAipayUid(),
                            privacy == null ? null : privacy.getHideRealName(),
                            privacy == null ? null : privacy.getPersonalizedRecommendationEnabled(),
                            item.getUpdatedAt()
                    );
                })
                .toList();
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
            result.put(userId, new UserDigest(
                    userId,
                    profile != null && hasText(profile.getNickname()) ? profile.getNickname() : account == null ? null : account.getAipayUid(),
                    account == null ? null : account.getAipayUid(),
                    profile == null ? null : profile.getMobile()
            ));
        });
        return result;
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

    private AdminRiskRuleDTO toRiskRule(RiskRuleDO entity) {
        return new AdminRiskRuleDTO(
                entity.getRuleCode(),
                entity.getSceneCode(),
                entity.getRuleType(),
                entity.getScopeType(),
                entity.getScopeValue(),
                entity.getThresholdAmount(),
                entity.getCurrencyCode(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getRuleDesc(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private boolean matchesScope(RiskRuleDO rule, Long userId) {
        if (rule == null) {
            return false;
        }
        String scopeType = normalizeUpper(rule.getScopeType());
        if ("GLOBAL".equals(scopeType)) {
            return true;
        }
        if (!"USER".equals(scopeType)) {
            return false;
        }
        Long scopedUserId = parsePositiveLongOrNull(rule.getScopeValue());
        return scopedUserId != null && scopedUserId.equals(userId);
    }

    private BigDecimal queryTodayAccumulatedAmount(String sceneCode, Long userId, String currencyCode) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal value = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(payable_amount), 0)
                          FROM trade_order
                         WHERE business_scene_code = ?
                           AND payer_user_id = ?
                           AND status IN ('CREATED','QUOTED','PAY_SUBMITTED','PAY_PROCESSING','PAY_PREPARING','PAY_PREPARED','PAY_COMMITTING','SUCCEEDED','RECON_PENDING')
                           AND created_at >= ?
                           AND created_at < ?
                        """,
                BigDecimal.class,
                sceneCode,
                userId,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeUpper(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String normalizeKeyword(String raw) {
        String normalized = (raw == null ? "" : raw).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String buildPageClause(int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(1, pageNo);
        int normalizedPageSize = Math.max(1, pageSize);
        int offset = (normalizedPageNo - 1) * normalizedPageSize;
        return "LIMIT " + normalizedPageSize + " OFFSET " + offset;
    }

    private Long parsePositiveLongOrNull(String raw) {
        try {
            long value = Long.parseLong((raw == null ? "" : raw).trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private Long requirePositive(Long value, String label) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private record UserDigest(
            Long userId,
            String displayName,
            String aipayUid,
            String mobile
    ) {
    }
}
