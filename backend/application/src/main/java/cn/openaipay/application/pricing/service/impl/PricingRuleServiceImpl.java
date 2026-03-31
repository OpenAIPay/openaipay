package cn.openaipay.application.pricing.service.impl;

import cn.openaipay.application.pricing.command.ChangePricingRuleStatusCommand;
import cn.openaipay.application.pricing.command.CreatePricingRuleCommand;
import cn.openaipay.application.pricing.command.UpdatePricingRuleCommand;
import cn.openaipay.application.pricing.dto.PricingRuleDTO;
import cn.openaipay.application.pricing.service.PricingRuleService;
import cn.openaipay.domain.pricing.model.PricingFeeBearer;
import cn.openaipay.domain.pricing.model.PricingFeeMode;
import cn.openaipay.domain.pricing.model.PricingRule;
import cn.openaipay.domain.pricing.model.PricingRuleStatus;
import cn.openaipay.domain.pricing.repository.PricingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Pricing规则应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PricingRuleServiceImpl implements PricingRuleService {

    /** PricingRepository组件 */
    private final PricingRepository pricingRepository;

    public PricingRuleServiceImpl(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    /**
     * 创建规则。
     */
    @Override
    @Transactional
    public PricingRuleDTO createRule(CreatePricingRuleCommand command) {
        String ruleCode = normalizeRequired(command.ruleCode(), "ruleCode").toUpperCase(Locale.ROOT);
        if (pricingRepository.findRuleByCode(ruleCode).isPresent()) {
            throw new IllegalArgumentException("pricing rule code already exists: " + ruleCode);
        }

        LocalDateTime now = LocalDateTime.now();
        PricingRule rule = PricingRule.createNew(
                ruleCode,
                normalizeRequired(command.ruleName(), "ruleName"),
                normalizeRequired(command.businessSceneCode(), "businessSceneCode"),
                normalizeRequired(command.paymentMethod(), "paymentMethod"),
                normalizeRequired(command.currencyCode(), "currencyCode"),
                PricingFeeMode.from(command.feeMode()),
                command.feeRate(),
                command.fixedFee(),
                command.minFee(),
                command.maxFee(),
                PricingFeeBearer.from(command.feeBearer()),
                command.priority(),
                parseStatus(command.initialStatus()),
                parseDateTimeOptional(command.validFrom(), "validFrom"),
                parseDateTimeOptional(command.validTo(), "validTo"),
                command.rulePayload(),
                normalizeOperator(command.operator()),
                now
        );
        return toRuleDTO(pricingRepository.saveRule(rule));
    }

    /**
     * 更新规则。
     */
    @Override
    @Transactional
    public PricingRuleDTO updateRule(UpdatePricingRuleCommand command) {
        Long ruleId = requirePositive(command.ruleId(), "ruleId");
        PricingRule rule = pricingRepository.findRuleById(ruleId)
                .orElseThrow(() -> new NoSuchElementException("pricing rule not found: " + ruleId));

        rule.update(
                normalizeRequired(command.ruleName(), "ruleName"),
                normalizeRequired(command.businessSceneCode(), "businessSceneCode"),
                normalizeRequired(command.paymentMethod(), "paymentMethod"),
                normalizeRequired(command.currencyCode(), "currencyCode"),
                PricingFeeMode.from(command.feeMode()),
                command.feeRate(),
                command.fixedFee(),
                command.minFee(),
                command.maxFee(),
                PricingFeeBearer.from(command.feeBearer()),
                command.priority(),
                parseDateTimeOptional(command.validFrom(), "validFrom"),
                parseDateTimeOptional(command.validTo(), "validTo"),
                command.rulePayload(),
                normalizeOperator(command.operator()),
                LocalDateTime.now()
        );
        return toRuleDTO(pricingRepository.saveRule(rule));
    }

    /**
     * 处理规则状态。
     */
    @Override
    @Transactional
    public PricingRuleDTO changeRuleStatus(ChangePricingRuleStatusCommand command) {
        Long ruleId = requirePositive(command.ruleId(), "ruleId");
        PricingRule rule = pricingRepository.findRuleById(ruleId)
                .orElseThrow(() -> new NoSuchElementException("pricing rule not found: " + ruleId));
        rule.changeStatus(
                PricingRuleStatus.from(command.status()),
                normalizeOperator(command.operator()),
                LocalDateTime.now()
        );
        return toRuleDTO(pricingRepository.saveRule(rule));
    }

    /**
     * 获取规则。
     */
    @Override
    @Transactional(readOnly = true)
    public PricingRuleDTO getRule(Long ruleId) {
        Long id = requirePositive(ruleId, "ruleId");
        PricingRule rule = pricingRepository.findRuleById(id)
                .orElseThrow(() -> new NoSuchElementException("pricing rule not found: " + id));
        return toRuleDTO(rule);
    }

    /**
     * 查询规则列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<PricingRuleDTO> listRules(String businessSceneCode, String paymentMethod, String status) {
        return pricingRepository.findRules(
                        normalizeFilter(businessSceneCode),
                        normalizeFilter(paymentMethod),
                        normalizeFilter(status)
                )
                .stream()
                .map(this::toRuleDTO)
                .toList();
    }

    private PricingRuleStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return PricingRuleStatus.DRAFT;
        }
        return PricingRuleStatus.from(rawStatus);
    }

    private PricingRuleDTO toRuleDTO(PricingRule rule) {
        return new PricingRuleDTO(
                rule.getRuleId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getBusinessSceneCode(),
                rule.getPaymentMethod(),
                rule.getCurrencyCode(),
                rule.getFeeMode().name(),
                rule.getFeeRate(),
                rule.getFixedFee(),
                rule.getMinFee(),
                rule.getMaxFee(),
                rule.getFeeBearer().name(),
                rule.getPriority(),
                rule.getStatus().name(),
                rule.getValidFrom(),
                rule.getValidTo(),
                rule.getRulePayload(),
                rule.getCreatedBy(),
                rule.getUpdatedBy(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private String normalizeFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private LocalDateTime parseDateTimeOptional(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(' ', 'T');
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must use format yyyy-MM-ddTHH:mm:ss");
        }
    }

    private String normalizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return "admin";
        }
        return operator.trim();
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
