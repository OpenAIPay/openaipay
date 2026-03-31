package cn.openaipay.application.adminrisk.service.impl;

import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.application.adminrisk.port.AdminRiskManagePort;
import cn.openaipay.application.adminrisk.service.AdminRiskManageService;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 风控管理服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminRiskManageServiceImpl implements AdminRiskManageService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<String> ALLOWED_KYC_LEVELS = Set.of("L0", "L1", "L2", "L3");
    private static final Set<String> ALLOWED_RISK_LEVELS = Set.of("LOW", "MEDIUM", "HIGH");
    private static final Set<String> ALLOWED_TWO_FACTOR_MODES = Set.of("NONE", "SMS", "APP", "BIOMETRIC");
    private static final Set<String> ALLOWED_RULE_TYPES = Set.of("SINGLE_LIMIT", "DAILY_LIMIT", "USER_BLOCK");
    private static final Set<String> ALLOWED_SCOPE_TYPES = Set.of("GLOBAL", "USER");
    private static final Set<String> ALLOWED_RULE_STATUS = Set.of("ACTIVE", "INACTIVE");

    private final AdminRiskManagePort adminRiskManagePort;

    public AdminRiskManageServiceImpl(AdminRiskManagePort adminRiskManagePort) {
        this.adminRiskManagePort = adminRiskManagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRiskOverviewDTO overview() {
        return adminRiskManagePort.overview();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRiskUserRowDTO> listUsers(String keyword, String kycLevel, String riskLevel, Integer pageNo, Integer pageSize) {
        return adminRiskManagePort.listUsers(
                keyword,
                normalizeCode(kycLevel),
                normalizeCode(riskLevel),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRiskBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, Integer pageNo, Integer pageSize) {
        return adminRiskManagePort.listBlacklists(
                normalizeOptionalPositive(ownerUserId, "ownerUserId"),
                normalizeOptionalPositive(blockedUserId, "blockedUserId"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional
    public AdminRiskUserRowDTO updateRiskProfile(Long userId,
                                                 String kycLevel,
                                                 String riskLevel,
                                                 String twoFactorMode,
                                                 Boolean deviceLockEnabled,
                                                 Boolean privacyModeEnabled) {
        return adminRiskManagePort.updateRiskProfile(
                requirePositive(userId, "userId"),
                normalizeAllowedCodeNullable(kycLevel, ALLOWED_KYC_LEVELS, "kycLevel"),
                normalizeAllowedCodeNullable(riskLevel, ALLOWED_RISK_LEVELS, "riskLevel"),
                normalizeAllowedCodeNullable(twoFactorMode, ALLOWED_TWO_FACTOR_MODES, "twoFactorMode"),
                deviceLockEnabled,
                privacyModeEnabled
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRiskRuleDTO> listRules(String sceneCode, String status, Integer pageNo, Integer pageSize) {
        return adminRiskManagePort.listRules(
                normalizeCode(sceneCode),
                normalizeAllowedCodeNullable(status, ALLOWED_RULE_STATUS, "status"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional
    public AdminRiskRuleDTO saveRule(AdminRiskRuleDTO rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null");
        }
        String ruleCode = normalizeRequiredText(rule.ruleCode(), "ruleCode");
        String sceneCode = normalizeRequiredText(rule.sceneCode(), "sceneCode");
        String ruleType = normalizeAllowedCode(rule.ruleType(), ALLOWED_RULE_TYPES, "ruleType");
        String scopeType = normalizeAllowedCode(rule.scopeType(), ALLOWED_SCOPE_TYPES, "scopeType");
        String status = normalizeAllowedCode(rule.status(), ALLOWED_RULE_STATUS, "status");
        String scopeValue = normalizeScopeValue(scopeType, rule.scopeValue());
        BigDecimal thresholdAmount = normalizeThresholdAmount(ruleType, rule.thresholdAmount());
        String currencyCode = normalizeCurrencyCode(rule.currencyCode());
        Integer priority = normalizePriority(rule.priority());
        String ruleDesc = normalizeOptionalText(rule.ruleDesc());
        String updatedBy = normalizeOptionalText(rule.updatedBy()) == null ? "admin" : normalizeOptionalText(rule.updatedBy());
        return adminRiskManagePort.saveRule(new AdminRiskRuleDTO(
                ruleCode,
                sceneCode,
                ruleType,
                scopeType,
                scopeValue,
                thresholdAmount,
                currencyCode,
                priority,
                status,
                ruleDesc,
                updatedBy,
                rule.createdAt(),
                rule.updatedAt()
        ));
    }

    @Override
    @Transactional
    public AdminRiskRuleDTO changeRuleStatus(String ruleCode, String status, String operator) {
        return adminRiskManagePort.changeRuleStatus(
                normalizeRequiredText(ruleCode, "ruleCode"),
                normalizeAllowedCode(status, ALLOWED_RULE_STATUS, "status"),
                normalizeOptionalText(operator) == null ? "admin" : normalizeOptionalText(operator)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RiskDecision evaluateTradeRisk(String sceneCode, Long userId, BigDecimal amount, String currencyCode) {
        String normalizedSceneCode = normalizeRequiredText(sceneCode, "sceneCode");
        Long normalizedUserId = requirePositive(userId, "userId");
        BigDecimal normalizedAmount = normalizePositiveAmount(amount, "amount");
        String normalizedCurrencyCode = normalizeCurrencyCode(currencyCode);
        return adminRiskManagePort.evaluateTradeRisk(
                normalizedSceneCode,
                normalizedUserId,
                normalizedAmount,
                normalizedCurrencyCode
        );
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private Long normalizeOptionalPositive(Long value, String label) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    private Long requirePositive(Long value, String label) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    private String normalizeAllowedCode(String raw, Set<String> allowed, String label) {
        String normalized = normalizeCode(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(label + " is invalid");
        }
        return normalized;
    }

    private String normalizeAllowedCodeNullable(String raw, Set<String> allowed, String label) {
        String normalized = normalizeCode(raw);
        if (normalized == null) {
            return null;
        }
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(label + " is invalid");
        }
        return normalized;
    }

    private String normalizeCurrencyCode(String rawCurrencyCode) {
        String normalized = normalizeCode(rawCurrencyCode);
        return normalized == null ? "CNY" : normalized;
    }

    private String normalizeScopeValue(String scopeType, String scopeValue) {
        if ("GLOBAL".equals(scopeType)) {
            return null;
        }
        return normalizeRequiredText(scopeValue, "scopeValue");
    }

    private Integer normalizePriority(Integer priority) {
        if (priority == null) {
            return 100;
        }
        return Math.max(1, Math.min(priority, 9999));
    }

    private BigDecimal normalizeThresholdAmount(String ruleType, BigDecimal thresholdAmount) {
        if ("USER_BLOCK".equals(ruleType)) {
            return null;
        }
        BigDecimal normalized = normalizePositiveAmount(thresholdAmount, "thresholdAmount");
        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePositiveAmount(BigDecimal amount, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return amount;
    }

    private String normalizeRequiredText(String raw, String label) {
        String normalized = normalizeOptionalText(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim();
    }

    private String normalizeCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }
}
