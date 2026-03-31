package cn.openaipay.application.coupon.service.impl;

import cn.openaipay.application.coupon.command.ChangeCouponTemplateStatusCommand;
import cn.openaipay.application.coupon.command.CreateCouponTemplateCommand;
import cn.openaipay.application.coupon.command.IssueCouponCommand;
import cn.openaipay.application.coupon.command.RedeemCouponCommand;
import cn.openaipay.application.coupon.command.UpdateCouponTemplateCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.dto.CouponTemplateDTO;
import cn.openaipay.application.coupon.service.CouponService;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.coupon.model.CouponIssue;
import cn.openaipay.domain.coupon.model.CouponIssueStatus;
import cn.openaipay.domain.coupon.model.CouponSceneType;
import cn.openaipay.domain.coupon.model.CouponTemplate;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;
import cn.openaipay.domain.coupon.model.CouponValueType;
import cn.openaipay.domain.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * 优惠券应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CouponServiceImpl implements CouponService {

    /** CouponRepository组件 */
    private final CouponRepository couponRepository;
    /** AiPayIdGenerator组件 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AiPayBizTypeRegistry组件 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;

    public CouponServiceImpl(CouponRepository couponRepository,
                                        AiPayIdGenerator aiPayIdGenerator,
                                        AiPayBizTypeRegistry aiPayBizTypeRegistry) {
        this.couponRepository = couponRepository;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
    }

    /**
     * 创建模板信息。
     */
    @Override
    @Transactional
    public CouponTemplateDTO createTemplate(CreateCouponTemplateCommand command) {
        String templateCode = normalizeRequired(command.templateCode(), "templateCode");
        if (couponRepository.findTemplateByCode(templateCode).isPresent()) {
            throw new IllegalArgumentException("templateCode already exists: " + templateCode);
        }

        LocalDateTime now = LocalDateTime.now();
        String operator = normalizeRequired(command.operator(), "operator");

        CouponTemplate template = CouponTemplate.createNew(
                templateCode,
                normalizeRequired(command.templateName(), "templateName"),
                CouponSceneType.from(command.sceneType()),
                CouponValueType.from(command.valueType()),
                command.amount(),
                command.minAmount(),
                command.maxAmount(),
                defaultAmount(command.thresholdAmount(), zeroMoney()),
                normalizePositiveAmount(command.totalBudget(), "totalBudget"),
                requirePositive(command.totalStock(), "totalStock"),
                defaultPositive(command.perUserLimit(), 1, "perUserLimit"),
                parseDateTime(command.claimStartTime(), "claimStartTime"),
                parseDateTime(command.claimEndTime(), "claimEndTime"),
                parseDateTime(command.useStartTime(), "useStartTime"),
                parseDateTime(command.useEndTime(), "useEndTime"),
                normalizeRequired(command.fundingSource(), "fundingSource"),
                command.rulePayload(),
                command.initialStatus() == null ? CouponTemplateStatus.DRAFT : CouponTemplateStatus.from(command.initialStatus()),
                operator,
                now
        );
        return toTemplateDTO(couponRepository.saveTemplate(template));
    }

    /**
     * 更新模板信息。
     */
    @Override
    @Transactional
    public CouponTemplateDTO updateTemplate(UpdateCouponTemplateCommand command) {
        Long templateId = requirePositive(command.templateId(), "templateId");
        CouponTemplate template = couponRepository.findTemplateById(templateId)
                .orElseThrow(() -> new NoSuchElementException("coupon template not found: " + templateId));

        template.update(
                normalizeRequired(command.templateName(), "templateName"),
                CouponSceneType.from(command.sceneType()),
                CouponValueType.from(command.valueType()),
                command.amount(),
                command.minAmount(),
                command.maxAmount(),
                defaultAmount(command.thresholdAmount(), zeroMoney()),
                normalizePositiveAmount(command.totalBudget(), "totalBudget"),
                requirePositive(command.totalStock(), "totalStock"),
                defaultPositive(command.perUserLimit(), 1, "perUserLimit"),
                parseDateTime(command.claimStartTime(), "claimStartTime"),
                parseDateTime(command.claimEndTime(), "claimEndTime"),
                parseDateTime(command.useStartTime(), "useStartTime"),
                parseDateTime(command.useEndTime(), "useEndTime"),
                normalizeRequired(command.fundingSource(), "fundingSource"),
                command.rulePayload(),
                normalizeRequired(command.operator(), "operator"),
                LocalDateTime.now()
        );

        return toTemplateDTO(couponRepository.saveTemplate(template));
    }

    /**
     * 处理模板状态。
     */
    @Override
    @Transactional
    public CouponTemplateDTO changeTemplateStatus(ChangeCouponTemplateStatusCommand command) {
        Long templateId = requirePositive(command.templateId(), "templateId");
        CouponTemplate template = couponRepository.findTemplateById(templateId)
                .orElseThrow(() -> new NoSuchElementException("coupon template not found: " + templateId));
        template.changeStatus(
                CouponTemplateStatus.from(command.status()),
                normalizeRequired(command.operator(), "operator"),
                LocalDateTime.now()
        );
        return toTemplateDTO(couponRepository.saveTemplate(template));
    }

    /**
     * 获取模板信息。
     */
    @Override
    @Transactional(readOnly = true)
    public CouponTemplateDTO getTemplate(Long templateId) {
        Long id = requirePositive(templateId, "templateId");
        CouponTemplate template = couponRepository.findTemplateById(id)
                .orElseThrow(() -> new NoSuchElementException("coupon template not found: " + id));
        return toTemplateDTO(template);
    }

    /**
     * 查询模板信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CouponTemplateDTO> listTemplates(String sceneType, String status) {
        String normalizedSceneType = normalizeEnumFilter(sceneType);
        String normalizedStatus = normalizeEnumFilter(status);
        return couponRepository.findTemplates(normalizedSceneType, normalizedStatus)
                .stream()
                .map(this::toTemplateDTO)
                .toList();
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    @Transactional
    public CouponIssueDTO issueCoupon(IssueCouponCommand command) {
        Long templateId = requirePositive(command.templateId(), "templateId");
        Long userId = requirePositive(command.userId(), "userId");

        CouponTemplate template = couponRepository.findTemplateById(templateId)
                .orElseThrow(() -> new NoSuchElementException("coupon template not found: " + templateId));

        LocalDateTime now = LocalDateTime.now();
        if (!template.canClaim(now)) {
            throw new IllegalStateException("coupon template is not claimable now");
        }

        long userClaimCount = couponRepository.countUserClaims(templateId, userId);
        if (userClaimCount >= template.getPerUserLimit()) {
            throw new IllegalStateException("user claim limit exceeded");
        }

        Money couponAmount = template.nextCouponAmount();
        String channel = normalizeRequired(command.claimChannel(), "claimChannel");
        String operator = normalizeOptional(command.operator()) == null ? "system" : normalizeOptional(command.operator());

        CouponIssue issue = CouponIssue.issue(
                buildCouponNo(userId),
                templateId,
                userId,
                couponAmount,
                channel,
                normalizeOptional(command.businessNo()),
                now,
                template.getUseEndTime()
        );

        template.increaseClaimedCount(operator, now);
        couponRepository.saveTemplate(template);
        return toIssueDTO(couponRepository.saveIssue(issue));
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    @Transactional
    public CouponIssueDTO redeemCoupon(RedeemCouponCommand command) {
        String couponNo = normalizeRequired(command.couponNo(), "couponNo");
        CouponIssue issue = couponRepository.findIssueByCouponNo(couponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + couponNo));
        CouponTemplate template = couponRepository.findTemplateById(issue.getTemplateId())
                .orElseThrow(() -> new NoSuchElementException("coupon template not found: " + issue.getTemplateId()));

        issue.redeem(
                normalizeRequired(command.orderNo(), "orderNo"),
                LocalDateTime.now(),
                template.getUseStartTime(),
                template.getUseEndTime()
        );
        return toIssueDTO(couponRepository.saveIssue(issue));
    }

    /**
     * 查询用户优惠券信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CouponIssueDTO> listUserCoupons(Long userId) {
        Long uid = requirePositive(userId, "userId");
        return couponRepository.findIssuesByUserId(uid)
                .stream()
                .map(this::toIssueDTO)
                .toList();
    }

    /**
     * 获取业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public CouponIssueDTO getIssue(String couponNo) {
        String normalizedCouponNo = normalizeRequired(couponNo, "couponNo");
        CouponIssue issue = couponRepository.findIssueByCouponNo(normalizedCouponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + normalizedCouponNo));
        return toIssueDTO(issue);
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CouponIssueDTO> listIssues(Long templateId, Long userId, String status, Integer limit) {
        Long normalizedTemplateId = normalizeOptionalPositive(templateId, "templateId");
        Long normalizedUserId = normalizeOptionalPositive(userId, "userId");
        String normalizedStatus = normalizeEnumFilter(status);
        if (normalizedStatus != null) {
            normalizedStatus = CouponIssueStatus.from(normalizedStatus).name();
        }
        int resolvedLimit = resolveLimit(limit);
        return couponRepository.findIssues(normalizedTemplateId, normalizedUserId, normalizedStatus, resolvedLimit)
                .stream()
                .map(this::toIssueDTO)
                .toList();
    }

    private CouponTemplateDTO toTemplateDTO(CouponTemplate template) {
        return new CouponTemplateDTO(
                template.getTemplateId(),
                template.getTemplateCode(),
                template.getTemplateName(),
                template.getSceneType().name(),
                template.getValueType().name(),
                template.getAmount(),
                template.getMinAmount(),
                template.getMaxAmount(),
                template.getThresholdAmount(),
                template.getTotalBudget(),
                template.getTotalStock(),
                template.getClaimedCount(),
                template.getPerUserLimit(),
                template.getClaimStartTime(),
                template.getClaimEndTime(),
                template.getUseStartTime(),
                template.getUseEndTime(),
                template.getFundingSource(),
                template.getRulePayload(),
                template.getStatus().name(),
                template.getCreatedBy(),
                template.getUpdatedBy(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private CouponIssueDTO toIssueDTO(CouponIssue issue) {
        return new CouponIssueDTO(
                issue.getIssueId(),
                issue.getCouponNo(),
                issue.getTemplateId(),
                issue.getUserId(),
                issue.getCouponAmount(),
                issue.getStatus().name(),
                issue.getClaimChannel(),
                issue.getBusinessNo(),
                issue.getOrderNo(),
                issue.getBizOrderNo(),
                issue.getTradeOrderNo(),
                issue.getPayOrderNo(),
                issue.getClaimedAt(),
                issue.getExpireAt(),
                issue.getUsedAt(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    private String buildCouponNo(Long userId) {
        return aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_COUPON,
                aiPayBizTypeRegistry.couponIssueBizType(),
                String.valueOf(requirePositive(userId, "userId"))
        );
    }

    private LocalDateTime parseDateTime(String raw, String field) {
        String normalized = normalizeRequired(raw, field).replace(' ', 'T');
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must use format yyyy-MM-ddTHH:mm:ss");
        }
    }

    private Money defaultAmount(Money amount, Money fallback) {
        return amount == null ? fallback : amount;
    }

    private Money normalizePositiveAmount(Money amount, String field) {
        if (amount == null || amount.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return amount;
    }

    private Integer defaultPositive(Integer value, int fallback, String field) {
        if (value == null) {
            return fallback;
        }
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private Long normalizeOptionalPositive(Long value, String field) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private Integer requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private String normalizeRequired(String raw, String field) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEnumFilter(String raw) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        if (limit <= 0 || limit > 200) {
            throw new IllegalArgumentException("limit must be between 1 and 200");
        }
        return limit;
    }

    private Money zeroMoney() {
        return Money.zero(CurrencyUnit.of("CNY"));
    }
}
